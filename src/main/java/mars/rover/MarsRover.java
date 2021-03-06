package mars.rover;

import mars.coordinate.Coordinate;
import mars.map.GeoTIFF;

/**
 * Represents a rover which traverses a given terrain.
 */
public class MarsRover extends Rover {

    /**
     * Default constructor for the MarsRover class.
     *
     * @param slope the maximum slope that the rover can handle
     * @param startCoords the beginning X, Y position of the rover (passed in as an array).
     * @param endCoords the ending X, Y position of the rover (passed in as an array).
     */
    public MarsRover(double slope, String coordType, Coordinate startCoords, Coordinate endCoords, String mapPath) {
        setMaxSlope(slope);
        setCurrentPosition(startCoords);
        setStartPosition(startCoords);
        setCoordType(coordType);
        setEndPosition(endCoords);
        try {
            map.initTif(mapPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        fieldOfView = Double.POSITIVE_INFINITY; //"Unlimited"
    }

    /**
     * Constructor for a MarsRover with a limited field of view.
     *
     * @param slope The maximum slope that the rover can handle
     * @param startCoords The beginning X, Y position of the rover (passed in as an array).
     * @param endCoords The ending X, Y position of the rover (passed in as an array).
     * @param radius The radius of this rover's field of view
     */
    public MarsRover(double slope, String coordType, Coordinate startCoords, Coordinate endCoords, String mapPath, double radius) {
        setMaxSlope(slope);
        setCurrentPosition(startCoords);
        setStartPosition(startCoords);
        setCoordType(coordType);
        setEndPosition(endCoords);
        setFieldOfView(radius);
        try {
            map.initTif(mapPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function to determine mathematical slop between two points on the elevation map.
     * Formally, we find the difference between the elevation (z) of slope1 and slope2, and the xy distance between
     * (x1,y1) and (x2,y2), then construct a right-angle triangle such that:
     *                              /| vertex b (x2,y2,z2)
     *                            /' |
     *                          /'   | zDistance (adj)
     *                        /'     |
     *   vertex a (x1,y1,z1) '-------+
     *                xyDistance (opp)
     * Angle A at vertex a equals arctan(adj/opp) = atan(zDistance/xyDistance)
     * @param x1 x-coordinate (in pixels) of the first point
     * @param y1 y-coordinate (in pixels) of the first point
     * @param x2 x-coordinate (in pixels) of the second point
     * @param y2 y-coordinate (in pixels) of the second point
     * @return slope in degrees
     * @throws Exception exception generated by Geotools
     */
    public double getSlope(int x1, int y1, int x2, int y2) throws Exception {
        double z1 = map.getValue(x1,y1);
        double z2 = map.getValue(x2,y2);
        double zDistance = z2 - z1;
        double xyDistance = Math.sqrt(Math.pow((x2-x1),2)+Math.pow((y2-y1),2));
        return Math.toDegrees(Math.atan(zDistance / xyDistance)); // construct a right-angle triangle such that adjacent = xyDistance and opposite = zDistance
    }

    /**
     * Overload of getSlope to allow passing coordinates instead of x's and y's.
     * @param point1 coordinate 1 (in pixels) of where the rover currently is
     * @param point2 coordinate 2 (in pixels) of where the rover currently wants to go
     * @return slope in degrees
     * @throws Exception exception generated by Geotools
     */
    public double getSlope(Coordinate point1, Coordinate point2) throws Exception {
        int x1 = point1.getX();
        int y1 = point1.getY();
        int x2 = point2.getX();
        int y2 = point2.getY();

        double z1 = map.getValue(x1,y1);
        double z2 = map.getValue(x2,y2);
        double zDistance = z2 - z1;
        double xyDistance = Math.sqrt(Math.pow((x2-x1),2)+Math.pow((y2-y1),2));
        return Math.toDegrees(Math.atan(zDistance / xyDistance)); // construct a right-angle triangle such that adjacent = xyDistance and opposite = zDistance
    }

    /**
     * gets angle between two coordinates
     * @param current coord 1
     * @param goal coord 2
     * @return angle in degrees between the two coords
     */
    public double getAngle(Coordinate current, Coordinate goal) {
        int xdiff = goal.getX() - current.getX();
        int ydiff = goal.getY() - current.getY();
        double result = Math.toDegrees(Math.atan2(ydiff,xdiff));
        while(result < 0){result += 360;}
        return result;
    }

    /**
     * Main slope function. Derived from processSlope idea from Greedy Algorithm.
     * given two points, does the following:
     * 1. finds the angle between the two points (usually a cardinal direction or a diagonal)
     * 2. adjusts the points away from each other along the line produced by that angle, until they would change elevation
     * 3. finds the slope between the two adjusted points using their respective elevations and returns if it's traversable
     *
     * @param point1 first coord
     * @param point2 second coord
     * @return boolean if slope is acceptable
     */
    public boolean canTraverse(Coordinate point1, Coordinate point2) {
        try {
            // step 1
            double temp1x = point1.getX(); //manually get the components (makes the math a lot easier)
            double temp1y = point1.getY();
            double temp2x = point2.getX();
            double temp2y = point2.getY();

            double angle = getAngle(point1,point2);

            // make sure the point we're looking at is actually valid. protects from unexpected exceptions from the map functions
            if(temp1x < 0 || temp2x < 0 || temp1x > map.getWidth() || temp2x > map.getWidth()
                    || temp1y < 0 || temp2y < 0 || temp1y > map.getHeight() | temp2y > map.getHeight() )
                return false;

            // step 2
            double point1height = map.getValue(point1.getX(),point1.getY()); //get the heights of the given points
            double point2height = map.getValue(point2.getX(),point2.getY());
            if((point1height != point2height && !(map.getMapPath()).equals("src/main/resources/marsMap.tif")) ||
                    (Math.abs(point1height - point2height) > 6 && (map.getMapPath()).equals("src/main/resources/marsMap.tif"))){ //if the heights aren't the same
                //while the current adjusted point height and original are the same, and points are in bounds
                while(temp1x > 0 && temp1x < map.getWidth() && temp1y > 0 && temp1y < map.getHeight() ){
                    if(point1height != map.getValue(temp1x,temp1y)) break;
                    temp1x -= Math.cos(angle); //subtract one unit length in the desired angle. note we don't round until the end
                    temp1y -= Math.sin(angle);
                }
                //then do the same for the second point
                while(temp2x > 0 && temp2x < map.getWidth() && temp2y > 0 && temp2y < map.getHeight() ){
                    if(point2height != map.getValue(temp2x,temp2y)) break;
                    temp2x += Math.cos(angle);
                    temp2y += Math.sin(angle);
                }


                // step 3. finds the slope of these points and compares to maxSlope
                return Math.abs(getSlope((int)temp1x,(int)temp1y,(int)temp2x,(int)temp2y)) <= maxSlope;
            }else return true; //if they're the same height, then it can just freely go there and we can skip the hard part
        }
        catch(Exception e) { //if something breaks, then let's not go there
            return false;
        }
    }

    // Prints out all of the specs of this rover.
    public void printSpecs() {
        System.out.println("\nThe specs of this rover: ");
        System.out.println("Max slope: " + maxSlope);
        System.out.println("Ouput coordinates: "+ coordType);
        System.out.println("Field of view: " + ((fieldOfView==Double.MAX_VALUE) ? "Unlimited" : fieldOfView));
        System.out.println("Current position - X: " + currentPosition.getX() + ", Y: " + currentPosition.getY());
        System.out.println("Start position - X: " + startPosition.getX() + ", Y: " + startPosition.getY());
        System.out.println("End position - X: " + endPosition.getX() + ", Y: " + endPosition.getY());
    }

    //----Getter/Setter Methods----------------------------------------------------------------------------------------

    public GeoTIFF getMap() { return map; }

    public void setMaxSlope(double slope) {
        maxSlope = slope;
    }

    public double getMaxSlope() {
        return maxSlope;
    }

    public void setFieldOfView(double radius) {
        fieldOfView = radius;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setCoordType(String coordinateType) { coordType = coordinateType;}

    public String getCoordType(){return coordType;}

    public void setCurrentPosition(Coordinate position) {
        currentPosition = position;
    }

    public Coordinate getCurrentPosition() {
        return currentPosition;
    }

    public void setStartPosition(Coordinate position) {
        startPosition = position;
    }

    public Coordinate getStartPosition() {
        return startPosition;
    }

    public void setEndPosition(Coordinate position) {
        endPosition = position;
    }

    public Coordinate getEndPosition() {
        return endPosition;
    }

    public void setXPosition(int x) { currentPosition.setX(x); }

    public int getXPosition() {
        return currentPosition.getX();
    }

    public void setYPosition(int y) {
        currentPosition.setY(y);
    }

    public int getYPosition() {
        return currentPosition.getY();
    }

}
