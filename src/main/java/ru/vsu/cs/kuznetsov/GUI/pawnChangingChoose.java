package ru.vsu.cs.kuznetsov.GUI;

import ru.vsu.cs.kuznetsov.scripts.FileContactor;

import javax.swing.*;
import java.awt.*;

public class pawnChangingChoose extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private JPanel optionsPanel;
    private String factionName;
    private String[] figuresNames = new String[]{"Knight", "Bishop", "Rook", "Queen"};

    public pawnChangingChoose(Component parentComponent, String factionName) {
        this.factionName = factionName;
        optionsPanel.setMaximumSize(new Dimension(61 * 4, 61));
        optionsPanel.setMinimumSize(new Dimension(61 * 4, 61));
        setContentPane(contentPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModal(true);
        getRootPane().setDefaultButton(submitButton);
        setTitle("Замена пешки");
        setVisible(true);
        pack();
    }

    public void paint(Graphics g){
        super.paint(g);
        Graphics g2 = optionsPanel.getGraphics();
        for (int i = 0; i < figuresNames.length; i++){
            String str = factionName + figuresNames[i] + ".png";
            try {
                Image img = FileContactor.readImage(str);
                g.drawImage(img, 61 * i, 0, 61, 61, null);
            } catch (Exception ex){
                JOptionPane.showMessageDialog(null,
                        "Не удалось открыть изображение " + str + " \n" + ex.getMessage());
            }
        }
    }
}
