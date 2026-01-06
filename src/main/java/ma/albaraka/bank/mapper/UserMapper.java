package ma.albaraka.bank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ma.albaraka.bank.domain.entity.User;
import ma.albaraka.bank.domain.enums.Role;
import ma.albaraka.bank.dto.request.CreateUserRequest;
import ma.albaraka.bank.dto.response.UserResponse;

@Mapper(componentModel = "spring", imports = {Role.class})
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(ma.albaraka.bank.domain.enums.Role.valueOf(request.getRole()))")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);
}
