package br.com.uepg.operacaoretrolaser.settings;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Settings {
    private static final String ARQUIVO_CONFIG = "config.properties";

    public static float masterVolume = 1.0f; // 0.0 a 1.0
    public static float musicVolume = 0.5f;
    public static float sfxVolume = 1.0f;
    public static float brilho = 1.0f;       // 0.2 (Escuro) a 1.0 (Claro)
    // Opções de Vídeo
    public static boolean isFullScreen = true;
    public static boolean stretchScreen = false;
    public static int resolutionIndex;
    public static final int[][] RESOLUTIONS;

    public static void salvar() {
        Properties props = new Properties();
        props.setProperty("masterVolume", String.valueOf(masterVolume));
        props.setProperty("musicVolume", String.valueOf(musicVolume));
        props.setProperty("sfxVolume", String.valueOf(sfxVolume));
        props.setProperty("brilho", String.valueOf(brilho));
        props.setProperty("isFullScreen", String.valueOf(isFullScreen));
        props.setProperty("stretchScreen", String.valueOf(stretchScreen));
        props.setProperty("resolutionIndex", String.valueOf(resolutionIndex));

        try (FileOutputStream out = new FileOutputStream(ARQUIVO_CONFIG)) {
            props.store(out, "Configuracoes - Operacao RetroLaser");
        } catch (IOException e) {
            System.out.println("Erro ao salvar configurações: " + e.getMessage());
        }
    }

    public static void carregar() {
        var props = new Properties();
        try (var in = new FileInputStream(ARQUIVO_CONFIG)) {
            props.load(in);

            masterVolume = Float.parseFloat(props.getProperty("masterVolume", String.valueOf(masterVolume)));
            musicVolume = Float.parseFloat(props.getProperty("musicVolume", String.valueOf(musicVolume)));
            sfxVolume = Float.parseFloat(props.getProperty("sfxVolume", String.valueOf(sfxVolume)));
            brilho = Float.parseFloat(props.getProperty("brilho", String.valueOf(brilho)));
            isFullScreen = Boolean.parseBoolean(props.getProperty("isFullScreen", String.valueOf(isFullScreen)));
            stretchScreen = Boolean.parseBoolean(props.getProperty("stretchScreen", String.valueOf(stretchScreen)));

            // Carrega o índice da resolução e garante que ele não ultrapasse o limite do monitor atual
            int indexSalvo = Integer.parseInt(props.getProperty("resolutionIndex", String.valueOf(resolutionIndex)));
            resolutionIndex = Math.min(indexSalvo, RESOLUTIONS.length - 1);

        } catch (Exception e) {
            System.out.println("Arquivo de configuração não encontrado ou inválido. Usando padrões. " + e.getMessage());
            salvar(); // Cria o arquivo pela primeira vez
        }
    }

    static {
        // Pega a resolução nativa do monitor atual do usuário
        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        int screenWidth = mode.getWidth();
        int screenHeight = mode.getHeight();

        int[][] allResolutions = {
                {1280, 720},   // 16:9 HD
                {1366, 768},   // 16:9
                {1600, 900},   // 16:9
                {1920, 1080},  // 16:9 Full HD
                {2560, 1080},  // 21:9 Ultrawide (WFHD)
                {2560, 1440},  // 16:9 Quad HD
                {2560, 1520},  // Proporção do monitor atual
                {3440, 1440},  // 21:9 Ultrawide (UWQHD)
                {3840, 2160},  // 16:9 4K
                {5120, 2160}   // 21:9 Ultrawide (5K2K)
        };

        // Filtra para manter APENAS as resoluções que cabem no monitor
        List<int[]> validRes = new ArrayList<>();
        for (int[] res : allResolutions) {
            if (res[0] <= screenWidth && res[1] <= screenHeight) {
                validRes.add(res);
            }
        }

        // Medida de segurança: se a tela for menor que 1280x720, adicionamos
        // o tamanho exato da tela dele como única opção.
        if (validRes.isEmpty()) {
            validRes.add(new int[]{screenWidth, screenHeight});
        }

        RESOLUTIONS = validRes.toArray(new int[0][]);
        resolutionIndex = validRes.size() - 1;

        Settings.carregar();
    }
}