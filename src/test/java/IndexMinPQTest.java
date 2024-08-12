import Model.Pathfinding.IndexMinPQ;
import org.junit.Test;
import static org.junit.Assert.*;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexMinPQTest {

    /**
     * Tests the insert method and checks with size() calls
     */
    @Test
    public void insertTest() {
        IndexMinPQ minPQ = new IndexMinPQ(10);

        // Contains 0 elements
        assertEquals(0, minPQ.size());

        minPQ.insert(0, 10);

        // Contains 1 element
        assertEquals(1, minPQ.size());

        // Inserts 9 elements
        for (int i = 0; i < 9; i++) {
            minPQ.insert(i, 10+i);
        }

        // Contains 10
        assertEquals(10, minPQ.size());
    }


    /**
     * Tests the deletemin method and asserts by comparing the returned index with the expected index to be returned
     */
    @Test
    public void deleteMinTest() {
        IndexMinPQ minPQ = new IndexMinPQ(100);

        // range for random number
        int max = 100;
        int min = 1;
        int range = max - min + 1;

        // generate random number between 1 - 100
        int rndint1 = (int) (Math.random() * range) + min;
        int rndint2 = (int) (Math.random() * range) + min;
        int rndint3 = (int) (Math.random() * range) + min;
        int rndint4 = (int) (Math.random() * range) + min;
        int rndint5 = (int) (Math.random() * range) + min;
        int rndint6 = (int) (Math.random() * range) + min;
        int rndint7 = (int) (Math.random() * range) + min;
        int rndint8 = (int) (Math.random() * range) + min;
        int rndint9 = (int) (Math.random() * range) + min;
        int rndint10 = (int) (Math.random() * range) + min;

        // Inserted in random order based on weight(distTo) to make sure the pq doesnt delete based on insertion order
        minPQ.insert(rndint10, 900);
        minPQ.insert(rndint4, 300);
        minPQ.insert(rndint7, 600);
        minPQ.insert(rndint6, 500);
        minPQ.insert(rndint8, 700);
        minPQ.insert(rndint9, 800);
        minPQ.insert(rndint3, 200);
        minPQ.insert(rndint5, 400);
        minPQ.insert(rndint1, 10);
        minPQ.insert(rndint2, 100);

        int returnedMinIndex;

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint1, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint2, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint3, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint4, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint5, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint6, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint7, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint8, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint9, returnedMinIndex);

        returnedMinIndex = minPQ.deleteMin();
        assertEquals(rndint10, returnedMinIndex);
    }

    /**
     * Tests the size method by insertion
     */
    @Test
    public void insertSizeTest() {
        IndexMinPQ minPQ = new IndexMinPQ(10);

        assertEquals(0, minPQ.size());

        for (int i = 0; i < 10; i++) {
            minPQ.insert(i, 0);
        }
        assertEquals(10, minPQ.size());
    }

    /**
     * Tests the size method by deletion
     */
    @Test
    public void deleteSizeTest() {
        IndexMinPQ minPQ = new IndexMinPQ(10);

        assertEquals(0, minPQ.size());

        for (int i = 0; i < 10; i++) {
            minPQ.insert(i, 0);
        }
        assertEquals(10, minPQ.size());

        minPQ.deleteMin();
        assertEquals(9, minPQ.size());

        minPQ.deleteMin();
        minPQ.deleteMin();
        minPQ.deleteMin();
        assertEquals(6, minPQ.size());

        for (int i = 6; i > 0; i--) {
            minPQ.deleteMin();
        }
        assertEquals(0, minPQ.size());
        assertNotEquals(1, minPQ.size());
    }
}
