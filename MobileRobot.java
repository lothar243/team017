package team017;

import battlecode.common.*;


/**
 * Created by Jeff on 9/12/2014.
 */
public class MobileRobot extends RobotPlayer{
    public static int currentWayPoint = -1;     //set this to zero only after waypoints is initialized
    public static MapLocation[] waypoints;
    private static int WAYPOINTTOLERANCE = 8;
    public static boolean canDefuse = true;
    public static int maxPatiece = 20;
    public static int patience = 10;
    public static Direction lastTraveled;

    public static void goTo(MapLocation destination) {
        if(rc.isActive()) {
            if(destination != null) {
                int dist = rc.getLocation().distanceSquaredTo(destination);
                if (rc.isActive() && dist > 0) {
                    Direction dir = rc.getLocation().directionTo(destination);
                    move(dir);
                }
            }
            else
                System.out.println("Warning, tried to go to null");
        }
    }

    public static void move(Direction dir) {
        int[] directionOffsets = {0, 1, -1, 2, -2};
        boolean canMove = false;
        lookAround:
        for (int d : directionOffsets) {
            Direction currentDir = Direction.values()[(dir.ordinal() + d + 8) % 8];
            if (rc.canMove(currentDir)) {
                canMove = true;
                MapLocation targetPosition = new MapLocation(rc.getLocation().x + deltaLocation[currentDir.ordinal()][0], rc.getLocation().y + deltaLocation[currentDir.ordinal()][1]);
                Team sensedMineTeam = rc.senseMine(targetPosition);
                if (sensedMineTeam == null || sensedMineTeam.equals(rc.getTeam())) { // the way is clear. Time to move
                    try {
                        lastTraveled = currentDir;
                        rc.move(currentDir);
                        patience = 10;
                    } catch (GameActionException e) {
                        System.out.println("Exception: can't move");
                        e.printStackTrace();
                    }
                    patience = maxPatiece;
                    break lookAround;
                }
                else if (canDefuse || patience < 1) {
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
                else {
                    patience--;
                }
            }
        }
        if(!canMove) {
            patience--;
        }
    }

    public static void followWayPoints() { // always between currentWayPoint and currentWaypoint + 1
        if(waypoints[currentWayPoint] == null) {
            // something bad happened. running headlong into enemy
            currentWayPoint = 0;
            waypoints[0] = rc.senseEnemyHQLocation();
        }
        if(currentWayPoint >= 0) {
            if((rc.getLocation().distanceSquaredTo(waypoints[currentWayPoint]) <= WAYPOINTTOLERANCE || patience <= 0) && currentWayPoint + 1 < waypoints.length) { //time to move on to the next waypoint if we're not already done
                currentWayPoint++;
            }
            goTo(waypoints[currentWayPoint]);
        }

    }
    public static void backtrack() {
        if(currentWayPoint > 0) {
            if(rc.getLocation().distanceSquaredTo(waypoints[currentWayPoint-1]) <= WAYPOINTTOLERANCE) {
                currentWayPoint--;
            }
            else {
                goTo(waypoints[currentWayPoint-1]);
            }
        }
    }

}
