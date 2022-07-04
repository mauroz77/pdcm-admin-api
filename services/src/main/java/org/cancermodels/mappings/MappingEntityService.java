package org.cancermodels.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntityStatus;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider.SummaryEntry;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsSpecs;
import org.cancermodels.mappings.suggestions.SuggestionsManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;
  private final SuggestionsManager suggestionsManager;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      SuggestionsManager suggestionsManager) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.suggestionsManager = suggestionsManager;
  }

  public Page<MappingEntity> findPaginatedAndFiltered(
      Pageable pageable, MappingsFilter mappingsFilter) {

    Specification<MappingEntity> specs = buildSpecifications(mappingsFilter);
    return mappingEntityRepository.findAll(specs, pageable);
  }

  private Specification<MappingEntity> buildSpecifications(MappingsFilter mappingsFilter)
  {
    Specification<MappingEntity> specifications =
        Specification.where(
            MappingsSpecs.withStatus(mappingsFilter.getStatus())
                .and(MappingsSpecs.withMappingQuery(mappingsFilter.getMappingQuery())
                .and(MappingsSpecs.withEntityTypeNames(mappingsFilter.getEntityTypeNames()))
            ));
    return specifications;
  }

  public MappingSummaryByTypeAndProvider getSummaryByTypeAndProvider(String entityTypeName) {
    MappingSummaryByTypeAndProvider mappingSummaryByTypeAndProvider = new MappingSummaryByTypeAndProvider();
    List<SummaryEntry> summaryEntries = new ArrayList<>();
    mappingSummaryByTypeAndProvider.setEntityTypeName(entityTypeName);

    Map<String, Map<String, Integer>> data = new HashMap<>();

    String mappedKey = MappingEntityStatus.MAPPED.getDescription();
    String unmappedKey = MappingEntityStatus.UNMAPPED.getDescription();

    List<Object[]> list = mappingEntityRepository.countEntityTypeStatusByProvider(entityTypeName);
    for (Object[] row : list) {
      String dataSource = row[0].toString();
      String status  = row[1].toString();
      int count = Integer.parseInt(row[2].toString());
      if (!data.containsKey(dataSource)) {
        data.put(dataSource, new HashMap<>());
      }
      data.get(dataSource).put(status, count);
    }

    for (String dataSource : data.keySet()) {
      SummaryEntry summaryEntry = new SummaryEntry();
      summaryEntry.setDataSource(dataSource);
      Map<String, Integer> countByDataSource = data.get(dataSource);

      if (countByDataSource.containsKey(mappedKey)) {
        summaryEntry.setMapped(countByDataSource.get(mappedKey));
      }
      if (countByDataSource.containsKey(unmappedKey)) {
        summaryEntry.setUnmapped(countByDataSource.get(unmappedKey));
      }
      int totalTerms = summaryEntry.getMapped() + summaryEntry.getUnmapped();
      summaryEntry.setTotalTerms(totalTerms);
      summaryEntry.setProgress(summaryEntry.getMapped()*1.0 / totalTerms );
      summaryEntries.add(summaryEntry);
    }

    mappingSummaryByTypeAndProvider.setSummaryEntries(summaryEntries);
    return mappingSummaryByTypeAndProvider;
  }

  public List<MappingEntity> getAllByTypeName(String entityTypeName) {
    return mappingEntityRepository.findAllByEntityTypeNameIgnoreCase(entityTypeName);
  }

  public void calculateSuggestedMappings() {
    // Set suggestions for treatment rules
    List<MappingEntity> allTreatmentMappings = getAllByTypeName("treatment");
    suggestionsManager.updateSuggestedMappingsByExistingRules(allTreatmentMappings);
    mappingEntityRepository.saveAll(allTreatmentMappings);
  }
}
