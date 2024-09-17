package se.mdh.idt.fbdtool.utility;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ado_4 on 3/9/2017.
 */
public class CLI {
  public static Options generateCLIOptions() {
    Options options = new Options();
    List<Option> optionList = new ArrayList<Option>();
    optionList.add(new Option("f", "file", true, "target file, or directory containing PLC files."));
    optionList.add(new Option("c", "config", true, "Java configuration property file."));
    optionList.add(new Option("v", "validate", true, "Validate project files against an xsd schema."));
    optionList.add(new Option("o", "output", true, "Output file path"));

    String[] optionals = {"v", "o", "c"};
    for (Option o : optionList) {
      if (Arrays.stream(optionals).anyMatch(e -> o.getOpt().equals(e))) {
        o.setRequired(false);
      } else {
        o.setRequired(true);
      }

      if (o.getOpt().equals("m")) {
        o.setArgs(Option.UNLIMITED_VALUES);
      }

      options.addOption(o);
    }

    return options;
  }

  public static CommandLine parseArguments(String[] args, Options options) {
    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      formatter.printHelp("java -jar " + "tiqva"
              + " -f <target_path>"
              + " [-c <config_file>]"
              + " [-o <output_path>]"
              + "[-v <validation_schema>]",options);
      for (Object op : options.getRequiredOptions())
      {
        System.getLogger("Error").log(System.Logger.Level.ERROR, "option " + op.toString() + " is required, but missing");
      }

      System.exit(1);
      return null;
    }
    return cmd;
  }
}
