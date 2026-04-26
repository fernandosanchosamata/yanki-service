package com.ntt.yanki.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wallets")
public class Wallet {

  @Id private String id;

  @Indexed(unique = true)
  private String phoneNumber;

  private String documentType;
  private String documentNumber;
  private String email;
  private String imei;

  private String associatedDebitCard;

  // Cuenta principal recuperada del AccountService al vincular tarjeta
  private String linkedAccountId;

  private BigDecimal balance;

  private LocalDateTime createdAt;
}
