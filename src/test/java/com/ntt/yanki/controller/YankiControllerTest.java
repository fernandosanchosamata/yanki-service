package com.ntt.yanki.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ntt.yanki.model.dto.WalletCreationRequest;
import com.ntt.yanki.model.dto.YankiTransactionRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.model.entity.YankiTransaction;
import com.ntt.yanki.service.WalletService;
import com.ntt.yanki.service.YankiTransactionService;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class YankiControllerTest {

  @Mock private WalletService walletService;

  @Mock private YankiTransactionService transactionService;

  @InjectMocks private YankiController controller;

  @Test
  void createWalletReturnsCreatedResponse() {
    WalletCreationRequest request =
        WalletCreationRequest.builder()
            .documentType("DNI")
            .documentNumber("12345678")
            .phoneNumber("999888777")
            .imei("IMEI-123")
            .email("user@test.com")
            .build();
    Wallet wallet = wallet();
    when(walletService.createWallet(request)).thenReturn(Single.just(wallet));

    var result = controller.createWallet(request).blockingGet();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(wallet);
  }

  @Test
  void executeTransactionReturnsCreatedResponse() {
    YankiTransactionRequest request =
        YankiTransactionRequest.builder()
            .sourcePhoneNumber("999888777")
            .targetPhoneNumber("999111222")
            .amount(BigDecimal.valueOf(25))
            .build();
    YankiTransaction transaction =
        YankiTransaction.builder()
            .id("tx-1")
            .sourcePhoneNumber("999888777")
            .targetPhoneNumber("999111222")
            .amount(BigDecimal.valueOf(25))
            .status("COMPLETED")
            .build();
    when(transactionService.executeTransaction(request)).thenReturn(Single.just(transaction));

    var result = controller.executeTransaction(request).blockingGet();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getBody()).isEqualTo(transaction);
    verify(transactionService).executeTransaction(request);
  }

  @Test
  void getWalletReturnsOkResponse() {
    Wallet wallet = wallet();
    when(walletService.getWalletByPhoneNumber("999888777")).thenReturn(Single.just(wallet));

    var result = controller.getWallet("999888777").blockingGet();

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(wallet);
  }

  private Wallet wallet() {
    return Wallet.builder()
        .id("wallet-1")
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("999888777")
        .imei("IMEI-123")
        .email("user@test.com")
        .balance(BigDecimal.ZERO)
        .build();
  }
}
