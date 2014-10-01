package team017;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/20/2014.
 */
public class SortedQueue {
    public boolean DEBUG = false;
    QueueItem queueItem;

    public void push(MapLocation location, int weight) {

        if(queueItem == null) { // queue was empty, so adding to the start
            queueItem = new QueueItem(location, weight);
        }
        else {
            if(weight <= queueItem.weight) {
                queueItem = new QueueItem(location, weight, queueItem);
            }
            else {
                QueueItem index = queueItem;
                while(index.nextItem != null && index.nextItem.weight < weight) {
                    index = index.nextItem;
                }
                // index has a lower weight than the new item, but index.nextItem has a higher weight, so insert the new item between them
                index.nextItem = new QueueItem(location, weight, index.nextItem);
            }
        }
    }

    public QueueItem pop() {
        if(queueItem == null) {
            System.out.println("Warning, tried to pop an empty queueu");
            return null;
        }
        else {
            QueueItem output = queueItem;
            queueItem = queueItem.nextItem;
            return output;
        }
    }

    public boolean isEmpty() {
        return queueItem == null;
    }

    public SortedQueue(MapLocation start) {
        queueItem = new QueueItem(start, 0);
    }

    public static void main(String[] args) {
        System.out.println("10");
        MapLocation startLoc = new MapLocation(0,0);
        SortedQueue testQueue = new SortedQueue(startLoc);
        System.out.println(testQueue.queueItem.toString());
        System.out.println("20");

        QueueItem item = testQueue.pop();
        System.out.println("Popped:\n" + item.toString());
        for(int i = 0; i < 8; i++) {
            System.out.println("Pushing:");
            testQueue.push(startLoc.add(Direction.values()[i]), item.weight+1);
        }
        while(!testQueue.isEmpty()) {
            System.out.println("Popping...");
            System.out.println(testQueue.pop().toString());
        }
    }
}
