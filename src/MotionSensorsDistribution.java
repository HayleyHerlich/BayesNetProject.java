import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class MotionSensorsDistribution {

    // Controls debugging output, based on main class boolean
    private static boolean debug = MonkeyCapture.getDebug();

    // Maps to store conditional probs P(M1|C) & P(M2|C)
    private static Map<Location, BigDecimal> m1T = new HashMap<>(); // top left = T
    private static Map<Location, BigDecimal> m1F = new HashMap<>(); // top left = F
    private static Map<Location, BigDecimal> m2T = new HashMap<>(); // top right = T
    private static Map<Location, BigDecimal> m2F = new HashMap<>(); // top right = F

    // Initialize conditional probs for P(M1|C) & P(M2|C)
    public static void updateProbs(int row, int col) {
        Location locationUP;

        for (int i = 0; i < row; i++) {
            // Calc probs based on "i"
            BigDecimal prob = BigDecimal.valueOf(0.9 - (0.1 * i));
            BigDecimal normalizedProb = BigDecimal.valueOf(1 - (0.9 - (0.1 * i)));
            prob = prob.setScale(8, RoundingMode.HALF_UP);
            normalizedProb = normalizedProb.setScale(8, RoundingMode.HALF_UP);

            // Calc and init P(M1|C) for diff locs
            locationUP = new Location(i, 0);
            m1T.put(locationUP, prob);
            m1F.put(locationUP, normalizedProb);
            locationUP = new Location(0, i);
            m1T.put(locationUP, prob);
            m1F.put(locationUP, normalizedProb);

            int newRow = row - i - 1;
            int newCol = col - 1;
            locationUP = new Location(newRow, newCol);
            m2T.put(locationUP, prob);
            m2F.put(locationUP, normalizedProb);

            int newerRow = row - 1;
            int newerCol = col - i - 1;
            locationUP = new Location(newerRow, newerCol);
            m2T.put(locationUP, prob);
            m2F.put(locationUP, normalizedProb);
        }

        // Define probs for locs that aren't initilaized with P(M1|C)
        BigDecimal trueProb = new BigDecimal("0.05000000").setScale(8, RoundingMode.HALF_UP);
        BigDecimal falseProb = new BigDecimal("0.95000000").setScale(8, RoundingMode.HALF_UP);

        for (Location loc : MonkeyCapture.getLocations()) {
            if (!m1T.containsKey(loc)) {
                m1T.put(loc, trueProb);
                m1F.put(loc, falseProb);
            }
            if (!m2T.containsKey(loc)) {
                m2T.put(loc, trueProb);
                m2F.put(loc, falseProb);
            }
        }
    }

    // Distribution of P(M1|C) for true or false
    public static Map<Location, BigDecimal> getM1Distrib(boolean answer) {
        if (debug) {
            System.out.println("MS1 distrib: ");
            for (Location location : MonkeyCapture.getLocations()) {
                System.out.println("Curr loc: " + location.toString() + ", true prob: " + m1T.get(location) + "false prob " + m1F.get(location));
            }
        }
        if (answer) {
            return m1T;
        } else {
            return m1F;
        }
    }

    // Distribution of P(M2|C) for true or false
    public static Map<Location, BigDecimal> getM2Distrib(boolean answer) {
        if (debug) {
            System.out.println("MS2 distrib: ");
            for (Location location : MonkeyCapture.getLocations()) {
                System.out.println("Curr loc: " + location.toString() + ", true prob: " + m2T.get(location) + "false prob " + m2F.get(location));
            }
        }
        if (answer) {
            return m2T;
        } else {
            return m2F;
        }
    }

    // Get prob of M1 given value of m1 & curr loc
    public static BigDecimal probM1(boolean m1, Location currLoc) {
        if (m1) {
            return m1T.get(currLoc);
        } else {
            return m1F.get(currLoc);
        }
    }

    // Get prob of m2 given m2 & curr loc
    public static BigDecimal probM2(boolean m2, Location currLoc) {
        if (m2) {
            return m2T.get(currLoc);
        } else {
            return m2F.get(currLoc);
        }
    }
}
