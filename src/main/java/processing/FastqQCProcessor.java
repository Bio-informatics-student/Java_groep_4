
package processing;

import input.FastQReader;
import output.QCResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects FASTQ reading, quality analysis and duplicate calculation.
 */
public class FastqQCProcessor {

    private final FastQReader reader;
    private final AnalyseReadQuality qualityAnalyzer;
    private final DuplicateCalculator duplicateCalculator;

    public FastqQCProcessor() {
        this.reader = new FastQReader();
        this.qualityAnalyzer = new AnalyseReadQuality();
        this.duplicateCalculator = new DuplicateCalculator();
    }

    /**
     * Processes one FASTQ file using Q30 as the default minimum score.
     */
    public QCResult process(
            Path file,
            double endPercentage,
            double requiredPassingReadsPercent,
            double maximumDuplicatePercent
    ) throws IOException {
        return process(
                file,
                endPercentage,
                requiredPassingReadsPercent,
                maximumDuplicatePercent,
                30
        );
    }

    /**
     * Processes one FASTQ file using the specified minimum Phred score.
     *
     * Beginning Quality and Ending Quality represent the percentages
     * of bases in the analysed read-end regions that meet the threshold.
     *
     * A read passes when at least 90% of the bases at both ends meet
     * the selected minimum Phred score.
     */
    public QCResult process(
            Path file,
            double endPercentage,
            double requiredPassingReadsPercent,
            double maximumDuplicatePercent,
            int minQualityScore
    ) throws IOException {

        // Validate the processing settings.
        if (!Double.isFinite(endPercentage)
                || endPercentage < 1 || endPercentage > 50) {
            throw new IllegalArgumentException(
                    "Read-end percentage must be between 1 and 50."
            );
        }

        if (!Double.isFinite(requiredPassingReadsPercent)
                || requiredPassingReadsPercent < 0
                || requiredPassingReadsPercent > 100) {
            throw new IllegalArgumentException(
                    "Required passing-read percentage must be between 0 and 100."
            );
        }

        if (!Double.isFinite(maximumDuplicatePercent)
                || maximumDuplicatePercent < 0
                || maximumDuplicatePercent > 100) {
            throw new IllegalArgumentException(
                    "Maximum duplicate percentage must be between 0 and 100."
            );
        }

        if (minQualityScore < 0 || minQualityScore > 93) {
            throw new IllegalArgumentException(
                    "Minimum Phred score must be between 0 and 93."
            );
        }

        List<String[]> records = reader.read(file);

        if (records.isEmpty()) {
            throw new IOException(
                    "No reads were found in FASTQ file: " + file
            );
        }

        List<String> sequences = new ArrayList<>(records.size());

        long startPassingBases = 0;
        long endPassingBases = 0;
        long totalStartBases = 0;
        long totalEndBases = 0;

        int passingReads = 0;

        for (String[] record : records) {
            String sequence = record[1];
            String quality = record[3];

            sequences.add(sequence);

            QualityResult result;

            try {
                result = qualityAnalyzer.analyse(
                        sequence,
                        quality,
                        endPercentage
                );
            } catch (IllegalArgumentException e) {
                throw new IOException(
                        "Could not analyse read " + record[0]
                                + " in " + file + ": " + e.getMessage(),
                        e
                );
            }

            int basesPerEnd = result.getBasesPerEnd();

            // Calculate the percentage of bases meeting the selected
            // Phred threshold at the beginning and end of each read.
            double startQualityPercent = calculatePercentageAtLeast(
                    quality,
                    0,
                    basesPerEnd,
                    minQualityScore
            );

            double endQualityPercent = calculatePercentageAtLeast(
                    quality,
                    quality.length() - basesPerEnd,
                    quality.length(),
                    minQualityScore
            );

            // Convert the percentages back to base counts for totals.
            startPassingBases += Math.round(
                    startQualityPercent * basesPerEnd / 100.0
            );

            endPassingBases += Math.round(
                    endQualityPercent * basesPerEnd / 100.0
            );

            totalStartBases += basesPerEnd;
            totalEndBases += basesPerEnd;

            // A read passes only when at least 90% of the bases
            // at both ends meet the selected minimum Phred score.
            boolean startPasses = startQualityPercent >= 90.0;
            boolean endPasses = endQualityPercent >= 90.0;

            if (startPasses && endPasses) {
                passingReads++;
            }
        }

        // Calculate the overall percentages of bases passing the threshold.
        double beginningQuality =
                100.0 * startPassingBases / totalStartBases;

        double endingQuality =
                100.0 * endPassingBases / totalEndBases;

        // Exact sequence matches count as duplicates.
        double duplicatePercent = duplicateCalculator.calculateDuplicates(
                sequences.toArray(new String[0])
        );

        // Calculate the percentage of reads passing at both ends.
        double passingReadsPercent =
                100.0 * passingReads / records.size();

        boolean qualityPasses =
                passingReadsPercent >= requiredPassingReadsPercent;

        boolean duplicatesPass =
                duplicatePercent <= maximumDuplicatePercent;

        String status = qualityPasses && duplicatesPass
                ? "PASS"
                : "FAIL";

        return new QCResult(
                file.getFileName().toString(),
                "FASTQ",
                beginningQuality,
                endingQuality,
                duplicatePercent,
                status
        );
    }

    /**
     * Calculates the percentage of bases in a quality-string region
     * that meet the specified minimum Phred score.
     *
     * FASTQ quality characters use Phred+33 encoding.
     */
    private double calculatePercentageAtLeast(
            String quality,
            int startIndex,
            int endIndex,
            int minQualityScore
    ) {
        int passingBases = 0;
        int totalBases = endIndex - startIndex;

        for (int i = startIndex; i < endIndex; i++) {
            int phredScore = quality.charAt(i) - 33;

            if (phredScore >= minQualityScore) {
                passingBases++;
            }
        }

        return 100.0 * passingBases / totalBases;
    }
}
