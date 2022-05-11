package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* This model class defines the grid components */
public class Grid {
    private Situated[][] grid;    
    protected final int rows;
    protected final int columns;
    protected int nbRobots;
    protected int nbObstacles;
    private Random rnd;
    
    public Grid(int rows, int columns, int seed) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Situated[rows][columns];
        nbRobots = 0;
        nbObstacles = 0;
        rnd = new Random(seed);
    }

    public int[] locate() {
        int l=-1, c = -1;
        boolean locationNotFound = true; 
        while (locationNotFound) {
            l = rnd.nextInt(rows);
            c = rnd.nextInt(columns);
            if (grid[l][c].getComponentType() == ComponentType.empty) {
                locationNotFound = false;
                //grid[l][c] = nb; 
            }
        }
        int [] result = {c,l};
        return result;
    }

    public void initUnknown(){
        for(int i=0; i < rows;i++){
            for(int j=0; j < columns; j++){
                grid[i][j]=new UnknownCell(j,i);
            }
        }
    }

    public void display(ArrayList<Goal> gl){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Situated s = getCell(i, j); 
                if(s.getComponentType() == ComponentType.empty) {
                    boolean tt = false;
                    for(Goal g : gl) {
                        if(g.getX() == j && g.getY() == i) {
                            System.out.print("   " + g.getRobot());
                            tt = true;
                        }
                    }
                    if(!tt){
                        System.out.print("   " + s.display());
                    }
                } else {
                    System.out.print("   " + s.display());
                }
            }
            System.out.println();       
        }
    }

    public void display(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) 
                System.out.print("   " + getCell(i, j).display());
            System.out.println();       
        }
    }

    public void initEmpty(){
        for(int i=0; i < rows;i++){
            for(int j=0; j < columns; j++){
                //grid[i][j]=new EmptyValuedCell(j,i,1);
                grid[i][j]=new EmptyValuedCell(j,i,1);
            }
        }
    }

    public EmptyCell[] getAdjacentEmptyCell(int x, int y) {
        EmptyCell[] ls = new EmptyCell[4];
        ls[0] = null;
        ls[1] = null;
        ls[2] = null;
        ls[3] = null;
        Situated s;
        if(y>0){
           s = grid[y-1][x];
            if(s.getComponentType() == ComponentType.empty) {
                ls[0] = (EmptyCell)s;
            }
        }
        if(y<rows-1) {
            s = grid[y+1][x];
            if(s.getComponentType() == ComponentType.empty) {
                ls[1] = (EmptyCell)s;
            }
        }
        if(x > 0){
            s = grid[y][x-1];
            if(s.getComponentType() == ComponentType.empty) {
                ls[2] = (EmptyCell)s;
            }
        }
        if(x<columns-1){
            s = grid[y][x+1];
            if(s.getComponentType() == ComponentType.empty) {
                ls[3] = (EmptyCell)s;
            }
        }
        return ls;
    }
       
    public List<Situated> adjacentEmptyCell(int x, int y) {
        List<Situated> adjacentPos = new ArrayList<Situated>();
        Situated si;
        if(validCoordinate(x,y)){
            if (x > 0) {
                si = grid[y][x-1];
                if (si.getComponentType() == ComponentType.empty) 
                    adjacentPos.add(si);            
            } 
            if (y > 0) {
                si = grid[y-1][x];
                if (si.getComponentType() == ComponentType.empty) 
                    adjacentPos.add(si);  
            }
            if (x < columns - 1) {
                si = grid[y][x+1];
                if (si.getComponentType() == ComponentType.empty) 
                    adjacentPos.add(si);                          
            }
            if (y < rows - 1) {
                si = grid[y+1][x];
                if (si.getComponentType() == ComponentType.empty) 
                    adjacentPos.add(si);  
            }
        }
        return adjacentPos;
    }
    

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Situated[][] getGrid() {
        return grid;
    }
    
    public Situated getCell(int r, int c) {
    	return grid[r][c];
    }

    public String getCellsToString(int r, int c) {
        String st = "[";
        for(int i=0;i < 3;i++) {
            for(int j=0;j < 3;j++) {
                if((r-1+i)<0) {
                    st += j + "," + i + ": null; ";
                }
                else if((r-1+i) >= rows) {
                    st += j + "," + i + ": null; ";
                } 
                else if((c-1+j)<0) {
                    st += j + "," + i + ": null; ";
                }
                else if((c-1+j) >= columns) {
                    st += j + "," + i + ": null; ";
                } else {
                    st += j + "," + i + ": " + grid[(r-1+i)][(c-1+j)].display() + "; ";
                }
            }    
        }
        st = st.substring(0, st.length() - 2);
        return st+"]";
    }
    
    public boolean validCoordinate(int x, int y) {
    	return (x >= 0 && y >= 0 && x < columns && y < rows);
    }

    /**
     * move the SituatedComponent from (ox,oy) to (dx, dy)
     * @param ox origin abscissa of the situated component
     * @param oy  origin ordinate of the situated component
     * @param dx destination abscissa of the situated component
     * @param dy  destination ordinate of the situated component
     * @return true if the move has been done false else
     */
	public boolean moveSituatedComponent(int ox, int oy, int dx, int dy) {
		Situated sc = removeSituatedComponent(ox,oy);
		if (sc != null) {
			sc.setLocation(dx,dy);
			putSituatedComponent(sc);
			return true;
		}
		return false;
	}
	
	public boolean putSituatedComponent(Situated sc) {		
		if (validCoordinate(sc.getX(), sc.getY()) && grid[sc.getY()][sc.getX()].getComponentType() == ComponentType.empty) {
			grid[sc.getY()][sc.getX()] = sc;
            if(sc.getComponentType() == ComponentType.robot)
                nbRobots++;
            else if(sc.getComponentType() == ComponentType.obstacle)
                nbObstacles++;
			return true;
		}		
    	return false;
	}

    public boolean forceSituatedComponent(Situated sc) {      
        if (validCoordinate(sc.getX(), sc.getY())) {
            grid[sc.getY()][sc.getX()] = sc;
            if(sc.getComponentType() == ComponentType.robot)
                nbRobots++;
            else if(sc.getComponentType() == ComponentType.obstacle)
                nbObstacles++;
            return true;
        }       
        return false;
    }
	
	public Situated removeSituatedComponent(int x, int y) {
		if (validCoordinate(x, y) && grid[y][x].getComponentType() != ComponentType.empty) {
			Situated sc = grid[y][x];
			//grid[y][x] = new EmptyValuedCell(x, y, 1);
            grid[y][x] = new EmptyValuedCell(x,y,1);
            if(sc.getComponentType() == ComponentType.robot)
                nbRobots--;
            else if(sc.getComponentType() == ComponentType.obstacle)
                nbObstacles--;
			return sc;
		}		
    	return null;
	}

    public List<Situated> get(ComponentType ct){
        List<Situated> result = new ArrayList<Situated>();
        for(int i=0; i < rows; i++) {
            for(int j=0; j < columns; j++) {
                Situated elt = grid[i][j];
                if(elt.getComponentType() == ct)
                    result.add(elt);
            }   
        }
        return result;
    }
}
