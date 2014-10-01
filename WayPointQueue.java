package team017;



/**
 * Created by Mr. Arends on 9/12/14.
 */
public class WayPointQueue {
    public int[] loc;
    public WayPointQueue nextLoc;
    public int depth;


    public WayPointQueue() {

    }
    public WayPointQueue(WayPointQueue wq) {
        loc = new int[]{wq.loc[0],wq.loc[1]};
        if(wq.nextLoc == null)
            nextLoc = null;
        else
            nextLoc = new WayPointQueue(wq.nextLoc);
        depth = wq.depth;
    }
    public WayPointQueue(WayPointQueue previousQueue, int xToAdd, int yToAdd) {
        //clone queue then add incoming x and y to end
        this(previousQueue);
        this.addWaypoint(new int[]{xToAdd, yToAdd});

    }
    public WayPointQueue(int[] _loc) {
        this(_loc, 1);
    }
    public WayPointQueue(int[] _loc, int _depth) {
        loc = _loc;
        depth = _depth;
    }
    public WayPointQueue(int x, int y) {
        this(new int[]{x,y},1);
    }

    public void addWaypoint(int[] newLoc) {  // add a map location to the end of the queue
        if(nextLoc == null) // we're at the end of the queue, so set the nextLoc to the one being added
            nextLoc = new WayPointQueue(newLoc, depth + 1);
        else //we're not at the end of the queue, so pass along the newLoc
            nextLoc.addWaypoint(newLoc);
    }

    // toArray converts the entire queue to an array of int[][]s
    public int[][] toArray() {
        int[][] waypointArray;
        //System.out.println("converting to array a queue of depth: " + this.getDepth());
        waypointArray = new int[this.getDepth()][2];
        //System.out.println("waypointQueue.toArray: created array of dimensions " + waypointArray.length + " by " + waypointArray[0].length);
        return this.toArray(waypointArray);
    }
/*    public int[][] toArray(int[][] waypointArray) {
        if(nextLoc == null) {
            waypointArray[waypointArray.length - depth][0] = loc[0];
            waypointArray[waypointArray.length - depth][1] = loc[1];
        }
        else {
            waypointArray = nextLoc.toArray(waypointArray);
            //System.out.println("waypointQueue.toArray: " + waypointArray.length +  " " + depth);
            waypointArray[waypointArray.length - depth][0] = loc[0];
            waypointArray[waypointArray.length - depth][1] = loc[1];
        }
        return waypointArray;
    }
    */
public int[][] toArray(int[][] waypointArray) {
    if(nextLoc == null) {
        waypointArray[depth-1][0] = loc[0];
        waypointArray[depth-1][1] = loc[1];
    }
    else {
        waypointArray = nextLoc.toArray(waypointArray);
        //System.out.println("waypointQueue.toArray: " + waypointArray.length +  " " + depth);
        waypointArray[depth-1][0] = loc[0];
        waypointArray[depth-1][1] = loc[1];
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
        String outputString = "";
        if(nextLoc == null)
            outputString = "(" + loc[0] + "," + loc[1] + "), ";
        else
            outputString = "(" + loc[0] + "," + loc[1] + "), " + nextLoc.toString();
        return outputString;
    }

    public static void main(String [] args) {
        int[] first = new int[]{1,2};
        int[] second = new int[]{2,3};
        int[] third = new int[] {3,4};
        WayPointQueue wq = new WayPointQueue(first);
        wq.addWaypoint(second);
        wq.addWaypoint(third);
        WayPointQueue wq2 = new WayPointQueue(wq);
        wq2.addWaypoint(new int[]{9,9});
        WayPointQueue wq3 = new WayPointQueue(wq, 4,5);

        System.out.println("wq:");
        System.out.println("Depth test:" + (wq.getDepth() == 3));
        System.out.println("String: " + wq.toString());
        System.out.println("ToArray to String: " + Test.arrayToString(wq.toArray()));

        System.out.println("wq2:");
        System.out.println("Depth test:" + (wq2.getDepth() == 4));
        System.out.println("String: " + wq2.toString());
        System.out.println("ToArray to String: " + Test.arrayToString(wq2.toArray()));

        System.out.println("wq3:");
        System.out.println("Depth test:" + (wq3.getDepth() == 4));
        System.out.println("String: " + wq3.toString());
        System.out.println("ToArray to String: \r" + Test.arrayToString(wq3.toArray()));

    }

}
