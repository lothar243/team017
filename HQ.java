package team017;

import battlecode.common.*;

/**
 * Created by Jeff on 9/12/2014.
 */
public class HQ extends RobotPlayer{

    private static MapLocation ourHQLoc;
    private static MapLocation enemyHQLoc;
    private static MapLocation[] waypoints;
    private static double mineWeightModifier = 7;
    public static MapLocation[] encampmentLocs;
    public static boolean[] encampmentSettled;
    private static BroadcastMessage messageToSend;
    private static int numSoldiersToSend = 10;
    private static boolean awaitingQueryResults = false;
    private static Robot[] nearbyEnemies;
    private static int lastMessageReceived;

    private static int[] gridDimensions;
    private static int gridSize;
    private static MapLocation[][] gridCenters;

    private static int timeBetweenScans = 150;
    private static int waveSendFrequency = 100;
    private static int[] numRobots;

    private static int[] nextGridToScan;
    private static int[][] numMines;
    private static int mostRecentScanRound = -9999;
    private static int mostRecentAttackRound = -9999;
    private static int sendToSettleFrequency = 50;
    private static int mostRecentSettleMessage = 0;


    private static boolean goForNuke = false;


    public static void init() {
        ourHQLoc = rc.senseHQLocation();
        enemyHQLoc = rc.senseEnemyHQLocation();
        gridSize = 3;
        lastMessageReceived = 0;

        //int numberOfLocs = rc.getMapHeight() * rc.getMapWidth();
        //gridSize = 1;
        //while(numberOfLocs / (gridSize*gridSize) > MAXNUMBEROFCELLS)
//            gridSize++;
        //bubbleSize = (int)(gridSize / 1.3);

        //System.out.println("Number of mines for radius 2: " + rc.senseMineLocations(new MapLocation(1,1), 2, Team.NEUTRAL).length);

        //gridDimensions = new int[]{rc.getMapWidth()/gridSize, rc.getMapHeight()/gridSize};
        gridDimensions = new int[]{rc.getMapWidth()/3, rc.getMapHeight()/3};
        gridCenters = new MapLocation[gridDimensions[0]][gridDimensions[1]];
        for(int i = 0; i < gridDimensions[0]; i++) {
            for(int j = 0; j < gridDimensions[1]; j++) {
                //gridCenters[i][j] = new MapLocation((rc.getMapWidth() * (2 * i + 1)) / (2 * gridDimensions[0]), (rc.getMapHeight() * (2 * j + 1)) / (2 * gridDimensions[1]));
                gridCenters[i][j] = new MapLocation(3 * i + 1, 3 * j + 1);
            }
        }
        if(ourHQLoc != null) {
            initiated = true;
        }

        /*System.out.println("Map size: " + rc.getMapWidth() + " x " + rc.getMapHeight());
        for (MapLocation[] gridCenter : gridCenters) {
            System.out.println(Test.arrayToString(gridCenter));
        }
        */
    }

    public static void hqSpawn() {
        final boolean DEBUG = false;
        if (rc.isActive()) {
            if(DEBUG) System.out.println("Spawning");
            Direction spawnDirection = hqSpawnDirection();
            if(spawnDirection != null) {
                try {
                    rc.spawn(spawnDirection);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static Direction hqSpawnDirection() {
        // Determine the best direction to spawn a soldier
        int[] directionOffsets = {0, 1, -1, 2, -2, 3, -3, 4};
        Direction spawnDirection;
        spawnDirection = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        Direction checkingDirection;
        MapLocation checkingLocation;
        boolean canPlace = false;
        lookaround:
        for(int i : directionOffsets) {
            checkingDirection = Direction.values()[(spawnDirection.ordinal() + i + 8) % 8];
            checkingLocation = new MapLocation(ourHQLoc.x + deltaLocation[checkingDirection.ordinal()][0], ourHQLoc.y + deltaLocation[checkingDirection.ordinal()][1]);
            Team sensedMineTeam = rc.senseMine(checkingLocation);
            if(rc.canMove(checkingDirection) && sensedMineTeam != Team.NEUTRAL) {
                spawnDirection = checkingDirection;
                canPlace = true;
                break lookaround;
            }
        }
        if(canPlace) {
            return spawnDirection;
        }
        else {
            return null;
        }

    }

    public static void hqTest() {

    }

    public static void hqCode() {
        //hqTest();
        boolean HQDEBUG = true;
        if(messageToSend != null) {        // continue broadcasts...
            if(messageToSend.index == -2) {
                try {
                    int queryResults = rc.readBroadcast(cycleFrequency(Clock.getRoundNum() - 1));
                    numRobots = BroadcastMessage.parseFirstMessage(queryResults);
                    if(numRobots == null || numRobots.length != 8) {
                        System.out.println("queryMessage didn't turn out as expected");
                        numRobots = null;
                        messageToSend = null;
                    }
                    else {
                        if(HQDEBUG) {
                            for (int i = 2; i < numRobots.length; i++) {
                                System.out.print(RobotType.values()[i - 1] + ": " + numRobots[i] + ", ");
                            }
                            System.out.println();

                        }
                        messageToSend = null;
                    }
                }
                catch(Exception e){
                    System.out.println("Exception caught, couldn't read a broadcast, resending");
                    messageToSend = BroadcastMessage.composeQueryMessage(); // error reading the results, trying to send again
                }
            }
            else if(messageToSend.index == -1) { // about to broadcast a query
                try {
                    rc.broadcast(cycleFrequency(), messageToSend.message[0]);
                    messageToSend.index = -2;
                }
                catch(Exception e){}
            }
            else {
                broadcastNextMessage();
            }
        }
        else {
            try {
                rc.broadcast(RobotPlayer.cycleFrequency(), EMPTYMESSAGE); // empty messageToSend
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        try {
            if (numRobots != null && numRobots[2] > 15 && mostRecentScanRound > mostRecentAttackRound || rc.checkResearchProgress(Upgrade.NUKE) > 100) {
                goForNuke = true;
                if (rc.isActive()) {
                    rc.researchUpgrade(Upgrade.NUKE);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        hqSpawn();
        if(messageToSend == null) {
            int newMessageReceived = 0;
            try{
                nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 49, rc.getTeam().opponent());
                newMessageReceived = rc.readBroadcast(cycleFrequency(Clock.getRoundNum() - 1) + 1);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(newMessageReceived != lastMessageReceived) {
                MapLocation enemyLoc = BroadcastMessage.decodeMapLoc(newMessageReceived);
                if(HQDEBUG) System.out.println("HQ received a report of nearby enemies, new: " + newMessageReceived + ", old: " + lastMessageReceived + ", that's at " + BroadcastMessage.decodeMapLoc(newMessageReceived));
                if (enemyLoc.distanceSquaredTo(ourHQLoc) < ALARMDISTANCESQUARED) {

                    messageToSend = BroadcastMessage.composeAttackMessage(new MapLocation[]{enemyLoc}, 0);
                    //todo for some reason no soldiers are responding to the attack message
                }
                else {
                    if(HQDEBUG) System.out.println("False alarm");
                }
                try {
                    lastMessageReceived = rc.readBroadcast(cycleFrequency() + 1);
                }
                catch(Exception e) {e.printStackTrace();}
            }
/*            if(false && nearbyEnemies != null && nearbyEnemies.length > 0) {
                MapLocation[] destination = null;
                try {
                    destination = new MapLocation[]{rc.senseRobotInfo(nearbyEnemies[0]).location};
                }
                catch (Exception e) {e.printStackTrace();}
                if(destination != null) {
                    messageToSend = BroadcastMessage.composeAttackMessage(destination,0);
                }
            } */
            else if(Clock.getRoundNum() % 50 == 0) {
                if(HQDEBUG) System.out.println("Round: " + Clock.getRoundNum() + ", sending query");
                messageToSend = BroadcastMessage.composeQueryMessage();
            }
            else if(Clock.getRoundNum() - mostRecentScanRound > timeBetweenScans) {
                if(HQDEBUG) System.out.println("Round: " + Clock.getRoundNum() + ", HQ calculating attack path");
                calculateAttackPath();
            }
            else if(Clock.getRoundNum() - mostRecentSettleMessage > sendToSettleFrequency) {
                MapLocation[] spotsToSettle = settleSpots();
                if(spotsToSettle.length > 0) {
                    Test.reportNullValuedArray(spotsToSettle);
                    Test.reportNullValuedArray(determineEncampmentTypesToSettle(spotsToSettle.length));
                    messageToSend = BroadcastMessage.composeSettleMessage(spotsToSettle, null, determineEncampmentTypesToSettle(spotsToSettle.length));
                }
                mostRecentSettleMessage = Clock.getRoundNum();
            }
            else if(numRobots != null && numRobots[2] >= attackGroupSize() && waypoints != null && mostRecentScanRound > mostRecentAttackRound && !goForNuke) {
                if(HQDEBUG) System.out.println("Round :" + Clock.getRoundNum() + ", Sending attack");
                messageToSend = BroadcastMessage.composeAttackMessage(waypoints, Clock.getRoundNum() + 20);
                mostRecentAttackRound = Clock.getRoundNum();
            }
        }

    }

    private static int attackGroupSize() {
        return BreadthFirstSearch.totalDistance / 16 + Clock.getRoundNum() / 400;
    }

    private static RobotType[] determineEncampmentTypesToSettle(int numNewEncampmentSpots) {
        if(numNewEncampmentSpots > 0) {
            int totalEncampmentsAfterThisSettle = numRobots[6] + numRobots[7] + numNewEncampmentSpots;
            int numNewGenerators = (numNewEncampmentSpots + 2) / 2 - numRobots[6];
            if(numNewGenerators < 0) numNewGenerators = 0;
            int numNewSuppliers = 0;
            int numNewArtillery = 0;
            RobotType[] output = new RobotType[numNewEncampmentSpots];
            for (int i = 0; i < numNewGenerators; i++) {
                output[i] = RobotType.GENERATOR;
            }
            if(goForNuke) {
                numNewArtillery = numNewEncampmentSpots - numNewGenerators;
                for(int i = 0; i < numNewArtillery; i++) {
                    output[i + numNewGenerators] = RobotType.ARTILLERY;
                }
            }
            else {
                numNewSuppliers = numNewEncampmentSpots - numNewGenerators;
                for (int i = 0; i < numNewSuppliers; i++) {
                    output[i + numNewGenerators] = RobotType.SUPPLIER;
                }
            }
            Test.reportNullValuedArray(output);
            return output;
        }
        return new RobotType[0];
    }


    private static int calculateTheNumberOfEncampmentsToSettle(int roundNum) {
        return BreadthFirstSearch.totalDistance * roundNum / 10000;
    }

    public static MapLocation[] settleSpots() {
        final boolean DEBUG = false;
        MapLocation[] settledLocations = rc.senseAlliedEncampmentSquares();
        if(encampmentLocs == null) {
            findEncampments();
        }
        encampmentSettled = new boolean[encampmentLocs.length];

        boolean encampmentFound;
        for(int i = 0; i < settledLocations.length; i++) {
            encampmentFound = false;
            for(int j = 0; j < encampmentLocs.length && !encampmentFound; j++) {
                if(settledLocations[i].equals(encampmentLocs[j])) {
                    encampmentSettled[j] = true;
                    encampmentFound = true;
                }
            }
        }
        int numToSend = Math.max(calculateTheNumberOfEncampmentsToSettle(Clock.getRoundNum()) - settledLocations.length, 0);
        MapLocation[] locationsToSettle = new MapLocation[numToSend];
        int numSent = 0;
        for(int i = 0; i < encampmentLocs.length && numSent < numToSend; i++) {
            if(!encampmentSettled[i]) {
                //System.out.println("settleLocations.length = " + settleLocations.length + ", numToSend: " + numToSend);
                //System.out.println("encampmentLocs[i] = " + encampmentLocs[i] + ", i: " + i);
                locationsToSettle[numSent++] = encampmentLocs[i];
            }
        }
        if(DEBUG) for(int i = 0; i < locationsToSettle.length; i++) {
            System.out.print(locationsToSettle[i] + ", ");
        }
        if(DEBUG) System.out.println();
        if(DEBUG) Test.reportNullValuedArray(locationsToSettle);
        return locationsToSettle;
    }

    public static void findEncampments() {
        MapLocation[] encampments = rc.senseAllEncampmentSquares();
        int[] distances = new int[encampments.length];
        for(int i = 0; i < encampments.length; i++) {
            distances[i] = rc.getLocation().distanceSquaredTo(encampments[i]);
        }

        MapLocationDistances myList = new MapLocationDistances(encampments,distances);
        myList.sortByDistance();
        encampmentLocs = myList.locations;
        encampmentSettled = new boolean[encampmentLocs.length];
    }

    private static void broadcastNextMessage() {

        if(messageToSend != null && messageToSend.index < messageToSend.message.length) { //there still a messageToSend to broadcast
            try {
                rc.broadcast(RobotPlayer.cycleFrequency(Clock.getRoundNum()), messageToSend.message[messageToSend.index]);
                boolean BROADCASTDEBUG = false;
                if(BROADCASTDEBUG) System.out.println("HQ broadcasting on round " + Clock.getRoundNum() + "," + messageToSend.index + ": " + messageToSend.message[messageToSend.index]);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            messageToSend.index++;
        }
        else if(++messageToSend.broadcastNumber  < messageToSend.timesToBroadcast) {
            messageToSend.index = 0;
        }
        else {
            messageToSend = null;
        }
    }

    public static void calculateAttackPath() {
        boolean PATHFINDINGDEBUG = false;
        //for pathfinding, we're approximating the map by a grid so that we can deal with large maps
        //first getting a scan of the map in terms of this grid
        if(mostRecentScanRound == 0 || Clock.getRoundNum() - mostRecentScanRound > timeBetweenScans) {
            numMines = new int[gridDimensions[0]][gridDimensions[1]];
            nextGridToScan = new int[2];
            mostRecentScanRound = Clock.getRoundNum();
        }
        else {
            if(PATHFINDINGDEBUG) System.out.println("HQ round " + Clock.getRoundNum() + ", resuming previous scan");
        }
        for(; nextGridToScan[0] < gridDimensions[0]; nextGridToScan[0]++) {
            for(; nextGridToScan[1] < gridDimensions[1]; nextGridToScan[1]++) {
                int bubbleSize = 2;
                MapLocation[] mines = rc.senseMineLocations(gridCenters[nextGridToScan[0]][nextGridToScan[1]], bubbleSize, Team.NEUTRAL);
                numMines[nextGridToScan[0]][nextGridToScan[1]] = mines.length;
            }
            nextGridToScan[1] = 0;
        }
        // now building a weight array that takes the number of mines and distance into account
        int[][] gridWeight = new int[gridDimensions[0]][gridDimensions[1]];
        for(int i=0; i < gridDimensions[0]; i++) {
            for(int j = 0; j < gridDimensions[1]; j++) {
                gridWeight[i][j] = (int) (gridSize + mineWeightModifier * numMines[i][j]);
            }
        }
        //determine which grid our HQ lies in
        MapLocation ourHQGridNum = new MapLocation((int)(ourHQLoc.x / gridSize), (int)(ourHQLoc.y / gridSize));
        if(ourHQGridNum.x >= gridDimensions[0])
            ourHQGridNum = new MapLocation(ourHQGridNum.x-1, ourHQGridNum.y);
        if(ourHQGridNum.y >= gridDimensions[1])
            ourHQGridNum = new MapLocation(ourHQGridNum.x, ourHQGridNum.y - 1);

        //and the enemy's HQ as well
        MapLocation enemyHQGridNum = new MapLocation(enemyHQLoc.x / gridSize, enemyHQLoc.y / gridSize);
        if(enemyHQGridNum.x >= gridDimensions[0])
            enemyHQGridNum = new MapLocation(enemyHQGridNum.x - 1, enemyHQGridNum.y);
        if(enemyHQGridNum.y >= gridDimensions[1])
            enemyHQGridNum = new MapLocation(enemyHQGridNum.x, enemyHQGridNum.y - 1);

        if(PATHFINDINGDEBUG) System.out.println("PathFinding: \n" + Test.arrayToFormatedString(gridWeight, 4));
        MapLocation[] gridRallyPoints = BreadthFirstSearch.shortenedPath(ourHQGridNum, enemyHQGridNum, gridWeight);
        // now we need to convert back from the coarser grid to the actual MapLocations for everything but the start and finish
        waypoints = new MapLocation[gridRallyPoints.length];
        for(int i = 1; i < gridRallyPoints.length - 1; i++) {
            waypoints[i] = centerOfGrid(gridRallyPoints[i], gridSize);
        }
        // And we will use the exact location of the start and the finish
        waypoints[0] = ourHQLoc;
        waypoints[waypoints.length-1] = enemyHQLoc;
        //System.out.println("Found fastest path: " + Test.arrayToString(waypoints));
        //System.out.println("From array + " + Test.arrayToString(gridRallyPoints));
        if(PATHFINDINGDEBUG) System.out.println("Ending with + \n" + Test.arrayToString(waypoints));
        if(PATHFINDINGDEBUG) System.out.println("For a total weighted distance of " + BreadthFirstSearch.totalDistance);



    }

    private static MapLocation centerOfGrid(MapLocation gridLoc, int gridSize) {
        return new MapLocation((int)((.5 + gridLoc.x)*gridSize ), (int)((.5 + gridLoc.y)*gridSize ));
    }

    public static void main(String [] args) {
    }

}
