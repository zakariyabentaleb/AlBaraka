package ma.albaraka.bank.mapper;

import ma.albaraka.bank.domain.entity.Account;
import ma.albaraka.bank.dto.response.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.fullName")
    @Mapping(target = "balance", expression = "java(account.getBalance().toPlainString())")
    @Mapping(target = "status", expression = "java(account.getStatus().name())")
    AccountResponse toResponse(Account account);
}
