package com.ntt.yanki.service.impl;

import com.ntt.yanki.client.AccountClient;
import com.ntt.yanki.model.dto.WalletCreationRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.repository.WalletRepository;
import com.ntt.yanki.service.WalletService;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final AccountClient accountClient;

  @Override
  public Single<Wallet> createWallet(WalletCreationRequest request) {
    return walletRepository
        .findByPhoneNumber(request.getPhoneNumber())
        .isEmpty()
        .flatMap(
            isEmpty -> {
              if (!isEmpty) {
                return Single.error(
                    new IllegalArgumentException("El numero de celular ya esta registrado."));
              }

              Wallet wallet =
                  Wallet.builder()
                      .documentType(request.getDocumentType())
                      .documentNumber(request.getDocumentNumber())
                      .phoneNumber(request.getPhoneNumber())
                      .email(request.getEmail())
                      .imei(request.getImei())
                      .balance(BigDecimal.ZERO)
                      .createdAt(LocalDateTime.now())
                      .build();

              if (request.getAssociatedDebitCard() != null
                  && !request.getAssociatedDebitCard().isBlank()) {
                wallet.setAssociatedDebitCard(request.getAssociatedDebitCard());
                return accountClient
                    .getMainAccountIdByCardNumber(request.getAssociatedDebitCard())
                    .flatMap(
                        accountId -> {
                          wallet.setLinkedAccountId(accountId);
                          return walletRepository.save(wallet);
                        })
                    .onErrorResumeNext(
                        e ->
                            Single.error(
                                new IllegalArgumentException(
                                    "No se pudo validar la tarjeta de debito: " + e.getMessage())));
              } else {
                return walletRepository.save(wallet);
              }
            });
  }

  @Override
  public Single<Wallet> getWalletByPhoneNumber(String phoneNumber) {
    return walletRepository
        .findByPhoneNumber(phoneNumber)
        .switchIfEmpty(Single.error(new IllegalArgumentException("Monedero no encontrado.")));
  }
}
