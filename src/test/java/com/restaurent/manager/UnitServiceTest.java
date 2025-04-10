package com.restaurent.manager;

import com.restaurent.manager.dto.request.UnitRequest;
import com.restaurent.manager.dto.response.UnitResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Unit;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.UnitMapper;
import com.restaurent.manager.mapper.UnitMapperImpl;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.repository.UnitRepository;
import com.restaurent.manager.service.impl.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnitServiceTest {

    @Spy
    UnitMapper unitMapper = new UnitMapperImpl();

    @Mock
    UnitRepository unitRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    DishRepository dishRepository;

    @InjectMocks
    UnitService unitService;

    UnitRequest unitRequest;
    Unit unit;
    UnitResponse unitResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        unitRequest = new UnitRequest();
        unitResponse = new UnitResponse();
        unit = new Unit();

        unit.setId(1L);
        unit.setName("Test Unit");

        unitRequest.setName("Test Unit");

        unitResponse.setId(1L);
        unitResponse.setName("Test Unit");
    }

    @Test
    void createUnitShouldReturnUnitResponse() {
        when(accountRepository.findById(unitRequest.getAccountId())).thenReturn(Optional.of(new Account()));
        when(unitRepository.save(any(Unit.class))).thenAnswer(invocation -> {
            Unit savedUnit = invocation.getArgument(0);
            savedUnit.setId(1L); // Simulate the ID being set by the database
            return savedUnit;
        });

        UnitResponse actualResponse = unitService.createUnit(unitRequest);

        // Verify that the unit was saved
        assertEquals(1L, actualResponse.getId()); // Verify restaurant's VAT is set
        assertEquals("Test Unit", actualResponse.getName()); // Verify restaurant's VAT is set

        verify(accountRepository).findById(unitRequest.getAccountId());
    }

    @Test
    void createUnitShouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(unitRequest.getAccountId())).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            unitService.createUnit(unitRequest);
        });

        // Verify the error code
        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        // Verify that the account was looked up
        verify(accountRepository).findById(unitRequest.getAccountId());
    }

    @Test
    void getUnitsByAccountIdShouldReturnListOfUnitResponse() {
        when(unitRepository.getUnitsByAccount_Id(1L)).thenReturn(List.of(unit));

        List<UnitResponse> actualResponse = unitService.getUnitsByAccountId(1L);

        assertEquals(1, actualResponse.size());
        assertEquals(1L, actualResponse.getFirst().getId());
        assertEquals("Test Unit", actualResponse.getFirst().getName());

        verify(unitRepository).getUnitsByAccount_Id(1L);
    }

    @Test
    void updateUnitShouldReturnUpdatedUnitResponse() {
        unitRequest.setName("Another Test Unit");

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(Unit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UnitResponse actualResponse = unitService.updateUnit(1L, unitRequest);

        assertEquals(1L, actualResponse.getId());
        assertEquals("Another Test Unit", actualResponse.getName());

        verify(unitRepository).findById(1L);
        verify(unitRepository).save(any(Unit.class));
    }

    @Test
    void updateUnitShouldThrowExceptionWhenUnitNotFound() {
        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            unitService.updateUnit(1L, unitRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(unitRepository).findById(1L);
    }

    @Test
    void deleteUnitByIdShouldDeleteUnitWhenNoDishExists() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(dishRepository.existsByUnit_Id(1L)).thenReturn(false);

        unitService.deleteUnitById(1L);

        // Verify that the unit was deleted
        verify(unitRepository).deleteById(1L);
    }

    @Test
    void deleteUnitByIdShouldSetHiddenWhenDishExists() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(dishRepository.existsByUnit_Id(1L)).thenReturn(true);

        unitService.deleteUnitById(1L);

        // Verify that the unit was set to hidden
        assertTrue(unit.isHidden());

        // Verify that the unit was saved
        verify(unitRepository).save(unit);
    }

    @Test
    void deleteUnitByIdShouldThrowExceptionWhenUnitNotFound() {
        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            unitService.deleteUnitById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(unitRepository).findById(1L);
    }


    @Test
    void findByIdShouldReturnUnitWhenExists() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        Unit actualUnit = unitService.findById(1L);

        assertEquals(1L, actualUnit.getId());
        assertEquals("Test Unit", actualUnit.getName());

        verify(unitRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowExceptionWhenNotExists() {
        when(unitRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            unitService.findById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(unitRepository).findById(1L);
    }
}
