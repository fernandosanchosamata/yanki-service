package com.ntt.yanki.service.impl;

import com.ntt.yanki.client.AccountClient;
import com.ntt.yanki.model.dto.YankiTransactionRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.model.entity.YankiTransaction;
import com.ntt.yanki.repository.WalletRepository;
import com.ntt.yanki.repository.YankiTransactionRepository;
import com.ntt.yanki.service.YankiTransactionService;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;

@Slf4j
@Service
@RequiredArgsConstructor
public class YankiTransactionServiceImpl implements YankiTransactionService {

  private final WalletRepository walletRepository;
  private final YankiTransactionRepository transactionRepository;
  private final AccountClient accountClient;
  private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

  @Override
  public Single<YankiTransaction> executeTransaction(YankiTransactionRequest request) {
    log.info("Iniciando transaccion Yanki. amount={}", request.getAmount());
    return walletRepository
        .findByPhoneNumber(request.getSourcePhoneNumber())
        .switchIfEmpty(Single.error(new IllegalArgumentException("Celular origen no registrado.")))
        .flatMap(
            sourceWallet ->
                walletRepository
                    .findByPhoneNumber(request.getTargetPhoneNumber())
                    .switchIfEmpty(
                        Single.error(
                            new IllegalArgumentException("Celular destino no registrado.")))
                    .flatMap(
                        targetWallet -> {
                          YankiTransaction transaction =
                              YankiTransaction.builder()
                                  .sourcePhoneNumber(request.getSourcePhoneNumber())
                                  .targetPhoneNumber(request.getTargetPhoneNumber())
                                  .amount(request.getAmount())
                                  .status("PROCESSING")
                                  .timestamp(LocalDateTime.now())
                                  .build();

                          return transactionRepository
                              .save(transaction)
                              .flatMap(
                                  savedTransaction ->
                                      executeSourceDeduction(sourceWallet, request.getAmount())
                                          .flatMap(
                                              ignored ->
                                                  executeTargetDeposit(
                                                      targetWallet, request.getAmount()))
                                          .flatMap(
                                              ignored -> {
                                                savedTransaction.setStatus("COMPLETED");
                                                return transactionRepository
                                                    .save(savedTransaction)
                                                    .doOnSuccess(
                                                        saved ->
                                                            log.info(
                                                                "Transaccion Yanki completada."
                                                                    + " transactionId={}",
                                                                saved.getId()));
                                              })
                                          .flatMap(this::publishTransactionEvent)
                                          .onErrorResumeNext(
                                              e -> {
                                                log.warn(
                                                    "Transaccion Yanki fallida. transactionId={},"
                                                        + " error={}",
                                                    savedTransaction.getId(),
                                                    e.getMessage());
                                                savedTransaction.setStatus("FAILED");
                                                return transactionRepository
                                                    .save(savedTransaction)
                                                    .flatMap(
                                                        ignored ->
                                                            Single.error(
                                                                new IllegalArgumentException(
                                                                    "Error en transaccion: "
                                                                        + e.getMessage())));
                                              }));
                        }));
  }

  private Single<Boolean> executeSourceDeduction(Wallet source, BigDecimal amount) {
    if (source.getLinkedAccountId() != null) {
      log.info("Descontando desde la cuenta principal del banco: {}", source.getLinkedAccountId());
      return accountClient
          .withdrawFromAccount(source.getLinkedAccountId(), amount)
          .map(res -> true);
    } else {
      if (source.getBalance().compareTo(amount) < 0) {
        log.warn("Descuento Yanki rechazado por saldo insuficiente.");
        return Single.error(new IllegalArgumentException("Saldo Yanki insuficiente."));
      }
      source.setBalance(source.getBalance().subtract(amount));
      return walletRepository
          .save(source)
          .doOnSuccess(wallet -> log.debug("Saldo Yanki descontado. walletId={}", wallet.getId()))
          .map(w -> true);
    }
  }

  private Single<Boolean> executeTargetDeposit(Wallet target, BigDecimal amount) {
    if (target.getLinkedAccountId() != null) {
      log.info("Depositando a la cuenta principal del banco: {}", target.getLinkedAccountId());
      return accountClient.depositToAccount(target.getLinkedAccountId(), amount).map(res -> true);
    } else {
      target.setBalance(target.getBalance().add(amount));
      return walletRepository
          .save(target)
          .doOnSuccess(wallet -> log.debug("Saldo Yanki depositado. walletId={}", wallet.getId()))
          .map(w -> true);
    }
  }

  private Single<YankiTransaction> publishTransactionEvent(YankiTransaction transaction) {
    // Enviar a transactions-executed-topic simulando la estructura esperada
    String eventTemplate =
        "{\"transactionId\":\"%s\", \"type\":\"YANKI_TRANSFER\", "
            + "\"sourceId\":\"%s\", \"targetId\":\"%s\", \"amount\":%s}";
    String eventJson =
        String.format(
            eventTemplate,
            transaction.getId(),
            transaction.getSourcePhoneNumber(),
            transaction.getTargetPhoneNumber(),
            transaction.getAmount());

    return RxJava3Adapter.monoToSingle(
        kafkaTemplate
            .send("transactions-executed-topic", transaction.getId(), eventJson)
            .doOnSuccess(result -> log.info("Evento Yanki publicado en Kafka."))
            .thenReturn(transaction));
  }
}
