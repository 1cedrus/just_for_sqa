package com.restaurent.manager.dto.response.Pack;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class PackUpgradeResponse {
    List<PackageResponse> packages;
    double deposit;
}
