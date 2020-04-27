package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.GreedySolver.PriorityESTRule;
import jobshop.solvers.GreedySolver.PriorityRule;

import java.io.IOException;
import java.nio.file.Paths;

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
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("\nJOB NUMBER ENCODING: " + enc + "\n");

            Schedule sched = enc.toSchedule();
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid() + "\n");
            System.out.println("MAKESPAN: " + sched.makespan() + "\n");
            
            System.out.println("---------------------------------------------\n");
        
            ResourceOrder ro = new ResourceOrder(instance);
            ro.tasksByMachine[0][0] = new Task(0,0);
            ro.tasksByMachine[0][1] = new Task(1,1);
            ro.tasksByMachine[1][0] = new Task(1,0);
            ro.tasksByMachine[1][1] = new Task(0,1);
            ro.tasksByMachine[2][0] = new Task(0,2);
            ro.tasksByMachine[2][1] = new Task(1,2);
            
            System.out.println("RESOURCE ORDER ENCODING:\n" + ro + "\n");
            
            System.out.println("Default Solver\n");
            sched = ro.toSchedule();
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
            System.out.println("---------------------------------------------\n");
            
            JobNumbers jo = JobNumbers.fromSchedule(sched);
            System.out.println("JOB NUMBER ENCODING (FROM_SCHEDULE): " + jo + "\n");
            
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
            System.out.println("Greedy Solver: EST_SPT\n");
            PriorityESTRule EST_LRPT = PriorityESTRule.EST_LRPT;
            Solver solverEST_LRPT = new GreedySolver(EST_SPT);
            Result resultEST_LRPT = solverEST_LRPT.solve(instance, System.currentTimeMillis() + 10);
            sched = resultEST_LRPT.schedule;
            
            System.out.println("SCHEDULE:\n" + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
