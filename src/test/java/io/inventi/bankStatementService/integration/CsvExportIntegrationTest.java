package io.inventi.bankStatementService.integration;

import io.inventi.bankStatementService.model.BankStatement;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CsvExportIntegrationTest {
    private static final String CSV_EXPORT_ENDPOINT = "/api/statements/export";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankStatementRepository bankStatementRepository;

    @BeforeEach
    void setup() {
        bankStatementRepository.deleteAll();
        // Add test data
        bankStatementRepository.saveAll(List.of(
                new BankStatement(null, "123", LocalDateTime.parse("2024-06-01T10:00"), "Alice", BigDecimal.valueOf(100), "USD", "Payment"),
                new BankStatement(null, "456", LocalDateTime.parse("2024-06-05T15:30"), "Bob", BigDecimal.valueOf(-50), "EUR", "Refund")
        ));
    }

    @Test
    void exportCsv_singleAccount_returnsValidCsv() throws Exception {
        mockMvc.perform(get(CSV_EXPORT_ENDPOINT)
                        .param("accountNumbers", "123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("Alice")))
                .andExpect(content().string(containsString("Payment")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
    }

    @Test
    void exportCsv_multipleAccounts_returnsBothRows() throws Exception {
        mockMvc.perform(get(CSV_EXPORT_ENDPOINT)
                        .param("accountNumbers", "123")
                        .param("accountNumbers", "456"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alice")))
                .andExpect(content().string(containsString("Bob")));
    }

    @Test
    void exportCsv_noMatch_returnsOnlyHeaders() throws Exception {
        mockMvc.perform(get(CSV_EXPORT_ENDPOINT)
                        .param("accountNumbers", "999")) // no such account
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Account number"))) // CSV headers only
                .andExpect(content().string(not(containsString("Alice"))));
    }
}
