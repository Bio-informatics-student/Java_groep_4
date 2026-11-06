package output;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests whether OutputWriter correctly creates the QC output.
 */
public class OutputWriterTest {

    /**
     * Checks that an output file is created.
     */
    @Test
    void createsOutputFile() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                91.0,
                8.0,
                41.00,
                50,
                150,
                null,
                90.00,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                true,
                false,
                false,
                false,
                false,
                false,
                null
        );

        assertTrue(Files.exists(outputFile));
    }

    /**
     * Checks that the output contains the correct header.
     */
    @Test
    void writesCorrectHeader() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "FASTQ",
                96.0,
                91.0,
                8.0,
                41.00,
                50,
                150,
                90.00,
                85.00,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                true,
                false,
                false,
                false,
                false,
                false,
                100
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample\tType\tTotal Reads\tBeginning Quality\tEnding Quality\tDuplicates\tGC Percentage" +
                        "\tMinimum Length\tMaximum Length\t% Reads Below Minimum Length" +
                        "\tPassing Reads\tFinal Status"
        ));
    }

    /**
     * Checks that one sample is written correctly.
     */
    @Test
    void writesOneSampleCorrectly() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "FASTQ",
                96.0,
                91.0,
                8.0,
                41.00,
                50,
                150,
                null,
                10.0,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                true,
                false,
                false,
                false,
                false,
                false,
                null
        );
        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample01\tFASTQ\tN/A\t96.00%\t91.00%\t8.00%\t41.00%\t50\t150\t10.00%\tPASS"
        ));
    }

    /**
     * Checks that multiple samples are written correctly.
     */
    @Test
    void writesMultipleSamplesCorrectly() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result1 = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                91.0,
                8.0,
                41.00,
                50,
                150,
                10.00,
                90.00,
                "PASS"
        );

        QCResult result2 = new QCResult(
                "Sample02",
                "BAM",
                94.0,
                72.0,
                11.0,
                45.00,
                60,
                150,
                15.00,
                75.00,
                "FAIL"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result1, result2),
                outputFile,
                true,
                false,
                false,
                false,
                false,
                false,
                100
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains("Sample01"));
        assertTrue(output.contains("90.00%"));
        assertTrue(output.contains("PASS"));

        assertTrue(output.contains("Sample02"));
        assertTrue(output.contains("75.00%"));
        assertTrue(output.contains("FAIL"));
    }

    /**
     * Checks that a missing QC value is shown as N/A.
     */
    @Test
    void writesMissingValueAsNA() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                null,
                8.0,
                41.00,
                50,
                150,
                10.0,
                90.00,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                true,
                false,
                false,
                false,
                false,
                false,
                100
        );
        String output = Files.readString(outputFile);

        assertTrue(output.contains("N/A"));
    }

    /**
     * Checks that only the selected GC percentage is shown.
     */
    @Test
    void writesOnlySelectedGCStatistic() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "FASTQ",
                96.00,
                91.00,
                8.0,
                41.00,
                50,
                150,
                10.00,
                90.00,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                false,
                false,
                false,
                true,
                false,
                false,
                null
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains("GC Percentage"));
        assertTrue(output.contains("41.00%"));
    }

    /**
     * Checks that length statistics and the percentage of reads below the
     * required minimum length are shown.
     */
    @Test
    void writesMultipleGCStatistic() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "FASTQ",
                96.0,
                91.0,
                8.0,
                41.0,
                50,
                150,
                10.0,
                90.0,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                false,
                false,
                false,
                false,
                true,
                false,
                100
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains("Minimum Length"));
        assertTrue(output.contains("Maximum Length"));
        assertTrue(output.contains("% Reads Below Minimum Length"));

        assertTrue(output.contains("50"));
        assertTrue(output.contains("150"));
        assertTrue(output.contains("10.00%"));
    }

    /**
     * Checks that the passing reads percentage can be selected separately.
     */
    @Test
    void writesSelectedPassingReadsPercentage() throws Exception {
        OutputWriter writer = new OutputWriter();

        QCResult result = new QCResult(
                "Sample01",
                "FASTQ",
                96.0,
                91.0,
                8.0,
                41.0,
                50,
                150,
                10.0,
                90.0,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result),
                outputFile,
                false,
                false,
                false,
                false,
                false,
                true,
                null
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains("Passing Reads"));
        assertTrue(output.contains("90.00%"));
    }
}