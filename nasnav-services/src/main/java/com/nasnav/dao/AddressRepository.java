package com.nasnav.dao;

import com.nasnav.dto.AddressRepObj;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.CountriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AddressRepository extends JpaRepository<AddressesEntity, Long> {

    boolean existsByAreasEntity_IdIn(List<Long> areaId);

    @Query(value = "select addr from UserEntity usr left join usr.addresses addr" +
            " where usr.id =:userId and addr.id = :addressId")
    Optional<AddressesEntity> findByIdAndUserId(@Param("addressId") Long addressId,
                                                @Param("userId") Long userId);

    @Query(value = "select count(usr.id) from UserEntity usr left join usr.addresses addr" +
                   " where usr.id =:userId and addr.id = :addressId")
    Integer countByUserIdAndAddressId(@Param("addressId") Long addressId,
                                      @Param("userId") Long userId);

    @Query(value = "select new com.nasnav.dto.AddressRepObj(a.id, a.firstName, a.lastName, a.flatNumber, a.buildingNumber, a.addressLine1," +
                    " a.addressLine2, a.latitude, a.longitude, a.postalCode, a.phoneNumber, ua.principal, area.id," +
                    " area.name , city.name , country.name, subArea.id, subArea.name) " +
                    " from UserEntity usr " +
                    " left join usr.userAddresses ua"+
                    " left join ua.address a " +
                    " left join a.areasEntity area " +
                    " left join area.citiesEntity city " +
                    " left join city.countriesEntity country " +
                    " left join a.subAreasEntity subArea "+
                    " where usr.id = :userId order by ua.principal desc")
    List<AddressRepObj> findByUserId(@Param("userId") Long userId);

    @Query(value = "select a " +
            " from UserEntity usr " +
            " left join  usr.userAddresses ua"+
            " left join ua.address a " +
            " left join fetch a.areasEntity area " +
            " left join fetch area.citiesEntity city " +
            " left join fetch city.countriesEntity country " +
            " left join fetch a.subAreasEntity subArea "+
            " where usr.id = :userId order by ua.principal desc")
    List<AddressesEntity> findAddressByUserId(@Param("userId") Long userId);



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
            + " LEFT JOIN FETCH city.countriesEntity country "
            + " LEFT JOIN FETCH addr.subAreasEntity subArea "
            + " WHERE addr.id in :ids")
    List<AddressesEntity> findByIdIn(@Param("ids")List<Long> ids);


    @Query("SELECT addr FROM AddressesEntity addr "
            + " LEFT JOIN FETCH addr.areasEntity area "
            + " LEFT JOIN FETCH area.citiesEntity city "
            + " LEFT JOIN FETCH city.countriesEntity country "
            + " LEFT JOIN FETCH addr.subAreasEntity subArea "
            + " WHERE addr.id = :id")
    Optional<AddressesEntity> findByIdWithDetails(@Param("id")Long id);


    @Query(value = "select DISTINCT country " +
            " from CountriesEntity country " +
            " left JOIN FETCH country.cities city " +
            " left JOIN FETCH city.areas area " +
            " order by country.name")
    List<CountriesEntity> getCountries();

    @Transactional
    @Modifying
    @Query(value = "update user_addresses set principal = false where user_id = :userId", nativeQuery = true)
    void makeAddressNotPrincipal(@Param("userId")Long userId);


    @Transactional
    @Modifying
    @Query(value = " update user_addresses set principal = true where user_id = :userId and address_id = :addrId", nativeQuery = true)
    void makeAddressPrincipal(@Param("userId")Long userId, @Param("addrId")Long addrId);


    @Transactional
    @Modifying
    @Query(" update AddressesEntity addr " +
            " set addr.subAreasEntity = null " +
            " where addr.subAreasEntity.id in :subAreas")
    void clearSubAreasFromAddresses(@Param("subAreas") Set<Long> subAreas);
}
