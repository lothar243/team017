package team017;

import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/13/2014.
 */
public class Test {

    public static void printArray(int[][] arr) {
        System.out.print(arrayToString(arr));
    }
    public static String arrayToString(int[][] arr) {
        String outputString = "";
        for(int j = 0; j < arr[0].length; j++) {
            for(int i = 0; i < arr.length; i++) {
                outputString += arr[i][j] + "  ";
            }
            outputString += "\r\n";
        }
        return outputString;
    }

    public static String arrayToString(MapLocation[] locations) {
        String outputString = "";
        for(MapLocation loc: locations) {
            outputString += mapLocString(loc) + ",";
        }
        return outputString;
    }

    public static String mapLocString(MapLocation loc) {
        return "(" + loc.x + "," + loc.y + ")";
    }

    public static String arrayToString(int[] loc) {
        return "(" + loc[0] + "," + loc[1] + ")";
    }

    public static String arrayToString(boolean[] objectArray) {
        String output = "";
        for(Object obj: objectArray) {
            output += obj.toString();
        }
        return output;
    }

    public static String arrayToFormatedString(int[][] arr, int cellWidth) {
        String outputString = "";
        for(int j = 0; j < arr[0].length; j++) {
            for(int i = 0; i < arr.length; i++) {
                outputString += setWidth(arr[i][j], cellWidth-1) + " ";
            }
            outputString += "\r\n";
        }
        return outputString;

    }

    public static String setWidth(int num, int width) {
        String output = "" + num;
        while (output.length() < width) {
            output = " " + output;
        }
        return output;
    }

    public static void reportNullValuedArray(Object[] arr) {
        boolean hasNullValue = false;
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] == null) hasNullValue = true;
        }
        if(hasNullValue) {
            for(int i = 0; i < arr.length; i++) {
                if(arr[i] != null) {
                    System.out.print(arr[i] + " ");
                }
                else {
                    System.out.print("null ");
                }
            }
        }
    }

}
