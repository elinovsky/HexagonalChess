package ru.vsu.cs.kuznetsov.scripts;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static class Player{
        List<Figure> aliveFigures;
        Factions faction;

        Player(Factions faction){
            this.faction = faction;
            aliveFigures = new ArrayList<>();
        }

//        Figure.King king;
    }

    HexagonalMap board;
    Player[] turnQueue;
    int activPlayerIndex;
    Figure selectedFigure = null;
    Factions[] gameFactions = new Factions[]{Factions.WHITE, Factions.BLACK};

    public Game(){
        board = new HexagonalMap();
        activPlayerIndex = 0;
        turnQueue = new Player[2];
        for (int i = 0; i < turnQueue.length; i++){
            turnQueue[i] = initPlayer(gameFactions[i]);
        }
    }

    /**
     * Give turn to next player.
     */
    public void giveTurnFurther(){
        activPlayerIndex = (activPlayerIndex + 1) % turnQueue.length;
        selectedFigure = null;
    }

    public HexagonalMap getBoard(){
        return board;
    }

    /**
     *
     * @param faction - faction of all player's figures
     * @return new player with all figures initialized and placed on board
     */
    private Player initPlayer(Factions faction){
        Player newPlayer = new Player(faction);
        if (faction == Factions.BLACK){
            newPlayer.aliveFigures.add(new Figure.King(board, board.getCellState('g', 10), faction));
            for (int i = 11; i >= 9; i--) {
                newPlayer.aliveFigures.add(new Figure.Bishop(board, board.getCellState('f', i), faction));
            }
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('c', 8), faction));
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('i', 8), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('d', 9), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('h', 9), faction));
            newPlayer.aliveFigures.add(new Figure.Queen(board, board.getCellState('e', 10), faction));
            //newPlayer.king = new Figure.King(board, board.getCellState('g', 10), faction);
            for (int i = 1; i < 10; i++){
                newPlayer.aliveFigures.add(new
                        Figure.Pawn(board, board.getCellState(HexagonalMap.validLetters[i], 7), faction));
            }
        } else {
            newPlayer.aliveFigures.add(new Figure.King(board, board.getCellState('g', 1), faction));
            for (int i = 3; i >= 1; i--) {
                newPlayer.aliveFigures.add(new Figure.Bishop(board, board.getCellState('f', i), faction));
            }
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('c', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('i', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('d', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('h', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Queen(board, board.getCellState('e', 1), faction));
            //newPlayer.king = new Figure.King(board, board.getCellState('g', 1), faction);
            for (int i = 1; i < 10; i++){
                newPlayer.aliveFigures.add(new
                        Figure.Pawn(board, board.getCellState(HexagonalMap.validLetters[i], 5 - Math.abs(i - 5)), faction));
            }
        }
        for(Figure figure : newPlayer.aliveFigures){
            board.setFigure(figure.getPosition(), figure);
        }
        return newPlayer;
    }

    /**
     * @param col - column of cell was clicked
     * @param row - row of cell was clicked
     * @return constant code of what happened in game
     */
    public GameResponds cellClicked(int col, int row){
        if (selectedFigure == null){
            Figure clickedFigure = board.getCellState((char)col, row).getFigure();
            if (clickedFigure == null || clickedFigure.getFaction() != turnQueue[activPlayerIndex].faction){
                return GameResponds.NO_ANSWER;
            }
            selectedFigure = clickedFigure;
            return GameResponds.FIGURE_SELECTED;
        }
        HexagonalMap.Position selectedCell = board.getCellState((char)col, row);
        if (selectedCell.getFigure() != null &&
                selectedCell.getFigure().getFaction() == turnQueue[activPlayerIndex].faction){
            if (selectedCell.getFigure().equals(selectedFigure)){
                selectedFigure = null;
                return GameResponds.FIGURE_DESELECTED;
            }
            selectedFigure = selectedCell.getFigure();
            return GameResponds.FIGURE_RESELECTED;
        }
        if (selectedFigure.getMoveOptions().contains(selectedCell)) {
            if (selectedCell.getFigure() != null){
                if (selectedCell.getFigure().equals(turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures.get(0))){
                    if (turnQueue[activPlayerIndex].faction == Factions.BLACK){
                        return GameResponds.BLACK_WIN;
                    }
                    return GameResponds.WHITE_WIN;
                }
                turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures.remove(selectedCell.getFigure());
            }
            selectedFigure.moveTo(selectedCell);
            ((Figure.King)(turnQueue[activPlayerIndex].aliveFigures.get(0))).updateAvailablePositions(turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures);
            giveTurnFurther();
            ((Figure.King)(turnQueue[activPlayerIndex].aliveFigures.get(0))).updateAvailablePositions(turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures);
            return GameResponds.TURN_DONE;
        }
        return GameResponds.NO_ANSWER;
    }

    /**
     * @return cells where could move selected figure
     */
    public List<HexagonalMap.Position> getSelectedMoves(){
        if (selectedFigure == null){
            return null;
        }
        return selectedFigure.getMoveOptions();
    }

    /**
     * @return faction of player doing turn
     */
    public Factions getActivPlayerFaction(){
        return turnQueue[activPlayerIndex].faction;
    }
}
