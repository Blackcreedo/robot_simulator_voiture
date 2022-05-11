package model;

import java.util.Comparator;

public class SortByEvaluation implements Comparator<aStarNode> {

    public int compare(aStarNode a, aStarNode b) {
        return (int)(a.getEvaluation()-b.getEvaluation());
    }

}
