package io.inventi.bankStatementService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatementDto {
    @NotBlank
    private String accountNumber;

    @NotBlank
    private String operationDateTime;

    @NotBlank
    private String beneficiary;

    private String comment;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;
}
