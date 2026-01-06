package ma.albaraka.bank.service;

import java.util.List;

import ma.albaraka.bank.dto.request.CreateOperationRequest;
import ma.albaraka.bank.dto.response.OperationResponse;

public interface OperationService {

    OperationResponse createOperation(CreateOperationRequest request, Long userId);

    OperationResponse getOperationById(Long id);

    OperationResponse getOperationById(Long id, Long userId);

    List<OperationResponse> getOperationsByUserId(Long userId);

    List<OperationResponse> getPendingOperations();

    OperationResponse approveOperation(Long id, Long agentId);

    OperationResponse rejectOperation(Long id, Long agentId, String reason);

    List<OperationResponse> getAllOperations();
}
