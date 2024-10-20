package org.example;

import java.util.*;

class PuzzleNode {
    int[][] state;
    int blankX, blankY;
    int g;
    int h;
    PuzzleNode parent;
    String move;

    public PuzzleNode(int[][] state, int blankX, int blankY, int g, int h, PuzzleNode parent, String move) {
        this.state = state;
        this.blankX = blankX;
        this.blankY = blankY;
        this.g = g;
        this.h = h;
        this.parent = parent;
        this.move = move;
    }

    public int getF() {
        return g + h;
    }

    public static int calculateH1(int[][] state, int[][] goalState) {
        int h1 = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (state[i][j] != 0 && state[i][j] != goalState[i][j]) {
                    h1++;
                }
            }
        }
        return h1;
    }

    public List<PuzzleNode> generateSuccessors() {
        List<PuzzleNode> successors = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        String[] moves = {"Down", "Up", "Right", "Left"};

        for (int i = 0; i < directions.length; i++) {
            int newX = blankX + directions[i][0];
            int newY = blankY + directions[i][1];
            if (newX >= 0 && newX < 3 && newY >= 0 && newY < 3) {
                int[][] newState = deepCopy(state);
                newState[blankX][blankY] = newState[newX][newY];
                newState[newX][newY] = 0;
                successors.add(new PuzzleNode(newState, newX, newY, g + 1, 0, this, moves[i]));
            }
        }
        return successors;
    }

    public boolean isGoal(int[][] goalState) {
        return Arrays.deepEquals(state, goalState);
    }

    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    public List<String> getMoves() {
        List<String> moves = new ArrayList<>();
        PuzzleNode current = this;
        while (current.move != null) {
            moves.add(current.move);
            current = current.parent;
        }
        Collections.reverse(moves);
        return moves;
    }
}

class PuzzleSolver {
    private static final int[][] GOAL_STATE = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8}
    };

    public static List<String> aStarSearch(int[][] initialState, int blankX, int blankY) {
        PriorityQueue<PuzzleNode> openList = new PriorityQueue<>(Comparator.comparingInt(PuzzleNode::getF));
        Set<String> closedList = new HashSet<>();

        int steps = 0;
        int generatedStates = 0;
        int maxStoredStates = 0;

        PuzzleNode startNode = new PuzzleNode(initialState, blankX, blankY, 0, PuzzleNode.calculateH1(initialState, GOAL_STATE), null, null);
        openList.add(startNode);
        generatedStates++;

        while (!openList.isEmpty()) {
            steps++;
            PuzzleNode currentNode = openList.poll();

            maxStoredStates = Math.max(maxStoredStates, openList.size() + closedList.size());

            if (currentNode.isGoal(GOAL_STATE)) {
                System.out.println("Number of iterations: " + steps);
                System.out.println("Generated states: " + generatedStates);
                System.out.println("Max stored states: " + maxStoredStates);
                return currentNode.getMoves();
            }

            closedList.add(Arrays.deepToString(currentNode.state));

            for (PuzzleNode successor : currentNode.generateSuccessors()) {
                if (!closedList.contains(Arrays.deepToString(successor.state))) {
                    successor.h = PuzzleNode.calculateH1(successor.state, GOAL_STATE);
                    openList.add(successor);
                    generatedStates++;
                }
            }
        }
        return null;
    }

    public static List<String> limitedDepthFirstSearch(int[][] initialState, int blankX, int blankY, int maxDepth) {
        Stack<PuzzleNode> stack = new Stack<>();
        Set<String> visited = new HashSet<>();

        int steps = 0;
        int deadEnds = 0;
        int generatedStates = 0;
        int maxStoredStates = 0;

        PuzzleNode startNode = new PuzzleNode(initialState, blankX, blankY, 0, 0, null, null);
        stack.push(startNode);
        generatedStates++;

        while (!stack.isEmpty()) {
            steps++;
            PuzzleNode currentNode = stack.pop();

            maxStoredStates = Math.max(maxStoredStates, stack.size() + visited.size());

            if (currentNode.isGoal(GOAL_STATE)) {
                System.out.println("Number of iterations: " + steps);
                System.out.println("Generated states: " + generatedStates);
                System.out.println("Max stored states: " + maxStoredStates);
                System.out.println("Number of dead ends: " + deadEnds);
                return currentNode.getMoves();
            }

            if (currentNode.g >= maxDepth) {
                deadEnds++;
                continue;
            }

            visited.add(Arrays.deepToString(currentNode.state));

            for (PuzzleNode successor : currentNode.generateSuccessors()) {
                if (!visited.contains(Arrays.deepToString(successor.state))) {
                    stack.push(successor);
                    generatedStates++;
                }
            }
        }
        return null;
    }

    public static int[][] generateRandomPuzzle() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i <= 8; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        int[][] puzzle = new int[3][3];
        int blankX = 0, blankY = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                puzzle[i][j] = numbers.get(i * 3 + j);
                if (puzzle[i][j] == 0) {
                    blankX = i;
                    blankY = j;
                }
            }
        }

        if (!isSolvable(puzzle)) {
            return generateRandomPuzzle();
        }
        return puzzle;
    }

    public static boolean isSolvable(int[][] puzzle) {
        int[] flat = new int[9];
        int index = 0;
        for (int[] row : puzzle) {
            for (int num : row) {
                flat[index++] = num;
            }
        }

        int inversions = 0;
        for (int i = 0; i < flat.length; i++) {
            for (int j = i + 1; j < flat.length; j++) {
                if (flat[i] != 0 && flat[j] != 0 && flat[i] > flat[j]) {
                    inversions++;
                }
            }
        }

        return inversions % 2 == 0;
    }

    public static void printPuzzle(int[][] puzzle) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (puzzle[i][j] == 0) {
                    System.out.print("  ");
                } else {
                    System.out.print(puzzle[i][j] + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose algorithm: 1 - A*, 2 - LDFS:");
        int choice = scanner.nextInt();

        int[][] initialState = generateRandomPuzzle();
        int blankX = 0, blankY = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (initialState[i][j] == 0) {
                    blankX = i;
                    blankY = j;
                }
            }
        }

        System.out.println("Base state:");
        printPuzzle(initialState);

        List<String> solution = null;

        if (choice == 1) {
            solution = aStarSearch(initialState, blankX, blankY);
        } else if (choice == 2) {
            //System.out.println("Enter max depth:");
            //int maxDepth = scanner.nextInt();
            int maxDepth = 100;
            solution = limitedDepthFirstSearch(initialState, blankX, blankY, maxDepth);
        }

        if (solution != null) {
            System.out.println("Sequence of moves:");
            System.out.println(String.join(" -> ", solution));
            System.out.println("Number of moves: " + solution.size());
        } else {
            System.out.println("There is no solution");
        }
    }
}
