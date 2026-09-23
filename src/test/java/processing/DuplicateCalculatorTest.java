package processing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the duplicate percentage calculation using different scenarios.
 */

public class DuplicateCalculatorTest {
    /**
     * Checks that the duplicate percentage is zero when all reads are unique.
     */
    @Test
    void returnsZeroWhenAllReadsUnique() {
        String[] reads = {"ACTG", "GGTA", "CCAA", "TTTT"};
        DuplicateCalculator calculator = new DuplicateCalculator();

        double duplicatesPercentage = calculator.calculateDuplicates(reads);

        assertEquals(0.0, duplicatesPercentage);
    }

    /**
     * Checks that the duplicate percentage is 25% when a quarter of the reads are duplicates.
     */
    @Test
    void returnsPercentageWhenDuplicates() {
        String[] reads = {"A", "B", "C", "A"};
        DuplicateCalculator calculator = new DuplicateCalculator();

        double duplicatesPercentage = calculator.calculateDuplicates(reads);

        assertEquals(25.0, duplicatesPercentage);
    }

    /**
     * Checks that the duplicate percentage is 50% when half of the reads are duplicates.
     */
    @Test
        void returnsPercentageWhenMoreDuplicates() {
            String[] reads = {"A", "A", "B", "A"};
            DuplicateCalculator calculator = new DuplicateCalculator();

            double duplicatesPercentage = calculator.calculateDuplicates(reads);

            assertEquals(50.0, duplicatesPercentage);
        }

    /**
     * Checks that the duplicate percentage is 75% when 3/4 of the reads are duplicates.
     */
    @Test
    void returnsPercentageWhenSameSequence() {
        String[] reads = {"A", "A", "A", "A"};
        DuplicateCalculator calculator = new DuplicateCalculator();

        double duplicatesPercentage = calculator.calculateDuplicates(reads);

        assertEquals(75.0, duplicatesPercentage);
    }

    /**
     * Checks that a duplicate percentage with decimals is calculated correctly,
     * using a tolerance of 0.01.
     */
    @Test
    void returnsNumberWhenPercentageIsDecimal() {
        String[] reads = {"A", "A", "B"};
        DuplicateCalculator calculator = new DuplicateCalculator();

        double duplicatesPercentage = calculator.calculateDuplicates(reads);
        assertEquals(33.33, duplicatesPercentage, 0.01);
    }
}



