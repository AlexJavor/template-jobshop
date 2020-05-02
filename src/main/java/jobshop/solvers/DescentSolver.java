package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

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
            // Make the swap
            order.tasksByMachine[this.machine][this.t1] = task2;
            order.tasksByMachine[this.machine][this.t2] = task1;
        }
        
        public String toString() {
        	return "Swap: {M" + this.machine + " | t1 = " + this.t1 + " | t2 = " + this.t2 + "}";
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        throw new UnsupportedOperationException();
    }

    /** Returns a list of all blocks of the critical path. */
    public List<Block> blocksOfCriticalPath(ResourceOrder order) {
    	List<Block> criticalBlockList = new ArrayList<>();
    	List<Integer> checkedMachines = new ArrayList<>();
    	
    	// Obtain the critical task list from the resource order instance
        Schedule criticalSchedule = order.toSchedule();
        List<Task> criticalTaskList = criticalSchedule.criticalPath();
        
        Block currentBlock;
        int currentMachine, m;
        int firstTask = 0, lastTask = 0;
        Task currentTask;
        
        System.out.print("Number of Jobs     : " + order.instance.numJobs + "\n");
        System.out.print("Number of Tasks    : " + order.instance.numTasks + "\n");
        System.out.print("Number of Machines : " + order.instance.numMachines + "\n");
        System.out.print("Critical path      : " + criticalTaskList + "\n");
        
        // Initialize the block list
        for(int i = 0; i < order.instance.numMachines; i++) {
        	currentBlock = new Block(i, -1, -1);
        	criticalBlockList.add(i, currentBlock);
        }
        
        for(int i = 0; i < criticalTaskList.size(); i++) {
        	currentTask = criticalTaskList.get(i);
        	currentMachine = order.instance.machine(currentTask.job, currentTask.task);
        	
        	// When we find a machine we have not explored, we start searching for all its appearances in the critical path 
        	// and we safe the first and last occurrence of the machine.  
        	if(!checkedMachines.contains(currentMachine)) {
        		firstTask = 0;
                lastTask = 0;
        		for(int index = i; index < criticalTaskList.size(); index++) {
	        		m = order.instance.machine(criticalTaskList.get(index).job, criticalTaskList.get(index).task);
	        		// If we find a task running in the same machine and it is not the first task, add 1 to the last task
	        		if(currentMachine == m && index > i){
	    				lastTask++;
	        		}
        		}
            	// Add the machine to the checked machines list
            	checkedMachines.add(currentMachine);
            	// Create and add the new block to the list
            	currentBlock = new Block(currentMachine, firstTask, lastTask);
            	criticalBlockList.set(currentMachine, currentBlock);
        	}
        }
        return criticalBlockList;
    }
    

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    public List<Swap> neighbors(Block block) {
    	List<Swap> swapList = new ArrayList<>();
    	Swap currentSwap;
    	
    	int machine   = block.machine;
    	int firstTask = block.firstTask;
    	int lastTask  = block.lastTask;
    	
    	// Case when there is just one element in the block
    	if(firstTask == lastTask) {
    		swapList = null;
    	}
    	
     	for(int i = firstTask; i < lastTask; i++) {
     		if(i == firstTask + 1) {
     			currentSwap = new Swap(machine, firstTask, i);
     			swapList.add(currentSwap);
     		}
     		if (i == lastTask - 1) {
     			currentSwap = new Swap(machine, i, lastTask);
     			swapList.add(currentSwap);
     		}
    	}
        return swapList;
    }

}
