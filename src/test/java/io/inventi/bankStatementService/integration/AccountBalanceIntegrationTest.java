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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountBalanceIntegrationTest {
    private static final String BALANCE_ENDPOINT = "/api/statements/balance";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankStatementRepository bankStatementRepository;

    @BeforeEach
    void setup() {
        bankStatementRepository.deleteAll();
        // Add test data
        bankStatementRepository.saveAll(List.of(
                new BankStatement(null, "123", LocalDateTime.parse("2024-06-01T10:00"), "Alice", BigDecimal.valueOf(200), "USD", "Salary"),
                new BankStatement(null, "123", LocalDateTime.parse("2024-06-02T11:00"), "Alice", BigDecimal.valueOf(-50), "USD", "Groceries")
        ));
    }

    @Test
    void getBalance_returnsSumForAccount() throws Exception {
        mockMvc.perform(get(BALANCE_ENDPOINT)
                        .param("accountNumber", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.00")); // 200 - 50
    }

    @Test
    void getBalance_withDateRange_filtersCorrectly() throws Exception {
        mockMvc.perform(get(BALANCE_ENDPOINT)
                        .param("accountNumber", "123")
                        .param("dateFrom", "2024-06-02")
                        .param("dateTo", "2024-06-02"))
                .andExpect(status().isOk())
                .andExpect(content().string("-50.00")); // Only the grocery transaction
    }

    @Test
    void getBalance_noTransactions_returnsZero() throws Exception {
        mockMvc.perform(get(BALANCE_ENDPOINT)
                        .param("accountNumber", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
