package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TaxRequest;
import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Vat;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.VatMapper;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.VatRepository;
import com.restaurent.manager.service.impl.VatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VatServiceTest {
    @Mock
    VatRepository vatRepository;

    @Mock
    RestaurantRepository restaurantRepository;

    @Mock
    VatMapper vatMapper;

    @Mock
    IRestaurantService restaurantService;

    @InjectMocks
    VatService vatService;

    Long restaurantId = 1L;
    Long vatId = 1L;
    VatRequest vatRequest;
    Restaurant restaurant;
    TaxRequest taxRequest;
    Vat vat;
    Vat savedVat;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        vatRequest = new VatRequest();
        restaurant = new Restaurant();
        taxRequest = new TaxRequest();
        vat = new Vat();
        savedVat = new Vat();
        savedVat.setId(1L);
        savedVat.setTaxValue(0.20f);
        savedVat.setTaxName("VAT");
    }

    @Test
    void createVatShouldCreateVatWhenDataIsValid() {
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurant);
        when(vatMapper.toVat(vatRequest)).thenReturn(vat);
        when(vatRepository.save(vat)).thenReturn(savedVat);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        // Act: Call the method under test
        Vat result = vatService.createVat(restaurantId, vatRequest);

        // Assert: Verify the result and interactions
        assertEquals(savedVat, result); // Check the returned VAT
        assertEquals(0.20f, result.getTaxValue()); // Verify taxValue is set
        assertEquals("VAT", result.getTaxName());  // Verify taxName is set
        assertTrue(restaurant.isVatActive());      // Verify restaurant's vatActive is true
        assertEquals(savedVat, restaurant.getVat()); // Verify restaurant's VAT is set

        // Verify these function get called
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(vatMapper).toVat(vatRequest);
        verify(vatRepository).save(vat);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void createVatShouldThrowErrorWhenRestaurantNotFound() {
        when(restaurantService.getRestaurantById(restaurantId)).thenThrow(new AppException(ErrorCode.NOT_EXIST));

        AppException e = assertThrows(AppException.class, () -> {
            vatService.createVat(restaurantId, vatRequest);
        });

        // Assert: Verify the exception
        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        // Verify that the restaurantService method was called
        verify(restaurantService).getRestaurantById(restaurantId);
    }

    @Test
    void findByIdShouldReturnVatWhenExists() {
        when(vatRepository.findById(vatId)).thenReturn(java.util.Optional.of(savedVat));

        // Act: Call the method under test
        Vat result = vatService.findById(vatId);

        // Assert: Verify the result and interactions
        assertEquals(savedVat, result); // Check the returned VAT

        // Verify interactions with mocks
        verify(vatRepository).findById(vatId);
    }

    @Test
    void findByIdShouldThrowErrorWhenNotExists() {
        when(vatRepository.findById(vatId)).thenReturn(java.util.Optional.empty());

        AppException e = assertThrows(AppException.class, () -> {
            vatService.findById(vatId);
        });

        // Assert: Verify the exception
        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());

        // Verify that the vatRepository method was called
        verify(vatRepository).findById(vatId);
    }

    @Test
    void updateVatInformationShouldUpdateVatWhenDataIsValid() {
        when(vatRepository.findById(vatId)).thenReturn(java.util.Optional.of(savedVat));
        when(vatRepository.save(savedVat)).thenReturn(savedVat);

        // Act: Call the method under test
        Vat result = vatService.updateVatInformation(vatId, vatRequest);

        // Assert: Verify the result and interactions
        assertEquals(savedVat, result); // Check the returned VAT

        // Verify interactions with mocks
        verify(vatRepository).findById(vatId);
        verify(vatMapper).updateVat(savedVat, vatRequest);
        verify(vatRepository).save(savedVat);
    }

    @Test
    void updateTaxShouldUpdateTaxWhenDataIsValid() {
        when(vatRepository.findById(vatId)).thenReturn(java.util.Optional.of(savedVat));
        when(vatRepository.save(savedVat)).thenReturn(savedVat);

        // Act: Call the method under test
        Vat result = vatService.updateTax(vatId, taxRequest);

        // Assert: Verify the result and interactions
        assertEquals(savedVat, result); // Check the returned VAT

        // Verify interactions with mocks
        verify(vatRepository).findById(vatId);
        verify(vatRepository).save(savedVat);
    }
}
