package com.restaurent.manager.mapper;

import com.restaurent.manager.dto.request.AccountRequest;
import com.restaurent.manager.dto.request.RoleRequest;
import com.restaurent.manager.dto.response.AccountResponse;
import com.restaurent.manager.dto.response.RoleResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "role",ignore = true)
    Account toAccount(AccountRequest req);
    AccountResponse toAccountResponse(Account account);
}
