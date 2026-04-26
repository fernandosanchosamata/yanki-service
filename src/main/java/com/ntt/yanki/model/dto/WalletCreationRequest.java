package com.ntt.yanki.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreationRequest {

  @NotBlank(message = "El tipo de documento es obligatorio")
  private String documentType;

  @NotBlank(message = "El numero de documento es obligatorio")
  private String documentNumber;

  @NotBlank(message = "El numero de celular es obligatorio")
  private String phoneNumber;

  @NotBlank(message = "El IMEI es obligatorio por seguridad")
  private String imei;

  @NotBlank(message = "El correo es obligatorio")
  private String email;

  private String associatedDebitCard;
}
