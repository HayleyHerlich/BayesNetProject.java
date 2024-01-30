import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class LocationDistribution {

    // Map to store prob dist for monkey's last loc
    private static Map<Location, BigDecimal> locDistribution = new HashMap<>(); // P(L)

    // Init prob dist for last loc
    public static void initializeLoc(int row, int col) {
        // Calc init prob for each loc
        double prob = 1.0 / ((double) row * (double) col);
        // BigDec rounds prob to 8 decimal places
        BigDecimal probab = new BigDecimal(prob).setScale(8, RoundingMode.HALF_UP);

        // Iterate through all possible locs and initialize probabilities
        for (Location location : MonkeyCapture.getLocations()) {
            locDistribution.put(location, probab);
        }
    }

    // Return P(L)
    public static BigDecimal getProb(Location lastLocation) {
        return locDistribution.get(lastLocation);
    }

    // Return all probs for location
    public static Map<Location, BigDecimal> getDistribution() {
        return locDistribution;
    }
}
