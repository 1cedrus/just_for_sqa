package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TableTypeRequest;
import com.restaurent.manager.dto.request.TableTypeUpdateRequest;
import com.restaurent.manager.dto.response.TableTypeResponse;
import com.restaurent.manager.entity.TableType;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.TableTypeMapper;
import com.restaurent.manager.mapper.TableTypeMapperImpl;
import com.restaurent.manager.repository.TableTypeRepository;
import com.restaurent.manager.service.impl.TableTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TableTypeServiceTest {
    @Mock
    TableTypeRepository tableTypeRepository;

    @Spy
    TableTypeMapper tableTypeMapper = new TableTypeMapperImpl();

    @InjectMocks
    TableTypeService tableTypeService;

    TableTypeRequest tableTypeRequest;
    TableTypeUpdateRequest tableTypeUpdateRequest;
    TableType tableType;
    TableTypeResponse tableTypeResponse;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        tableTypeRequest = new TableTypeRequest();
        tableTypeUpdateRequest = new TableTypeUpdateRequest();
        tableType = new TableType();
        tableTypeResponse = new TableTypeResponse();

        tableTypeRequest.setName("Test TableType");

        tableTypeUpdateRequest.setId(1L);

        tableType.setId(1);
        tableType.setName("Test TableType");

        tableTypeResponse.setId(1L);
        tableTypeResponse.setName("Test TableType");
    }

    // TTS-1
    @Test
    void createTableTypeShouldReturnTableTypeResponse() {
        when(tableTypeRepository.save(any(TableType.class))).thenAnswer(invocation -> {
            TableType saved = invocation.getArgument(0);
            saved.setId(1); // Simulate the ID being set by the repository
            return saved;
        });

        TableTypeResponse actualResponse = tableTypeService.createTableType(tableTypeRequest);

        assertEquals(1L, actualResponse.getId());
        assertEquals("Test TableType", actualResponse.getName());
    }

    // TTS-2
    @Test
    void getTableTypesShouldReturnListOfTableTypeResponse() {
        when(tableTypeRepository.findAll()).thenReturn(List.of(tableType));

        List<TableTypeResponse> actualResponse = tableTypeService.getTableTypes();

        assertEquals(1, actualResponse.size());
        assertEquals(1L, actualResponse.getFirst().getId());
        assertEquals("Test TableType", actualResponse.getFirst().getName());

        verify(tableTypeRepository).findAll();
    }

    // TTS-3
    @Test
    void deleteTableTypeShouldDeleteTableTypeWhenExists() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));

        tableTypeService.deleteTableType(1L);

        verify(tableTypeRepository).delete(tableType);
    }

    // TTS-4
    @Test
    void deleteTableTypeShouldThrowExceptionWhenNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.deleteTableType(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L);
    }

    // TTS-5
    @Test
    void updateTableTypeShouldReturnUpdatedTableTypeResponse() {
        tableTypeUpdateRequest.setName("Another Test TableType");

        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));

        TableTypeResponse actualResponse = tableTypeService.updateTableType(tableTypeUpdateRequest);

        assertEquals(1L, actualResponse.getId());
        assertEquals("Another Test TableType", actualResponse.getName());

        verify(tableTypeRepository).findById(1L);
        verify(tableTypeRepository).save(any(TableType.class));
    }

    // TTS-6
    @Test
    void updateTableTypeShouldThrowExceptionWhenNotFound() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.updateTableType(tableTypeUpdateRequest);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L);
    }

    // TTS-7
    @Test
    void findTableTypeByIdShouldReturnTableTypeWhenExists() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.of(tableType));

        TableType actualTableType = tableTypeService.findTableTypeById(1L);

        assertEquals(1L, actualTableType.getId());
        assertEquals("Test TableType", actualTableType.getName());

        verify(tableTypeRepository).findById(1L);
    }

    // TTS-8
    @Test
    void findTableTypeByIdShouldThrowExceptionWhenNotExists() {
        when(tableTypeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            tableTypeService.findTableTypeById(1L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        verify(tableTypeRepository).findById(1L);
    }
}