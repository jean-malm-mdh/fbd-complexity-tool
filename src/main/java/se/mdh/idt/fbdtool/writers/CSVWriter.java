package se.mdh.idt.fbdtool.writers;

import se.mdh.idt.fbdtool.utility.MetricSuite;
import se.mdh.idt.fbdtool.utility.TargetType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by ado_4 on 3/11/2017.
 */
public class CSVWriter implements ComplexityWriter {
  public static final String DELIMITER = ",";
  public static final String NEWLINE = "\n";
  private BufferedWriter writer;
  private List<String> headerList;

  @Override
  public BufferedWriter getWriter() {
    return writer;
  }

  public CSVWriter(String outputFile, List<String> header) {
      this.headerList = header;
      try {
          this.writer = new BufferedWriter(new FileWriter(outputFile));
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }

  public static String writeToString(List<String> headerList, MetricSuite suite, TargetType type) {

    StringBuilder headerRow = new StringBuilder();
    for (String s : headerList) {
      headerRow.append(s).append(DELIMITER);
    }
    headerRow.deleteCharAt(headerRow.length() - 1);
    headerRow.append(NEWLINE);
    List<HashMap<String, Double>> measurementResults = new ArrayList<>();
    if (type == TargetType.PROJECT) {
      measurementResults.add(suite.getProjectresults());
    } else {
      measurementResults = suite.getPouResults();
    }
    int pouCounter = 0;
    String fileName = "Name";
    StringBuilder row = new StringBuilder();

    for (HashMap<String, Double> results : measurementResults) {
      if (type == TargetType.PROJECT) {
        row.append(suite.getName()).append(DELIMITER);
      } else {
        row.append(suite.getName().split(",")[pouCounter]).append(DELIMITER);
        pouCounter++;
      }
      for (String s : headerList) {
        if (s.equals(fileName)) {
          continue;
        }

        if (results.get(s) == null) {
          System.err.println("Key " + s + " not found");
        } else {
          DecimalFormat df = new DecimalFormat("#0.00",
                  DecimalFormatSymbols.getInstance(Locale.forLanguageTag("US")));
          row.append(df.format(results.get(s))).append(DELIMITER);
        }
      }
      row.deleteCharAt(row.length() - 1);
      row.append(NEWLINE);
    }
    return headerRow.append(row).toString();
  }

  @Override
  public boolean writeToFile(MetricSuite suite, TargetType type, boolean shouldCloseFile) {
    String result = writeToString(this.headerList, suite, type);
    boolean success = false;
    try {
      this.writer.write(result);
      if (shouldCloseFile) {
       this.close();
      }
      success = true;
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return success;
  }
}
