package ma.albaraka.bank.mapper;

import ma.albaraka.bank.domain.entity.Document;
import ma.albaraka.bank.dto.response.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "operationId", source = "operation.id")
    @Mapping(target = "fileType", expression = "java(document.getFileType().name())")
    DocumentResponse toResponse(Document document);
}
