package com.ntt.yanki.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.rxjava.RxJava3Adapter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountClient {

  private final WebClient webClient;

  @Value("${yanki.account-service.url}")
  private String accountServiceUrl;

  public Single<String> getMainAccountIdByCardNumber(String cardNumber) {
    log.debug("Consultando cuenta principal por tarjeta de debito.");
    return RxJava3Adapter.monoToSingle(
            webClient
                .get()
                .uri(accountServiceUrl + "/debit-cards/" + cardNumber)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.get("mainAccountId").asText()))
        .doOnSuccess(accountId -> log.debug("Cuenta principal obtenida. accountId={}", accountId))
        .doOnError(
            error -> log.error("Error consultando tarjeta de debito: {}", error.getMessage()));
  }

  public Single<JsonNode> withdrawFromAccount(String accountId, java.math.BigDecimal amount) {
    log.debug("Solicitando retiro en Account Service. accountId={}, amount={}", accountId, amount);
    return RxJava3Adapter.monoToSingle(
            webClient
                .post()
                .uri(accountServiceUrl + "/" + accountId + "/withdraw")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(JsonNode.class))
        .doOnSuccess(
            response -> log.debug("Retiro completado en Account Service. accountId={}", accountId))
        .doOnError(
            error ->
                log.error(
                    "Error retirando en Account Service. accountId={}, error={}",
                    accountId,
                    error.getMessage()));
  }

  public Single<JsonNode> depositToAccount(String accountId, java.math.BigDecimal amount) {
    log.debug(
        "Solicitando deposito en Account Service. accountId={}, amount={}", accountId, amount);
    return RxJava3Adapter.monoToSingle(
            webClient
                .post()
                .uri(accountServiceUrl + "/" + accountId + "/deposit")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(JsonNode.class))
        .doOnSuccess(
            response ->
                log.debug("Deposito completado en Account Service. accountId={}", accountId))
        .doOnError(
            error ->
                log.error(
                    "Error depositando en Account Service. accountId={}, error={}",
                    accountId,
                    error.getMessage()));
  }
}
