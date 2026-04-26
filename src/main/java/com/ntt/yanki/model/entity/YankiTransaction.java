package com.ntt.yanki.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_transactions")
public class YankiTransaction {

  @Id private String id;

  private String sourcePhoneNumber;
  private String targetPhoneNumber;
  private BigDecimal amount;
  private String status;

  private LocalDateTime timestamp;
}
