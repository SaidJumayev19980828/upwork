package com.nasnav.persistence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Builder
@Table(name = "web_scraping_log")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WebScrapingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_message", nullable = false, length = 500)
    private String logMessage;
    @Column(name = "http_status_code", nullable = false)
    private Integer httpStatusCode;
    @Column(name = "status_message", nullable = false, length = 50)
    private String statusMessage;
    @Column(name = "request_url", nullable = false, length = 500)
    private String requestUrl;
    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;
    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private OrganizationEntity organization;

}

