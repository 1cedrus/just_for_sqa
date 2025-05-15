package com.restaurent.manager.repository;

import com.restaurent.manager.entity.Employee;
import com.restaurent.manager.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee sampleEmployee;
    private Restaurant sampleRestaurant;

    @BeforeEach
    public void setUp() {
        // Tạo dữ liệu mẫu
        Restaurant setupRestaurant = new Restaurant();

        Employee setupEmployee = new Employee();
        setupEmployee.setEmployeeName("John Doe");
        setupEmployee.setUsername("johndoe");
        setupEmployee.setRestaurant(setupRestaurant);

        sampleRestaurant = entityManager.persist(setupRestaurant);
        sampleEmployee = entityManager.persist(setupEmployee);
        entityManager.flush();
    }

    // Test findByRestaurant_IdAndEmployeeNameContaining - Thành công
    // TestcaseID: EPR-1
    @Test
    public void testFindByRestaurantIdAndEmployeeNameContaining_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> result = employeeRepository.findByRestaurant_IdAndEmployeeNameContaining(
            sampleRestaurant.getId(), "John", pageable);
        assertEquals(1, result.size(), "Phải tìm thấy 1 employee với restaurantId " + sampleRestaurant.getId() + " và query 'John'");
        assertEquals("John Doe", result.get(0).getEmployeeName(), "Tên employee phải là 'John Doe'");
    }

    // Test findByRestaurant_IdAndEmployeeNameContaining - Thất bại
    // TestcaseID: EPR-2
    @Test
    public void testFindByRestaurantIdAndEmployeeNameContaining_NoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Employee> result = employeeRepository.findByRestaurant_IdAndEmployeeNameContaining(
            sampleRestaurant.getId(), "Jane", pageable);
        assertTrue(result.isEmpty(), "Không được tìm thấy employee nào với query 'Jane'");
    }

    // Test countByRestaurant_IdAndEmployeeNameContaining - Thành công
    // TestcaseID: EPR-3
    @Test
    public void testCountByRestaurantIdAndEmployeeNameContaining_Success() {
        int count = employeeRepository.countByRestaurant_IdAndEmployeeNameContaining(
            sampleRestaurant.getId(), "John");
        assertEquals(1, count, "Phải đếm được 1 employee với restaurantId " + sampleRestaurant.getId() + " và query 'John'");
    }

    // Test countByRestaurant_IdAndEmployeeNameContaining - Thất bại
    // TestcaseID: EPR-4
    @Test
    public void testCountByRestaurantIdAndEmployeeNameContaining_NoMatch() {
        int count = employeeRepository.countByRestaurant_IdAndEmployeeNameContaining(
            sampleRestaurant.getId(), "Jane");
        assertEquals(0, count, "Phải đếm được 0 employee với query 'Jane'");
    }

    // Test findByUsernameAndRestaurant_Id - Thành công
    // TestcaseID: EPR-5
    @Test
    public void testFindByUsernameAndRestaurantId_Success() {
        Optional<Employee> result = employeeRepository.findByUsernameAndRestaurant_Id("johndoe", sampleRestaurant.getId());
        assertTrue(result.isPresent(), "Phải tìm thấy employee với username 'johndoe' và restaurantId " + sampleRestaurant.getId());
        assertEquals("John Doe", result.get().getEmployeeName(), "Tên employee phải là 'John Doe'");
    }

    // Test findByUsernameAndRestaurant_Id - Thất bại
    // TestcaseID: EPR-6
    @Test
    public void testFindByUsernameAndRestaurantId_NoMatch() {
        Optional<Employee> result = employeeRepository.findByUsernameAndRestaurant_Id("janedoe", sampleRestaurant.getId());
        assertFalse(result.isPresent(), "Không được tìm thấy employee với username 'janedoe'");
    }

    // Test existsByUsernameAndRestaurant_Id - Thành công
    // TestcaseID: EPR-7
    @Test
    public void testExistsByUsernameAndRestaurantId_Success() {
        boolean exists = employeeRepository.existsByUsernameAndRestaurant_Id("johndoe", sampleRestaurant.getId());
        assertTrue(exists, "Phải tồn tại employee với username 'johndoe' và restaurantId " + sampleRestaurant.getId());
    }

    // Test existsByUsernameAndRestaurant_Id - Thất bại
    // TestcaseID: EPR-8
    @Test
    public void testExistsByUsernameAndRestaurantId_NoMatch() {
        boolean exists = employeeRepository.existsByUsernameAndRestaurant_Id("janedoe", sampleRestaurant.getId());
        assertFalse(exists, "Không được tồn tại employee với username 'janedoe'");
    }
}