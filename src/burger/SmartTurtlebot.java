package burger;

import components.Obstacle;
import model.*;
import components.Turtlebot;
import mqtt.Message;

import java.util.ArrayList;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;
import java.io.IOException;

public class SmartTurtlebot extends Turtlebot{
	protected Random rnd;
	protected Grid grid;
	protected int xGoal, yGoal;
    protected ArrayList<aStarNode> path = null;
	protected double cellValue = 1.0;

	public double getCellValue() {
		return cellValue;
	}

	public SmartTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		rnd = new Random(seed);	
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
      		JSONArray ja = (JSONArray)content.get("cells");
      		List<Situated> ls = grid.get(ComponentType.robot);
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
           		if(typeCell.equals("robot")) {
           			int idr = Integer.parseInt((String)jo.get("id"));
           			boolean findr = false;
           			for(Situated sss:ls) {
           				if(sss != this){
	           				RobotDescriptor rd = (RobotDescriptor)sss;
    	       				if(rd.getId() == idr) {
        	   					grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo, rd.getValueCell());
           						findr = true;
           						break;
           					}
           				}
           			}
           			if(!findr) {
	           			String namer = (String)jo.get("name");
						double value = Double.parseDouble((String)jo.get("value"));
    	    			grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer, value));
    	    		}
        		} else {
        			Situated sg = grid.getCell(yo,xo);
        			Situated s;
					if(typeCell.equals("obstacle")){
						//System.out.println("Add ObstacleCell");
						s = new ObstacleDescriptor(to);
					} else {
						//System.out.println("Add EmptyCell " + xo + ", " + yo);
						//s = new EmptyValuedCell(xo,yo, 1);
						double value = (Double)jo.get("value");
						s = new EmptyValuedCell(xo,yo,value);
					}
					grid.forceSituatedComponent(s);
    			}
    		}
      		if(debug == 1 && this.id!=3) {
		   		System.out.println("---- " + name + " ----");
        		grid.display();
        	}
        } else if (topic.contains(name+"/action")) {
			if(x==xGoal && y==yGoal){
				goalReached = true;
				JSONObject removeRobot = new JSONObject();
				removeRobot.put("id", id + "");
				removeRobot.put("xGoal", xGoal + "");
				removeRobot.put("yGoal", yGoal + "");
				clientMqtt.publish("robot/remove", removeRobot.toJSONString());
			}
			if(!goalReached){
				int stepr = Integer.parseInt((String)content.get("step"));
				move(stepr);
			}
        } else if (topic.contains("inform/grid/init")) {
        	int rows = Integer.parseInt((String)content.get("rows"));
        	int columns = Integer.parseInt((String)content.get("columns"));
        	grid = new Grid(rows, columns, seed);
			grid.initUnknown();
		    grid.forceSituatedComponent(this);
		}
        else if (topic.contains(name+"/position/init")) {
      		x = Integer.parseInt((String)content.get("x"));
        	y = Integer.parseInt((String)content.get("y"));
			double value = Double.parseDouble((String)content.get("value"));
			this.cellValue = value;
        }
        else if (topic.contains(name+"/grid/init")) {
			this.xGoal = Integer.parseInt((String)content.get("xGoal"));
			this.yGoal = Integer.parseInt((String)content.get("yGoal"));
      		JSONArray ja = (JSONArray)content.get("cells");
      		for(int i=0; i < ja.size(); i++) {
      			JSONObject jo = (JSONObject)ja.get(i);
	        	String typeCell = (String)jo.get("type");
    	    	int xo = Integer.parseInt((String)jo.get("x"));
    	    	int yo = Integer.parseInt((String)jo.get("y"));
        		int[] to = new int[]{xo,yo};
        		Situated s;
				if(typeCell.equals("obstacle")){
					//System.out.println("Add ObstacleCell");
        			s = new ObstacleDescriptor(to);
        		} else if(typeCell.equals("robot")){
        			//System.out.println("Add RobotCell");
        			int idr = Integer.parseInt((String)jo.get("id"));
        			String namer = (String)jo.get("name");
					double value = Double.parseDouble((String)jo.get("value"));
        			s = new RobotDescriptor(to, idr, namer, value);
        		} else if (typeCell.equals("empty")) {
					double value = (Double)jo.get("value");
					s = new EmptyValuedCell(xo, yo, value);
				} else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			//s = new EmptyValuedCell(xo,yo, 1);
					s = new EmptyValuedCell(xo,yo,1);
        		}
        		grid.forceSituatedComponent(s);
      		}
        }
	}

	public void setLocation(int x, int y) {
		int xo = this.x;
		int yo = this.y;
		this.x = x;
		this.y = y;
		grid.moveSituatedComponent(xo, yo, x, y);
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

/*	public void randomOrientation() {
		Random generator = new Random(0);
		double d = generator.nextDouble();
		Orientation oldo = orientation;
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
*/
	public void newPath() {
		aStarSearch search = new aStarSearch();
		this.path = search.solve(this.grid, this.x, this.y, this.xGoal, this.yGoal);
	}

	public void move(int step) {
		if (this.path==null) {
			newPath();
			this.path.remove(0); //current node
		}
		Random generator = new Random();
		String actionr = "move_forward";
		String result = x + "," + y + "," + orientation + "," + grid.getCellsToString(y,x) + ",";
		for(int i = 0; i < step; i++) {
			//Integer distanceMin=Integer.MAX_VALUE;
			//Integer distanceTest;
			Integer jmin=null;
			EmptyCell emptyCellMin=null;
			EmptyCell[] ec = grid.getAdjacentEmptyCell(x,y);


			aStarNode nextNode = this.path.get(0);
			for (int j=0; j<ec.length; j++) {
				EmptyCell ecTest = ec[j];
				if (ecTest != null) {

					if (ecTest.getX()==nextNode.getNode().getX() && ecTest.getY()==nextNode.getNode().getY()) {
						jmin=j;
					}



					/*distanceTest = Math.abs(ecTest.getX()-this.xGoal)+ Math.abs(ecTest.getY()-this.yGoal);
					if (distanceTest<distanceMin) {
						distanceMin=distanceTest;
						emptyCellMin=ecTest;
						jmin=j;
					}*/
				}
			}
			if(jmin==null){
				int ymax = grid.getRows()-1;
				int xmax = grid.getColumns()-1;
				if(orientation == Orientation.up) {
					if(y<ymax){
						if(grid.getCell(y+1,x)==null || grid.getCell(y+1,x) instanceof Obstacle){
							//je sais pas trop quoi faire
							actionr = randomOrientation(generator);
						}else{
							moveBackward();
						}
					}else{
						moveBackward();
					}
				}

				else if(orientation == Orientation.down) {
					if(y>0){
						if(grid.getCell(y+1,x)==null || grid.getCell(y-1,x) instanceof Obstacle){
							//je sais pas trop quoi faire
							actionr = randomOrientation(generator);
						}else{
							moveBackward();
						}
					}else{
						moveBackward();
					}
				}

				else if(orientation == Orientation.right) {
					if(x>0){
						if(grid.getCell(y+1,x)==null || grid.getCell(y,x-1) instanceof Obstacle){
							//je sais pas trop quoi faire
							actionr = randomOrientation(generator);
						}else{
							moveBackward();
						}
					}else{
						moveBackward();
					}
				}

				else if(orientation == Orientation.left) {
					if(x<xmax){
						if(grid.getCell(y+1,x)==null || grid.getCell(y,x+1) instanceof Obstacle){
							//je sais pas trop quoi faire
							actionr = randomOrientation(generator);
						}else{
							moveBackward();
						}
					}else{
						moveBackward();
					}
				}



			}else{
				if(orientation == Orientation.up) {
					if(jmin==3) {
						moveForward();
					}
					else {
						actionr = randomOrientation(generator);
					}
				}
				else if(orientation == Orientation.down) {
					if(jmin==2) {
						moveForward();
					}
					else {
						actionr = randomOrientation(generator);
					}
				}
				else if(orientation == Orientation.right) {
					if(jmin==1) {
						moveForward();
					}
					else {
						actionr = randomOrientation(generator);
					}
				}
				else if(orientation == Orientation.left) {
					if(jmin ==0) {
						moveForward();
					}
					else {
						actionr = randomOrientation(generator);
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
	}

	private String randomOrientation(Random generator){
		double d = generator.nextDouble();
		String actionr;
		if(d < 0.5) {
			moveLeft(1);
			actionr = "turn_left";
		} else {
			moveRight(1);
			actionr = "turn_right";
		}
		return actionr;
	}

	public void moveLeft(int step) {
		Orientation oldo = orientation;
		for(int i = 0; i < step; i++){
			if(orientation == Orientation.up) {
				orientation = Orientation.left;
			}
			else if(orientation == Orientation.left) {
				orientation = Orientation.down;
			}
			else if(orientation == Orientation.right) {
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
			else if(orientation == Orientation.left) {
				orientation = Orientation.up;
			}
			else if(orientation == Orientation.right) {
				orientation = Orientation.down;
			}
			else {
				orientation = Orientation.left;
			}
		}	
	}

	public void moveForward() {
		this.path.remove(0);
		int xo = x;
		int yo = y;
		if(orientation == Orientation.up) {
			x += 1;
			x = Math.min(x,grid.getColumns()-1);
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,grid.getRows()-1);
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}
		double nextValue = 1.0;
		if(grid.getCell(y,x) instanceof EmptyValuedCell){
			nextValue = ((EmptyValuedCell) grid.getCell(y,x)).getValue();
		}
		grid.moveSituatedComponent(xo,yo,x,y,cellValue);
		JSONObject robotj = new JSONObject();
		robotj.put("name", name);
		robotj.put("id", ""+id);
		robotj.put("x", ""+x);
		robotj.put("y", ""+y);
		robotj.put("xo", ""+xo);
		robotj.put("yo", ""+yo);
		robotj.put("value", ""+cellValue);
		cellValue = nextValue;
		//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
		clientMqtt.publish("robot/nextPosition", robotj.toJSONString());
	}




	public void moveBackward() {
		this.path.add(0,new aStarNode((EmptyValuedCell) grid.getCell(y,x)));
		int xo = x;
		int yo = y;
		if(orientation == Orientation.up) {
			x += 1;
			x = Math.min(x,grid.getColumns()-1);
		}
		else if(orientation == Orientation.left) {
			y -= 1;
			y = Math.max(y,0);
		}
		else if(orientation == Orientation.right) {
			y += 1;
			y = Math.min(y,grid.getRows()-1);
		}
		else {
			x -= 1;
			x = Math.max(x,0);
		}
		double nextValue = 1.0;
		if(grid.getCell(y,x) instanceof EmptyValuedCell){
			nextValue = ((EmptyValuedCell) grid.getCell(y,x)).getValue();
		}
		cellValue+=3; //It takes longer to drive backward
		grid.moveSituatedComponent(xo,yo,x,y,cellValue);
		JSONObject robotj = new JSONObject();
		robotj.put("name", name);
		robotj.put("id", ""+id);
		robotj.put("x", ""+x);
		robotj.put("y", ""+y);
		robotj.put("xo", ""+xo);
		robotj.put("yo", ""+yo);
		robotj.put("value", ""+cellValue);
		cellValue = nextValue;
		//System.out.println("MOVE MOVE " + xo + " " + yo + " --> " + x + " " + y);
		clientMqtt.publish("robot/nextPosition", robotj.toJSONString());


	}
}