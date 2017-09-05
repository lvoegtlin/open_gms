package ch.unifr.hisdoc2.graphmanuscribble.view.helper;

import ch.unifr.hisdoc2.graphmanuscribble.helper.Constants;
import ch.unifr.hisdoc2.graphmanuscribble.model.graph.GraphEdge;
import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

/**
 * Quadtree for the collision detection of scribbles with graph edges.
 * This is build after this tutorial (https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374)
 */
public class Quadtree{

    /**
     * Current node level
     */
    private int level;

    /**
     * The edges as polygon the quadtree is holding.
     */
    private ArrayList<GraphEdge> edges;

    /**
     * Space the tree occupies
     */
    private Rectangle bounds;

    /**
     * Subnodes of this node
     */
    private Quadtree[] nodes;

    public Quadtree(int level, Rectangle bounds){
        this.level = level;
        this.edges = new ArrayList<>();
        this.bounds = bounds;
        this.nodes = new Quadtree[4];
    }

    /**
     * Splits the current Quad tree into 4 subnodes.
     */
    public void split(){
        int subWidth = (int) (bounds.getWidth() / 2);
        int subHeight = (int) (bounds.getHeight() / 2);
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();

        nodes[0] = new Quadtree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    /**
     * Gets the node the edge belongs to. If this method returns -1 the edge fits not completely within the child node
     * so it is part of the parent node.
     *
     * @param edge - GraphEdge you want to know the containing node
     * @return index of the node containing edge (if parent -1)
     */
    private int getIndex(GraphEdge edge){
        int i = -1;
        double verMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horMidpoint = bounds.getY() + (bounds.getHeight() / 2);

        Bounds edgeB = edge.getPolygonRepresentation().getLayoutBounds();

        //edge completely fits into the top quadrants
        boolean topQ = (edgeB.getMinY() < horMidpoint && edgeB.getMaxY() < horMidpoint);
        //edge completely fits into the bottom quadrants
        boolean botQ = (edgeB.getMinY() > horMidpoint && edgeB.getMaxY() > horMidpoint);

        //edge fits into left quadrants
        if(edgeB.getMinX() < verMidpoint && edgeB.getMaxX() < verMidpoint){
            if(topQ){
                i = 1;
            } else if(botQ){
                i = 2;
            }
        } else if(edgeB.getMinX() > verMidpoint && edgeB.getMaxX() > verMidpoint){ //fits into right quadrants
            if(topQ){
                i = 0;
            } else if(botQ){
                i = 3;
            }
        }

        return i;
    }

    /**
     * Insert a edge into the quadtree. If the capacity is reached it will split the tree
     * and add all the edges to their corresponding nodes.
     *
     * @param edge - edge to add to the quadtree
     */
    public void insert(GraphEdge edge){
        if(nodes[0] != null){
            int index = getIndex(edge);

            if(index != -1){
                nodes[index].insert(edge);
                return;
            }
        }

        edges.add(edge);

        if(edges.size() > Constants.MAX_OBJECTS && level < Constants.MAX_LEVELS){
            if(nodes[0] == null){
                split();
            }

            int i = 0;
            while(i < edges.size()){
                int index = getIndex(edges.get(i));
                if(index != -1){
                    nodes[index].insert(edges.remove(i));
                } else {
                    i++;
                }
            }
        }
    }

    /**
     * Returns all the edges the input could collide with.
     *
     * @param edgeList - the list with the potential collision objects
     * @param scribble - the input
     * @return object the input could collide with
     */
    public ArrayList<GraphEdge> retrieve(ArrayList<GraphEdge> edgeList, GraphEdge scribble){
        int i = getIndex(scribble);

        if(i != -1 && nodes[0] != null){
            nodes[i].retrieve(edgeList, scribble);
        }

        edgeList.addAll(edges);

        return edgeList;
    }

}
