package org.cancermodels.prototype;

import static org.cancermodels.mappings.suggestions.SuggestionsConstants.UNKNOWN_ELEMENT;
import static org.cancermodels.mappings.suggestions.SuggestionsConstants.UNKNOWN_VALUES;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermsWeightedSimilarityCalculator {

  private static final Logger LOG = LoggerFactory.getLogger(TermsWeightedSimilarityCalculator.class);

  private final SimilarityComparator similarityComparator;

  public TermsWeightedSimilarityCalculator(SimilarityComparator similarityComparator) {
    this.similarityComparator = similarityComparator;
  }

  public double calculateTermsWeightedSimilarity(
      Map<String, String> leftValues,
      Map<String, String> rightValues,
      Map<String, Double> weights) {

    validate(leftValues, rightValues, weights);

    double score = 0;

    for (String key : leftValues.keySet()) {

      String leftTerm = leftValues.get(key);
//      System.out.println("orig l: " + leftTerm);

      leftTerm = transformTerm(leftTerm);

      String rightTerm = rightValues.get(key);
//      System.out.println("orig r: " + rightTerm);

      rightTerm = transformTerm(rightTerm);
      double weight = weights.get(key);

      score = score + similarityComparator.calculate(leftTerm, rightTerm) * weight;
//      System.out.printf("l[%s] r[%s] : [%s]%n", leftTerm, rightTerm, score);
    }

    return score;

  }

  private void validate(
      Map<String, String> leftValues,
      Map<String, String> rightValues,
      Map<String, Double> weights) {

    if (leftValues.size() != rightValues.size() || leftValues.size() != weights.size()) {
      LOG.error("Wrong number of values in TermsWeightedSimilarityCalculator");
      LOG.error(leftValues.toString());
      LOG.error(rightValues.toString());
      LOG.error(weights.toString());

      throw new IllegalArgumentException(
          "Wrong number of values in TermsWeightedSimilarityCalculator");
    }

    double totalWeight = 0;

    for (Double value : weights.values()) {
      totalWeight += value;
    }

    if (totalWeight != 1) {
      LOG.error("Total weight must be 1");
      LOG.error(weights.toString());
      throw new IllegalArgumentException("Wrong weights");
    }

  }

  /**
   * Apply whatever transformation is needed to help the similarity calculation process
   * @param term Original term
   * @return Transformed term
   */
  private String transformTerm(String term) {
    if (UNKNOWN_VALUES.contains(term.toLowerCase())) {
      term = UNKNOWN_ELEMENT;
    }
    return term;
  }
}
