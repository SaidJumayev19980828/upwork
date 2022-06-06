package com.nasnav.service.helpers;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.OrdersFiltersResponse;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.UserRepresentationObject;

import java.util.List;

public class OrdersFiltersHelper {
    private List<DetailedOrderRepObject> filteredOrders;
    private OrdersFiltersResponse filtersResponse;

    public OrdersFiltersHelper(List<DetailedOrderRepObject> filteredOrders) {
        this.filteredOrders = filteredOrders;
        filtersResponse = new OrdersFiltersResponse();
    }

    public OrdersFiltersResponse getFiltersResponse() {
        filteredOrders.forEach(order -> {
            assignShops(order);
            assignUsers(order);
            assignShippingServices(order);
            assignPaymentOperators(order);
            assignPrices(order);
            assignDates(order);
            assignQuantities(order);
        });


        return filtersResponse;
    }

    private void assignShops(DetailedOrderRepObject order) {
        filtersResponse.addShop(getShopRepresentationObject(order));
    }

    private ShopRepresentationObject getShopRepresentationObject(DetailedOrderRepObject order) {
        long shopId = order.getShopId();
        String shopName = order.getShopName();
        ShopRepresentationObject shop = new ShopRepresentationObject();

        shop.setId(shopId);
        shop.setName(shopName);

        return shop;
    }

    private void assignUsers(DetailedOrderRepObject order) {
        filtersResponse.addUser(getUserRepresentationObject(order));
    }

    private UserRepresentationObject getUserRepresentationObject(DetailedOrderRepObject order) {
        Long userId = order.getUserId();
        String userName = order.getUserName();
        UserRepresentationObject user = new UserRepresentationObject();

        user.setId(userId);
        user.setName(userName);

        return user;
    }

    private void assignShippingServices(DetailedOrderRepObject order) {
        filtersResponse.addShippingService(order.getShippingService());
    }

    private void assignPaymentOperators(DetailedOrderRepObject order) {
        filtersResponse.addPaymentOperator(order.getPaymentOperator());
    }

    private void assignPrices(DetailedOrderRepObject order) {
        filtersResponse.setFilterPrices(order.getTotal());
    }

    private void assignDates(DetailedOrderRepObject order) {
        filtersResponse.setFilterDates(order.getCreatedAt().toLocalDate());
    }

    private void assignQuantities(DetailedOrderRepObject order) {
        filtersResponse.setFilterQuantities(order.getTotalQuantity());
    }
}