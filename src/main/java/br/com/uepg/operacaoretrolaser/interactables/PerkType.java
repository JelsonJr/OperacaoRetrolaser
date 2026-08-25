package br.com.uepg.operacaoretrolaser.interactables;

public enum PerkType {
    TANQUE("Tanque", 2350),
    CHANCE_EXTRA("Chance Extra", 1500),
    PULMAO_ATLETA("Pulmão de Atleta", 4000),
    GOLPE_DURO("Golpe Duro", 2000),
    VISAO_AGUIA("Visão de Águia", 4500),
    PISTOLEIRO("Pistoleiro", 2500),
    TIRO_DUPLO("Tiro Duplo", 4000),
    REPLICANTE("Replicante", 2500);

    private final String nome;
    private final int custo;

    PerkType(String nome, int custo) {
        this.nome = nome;
        this.custo = custo;
    }

    public String getNome() { return nome; }
    public int getCusto() { return custo; }
}