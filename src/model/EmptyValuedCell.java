package model;

import org.json.simple.JSONObject;
import sun.invoke.empty.Empty;

public class EmptyValuedCell extends EmptyCell{

    double value;

    public EmptyValuedCell(int x, int y, double value) {
        super(x,y);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("value",""+value);
        return jo;
    }

    @Override
    public String toString() {
        return super.toString() + ",value: "+value;
    }
}
