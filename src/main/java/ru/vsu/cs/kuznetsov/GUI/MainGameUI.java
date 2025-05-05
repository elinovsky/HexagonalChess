package ru.vsu.cs.kuznetsov.GUI;

import ru.vsu.cs.kuznetsov.scripts.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.List;

public class MainGameUI extends JFrame{
    private JPanel mainPanel;
    private JPanel graphicPanel;
    private JLabel messageLable;
    private JScrollPane gameScene;
    private JPanel pawnChangingDialog;
    private JButton submitButton;
    private JPanel optionsPanel;

    private Game game;
    private Color[] cellColors = new Color[]{new Color(0xe2e2e2),
            new Color(0xa0a0a0), new Color(0x565656)};
    private int cellWidth = 60;
    private int cellHeight = 50;
    private boolean pawnChanging = false;
    pawnChangingChoose pChCh;

    private int chooseFinalizer(int option){
        game.replace(game.getSelectedFigure(),
                game.figureIneters[option + 1].apply(game.getActivPlayerFaction(),
                        game.getSelectedFigure().getPosition()));
        messageLable.setText(getTurnTip(game.getActivPlayerFaction()));
        pChCh.dispose();
        paintBoard();
        return 0;
    }

    public MainGameUI(){
        game = new Game();
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, "gameScene");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 650);
        setContentPane(mainPanel);
        setVisible(true);
        setTitle("Шахматы Глинского. Выбирать фигуры и ходить ими нажатием мыши!");
        //Mouse listener, reacting only on click, defining which cell was pressed.
        //Calls UI reaction function on game event.
        graphicPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double rectTCY = (graphicPanel.getHeight() - 11 * cellHeight) / 2.0;
                double rectTCX = graphicPanel.getWidth() / 2.0 - 3.75 * cellWidth;
                int subrectTopY = (int)((e.getY() - rectTCY) / cellHeight);
                int subrectLeftX = (int)((e.getX() - rectTCX) / (1.5 * cellWidth));
                int col = 1 + 2 * subrectLeftX;
                int row = (10 - subrectTopY) - Math.abs(col - 5) / 2;
                if (subrectTopY < 0 || subrectTopY > 10 || subrectLeftX < 0 || subrectLeftX > 4) {
                    return;
                }
                if ((e.getX() - rectTCX) % (1.5 * cellWidth) <
                        (cellWidth / 4.0 + 0.6 * Math.abs((e.getY() - rectTCY) % cellHeight - cellHeight / 2.0))){
                    if (col > 5 && (e.getY() - rectTCY) % cellHeight < (double) cellHeight / 2){
                        row += 1;
                    } else if (col <= 5 && (e.getY() - rectTCY) % cellHeight > (double) cellHeight / 2) {
                        row -= 1;
                    }
                    col -= 1;
                } else if ((e.getX() - rectTCX) % (1.5 * cellWidth) >
                        (1.25 * cellWidth - 0.6 * Math.abs((e.getY() - rectTCY) % cellHeight - cellHeight / 2.0))) {
                    if (col >= 5 && (e.getY() - rectTCY) % cellHeight > (double) cellHeight / 2){
                        row -= 1;
                    } else if (col < 5 && (e.getY() - rectTCY) % cellHeight < (double) cellHeight / 2) {
                        row += 1;
                    }
                    col += 1;
                }
                if (game.getBoard().isCoordinatesValid(HexagonalMap.validLetters[col], row + 1)) {
                    gameListener(game.cellClicked(HexagonalMap.validLetters[col], row + 1));
                }
            }
        });
    }

    public void paint(Graphics g){
        super.paint(g);
        paintBoard();
        showMoves(game.getSelectedMoves());
    }

    /**
     * Wipe graphicsPlane and paint game board on it
     */
    public void paintBoard(){
        Graphics g = graphicPanel.getGraphics();
        g.clearRect(0, 0, graphicPanel.getWidth(), graphicPanel.getHeight());
        int cellX = (int)(graphicPanel.getWidth() / 2 - cellWidth * 4.25);
        int cellY;
        int colorInd;
        Iterator<HexagonalMap.Position> boardCells =  game.getBoard().getBoardState().iterator();
        for(int col = 0; col < 11; col ++){
            colorInd = ((col < 6) ? (col) : (10 - col)) % cellColors.length;
            cellY = (int)(graphicPanel.getHeight() / 2 - cellHeight * (5.5 - Math.abs(col - 5) / 2.0));
            for (int row = 0; row < 11 - Math.abs(col - 5); row ++){
                g.setColor(cellColors[colorInd]);
                Polygon polygon = new Polygon(
                        new int[]{cellX, cellX + cellWidth / 4, cellX + 3 * cellWidth / 4, cellX + cellWidth,
                                cellX + 3 * cellWidth / 4, cellX + cellWidth / 4, cellX},
                        new int[]{cellY + cellHeight / 2, cellY, cellY, cellY + cellHeight / 2,
                                cellY + cellHeight, cellY + cellHeight}, 6);
                g.fillPolygon(polygon);
                g.setColor(Color.BLACK);
                g.drawPolygon(polygon);
                Figure cellStander = boardCells.next().getFigure();
                if (cellStander != null) {
                    try {
                        Image img = FileContactor.readImage(cellStander.getImageName());
                        g.drawImage(img, cellX + (cellWidth - 40)/2, cellY - (cellHeight - 50)/2,
                                40, 40, null);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null,
                                "Не удалось найти изображение " + cellStander.getImageName() + ".");
                    }
                }
                colorInd = (colorInd + 1) % cellColors.length;
                cellY += cellHeight;
            }
            cellX += 3 * cellWidth / 4;
        }
    }

    /**
     * UI reaction on game event
     * @param respond - code of game event
     */
    public void gameListener(GameResponds respond){
        switch (respond){
            case FIGURE_DESELECTED->paintBoard();
            case FIGURE_SELECTED->{
                showMoves(game.getSelectedMoves());
            }
            case FIGURE_RESELECTED->{
                paintBoard();
                showMoves(game.getSelectedMoves());
            }
            case BLACK_WIN->{
                messageLable.setText("Победа игрока за чёрные фигуры!");
                removeBoardListeners();
                paintBoard();
            }
            case WHITE_WIN->{
                messageLable.setText("Победа игрока за белые фигуры!");
                removeBoardListeners();
                paintBoard();
            }
            case STALEMATE->{
                messageLable.setText("Пат!");
                removeBoardListeners();
                paintBoard();
            }
            case TURN_DONE->{
                messageLable.setText(getTurnTip(game.getActivPlayerFaction()));
                paintBoard();
            }
            case SELF_ENDANGERING->{
                messageLable.setText("Этот ход подвергнет короля опасности. " +
                        getTurnTip(game.getActivPlayerFaction()));
                paintBoard();
            }
            case PAWN_CHANGE_REQUIRED->{
                showPawnChangingDialog(game.getActivPlayerFaction());
                paintBoard();
            }
        }
    }

    private String getTurnTip(Factions faction){
        return switch (faction) {
            case WHITE -> "Ход белых";
            case BLACK -> "Ход чёрных";
            default -> "Как сюда попало это значение?";
        };
    }

    private void removeBoardListeners(){
        for(MouseListener aL : graphicPanel.getListeners(MouseListener.class)){
            graphicPanel.removeMouseListener(aL);
        }
    }

    public void showMoves(List<HexagonalMap.Position> moves){
        if (moves == null){
            return;
        }
        String toGetIndex = new String(HexagonalMap.validLetters);
        int rectBottomLeftX = (int)(graphicPanel.getWidth() / 2.0 - 3.75 * cellWidth);
        int rectBottomLeftY = (int)((graphicPanel.getHeight() + 11 * cellHeight) / 2.0);
        Graphics g = graphicPanel.getGraphics();
        g.setColor(new Color(0x61898989, true));
        HexagonalMap board = game.getBoard();
        for (HexagonalMap.Position p : moves){
            int colInd = toGetIndex.indexOf(p.getCol());
            if (!board.isCoordinatesValid(p)) {
                continue;
            }
            int posX = (int) (rectBottomLeftX + cellWidth * (colInd * 0.75 - 0.25));
            int posY = (int) (rectBottomLeftY + cellHeight * (0.25 - Math.abs(colInd - 5) / 2.0 - p.getRow()));
            if (p.getFigure() ==null) {
                g.fillOval(posX, posY, cellWidth / 2, cellHeight / 2);
            } else {
                try {
                    Image img = FileContactor.readImage("targetMark.png");
                    g.drawImage(img, posX - cellWidth / 4, posY - cellHeight / 4,
                            cellWidth, cellHeight, null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Не удалось отрыть изображение targetMark.png.\n" + ex.getMessage());
                }
            }
        }
    }

    public void showPawnChangingDialog(Factions faction){
        pChCh = new pawnChangingChoose(this, Figure.getFactionName(faction), this::chooseFinalizer);
    }
}
