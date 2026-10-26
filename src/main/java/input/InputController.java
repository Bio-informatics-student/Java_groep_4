
package input;

import output.QCResult;
import processing.FastqQCProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Coordinates input discovery and FASTQ processing.
 */
public class InputController {

    private final InputHandler inputHandler;
    private final FastqQCProcessor processor;

    // Creates the components needed to discover and process input files.
    public InputController() {
        this.inputHandler = new InputHandler();
        this.processor = new FastqQCProcessor();
    }

    /**
     * Finds FASTQ files in the supplied file or directory and processes each one.
     * The results are collected into a list so they can be displayed and saved together.
     *
     * @param input FASTQ file or directory containing input files
     * @param endPercentage percentage of each read end to analyse
     * @param requiredPassingReadsPercent minimum percentage of reads that must pass
     * @param maximumDuplicatePercent maximum allowed duplicate percentage
     * @param minQualityScore minimum Phred score for a base to pass
     * @return one QC result for each processed FASTQ file
     * @throws IOException if an input file cannot be read
     * @throws IllegalArgumentException if the input is invalid or no FASTQ files are found
     */
    public List<QCResult> handleInput(
            Path input,
            double endPercentage,
            double requiredPassingReadsPercent,
            double maximumDuplicatePercent,
            int minQualityScore
    ) throws IOException {

        List<Path> fastqFiles = new ArrayList<>();

        if (Files.isRegularFile(input)) {
            if (isFastq(input)) {
                fastqFiles.add(input);
            }
        } else if (Files.isDirectory(input)) {
            inputHandler.scanFolder(input.toString());
            fastqFiles.addAll(inputHandler.getFastqFiles());
        } else {
            throw new IllegalArgumentException(
                    "Input is not a file or directory: " + input
            );
        }

        if (fastqFiles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No FASTQ files found in: " + input
            );
        }

        List<QCResult> results = new ArrayList<>();

        for (Path file : fastqFiles) {
            results.add(
                    processor.process(
                            file,
                            endPercentage,
                            requiredPassingReadsPercent,
                            maximumDuplicatePercent,
                            minQualityScore
                    )
            );
        }

        return results;
    }

    // Checks the supported filename extensions for FASTQ files.
    private boolean isFastq(Path file) {
        String name = file.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return name.endsWith(".fastq")
                || name.endsWith(".fq")
                || name.endsWith(".fastq.gz")
                || name.endsWith(".fq.gz");
    }
}
