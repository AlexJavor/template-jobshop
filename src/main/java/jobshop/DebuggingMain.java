package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.DescentSolver.Block;
import jobshop.solvers.DescentSolver.Swap;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.GreedySolver.PriorityESTRule;
import jobshop.solvers.GreedySolver.PriorityRule;
import jobshop.solvers.TabooSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();
            // TODO: make it print something meaningful
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            
            ResourceOrder ro = new ResourceOrder(instance);
            ro.tasksByMachine[0][0] = new Task(0,0);
            ro.tasksByMachine[0][1] = new Task(1,1);
            ro.tasksByMachine[1][0] = new Task(1,0);
            ro.tasksByMachine[1][1] = new Task(0,1);
            ro.tasksByMachine[2][0] = new Task(0,2);
            ro.tasksByMachine[2][1] = new Task(1,2);
            
            System.out.println("RESOURCE ORDER ENCODING:\n" + ro + "\n");
            
            System.out.println("---------------------------------------------\n");
            
            // load the aaa2 instance
            Instance instance2 = Instance.fromFile(Paths.get("instances/aaa2"));
            
            ResourceOrder ro2 = new ResourceOrder(instance2);
            ro2.tasksByMachine[0][0] = new Task(2,0); //O7: Job 2, Task 0 (Machine 0)
            ro2.tasksByMachine[0][1] = new Task(1,1); //O5: Job 1, Task 1 (Machine 0)
            ro2.tasksByMachine[0][2] = new Task(0,1); //O2: Job 0, Task 1 (Machine 0)
            ro2.tasksByMachine[1][0] = new Task(1,0); //O4: Job 1, Task 0 (Machine 1)
            ro2.tasksByMachine[1][1] = new Task(2,1); //O8: Job 2, Task 1 (Machine 1)
            ro2.tasksByMachine[1][2] = new Task(0,2); //O3: Job 0, Task 2 (Machine 1)
            ro2.tasksByMachine[2][0] = new Task(2,2); //O9: Job 2, Task 2 (Machine 2)
            ro2.tasksByMachine[2][1] = new Task(0,0); //O1: Job 0, Task 0 (Machine 2)
            ro2.tasksByMachine[2][2] = new Task(1,2); //O6: Job 1, Task 2 (Machine 2)
            
            System.out.println("RESOURCE ORDER ENCODING:\n" + ro2 + "\n");
            
            System.out.println("---------------------------------------------\n");
            
            System.out.println("Default Solver\n");
            sched = ro.toSchedule();
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            
            /*JobNumbers jo = JobNumbers.fromSchedule(sched);
            System.out.println("JOB NUMBER ENCODING (FROM_SCHEDULE): " + jo + "\n");*/
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Greedy Solver: STP");
            PriorityRule SPT = PriorityRule.SPT;
            Solver solverSPT = new GreedySolver(SPT);
            Result resultSPT = solverSPT.solve(instance, System.currentTimeMillis() + 10);
            sched = resultSPT.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Greedy Solver: LRPT\n");
            PriorityRule LRPT = PriorityRule.LRPT;
            Solver solverLRPT = new GreedySolver(LRPT);
            Result resultLRPT = solverLRPT.solve(instance, System.currentTimeMillis() + 10);
            sched = resultLRPT.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Greedy Solver: EST_SPT\n");
            PriorityESTRule EST_SPT = PriorityESTRule.EST_SPT;
            Solver solverEST_SPT = new GreedySolver(EST_SPT);
            Result resultEST_SPT = solverEST_SPT.solve(instance, System.currentTimeMillis() + 10);
            sched = resultEST_SPT.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Greedy Solver: EST_LRPT\n");
            PriorityESTRule EST_LRPT = PriorityESTRule.EST_LRPT;
            Solver solverEST_LRPT = new GreedySolver(EST_LRPT);
            Result resultEST_LRPT = solverEST_LRPT.solve(instance, System.currentTimeMillis() + 10);
            sched = resultEST_LRPT.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Descent Solver: [Executed with EST_LRPT]\n");
            
            DescentSolver solverDescent = new DescentSolver(EST_LRPT);
            
            
            System.out.print("****** TEST: blocksOfCriticalPath() ******\n");
            System.out.print("Number of Jobs     : " + instance2.numJobs + "\n");
            System.out.print("Number of Tasks    : " + instance2.numTasks + "\n");
            System.out.print("Number of Machines : " + instance2.numMachines + "\n\n");
            
            
            
            List<Block> criticalBlockList = solverDescent.blocksOfCriticalPath(ro2);
            for(Block b : criticalBlockList) {
            	System.out.println(b);
            	for(Swap s : solverDescent.neighbors(b)) {
            		System.out.println(s);
            	}
            }
            System.out.print("******************************************\n");
            
            
            Result resultDescent = solverDescent.solve(instance2, System.currentTimeMillis() + 10);
            sched = resultDescent.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            System.out.println("Taboo Solver: [Executed with EST_LRPT]\n");
            
            TabooSolver solverTaboo = new TabooSolver(EST_LRPT, 50, 1000);            
            Result resultTaboo = solverTaboo.solve(instance2, System.currentTimeMillis() + 10);
            sched = resultTaboo.schedule;
            /*
            List<Block>criticalBlockList2 = solverTaboo.blocksOfCriticalPath(ro2);
            for(Block b : criticalBlockList2) {
            	System.out.println(b);
            	for(Swap s : solverTaboo.neighbors(b)) {
            		System.out.println(s);
            	}
            }
            */
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
