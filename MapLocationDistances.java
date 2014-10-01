package team017;

import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/14/2014.
 */
public class MapLocationDistances {
    MapLocation[] locations;
    int[] distances;
    static final int maxEncampments = 30;

    public void sortByDistance() {
        MapLocation [] sortedList = new MapLocation[Math.min(locations.length,30)];
        int [] sortedDistance = new int[sortedList.length];
        for(int i = 0; i < sortedList.length; i++) {
            int minDistance = 999999999;
            int shortestDistanceIndex = -1;
            for(int j = 0; j < locations.length; j++) {
                if(distances[j] < minDistance && locations[j] != null) {
                    minDistance = distances[j];
                    shortestDistanceIndex = j;
                }
            }
            sortedDistance[i] = minDistance;
            sortedList[i] = locations[shortestDistanceIndex];
            distances[shortestDistanceIndex] = 999999999;
            /*shortestDistanceIndex = 0;
            for(int j = 1; j < locations.length; j++) {
                if(distances[j] < distances[shortestDistanceIndex]) {
                    shortestDistanceIndex = j;
                }
            }
            // we've found the shortest in the list, now to assign it to the new array (I know it's just a bubblesort)
            sortedList[i] = locations[shortestDistanceIndex];
            sortedDistance[i] = distances[shortestDistanceIndex];
            distances[shortestDistanceIndex] = 1000000000;*/
        }
        System.out.println("Sorting");
        Test.reportNullValuedArray(locations);
        Test.reportNullValuedArray(sortedList);

        locations = sortedList;
        distances = sortedDistance;
    }



    public MapLocationDistances(MapLocation [] _locations, int[] _distances) {
        locations = _locations;
        distances = _distances;
    }

    public String toString() {
        String output = "";
        for(int i = 0; i < locations.length; i++) {
            output += locations[i].toString() + "-" + distances[i] + ", ";
        }
        return output;
    }

    public static void main(String [] args) {
        int numSpots = 40;
        MapLocation [] testLocations = new MapLocation[numSpots];
        int[] distances = new int[numSpots];
        for(int i = 0; i < numSpots; i++) {
            testLocations[i] = new MapLocation(i,i);
            distances[i] = (29 * i) % numSpots;
        }
        MapLocationDistances myList = new MapLocationDistances(testLocations,distances);
        System.out.println("Before sorting: " + myList.toString());
        myList.sortByDistance();
        System.out.println("After sorting: " + myList.toString());
    }
}
