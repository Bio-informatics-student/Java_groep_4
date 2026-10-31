package processing;

import java.util.List;

/**
 * Analyses sequence lengths and filters if requested
 */

public class SequenceLengthAnalyzer {

    /**
     * Finds the shortest sequence length in a FASTQ file.
     *
     * @param sequences nucleotide sequences from one FASTQ file
     * @return length of the shortest sequence
     */
    public int getMinimumLength(List<String> sequences) {
        // Start with the largest possible integer value.
        // This ensures that the first sequence length will always replace it as the current minimum.
        int minimumLength = Integer.MAX_VALUE;

        for (String sequence : sequences) {
            if (sequence.length() < minimumLength) {
                minimumLength = sequence.length();
            }
        }

        return minimumLength;
    }

    /**
     * Finds the longest sequence length in a FASTQ file
     *
     * @param sequences nucleotide sequences from one FASTQ file
     * @return length of longest sequence
     */
    public int getMaximumLength(List<String> sequences) {
        int maximumLength = 0;

        for (String sequence : sequences) {
            if (sequence.length() > maximumLength) {
                maximumLength = sequence.length();
            }
        }

        return maximumLength;

    }

    /**
     * Calculates the percentage of reads below the minimum sequence length selected by the user.
     *
     * @param sequences nucleotide sequences from one FASTQ file
     * @param requiredMinimumLength minimum sequence length selected by the user
     * @return percentage of reads below the minimum length
     */
    public double calculatePercentageBelowMinimum(
            List<String> sequences,
            int requiredMinimumLength
    ) {
        int belowMinimum = 0;

        for (String sequence : sequences) {
            if (sequence.length() < requiredMinimumLength) {
                belowMinimum++;
            }
        }

        return ((double) belowMinimum / sequences.size()) * 100;
    }
}
