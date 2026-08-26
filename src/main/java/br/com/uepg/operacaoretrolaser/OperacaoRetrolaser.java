package br.com.uepg.operacaoretrolaser;

import javax.swing.JFrame;

public class OperacaoRetrolaser {

   static void main(String[] args) {
      JFrame frame = new JFrame("Operação RetroLaser - Defender 2D");
      GamePanel panel = new GamePanel();

      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setResizable(false);
      frame.add(panel);
      panel.applyVideoSettings(frame);
      panel.startGame();
   }
}