package ru.vsu.cs.kuznetsov.GUI;

import ru.vsu.cs.kuznetsov.scripts.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

public class MainGameUI extends JFrame{
    private JPanel mainPanel;
    private JPanel graphicPanel;
    private JLabel messageLable;

    private Game game;
    private Color[] cellColors = new Color[]{new Color(0xe2e2e2),
            new Color(0xa0a0a0), new Color(0x565656)};
    private int cellWidth = 60;
    private int cellHeight = 50;

    public MainGameUI(){
        game = new Game();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 700);
        setContentPane(mainPanel);
        setVisible(true);
        setTitle("Шахматы Глинского");
        graphicPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double rectTCY = (graphicPanel.getHeight() - 11 * cellHeight) / 2.0;
                double rectTCX = graphicPanel.getWidth() / 2.0 - 3.75 * cellWidth;
                int cellYHalfNum = (int)((e.getY() - rectTCY) / cellHeight);
                int cellXHalfNum = (int)((e.getX() - rectTCX) / (1.5 * cellWidth));
                int col = 1 + 2 * cellXHalfNum;
                int row = (10 - cellYHalfNum) - Math.abs(col - 5) / 2;
                if (cellYHalfNum < 0 || cellYHalfNum > 10 || cellXHalfNum < 0 || cellXHalfNum > 4) {
                    return;
                }
                Graphics g = graphicPanel.getGraphics();
                g.setColor(Color.cyan);
                g.drawRect((int)rectTCX, (int)rectTCY, (int)(7.5 * cellWidth), 11 * cellHeight);
                g.drawRect((int)(rectTCX + cellXHalfNum * (1.5 * cellWidth)),
                        (int)rectTCY + cellYHalfNum * cellHeight, (int)(1.5 * cellWidth), cellHeight);
                if ((e.getX() - rectTCX) % (1.5 * cellWidth) <
                        (cellWidth / 4.0 + 0.6 * Math.abs((e.getY() - rectTCY) % cellHeight - cellHeight / 2.0))){
                    if (col > 5 && (e.getY() - rectTCY) % cellHeight < (double) cellHeight / 2){
                        row += 1;
                    } else if (col < 5 && (e.getY() - rectTCY) % cellHeight > (double) cellHeight / 2) {
                        row -= 1;
                    }
                    col -= 1;
                } else if ((e.getX() - rectTCX) % (1.5 * cellWidth) >
                        (1.25 * cellWidth - 0.6 * Math.abs((e.getY() - rectTCY) % cellHeight - cellHeight / 2.0))) {
                    if (col > 5 && (e.getY() - rectTCY) % cellHeight > (double) cellHeight / 2){
                        row -= 1;
                    } else if (col < 5 && (e.getY() - rectTCY) % cellHeight < (double) cellHeight / 2) {
                        row += 1;
                    }
                    col += 1;
                }
                System.out.println(col + " " + row);
                if (game.getBoard().isCoordinatesValid(HexagonalMap.validLetters[col], row + 1)) {
                    gameListener(game.cellClicked(HexagonalMap.validLetters[col], row + 1));
                }
            }
        });
    }

    public void paint(Graphics g){
        super.paint(g);
        paintBoard();
    }

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
                        Image img = FileContactor.readImageForFigure(cellStander);
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

    public void gameListener(GameResponds respond){
        System.out.println(respond);
        switch (respond){
            case FIGURE_DESELECTED -> paintBoard();
            case FIGURE_SELECTED -> showMoves(game.getSelectedMoves());
            case BLACK_WIN -> {
                messageLable.setText("Победа игрока за чёрные фигуры!");
                for(MouseListener aL : graphicPanel.getListeners(MouseListener.class)){
                    graphicPanel.removeMouseListener(aL);
                }
                paintBoard();
            }
            case WHITE_WIN -> {
                messageLable.setText("Победа игрока за белые фигуры!");
                for(MouseListener aL : graphicPanel.getListeners(MouseListener.class)){
                    graphicPanel.removeMouseListener(aL);
                }
                paintBoard();
            }
            case TURN_DONE -> {
                paintBoard();
                if(game.getActivPlayerFaction() == Factions.BLACK){
                    messageLable.setText("Ход чёрных");
                }else{
                    messageLable.setText("Ход белых");
                }
            }
        }
    }

    public void showMoves(List<HexagonalMap.Position> moves){

    }
}
