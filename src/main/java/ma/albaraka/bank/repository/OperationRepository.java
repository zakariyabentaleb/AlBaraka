package ma.albaraka.bank.repository;

import ma.albaraka.bank.domain.entity.Operation;
import ma.albaraka.bank.domain.enums.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    List<Operation> findByStatus(OperationStatus status);

    List<Operation> findByCreatedById(Long userId);

    List<Operation> findBySourceAccountId(Long accountId);

    List<Operation> findByDestinationAccountId(Long accountId);
}
