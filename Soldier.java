package team017;

import battlecode.common.*;


/**
 * Created by Jeff on 9/12/2014.
 */
public class Soldier extends MobileRobot{
    private static boolean BROADCASTINGDEBUG = false;
    private static int roundsToListenToBroadcast = -1;
    public static int sendRound = 0;
    public static final int ROUNDSTOLAYMINE = 25;
    public static final int SIGHTRANGESQUARED = 20;
    public static int mode = 0; // 0: listening, 1: attacking, 2:settling
    private static BroadcastMessage broadcastMessage;
    private static int roundsSinceCombat = 1000;
    public static int backtracking = 0;
    public static MapLocation HQLoc;


    public static void init() {
        HQLoc = rc.senseHQLocation();
        if(HQLoc != null) {
            initiated = true;
        }
    }

    public static void soldierCode() {
        listenToBroadcast();
        if(rc.isActive()) {
            Robot[] nearbyEnemies;
            if(backtracking < 1) {
                nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 8, rc.getTeam().opponent());
            }
            else {
                nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 16, rc.getTeam().opponent());
                backtracking--;
            }

            //todo fix backtracking

            if (nearbyEnemies.length > 0) {  // respond to the enemy nearby
                canDefuse = false;
                roundsSinceCombat = 0;
                attack(nearbyEnemies);
            }
            else {
                roundsSinceCombat++;
                canDefuse = true;

                if (currentWayPoint == -1) {
                    improveHQ();
                } else if(Clock.getRoundNum() + ROUNDSTOLAYMINE < sendRound) { // there's time for another mine potentially
                    improveHQ();
                }
                else if(mode == 1){  // time to go
                    if(sendRound <= Clock.getRoundNum()) {
                        advanceAlongWayPoints();
                    }
                    else if(sendRound - GameConstants.MINE_LAY_DELAY <= Clock.getRoundNum()) {
                        improveHQ(); //still time to lay another mine potentially
                    }
                }
                else if(mode == 2 || mode == 3) { //heading to an encampment
                    if(patience < 1) {
                        roundsToListenToBroadcast = -1;
                        mode = 0;
                    }
                    settleEncampment();
                    if(Clock.getRoundNum() - sendRound > 49) {
                        mode = 0;
                    }
                    //reportStatus();
                }
            }
        }
    }

    private static void settleEncampment() {
        if(rc.getLocation().equals(waypoints[0])) {
            try {
                if(mode==2) {
                    int[] decodedMessage = BroadcastMessage.parseFirstMessage(broadcastMessage.message[0]);
                    rc.captureEncampment(RobotType.values()[decodedMessage[2]]);
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        else {
            goTo(waypoints[0]);

        }
    }

    private static void improveHQ() {

        final int[] directionChange = new int[]{0,1,7};
        if(roundsToListenToBroadcast < 0) {
            Team mineTeam = rc.senseMine(rc.getLocation());

            if (mineTeam == null) {
                try {
                    rc.layMine();
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            } else {
                if(lastTraveled == null) {
                    move(Direction.values()[(int)(Math.random() * 8)]);
                }
                else {
                    move(Direction.values()[(lastTraveled.ordinal() + (int) (Math.random() * directionChange.length)) % 8]); // bots tend to travel straight here
                }
            }
        }
    }

    private static void attack(Robot [] nearbyEnemies) {
        Robot[] nearbyFriends = rc.senseNearbyGameObjects(Robot.class, 4, rc.getTeam());
        //System.out.println("sensing " + nearbyEnemies.length + " enemies and " + nearbyFriends.length + " friends");
        if(HQLoc == null) {
            HQLoc = rc.senseHQLocation();
            System.out.println("Soldier.init must not have been called...");
        }
        int numEnemySoldiers = 0;
        try {
            for (Robot enemy : nearbyEnemies) {
                RobotInfo enemyInfo = rc.senseRobotInfo(enemy);
                if (enemyInfo.type.equals(RobotType.SOLDIER)) {
                    numEnemySoldiers++;
                }
                if(enemyInfo.location.distanceSquaredTo(HQLoc) < ALARMDISTANCESQUARED) {
                    rc.broadcast(cycleFrequency() + 1, BroadcastMessage.encodeMapLoc(enemyInfo.location));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(nearbyFriends.length < nearbyEnemies.length) {
            backtracking = 5 - nearbyFriends.length;
        }
        else { //engage the enemy
            try {
                move(rc.getLocation().directionTo(rc.senseLocationOf(nearbyEnemies[0])));
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }



    }

    public static void reportStatus() {
        System.out.println(rc.getRobot().getID() + ": mode " + mode);
    }

    public static void advanceAlongWayPoints() {
        followWayPoints();
        if(waypoints != null && currentWayPoint >= waypoints.length) { //we've reached the destination
            System.out.println("waypoint reached");
            mode = 0;
            waypoints = null;
            currentWayPoint = -1;
        }
    }

    public static void listenToBroadcast() {
        // Example message for sending soldiers to attack
        // if rallyPoints has 5 objects, the message will have 5 elements: 1 + 3 + 1
        // 1, 1, 5
        // 0, maploc0, maploc1
        // 0, maploc2, maploc3
        // 0, maploc4
        // 0, sendround
        boolean DEBUG = false;
        try {
            int receivedMessage = rc.readBroadcast(cycleFrequency());
            if(receivedMessage != 0) {
                int[] interpretedMessage = BroadcastMessage.parseFirstMessage(receivedMessage);
                if (interpretedMessage == null) {

                } else if (interpretedMessage.length == 0) { //message is a continuation
                    if (mode == -1 && broadcastMessage != null) {
                        // continue listening to message
                        int messageIndex = BroadcastMessage.parseMapLocMessage(receivedMessage)[2];
                        if (broadcastMessage.index % BroadcastMessage.MAXMESSAGEWITHLOCATION != messageIndex) { // desync, we must have missed a round
                            System.out.println("Round: " + Clock.getRoundNum() + ", Soldier.listenToBroadcast warning: index and messageIndex don't match - messageIndex: " + messageIndex + ", index: " + broadcastMessage.index);
                            broadcastMessage = null;
                            mode = 0;
                        } else {
                            //System.out.println("Round: " + Clock.getRoundNum() + ", - messageIndex: " + messageIndex + ", index: " + broadcastMessage.index);
                            broadcastMessage.message[broadcastMessage.index++] = receivedMessage;
                        }
                    }
                } else if (interpretedMessage[1] == 1) {
                    // attack message received, start listening
                    if(mode == 0) {
                        mode = -1;
                        broadcastMessage = new BroadcastMessage();
                        broadcastMessage.message = new int[interpretedMessage[2]];
                        broadcastMessage.message[0] = receivedMessage;
                        broadcastMessage.firstTurnOfBroadcast = Clock.getRoundNum();
                        broadcastMessage.index = 1;
                    }
                } else if (interpretedMessage[1] == 2) {
                    // settle message received, taking this message and erasing it before anyone else responds if i'm not busy already
                    if(mode == 0) {
                        broadcastMessage = new BroadcastMessage();
                        broadcastMessage.message = new int[]{receivedMessage};
                        currentWayPoint = 0;
                        mode = 2;
                        sendRound = Clock.getRoundNum();
                        waypoints = new MapLocation[]{new MapLocation(interpretedMessage[4], interpretedMessage[5])};
                        rc.broadcast(cycleFrequency(),0);
                        if(DEBUG) System.out.println("Round " + Clock.getRoundNum() + ", Settling a " + RobotType.values()[interpretedMessage[2]] + " at " + waypoints[0]);
                    }
                } else if (interpretedMessage[1] == 3) {
                    // query message received
                    if(mode != 1 || roundsSinceCombat < 5) {  // only respond if not already sent so we get the bots to travel in waves
                        BroadcastMessage newMessage = BroadcastMessage.updateQueryMessage(interpretedMessage, rc.getType());
                        rc.broadcast(cycleFrequency(), newMessage.message[0]);
                    }
                }
            }
        }
        catch(Exception e) {}
        if(mode == -1 && broadcastMessage.message.length == broadcastMessage.index) {
            convertAttackMessageToWaypoints();
        }
    }

    private static void convertAttackMessageToWaypoints() {
        final boolean DEBUG = false;
        if(broadcastMessage != null && broadcastMessage.message.length > 1) {
            int[] firstMessage = BroadcastMessage.parseFirstMessage(broadcastMessage.message[0]);
            sendRound = firstMessage[3];
            waypoints = new MapLocation[2 * (broadcastMessage.message.length - 1)];
            MapLocation[] tempWaypoints;
            for (int i = 1; i < broadcastMessage.message.length; i++) {
                int[] parsedMessage = BroadcastMessage.parseMapLocMessage(broadcastMessage.message[i]);
                waypoints[2 * i - 2] = BroadcastMessage.decodeMapLoc(parsedMessage[0]);
                waypoints[2 * i - 1] = BroadcastMessage.decodeMapLoc(parsedMessage[1]);
            }
            mode = 1;
            currentWayPoint = 1;
            if (DEBUG) System.out.println("Commencing attack soon on " + waypoints[waypoints.length-1]);
        }
        else {
            System.out.println("Soldier.convertAttackMessageToWaypoings error: broadcastMessage was as expected");
        }
    }




}
