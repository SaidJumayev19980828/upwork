package com.nasnav.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CallQueueMapper {
    CallQueueMapper INSTANCE = Mappers.getMapper(CallQueueMapper.class);

//    CallQueueEntity dtoToEntity(CallQueueDTO callQueueDTO);
//    CallQueueDTO entityToDto(CallQueueEntity callQueueEntity);
//
//    default List<CallQueueDTO> entitiesToBeansWithoutList(List<CallQueueEntity> entities){
//        if(entities == null) return new LinkedList<>();
//        List<CallQueueDTO> beans = new LinkedList<>();
//        for (CallQueueEntity entity : entities){
//            beans.add(this.entityToDto(entity));
//        }
//        return beans;
//    }

}
