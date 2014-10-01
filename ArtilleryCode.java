package team017;

import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotType;

/**
 * Created by Jeff on 9/30/2014.
 */
public class ArtilleryCode extends RobotPlayer{
    private static MapLocation[][] map;

    public static void init() {
        initiated = true;
        map = new MapLocation[rc.getMapWidth()][rc.getMapHeight()];
    }

    public static void run() {
        if(rc.isActive()) {
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, RobotType.ARTILLERY.attackRadiusMaxSquared, rc.getTeam().opponent());
            if (nearbyEnemies.length > 0) {
                MapLocation enemySquare = null;
                try {
                    enemySquare = rc.senseRobotInfo(nearbyEnemies[0]).location;
                    if (enemySquare != null && rc.canAttackSquare(enemySquare)) {
                        rc.attackSquare(enemySquare);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
