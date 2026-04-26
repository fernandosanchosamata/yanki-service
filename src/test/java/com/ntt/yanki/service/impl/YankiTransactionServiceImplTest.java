package com.ntt.yanki.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.ntt.yanki.client.AccountClient;
import com.ntt.yanki.model.dto.YankiTransactionRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.model.entity.YankiTransaction;
import com.ntt.yanki.repository.WalletRepository;
import com.ntt.yanki.repository.YankiTransactionRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class YankiTransactionServiceImplTest {

  @Mock private WalletRepository walletRepository;
  @Mock private YankiTransactionRepository transactionRepository;
  @Mock private AccountClient accountClient;
  @Mock private JsonNode accountResponse;
  @Mock private ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

  private YankiTransactionServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new YankiTransactionServiceImpl(
            walletRepository, transactionRepository, accountClient, kafkaTemplate);
  }

  @Test
  void executeTransactionMovesBalanceBetweenLocalWalletsAndPublishesEvent() {
    Wallet source = wallet("source", "999888777", BigDecimal.valueOf(100));
    Wallet target = wallet("target", "999111222", BigDecimal.TEN);
    YankiTransactionRequest request = transactionRequest();
    when(walletRepository.findByPhoneNumber("999888777")).thenReturn(Maybe.just(source));
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(target));
    when(transactionRepository.save(any(YankiTransaction.class)))
        .thenAnswer(
            invocation -> {
              YankiTransaction transaction = invocation.getArgument(0);
              if (transaction.getId() == null) {
                transaction.setId("yanki-tx-1");
              }
              return Single.just(transaction);
            });
    when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(Mono.empty());

    YankiTransaction transaction = service.executeTransaction(request).blockingGet();

    assertThat(transaction.getStatus()).isEqualTo("COMPLETED");
    assertThat(source.getBalance()).isEqualByComparingTo("75");
    assertThat(target.getBalance()).isEqualByComparingTo("35");
    verify(kafkaTemplate).send(anyString(), anyString(), any());
  }

  @Test
  void executeTransactionUsesLinkedBankAccountsWhenWalletsAreLinked() {
    Wallet source = wallet("source", "999888777", BigDecimal.ZERO);
    source.setLinkedAccountId("account-1");
    Wallet target = wallet("target", "999111222", BigDecimal.ZERO);
    target.setLinkedAccountId("account-2");
    YankiTransactionRequest request = transactionRequest();
    when(walletRepository.findByPhoneNumber("999888777")).thenReturn(Maybe.just(source));
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(target));
    when(transactionRepository.save(any(YankiTransaction.class)))
        .thenAnswer(
            invocation -> {
              YankiTransaction transaction = invocation.getArgument(0);
              transaction.setId("yanki-tx-1");
              return Single.just(transaction);
            });
    when(accountClient.withdrawFromAccount("account-1", BigDecimal.valueOf(25)))
        .thenReturn(Single.just(accountResponse));
    when(accountClient.depositToAccount("account-2", BigDecimal.valueOf(25)))
        .thenReturn(Single.just(accountResponse));
    when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(Mono.empty());

    YankiTransaction transaction = service.executeTransaction(request).blockingGet();

    assertThat(transaction.getStatus()).isEqualTo("COMPLETED");
    verify(accountClient).withdrawFromAccount("account-1", BigDecimal.valueOf(25));
    verify(accountClient).depositToAccount("account-2", BigDecimal.valueOf(25));
  }

  @Test
  void executeTransactionFailsWhenSourceWalletHasInsufficientBalance() {
    Wallet source = wallet("source", "999888777", BigDecimal.TEN);
    Wallet target = wallet("target", "999111222", BigDecimal.ZERO);
    when(walletRepository.findByPhoneNumber("999888777")).thenReturn(Maybe.just(source));
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(target));
    when(transactionRepository.save(any(YankiTransaction.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    var observer = service.executeTransaction(transactionRequest()).test();

    observer.assertError(error -> error.getMessage().contains("Saldo Yanki insuficiente"));
  }

  private YankiTransactionRequest transactionRequest() {
    return YankiTransactionRequest.builder()
        .sourcePhoneNumber("999888777")
        .targetPhoneNumber("999111222")
        .amount(BigDecimal.valueOf(25))
        .build();
  }

  private Wallet wallet(String id, String phoneNumber, BigDecimal balance) {
    return Wallet.builder().id(id).phoneNumber(phoneNumber).balance(balance).build();
  }
}
