import java.util.*;

public class MyCITS2200Project implements CITS2200Project {
    public Map<String, Integer> urlToIDMap = new HashMap<>();
    public List<String> idToURLList = new ArrayList<>();
    public Map<Integer, Set<Integer>> adjacencyList = new HashMap<>();
    private int vertexID = 0;

    /**
     * Adds a vertex to the graph.
     * @param url the URL representing the vertex
     * @return the ID of the vertex
     */
    private int addVert(String url) {
        if (!urlToIDMap.containsKey(url)) {
            urlToIDMap.put(url, vertexID);
            idToURLList.add(url);
            adjacencyList.put(vertexID, new HashSet<>());
            vertexID++;
        }
        return urlToIDMap.get(url);
    }

    /**
     * Adds a directed edge in the graph
     * @param urlFrom URL of the starting edge
     * @param urlTo URL of the ending edge
     */
    public void addEdge(String urlFrom, String urlTo) {
        int startID = addVert(urlFrom);
        int endID = addVert(urlTo);
        adjacencyList.get(startID).add(endID);
    }

    /**
     * Calculates the shortest distances from a vertex to all other vertices.
     * @param startVert starting vertex ID
     * @return an array containing the shortest distances
     */
    private int[] getDistances(int startVert) {
        int vertexCount = vertexID;
        int[] distances = new int[vertexCount];
        boolean[] visited = new boolean[vertexCount];
        Queue<Integer> vertexQueue = new LinkedList<>();

        Arrays.fill(distances, -1);
        Arrays.fill(visited, false);

        vertexQueue.add(startVert);
        visited[startVert] = true;
        distances[startVert] = 0;

        while (!vertexQueue.isEmpty()) {
            int currentNode = vertexQueue.poll();
            Set<Integer> adjacentNode = adjacencyList.get(currentNode);
            for (Integer neighbour : adjacentNode) {
                if (!visited[neighbour]) {
                    vertexQueue.add(neighbour);
                    visited[neighbour] = true;
                    distances[neighbour] = distances[currentNode] + 1;
                }
            }
        }
        return distances;
    }

    /**
     * Returns the shortest path from one URL to another.
     * @param urlFrom URL of the starting path
     * @param urlTo URL of the ending path
     * @return the length of the shortest path
     */
    public int getShortestPath(String urlFrom, String urlTo) {
        if (!urlToIDMap.containsKey(urlFrom) || !urlToIDMap.containsKey(urlTo)) {
            throw new IllegalArgumentException("One or both URLs not found in the graph.");
        }

        int startID = urlToIDMap.get(urlFrom);
        int endID = urlToIDMap.get(urlTo);
        int[] distances = getDistances(startID);

        if (distances[endID] == -1) {
            throw new IllegalStateException("There is no path between the two URLs.");
        }

        return distances[endID];
    }

    // Field variables for finding Hamiltonian path
    private Boolean[][] memo;
    private int[][] successor;

    /**
     * Determines if a Hamiltonian path exists in the graph.
     * @param i the vertex to start the path from
     * @param set a bitset representing the vertices that must be traversed
     * @return a Boolean indicating whether a Hamiltonian path exists
     */
    private boolean hasHamiltonianPath(int i, int set) {
        if (memo[i][set] != null) {
            return memo[i][set];
        }

        if (set == (1 << i)) {
            memo[i][set] = true;
            return true;
        }

        int newSet = set & ~(1 << i);

        for (int k : adjacencyList.get(i)) {
            if ((set & (1 << k)) != 0 && hasHamiltonianPath(k, newSet)) {
                memo[i][set] = true;
                successor[i][set] = k;
                return true;
            }
        }

        memo[i][set] = false;
        return false;
    }

    /**
     * Returns a Hamiltonian path in the graph, if one exists.
     * @return an array of URLs representing a Hamiltonian path, or an empty array if there is none
     */
    public String[] getHamiltonianPath() {
        int vertexCount = vertexID;
        int allSet = (1 << vertexCount) - 1;
        memo = new Boolean[vertexCount][1 << vertexCount];
        successor = new int[vertexCount][1 << vertexCount];

        for (int[] row : successor) {
            Arrays.fill(row, -1);
        }

        for (int i = 0; i < vertexCount; i++) {
            if (hasHamiltonianPath(i, allSet)) {
                List<String> path = new ArrayList<>();
                int set = allSet;

                while (set != 0) {
                    path.add(idToURLList.get(i));
                    int childVertex = successor[i][set];
                    set &= ~(1 << i);
                    if (childVertex != -1) {
                        i = childVertex;
                    }
                }
                return path.toArray(new String[0]);
            }
        }

        return new String[0];
    }

    // Field variables for finding SCCs
    private int[] dfsOrderID;
    private int[] lowLink;
    private boolean[] onStack;
    private Deque<Integer> dfsStack;
    private List<List<String>> sccList;
    private int dfsCounter;

    /**
     * Performs a depth-first search (DFS) on the graph starting from a specific vertex.
     * @param currentNode the vertex ID to start the DFS from
     */
    private void DFS(int currentNode) {
        dfsOrderID[currentNode] = lowLink[currentNode] = dfsCounter++;
        dfsStack.push(currentNode);
        onStack[currentNode] = true;
        Set<Integer> neighbours = adjacencyList.get(currentNode);

        for (int neighbour : neighbours) {
            if (dfsOrderID[neighbour] == 0) {
                DFS(neighbour);
                lowLink[currentNode] = Math.min(lowLink[currentNode], lowLink[neighbour]);
            } else if (onStack[neighbour]) {
                lowLink[currentNode] = Math.min(lowLink[currentNode], dfsOrderID[neighbour]);
            }
        }

        if (lowLink[currentNode] == dfsOrderID[currentNode]) {
            List<String> scc = new ArrayList<>();
            int node;
            do {
                node = dfsStack.pop();
                onStack[node] = false;
                scc.add(idToURLList.get(node));
            } while (node != currentNode);
            sccList.add(scc);
        }
    }

    /**
     * Returns all strongly connected components in the graph.
     * @return a 2D array where each inner array represents a strongly connected component
     */
    public String[][] getStronglyConnectedComponents() {
        int vertexCount = vertexID;
        dfsOrderID = new int[vertexCount];
        lowLink = new int[vertexCount];
        onStack = new boolean[vertexCount];
        dfsStack = new ArrayDeque<>();
        sccList = new ArrayList<>();
        dfsCounter = 1;

        for (int i = 0; i < vertexCount; i++) {
            if (dfsOrderID[i] == 0) {
                DFS(i);
            }
        }

        String[][] sccArray = new String[sccList.size()][];
        for (int i = 0; i < sccList.size(); i++) {
            sccArray[i] = sccList.get(i).toArray(new String[0]);
        }

        return sccArray;
    }

    /**
     * Returns the centers of the graph.
     * @return an array containing the URLs of the center vertices
     */
    public String[] getCenters() {
        int vertexCount = vertexID;
        int[] eccentricity = new int[vertexCount];
        int minEccentricity = Integer.MAX_VALUE;

        for (int i = 0; i < vertexCount; i++) {
            int[] distances = getDistances(i);
            eccentricity[i] = Arrays.stream(distances).max().orElse(-1);
            minEccentricity = Math.min(minEccentricity, eccentricity[i]);
        }

        List<String> centers = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            if (eccentricity[i] == minEccentricity) {
                centers.add(idToURLList.get(i));
            }
        }

        return centers.toArray(new String[0]);
    }
}