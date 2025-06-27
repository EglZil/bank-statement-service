package io.inventi.bankStatementService.service;

import io.inventi.bankStatementService.dto.BankStatementDto;
import io.inventi.bankStatementService.dto.ResponseDto;
import io.inventi.bankStatementService.mapper.BankStatementMapper;
import io.inventi.bankStatementService.model.BankStatement;
import io.inventi.bankStatementService.repository.BankStatementRepository;
import io.inventi.bankStatementService.util.CsvHelper;
import io.inventi.bankStatementService.util.CsvHelperResult;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankStatementServiceTest {

    @Mock
    private BankStatementRepository bankStatementRepository;

    @Mock
    private BankStatementMapper bankStatementMapper;

    @Mock
    private Validator validator;

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
    void importCsv_shouldSaveValidStatementsAndReturnResponse() {
        MultipartFile mockFile = mock(MultipartFile.class);

        BankStatement valid1 = new BankStatement();
        BankStatement valid2 = new BankStatement();
        List<BankStatement> validStatements = List.of(valid1, valid2);
        List<ResponseDto.RowError> errors = List.of(
                new ResponseDto.RowError(1L, "Row 3: Missing amount"),
                new ResponseDto.RowError(2L, "Row 4: Invalid date")
        );

        CsvHelperResult result = new CsvHelperResult(validStatements, errors);

        try (MockedStatic<CsvHelper> mockedHelper = mockStatic(CsvHelper.class)) {
            mockedHelper.when(() -> CsvHelper.validateAndParseCsv(eq(mockFile), eq(validator)))
                    .thenReturn(result);

            ResponseDto response = bankStatementService.importCsv(mockFile);

            verify(bankStatementRepository).saveAll(validStatements);

            assertEquals(2, response.getImported());
            assertEquals(2, response.getSkipped());
            assertEquals(errors, response.getErrors());
        }
    }

    @Test
    void findBankStatements_withDateFilters_shouldQueryBetweenDates() {
        String acc = "123";
        List<String> accounts = List.of(acc);
        LocalDate dateFrom = LocalDate.of(2024, 6, 1);
        LocalDate dateTo = LocalDate.of(2024, 6, 5);

        BankStatement entity = new BankStatement();
        BankStatementDto dto = new BankStatementDto();

        when(bankStatementRepository.findByAccountNumberInAndOperationDateBetween(
                eq(accounts),
                eq(dateFrom.atStartOfDay()),
                eq(dateTo.atTime(LocalTime.MAX))
        )).thenReturn(List.of(entity));

        when(bankStatementMapper.entityToDto(entity)).thenReturn(dto);

        List<BankStatementDto> result = bankStatementService.findBankStatements(accounts, dateFrom, dateTo);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void findBankStatements_withoutDates_shouldQueryOnlyByAccount() {
        List<String> accounts = List.of("123");
        BankStatement entity = new BankStatement();
        BankStatementDto dto = new BankStatementDto();

        when(bankStatementRepository.findByAccountNumberIn(accounts)).thenReturn(List.of(entity));
        when(bankStatementMapper.entityToDto(entity)).thenReturn(dto);

        List<BankStatementDto> result = bankStatementService.findBankStatements(accounts, null, null);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void findBankStatements_noResults_returnsEmptyList() {
        List<String> accounts = List.of("123");
        when(bankStatementRepository.findByAccountNumberIn(accounts)).thenReturn(List.of());

        List<BankStatementDto> result = bankStatementService.findBankStatements(accounts, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateBalance_withinDateRange_returnsCorrectSum() {
        String accountNumber = "123";
        List<BankStatement> data = List.of(
                new BankStatement(null, accountNumber, LocalDateTime.now(), "Alice", BigDecimal.valueOf(100), "USD", "Payment"),
                new BankStatement(null, accountNumber, LocalDateTime.now(), "Alice", BigDecimal.valueOf(-40), "USD", "Withdrawal")
        );

        when(bankStatementRepository.findByAccountNumberAndOperationDateBetween(eq(accountNumber), any(), any())).thenReturn(data);
        BigDecimal balance = bankStatementService.calculateBalance(accountNumber, null, null);

        assertEquals(BigDecimal.valueOf(60), balance);
    }

    @Test
    void calculateBalance_noTransactions_returnsZero() {
        when(bankStatementRepository.findByAccountNumberAndOperationDateBetween(eq("999"), any(), any()))
                .thenReturn(List.of());

        BigDecimal result = bankStatementService.calculateBalance("999", null, null);

        assertEquals(BigDecimal.ZERO, result);
    }
}
