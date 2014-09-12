package team017;

import battlecode.common.MapLocation;

import java.util.Map;

/**
 * Created by Mr. Arends on 9/12/14.
 */
public class waypointQueue {
    public MapLocation loc;
    public waypointQueue nextLoc;
    public int depth;

    public waypointQueue(waypointQueue wq) {
        loc = new MapLocation(wq.loc.x,wq.loc.y);
        nextLoc = new waypointQueue(wq.nextLoc);
        depth = wq.depth;
    }
    public waypointQueue(MapLocation _loc) {
        this(_loc, 1);
    }
    public waypointQueue(MapLocation _loc, int _depth) {
        loc = _loc;
        depth = _depth;
    }

    public void addWaypoint(MapLocation newLoc) {  // add a map location to the end of the queue
        if(nextLoc == null) // we're at the end of the queue, so set the nextLoc to the one being added
            nextLoc = new waypointQueue(newLoc, depth + 1);
        else //we're not at the end of the queue, so pass along the newLoc
            nextLoc.addWaypoint(newLoc);
    }

    // toArray converts the entire object to an array of MapLocations
    public MapLocation[] toArray() {
        MapLocation[] waypointArray = new MapLocation[nextLoc.getDepth()];
        return this.toArray(waypointArray);
    }
    public MapLocation[] toArray(MapLocation[] waypointArray) {
        if(nextLoc == null)
            waypointArray[depth - 1] = loc;
        else {
            waypointArray = nextLoc.toArray(waypointArray);
            waypointArray[depth - 1] = loc;
        }
        return waypointArray;
    }


    public int getDepth() {
        if(nextLoc == null)
            return depth;
        else
            return nextLoc.getDepth();
    }

    public String toString() {
        MapLocation[] waypointArray = this.toArray();
        String outputString = "";
        for(int i = 0; i < waypointArray.length; i++) {
            outputString += "(" + waypointArray[i].x + ", " + waypointArray[i].y + "), ";
        }
        outputString += "\r\n";
        return outputString;
    }

    public static void main(String [] args) {
        MapLocation first = new MapLocation(1,2);
        MapLocation second = new MapLocation(2,3);
        MapLocation third = new MapLocation (3,4);
        waypointQueue wq = new waypointQueue(first);
        wq.addWaypoint(second);
        wq.addWaypoint(third);
        System.out.println("Depth test:" + (wq.getDepth() == 3));
        System.out.println("String: " + wq.toString());
    }
}
