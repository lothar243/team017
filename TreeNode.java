package team017;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/16/2014.
 */
public class TreeNode {
    public MapLocation coords;
    public TreeNode parentNode;
    public int distanceFromRoot;
    public Direction directionToGetHere;

    public TreeNode() {

    }
    // this constructor is for the root of the tree
    public TreeNode(MapLocation _rootCoords, Direction startDirection) {
        coords = _rootCoords;
        parentNode = null;
        distanceFromRoot = 0;
        directionToGetHere = startDirection;
    }
    // this constructor is for child nodes
    public TreeNode(MapLocation _childNodeCoords, TreeNode _parentNode, Direction _directionToGetHere) {
        coords = _childNodeCoords;
        parentNode = _parentNode;
        distanceFromRoot = parentNode.distanceFromRoot + 1;
        directionToGetHere = _directionToGetHere;
    }

    public static TreeNode addTreeNode(MapLocation _coords, TreeNode _parentNode, Direction _travelDirection) {
        return new TreeNode(_coords, _parentNode, _travelDirection);
    }

    public MapLocation[] toArray() {
        MapLocation[] output = new MapLocation[distanceFromRoot + 1];
        output = populate(output);
        for(MapLocation loc: output) { // checking for errors
            if(loc == null) {
                System.out.println("treeNode warning: returning a null value");
            }
            else {
                //System.out.println("Adding " + loc.toString());
            }
        }
        return output;
    }

    private MapLocation[] populate(MapLocation[] output) {
        output[distanceFromRoot] = coords;
        if(parentNode == null) {
            return output;
        }
        else {
            return parentNode.populate(output);
        }
    }

    @Override
    public String toString() {
        String output = "";
        MapLocation[] mapLocationArray = this.toArray();
        for(MapLocation loc: mapLocationArray) {
            output += loc.toString() + ", ";
        }
        return output;
    }

    public TreeNode removeRedundantNodes() {  // takes a tree and removes waypoint that are in a line
        if(parentNode == null) { // nothing to do here...
            return this;
        }
        else {
            MapLocation[] locArray = this.toArray();
            boolean[] necessaryWayPoints = new boolean[distanceFromRoot + 1];
            necessaryWayPoints[0] = true; // the start and the end of the path are necessary
            necessaryWayPoints[necessaryWayPoints.length - 1] = true;

            int optimizedIndex = 0; // work from the front of the array to the back determining which waypoints should be included
            while(optimizedIndex + 2 < necessaryWayPoints.length) { // there are at least two more waypoints left
                if(locArray[optimizedIndex].directionTo(locArray[optimizedIndex+1]).equals(locArray[optimizedIndex+1].directionTo(locArray[optimizedIndex+2]))) {
                    // we're not on a corner, so ignore this one
                    //System.out.println("Ignoring entry " + (optimizedIndex + 1));
                    necessaryWayPoints[optimizedIndex+1] = false;
                }
                else { // the direction has changed, so it's on a corner
                    necessaryWayPoints[optimizedIndex+1] = true;
                    //System.out.println("Including entry " + (optimizedIndex + 1));

                }
                optimizedIndex++;
            }

            //now to rebuild the tree with only the necessary waypoints
            TreeNode output = new TreeNode(locArray[0], directionToGetHere);
            for(int i = 1; i < necessaryWayPoints.length; i++) {
                if(necessaryWayPoints[i]) {
                    output = addTreeNode(locArray[i], output, Direction.NONE);
                }
            }
            return output;


        }
    }

    public static void main(String [] args) {
        TreeNode sample = new TreeNode(new MapLocation(0,0), Direction.EAST);
        TreeNode step1 = addTreeNode(new MapLocation(1,0), sample, Direction.EAST);
        TreeNode stepA = addTreeNode(new MapLocation(-1,0), sample, Direction.EAST);
        TreeNode step2 = addTreeNode(new MapLocation(2,0), step1, Direction.EAST);
        TreeNode step3 = addTreeNode(new MapLocation(3,0), step2, Direction.EAST);
        TreeNode step4 = addTreeNode(new MapLocation(4, 1), step3, Direction.EAST);
        TreeNode step3a = addTreeNode(new MapLocation(2,-1), step3, Direction.EAST);


        System.out.println("First test: distance: " + stepA.distanceFromRoot + ", toString(): " + stepA.toString());
        TreeNode stepAOpt = stepA.removeRedundantNodes();
        System.out.println("Optimized: distance: " + stepAOpt.distanceFromRoot + ", toString(): " + stepAOpt.toString());

        System.out.println("Second test: distance: " + step4.distanceFromRoot + ", toString(): " + step4.toString());
        TreeNode step4Opt = step4.removeRedundantNodes();
        System.out.println("Optimized: distance: " + step4Opt.distanceFromRoot + ", toString(): " + step4Opt.toString());

        System.out.println("Third test: distance: " + step3a.distanceFromRoot + ", toString(): " + step3a.toString());
        TreeNode step3aOpt = step3a.removeRedundantNodes();
        System.out.println("Optimized: distance: " + step3aOpt.distanceFromRoot + ", toString(): " + step3aOpt.toString());


    }


}
