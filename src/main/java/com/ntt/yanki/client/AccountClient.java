package com.ntt.yanki.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.rxjava.RxJava3Adapter;

@Component
@RequiredArgsConstructor
public class AccountClient {

  private final WebClient webClient;

  @Value("${yanki.account-service.url}")
  private String accountServiceUrl;

  public Single<String> getMainAccountIdByCardNumber(String cardNumber) {
    return RxJava3Adapter.monoToSingle(
        webClient
            .get()
            .uri(accountServiceUrl + "/debit-cards/" + cardNumber)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(node -> node.get("mainAccountId").asText()));
  }

  public Single<JsonNode> withdrawFromAccount(String accountId, java.math.BigDecimal amount) {
    return RxJava3Adapter.monoToSingle(
        webClient
            .post()
            .uri(accountServiceUrl + "/" + accountId + "/withdraw")
            .bodyValue(Map.of("amount", amount))
            .retrieve()
            .bodyToMono(JsonNode.class));
  }

  public Single<JsonNode> depositToAccount(String accountId, java.math.BigDecimal amount) {
    return RxJava3Adapter.monoToSingle(
        webClient
            .post()
            .uri(accountServiceUrl + "/" + accountId + "/deposit")
            .bodyValue(Map.of("amount", amount))
            .retrieve()
            .bodyToMono(JsonNode.class));
  }
}
