package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.UnitRequest;
import com.restaurent.manager.dto.response.UnitResponse;
import com.restaurent.manager.entity.Unit;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.UnitMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.DishRepository;
import com.restaurent.manager.repository.UnitRepository;
import com.restaurent.manager.service.IAccountService;
import com.restaurent.manager.service.IUnitService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UnitService implements IUnitService {

    UnitMapper unitMapper;
    UnitRepository unitRepository;
    AccountRepository accountRepository;
    DishRepository dishRepository;
    @Override
    public UnitResponse createUnit(UnitRequest request) {
        Unit unit = unitMapper.toUnit(request);
        unit.setAccount(accountRepository.findById(request.getAccountId())
                .orElseThrow(
                        () -> new AppException(ErrorCode.NOT_EXIST)
                ));
        return unitMapper.toUnitResponse(unitRepository.save(
                unit
        ));
    }

    @Override
    public List<UnitResponse> getUnitsByAccountId(Long accountId) {
        return unitRepository.getUnitsByAccount_Id(accountId).stream().map(unitMapper::toUnitResponse).toList();
    }

    @Override
    public UnitResponse updateUnit(Long unitId, UnitRequest request) {
        Unit unit = unitRepository.findById(unitId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
        unitMapper.updateUnit(unit,request);
        unitRepository.save(unit);
        return unitMapper.toUnitResponse(unit);
    }

    @Override
    public void deleteUnitById(Long unitId) {
        Unit unit = findById(unitId);
        if(dishRepository.existsByUnit_Id(unitId)){
            unit.setHidden(true);
            unitRepository.save(unit);
        }else{
            unitRepository.deleteById(unitId);
        }
    }

    @Override
    public Unit findById(Long id) {
        return unitRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }
}
