package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.dto.response.VideoCallStatsResponse;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.exceptions.CustomException;
import com.nasnav.persistence.dto.query.result.CartStatisticsData;
import com.nasnav.service.StatisticsService;
import com.nasnav.commons.YeshteryConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_STATISTICS_MONTH_COUNT;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(StatisticsController.API_PATH)
@Log4j2
public class StatisticsController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/statistics";

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("orders")
    public List<Map<String,Object>> getOrderStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                        @RequestParam List<OrderStatus> statuses,
                                                        @RequestParam RequestType type,
                                                        @RequestParam (name = "months_count", required = false, defaultValue = DEFAULT_STATISTICS_MONTH_COUNT) Integer months) {
        return statisticsService.getOrderStatistics(statuses, type, months);
    }

    @GetMapping("carts")
    public List<CartStatisticsData> getOrganizationCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getOrganizationCarts();
    }

    @GetMapping("sold_products")
    public Map<String, List<ProductStatisticsInfo>> getProductsStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam (name = "months_count", required = false, defaultValue = DEFAULT_STATISTICS_MONTH_COUNT) Integer months) {
        return statisticsService.getProductsStatistics(months);
    }

    @GetMapping("sales")
    public Map getSalesStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                  @RequestParam(required = false) Integer month,
                                  @RequestParam(required = false) Integer week) {
        return statisticsService.getSalesStatistics(month, week);
    }

    @GetMapping("users")
    public Map getSalesStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                  @RequestParam(required = false) Integer month) {
        return statisticsService.getSalesStatisticsPerMonth(month);
    }

    @GetMapping("users/carts")
    public List getUsersAbandonedCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getUsersAbandonedCarts();
    }

    @GetMapping("video_call")
    public VideoCallStatsResponse getVideoCallStats(@RequestHeader(name = "User-Token", required = false) String userToken){
        log.info("Get Video Call Statistics request received .");
        try{
            return statisticsService.getVideoCallStats();
        }catch (CustomException e){
            log.error("Error while processing Get Video Call Statistics", e);
            throw e;
        }catch (Exception e){
            log.error("Error while processing Get Video Call Statistics", e);
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
