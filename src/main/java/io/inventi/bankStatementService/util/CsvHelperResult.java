package io.inventi.bankStatementService.util;

import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.model.BankStatement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CsvHelperResult {
    private List<BankStatement> validBankStatements;
    private List<ResponseDto.RowError> errors;
}
