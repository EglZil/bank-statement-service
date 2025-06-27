package io.inventi.bankStatementService.service;

import io.inventi.bankStatementService.dto.BankStatementDto;
import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.mapper.BankStatementMapper;
import io.inventi.bankStatementService.model.BankStatement;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import io.inventi.bankStatementService.util.CsvHelper;
import io.inventi.bankStatementService.util.CsvHelperResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BankStatementService {
    private final BankStatementRepository bankStatementRepository;
    private final BankStatementMapper bankStatementMapper;
    private final Validator validator;

    public BankStatementService(BankStatementRepository bankStatementRepository,
                                Validator validator,
                                BankStatementMapper bankStatementMapper) {
        this.bankStatementRepository = bankStatementRepository;
        this.validator = validator;
        this.bankStatementMapper = bankStatementMapper;
    }

    public ResponseDto importCsv(MultipartFile file) {
        CsvHelperResult result = CsvHelper.validateAndParseCsv(file, validator);
        bankStatementRepository.saveAll(result.getValidBankStatements());

        return ResponseDto.builder()
                .imported(result.getValidBankStatements().size())
                .skipped(result.getErrors().size())
                .errors(result.getErrors())
                .build();
    }

    public List<BankStatementDto> findBankStatements(List<String> accountNumbers, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : LocalDate.of(1970, 1, 1).atStartOfDay();
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : LocalDateTime.now().plusYears(100);
        List<BankStatement> bankStatements;

        if (dateFrom != null || dateTo != null) {
            bankStatements = bankStatementRepository.findByAccountNumberInAndOperationDateBetween(accountNumbers, from, to);
        } else {
            bankStatements = bankStatementRepository.findByAccountNumberIn(accountNumbers);
        }

        return mapToBankStatementDto(bankStatements);
    }

    private List<BankStatementDto> mapToBankStatementDto(List<BankStatement> bankStatements) {
        if (bankStatements == null || bankStatements.isEmpty()) {
            return new ArrayList<>();
        }
        return bankStatements
                .stream()
                .map(bankStatementMapper::entityToDto)
                .toList();
    }
}
