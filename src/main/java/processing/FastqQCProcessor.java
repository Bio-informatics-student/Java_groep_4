
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
     * Processes one FASTQ file and returns its QC results.
     */
    public QCResult process(
            Path file,
            double endPercentage,
            double requiredPassingReadsPercent,
            double maximumDuplicatePercent
    ) throws IOException {

        List<String[]> records = reader.read(file);
        List<String> sequences = new ArrayList<>(records.size());

        long startQ30Bases = 0;
        long endQ30Bases = 0;
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
                        sequence, quality, endPercentage
                );
            } catch (IllegalArgumentException e) {
                throw new IOException(
                        "Could not analyse read " + record[0]
                                + " in " + file + ": " + e.getMessage(),
                        e
                );
            }

            int basesPerEnd = result.getBasesPerEnd();

            // Count Q30+ bases at the beginning and end.
            startQ30Bases += Math.round(
                    result.getStart().getPercentageQ30()
                            * basesPerEnd / 100.0
            );

            endQ30Bases += Math.round(
                    result.getEnd().getPercentageQ30()
                            * basesPerEnd / 100.0
            );

            totalStartBases += basesPerEnd;
            totalEndBases += basesPerEnd;

            // A read passes if at least 90% of bases at
            // BOTH ends have Q30 or higher.
            boolean startPasses =
                    result.getStart().getPercentageQ30() >= 90.0;

            boolean endPasses =
                    result.getEnd().getPercentageQ30() >= 90.0;

            if (startPasses && endPasses) {
                passingReads++;
            }
        }

        double beginningQuality =
                100.0 * startQ30Bases / totalStartBases;

        double endQuality =
                100.0 * endQ30Bases / totalEndBases;

        // Exact sequence matches count as duplicates.
        double duplicatePercent =
                duplicateCalculator.calculateDuplicates(
                        sequences.toArray(new String[0])
                );

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
                endQuality,
                duplicatePercent,
                status
        );
    }
}
