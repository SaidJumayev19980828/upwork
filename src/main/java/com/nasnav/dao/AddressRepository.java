package com.nasnav.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.CountriesEntity;

public interface AddressRepository extends JpaRepository<AddressesEntity, Long> {

    @Query(value = "select * from addresses a join user_Addresses ua on ua.address_id = a.id" +
            " where ua.address_id = :addressId and ua.user_id = :userId ", nativeQuery = true)
    AddressesEntity findByIdAndUserId(@Param("addressId") Long addressId,
                                      @Param("userId") Long userId);

    @Query(value = "select * from addresses a join user_Addresses ua on ua.address_id = a.id" +
            " where ua.user_id = :userId ", nativeQuery = true)
    Set<AddressesEntity> findByUserId(@Param("userId") Long userId);


    @Query(value = "select * from addresses a join user_Addresses ua on ua.address_id = a.id" +
            " where ua.user_id = :userId limit 1", nativeQuery = true)
    Optional<AddressesEntity> findOneByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "delete from user_Addresses where address_id = :addressId and user_id = :userId", nativeQuery = true)
    void unlinkAddressFromUser(@Param("addressId") Long addressId, @Param("userId") Long userId);
    
    @Query("SELECT addr FROM AddressesEntity addr "
    		+ " LEFT JOIN FETCH addr.areasEntity area "
    		+ " LEFT JOIN FETCH area.citiesEntity city "
    		+ " LEFT JOIN FETCH city.countriesEntity country"
    		+ " WHERE addr.id in :ids")
    List<AddressesEntity> findByIdIn(@Param("ids")List<Long> ids);


    @Query(value = "select DISTINCT co from CountriesEntity co left JOIN FETCH co.cities ci left JOIN FETCH ci.areas a")
    List<CountriesEntity> getCountries();
}
