package team017;

/**
 * Created by Jeff on 9/12/2014.
 */
public class PathQueue {    //use insertion sort to generate a queue while executing the breadth first search
    public PathQueue nextItem = null;
    public int[] curCoords; // where would this path have them end up?
    public boolean[][] visited; // no use checking backtracking
    public int totalWeight; // the total weight of moving here

    PathQueue(PathQueue _nextItem, int[] _curCoords, boolean[][] previouslyVisited, int _totalWeight) {
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


    public static void main(String [] args) {
        int[][] weighting = new int[][]{    {0, 0, 0, 0},
                                            {0, 1, 0, 1},
                                            {0, 1, 0, 1},
                                            {0, 1, 0, 0} };

        PathQueue pq = new PathQueue(null, new int[]{0,0}, )
    }
}
