package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TaxRequest;
import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.entity.Vat;

public interface IVatService {
    Vat createVat(Long restaurantId,VatRequest request);
    Vat updateVatInformation(Long vatId, VatRequest request);
    Vat findById(Long vatId);
    Vat updateTax(Long vatId, TaxRequest request);
}
