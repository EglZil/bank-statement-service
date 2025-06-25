package io.inventi.bankStatementService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String accountNumber;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime operationDate;

    @NotBlank
    @Column(nullable = false)
    private String beneficiary;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Column(nullable = false)
    private String currency;

    private String comment;
}
