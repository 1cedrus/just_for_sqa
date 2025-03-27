package com.restaurent.manager.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    String id;
    String username;
    String password;
    String employeeName;
    String phoneNumber;
    RoleResponse role ;
}
