package burger;

import model.ComponentType;
import model.Situated;
import components.Turtlebot;
import model.EmptyCell;
import model.UnknownCell;
import model.Grid;

import mqtt.Message;
import rosbridgeConnection.RosbridgeClient;

import model.ObstacleDescriptor;
import model.RobotDescriptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;

public class RealTurtlebot extends Turtlebot{
	protected Grid grid;
	protected RosbridgeClient clientRosbridge;
	public static int waitTimeCommunication = 300;
	public static int waitTimeAction= 4000;
	public static String ip = "10.200.3.101"; //"10.3.143.1";
	public static String port = "9090";

	public RealTurtlebot(int id, String name, int seed, int field, Message clientMqtt, int debug) {
		super(id, name, seed, field, clientMqtt, debug);
		clientRosbridge = new RosbridgeClient(RealTurtlebot.ip, RealTurtlebot.port);
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
        	   					grid.moveSituatedComponent(rd.getX(), rd.getY(), xo, yo);
           						findr = true;
           						break;
           					}
           				}
           			}
           			if(!findr) {
	           			String namer = (String)jo.get("name");
    	    			grid.forceSituatedComponent(new RobotDescriptor(to, idr, namer, 1.0));
    	    		}
        		} else {
        			Situated sg = grid.getCell(yo,xo);
        			Situated s;
        			if(sg.getComponentType() == ComponentType.unknown) {
        				if(typeCell.equals("obstacle")){
							//System.out.println("Add ObstacleCell");
        					s = new ObstacleDescriptor(to);
        				} else {
        					//System.out.println("Add EmptyCell " + xo + ", " + yo);
        					s = new EmptyCell(xo,yo);
        				}
        				grid.forceSituatedComponent(s);
    				}
    			}
    		}
      		if(debug == 1) {
		   		System.out.println("---- " + name + " ----");
        		grid.display();
        	}
        } else if (topic.contains(name+"/action")) {
    	    int stepr = Integer.parseInt((String)content.get("step"));
        	move(stepr);
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
        }
        else if (topic.contains(name+"/grid/init")) {
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
        		}
        		else if(typeCell.equals("robot")){
        			//System.out.println("Add RobotCell");
        			int idr = Integer.parseInt((String)jo.get("id"));
        			String namer = (String)jo.get("name");
        			s = new RobotDescriptor(to, idr, namer, 1.0);
        		}
        		else {
        			//System.out.println("Add EmptyCell " + xo + ", " + yo);
        			s = new EmptyCell(xo,yo);
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

	public String display(){
		return super.toString();
	}

	public boolean isGoalReached(){
		return false;
	}

	public void move(int step) {
		System.out.println(step);
	}

	public void moveForward() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/robot_command");
		JSONObject msg = new JSONObject();
		msg.put("data", "forward");
		message.put("msg", msg);
		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());
		System.out.println("Message ROS" + message.toJSONString());
		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void moveBackward() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/robot_command");
		JSONObject msg = new JSONObject();
		msg.put("data", "backward");
		message.put("msg", msg);
		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());

		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* Rotate the robot to the left */
	public void moveLeft(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/robot_command");
			JSONObject msg = new JSONObject();
			msg.put("data", "left");
			message.put("msg", msg);
			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			try {
				Thread.sleep(RealTurtlebot.waitTimeAction);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/* Rotate the robot to the right */
	public void moveRight(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/robot_command");
			JSONObject msg = new JSONObject();
			msg.put("data", "right");
			message.put("msg", msg);
			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			try {
				Thread.sleep(RealTurtlebot.waitTimeAction);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	/* Stop the robot */
	public void stopRobot() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/cmd_vel");
		JSONObject msg = new JSONObject();
		JSONObject linear = new JSONObject();
		linear.put("x", 0.0);
		linear.put("y", 0.0);
		linear.put("z", 0.0);
		JSONObject angular = new JSONObject();
		angular.put("x", 0.0);
		angular.put("y", 0.0);
		angular.put("z", 0.0);

		msg.put("linear", linear);
		msg.put("angular", angular);

		message.put("msg", msg);

		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());
	}	

	/* Move the robot straight */
	public void moveRobot() {
		JSONObject message = new JSONObject();
		message.put("topic", "/" + name + "/cmd_vel");
		JSONObject msg = new JSONObject();
		JSONObject twist1 = new JSONObject();
		JSONObject linear = new JSONObject();
		linear.put("x", 0.040);
		linear.put("y", 0.0);
		linear.put("z", 0.0);
		JSONObject angular = new JSONObject();
		angular.put("x", 0.0);
		angular.put("y", 0.0);
		angular.put("z", 0.0);

		twist1.put("linear", linear);
		twist1.put("angular", angular);

		msg.put("linear", linear);
		msg.put("angular", angular);

		message.put("msg", msg);

		message.put("op", "publish");

		clientRosbridge.getWsc().send(message.toJSONString());

		try {
			Thread.sleep(RealTurtlebot.waitTimeAction);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stopRobot();
	}

	/* Rotate the robot to the left */
	public void rotateLeft(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/cmd_vel");
			JSONObject msg = new JSONObject();
			JSONObject linear = new JSONObject();
			linear.put("x", 0.0);
			linear.put("y", 0.0);
			linear.put("z", 0.0);
			JSONObject angular = new JSONObject();
			angular.put("x", 0.0);
			angular.put("y", 0.0);
			angular.put("z", 1.0);

			msg.put("linear", linear);
			msg.put("angular", angular);

			message.put("msg", msg);

			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			/*
			 * Time required to rotate the robot 90 degrees while doing 1m/s is 1.6 seconds
			 */
			try {
				Thread.sleep(RealTurtlebot.waitTimeCommunication);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stopRobot();
		}
	}

	/* Rotate the robot to the right */
	public void rotateRight(int step) {
		for (int i = 0; i < step; i++) {
			JSONObject message = new JSONObject();
			message.put("topic", "/" + name + "/cmd_vel");
			JSONObject msg = new JSONObject();
			JSONObject linear = new JSONObject();
			linear.put("x", 0.0);
			linear.put("y", 0.0);
			linear.put("z", 0.0);
			JSONObject angular = new JSONObject();
			angular.put("x", 0.0);
			angular.put("y", 0.0);
			angular.put("z", -1.0);

			msg.put("linear", linear);
			msg.put("angular", angular);

			message.put("msg", msg);

			message.put("op", "publish");

			clientRosbridge.getWsc().send(message.toJSONString());

			/*
			 * Time required to rotate the robot 90 degrees while doing 1m/s is 1.6 seconds
			 */
			try {
				Thread.sleep(RealTurtlebot.waitTimeCommunication);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stopRobot();
		}
	}
}