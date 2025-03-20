package com.restaurent.manager.dto.response.Pack;

import com.restaurent.manager.dto.response.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageResponse {
   private Long id;
   private String packName;
   private Set<PermissionResponse> permissions;
   private double pricePerMonth;
   private double pricePerYear;
   private int total;
}
