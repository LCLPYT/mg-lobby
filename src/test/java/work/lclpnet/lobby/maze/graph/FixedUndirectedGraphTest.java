package work.lclpnet.lobby.maze.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedUndirectedGraphTest {

    @Test
    void init_negativeCount_throws() {
        assertThrows(IllegalArgumentException.class, () -> new FixedUndirectedGraph(-1));
        assertThrows(IllegalArgumentException.class, () -> new FixedUndirectedGraph(-100));
    }

    @Test
    void getNodeCount_given_echo() {
        assertEquals(0, new FixedUndirectedGraph(0).getNodeCount());
        assertEquals(1, new FixedUndirectedGraph(1).getNodeCount());
        assertEquals(5, new FixedUndirectedGraph(5).getNodeCount());
    }

    @Test
    void getAdjacent_count_correct() {
        var graph = new FixedUndirectedGraph(5);
        assertEquals(0, graph.getAdjacent(0).count());

        graph.addEdge(0, 1);
        assertEquals(1, graph.getAdjacent(0).count());

        graph.addEdge(0, 1);
        assertEquals(1, graph.getAdjacent(0).count());

        graph.addEdge(0, 4);
        assertEquals(2, graph.getAdjacent(0).count());
    }

    @Test
    void hasEdge_outOfBounds_throws() {
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).hasEdge(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).hasEdge(0, 5));
    }

    @Test
    void addEdge_reflexive_throws() {
        assertThrows(IllegalArgumentException.class, () -> new FixedUndirectedGraph(5).addEdge(1, 1));
        assertThrows(IllegalArgumentException.class, () -> new FixedUndirectedGraph(5).addEdge(0, 0));
    }

    @Test
    void addEdge_valid_hasEdge() {
        var graph = new FixedUndirectedGraph(5);
        assertFalse(graph.hasEdge(0, 1));
        graph.addEdge(0, 1);
        assertTrue(graph.hasEdge(0, 1));
    }

    @Test
    void addEdge_outOfBounds_throws() {
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).addEdge(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).addEdge(0, 5));
    }

    @Test
    void removeEdge_valid_removed() {
        var graph = new FixedUndirectedGraph(5);
        graph.addEdge(0, 1);
        assertTrue(graph.hasEdge(0, 1));
        graph.removeEdge(0, 1);
        assertFalse(graph.hasEdge(0, 1));
    }

    @Test
    void removeEdge_outOfBounds_throws() {
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).removeEdge(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> new FixedUndirectedGraph(5).removeEdge(0, 5));
    }
}