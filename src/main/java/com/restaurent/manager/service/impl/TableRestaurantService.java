package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.*;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.TableRestaurantMapper;
import com.restaurent.manager.repository.AreaRepository;
import com.restaurent.manager.repository.ScheduleRepository;
import com.restaurent.manager.repository.TableRestaurantRepository;
import com.restaurent.manager.repository.TableTypeRepository;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.ITableRestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class TableRestaurantService implements ITableRestaurantService {
    TableRestaurantMapper tableRestaurantMapper;
    AreaRepository areaRepository;
    TableTypeRepository tableTypeRepository;
    TableRestaurantRepository tableRestaurantRepository;
    IRestaurantService restaurantService;
    ScheduleRepository scheduleRepository;
    @Override
    public TableRestaurantResponse createTable(TableRestaurantRequest request) {
        TableRestaurant tableRestaurant = tableRestaurantMapper.toTableRestaurant(request);
        tableRestaurant.setHidden(false);
        tableRestaurant.setTableType(tableTypeRepository.findById(request.getTableTypeId()).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        ));
        tableRestaurant.setArea(areaRepository.findById(request.getAreaId()).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        ));
        return tableRestaurantMapper.toTableRestaurantResponse(tableRestaurantRepository.save(tableRestaurant));
    }

    @Override
    public List<TableRestaurantResponse> getTableByAreaId(Long areaId) {
        List<TableRestaurantResponse> responses = tableRestaurantRepository.findByArea_IdAndHidden(areaId,false).stream().map(tableRestaurantMapper::toTableRestaurantResponse).toList();
        for (TableRestaurantResponse tableRestaurantResponse : responses){
            List<Schedule> scheduleOfTable = scheduleRepository.findByTableIdAndBookedDate(tableRestaurantResponse.getId(), LocalDate.now());
            tableRestaurantResponse.setBooked(!scheduleOfTable.isEmpty());
        }
        return responses;
    }

    @Override
    public List<TableRestaurantResponse> createManyTable(int numbers, TableRestaurantRequest request) {
        List<Area> areas = areaRepository.findByRestaurant_Id(request.getRestaurantId());
        Restaurant restaurant = restaurantService.getRestaurantById(request.getRestaurantId());
        int totalTable = 0;
        if(!areas.isEmpty()){
            for (Area area : areas){
                totalTable += tableRestaurantRepository.findByArea_IdAndHidden(area.getId(),false).size();
            }
            for (Permission permission : restaurant.getRestaurantPackage().getPermissions()){
                if(permission.getName().equals("TABLE_MAX")){
                    if(totalTable + numbers > permission.getMaximum()){
                        throw new AppException(ErrorCode.MAX_TABLE);
                    }
                }
            }
        }
        List<TableRestaurantResponse> tableRestaurantResponses = new ArrayList<>();
        TableRestaurant tableRestaurant = tableRestaurantRepository.findTopByRestaurant_IdAndNameStartingWithOrderByNameDesc(request.getRestaurantId(),request.getName() + "-");
        if(tableRestaurant != null){
            String[] originalName = tableRestaurant.getName().split("-");
            log.info(tableRestaurant.getName());
            int tableNumber = Integer.parseInt(originalName[1]);
            for (int i = 1; i <= numbers; i++) {
                tableNumber ++;
                request.setName(request.getName() + "-" + tableNumber);
                tableRestaurantResponses.add(createTable(request));
                request.setName(originalName[0]);
            }
        }else{
            String originalName = request.getName();
            for (int i = 1; i <= numbers; i++) {
                request.setName(request.getName() + "-" + i);
                tableRestaurantResponses.add(createTable(request));
                request.setName(originalName);
            }
        }
        return tableRestaurantResponses;
    }

    @Override
    public TableRestaurantResponse updateTableByTableId(Long tableId, TableRestaurantRequest request) {
        TableRestaurant tableRestaurant = findById(tableId);
        if(!tableRestaurant.getName().equals(request.getName())){
            if(tableRestaurantRepository.existsByNameAndArea_Id(request.getName(),request.getAreaId())){
                throw new AppException(ErrorCode.TABLE_NAME_EXISTED);
            }
            tableRestaurant.setName(request.getName());
        }
        tableRestaurant.setNumberChairs(request.getNumberChairs());
        tableRestaurant.setTableType(tableTypeRepository.findById(request.getTableTypeId()).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        ));
        return tableRestaurantMapper.toTableRestaurantResponse(tableRestaurantRepository.save(tableRestaurant));
    }

    @Override
    public TableRestaurant findById(Long id) {
        return tableRestaurantRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public void deleteTableById(Long tableId) {
        TableRestaurant tableRestaurant = findById(tableId);
        tableRestaurant.setHidden(true);
        tableRestaurantRepository.save(tableRestaurant);
    }

    @Override
    public void updateTables(List<TableRestaurantResponse> tables) {
        for (TableRestaurantResponse tableRestaurantResponse : tables){
            TableRestaurant tableRestaurant = findById(tableRestaurantResponse.getId());
            tableRestaurant.setPositionX(tableRestaurantResponse.getPositionX());
            tableRestaurant.setPositionY(tableRestaurantResponse.getPositionY());
            tableRestaurantRepository.save(tableRestaurant);
        }
    }

    @Override
    public Order findOrderByTableId(Long tableId) {
        return null;
    }

    @Override
    public TableRestaurantResponse findTableRestaurantByIdToResponse(Long id) {
        return tableRestaurantMapper.toTableRestaurantResponse(findById(id));
    }

    @Override
    public List<TableRestaurantResponse> getTableByAreaIdHaveOrder(Long areaId) {
        List<TableRestaurantResponse> res = new ArrayList<>();
        List<TableRestaurant> tableRestaurants = tableRestaurantRepository.findByArea_IdAndHidden(areaId,false);
        if(!tableRestaurants.isEmpty()){
            tableRestaurants.forEach((item) -> {
                if(item.getOrderCurrent() != null){
                    res.add(tableRestaurantMapper.toTableRestaurantResponse(item));
                }
            });
        }
        return res;
    }
}
