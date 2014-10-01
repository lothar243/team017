package team017;


import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/12/2014.
 */
public class BreadthFirstSearch {

    public static boolean DEBUG = false;
    public static int totalDistance = 100;

    public static TreeNode findPath(MapLocation start, MapLocation destination, int[][] weight) {
        TreeNode[][] myPaths = new TreeNode[weight.length][weight[0].length];
        myPaths[start.x][start.y] = new TreeNode(start, start.directionTo(destination));
        SortedQueue myQueue = new SortedQueue(start);
        int[] searchPattern = {0, 1, -1, 2, -2, 3, -3};

        TreeNode finalPath = null;
        if(start.equals(destination)) {
            return new TreeNode(start, Direction.NONE);
        }
        if(DEBUG) System.out.println("Test 10");

        while(myQueue.queueItem != null && finalPath == null) {
            if(DEBUG) System.out.println("Test 20");
            // pop the shortest existing path
            QueueItem current = myQueue.pop();
            TreeNode currentNode = myPaths[current.coord.x][current.coord.y];
            for(int directionChange: searchPattern) { // try continuing in the same direction first
                if(DEBUG) System.out.println("Test 30");
                Direction newDirection = Direction.values()[(currentNode.directionToGetHere.ordinal() + directionChange + 8) % 8];
                MapLocation newCoord = current.coord.add(newDirection);
                if(newCoord.x >= 0 && newCoord.x < weight.length && newCoord.y >= 0 && newCoord.y < weight[0].length) { // the new coordinate is valid
                    if(DEBUG) System.out.println("Test 40");
                    if (myPaths[newCoord.x][newCoord.y] == null) { // we've found the shorted path to this location, so adding it to the queue and updating myPaths
                        if(DEBUG) System.out.println("Test 50");
                        myPaths[newCoord.x][newCoord.y] = new TreeNode(newCoord, currentNode, newDirection);
                        if(DEBUG) System.out.println("Test 55");
                        myQueue.push(newCoord, current.weight + weight[newCoord.x][newCoord.y]);
                    }
                    if (newCoord.equals(destination)) { // we've found the shortest path to the destination
                        if(DEBUG) System.out.println("Test 60");
                        totalDistance = current.weight + weight[newCoord.x][newCoord.y];
                        finalPath = myPaths[newCoord.x][newCoord.y];
                    }
                }
            }
        }
        if(finalPath == null) {
            System.out.println("Error, unable to find a path");
            return null;
        }
        else {
            return finalPath;
        }
    }

    public static MapLocation[] shortenedPath(MapLocation start, MapLocation destination, int[][] weight) {
        TreeNode pathBeforeShortening = findPath(start, destination, weight);
        return pathBeforeShortening.removeRedundantNodes().toArray();
    }

    public static void main(String [] args) {
        int[][] weighting = new int[][]{    {1,  10,  1,  1,  1,  1},
                                            {1,  10, 1,  10, 1,  10},
                                            {1,  1,  10, 10,  1,  1},
                                            {1,  10, 10,  1,  1,  1} };
        MapLocation startLoc = new MapLocation(0,0);
        MapLocation destLoc = new MapLocation(3,3);

        if(DEBUG) System.out.println("Test 0");

        System.out.println("Finding a path through :" + "\n" +
                Test.arrayToFormatedString(weighting,3));
        TreeNode path = findPath(startLoc, destLoc, weighting);




        System.out.println("\tBefore shortening: \n" +
                Test.arrayToString(path.toArray()) + "\n" +
                "\tAfter shortening: \n" +
                Test.arrayToString(path.removeRedundantNodes().toArray()));

    }








    /*private static final int[][] deltaLocation = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1} };


    public static void main(String [] args) {
        int[] curLoc = new int[]{0,0};
        waypointQueue firstQueue = new waypointQueue(0,0);
        waypointQueue secondQueue = new waypointQueue(0,2);
        waypointQueue thirdQueue = new waypointQueue(2,0);
        BreadthFirstSearch bfs = new BreadthFirstSearch(curLoc,firstQueue,3);
        System.out.println(bfs.toString());
        System.out.println("---------------");

        bfs.insertIntoList(curLoc,secondQueue,2);
        System.out.println(bfs.toString());
        System.out.println("---------------");

        bfs.insertIntoList(curLoc,thirdQueue,4);
        System.out.println(bfs.toString());
        System.out.println("---------------");

        bfs.insertIntoList(curLoc,thirdQueue,3);
        System.out.println(bfs.toString());
        System.out.println("---------------");

        bfs.insertIntoList(curLoc,secondQueue,2);
        System.out.println(bfs.toString());
        System.out.println("---------------");

        int[][] weighting = new int[][]{    {1,  10,  1,  1,  1,  1},
                                            {1,  10, 1,  10, 1,  10},
                                            {1,  1,  10, 10,  1,  1},
                                            {1,  10, 10,  1,  1,  1} };
        int[] startLoc = new int[]{0,0};
        int[] destLoc = new int[]{3,3};
        waypointQueue myList = calculateAttackPath(startLoc, destLoc, weighting);
        System.out.println("Done. Shortest path:" + myList.toString());

        System.out.println("Now converting to array: ");
        int[][] myListToArray = myList.toArray();
        Test.printArray(myListToArray);



    }

    public static waypointQueue calculateAttackPath(int[] start, int[] destination, int[][] weight) { // careful: the start and destination are gridnumbers, not map locations
        if(start == null || destination == null || weight == null) {
            System.out.println("Error, findpath was passed a null value");
            return null;
        }
        //System.out.println("Processing:");
        //Test.printArray(weight);
        //System.out.println("Going from " + Test.arrayToString(start) + " to " + Test.arrayToString(destination));
        waypointQueue emptyQueue = new waypointQueue(start[0], start[1]);
        BreadthFirstSearch mySearch = new BreadthFirstSearch(start,emptyQueue);
        boolean destinationReached = false;
        boolean[][] visited = new boolean[weight.length][weight[0].length];
        for(int i = 0; i < visited.length;i++) {
            for(int j = 0; j < visited[i].length; j++) {
                visited[i][j] = false;
            }
        }
        waypointQueue shortestPath = null;

        while(!destinationReached) {
            //pop the first element
            //System.out.println("Popping: " + mySearch.pathFollowed.toString());
            if(mySearch == null) {
                System.out.println("Error, couldn't find the path");
                Test.printArray(weight);
                System.out.println("Going from " + Test.arrayToString(start) + " to " + Test.arrayToString(destination));

            }
            BreadthFirstSearch curBFS = new BreadthFirstSearch(mySearch.curLocation,mySearch.pathFollowed,mySearch.weight);
            mySearch = mySearch.nextItem;

            //is it the destination?
            if(curBFS.curLocation[0] == destination[0] && curBFS.curLocation[1]== destination[1]) {
                destinationReached = true;
                shortestPath = curBFS.pathFollowed;
            }
            else {
                //extend the paths
                for(int i = 0; i < 8; i++) {
                    //only use the path extensions that remain on the map
                    int[] checking = new int[]{curBFS.curLocation[0] + deltaLocation[i][0], curBFS.curLocation[1] + deltaLocation[i][1]};
                    if(0 <= checking[0] && checking[0] < weight.length && 0 <= checking[1] && checking[1] < weight[0].length) { // in range of map
                        if(!visited[checking[0]][checking[1]]) {
                            visited[checking[0]][checking[1]] = true;
                            //add each new direction to the queue
                            waypointQueue newPathFollowed = new waypointQueue(curBFS.pathFollowed, checking[0], checking[1]);
                            int newWeight = curBFS.weight + weight[checking[0]][checking[1]];
                            if (mySearch == null)
                                mySearch = new BreadthFirstSearch(checking, newPathFollowed, newWeight);
                            else
                                mySearch.insertIntoList(checking, newPathFollowed, newWeight);
                        }
                    }
                }
            }
            //System.out.println("Done considering " + curBFS.pathFollowed.toString() + " with weight " + curBFS.weight);
            //System.out.println("Tree size: " + mySearch.sizeOfTree());


        }


        return shortestPath;
    }

    public int sizeOfTree() {
        if(nextItem == null) {
            return 1;

        }
        else {
            return nextItem.sizeOfTree() + 1;
        }
    }

    waypointQueue pathFollowed;
    BreadthFirstSearch nextItem;
    int weight;
    int[] curLocation;
    public BreadthFirstSearch (int[] _curLocation, waypointQueue _pathfollowed) {
        this(_curLocation,_pathfollowed,0);
        curLocation = _curLocation;
    }
    public BreadthFirstSearch (int[] _curLocation, waypointQueue _pathfollowed, int _weight) {
        pathFollowed = _pathfollowed;
        nextItem = null;
        weight = _weight;
        curLocation = _curLocation;
    }

    public void insertIntoList(int[] _curLocation, waypointQueue _pathfollowed, int _weight) {
        if(curLocation[0] == _curLocation[0] && curLocation[1] == _curLocation[1]) {
            if(weight > _weight) { //replace the current item with the one being added, else nothing
                pathFollowed = _pathfollowed;
                weight = _weight;
            }

        }
        else if(weight > _weight) { // replace the existing item with the one being added, pushing this one down the list
            BreadthFirstSearch temp = new BreadthFirstSearch(_curLocation, pathFollowed, weight);
            temp.nextItem = nextItem;
            nextItem = temp;
            pathFollowed = _pathfollowed;
            weight = _weight;
            curLocation = _curLocation;
        }
        else {
            if(nextItem == null) {
                nextItem = new BreadthFirstSearch(_curLocation, _pathfollowed,_weight);
            }
            else {
                nextItem.insertIntoList(_curLocation, _pathfollowed, _weight);
            }
        }
    }

    public String toString() {
        String outputString;
        if( nextItem == null ) {
            outputString = pathFollowed.toString() + ", weight: " + weight + "\n\r";
        }
        else {
            outputString = pathFollowed.toString() + ", weight: " + weight + "\n\r" + nextItem.toString();
        }
        return outputString;
    }
    */
}
