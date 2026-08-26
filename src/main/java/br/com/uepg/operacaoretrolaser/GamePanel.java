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
    private Robot robot;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Erro ao iniciar o Robot: " + e.getMessage());
        }

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

        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (currentState == playState) {
                    setState(pauseState);
                }
            }
        });

        this.addMouseWheelListener(e -> {
            if (currentState instanceof PlayState) {
                ((PlayState) currentState).mouseWheelMoved(e);
            }
        });
    }

    public void applyVideoSettings() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            applyVideoSettings(frame);
        }
    }

    private void travarMouseNaJanela(MouseEvent e) {
        // Só prende o mouse se estivermos jogando e no modo janela
        if (robot == null || currentState != playState || Settings.isFullScreen) return;

        Point telaCursor = e.getPoint();
        SwingUtilities.convertPointToScreen(telaCursor, this);

        Point limiteJanela = new Point(0, 0);
        SwingUtilities.convertPointToScreen(limiteJanela, this);

        int minX = limiteJanela.x;
        int minY = limiteJanela.y;
        int maxX = minX + getWidth() - 1;
        int maxY = minY + getHeight() - 1;

        boolean fugiu = false;
        int empurrarX = telaCursor.x;
        int empurrarY = telaCursor.y;

        if (empurrarX <= minX) { empurrarX = minX + 2; fugiu = true; }
        else if (empurrarX >= maxX) { empurrarX = maxX - 2; fugiu = true; }

        if (empurrarY <= minY) { empurrarY = minY + 2; fugiu = true; }
        else if (empurrarY >= maxY) { empurrarY = maxY - 2; fugiu = true; }

        if (fugiu) {
            robot.mouseMove(empurrarX, empurrarY);
        }
    }

    public void applyVideoSettings(JFrame frame) {
        frame.dispose(); // Libera recursos para alterar decorações sem erro
        frame.setUndecorated(Settings.isFullScreen);

        if (Settings.isFullScreen) {
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        } else {
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
            int[] res = Settings.RESOLUTIONS[Settings.resolutionIndex];
            this.setPreferredSize(new Dimension(res[0], res[1]));
            frame.pack();
            frame.setLocationRelativeTo(null); // Centraliza a janela
        }
        frame.setVisible(true);
        this.requestFocusInWindow(); // Retoma o foco de entrada
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
            titleFont = baseFont.deriveFont(48f);
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

        int screenWidth = getWidth();
        int screenHeight = getHeight();

        double scaleX = (double) screenWidth / WIDTH;
        double scaleY = (double) screenHeight / HEIGHT;

        double scale = Settings.stretchScreen ? 1.0 : Math.min(scaleX, scaleY);

        int offsetX = 0;
        int offsetY = 0;

        if (!Settings.stretchScreen) {
            offsetX = (int) ((screenWidth - (WIDTH * scale)) / 2);
            offsetY = (int) ((screenHeight - (HEIGHT * scale)) / 2);
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        Graphics2D g2dState = (Graphics2D) g.create();
        g2dState.translate(offsetX, offsetY);

        if (Settings.stretchScreen) {
            g2dState.scale(scaleX, scaleY);
        } else {
            g2dState.scale(scale, scale);
        }

        g2dState.clipRect(0, 0, WIDTH, HEIGHT);
        g2dState.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2dState.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        currentState.draw(g2dState);
        g2dState.dispose();

        Graphics2D g2dUI = (Graphics2D) g.create();
        g2dUI.translate(offsetX, offsetY);

        if (Settings.stretchScreen) {
            g2dUI.scale(scaleX, scaleY);
        } else {
            g2dUI.scale(scale, scale);
        }

        g2dUI.clipRect(0, 0, WIDTH, HEIGHT);
        if (Settings.brilho < 1.0f) {
            int alpha = (int) ((1.0f - Settings.brilho) * 255);
            g2dUI.setColor(new Color(0, 0, 0, alpha));
            g2dUI.fillRect(0, 0, WIDTH, HEIGHT);
        }
        Toolkit.getDefaultToolkit().sync();
        g2dUI.dispose();
    }

    private MouseEvent scaleMouse(MouseEvent e) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        double scaleX = (double) screenWidth / WIDTH;
        double scaleY = (double) screenHeight / HEIGHT;

        int logicalX, logicalY;

        if (Settings.stretchScreen) {
            logicalX = (int) (e.getX() / scaleX);
            logicalY = (int) (e.getY() / scaleY);
        } else {
            double scale = Math.min(scaleX, scaleY);
            int offsetX = (int) ((screenWidth - (WIDTH * scale)) / 2);
            int offsetY = (int) ((screenHeight - (HEIGHT * scale)) / 2);
            logicalX = (int) ((e.getX() - offsetX) / scale);
            logicalY = (int) ((e.getY() - offsetY) / scale);
        }

        // Garante que o mouse fique estritamente dentro da área de jogo (0 a WIDTH / 0 a HEIGHT)
        logicalX = Math.clamp(logicalX, 0, WIDTH);
        logicalY = Math.clamp(logicalY, 0, HEIGHT);

        return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), logicalX, logicalY, e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        travarMouseNaJanela(e); // <-- ADICIONE AQUI
        MouseEvent translated = scaleMouse(e);
        this.mouseX = translated.getX();
        this.mouseY = translated.getY();
        currentState.mouseMoved(translated);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        travarMouseNaJanela(e); // <-- ADICIONE AQUI
        MouseEvent translated = scaleMouse(e);
        this.mouseX = translated.getX();
        this.mouseY = translated.getY();
        currentState.mouseMoved(translated);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentState.mousePressed(scaleMouse(e));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentState.mouseReleased(scaleMouse(e));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}