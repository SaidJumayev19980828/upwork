package com.nasnav.service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.enumerations.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.nasnav.dto.request.RequestType.COUNT;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

@Service
public class StatisticsService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private OrdersRepository ordersRepo;
    @Autowired
    private MetaOrderRepository metaOrderRepo;
    @Autowired
    private CartItemRepository cartItemRepo;
    @Autowired
    private UserRepository userRepo;

    public List<OrderStatisticsInfo> getOrderStatistics(List<OrderStatus> statuses, RequestType type) {
        if (type == null || statuses == null || statuses.isEmpty())
            return null;
        Long orgId = securityService.getCurrentUserOrganizationId();
        List<Integer> statusesIntegers = statuses.stream().map(status -> status.getValue()).collect(toList());
        List<OrderStatisticsInfo> info;
        if (type.equals(COUNT)) {
            info =  toOrderCountStatisticsInfo(metaOrderRepo.getOrderIncomeStatisticsPerMonth(orgId, statusesIntegers));
        } else {
            info =  toOrderIncomeStatisticsInfo(metaOrderRepo.getOrderCountStatisticsPerMonth(orgId, statusesIntegers));
        }
        return info;
    }


    private List<OrderStatisticsInfo>  toOrderCountStatisticsInfo(List<OrderStatisticsInfo> infoList) {
        List<OrderStatisticsInfo> finalList =  new ArrayList<>();
        Map<Date, List<OrderStatisticsInfo>> map = infoList.stream().collect(groupingBy(OrderStatisticsInfo::getDate));
        for (Map.Entry e : map.entrySet()) {
            Map<String,Long> statusToCountMap = ((List<OrderStatisticsInfo>) e.getValue())
                    .stream()
                    .collect(toMap(OrderStatisticsInfo::getStatus, OrderStatisticsInfo::getCount));
            OrderStatisticsInfo info = new OrderStatisticsInfo(((Date)e.getKey()), statusToCountMap, null);
            finalList.add(info);
        }
        return finalList;
    }


    private List<OrderStatisticsInfo>  toOrderIncomeStatisticsInfo(List<OrderStatisticsInfo> infoList) {
        List<OrderStatisticsInfo> finalList =  new ArrayList<>();
        Map<Date, List<OrderStatisticsInfo>> map = infoList.stream().collect(groupingBy(OrderStatisticsInfo::getDate));
        for (Map.Entry e : map.entrySet()) {
            Map<String, BigDecimal> statusToIncomeMap = ((List<OrderStatisticsInfo>) e.getValue())
                    .stream()
                    .collect(toMap(OrderStatisticsInfo::getStatus, OrderStatisticsInfo::getIncome));
            OrderStatisticsInfo info = new OrderStatisticsInfo(((Date)e.getKey()), null, statusToIncomeMap);
            finalList.add(info);
        }
        return finalList;
    }


    public Map<Long, Long> getOrganizationCarts() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LinkedHashMap map = new LinkedHashMap();
        cartItemRepo.findCartVariantsByOrg_Id(orgId, PageRequest.of(0, 10))
                .stream()
                .forEach(x -> map.put(x.getFirst(), x.getSecond()));
        return map;
    }


    public Map<Date, List<ProductStatisticsInfo>> getProductsStatistics() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return ordersRepo.getProductsStatisticsPerMonth(orgId)
                .stream()
                .collect(groupingBy(ProductStatisticsInfo::getDate));
    }

    public Map getSalesStatistics(Integer monthNumber, Integer week) {
        Long orgId = securityService.getCurrentUserOrganizationId();

        LocalDateTime maxMonth = getMaxMonth(monthNumber);
        LocalDateTime minMonth = getMinMonth(maxMonth);

        LocalDateTime minWeek = minMonth.plusWeeks( (week-1) );
        LocalDateTime maxWeek = minWeek.plusWeeks( 1 );

        BigDecimal income = ordersRepo.getTotalIncomePerMonth(orgId, minMonth, maxMonth);
        Integer sales = ordersRepo.getSalesPerWeek(orgId, minWeek, maxWeek);

        LinkedHashMap map = new LinkedHashMap();
        map.put("monthly_income", income);
        map.put("weekly_sales", sales);
        return map;
    }

    public Long getNewCustomersPerMonth(Integer monthNumber) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LocalDateTime maxMonth = getMaxMonth(monthNumber);
        LocalDateTime minMonth = getMinMonth(maxMonth);
        return userRepo.getNewCustomersCountPerMonth(orgId, minMonth, maxMonth);
    }

    private LocalDateTime getMaxMonth(Integer monthNumber) {
        Integer finalMonthNumber = monthNumber == 12 ? 1 : monthNumber + 1;
        return (finalMonthNumber == 1 ? LocalDateTime.now().plusYears(1) : LocalDateTime.now() )
                .withMonth(finalMonthNumber).withDayOfMonth(1).withHour(11).withMinute(59).withSecond(59).minusDays(1);
    }

    private LocalDateTime getMinMonth(LocalDateTime maxMonth) {
        return maxMonth.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    }
}
