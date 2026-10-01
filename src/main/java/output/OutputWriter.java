package output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * OutputWriter writes QC results to the terminal
 * and to a text file.
 */
public class OutputWriter {

    /**
     * Prints the QC results to the terminal.
     *
     * @param results the QC results to print
     */
    public void printToTerminal(List<QCResult> results) {
        System.out.println(
                "Sample\tType\tBegin kwaliteit\tEind kwaliteit\tDuplicaten\tEindstatus"
        );

        for (QCResult result : results) {
            System.out.println(formatResult(result));
        }
    }

    /**
     * Writes the QC results to a text file.
     *
     * @param results the QC results to write
     * @param outputFile the file where the results are written
     * @throws IOException when the file cannot be written
     */
    public void writeToFile(
            List<QCResult> results,
            Path outputFile
    ) throws IOException {

        StringBuilder output = new StringBuilder();

        output.append(
                "Sample\tType\tBegin kwaliteit\tEind kwaliteit\tDuplicaten\tEindstatus\n"
        );

        for (QCResult result : results) {
            output.append(formatResult(result));
            output.append("\n");
        }

        Files.writeString(outputFile, output.toString());
    }

    /**
     * Converts one QC result into one line of output.
     *
     * @param result the QC result
     * @return the formatted result
     */
    private String formatResult(QCResult result) {
        return result.getSample() + "\t"
                + result.getFileType() + "\t"
                + formatPercentage(result.getBeginningQuality()) + "\t"
                + formatPercentage(result.getEndQuality()) + "\t"
                + formatPercentage(result.getDuplicates()) + "\t"
                + result.getStatus();
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