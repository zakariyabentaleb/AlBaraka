package ma.albaraka.bank.repository;

import ma.albaraka.bank.domain.entity.Account;
import ma.albaraka.bank.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByOwnerId(Long ownerId);

    List<Account> findByOwnerIdAndStatus(Long ownerId, AccountStatus status);

    Boolean existsByAccountNumber(String accountNumber);
}
