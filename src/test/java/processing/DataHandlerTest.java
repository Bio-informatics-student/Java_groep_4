package processing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the DataHandler class.
 *
 * These tests verify whether FASTQ reads are correctly categorized
 * into headers, nucleotide sequences, separator lines and quality scores.
 *
 * The tests also verify the validation of incomplete or invalid reads
 * and the behaviour of DataHandler when invalid reads should be skipped.
 */
class DataHandlerTest {

    /**
     * Tests the normal situation with two valid reads.
     *
     * The DataHandler should categorize each part of the reads
     * into the correct output list.
     */
    @Test
    void dataHandler() {

        // Arrange: create the DataHandler and valid test data.
        DataHandler handler = new DataHandler();

        List<String> read1 = List.of(
                "@read1",
                "ACTG",
                "+",
                "IIII"
        );

        List<String> read2 = List.of(
                "@read2",
                "GGTA",
                "+",
                "HHHH"
        );

        List<List<String>> reads = List.of(
                read1,
                read2
        );

        // Act: process the reads in strict mode.
        Map<String, List<String>> result = handler.dataHandler(reads, false);

        // Assert: verify that all headers are categorized correctly.
        assertEquals(
                List.of("@read1", "@read2"),
                result.get("headers")
        );

        // Verify that all nucleotide sequences are categorized correctly.
        assertEquals(
                List.of("ACTG", "GGTA"),
                result.get("nucleotides")
        );

        // Verify that all separator lines are categorized correctly.
        assertEquals(
                List.of("+", "+"),
                result.get("separators")
        );

        // Verify that all quality score strings are categorized correctly.
        assertEquals(
                List.of("IIII", "HHHH"),
                result.get("qualityScores")
        );
    }

    /**
     * Tests whether DataHandler can handle an empty input list.
     *
     * Empty input is valid and should result in four empty output lists,
     * rather than an exception or null values.
     */
    @Test
    void dataHandlerWithEmptyInput() {

        // Arrange.
        DataHandler handler = new DataHandler();

        List<List<String>> reads = List.of();

        // Act.
        Map<String, List<String>> result = handler.dataHandler(reads, false);

        // Assert: every output category should exist and be empty.
        assertEquals(List.of(), result.get("headers"));
        assertEquals(List.of(), result.get("nucleotides"));
        assertEquals(List.of(), result.get("separators"));
        assertEquals(List.of(), result.get("qualityScores"));
    }

    /**
     * Tests whether a read without a header is rejected.
     *
     * A FASTQ read requires a header as its first element,
     * therefore an InvalidReadException is expected.
     */
    @Test
    void dataHandlerThrowsExceptionWhenHeaderIsMissing() {

        // Arrange: create a read without any elements.
        DataHandler handler = new DataHandler();

        List<String> read = List.of();
        List<List<String>> reads = List.of(read);

        // Act and Assert: processing the read should throw an exception.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        // Verify that the exception clearly describes the problem.
        assertEquals(
                "No header found",
                exception.getMessage()
        );
    }

    /**
     * Tests whether a read without a nucleotide sequence is rejected.
     */
    @Test
    void dataHandlerThrowsExceptionWhenNucleotideSequenceIsMissing() {

        // Arrange: the read contains a header but no sequence.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "No nucleotide sequence found",
                exception.getMessage()
        );
    }

    /**
     * Tests whether a read without a separator line is rejected.
     */
    @Test
    void dataHandlerThrowsExceptionWhenSeparatorIsMissing() {

        // Arrange: the header and sequence exist, but the separator is missing.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGT"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "No separator found",
                exception.getMessage()
        );
    }

    /**
     * Tests whether a read without quality scores is rejected.
     *
     * Quality scores are required for later quality-control analyses,
     * therefore missing quality data makes the read invalid in strict mode.
     */
    @Test
    void dataHandlerThrowsExceptionWhenQualityScoresAreMissing() {

        // Arrange: the read contains no fourth element with quality scores.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGT",
                "+"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "No quality scores found",
                exception.getMessage()
        );
    }

    /**
     * Tests whether an invalid FASTQ header is rejected.
     *
     * A header is considered valid when it starts with the '@' character.
     */
    @Test
    void dataHandlerThrowsExceptionWhenHeaderIsInvalid() {

        // Arrange: the first element does not start with '@'.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "read1",
                "ACTG",
                "+",
                "IIII"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "Invalid header",
                exception.getMessage()
        );
    }

    /**
     * Tests whether an invalid nucleotide sequence is rejected.
     *
     * The current implementation accepts nucleotide sequences
     * containing only A, C, G and T.
     */
    @Test
    void dataHandlerThrowsExceptionWhenNucleotideSequenceIsInvalid() {

        // Arrange: X is not accepted as a valid nucleotide.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGTX",
                "+",
                "IIIII"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "Invalid nucleotide sequence",
                exception.getMessage()
        );
    }

    /**
     * Tests whether an invalid separator line is rejected.
     *
     * The current implementation expects the separator to be exactly "+".
     */
    @Test
    void dataHandlerThrowsExceptionWhenSeparatorIsInvalid() {

        // Arrange: '-' is used instead of the expected '+' separator.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGT",
                "-",
                "IIII"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "Invalid separator",
                exception.getMessage()
        );
    }

    /**
     * Tests whether the number of quality-score characters matches
     * the number of nucleotides in the sequence.
     *
     * Every nucleotide should have one corresponding quality score.
     */
    @Test
    void dataHandlerThrowsExceptionWhenQualityScoreLengthDoesNotMatchSequence() {

        // Arrange: the sequence has length 4 while the quality string has length 3.
        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGT",
                "+",
                "III"
        );

        List<List<String>> reads = List.of(read);

        // Act and Assert.
        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "Quality score length does not match nucleotide sequence",
                exception.getMessage()
        );
    }

    /**
     * Tests the optional skip-invalid-reads behaviour.
     *
     * When skipInvalidReads is true, an invalid read should not terminate
     * processing. The invalid read should be ignored while valid reads
     * are still returned.
     */
    @Test
    void dataHandlerSkipsInvalidReadsWhenSkipInvalidReadsIsTrue() {

        // Arrange.
        DataHandler handler = new DataHandler();

        List<String> validRead = List.of(
                "@read1",
                "ACGT",
                "+",
                "IIII"
        );

        // This read is invalid because the header does not start with '@'.
        List<String> invalidRead = List.of(
                "read2",
                "GGTA",
                "+",
                "HHHH"
        );

        List<List<String>> reads = List.of(
                validRead,
                invalidRead
        );

        // Act: enable skipping of invalid reads.
        Map<String, List<String>> result = handler.dataHandler(reads, true);

        // Assert: only data from the valid read should remain.
        assertEquals(
                List.of("@read1"),
                result.get("headers")
        );

        assertEquals(
                List.of("ACGT"),
                result.get("nucleotides")
        );

        assertEquals(
                List.of("+"),
                result.get("separators")
        );

        assertEquals(
                List.of("IIII"),
                result.get("qualityScores")
        );
    }

    @Test
    void dataHandlerThrowsExceptionWhenReadContainsTooManyElements() {

        DataHandler handler = new DataHandler();

        List<String> read = List.of(
                "@read1",
                "ACGT",
                "+",
                "IIII",
                "EXTRA"
        );

        List<List<String>> reads = List.of(read);

        InvalidReadException exception = assertThrows(
                InvalidReadException.class,
                () -> handler.dataHandler(reads, false)
        );

        assertEquals(
                "Too many elements in read",
                exception.getMessage()
        );
    }
}