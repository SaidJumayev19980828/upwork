package com.nasnav.dto.response;

import com.nasnav.dto.response.navbox.AdvertisementProductDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdvertisementDTO {
    private Long id;
    @NotNull
    private Long orgId;
    @NotNull
    private String bannerUrl;
    private List<AdvertisementProductDTO> advertisementProductDTOS;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime creationDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;
}
