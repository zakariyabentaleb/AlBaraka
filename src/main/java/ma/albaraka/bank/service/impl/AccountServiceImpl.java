package ma.albaraka.bank.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.domain.entity.Account;
import ma.albaraka.bank.domain.entity.User;
import ma.albaraka.bank.domain.enums.AccountStatus;
import ma.albaraka.bank.dto.response.AccountResponse;
import ma.albaraka.bank.exception.ForbiddenException;
import ma.albaraka.bank.exception.ResourceNotFoundException;
import ma.albaraka.bank.mapper.AccountMapper;
import ma.albaraka.bank.repository.AccountRepository;
import ma.albaraka.bank.repository.UserRepository;
import ma.albaraka.bank.service.AccountService;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    private final Random random = new Random();

    @Override
    public AccountResponse createAccountForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .owner(user)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id, Long userId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        
        if (!account.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You can only access your own accounts");
        }
        
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByOwnerId(userId).stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void freezeAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
    }

    @Override
    public void activateAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            int randomNumber = 10000000 + random.nextInt(90000000);
            accountNumber = "AB-" + randomNumber;
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }
}
