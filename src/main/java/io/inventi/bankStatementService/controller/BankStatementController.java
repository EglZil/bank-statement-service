package io.inventi.bankStatementService.controller;

import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.service.BankStatementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.apache.tomcat.util.http.fileupload.FileUploadBase.MULTIPART_FORM_DATA;

@RestController
@RequestMapping("/api/statements")
public class BankStatementController {
    private final BankStatementService service;

    public BankStatementController(BankStatementService service) {
        this.service = service;
    }

    @PostMapping(value = "/import", consumes = MULTIPART_FORM_DATA)
    @Operation(summary = "Import bank statements via CSV file")
    public ResponseEntity<ResponseDto> importCsv(@RequestParam("file") final MultipartFile file) {
        ResponseDto responseDto = service.importCsv(file);
        return ResponseEntity.ok(responseDto);
    }
}
