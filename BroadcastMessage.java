package team017;

import battlecode.common.*;

/**
 * Created by Jeff on 9/27/2014.
 */
public class BroadcastMessage {
    int[] message;
    int index;
    int broadcastNumber = 0;
    int timesToBroadcast = 1;
    int firstTurnOfBroadcast;


    private static final int FIRSTMESSAGEPARTITION = 2;
    private static final int MESSAGETYPEPARTITION = 10;
    private static final int ATTACKMESSAGE_MESSAGELENGTHPARTITION = 200;
    private static final int ATTACKMESSAGE_SENDROUNDPARTITION = 10000;
    private static final int SETTLEMESSAGE_ENCAMPMENTTYPE_PARTITION = 5;
    private static final int SETTLEMESSAGE_MAXROBOTID = 4000;
    private static final int MAPLOCATION_PARTITION = 4900;
    private static final int MAXENCAMPMENTS = 17;
    private static final int MAXSOLDIERS = 60;
    public static final int MAXMESSAGEWITHLOCATION = 44;
    private static boolean DEBUG = true;

    private static boolean MESSAGEDEBUG = false;



    // Message type 1: attack
    // if rallyPoints has 5 objects then sendMessage will have 4 elements: 1 + 3
    //First message partition: 2, 10, 200, 10000
    // 1, 1, 4, sendRound
    // 0, maploc0, maploc1
    // 0, maploc2, maploc3
    // 0, maploc4

    /**
     *
     * @param waypoints An array of waypoints to create the message about
     * @param sendRound Intended to be able to send large waves of robots, rather than one at a time
     * @return Return a BroadcastMessage object that stores the current broadcast index and the entire message to be broadcast
     */
    public static BroadcastMessage composeAttackMessage(MapLocation[] waypoints, int sendRound) {
        if (waypoints == null) {
            System.out.println("BroadcastMessage.composeAttackMessage error: passed in null");
            return null;
        } else {
            BroadcastMessage output = new BroadcastMessage();
            int messageLength = (waypoints.length + 1) / 2 + 1;
            output.message = new int[messageLength];
            int[] preEncodedMessage = new int[]{1, 1, messageLength, sendRound};
            int[] attackMessagePartitions = new int[]{FIRSTMESSAGEPARTITION, MESSAGETYPEPARTITION, ATTACKMESSAGE_MESSAGELENGTHPARTITION, ATTACKMESSAGE_SENDROUNDPARTITION};
            output.message[0] = encodeMessage(preEncodedMessage, attackMessagePartitions);

            for (int i = 1; i < messageLength; i++) {
                // i = 1 -> 0 and 1
                // i = 2 -> 2 and 3
                // i = 3 -> 4
                if (2 * i > waypoints.length) { //we're at the end of the rallyPoints and we don't have enough for a pair, so just repeat the last waypoint
                    output.message[i] = encodeMapLocPairs(waypoints[2 * i - 2], waypoints[2 * i - 2], i);
                } else {
                    output.message[i] = encodeMapLocPairs(waypoints[2 * i - 2], waypoints[2 * i - 1], i);
                }
            }
            output.index = 0;
            return output;
        }

    }
    // 1, 2, encampmentType, robotId, encampmentLocation     Partitioned: 2, 10, 5, 4000, 4900
    /**
     * Create a message to send specific robots to specific encampment spots to settle
     * @param waypoints Location of encampment
     * @param robotIds Which robots to send (current max is 4000, so cycle.. id 4050 would be 50)
     * @param encampmentTypes Which encampments to settle
     * @return A bunch of individual message to individual robots
     */
    public static BroadcastMessage composeSettleMessage(MapLocation[] waypoints, int[] robotIds, RobotType[] encampmentTypes) {
        if(waypoints == null || encampmentTypes == null) {
            System.out.println("BroadcastMessage.composeSettleMessage error: null input");
            return null;
        }
        if(waypoints.length != encampmentTypes.length) {
            System.out.println("BroadcastMessage.composeSettleMessage error: input arrays are different lengths: " + waypoints.length + ", " + encampmentTypes.length);
            return null;
        }
        robotIds = new int[waypoints.length];
        BroadcastMessage output = new BroadcastMessage();
        output.message = new int[waypoints.length];
        output.index = 0;

        int mapLocInInt = 0;
        int encampmentTypeInInt = 0;
        int[] broadcastMessagePartitions = new int[]{FIRSTMESSAGEPARTITION, MESSAGETYPEPARTITION, SETTLEMESSAGE_ENCAMPMENTTYPE_PARTITION, SETTLEMESSAGE_MAXROBOTID, MAPLOCATION_PARTITION};
        for(int i = 0; i < waypoints.length; i++) {
            mapLocInInt = encodeMapLoc(waypoints[i]);
            robotIds[i] = robotIds[i] % SETTLEMESSAGE_MAXROBOTID;
            encampmentTypeInInt = encampmentTypes[i].ordinal() - 2;
            output.message[i] = encodeMessage(new int[]{1, 2, encampmentTypeInInt, robotIds[i], mapLocInInt}, broadcastMessagePartitions);
        }
        return output;

    }
    /**
     * Creates a scaffold message
     * @return see above
     */
    public static BroadcastMessage composeQueryMessage() {
        BroadcastMessage output = new BroadcastMessage();
        output.message = new int[]{1 + FIRSTMESSAGEPARTITION * 3};
        output.index = -1;
        return output;
    }
    /**
     * Only to be called by encampments. A query
     * @param decodedMessage pass in the output of parseFirstMessage
     * @param encampmentType the RobotType of this encampment
     * @return
     */
    public static BroadcastMessage updateQueryMessage(int[] decodedMessage, RobotType encampmentType) {
        int[] EncampmentReportPartitions = {FIRSTMESSAGEPARTITION, MESSAGETYPEPARTITION, MAXSOLDIERS, MAXENCAMPMENTS, MAXENCAMPMENTS, MAXENCAMPMENTS, MAXENCAMPMENTS, MAXENCAMPMENTS};
        decodedMessage[encampmentType.ordinal() + 1 ]++; // offset -1 because it should only count encampments, offset +2 for the identifying markers of the message

        int newMessage = encodeMessage(new int[]{1,3,decodedMessage[2],decodedMessage[3], decodedMessage[4], decodedMessage[5], decodedMessage[6], decodedMessage[7]},EncampmentReportPartitions);
        BroadcastMessage output = new BroadcastMessage();
        output.message = new int[]{newMessage};
        output.index = 0;
        return output;
    }
    /**
     *
     * @param message message after first two partitions are stripped away
     * @return {1, 3, numSoldiers, numMedbays, numShields, numArtillery, numGenerators, numSuppliers}
     */
    public static int[] parseQueryMessage(int message) {
        int numSoldiers = message % MAXSOLDIERS;
        message /= MAXSOLDIERS;
        int numMedbays = message % MAXENCAMPMENTS;
        message /= MAXENCAMPMENTS;
        int numShields = message % MAXENCAMPMENTS;
        message /= MAXENCAMPMENTS;
        int numArtillery = message % MAXENCAMPMENTS;
        message /= MAXENCAMPMENTS;
        int numGenerators = message % MAXENCAMPMENTS;
        message /= MAXENCAMPMENTS;
        int numSuppliers = message;
        return new int[]{1, 3, numSoldiers, numMedbays, numShields, numArtillery, numGenerators, numSuppliers};
    }
    private static int encodeMessage(int [] message, int[] partitionSize) {
        if(message.length != partitionSize.length) {
            System.out.println("BroadcastMessage.encodeMessage error: arrays do not match dimensions");
            return -1;
        }
        for(int i = 0; i < message.length; i++) {
            if(message[i] > partitionSize[i] || message[i] < 0) {
                System.out.println("BroadcastMessage.encodeMessage error: message size exceeds its partition - partition size: " + partitionSize[i] + ", value: " + message[i]);
                return -1;
            }
        }
        int output = message[message.length-1];
        for(int i = message.length-1; i > 0; i--) {
            output = output * partitionSize[i-1] + message[i-1];
        }
        return output;
    }
    // convert encoded integer to several individual integers based on the partition sizes
    /**
     *
     * @param message When listening, this will tell if the message recieved is the beginning of a broadcast
     * @return null if the message type doesn't fit the format correctly, {} if it's not actually a first message, and an array of info if it is the first message
     */
    public static int[] parseFirstMessage(int message) {
        if(message % FIRSTMESSAGEPARTITION == 1) {      // the message is the start of a series of message
            message /= FIRSTMESSAGEPARTITION;           // cut off the used information
            switch(message % MESSAGETYPEPARTITION) {
                case 1: // attack message
                    message /= MESSAGETYPEPARTITION;
                    return parseAttackMessage(message);
                case 2: // encampment message
                    message /= MESSAGETYPEPARTITION;
                    return parseSettleMessage(message);
                case 3: // encampment query
                    message /= MESSAGETYPEPARTITION;
                    return parseQueryMessage(message);
            }
        }
        else { // the message is a continuation
            return new int[]{};
        }
        if(MESSAGEDEBUG) System.out.println("BroadcastMessage.parseFirstMessage error: first message - type unknown");
        return null;
    }
    /**
     *
     * @param message This message has the first two partitions cut off already in order to know it's a settle message
     * @return int array {1, 2, encampmentType, robotId, MapLocation.x, MapLocation.y}
     */
    private static int[] parseSettleMessage(int message) {
        int encampmentType = 2 + message % SETTLEMESSAGE_ENCAMPMENTTYPE_PARTITION;
        message /= SETTLEMESSAGE_ENCAMPMENTTYPE_PARTITION;
        int robotId = message % SETTLEMESSAGE_MAXROBOTID;
        message /= SETTLEMESSAGE_MAXROBOTID;
        MapLocation mapLocation = decodeMapLoc(message);
        return new int[]{1, 2, encampmentType, robotId, mapLocation.x, mapLocation.y};
    }
    /**
     * Used internally as part of parseFirstMessage
     * @param message pass in only the third and fourth partitions
     * @return returns message length and attack round
     */
    private static int[] parseAttackMessage(int message) {
        int messageLength = message % ATTACKMESSAGE_MESSAGELENGTHPARTITION;
        int attackRound = message / ATTACKMESSAGE_MESSAGELENGTHPARTITION;
        if(messageLength == 0 || attackRound == 0 || attackRound > 10000) { // we intercepted a message
            return null;
        }
        else {
            return new int[]{1, 1, messageLength, attackRound};
        }
    }
    public static int encodeMapLoc(MapLocation mapLoc) {
        if(mapLoc != null) {
            return mapLoc.x * 70 + mapLoc.y;
        }
        else {
            return 0;
        }
    }
    private static int encodeMapLocPairs(MapLocation first, MapLocation second, int messageIndex) {
        final int[] partitions = new int[]{2, 4900, 4900, MAXMESSAGEWITHLOCATION};
        int[] messageToEncode = new int[]{0, encodeMapLoc(first), encodeMapLoc(second), messageIndex % MAXMESSAGEWITHLOCATION};
        return encodeMessage(messageToEncode, partitions);
        //return 2 * (encodeMapLoc(first) + encodeMapLoc(second) * 4900);
    }
    public static MapLocation decodeMapLoc(int encodedMapLoc) {
        int yVal = encodedMapLoc % 70;
        int xVal = (encodedMapLoc - yVal) / 70;
        return new MapLocation(xVal,yVal);
    }
    /**
     *
     * @param encodedMapLocs Pass in any message after the first for an attack message
     * @return two encoded mapLocs and the index of the message (modulo MAXMESSAGEWITHLOCATION)
     *
     */
    public static int[] parseMapLocMessage(int encodedMapLocs) {
        if(encodedMapLocs % 2 == 1) {
            System.out.println("BroadcastMessage.parseMapLocMessage incorrectly receive a first message");
            return null;
        }
        encodedMapLocs /= 2;
        int firstLoc = encodedMapLocs % 4900;
        encodedMapLocs /= 4900;
        int secondLoc = encodedMapLocs % 4900;
        int messageIndex = encodedMapLocs / 4900;
        if(secondLoc == 0) {
            secondLoc = firstLoc;
        }
        return new int[]{firstLoc, secondLoc, messageIndex};
    }
    private static boolean testEncodeDecodeAttackMessage(MapLocation [] testWaypoints, int testSendRound) {
        boolean printoutput = false;
        BroadcastMessage testMessage = composeAttackMessage(testWaypoints, testSendRound);
        boolean success = true;
        for(int i = 0; i < testMessage.message.length; i++) {
            if(printoutput) if(DEBUG) System.out.println("Actual Message: " + testMessage.message[i]);
            int[] decodedMessage = parseFirstMessage(testMessage.message[i]);
            if(decodedMessage == null) {
                System.out.println("null returned");
            }
            else if(decodedMessage.length > 0) { // checking first message
                success &= decodedMessage[0] == 1;
                success &= decodedMessage[1] == 1;
                success &= decodedMessage[2] == testMessage.message.length;
                success &= decodedMessage[3] == testSendRound;
                if(!success) {
                    if(printoutput) System.out.println( "Input: 1, Output: " + decodedMessage[0] +
                                        "Input: 1, Output: " + decodedMessage[1] +
                                        "Input: " + testMessage.message.length + ", Output: " + decodedMessage[2] +
                                        "Input: " + testSendRound + ", Output: " + decodedMessage[3]);
                }

                if(printoutput) System.out.println("in: 1, 1, " + testMessage.message.length + ", " + testSendRound);
                if(printoutput) System.out.println("out: " + decodedMessage[0] + ", " + decodedMessage[1] + ", " + decodedMessage[2] + ", " + decodedMessage[3]);
            }
            else { // checking mapLocations
                int[] decodedMapLocMessage = parseMapLocMessage(testMessage.message[i]);
                MapLocation[] waypoints = new MapLocation[]{decodeMapLoc(decodedMapLocMessage[0]), decodeMapLoc(decodedMapLocMessage[1])};
                if(printoutput) System.out.println("i = " + i + ", testWaypoints.length = " + testWaypoints.length);
                if (2 * i  < testWaypoints.length) {
                    if(printoutput) System.out.println("in: " + testWaypoints[2 * i - 2] + ", " + testWaypoints[2 * i - 1]);
                    success &= testWaypoints[2 * i - 2].equals(waypoints[0]) && testWaypoints[2 * i - 1].equals(waypoints[1]);
                } else {
                    if(printoutput) System.out.println("in: " + testWaypoints[2 * i - 2]);
                    success &= testWaypoints[2 * i - 2].equals(waypoints[0]);
                }
                if(printoutput) System.out.println("out: " + waypoints[0] + ", " + waypoints[1]);
            }
        }
        return success;
    }
    private static boolean testEncodeDecodeSettleMessage(MapLocation[] waypoints, int[] robotIDs, RobotType[] encampmentTypes) {
        BroadcastMessage testMessage = composeSettleMessage(waypoints, robotIDs, encampmentTypes);

        boolean success = true;
        if(testMessage.message.length != waypoints.length) {
            System.out.println("Array sizes don't match");
            return false;
        }
        for(int i = 0; i < waypoints.length; i++) {
            int[] receivedMessage = parseFirstMessage(testMessage.message[i]);
            // expecting {1, 2, encampmentType, robotId, MapLocation.x, MapLocation.y}
            if(receivedMessage.length != 6) {
                System.out.println("Didn't identify the message as a settle message");
                return false;
            }
            success &= receivedMessage[0] == 1;
            success &= receivedMessage[1] == 2;
            success &= receivedMessage[2] == encampmentTypes[i].ordinal();
            success &= receivedMessage[3] == robotIDs[i] % SETTLEMESSAGE_MAXROBOTID;
            success &= receivedMessage[4] == waypoints[i].x;
            success &= receivedMessage[5] == waypoints[i].y;
        }
        return success;
    }
    private static boolean testReportMessage(RobotType[] robotTypes) {
        boolean printoutput = false;
        int passedMessage = composeQueryMessage().message[0];
        boolean success = true;
        int[] numEncampmentsByType = {0,0,0,0,0,0};
        int[] firstMessage = null;

        for(int i = 1; i < robotTypes.length; i++) {
            firstMessage = parseFirstMessage(passedMessage);
            if(firstMessage == null) {
                System.out.println("Error: parseFirstMessage returned null");
                return false;
            }
            if(firstMessage.length != 8) {
                System.out.println("Error: parseFirstMessage returned an array of the wrong size");
                return false;
            }
            if(printoutput) {
                System.out.print("Current count: " );
                for(int j = 0; j < 6; j++) {
                    System.out.print(RobotType.values()[j+1] + ": " + firstMessage[j + 2] + ", ");
                }
                System.out.println();
            }
            if(printoutput) System.out.println("Passed message: " + passedMessage + ", adding a " + robotTypes[i].toString());
            passedMessage = updateQueryMessage(firstMessage, robotTypes[i]).message[0];
            numEncampmentsByType[robotTypes[i].ordinal() - 1]++;
        }
        firstMessage = parseFirstMessage(passedMessage);
        success &= firstMessage[0] == 1;
        success &= firstMessage[1] == 3;
        for(int i = 0; i < 6; i++) {
            if(printoutput) System.out.println(RobotType.values()[i+1].toString() + " counted: " + numEncampmentsByType[i] +  ", reported: " + firstMessage[i+2]);
            success &= firstMessage[i+2] == numEncampmentsByType[i];
        }
        return true;
    }
    public static void main(String[] args) {


        MapLocation[] test1Waypoints = new MapLocation[]{new MapLocation(1,2), new MapLocation(4,5), new MapLocation(69, 0), new MapLocation(0,69), new MapLocation(69,69)};
        RobotType[] robotTypes1 = new RobotType[]{RobotType.MEDBAY, RobotType.SHIELDS, RobotType.ARTILLERY, RobotType.SUPPLIER, RobotType.GENERATOR};
        int[] robotIds1 = new int[]{2345, 4567, 99999, 1, 55};
        int test1SendRound = 150;
        // 2, 10, 200, 10000

        MapLocation[] test2Waypoints = new MapLocation[]{new MapLocation(1,2), new MapLocation(4,5), new MapLocation(69, 0), new MapLocation(0,69), new MapLocation(69,69), new MapLocation(0,0)};
        RobotType[] robotTypes2 = new RobotType[]{RobotType.MEDBAY, RobotType.SHIELDS, RobotType.ARTILLERY, RobotType.SUPPLIER, RobotType.GENERATOR, RobotType.SUPPLIER};
        int[] robotIds2 = new int[]{2345, 4567, 99999, 1, 55, 0};
        int test2SendRound = 150;

        RobotType[] robotTypes3 = new RobotType[]{RobotType.MEDBAY, RobotType.SOLDIER, RobotType.SHIELDS, RobotType.SOLDIER, RobotType.ARTILLERY, RobotType.SOLDIER, RobotType.SUPPLIER, RobotType.SOLDIER, RobotType.GENERATOR, RobotType.SUPPLIER};

        System.out.println("Test1 attack results: " + testEncodeDecodeAttackMessage(test1Waypoints, test1SendRound));
        System.out.println("Test1 settle results: " + testEncodeDecodeSettleMessage(test1Waypoints, robotIds1, robotTypes1));
        System.out.println("Test1 query results: " + testReportMessage(robotTypes1));
        System.out.println("Test2 attack results: " + testEncodeDecodeAttackMessage(test2Waypoints, test2SendRound));
        System.out.println("Test2 settle results: " + testEncodeDecodeSettleMessage(test2Waypoints, robotIds2, robotTypes2));
        System.out.println("Test2 query results: " + testReportMessage(robotTypes2));
        System.out.println("Test3 query results: " + testReportMessage(robotTypes3));


        BroadcastMessage report = composeQueryMessage();


    }
}