package ru.vsu.cs.kuznetsov.GUI;

import ru.vsu.cs.kuznetsov.scripts.FileContactor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

public class pawnChangingChoose extends JDialog {
    private JPanel contentPane;
    private JButton submitButton;
    private JPanel optionsPanel;
    private String factionName;
    private String[] figuresNames = new String[]{"Knight", "Bishop", "Rook", "Queen"};
    int option = -1;
    Function<Integer, Integer> finalizer;

    public pawnChangingChoose(Component parentComponent, String factionName, Function<Integer, Integer> finalizer) {
        this.factionName = factionName;
        optionsPanel.setMaximumSize(new Dimension(61 * 4, 61));
        optionsPanel.setMinimumSize(new Dimension(61 * 4, 61));
        setSize(4*61+10, 61 + 35*2);
        setContentPane(contentPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.finalizer = finalizer;
//        setModal(true);
        parentComponent.disable();
        setTitle("Замена пешки.");
        setVisible(true);
        pack();

        optionsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                option = e.getX() / 61;
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (option < 0){
                    return;
                }
                parentComponent.enable();
                finalizer.apply(option);
            }
        });
    }

    public void paint(Graphics g){
        super.paint(g);
        Graphics g2 = optionsPanel.getGraphics();
        for (int i = 0; i < figuresNames.length; i++){
            String str = factionName + figuresNames[i] + ".png";
            try {
                Image img = FileContactor.readImage(str);
                g2.drawImage(img, 61 * i, 0, 61, 61, null);
            } catch (Exception ex){
                JOptionPane.showMessageDialog(null,
                        "Не удалось открыть изображение " + str + " \n" + ex.getMessage());
            }
        }
    }
}
