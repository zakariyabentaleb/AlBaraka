package ma.albaraka.bank.service.impl;

import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.domain.entity.Account;
import ma.albaraka.bank.domain.entity.User;
import ma.albaraka.bank.domain.enums.AccountStatus;
import ma.albaraka.bank.domain.enums.Role;
import ma.albaraka.bank.dto.request.LoginRequest;
import ma.albaraka.bank.dto.request.RegisterRequest;
import ma.albaraka.bank.dto.response.AuthResponse;
import ma.albaraka.bank.exception.BadRequestException;
import ma.albaraka.bank.exception.UnauthorizedException;
import ma.albaraka.bank.repository.AccountRepository;
import ma.albaraka.bank.repository.UserRepository;
import ma.albaraka.bank.security.JwtTokenProvider;
import ma.albaraka.bank.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

            if (!user.getActive()) {
                throw new UnauthorizedException("Account is inactive");
            }

            String token = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getRole().name(),
                    user.getId()
            );

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        // Create user with CLIENT role by default
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);

        // Create account automatically
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .owner(user)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);

        // Generate token
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            accountNumber = "AB-" + String.format("%08d", random.nextInt(100000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
