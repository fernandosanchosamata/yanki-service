package com.ntt.yanki.repository;

import com.ntt.yanki.model.entity.YankiTransaction;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YankiTransactionRepository
    extends RxJava3CrudRepository<YankiTransaction, String> {}
