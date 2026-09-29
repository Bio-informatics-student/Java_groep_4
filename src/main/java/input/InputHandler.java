package input;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the files provided by the user.
 * The user can provide a folder containing multiple file types. This class
 * sorts the supported files into separate lists so they can later be passed
 * to the correct reader, where the file contents are read and saved.
 */

public class InputHandler {

    private final List<Path> fastqFiles = new ArrayList<>();
    private final List<Path> samFiles = new ArrayList<>();
    private final List<Path> bamFiles = new ArrayList<>();
    private final List<Path> fastqcFiles = new ArrayList<>();

    /**
     * This method scans the given folder for supported input files and categorizes them by type.
     *
     * @param folderPath path to the folder selected by the user
     * @throws IOException if an error occurs while accessing the folder
     * @throws IllegalArgumentException if the given path is not a valid directory
     */

    public void scanFolder(String folderPath) throws IOException {
        Path folder = Path.of(folderPath);

        // Check whether the given path is a valid directory
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Invalid folder: " + folderPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {

                // Check subdirectories for FastQC output
                if (Files.isDirectory(file)) {
                    Path fastqcData = file.resolve("fastqc_data.txt");

                    if (Files.isRegularFile(fastqcData)) {
                        fastqcFiles.add(fastqcData);
                    }

                    continue;
                }

                // Ignore unsupported files
                if (!Files.isRegularFile(file)) {
                    continue;
                }

                String fileName = file.getFileName()
                        .toString()
                        .toLowerCase();

                // Categorize supported files based on their extension
                if (fileName.endsWith(".fastq")
                        || fileName.endsWith(".fq")
                        || fileName.endsWith(".fastq.gz")
                        || fileName.endsWith(".fq.gz")) {

                    // Add the detected files to a list
                    fastqFiles.add(file);

                } else if (fileName.endsWith(".sam")) {
                    samFiles.add(file);

                }  else if (fileName.endsWith(".bam")) {
                    bamFiles.add(file);
                }
            }
        }
    }

    // Return the detected files in lists
    public List<Path> getFastqFiles() {
        return fastqFiles;
    }

    public List<Path> getSamFiles() {
        return samFiles;
    }

    public List<Path> getBamFiles() {
        return bamFiles;
    }

    public List<Path> getFastqcFiles() {
        return fastqcFiles;
    }
}
