package com.restaurent.manager.service.impl;


import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Combo.ComboRequest;
import com.restaurent.manager.dto.request.Combo.ComboUpdateRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Combo;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.ComboMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.ComboRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.service.IComboService;
import com.restaurent.manager.service.IRestaurantService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ComboService implements IComboService {
    ComboRepository comboRepository;
    DishRepository dishRepository;
    ComboMapper comboMapper;
    IRestaurantService restaurantService;

    @Override
    public ComboResponse createCombo(ComboRequest comboRequest) {
        Combo combo = comboMapper.toCombo(comboRequest);

        Set<Dish> dishes = new HashSet<>();
        for (Long dishId : comboRequest.getDishIds()) {
            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
            dishes.add(dish);
        }
        combo.setDishes(dishes);


        combo.setRestaurant(restaurantService.getRestaurantById(comboRequest.getRestaurantId()));

        Combo savedCombo = comboRepository.save(combo);

        return comboMapper.toComboResponse(savedCombo);
    }

    public List<ComboResponse> getAllCombos() {
        List<Combo> combos = comboRepository.findAllActiveCombos();
        return  combos.stream()
                .map(comboMapper::toComboResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ComboResponse updateCombo(Long comboId, ComboUpdateRequest request) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_EXISTED));

        combo.setName(request.getName());
        combo.setPrice(request.getPrice());
        combo.setDescription(request.getDescription());
        combo.setStatus(request.isStatus());

        Set<Dish> dishes = new HashSet<>();
        for (Long dishId : request.getDishIds()) {
            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
            dishes.add(dish);
        }
        combo.setDishes(dishes);

        Combo updatedCombo = comboRepository.save(combo);

        return comboMapper.toComboResponse(updatedCombo);
    }

    public ComboResponse getComboById(Long id){
        Combo combo = findComboById(id);
        return comboMapper.toComboResponse(combo);
    }

    @Override
    public Combo findComboById(Long id) {
        return comboRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.COMBO_NOT_EXISTED)
        );
    }

    @Override
    public PagingResult<ComboResponse> getComboByRestaurantID(Long restaurantID, Pageable pageable, String query) {
        return PagingResult.<ComboResponse>builder()
                .results(comboRepository.findByRestaurant_IdAndNameContaining(restaurantID,query,pageable).stream().map(comboMapper::toComboResponse).toList())
                .totalItems(comboRepository.countByRestaurant_IdAndNameContaining(restaurantID,query))
                .build();
    }


}