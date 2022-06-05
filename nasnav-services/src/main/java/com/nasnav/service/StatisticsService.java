package com.nasnav.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dao.*;
import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.dto.response.navbox.StatisticsCartItem;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartStatisticsData;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.dto.request.RequestType.COUNT;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

@Service
public class StatisticsService {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProductService productService;

    @Autowired
    private OrdersRepository ordersRepo;
    @Autowired
    private MetaOrderRepository metaOrderRepo;
    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserTokenRepository tokenRepo;

    public List<Map<String,Object>> getOrderStatistics(List<OrderStatus> statuses, RequestType type, Integer months) {
        if (type == null || statuses == null || statuses.isEmpty())
            return null;
        LocalDateTime startDate = getStartDate(months);
        Long orgId = securityService.getCurrentUserOrganizationId();
        List<Integer> statusesIntegers = statuses.stream().map(OrderStatus::getValue).collect(toList());
        List<Map<String,Object>> info;
        if (type.equals(COUNT)) {
            info =  toOrderCountStatisticsInfo(metaOrderRepo.getOrderIncomeStatisticsPerMonth(orgId, statusesIntegers, startDate));
        } else {
            info =  toOrderIncomeStatisticsInfo(metaOrderRepo.getOrderCountStatisticsPerMonth(orgId, statusesIntegers, startDate));
        }
        return info;
    }

    private LocalDateTime getStartDate(Integer months) {
        return now().minusMonths(months-1).withDayOfMonth(1).withHour(0).withMinute(0);
    }

    private List<Map<String,Object>>  toOrderCountStatisticsInfo(List<OrderStatisticsInfo> infoList) {
        List<Map<String,Object>> finalList =  new ArrayList<>();
        Map<Date, List<OrderStatisticsInfo>> map = infoList.stream().collect(groupingBy(OrderStatisticsInfo::getDate, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<Date, List<OrderStatisticsInfo>> e : map.entrySet()) {
            Map<String,Object> statusToCountMap = e.getValue()
                    .stream()
                    .collect(toMap(OrderStatisticsInfo::getStatus, OrderStatisticsInfo::getCount));
            statusToCountMap.put("month", new SimpleDateFormat("MMMM").format(e.getKey()));
            finalList.add(statusToCountMap);
        }
        return finalList;
    }


    private List<Map<String,Object>>  toOrderIncomeStatisticsInfo(List<OrderStatisticsInfo> infoList) {
        List<Map<String,Object>> finalList =  new ArrayList<>();
        Map<Date, List<OrderStatisticsInfo>> map = infoList.stream().collect(groupingBy(OrderStatisticsInfo::getDate, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<Date, List<OrderStatisticsInfo>> e : map.entrySet()) {
            Map<String, Object> statusToIncomeMap = e.getValue()
                    .stream()
                    .collect(toMap(OrderStatisticsInfo::getStatus, OrderStatisticsInfo::getIncome));
            statusToIncomeMap.put("month", new SimpleDateFormat("MMMM").format(e.getKey()));
            finalList.add(statusToIncomeMap);
        }
        return finalList;
    }


    public List<CartStatisticsData> getOrganizationCarts() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return cartItemRepo.findCartVariantsByOrg_Id(orgId, PageRequest.of(0, 10));
    }


    public Map<String, List<ProductStatisticsInfo>> getProductsStatistics(Integer months) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LocalDateTime startDate = getStartDate(months);
        return ordersRepo.getProductsStatisticsPerMonth(orgId, startDate)
                .stream()
                .collect(groupingBy(ProductStatisticsInfo::getDate))
                .entrySet()
                .stream()
                .collect(toMap(e -> new SimpleDateFormat("MMMM").format(e.getKey()), e -> e.getValue()));
    }

    public Map getSalesStatistics(Integer monthNumber, Integer week) {
        Long orgId = securityService.getCurrentUserOrganizationId();

        LocalDateTime maxMonth = getMaxMonth(monthNumber);
        LocalDateTime minMonth = getMinMonth(maxMonth);

        LocalDateTime minWeek = minMonth.plusWeeks( (getWeekNumber(week)-1) );
        LocalDateTime maxWeek = minWeek.plusWeeks( 1 );

        BigDecimal income = ordersRepo.getTotalIncomePerMonth(orgId, minMonth, maxMonth).orElse(ZERO);
        Integer sales = ordersRepo.getSalesPerWeek(orgId, minWeek, maxWeek).orElse(0);

        LinkedHashMap map = new LinkedHashMap();
        map.put("monthly_income", income);
        map.put("weekly_sales", sales);
        return map;
    }

    public Map getSalesStatisticsPerMonth(Integer monthNumber) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        monthNumber = getMonthNumber(monthNumber);
        LocalDateTime maxMonth = getMaxMonth(monthNumber);
        LocalDateTime minMonth = getMinMonth(maxMonth);
        Map<String, Long> result = new HashMap<>();
        result.put("new_users", userRepo.getNewCustomersCountPerMonth(orgId, minMonth, maxMonth));
        result.put("active_users", tokenRepo.countActiveUsers(orgId, now().minusDays(21)));
        return result;
    }

    public List<UserCartInfo> getUsersAbandonedCarts() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return cartItemRepo.findUsersCartsOrg_Id(orgId)
                .stream()
                .collect(groupingBy(CartItemEntity::getUser))
                .entrySet()
                .stream()
                .map(this::toUserCartInfo)
                .collect(toList());
    }

    public UserCartInfo toUserCartInfo(Map.Entry<UserEntity, List<CartItemEntity>> map) {
        UserCartInfo info = new UserCartInfo();
        UserEntity user = map.getKey();
        var items = map
                .getValue()
                .stream()
                .map(this::toCartItem)
                .collect(Collectors.toList());
        info.setId(user.getId());
        info.setName(user.getName());
        info.setEmail(user.getEmail());
        info.setPhoneNumber(user.getPhoneNumber());
        info.setItems(items);
        return info;
    }

    private StatisticsCartItem toCartItem(CartItemEntity entity) {
        var item = new StatisticsCartItem();
        StocksEntity stock = entity.getStock();
        ProductVariantsEntity variant = stock.getProductVariantsEntity();
        ProductEntity product = variant.getProductEntity();
        Map<String,String> variantFeatures = ofNullable(productService.parseVariantFeatures(variant, 0))
                .orElse(new HashMap<>());
        item.setId(entity.getId());
        item.setCoverImg(entity.getCoverImage());
        item.setStockId(stock.getId());
        item.setQuantity(entity.getQuantity());
        item.setPrice(stock.getPrice());
        item.setDiscount(stock.getDiscount());
        item.setVariantId(variant.getId());
        item.setVariantName(variant.getName());
        item.setVariantFeatures(variantFeatures);
        item.setProductId(product.getId());
        item.setBarcode(product.getBarcode());
        item.setName(product.getName());
        item.setCreatedAt(entity.getCreatedAt());
        return item;
    }

    private LocalDateTime getMaxMonth(Integer monthNumber) {
        monthNumber = getMonthNumber(monthNumber);
        Integer finalMonthNumber = monthNumber == 12 ? 1 : monthNumber + 1;
        return (finalMonthNumber == 1 ? now().plusYears(1) : now() )
                .withMonth(finalMonthNumber).withDayOfMonth(1).withHour(11).withMinute(59).withSecond(59).minusDays(1);
    }

    private LocalDateTime getMinMonth(LocalDateTime maxMonth) {
        return maxMonth.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    }

    private Integer getMonthNumber(Integer monthNumber) {
        return ofNullable(monthNumber).orElseGet(() -> now().getMonthValue());
    }

    private Integer getWeekNumber(Integer week) {
        return ofNullable(week).orElseGet(() -> now().get(ChronoField.ALIGNED_WEEK_OF_MONTH));
    }
}

@Data
class UserCartInfo {
    Long id;
    String name;
    String email;
    @JsonProperty("phone_number")
    String phoneNumber;
    List<StatisticsCartItem> items;
}

