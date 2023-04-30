package com.nasnav.dao;

import com.nasnav.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity,Long> {
    boolean existsByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUser(LocalDateTime start, LocalDateTime end, EmployeeUserEntity employee);
    boolean existsByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUserAndIdNotIn(LocalDateTime start, LocalDateTime end, EmployeeUserEntity employee,List<Long> ids);
    @Query("SELECT c from AvailabilityEntity c where c.organization =:organizationEntity and c.startsAt > CURRENT_DATE and c.user is null group by c.id,c.organization,c.startsAt order by c.startsAt")
    List<AvailabilityEntity> getAllFreeAvailabilitiesByOrganization(OrganizationEntity organizationEntity);

    @Query("SELECT c from AvailabilityEntity c where c.organization =:organizationEntity and c.employeeUser =:employeeUserEntity and c.startsAt > CURRENT_DATE and c.user is null group by c.id,c.organization,c.startsAt order by c.startsAt")
    List<AvailabilityEntity> getAllFreeAvailabilitiesByOrganizationAndEmployeeUser(OrganizationEntity organizationEntity, EmployeeUserEntity employeeUserEntity);

    @Query("SELECT c from AvailabilityEntity c where c.shop =:shopsEntity and c.startsAt > CURRENT_DATE and c.user is null group by c.id,c.shop,c.startsAt order by c.startsAt")
    List<AvailabilityEntity> getAllFreeAvailabilitiesByShop(ShopsEntity shopsEntity);

    /**
     * getting all occupied availabilities for specific employee
     * @param employeeUserEntity
     * @param now
     * @return
     */
    List<AvailabilityEntity> getAllByEmployeeUserAndStartsAtAfterAndUserNotNull(EmployeeUserEntity employeeUserEntity, LocalDateTime now);
    List<AvailabilityEntity> getAllByUserAndStartsAtAfter(UserEntity userEntity, LocalDateTime now);
    List<AvailabilityEntity> getAllByIdInOrderByStartsAtAsc(List<Long> ids);
    List<AvailabilityEntity> deleteAllByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUser(LocalDateTime start, LocalDateTime end, EmployeeUserEntity employee);
}
