package output;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests whether QCResult correctly stores the QC results for a sequencing sample.
 */
public class QCResultTest {

    /**
     * Checks that the sample name and file type are stored correctly.
     */
    @Test
    void storesSampleInformation() {
        QCResult result = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                91.0,
                8.0,
                "PASS"
        );

        assertEquals("Sample01", result.getSample());
        assertEquals("BAM", result.getFileType());
    }

    /**
     * Checks that the quality and duplicate percentages are stored correctly.
     */
    @Test
    void storesQCResults() {
        QCResult result = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                91.0,
                8.0,
                "PASS"
        );

        assertEquals(96.0, result.getBeginningQuality());
        assertEquals(91.0, result.getEndQuality());
        assertEquals(8.0, result.getDuplicates());
    }

    /**
     * Checks that the final PASS or FAIL status is stored correctly.
     */
    @Test
    void storesStatus() {
        QCResult result = new QCResult(
                "Sample02",
                "BAM",
                94.0,
                72.0,
                11.0,
                "FAIL"
        );

        assertEquals("FAIL", result.getStatus());
    }

    /**
     * Checks that a QC result can contain a missing value.
     */
    @Test
    void allowsMissingQCResult() {
        QCResult result = new QCResult(
                "Sample01",
                "BAM",
                96.0,
                null,
                8.0,
                "PASS"
        );

        assertNull(result.getEndQuality());
    }
}