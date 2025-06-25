package io.inventi.bankStatementService.service;

import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import io.inventi.bankStatementService.util.CsvHelper;
import io.inventi.bankStatementService.util.CsvHelperResult;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BankStatementService {
    private final BankStatementRepository repository;
    private final Validator validator;

    public BankStatementService(BankStatementRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public ResponseDto importCsv(MultipartFile file) {
        CsvHelperResult result = CsvHelper.validateAndParseCsv(file, validator);
        repository.saveAll(result.getValidBankStatements());

        return ResponseDto.builder()
                .imported(result.getValidBankStatements().size())
                .skipped(result.getErrors().size())
                .errors(result.getErrors())
                .build();
    }
}
