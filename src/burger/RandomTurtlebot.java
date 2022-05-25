package burger;

import model.*;
import components.Turtlebot;
import mqtt.Message;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RandomTurtlebot extends Turtlebot{
	protected Random rnd;
	protected List<Situated> grid;
	protected int rows;
	protected int columns;

	public RandomTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		rnd = new Random(seed);
		grid = new ArrayList<Situated>();	
	}

	protected void init() {
		clientMqtt.subscribe("inform/grid/init");
    	clientMqtt.subscribe(name + "/position/init");		
		clientMqtt.subscribe(name + "/grid/init");		
		clientMqtt.subscribe(name + "/grid/update");		
		clientMqtt.subscribe(name + "/action");		
	}

	public void handleMessage(String topic, JSONObject content){				
		if (topic.contains(name+"/grid/update")) {
			grid = new ArrayList<Situated>();
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			String namer = (String)jo.get("name");
           			if(idr != id){
	    	    		grid.add(new RobotDescriptor(to, idr, namer, 1.0));
	    	    	}
        		} else if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			grid.add(new ObstacleDescriptor(to));
        		} else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			grid.add(new EmptyCell(xo,yo));
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		for(Situated s:grid){
        			s.display();
        		}
        	}
        } else if (topic.contains(name+"/action")) {
    	    int stepr = Integer.parseInt((String)content.get("step"));
        	move(stepr);
        } 
        else if (topic.contains("inform/grid/init")) {
        	rows = Integer.parseInt((String)content.get("rows"));
        	columns = Integer.parseInt((String)content.get("columns"));
        }
        else if (topic.contains(name+"/position/init")) {
      		x = Integer.parseInt((String)content.get("x"));
        	y = Integer.parseInt((String)content.get("y"));
        }
        else if (topic.contains(name+"/grid/init")) {
        	grid = new ArrayList<Situated>();
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			boolean findr = false;
           			String namer = (String)jo.get("name");
    	    		if(idr != id){
	    	    		grid.add(new RobotDescriptor(to, idr, namer, 1.0));
	    	    	}
        		} else if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			grid.add(new ObstacleDescriptor(to));
        		} else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			grid.add(new EmptyValuedCell(xo,yo,1));
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		for(Situated s:grid){
        			s.display();
        		}
        	}
        }
	}

	public void setLocation(int x, int y) {
		int xo = this.x;
		int yo = this.y;
		this.x = x;
		this.y = y;		
	}

	public List<Situated> getGrid() {
		return grid;
	}

	public void setGrid(List<Situated> grid) {
		this.grid = grid;
	}

	public void randomOrientation() {
		double d = Math.random();
		if(d < 0.25) {
			if(orientation != Orientation.up) 
				orientation = Orientation.up;
			else 
				orientation = Orientation.down;
		}
		else if(d < 0.5) {
			if(orientation != Orientation.down) 
				orientation = Orientation.down;
			else 
				orientation = Orientation.up;
		}
		else if(d < 0.75) {
			if(orientation != Orientation.left) 
				orientation = Orientation.left;
			else 
				orientation = Orientation.right;
		}
		else {
			if(orientation != Orientation.right) 
				orientation = Orientation.right;
			else 
				orientation = Orientation.left;
		}
	}

	public void move(int step) {	
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + ",";
		for(int i = 0; i < step; i++) {
			EmptyCell[] ec = new EmptyCell[4];
			ec[0] = null;
        	ec[1] = null;
        	ec[2] = null;
        	ec[3] = null;
			//System.out.println("myRobot (" + columns + "," + rows + "): " + getX() + " " + getY());
			String st = "[";
			for(Situated s:grid){
				//System.out.println("neighbour (" + s.getComponentType() + "): " + s.getX() + " " + s.getY());
				if(getX() > 0 && s.getX() == getX()-1 && s.getY()==getY()) {
					if(s.getComponentType() == ComponentType.empty) {
						ec[2] = (EmptyCell)s;
					} else {
						ec[2] = null;		
					}
				}

				if(getX() < columns-1 && s.getX() == getX()+1 && s.getY()==getY()) {
					if(s.getComponentType() == ComponentType.empty) {
						ec[3] = (EmptyCell)s;
					} else {
						ec[3] = null;		
					}
				}

				if(getY() < rows-1 && s.getY() == getY()+1 && s.getX()==getX()) {
					if(s.getComponentType() == ComponentType.empty) {
						ec[1] = (EmptyCell)s;
					} else {
						ec[1] = null;		
					}
				}

				if(getY() > 0 && s.getY() == getY()-1 && s.getX()==getX()) {
					if(s.getComponentType() == ComponentType.empty) {
						ec[0] = (EmptyCell)s;
					} else {
						ec[0] = null;		
					}
				}
				st+= s.getX() + "," + s.getY() + ": " + s.display() + "; ";
			}
			st = st.substring(0, st.length() - 2);        
			result += st + ",";
			if(orientation == Orientation.up) {
				if(ec[3] != null) 
					moveForward();
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.down) {
				if(ec[2] != null) 
					moveForward();
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.right) {
				if(ec[1] != null) 
					moveForward();
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
			else if(orientation == Orientation.left) {
				if(ec[0] != null) 
					moveForward();
				else {
					//randomOrientation();
					double d = Math.random();
					if(d < 0.5) {
						moveLeft(1);
						actionr = "turn_left";
					} else {
						moveRight(1);
						actionr = "turn_right";
					}
				}
			}
		}
		if(debug==2){
			try{
				writer.write(result + actionr); 
				writer.newLine();
				writer.flush();
			} catch(IOException ioe){
				System.out.println(ioe);
			}
		}
	}

	public void moveLeft(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.left;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.down;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.up;
			}
			else {
				orientation = Orientation.right;
			}
		}
	}

	public void moveRight(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.right;
			}
			if(orientation == Orientation.left) {
				orientation = Orientation.up;
			}
			if(orientation == Orientation.right) {
				orientation = Orientation.down;
			}
			else {
				orientation = Orientation.left;
			}
		}	
	}

	public void moveForward() {
		int xo = x;
		int yo = y;
		if(orientation == Orientation.up) {
			x += 1;
			x = Math.min(x,columns-1);
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,rows-1);
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}	
		JSONObject robotj = new JSONObject();
		robotj.put("name", name);
		robotj.put("id", ""+id);
		robotj.put("x", ""+x);
		robotj.put("y", ""+y);
		robotj.put("xo", ""+xo);
		robotj.put("yo", ""+yo);
		//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
		clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
	}

	public void moveBackward() {

	}
}