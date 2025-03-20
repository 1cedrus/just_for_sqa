package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.Order;
import com.restaurent.manager.entity.TableRestaurant;

import java.util.List;

public interface ITableRestaurantService {
    TableRestaurantResponse createTable(TableRestaurantRequest request);
    List<TableRestaurantResponse> getTableByAreaId(Long areaId);
    List<TableRestaurantResponse> createManyTable(int numbers,TableRestaurantRequest request);
    TableRestaurantResponse updateTableByTableId(Long tableId, TableRestaurantRequest request);
    TableRestaurant findById(Long id);
    void deleteTableById(Long tableId);
    void updateTables(List<TableRestaurantResponse> tables);
    Order findOrderByTableId(Long tableId);
    TableRestaurantResponse findTableRestaurantByIdToResponse(Long id);
    List<TableRestaurantResponse> getTableByAreaIdHaveOrder(Long areaId);
}
