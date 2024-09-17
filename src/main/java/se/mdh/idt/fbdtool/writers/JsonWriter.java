package se.mdh.idt.fbdtool.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.mdh.idt.fbdtool.structures.POU;
import se.mdh.idt.fbdtool.structures.Project;
import se.mdh.idt.fbdtool.utility.MetricSuite;
import se.mdh.idt.fbdtool.utility.TargetType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Target;
import java.nio.Buffer;

public class JsonWriter implements ComplexityWriter {

    BufferedWriter writer;

    public JsonWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(filePath));
    }
    public Writer getWriter()
    {
        return writer;
    }

    public static String MakeProjectResultReport(MetricSuite suite){
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(String.format(
                            """
                    {
                        "Target_Name": "%s",
                        "Project_Results": %s                  
                    }
                    """,
                            suite.getName(),
                            new ObjectMapper().writeValueAsString(suite.getProjectresults())
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
    public static String MakePOUResultReport(MetricSuite suite)
    {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(String.format(
                    """
            {
                "Target_Name": "%s",
                "POU_Results": %s                
            }
            """,
                    suite.getName(),
                    new ObjectMapper().writeValueAsString(suite.getPouResults())
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
    public static String writeToString(MetricSuite suite, TargetType type)
    {
        if (type == TargetType.PROJECT) {
            return MakeProjectResultReport(suite);
        }
        else {
            return MakePOUResultReport(suite);
        }
    }
    @Override
    public boolean writeToFile(MetricSuite suite, TargetType type, boolean shouldCloseFile) {
        boolean res = true;
        try {
            getWriter().write(writeToString(suite, type));
        }
        catch (IOException io_except)
        {
            io_except.printStackTrace();
            res = false;
        }
        finally
        {
            if(shouldCloseFile) {
                this.close();
            }
        }
        return res;
    }
}
