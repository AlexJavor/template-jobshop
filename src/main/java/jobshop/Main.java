package jobshop;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import jobshop.solvers.*;
import jobshop.solvers.GreedySolver.PriorityESTRule;
import jobshop.solvers.GreedySolver.PriorityRule;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {
	// ******************************************** Main - Arguments ************************************************ //
	// *** Basic + Random *** //
	// --solver basic random --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	
	// *** Greedy Solvers *** // 
	// --solver Greedy-SPT Greedy-LRPT Greedy-EST_SPT Greedy-EST_LRPT --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	
	// *** Descent Solvers *** //
	// --solver Descent-SPT Descent-LRPT Descent-EST_SPT Descent-EST_LRPT --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	
	// *** Taboo Solvers *** //
	// --solver Taboo-EST_LRPT(1,1) --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	// --solver Taboo-EST_LRPT(1,10) Taboo-EST_LRPT(2,10) Taboo-EST_LRPT(3,10) Taboo-EST_LRPT(4,10) Taboo-EST_LRPT(5,10) Taboo-EST_LRPT(6,10) Taboo-EST_LRPT(7,10) Taboo-EST_LRPT(8,10) Taboo-EST_LRPT(9,10) Taboo-EST_LRPT(10,10) --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	// --solver Taboo-EST_LRPT(1,100) Taboo-EST_LRPT(6,100) Taboo-EST_LRPT(8,100) Taboo-EST_LRPT(10,100) Taboo-EST_LRPT(12,100) Taboo-EST_LRPT(14,100) Taboo-EST_LRPT(20,100) Taboo-EST_LRPT(50,100) Taboo-EST_LRPT(100,100) --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	// --solver Taboo-EST_LRPT(1,1000) Taboo-EST_LRPT(6,1000) Taboo-EST_LRPT(8,1000) Taboo-EST_LRPT(10,1000) Taboo-EST_LRPT(12,1000) Taboo-EST_LRPT(14,1000) Taboo-EST_LRPT(20,1000) Taboo-EST_LRPT(50,1000) Taboo-EST_LRPT(100,1000) --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	// --solver Taboo-EST_LRPT(1,5000) Taboo-EST_LRPT(6,5000) Taboo-EST_LRPT(8,5000) Taboo-EST_LRPT(10,5000) Taboo-EST_LRPT(12,5000) Taboo-EST_LRPT(14,5000) Taboo-EST_LRPT(20,5000) Taboo-EST_LRPT(50,5000) Taboo-EST_LRPT(100,5000) --instance aaa1 ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09
	
	/** All solvers available in this program */
    private static HashMap<String, Solver> solvers;
    static {
        solvers = new HashMap<>();
        solvers.put("basic", new BasicSolver());
        solvers.put("random", new RandomSolver());
        // Add new solvers here
        // ******************** Greedy Solver ******************** //
        PriorityRule SPT = PriorityRule.SPT;
        solvers.put("Greedy-SPT", new GreedySolver(SPT));
        PriorityRule LRPT = PriorityRule.LRPT;
        solvers.put("Greedy-LRPT", new GreedySolver(LRPT));
        PriorityESTRule EST_SPT = PriorityESTRule.EST_SPT;
        solvers.put("Greedy-EST_SPT", new GreedySolver(EST_SPT));
        PriorityESTRule EST_LRPT = PriorityESTRule.EST_LRPT;
        solvers.put("Greedy-EST_LRPT", new GreedySolver(EST_LRPT));
        
        // ******************* Descent Solver ******************** //
        solvers.put("Descent-SPT",      new DescentSolver(SPT));
        solvers.put("Descent-LRPT",     new DescentSolver(LRPT));
        solvers.put("Descent-EST_SPT",  new DescentSolver(EST_SPT));
        solvers.put("Descent-EST_LRPT", new DescentSolver(EST_LRPT));
        
        // ******************** Taboo Solver ********************* //
        solvers.put("Taboo-EST_LRPT(1,1)",   new TabooSolver(EST_LRPT, 1, 1));
        
        solvers.put("Taboo-EST_LRPT(1,10)",  new TabooSolver(EST_LRPT, 1, 10));
        solvers.put("Taboo-EST_LRPT(2,10)",  new TabooSolver(EST_LRPT, 2, 10));
        solvers.put("Taboo-EST_LRPT(3,10)",  new TabooSolver(EST_LRPT, 3, 10));
        solvers.put("Taboo-EST_LRPT(4,10)",  new TabooSolver(EST_LRPT, 4, 10));
        solvers.put("Taboo-EST_LRPT(5,10)",  new TabooSolver(EST_LRPT, 5, 10));
        solvers.put("Taboo-EST_LRPT(6,10)",  new TabooSolver(EST_LRPT, 6, 10));
        solvers.put("Taboo-EST_LRPT(7,10)",  new TabooSolver(EST_LRPT, 7, 10));
        solvers.put("Taboo-EST_LRPT(8,10)",  new TabooSolver(EST_LRPT, 8, 10));
        solvers.put("Taboo-EST_LRPT(9,10)",  new TabooSolver(EST_LRPT, 9, 10));
        solvers.put("Taboo-EST_LRPT(10,10)", new TabooSolver(EST_LRPT, 10, 10));
        
        solvers.put("Taboo-EST_LRPT(1,100)",   new TabooSolver(EST_LRPT, 1, 100));
        solvers.put("Taboo-EST_LRPT(6,100)",   new TabooSolver(EST_LRPT, 6, 100));
        solvers.put("Taboo-EST_LRPT(8,100)",   new TabooSolver(EST_LRPT, 8, 100));
        solvers.put("Taboo-EST_LRPT(10,100)",  new TabooSolver(EST_LRPT, 10, 100));
        solvers.put("Taboo-EST_LRPT(12,100)",  new TabooSolver(EST_LRPT, 12, 100));
        solvers.put("Taboo-EST_LRPT(14,100)",  new TabooSolver(EST_LRPT, 14, 100));
        solvers.put("Taboo-EST_LRPT(20,100)",  new TabooSolver(EST_LRPT, 20, 100));
        solvers.put("Taboo-EST_LRPT(50,100)",  new TabooSolver(EST_LRPT, 50, 100));
        solvers.put("Taboo-EST_LRPT(100,100)", new TabooSolver(EST_LRPT, 100, 100));
        
        solvers.put("Taboo-EST_LRPT(1,1000)",   new TabooSolver(EST_LRPT, 1, 1000));
        solvers.put("Taboo-EST_LRPT(6,1000)",   new TabooSolver(EST_LRPT, 6, 1000));
        solvers.put("Taboo-EST_LRPT(8,1000)",   new TabooSolver(EST_LRPT, 8, 1000));
        solvers.put("Taboo-EST_LRPT(10,1000)",  new TabooSolver(EST_LRPT, 10, 1000));
        solvers.put("Taboo-EST_LRPT(12,1000)",  new TabooSolver(EST_LRPT, 12, 1000));
        solvers.put("Taboo-EST_LRPT(14,1000)",  new TabooSolver(EST_LRPT, 14, 1000));
        solvers.put("Taboo-EST_LRPT(20,1000)",  new TabooSolver(EST_LRPT, 20, 1000));
        solvers.put("Taboo-EST_LRPT(50,1000)",  new TabooSolver(EST_LRPT, 50, 1000));
        solvers.put("Taboo-EST_LRPT(100,1000)", new TabooSolver(EST_LRPT, 100, 1000));
        
        solvers.put("Taboo-EST_LRPT(1,5000)",   new TabooSolver(EST_LRPT, 1, 5000));
        solvers.put("Taboo-EST_LRPT(6,5000)",   new TabooSolver(EST_LRPT, 6, 5000));
        solvers.put("Taboo-EST_LRPT(8,5000)",   new TabooSolver(EST_LRPT, 8, 5000));
        solvers.put("Taboo-EST_LRPT(10,5000)",  new TabooSolver(EST_LRPT, 10, 5000));
        solvers.put("Taboo-EST_LRPT(12,5000)",  new TabooSolver(EST_LRPT, 12, 5000));
        solvers.put("Taboo-EST_LRPT(14,5000)",  new TabooSolver(EST_LRPT, 14, 5000));
        solvers.put("Taboo-EST_LRPT(20,5000)",  new TabooSolver(EST_LRPT, 20, 5000));
        solvers.put("Taboo-EST_LRPT(50,5000)",  new TabooSolver(EST_LRPT, 50, 5000));
        solvers.put("Taboo-EST_LRPT(100,5000)", new TabooSolver(EST_LRPT, 100, 5000));     
    }


    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
                .defaultHelp(true)
                .description("Solves jobshop problems.");

        parser.addArgument("-t", "--timeout")
                .setDefault(1L)
                .type(Long.class)
                .help("Solver timeout in seconds for each instance");
        parser.addArgument("--solver")
                .nargs("+")
                .required(true)
                .help("Solver(s) to use (space separated if more than one)");

        parser.addArgument("--instance")
                .nargs("+")
                .required(true)
                .help("Instance(s) to solve (space separated if more than one)");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        PrintStream output = System.out;

        long solveTimeMs = ns.getLong("timeout") * 1000;

        List<String> solversToTest = ns.getList("solver");
        for(String solverName : solversToTest) {
            if(!solvers.containsKey(solverName)) {
                System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
                System.err.println("       Available solvers: " + solvers.keySet().toString());
                System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
                System.exit(1);
            }
        }
        List<String> instancePrefixes = ns.getList("instance");
        List<String> instances = new ArrayList<>();
        for(String instancePrefix : instancePrefixes) {
            List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
            if(matches.isEmpty()) {
                System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
                System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
                System.exit(1);
            }
            instances.addAll(matches);
        }

        float[] runtimes = new float[solversToTest.size()];
        float[] distances = new float[solversToTest.size()];

        try {
            output.print(  "                         ");
            for(String s : solversToTest)
                output.printf("%-30s", s);
            output.println();
            output.print("instance size  best      ");
            for(String s : solversToTest) {
                output.print("runtime makespan ecart        ");
            }
            output.println();


            for(String instanceName : instances) {
                int bestKnown = BestKnownResult.of(instanceName);


                Path path = Paths.get("instances/", instanceName);
                Instance instance = Instance.fromFile(path);

                output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);

                for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                    String solverName = solversToTest.get(solverId);
                    Solver solver = solvers.get(solverName);
                    long start = System.currentTimeMillis();
                    long deadline = System.currentTimeMillis() + solveTimeMs;
                    Result result = solver.solve(instance, deadline);
                    long runtime = System.currentTimeMillis() - start;

                    if(!result.schedule.isValid()) {
                        System.err.println("ERROR: solver returned an invalid schedule");
                        System.exit(1);
                    }

                    assert result.schedule.isValid();
                    int makespan = result.schedule.makespan();
                    float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
                    runtimes[solverId] += (float) runtime / (float) instances.size();
                    distances[solverId] += dist / (float) instances.size();

                    output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
                    output.flush();
                }
                output.println();

            }


            output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
            for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
