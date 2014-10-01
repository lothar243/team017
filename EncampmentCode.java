package team017;

/**
 * Created by Jeff on 9/29/2014.
 */
public class EncampmentCode extends RobotPlayer {
    public static void run() {
        listenToBroadcast();
    }

    public static void init() {
        initiated = true;
    }

    public static void listenToBroadcast() {
        try {
            int receivedMessage = rc.readBroadcast(cycleFrequency());
            if(receivedMessage != 0) {
                int[] interpretedMessage = BroadcastMessage.parseFirstMessage(receivedMessage);
                if (interpretedMessage == null) {

                } else if (interpretedMessage.length == 0) {
                } else if (interpretedMessage[1] == 3) {
                    // query message received
                    BroadcastMessage newMessage = BroadcastMessage.updateQueryMessage(interpretedMessage, rc.getType());
                    rc.broadcast(cycleFrequency(), newMessage.message[0]);
                }
            }
        }
        catch(Exception e) {}
    }
}
