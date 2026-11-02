package output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * OutputWriter writes QC results to the terminal
 * and a tsv-file
 */
public class OutputWriter {

    /**
     * Prints the QC results to the terminal.
     *
     * @param results the QC results to print
     * @param showAll whether all QC statistics should be shown
     * @param showQuality whether beginning and ending quality should be shown
     * @param showDuplicates whether the duplicate percentage should be shown
     * @param showGC whether the GC percentage should be shown
     * @param showLength whether the read length statistics should be shown
     * @param showPassingReads whether the percentage of passing reads should be shown
     * @param requiredMinimumLength minimum required read length, or null if no minimum is given
     */
    public void printToTerminal(
            List<QCResult> results,
            boolean showAll,
            boolean showQuality,
            boolean showDuplicates,
            boolean showGC,
            boolean showLength,
            boolean showPassingReads,
            Integer requiredMinimumLength

    ) {
        System.out.println(
                formatHeader(
                        showAll,
                        showQuality,
                        showDuplicates,
                        showGC,
                        showLength,
                        showPassingReads,
                        requiredMinimumLength
                )
        );

        for (QCResult result : results) {
            System.out.println(
                    formatResult(
                            result,
                            showAll,
                            showQuality,
                            showDuplicates,
                            showGC,
                            showLength,
                            showPassingReads,
                            requiredMinimumLength
                    )
            );
        }
    }

    /**
     * Writes the QC results to a text file.
     *
     * @param results the QC results to write
     * @param outputFile the file where the results are written
     * @param showAll whether all QC statistics should be shown
     * @param showQuality whether beginning and ending quality should be shown
     * @param showDuplicates whether the duplicate percentage should be shown
     * @param showGC whether the GC percentage should be shown
     * @param showLength whether the read length statistics should be shown
     * @param showPassingReads whether the percentage of passing reads should be shown
     * @param requiredMinimumLength minimum required read length, or null if no minimum is given
     * @throws IOException when the file cannot be written
     */
    public void writeToFile(
            List<QCResult> results,
            Path outputFile,
            boolean showAll,
            boolean showQuality,
            boolean showDuplicates,
            boolean showGC,
            boolean showLength,
            boolean showPassingReads,
            Integer requiredMinimumLength
    ) throws IOException {

        // Build one TSV table with a header and one row for each sample.
        StringBuilder output = new StringBuilder();

        output.append(
                formatHeader(
                        showAll,
                        showQuality,
                        showDuplicates,
                        showGC,
                        showLength,
                        showPassingReads,
                        requiredMinimumLength
                )
        );
        output.append("\n");

        for (QCResult result : results) {
            output.append(
                    formatResult(
                            result,
                            showAll,
                            showQuality,
                            showDuplicates,
                            showGC,
                            showLength,
                            showPassingReads,
                            requiredMinimumLength
                    )
            );
            output.append("\n");
        }

        Files.writeString(outputFile, output.toString());
    }

    /**
     * Creates a header only containing the selected QC statistics.
     *
     * @param showAll whether all QC statistics should be shown
     * @param showQuality whether beginning and ending quality should be shown
     * @param showDuplicates whether the duplicate percentage should be shown
     * @param showGC whether the GC percentage should be shown
     * @param showLength whether the read length statistics should be shown
     * @param showPassingReads whether the percentage of passing reads should be shown
     * @param requiredMinimumLength minimum required read length, or null if no minimum is given
     * @return the formatted header
     */
    private String formatHeader(
            boolean showAll,
            boolean showQuality,
            boolean showDuplicates,
            boolean showGC,
            boolean showLength,
            boolean showPassingReads,
            Integer requiredMinimumLength
    ) {
        StringBuilder header = new StringBuilder();

        header.append("Sample\tType");

        if (showAll || showQuality) {
            header.append("\tBeginning Quality\tEnding Quality");
        }

        if (showAll || showDuplicates) {
            header.append("\tDuplicates");
        }

        if (showAll || showGC) {
            header.append("\tGC Percentage");
        }

        if (showAll || showLength) {
            header.append("\tMinimum Length\tMaximum Length");

            if (requiredMinimumLength != null) {
                header.append("\t% Reads Below Minimum Length");
            }
        }

        if (showAll || showPassingReads) {
            header.append("\tPassing Reads");
        }

        header.append("\tFinal Status");

        return header.toString();
    }

    /**
     * Converts one QC result into one line of output.
     *
     * @param result the QC result
     * @param showAll whether all QC statistics should be shown
     * @param showQuality whether beginning and ending quality should be shown
     * @param showDuplicates whether the duplicate percentage should be shown
     * @param showGC whether the GC percentage should be shown
     * @param showLength whether the read length statistics should be shown
     * @param showPassingReads whether the percentage of passing reads should be shown
     * @param requiredMinimumLength minimum required read length, or null if no minimum is given
     * @return the formatted result
     */
    private String formatResult(
            QCResult result,
            boolean showAll,
            boolean showQuality,
            boolean showDuplicates,
            boolean showGC,
            boolean showLength,
            boolean showPassingReads,
            Integer requiredMinimumLength
    ) {
        StringBuilder output = new StringBuilder();

        output.append(result.getSample())
                .append("\t")
                .append(result.getFileType());

        if (showAll || showQuality) {
            output.append("\t")
                    .append(formatPercentage(result.getBeginningQuality()))
                    .append("\t")
                    .append(formatPercentage(result.getEndQuality()));
        }

        if (showAll || showDuplicates) {
            output.append("\t")
                    .append(formatPercentage(result.getDuplicates()));
        }

        if (showAll || showGC) {
            output.append("\t")
                    .append(formatPercentage(result.getGcPercentage()));
        }

        if (showAll || showLength) {
            output.append("\t")
                    .append(result.getMinimumSequenceLength())
                    .append("\t")
                    .append(result.getMaximumSequenceLength());

            if (requiredMinimumLength != null) {
                output.append("\t")
                        .append(formatPercentage(result.getBelowMinimumPercent()));
            }
        }

        if (showAll || showPassingReads) {
            output.append("\t")
                    .append(formatPercentage(result.getPassingReadsPercent()));
        }

        output.append("\t")
                .append(result.getStatus());

        return output.toString();
    }


    /**
     * Formats a percentage or returns N/A when no value is available.
     *
     * @param value the percentage
     * @return the formatted percentage
     */
    private String formatPercentage(Double value) {
        if (value == null) {
            return "N/A";
        }

        return value + "%";
    }
}