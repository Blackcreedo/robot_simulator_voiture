package model;

import org.json.simple.JSONObject;

public class EmptyCell implements Situated {
	
	int x, y;	
	
	public EmptyCell(int x, int y) {
		this.x = x;
		this.y = y;		
	}

	@Override
	public 	ComponentType getComponentType() {
		return ComponentType.empty;
	}
	
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setX(int x) {
		this.x = x;
		
	}

	@Override
	public void setY(int dy) {
		this.y = y;
		
	}

	@Override
	public void setLocation(int x, int y) {
		setX(x);
		setY(y);
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("type","empty");
		jo.put("x",""+x);
		jo.put("y",""+y);
		return jo;
	}

	public String toString(){
		return "Empty - x: " + x + ",y: "+ y;
	}

	@Override
	public String display() {		
		return "_";
	}

}
