package com.nasnav.service.helpers;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrdersFiltersResponse;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;

import java.time.LocalDateTime;
import java.util.List;

public class OrdersFiltersHelper {
    private List<DetailedOrderRepObject> filteredOrders;
    private OrdersFiltersResponse filtersResponse;

    public OrdersFiltersHelper(List<DetailedOrderRepObject> filteredOrders) {
        this.filteredOrders = filteredOrders;
        filtersResponse = new OrdersFiltersResponse();
    }

    public OrdersFiltersResponse getFiltersResponse(){
        assignShops();
        assignUsers();
        assignShippingServices();
        assignPaymentOperators();
        assignPrices();
        assignDates();
        assignQuantities();

        return filtersResponse;
    }

    private void assignShops() {
        filteredOrders
                .stream()
                .map(this::getShopRepresentationObject)
                .forEach(shop -> filtersResponse.addShop(shop));
    }

    private ShopRepresentationObject getShopRepresentationObject(DetailedOrderRepObject order){
        long shopId = order.getShopId();
        String shopName = order.getShopName();
        ShopRepresentationObject shop = new ShopRepresentationObject();

        shop.setId(shopId);
        shop.setName(shopName);

        return shop;
    }

    private void assignUsers() {
        filteredOrders
                .stream()
                .map(this::getUserRepresentationObject)
                .forEach(user -> filtersResponse.addUser(user));
    }

    private UserRepresentationObject getUserRepresentationObject(DetailedOrderRepObject order){
        long userId = order.getUserId();
        String userName = order.getUserName();
        UserRepresentationObject user = new UserRepresentationObject();

        user.setId(userId);
        user.setName(userName);

        return user;
    }

    private void assignShippingServices() {
        filteredOrders
                .stream()
                .map(DetailedOrderRepObject::getShippingService)
                .forEach(service -> filtersResponse.addShippingService(service));
    }

    private void assignPaymentOperators() {
        filteredOrders
                .stream()
                .map(DetailedOrderRepObject::getPaymentOperator)
                .forEach(operator -> filtersResponse.addPaymentOperator(operator));
    }

    private void assignPrices() {
        filteredOrders
                .stream()
                .map(DetailedOrderRepObject::getTotal)
                .forEach(price -> filtersResponse.setFilterPrices(price));
    }

    private void assignDates() {
        filteredOrders
                .stream()
                .map(DetailedOrderRepObject::getCreatedAt)
                .map(LocalDateTime::toLocalDate)
                .forEach(date -> filtersResponse.setFilterDates(date));
    }

    private void assignQuantities() {
        filteredOrders
                .stream()
                .map(DetailedOrderRepObject::getTotalQuantity)
                .forEach(quantity -> filtersResponse.setFilterQuantities(quantity));
    }
}