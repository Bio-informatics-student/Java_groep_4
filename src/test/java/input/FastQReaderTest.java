package input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FastQReaderTest {

    @TempDir
    Path tempDir;

    /**
     * Tests whether FASTQ records are read correctly from a file.
     */
    @Test
    void readsFastqRecords() throws IOException {
        Path fastqFile = tempDir.resolve("sample.fastq");

        Files.writeString(
                fastqFile,
                "@read1\n" +
                "ACTG\n" +
                "+\n" +
                "IIII\n" +
                "@read2\n" +
                "GGCA\n" +
                "+\n" +
                "HHHH\n");

        FastQReader reader = new FastQReader();

        List<String[]> records = reader.read(fastqFile);

        assertEquals(2, records.size());

        assertEquals("@read1", records.get(0)[0]);
        assertEquals("ACTG", records.get(0)[1]);
        assertEquals("+", records.get(0)[2]);
        assertEquals("IIII", records.get(0)[3]);

        assertEquals("@read2", records.get(1)[0]);
        assertEquals("GGCA", records.get(1)[1]);
        assertEquals("+",  records.get(1)[2]);
        assertEquals("HHHH", records.get(1)[3]);
    }
}
