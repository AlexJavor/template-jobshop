package jobshop.solvers;

import java.util.ArrayList;

import jobshop.*;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.GreedySolver.PriorityESTRule;

public class GreedySolver implements Solver {
	
	/*********************************************************************************************/
	/******************* Priority Rules and EST Priority Rules enumerations **********************/
	/*********************************************************************************************/
	public enum PriorityRule{
		SPT {
			@Override
			public Task getTaskByRule(ArrayList<Task> achievableTasks, Instance instance) {
				// Return the task with the MINIMAL duration
				Task TaskSPT = null;
				int minDuration = Integer.MAX_VALUE;
				
				for(int i = 0; i < achievableTasks.size(); i++) {
					Task current = achievableTasks.get(i);
					int currentDuration = instance.duration(current.job, current.task);
					
					if(currentDuration < minDuration) {
						minDuration = currentDuration;
						TaskSPT = current;
					}
				}
				return TaskSPT;
			}
		},
		LRPT {
			@Override
			public Task getTaskByRule(ArrayList<Task> achievableTasks, Instance instance) {	
				Task TaskLRPT = null;
				Task current;
				int maxDuration = -1;
				
				for(int i = 0; i < achievableTasks.size(); i++) {
					current = achievableTasks.get(i);
					int remainingTime = 0;
					for(int j = current.task; j < instance.numTasks; j++) {
						remainingTime += instance.duration(current.job, j);
					}
					if(remainingTime > maxDuration) {
						maxDuration = remainingTime;
						TaskLRPT = current;
					}
				}
				return TaskLRPT;
			}
		};
		/**
		 * Returns the Task to work with depending on the rule chosen
		 * @param achievableTasks, instance
		 * @return currentTask
		 */
		public abstract Task getTaskByRule(ArrayList<Task> achievableTasks, Instance instance);
	}
	
	public enum PriorityESTRule{
		EST_SPT {
			@Override
			public Task getTaskByESTRule(ArrayList<Task> achievableTasks, Instance instance, int[] nextStartDateJobs, int[] nextStartDateMachines) {

				ArrayList<Task> priorityTasks = this.getESTPriorityTasks(achievableTasks, instance, nextStartDateJobs, nextStartDateMachines);
				// STP function: Return the task with the MINIMAL duration
				PriorityRule SPT = PriorityRule.SPT;
				Task TaskSPT = SPT.getTaskByRule(priorityTasks, instance);
			
				return TaskSPT;
			}
		},
		EST_LRPT {
			@Override
			public Task getTaskByESTRule(ArrayList<Task> achievableTasks, Instance instance, int[] nextStartDateJobs, int[] nextStartDateMachines) {	
				ArrayList<Task> priorityTasks = this.getESTPriorityTasks(achievableTasks, instance, nextStartDateJobs, nextStartDateMachines);
				// STP function: Return the task with the MINIMAL duration
				PriorityRule LRPT = PriorityRule.LRPT;
				Task TaskLRPT = LRPT.getTaskByRule(priorityTasks, instance);
			
				return TaskLRPT;
			}
		};
		/**
		 * Returns the Task to work with depending on the rule chosen
		 * @param achievableTasks, instance
		 * @return currentTask
		 */
		public abstract Task getTaskByESTRule(ArrayList<Task> achievableTasks, Instance instance, int[] nextStartDateJobs, int[] nextStartDateMachines);
		/**
		 * Returns the array of tasks with the shortest start date
		 * @param achievableTasks, instance
		 * @return priorityTask
		 */
		protected ArrayList<Task> getESTPriorityTasks(ArrayList<Task> achievableTasks, Instance instance, int[] nextStartDateJobs, int[] nextStartDateMachines){
			// Search for the date or dates which start sooner
			ArrayList<Task> priorityTasks = new ArrayList<>();
			int minStartDate = Integer.MAX_VALUE;

			
			for(int i = 0; i < achievableTasks.size(); i++) {
				Task currentTask = achievableTasks.get(i);
				int currentMachine = instance.machine(currentTask.job, currentTask.task);
				int currentStartDate = Integer.max(nextStartDateJobs[currentTask.job], nextStartDateMachines[currentMachine]);
						
				if(currentStartDate < minStartDate) {
					minStartDate = currentStartDate;
					// When we find a smaller start date we "restart" the array 
					priorityTasks.clear();
					priorityTasks.add(currentTask);
				} else if (currentStartDate == minStartDate) {
					priorityTasks.add(currentTask);
				}
			}
			return priorityTasks;
		}
	}
	
	/*********************************************************************************************/
	/********************** Greedy Solver: Constructors + Solve function *************************/
	/*********************************************************************************************/
	
	public PriorityRule priorityRule;
	public PriorityESTRule priorityESTRule;
	
	// 2 constructors: the default and one with the EST restriction
	public GreedySolver(PriorityRule rule) {
		this.priorityRule = rule;
		this.priorityESTRule = null;
	}
	
	public GreedySolver(PriorityESTRule ruleEST) {
		this.priorityESTRule = ruleEST;
		this.priorityRule = null;
	}
	
	
	
	@Override
    public Result solve(Instance instance, long deadline) {
		
		int currentMachine, currentDuration;
		// We declare 2 arrays containing the updated moment the next task will start in a job and a machine respectively
		int[] nextStartDateJobs = new int[instance.numJobs];
		int[] nextStartDateMachines = new int[instance.numMachines];
		
		// We create a new ResourceOrder for putting all tasks in the schedule
		ResourceOrder solution = new ResourceOrder(instance);
		// Array list with all the achievable current tasks
        ArrayList<Task> achievableTasks = new ArrayList<>();
        
        // Initialize the array list with all the first task achievable
		for(int i = 0 ; i < instance.numJobs ; i++) {
			Task currentTask = new Task(i, 0);
			achievableTasks.add(currentTask);
        }
		
        while(!achievableTasks.isEmpty()) {
            // We take the task we should do now in function of the priority rule used
        	Task currentTask = null;
        	if(priorityESTRule == null) {
        		currentTask = priorityRule.getTaskByRule(achievableTasks, instance);
        	} else if(priorityRule == null) {
        		currentTask = priorityESTRule.getTaskByESTRule(achievableTasks, instance, nextStartDateJobs, nextStartDateMachines);
        	} else {
        		System.out.printf("Error priorityRule and priorityRuleEST are null. You must give a value to one of them.");
        	}
        	
        	// Updating starting dates
			currentMachine = instance.machine(currentTask.job, currentTask.task);
			currentDuration = instance.duration(currentTask.job, currentTask.task);
			nextStartDateJobs[currentTask.job] += currentDuration;
			nextStartDateMachines[currentMachine] += currentDuration;
            
            // We remove the current task from the achievable tasks list
            achievableTasks.remove(currentTask);
            
            // If it's not the last task of the job, we update the array list with the new task
            if (currentTask.task < (instance.numTasks - 1)) {
            	achievableTasks.add(new Task(currentTask.job, currentTask.task + 1));
            }
            
            // We add the current task to the solution
            int nextFreeSlot = solution.nextFreeSlot[currentMachine]++;
            solution.tasksByMachine[currentMachine][nextFreeSlot] = currentTask;
        }

        return new Result(instance, solution.toSchedule(), Result.ExitCause.Blocked);
    }
}