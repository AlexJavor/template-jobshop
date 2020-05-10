package jobshop.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.Result.ExitCause;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.GreedySolver.PriorityESTRule;
import jobshop.solvers.GreedySolver.PriorityRule;
import jobshop.solvers.DescentSolver.*;


public class TabooSolver implements Solver {
	
	private PriorityRule priorityRule;
	private PriorityESTRule priorityESTRule;
	private int dureeTaboo;
	private int maxIter;
	
	// 2 constructors: the default and one with the EST restriction
	public TabooSolver(PriorityRule rule, int dureeTaboo, int maxIter) {
		super();
		this.priorityRule = rule;
		this.priorityESTRule = null;
		this.dureeTaboo = dureeTaboo;
		this.maxIter = maxIter;
	}
	
	public TabooSolver(PriorityESTRule ruleEST, int dureeTaboo, int maxIter) {
		super();
		this.priorityESTRule = ruleEST;
		this.priorityRule = null;
		this.dureeTaboo = dureeTaboo;
		this.maxIter = maxIter;
	}
	
	
	// ************************************************************************************************************* //
	// *************************************** TabooSolver: solve Method ******************************************* //
	// ************************************************************************************************************* //
	
    @Override
    public Result solve(Instance instance, long deadline) {
    	// Choosing rule (SPT / LRPT / EST_SPT / EST_LRPT)
    	GreedySolver greedy = null;
    	if(priorityESTRule == null) {
    		PriorityRule currentRule = this.priorityRule;
    		greedy = new GreedySolver(currentRule);
    	} else if(priorityRule == null) {
    		PriorityESTRule currentESTRule = this.priorityESTRule;
    		greedy = new GreedySolver(currentESTRule);
    	} else {
    		System.out.printf("Error priorityRule and priorityRuleEST are null. You must give a value to one of them.");
    	}
    	
        // Generating a viable solution
    	Result result = greedy.solve(instance, deadline);
    	Schedule initialSolution = result.schedule;
    	ResourceOrder initialResourceOrder = new ResourceOrder(initialSolution);
        
    	// Declaring all solution types
    	ResourceOrder bestRO         = initialResourceOrder; // s*
    	ResourceOrder currentRO      = bestRO.copy();		 // s
    	ResourceOrder bestNeighborRO = bestRO.copy();		 // s' 
    	ResourceOrder neighborRO;							 // s''
    	
		// Defining the sTaboo variables
    	int totalTasks = instance.numJobs * instance.numTasks;
		int[][] sTaboo = new int[totalTasks][totalTasks];
		// Initializing sTaboo with all 0
		for (int[] row : sTaboo) {
			Arrays.fill(row, 0);
		}
		
    	// Declaring other variables
		List<Block> criticalBlockList;
    	int TASK_PER_JOB, j1, i1, j2, i2, taskID1, taskID2, forbiddenTaskID1, forbiddenTaskID2;
    	int bestMakespan = initialSolution.makespan();
    	int bestNeighborMakespan, neighborMakespan;
    	boolean updated;
    	
    	// Iteration Counter
    	int k = 0;

        while (deadline > System.currentTimeMillis() && k <= this.maxIter) {
        	// ***************** 1. k <- k + 1 ******************************************************** //
            k++;
            
            // ***************** 2. Choose the best neighbor s' that is not in sTaboo ***************** //
            bestNeighborMakespan = Integer.MAX_VALUE;
            forbiddenTaskID1 = -1; 
            forbiddenTaskID2 = -1;
            updated = false;
    		
            // We first take the critical path from the currentRO (s)
    		currentRO = bestNeighborRO.copy(); // (s <- s')
        	criticalBlockList = this.blocksOfCriticalPath(currentRO);
        	
            for(Block b : criticalBlockList) {
                for(Swap s : neighbors(b)) {
                	// Extract the current index values for sTaboo
        			TASK_PER_JOB = currentRO.instance.numTasks;
        			j1 = currentRO.tasksByMachine[s.machine][s.t1].job;
         			j2 = currentRO.tasksByMachine[s.machine][s.t2].job;
        			i1 = currentRO.tasksByMachine[s.machine][s.t1].task;
        			i2 = currentRO.tasksByMachine[s.machine][s.t2].task;
        			taskID1 = j1 * TASK_PER_JOB + i1;
        			taskID2 = j2 * TASK_PER_JOB + i2;
        			
                    // Check if it is a forbidden swap
                    if(sTaboo[taskID1][taskID2] < k) {
                    	updated= true;
                        neighborRO = currentRO.copy();
                        // We apply the swap on the current Resource Order and we schedule it to find its makespan
                        s.applyOn(neighborRO);
                        neighborMakespan = neighborRO.toSchedule().makespan();                        
                        
                        if(neighborMakespan < bestNeighborMakespan) {
                        	// We forbid the opposite permutation of the given tasks (in index taskID1 and taskID2)
                        	forbiddenTaskID1 = taskID1;
                        	forbiddenTaskID2 = taskID2;
                            // We have checked all neighbors and we have chosen the best one: bestNeighborRO
                        	bestNeighborMakespan = neighborMakespan;
                            bestNeighborRO = neighborRO.copy();
                        }
                    }
                }
            }
            // ************************ 3. Add bestNeighborSolution (s') to sTaboo *************************** //
            // If it is not updated it means all solutions are forbidden
            if(updated) {
            	sTaboo[forbiddenTaskID2][forbiddenTaskID1] = this.dureeTaboo + k;
            	// ******************** 4. If s' is better than s* then s* <- s' ************************** //
                if(bestNeighborMakespan < bestMakespan) {
                	bestMakespan = bestNeighborMakespan;
                	bestRO = bestNeighborRO.copy();
                }
            } 
        }
    	// We find the exit cause in order to create the result we will return
    	ExitCause exitCause = null;
    	if(deadline <= System.currentTimeMillis()) {
    		exitCause = ExitCause.Timeout;
    	} else if(k >= this.maxIter) {
    		exitCause = ExitCause.Blocked;
    	} else {
    		exitCause = ExitCause.ProvedOptimal;
    	}
    	return new Result(instance, bestRO.toSchedule(), exitCause);
    }
    // ************************************************************************************************************* //
    
    
    // ************************************************************************************************************* //
    // ********************************** Copied functions from DescentSolver ************************************** //
    // ************************************************************************************************************* //
    
    /** Returns a list of all blocks of the critical path. */
    private List<Block> blocksOfCriticalPath(ResourceOrder order) {
     	List<Block> criticalBlockList = new ArrayList<>();
     	Block currentBlock;
     	// Obtain the critical task list from the resource order instance
         Schedule criticalSchedule = order.toSchedule();
         List<Task> criticalTaskList = criticalSchedule.criticalPath();
         
         int totalNumMachines = criticalSchedule.pb.numMachines;
         int totalNumJobs     = criticalSchedule.pb.numJobs;
         
         Task currentTaskRO;
         int currentTaskIndexRO, currentCriticalTaskIndex, firstTask, lastTask;
         
         // We check for all machines
         for(int currentMachine = 0; currentMachine < totalNumMachines; currentMachine++) {
         	currentTaskIndexRO = 0;
             while(currentTaskIndexRO < (totalNumJobs-1)){
                 currentTaskRO = order.tasksByMachine[currentMachine][currentTaskIndexRO];
                 if (criticalTaskList.contains(currentTaskRO)) {
                     currentCriticalTaskIndex = criticalTaskList.indexOf(currentTaskRO);
                     //If the next task in the critical path is running in the same machine try find the last task index
                     if(currentMachine == criticalSchedule.pb.machine(criticalTaskList.get(currentCriticalTaskIndex+1))) {
                         firstTask = currentTaskIndexRO;
                         while(currentCriticalTaskIndex < (criticalTaskList.size()-1) && currentMachine == criticalSchedule.pb.machine(criticalTaskList.get(currentCriticalTaskIndex+1))){
                         	// We advance in the list
                             currentCriticalTaskIndex++;
                             // We have to also advance in the resource order
                             currentTaskIndexRO++;
                         }
                         // Create and add the new block to the list
                         lastTask = currentTaskIndexRO;
                 	    currentBlock = new Block(currentMachine, firstTask, lastTask);
                         criticalBlockList.add(currentBlock);
                     }
                 }
                 // We move on to the next task in the resource order
                 currentTaskIndexRO++;
             }
         }
         return criticalBlockList;
     }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    private List<Swap> neighbors(Block block) {
    	List<Swap> swapList = new ArrayList<>();
    	Swap swap1;
    	Swap swap2;
    	
    	int machine   = block.machine;
    	int firstTask = block.firstTask;
    	int lastTask  = block.lastTask;
    	
    	// One single swap if there are just 2 elements in the block, two swaps if there are more than 2.
 		if(firstTask + 1 == lastTask) {
 			swap1 = new Swap(machine, firstTask, lastTask);
 			swapList.add(swap1);
 		} else {
 			swap1 = new Swap(machine, firstTask, firstTask+1);
 			swap2 = new Swap(machine, lastTask-1 , lastTask);
 			swapList.add(swap1);
 			swapList.add(swap2);
 		}
        return swapList;
    }
		
    // ************************************************************************************************************* //
}
