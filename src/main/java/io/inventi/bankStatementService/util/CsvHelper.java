package io.inventi.bankStatementService.util;

import io.inventi.bankStatementService.dto.BankStatementDto;
import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.model.BankStatement;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CsvHelper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ACCOUNT_NUMBER = "Account number";
    private static final String OPERATION_DATE = "Operation date/time";
    private static final String BENEFICIARY = "Beneficiary";
    private static final String COMMENT = "Comment";
    private static final String AMOUNT = "Amount";
    private static final String CURRENCY = "Currency";

    public static CsvHelperResult validateAndParseCsv(MultipartFile file, Validator validator) {
        List<BankStatement> validStatements = new ArrayList<>();
        List<ResponseDto.RowError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat format = configureCsvFormat();
            CSVParser parser = format.parse(reader);

            for (CSVRecord record : parser) {
                if (record == null) {
                    continue;
                }
                long rowNum = record.getRecordNumber();

                try {
                    BankStatementDto dto = createBankStatementDto(record);

                    Set<ConstraintViolation<BankStatementDto>> violations = validator.validate(dto);

                    if (!violations.isEmpty()) {
                        String message = violations.stream()
                                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                                .collect(Collectors.joining("; "));
                        errors.add(new ResponseDto.RowError(rowNum, message));
                        continue;
                    }

                    BankStatement statement = createBankStatement(record);
                    validStatements.add(statement);
                } catch (Exception e) {
                    errors.add(new ResponseDto.RowError(rowNum, e.getMessage()));
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage());
        }

        return new CsvHelperResult(validStatements, errors);
    }

    private static CSVFormat configureCsvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get();
    }

    private static BankStatementDto createBankStatementDto(CSVRecord record) {
        return BankStatementDto.builder()
                .accountNumber(record.get(ACCOUNT_NUMBER))
                .operationDateTime(record.get(OPERATION_DATE))
                .beneficiary(record.get(BENEFICIARY))
                .comment(record.get(COMMENT))
                .amount(new BigDecimal(record.get(AMOUNT)))
                .currency(record.get(CURRENCY))
                .build();
    }

    private static BankStatement createBankStatement(CSVRecord record) {
        return BankStatement.builder()
                .accountNumber(record.get(ACCOUNT_NUMBER))
                .operationDate(LocalDateTime.parse(record.get(OPERATION_DATE), FORMATTER))
                .beneficiary(record.get(BENEFICIARY))
                .comment(record.get(COMMENT))
                .amount(new BigDecimal(record.get(AMOUNT)))
                .currency(record.get(CURRENCY))
                .build();
    }
}
