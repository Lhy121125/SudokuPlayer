/*
 * SudokuPlayer.java
 * Ray Zeng(Tianrui) & Nick Luo(Haiyu)
 * All group members were present and contributing during all work on this project.
 * We have neither received nor given any unauthorized aid in this assignment.
 */
package hw2;
import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;



    /// --- AC-3 Constraint Satisfication --- ///


    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();
    
    
    //Board of heuristic
    int[] heuristic = new int[81];
    //Array keep track of the frequency of values
    int[] frequency = new int[10];


	/*
	 * This method sets up the data structures and the initial global constraints
	 * (by calling allDiff()) and makes the initial call to backtrack().
	 * You should not change this method header.
 	 */
    private final void AC3Init(){
        //Do NOT remove these lines (required for the GUI)
        board.Clear();
		recursions = 0;


        /**
         *  YOUR CODE HERE:
         *  Create Data structures ( or populate the ones defined above ).
         *  These will be the data structures necessary for AC-3.
         **/

        for(int i = 0; i < 81; i++) {
        	int x = i / 9;
        	int y = i % 9;
        	
        	/*filled in the global domain of i*/
        	//already filled in;
            if(vals[x][y] != 0) {
            	globalDomains[i] = new ArrayList<Integer>();
            	globalDomains[i].add(vals[x][y]);
            }else {//filled in from 1 to 9;
	        	for(int k = 1; k < 10; k++) {
	        			if(globalDomains[i]==null) {
	        				globalDomains[i] = new ArrayList<Integer>();
	        			 }
	        			globalDomains[i].add(k);
	        	}
            }
        }
        
        //Fill globalQueue and neighbors with same row
        for(int r = 0; r < 9; r++) {
            /* create input parametes for neighbors and globalQueue*/
        	int[] row = new int[9];
        	for(int c = 0; c < 9; c++) {
        		row[c] = r * 9 + c;
        	}
        	allDiff(row);
        }
        
      //Fill globalQueue and neighbors with same col
        for(int c = 0; c < 9; c++) {
            /* create input parametes for neighbors and globalQueue*/
        	int[] col = new int[9];
        	for(int r = 0; r < 9; r++) {
        		col[r] = r * 9 + c;
        	}
        	allDiff(col);
        }
        
      //Fill globalQueue and neighbors with same box
        int upLeft = -21;
        for(int c = 0; c < 3; c++) {
        	upLeft += 18;//24
        	
        	for(int r = 0; r< 3; r++) {
        		upLeft += 3; //0 3 6

            	/*fill in subGroup*/
            	int[] subGroup = new int[9];
            	for(int i = 0; i < 9; i++) {//6
                 // i 4
            	  subGroup[i] = upLeft + i/3 * 9 + i % 3;//6 7 8 15 16
            	}
            	allDiff(subGroup);
        	}
        }

         // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0, globalDomains);

        // Prints evaluation of run
        Finished(success);

    }



    /*
     *  This method defines constraints between a set of variables.
     *  Refer to the book for more details. You may change this method header.
     */
    private final void allDiff(int[] all){
        // YOUR CODE HERE
    	for(int pos : all) { 
    		for(int i : all) {
    	       if(i == pos) continue;
    	       if(neighbors[pos]==null) neighbors[pos] = new ArrayList<Integer>();
    	       neighbors[pos].add(i);
    	       globalQueue.add(new Arc(pos, i));
    		}
    	}
    }



    /*
     * This is the backtracking algorithm. If you change this method header, you will have
     * to update the calls to this method.
     */
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {

    	//Do NOT remove
    	recursions +=1;
    	//int j = 0;
    	
   	 //System.out.
    	
    	if(cell > 80) return true;
        //If already set
    	if(vals[cell / 9][cell % 9] != 0){
    		return backtrack(cell + 1, Domains);
    	}
    	
    	 //Deep copy the domain
	   	 ArrayList<Integer>[] copy = new ArrayList[81];
	   	 for(int i = 0; i < 81; i++) {
	   		 copy[i] = new ArrayList<Integer>(Domains[i]);
	   	 }
    	
	   //fail change copy back to Domain
 		//but here Domain is different from copy so its ok
    	if(!AC3(copy)) {
    		return false;
    	}//else successful continue on copy
    	
        // YOUR CODE HERE
    	int  r = cell / 9;
    	int  c = cell % 9;
    	
    	//Check each values for cell's domain
    	ArrayList<Integer> tmp = new ArrayList<>(copy[cell]);
    	for(int val : tmp){
    		copy[cell].clear();
    		copy[cell].add(val);
    		
    		//Recursive 
    		if(backtrack(cell + 1, copy)){
    			vals[r][c] = val;
    			return true;
    		}
    	}
    	
        return false;

    }


    /*
     * This is the actual AC3 Algorithm. You may change this method header.
     */
    private final boolean AC3(ArrayList<Integer>[] Domains) {
    	//Initialize a new Queue and deep copy it
    	 Queue<Arc> q = new LinkedList<Arc>();
    	 for(Arc a : globalQueue) {
    		 q.offer(new Arc(a.Xi,a.Xj));
    	 }
    	 
    	     	 
		// YOUR CODE HERE
    	while(!q.isEmpty()) {
    		Arc arc = q.poll();
    		if (Revise(arc, Domains)) {
    			if(Domains[arc.Xi].size() == 0) {
    				return false;
    			}
    			//Reinserting the arcs
    			for(int Xk : neighbors[arc.Xi]){
    				if(Xk != arc.Xj) {
    				q.offer(new Arc(Xk, arc.Xi));
    				//System.out.println("add");
    				}
    			}
    		}
    	}
		return true;
    	
    }



    /*
     * This is the Revise() procedure. You may change this method header.
     */
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
    	 boolean revised = false;
         ArrayList<Integer> Di = Domains[t.Xi];
         ArrayList<Integer> Dj = Domains[t.Xj];
         
         for(int i = 0; i < Di.size(); i++) {
         	int x= Di.get(i);

         	boolean found = false;
         	//If found y that satisfy x != y
         	for(int  y: Dj){
         		if(y != x) {
         			found= true;
         			break;
         		}
         	}
         	//If not found, then remove i from Di's domain
         	if(!found) {
         		Di.remove(i);
         		revised = true;
         	}

         }

  		// YOUR CODE HERE
          return revised;
 	}


     /*
      * This is where you will write your custom solver.
      * You should not change this method header.
      * Use Forward checking and heuristic 
      */
     private final void customSolver(){
    	 
  	   //set 'success' to true if a successful board
  	   //is found and false otherwise.
  	   boolean success = true;
		   board.Clear();
		   recursions = 0;


	        System.out.println("Running custom algorithm");

	        //-- Your Code Here --
	        //Fill globalDomains
	        for(int i = 0; i < 81; i++) {
	        	int x = i / 9;
	        	int y = i % 9;

	        	/*filled in the global domain of i*/
	        	//already filled in;
	            if(vals[x][y] != 0) {
	            	globalDomains[i] = new ArrayList<Integer>();
	            	globalDomains[i].add(vals[x][y]);
	            }else {//filled in from 1 to 9;
		        	for(int k = 1; k < 10; k++) {
		        			if(globalDomains[i]==null) {
		        				globalDomains[i] = new ArrayList<Integer>();
		        			 }
		        			globalDomains[i].add(k);
		        	}
	            }
	        }

	        //Fill the neighbors
	        for(int r = 0; r < 9; r++) {
	            /* create input parametes for neighbors and globalQueue*/
	        	int[] row = new int[9];
	        	for(int c = 0; c < 9; c++) {
	        		row[c] = r * 9 + c;
	        	}
	        	allDiff(row);
	        }

	        for(int c = 0; c < 9; c++) {
	            /* create input parametes for neighbors and globalQueue*/
	        	int[] col = new int[9];
	        	for(int r = 0; r < 9; r++) {
	        		col[r] = r * 9 + c;
	        	}
	        	allDiff(col);
	        }

	        int upLeft = -21;
	        for(int c = 0; c < 3; c++) {
	        	upLeft += 18;//24

	        	for(int r = 0; r< 3; r++) {
	        		upLeft += 3; //0 3 6

	            	/*fill in subGroup*/
	            	int[] subGroup = new int[9];
	            	for(int i = 0; i < 9; i++) {//6
	                 // i 4
	            	  subGroup[i] = upLeft + i/3 * 9 + i % 3;//6 7 8 15 16
	            	}
	            	allDiff(subGroup);
	        	}
	        }

	        
	        //Here are the heuristics for the most remaining values
	        /*count the frequency
	        for(int[] i: vals) {
	        	for(int j: i) {
	        		if(j!=0) {
	        			frequency[j]++;
	        		}
	        	}
	        }*/
	      
	       success = forwardBacktrack(0, globalDomains);

	      
		   Finished(success);

  	}


  //Backtrack algorithm Using forward checking
  private final boolean forwardBacktrack(int cell, ArrayList<Integer>[] Domains) {
	  recursions++;
	  /*Here are the implementation of the Heuristic of most constraint variables, for some reasons it 
	   *takes much longer time than simply using forwrd checking, although less recursive steps, I guess maybe
	   *because of the forloop used in getNextCell and updateHeuristic.
	   */
	  //cell = getNextCell();
	 //updateHeuristic();
  	//if(cell == -1) return true;
	
	//Return true after all cells have gone through
  	if(cell > 80) return true;
  	//If already set
  	if(vals[cell / 9][cell % 9] != 0){
  		//heuristic[cell] = -1;
  		return forwardBacktrack(cell+1, Domains);
  	}

  	
	int  r = cell / 9;
  	int  c = cell % 9;
  	
  	//Backtrack to previous chosen variables by returning false
  	if(Domains[cell].isEmpty()) {
  		return false;
  	}

  	//Go through each value in the domain
  	ArrayList<Integer> tmp = new ArrayList<>(Domains[cell]);
  	for(int i=0; i<tmp.size(); i++) {
  		//Make a copy of Domains
  		 ArrayList<Integer>[] copy = new ArrayList[81];
  	   	 for(int m = 0; m < 81; m++) {
  	   		 copy[m] = new ArrayList<Integer>(Domains[m]);
  	   	 }
  	   	 
  	   	//Use forward checking to check
  		int val = tmp.get(i);
  		if(forwardChecking(cell,val,copy)) {
  			copy[cell].clear();
      		copy[cell].add(val);

      		//update domains of all unassigned neighbors
      		for(int nei : neighbors[cell]) {
          		if(copy[nei].contains(val)) {
          			copy[nei].remove(copy[nei].indexOf(val));
          		}
          	}
      		//Recursion
  			if(forwardBacktrack(cell+1,copy)) {
  				vals[r][c] = val;
      			return true;
      		}
  		}
  		else {
  			tmp.remove(i);
  			i--;
  		}

  	}
	   	return false;


  }
  /*The method that update the heuristics
   *However, this method is very inefficient that it go through every element in domain and neighbors of each
   *value in domain, but if it doesn't update every time, it will make lead to errors to infinite loop since
   *the ending case will never happen
   */
    private void updateHeuristic(ArrayList<Integer>[] Domains) {
        for(int i = 0;i<Domains.length;i++) {
        	int count = 0;
        	if(Domains[i].size()==1) {
        		heuristic[i] = -1;
        		continue;
        	}
        	for(int nei : neighbors[i]) {
        		if(Domains[nei].size()==1) count++;
        	}
        	heuristic[i] = count;
        }
    }
    
    //The method return the cell-number of the variable with the most constraints
    private int getNextCell() {
    	//heuristic[cell] = -1;
    	int max = heuristic[0];
    	int maxIndex = 0;
    	for(int i=0; i<81;i++) {
    		if(heuristic[i] > max) {
    			max = heuristic[i];
    			maxIndex = i;
    		}
    	}
    	return maxIndex;
    }
    
    //The method that return the least constraint value
    private int getNextVal(ArrayList<Integer> d) {
    	int max = -10000;
    	for(int i = 0;i<d.size();i++) {
    		if(frequency[d.get(i)] > max) max = frequency[d.get(i)];
    	}
    	return max;
    }
   
    //The forward checking algorithm that check whether setting a value make its neighbor's domain become empty
    private final boolean forwardChecking(int x, int value, ArrayList<Integer>[] Domains) {
    	
    	for(int nei : neighbors[x]) {
    		if(Domains[nei].contains(value) && Domains[nei].size()==1) {
    				return false;
    			}
    	}
    	
    	return true;
    }
    
   
    	
    


    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){

        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
	        for(int j=0; j<9; j++)
	        	System.out.print(vals[i][j]+" ");
	        System.out.println();
        }*/

        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }



    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
        	AC3Init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);

        long start=0, end=0;

        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                	start = System.currentTimeMillis();
                	AC3Init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                	start = System.currentTimeMillis();
                	customSolver();
                	end = System.currentTimeMillis();
                    break;
            }

            CheckSolution();

            if(!gui)
            	System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
            	vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
            	vals[1] = new int[] {3,0,0,0,1,0,0,5,0};
            	vals[2] = new int[] {0,0,6,0,0,0,1,0,0};
            	vals[3] = new int[] {7,0,0,0,9,0,0,0,0};
            	vals[4] = new int[] {0,4,0,6,0,3,0,0,0};
            	vals[5] = new int[] {0,0,3,0,0,2,0,0,0};
            	vals[6] = new int[] {5,0,0,0,8,0,7,0,0};
            	vals[7] = new int[] {0,0,7,0,0,0,0,0,5};
            	vals[8] = new int[] {0,0,0,0,0,0,0,9,8};
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){

    	if(success) {
            board.writeVals();
            //board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
            board.showMessage("Solved in " + myformat.format(recursions) + " recursive ops");

    	} else {
            //board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        	board.showMessage("No valid configuration found");
        }
         recursions = 0;

    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("Gui? y or n ");
        char g=scan.nextLine().charAt(0);

        if (g=='n')
            gui = false;
        else
            gui = true;

        if(gui) {
        	System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

	        char c = '*';

	        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
	        	c = scan.nextLine().charAt(0);
	            if(c=='e')
	                level = difficulty.valueOf("easy");
	            else if(c=='m')
	                level = difficulty.valueOf("medium");
	            else if(c=='h')
	                level = difficulty.valueOf("hard");
	            else if(c=='r')
	                level = difficulty.valueOf("random");
	            else{
	                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
	            }
	        }

	        SudokuPlayer app = new SudokuPlayer();
	        app.run();

        }
        else { //no gui

        	boolean again = true;

        	int numiters = 0;
        	long starttime, endtime, totaltime=0;

        	while(again) {

        		numiters++;
        		System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        		char c = '*';

		        while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
		        	c = scan.nextLine().charAt(0);
		            if(c=='e')
		                level = difficulty.valueOf("easy");
		            else if(c=='m')
		                level = difficulty.valueOf("medium");
		            else if(c=='h')
		                level = difficulty.valueOf("hard");
		            else if(c=='r')
		                level = difficulty.valueOf("random");
		            else{
		                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
		            }

		        }

	            System.out.println("Algorithm? AC3 (1) or Custom (2)");
	            if(scan.nextInt()==1)
	                alg = algorithm.valueOf("AC3");
	            else
	                alg = algorithm.valueOf("Custom");


		        SudokuPlayer app = new SudokuPlayer();

		        starttime = System.currentTimeMillis();

		        app.run();

		        endtime = System.currentTimeMillis();

		        totaltime += (endtime-starttime);


	        	System.out.println("quit(0), run again(1)");
	        	if (scan.nextInt()==1)
	        		again=true;
	        	else
	        		again=false;

	        	scan.nextLine();

        	}

        	System.out.println("average time over "+numiters+" iterations: "+(totaltime/numiters));
        }



        scan.close();
    }



    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");

    //For printing
	static int recursions;
}
