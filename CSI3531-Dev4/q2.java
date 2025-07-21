import java.util.*;

public class q2 {

    public static void main(String[] args) {
        // Generate a random reference string of 20 pages (values between 0 and 9)
        int[] referenceString = generateReferenceString(20, 10);
        System.out.println("Reference String: " + Arrays.toString(referenceString));
        System.out.println("\nFrames | FIFO | LRU");
        System.out.println("--------------------");

        // Test both FIFO and LRU for frame counts from 1 to 7
        for (int frames = 1; frames <= 7; frames++) {
            int fifoFaults = fifo(referenceString, frames);
            int lruFaults = lru(referenceString, frames);
            System.out.printf("  %d     |  %d   | %d\n", frames, fifoFaults, lruFaults);
        }
    }

    /**
     * Generates a random reference string.
     * @param length number of page references
     * @param pageRange range of page numbers (e.g., 0 to 9)
     * @return array of random page numbers
     */
    public static int[] generateReferenceString(int length, int pageRange) {
        Random rand = new Random();
        int[] reference = new int[length];
        for (int i = 0; i < length; i++) {
            reference[i] = rand.nextInt(pageRange); // Random number between 0 and pageRange - 1
        }
        return reference;
    }

    /**
     * Simulates the FIFO page replacement algorithm.
     * @param reference the page reference string
     * @param frameCount number of frames (slots) in memory
     * @return number of page faults
     */
    public static int fifo(int[] reference, int frameCount) {
        Set<Integer> frames = new HashSet<>();      // Tracks pages currently in memory
        Queue<Integer> queue = new LinkedList<>();  // Maintains the FIFO order
        int pageFaults = 0;

        for (int page : reference) {
            // If page is not already in memory, it's a fault
            if (!frames.contains(page)) {
                pageFaults++;

                // If memory is full, remove the oldest page
                if (frames.size() == frameCount) {
                    int removed = queue.poll();
                    frames.remove(removed);
                }

                // Add the new page
                frames.add(page);
                queue.offer(page);
            }
            // Else, the page is already in memory – no fault
        }
        return pageFaults;
    }

    /**
     * Simulates the LRU (Least Recently Used) page replacement algorithm.
     * @param reference the page reference string
     * @param frameCount number of frames (slots) in memory
     * @return number of page faults
     */
    public static int lru(int[] reference, int frameCount) {
        Set<Integer> frames = new HashSet<>();              // Tracks pages currently in memory
        Map<Integer, Integer> recentUse = new HashMap<>();  // Maps each page to its last used index
        int pageFaults = 0;

        for (int i = 0; i < reference.length; i++) {
            int page = reference[i];

            // If page is not in memory → page fault
            if (!frames.contains(page)) {
                pageFaults++;

                // If memory is full, remove the least recently used page
                if (frames.size() == frameCount) {
                    int lruPage = getLRUPage(recentUse);
                    frames.remove(lruPage);
                    recentUse.remove(lruPage);
                }

                // Add the new page
                frames.add(page);
            }

            // Update the last used index of the current page
            recentUse.put(page, i);
        }

        return pageFaults;
    }

    /**
     * Finds the page with the earliest last used time (Least Recently Used).
     * @param usageMap map of pages to their last used index
     * @return the LRU page number
     */
    private static int getLRUPage(Map<Integer, Integer> usageMap) {
        int lruPage = -1;
        int minTime = Integer.MAX_VALUE;

        for (Map.Entry<Integer, Integer> entry : usageMap.entrySet()) {
            if (entry.getValue() < minTime) {
                minTime = entry.getValue();
                lruPage = entry.getKey();
            }
        }
        return lruPage;
    }
}
