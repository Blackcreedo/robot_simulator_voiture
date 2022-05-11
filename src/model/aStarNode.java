package model;


public class aStarNode {
    private EmptyValuedCell node;

    private aStarNode parent;
    private double evaluation;
    private double pathCost;

    public aStarNode(EmptyValuedCell node) {
        this.node = node;
        this.pathCost = 0;
        this.evaluation = 0;
    }

    public aStarNode(EmptyValuedCell node, aStarNode parent, double heuristique) {
        this.node = node;
        this.parent = parent;
        this.pathCost = parent.getPathCost()+node.getValue();
        this.evaluation = this.getPathCost() + heuristique;
    }

    public EmptyValuedCell getNode() {
        return node;
    }

    public void setNode(EmptyValuedCell node) {
        this.node = node;
    }

    public aStarNode getParent() {
        return parent;
    }

    public void setParent(aStarNode parent) {
        this.parent = parent;
    }

    public double getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(double evaluation) {
        this.evaluation = evaluation;
    }

    public double getPathCost() {
        return pathCost;
    }

    public void setPathCost(double pathCost) {
        this.pathCost = pathCost;
    }

    @Override
    public String toString(){
        return "[Node : " + this.node.toString() + " evaluation : " + this.evaluation +
                " pathCost : " + this.pathCost + " parent : " + this.parent +"]";
    }
}
