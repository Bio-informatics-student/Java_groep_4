package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles and validates FASTQ read data.
 *
 * Each read is expected to contain exactly four elements in a fixed order:
 * index 0 = header
 * index 1 = nucleotide sequence
 * index 2 = separator line
 * index 3 = quality scores
 *
 * Valid reads are categorized into four separate output lists.
 * Invalid reads either cause an InvalidReadException or are skipped,
 * depending on the value of skipInvalidReads.
 */
public class DataHandler {

    /**
     * Validates and categorizes FASTQ reads.
     *
     * @param reads a list containing FASTQ reads.
     *              Each read is represented by a List<String> with exactly four elements.
     * @param skipInvalidReads determines how invalid reads are handled.
     *                         If false, processing stops with an InvalidReadException.
     *                         If true, invalid reads are skipped and processing continues.
     * @return a map containing four categorized lists:
     *         headers, nucleotides, separators and qualityScores.
     */
    public Map<String, List<String>> dataHandler(
            List<List<String>> reads,
            boolean skipInvalidReads
    ) {

        // Create separate output lists for each FASTQ component.
        // These lists are created before the loop so data from all valid reads is retained.
        List<String> headers = new ArrayList<>();
        List<String> nucleotides = new ArrayList<>();
        List<String> separators = new ArrayList<>();
        List<String> qualityScores = new ArrayList<>();

        // Process each read individually.
        for (List<String> read : reads) {

            try {

                /*
                 * Check whether all required FASTQ elements are present.
                 *
                 * These checks are performed before using read.get(index)
                 * to prevent an IndexOutOfBoundsException and to provide
                 * a specific error message.
                 */
                if (read.size() < 1) {
                    throw new InvalidReadException("No header found");
                }

                if (read.size() < 2) {
                    throw new InvalidReadException("No nucleotide sequence found");
                }

                if (read.size() < 3) {
                    throw new InvalidReadException("No separator found");
                }

                if (read.size() < 4) {
                    throw new InvalidReadException("No quality scores found");
                }

                /*
                 * A valid read should contain exactly four elements.
                 *
                 * Additional elements are rejected because the current
                 * DataHandler does not define a valid meaning for data
                 * beyond the four expected FASTQ components.
                 */
                if (read.size() > 4) {
                    throw new InvalidReadException("Too many elements in read");
                }

                // Retrieve the FASTQ header from the first position.
                String header = read.get(0);

                /*
                 * A valid FASTQ header must start with '@'.
                 */
                if (!header.startsWith("@")) {
                    throw new InvalidReadException("Invalid header");
                }

                // Retrieve the nucleotide sequence from the second position.
                String nucleotide = read.get(1);

                /*
                 * The current implementation only accepts A, C, G and T.
                 *
                 * The regular expression [ACGT]+ means that the sequence
                 * must contain one or more characters and that every
                 * character must be A, C, G or T.
                 */
                if (!nucleotide.matches("[ACGT]+")) {
                    throw new InvalidReadException("Invalid nucleotide sequence");
                }

                // Retrieve the separator line from the third position.
                String separator = read.get(2);

                /*
                 * The current implementation expects the separator
                 * to be exactly "+".
                 */
                if (!separator.equals("+")) {
                    throw new InvalidReadException("Invalid separator");
                }

                // Retrieve the quality scores from the fourth position.
                String qualityScore = read.get(3);

                /*
                 * Every nucleotide should have one corresponding
                 * quality-score character.
                 *
                 * Therefore, the nucleotide sequence and quality-score
                 * string must have equal lengths.
                 */
                if (qualityScore.length() != nucleotide.length()) {
                    throw new InvalidReadException(
                            "Quality score length does not match nucleotide sequence"
                    );
                }

                /*
                 * Only fully validated reads are added to the output lists.
                 * This prevents partially invalid reads from entering
                 * later processing steps.
                 */
                headers.add(header);
                nucleotides.add(nucleotide);
                separators.add(separator);
                qualityScores.add(qualityScore);

            } catch (InvalidReadException e) {

                /*
                 * If skipInvalidReads is enabled, discard the current
                 * invalid read and continue with the next read.
                 */
                if (skipInvalidReads) {
                    continue;
                }

                /*
                 * In strict mode, rethrow the exception so processing
                 * stops immediately when invalid input is encountered.
                 */
                throw e;
            }
        }

        /*
         * Combine the four categorized lists into one Map.
         *
         * A Map is used so each list can be retrieved using
         * a descriptive key.
         */
        Map<String, List<String>> categorizedReads = new HashMap<>();

        categorizedReads.put("headers", headers);
        categorizedReads.put("nucleotides", nucleotides);
        categorizedReads.put("separators", separators);
        categorizedReads.put("qualityScores", qualityScores);

        // Return all categorized read data to the caller.
        return categorizedReads;
    }
}