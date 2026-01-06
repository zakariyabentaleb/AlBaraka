package ma.albaraka.bank.controller;

import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.dto.response.OperationResponse;
import ma.albaraka.bank.security.JwtTokenProvider;
import ma.albaraka.bank.service.OperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final OperationService operationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/operations/pending")
    public ResponseEntity<List<OperationResponse>> getPendingOperations() {
        List<OperationResponse> operations = operationService.getPendingOperations();
        return ResponseEntity.ok(operations);
    }

    @GetMapping("/operations/{id}")
    public ResponseEntity<OperationResponse> getOperationById(@PathVariable Long id) {
        OperationResponse operation = operationService.getOperationById(id);
        return ResponseEntity.ok(operation);
    }

    @PutMapping("/operations/{id}/approve")
    public ResponseEntity<OperationResponse> approveOperation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long agentId = extractUserIdFromToken(authHeader);
        OperationResponse response = operationService.approveOperation(id, agentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/operations/{id}/reject")
    public ResponseEntity<OperationResponse> rejectOperation(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authHeader) {

        Long agentId = extractUserIdFromToken(authHeader);
        String rejectionReason = reason != null ? reason : "Rejected by agent";
        OperationResponse response = operationService.rejectOperation(id, agentId, rejectionReason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponse>> getAllOperations() {
        List<OperationResponse> operations = operationService.getAllOperations();
        return ResponseEntity.ok(operations);
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtTokenProvider.extractUserId(token);
    }
}
