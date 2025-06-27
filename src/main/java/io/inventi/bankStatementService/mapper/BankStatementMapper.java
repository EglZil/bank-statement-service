package io.inventi.bankStatementService.mapper;

import io.inventi.bankStatementService.dto.BankStatementDto;
import io.inventi.bankStatementService.model.BankStatement;
import org.springframework.stereotype.Component;

@Component
public class BankStatementMapper {
    public BankStatementDto entityToDto(BankStatement entity) {
        if (entity == null) {
            return null;
        }
        BankStatementDto dto = new BankStatementDto();
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setOperationDateTime(entity.getOperationDate().toString());
        dto.setAmount(entity.getAmount());
        dto.setBeneficiary(entity.getBeneficiary());
        dto.setComment(entity.getComment());
        dto.setCurrency(entity.getCurrency());
        return dto;
    }
}
