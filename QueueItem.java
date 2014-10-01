package team017;

import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/20/2014.
 */
public class QueueItem {
    MapLocation coord;
    int weight;
    QueueItem nextItem;


    public QueueItem(MapLocation _coord, int _weight) {
        this(_coord,_weight,null);
    }
    public QueueItem(MapLocation _coord, int _weight, QueueItem _nextItem) {
        coord = _coord;
        weight = _weight;
        nextItem = _nextItem;
    }
    public String toString() {
        String output = coord.toString() + ": Distance " + weight + "\n";
        if(nextItem != null) {
            output = nextItem.toString() + output;
        }
        return output;
    }
}
