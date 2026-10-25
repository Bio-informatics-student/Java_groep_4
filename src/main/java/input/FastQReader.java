
package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class FastQReader {

    /**
     * Reads a FASTQ file and returns its records.
     * Each record contains the header, sequence, separator and quality.
     *
     * @param file path to the FASTQ file
     * @return list of FASTQ records
     * @throws IOException if the file cannot be read or is invalid
     */
    public List<String[]> read(Path file) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader reader = openReader(file)) {
            while (true) {
                String header = reader.readLine();

                if (header == null) {
                    break;
                }

                String sequence = reader.readLine();
                String separator = reader.readLine();
                String quality = reader.readLine();

                if (sequence == null || separator == null || quality == null) {
                    throw new IOException(
                            "Incomplete FASTQ record in file: " + file
                    );
                }

                if (!header.startsWith("@")) {
                    throw new IOException(
                            "Invalid FASTQ header in file: " + file
                    );
                }

                if (!separator.startsWith("+")) {
                    throw new IOException(
                            "Invalid FASTQ separator in file: " + file
                    );
                }

                if (sequence.length() != quality.length()) {
                    throw new IOException(
                            "Sequence and quality lengths differ in file: " + file
                    );
                }

                records.add(new String[]{
                        header,
                        sequence,
                        separator,
                        quality
                });
            }
        }

        if (records.isEmpty()) {
            throw new IOException("FASTQ file is empty: " + file);
        }

        return records;
    }

    private BufferedReader openReader(Path file) throws IOException {
        String filename = file.getFileName()
                .toString()
                .toLowerCase(java.util.Locale.ROOT);

        if (filename.endsWith(".gz")) {
            return new BufferedReader(
                    new java.io.InputStreamReader(
                            new GZIPInputStream(Files.newInputStream(file)),
                            StandardCharsets.UTF_8
                    )
            );
        }

        return Files.newBufferedReader(file, StandardCharsets.UTF_8);
    }
}
