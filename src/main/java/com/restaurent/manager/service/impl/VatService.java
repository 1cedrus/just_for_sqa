package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.TaxRequest;
import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Vat;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.VatMapper;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.VatRepository;
import com.restaurent.manager.service.IRestaurantService;
import com.restaurent.manager.service.IVatService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class VatService implements IVatService {
    VatRepository vatRepository;
    VatMapper vatMapper;
    @NonFinal
    @Value("${vat-default}")
    float taxValue;
    @NonFinal
    @Value("${vat-name-default}")
    String taxName;
    IRestaurantService restaurantService;
    RestaurantRepository repository;
    @Override
    public Vat createVat(Long restaurantId,VatRequest request) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        Vat vat = vatMapper.toVat(request);
        vat.setTaxValue(taxValue);
        vat.setTaxName(taxName);
        Vat saved = vatRepository.save(vat);
        restaurant.setVatActive(true);
        restaurant.setVat(saved);
        repository.save(restaurant);
        return saved;
    }

    @Override
    public Vat updateVatInformation(Long vatId, VatRequest request) {
        Vat vat = findById(vatId);
        vatMapper.updateVat(vat,request);
        return vatRepository.save(vat);
    }

    @Override
    public Vat findById(Long vatId) {
        return vatRepository.findById(vatId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_EXIST)
        );
    }

    @Override
    public Vat updateTax(Long vatId, TaxRequest request) {
        Vat vat = findById(vatId);
        vat.setTaxValue(request.getTaxValue());
        vat.setTaxName(request.getTaxName());
        return vatRepository.save(vat);
    }
}
