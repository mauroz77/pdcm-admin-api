package org.cancermodels.mappings.search;


import java.util.*;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.cancermodels.filters.PredicateBuilder;
import org.cancermodels.pdcm_admin.persistance.EntityType;
import org.cancermodels.pdcm_admin.persistance.EntityType_;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingEntity_;
import org.cancermodels.pdcm_admin.persistance.MappingKey;
import org.cancermodels.pdcm_admin.persistance.MappingKey_;
import org.cancermodels.pdcm_admin.persistance.MappingValue;
import org.cancermodels.pdcm_admin.persistance.MappingValue_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * This class helps to create Specifications with the needed predicates to filter records in a
 * database search.
 */
@Component
public class MappingsSpecs {

  /**
   * Creates the conditions in the WHERE to filter using a list of status
   * @param status List of status to use in the filter
   * @return Specification with the predicate: MappingEntity_.status in (status)
   */
  public static Specification<MappingEntity> withStatus(List<String> status) {
    Specification<MappingEntity> specification = Specification.where(null);
    if (status != null) {
      specification = (Specification<MappingEntity>) (root, query, criteriaBuilder) -> {
        // Explicitly cast to SingularAttribute to resolve ambiguity
        SingularAttribute<? super MappingEntity, String> statusAttribute = MappingEntity_.status;
        Path<String> statusPath = root.get(statusAttribute);

        query.distinct(true);
        return PredicateBuilder.addLowerInPredicates(criteriaBuilder, statusPath, status);
      };
    }
    return specification;
  }

  /**
   * Creates the conditions in the WHERE to filter using a list of entity type names
   * @param entityTypeNames List of status to use in the filter
   * @return Specification with the predicate: MappingEntity_.entityType.name in (entityTypeNames)
   */
  public static Specification<MappingEntity> withEntityTypeNames(List<String> entityTypeNames)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (entityTypeNames != null)
    {
      specification =
          (Specification<MappingEntity>)
              (root, query, criteriaBuilder) -> {
                Path<EntityType> entityPath = root.get(MappingEntity_.entityType);
                Path<String> entityTypeName = entityPath.get(EntityType_.name);
                query.distinct(true);
                return PredicateBuilder.addLowerInPredicates(criteriaBuilder, entityTypeName, entityTypeNames);
              };
    }
    return specification;
  }

  public static Specification<MappingEntity> withMappingType(List<String> mappingTypes)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (mappingTypes != null)
    {
      specification = (Specification<MappingEntity>) (root, query, criteriaBuilder) -> {
        Path<String> mappingTypePath = root.get(MappingEntity_.mappingType);
        query.distinct(true);
        return PredicateBuilder.addLowerInPredicates(
            criteriaBuilder, mappingTypePath, mappingTypes);
      };
    }
    return specification;
  }

  public static Specification<MappingEntity> withLabel(List<String> labels)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (labels != null)
    {
      specification = (root, query, criteriaBuilder) -> {

        List<Predicate> predicates = new ArrayList<>();
        ListJoin<MappingEntity, MappingValue> mappingValuesJoin = root.join(MappingEntity_.mappingValues);

        Path<String> mappingValuePath = mappingValuesJoin.get(MappingValue_.value);
        Path<MappingKey> mappingKeyPath = mappingValuesJoin.get(MappingValue_.mappingKey);
        Path<String> keyValuePath = mappingKeyPath.get(MappingKey_.key);

        // Apply the conditions to key and value
        Predicate keyValuePathPredicate = PredicateBuilder.addInPredicates(
            criteriaBuilder, keyValuePath, Arrays.asList("SampleDiagnosis", "TreatmentName"));
        Predicate mappingValuePredicate =
            PredicateBuilder.addLowerLikeOrPredicates(criteriaBuilder, mappingValuePath, labels);

        predicates.add(keyValuePathPredicate);
        predicates.add(mappingValuePredicate);

        query.distinct(true);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
      };
    }
    return specification;
  }

  /**
   * Creates the conditions in the WHERE to filter using a labels(keys) and values.
   * Because those elements are not columns but records, it's a bit more complex to use them in a
   * query. This solution creates the predicated as subqueries.
   * The main query gets the MappingEntity and each subquery brings the ids of the entities that
   * match every pair of label-value. That allows us to have a query asking for two labels at the
   * same time. Example
   *    select * from mapping_entity me
   *    where me.id in (... select mapping entity id where it's label/key is 'DataSource' and its value is 'trace')
   *    AND   me.id in (... select mapping entity id where it's label/key is 'TumorType' and its value is 'primary')
   * @param mappingQuery Map of labels and values to use in the search. For instance:
   *                     {
   *                        "DataSource": ['trace'],
   *                        "TumorType" : ['Primary']
   *                     }
   * @return A specification that can be used to search by the label/values information given
   * as parameter
   */
  public static Specification<MappingEntity> withMappingQuery(Map<String, List<String>> mappingQuery)
  {
    Specification<MappingEntity> specification = Specification.where(null);
    if (mappingQuery != null)
    {
      specification =
          (Specification<MappingEntity>)
              (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // For every key and its list of values, let's create a subquery
                for (String key : mappingQuery.keySet()) {

                  Subquery<Long> subQuery = query.subquery(Long.class);
                  Root<MappingEntity> subRoot = subQuery.from(MappingEntity.class);

                  // Joins for each subquery
                  ListJoin<MappingEntity, MappingValue> subEntityValuesJoin =
                      subRoot.join(MappingEntity_.mappingValues);
                  Path<String> subMappingValuePath = subEntityValuesJoin.get(MappingValue_.value);
                  Path<MappingKey> subMappingKeyPath =
                      subEntityValuesJoin.get(MappingValue_.mappingKey);
                  Path<String> subKeyValuePath = subMappingKeyPath.get(MappingKey_.key);

                  // Apply the conditions to key and value
                  Predicate subKeyValuesPredicate = PredicateBuilder.addLowerInPredicates(
                      criteriaBuilder, subKeyValuePath, Arrays.asList(key));
                  Predicate subMappingValuesPredicate =
                      PredicateBuilder.addLowerInPredicates(
                          criteriaBuilder, subMappingValuePath, mappingQuery.get(key));

                  Predicate subKeyAndValuePredicate =
                      criteriaBuilder.and(subKeyValuesPredicate, subMappingValuesPredicate);

                  subQuery =
                      subQuery
                          .select(subRoot.get(MappingEntity_.ID))
                          .where(subKeyAndValuePredicate);
                  // Add the subquery to the main query using an "in" statement
                  predicates.add(root.get(MappingEntity_.ID).in(subQuery));
                }
                query.distinct(true);
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
              };
    }
    return specification;
  }
}
