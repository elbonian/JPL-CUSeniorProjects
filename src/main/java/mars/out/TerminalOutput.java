package mars.out;

import mars.algorithm.Algorithm;
import mars.coordinate.*;
import mars.map.GeoTIFF;

import java.util.*;

/**
 * Class which writes a discovered path to the terminal.
 */
public class TerminalOutput extends Output {

    /**
     * Constructor for TerminalOutput which takes an Algorithm.
     * It pulls the path out of the algorithm and then immediately prints the output.
     * @param algorithm The completed algorithm which holds the path to be output.
     */
    public TerminalOutput(Algorithm algorithm) {
        resultList = algorithm.getPath();
        coordinateType = algorithm.rover.getcoordType();
        writeToOutput();
    }

    /**
     * Constructor for TerminalOutput which takes a list of coordinates.
     * It immediately prints the output.
     * @param out list of coordinates generated by program
     */
    public TerminalOutput(List<? extends Coordinate> out) {
        resultList = out;
        writeToOutput();
    }

    /**
     * Function that outputs resultList to terminal in a user-friendly format.
     */
    public void writeToOutput() {
        System.out.println("\nOutput path: ");
        System.out.println("------------");

        if(coordinateType.equals("L") || coordinateType.equals("l")){
            GeoTIFF convert = new GeoTIFF();
            for (int i = 1; i <= resultList.size(); i++) {
                int x = resultList.get(i-1).getX();
                int y = resultList.get(i-1).getY();
                double [] outputCoordinate = convert.coordinateConvert(new Coordinate(x, y));
                System.out.println(i + ". (" + outputCoordinate[0]+ ", " + outputCoordinate[1] + ")");
            }
        }
        else{
            for (int i = 1; i <= resultList.size(); i++) {
                int x = resultList.get(i - 1).getX();
                int y = resultList.get(i - 1).getY();
                System.out.println(i + ". (" + x + ", " + y + ")");
            }
        }
        System.out.println("------------");
    }
}
