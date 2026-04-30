package com.ntt.yanki.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ntt.yanki.client.AccountClient;
import com.ntt.yanki.model.dto.WalletCreationRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.repository.WalletRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

  @Mock private WalletRepository walletRepository;
  @Mock private AccountClient accountClient;

  private WalletServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new WalletServiceImpl(walletRepository, accountClient);
  }

  @Test
  void createWalletPersistsWalletWithoutDebitCard() {
    WalletCreationRequest request = walletRequest();
    when(walletRepository.findByPhoneNumber("999888777")).thenReturn(Maybe.empty());
    when(walletRepository.save(any(Wallet.class)))
        .thenAnswer(
            invocation -> {
              Wallet wallet = invocation.getArgument(0);
              wallet.setId("wallet-1");
              return Single.just(wallet);
            });

    Wallet wallet = service.createWallet(request).blockingGet();

    assertThat(wallet.getId()).isEqualTo("wallet-1");
    assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(wallet.getLinkedAccountId()).isNull();
    verify(accountClient, never()).getMainAccountIdByCardNumber(any());
  }

  @Test
  void createWalletLinksDebitCardMainAccount() {
    WalletCreationRequest request = walletRequest();
    request.setAssociatedDebitCard("4555-6666-7777-8888");
    when(walletRepository.findByPhoneNumber("999888777")).thenReturn(Maybe.empty());
    when(accountClient.getMainAccountIdByCardNumber("4555-6666-7777-8888"))
        .thenReturn(Single.just("account-1"));
    when(walletRepository.save(any(Wallet.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    Wallet wallet = service.createWallet(request).blockingGet();

    assertThat(wallet.getAssociatedDebitCard()).isEqualTo("4555-6666-7777-8888");
    assertThat(wallet.getLinkedAccountId()).isEqualTo("account-1");
  }

  @Test
  void createWalletRejectsDuplicatedPhoneNumber() {
    WalletCreationRequest request = walletRequest();
    when(walletRepository.findByPhoneNumber("999888777"))
        .thenReturn(Maybe.just(Wallet.builder().build()));

    var observer = service.createWallet(request).test();

    observer.assertError(error -> error.getMessage().contains("celular ya esta registrado"));
    verify(walletRepository, never()).save(any());
  }

  private WalletCreationRequest walletRequest() {
    return WalletCreationRequest.builder()
        .documentType("DNI")
        .documentNumber("12345678")
        .phoneNumber("999888777")
        .imei("IMEI-123")
        .email("user@test.com")
        .build();
  }
}
