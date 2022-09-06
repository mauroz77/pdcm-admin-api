package org.cancermodels.types;

public enum Source {
  RULE("Rule"),
  ONTOLOGY("NCIt"),
  LEGACY("Legacy");

  private final String label;

  Source(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}