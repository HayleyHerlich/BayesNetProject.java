import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SoundSensorDistribution {
    // Controls debugging output, based on main class boolean
    private static boolean debug = MonkeyCapture.getDebug();

    // Map to store conditional probs P(S|C) for diff sound locs given curr loc
    private static Map<Location, Map<Location, BigDecimal>> soundDistrib = new HashMap<>();

    // Update P(S|C) based on curr loc
    private static void updateSound(Location location) {
        Map<Location, BigDecimal> sound = new HashMap<>();

        // P(S|C) when sound is found at curr loc
        sound.put(location, new BigDecimal("0.6").setScale(8, RoundingMode.HALF_UP));

        // Calc P(S|C) when sound is found in 1 manhattan dist away
        ArrayList<Location> sLocations;
        sLocations = MonkeyCapture.locsOneMDAway(location.row(), location.col());
        BigDecimal probability = BigDecimal.valueOf(0.3 / sLocations.size()).setScale(8, RoundingMode.HALF_UP);
        for (Location loc : sLocations) {
            sound.put(loc, probability);
        }

        // Calc P(S|C) when sound is found in 2 manhattan dists away
        sLocations = MonkeyCapture.locsTwoMDAway(location.row(), location.col());
        probability = BigDecimal.valueOf(0.1 / sLocations.size()).setScale(8, RoundingMode.HALF_UP);
        for (Location loc : sLocations) {
            sound.put(loc, probability);
        }

        // Set P(S|C) to 0 for rest of locs
        for (Location loc : MonkeyCapture.getLocations()) {
            if (!sound.containsKey(loc)) {
                sound.put(loc, new BigDecimal(0));
            }
        }
        // Store updated P(S|C) in dist map
        soundDistrib.put(location, sound);
    }

    // Get dist of P(S|C) for all curr locs
    public static Map<Location, Map<Location, BigDecimal>> getSoundDistrib() {
        // Update P(S|C)
        for (Location loc : MonkeyCapture.getLocations()) {
            updateSound(loc);
        }

        // Show debug info if enabled
        if (debug) {
            System.out.println("\nSound distrib: ");
            for (Location loc : MonkeyCapture.getLocations()) {
                System.out.println("Curr loc: " + loc.toString());
                for (Location currLoc : soundDistrib.get(loc).keySet()) {
                    System.out.println(" Sound reported at: " + currLoc + " prob: " + soundDistrib.get(loc).get(currLoc));
                }
            }
        }
        // Return updated P(S|C) dist
        return soundDistrib;
    }

    // Return specific P(S|C)
    public static BigDecimal getProb(Location soundLocation, Location currLocation) {
        // Update P(S|C)
        updateSound(soundLocation);
        return soundDistrib.get(currLocation).get(soundLocation);
    }
}
