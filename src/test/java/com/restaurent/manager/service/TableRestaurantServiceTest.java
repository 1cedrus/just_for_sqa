package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.request.Table.TableRestaurantUpdateRequest;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.TableRestaurantMapper;
import com.restaurent.manager.mapper.TableRestaurantMapperImpl;
import com.restaurent.manager.repository.AreaRepository;
import com.restaurent.manager.repository.ScheduleRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.repository.TableTypeRepository;
import com.restaurent.manager.service.impl.TableRestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TableRestaurantServiceTest {

    @Mock
    AreaRepository areaRepository;

    @Mock
    TableTypeRepository tableTypeRepository;

    @Mock
    TableRestaurantRepository tableRestaurantRepository;

    @Mock
    IRestaurantService restaurantService;

    @Mock
    ScheduleRepository scheduleRepository;

    @Spy
    TableRestaurantMapper tableRestaurantMapper = new TableRestaurantMapperImpl(); // Use the actual mapper

    @InjectMocks
    TableRestaurantService tableRestaurantService;

    TableRestaurantRequest tableRestaurantRequest;
    Restaurant restaurant;
    TableRestaurantUpdateRequest tableRestaurantUpdateRequest;
    TableRestaurant tableRestaurant;
    TableRestaurantResponse tableRestaurantResponse;
    TableType tableType;
    Permission tableMaxPermission;
    Area area;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        tableRestaurantRequest = new TableRestaurantRequest();
        restaurant = new Restaurant();
        tableRestaurantUpdateRequest = new TableRestaurantUpdateRequest();
        tableType = new TableType();
        area = new Area();

        tableType.setId(1);
        area.setId(1L);

        tableMaxPermission = new Permission();
        tableMaxPermission.setId(1L);
        tableMaxPermission.setName("TABLE_MAX");
        tableMaxPermission.setMaximum(5);

        restaurant.setId(1L);
        restaurant.setRestaurantPackage(new Package());
        restaurant.getRestaurantPackage().setPermissions(Set.of(tableMaxPermission));

        tableRestaurantRequest.setRestaurantId(1L);
        tableRestaurantRequest.setAreaId(1L);
        tableRestaurantRequest.setTableTypeId(1L);
        tableRestaurantRequest.setName("Test Table Restaurant");
        tableRestaurantRequest.setNumberChairs(4);

        tableRestaurant = tableRestaurantMapper.toTableRestaurant(tableRestaurantRequest);
        tableRestaurant.setId(1L);
        tableRestaurantResponse = tableRestaurantMapper.toTableRestaurantResponse(tableRestaurant);
    }

    @Test
    void createTableRestaurantShouldCreateTableRestaurantWhenDataIsValid() {
        when(tableRestaurantRepository.save(any(TableRestaurant.class))).thenReturn(tableRestaurant);
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));

        // Act: Call the method under test
        TableRestaurantResponse result = tableRestaurantService.createTable(tableRestaurantRequest);

        // Assert: Verify the result and interactions
        assertEquals(tableRestaurantResponse, result); // Check the returned Table Restaurant

        verify(tableRestaurantRepository).save(any(TableRestaurant.class)); // Verify save method was called
        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
        verify(areaRepository).findById(1L); // Verify area repository was called
    }

    @Test
    void createTableRestaurantShouldThrowExceptionWhenAreaNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
        verify(areaRepository).findById(1L); // Verify area repository was called
    }

    @Test
    void createTableRestaurantShouldThrowExceptionWhenTableTypeNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
    }

    @Test
    void createTableRestaurantShouldThrowExceptionWhenTableNameExisted() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(tableRestaurantRepository.existsByNameAndArea_Id(tableRestaurantRequest.getName(), tableRestaurantRequest.getAreaId())).thenReturn(true);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(tableRestaurantRequest);
        });

        assertEquals(ErrorCode.TABLE_NAME_EXISTED, e.getErrorCode());

        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
        verify(areaRepository).findById(1L); // Verify area repository was called
        verify(tableRestaurantRepository).existsByNameAndArea_Id(tableRestaurantRequest.getName(), tableRestaurantRequest.getAreaId()); // Verify table restaurant repository was called
    }


    @Test
    void updateTableRestaurantShouldUpdateTableRestaurantWhenDataIsValid() {
        tableRestaurantRequest.setName("Another Test Table Restaurant");
        tableRestaurantRequest.setNumberChairs(5);

        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(tableRestaurantRepository.save(any(TableRestaurant.class))).thenReturn(tableRestaurant);

        // Act: Call the method under test
        TableRestaurantResponse result = tableRestaurantService.updateTableByTableId(1L, tableRestaurantRequest);

        // Assert: Verify the result and interactions
        assertEquals(1L, result.getId()); // Check the returned Table Restaurant
        assertEquals("Another Test Table Restaurant", result.getName()); // Check the name
        assertEquals(5, result.getNumberChairs()); // Check the number of chairs
        assertEquals(1L, result.getTableType().getId());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
        verify(tableRestaurantRepository).save(any(TableRestaurant.class)); // Verify save method was called
    }

    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableNotFound() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(1L, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableNameExisted() {
        tableRestaurantRequest.setName("Another Table Name");

        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));
        when(tableRestaurantRepository.existsByNameAndArea_Id(tableRestaurantRequest.getName(), tableRestaurantRequest.getAreaId())).thenReturn(true);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(1L, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.TABLE_NAME_EXISTED, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
        verify(tableRestaurantRepository).existsByNameAndArea_Id(tableRestaurantRequest.getName(), tableRestaurantRequest.getAreaId());
    }

    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableTypeNotFound() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(1L, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
    }

    @Test
    void deleteTableRestaurantShouldDeleteTableRestaurantWhenDataIsValid() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));

        // Act: Call the method under test
        tableRestaurantService.deleteTableById(1L);

        // Assert: Verify the result and interactions
        assertTrue(tableRestaurant.isHidden()); // Check if the table is marked as hidden

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void deleteTableRestaurantShouldThrowExceptionWhenTableNotFound() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.deleteTableById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void findTableRestaurantByIdShouldReturnTableRestaurantWhenExists() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));

        // Act: Call the method under test
        TableRestaurant result = tableRestaurantService.findById(1L);

        // Assert: Verify the result and interactions
        assertEquals(tableRestaurant, result); // Check the returned Table Restaurant

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void findTableRestaurantByIdShouldThrowExceptionWhenNotExists() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.findById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void findTableRestaurantByIdToResponseShouldReturnTableRestaurantResponseWhenExists() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));

        // Act: Call the method under test
        TableRestaurantResponse result = tableRestaurantService.findTableRestaurantByIdToResponse(1L);

        // Assert: Verify the result and interactions
        assertEquals(tableRestaurantResponse, result); // Check the returned Table Restaurant Response

        verify(tableRestaurantRepository).findById(1L); // Verify findById method was called
    }

    @Test
    void findTableRestaurantByIdToResponseShouldThrowExceptionWhenNotExists() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.findTableRestaurantByIdToResponse(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L);
    }

    @Test
    void updateTablesShouldUpdateTablePositionsWhenDataIsValid() {
        tableRestaurantResponse.setId(1L);
        tableRestaurantResponse.setPositionX(10); // Example position
        tableRestaurantResponse.setPositionY(20);

        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.of(tableRestaurant));
        when(tableRestaurantRepository.save(any(TableRestaurant.class))).thenReturn(tableRestaurant);

        tableRestaurantService.updateTables(List.of(tableRestaurantResponse));

        assertEquals(10, tableRestaurant.getPositionX());
        assertEquals(20, tableRestaurant.getPositionY());

        verify(tableRestaurantRepository).findById(1L);
        verify(tableRestaurantRepository).save(tableRestaurant);
    }

    @Test
    void updateTablesShouldThrowExceptionWhenTableNotFound() {
        when(tableRestaurantRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTables(List.of(tableRestaurantResponse));
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableRestaurantRepository).findById(1L);
    }

    @Test
    void getTablesByAreaIdShouldReturnListOfTableRestaurantResponse() {
        tableRestaurant.setId(1L);
        when(tableRestaurantRepository.findByArea_IdAndHidden(1L, false)).thenReturn(List.of(tableRestaurant));
        when(scheduleRepository.findByTableIdAndBookedDate(1L, LocalDate.now())).thenReturn(List.of(new Schedule()));

        List<TableRestaurantResponse> actualResponse = tableRestaurantService.getTableByAreaId(1L);

        assertEquals(1, actualResponse.size());
        assertEquals(1L, actualResponse.get(0).getId());
        assertEquals("Test Table Restaurant", actualResponse.get(0).getName());
        assertTrue(actualResponse.get(0).isBooked());

        verify(tableRestaurantRepository).findByArea_IdAndHidden(1L, false);
        verify(scheduleRepository).findByTableIdAndBookedDate(1L, LocalDate.now());
    }

    @Test
    void getTableByAreaIdHaveOrderShouldReturnListOfTableRestaurantResponse() {
        tableRestaurant.setId(1L);
        tableRestaurant.setOrderCurrent(1L);

        when(tableRestaurantRepository.findByArea_IdAndHidden(1L, false)).thenReturn(List.of(tableRestaurant));

        List<TableRestaurantResponse> actualResponse = tableRestaurantService.getTableByAreaIdHaveOrder(1L);

        assertEquals(1, actualResponse.size());
        assertEquals(1L, actualResponse.get(0).getId());
        assertEquals("Test Table Restaurant", actualResponse.get(0).getName());

        verify(tableRestaurantRepository).findByArea_IdAndHidden(1L, false);
    }

    @Test
    void createManyTableShouldCreateMultipleTables() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(tableRestaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TableRestaurantResponse> result = tableRestaurantService.createManyTable(3, tableRestaurantRequest);

        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("Test Table Restaurant-" + (i + 1), result.get(i).getName());
        }

        verify(tableRestaurantRepository, times(3)).save(any(TableRestaurant.class)); // Verify save method was called 3 times
        verify(tableTypeRepository, times(3)).findById(1L); // Verify table type repository was called
        verify(areaRepository, times(3)).findById(1L); // Verify area repository was called
    }

    @Test
    void createManyTableShouldThrowExceptionWhenAreaNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(3, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
        verify(areaRepository).findById(1L); // Verify area repository was called
    }

    @Test
    void createManyTableShouldThrowExceptionWhenTableTypeNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(3, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L); // Verify table type repository was called
    }

    @Test
    void createManyTableShouldThrowExceptionWhenMaxTableExceeded() {
        when(areaRepository.findByRestaurant_Id(1L)).thenReturn(List.of(area));
        when(restaurantService.getRestaurantById(1L)).thenReturn(restaurant);
        when(tableRestaurantRepository.findByArea_IdAndHidden(1L, false)).thenReturn(List.of(new TableRestaurant(), new TableRestaurant()));

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(5, tableRestaurantRequest);
        });

        assertEquals(ErrorCode.MAX_TABLE, e.getErrorCode());

        verify(areaRepository).findByRestaurant_Id(1L); // Verify area repository was called
        verify(restaurantService).getRestaurantById(1L); // Verify restaurant service was called
        verify(tableRestaurantRepository).findByArea_IdAndHidden(1L, false); // Verify table restaurant repository was called
    }

    @Test
    void createManyTableShouldContinueCreatingTablesWhenSomeAlreadyExist() {
        int numbers = 2;
        TableRestaurant existingTable = new TableRestaurant();
        existingTable.setName("Test Table Restaurant-3");

        when(areaRepository.findByRestaurant_Id(1L)).thenReturn(List.of(area));
        when(tableRestaurantRepository.findByArea_IdAndHidden(1L, false)).thenReturn(List.of(existingTable));
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));
        when(areaRepository.findById(1L)).thenReturn(Optional.of(area));
        when(restaurantService.getRestaurantById(1L)).thenReturn(restaurant);
        when(tableRestaurantRepository.findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(1L, "Test Table Restaurant-")).thenReturn(existingTable);
        when(tableRestaurantRepository.save(any(TableRestaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<TableRestaurantResponse> result = tableRestaurantService.createManyTable(numbers, tableRestaurantRequest);

        assertEquals(numbers, result.size());
        assertEquals("Test Table Restaurant-4", result.get(0).getName());
        assertEquals("Test Table Restaurant-5", result.get(1).getName());

        verify(tableRestaurantRepository, times(2)).save(any(TableRestaurant.class));
        verify(areaRepository).findByRestaurant_Id(1L); // Verify area repository was called
        verify(restaurantService).getRestaurantById(1L); // Verify restaurant service was called
        verify(tableRestaurantRepository).findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(1L, "Test Table Restaurant-"); // Verify table restaurant repository was called
        verify(tableRestaurantRepository).findByArea_IdAndHidden(1L, false); // Verify table restaurant repository was called
        verify(tableTypeRepository, times(2)).findById(1L); // Verify table type repository was called
        verify(areaRepository, times(2)).findById(1L); // Verify area repository was called
    }
}
