package com.restaurent.manager.service;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.restaurent.manager.service.impl.AreaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.restaurent.manager.dto.request.AreaRequest;
import com.restaurent.manager.dto.response.AreaResponse;
import com.restaurent.manager.entity.Area;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Permission;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.AreaMapper;
import com.restaurent.manager.repository.AreaRepository;
import com.restaurent.manager.service.IRestaurantService;

class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AreaMapper areaMapper;

    @Mock
    private IRestaurantService restaurantService;

    @InjectMocks
    private AreaService areaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //AreaS1
    @Test
    void createArea_withAreaMaxPermissionAndLimitExceeded_shouldThrowException() {
        AreaRequest request = new AreaRequest();
        request.setName("VIP Zone");
        request.setRestaurantId(1L);

        Permission permission = new Permission();
        permission.setName("AREA_MAX");
        permission.setMaximum(2);

        Package restaurantPackage = new Package();
        restaurantPackage.setPermissions(Set.of(permission));

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(restaurantPackage);

        when(areaMapper.toArea(request)).thenReturn(new Area());
        when(areaRepository.countByRestaurant_Id(1L)).thenReturn(2);
        when(restaurantService.getRestaurantById(1L)).thenReturn(restaurant);

        AppException exception = assertThrows(AppException.class, () -> areaService.createArea(request));
        assertEquals(ErrorCode.MAX_AREA, exception.getErrorCode());
    }

    //AreaS2
    @Test
    void createArea_withAreaMaxPermissionUnderLimit_shouldCreateSuccessfully() {
        AreaRequest request = new AreaRequest();
        request.setName("Garden");
        request.setRestaurantId(1L);

        Permission permission = new Permission();
        permission.setName("AREA_MAX");
        permission.setMaximum(5);

        Package restaurantPackage = new Package();
        restaurantPackage.setPermissions(Set.of(permission));

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(restaurantPackage);

        Area area = new Area();
        Area savedArea = new Area();
        AreaResponse response = new AreaResponse();

        when(areaMapper.toArea(request)).thenReturn(area);
        when(areaRepository.countByRestaurant_Id(1L)).thenReturn(3);
        when(restaurantService.getRestaurantById(1L)).thenReturn(restaurant);
        when(areaRepository.save(area)).thenReturn(savedArea);
        when(areaMapper.toAreaResponse(savedArea)).thenReturn(response);

        AreaResponse result = areaService.createArea(request);
        assertEquals(response, result);
    }

    //AreaS3
    @Test
    void createArea_withoutAreaMaxPermission_shouldCreateSuccessfully() {
        AreaRequest request = new AreaRequest();
        request.setName("Main Hall");
        request.setRestaurantId(1L);

        Permission permission = new Permission();
        permission.setName("OTHER_PERMISSION");

        Package restaurantPackage = new Package();
        restaurantPackage.setPermissions(Set.of(permission));

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantPackage(restaurantPackage);

        Area area = new Area();
        Area savedArea = new Area();
        AreaResponse response = new AreaResponse();

        when(areaMapper.toArea(request)).thenReturn(area);
        when(areaRepository.countByRestaurant_Id(1L)).thenReturn(10);
        when(restaurantService.getRestaurantById(1L)).thenReturn(restaurant);
        when(areaRepository.save(area)).thenReturn(savedArea);
        when(areaMapper.toAreaResponse(savedArea)).thenReturn(response);

        AreaResponse result = areaService.createArea(request);
        assertEquals(response, result);
    }

    //AreaS4
    @Test
    void getAreasByRestaurantId_shouldReturnMappedAreaResponses() {
        Long restaurantId = 1L;

        Area area1 = new Area();
        area1.setId(101L);
        area1.setName("Zone A");

        Area area2 = new Area();
        area2.setId(102L);
        area2.setName("Zone B");

        AreaResponse response1 = new AreaResponse();
        response1.setId(101L);
        response1.setName("Zone A");

        AreaResponse response2 = new AreaResponse();
        response2.setId(102L);
        response2.setName("Zone B");

        when(areaRepository.findByRestaurant_Id(restaurantId)).thenReturn(List.of(area1, area2));
        when(areaMapper.toAreaResponse(area1)).thenReturn(response1);
        when(areaMapper.toAreaResponse(area2)).thenReturn(response2);

        List<AreaResponse> results = areaService.getAreasByRestaurantId(restaurantId);

        assertEquals(2, results.size());
        assertEquals("Zone A", results.get(0).getName());
        assertEquals("Zone B", results.get(1).getName());

        verify(areaRepository, times(1)).findByRestaurant_Id(restaurantId);
        verify(areaMapper, times(1)).toAreaResponse(area1);
        verify(areaMapper, times(1)).toAreaResponse(area2);
    }

    //AreaS5
    @Test
    void getAreasByRestaurantId_whenNoAreasExist_shouldReturnEmptyList() {
        Long restaurantId = 999L;

        when(areaRepository.findByRestaurant_Id(restaurantId)).thenReturn(List.of());

        List<AreaResponse> results = areaService.getAreasByRestaurantId(restaurantId);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(areaRepository, times(1)).findByRestaurant_Id(restaurantId);
        verifyNoInteractions(areaMapper); // vì không có Area nào, nên không map gì cả
    }

    //AreaS6
    @Test
    void updateArea_whenAreaExists_shouldUpdateAndReturnResponse() {
        Long areaId = 1L;

        AreaRequest request = new AreaRequest();
        request.setName("New Name");

        Area existingArea = new Area();
        existingArea.setId(areaId);
        existingArea.setName("Old Name");

        Area savedArea = new Area();
        savedArea.setId(areaId);
        savedArea.setName("New Name");

        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setName("New Name");

        when(areaRepository.findById(areaId)).thenReturn(Optional.of(existingArea));
        when(areaRepository.save(existingArea)).thenReturn(savedArea);
        when(areaMapper.toAreaResponse(savedArea)).thenReturn(response);

        AreaResponse result = areaService.updateArea(areaId, request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(areaId, result.getId());

        verify(areaRepository).findById(areaId);
        verify(areaRepository).save(existingArea);
        verify(areaMapper).toAreaResponse(savedArea);
    }

}

