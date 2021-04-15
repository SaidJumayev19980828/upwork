package com.nasnav.dao;

import com.nasnav.persistence.ReturnRequestItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestItemRepository extends JpaRepository<ReturnRequestItemEntity, Long> {

    @Query(value = "select i from ReturnRequestItemEntity i" +
            " left join fetch i.returnRequest r" +
            " left join fetch i.basket b " +
            " where i.id in :ids")
    List<ReturnRequestItemEntity> findByIdIn(@Param("ids") List<Long> ids);


    List<ReturnRequestItemEntity> findByBasket_IdIn(List<Long> basketIds);
}
