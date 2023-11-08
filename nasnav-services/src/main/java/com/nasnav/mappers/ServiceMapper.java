package com.nasnav.mappers;

import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.PackageResponse;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.persistence.PackageEntity;
import com.nasnav.persistence.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.LinkedList;
import java.util.List;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);
    ServiceEntity dtoToEntity(ServiceDTO serviceDTO);
    ServiceResponse entityToDto(ServiceEntity serviceEntity);

    default List<ServiceResponse> entitiesToBeansWithoutList(List<ServiceEntity> entities){
        if(entities == null) return new LinkedList<>();
        List<ServiceResponse> beans = new LinkedList<>();
        for (ServiceEntity entity : entities){
            beans.add(this.entityToDto(entity));
        }
        return beans;
    }
}
