package com.restaurent.manager.service;

import com.restaurent.manager.dto.request.TaxRequest;
import com.restaurent.manager.dto.request.VatRequest;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.entity.Vat;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.repository.VatRepository;
import com.restaurent.manager.service.impl.RestaurantService;
import com.restaurent.manager.service.impl.VatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
class VatServiceTest {

    @Autowired
    private VatService vatService;

    @Autowired
    private VatRepository vatRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantService restaurantService;

    @MockBean
    private Clock clock;

    private Restaurant restaurant;
    private Vat vat;
    private Long restaurantId;

    @BeforeEach
    void setup() {
        // Set up a fixed Clock for 2025-04-08 12:00
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 8, 12, 0);
        Clock fixedClock = Clock.fixed(
                fixedDateTime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // Create restaurant
        restaurant = new Restaurant();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("Test Address");
        restaurant.setProvince("Test Province");
        restaurant.setDistrict("Test District");
        restaurant.setMoneyToPoint(1.0);
        restaurant.setPointToMoney(1.0);
        restaurant.setMonthsRegister(12);
        restaurant.setVatActive(false);
        restaurant.setDateCreated(LocalDate.now(clock));
        restaurant = restaurantRepository.saveAndFlush(restaurant);
        restaurantId = restaurant.getId();

        // Create VAT
        vat = new Vat();
        vat.setName("Test VAT");
        vat.setTaxCode("TAX123");
        vat.setAddress("VAT Address");
        vat.setBranch("Main Branch");
        vat.setRegistrationNumber("REG123");
        vat.setTaxValue(0.20f);
        vat.setTaxName("VAT");
        vat.setRestaurantId(restaurantId);
        vat = vatRepository.saveAndFlush(vat);
    }

    // VS-1
    @Test
    void createVatShouldCreateVatWhenDataIsValid() {
        VatRequest request = new VatRequest();
        request.setName("New VAT");
        request.setTaxCode("NEWTAX123");
        request.setAddress("New VAT Address");
        request.setBranch("New Branch");
        request.setRegistrationNumber("NEWREG123");

        Vat result = vatService.createVat(restaurantId, request);

        assertNotNull(result);
        assertEquals("New VAT", result.getName());
        assertEquals("NEWTAX123", result.getTaxCode());
        assertEquals(restaurantId, result.getRestaurantId());

        // Verify restaurant is updated
        Restaurant updatedRestaurant = restaurantRepository.findById(restaurantId).orElse(null);
        assertNotNull(updatedRestaurant);
        assertTrue(updatedRestaurant.isVatActive());
        assertEquals(result, updatedRestaurant.getVat());
    }

    // VS-2
    @Test
    void createVatShouldThrowErrorWhenRestaurantNotFound() {
        VatRequest request = new VatRequest();
        request.setName("New VAT");

        AppException e = assertThrows(AppException.class, () -> {
            vatService.createVat(999L, request);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // VS-3
    @Test
    void findByIdShouldReturnVatWhenExists() {
        Vat result = vatService.findById(vat.getId());

        assertNotNull(result);
        assertEquals(vat.getId(), result.getId());
        assertEquals("Test VAT", result.getName());
        assertEquals("TAX123", result.getTaxCode());
    }

    // VS-4
    @Test
    void findByIdShouldThrowErrorWhenNotExists() {
        AppException e = assertThrows(AppException.class, () -> {
            vatService.findById(999L);
        });

        assertEquals(ErrorCode.NOT_EXIST, e.getErrorCode());
    }

    // VS-5
    @Test
    void updateVatInformationShouldUpdateVatWhenDataIsValid() {
        VatRequest request = new VatRequest();
        request.setName("Updated VAT");
        request.setTaxCode("UPDATEDTAX123");
        request.setAddress("Updated VAT Address");
        request.setBranch("Updated Branch");
        request.setRegistrationNumber("UPDATEDREG123");

        Vat result = vatService.updateVatInformation(vat.getId(), request);

        assertNotNull(result);
        assertEquals(vat.getId(), result.getId());
        assertEquals("Updated VAT", result.getName());
        assertEquals("UPDATEDTAX123", result.getTaxCode());

        // Verify in database
        Vat updatedVat = vatRepository.findById(vat.getId()).orElse(null);
        assertNotNull(updatedVat);
        assertEquals("Updated VAT", updatedVat.getName());
        assertEquals("UPDATEDTAX123", updatedVat.getTaxCode());
    }

    // VS-6
    @Test
    void updateTaxShouldUpdateTaxWhenDataIsValid() {
        TaxRequest request = new TaxRequest();
        request.setTaxValue(0.15f);
        request.setTaxName("Updated Tax");

        Vat result = vatService.updateTax(vat.getId(), request);

        assertNotNull(result);
        assertEquals(vat.getId(), result.getId());
        assertEquals(0.15f, result.getTaxValue());
        assertEquals("Updated Tax", result.getTaxName());

        // Verify in database
        Vat updatedVat = vatRepository.findById(vat.getId()).orElse(null);
        assertNotNull(updatedVat);
        assertEquals(0.15f, updatedVat.getTaxValue());
        assertEquals("Updated Tax", updatedVat.getTaxName());
    }
}
