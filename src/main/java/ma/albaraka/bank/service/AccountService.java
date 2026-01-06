package ma.albaraka.bank.service;

import java.util.List;

import ma.albaraka.bank.dto.response.AccountResponse;

public interface AccountService {

    AccountResponse createAccountForUser(Long userId);

    AccountResponse getAccountById(Long id);

    AccountResponse getAccountById(Long id, Long userId);

    AccountResponse getAccountByNumber(String accountNumber);

    List<AccountResponse> getAccountsByUserId(Long userId);

    List<AccountResponse> getAllAccounts();

    void freezeAccount(Long id);

    void activateAccount(Long id);

    void closeAccount(Long id);
}
