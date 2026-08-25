package br.com.uepg.operacaoretrolaser.weapons;

public class MeleeWeapon {
    private int dano;
    private float cadencia; // ataques por segundo
    private float alcance; // em pixels
    private long ultimoAtaque = 0;

    public MeleeWeapon(int dano, float cadencia, float alcance) {
        this.dano = dano;
        this.cadencia = cadencia;
        this.alcance = alcance;
    }

    public void setDano(int dano) {
        this.dano = dano;
    }

    public void setCadencia(float cadencia) {
        this.cadencia = cadencia;
    }

    public void setAlcance(float alcance) {
        this.alcance = alcance;
    }

    public int getDano() {
        return dano;
    }

    public float getCadencia() {
        return cadencia;
    }

    public float getAlcance() {
        return alcance;
    }

    public boolean podeAtacar() {
        return (System.currentTimeMillis() - ultimoAtaque) >= (1000f / cadencia);
    }

    public void registrarAtaque() {
        this.ultimoAtaque = System.currentTimeMillis();
    }
}