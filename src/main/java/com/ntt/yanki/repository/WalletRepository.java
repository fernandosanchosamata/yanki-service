package com.ntt.yanki.repository;

import com.ntt.yanki.model.entity.Wallet;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends RxJava3CrudRepository<Wallet, String> {

  Maybe<Wallet> findByPhoneNumber(String phoneNumber);
}
