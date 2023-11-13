package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Key-Value to represent counts per release
 */
@Entity
@Data
@NoArgsConstructor
public class ReleaseCounts {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Integer id;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "release_id", nullable = false)
  private Release release;

  private String key;

  private Integer value;
}
