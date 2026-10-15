
package processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the AnalyseReadQuality class.
 *
 * <p>This test class verifies the quality analysis of FASTQ reads
 * performed by SeqSpectre. The analysis focuses on a user-defined
 * percentage of bases at the beginning and end of each read.
 *
 * <p>The following functionality is tested:
 * <ul>
 *   <li>Calculation of the number of bases per read end.</li>
 *   <li>Correct rounding of calculated base counts.</li>
 *   <li>Calculation of mean and median Phred quality scores.</li>
 *   <li>Identification of the minimum Phred quality score.</li>
 *   <li>Calculation of percentages of bases meeting Q20 and Q30.</li>
 *   <li>Calculation of the mean sequencing error probability.</li>
 *   <li>Classification of read quality as GOOD, MODERATE or POOR.</li>
 *   <li>Validation of input values and FASTQ quality strings.</li>
 *   <li>Handling of overlapping read ends in short sequences.</li>
 * </ul>
 *
 * <p>All quality strings use Phred+33 encoding.
 *
 * @see AnalyseReadQuality
 * @see QualityResult
 */
class AnalyseReadQualityTest {

    // A sequence of ten nucleotides used as standard test data.
    private static final String TEST_SEQUENCE = "ACGTACGTAC";

    // Character 'I' has ASCII value 73, corresponding to Phred Q40.
    private static final String HIGH_QUALITY = "IIIIIIIIII";

    // Character 'D' represents Q35 and '&' represents Q5.
    // The first five positions therefore contain Q35, Q35, Q35, Q35, Q5.
    private static final String MIXED_QUALITY = "DDDD&DDDDD";

    // Allowed tolerance for comparing floating-point calculations.
    private static final double DELTA = 0.0001;

    // Analyzer instance used by the individual test methods.
    private AnalyseReadQuality analyzer;

    /**
     * Creates a new analyzer before each test.
     *
     * <p>This ensures that every test starts with a fresh instance
     * and does not depend on the execution of another test.
     */
    @BeforeEach
    void setUp() {
        analyzer = new AnalyseReadQuality();
    }

    /**
     * Tests whether the number of selected bases is rounded upwards.
     *
     * <p>For a read of 83 bases, 15% equals 12.45 bases.
     * Since a fraction of a base cannot be analysed, the expected
     * result is 13 bases.
     */
    @Test
    void testBaseCountRoundsUp() {
        int result = analyzer.calculateBaseCount(83, 15);

        assertEquals(13, result);
    }

    /**
     * Tests whether a whole-number result remains unchanged.
     *
     * <p>For a read of 100 bases, 10% equals exactly 10 bases.
     */
    @Test
    void testBaseCountWholeNumber() {
        int result = analyzer.calculateBaseCount(100, 10);

        assertEquals(10, result);
    }

    /**
     * Tests whether the selected percentage is applied independently
     * to both ends of the read.
     *
     * <p>A read of 10 bases with a percentage of 50% should result
     * in five bases being analysed at each end.
     *
     * <p>The first five bases have Q40 and the last five have Q35.
     */
    @Test
    void testPercentageAppliesToBothEnds() {
        String quality = "IIIIIDDDDD";

        QualityResult result = analyzer.analyse(TEST_SEQUENCE, quality, 50);

        // Verify the number of bases selected per end.
        assertEquals(5, result.getBasesPerEnd());

        // Verify that both ends are analysed separately.
        assertEquals(40.0, result.getStart().getMeanQuality(), DELTA);
        assertEquals(35.0, result.getEnd().getMeanQuality(), DELTA);
    }

    /**
     * Tests the calculation of the arithmetic mean Phred score.
     *
     * <p>The selected scores are 35, 35, 35, 35 and 5.
     *
     * <p>Mean = (35 + 35 + 35 + 35 + 5) / 5 = 29.
     *
     * <p>The low-quality base should influence the mean.
     */
    @Test
    void testMeanQuality() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, MIXED_QUALITY, 50);

        assertEquals(29.0, result.getStart().getMeanQuality(), DELTA);
    }

    /**
     * Tests the calculation of the median Phred score.
     *
     * <p>After sorting, the scores are 5, 35, 35, 35 and 35.
     * The middle score is 35.
     *
     * <p>This demonstrates that the median is less sensitive to
     * a single low-quality base than the arithmetic mean.
     */
    @Test
    void testMedianQuality() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, MIXED_QUALITY, 50);

        assertEquals(35.0, result.getStart().getMedianQuality(), DELTA);
    }

    /**
     * Tests whether the lowest Phred score is identified correctly.
     *
     * <p>The selected scores are 35, 35, 35, 35 and 5.
     * The expected minimum is therefore Q5.
     */
    @Test
    void testMinimumQuality() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, MIXED_QUALITY, 50);

        assertEquals(5, result.getStart().getMinimumQuality());
    }

    /**
     * Tests the percentage of bases with Phred scores of Q30 or higher.
     *
     * <p>Four out of five selected bases have Q35.
     * The remaining base has Q5.
     *
     * <p>Percentage Q30+ = (4 / 5) * 100 = 80%.
     */
    @Test
    void testPercentageQ30() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, MIXED_QUALITY, 50);

        assertEquals(80.0, result.getStart().getPercentageQ30(), DELTA);
    }

    /**
     * Tests the percentage of bases with Phred scores of Q20 or higher.
     *
     * <p>Four out of five selected bases meet the Q20 threshold.
     *
     * <p>Percentage Q20+ = (4 / 5) * 100 = 80%.
     */
    @Test
    void testPercentageQ20() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, MIXED_QUALITY, 50);

        assertEquals(80.0, result.getStart().getPercentageQ20(), DELTA);
    }

    /**
     * Tests the mean sequencing error probability.
     *
     * <p>A Phred quality score is converted to an estimated
     * sequencing error probability using:
     *
     * <p>P(error) = 10^(-Q / 10).
     *
     * <p>For Q40:
     * P(error) = 10^(-4) = 0.0001.
     *
     * <p>Since all selected bases have Q40, their mean error
     * probability should also be 0.0001.
     */
    @Test
    void testMeanErrorProbability() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, HIGH_QUALITY, 50);

        assertEquals(
                0.0001,
                result.getStart().getMeanErrorProbability(),
                0.00000001);
    }

    /**
     * Tests the GOOD quality classification.
     *
     * <p>According to the proposed SeqSpectre thresholds, an end
     * is classified as GOOD if at least 90% of its bases have Q30+.
     *
     * <p>All selected bases have Q40 and therefore meet this requirement.
     */
    @Test
    void testGoodClassification() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, HIGH_QUALITY, 50);

        assertEquals("GOOD", result.getStart().getClassification());
    }

    /**
     * Tests the MODERATE quality classification.
     *
     * <p>An end is classified as MODERATE when:
     * <ul>
     *   <li>Fewer than 90% of bases have Q30+.</li>
     *   <li>At least 90% of bases have Q20+.</li>
     * </ul>
     *
     * <p>The character ':' represents Q25 in Phred+33 encoding.
     * All selected bases therefore meet Q20 but not Q30.
     */
    @Test
    void testModerateClassification() {
        String quality = "::::::::::";

        QualityResult result = analyzer.analyse(TEST_SEQUENCE, quality, 50);

        assertEquals("MODERATE", result.getStart().getClassification());
    }

    /**
     * Tests the POOR quality classification.
     *
     * <p>An end is classified as POOR when fewer than 90%
     * of its bases have Q20+.
     *
     * <p>The character '+' represents Q10 in Phred+33 encoding.
     * None of the selected bases meet the Q20 threshold.
     */
    @Test
    void testPoorClassification() {
        String quality = "++++++++++";

        QualityResult result = analyzer.analyse(TEST_SEQUENCE, quality, 50);

        assertEquals("POOR", result.getStart().getClassification());
    }

    /**
     * Tests whether a percentage above the maximum is rejected.
     *
     * <p>SeqSpectre currently accepts percentages from 1% to 50%.
     * A value of 60% should cause an IllegalArgumentException.
     */
    @Test
    void testInvalidPercentage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.calculateBaseCount(100, 60));
    }

    /**
     * Tests whether a percentage of zero is rejected.
     *
     * <p>A percentage of 0% would select no bases for analysis
     * and is therefore not allowed.
     */
    @Test
    void testZeroPercentage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.calculateBaseCount(100, 0));
    }

    /**
     * Tests whether an empty FASTQ read is rejected.
     *
     * <p>A read without nucleotides cannot be used to calculate
     * meaningful quality statistics.
     */
    @Test
    void testEmptyRead() {
        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyse("", "", 10));
    }

    /**
     * Tests whether unequal sequence and quality lengths are rejected.
     *
     * <p>In a FASTQ record, each nucleotide must have exactly one
     * corresponding quality character.
     *
     * <p>The sequence contains four bases, while the quality
     * string contains only three characters.
     */
    @Test
    void testUnequalLengths() {
        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyse("ACGT", "III", 10));
    }

    /**
     * Tests whether invalid Phred+33 characters are rejected.
     *
     * <p>Valid Phred+33 quality characters have ASCII values
     * between 33 and 126.
     *
     * <p>A space character has ASCII value 32 and should
     * therefore cause an IllegalArgumentException.
     */
    @Test
    void testInvalidQualityCharacter() {
        assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.analyse("ACGT", "II I", 50));
    }

    /**
     * Tests whether general read information is stored correctly.
     *
     * <p>The analysed read contains 10 bases and the user
     * selects 20% of each end.
     *
     * <p>The expected number of selected bases per end is 2.
     */
    @Test
    void testReadLengthAndPercentageStored() {
        QualityResult result = analyzer.analyse(TEST_SEQUENCE, HIGH_QUALITY, 20);

        assertEquals(10, result.getReadLength());
        assertEquals(20.0, result.getPercentage(), DELTA);
        assertEquals(2, result.getBasesPerEnd());
    }

    /**
     * Tests the handling of overlapping read ends.
     *
     * <p>For a read of three bases, 50% equals 1.5 bases.
     * Rounding upwards results in two bases per end.
     *
     * <p>The middle base is included in both the start and end
     * analyses, since the two selected regions overlap.
     *
     * <p>Phred scores:
     * <ul>
     *   <li>Start: Q40 and Q35, resulting in a mean of 37.5.</li>
     *   <li>End: Q35 and Q10, resulting in a mean of 22.5.</li>
     * </ul>
     */
    @Test
    void testShortReadWithOverlappingEnds() {
        QualityResult result = analyzer.analyse("ACG", "ID+", 50);

        // Both selected regions contain two bases.
        assertEquals(2, result.getBasesPerEnd());

        // Verify the mean quality of the start region.
        assertEquals(37.5, result.getStart().getMeanQuality(), DELTA);

        // Verify the mean quality of the end region.
        assertEquals(22.5, result.getEnd().getMeanQuality(), DELTA);
    }
}
