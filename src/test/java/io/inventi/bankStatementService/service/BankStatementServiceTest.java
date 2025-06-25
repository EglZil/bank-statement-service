package io.inventi.bankStatementService.service;

import io.inventi.bankStatementService.model.BankStatement;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BankStatementServiceTest {

    @Mock
    private BankStatementRepository bankStatementRepository;

    @InjectMocks
    private BankStatementService bankStatementService;
    private AutoCloseable closable;

    @BeforeEach
    void setup() {
        closable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closable.close();
    }

    @Test
    void testImportCsv_success() {
        // Given: sample CSV data
        String csvContent = """
                Account number,Operation date/time,Beneficiary,Comment,Amount,Currency
                1234567890,2024-06-01 10:15:00,Alice,Payment for invoice,-500.00,USD
                1234567890,2024-06-02 09:00:00,Bob,Salary,3000.00,USD
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "transactions.csv", "text/csv", csvContent.getBytes()
        );

        // When
        bankStatementService.importCsv(file);

        // Then
        ArgumentCaptor<List<BankStatement>> captor = ArgumentCaptor.forClass(List.class);
        verify(bankStatementRepository, times(1)).saveAll(captor.capture());

        List<BankStatement> saved = captor.getValue();
        assert saved.size() == 2;
        assert saved.get(0).getBeneficiary().equals("Alice");
        assert saved.get(1).getAmount().toString().equals("3000.00");
    }
}
