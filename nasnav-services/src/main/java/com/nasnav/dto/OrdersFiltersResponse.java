package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.isNull;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrdersFiltersResponse {
    private Set<ShopRepresentationObject> shops;
    private Set<UserRepresentationObject> users;
    private Set<String> shippingServices;
    private Set<String> paymentOperators;
    private Prices prices;
    private Dates dates;
    private Quantities quantities;

    public OrdersFiltersResponse() {
        this.shops= new HashSet<>();
        this.users = new HashSet<>();
        this.shippingServices = new HashSet<>();
        this.paymentOperators = new HashSet<>();
    }


    public void addShop(ShopRepresentationObject shop){
        shops.add(shop);
    }

    public void addUser(UserRepresentationObject user){
        users.add(user);
    }

    public void addShippingService(String shippingService){
        shippingServices.add(shippingService);
    }

    public void addPaymentOperator(String operator){
        paymentOperators.add(operator);
    }

    public void setFilterPrices(BigDecimal total){
        if(isNull(total))
            return;

        if(isNull(prices))
            prices = new Prices(total, total);

        if(total.compareTo(prices.getMinPrice()) == -1) {
            prices.setMinPrice(total);
        }else if (total.compareTo(prices.getMaxPrice()) == 1) {
            prices.setMaxPrice(total);
        }
    }

    public void setFilterDates(LocalDate date){
        String stringDate = date.toString();

        if(isNull(dates))
            dates = new Dates(stringDate, stringDate);

        if(date.compareTo(LocalDate.parse(dates.minCreatedDate)) < 0) {
            dates.setMinCreatedDate(stringDate);
        }else if (date.compareTo(LocalDate.parse(dates.maxCreatedDate)) > 0) {
            dates.setMaxCreatedDate(stringDate);
        }
    }

    public void setFilterQuantities(Integer quantity){
        if(isNull(quantity))
            return;

        if(isNull(quantities))
            quantities = new Quantities(quantity, quantity);

        if(quantity.compareTo(quantities.getMinQuantity()) == -1) {
            quantities.setMinQuantity(quantity);
        }else if (quantity.compareTo(quantities.getMaxQuantity()) == 1) {
            quantities.setMaxQuantity(quantity);
        }
    }
}