package team017;

import battlecode.common.MapLocation;

/**
 * Created by Jeff on 9/15/2014.
 */
public class Stack {
    StackItem firstItem;
    int size;
    Class objectType;

    public Stack(Class _objectType) {
        objectType = _objectType;
        firstItem = null;
        size = 0;
    }

    public void push(Object obj) {
        if(obj.getClass() != objectType) {
            System.out.println("Stack exception: Trying to add the wrong Class type when adding " + obj.toString());
        }
        else {
            StackItem newItem = new StackItem(obj);
            newItem.nextItem = firstItem;
            firstItem = newItem;
            size++;
        }
    }

    public Object pop() {
        if(size > 0) {
            Object output = firstItem.item;
            firstItem = firstItem.nextItem;
            size--;
            return output;
        }
        else
            System.out.println("Stack exception: Tried to pop an empty stack");
            return null;
    }

    public Object[] toArray() {
        Object[] output = new Object[size];
        StackItem index = firstItem;
        for(int i = 0; i < size; i++) {
            if(index != null) {
                output[i] = index.item;
                index = index.nextItem;
            }
            else {
                System.out.println("Exception while trying to convert stack to array: Encountered null value");
                return null;
            }
        }
        return output;
    }
    public Object[] toReverseArray() {
        Object[] output = new Object[size];
        StackItem index = firstItem;
        for(int i = 0; i < size; i++) {
            if(index != null) {
                output[output.length - i - 1] = index.item;
                index = index.nextItem;
            }
            else {
                System.out.println("Exception while trying to convert stack to array: Encountered null value");
                return null;
            }
        }
        return output;
    }

    public static void main(String[] args) {
        MapLocation myLocation = new MapLocation(1,1);
        MapLocation myLocation2 = new MapLocation(2,2);
        MapLocation myLocation3 = new MapLocation(3,3);

        Stack myStack = new Stack(MapLocation.class);
        myStack.push(myLocation);
        System.out.println("Should be 1: " + myStack.size);
        MapLocation outputLocation = (MapLocation) myStack.pop();
        System.out.println(outputLocation.toString());

        System.out.println("Attempting to produce an error:");
        myStack.push(new int[]{1,2,3});

        myStack.push(myLocation);
        myStack.push(myLocation2);
        myStack.push(myLocation3);

        Object[] objArray = myStack.toArray();
        MapLocation[] myArray = new MapLocation[objArray.length];
        for(int i = 0; i < objArray.length; i++) {
            myArray[i] = (MapLocation)objArray[i];
        }
        System.out.println("Output should be (3,3), (2,2), (1,1):");
        for(MapLocation loc: myArray) {
            System.out.println(loc.toString());
        }

        objArray = myStack.toReverseArray();
        myArray = new MapLocation[objArray.length];
        for(int i = 0; i < objArray.length; i++) {
            myArray[i] = (MapLocation)objArray[i];
        }
        System.out.println("Output should be (1,1), (2,2), (3,3):");
        for(MapLocation loc: myArray) {
            System.out.println(loc.toString());
        }

    }

}
