package team017;


import battlecode.common.Robot;

/**
 * Created by Jeff on 9/14/2014.
 */
public class RobotStack {
    Robot currentRobot;
    RobotStack nextRobot;

    public void addToStack(Robot _nextRobot) {
        if(currentRobot == null) {
            currentRobot = _nextRobot;
        }
        else {
            if(nextRobot==null) {
                nextRobot = new RobotStack(_nextRobot);
            }
            else {
                nextRobot.addToStack(_nextRobot);
            }
        }
    }

    public RobotStack(Robot _currentRobot) {
        currentRobot = _currentRobot;
    }
    public RobotStack() {

    }

    public Robot[] toArray() {
        int size = this.size();
        Robot[] output = new Robot[size];
        RobotStack rs = this;
        for(int i = 0; i < size; i++) {
            output[i] = rs.currentRobot;
            rs = rs.nextRobot;
        }
        return output;
    }

    public int size() {
        if(nextRobot==null) {
            return 1;
        }
        else {
            return nextRobot.size() + 1;
        }
    }
    public String toString() {
        Robot[] robots = this.toArray();
        String output = "";
        for(Robot rob: robots) {
            if(rob != null) {
                output += rob.getID() + ", ";
            }
            else {
                output += "null, ";
            }
        }
        return  output;
    }

    public Robot pop() {
        Robot output;
        output = currentRobot;
        if(nextRobot != null) {
            currentRobot = nextRobot.currentRobot;
            nextRobot = nextRobot.nextRobot;
        }
        else {
            currentRobot = null;
            nextRobot = null;
        }
        return output;
    }

}
