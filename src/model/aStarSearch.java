package model;

import java.util.ArrayList;

public class aStarSearch {

    ArrayList<aStarNode> openList;
    ArrayList<aStarNode> closedList;

    public ArrayList<aStarNode> expand(aStarNode node, Grid grid, int xGoal, int yGoal) {
        int x = node.getNode().x;
        int y = node.getNode().y;
        ArrayList<aStarNode> nodes = new ArrayList<>();
        EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);
        for (int i=0; i<4; i++) {
            if (ec[i] != null) { //EmptyValuedCell node, aStarNode parent, double heuristique
                //Math.abs(ecTest.getX()-this.xGoal)+ Math.abs(ecTest.getY()-this.yGoal);
                double heuristique = Math.abs(xGoal-ec[i].x)+Math.abs(yGoal-ec[i].y);
                aStarNode voisin = new aStarNode((EmptyValuedCell) ec[i], node, heuristique);
                nodes.add(voisin);
            }
        }
        return nodes;
    }
}
