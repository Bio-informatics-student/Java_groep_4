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
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(List.of(result), outputFile);

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
                "BAM",
                96.0,
                91.0,
                8.0,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(List.of(result), outputFile);

        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample\tType\tBegin kwaliteit\tEind kwaliteit\tDuplicaten\tEindstatus"
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
                "BAM",
                96.0,
                91.0,
                8.0,
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(List.of(result), outputFile);

        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample01\tBAM\t96.0%\t91.0%\t8.0%\tPASS"
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
                "PASS"
        );

        QCResult result2 = new QCResult(
                "Sample02",
                "BAM",
                94.0,
                72.0,
                11.0,
                "FAIL"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(
                List.of(result1, result2),
                outputFile
        );

        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample01\tBAM\t96.0%\t91.0%\t8.0%\tPASS"
        ));

        assertTrue(output.contains(
                "Sample02\tBAM\t94.0%\t72.0%\t11.0%\tFAIL"
        ));
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
                "PASS"
        );

        Path outputFile = Files.createTempFile("QCOutput", ".txt");

        writer.writeToFile(List.of(result), outputFile);

        String output = Files.readString(outputFile);

        assertTrue(output.contains(
                "Sample01\tBAM\t96.0%\tN/A\t8.0%\tPASS"
        ));
    }
}