package com.ntt.yanki.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class AccountClientTest {

  @Mock private WebClient webClient;
  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
  @Mock private WebClient.RequestBodySpec requestBodySpec;
  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private WebClient.ResponseSpec responseSpec;

  private AccountClient client;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    client = new AccountClient(webClient);
    ReflectionTestUtils.setField(client, "accountServiceUrl", "http://account/api/v1/accounts");
    objectMapper = new ObjectMapper();
  }

  @Test
  void getMainAccountIdByCardNumberReturnsMainAccountId() throws Exception {
    JsonNode response = objectMapper.readTree("{\"mainAccountId\":\"account-1\"}");
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri("http://account/api/v1/accounts/debit-cards/4555"))
        .thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(response));

    String result = client.getMainAccountIdByCardNumber("4555").blockingGet();

    assertThat(result).isEqualTo("account-1");
  }

  @Test
  void withdrawFromAccountPostsAmountToWithdrawEndpoint() throws Exception {
    JsonNode response = objectMapper.readTree("{\"id\":\"account-1\"}");
    stubPost("http://account/api/v1/accounts/account-1/withdraw", response);

    JsonNode result = client.withdrawFromAccount("account-1", BigDecimal.TEN).blockingGet();

    assertThat(result.get("id").asText()).isEqualTo("account-1");
    verify(requestBodySpec).bodyValue(anyMap());
  }

  @Test
  void depositToAccountPostsAmountToDepositEndpoint() throws Exception {
    JsonNode response = objectMapper.readTree("{\"id\":\"account-1\"}");
    stubPost("http://account/api/v1/accounts/account-1/deposit", response);

    JsonNode result = client.depositToAccount("account-1", BigDecimal.ONE).blockingGet();

    assertThat(result.get("id").asText()).isEqualTo("account-1");
  }

  private void stubPost(String uri, JsonNode response) {
    when(webClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec);
    when(requestBodySpec.bodyValue(anyMap())).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(response));
  }
}
