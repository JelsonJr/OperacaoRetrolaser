package br.com.uepg.operacaoretrolaser.weapons;

/**
 * Fábrica responsável pela criação e configuração de todas as armas do jogo.
 * Define estatísticas de dano, cadência, projéteis, espalhamento e disparos secundários
 * para todos os níveis de upgrade (0 a 3).
 */
public class WeaponFactory {

    /**
     * Cria a arma inicial balanceada "Pistola Apex-9".
     * <p><b>Características:</b> Disparo semi-automático confiável. Aumenta drasticamente a precisão e cadência nos níveis finais, disparando projéteis duplos no nível máximo.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [27, 45, 65, 85]</li>
     *   <li><b>Cooldown Principal:</b> [220ms, 190ms, 160ms, 120ms]</li>
     *   <li><b>Projéteis por Tiro:</b> [1, 1, 2, 3]</li>
     *   <li><b>Espalhamento:</b> [3.0°, 2.0°, 1.0°, 0.5°]</li>
     *   <li><b>Alcance Principal:</b> [500, 600, 700, 800] unidades</li>
     *   <li><b>Dano Secundário (Nív 2+):</b> [0, 0, 150, 350]</li>
     *   <li><b>Cooldown Secundário:</b> [0, 0, 600ms, 500ms]</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Pistola Apex-9.
     */
    public static Weapon criarPistola() {
        return new Weapon(
                "Pistola Apex-9", FireType.SEMI_AUTOMATIC,
                new int[]{27, 45, 65, 85},
                new long[]{220, 190, 160, 120},
                new int[]{1, 1, 2, 3},
                new float[]{3.0f, 2.0f, 1.0f, 0.5f},
                new float[]{500f, 600f, 700f, 800f},
                new int[]{0, 0, 150, 350},
                new long[]{0, 0, 600, 500},
                new int[]{0, 0, 1, 1},
                new float[]{0f, 0f, 0f, 0f},
                new float[]{0f, 0f, 800f, 1200f}
        );
    }

    /**
     * Cria a submetralhadora "SMG Viper-X".
     * <p><b>Características:</b> Projetada para fogo supressivo de curtíssimo alcance. Altíssima taxa de disparo compensada por menor dano individual por projétil.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [15, 25, 35, 42]</li>
     *   <li><b>Cooldown Principal:</b> [70ms, 60ms, 50ms, 40ms]</li>
     *   <li><b>Projéteis por Tiro:</b> [1, 1, 1, 1]</li>
     *   <li><b>Espalhamento:</b> [13.0°, 11.0°, 9.0°, 7.2°]</li>
     *   <li><b>Alcance Principal:</b> [350, 400, 450, 550] unidades</li>
     *   <li><b>Ataque Secundário (Nív 2+):</b> Disparo em rajada com múltiplos projéteis dispersos (Dano: 50 a 80, Projéteis: 6 a 8).</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como SMG Viper-X.
     */
    public static Weapon criarSubmetralhadora() {
        return new Weapon(
                "SMG Viper-X", FireType.AUTOMATIC,
                new int[]{15, 25, 35, 42},
                new long[]{70, 60, 50, 40},
                new int[]{1, 1, 1, 1},
                new float[]{13.0f, 11.0f, 9.0f, 7.2f},
                new float[]{350f, 400f, 450f, 550f},
                new int[]{0, 0, 50, 80},
                new long[]{0, 0, 300, 200},
                new int[]{0, 0, 6, 8},
                new float[]{0f, 0f, 30.0f, 27.0f},
                new float[]{0f, 0f, 350f, 450f}
        );
    }

    /**
     * Cria a metralhadora de combate "SMG Nuxia-P".
     * <p><b>Características:</b> Variante focada em melhor precisão e alcance intermediário, entregando dano consistente com disparos secundários precisos.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [22, 32, 45, 54]</li>
     *   <li><b>Cooldown Principal:</b> [120ms, 108ms, 85ms, 70ms]</li>
     *   <li><b>Projéteis por Tiro:</b> [1, 1, 1, 1]</li>
     *   <li><b>Espalhamento:</b> [11.0°, 9.5°, 7.5°, 6.0°]</li>
     *   <li><b>Alcance Principal:</b> [350, 450, 500, 550] unidades</li>
     *   <li><b>Dano Secundário (Nív 2+):</b> [0, 0, 200, 400]</li>
     *   <li><b>Cooldown Secundário:</b> [0, 0, 420ms, 380ms]</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como SMG Nuxia-P.
     */
    public static Weapon criarMetralhadoraDeCombate() {
        return new Weapon(
                "SMG Nuxia-P", FireType.AUTOMATIC,
                new int[]{22, 32, 45, 54},
                new long[]{120, 108, 85, 70},
                new int[]{1, 1, 1, 1},
                new float[]{11.0f, 9.5f, 7.5f, 6f},
                new float[]{350f, 450f, 500f, 550f},
                new int[]{0, 0, 200, 400},
                new long[]{0, 0, 420, 380},
                new int[]{0, 0, 1, 2},
                new float[]{0f, 0f, 1.0f, 2.5f},
                new float[]{0f, 0f, 650f, 750f}
        );
    }

    /**
     * Cria o rifle de assalto "Rifle Tempest".
     * <p><b>Características:</b> O clássico versátil para combate de médio a longo alcance. Combina dano substancial e alcance elevado com controle moderado de recuo.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [35, 55, 75, 100]</li>
     *   <li><b>Cooldown Principal:</b> [130ms, 120ms, 110ms, 100ms]</li>
     *   <li><b>Projéteis por Tiro:</b> [1, 1, 1, 1]</li>
     *   <li><b>Espalhamento:</b> [6.0°, 5.0°, 4.0°, 2.5°]</li>
     *   <li><b>Alcance Principal:</b> [700, 800, 1000, 1200] unidades</li>
     *   <li><b>Dano Secundário (Nív 2+):</b> Disparo pesado concentrado [0, 0, 200, 380].</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Rifle Tempest.
     */
    public static Weapon criarRifleAssalto() {
        return new Weapon(
                "Rifle Tempest", FireType.AUTOMATIC,
                new int[]{35, 55, 75, 100},
                new long[]{130, 120, 110, 100},
                new int[]{1, 1, 1, 1},
                new float[]{6.0f, 5.0f, 4.0f, 2.5f},
                new float[]{700f, 800f, 1000f, 1200f},
                new int[]{0, 0, 200, 380},
                new long[]{0, 0, 800, 650},
                new int[]{0, 0, 3, 5},
                new float[]{0f, 0f, 10.0f, 13.0f},
                new float[]{0f, 0f, 800f, 1000f}
        );
    }

    /**
     * Cria a escopeta pesada "Escopeta Vulcan".
     * <p><b>Características:</b> Lenta, porém devastadora a curtas distâncias. Dispara uma grande quantidade de bagos por cartucho em área cônica.</p>
     *
     * <ul>
     *   <li><b>Dano por Bago:</b> [15, 22, 30, 45]</li>
     *   <li><b>Cooldown Principal:</b> [650ms, 500ms, 400ms, 300ms]</li>
     *   <li><b>Bagos por Disparo:</b> [7, 9, 11, 14]</li>
     *   <li><b>Espalhamento:</b> [15.0°, 14.0°, 12.0°, 10.0°]</li>
     *   <li><b>Alcance Principal:</b> [400, 450, 500, 650] unidades</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Escopeta Vulcan.
     */
    public static Weapon criarEscopeta() {
        return new Weapon(
                "Escopeta Vulcan", FireType.SHOTGUN,
                new int[]{15, 22, 30, 45},
                new long[]{650, 500, 400, 300},
                new int[]{7, 9, 11, 14},
                new float[]{15.0f, 14.0f, 12.0f, 10.0f},
                new float[]{400f, 450f, 500f, 650f},
                new int[]{0, 0, 400, 700},
                new long[]{0, 0, 1200, 1000},
                new int[]{0, 0, 2, 3},
                new float[]{0f, 0f, 0.0f, 4.0f},
                new float[]{0f, 0f, 700f, 900f}
        );
    }

    /**
     * Cria a escopeta automática "Escopeta Cerberus".
     * <p><b>Características:</b> Combina a taxa de disparo automática com espalhamento alto. Ideal para limpar salas com múltiplos inimigos próximos.</p>
     *
     * <ul>
     *   <li><b>Dano por Bago:</b> [8, 12, 16, 22]</li>
     *   <li><b>Cooldown Principal:</b> [350ms, 300ms, 250ms, 200ms]</li>
     *   <li><b>Bagos por Disparo:</b> [6, 7, 8, 8]</li>
     *   <li><b>Espalhamento:</b> [20.0°, 18.0°, 16.0°, 14.0°]</li>
     *   <li><b>Alcance Principal:</b> [280, 320, 400, 500] unidades</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Escopeta Cerberus.
     */
    public static Weapon criarEscopetaK() {
        return new Weapon(
                "Escopeta Cerberus", FireType.AUTOMATIC,
                new int[]{8, 12, 16, 22},
                new long[]{350, 300, 250, 200},
                new int[]{6, 7, 8, 8},
                new float[]{20.0f, 18.0f, 16.0f, 14.0f},
                new float[]{280f, 320f, 400f, 500f},
                new int[]{0, 0, 60, 80},
                new long[]{0, 0, 500, 500},
                new int[]{0, 0, 12, 16},
                new float[]{0f, 0f, 35.0f, 40.0f},
                new float[]{0f, 0f, 250f, 400f}
        );
    }

    /**
     * Cria o rifle eletromagnético "Rifle Magnetar-L".
     * <p><b>Características:</b> Disparos ritmados de alta velocidade e precisão absoluta (0° de espalhamento). Possui longo alcance funcional.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [90, 140, 200, 280]</li>
     *   <li><b>Cooldown Principal:</b> [400ms, 350ms, 300ms, 235ms]</li>
     *   <li><b>Espalhamento:</b> 0.0° (Precisão perfeita)</li>
     *   <li><b>Alcance Principal:</b> [1000, 1200, 1400, 1800] unidades</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Rifle Magnetar-L.
     */
    public static Weapon criarRifleGaussL() {
        return new Weapon(
                "Rifle Magnetar-L", FireType.SEMI_AUTOMATIC,
                new int[]{90, 140, 200, 280},
                new long[]{400, 350, 300, 235},
                new int[]{1, 1, 1, 1},
                new float[]{0.0f, 0.0f, 0.0f, 0.0f},
                new float[]{1000f, 1200f, 1400f, 1800f},
                new int[]{0, 0, 70, 85},
                new long[]{0, 0, 150, 100},
                new int[]{0, 0, 2, 3},
                new float[]{0f, 0f, 0f, 0f},
                new float[]{0f, 0f, 1500f, 2000f}
        );
    }

    /**
     * Cria o fuzil de longa distância "Fuzil Hyperion".
     * <p><b>Características:</b> Disparo único colossal capaz de eliminar inimigos em um só tiro. Cadência extremamente lenta e alcance máximo da arena.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [120, 280, 400, 600]</li>
     *   <li><b>Cooldown Principal:</b> [1000ms, 8000ms, 700ms, 600ms]</li>
     *   <li><b>Espalhamento:</b> 0.0°</li>
     *   <li><b>Alcance Principal:</b> [1500, 1800, 2200, 2800] unidades</li>
     *   <li><b>Dano Secundário (Nív 2+):</b> Disparo devastador perfurante [0, 0, 120, 150].</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Fuzil Hyperion.
     */
    public static Weapon criarSniper() {
        return new Weapon(
                "Fuzil Hyperion", FireType.SEMI_AUTOMATIC,
                new int[]{120, 280, 400, 600},
                new long[]{1000, 8000, 700, 600},
                new int[]{1, 1, 1, 1},
                new float[]{0.0f, 0.0f, 0.0f, 0.0f},
                new float[]{1500f, 1800f, 2200f, 2800f},
                new int[]{0, 0, 120, 150},
                new long[]{0, 0, 300, 200},
                new int[]{0, 0, 3, 5},
                new float[]{0f, 0f, 4.0f, 2.5f},
                new float[]{0f, 0f, 2000f, 2500f}
        );
    }

    /**
     * Cria a metralhadora pesada de supressão "Metralhadora Titan".
     * <p><b>Características:</b> Dispara projéteis múltiplos por ciclo em área levemente aberta. Excelente para controle de grandes hordas de inimigos.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [40, 60, 85, 105]</li>
     *   <li><b>Cooldown Principal:</b> [200ms, 170ms, 150ms, 130ms]</li>
     *   <li><b>Projéteis por Tiro:</b> [1, 1, 2, 3]</li>
     *   <li><b>Espalhamento:</b> [15.0°, 13.0°, 11.0°, 8.5°]</li>
     *   <li><b>Alcance Principal:</b> [750, 850, 950, 1100] unidades</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Metralhadora Titan.
     */
    public static Weapon criarMetralhadoraPesada() {
        return new Weapon(
                "Metralhadora Titan", FireType.AUTOMATIC,
                new int[]{40, 60, 85, 105},
                new long[]{200, 170, 150, 130},
                new int[]{1, 1, 2, 3},
                new float[]{15.0f, 13.0f, 11.0f, 8.5f},
                new float[]{750f, 850f, 950f, 1100f},
                new int[]{0, 0, 250, 350},
                new long[]{0, 0, 1400, 1250},
                new int[]{0, 0, 10, 15},
                new float[]{0f, 0f, 30.0f, 22.0f},
                new float[]{0f, 0f, 550f, 700f}
        );
    }

    /**
     * Cria o armamento energético "Canhão Pulsar".
     * <p><b>Características:</b> Projéteis de alta velocidade com excelente cadência e escala de dano progressiva. O tiro secundário dispara uma carga explosiva de altíssimo dano.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [25, 40, 55, 85]</li>
     *   <li><b>Cooldown Principal:</b> [88ms, 85ms, 75ms, 65ms]</li>
     *   <li><b>Espalhamento:</b> [3.0°, 2.5°, 2.0°, 0.7°]</li>
     *   <li><b>Dano Secundário (Nív 2+):</b> [0, 0, 750, 930]</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Canhão Pulsar.
     */
    public static Weapon criarPlasma() {
        return new Weapon(
                "Canhão Pulsar", FireType.AUTOMATIC,
                new int[]{25, 40, 55, 85},
                new long[]{88, 85, 75, 65},
                new int[]{1, 1, 1, 1},
                new float[]{3.0f, 2.5f, 2.0f, 0.7f},
                new float[]{650f, 750f, 850f, 1000f},
                new int[]{0, 0, 750, 930},
                new long[]{0, 0, 1500, 1200},
                new int[]{0, 0, 1, 2},
                new float[]{0f, 0f, 0f, 4.0f},
                new float[]{0f, 0f, 640f, 800f}
        );
    }

    /**
     * Cria a pistola pesada energética "Revólver Nova".
     * <p><b>Características:</b> Disparos potentes de energia em ritmo moderado. Funciona como um canhão de bolso com boa precisão.</p>
     *
     * <ul>
     *   <li><b>Dano Principal:</b> [60, 95, 140, 200]</li>
     *   <li><b>Cooldown Principal:</b> [350ms, 320ms, 280ms, 240ms]</li>
     *   <li><b>Espalhamento:</b> 0.0°</li>
     *   <li><b>Alcance Principal:</b> [550, 650, 750, 850] unidades</li>
     * </ul>
     *
     * @return Instância da arma {@link Weapon} configurada como Revólver Nova.
     */
    public static Weapon criarRevolver() {
        return new Weapon(
                "Revólver Nova", FireType.SEMI_AUTOMATIC,
                new int[]{60, 95, 140, 200},
                new long[]{350, 320, 280, 240},
                new int[]{1, 1, 1, 1},
                new float[]{0f, 0f, 0f, 0f},
                new float[]{550f, 650f, 750f, 850f},
                new int[]{0, 0, 220, 400},
                new long[]{0, 0, 350, 300},
                new int[]{0, 0, 1, 1},
                new float[]{0f, 0f, 0f, 0f},
                new float[]{0f, 0f, 600f, 800f}
        );
    }
}