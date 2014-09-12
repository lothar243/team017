package team017;

import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/12/2014.
 */
public class PathQueue {    //use insertion sort to generate a queue while executing the breadth first search
    public PathQueue nextItem = null;
    public int[] curCoords; // where would this path have them end up?
    public boolean[][] visited; // no use checking backtracking
    public int totalWeight; // the total weight of moving here
    public waypointQueue totalPath;

    PathQueue(PathQueue _nextItem, int[] _curCoords, boolean[][] previouslyVisited, int _totalWeight, waypointQueue _previousPath) {
        nextItem = _nextItem;
        curCoords = new int[]{_curCoords[0], _curCoords[1]};
        visited = new boolean[previouslyVisited.length][previouslyVisited[0].length];
        for(int i = 0; i < visited.length; i++) {
            for(int j = 0; j < visited[0].length; j++) {
                visited[i][j] = previouslyVisited[i][j];
            }
        }
        visited[curCoords[0]][curCoords[1]] = true;
        totalWeight = _totalWeight;
        totalPath = new waypointQueue(_previousPath);
        totalPath.addWaypoint(new MapLocation(curCoords[0],curCoords[1]));
    }

    public void addItem(PathQueue newItem) {
        if(nextItem == null)
            nextItem = newItem;
        else {
            if(nextItem.totalWeight > newItem.totalWeight) { // we've found where the newItem goes... inserting into queue
                newItem.nextItem = nextItem;
                nextItem = newItem;
            }
            else {
                nextItem.addItem(newItem); // passing along the add to the next item
            }
        }
    }
    public String toString() {
        if(nextItem == null)
            return curCoords.toString();
        else
            return curCoords.toString() + nextItem.toString();
    }

    public static void main(String [] args) {
        int[][] weighting = new int[][]{    {0, 0, 0, 0},
                                            {0, 1, 0, 1},
                                            {0, 1, 0, 1},
                                            {0, 1, 0, 0} };

        int[]curCoords = new int[]{0,0};
        boolean[][] previouslyVisited = new boolean[4][4];
        int totalWeight = 0;
        waypointQueue previousPath = null;
        PathQueue pq = new PathQueue(null, curCoords, previouslyVisited, 0, previousPath);
        PathQueue pq1 = new PathQueue(null, curCoords, previouslyVisited, 2, previousPath);
        PathQueue pq2 = new PathQueue(null, curCoords, previouslyVisited, 1, previousPath);



        pq.addItem(pq1);
        pq.addItem(pq2);
        System.out.println(pq.toString());

    }
}
