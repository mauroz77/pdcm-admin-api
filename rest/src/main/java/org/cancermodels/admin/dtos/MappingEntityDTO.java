package org.cancermodels.admin.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.cancermodels.persistance.Suggestion;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "mappings")
@Data
public class MappingEntityDTO {
  private int id;
  private String entityTypeName;
  private Map<String, String> mappingValues;
  private String mappedTermUrl;
  private String mappedTermLabel;
  private String status;
  private String mappingType;
  private String source;
  private List<Suggestion> suggestions;
  private LocalDateTime dateCreated;
  private LocalDateTime dateUpdated;
}
