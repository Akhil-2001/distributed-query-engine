package org.worker.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.worker.model.Row;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser reads CSV files containing three columns: ID, year, score.
 * Each CSV row is mapped to a `Row` instance.
 */
public class DataParser {
    private static final String STORAGE_DIR = "../../student_scores";
    private final Path filePath;

    public DataParser(String dir) {
        // Build the path in a platform-independent way
        this.filePath = Paths.get(STORAGE_DIR).resolve(dir);
    }

    /**
     * Parse the CSV and return a list of Row rows.
     * The CSV is expected to have no header and exactly 3 columns per row: ID, year, score.
     */
    public List<Row> parse() throws IOException {
        List<Row> result = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withTrim().withIgnoreSurroundingSpaces());
            for (CSVRecord rec : parser) {
                if (rec.size() < 3) {
                    // skip or throw; for robustness we skip malformed rows
                    continue;
                }
                String id = rec.get(0);
                String year = rec.get(1);
                String scoreStr = rec.get(2);

                int score;
                try {
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    // skip invalid numeric rows
                    System.out.println("ERROR :: while parsing score: " + scoreStr);
                    continue;
                }
                result.add(new Row(id, year, score));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
