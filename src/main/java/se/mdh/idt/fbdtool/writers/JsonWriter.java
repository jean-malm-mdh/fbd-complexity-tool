package se.mdh.idt.fbdtool.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.mdh.idt.fbdtool.structures.POU;
import se.mdh.idt.fbdtool.structures.Project;
import se.mdh.idt.fbdtool.utility.MetricSuite;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
    private String MakeProjectResultReport(MetricSuite suite){
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
    private String MakePOUResultReport(MetricSuite suite)
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
    @Override
    public boolean write(MetricSuite suite, String type, boolean shouldCloseFile) {
        boolean succ = true;
        try {
            if (type.equalsIgnoreCase("project")) {
                getWriter().write(MakeProjectResultReport(suite));
            }
            else {
                getWriter().write(MakePOUResultReport(suite));
            }
        }
        catch (IOException io_except)
        {
            io_except.printStackTrace();
        }
        finally
        {
            if(shouldCloseFile) {
                this.close();
            }
        }
        return succ;
    }
}
