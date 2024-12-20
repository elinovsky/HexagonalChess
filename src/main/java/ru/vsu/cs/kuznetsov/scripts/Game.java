package ru.vsu.cs.kuznetsov.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Game {
    private static class Player{
        List<Figure> aliveFigures;
        Factions faction;
        Figure.King king;

        Player(Factions faction){
            this.faction = faction;
            aliveFigures = new ArrayList<>();
            king = null;
        }
    }

    HexagonalMap board;
    Player[] turnQueue;
    int activPlayerIndex;
    Player activPlayer;
    Figure selectedFigure = null;
    private boolean isPawnChangingRequired = false;
    private List <String> algebraicNotation;
    public static final Factions[] gameFactions = new Factions[]{Factions.WHITE, Factions.BLACK};

    public final BiFunction<Factions, HexagonalMap.Position, Figure>[] figureIneters = new BiFunction[]{
            (f, p)->{return new Figure.Pawn(this.board, (HexagonalMap.Position) p, (Factions) f);},
            (f, p)->{return new Figure.Knight(this.board, (HexagonalMap.Position) p, (Factions) f);},
            (f, p)->{return new Figure.Bishop(this.board, (HexagonalMap.Position) p, (Factions) f);},
            (f, p)->{return new Figure.Rook(this.board, (HexagonalMap.Position) p, (Factions) f);},
            (f, p)->{return new Figure.Queen(this.board, (HexagonalMap.Position) p, (Factions) f);}
    };

    public Game(){
        algebraicNotation = new ArrayList<>();
        board = new HexagonalMap();
        activPlayerIndex = 0;
        turnQueue = new Player[2];
        for (int i = 0; i < turnQueue.length; i++){
            turnQueue[i] = initPlayer(gameFactions[i]);
        }
        activPlayer = turnQueue[activPlayerIndex];
        updateKingsMoves();
    }

    private void updateKingsMoves(){
        for (int i = 0; i < turnQueue.length; i++){
            List<Figure> figures = new ArrayList<>(turnQueue[(i + 1) % turnQueue.length].aliveFigures);
            figures.add(turnQueue[(i + 1) % turnQueue.length].king);
            turnQueue[i].king.updateAvailablePositions(figures);
        }
    }

    /**
     * Give turn to next player.
     */
    public void giveTurnFurther(){
        activPlayerIndex = (activPlayerIndex + 1) % turnQueue.length;
        activPlayer = turnQueue[activPlayerIndex];
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

    /**
     * @param col - column of cell was clicked
     * @param row - row of cell was clicked
     * @return constant code of what happened in game
     */
    public GameResponds cellClicked(char col, int row){
        if (isPawnChangingRequired){
            return GameResponds.PAWN_CHANGE_REQUIRED;
        }
        //figure selection
        if (selectedFigure == null){
            Figure clickedFigure = board.getCellState(col, row).getFigure();
            if (clickedFigure == null || clickedFigure.getFaction() != activPlayer.faction){
                return GameResponds.NO_ANSWER;
            }
            selectedFigure = clickedFigure;
            return GameResponds.FIGURE_SELECTED;
        }
        HexagonalMap.Position selectedCell = board.getCellState(col, row);
        Figure clickedFigure = selectedCell.getFigure();
        //checking if clicked figure is friendly
        if (clickedFigure != null &&
                selectedCell.getFigure().getFaction() == activPlayer.faction){
            if (clickedFigure.equals(selectedFigure)){
                selectedFigure = null;
                return GameResponds.FIGURE_DESELECTED;
            }
            selectedFigure = clickedFigure;
            return GameResponds.FIGURE_RESELECTED;
        }
        //can move on clicked cell
        Player opposite = turnQueue[turnQueue.length - activPlayerIndex - 1];
        if (selectedFigure.getMoveOptions().contains(selectedCell)) {
            if(!moveSelected(col, row)){
                return GameResponds.SELF_ENDANGERING;
            }
            if (selectedFigure.canBeChanged()){
                isPawnChangingRequired = true;
                return GameResponds.PAWN_CHANGE_REQUIRED;
            }
            //turn passing, king check
            updateKingsMoves();
            List <Figure> allActiveFigures = new ArrayList<>(activPlayer.aliveFigures);
            allActiveFigures.add(activPlayer.king);
            if (opposite.king.isUnderMate(allActiveFigures, opposite.aliveFigures) ||
                    opposite.king.getMoveOptions().isEmpty() && opposite.aliveFigures.isEmpty()){
                return getLoseRespond(opposite.faction);
            }
            if (activPlayer.aliveFigures.isEmpty() && opposite.aliveFigures.isEmpty())
                return GameResponds.STALEMATE;
            giveTurnFurther();
            return GameResponds.TURN_DONE;
        }
        return GameResponds.NO_ANSWER;
    }

    /**
     * Moves selected figure to position (targetCol, targetRow) if it doesn't endanger active player's king.
     * WARNING: make sure that selected figure not null!
     * @return true if selected figure was moved, false else.
     */
    private boolean moveSelected(char targetCol, int targetRow){
        Figure attacked = board.getCellState(targetCol, targetRow).getFigure();
        HexagonalMap.Position leaving = selectedFigure.getPosition();
        selectedFigure.moveTo(board.getCellState(targetCol, targetRow));
        if (attacked != null){
            turnQueue[(activPlayerIndex + 1) % 2].aliveFigures.remove(attacked);
        }
        if (Figure.isPositionChecked(activPlayer.king.position, turnQueue[(activPlayerIndex + 1) % 2].aliveFigures)){
            selectedFigure.moveTo(leaving);
            if (attacked != null)
                addFigure(attacked);
            return false;
        }
        return true;
    }

    private GameResponds getLoseRespond(Factions faction){
        if (faction == Factions.WHITE){
            return GameResponds.BLACK_WIN;
        }
        return GameResponds.WHITE_WIN;
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

    /**
     * @param figure figure needed to place on board and add under player control, target cell must be empty
     */
    public void addFigure(Figure figure){
        if (board.getCellState(figure.getPosition()).getFigure() != null){
            throw new RuntimeException("New figure cannot be placed to occupied cell.");
        }
        for (int i = 0; i < turnQueue.length; i++) {
            if (turnQueue[i].faction == figure.faction){
                board.setFigure(figure.getPosition(), figure);
                turnQueue[i].aliveFigures.add(figure);
                break;
            }
        }
    }

    /**
     * @param oldFigure
     * @param newFigure
     * Replace figure on bord. Factions of new figure and old figure must be same.
     */
    public void replace(Figure oldFigure, Figure newFigure){
        int oldIndex = turnQueue[activPlayerIndex].aliveFigures.indexOf(oldFigure);
        if (oldIndex < 0){
            throw new RuntimeException("No old figure as given on board.");
        }
        turnQueue[activPlayerIndex].aliveFigures.set(oldIndex, newFigure);
        board.setFigure(oldFigure.getPosition(), newFigure);
        if (oldFigure.getClass() == Figure.Pawn.class && isPawnChangingRequired){
            selectedFigure = null;
            isPawnChangingRequired = false;
            giveTurnFurther();
        }
    }

    public Figure getSelectedFigure(){
        return selectedFigure;
    }

    //TODO: rewrite
//    /**
//     * @return string with player configuration in one line separated with space. Each player will be writing next scope
//     * player <player_faction> <figure_code> <figure_column> <figure_row> <next_figure_code> <next_figure_column> <next_figure_row> ...
//     */
//    public String getGameConfiguration(){
//        StringBuilder result = new StringBuilder();
//        for (int i = 0; i < 2; i++){
//            result.append("player ").append(Figure.getFactionName(turnQueue[i].faction)).append(" ");
//            result.append(turnQueue[i].king.getNotationCode()).append(" ").
//                    append(turnQueue[i].king.getPosition().toString()).append(" ");
//            for (Figure figure : turnQueue[i].aliveFigures){
//                result.append(figure.getNotationCode()).append(" ").
//                        append(figure.getPosition().toString()).append(" ");
//            }
//        }
//        return result.toString();
//    }

    //TODO: rewrite
//    public void readBoardConfigurationToThis(String configuration){
//        return;
//        if (configuration.isBlank()){
//            throw new RuntimeException("Configuration can not be empty or blank.");
//        }
//        Player[] newTurnQueue = new Player[2];
//        HexagonalMap newBoard = new HexagonalMap();
//        int playerIndex = 0;
//        String[] configParts = configuration.split(" *(\n|/|\\|:|;)+ *");
//        if (!configParts[0].equals("player")){
//            throw new RuntimeException("Configuration must start with player.");
//        }
//        int partInd = 1;
//        while (partInd < configParts.length){
//            String configPart = configParts[partInd];
//            Factions f = Figure.getFactionByName(configPart);
//            if (f == Factions.NO_FIGURE){
//                throw new RuntimeException("Unknown faction: " + configPart + ".");
//            }
//            Player newPlayer = new Player(f);
//            partInd++;
//            while (partInd < configParts.length){
//                if (configParts.length - partInd < 3){
//                    throw new RuntimeException("Can not continue reading configuration.\n " +
//                            "Make sure that each player has at least king and each figure has three parameters");
//                }
//                String figureCode = configParts[partInd];
//                String col = configParts[partInd + 1];
//                String row = configParts[partInd + 2];
//                if (figureCode.equals("player")){
//                    if (turnQueue[playerIndex].faction == Figure.getFactionByName(col)){
//                        throw new RuntimeException("")
//                    }
//                    partInd++;
//                    playerIndex++;
//                    break;
//                }
//                int figureConstrInd = Arrays.binarySearch(Figure.getNotationCodes(), figureCode);
//                if (figureConstrInd < 0){
//                    throw new RuntimeException("Unknown figure code " + configPart + ".");
//                }
//                if (configParts.length - partInd < 3){
//                    throw new RuntimeException("Cannot read another figure.");
//                }
//                Figure newFigure = figureIneters[figureConstrInd].apply(f, newBoard.getCellState());
//            }
//        }
//        this.turnQueue = newTurnQueue;
//        this.board = newBoard;
//        this.activPlayerIndex = 0;
//        this.activPlayer = this.turnQueue[0];
//        this.selectedFigure = null;
//        this.isPawnChangingRequired = false;
//        this.algebraicNotation = new ArrayList<String>();
//    }

//    public String getAlgebraicNotation(){
//        StringBuilder res = new StringBuilder();
//        for (String line : algebraicNotation){
//            res.append(line).append('\n');
//        }
//        return res.toString();
//    }
}
