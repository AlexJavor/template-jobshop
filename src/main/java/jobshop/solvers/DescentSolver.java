package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Result.ExitCause;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.GreedySolver.PriorityESTRule;
import jobshop.solvers.GreedySolver.PriorityRule;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {
	
	private PriorityRule priorityRule;
	private PriorityESTRule priorityESTRule;
	
	// 2 constructors: the default and one with the EST restriction
	public DescentSolver(PriorityRule rule) {
		super();
		this.priorityRule = rule;
		this.priorityESTRule = null;
	}
	
	public DescentSolver(PriorityESTRule ruleEST) {
		super();
		this.priorityESTRule = ruleEST;
		this.priorityRule = null;
	}

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
	public static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        
        public String toString() {
        	return "Block: {M" + this.machine + " | firstTask = " + this.firstTask + " | lastTask = " + this.lastTask + "}";
        }

    }
    
    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            // Retrieve the tasks to be swap
        	Task task1 = order.tasksByMachine[this.machine][this.t1];
            Task task2 = order.tasksByMachine[this.machine][this.t2];
            // Make the swap (default in/out)
            order.tasksByMachine[this.machine][this.t1] = task2;
            order.tasksByMachine[this.machine][this.t2] = task1;
        }
        
        public String toString() {
        	return "Swap: {M" + this.machine + " | t1 = " + this.t1 + " | t2 = " + this.t2 + "}";
        }
    }
    // ************************************************************************************************************* //
    // *************************************** DescentSolver: solve Method ***************************************** //
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
    	
        // Start: Sinit <- GreedySolver(instance)
    	Result resultLRPT = greedy.solve(instance, deadline);
    	Schedule initialSolution = resultLRPT.schedule;
    	
    	// Record the best solution
    	Schedule bestSolution = initialSolution;
    	ResourceOrder bestResourceOrder = new ResourceOrder(bestSolution);
    	
    	// Repeat: Explore the concurrent neighbors
    	Boolean optimizable = true;
    	Schedule currentSolution;
    	ResourceOrder currentResourceOrder;
    	List<Block> criticalBlockList;
    	
    	while(optimizable && deadline > System.currentTimeMillis()) {
    		// We first take the critical path from the bestSolution
        	bestResourceOrder = new ResourceOrder(bestSolution);
        	criticalBlockList = this.blocksOfCriticalPath(bestResourceOrder);
        	// By default we suppose there will be no optimization possible. If there is, this value will be later changed
        	optimizable = false;
        	// We search for the best solution by checking all neighbors
        	for(Block b : criticalBlockList) {
        		for(Swap s : this.neighbors(b)) {
                	// We copy to a variable the bestResourceOrder in order to modified freely while searching for the best solution
                	currentResourceOrder = bestResourceOrder.copy();
        			// We apply the swap on the current Resource Order and we schedule it
        			s.applyOn(currentResourceOrder);
        			currentSolution = currentResourceOrder.toSchedule();
        			// If the currentSolution duration is smaller than the bestSolution one, save the currentSolution
        			if(currentSolution != null) {
	        			if(currentSolution.makespan() < bestSolution.makespan()) {
	        				bestSolution = currentSolution;
	        				// While we find better solutions keep running the solve method
	        				optimizable = true;
	        			}
        			}
        		}
        	}
    	}
    	// We find the exit cause in order to create the result we will return
    	ExitCause exitCause = null;
    	if(deadline <= System.currentTimeMillis()) {
    		exitCause = ExitCause.Timeout;
    	} else {
    		exitCause = ExitCause.ProvedOptimal;
    	}
    	
    	return new Result(instance, bestSolution, exitCause);
    }
    
    // ************************************************************************************************************* //
    
    
    // ************************************************************************************************************* //
    // ***************************** blocksOfCriticalPath and neighbors Methods ************************************ //
    // ************************************************************************************************************* //
    /** Returns a list of all blocks of the critical path. */
    public List<Block> blocksOfCriticalPath(ResourceOrder order) {
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
     public List<Swap> neighbors(Block block) {
    	List<Swap> swapList = new ArrayList<>();
    	Swap swap1, swap2;
    	
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
