package mars.algorithm.unlimited;

import mars.algorithm.Algorithm;
import mars.coordinate.BestFirstCoordinate;
import mars.coordinate.Coordinate;
import mars.out.TerminalOutput;
import mars.rover.MarsRover;

import java.util.ArrayList;



public class AlgorithmUnlimitedBestFirst extends Algorithm {

    ArrayList<Coordinate> fullPath = new ArrayList<Coordinate>();
    Coordinate goal;

    /**
     * Default constructor for an AlgorithmUnlimitedScopeNonRecursive.
     *
     * @param r The rover
     * @param output The output type specified during this algorithm's instantiation
     */
    public AlgorithmUnlimitedBestFirst(MarsRover r, String output) {
        rover = r;
        map = rover.getMap();
        goal = r.getEndPosition();
        outputClass = output;
    }

    /**
     * Second constructor for an AlgorithmUnlimitedScopeNonRecursive which defaults output to "TerminalOutput".
     *
     * @param r The rover
     */
    public AlgorithmUnlimitedBestFirst(MarsRover r) {
        rover = r;
        map = rover.getMap();
        goal = r.getEndPosition();
        outputClass = "TerminalOutput";
    }

    /*
     *   Return path
     */
    public ArrayList<Coordinate> getPath() {
        return fullPath;
    }



    /*
     * Implementation of Best First
     */
    public void findPath() {
        BestFirstCoordinate startPosition = new BestFirstCoordinate(rover.getStartPosition());
        BestFirstCoordinate endPosition = new BestFirstCoordinate(rover.getEndPosition());

        ArrayList<BestFirstCoordinate> closed = new ArrayList<BestFirstCoordinate>();
        ArrayList<BestFirstCoordinate> open = new ArrayList<BestFirstCoordinate>();
        open.add(startPosition);

        BestFirstCoordinate current;
        ArrayList<BestFirstCoordinate> neighbors = new ArrayList<BestFirstCoordinate>();

        while(! open.isEmpty()){
            current = getLowestFScore(open);
            if (current.equals(goal)) { //if we found the goal
                //No-op. We're done.
            }
            closed.add(current);
            open.remove(current);
            neighbors = getReachableNeighbors(current);

            for(BestFirstCoordinate n : neighbors){
                if(!closed.contains(n)){
                    open.add(n);
                }
            }



        }

    }

    //------------------Helper_methods---------------------------------------------------
    /**
     * Given a coordinate, return all the neighboring coordinates
     * which can be visited by this algorithm's rover (meaning that
     * the slope between the coordinates is not too steep).
     * Possible neighbors are all eight coordinates surrounding the given one.
     * @param coord The coordinate whose neighbors will be found.
     */
    public ArrayList<BestFirstCoordinate> getReachableNeighbors(BestFirstCoordinate coord) {
        int x = coord.getX();
        int y = coord.getY();

        ArrayList<BestFirstCoordinate> neighbors = new ArrayList<BestFirstCoordinate>();

        for (int i = x-1; i <= x+1; i++) {
            for (int j = y-1; j <= y+1; j++) {
                if (!(i == x && j == y)) { //if this is not the given coordinate "coord"
                    try {
                        BestFirstCoordinate potentialNeighbor = new BestFirstCoordinate(i, j);
                        potentialNeighbor.setParent(coord);

                        if (rover.canTraverse(coord, potentialNeighbor)) { //if rover could visit this coordinate, add it
                            neighbors.add(potentialNeighbor);
                        }
                    } catch (Exception e) {
                        //"potentialNeighbor" was out of bounds of the map, so no-op.
                    }
                }
            }
        }

        return neighbors;
    }


    /**
     * Heuristic function for BestFirst implementing diagonal distance between two nodes
     * @param current current node
     * @param goal goal node
     * @return diagonal distance
     */

    private double estimateHeuristic(Coordinate current, Coordinate goal) {
        //Chebyshev/Diagonal Distance (allows diagonal movement)


        double currentXPos = current.getX();
        double currentYPos = current.getY();

        double goalXPos = goal.getX();
        double goalYPos = goal.getY();

        return Math.max(Math.abs(currentXPos - goalXPos) , Math.abs(currentYPos - goalYPos));
    }

    private BestFirstCoordinate getLowestFScore(ArrayList<BestFirstCoordinate> list) {
        BestFirstCoordinate lowest = list.get(0);
        for (BestFirstCoordinate n : list) {
            if (estimateHeuristic(n, goal) < estimateHeuristic(lowest, goal)) {
                lowest = n;
            }
        }
        return lowest;
    }

    /**
     * Constructs path using parent nodes
     * @param coord final coordinate in path
     *
     */
    private ArrayList<BestFirstCoordinate> constructPath(BestFirstCoordinate coord){
        ArrayList<BestFirstCoordinate> path = new ArrayList<BestFirstCoordinate>();
        while (coord != null){
            path.add(coord);
            coord = coord.getParent();
        }
        return path;
    }

}
