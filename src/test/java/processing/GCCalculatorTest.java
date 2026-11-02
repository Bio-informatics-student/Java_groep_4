package processing;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GCCalculatorTest {
    @Test
    public void testGCCalculator() {
        GCCalculator gcCalculator = new GCCalculator();

        double result = gcCalculator.calculateGCPercentage(List.of("ATCG"));

        assertEquals(50.0, result);
    }

    @Test
    public void testGCCalculatorZero() {
        GCCalculator gcCalculator = new GCCalculator();

        double result = gcCalculator.calculateGCPercentage(List.of("ATTA"));

        assertEquals(0.0, result);
    }
}
