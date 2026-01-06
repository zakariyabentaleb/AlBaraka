package ma.albaraka.bank.service;

import ma.albaraka.bank.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(Long operationId, MultipartFile file);

    DocumentResponse getDocumentById(Long id);

    List<DocumentResponse> getDocumentsByOperationId(Long operationId);

    void deleteDocument(Long id);
}
