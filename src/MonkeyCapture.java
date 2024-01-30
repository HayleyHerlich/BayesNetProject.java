import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MonkeyCapture {
    private static int rows;
    private static int cols;

    //private Map<String, Double> locationDistribution;
    //private Map<String, Double> currentLocationDistribution;

    // Debugging constant
    private static boolean debug = true;

    // Main function
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt user for filename
        System.out.println("Textfile?");
        String filename = scanner.nextLine();
        //System.out.println(filename);

        System.out.println("Hi Prof Kirlin, do you want the full debugging info? (y/n)");
        String answer = scanner.nextLine();

        debug = Objects.equals(answer, "y");

        // Open text file
        InputStream file = MonkeyCapture.class.getResourceAsStream(filename);
        System.out.println(file);

        // Check if file is found
        if (file == null) {
            System.out.println("File doesn't work");
            System.exit(1);
        }

        Scanner info = new Scanner(file);

        // Read grid dimensions from 1st line of textfiles
        String gridSizeLine = info.nextLine();
        String[] gridParts = gridSizeLine.split(" ");
        rows = Integer.parseInt(gridParts[0]);
        cols = Integer.parseInt(gridParts[1]);

        // Initialize location distribution for monkey's last loc
        LocationDistribution.initializeLoc(rows, cols);
        Map<Location, BigDecimal> location = LocationDistribution.getDistribution();

        // Print initial distrib of monkey's last loc
        System.out.println("Init distrib of monkey's last loc: ");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.println("   " + location.get(new Location(i, j)));
            }
            System.out.println();
        }

        // Initialize the rest of the probability distributions
        MotionSensorsDistribution.updateProbs(rows, cols);
        CurrentLocationDistribution.getDistrib();
        MotionSensorsDistribution.getM1Distrib(true);
        MotionSensorsDistribution.getM2Distrib(true);
        SoundSensorDistribution.getSoundDistrib();

        // Init timestep as zero
        int timestep = 0;
        while (info.hasNextLine()) {
            // Parse the rest of the file for m1, m2, sRow, and sCol
            String[] nextPart = info.nextLine().split(" ");
            boolean m1 = false;
            boolean m2 = false;

            // Sensor data
            if (nextPart[0].equals("1")) {
                m1 = true;
            }
            if (nextPart[1].equals("1")) {
                m2 = true;
            }

            // Sound sensor data location
            int sRow = Integer.parseInt(nextPart[2]);
            int sCol = Integer.parseInt(nextPart[3]);
            Location soundLocation = new Location(sRow, sCol);

            System.out.println("Observation: M1: " + m1 + " M2: " + m2
                    + "Sound loc: " + soundLocation);
            System.out.println("Monk's predicted curr loc at time step: " + timestep);

            // Calc probs for monk's curr loc
            Map<Location, Double> currLocProbDistribution = new HashMap<>();
            double total = 0;

            // Iterate through locations the monkey could currently be in
            for (Location currLoc : getLocations()) {
                double num = jointProb(location, currLoc, m1, m2, soundLocation);
                currLocProbDistribution.put(currLoc, num);
                total += num;
            }

            // Normalize and update loc distributions
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Location normLoc = new Location(i, j);
                    BigDecimal normalizedProbability = new BigDecimal(currLocProbDistribution.get(normLoc) / total).setScale(8, RoundingMode.HALF_UP);
                    location.put(normLoc, normalizedProbability);
                    System.out.println("Norm prob: " + normalizedProbability);
                }
                System.out.println();
            }
            timestep++;
        }
    }

    // Func to gen all possible locs
    public static ArrayList<Location> getLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                locations.add(new Location(i, j));
            }
        }
        return locations;
    }

    // Joint Prob function for monkey's curr loc
    public static double jointProb(Map<Location, BigDecimal> lastLocation, Location currLocation, boolean m1, boolean m2, Location sound) {
        double sum = 0;

        // Conditional probabilities
        double probM1GivenC = MotionSensorsDistribution.probM1(m1, currLocation).doubleValue();
        double probM2GivenC = MotionSensorsDistribution.probM2(m2, currLocation).doubleValue();
        double probSGivenC = SoundSensorDistribution.getProb(sound, currLocation).doubleValue();

        if (debug) {
            System.out.println("Calculating total prob for curr loc " + currLocation);
        }

        // Iterate through locations for Monkey's last loc
        for (Location location : lastLocation.keySet()) {
            double locProb = LocationDistribution.getProb(location).doubleValue();
            double probCGivenL = CurrentLocationDistribution.getProb(location, currLocation).doubleValue();

            if (debug) {
                System.out.println("Probs being multiplied for last loc " + location + ": " + locProb +
                        " " + probCGivenL + " " + probM1GivenC + " " + probM2GivenC + " " + probSGivenC);
            }

            // Calculate joint prob for curr loc
            double product = locProb * probCGivenL * probM2GivenC * probM1GivenC * probSGivenC;
            sum += product;
        }
        return sum;
    }

    // Func to gen locs 1 manhattan dist away
    public static ArrayList<Location> locsOneMDAway(int row, int col) {
        ArrayList<Location> locations = new ArrayList<>();
        Location loc;
        if (col - 1 >= 0) {
            loc = new Location(row, col - 1);
            locations.add(loc);
        }
        if (col + 1 < cols) {
            loc = new Location(row, col + 1);
            locations.add(loc);
        }
        if (row - 1 >= 0) {
            loc = new Location(row - 1, col);
            locations.add(loc);
        }
        if (row + 1 < rows) {
            loc = new Location(row + 1, col);
            locations.add(loc);
        }
        return locations;
    }

    // Func to gen locs 2 manhattan distances away
    public static ArrayList<Location> locsTwoMDAway(int row, int col) {
        ArrayList<Location> locations = new ArrayList<>();
        Location loc;
        if (col - 2 >= 0) {
            loc = new Location(row, col - 2);
            locations.add(loc);
        }
        if (col + 2 < cols) {
            loc = new Location(row, col + 2);
            locations.add(loc);
        }
        if (row - 2 >= 0) {
            loc = new Location(row - 2, col);
            locations.add(loc);
        }
        if (row + 2 < rows) {
            loc = new Location(row + 2, col);
            locations.add(loc);
        }
        if (col - 1 >= 0 && row - 1 >= 0) {
            loc = new Location(row - 1, col - 1);
            locations.add(loc);
        }
        if (row + 1 < rows && col - 1 >= 0) {
            loc = new Location(row + 1, col - 1);
            locations.add(loc);
        }
        if (row - 1 >= 0 && col + 1 < cols) {
            loc = new Location(row - 1, col + 1);
            locations.add(loc);
        }
        if (row + 1 < rows && col + 1 < cols) {
            loc = new Location(row + 1, col + 1);
            locations.add(loc);
        }
        return locations;
    }

    // Func to return debug value
    public static boolean getDebug() {
        return debug;
    }

}

