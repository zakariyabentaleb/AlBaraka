package ma.albaraka.bank.service.impl;

import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.domain.entity.Account;
import ma.albaraka.bank.domain.entity.Operation;
import ma.albaraka.bank.domain.entity.User;
import ma.albaraka.bank.domain.enums.AccountStatus;
import ma.albaraka.bank.domain.enums.OperationStatus;
import ma.albaraka.bank.domain.enums.OperationType;
import ma.albaraka.bank.dto.request.CreateOperationRequest;
import ma.albaraka.bank.dto.response.OperationResponse;
import ma.albaraka.bank.exception.BadRequestException;
import ma.albaraka.bank.exception.BusinessRuleException;
import ma.albaraka.bank.exception.ForbiddenException;
import ma.albaraka.bank.exception.ResourceNotFoundException;
import ma.albaraka.bank.mapper.OperationMapper;
import ma.albaraka.bank.repository.AccountRepository;
import ma.albaraka.bank.repository.OperationRepository;
import ma.albaraka.bank.repository.UserRepository;
import ma.albaraka.bank.service.OperationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final OperationMapper operationMapper;

    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000.00");

    @Override
    public OperationResponse createOperation(CreateOperationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        OperationType type;
        try {
            type = OperationType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid operation type: " + request.getType());
        }

        Operation operation = operationMapper.toEntity(request);
        operation.setCreatedBy(user);

        switch (type) {
            case DEPOSIT:
                handleDeposit(operation, request, userId);
                break;
            case WITHDRAWAL:
                handleWithdrawal(operation, request, userId);
                break;
            case TRANSFER:
                handleTransfer(operation, request, userId);
                break;
        }

        if (request.getAmount().compareTo(APPROVAL_THRESHOLD) <= 0) {
            operation.setStatus(OperationStatus.APPROVED);
            operation.setExecutedAt(LocalDateTime.now());
            executeOperation(operation);
        } else {
            operation.setStatus(OperationStatus.PENDING);
        }

        Operation savedOperation = operationRepository.save(operation);
        return operationMapper.toResponse(savedOperation);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationResponse getOperationById(Long id) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", id));
        return operationMapper.toResponse(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public OperationResponse getOperationById(Long id, Long userId) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", id));

        if (!operation.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenException("You can only access your own operations");
        }

        return operationMapper.toResponse(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationResponse> getOperationsByUserId(Long userId) {
        return operationRepository.findByCreatedById(userId).stream()
                .map(operationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationResponse> getPendingOperations() {
        return operationRepository.findByStatus(OperationStatus.PENDING).stream()
                .map(operationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OperationResponse approveOperation(Long id, Long agentId) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", id));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new BusinessRuleException("Only pending operations can be approved");
        }

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", agentId));

        operation.setStatus(OperationStatus.APPROVED);
        operation.setApprovedBy(agent);
        operation.setApprovedAt(LocalDateTime.now());
        operation.setExecutedAt(LocalDateTime.now());

        executeOperation(operation);

        Operation savedOperation = operationRepository.save(operation);
        return operationMapper.toResponse(savedOperation);
    }

    @Override
    public OperationResponse rejectOperation(Long id, Long agentId, String reason) {
        Operation operation = operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", id));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new BusinessRuleException("Only pending operations can be rejected");
        }

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", agentId));

        operation.setStatus(OperationStatus.REJECTED);
        operation.setApprovedBy(agent);
        operation.setApprovedAt(LocalDateTime.now());
        operation.setNote(reason);

        Operation savedOperation = operationRepository.save(operation);
        return operationMapper.toResponse(savedOperation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationResponse> getAllOperations() {
        return operationRepository.findAll().stream()
                .map(operationMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void handleDeposit(Operation operation, CreateOperationRequest request, Long userId) {
        if (request.getDestinationAccountId() == null) {
            throw new BadRequestException("Destination account is required for deposit");
        }

        Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getDestinationAccountId()));

        if (!destinationAccount.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You can only deposit to your own accounts");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Destination account is not active");
        }

        operation.setDestinationAccount(destinationAccount);
    }

    private void handleWithdrawal(Operation operation, CreateOperationRequest request, Long userId) {
        if (request.getSourceAccountId() == null) {
            throw new BadRequestException("Source account is required for withdrawal");
        }

        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getSourceAccountId()));

        if (!sourceAccount.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You can only withdraw from your own accounts");
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Source account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient balance");
        }

        operation.setSourceAccount(sourceAccount);
    }

    private void handleTransfer(Operation operation, CreateOperationRequest request, Long userId) {
        if (request.getSourceAccountId() == null || request.getDestinationAccountId() == null) {
            throw new BadRequestException("Both source and destination accounts are required for transfer");
        }

        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getSourceAccountId()));

        Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getDestinationAccountId()));

        if (!sourceAccount.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You can only transfer from your own accounts");
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Destination account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessRuleException("Insufficient balance");
        }

        operation.setSourceAccount(sourceAccount);
        operation.setDestinationAccount(destinationAccount);
    }

    private void executeOperation(Operation operation) {
        switch (operation.getType()) {
            case DEPOSIT:
                Account destinationAccount = operation.getDestinationAccount();
                destinationAccount.setBalance(destinationAccount.getBalance().add(operation.getAmount()));
                accountRepository.save(destinationAccount);
                break;

            case WITHDRAWAL:
                Account sourceAccount = operation.getSourceAccount();
                sourceAccount.setBalance(sourceAccount.getBalance().subtract(operation.getAmount()));
                accountRepository.save(sourceAccount);
                break;

            case TRANSFER:
                Account source = operation.getSourceAccount();
                Account destination = operation.getDestinationAccount();
                source.setBalance(source.getBalance().subtract(operation.getAmount()));
                destination.setBalance(destination.getBalance().add(operation.getAmount()));
                accountRepository.save(source);
                accountRepository.save(destination);
                break;
        }
    }
}
