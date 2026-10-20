
package processing;

import java.util.Arrays;

/**
 * Analyses the sequencing quality at both ends of a FASTQ read.
 *
 * <p>This class is part of the SeqSpectre quality control tool.
 * It examines a user-defined percentage of bases at the beginning
 * and end of a sequencing read.
 *
 * <p>The following statistics are calculated for each read end:
 * <ul>
 *   <li>Mean Phred quality score.</li>
 *   <li>Median Phred quality score.</li>
 *   <li>Minimum Phred quality score.</li>
 *   <li>Percentage of bases with Q20 or higher.</li>
 *   <li>Percentage of bases with Q30 or higher.</li>
 *   <li>Mean sequencing error probability.</li>
 *   <li>Quality classification (GOOD, MODERATE or POOR).</li>
 * </ul>
 *
 * <p>Quality scores are extracted from FASTQ quality strings
 * using Phred+33 encoding.
 *
 * <p>The analysis returns a QualityResult object containing
 * the results for both ends of the read.
 *
 * @see QualityResult
 */
public class AnalyseReadQuality {

    /**
     * Calculates the number of bases to analyse at each read end.
     *
     * <p>The requested percentage is applied separately to the
     * beginning and end of the read.
     *
     * <p>Since individual bases cannot be divided, the calculated
     * number is always rounded upwards using Math.ceil().
     *
     * <p>For example, 15% of a read containing 83 bases equals
     * 12.45 bases. The method therefore returns 13.
     *
     * @param readLength total number of bases in the read
     * @param percentage percentage of each end to analyse
     * @return number of bases to analyse at each read end
     * @throws IllegalArgumentException if the read length is not
     *     positive or the percentage is outside the allowed range
     */
    public int calculateBaseCount(int readLength, double percentage) {

        // A read must contain at least one nucleotide.
        if (readLength <= 0) {
            throw new IllegalArgumentException(
                    "Read length must be greater than zero.");
        }

        // Check whether the user-defined percentage is valid.
        validatePercentage(percentage);

        // Round upwards to ensure that a fractional base is included.
        return (int) Math.ceil(readLength * percentage / 100.0);
    }

    /**
     * Analyses the quality scores at both ends of a FASTQ read.
     *
     * <p>The sequence and quality string must have identical lengths,
     * because every nucleotide must have one corresponding quality score.
     *
     * <p>The analysis consists of the following steps:
     * <ol>
     *   <li>Validate the input sequence and quality string.</li>
     *   <li>Calculate the number of bases to analyse per end.</li>
     *   <li>Convert FASTQ quality characters to Phred scores.</li>
     *   <li>Select the quality scores at both ends of the read.</li>
     *   <li>Calculate quality statistics for each selected region.</li>
     *   <li>Combine the statistics into a QualityResult object.</li>
     * </ol>
     *
     * <p>Short reads may contain overlapping selected regions due
     * to upward rounding. These regions are analysed independently.
     *
     * @param sequence nucleotide sequence from the FASTQ record
     * @param qualityString corresponding Phred+33 quality string
     * @param percentage percentage of each end to analyse
     * @return QualityResult containing statistics for both read ends
     * @throws IllegalArgumentException if the input is null, empty,
     *     has unequal lengths or contains invalid quality characters
     */
    public QualityResult analyse(
            String sequence,
            String qualityString,
            double percentage) {

        // Both the sequence and its quality string must be present.
        if (sequence == null || qualityString == null) {
            throw new IllegalArgumentException(
                    "Sequence and quality string cannot be null.");
        }

        // An empty sequence cannot produce meaningful quality statistics.
        if (sequence.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sequence cannot be empty.");
        }

        // Every nucleotide must have exactly one quality character.
        if (sequence.length() != qualityString.length()) {
            throw new IllegalArgumentException(
                    "Sequence and quality string must have equal lengths.");
        }

        int readLength = sequence.length();

        // Determine how many bases belong to each selected read end.
        int basesPerEnd = calculateBaseCount(readLength, percentage);

        // Store the decoded Phred score of each nucleotide.
        int[] scores = new int[readLength];

        for (int i = 0; i < readLength; i++) {

            // Retrieve the ASCII value of the current quality character.
            int asciiValue = qualityString.charAt(i);

            // Phred+33 quality characters must be printable ASCII
            // characters between 33 and 126.
            if (asciiValue < 33 || asciiValue > 126) {
                throw new IllegalArgumentException(
                        "Invalid Phred+33 character at position " + i);
            }

            // Convert the encoded character to its numerical Phred score.
            // For example, 'I' has ASCII value 73, corresponding to Q40.
            scores[i] = asciiValue - 33;
        }

        // Select the quality scores belonging to the beginning
        // of the sequencing read.
        int[] startScores = Arrays.copyOfRange(
                scores,
                0,
                basesPerEnd);

        // Select the quality scores belonging to the end
        // of the sequencing read.
        int[] endScores = Arrays.copyOfRange(
                scores,
                readLength - basesPerEnd,
                readLength);

        // Calculate all quality statistics for the selected start region.
        QualityResult.EndQuality start =
                calculateStatistics(startScores);

        // Calculate all quality statistics for the selected end region.
        QualityResult.EndQuality end =
                calculateStatistics(endScores);

        // Combine both regions and the original read information
        // into one object that can be used by the reporting system.
        return new QualityResult(
                percentage,
                readLength,
                basesPerEnd,
                start,
                end);
    }

    /**
     * Validates the percentage provided by the user.
     *
     * <p>SeqSpectre currently accepts percentages from 1% to 50%.
     * This range prevents the user from intentionally selecting
     * more than half of the read at either end.
     *
     * <p>Non-finite values, including NaN and infinity,
     * are also rejected.
     *
     * @param percentage percentage to validate
     * @throws IllegalArgumentException if the percentage is invalid
     */
    private void validatePercentage(double percentage) {

        // Reject percentages outside the allowed range and non-finite values.
        if (!Double.isFinite(percentage)
                || percentage < 1
                || percentage > 50) {

            throw new IllegalArgumentException(
                    "Percentage must be between 1 and 50.");
        }
    }

    /**
     * Calculates sequencing quality statistics for one read end.
     *
     * <p>The provided scores represent either the beginning or
     * the end of a sequencing read.
     *
     * <p>The mean and median describe the distribution of Phred
     * scores, while the minimum identifies the lowest-quality base.
     *
     * <p>Q20 and Q30 percentages indicate the proportion of bases
     * meeting commonly used sequencing quality thresholds.
     *
     * <p>The mean error probability is calculated by first
     * converting each Phred score to an error probability:
     *
     * <p>P(error) = 10^(-Q / 10).
     *
     * <p>These probabilities are averaged separately because
     * Phred quality scores use a logarithmic scale.
     *
     * @param scores Phred quality scores of one selected read end
     * @return EndQuality containing the calculated statistics
     */
    private QualityResult.EndQuality calculateStatistics(int[] scores) {

        int count = scores.length;

        // Variables used to calculate mean quality and error probability.
        double sum = 0;
        double sumErrorProbability = 0;

        // Variables used to identify the minimum and count good-quality bases.
        int minimum = Integer.MAX_VALUE;
        int q20Count = 0;
        int q30Count = 0;

        // Process every quality score in the selected region.
        for (int score : scores) {

            // Add the current score to the total for the arithmetic mean.
            sum += score;

            // Keep track of the lowest Phred score.
            minimum = Math.min(minimum, score);

            // Count bases with a Phred score of at least Q20.
            if (score >= 20) {
                q20Count++;
            }

            // Count bases with a Phred score of at least Q30.
            if (score >= 30) {
                q30Count++;
            }

            // Convert the Phred score to its estimated error probability.
            // Q20 corresponds to 0.01 and Q30 corresponds to 0.001.
            sumErrorProbability += Math.pow(10.0, -score / 10.0);
        }

        // Calculate the arithmetic mean of all selected Phred scores.
        double mean = sum / count;

        // Calculate the percentage of bases meeting each threshold.
        double percentageQ20 = 100.0 * q20Count / count;
        double percentageQ30 = 100.0 * q30Count / count;

        // Calculate the average estimated sequencing error probability.
        double meanErrorProbability = sumErrorProbability / count;

        // Copy the original scores before sorting to avoid modifying
        // the input array used by other calculations.
        int[] sorted = Arrays.copyOf(scores, count);
        Arrays.sort(sorted);

        double median;

        if (count % 2 == 0) {

            // For an even number of bases, the median is the mean
            // of the two middle Phred scores.
            median = ((double) sorted[count / 2 - 1]
                    + sorted[count / 2]) / 2.0;

        } else {

            // For an odd number of bases, select the middle score.
            median = sorted[count / 2];
        }

        // Assign a quality category using SeqSpectre's thresholds.
        String classification = classify(
                percentageQ20,
                percentageQ30);

        // Store all calculated statistics in a single result object.
        return new QualityResult.EndQuality(
                mean,
                median,
                minimum,
                percentageQ20,
                percentageQ30,
                meanErrorProbability,
                classification);
    }

    /**
     * Determines the quality classification of a read end.
     *
     * <p>The following SeqSpectre-specific thresholds are used:
     *
     * <ul>
     *   <li>GOOD: at least 90% of bases have Q30 or higher.</li>
     *   <li>MODERATE: fewer than 90% meet Q30, but at least
     *       90% meet Q20.</li>
     *   <li>POOR: fewer than 90% of bases have Q20 or higher.</li>
     * </ul>
     *
     * <p>These thresholds are design choices for SeqSpectre
     * and are not the official FastQC classification thresholds.
     *
     * @param percentageQ20 percentage of bases with Q20 or higher
     * @param percentageQ30 percentage of bases with Q30 or higher
     * @return quality classification as GOOD, MODERATE or POOR
     */
    private String classify(
            double percentageQ20,
            double percentageQ30) {

        // High-quality read end: at least 90% of bases meet Q30.
        if (percentageQ30 >= 90.0) {
            return "GOOD";
        }

        // Moderate-quality read end: at least 90% of bases meet Q20.
        if (percentageQ20 >= 90.0) {
            return "MODERATE";
        }

        // Low-quality read end: insufficient bases meet Q20.
        return "POOR";
    }
}
