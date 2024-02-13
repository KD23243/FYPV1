package kd.finalyearproject.runtime.profiler.ui;

import kd.finalyearproject.runtime.profiler.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FrontEnd {
    public static void initializeHTMLExport() throws IOException {
        String currentDirectory = System.getProperty("user.dir") + "/ProfilerOutput.html";
        Constants.OUT.printf("   Output: " + "file://" + currentDirectory + "%n");
        String flameGraphData = readData("performance_report.json");
        String functionData = readData("OptimizeData.json");
        String htmlData = FlameGraph.getSiteData(flameGraphData, functionData);
        String fileName = "ProfilerOutput.html";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8);
            fileWriter.write(htmlData);
        } catch (IOException e) {
            Constants.OUT.printf(e + "%n");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // Handle the exception if closing the writer fails
                    Constants.OUT.printf(e + "%n");
                }
            }
        }
    }

    private static String readData(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename, StandardCharsets.UTF_8))) {
            StringBuilder contents = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contents.append(line);
            }
            return contents.toString();
        }
    }
}
