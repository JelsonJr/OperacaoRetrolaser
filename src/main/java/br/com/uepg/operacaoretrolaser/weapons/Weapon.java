package br.com.uepg.operacaoretrolaser.weapons;

import java.util.ArrayList;
import java.util.List;

public class Weapon {
    private final String nome;
    private final FireType fireType;
    private int nivelUpgrade = 0;

    // Configurações do Tiro Principal [0=Base, 1=Niv1, 2=Niv2, 3=Niv3]
    private final int[] danoPrincipal;
    private final long[] cdPrincipalMs;
    private final int[] projeteisPorTiro;
    private final float[] espalhamentoGraus;
    private final float[] alcancePrincipal;

    // Configurações do Tiro Secundário
    private final int[] danoSecundario;
    private final long[] cdSecundarioMs;
    private final int[] projeteisSecundario;
    private final float[] espalhamentoSecundario;
    private final float[] alcanceSecundario;

    private long ultimoTiroPrincipal = 0;
    private long ultimoTiroSecundario = 0;
    private boolean gatilhoLiberado = true;

    public Weapon(String nome, FireType fireType,
                  int[] danoPrincipal, long[] cdPrincipalMs, int[] projeteisPorTiro, float[] espalhamentoGraus, float[] alcancePrincipal,
                  int[] danoSecundario, long[] cdSecundarioMs, int[] projeteisSec, float[] espalhamentoSec, float[] alcanceSecundario) {
        this.nome = nome;
        this.fireType = fireType;
        this.danoPrincipal = danoPrincipal;
        this.cdPrincipalMs = cdPrincipalMs;
        this.projeteisPorTiro = projeteisPorTiro;
        this.espalhamentoGraus = espalhamentoGraus;
        this.alcancePrincipal = alcancePrincipal;
        this.danoSecundario = danoSecundario;
        this.cdSecundarioMs = cdSecundarioMs;
        this.projeteisSecundario = projeteisSec;
        this.espalhamentoSecundario = espalhamentoSec;
        this.alcanceSecundario = alcanceSecundario;
    }

    public void promoverUpgrade() {
        if (nivelUpgrade < 3) nivelUpgrade++;
    }

    public void liberarGatilho() {
        this.gatilhoLiberado = true;
    }

    public List<Projectile> atirarPrincipal(float startX, float startY, float targetX, float targetY) {
        List<Projectile> criados = new ArrayList<>();
        long agora = System.currentTimeMillis();

        if (agora - ultimoTiroPrincipal < cdPrincipalMs[nivelUpgrade]) return criados;
        if (fireType == FireType.SEMI_AUTOMATIC && !gatilhoLiberado) return criados;

        criados.addAll(gerarProjeteis(startX, startY, targetX, targetY,
                projeteisPorTiro[nivelUpgrade], espalhamentoGraus[nivelUpgrade],
                danoPrincipal[nivelUpgrade], alcancePrincipal[nivelUpgrade], false));

        ultimoTiroPrincipal = agora;
        gatilhoLiberado = false;
        return criados;
    }

    public List<Projectile> atirarSecundario(float startX, float startY, float targetX, float targetY) {
        List<Projectile> criados = new ArrayList<>();
        if (nivelUpgrade < 2) return criados;

        long agora = System.currentTimeMillis();
        if (agora - ultimoTiroSecundario < cdSecundarioMs[nivelUpgrade]) return criados;

        criados.addAll(gerarProjeteis(startX, startY, targetX, targetY,
                projeteisSecundario[nivelUpgrade], espalhamentoSecundario[nivelUpgrade],
                danoSecundario[nivelUpgrade], alcanceSecundario[nivelUpgrade], true));

        ultimoTiroSecundario = agora;
        return criados;
    }

    private List<Projectile> gerarProjeteis(float sx, float sy, float tx, float ty, int qtd, float espalhamento, int dano, float alcance, boolean isEspecial) {
        List<Projectile> lista = new ArrayList<>();
        double anguloBase = Math.atan2(ty - sy, tx - sx);

        for (int i = 0; i < qtd; i++) {
            double desvio = (Math.random() - 0.5) * Math.toRadians(espalhamento);
            double anguloFinal = anguloBase + desvio;

            float destX = sx + (float) Math.cos(anguloFinal) * 500;
            float destY = sy + (float) Math.sin(anguloFinal) * 500;

            lista.add(new Projectile(sx, sy, destX, destY, dano, isEspecial, alcance));
        }
        return lista;
    }

    public String getNome() { return nome; }
    public int getNivelUpgrade() { return nivelUpgrade; }
}