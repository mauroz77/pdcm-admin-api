package org.cancermodels.admin.mappers;

import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.springframework.stereotype.Component;

@Component
public class MappingEntityMapper {

  public MappingEntityDTO convertToDto(MappingEntity mappingEntity) {
    MappingEntityDTO mappingEntityDTO = new MappingEntityDTO();
    mappingEntityDTO.setId(mappingEntity.getId());
    mappingEntityDTO.setEntityTypeName(mappingEntity.getEntityType().getName());
    mappingEntityDTO.setMappingValues(mappingEntity.getMappingValues());
    mappingEntityDTO.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    mappingEntityDTO.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    mappingEntityDTO.setStatus(mappingEntity.getStatus());
    mappingEntityDTO.setMappingType(mappingEntity.getMappingType());
    mappingEntityDTO.setSource(mappingEntity.getSource());
    mappingEntityDTO.setDateCreated(mappingEntity.getDateCreated());
    mappingEntityDTO.setDateUpdated(mappingEntity.getDateUpdated());

    return mappingEntityDTO;
  }

}
