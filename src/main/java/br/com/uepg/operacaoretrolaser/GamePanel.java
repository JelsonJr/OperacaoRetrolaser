package br.com.uepg.operacaoretrolaser;

import br.com.uepg.operacaoretrolaser.settings.Settings;
import br.com.uepg.operacaoretrolaser.settings.SoundManager;
import br.com.uepg.operacaoretrolaser.states.GameState;
import br.com.uepg.operacaoretrolaser.states.MenuState;
import br.com.uepg.operacaoretrolaser.states.PauseState;
import br.com.uepg.operacaoretrolaser.states.PlayState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private final GameState menuState, pauseState;
    private final Cursor invisibleCursor;
    private PlayState playState;
    private GameState currentState;
    private boolean isRunning = false;
    private Font pixelFont, titleFont;
    private int fps = 0;
    private int mouseX, mouseY;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        carregarFontes();

        SoundManager.loadSound("tiro", "/sounds/mixkit-short-laser-gun-shot-1670.wav");
        SoundManager.loadSound("main-music", "/sounds/Against_the_Steel_Horde.wav");
        SoundManager.loadSound("soco", "/sounds/mixkit-impact-of-a-strong-punch-2155.wav");
        SoundManager.loadSound("dano", "/sounds/mixkit-electronic-retro-block-hit-2185.wav"); 
        SoundManager.loadSound("game-over", "/sounds/mixkit-arcade-retro-game-over-213.wav"); 
        SoundManager.loadSound("clique", "/sounds/mixkit-video-game-retro-click-237.wav");    
        SoundManager.loadSound("porta", "/sounds/mixkit-futuristic-doorbell-928.wav");        
        SoundManager.loadSound("comprar", "/sounds/mixkit-unlock-new-item-game-notification-254.wav"); 
        SoundManager.loadSound("tiro-inimigo", "/sounds/mixkit-laser-cannon-shot-1678.wav");  

        var cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

        playState = new PlayState(this);
        pauseState = new PauseState(this);
        currentState = menuState = new MenuState(this);
    }

    public void reiniciarJogo() {
        this.playState = new PlayState(this);
        setState(this.playState);
        SoundManager.playMusic("main-music");
    }

    public PlayState getPlayState() {
        return playState;
    }

    private void carregarFontes() {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf");
            if (is == null) throw new FileNotFoundException("Fonte não encontrada");
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            pixelFont = baseFont.deriveFont(20f);
            titleFont = baseFont.deriveFont(65f);
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.BOLD, 20);
            titleFont = new Font("Monospaced", Font.BOLD, 65);
        }
    }

    public void startGame() {
        isRunning = true;
        var gameThread = new Thread(this);
        gameThread.start();
    }

    public void setState(GameState state) {
        if (this.currentState == menuState && state == playState) {
            SoundManager.playMusic("main-music");
        }

        this.currentState = state;

        if (state == playState) {
            this.setCursor(invisibleCursor);
            return;
        }

        if (state == menuState) {
            SoundManager.stopMusic();
            this.playState = new PlayState(this);
        }

        this.setCursor(Cursor.getDefaultCursor());
    }

    public GameState getMenuState() {
        return menuState;
    }

    public GameState getPauseState() {
        return pauseState;
    }

    public Font getPixelFont() {
        return pixelFont;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public int getFps() {
        return fps;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    @Override
    public void run() {
        final double ticksPerSecond = 60.0;
        final double nanosPerTick = 1_000_000_000.0 / ticksPerSecond;

        double delta = 0;
        long lastTime = System.nanoTime();

        int frames = 0;
        long timer = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();

            delta += (now - lastTime) / nanosPerTick;
            lastTime = now;

            boolean updated = false;

            while (delta >= 1) {
                currentState.update();
                delta--;
                updated = true;
            }

            if (updated) {
                repaint();
                frames++;
            }

            long sleepNanos = (long) ((1.0 - delta) * nanosPerTick);

            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                fps = frames;
                frames = 0;
                timer += 1000;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Cria uma CÓPIA do Graphics apenas para desenhar o mundo e os states
        Graphics2D g2dState = (Graphics2D) g.create();
        g2dState.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2dState.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // O PlayState pode mover a câmera à vontade aqui dentro...
        currentState.draw(g2dState);

        // Descarta essa cópia (e qualquer movimento de câmera que ela sofreu)
        g2dState.dispose();

        // brilho e HUD
        Graphics2D g2dUI = (Graphics2D) g.create();
        if (Settings.brilho < 1.0f) {
            int alpha = (int) ((1.0f - Settings.brilho) * 255);
            g2dUI.setColor(new Color(0, 0, 0, alpha));
            g2dUI.fillRect(0, 0, WIDTH, HEIGHT);
        }

        Toolkit.getDefaultToolkit().sync();
        g2dUI.dispose();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        currentState.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
        currentState.mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentState.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentState.mouseReleased(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentState.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentState.keyReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}