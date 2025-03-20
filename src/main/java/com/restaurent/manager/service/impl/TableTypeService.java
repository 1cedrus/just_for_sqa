package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.TableTypeRequest;
import com.restaurent.manager.dto.request.TableTypeUpdateRequest;
import com.restaurent.manager.dto.response.TableTypeResponse;
import com.restaurent.manager.entity.TableType;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.TableTypeMapper;
import com.restaurent.manager.repository.TableTypeRepository;
import com.restaurent.manager.service.ITableTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TableTypeService implements ITableTypeService {
    TableTypeRepository tableTypeRepository;
    TableTypeMapper tableTypeMapper;
    @Override
    public TableTypeResponse createTableType(TableTypeRequest request) {
        TableType tableType = tableTypeMapper.toTableType(request);
        return tableTypeMapper.toTableTypeResponse(tableTypeRepository.save(tableType));
    }

    @Override
    public List<TableTypeResponse> getTableTypes() {
        return tableTypeRepository.findAll().stream().map(tableTypeMapper::toTableTypeResponse).toList();
    }

    @Override
    public void deleteTableType(Long id) {
        TableType tableType = findTableTypeById(id);
        tableTypeRepository.delete(tableType);
    }

    @Override
    public TableType findTableTypeById(Long id) {
        return tableTypeRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public TableTypeResponse updateTableType(TableTypeUpdateRequest request) {
        TableType tableType = findTableTypeById(request.getId());
        tableTypeMapper.updateRestaurant(tableType,request);
        return tableTypeMapper.toTableTypeResponse(tableType);
    }
}
