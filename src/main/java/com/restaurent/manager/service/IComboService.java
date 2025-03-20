package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Combo.ComboRequest;
import com.restaurent.manager.dto.request.Combo.ComboUpdateRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.entity.Combo;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IComboService {
    ComboResponse createCombo(ComboRequest comboRequest);

    List<ComboResponse> getAllCombos();

    ComboResponse updateCombo(Long comboId,ComboUpdateRequest request);

    ComboResponse getComboById(Long id);
    Combo findComboById(Long id);
    PagingResult<ComboResponse> getComboByRestaurantID(Long restaurantID, Pageable pageable, String query);
}
