package team017;

/**
 * Created by Jeff on 9/12/2014.
 */
public class BreadthFirstSearch {
    public double flexibility = 1.2; // will compute all paths up to 1.2 * the length of the shortest
    private final int[][] deltaLocation = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1} };

    public static void main(String[] args) {

    }

    public int[][] waypointList(int[][] gridWeight, int[] startingLoc, int[] destLoc) {

        int width = gridWeight.length;
        int height = gridWeight[0].length;
        boolean destinationReached = false;
        PathQueue myPath = new PathQueue(null, startingLoc, new boolean[width][height], 0);

        while( !destinationReached ) {
            for(int i = 0;i < 8; i++) {
                int [] checkingCoord = new int[] {myPath.curCoords[0] + deltaLocation[i][0], myPath.curCoords[1] + deltaLocation[i][1]};
                if( checkingCoord[0] >= 0 && checkingCoord[0] < width && checkingCoord[1] >= 0 && checkingCoord[1] < height) {  // don't want to run off the edge of the map
                    if (!myPath.visited[checkingCoord[0]][checkingCoord[1]]) {
                        //
                    }
                }
            }
        }

        return null;
    }


}
