package org.cancermodels.pdcm_admin.types;

import lombok.Getter;

@Getter
public enum MappingKeyName {
  DATASOURCE("DataSource"),
  SAMPLE_DIAGNOSIS("SampleDiagnosis"),
  TUMOR_TYPE("TumorType"),
  ORIGIN_TISSUE("OriginTissue"),
  TREATMENT_NAME("TreatmentName");

  private final String label;

  MappingKeyName(String label) {
    this.label = label;
  }

}
