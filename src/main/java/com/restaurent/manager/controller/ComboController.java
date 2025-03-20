package com.restaurent.manager.controller;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Combo.ComboRequest;
import com.restaurent.manager.dto.request.Combo.ComboUpdateRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.dto.response.CustomerResponse;
import com.restaurent.manager.service.IComboService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ComboController {
    private final IComboService comboService;

    @PostMapping
    public ComboResponse createCombo(@RequestBody @Valid ComboRequest comboRequest) {
        return comboService.createCombo(comboRequest);
    }

    @GetMapping("/getAllCombos")
    public List<ComboResponse> getAllCombos() {
        return comboService.getAllCombos();
    }

    @PutMapping("update/{comboId}")
    public ApiResponse<ComboResponse> updateCombo(@PathVariable Long comboId, @Valid @RequestBody ComboUpdateRequest comboUpdateRequest){
        return ApiResponse.<ComboResponse>builder()
                .result(comboService.updateCombo(comboId, comboUpdateRequest))
                .build();
    }

    @GetMapping("/getDetail/{comboId}")
    public ApiResponse<ComboResponse> getComboDetail(@PathVariable Long comboId){
        return ApiResponse.<ComboResponse>builder()
                .result(comboService.getComboById(comboId))
                .build();
    }
    @GetMapping(value = "/restaurant/{restaurantId}")
    public ApiResponse<PagingResult<ComboResponse>> getComboByRestaurantId(@PathVariable Long restaurantId, @RequestParam(value = "page", defaultValue = "1") int pageIndex, @RequestParam(value = "size",defaultValue = "10") int size,
                                                            @RequestParam(value = "query", defaultValue = "") String query){
        Pageable pageable = PageRequest.of(pageIndex - 1,size);
        return ApiResponse.<PagingResult<ComboResponse>>builder()
                .result(comboService.getComboByRestaurantID(restaurantId,pageable,query))
                .build();
    }
}