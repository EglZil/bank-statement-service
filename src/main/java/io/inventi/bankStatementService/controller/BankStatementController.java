package io.inventi.bankStatementService.controller;

import io.inventi.bankStatementService.dto.BankStatementDto;
import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.service.BankStatementService;
import io.inventi.bankStatementService.util.CsvHelper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.apache.tomcat.util.http.fileupload.FileUploadBase.MULTIPART_FORM_DATA;

@RestController
@RequestMapping("/api/statements")
public class BankStatementController {
    private final BankStatementService bankStatementService;

    public BankStatementController(BankStatementService bankStatementService) {
        this.bankStatementService = bankStatementService;
    }

    @PostMapping(value = "/import", consumes = MULTIPART_FORM_DATA)
    @Operation(summary = "Import bank statements via CSV file")
    public ResponseEntity<ResponseDto> importCsv(@RequestParam("file") final MultipartFile file) {
        ResponseDto responseDto = bankStatementService.importCsv(file);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping(value = "/export", produces = MULTIPART_FORM_DATA)
    @Operation(summary = "Export bank statements to CSV")
    public void exportCsv(
            @RequestParam List<String> accountNumbers,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bank_statements.csv\"");

        List<BankStatementDto> transactions = bankStatementService.findBankStatements(accountNumbers, dateFrom, dateTo);
        CsvHelper.writeToCsv(response.getWriter(), transactions);
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @RequestParam String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        BigDecimal balance = bankStatementService.calculateBalance(accountNumber, dateFrom, dateTo);
        return ResponseEntity.ok(balance);
    }
}
