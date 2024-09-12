package se.mdh.idt.fbdtool.utility;

import org.dom4j.DocumentException;
import se.mdh.idt.fbdtool.metrics.*;
import se.mdh.idt.fbdtool.parsers.fbd.DOM4JParser;
import se.mdh.idt.fbdtool.parsers.fbd.FBDParser;
import se.mdh.idt.fbdtool.structures.POU;
import se.mdh.idt.fbdtool.structures.Project;

import java.util.*;
import java.util.function.Function;


/**
 * Created by ado_4 on 3/10/2017.
 */
public class MetricSuite implements Runnable {
  HashMap<String, Double> ProjectResults;
  List<HashMap<String, Double> > pouResults;
  List<ComplexityMetric> metricList;

  private FBDParser parser;
  private Properties config;
  private String filePath;
  private String name;
  private TargetType targetType;
  private boolean validated = false;

  public MetricSuite(Properties config, String filePath, String name, TargetType targetType) {
    this.init(config, filePath, name, targetType);
  }

  public MetricSuite(Properties config, String filePath, String name, String xsdPath, TargetType targetType) {
    this.init(config, filePath, name, targetType);
    this.validated = XMLProjectValidator.validateProjectFile(filePath, xsdPath);
  }

  public String getName() {
    return name;
  }

  public HashMap<String, Double> getProjectresults() {
    return this.ProjectResults;
  }

  public List<HashMap<String, Double> > getPouResults() {
    return this.pouResults;
  }

  private boolean configureSuite(Properties config, String folderPath) {
    try {
      parser = new DOM4JParser(folderPath, config);
    } catch (DocumentException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private HashMap<String, Double> computeProjectComplexity(Project project) {
    this.ProjectResults = new HashMap<>();
    for (ComplexityMetric metric : this.metricList) {
      this.ProjectResults.putAll(metric.measureProjectComplexity(project));
    }
    return this.ProjectResults;
  }

  private void init(Properties config, String filePath, String name, TargetType targetType) {
    this.config = config;
    this.filePath = filePath;
    this.name = name.split(".xml")[0];
    this.targetType = targetType;
    initializeMetrics(config);
  }
  private static Optional<ComplexityMetric> MetricsFactory(Properties config, String metric_type)
  {
    switch (metric_type) {
      case "noe":
        return Optional.of(new NOEMetric());
      case "hc":
        return Optional.of(new HalsteadMetric());
      case "ifc":
        return Optional.of(new IFCMetric());
      case "cc":
        return Optional.of(new CCMetric(config));
      default:
        // Would be more elegant to return an Error here, I suppose
        System.err.println(metric_type + " is not a valid metric. Skipped!");
        return Optional.empty();
    }
  }
  private static Function<String, Optional<ComplexityMetric>>
    applyConfig(Properties config)
  {
    Function<Properties, Function<String, Optional<ComplexityMetric>>> part_metrics_factory =
            aProp -> aMetric -> MetricsFactory(aProp, aMetric);
    return part_metrics_factory.apply(config);
  }
  private void initializeMetrics(Properties config) {
    this.metricList = new ArrayList<>();
    Function<String, Optional<ComplexityMetric>> prop_applied = applyConfig(config);
    String chosenMetrics = config.getProperty("complexity.metrics");
    Iterator<ComplexityMetric> metrics =
            Arrays.stream(chosenMetrics.split(","))
            .map((prop_applied))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .iterator();
    metrics.forEachRemaining(m -> this.metricList.add(m));
  }

  private static String computeResultName(Project project)
  {
    StringBuilder completeName = new StringBuilder();
    project.getPOUs().stream().iterator().forEachRemaining(p -> completeName.append(p.getName()).append(','));

    completeName.append(project.getTitle());
    return completeName.toString();
  }
  private List<HashMap<String, Double> > pouComplexity(Project project) {
    this.pouResults = new ArrayList<>();

    for (POU pou : project.getPOUs()) {
      HashMap<String, Double> results = new HashMap<>();
      this.metricList.iterator().forEachRemaining(
              m -> results.putAll(m.measurePOUComplexity(pou)));
      this.pouResults.add(results);
    }
    pouResults.add(this.computeProjectComplexity(project));
    this.name = computeResultName(project);
    return this.pouResults;
  }


  public void measureComplexity(TargetType targetType) {
    Project project = this.parser.extractFBDProject();
    switch (targetType)
    {
      case PROJECT:
        this.computeProjectComplexity(project);
        break;
      case POU:
        this.pouComplexity(project);
        break;
    }
  }

  @Override
  public void run() {
    if (!this.configureSuite(this.config, this.filePath))
    {
      System.err.println("Parser not initiated correctly.");
      return;
    }
    if (!validated) {
      System.err.println(this.getName() + " " + "does not pass XSD schema validation");
    } else {
      this.measureComplexity(this.targetType);
    }
  }
}
