
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

    public InputController() {
        this.inputHandler = new InputHandler();
        this.processor = new FastqQCProcessor();
    }

    /**
     * Finds FASTQ files and processes each one.
     */
    public List<QCResult> handleInput(
            Path input,
            double endPercentage,
            double requiredPassingReadsPercent,
            double maximumDuplicatePercent
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
                            maximumDuplicatePercent
                    )
            );
        }

        return results;
    }

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
