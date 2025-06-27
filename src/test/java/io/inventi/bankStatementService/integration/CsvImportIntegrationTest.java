package io.inventi.bankStatementService.integration;

import io.inventi.bankStatementService.model.BankStatement;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CsvImportIntegrationTest {
    private static final String CSV_IMPORT_ENDPOINT = "/api/statements/import";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankStatementRepository bankStatementRepository;

    @BeforeEach
    void setup() {
        bankStatementRepository.deleteAll();
    }

    @Test
    void testCsvImport_withMixedValidAndInvalidRows() throws Exception {
        String csv = """
        Account number,Operation date/time,Beneficiary,Comment,Amount,Currency
        1234567890,2024-06-01 10:15:00,Alice,Valid,-500.00,USD
        ,2024-06-02 09:00:00,Bob,Missing account,3000.00,USD
        9876543210,,Charlie,Missing date,1000.00,EUR
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(CSV_IMPORT_ENDPOINT).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.skipped").value(2))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0].row").value(2))
                .andExpect(jsonPath("$.errors[1].row").value(3));

        // Verify DB has only 1 record
        List<BankStatement> all = bankStatementRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getBeneficiary()).isEqualTo("Alice");
    }
}
