package team017;

import battlecode.common.*;

/** The example functions player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously.
 */


public class RobotPlayer {
    // allow referring to directions with numbers
    public static final int[][] deltaLocation = {{0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1} };
    public static final int EMPTYMESSAGE = 0;
    public static final int FIRSTMESSAGEPARTITION = 1000;
    public static final int LASTMESSAGEPARTITION = 10000;
    public static final int ALARMDISTANCESQUARED = 100;
    public static boolean initiated = false;

    public static RobotController rc;

    public static void run(RobotController _rc) {
        // initialize constants
        rc = _rc;

        // run any init methods
        if(!initiated) {
            if(rc.isActive()) {
                try {
                    switch (rc.getType()) {
                        case HQ:
                            HQ.init();
                            break;
                        case SOLDIER:
                            Soldier.init();
                            break;
                        case MEDBAY:
                        case SHIELDS:
                        case GENERATOR:
                        case SUPPLIER:
                            EncampmentCode.init();
                            break;
                        case ARTILLERY:
                            ArtilleryCode.init();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // now run indefinitely

        while (true) {
            try {
                switch (rc.getType()) {
                    case HQ:
                        HQ.hqCode();
                        break;
                    case SOLDIER:
                        Soldier.soldierCode();
                        break;
                    case MEDBAY:
                    case SHIELDS:
                    case GENERATOR:
                    case SUPPLIER:
                        EncampmentCode.run();
                        break;
                    case ARTILLERY:
                        ArtilleryCode.run();
                        break;
                }

                // End turn
                rc.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int cycleFrequency() {
        return cycleFrequency(Clock.getRoundNum());
    }
    public static int cycleFrequency(int roundNum) {
        return ((int)((roundNum + 1) * 7141.592)) % GameConstants.BROADCAST_MAX_CHANNELS;
    }
    public static void main(String [] args) {



    }

}