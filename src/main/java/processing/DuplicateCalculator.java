package processing;

import java.util.HashSet;
import java.util.Set;

/**
 * DuplicateCalculator calculates the percentage of duplicate sequences
 * and returns the calculated percentage.
 */


public class DuplicateCalculator {
    /*
     * First implementation of the duplicate calculation using Map and HashMap.
     * It was later changed to Set and HashSet because only the unique reads
     * are needed to calculate the duplicate percentage.
     */

   /* public double calculateDuplicates(String[] reads) {
        Map<String, Integer> readCounts = new HashMap<>();
        for (String read : reads) {
            if (readCounts.containsKey(read)) {
                readCounts.put(read, readCounts.get(read) + 1);
            } else {
                readCounts.put(read, 1);
            }
        }

        int duplicates = reads.length - readCounts.size();

        double duplicatePercentage = (double) duplicates / reads.length * 100;

        return duplicatePercentage;
    }
    */


    /**
     * Calculates the percentage of duplicate reads in the array of sequence reads.
     *
     * @param reads an array containing the read sequences
     * @return the percentage of reads that are duplicates
     */

    public double calculateDuplicates(String[] reads) {
        Set<String> uniqueReads = new HashSet<>();
        for (String read : reads) {
            uniqueReads.add(read);
        }

        int duplicates = reads.length - uniqueReads.size();

        double duplicatePercentage = (double) duplicates / reads.length * 100;

        return duplicatePercentage;
    }

}

