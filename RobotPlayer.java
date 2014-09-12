package team017;

import battlecode.common.*;

import java.util.Map;

/** The example functions player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously.
 */


public class RobotPlayer {
    // allow referring to directions with numbers
    private static final int NUMPERWAVE = 4;

    private static MapLocation oldMapLocation;
    private static RobotController rc;

    private static MapLocation rallyPoint;
    private static MapLocation enemyHQLoc;
    private static MapLocation ourHQLoc;
    private static Direction lastSpawnedDir = Direction.NORTH;

    private static boolean sent = false; // has the bot been sent to the enemy base yet
    private static Object location;

    private static int[][] deltaLocation = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1} };

    private static double findPathMineWeight = 1;


    private static void findRallyPoint() {
        int x = (5 * ourHQLoc.x + enemyHQLoc.x) / 6;
        int y = (5 * ourHQLoc.y + enemyHQLoc.y) / 6;
        rallyPoint = new MapLocation(x,y);
    }

    public static void run(RobotController _rc) {
        // initialize constants
        rc = _rc;
        enemyHQLoc = rc.senseEnemyHQLocation();
        ourHQLoc = rc.senseHQLocation();
        findRallyPoint();

        while (true) {
            try {
                if (rc.getType() == RobotType.HQ) {
                    hqCode();
                } else if (rc.getType() == RobotType.SOLDIER) {
                    if (rc.isActive()) {
                        soldierCode();
                    }

                }

                // End turn
                rc.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void hqCode() {
        if(Clock.getRoundNum() % 150 == 0) {
            findPath(4);
            System.out.println("findpath");
        }
        if (rc.isActive()) {
            Direction spawnDirection = hqSpawnDirection();

            lastSpawnedDir = lastSpawnedDir.rotateRight();
            spawnDirection = lastSpawnedDir;
            if (!spawnDirection.equals(Direction.NONE) && rc.canMove(spawnDirection)) {
                try {
                    rc.spawn(spawnDirection);
                } catch (GameActionException e) {
                    System.out.println("HQ tried to spawn in an invalid direction");
                    e.printStackTrace();
                }
            }

        }
        Robot[] friendlyBots = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam());
        int broadcastMessage = 0;
        if(friendlyBots.length > NUMPERWAVE) {
            broadcastMessage = 1;
        }
        try {
            rc.broadcast(0, broadcastMessage);  //signal the robots to gather at waypoint
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
    public static Direction hqSpawnDirection() {
        // Determine the best direction to spawn a soldier
        Direction enemyDirection = ourHQLoc.directionTo(enemyHQLoc);
        Direction spawnDirection = enemyDirection;
        boolean directionFound = false;
        while(!directionFound && !(spawnDirection.equals(enemyDirection.opposite()))){
            if(rc.canMove(spawnDirection)) {
                directionFound = true;
            }
            else {
                spawnDirection = spawnDirection.rotateRight();
            }
        }
        if(!directionFound) {
            spawnDirection = enemyDirection.rotateLeft();
            while (!directionFound && !(spawnDirection.equals(enemyDirection.opposite()))) {
                if (rc.canMove(spawnDirection)) directionFound = true;
                else {
                    spawnDirection = spawnDirection.rotateLeft();
                }
            }
        }
        if(!directionFound) {
            if(rc.canMove(enemyDirection.opposite())) {
                directionFound = true;
            }
            else {
                //System.out.println("There is nowhere to spawn a robot");
                spawnDirection = Direction.NONE;
            }
        }
        //System.out.println("HQ spawning in direction: " + spawnDirection.toString());
        return spawnDirection;

    }



    public static void soldierCode() {
        int message = 1;
        if (!sent) {
            try {
                message = rc.readBroadcast(0);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if(message == 1) {
            sent = true;
            goTo(enemyHQLoc);
        }
        else
            goTo(rallyPoint);
    }

    public static void goTo(MapLocation destination) {
        int dist = rc.getLocation().distanceSquaredTo(destination);
        if (rc.isActive() && dist > 0) {
            Direction dir = rc.getLocation().directionTo(destination);
            int[] directionOffsets = {0, 1, -1, 2, -2};
            lookAround:
            for (int d : directionOffsets) {
                Direction currentDir = Direction.values()[(dir.ordinal() + d + 8) % 8];
                if (rc.canMove(currentDir)) {
                    MapLocation targetPosition = new MapLocation(rc.getLocation().x + deltaLocation[currentDir.ordinal()][0], rc.getLocation().y + deltaLocation[currentDir.ordinal()][1]);
                    Team sensedMineTeam = rc.senseMine(targetPosition);
                    if (sensedMineTeam == null || sensedMineTeam.equals(rc.getTeam())) { // the way is clear. Time to move
                        try {
                            rc.move(currentDir);
                        } catch (GameActionException e) {
                            System.out.println("Exception: can't move");
                            e.printStackTrace();
                        }
                        break lookAround;
                    }
                    else {
                        if (sensedMineTeam.equals(rc.getTeam().opponent()) || sensedMineTeam.equals(Team.NEUTRAL)) {
                            try {
                                rc.defuseMine(targetPosition);
                            } catch (GameActionException e) {
                                e.printStackTrace();
                            }
                            //System.out.println("defusing a mine");
                        }
                        break lookAround;
                    }
                }
            }
        }
    }

    public static void findPath(int bubbleRadius) {
        int gridSize = (int) (1.3 * bubbleRadius + .5); // first an approximation of the size of the grid squares
        int myGridWidth = (int)(rc.getMapWidth() / gridSize + .5); // now figure out the dimensions of my pathfinding array
        int myGridHeight = (int)(rc.getMapHeight() / gridSize + .5);


        int halfGrid = (int) gridSize / 2;
        while(myGridHeight * (gridSize - 1) + halfGrid > rc.getMapHeight()) {  // in the case that adding the halfgrid takes me off the edge of the map, decrease height
            myGridHeight--;
            System.out.println("findPath:myGridHeight was too large... adjusting");
        }
        while(myGridWidth * (gridSize - 1) + halfGrid > rc.getMapWidth()) {  // in the case that adding the halfgrid takes me off the edge of the map, decrease height
            myGridHeight--;
            System.out.println("findPath:myGridHeight was too large... adjusting");
        }


        int[][] numMines = new int[myGridWidth][myGridHeight];
        for(int i = 0; i < myGridWidth; i++) {
            for(int j = 0; j < myGridHeight; j++) {
                MapLocation[] mines = rc.senseMineLocations(new MapLocation(i*gridSize + halfGrid, j*gridSize + halfGrid), bubbleRadius, Team.NEUTRAL);
                numMines[i][j] = mines.length;
            }
        }
        int[][] gridWeight = new int[myGridWidth][myGridHeight];    // takes the number of mines and distance into account
        for(int i=0; i < myGridWidth; i++) {
            for(int j = 0; j < myGridHeight; j++) {
                gridWeight[i][j] = (int) (gridSize + findPathMineWeight*numMines[i][j]);
            }
        }


        /*System.out.println("Mapsize: " + rc.getMapWidth() + " x " + rc.getMapHeight());
        System.out.println("Gridsize: " + gridSize);
        System.out.println("HalfGrid: " + halfGrid);
        System.out.println("GridDimensions: " + myGridWidth + " x " + myGridHeight);
        System.out.println("Mine Detection");
        for(int i = 0; i < myGridWidth; i++) {
            for(int j = 0;j < myGridHeight; j++) {
                System.out.print("" + numMines[i][j] + "  ");
            }
            System.out.println();
        }
        System.out.println("Coordinate locations:");
        for(int i = 0; i < myGridWidth; i++) {
            for(int j = 0;j < myGridHeight; j++) {
                System.out.print("(" + (i*gridSize + halfGrid) + "," + (j*gridSize + halfGrid) + ")  ");
            }
            System.out.println();
        }
        */

    }


}