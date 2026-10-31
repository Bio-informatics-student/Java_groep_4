package processing;

import java.util.List;

/**
 * Calculates the GC percentage of all sequencing reads per FastQ file.
 */

public class GCCalculator {

    /**
     * Calculates the percentage of guanine (G) and cytosine (C) per FastQ file.
     *
     * @param sequences
     * @return GC percentage
     */

    public double calculateGCPercentage(List<String> sequences) {
        // set counters
        long gcCount = 0;
        long totalCount = 0;

        for (String sequence : sequences) {
            // Check every base if it is a guanine (G) or cytosine (C) and keep track of the amount found
            for (char base : sequence.toUpperCase().toCharArray()) {
                if (base == 'G' || base == 'C') {
                    gcCount++;
                }
            }

            totalCount += sequence.length();
        }

        // Calculate GC percentage in the whole file
        return ((double) gcCount / totalCount) *100;
    }

}
