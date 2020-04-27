package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/** Représentation par numéro de job. */
public class JobNumbers extends Encoding {

    /** A numJobs * numTasks array containing the representation by job numbers. */
    public final int[] jobs;

    /** In case the encoding is only partially filled, indicates the index of first
     * element of `jobs` that has not been set yet. */
    public int nextToSet = 0;

    public JobNumbers(Instance instance) {
        super(instance);

        jobs = new int[instance.numJobs * instance.numMachines];
        Arrays.fill(jobs, -1);
    }
    
    public JobNumbers(Schedule schedule) {
        super(schedule.pb);

        this.jobs = new int[instance.numJobs * instance.numTasks];

        // for each job indicates which is the next task to be scheduled
        int[] nextOnJob = new int[instance.numJobs];

        while(Arrays.stream(nextOnJob).anyMatch(t -> t < instance.numTasks)) {
            Task next = IntStream
                    // for all jobs numbers
                    .range(0, instance.numJobs)
                    // build the next task for this job
                    .mapToObj(j -> new Task(j, nextOnJob[j]))
                    // only keep valid tasks (some jobs have no task left to be executed)
                    .filter(t -> t.task < instance.numTasks)
                    // select the task with the earliest execution time
                    .min(Comparator.comparing(t -> schedule.startTime(t.job, t.task)))
                    .get();

            this.jobs[nextToSet++] = next.job;
            nextOnJob[next.job] += 1;
        }
    }

    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        // compute the earliest start time for every task of every job
        for(int job : jobs) {
            int task = nextTask[job];
            int machine = instance.machine(job, task);
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
            nextTask[job] = task + 1;
        }

        return new Schedule(instance, startTimes);
    }
    
    public static JobNumbers fromSchedule(Schedule sched) {
		JobNumbers jo = new JobNumbers(sched.pb);
		
		int current_time = 0;
		Task current_task = new Task(-1,-1);
		Task [] done_tasks = new Task[sched.pb.numJobs*sched.pb.numTasks];
		Arrays.fill(done_tasks, current_task);
		
		int min;
		
		for (int i = 0; i < sched.pb.numJobs*sched.pb.numTasks; i++) {
			// Il faut faire le code ci-dessous autant de fois que l'on a de taches
			// On trouve le minimum parmis les restants
			min = Integer.MAX_VALUE;
			for (int job = 0; job < sched.pb.numJobs; job++) {
				for (int task = 0; task < sched.pb.numTasks; task++) {
					int task_start_time = sched.startTime(job, task);
					Task this_task = new Task(job, task);
					if (task_start_time < min && task_start_time >= current_time && !(Arrays.asList(done_tasks).contains(this_task))) {
						min = task_start_time;
						current_task = this_task;
					}
				}
			}
			// Une fois on a trouvé la suivante tache a realiser on introduit le numero du job dans jobs
			jo.jobs[jo.nextToSet++] = current_task.job;
			done_tasks[i] = current_task;
		}
    	
    	return jo;
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOfRange(jobs,0, nextToSet));
    }
}
