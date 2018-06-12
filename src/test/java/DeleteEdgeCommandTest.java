package java;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
//import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DeleteEdgeCommandTest{

    /*@Test
    public void getIntersectionTreeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
        DeleteEdgeCommand cmd = new DeleteEdgeCommand(null,null, null, 0);

        List<LarsGraph> annotations = new ArrayList<>();
        List<LarsGraph> nonAnnotations = new ArrayList<>();

        GraphVertex v1 = new GraphVertex(0,0);
        GraphVertex v2 = new GraphVertex(0,100);
        GraphVertex v3 = new GraphVertex(100,0);
        GraphVertex v4 = new GraphVertex(100,100);
        GraphVertex v5 = new GraphVertex(-10,50);
        GraphVertex v6 = new GraphVertex(110,50);

        UndirectedGraph<GraphVertex, GraphEdge> nA1 = new SimpleGraph<GraphVertex, GraphEdge>(GraphEdge.class);
        nA1.addVertex(v1);
        nA1.addVertex(v2);
        nA1.addEdge(v1, v2);
        ArrayList<PointHD2> hull1 = new ArrayList<>();
        hull1.add(new PointHD2(-5, 0));
        hull1.add(new PointHD2(-5, -5));
        hull1.add(new PointHD2(5, 0));
        hull1.add(new PointHD2(5, 100));
        hull1.add(new PointHD2(0, 105));
        hull1.add(new PointHD2(-5, 100));
        LarsGraph nonAnnotation1 = new LarsGraph(nA1, false);
        nonAnnotation1.setConcaveHull(hull1);

        UndirectedGraph<GraphVertex, GraphEdge> nA2 = new SimpleGraph<GraphVertex, GraphEdge>(GraphEdge.class);
        nA2.addVertex(v3);
        nA2.addVertex(v4);
        nA2.addEdge(v3, v4);
        ArrayList<PointHD2> hull2 = new ArrayList<>();
        hull2.add(new PointHD2(95, 0));
        hull2.add(new PointHD2(100, -5));
        hull2.add(new PointHD2(105, 0));
        hull2.add(new PointHD2(105, 100));
        hull2.add(new PointHD2(100, 105));
        hull2.add(new PointHD2(95, 100));
        LarsGraph nonAnnotation2 = new LarsGraph(nA2, false);
        nonAnnotation2.setConcaveHull(hull2);

        nonAnnotations.add(nonAnnotation2);

        UndirectedGraph<GraphVertex, GraphEdge> a1 = new SimpleGraph<GraphVertex, GraphEdge>(GraphEdge.class);
        a1.addVertex(v5);
        a1.addVertex(v6);
        a1.addEdge(v5, v6);
        ArrayList<PointHD2> hull3 = new ArrayList<>();
        hull3.add(new PointHD2(-10, 55));
        hull3.add(new PointHD2(-15, 50));
        hull3.add(new PointHD2(-10, 45));
        hull3.add(new PointHD2(110, 45));
        hull3.add(new PointHD2(115, 50));
        hull3.add(new PointHD2(110, 55));
        LarsGraph anno1 = new LarsGraph(a1, true);
        anno1.setConcaveHull(hull3);

        annotations.add(anno1);

        Class<?>[] signature = new Class[4];
        signature[0] = LarsGraph.class;
        signature[1] = List.class;
        signature[2] = List.class;
        signature[3] = List.class;
        Method m = DeleteEdgeCommand.class.getDeclaredMethod("getIntersectionTree", signature);
        m.setAccessible(true);

        List<LarsGraph> result = new ArrayList<>();

        Object[] params = new Object[4];
        params[0] = nonAnnotation1;
        params[1] = annotations;
        params[2] = nonAnnotations;
        params[3] = result;

        m.invoke(cmd, params);

    }*/
}
