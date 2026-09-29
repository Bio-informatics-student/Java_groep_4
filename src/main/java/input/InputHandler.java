package input;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class InputHandler {

    private final List<Path> fastqFiles = new ArrayList<>();
    private final List<Path> samFiles = new ArrayList<>();
    private final List<Path> bamFiles = new ArrayList<>();

    public void readFolder(String folderPath) throws IOException {
        Path folder = Path.of(folderPath);

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Invalid folder: " + folderPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {

                if (!Files.isRegularFile(file)) {
                    continue;
                }

                String fileName = file.getFileName()
                        .toString()
                        .toLowerCase();

                if (fileName.endsWith(".fastq")
                || fileName.endsWith(".fq")
                || fileName.endsWith(".fastq.gz")
                || fileName.endsWith(".fq.gz")) {

                    fastqFiles.add(file);
                } else if (fileName.endsWith(".sam")) {
                    samFiles.add(file);
                }  else if (fileName.endsWith(".bam")) {
                    bamFiles.add(file);
                }
            }
        }
    }

    public List<Path> getFastqFiles() {
        return fastqFiles;
    }

    public List<Path> getSamFiles() {
        return samFiles;
    }

    public List<Path> getBamFiles() {
        return bamFiles;
    }
}
