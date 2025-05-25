package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.request.Table.TableRestaurantUpdateRequest;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.*;
import com.restaurent.manager.service.impl.RestaurantService;
import com.restaurent.manager.service.impl.TableRestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class TableRestaurantServiceTest {

    @Autowired
    private TableRestaurantService tableRestaurantService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private TableTypeRepository tableTypeRepository;

    @Autowired
    private TableRestaurantRepository tableRestaurantRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @MockBean
    private Clock clock;

    private Restaurant restaurant;
    private TableRestaurant tableRestaurant;
    private TableType tableType;
    private Permission tableMaxPermission;
    private Area area;
    private Long restaurantId;
    private Package restaurantPackage;
    private Customer customer;
    private Employee employee;
    private Role role;

    @BeforeEach
    void setup() {
        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
                fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Create permission first
        tableMaxPermission = new Permission();
        tableMaxPermission.setName("TABLE_MAX");
        tableMaxPermission.setMaximum(5);
        tableMaxPermission = permissionRepository.saveAndFlush(tableMaxPermission);

        // Create package
        restaurantPackage = new Package();
        restaurantPackage.setPackName("Test Package");
        restaurantPackage.setPricePerMonth(100.0);
        restaurantPackage.setPermissions(Set.of(tableMaxPermission));
        restaurantPackage = packageRepository.saveAndFlush(restaurantPackage);

        // Create restaurant
        restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setProvince("Test Province");
        restaurant.setDistrict("Test District");
        restaurant.setMoneyToPoint(1.0);
        restaurant.setPointToMoney(1.0);
        restaurant.setMonthsRegister(12);
        restaurant.setVatActive(false);
        restaurant.setDateCreated(LocalDate.now(clock));
        restaurant.setRestaurantPackage(restaurantPackage);
        restaurant = restaurantRepository.saveAndFlush(restaurant);
        restaurantId = restaurant.getId();

        // Create table type
        tableType = new TableType();
        tableType.setName("Standard Table");
        tableType = tableTypeRepository.saveAndFlush(tableType);

        // Create area
        area = new Area();
        area.setName("Main Area");
        area.setRestaurant(restaurant);
        area = areaRepository.saveAndFlush(area);

        // Create role
        role = new Role();
        role.setName("EMPLOYEE");
        role.setDescription("Employee role");
        role = roleRepository.saveAndFlush(role);

        // Create employee
        employee = new Employee();
        employee.setUsername("test_employee");
        employee.setPassword("password");
        employee.setEmployeeName("Test Employee");
        employee.setPhoneNumber("1234567890");
        employee.setRestaurant(restaurant);
        employee.setRole(role);
        employee = employeeRepository.saveAndFlush(employee);

        // Create customer
        customer = new Customer();
        customer.setName("Test Customer");
        customer.setPhoneNumber("123456789");
        customer.setAddress("Test Address");
        customer.setRestaurant(restaurant);
        customer.setCurrentPoint(0);
        customer.setTotalPoint(0);
        customer.setDateCreated(LocalDateTime.now(clock));
        customer = customerRepository.saveAndFlush(customer);

        // Create table restaurant
        tableRestaurant = new TableRestaurant();
        tableRestaurant.setName("Test Table Restaurant");
        tableRestaurant.setArea(area);
        tableRestaurant.setTableType(tableType);
        tableRestaurant.setNumberChairs(4);
        tableRestaurant.setPositionX(0);
        tableRestaurant.setPositionY(0);
        tableRestaurant.setHidden(false);
        tableRestaurant = tableRestaurantRepository.saveAndFlush(tableRestaurant);
    }

    // TRS-1
    @Test
    void createTableRestaurantShouldCreateTableRestaurantWhenDataIsValid() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("New Test Table");
        request.setNumberChairs(6);

        TableRestaurantResponse result = tableRestaurantService.createTable(request);

        assertNotNull(result);
        assertEquals("New Test Table", result.getName());
        assertEquals(6, result.getNumberChairs());

        // Verify in database
        List<TableRestaurant> tables = tableRestaurantRepository.findByArea_IdAndHidden(area.getId(), false);
        assertTrue(tables.stream().anyMatch(t -> "New Test Table".equals(t.getName())));
    }

    // TRS-2
    @Test
    void createTableRestaurantShouldThrowExceptionWhenAreaNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(999L);
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("New Test Table");
        request.setNumberChairs(6);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-3
    @Test
    void createTableRestaurantShouldThrowExceptionWhenTableTypeNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(999L);
        request.setName("New Test Table");
        request.setNumberChairs(6);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-4
    @Test
    void createTableRestaurantShouldThrowExceptionWhenTableNameExisted() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Test Table Restaurant"); // Same name as existing table
        request.setNumberChairs(6);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createTable(request);
        });

        assertEquals(ErrorCode.TABLE_NAME_EXISTED, e.getErrorCode());
    }

    // TRS-5
    @Test
    void updateTableRestaurantShouldUpdateTableRestaurantWhenDataIsValid() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Updated Table Restaurant");
        request.setNumberChairs(8);

        TableRestaurantResponse result = tableRestaurantService.updateTableByTableId(tableRestaurant.getId(), request);

        assertNotNull(result);
        assertEquals(tableRestaurant.getId(), result.getId());
        assertEquals("Updated Table Restaurant", result.getName());
        assertEquals(8, result.getNumberChairs());

        // Verify in database
        TableRestaurant updatedTable = tableRestaurantRepository.findById(tableRestaurant.getId()).orElse(null);
        assertNotNull(updatedTable);
        assertEquals("Updated Table Restaurant", updatedTable.getName());
        assertEquals(8, updatedTable.getNumberChairs());
    }

    // TRS-6
    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Updated Table Restaurant");
        request.setNumberChairs(8);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(999L, request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-7
    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableNameExisted() {
        // Create another table with a different name
        TableRestaurant anotherTable = new TableRestaurant();
        anotherTable.setName("Another Table");
        anotherTable.setArea(area);
        anotherTable.setTableType(tableType);
        anotherTable.setNumberChairs(4);
        anotherTable.setPositionX(0);
        anotherTable.setPositionY(0);
        anotherTable.setHidden(false);
        anotherTable = tableRestaurantRepository.saveAndFlush(anotherTable);

        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Another Table"); // Try to use existing name
        request.setNumberChairs(8);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(tableRestaurant.getId(), request);
        });

        assertEquals(ErrorCode.TABLE_NAME_EXISTED, e.getErrorCode());
    }

    // TRS-8
    @Test
    void updateTableRestaurantShouldThrowExceptionWhenTableTypeNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(999L);
        request.setName("Updated Table Restaurant");
        request.setNumberChairs(8);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTableByTableId(tableRestaurant.getId(), request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-9
    @Test
    void deleteTableRestaurantShouldDeleteTableRestaurantWhenDataIsValid() {
        tableRestaurantService.deleteTableById(tableRestaurant.getId());

        // Verify in database - table should be hidden
        TableRestaurant deletedTable = tableRestaurantRepository.findById(tableRestaurant.getId()).orElse(null);
        assertNotNull(deletedTable);
        assertTrue(deletedTable.isHidden());
    }

    // TRS-10
    @Test
    void deleteTableRestaurantShouldThrowExceptionWhenTableNotFound() {
        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.deleteTableById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-11
    @Test
    void findTableRestaurantByIdShouldReturnTableRestaurantWhenExists() {
        TableRestaurant result = tableRestaurantService.findById(tableRestaurant.getId());

        assertNotNull(result);
        assertEquals(tableRestaurant.getId(), result.getId());
        assertEquals("Test Table Restaurant", result.getName());
    }

    // TRS-12
    @Test
    void findTableRestaurantByIdShouldThrowExceptionWhenNotExists() {
        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.findById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-13
    @Test
    void findTableRestaurantByIdToResponseShouldReturnTableRestaurantResponseWhenExists() {
        TableRestaurantResponse result = tableRestaurantService
                .findTableRestaurantByIdToResponse(tableRestaurant.getId());

        assertNotNull(result);
        assertEquals(tableRestaurant.getId(), result.getId());
        assertEquals("Test Table Restaurant", result.getName());
    }

    // TRS-14
    @Test
    void findTableRestaurantByIdToResponseShouldThrowExceptionWhenNotExists() {
        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.findTableRestaurantByIdToResponse(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-15
    @Test
    void updateTablesShouldUpdateTablePositionsWhenDataIsValid() {
        TableRestaurantResponse response = new TableRestaurantResponse();
        response.setId(tableRestaurant.getId());
        response.setPositionX(10);
        response.setPositionY(20);

        tableRestaurantService.updateTables(List.of(response));

        // Verify in database
        TableRestaurant updatedTable = tableRestaurantRepository.findById(tableRestaurant.getId()).orElse(null);
        assertNotNull(updatedTable);
        assertEquals(10, updatedTable.getPositionX());
        assertEquals(20, updatedTable.getPositionY());
    }

    // TRS-16
    @Test
    void updateTablesShouldThrowExceptionWhenTableNotFound() {
        TableRestaurantResponse response = new TableRestaurantResponse();
        response.setId(999L);
        response.setPositionX(10);
        response.setPositionY(20);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.updateTables(List.of(response));
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-17
    @Test
    void getTablesByAreaIdShouldReturnListOfTableRestaurantResponse() {
        List<TableRestaurantResponse> result = tableRestaurantService.getTableByAreaId(area.getId());

        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(t -> "Test Table Restaurant".equals(t.getName())));
    }

    // TRS-18
    @Test
    void getTableByAreaIdHaveOrderShouldReturnListOfTableRestaurantResponse() {
        // Set an order current for the table
        tableRestaurant.setOrderCurrent(1L);
        tableRestaurantRepository.saveAndFlush(tableRestaurant);

        List<TableRestaurantResponse> result = tableRestaurantService.getTableByAreaIdHaveOrder(area.getId());

        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(t -> "Test Table Restaurant".equals(t.getName())));
    }

    // TRS-19
    @Test
    void createManyTableShouldCreateMultipleTables() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Bulk Table");
        request.setNumberChairs(4);

        List<TableRestaurantResponse> result = tableRestaurantService.createManyTable(3, request);

        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("Bulk Table-" + (i + 1), result.get(i).getName());
        }

        // Verify in database
        List<TableRestaurant> tables = tableRestaurantRepository.findByArea_IdAndHidden(area.getId(), false);
        long bulkTableCount = tables.stream().filter(t -> t.getName().startsWith("Bulk Table-")).count();
        assertEquals(3, bulkTableCount);
    }

    // TRS-20
    @Test
    void createManyTableShouldThrowExceptionWhenAreaNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(999L);
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Bulk Table");
        request.setNumberChairs(4);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(3, request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-21
    @Test
    void createManyTableShouldThrowExceptionWhenTableTypeNotFound() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(999L);
        request.setName("Bulk Table");
        request.setNumberChairs(4);

        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(3, request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TRS-22
    @Test
    void createManyTableShouldThrowExceptionWhenMaxTableExceeded() {
        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Bulk Table");
        request.setNumberChairs(4);

        // Try to create more tables than the maximum allowed (5)
        AppException e = assertThrows(AppException.class, () -> {
            tableRestaurantService.createManyTable(6, request);
        });

        assertEquals(ErrorCode.MAX_TABLE, e.getErrorCode());
    }

    // TRS-23
    @Test
    void createManyTableShouldContinueCreatingTablesWhenSomeAlreadyExist() {
        // Create an existing table with numbered name
        TableRestaurant existingTable = new TableRestaurant();
        existingTable.setName("Bulk Table-3");
        existingTable.setArea(area);
        existingTable.setTableType(tableType);
        existingTable.setNumberChairs(4);
        existingTable.setPositionX(0);
        existingTable.setPositionY(0);
        existingTable.setHidden(false);
        tableRestaurantRepository.saveAndFlush(existingTable);

        TableRestaurantRequest request = new TableRestaurantRequest();
        request.setRestaurantId(restaurantId);
        request.setAreaId(area.getId());
        request.setTableTypeId(Long.valueOf(tableType.getId()));
        request.setName("Bulk Table");
        request.setNumberChairs(4);

        List<TableRestaurantResponse> result = tableRestaurantService.createManyTable(2, request);

        assertEquals(2, result.size());
        // Should start from 4 since 3 already exists
        assertEquals("Bulk Table-4", result.get(0).getName());
        assertEquals("Bulk Table-5", result.get(1).getName());

        // Verify in database
        List<TableRestaurant> tables = tableRestaurantRepository.findByArea_IdAndHidden(area.getId(), false);
        assertTrue(tables.stream().anyMatch(t -> "Bulk Table-4".equals(t.getName())));
        assertTrue(tables.stream().anyMatch(t -> "Bulk Table-5".equals(t.getName())));
    }
}
