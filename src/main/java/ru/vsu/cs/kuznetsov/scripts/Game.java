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

        Figure.King king;
    }

    HexagonalMap board;
    Player[] turnQueue;
    int activPlayerIndex;
    int selectedFigureIndex = -1;
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

    public void giveTurnFurther(){
        activPlayerIndex = (activPlayerIndex + 1) % turnQueue.length;
    }

    public HexagonalMap getBoard(){
        return board;
    }

    private Player initPlayer(Factions faction){
        Player newPlayer = new Player(faction);
        if (faction == Factions.BLACK){
            for (int i = 11; i >= 9; i--) {
                newPlayer.aliveFigures.add(new Figure.Bishop(board, board.getCellState('f', i), faction));
            }
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('c', 8), faction));
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('i', 8), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('d', 9), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('h', 9), faction));
            newPlayer.aliveFigures.add(new Figure.Queen(board, board.getCellState('e', 10), faction));
            newPlayer.king = new Figure.King(board, board.getCellState('g', 10), faction);
            for (int i = 1; i < 10; i++){
                newPlayer.aliveFigures.add(new
                        Figure.Pawn(board, board.getCellState(HexagonalMap.validLetters[i], 7), faction));
            }
        } else {
            for (int i = 3; i >= 1; i--) {
                newPlayer.aliveFigures.add(new Figure.Bishop(board, board.getCellState('f', i), faction));
            }
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('c', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Rook(board, board.getCellState('i', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('d', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Knight(board, board.getCellState('h', 1), faction));
            newPlayer.aliveFigures.add(new Figure.Queen(board, board.getCellState('e', 1), faction));
            newPlayer.king = new Figure.King(board, board.getCellState('g', 1), faction);
            for (int i = 1; i < 10; i++){
                newPlayer.aliveFigures.add(new
                        Figure.Pawn(board, board.getCellState(HexagonalMap.validLetters[i], 5 - Math.abs(i - 5)), faction));
            }
        }
        for(Figure figure : newPlayer.aliveFigures){
            board.setFigure(figure.getPosition(), figure);
        }
        board.setFigure(newPlayer.king.position, newPlayer.king);
        return newPlayer;
    }

    public GameResponds cellClicked(int col, int row){
        if (selectedFigureIndex == -1){
            Figure clickedFigure = board.getCellState((char)col, row).getFigure();
            if (selectedFigure == null || selectedFigure.getFaction() != turnQueue[activPlayerIndex].faction){
                return GameResponds.NO_ANSWER;
            }
            selectedFigure = clickedFigure;
            return GameResponds.FIGURE_SELECTED;
        }
        HexagonalMap.Position selectedCell = board.getCellState((char)col, row);
        if (selectedCell.getFigure() != null &&
                selectedCell.getFigure().getFaction() == turnQueue[activPlayerIndex].faction){
            if (selectedCell.getFigure().equals(selectedFigure)){
                return GameResponds.FIGURE_DESELECTED;
            }
            return GameResponds.FIGURE_RESELECTED;
        }
        int ind = selectedFigure.getMoveOptions().indexOf(selectedCell);
        if (ind >= 0) {
            HexagonalMap.Position movingTo = selectedFigure.getMoveOptions().get(ind);
            selectedFigure.moveTo(selectedCell);
            if (selectedCell.getFigure() != null){
                if (selectedCell.getFigure() == turnQueue[turnQueue.length - activPlayerIndex - 1].king){
                    if (turnQueue[activPlayerIndex].faction == Factions.BLACK){
                        return GameResponds.WHITE_WIN;
                    }
                    return GameResponds.BLACK_WIN;
                }
                turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures.remove(movingTo.getFigure());
            }
            giveTurnFurther();
            turnQueue[activPlayerIndex].king.updateAvailablePositions(turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures);
            return GameResponds.TURN_DONE;
        }
        return GameResponds.NO_ANSWER;
    }

    public List<HexagonalMap.Position> getSelectedMoves(){
        if (selectedFigureIndex == -1 || selectedFigure == null){
            return null;
        }
        return selectedFigure.getMoveOptions();
    }

    public Factions getActivPlayerFaction(){
        return turnQueue[activPlayerIndex].faction;
    }
}
