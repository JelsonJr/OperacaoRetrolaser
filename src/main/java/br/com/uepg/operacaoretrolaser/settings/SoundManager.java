package br.com.uepg.operacaoretrolaser.settings;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, byte[]> soundBuffer = new HashMap<>();
    private static final Map<String, AudioFormat> formatMap = new HashMap<>();
    private static Clip bgmClip;

    public static void loadSound(String key, String resourcePath) {
        try (InputStream is = SoundManager.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                try (var bufferedIn = new BufferedInputStream(is); AudioInputStream ais = AudioSystem.getAudioInputStream(bufferedIn)) {
                    soundBuffer.put(key, ais.readAllBytes());
                    formatMap.put(key, ais.getFormat());
                }
            } else {
                System.err.println("Aviso: Arquivo de som não encontrado [" + key + "] em: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar o som [" + key + "]: " + e.getMessage());
        }
    }

    public static void playSFX(String key) {
        byte[] data = soundBuffer.get(key);
        AudioFormat format = formatMap.get(key);
        if (data == null || format == null) return;

        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length / format.getFrameSize());
                clip.open(ais);

                clip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                aplicarVolume(clip, Settings.sfxVolume);

                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {
                System.out.println("Erro ao reproduzir audio [" + key + "]: " + e.getMessage());
            }
        }).start();
    }

    public static void playMusic(String key) {
        stopMusic();
        byte[] data = soundBuffer.get(key);
        AudioFormat format = formatMap.get(key);
        if (data == null || format == null) return;

        try {
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length / format.getFrameSize());
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);

            aplicarVolume(bgmClip, Settings.musicVolume);

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Erro ao reproduzir audio: " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    public static void updateBackgroundMusicVolume() {
        if (bgmClip != null && bgmClip.isOpen()) {
            aplicarVolume(bgmClip, Settings.musicVolume);
        }
    }

    private static void aplicarVolume(Clip clip, float volumeDaCategoria) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeFinal = Settings.masterVolume * volumeDaCategoria;
            float dB = 20f * (float) Math.log10(Math.max(volumeFinal, 0.0001f));
            gainControl.setValue(dB);
        }
    }
}