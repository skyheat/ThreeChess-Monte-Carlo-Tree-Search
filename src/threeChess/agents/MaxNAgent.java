package threeChess.agents;

import threeChess.*;

import java.util.*;

/**
 * An interface for AI bots to implement. They are simply given a Board object
 * indicating the positions of all pieces, the history of the game and whose
 * turn it is, and they respond with a move, expressed as a pair of positions.
 **/

public class MaxNAgent extends Agent {
    private static final String name = "MaxNAgent";

    /**
     * A no argument constructor, required for tournament management.
     **/
    public MaxNAgent() {
    }

    /**
     * Play a move in the game. The agent is given a Board Object representing the
     * position of all pieces, the history of the game and whose turn it is. They
     * respond with a move represented by a pair (two element array) of positions:
     * the start and the end position of the move.
     * 
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is
     *         the current position of the piece to be moved, and the second element
     *         is the position to move that piece to.
     **/
    public Position[] playMove(Board board) {
        // get maximising player's turn
        Colour playerColour = board.getTurn();
        List<Object[]> result = maxNSearch(board, 2, 2); // set depth of 2
        Object finalResult[] = {};
        for (Object[] res : result) {
            if ((Colour) res[0] == playerColour) {
                finalResult = res;
                break;
            }
        }
        return new Position[] { (Position) finalResult[1], (Position) finalResult[2] };
    }

    /**
     * Implement MaxN algorithm which assumes that every player will try to make
     * their best move.
     * 
     * @author Jonathan Neo (21683439)
     * @param board      the chess board
     * @param depth      the search depth
     * @param startDepth the starting depth - used to search for kingTakes
     * @return a list of objects containing the colour, start and end positions, and
     *         best score
     */
    public List<Object[]> maxNSearch(Board board, int depth, int startDepth) {
        // get positions for the specified player
        Position[] occupiedPositions = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position start = occupiedPositions[0];
        Position end = occupiedPositions[0];

        ArrayList<Object[]> best = new ArrayList<>();
        for (Colour colour : Colour.values()) {
            Object[] worstScore = { colour, start, end, Integer.MIN_VALUE }; // [colour, start, end, bestScore]
            best.add(worstScore);
        }

        // for each of the player's piece on the board...
        for (Position position : occupiedPositions) {
            start = position;
            end = start;
            Piece piece = board.getPiece(start); // get the piece occupying the position

            Direction[][] steps = piece.getType().getSteps(); // get the type and steps that the piece can move

            // for each step the piece can take...
            for (Direction[] step : steps) {
                // get number of repetitions of steps the piece can take
                int maxReps = piece.getType().getStepReps();

                // for each repitition option...
                for (int reps = 1; reps <= maxReps; reps++) {

                    // get end position
                    for (int i = 0; i < reps; i++) {
                        try {
                            end = board.step(piece, step, end, start.getColour() != end.getColour());
                        } catch (ImpossiblePositionException e) {
                            // ignore the position
                        }
                    }

                    // if move is legal
                    if (board.isLegalMove(start, end)) {
                        try {
                            Board b = (Board) board.clone(); // copy board
                            b.move(start, end);

                            // check for checkmate
                            if (depth == startDepth) {
                                if (b.gameOver()) {
                                    if (b.getWinner() == board.getTurn()) {
                                        for (int i = 0; i < best.size(); i++) {
                                            if ((Colour) best.get(i)[0] == board.getTurn()) {
                                                Object[] winningScore = { board.getTurn(), start, end,
                                                        Integer.MAX_VALUE };
                                                best.set(i, winningScore);
                                            }
                                        }
                                        return best;
                                    } else {
                                        continue; // skip this move if it can cause me to lose
                                    }
                                }
                            }

                            // if game end
                            List<Object[]> evaluatedScore;
                            if (depth == 0 || b.gameOver()) {
                                evaluatedScore = evaluate(b, start, end);
                            }

                            // maximising player
                            else {
                                evaluatedScore = maxNSearch(b, depth - 1, startDepth);
                            }

                            // find best move for current player
                            for (Object[] newScore : evaluatedScore) {
                                if (((Colour) newScore[0] == board.getTurn())) {
                                    // compare against best scores
                                    for (int i = 0; i < best.size(); i++) {
                                        Object[] bestScore = best.get(i);
                                        if ((Colour) bestScore[0] == board.getTurn()) {
                                            // if new score is greater than current best score
                                            if ((int) newScore[3] > (int) bestScore[3]) {
                                                // update best score
                                                for (int j = 0; j < evaluatedScore.size(); j++) {
                                                    Object[] newBest = { evaluatedScore.get(j)[0], start, end,
                                                            evaluatedScore.get(j)[3] };
                                                    best.set(j, newBest);
                                                }
                                            }
                                        }
                                    }
                                    break; // found match
                                }
                            }
                        } catch (CloneNotSupportedException e) {
                            System.out.println("cloning of board is not supported.");
                        } catch (ImpossiblePositionException e) {
                            System.out
                                    .println("piece cannot move from: " + start.toString() + ", to: " + end.toString());
                        }
                    }
                }
            }
        }
        return best;
    }

    /**
     * Returns an array of object[]. Each object[] contains: [colour, score].
     * 
     * @param board the chess board
     * @return the score of each player after the move is played
     */
    public List<Object[]> evaluate(Board board, Position start, Position end) {
        ArrayList<Object[]> score = new ArrayList<>();

        for (Colour colour : Colour.values()) {
            Object[] colourScore = { colour, start, end, board.score(colour) };
            score.add(colourScore);
        }
        return score;
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
