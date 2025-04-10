package com.restaurent.manager.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.restaurent.manager.service.impl.ComboService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.Combo.ComboRequest;
import com.restaurent.manager.dto.request.Combo.ComboUpdateRequest;
import com.restaurent.manager.dto.response.Combo.ComboResponse;
import com.restaurent.manager.entity.Combo;
import com.restaurent.manager.entity.Dish;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.ComboMapper;
import com.restaurent.manager.repository.ComboRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.service.IRestaurantService;

class ComboServiceTest {
    private ComboRepository comboRepository;
    private DishRepository dishRepository;
    private ComboMapper comboMapper;
    private IRestaurantService restaurantService;
    private ComboService comboService;

    @BeforeEach
    void setUp() {
        comboRepository = mock(ComboRepository.class);
        dishRepository = mock(DishRepository.class);
        comboMapper = mock(ComboMapper.class);
        restaurantService = mock(IRestaurantService.class);

        comboService = new ComboService(comboRepository, dishRepository, comboMapper, restaurantService);
    }

    @Test
    void testCreateCombo_Success() {
        // Arrange
        ComboRequest request = new ComboRequest();
        request.setName("Family Combo");
        request.setPrice(299.99);
        request.setDescription("A big combo for the family.");
        request.setImageUrl("combo.jpg");
        request.setStatus(true);
        request.setDishIds(new HashSet<>(List.of(1L, 2L)));
        request.setRestaurantId(10L);

        Combo comboEntity = new Combo();
        Combo savedCombo = new Combo();
        ComboResponse expectedResponse = new ComboResponse();

        Dish dish1 = new Dish(); dish1.setId(1L);
        Dish dish2 = new Dish(); dish2.setId(2L);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(10L);

        when(comboMapper.toCombo(request)).thenReturn(comboEntity);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish1));
        when(dishRepository.findById(2L)).thenReturn(Optional.of(dish2));
        when(restaurantService.getRestaurantById(10L)).thenReturn(restaurant);
        when(comboRepository.save(any(Combo.class))).thenReturn(savedCombo);
        when(comboMapper.toComboResponse(savedCombo)).thenReturn(expectedResponse);

        // Act
        ComboResponse response = comboService.createCombo(request);

        // Assert
        assertEquals(expectedResponse, response);

        ArgumentCaptor<Combo> comboCaptor = ArgumentCaptor.forClass(Combo.class);
        verify(comboRepository).save(comboCaptor.capture());
        assertEquals(2, comboCaptor.getValue().getDishes().size());
        assertEquals(restaurant, comboCaptor.getValue().getRestaurant());
    }

    @Test
    void testCreateCombo_DishNotFound_ThrowsAppException() {
        // Arrange
        ComboRequest request = new ComboRequest();
        request.setName("Combo A");
        request.setPrice(100.0);
        request.setDescription("Combo with missing dish");
        request.setImageUrl("combo.jpg");
        request.setDishIds(new HashSet<>(List.of(1L, 999L)));
        request.setRestaurantId(1L);

        Combo comboEntity = new Combo();

        when(comboMapper.toCombo(request)).thenReturn(comboEntity);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(new Dish()));
        when(dishRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> comboService.createCombo(request));
        assertEquals(ErrorCode.DISH_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testCreateCombo_EmptyDishList_Success() {
        // Arrange
        ComboRequest request = new ComboRequest();
        request.setName("Simple Combo");
        request.setPrice(49.99);
        request.setDescription("No dishes yet.");
        request.setImageUrl("simple.jpg");
        request.setDishIds(new HashSet<>());
        request.setRestaurantId(5L);

        Combo comboEntity = new Combo();
        Combo savedCombo = new Combo();
        ComboResponse expectedResponse = new ComboResponse();

        Restaurant restaurant = new Restaurant();
        restaurant.setId(5L);

        when(comboMapper.toCombo(request)).thenReturn(comboEntity);
        when(restaurantService.getRestaurantById(5L)).thenReturn(restaurant);
        when(comboRepository.save(any(Combo.class))).thenReturn(savedCombo);
        when(comboMapper.toComboResponse(savedCombo)).thenReturn(expectedResponse);

        // Act
        ComboResponse response = comboService.createCombo(request);

        // Assert
        assertEquals(expectedResponse, response);
        assertTrue(comboEntity.getDishes().isEmpty());
    }

    @Test
    void testGetAllCombos_ReturnsListOfCombos() {
        // Arrange
        Combo combo1 = new Combo(); combo1.setId(1L);
        Combo combo2 = new Combo(); combo2.setId(2L);
        List<Combo> comboList = List.of(combo1, combo2);

        ComboResponse response1 = new ComboResponse(); response1.setId(1L);
        ComboResponse response2 = new ComboResponse(); response2.setId(2L);

        when(comboRepository.findAllActiveCombos()).thenReturn(comboList);
        when(comboMapper.toComboResponse(combo1)).thenReturn(response1);
        when(comboMapper.toComboResponse(combo2)).thenReturn(response2);

        // Act
        List<ComboResponse> result = comboService.getAllCombos();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(comboRepository).findAllActiveCombos();
        verify(comboMapper, times(2)).toComboResponse(any(Combo.class));
    }

    @Test
    void testGetAllCombos_EmptyList_ReturnsEmptyResponseList() {
        // Arrange
        when(comboRepository.findAllActiveCombos()).thenReturn(Collections.emptyList());

        // Act
        List<ComboResponse> result = comboService.getAllCombos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(comboRepository).findAllActiveCombos();
        verify(comboMapper, never()).toComboResponse(any());
    }

    @Test
    void testUpdateCombo_Success() {
        // Arrange
        Long comboId = 1L;
        Combo existingCombo = new Combo();
        existingCombo.setId(comboId);

        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setName("Updated Name");
        request.setPrice(199.99);
        request.setDescription("Updated description");
        request.setStatus(true);
        request.setDishIds(Set.of(1L, 2L));

        Dish dish1 = new Dish(); dish1.setId(1L);
        Dish dish2 = new Dish(); dish2.setId(2L);

        Combo updatedCombo = new Combo();
        updatedCombo.setId(comboId);
        updatedCombo.setName("Updated Name");

        ComboResponse response = new ComboResponse();
        response.setId(comboId);
        response.setName("Updated Name");

        when(comboRepository.findById(comboId)).thenReturn(Optional.of(existingCombo));
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish1));
        when(dishRepository.findById(2L)).thenReturn(Optional.of(dish2));
        when(comboRepository.save(any())).thenReturn(updatedCombo);
        when(comboMapper.toComboResponse(updatedCombo)).thenReturn(response);

        // Act
        ComboResponse result = comboService.updateCombo(comboId, request);

        // Assert
        assertNotNull(result);
        assertEquals(comboId, result.getId());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void testUpdateCombo_ComboNotFound_ThrowsException() {
        // Arrange
        Long comboId = 1L;
        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setDishIds(Set.of(1L));

        when(comboRepository.findById(comboId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> comboService.updateCombo(comboId, request));
        assertEquals(ErrorCode.COMBO_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void testUpdateCombo_DishNotFound_ThrowsException() {
        // Arrange
        Long comboId = 1L;
        Combo combo = new Combo(); combo.setId(comboId);
        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setName("abc");
        request.setPrice(100);
        request.setDescription("xyz");
        request.setStatus(true);
        request.setDishIds(Set.of(10L));

        when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        when(dishRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> comboService.updateCombo(comboId, request));
        assertEquals(ErrorCode.DISH_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testUpdateCombo_EmptyDishList_ShouldUpdateWithEmptySet() {
        // Arrange
        Long comboId = 1L;
        Combo combo = new Combo(); combo.setId(comboId);

        ComboUpdateRequest request = new ComboUpdateRequest();
        request.setName("No Dish Combo");
        request.setPrice(50.0);
        request.setDescription("Combo with no dish");
        request.setStatus(true);
        request.setDishIds(Collections.emptySet());

        Combo updatedCombo = new Combo(); updatedCombo.setId(comboId);
        updatedCombo.setName("No Dish Combo");

        ComboResponse response = new ComboResponse(); response.setId(comboId);
        response.setName("No Dish Combo");

        when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        when(comboRepository.save(any())).thenReturn(updatedCombo);
        when(comboMapper.toComboResponse(updatedCombo)).thenReturn(response);

        // Act
        ComboResponse result = comboService.updateCombo(comboId, request);

        // Assert
        assertNotNull(result);
        assertEquals(comboId, result.getId());
        assertEquals("No Dish Combo", result.getName());
        verify(dishRepository, never()).findById(any());
    }

    @Test
    void testGetComboById_Success() {
        // Arrange
        Long comboId = 1L;
        Combo combo = new Combo(); combo.setId(comboId);
        ComboResponse response = new ComboResponse(); response.setId(comboId);

        when(comboRepository.findById(comboId)).thenReturn(Optional.of(combo));
        when(comboMapper.toComboResponse(combo)).thenReturn(response);

        // Act
        ComboResponse result = comboService.getComboById(comboId);

        // Assert
        assertNotNull(result);
        assertEquals(comboId, result.getId());
    }

    @Test
    void testGetComboById_NotFound_ShouldThrowException() {
        // Arrange
        Long comboId = 99L;
        when(comboRepository.findById(comboId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> comboService.getComboById(comboId));
        assertEquals(ErrorCode.COMBO_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void testFindComboById_WhenExists_ShouldReturnCombo() {
        // Arrange
        Long comboId = 1L;
        Combo expectedCombo = new Combo();
        expectedCombo.setId(comboId);

        when(comboRepository.findById(comboId)).thenReturn(Optional.of(expectedCombo));

        // Act
        Combo result = comboService.findComboById(comboId);

        // Assert
        assertNotNull(result);
        assertEquals(comboId, result.getId());
    }

    @Test
    void testFindComboById_WhenNotFound_ShouldThrowException() {
        // Arrange
        Long comboId = 99L;
        when(comboRepository.findById(comboId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> comboService.findComboById(comboId));
        assertEquals(ErrorCode.COMBO_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void testGetComboByRestaurantID_WithResults() {
        // Arrange
        Long restaurantId = 1L;
        String query = "Combo";
        Pageable pageable = Pageable.ofSize(10);

        Combo combo = new Combo(); combo.setId(1L);
        ComboResponse comboResponse = new ComboResponse(); comboResponse.setId(1L);

        when(comboRepository.findByRestaurant_IdAndNameContaining(restaurantId, query, pageable))
                .thenReturn(List.of(combo));
        when(comboRepository.countByRestaurant_IdAndNameContaining(restaurantId, query)).thenReturn(1);
        when(comboMapper.toComboResponse(combo)).thenReturn(comboResponse);

        // Act
        PagingResult<ComboResponse> result = comboService.getComboByRestaurantID(restaurantId, pageable, query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(1, result.getTotalItems());
        assertEquals(comboResponse.getId(), result.getResults().get(0).getId());
    }

    @Test
    void testGetComboByRestaurantID_EmptyResults() {
        // Arrange
        Long restaurantId = 2L;
        String query = "NotFound";
        Pageable pageable = Pageable.ofSize(10);

        when(comboRepository.findByRestaurant_IdAndNameContaining(restaurantId, query, pageable))
                .thenReturn(List.of());
        when(comboRepository.countByRestaurant_IdAndNameContaining(restaurantId, query)).thenReturn(0);

        // Act
        PagingResult<ComboResponse> result = comboService.getComboByRestaurantID(restaurantId, pageable, query);

        // Assert
        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        assertEquals(0, result.getTotalItems());
    }

}
