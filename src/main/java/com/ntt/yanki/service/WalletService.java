package com.ntt.yanki.service;

import com.ntt.yanki.model.dto.WalletCreationRequest;
import com.ntt.yanki.model.entity.Wallet;
import io.reactivex.rxjava3.core.Single;

public interface WalletService {

  Single<Wallet> createWallet(WalletCreationRequest request);

  Single<Wallet> getWalletByPhoneNumber(String phoneNumber);
}
