package processing;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SequenceLengthAnalyzerTest {

    @Test
    public void testMinimumLength() {
        SequenceLengthAnalyzer analyzer = new SequenceLengthAnalyzer();

        int result = analyzer.getMinimumLength(
                List.of("ATGC", "ATCGCGA", "TGA")
        );

        assertEquals(3, result);
    }

    @Test
    public void testMaximumLength() {
        SequenceLengthAnalyzer analyzer = new SequenceLengthAnalyzer();

        int result = analyzer.getMaximumLength(
                List.of("ATGC", "ATCGCGA", "TGA")
        );

        assertEquals(7, result);
    }

    @Test
    public void testPercentageBelowMinimum() {
        SequenceLengthAnalyzer analyzer = new SequenceLengthAnalyzer();

        double result = analyzer.calculatePercentageBelowMinimum(
                List.of("AT", "ATCG", "ATCGAT", "ATCGATCG"),
                5
        );

        assertEquals(50.0, result);
    }

    @Test
    public void testFilterByMinimumLength() {
        SequenceLengthAnalyzer analyzer = new SequenceLengthAnalyzer();

        String[] read1 = {"@read1", "AT", "+", "II"};
        String[] read2 = {"@read2", "ATCG", "+", "IIII"};
        String[] read3 = {"@read3", "ATCGAT", "+", "IIIIII"};
        String[] read4 = {"@read4", "ATCGATCG", "+", "IIIIIIII"};

        List<String[]> records = List.of(
                read1,
                read2,
                read3,
                read4
        );

        List<String[]> result = analyzer.filterByMinimumLength(records, 5);

        assertEquals(2, result.size());
        assertArrayEquals(read3, result.get(0));
        assertArrayEquals(read4, result.get(1));
    }
}
