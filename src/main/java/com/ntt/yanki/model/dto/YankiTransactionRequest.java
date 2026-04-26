package com.ntt.yanki.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiTransactionRequest {

  @NotBlank(message = "El celular origen es obligatorio")
  private String sourcePhoneNumber;

  @NotBlank(message = "El celular destino es obligatorio")
  private String targetPhoneNumber;

  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
  private BigDecimal amount;
}
