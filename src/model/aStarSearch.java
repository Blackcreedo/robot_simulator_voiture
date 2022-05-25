package model;

import burger.SmartTurtlebot;

import java.util.*;

public class aStarSearch {

    LinkedList<aStarNode> openList;
    LinkedList<aStarNode> closedList;
    ArrayList<EmptyValuedCell> closedListCoord;

    public LinkedList<aStarNode> expand(aStarNode node, Grid grid, int xGoal, int yGoal) {
        int x = node.getNode().x;
        int y = node.getNode().y;
        LinkedList<aStarNode> nodes = new LinkedList<>();
        EmptyCell[] ec = grid.getAdjacentEmptyRobotCell(x,y);
        for (int i=0; i<4; i++) {
            if (ec[i] != null) { //EmptyValuedCell node, aStarNode parent, double heuristique
                //Math.abs(ecTest.getX()-this.xGoal)+ Math.abs(ecTest.getY()-this.yGoal);
                double heuristique = Math.abs(xGoal-ec[i].x)+Math.abs(yGoal-ec[i].y);
                aStarNode voisin = new aStarNode((EmptyValuedCell) ec[i], node, heuristique);


                if (!closedListCoord.contains(voisin.getNode())) {
                    nodes.add(voisin);
                } else if (closedList.get(closedListCoord.indexOf(voisin.getNode())).getPathCost()> voisin.getPathCost()) {
                    closedList.set(closedListCoord.indexOf(voisin.getNode()),voisin);
                }
            }
        }
        closedList.add(node);
        closedListCoord.add(node.getNode());
        return nodes;
    }

    public ArrayList<aStarNode> solve(Grid grid, int x, int y, int xGoal, int yGoal) {
        aStarNode initNode = new aStarNode(new EmptyValuedCell(x,y,1));
        aStarNode solution = null;
        openList = new LinkedList<>();
        closedList = new LinkedList<>();
        closedListCoord = new ArrayList<>();

        LinkedList<aStarNode> expandNodes;
        initNode.setEvaluation(Math.abs(x-xGoal)+Math.abs(y-yGoal));

        //closedList.add(initNode);
        //closedListCoord.add(new Integer[]{initNode.getNode().getX(),initNode.getNode().getY()});

        expandNodes = expand(initNode, grid, xGoal, yGoal);
        openList.addAll(expandNodes);

        Collections.sort(openList, new SortByEvaluation());

        boolean done = false;

        while(!done) {
            if (openList.isEmpty()) {
                done=true;
            } else {
                aStarNode node = openList.pop();
                if (node.getNode().getX()==xGoal && node.getNode().getY() == yGoal) {
                    solution = node;
                    done=true;
                } else {
                    expandNodes = expand(node, grid, xGoal, yGoal);
                    openList.addAll(expandNodes);
                    Collections.sort(openList, new SortByEvaluation());
                }
            }
        }

        ArrayList<aStarNode> path = new ArrayList<>();
        aStarNode current = solution;

        while(current.getParent()!=null) {
            path.add(0,current);
            current=current.getParent();
        }
        path.add(0,current);
        return path;

    }


}
