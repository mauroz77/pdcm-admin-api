package org.cancermodels.admin;

import org.cancermodels.OntologyLoadReport;
import org.cancermodels.ontologies.OntologyLoaderService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
public class OntologyController {

  private final OntologyLoaderService ontologyLoaderService;

  public OntologyController(OntologyLoaderService ontologyLoaderService) {
    this.ontologyLoaderService = ontologyLoaderService;
  }

  /**
   * Reloads the ontologies tables in the h2 database using OLS as a source
   * @return {@link OntologyLoadReport} object with a report of the process
   */
  @GetMapping("loadOntologies")
  public OntologyLoadReport loadOntologies() {
    return ontologyLoaderService.loadOntologies();
  }
}
