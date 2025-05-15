package com.restaurent.manager.service;

import com.restaurent.manager.dto.PagingResult;
import com.restaurent.manager.dto.request.employee.EmployeeLoginRequest;
import com.restaurent.manager.dto.request.employee.EmployeeRequest;
import com.restaurent.manager.dto.request.employee.EmployeeUpdateInformationRequest;
import com.restaurent.manager.dto.response.AuthenticationResponse;
import com.restaurent.manager.dto.response.EmployeeResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.EmployeeMapper;
import com.restaurent.manager.repository.EmployeeRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.RoleRepository;
import com.restaurent.manager.service.impl.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private IAccountService accountService;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    // Biến mẫu
    private Employee sampleEmployee;
    private EmployeeRequest sampleRequest;
    private EmployeeUpdateInformationRequest sampleUpdateRequest;
    private EmployeeResponse sampleResponse;
    private Restaurant sampleRestaurant;
    private Role sampleRole;
    private Account sampleAccount;

    @BeforeEach
    public void setUp() {
        sampleEmployee = new Employee();
        sampleEmployee.setId(1L);
        sampleEmployee.setUsername("employee1");
        sampleEmployee.setPassword(new BCryptPasswordEncoder().encode("password"));
        sampleEmployee.setEmployeeName("Employee Name");

        sampleRequest = new EmployeeRequest();
        sampleRequest.setUsername("employee1");
        sampleRequest.setPassword("password");
        sampleRequest.setAccountId(1L);
        sampleRequest.setRoleId(1L);

        sampleUpdateRequest = new EmployeeUpdateInformationRequest();
        sampleUpdateRequest.setRoleId(1L);

        sampleResponse = new EmployeeResponse();
        sampleResponse.setId(String.valueOf(1L));
        sampleResponse.setUsername("employee1");

        sampleRestaurant = new Restaurant();
        sampleRestaurant.setId(1L);

        sampleRole = new Role();
        sampleRole.setId(1L);
        sampleRole.setName("EMPLOYEE");

        sampleAccount = new Account();
        sampleAccount.setId(1L);
        sampleAccount.setRestaurant(sampleRestaurant);

        // Inject signerKey vào service (do @Value không hoạt động trong test)
        ReflectionTestUtils.setField(employeeService, "signerKey", "o7Mw6BZjVh0yALUyzY3kE4ZABkIlonmY");
    }

    /**
     * Test createEmployee khi tạo thành công.
     * Kiểm tra nhánh existsByUsername = false. role tìm thấy.
     */
    // TestcaseID: EPS-1
    @Test
    public void testCreateEmployee_Success() {
        sampleRestaurant.setEmployees(new HashSet<>());
        sampleRole.setEmployees(new HashSet<>());

        when(restaurantRepository.findByAccount_Id(1L)).thenReturn(sampleRestaurant);
        when(employeeRepository.existsByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(false);
        when(employeeMapper.toEmployee(sampleRequest)).thenReturn(sampleEmployee);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
        when(employeeMapper.toEmployeeResponse(sampleEmployee)).thenReturn(sampleResponse);

        EmployeeResponse result = employeeService.createEmployee(sampleRequest);

        assertNotNull(result);
        assertEquals("employee1", result.getUsername());
        verify(employeeRepository).save(sampleEmployee);
    }

    /**
     * Test createEmployee khi username đã tồn tại.
     * Kiểm tra nhánh existsByUsername = true.
     */
    // TestcaseID: EPS-2
    @Test
    public void testCreateEmployee_UserExisted() {
        when(restaurantRepository.findByAccount_Id(1L)).thenReturn(sampleRestaurant);
        when(employeeRepository.existsByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.createEmployee(sampleRequest);
        });

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
    }

    /**
     * Test createEmployee khi role không tồn tại.
     * Kiểm tra nhánh roleRepository.findById ném ROLE_NOT_EXISTED.
     */
    // TestcaseID: EPS-3
    @Test
    public void testCreateEmployee_RoleNotExisted() {
        when(restaurantRepository.findByAccount_Id(1L)).thenReturn(sampleRestaurant);
        when(employeeRepository.existsByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(false);
        when(employeeMapper.toEmployee(sampleRequest)).thenReturn(sampleEmployee);
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.createEmployee(sampleRequest);
        });

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
        verify(employeeRepository, never()).save(any());
    }

    /**
     * Test updateEmployee khi cập nhật thành công.
     */
    // TestcaseID: EPS-4
    @Test
    public void testUpdateEmployee_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);
        when(employeeMapper.toEmployeeResponse(sampleEmployee)).thenReturn(sampleResponse);

        EmployeeResponse result = employeeService.updateEmployee(1L, sampleUpdateRequest);

        assertNotNull(result);
        verify(employeeMapper).updateRestaurant(sampleEmployee, sampleUpdateRequest);
    }

    /**
     * Test updateEmployee khi role không tồn tại.
     * Kiểm tra nhánh ném ROLE_NOT_EXISTED.
     */
    // TestcaseID: EPS-5
    @Test
    public void testUpdateEmployee_RoleNotExisted() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.updateEmployee(1L, sampleUpdateRequest);
        });

        assertEquals(ErrorCode.ROLE_NOT_EXISTED, exception.getErrorCode());
    }

    /**
     * Test findEmployeeById khi tìm thấy.
     * Kiểm tra nhánh tìm thấy.
     */
    // TestcaseID: EPS-6
    @Test
    public void testFindEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        Employee result = employeeService.findEmployeeById(1L);

        assertNotNull(result);
        assertEquals("employee1", result.getUsername());
    }

    /**
     * Test findEmployeeById khi không tìm thấy.
     * Kiểm tra nhánh không tìm thấy.
     */
    // TestcaseID: EPS-7
    @Test
    public void testFindEmployeeById_NotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.findEmployeeById(1L);
        });

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    /**
     * Test findEmployeeByIdConvertDTO khi tìm thấy.
     */
    // TestcaseID: EPS-8
    @Test
    public void testFindEmployeeByIdConvertDTO() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeMapper.toEmployeeResponse(sampleEmployee)).thenReturn(sampleResponse);

        EmployeeResponse result = employeeService.findEmployeeByIdConvertDTO(1L);

        assertNotNull(result);
        assertEquals("employee1", result.getUsername());
    }

    /**
     * Test deleteEmployee khi xóa thành công.
     */
    // TestcaseID: EPS-9
    @Test
    public void testDeleteEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(sampleEmployee);
    }

    /**
     * Test findEmployeesByAccountId khi restaurant tồn tại.
     * Kiểm tra nhánh restaurant != null.
     */
    // TestcaseID: EPS-10
    @Test
    public void testFindEmployeesByAccountId_RestaurantExists() {
        Pageable pageable = PageRequest.of(0, 10);
        when(restaurantRepository.findByAccount_Id(1L)).thenReturn(sampleRestaurant);
        when(employeeRepository.findByRestaurant_IdAndEmployeeNameContaining(1L, "emp", pageable)).thenReturn(List.of(sampleEmployee));
        when(employeeRepository.countByRestaurant_IdAndEmployeeNameContaining(1L, "emp")).thenReturn(1);
        when(employeeMapper.toEmployeeResponse(sampleEmployee)).thenReturn(sampleResponse);

        PagingResult<EmployeeResponse> result = employeeService.findEmployeesByAccountId(1L, pageable, "emp");

        assertEquals(1, result.getResults().size());
        assertEquals(1L, result.getTotalItems());
    }

    /**
     * Test findEmployeesByAccountId khi restaurant không tồn tại.
     * Kiểm tra nhánh restaurant == null.
     */
    // TestcaseID: EPS-11
    @Test
    public void testFindEmployeesByAccountId_RestaurantNotExists() {
        Pageable pageable = PageRequest.of(0, 10);
        when(restaurantRepository.findByAccount_Id(1L)).thenReturn(null);

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.findEmployeesByAccountId(1L, pageable, "emp");
        });

        assertEquals(ErrorCode.NOT_EXIST, exception.getErrorCode());
    }

    /**
     * Test authenticated khi xác thực thành công.
     * Kiểm tra nhánh authenticated = true.
     */
    // TestcaseID: EPS-12
    @Test
    public void testAuthenticated_Success() {
        EmployeeLoginRequest request = new EmployeeLoginRequest();
        request.setPhoneNumberOfRestaurant("123456789");
        request.setUsername("employee1");
        request.setPassword("password");

        sampleEmployee.setRole(sampleRole);
        sampleEmployee.setRestaurant(sampleRestaurant);
        sampleRestaurant.setAccount(sampleAccount);
        Package restaurantPackage = new Package();
        Permission permission = new Permission();
        permission.setName("PERM_1");
        restaurantPackage.setPermissions(Set.of(permission));
        sampleRestaurant.setRestaurantPackage(restaurantPackage);

        when(accountService.findAccountByPhoneNumber("123456789")).thenReturn(sampleAccount);
        when(employeeRepository.findByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(Optional.of(sampleEmployee));

        AuthenticationResponse result = employeeService.authenticated(request);

        assertTrue(result.isAuthenticated());
        assertNotNull(result.getToken());
    }

    /**
     * Test authenticated khi mật khẩu sai.
     * Kiểm tra nhánh authenticated = false.
     */
    // TestcaseID: EPS-13
    @Test
    public void testAuthenticated_PasswordIncorrect() {
        EmployeeLoginRequest request = new EmployeeLoginRequest();
        request.setPhoneNumberOfRestaurant("123456789");
        request.setUsername("employee1");
        request.setPassword("wrongpassword");

        when(accountService.findAccountByPhoneNumber("123456789")).thenReturn(sampleAccount);
        when(employeeRepository.findByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(Optional.of(sampleEmployee));

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.authenticated(request);
        });

        assertEquals(ErrorCode.PASSWORD_INCORRECT, exception.getErrorCode());
    }

    /**
     * Test authenticated khi employee không tồn tại.
     */
    // TestcaseID: EPS-14
    @Test
    public void testAuthenticated_EmployeeNotExist() {
        EmployeeLoginRequest request = new EmployeeLoginRequest();
        request.setPhoneNumberOfRestaurant("123456789");
        request.setUsername("employee1");
        request.setPassword("password");

        when(accountService.findAccountByPhoneNumber("123456789")).thenReturn(sampleAccount);
        when(employeeRepository.findByUsernameAndRestaurant_Id("employee1", 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            employeeService.authenticated(request);
        });

        assertEquals(ErrorCode.EMPLOYEE_NOT_EXIST, exception.getErrorCode());
    }

    /**
     * Test updateEmployeePassword khi cập nhật thành công.
     */
    // TestcaseID: EPS-15
    @Test
    public void testUpdateEmployeePassword() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepository.save(sampleEmployee)).thenReturn(sampleEmployee);

        employeeService.updateEmployeePassword(1L, "newpassword");

        verify(employeeRepository).save(sampleEmployee);
    }

    /**
     * Test buildScope khi role và restaurant không null.
     * Kiểm tra nhánh role != null và restaurant != null.
     */
    // TestcaseID: EPS-16
    @Test
    public void testBuildScope_WithRoleAndRestaurant() {
        sampleEmployee.setRole(sampleRole);
        sampleEmployee.setRestaurant(sampleRestaurant);
        Package restaurantPackage = new Package();
        Permission permission = new Permission();
        permission.setName("PERM_1");
        restaurantPackage.setPermissions(Set.of(permission));
        sampleRestaurant.setRestaurantPackage(restaurantPackage);

        String scope = employeeService.buildScope(sampleEmployee);

        assertEquals("ROLE_EMPLOYEE PERM_1", scope);
    }

    /**
     * Test buildScope khi role null.
     * Kiểm tra nhánh role == null.
     */
    // TestcaseID: EPS-17
    @Test
    public void testBuildScope_RoleNull() {
        sampleEmployee.setRole(null);
        sampleEmployee.setRestaurant(sampleRestaurant);

        String scope = employeeService.buildScope(sampleEmployee);

        assertEquals("", scope);
    }

    /**
     * Test buildScope khi restaurant null.
     * Kiểm tra nhánh restaurant == null.
     */
    // TestcaseID: EPS-18
    @Test
    public void testBuildScope_RestaurantNull() {
        sampleEmployee.setRole(sampleRole);
        sampleEmployee.setRestaurant(null);

        String scope = employeeService.buildScope(sampleEmployee);

        assertEquals("ROLE_EMPLOYEE", scope);
    }

    /**
     * Test generateToken khi tạo token thành công.
     */
    // TestcaseID: EPS-19
    @Test
    public void testGenerateToken_Success() {
        sampleEmployee.setRole(sampleRole);
        sampleEmployee.setRestaurant(sampleRestaurant);
        sampleRestaurant.setAccount(sampleAccount);
        Package restaurantPackage = new Package();
        Permission permission = new Permission();
        permission.setName("PERM_1");
        restaurantPackage.setPermissions(Set.of(permission));
        sampleRestaurant.setRestaurantPackage(restaurantPackage);

        String token = employeeService.generateToken(sampleEmployee);

        assertNotNull(token);
    }
}