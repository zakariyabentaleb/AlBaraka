package ma.albaraka.bank.service.impl;

import lombok.RequiredArgsConstructor;
import ma.albaraka.bank.domain.entity.Document;
import ma.albaraka.bank.domain.entity.Operation;
import ma.albaraka.bank.domain.enums.DocumentType;
import ma.albaraka.bank.dto.response.DocumentResponse;
import ma.albaraka.bank.exception.BadRequestException;
import ma.albaraka.bank.exception.ResourceNotFoundException;
import ma.albaraka.bank.mapper.DocumentMapper;
import ma.albaraka.bank.repository.DocumentRepository;
import ma.albaraka.bank.repository.OperationRepository;
import ma.albaraka.bank.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;
    private final DocumentMapper documentMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    @Override
    public DocumentResponse uploadDocument(Long operationId, MultipartFile file) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operation", "id", operationId));

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Only PDF, JPG, and PNG are allowed");
        }

        DocumentType documentType = getDocumentType(contentType);
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Document document = Document.builder()
                    .operation(operation)
                    .fileName(originalFilename)
                    .fileType(documentType)
                    .storagePath(filePath.toString())
                    .fileSize(file.getSize())
                    .build();

            Document savedDocument = documentRepository.save(document);
            return documentMapper.toResponse(savedDocument);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByOperationId(Long operationId) {
        return documentRepository.findByOperationId(operationId).stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        try {
            Path filePath = Paths.get(document.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }

        documentRepository.delete(document);
    }

    private DocumentType getDocumentType(String contentType) {
        return switch (contentType) {
            case "application/pdf" ->
                DocumentType.PDF;
            case "image/jpeg", "image/jpg" ->
                DocumentType.JPG;
            case "image/png" ->
                DocumentType.PNG;
            default ->
                throw new BadRequestException("Unsupported file type");
        };
    }
}
