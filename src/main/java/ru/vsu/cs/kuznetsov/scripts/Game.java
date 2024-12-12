package ru.vsu.cs.kuznetsov.scripts;

import java.util.ArrayList;
import java.util.Arrays;
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

    public BiFunction<Factions, HexagonalMap.Position, Figure>[] figureIneters = new BiFunction[]{
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
//            newPlayer.aliveFigures.add(new Figure.King(board, board.getCellState('g', 10), faction));
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
//            newPlayer.aliveFigures.add(new Figure.King(board, board.getCellState('g', 1), faction));
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
    public GameResponds cellClicked(int col, int row){
        if (isPawnChangingRequired){
            return GameResponds.PAWN_CHANGE_REQUIRED;
        }
        //figure selection
        if (selectedFigure == null){
            Figure clickedFigure = board.getCellState((char)col, row).getFigure();
            if (clickedFigure == null || clickedFigure.getFaction() != activPlayer.faction){
                return GameResponds.NO_ANSWER;
            }
            selectedFigure = clickedFigure;
            return GameResponds.FIGURE_SELECTED;
        }
        HexagonalMap.Position selectedCell = board.getCellState((char)col, row);
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
        if (selectedFigure.getMoveOptions().contains(selectedCell)) {
            //pawn moved to enemy back row
            if (selectedFigure.getClass() == Figure.Pawn.class &&
                    (11 - Math.abs(- 5 + ((col < 'j')?(col - 'a'):(col - 'a' - 1))) == row) || row == 1){
                isPawnChangingRequired = true;
                selectedFigure.moveTo(selectedCell);
                return GameResponds.PAWN_CHANGE_REQUIRED;
            }
            //remove enemy figure if attacked
            if (selectedCell.getFigure() != null){
                if (selectedCell.getFigure().equals(turnQueue[turnQueue.length - activPlayerIndex - 1].king)){
                    if (activPlayer.faction == Factions.BLACK){
                        return GameResponds.BLACK_WIN;
                    }
                    return GameResponds.WHITE_WIN;
                }
                turnQueue[turnQueue.length - activPlayerIndex - 1].aliveFigures.remove(selectedCell.getFigure());
            }
            //step doing, turn passing, king check
            selectedFigure.moveTo(selectedCell);
            for (int i = 0; i < 2; i++) {
                List <Figure> attackers = new ArrayList<>(turnQueue[(i + 1) % 2].aliveFigures);
                attackers.add(turnQueue[(i + 1) % 2].king);
                turnQueue[i].king.updateAvailablePositions(attackers);
            }
            giveTurnFurther();
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
        if (oldFigure.getClass() == Figure.Pawn.class){
            selectedFigure = null;
            isPawnChangingRequired = false;
        }
    }

    public Figure getSelectedFigure(){
        return selectedFigure;
    }

    /**
     * @return figure placement next scope:
     * -name of activ player faction: figure_code figure_col figure_row; next figure
     * -other player same scope from new line
     */
    public String getGameConfiguration(){
        StringBuilder result = new StringBuilder();
        for (int i = activPlayerIndex; i <= activPlayerIndex + 1; i++){
            Player p = turnQueue[i % turnQueue.length];
            result.append(Figure.getFactionName(p.faction)).append(": ");
            for (Figure fig: p.aliveFigures){
                result.append(fig.getNotationCode()).append(" ").append(fig.getPosition().getCol()).append(" ").
                        append(fig.getPosition().getRow()).append("; ");
            }
            if (i == activPlayerIndex)
                result.append("\n");
        }
        return result.toString();
    }

    /**
     * If this function get correct configuration reinit fields of this game according to configuration,
     * else throws Exception.
     * @param configuration  String with two lines:
     *                      activ player configuration
     *                      next player configuration
     * Every player configuration must be given next scope: faction_name: figure_code figure_column figure_row;
     *                       next_figure_code next_figure_column next_figure_row; ... ;
     */
    public void readBoardConfigurationToThis(String configuration){
        String[] playersConfigs = configuration.split("\n+");
        HexagonalMap newBoard = new HexagonalMap();
        Player[] newTurnQueue = new Player[2];
        if (playersConfigs.length != 2){
            throw new RuntimeException("Too much or too little players in configuration for this game version. " +
                    "If 2 was passed, make sure that there is no only players separation new line.");
        }
        for (int i = 0; i < 2; i++){
            String [] playerConfig = playersConfigs[i].split(": +");
            Factions curFaction = Figure.getFactionByName(playerConfig[0]);
            if (curFaction == Factions.NO_FIGURE){
                throw new RuntimeException("Unknown player faction.");
            }
            newTurnQueue[i] = new Player(curFaction);
            for (String figureConfig : playersConfigs[1].split("; +")){
                String[] figureStats = figureConfig.split(" +");
                HexagonalMap.Position newFigurePosition = newBoard.getCellState((char) Integer.parseInt(figureStats[1]),
                        Integer.parseInt(figureStats[2]));
                if (newFigurePosition.getFigure() != null){
                    throw new RuntimeException("Two or more figures placed to one position");
                }
                Figure newFigure = (figureIneters[Arrays.binarySearch(Figure.getNotationCodes(), figureStats[0])]).apply(curFaction, newFigurePosition);
                if (newFigure.getClass() == Figure.King.class){
                    if (newTurnQueue[i].king != null){
                        throw new RuntimeException("Player can not have to kings.");
                    }
                    newTurnQueue[i].king = (Figure.King) newFigure;
                } else {
                    newTurnQueue[i].aliveFigures.add(newFigure);
                }
                newBoard.setFigure(newFigurePosition, newFigure);
            }
            if (newTurnQueue[i].king == null){
                throw new RuntimeException("Each player must have king.");
            }
        }
        this.turnQueue = newTurnQueue;
        this.board = newBoard;
        this.activPlayerIndex = 0;
        this.activPlayer = this.turnQueue[0];
        this.selectedFigure = null;
        this.isPawnChangingRequired = false;
        this.algebraicNotation = new ArrayList<String>();
    }

    public String getAlgebraicNotation(){
        StringBuilder res = new StringBuilder();
        for (String line : algebraicNotation){
            res.append(line).append('\n');
        }
        return res.toString();
    }
}
