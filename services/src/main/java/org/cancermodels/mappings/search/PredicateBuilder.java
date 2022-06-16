package org.cancermodels.mappings.search;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class PredicateBuilder {
  public static Predicate addInPredicates(
      CriteriaBuilder criteriaBuilder, Path<String> path, List<String> values)
  {
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(path.in(values));
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }

  public static Predicate addLowerLikeOrPredicates(
      CriteriaBuilder criteriaBuilder, Path<String> path, List<String> values)
  {
    List<Predicate> predicates = new ArrayList<>();
    values.forEach(x -> predicates.add(buildLowerLikePredicate(criteriaBuilder, path, x)));
    return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
  }

  private static Predicate buildLowerLikePredicate(
      CriteriaBuilder criteriaBuilder, Path<String> path, String value)
  {
    return criteriaBuilder.like(criteriaBuilder.lower(path), value.toLowerCase());
  }

  public static Predicate addLowerLikeOrPredicatesId(
      CriteriaBuilder criteriaBuilder, Path<Long> path, List<String> values)
  {
    List<Predicate> predicates = new ArrayList<>();
    values.forEach(x -> predicates.add(buildLowerLikePredicateId(criteriaBuilder, path, x)));
    return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
  }

  private static Predicate buildLowerLikePredicateId(
      CriteriaBuilder criteriaBuilder, Path<Long> path, String value)
  {
    return criteriaBuilder.equal(path, Long.parseLong(value));
  }
}
