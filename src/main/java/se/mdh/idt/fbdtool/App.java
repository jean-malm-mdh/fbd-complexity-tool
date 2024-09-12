package se.mdh.idt.fbdtool;

import org.apache.commons.cli.CommandLine;
import se.mdh.idt.fbdtool.utility.CLI;
import se.mdh.idt.fbdtool.utility.SuiteManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class App {

  public static void main(String[] args) {
    System.out.println("Starting FBD projects measurement...");
    long startTime = System.currentTimeMillis();
    CommandLine cli = CLI.parseArguments(args, CLI.generateCLIOptions());

    Predicate<File> filePredicate = f -> f.isFile() && (f.getName().endsWith(".xml") || f.getName().endsWith(".pou"));
    try {
      SuiteManager.filterPLCProjects(cli.getOptionValue("f"), filePredicate);
      SuiteManager.measurePLCMetrics(cli.getOptionValue("c"), cli.getOptionValue("v"));
      SuiteManager.saveMeasurementResults(cli.getOptionValue("o"));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException t) {
      t.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis();
    System.out.println("Measurement time: " + (endTime - startTime) + " milliseconds");
  }
}
