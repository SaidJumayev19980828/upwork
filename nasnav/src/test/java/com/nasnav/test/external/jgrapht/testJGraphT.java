package com.nasnav.test.external.jgrapht;

import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class testJGraphT {
	 
	
	
	
    @Test
    public void categoriesDAGCycles() throws Exception {
        DirectedAcyclicGraph<String, DefaultEdge> graph = createGraph();

        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<String, DefaultEdge>(graph);
        Set<String> cycleVertices = cycleDetector.findCycles();

        assertTrue(!cycleDetector.detectCycles());
        assertTrue(cycleVertices.size() == 0);
    }

    
    
    
    @Test
    public void categoriesDAGCreateCycleError() throws Exception {
        DirectedAcyclicGraph<String, DefaultEdge> graph = createGraph();
        try {
            graph.addEdge("Tag#2.2", "Tag#2.1");
            graph.addEdge("Tag#2.1", "Tag#2");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    
    
    
    private DirectedAcyclicGraph<String, DefaultEdge> createGraph() {
        DirectedAcyclicGraph<String, DefaultEdge> graph = new DirectedAcyclicGraph<>(DefaultEdge.class);
        graph.addVertex("Tag#1");
        graph.addVertex("Tag#2");
        graph.addVertex("Tag#1.1");
        graph.addVertex("Tag#1.2");
        graph.addVertex("Tag#2.1");
        graph.addVertex("Tag#2.2");
        graph.addVertex("Tag#1.1.1");

        graph.addEdge("Tag#1", "Tag#1.1");
        graph.addEdge("Tag#1", "Tag#1.2");
        graph.addEdge("Tag#1", "Tag#2.1");
        graph.addEdge("Tag#1.1", "Tag#1.1.1");
        graph.addEdge("Tag#2", "Tag#2.1");
        graph.addEdge("Tag#2", "Tag#2.2");
        graph.addEdge("Tag#2", "Tag#1.2");

        return graph;
    }
}
