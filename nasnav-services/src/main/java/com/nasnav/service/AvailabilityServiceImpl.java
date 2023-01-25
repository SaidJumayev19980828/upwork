package com.nasnav.service;

import com.nasnav.dao.AvailabilityRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.request.AvailabilityDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private ShopsRepository shopsRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private SchedulerTaskService schedulerTaskService;
    @Autowired
    private UserService userService;

    private List<AvailabilityEntity> createAvailabilities(AvailabilityDTO dto) {
        if(overlappingValidator(dto)){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$OVER$LAPPED,"please input empty interval");
        }

        LocalDateTime from =  dto.getStartsAt().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime to =  dto.getEndsAt().truncatedTo(ChronoUnit.MINUTES);
        long periodInMinute = dto.getPeriod();

        Duration duration = Duration.between(from,to);
        long minutes = duration.toMinutes();
        List<AvailabilityEntity> availabilityEntities = new ArrayList<>();

        while (minutes > 0 && minutes >= periodInMinute){
            availabilityEntities.add(toEntity(dto,from,from.plusMinutes(periodInMinute)));
            minutes -= periodInMinute;
            from = from.plusMinutes(periodInMinute);
        }
        availabilityEntities = availabilityRepository.saveAll(availabilityEntities);
        return availabilityEntities;
    }

    @Transactional
    @Override
    public List<AvailabilityDTO> overrideAvailabilities(AvailabilityDTO dto, boolean forceFlag) {
        boolean overLapped = overlappingValidator(dto);
        if(!forceFlag && overLapped)
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$OVER$LAPPED,"please input empty interval");

        List<AvailabilityEntity> deletedReservedEntities = getDeletedReservedAvailabilities(dto);
        List<AvailabilityEntity> savedEntities = createAvailabilities(dto);
        if(overLapped){
            schedulerTaskService.overrideAppointment(deletedReservedEntities,savedEntities);
        }
        return savedEntities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AvailabilityEntity getById(Long id){
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND,AVA$NOT$EXIST,"No availabilty found with this id"));
    }

    @Transactional
    @Override
    public List<AvailabilityDTO> shiftUpcomingAppointments(List<Long> ids, long postponedPeriodsInMins) {
        List<AvailabilityEntity> entities = availabilityRepository.getAllByIdInOrderByStartsAtAsc(ids);
        for(AvailabilityEntity entity:entities){
            entity.setStartsAt(entity.getStartsAt().plusMinutes(postponedPeriodsInMins));
            entity.setEndsAt(entity.getEndsAt().plusMinutes(postponedPeriodsInMins));
            if(overlappingValidator(entity,ids)){
                throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$OVER$LAPPED,"please input empty interval");
            }
            schedulerTaskService.updateAppointment(entity.getId());
        }
        return availabilityRepository.saveAll(entities).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AvailabilityEntity reserveAvailability(Long id) {
        Optional<AvailabilityEntity> availabilityEntity = availabilityRepository.findById(id);
        if(!availabilityEntity.isPresent()){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$NOT$EXIST,"availability not found");
        }
        availabilityEntity.get().setUser(getUser());
        return availabilityRepository.save(availabilityEntity.get());
    }

    @Override
    public void cancelReserveAvailability(Long id) {
        Optional<AvailabilityEntity> availabilityEntity = availabilityRepository.findById(id);
        if(!availabilityEntity.isPresent()){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$NOT$EXIST,"availability not found");
        }
        availabilityEntity.get().setUser(null);
        availabilityRepository.save(availabilityEntity.get());
    }

    @Override
    public void deleteAvailabilitiesByRange(AvailabilityDTO dto, boolean forceFlag) {
        boolean overLapped = overlappingValidator(dto);
        if(!forceFlag && overLapped)
            throw new RuntimeBusinessException(NOT_ACCEPTABLE,AVA$OVER$LAPPED,"please input empty interval");

        schedulerTaskService.deleteAppointmentFromEmployee(getDeletedReservedAvailabilities(dto));
    }

    @Override
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrg(Long orgId) {
        Optional<OrganizationEntity> org = organizationRepository.findById(orgId);
        if(org.isPresent()){
            return availabilityRepository.getAllFreeAvailabilitiesByOrganization(org.get())
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByShop(Long shopId) {
        Optional<ShopsEntity> shop = shopsRepository.findById(shopId);
        if(shop.isPresent()){
            return availabilityRepository.getAllFreeAvailabilitiesByShop(shop.get())
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<AvailabilityDTO> getAllOccupiedAvailabilitiesByLoggedEmployee() {
        return availabilityRepository.getAllByEmployeeUserAndStartsAtAfterAndUserNotNull(getEmployee(),LocalDateTime.now())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDTO> getAllAppointmentsByLoggedUser() {
        return availabilityRepository.getAllByUserAndStartsAtAfter(getUser(),LocalDateTime.now())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AvailabilityDTO> getAllAppointmentsByUserId(Long id) {
        BaseUserEntity user = userService.getUserById(id);
        if(user != null && user instanceof UserEntity){
            return availabilityRepository.getAllByUserAndStartsAtAfter((UserEntity) user,LocalDateTime.now())
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        return null;
    }

    private boolean overlappingValidator(AvailabilityDTO dto) {
        return availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUser
                (dto.getStartsAt(), dto.getEndsAt(),getEmployee());
    }

    private boolean overlappingValidator(AvailabilityEntity entity, List<Long> ids) {
        return availabilityRepository.existsByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUserAndIdNotIn
                (entity.getStartsAt(), entity.getEndsAt(),getEmployee(),ids);
    }

    private AvailabilityEntity toEntity(AvailabilityDTO dto, LocalDateTime from, LocalDateTime to){
        ShopsEntity shopEntity = null;
        if(dto.getShopId() != null){
            shopEntity = shopsRepository.findById(dto.getShopId())
                    .orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND,S$0003, " with id : "+dto.getShopId()));
        }
        OrganizationEntity organizationEntity = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(()-> new RuntimeBusinessException(NOT_FOUND,ORG$NOTFOUND, " with id : "+dto.getOrganizationId()));

        AvailabilityEntity entity = new AvailabilityEntity();
        entity.setStartsAt(from);
        entity.setEndsAt(to);
        entity.setOrganization(organizationEntity);
        entity.setEmployeeUser(getEmployee());
        entity.setShop(shopEntity);
        return entity;
    }

    private AvailabilityDTO toDTO(AvailabilityEntity entity){
        AvailabilityDTO availabilityDTO = new AvailabilityDTO();
        availabilityDTO.setId(entity.getId());
        availabilityDTO.setStartsAt(entity.getStartsAt());
        availabilityDTO.setEndsAt(entity.getEndsAt());
        availabilityDTO.setOrganizationId(entity.getOrganization().getId());
        availabilityDTO.setPeriod(Duration.between(entity.getStartsAt(),entity.getEndsAt()).toMinutes());
        if(entity.getShop() != null)
            availabilityDTO.setShopId(entity.getShop().getId());
        return availabilityDTO;
    }

    private List<AvailabilityEntity> getDeletedReservedAvailabilities(AvailabilityDTO dto){
        List<AvailabilityEntity> deletedEntities = availabilityRepository.deleteAllByEndsAtIsGreaterThanAndStartsAtIsLessThanAndEmployeeUser(dto.getStartsAt(),dto.getEndsAt(),getEmployee());
        return deletedEntities.stream()
                .filter((entity) -> entity.getUser() != null).collect(Collectors.toList());
    }

    private EmployeeUserEntity getEmployee(){
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof EmployeeUserEntity){
            return (EmployeeUserEntity) loggedInUser;
        }
        else {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, TYP$0001, "User should be Employee");
        }
    }

    private UserEntity getUser(){
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof UserEntity){
            return (UserEntity) loggedInUser;
        }
        else {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, TYP$0001, "User should be Customer");
        }
    }
}
