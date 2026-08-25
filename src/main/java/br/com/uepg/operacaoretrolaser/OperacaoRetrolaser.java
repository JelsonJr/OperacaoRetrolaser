package br.com.uepg.operacaoretrolaser;

import javax.swing.JFrame;

public class OperacaoRetrolaser {

     void main() {
        JFrame frame = new JFrame("Operação RetroLaser - Defender 2D");
        GamePanel panel = new GamePanel();
        
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        panel.startGame();
    }
}