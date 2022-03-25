package com.andreydymko;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class CsvWriter {

    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final char DELIMITER = ';';
    private static final String[] FILE_HEADERS = {"threadId", "allocations", "mbAllocated", "nanosec"};

    public void write(String dir, String filename, Collection<Measurement> measurements) {
        String delim = "";
        if (!dir.isEmpty()) {
            delim = "\\";
            boolean isCreated = new File(dir).mkdirs();
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dir + delim + filename), StandardCharsets.UTF_8))) {
            for (String fileHeader : FILE_HEADERS) {
                writer.write(fileHeader);
                writer.write(DELIMITER);
            }
            writer.write(NEW_LINE_SEPARATOR);

            for (Measurement measurement : measurements) {
                writer.write(Integer.toString(measurement.getThreadId()));
                writer.write(DELIMITER);
                writer.write(Integer.toString(measurement.getAllocations()));
                writer.write(DELIMITER);
                writer.write(String.format("%f", measurement.getAllocatedTotal()));
                writer.write(DELIMITER);
                writer.write(Long.toString(measurement.getMillis()));
                writer.write(DELIMITER);
                writer.write(NEW_LINE_SEPARATOR);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
