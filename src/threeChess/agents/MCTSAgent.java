package threeChess.agents;

import threeChess.*;

import java.util.Random;
import java.util.*;

//Monte-Carlo Tree Search Implementation

/**
 * An interface for AI bots to implement. They are simply given a Board object
 * indicating the positions of all pieces, the history of the game and whose
 * turn it is, and they respond with a move, expressed as a pair of positions.
 **/
public class MCTSAgent extends Agent {

    private String name;
    // private static final Random random = new Random();
    private Colour agentTurn;

    /**
     * A no argument constructor, required for tournament management.
     **/
    public MCTSAgent() {
        this.name = "MCTSAgent";
    }

    /**
     * Play a move in the game. The agent is given a Board Object representing the
     * position of all pieces, the history of the game and whose turn it is. They
     * respond with a move represented by a pair (two element array) of positions:
     * the start and the end position of the move.
     * 
     * @author Jun Han Yap (22507198)
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is
     *         the current position of the piece to be moved, and the second element
     *         is the position to move that piece to.
     **/
    public Position[] playMove(Board board) {
        agentTurn = board.getTurn();
        long totalTime = 0;
        long timeLimit = 200;
        int nLoops = 0;
        long averageTime = 0;
        MonteCarloTree mct = new MonteCarloTree(board);
        Position[][] moves = legalMoves(board);
        for (Position[] move : moves) {
            Board newBoard;
            try {
                newBoard = (Board) board.clone();
                newBoard.move(move[0], move[1]);
                if (newBoard.gameOver()) {
                    if (newBoard.getWinner() == agentTurn) {
                        return move;
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            } catch (ImpossiblePositionException | CloneNotSupportedException e) {
            }

        }

        MCTSNode bestNode = null;
        while (timeLimit - totalTime > averageTime) {
            long startTime = System.currentTimeMillis();
            // Selection phase.
            bestNode = mct.selectNode();

            // Expansion phase.
            MCTSNode expandedNode = mct.expansionPhase(bestNode);

            // Simulation phase.
            boolean won = mct.simulateGame(expandedNode);

            // Back propogation phase.
            mct.backPropagate(expandedNode, won);
            long executionTime = System.currentTimeMillis() - startTime;
            nLoops++;
            totalTime += executionTime;
            averageTime = totalTime / (long) nLoops;
        }
        Position[] bestNodeTest = mct.getBestMove();
        if (board.isLegalMove(bestNodeTest[0], bestNodeTest[1])) {
            return bestNodeTest;
        } else {
            Position[][] finalMoves = legalMoves(board);
            Random random = new Random();
            int randNum = random.nextInt(finalMoves.length);

            return new Position[] { finalMoves[randNum][0], finalMoves[randNum][1] };
        }

    }

    /**
     * All legal moves that can be done from the current position
     * 
     * @param board the current state of the game.
     * @return a 2D array indicating all available moves.
     */
    private Position[][] legalMoves(Board board) {
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position[] spots = Position.values();
        ArrayList<Position[]> moves = new ArrayList<>();
        for (Position piece : pieces) {
            for (Position spot : spots) {
                Position[] current = new Position[] { piece, spot };
                if (board.isLegalMove(piece, spot) && !moves.contains(current))
                    moves.add(current);
            }
        }
        return moves.toArray(new Position[0][0]);
    }

    private class MonteCarloTree {
        /**
         * A MonteCarloTree that performs search and holds the nodes
         */

        private MCTSNode rootNode;

        /**
         * @param node Tree Root Node
         */
        public MonteCarloTree(MCTSNode node) {
            rootNode = node;
        }

        /**
         * @param board Root Node Board
         */
        public MonteCarloTree(Board board) {
            this(new MCTSNode(board));
        }

        /**
         * Selection phase
         * 
         * @param MCTSNode Node to Select Optimal Child
         * @return Optimal Child
         */
        public MCTSNode selectNode(MCTSNode node) {
            if (node.getChildren().size() == 0) {
                return node;
            }

            MCTSNode bestNode = null;
            double bestNodeScore = -1;
            for (MCTSNode child : node.getChildren()) {
                if (child.getUpperConfidenceBound() > bestNodeScore) {
                    bestNode = child;
                    bestNodeScore = child.getUpperConfidenceBound();
                }
            }
            return selectNode(bestNode);
        }

        /**
         * Selection Phase
         * 
         * @return Optimal Child
         */
        public MCTSNode selectNode() {
            return selectNode(rootNode);
        }

        public Position[] getBestMove() {
            if (rootNode.getChildren().size() == 0) {
                throw new IllegalStateException("MCTS has not been done yet");
            }

            MCTSNode bestNode = null;
            double bestNodeScore = -1;
            for (MCTSNode child : rootNode.getChildren()) {
                double score;
                if (child.getVisits() != 0) {
                    score = (double) child.getWins() / child.getVisits();
                } else {
                    score = 0;
                }
                if (score > bestNodeScore) {
                    bestNode = child;
                    bestNodeScore = score;
                }
            }
            return bestNode.getPlayedMove();
        }

        /**
         * Expands a node and appends all its children
         * 
         * @param node The node to be expanded
         */
        public void expandNode(MCTSNode node) {
            Board state = node.getState();
            Position[][] moves = legalMoves(state);
            // Get all valid moves and append them
            for (Position[] move : moves) {
                Board newBoard;
                try {
                    newBoard = (Board) node.getState().clone();
                    newBoard.move(move[0], move[1]);
                    node.appendChild(newBoard);
                } catch (CloneNotSupportedException | ImpossiblePositionException e) {
                }

            }
        }

        /**
         * Expansion Phase
         * 
         * @param node The node to be expanded
         * @return Random child node
         */
        public MCTSNode expansionPhase(MCTSNode node) {
            if (node.getState().gameOver())
                return node;
            else {
                expandNode(node);
                Random random = new Random();
                List<MCTSNode> children = node.getChildren();
                return children.get(random.nextInt(children.size()));
            }
        }

        /**
         * Simulation Phase
         * 
         * @param MCTSNode Node to simulate
         * @return True if agent won, False if agent did not win
         */
        public boolean simulateGame(MCTSNode node) {
            Board board = node.getState();
            Colour playerTurn = board.getTurn();

            while (board.gameOver() == false) {
                Position[][] moves = legalMoves(board);
                Random random = new Random();
                int randNum = random.nextInt(moves.length);
                try {
                    board.move(moves[randNum][0], moves[randNum][1]);
                } catch (ImpossiblePositionException e) {

                }
            }

            if (board.getWinner() == playerTurn) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Performs the backpropagation phase Backpropagation Phase
         *
         * @param MCTSNode Node to backpropagate from
         * @param boolean  outcome of the game
         */
        public void backPropagate(MCTSNode node, boolean outcome) {
            node.incrementVisited();
            if (outcome)
                node.incrementWon();
            while (node.hasParent()) {
                node = node.getParent();
                node.incrementVisited();
                if (outcome)
                    node.incrementWon();
            }
        }
    }

    private class MCTSNode {
        private int nVisits;
        private int nWins;
        private MCTSNode parent;
        private List<MCTSNode> children;
        private Board board;

        /**
         * @param parent The parent of this node.
         * @param state  The gamestate this node represents
         */
        public MCTSNode(MCTSNode parent, Board board) {
            this.parent = parent;
            this.board = board;
            nVisits = 0;
            nWins = 0;
            children = new ArrayList<MCTSNode>();
        }

        /**
         * Creates root node
         * 
         * @param state State this node represents
         */
        // public MCTSNode(GameState state) {
        public MCTSNode(Board board) {
            // this(null, state);
            this(null, board);
        }

        /**
         * Upper Confidence Bound Formula
         * 
         * @return The UCB value of this node
         */
        public double getUpperConfidenceBound() {
            double winScore;
            if (nVisits != 0) {
                winScore = (double) nWins / nVisits;
            } else {
                winScore = 0;
            }

            // double c = 1/Math.sqrt(2);
            double c = 1.0;
            double lnt = Math.log(parent.getVisits());
            double ucb = winScore + c * Math.sqrt(lnt / nVisits);
            return ucb;
        }

        /**
         * @return The number of times this node has been visited
         */
        public int getVisits() {
            return nVisits;
        }

        /**
         * @return The number of simulated games this node has won
         */
        public int getWins() {
            return nWins;
        }

        /**
         * @return This node's parent
         */
        public MCTSNode getParent() {
            return parent;
        }

        /**
         * @return True if this node has a parent. False if this is the root node.
         */
        public boolean hasParent() {
            return getParent() != null;
        }

        /**
         * @return A list of this node's children
         */
        public List<MCTSNode> getChildren() {
            return children;
        }

        /**
         * @return The board of this node
         */
        public Board getState() {
            return board;
        }

        /**
         * Increment this node's win count
         */
        public void incrementWon() {
            nWins++;
        }

        /**
         * Increment this node's visited count
         */
        public void incrementVisited() {
            nVisits++;
        }

        /**
         * Add a child to this node.
         * 
         * @param state The gamestate to build the child node from
         */
        public void appendChild(Board board) {
            MCTSNode node = new MCTSNode(this, board);
            children.add(node);
        }

        /**
         * The move that got to this node
         * 
         * @return The card played to create this node from its parent.
         */
        public Position[] getPlayedMove() {

            int moveCount = board.getMoveCount();
            Position[] move = board.getMove(moveCount - 1);
            return move;
        }
    }

    /**
     * @return the Agent's name, for annotating game description.
     **/
    public String toString() {
        return name;
    }

    /**
     * Displays the final board position to the agent, if required for learning
     * purposes. Other a default implementation may be given.
     * 
     * @param finalBoard the end position of the board
     **/
    public void finalBoard(Board finalBoard) {
    }

}
