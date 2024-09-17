package se.mdh.idt.fbdtool.utility;

import se.mdh.idt.fbdtool.writers.CSVWriter;
import se.mdh.idt.fbdtool.writers.ComplexityWriter;
import se.mdh.idt.fbdtool.writers.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;

/**
 * Created by ado_4 on 3/10/2017.
 */
public class SuiteManager {

  private static List<File> fbdProjects;
  private static List<MetricSuite> results;
  private static final String defaultConfig = "config.properties";
  private static final int threadNumber = 100;
  private static TargetType targetType;

  public static void filterPLCProjects(String folderPath, Predicate<File> filePredicate) {
    File dir = new File(folderPath);
    List<File> fileList = Arrays.asList(Objects.requireNonNull(dir.listFiles()));
    fbdProjects = fileList.stream().filter(filePredicate).collect(Collectors.toList());
    System.getLogger("Statistics").log(INFO,"Number of FBD projects: " + fbdProjects.size());
  }

  public static void measurePLCMetrics(String config, String xsdValidation) throws IOException, TimeoutException {
    if (fbdProjects.isEmpty()) {
      throw new NoSuchFileException("No file to be analyzed");
    }
    Properties props = prepareSuite(config);
    List<String> filter = new ArrayList<>();
    if (props.getProperty("filter") != null) {
      filter = Arrays.asList(props.getProperty("filter").split(","));
    }
    targetType = TargetType.valueOf(props.getProperty("complexity.type"));

    ExecutorService service = Executors.newFixedThreadPool(threadNumber);
    boolean finished = false;
    results = new ArrayList<>();
    for (File f : fbdProjects) {
      MetricSuite suite = new MetricSuite(props, f.getPath(), f.getName(), xsdValidation, targetType);
      if (!filter.contains(suite.getName())) {
        results.add(suite);
        service.execute(suite);
      }
    }

    service.shutdown();
    try {
      finished = service.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (!finished) {
      throw new TimeoutException("Set of models not analysed within set timelimit");
    }
  }

  private static Properties prepareSuite(String configPath) throws IOException {
    InputStream inputStream;
    if (!(new File(configPath).exists())) {
      System.out.println("Provided properties file does not exist. Using builtin config.properties.");
      inputStream = SuiteManager.class.getClassLoader().getResourceAsStream(defaultConfig);
    } else {
      inputStream = new FileInputStream(configPath);
    }
    Properties props = new Properties();
    props.load(inputStream);

    return props;
  }

  public static String getMeasurementAsString(ResultOutputFormat format) throws Exception {
    List<String> headerRow = new ArrayList<>();
    headerRow.add("Name");

    if (targetType == TargetType.POU) {
      headerRow.addAll(results.get(0).getPouResults().get(0).keySet());
    } else {
      headerRow.addAll(results.get(0).getProjectresults().keySet());
    }

    StringBuilder sb = new StringBuilder();
    for (MetricSuite suite : results) {
      String formattedOutput = format == ResultOutputFormat.CSV ?
              CSVWriter.writeToString(headerRow, suite, targetType) :
              JsonWriter.writeToString(suite, targetType);
      sb.append(formattedOutput);
    }

    return sb.toString();
  }

  public static void saveMeasurementResults(String output, ResultOutputFormat format) throws Exception {
    List<String> headerRow = new ArrayList<>();
    headerRow.add("Name");
    if (results.isEmpty()) {
      throw new Exception("No results found");
    }
    if (targetType == TargetType.POU) {
      headerRow.addAll(results.get(0).getPouResults().get(0).keySet());
    } else {
      headerRow.addAll(results.get(0).getProjectresults().keySet());
    }

    ComplexityWriter writer = format == ResultOutputFormat.CSV ? new CSVWriter(output, headerRow) : new JsonWriter(output);
    for (MetricSuite suite : results) {
      writer.writeToFile(suite, targetType, false);
    }

    writer.close();
  }
}
