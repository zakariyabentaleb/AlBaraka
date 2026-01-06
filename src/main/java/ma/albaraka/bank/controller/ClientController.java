package ma.albaraka.bank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.dto.request.CreateOperationRequest;
import ma.albaraka.bank.dto.response.AccountResponse;
import ma.albaraka.bank.dto.response.DocumentResponse;
import ma.albaraka.bank.dto.response.OperationResponse;
import ma.albaraka.bank.security.JwtTokenProvider;
import ma.albaraka.bank.service.AccountService;
import ma.albaraka.bank.service.DocumentService;
import ma.albaraka.bank.service.OperationService;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final OperationService operationService;
    private final AccountService accountService;
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/operations")
    public ResponseEntity<OperationResponse> createOperation(
            @Valid @RequestBody CreateOperationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        OperationResponse response = operationService.createOperation(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponse>> getMyOperations(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        List<OperationResponse> operations = operationService.getOperationsByUserId(userId);
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/operations/{id}")
    public ResponseEntity<OperationResponse> getOperationById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        OperationResponse operation = operationService.getOperationById(id, userId);

        return ResponseEntity.ok(operation);
    }

    @PostMapping("/operations/{id}/document")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.uploadDocument(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/operations/{id}/documents")
    public ResponseEntity<List<DocumentResponse>> getOperationDocuments(@PathVariable Long id) {
        List<DocumentResponse> documents = documentService.getDocumentsByOperationId(id);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserIdFromToken(authHeader);
        AccountResponse account = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(account);
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtTokenProvider.extractUserId(token);
    }
}
