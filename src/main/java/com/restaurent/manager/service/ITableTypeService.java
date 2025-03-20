package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TableTypeRequest;
import com.restaurent.manager.dto.request.TableTypeUpdateRequest;
import com.restaurent.manager.dto.response.TableTypeResponse;
import com.restaurent.manager.entity.TableType;

import java.util.List;

public interface ITableTypeService {
    TableTypeResponse createTableType(TableTypeRequest request);
    List<TableTypeResponse> getTableTypes();
    void deleteTableType(Long id);
    TableType findTableTypeById(Long id);
    TableTypeResponse updateTableType(TableTypeUpdateRequest request);
}
