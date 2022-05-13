package environnementburger;

import components.SimulationComponent;
import model.*;
import components.Obstacle;
import main.TestAppli;
import mqtt.Message;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;


public class GridManagement implements SimulationComponent {
	protected Grid grid;
	private ArrayList<Goal> goals;
	private static final String turtlebotName = "burger_";
	protected int nbObstacles;
	protected int nbRobots;
	protected Message clientMqtt;
	protected String name;
	protected int debug;
	protected int rows;
	protected int columns;
	protected int display;
	protected int displaywidth;
	protected int displayheight;
	protected String displaytitle;
	protected Color colorrobot;
	protected Color colorobstacle;
	protected Color colorgoal;
	protected Color colorother;
	protected Color colorunknown;

	ColorGrid cg;
	
	protected int seed;

	public void initSubscribe() {
		clientMqtt.subscribe("robot/nextPosition");	
		clientMqtt.subscribe("configuration/nbRobot");	
		clientMqtt.subscribe("configuration/nbObstacle");	
		//clientMqtt.subscribe("configuration/nbRobot");
		clientMqtt.subscribe("configuration/seed");	
		clientMqtt.subscribe("configuration/display");	
		clientMqtt.subscribe("configuration/debug");	
		clientMqtt.subscribe("configuration/robot/grid");	
		clientMqtt.subscribe("robot/grid");
		clientMqtt.subscribe("robot/remove");
		clientMqtt.subscribe("environment/grid");
	}

	public void publishState(JSONObject content) {
		String robotName = (String)((JSONObject)content.get("robot")).get("id");
		//JSONObject state = giveState(content,robotName);
		//clientMqtt.publish(robotName+"/robot/state", state.toJSONString());	
	}

	public int isGoal(int x, int y) {
		for(Goal g : goals) {
			if(g.getX() == x && g.getY() == y)
				return g.getRobot();
		}
		return 0;
	}
	
	public void createColorGrid(int width, int height, String title){
		cg = new ColorGrid(width,height,grid.getColumns(),grid.getRows(), title);
		for(int i = 0; i < grid.getRows(); i++) {
			for(int j = 0; j < grid.getColumns(); j++) {
				Situated elt = grid.getCell(i, j);
				if(elt.getComponentType() == ComponentType.empty) {					
					if(isGoal(j,i) < 0) {
						cg.setBlockColor(j,i,colorgoal);
					} else {
						cg.setBlockColor(j,i,colorother);
					}
				}
				else if(elt.getComponentType() == ComponentType.robot)
					cg.setBlockColor(j,i,colorrobot);
				else if(elt.getComponentType() == ComponentType.obstacle)
					cg.setBlockColor(j,i,colorobstacle);
				else
					cg.setBlockColor(j,i,colorunknown);
			}
		}
		cg.init();
	}

	public GridManagement(){
		this.name = "grid";
		this.debug = 0;
		this.display = 0;
		goals = new ArrayList<Goal>();
	}

	public void publishGridSize(){
		// size of the the complete grid
		JSONObject gridsize = new JSONObject();
		gridsize.put("rows", ""+grid.getRows());
		gridsize.put("columns", ""+grid.getColumns());
		clientMqtt.publish("inform/grid/init", gridsize.toJSONString());
	}

	public void publishObstacles(){
		JSONArray ja = new JSONArray();
		List<Situated> lo = grid.get(ComponentType.obstacle);
		for (Situated ob:lo) {		
			JSONObject jo = new JSONObject();
			jo.put("x", ob.getX()+"");
			jo.put("y", ob.getY()+"");
			ja.add(jo);
		}
		JSONObject obst = new JSONObject();
		obst.put("obstacles", ja);
		clientMqtt.publish("inform/grid/obstacles", obst.toJSONString());
	}

	public void init() {
		for (int i = 0; i < nbObstacles; i++) {
			int[] pos = grid.locate();
			Obstacle obs = new Obstacle(pos);
			grid.putSituatedComponent(obs);			
		}
		if(display == 1) {
			createColorGrid(displaywidth, displayheight, displaytitle);			
		}						
	}

	public void initRoad(){
		File file = new File("src\\resources\\road10.png");
		try
		{
			BufferedImage img = ImageIO.read(file);
			int x = img.getWidth();
			int y = img.getHeight();
			int value = 0;
			for(int i=0; i<x; i++){
				for(int j=0; j<x; j++){
					value = img.getRGB(i,j);
					if(value!=-1){
						int[] pos = {j,i};
						Obstacle obs = new Obstacle(pos);
						grid.putSituatedComponent(obs);
					}
				}
			}
			if(display == 1) {
				createColorGrid(displaywidth, displayheight, displaytitle);
			}
		}
		catch (IOException e)
		{
			String workingDir = System.getProperty("user.dir");
			System.out.println("Current working directory : " + workingDir);
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public void setMessage(Message mqtt) {
		clientMqtt = mqtt;
	}
	
	public void refresh(){
		cg.refresh();
	}

	public boolean moveRobot(int id, int x1, int y1, int x2, int y2) {
		Situated elt = grid.getCell(y1, x1);
		if(elt.getComponentType() == ComponentType.robot) {
			RobotDescriptor eltR = (RobotDescriptor)elt;
			if(eltR.getId() == id) {
				grid.moveSituatedComponent(x1,y1,x2,y2);
				if(display == 1) { 
					cg.setBlockColor(x1,y1,colorother);
					cg.setBlockColor(x2,y2,colorrobot);
				}
				return true;
			}
		}
		return false;
	}
	
	public void displayGrid(){
		grid.display();
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject gridToJSONObject(int x, int y, int field) {
			
		// size of the the complete grid
		JSONObject jsongrid = new JSONObject();
		
		// obstacles definition
		
		JSONArray gt = new JSONArray();
		int xm = Math.max(x-field,0);
		int xM = Math.min(x+field,grid.getColumns()-1);
		int ym = Math.max(y-field,0);
		int yM = Math.min(y+field,grid.getRows()-1);

		//System.out.println("field " + field + " x " + x + " y " + y);
		//System.out.println("xm " + xm + " xM " + xM + " ym " + ym + " yM "+ yM);
		for (int i=0; i<grid.getRows(); i++) {
			for(int j = 0; j<grid.getColumns(); j++) {
				Situated s = grid.getCell(j,i);
				if(s.getComponentType()==ComponentType.obstacle) {
					JSONObject jo = new JSONObject();
					jo.put("type", s.getComponentType()+"");
					jo.put("x", s.getX()+"");
					jo.put("y", s.getY()+"");
					gt.add(jo);
				}
				if(s.getComponentType()==ComponentType.empty) {
					EmptyValuedCell emptyValuedCell = (EmptyValuedCell) s;
					JSONObject jo = new JSONObject();
					jo.put("type", s.getComponentType()+"");
					jo.put("x", s.getX()+"");
					jo.put("y", s.getY()+"");
					jo.put("value",emptyValuedCell.getValue());
					gt.add(jo);
				}
			}
		}
		for(int i = xm; i <= xM; i++){
			for(int j = ym; j <= yM; j++){
				if(i != x || j != y) {
					Situated s = grid.getCell(j,i);
					//System.out.println("j " + j + " i " + i + " cell " + s);
					JSONObject jo = new JSONObject();
					jo.put("type", s.getComponentType()+"");
					if(s.getComponentType() == ComponentType.robot) {
						RobotDescriptor rd = (RobotDescriptor)s;
						jo.put("name", rd.getName());	
						jo.put("id", rd.getId()+"");	
					}
					jo.put("x", s.getX()+"");
					jo.put("y", s.getY()+"");
					gt.add(jo);
				}
			}
		}

		jsongrid.put("x", x);
		jsongrid.put("y", y);
		jsongrid.put("field", field);
		jsongrid.put("cells", gt);
		return jsongrid;
	}
	
	public void publishInitRobot() {
		List<Situated> ls = grid.get(ComponentType.robot);
		for(Situated s:ls){
			RobotDescriptor rb = (RobotDescriptor)s;
			JSONObject jo = new JSONObject();
			jo.put("name", rb.getName());
			jo.put("id", rb.getId()+"");
			jo.put("x", rb.getX()+"");
			jo.put("y", rb.getY()+"");
			clientMqtt.publish(rb.getName()+"/position/init", jo.toJSONString());
		}
	}

	public void handleMessage(String topic, JSONObject content){
		//System.out.println("Message:"+ content.toJSONString());
		if (topic.contains("robot/nextPosition")) {
			//System.out.println("UPDATE ROBOT");
			String rn = (String)content.get("name");
			int idr = Integer.parseInt((String)content.get("id"));
			int xr = Integer.parseInt((String)content.get("x"));
			int yr = Integer.parseInt((String)content.get("y"));
			int xor = Integer.parseInt((String)content.get("xo"));
			int yor = Integer.parseInt((String)content.get("yo"));
			//System.out.println("MOVE MOVE " + xor + " " + yor + " --> " + xr + " " + yr);
			grid.moveSituatedComponent(xor,yor,xr,yr);
			if(display == 1) {
				//cg.setBlockColor(xor, yor, colorother);				
				if(isGoal(xor,yor)<0) {
					cg.setBlockColor(xor,yor,colorgoal);
				} else {
					cg.setBlockColor(xor,yor,colorother);
				}
				cg.setBlockColor(xr, yr, colorrobot);
				cg.refresh();
			}
			if(debug == 1) {
				grid.display();
			}
		}else if (topic.contains("configuration/nbRobot")) {
           	nbRobots = Integer.parseInt((String)content.get("nbRobot"));
           	for(int i = 2; i < nbRobots+2; i++) {
           		int[] pos = grid.locate();           	
				grid.putSituatedComponent(new RobotDescriptor(pos, i, GridManagement.turtlebotName+i));
				if(display == 1) {
					cg.setBlockColor(pos[0], pos[1], colorrobot);
					cg.refresh();
				}				
			}
			for (int i = 2; i < nbRobots+2; i++) {
				int[] pos = grid.locateGoal();
				// ec = (EmptyCell)grid.getCell(pos[1], pos[0]);
				goals.add(new Goal(pos[0],pos[1],-1*i));
				grid.forceSituatedComponent(new EmptyValuedCell(pos[0], pos[1],1));
				if(display == 1) {
					cg.setBlockColor(pos[0], pos[1], colorgoal);
					cg.refresh();
				}	
			}
			if(debug == 1) {
				grid.display(goals);
			}
        } else if (topic.contains("configuration/robot/grid")) {
            String nameR = (String)content.get("name");
            int fieldr = Integer.parseInt((String)content.get("field"));
            int xr = Integer.parseInt((String)content.get("x"));
            int yr = Integer.parseInt((String)content.get("y"));
            JSONObject jo = gridToJSONObject(xr, yr, fieldr);
			int id = Integer.parseInt((String)content.get("id"));
			for (Goal goal : this.goals) {
				if (goal.getRobot()==-id) {
					jo.put("xGoal",goal.getX()+"");
					jo.put("yGoal",goal.getY()+"");
				}
			}
            clientMqtt.publish(nameR+"/grid/init", jo.toJSONString());
        } else if (topic.contains("robot/remove")) {
			int xGoal = Integer.parseInt((String)content.get("xGoal"));
			int yGoal = Integer.parseInt((String)content.get("yGoal"));
			for(Goal goal: goals){
				if(goal.getX()==xGoal && goal.getY()==yGoal) goals.remove(goal);
			}
			grid.removeSituatedComponent(xGoal,yGoal);
			cg.setBlockColor(xGoal, yGoal, colorother);
			cg.refresh();
		}
        else if (topic.contains("robot/grid")) {
            String nameR = (String)content.get("name");
            int fieldr = Integer.parseInt((String)content.get("field"));
            int xr = Integer.parseInt((String)content.get("x"));
            int yr = Integer.parseInt((String)content.get("y"));
            JSONObject jo = gridToJSONObject(xr, yr, fieldr);
            clientMqtt.publish(nameR+"/grid/update", jo.toJSONString());
        }
		else if (topic.contains("robot/nextPosition")) {
            publishState(content);
        } 
		else if (topic.contains("configuration/debug")) {
    	    debug = Integer.parseInt((String)content.get("debug"));
        }
        else if (topic.contains("configuration/display")) {
    	    display = Integer.parseInt((String)content.get("display"));
    	    if(display==1){
   				clientMqtt.subscribe("display/width");
				clientMqtt.subscribe("display/height");
				clientMqtt.subscribe("display/title");
				clientMqtt.subscribe("display/robot");
				clientMqtt.subscribe("display/goal");
				clientMqtt.subscribe("display/obstacle");
				clientMqtt.subscribe("display/other");
				clientMqtt.subscribe("display/unknown");
    	    }
        }
        else if (topic.contains("configuration/seed")) {
    	    seed = Integer.parseInt((String)content.get("seed"));
        }
        else if (topic.contains("configuration/nbObstacle")) {
    	    nbObstacles = Integer.parseInt((String)content.get("nbObstacle"));
        }
        else if (topic.contains("environment/grid")) {
    	    rows = Integer.parseInt((String)content.get("rows"));
    	    columns = Integer.parseInt((String)content.get("columns"));
    	    grid = new Grid(rows, columns, seed);
			grid.initEmpty();
			//initRoad();
			initRoad();
        }
        /*else if(topic.contains("burger_5/position")) {
        	int x1 = Integer.parseInt((String)content.get("x1"));
        	int y1 = Integer.parseInt((String)content.get("y1"));
        	int x2 = Integer.parseInt((String)content.get("x2"));
        	int y2 = Integer.parseInt((String)content.get("y2"));
            moveRobot(5,x1,y1,x2,y2);
            if(display == 1)
				refresh();
        } */       
        else if(display == 1) {
			if (topic.contains("display/width")) {
    	        displaywidth = Integer.parseInt((String)content.get("displaywidth"));
        	}
			else if (topic.contains("display/height")) {
            	displayheight = Integer.parseInt((String)content.get("displayheight"));
        	}
			else if (topic.contains("display/title")) {
            	displaytitle = (String)content.get("displaytitle");
        	}
        	else if (topic.contains("display/robot")) {
            	colorrobot = new Color(Integer.parseInt((String)content.get("color")));
        	}
        	else if (topic.contains("display/goal")) {
            	colorgoal = new Color(Integer.parseInt((String)content.get("color")));
        	}
        	else if (topic.contains("display/obstacle")) {
            	colorobstacle = new Color(Integer.parseInt((String)content.get("color")));
        	}
        	else if (topic.contains("display/other")) {
            	colorother = new Color(Integer.parseInt((String)content.get("color")));
        	}
        }		
	}
}
