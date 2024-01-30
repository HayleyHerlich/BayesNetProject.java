import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrentLocationDistribution {
    // Controls debugging output, based on main class boolean
    private static boolean debug = MonkeyCapture.getDebug();

    // Map to store conditional probs P(C|L) for all curr locs
    private static final Map<Location, Map<Location, BigDecimal>> currLocDistribution = new HashMap<>();

    // Update P(C|L) for a last loc
    public static void updateCurrLocProb(Location lastLocation) {
        Map<Location, BigDecimal> currLocProbabilities = new HashMap<>();
        ArrayList<Location> locations = MonkeyCapture.locsOneMDAway(lastLocation.row(), lastLocation.col());
        BigDecimal probability = BigDecimal.valueOf(1 / (double) locations.size()).setScale(8, RoundingMode.HALF_UP);

        // Init P(C|L) for locs within one manhattan dist away from last loc
        for (Location location : locations) {
            currLocProbabilities.put(location, probability);

        }

        // Set P(C|L) to 0 for all other locs
        for (Location location : MonkeyCapture.getLocations()) {
            if (!currLocProbabilities.containsKey(location)) {
                currLocProbabilities.put(location, new BigDecimal(0));
            }
        }

        // Store updated P(C|L) in dist map
        currLocDistribution.put(lastLocation, currLocProbabilities);
    }

    // Get distribution of P(C|L) for all last locs
    public static Map<Location, Map<Location, BigDecimal>> getDistrib() {
        // Update P(C|L) for all possible last locs
        for (Location location : MonkeyCapture.getLocations()) {
            updateCurrLocProb(location);
        }

        // Display debugging info if enabled
        if (debug) {
            System.out.println("\nCurr loc distribution:");
            for (Location location : MonkeyCapture.getLocations()) {
                System.out.println("Last loc: " + location.toString());
                for (Location currLoc : currLocDistribution.get(location).keySet()) {
                    System.out.println(" Curr loc: " + currLoc + " prob: " + currLocDistribution.get(location).get(currLoc));
                }
            }
        }
        // Return updated P(C|L) distribution
        return currLocDistribution;
    }

    // Specific P(C|L)
    public static BigDecimal getProb(Location lastLoc, Location currLoc) {
        // Update P(C|L)
        updateCurrLocProb(lastLoc);
        return currLocDistribution.get(lastLoc).get(currLoc);
    }
}
