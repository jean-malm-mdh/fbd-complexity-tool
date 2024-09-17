package se.mdh.idt.fbdtool;

import org.apache.commons.cli.CommandLine;
import se.mdh.idt.fbdtool.utility.CLI;
import se.mdh.idt.fbdtool.utility.ResultOutputFormat;
import se.mdh.idt.fbdtool.utility.SuiteManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static java.lang.System.Logger.Level.INFO;

public class App {

  public static void main(String[] args) {
    System.getLogger("Statistics").log(INFO,"Starting FBD projects measurement...");
    long startTime = System.currentTimeMillis();
    CommandLine cli = CLI.parseArguments(args, CLI.generateCLIOptions());

    Predicate<File> filePredicate = f -> f.isFile() && (f.getName().endsWith(".xml") || f.getName().endsWith(".pou"));
    try {
      SuiteManager.filterPLCProjects(cli.getOptionValue("f"), filePredicate);
      SuiteManager.measurePLCMetrics(cli.getOptionValue("c"), cli.getOptionValue("v"));
      if (cli.hasOption('o'))
      SuiteManager.saveMeasurementResults(cli.getOptionValue("o"),
                cli.getOptionValue("o").contains(".csv") ? ResultOutputFormat.CSV : ResultOutputFormat.JSON);
      else
      {
        System.out.print(SuiteManager.getMeasurementAsString(ResultOutputFormat.JSON));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException t) {
      t.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis();
    System.getLogger("Statistics").log(INFO,"Measurement time: " + (endTime - startTime) + " milliseconds");
  }
}
