package br.com.uepg.operacaoretrolaser.interactables;

public enum PerkType {
    TANQUE("Tanque", 2500),
    CHANCE_EXTRA("Chance Extra", 1750),
    PULMAO_ATLETA("Pulmão de Atleta", 3500),
    GOLPE_DURO("Golpe Duro", 2500),
    VISAO_AGUIA("Visão de Águia", 4500),
    PISTOLEIRO("Pistoleiro", 2000),
    TIRO_DUPLO("Tiro Duplo", 4500),
    REPLICANTE("Replicante", 2000);

    private final String nome;
    private final int custo;

    PerkType(String nome, int custo) {
        this.nome = nome;
        this.custo = custo;
    }

    public String getNome() { return nome; }
    public int getCusto() { return custo; }
}