package com.nasnav.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.CountriesEntity;

public interface AddressRepository extends JpaRepository<AddressesEntity, Long> {

    @Query(value = "select addr from UserEntity usr left join usr.addresses addr" +
            " where usr.id =:userId and addr.id = :addressId")
    Optional<AddressesEntity> findByIdAndUserId(@Param("addressId") Long addressId,
                                                @Param("userId") Long userId);

    @Query(value = "select addr from UserEntity usr left join usr.addresses addr" +
            " where usr.id = :userId ")
    List<AddressesEntity> findByUserId(@Param("userId") Long userId);


    @Query(value = "select addr from user_addresses ua left join addresses addr on ua.address_id = addr.id" +
            " where ua.user_id = :userId limit 1", nativeQuery = true)
    Optional<AddressesEntity> findOneByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "delete from user_Addresses where address_id = :addressId and user_id = :userId", nativeQuery = true)
    void unlinkAddressFromUser(@Param("addressId") Long addressId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "insert into user_Addresses (user_id, Address_id) values(:userId, :addressId)", nativeQuery = true)
    void linkAddressToUser(@Param("userId") Long userId, @Param("addressId") Long addressId);



    @Query("SELECT addr FROM AddressesEntity addr "
            + " LEFT JOIN FETCH addr.areasEntity area "
            + " LEFT JOIN FETCH area.citiesEntity city "
            + " LEFT JOIN FETCH city.countriesEntity country"
            + " WHERE addr.id in :ids")
    List<AddressesEntity> findByIdIn(@Param("ids")List<Long> ids);


    @Query(value = "select DISTINCT co from CountriesEntity co left JOIN FETCH co.cities ci left JOIN FETCH ci.areas a")
    List<CountriesEntity> getCountries();

    @Transactional
    @Modifying
    @Query(value = "update user_addresses set principal = false where user_id = :userId", nativeQuery = true)
    void makeAddressNotPrincipal(@Param("userId")Long userId);


    @Transactional
    @Modifying
    @Query(value = " update user_addresses set principal = true where user_id = :userId and address_id = :addrId", nativeQuery = true)
    void makeAddressPrincipal(@Param("userId")Long userId, @Param("addrId")Long addrId);
}
