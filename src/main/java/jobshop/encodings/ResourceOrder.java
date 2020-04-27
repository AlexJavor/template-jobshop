package jobshop.encodings;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;


public class ResourceOrder extends Encoding {
	
	public final Task[][] tasksByMachine;
	
	public final int[] nextFreeSlot;
	
	public ResourceOrder(Instance instance) {
		super(instance);
		
		this.tasksByMachine = new Task[instance.numMachines][instance.numJobs];
		for (int i = 0; i < instance.numMachines; i++) {
			for (int j = 0; j < instance.numJobs; j++) {
				this.tasksByMachine[i][j] = new Task(-1,-1);
			}
		}
		
		// no task scheduled on any machine (0 is the default value)
        nextFreeSlot = new int[instance.numMachines];
	}
	
	public ResourceOrder(Schedule schedule) {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.tasksByMachine = new Task[pb.numMachines][];
        this.nextFreeSlot = new int[instance.numMachines];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for thi machine, find all tasks that are executed on it and sort them by their start time
            tasksByMachine[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
	 }
	
	
	@Override
    public Schedule toSchedule() {
        // indicate for each task that have been scheduled, its start time
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

        // for each job, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByJob = new int[instance.numJobs];

        // for each machine, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByMachine = new int[instance.numMachines];

        // for each machine, earliest time at which the machine can be used
        int[] releaseTimeOfMachine = new int[instance.numMachines];


        // loop while there remains a job that has unscheduled tasks
        while(IntStream.range(0, instance.numJobs).anyMatch(m -> nextToScheduleByJob[m] < instance.numTasks)) {

            // selects a task that has noun scheduled predecessor on its job and machine :
            //  - it is the next to be schedule on a machine
            //  - it is the next to be scheduled on its job
            // if there is no such task, we have cyclic dependency and the solution is invalid
            Optional<Task> schedulable =
                    IntStream.range(0, instance.numMachines) // all machines ...
                    .filter(m -> nextToScheduleByMachine[m] < instance.numJobs) // ... with unscheduled jobs
                    .mapToObj(m -> this.tasksByMachine[m][nextToScheduleByMachine[m]]) // tasks that are next to schedule on a machine ...
                    .filter(task -> task.task == nextToScheduleByJob[task.job])  // ... and on their job
                    .findFirst(); // select the first one if any

            if(schedulable.isPresent()) {
                // we found a schedulable task, lets call it t
                Task t = schedulable.get();
                int machine = instance.machine(t.job, t.task);

                // compute the earliest start time (est) of the task
                int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
                est = Math.max(est, releaseTimeOfMachine[instance.machine(t.job, t.task)]);
                startTimes[t.job][t.task] = est;

                // mark the task as scheduled
                nextToScheduleByJob[t.job]++;
                nextToScheduleByMachine[machine]++;
                // increase the release time of the machine
                releaseTimeOfMachine[machine] = est + instance.duration(t.job, t.task);
            } else {
                // no tasks are schedulable, there is no solution for this resource ordering
                return null;
            }
        }
        // we exited the loop : all tasks have been scheduled successfully
        return new Schedule(instance, startTimes);
    }
	
	@Override
    public String toString() {
		String res = "";
		for (int i = 0; i < this.tasksByMachine.length; i++) {
			res += "Machine number : " + Integer.toString(i+1) + "\n";
			for (int j = 0; j < this.tasksByMachine[i].length; j++) {
				res += "\tUse number " + Integer.toString(j+1) + " : " + this.tasksByMachine[i][j].add_one() + "\n";
			}
		}
        return res;
    }
	
}
