package se.mdh.idt.fbdtool.writers;

import se.mdh.idt.fbdtool.structures.POU;
import se.mdh.idt.fbdtool.structures.Project;
import se.mdh.idt.fbdtool.utility.MetricSuite;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    try {
      this.writer = new BufferedWriter(new FileWriter(outputFile));
      StringBuilder headerRow = new StringBuilder();
      this.headerList = header;
      for (String s : header) {
        headerRow.append(s + DELIMITER);

      }
      headerRow.deleteCharAt(headerRow.length() - 1);
      headerRow.append(NEWLINE);
      this.writer.write(headerRow.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean write(MetricSuite suite, String type, boolean shouldCloseFile) {

    List<HashMap<String, Double> > measurementResults = new ArrayList<>();
    if (type.equals("PROJECT")) {
      measurementResults.add(suite.getProjectresults());
    } else {
      measurementResults = suite.getPouResults();
    }
    int pouCounter = 0;
    String fileName = "Name";
    StringBuilder row = new StringBuilder();

    for (HashMap<String, Double> results : measurementResults) {
      if (type.equals("PROJECT")) {
        row.append(suite.getName()).append(DELIMITER);
      } else {
        row.append(suite.getName().split(",")[pouCounter]).append(DELIMITER);
        pouCounter++;
      }
      for (String s : this.headerList) {
        if (s.equals(fileName)) {
          continue;
        }

        if (results.get(s) == null) {
          System.out.println("Key " + s + " not found");
        } else {
          DecimalFormat df = new DecimalFormat("#0.00");
          row.append(df.format(results.get(s))).append(DELIMITER);
        }
      }
      row.deleteCharAt(row.length() - 1);
      row.append(NEWLINE);
    }
    boolean success = false;
    try {
      this.writer.write(row.toString());
      if (shouldCloseFile) {
       success = this.close();
      }
      else success = true;
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return success;
  }
}
