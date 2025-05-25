package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TableTypeRequest;
import com.restaurent.manager.dto.request.TableTypeUpdateRequest;
import com.restaurent.manager.dto.response.TableTypeResponse;
import com.restaurent.manager.entity.TableType;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.TableTypeRepository;
import com.restaurent.manager.service.impl.TableTypeService;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class TableTypeServiceTest {

    @Autowired
    private TableTypeService tableTypeService;

    @Autowired
    private TableTypeRepository tableTypeRepository;

    @MockBean
    private Clock clock;

    private TableType tableType;

    @BeforeEach
    void setup() {
        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
                fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Create a table type for testing
        tableType = new TableType();
        tableType.setName("Test TableType");
        tableType = tableTypeRepository.saveAndFlush(tableType);
    }

    // TTS-1
    @Test
    void createTableTypeShouldReturnTableTypeResponse() {
        TableTypeRequest request = new TableTypeRequest();
        request.setName("New TableType");

        TableTypeResponse result = tableTypeService.createTableType(request);

        assertNotNull(result);
        assertEquals("New TableType", result.getName());
        assertNotNull(result.getId());

        // Verify in database
        List<TableType> tableTypes = tableTypeRepository.findAll();
        assertTrue(tableTypes.stream().anyMatch(tt -> "New TableType".equals(tt.getName())));
    }

    // TTS-2
    @Test
    void getTableTypesShouldReturnListOfTableTypeResponse() {
        List<TableTypeResponse> result = tableTypeService.getTableTypes();

        assertNotNull(result);
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(tt -> "Test TableType".equals(tt.getName())));
    }

    // TTS-3
    @Test
    void deleteTableTypeShouldDeleteTableTypeWhenExists() {
        Long tableTypeId = Long.valueOf(tableType.getId());
        tableTypeService.deleteTableType(tableTypeId);

        // Verify in database
        assertFalse(tableTypeRepository.findById(tableTypeId).isPresent());
    }

    // TTS-4
    @Test
    void deleteTableTypeShouldThrowExceptionWhenNotFound() {
        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.deleteTableType(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TTS-5
    @Test
    void updateTableTypeShouldReturnUpdatedTableTypeResponse() {
        TableTypeUpdateRequest request = new TableTypeUpdateRequest();
        request.setId(Long.valueOf(tableType.getId()));
        request.setName("Updated TableType");

        TableTypeResponse result = tableTypeService.updateTableType(request);

        assertNotNull(result);
        assertEquals(Long.valueOf(tableType.getId()), result.getId());
        assertEquals("Updated TableType", result.getName());

        // Verify in database
        TableType updatedTableType = tableTypeRepository.findById(Long.valueOf(tableType.getId())).orElse(null);
        assertNotNull(updatedTableType);
        assertEquals("Updated TableType", updatedTableType.getName());
    }

    // TTS-6
    @Test
    void updateTableTypeShouldThrowExceptionWhenNotFound() {
        TableTypeUpdateRequest request = new TableTypeUpdateRequest();
        request.setId(999L);
        request.setName("Updated TableType");

        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.updateTableType(request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // TTS-7
    @Test
    void findTableTypeByIdShouldReturnTableTypeWhenExists() {
        TableType result = tableTypeService.findTableTypeById(Long.valueOf(tableType.getId()));

        assertNotNull(result);
        assertEquals(tableType.getId(), result.getId());
        assertEquals("Test TableType", result.getName());
    }

    // TTS-8
    @Test
    void findTableTypeByIdShouldThrowExceptionWhenNotExists() {
        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.findTableTypeById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }
}