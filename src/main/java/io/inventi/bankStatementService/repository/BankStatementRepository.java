package io.inventi.bankStatementService.repository;

import io.inventi.bankStatementService.model.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {
    List<BankStatement> findByAccountNumberInAndOperationDateBetween(List<String> accountNumbers, LocalDateTime from, LocalDateTime to);

    List<BankStatement> findByAccountNumberIn(List<String> accountNumbers);
}
