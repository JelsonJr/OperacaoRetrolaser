package br.com.uepg.operacaoretrolaser.states;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface GameState {
    void update();
    void draw(Graphics2D g2d);
    void mouseMoved(MouseEvent e);
    void mousePressed(MouseEvent e);
    void keyPressed(KeyEvent e);
    void keyReleased(KeyEvent e);
    void mouseReleased(MouseEvent e);
}