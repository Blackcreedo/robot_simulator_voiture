package model;

import org.json.simple.JSONObject;

import java.util.Objects;

public class EmptyValuedCell extends EmptyCell{

    private double value;
    private int anciennete;


    @Override
    public boolean equals(Object o) {
        if (o instanceof EmptyValuedCell) {
            EmptyValuedCell cell = (EmptyValuedCell) o;
            return (cell.getX()==this.getX() && cell.getY()==this.getY());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, anciennete);
    }

    public EmptyValuedCell(int x, int y, double value) {
        super(x,y);
        this.value = value;
        this.anciennete=0;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getAnciennete() { return anciennete; }

    public void setAnciennete(int anciennete) { this.anciennete = anciennete; }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("value",""+value);
        jo.put("anciennete",""+anciennete);
        return jo;
    }

    @Override
    public String toString() {
        return super.toString() + ",value: "+value+",anciennete: "+anciennete;
    }
}
