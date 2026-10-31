package processing;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
