import java.io.*;
import java.util.*;

public class CITS2200ProjectTester {

    private static Random random = new Random();
    private static Map<Integer, Double> sparseResults = new HashMap<>();
    private static Map<Integer, Double> denseResults = new HashMap<>();
    private static String[] testNames = {"Test 1: Check shortest path between two URLs", "Test 2: Check centers of the graph",
            "Test 3: Check strongly connected components", "Test 4: Check Hamiltonian path"};

    public static void generateAndLoadGraph(CITS2200Project project, int n, boolean isDense, String path) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            // Create a shuffled list of vertices
            List<Integer> vertices = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                vertices.add(i);
            }
            Collections.shuffle(vertices);

            // Add edges for a sparse graph
            for (int i = 0; i < n; i++) {
                String from = "www.node" + vertices.get(i) + ".com";
                String to = "www.node" + vertices.get((i + 1) % n) + ".com";
                writer.write(from + "\n");
                writer.write(to + "\n");
                project.addEdge(from, to);
            }

            if (isDense) {
                // Add additional edges for a dense graph
                for (int i = 0; i < n; i++) {
                    for (int j = i + 2; j < n; j++) {
                        if (random.nextBoolean()) { // Randomly decide whether to add an edge
                            String from = "www.node" + vertices.get(i) + ".com";
                            String to = "www.node" + vertices.get(j) + ".com";
                            writer.write(from + "\n");
                            writer.write(to + "\n");
                            project.addEdge(from, to);
                        }
                    }
                }
            }

            writer.close();
        } catch (Exception e) {
            System.out.println("Graph generation and loading error.");
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        // List of graph sizes to test on
        int[] sizes = {5, 10, 15, 20};
        // List of graph densities to test on
        boolean[] densities = {false, true};
        String pathToGraphFile = "CITS2200Project/graphData.txt";

        for (String testName : testNames) {
            System.out.println("\n--------------------------- START OF TEST ---------------------------");
            System.out.println(testName);

            for (int n : sizes) {
                for (boolean isDense : densities) {
                    MyCITS2200Project proj = new MyCITS2200Project();
                    generateAndLoadGraph(proj, n, isDense, pathToGraphFile);

                    int numRuns = 20;
                    int sourceVertex = 0;
                    int endVertex = n - 1; // fixate the endVertex
                    String urlFrom = proj.idToURLList.get(sourceVertex);
                    String urlTo = proj.idToURLList.get(endVertex);

                    long totalDurationInNano = 0;
                    for (int i = 0; i < numRuns; i++) {
                        long startTime = System.nanoTime();

                        // Run the appropriate test
                        switch (testName) {
                            case "Test 1: Check shortest path between two URLs":
                                proj.getShortestPath(urlFrom, urlTo);
                                break;
                            case "Test 2: Check centers of the graph":
                                proj.getCenters();
                                break;
                            case "Test 3: Check strongly connected components":
                                proj.getStronglyConnectedComponents();
                                break;
                            case "Test 4: Check Hamiltonian path":
                                proj.getHamiltonianPath();
                                break;
                        }

                        long endTime = System.nanoTime();
                        long durationInNano = endTime - startTime;
                        totalDurationInNano += durationInNano;
                    }

                    double averageDurationInNano = totalDurationInNano / (double) numRuns;
                    if (isDense) {
                        denseResults.put(n, averageDurationInNano);
                    } else {
                        sparseResults.put(n, averageDurationInNano);
                    }
                }
            }

            // Display the results for each density
            for (boolean isDense : densities) {
                System.out.println("\nDensity: " + (isDense ? "Dense" : "Sparse"));

                // Display the results for each size
                for (int n : sizes) {
                    double averageExecutionTimeNano = isDense ? denseResults.get(n) : sparseResults.get(n);
                    System.out.println("Average execution time in nanoseconds (" + n + " vertices): " + averageExecutionTimeNano);
                }
            }
        }
    }
}
