package org.cancermodels.mappings;

import java.time.LocalDateTime;
import org.cancermodels.types.MappingType;
import org.cancermodels.types.Status;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class Updater {

  private final MappingEntityRepository mappingEntityRepository;

  public Updater(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }


  public MappingEntity update(MappingEntity original, MappingEntity withChanges, MappingType mappingType) {
    boolean mappedTermChanged = processMappedTermChange(original, withChanges, mappingType);
    boolean statusChanged = processStatusChange(original, withChanges);

    if (mappedTermChanged || statusChanged) {
      original.setDateUpdated(LocalDateTime.now());
      mappingEntityRepository.save(original);
    }

    return original;
  }

  private boolean processMappedTermChange(MappingEntity original, MappingEntity withChanges,
      MappingType mappingType) {
    boolean changed = false;
    String originalMappedTermUrl =
        original.getMappedTermUrl() == null ? "" : original.getMappedTermUrl() ;
    String newMappedTermUrl =
        withChanges.getMappedTermUrl() == null ? "" : withChanges.getMappedTermUrl() ;

    if (!originalMappedTermUrl.equalsIgnoreCase(newMappedTermUrl) ) {
      original.setMappedTermUrl(newMappedTermUrl);
      original.setSource(withChanges.getSource());
      String newMappedTermLabel = withChanges.getMappedTermLabel();
      original.setMappedTermLabel(newMappedTermLabel);
      original.setMappingType(mappingType.getLabel());
      changed = true;
    }

    // A new mapping was generated
    if (originalMappedTermUrl.equals("") && !newMappedTermUrl.equals("")) {
      // Simulate a request of status change
      withChanges.setStatus(Status.MAPPED.getLabel());
    }

    return changed;
  }

  /**
   * Checks that if status changed the transitions are valid:
   *  - Unmapped -> Mapped
   *  - Unmapped -> Request
   *  - Mapped -> Revise
   *  - Revise -> Mapped
   *  - Request -> Unmapped
   *  @param original Mapping Entity it is in the database.
   * @param withChanges Edited Mapping entity.
   */
  private boolean processStatusChange(MappingEntity original, MappingEntity withChanges) {
    boolean changed = false;
    String originalStatus = original.getStatus();
    String newStatus = withChanges.getStatus();
    boolean valid = false;
    if (!originalStatus.equalsIgnoreCase(newStatus)) {
      changed = true;

      // Valid transitions
      if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REQUEST.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.MAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REVISE.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.REVISE.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.REQUEST.getLabel().equalsIgnoreCase(originalStatus)
          && Status.UNMAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
        original.setMappedTermUrl(null);
        original.setMappedTermLabel(null);
        original.setSource(null);
        original.setMappingType(null);
      }
      original.setStatus(newStatus);

      // Status did not change
    } else {
      valid = true;
    }

    if (!valid) {
      // TODO: change to specific exception
      throw new IllegalArgumentException(
          String.format( "Cannot change status from [%s] to [%s]", originalStatus, newStatus));
    }
    return changed;
  }

}
