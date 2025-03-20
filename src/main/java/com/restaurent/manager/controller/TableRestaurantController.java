package com.restaurent.manager.controller;

import com.restaurent.manager.dto.request.Table.TableRestaurantRequest;
import com.restaurent.manager.dto.request.Table.TableRestaurantUpdateRequest;
import com.restaurent.manager.dto.response.ApiResponse;
import com.restaurent.manager.dto.response.TableRestaurantResponse;
import com.restaurent.manager.entity.TableRestaurant;
import com.restaurent.manager.mapper.TableRestaurantMapper;
import com.restaurent.manager.service.ITableRestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/api/table")
@FieldDefaults(makeFinal = true)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TableRestaurantController {
    ITableRestaurantService tableRestaurantService;
    SimpMessagingTemplate messagingTemplate;
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('TABLE')")
    @PostMapping(value = "/create")
    public ApiResponse<TableRestaurantResponse> createTable(@RequestBody @Valid TableRestaurantRequest request){
        return ApiResponse.<TableRestaurantResponse>builder()
                .result(tableRestaurantService.createTable(request))
                .build();
    }
    @GetMapping(value = "/area/{areaId}")
    public ApiResponse<List<TableRestaurantResponse>> getTableByAreaId(@PathVariable Long areaId){
        return ApiResponse.<List<TableRestaurantResponse>>builder()
                .result(tableRestaurantService.getTableByAreaId(areaId))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('TABLE')")
    @PostMapping(value = "/create/{numbers}")
    public ApiResponse<List<TableRestaurantResponse>> createTables(@PathVariable String numbers,@RequestBody @Valid TableRestaurantRequest request){
        return ApiResponse.<List<TableRestaurantResponse>>builder()
                .result(tableRestaurantService.createManyTable(Integer.parseInt(numbers),request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('TABLE')")
    @PutMapping(value = "/{tableRestaurantId}")
    public ApiResponse<TableRestaurantResponse> updateTable(@PathVariable Long tableRestaurantId,@RequestBody @Valid TableRestaurantRequest request){
        return ApiResponse.<TableRestaurantResponse>builder()
                .result(tableRestaurantService.updateTableByTableId(tableRestaurantId,request))
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('TABLE')")
    @DeleteMapping(value = "/{tableRestaurantId}")
    public ApiResponse<Void> deleteTableById(@PathVariable Long tableRestaurantId){
        tableRestaurantService.deleteTableById(tableRestaurantId);
        return ApiResponse.<Void>builder()
                .message("Delete success")
                .build();
    }
    @PreAuthorize(value = "hasRole('MANAGER') and hasAuthority('TABLE')")
    @PutMapping(value = "/update")
    public ApiResponse<Void> updateTables(@RequestBody List<TableRestaurantResponse> tableRestaurantResponses){
        tableRestaurantService.updateTables(tableRestaurantResponses);
        return ApiResponse.<Void>builder()
                .message("Update success")
                .build();
    }
    @MessageMapping("/restaurant/{restaurantId}/table/{tableId}/status-table")
    public void notifyStatusTable(@DestinationVariable Long tableId,@DestinationVariable Long restaurantId){
        TableRestaurantResponse response = tableRestaurantService.findTableRestaurantByIdToResponse(tableId);
        String roomId = "" + restaurantId;
        messagingTemplate.convertAndSend("/topic/restaurant/" + roomId,response);
    }
    @PreAuthorize(value = "hasRole('CHEF') and hasAuthority('TABLE')")
    @GetMapping("/chef/area/{areaId}")
    public ApiResponse<List<TableRestaurantResponse>> getTableHaveOrderByAreaId(@PathVariable Long areaId){
        return ApiResponse.<List<TableRestaurantResponse>>builder()
                .result(tableRestaurantService.getTableByAreaIdHaveOrder(areaId))
                .build();
    }
}
