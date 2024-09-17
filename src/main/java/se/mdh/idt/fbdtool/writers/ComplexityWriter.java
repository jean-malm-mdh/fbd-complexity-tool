package se.mdh.idt.fbdtool.writers;

import se.mdh.idt.fbdtool.structures.POU;
import se.mdh.idt.fbdtool.structures.Project;
import se.mdh.idt.fbdtool.utility.MetricSuite;
import se.mdh.idt.fbdtool.utility.TargetType;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by ado_4 on 3/11/2017.
 */
public interface ComplexityWriter {
  Writer getWriter();

  boolean writeToFile(MetricSuite suite, TargetType type, boolean shouldCloseFile);
  default boolean close() {
    boolean success = false;
    try {
      getWriter().flush();
      getWriter().close();
      success = true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return success;
  }
}
