package ma.albaraka.bank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ma.albaraka.bank.domain.entity.Operation;
import ma.albaraka.bank.dto.request.CreateOperationRequest;
import ma.albaraka.bank.dto.response.OperationResponse;

@Mapper(componentModel = "spring")
public interface OperationMapper {

    @Mapping(target = "type", expression = "java(operation.getType().name())")
    @Mapping(target = "amount", expression = "java(operation.getAmount().toPlainString())")
    @Mapping(target = "status", expression = "java(operation.getStatus().name())")
    @Mapping(target = "sourceAccountId", source = "sourceAccount.id")
    @Mapping(target = "sourceAccountNumber", source = "sourceAccount.accountNumber")
    @Mapping(target = "destinationAccountId", source = "destinationAccount.id")
    @Mapping(target = "destinationAccountNumber", source = "destinationAccount.accountNumber")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    OperationResponse toResponse(Operation operation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", expression = "java(ma.albaraka.bank.domain.enums.OperationType.valueOf(request.getType()))")
    @Mapping(target = "status", expression = "java(ma.albaraka.bank.domain.enums.OperationStatus.PENDING)")
    @Mapping(target = "sourceAccount", ignore = true)
    @Mapping(target = "destinationAccount", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "executedAt", ignore = true)
    Operation toEntity(CreateOperationRequest request);
}
