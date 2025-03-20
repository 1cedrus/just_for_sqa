package com.restaurent.manager.service.impl;

import com.restaurent.manager.dto.request.restaurant.*;
import com.restaurent.manager.dto.response.RestaurantResponse;
import com.restaurent.manager.entity.Account;
import com.restaurent.manager.entity.Package;
import com.restaurent.manager.entity.Restaurant;
import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.mapper.RestaurantMapper;
import com.restaurent.manager.repository.AccountRepository;
import com.restaurent.manager.repository.RestaurantRepository;
import com.restaurent.manager.service.IPackageService;
import com.restaurent.manager.service.IRestaurantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RestaurantService implements IRestaurantService {
    RestaurantMapper restaurantMapper;
    RestaurantRepository restaurantRepository;
    AccountRepository accountRepository;
    IPackageService packageService;
    AccountService accountService;
    @Override
    public RestaurantResponse initRestaurant(RestaurantRequest request) {
        if(restaurantRepository.existsByAccount_Id(request.getAccountId())){
            throw new AppException(ErrorCode.LIMITED_RESTAURANT);
        }
        if(restaurantRepository.existsByRestaurantName((request.getRestaurantName()))){
            throw new AppException(ErrorCode.RESTAURANT_NAME_EXISTED);
        }
        Restaurant restaurant = restaurantMapper.toRestaurant(request);
        Account account = accountRepository.findById(request.getAccountId()).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        restaurant.setAccount(account);
        restaurant.setRestaurantPackage(packageService.findByPackName("TRIAL"));
        restaurant.setExpiryDate(LocalDateTime.now().plusDays(7));
        restaurant.setMonthsRegister(1);
        restaurant.setMoneyToPoint(100000);
        restaurant.setPointToMoney(1000);
        restaurant.setVatActive(false);
        account.setRestaurant(restaurant);
        restaurant.setDateCreated(LocalDate.now());
        Restaurant restaurantSaved = restaurantRepository.save(restaurant);
        RestaurantResponse restaurantResponse = restaurantMapper.toRestaurantResponse(restaurantSaved);
        restaurantResponse.setToken(accountService.generateToken(account));
        return restaurantResponse;
    }

    @Override
    public List<RestaurantResponse> getRestaurants() {
        return restaurantRepository.findAll().stream().map(restaurantMapper::toRestaurantResponse).toList();
    }

    @Override
    public RestaurantResponse updateRestaurant(Long restaurantId,RestaurantUpdateRequest request) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        restaurant.setRestaurantPackage(packageService.findPackById(request.getPackId()));
        restaurant.setExpiryDate(LocalDateTime.now().plusMonths(request.getMonths()));
        restaurantMapper.updateRestaurant(restaurant,request);
        return restaurantMapper.toRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse updateRestaurant(Long accountId, RestaurantManagerUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findByAccount_Id(accountId);
        if(restaurant == null){
            throw new AppException(ErrorCode.NOT_EXIST);
        }
        restaurantMapper.updateRestaurant(restaurant,request);
        return restaurantMapper.toRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse updateRestaurant(Long accountId, RestaurantPaymentRequest request) {
        Restaurant restaurant = restaurantRepository.findByAccount_Id(accountId);
        if(restaurant == null){
            throw new AppException(ErrorCode.NOT_EXIST);
        }
        restaurantMapper.updateRestaurant(restaurant,request);
        return restaurantMapper.toRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
    }

    @Override
    public RestaurantResponse getRestaurantByAccountId(Long accountId) {
        Restaurant restaurant = restaurantRepository.findByAccount_Id(accountId);
        RestaurantResponse response = null;
        if(restaurant != null){
            response = restaurantMapper.toRestaurantResponse(restaurant);
            response.setVatActive(restaurant.isVatActive());
        }
        return response;
    }

    @Override
    public double getMoneyToUpdatePackForRestaurant(Long restaurantId, RestaurantUpdateRequest request) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        double remainMoney;
        double requireMoney;
        long dayLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), restaurant.getExpiryDate());
        Package pack = packageService.findPackById(request.getPackId());
        if(dayLeft > 0){
            if(restaurant.getMonthsRegister() >= 12){
                // calculate the money remain by year
                log.info("Price pack per year : " + restaurant.getRestaurantPackage().getPricePerYear());
                remainMoney = (restaurant.getRestaurantPackage().getPricePerYear() / 365) * dayLeft;
                log.info("Remain money by year " + remainMoney + "in day left " + dayLeft);
            }else{
                // calculate the money remain by month
                log.info("Price pack per month : " + restaurant.getRestaurantPackage().getPricePerMonth());
                remainMoney = (restaurant.getRestaurantPackage().getPricePerMonth() / LocalDate.now().lengthOfMonth()) * dayLeft;
                log.info("Remain money by month " + remainMoney + " in day left " + dayLeft);
            }
            remainMoney = Math.round(remainMoney);
            if(request.getMonths() >= 12){
                requireMoney = (((double) request.getMonths() / 12) * pack.getPricePerYear()) - remainMoney;
                log.info("Require money by year : " + requireMoney);
            }else{
                requireMoney = (request.getMonths() * pack.getPricePerMonth()) - remainMoney;
                log.info("Require money by month : " + requireMoney);
            }
        }else{
            if(request.getMonths() >= 12){
                requireMoney = (((double) request.getMonths() / 12) * pack.getPricePerYear()) ;
                log.info("Require money by year dont have remain : " + requireMoney);
            }else{
                requireMoney = request.getMonths() * pack.getPricePerMonth();
                log.info("Require money by month dont have remain : " + requireMoney);
            }
        }
        return Math.round(requireMoney);
    }

    @Override
    public void updateRestaurantVatById(Long restaurantId, boolean status) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        restaurant.setVatActive(status);
        restaurantRepository.save(restaurant);
    }

    @Override
    public RestaurantResponse updatePointForRestaurant(Long restaurantId, PointsRequest request) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        restaurant.setMoneyToPoint(request.getMoneyToPoint());
        restaurant.setPointToMoney(request.getPointToMoney());
        return restaurantMapper.toRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public int countRestaurantByDateCreated(LocalDate date) {
        return restaurantRepository.countByDateCreated(date);
    }


}
