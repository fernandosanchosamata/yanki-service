package com.ntt.yanki.controller;

import com.ntt.yanki.model.dto.WalletCreationRequest;
import com.ntt.yanki.model.dto.YankiTransactionRequest;
import com.ntt.yanki.model.entity.Wallet;
import com.ntt.yanki.model.entity.YankiTransaction;
import com.ntt.yanki.service.WalletService;
import com.ntt.yanki.service.YankiTransactionService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/yanki")
@RequiredArgsConstructor
@Slf4j
public class YankiController {

  private final WalletService walletService;
  private final YankiTransactionService transactionService;

  @PostMapping("/wallets")
  @ResponseStatus(HttpStatus.CREATED)
  public Single<ResponseEntity<Wallet>> createWallet(
      @Valid @RequestBody WalletCreationRequest request) {
    log.info("Solicitud recibida para crear wallet.");
    return walletService
        .createWallet(request)
        .doOnSuccess(wallet -> log.info("Wallet creada. walletId={}", wallet.getId()))
        .doOnError(error -> log.warn("No se pudo crear wallet: {}", error.getMessage()))
        .map(wallet -> ResponseEntity.status(HttpStatus.CREATED).body(wallet));
  }

  @PostMapping("/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  public Single<ResponseEntity<YankiTransaction>> executeTransaction(
      @Valid @RequestBody YankiTransactionRequest request) {
    log.info("Solicitud recibida para transaccion Yanki.");
    return transactionService
        .executeTransaction(request)
        .doOnSuccess(
            transaction ->
                log.info(
                    "Transaccion Yanki procesada. transactionId={}, status={}",
                    transaction.getId(),
                    transaction.getStatus()))
        .doOnError(
            error -> log.warn("No se pudo procesar transaccion Yanki: {}", error.getMessage()))
        .map(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(transaction));
  }

  @GetMapping("/wallets/{phoneNumber}")
  public Single<ResponseEntity<Wallet>> getWallet(@PathVariable String phoneNumber) {
    log.info("Solicitud recibida para consultar wallet.");
    return walletService
        .getWalletByPhoneNumber(phoneNumber)
        .doOnSuccess(wallet -> log.info("Wallet consultada. walletId={}", wallet.getId()))
        .doOnError(error -> log.warn("No se pudo consultar wallet: {}", error.getMessage()))
        .map(ResponseEntity::ok);
  }
}
