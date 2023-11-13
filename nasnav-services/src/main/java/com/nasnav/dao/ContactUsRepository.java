package com.nasnav.dao;

import com.nasnav.persistence.ContactUsEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContactUsRepository extends JpaRepository<ContactUsEntity,Long> {

    PageImpl<ContactUsEntity> findAllByOrganizationIdAndCreatedAtAfter(Long organizationId, Pageable pageable, LocalDateTime fromDate);


    @Query("SELECT c FROM ContactUsEntity c WHERE c.organization.id = :organizationId " +
            " AND " +
            " ( to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            " to_timestamp(CAST(c.createdAt as text), 'yyyy-MM-dd HH24:MI:SS') >= " +
            "   to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "  ) " +
            "        " )
//            " (:fromDate is null OR c.createdAt >= :fromDate)")
    PageImpl<ContactUsEntity> findAllByOrganizationIdAndCreatedAt(@Param("organizationId") Long organizationId, Pageable pageable, @Param("fromDate")      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                  LocalDateTime fromDate);
}
