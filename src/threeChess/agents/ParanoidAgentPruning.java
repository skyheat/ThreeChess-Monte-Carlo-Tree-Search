package threeChess.agents;

import threeChess.*;

/**
 * An interface for AI bots to implement. They are simply given a Board object
 * indicating the positions of all pieces, the history of the game and whose
 * turn it is, and they respond with a move, expressed as a pair of positions.
 **/
public class ParanoidAgentPruning extends Agent {

    private static final String name = "ParanoidAgentPruning";

    /**
     * A no argument constructor, required for tournament management.
     **/
    public ParanoidAgentPruning() {
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
        // set depth of 2
        Object result[] = paranoidSearchPruning(board, 2, 2, playerColour, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return new Position[] { (Position) result[0], (Position) result[1] };
    }

    /**
     * The root player assumes that all the opponents build a coalition against him.
     * The root player is the only max-player and all the other players are
     * min-players which try to minimize the value of the root player.
     * 
     * @author Jonathan Neo (21683439)
     * @param board           the chess board
     * @param depth           the search depth
     * @param startDepth      the starting depth - used to search for kingTakes
     * @param maxPlayerColour the max player's colour
     * @param alpha           alpha for pruning
     * @param beta            beta for pruning
     * @return an object containing the start position, end position and score
     */
    public Object[] paranoidSearchPruning(Board board, int startDepth, int depth, Colour maxPlayerColour, int alpha,
            int beta) {
        // if it is players turn, then maximising player, else minimising player
        int worstScore = maxPlayerColour == board.getTurn() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score; // final score
        // get positions for the specified player
        Position[] occupiedPositions = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position start = occupiedPositions[0];
        Position end = occupiedPositions[0];
        // used to hold the best move seen so far. A list containing: start, end, score
        Object best[] = { start, end, worstScore };
        // for each of the player's piece on the board...
        for (Position position : occupiedPositions) {
            start = position;
            end = start;
            Piece piece = board.getPiece(start); // get the piece occupying the position

            Direction[][] steps = piece.getType().getSteps(); // get steps that the piece type can move
            // get number of repetitions of steps the piece can take
            int maxReps = piece.getType().getStepReps();

            // for each step the piece can take...
            for (Direction[] step : steps) {

                // for each repitition option...
                for (int reps = 1; reps <= maxReps; reps++) {

                    // get end position
                    for (int i = 0; i < reps; i++) {
                        try {
                            end = board.step(piece, step, end, start.getColour() != end.getColour());
                        } catch (ImpossiblePositionException e) {
                            // can't go any further
                            reps = maxReps;
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
                                    if (b.getWinner() == maxPlayerColour) {
                                        return new Object[] { start, end, Integer.MAX_VALUE };
                                    } else {
                                        continue; // skip this move if it can cause me to lose
                                    }
                                }
                            }

                            // if game end
                            if (depth == 0 || b.gameOver()) {
                                score = evaluate(b, maxPlayerColour);
                            }

                            // maximising or minimising players
                            else {
                                Object result[] = paranoidSearchPruning(b, startDepth, depth - 1, maxPlayerColour,
                                        alpha, beta);
                                score = (int) result[2];
                            }

                            // if move was better than previous, then keep it
                            if ((maxPlayerColour == board.getTurn() && score > (int) best[2])
                                    || (maxPlayerColour != board.getTurn() && score < (int) best[2])) {
                                best = new Object[] { start, end, score };
                            }

                            if (maxPlayerColour == board.getTurn() && score > alpha) {
                                alpha = score;
                            }

                            if (maxPlayerColour != board.getTurn() && score < beta) {
                                beta = score;
                            }

                            if (beta <= alpha) {
                                continue;
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
     * Returns the evaluation of the game for the max player colour, where higher
     * values are better. If the player wins, then score = MAX. If the player loses,
     * then score = MIN. Else return the player's score on the board.
     * 
     * @param board           the chess board
     * @param maxPlayerColour the colour of the max player
     * @return the score of the board after the move is played.
     */
    public int evaluate(Board board, Colour maxPlayerColour) {
        int score;
        if (board.getTurn() == maxPlayerColour) {
            if (board.gameOver()) {
                if (board.getWinner() == maxPlayerColour) {
                    score = Integer.MAX_VALUE;
                } else {
                    score = 0;
                }
            } else {
                score = board.score(board.getTurn());
            }
        } else {
            if (board.gameOver()) {
                if (board.getWinner() != maxPlayerColour) {
                    score = 0;
                } else {
                    score = Integer.MAX_VALUE;
                }
            } else {
                score = board.score(board.getTurn());
            }
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
