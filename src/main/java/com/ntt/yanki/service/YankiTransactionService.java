package com.ntt.yanki.service;

import com.ntt.yanki.model.dto.YankiTransactionRequest;
import com.ntt.yanki.model.entity.YankiTransaction;
import io.reactivex.rxjava3.core.Single;

public interface YankiTransactionService {

  Single<YankiTransaction> executeTransaction(YankiTransactionRequest request);
}
