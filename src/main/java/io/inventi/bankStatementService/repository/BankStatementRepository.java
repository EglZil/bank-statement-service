package io.inventi.bankStatementService.repository;

import io.inventi.bankStatementService.model.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {
}
