package com.nasnav.yeshtery.dao;

import com.nasnav.persistence.YeshteryUserAddressEntity;
import com.nasnav.persistence.YeshteryUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface YeshteryUserAddressRepository extends JpaRepository<YeshteryUserAddressEntity, Long> {

    @Query(value = "select ua " +
            " from YeshteryUserEntity usr " +
            " left join usr.userAddresses ua"+
            " left join fetch ua.address a " +
            " left join fetch a.areasEntity area " +
            " left join fetch area.citiesEntity city " +
            " left join fetch city.countriesEntity country " +
            " left join fetch a.subAreasEntity subArea "+
            " where usr.id = :userId order by ua.principal desc")
    List<YeshteryUserAddressEntity> findByUser_Id(@Param("userId") Long userId);
}
