package Model.Pathfinding;

import java.util.NoSuchElementException;

/**
 * Minimum-index based Priority Queue
 * supports delete min and insertion
 */
public class IndexMinPQ {

    private double[] weights;
    private Integer[] indices;
    private int size = 0;

    /**
     * Constructor for the priority queue
     * @param size int size of the indexMinPQ
     */
    public IndexMinPQ(int size) {
        indices = new Integer[size + 1];
        weights = new double[size + 1];
    }

    /**
     * Deletes the minimum indexed element from the queue and returns it
     * @return int corresponding to the vertexIndex
     */
    public int deleteMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Priority queue is empty");
        }

        int min = indices[1];
        exchange(1, size);

        indices[size] = null;
        size--;
        sink(1);

        return min;
    }

    private void sink(int index) {
        while(index * 2 <= size) {

            int i = index * 2;
            if(i < size && greater(i, i+1)) {
                i++;
            }
            if(!greater(index, i)) {
                break;
            }

            exchange(index, i);
            index = i;

        }
    }

    private void swim(int index) {
        while(index > 1 && greater(index/2, index)) {
            exchange(index/2, index);
            index /= 2;
        }
    }



    private boolean greater(int i, int j) {
        return weights[indices[i]] > weights[indices[j]];
    }

    private void exchange(int i, int j) {
        int temp = indices[i];
        indices[i] = indices[j];
        indices[j] = temp;
    }

    /**
     * Inserts vertex represented from it's index into the PQ based on it's distTo
     * @param vertex int index of the vertex
     * @param distTo the distTo of the vertex
     */
    public void insert(int vertex, double distTo) {
        size++;
        indices[size] = vertex;

        weights[vertex] = distTo;
        swim(size);
    }

    /**
     * Returns the size of the PQ
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * Clears the PQ
     */
    public void clear() {
        weights = null;
        indices = null;
    }

    /**
     * Returns true if empty, false if not empty
     * @return true if empty, false if not empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
