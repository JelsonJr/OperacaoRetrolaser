# Operação RetroLaser

**Operação RetroLaser** é um jogo de sobrevivência 2D *top-down* desenvolvido em Java. Originalmente concebido como um projeto acadêmico de Computação Gráfica, o sistema evoluiu para uma experiência orientada a rounds focada no gerenciamento de recursos e progressão contínua, fortemente inspirada no modo "Zombies" de jogos de tiro clássicos.

O objetivo central é sobreviver o maior tempo possível às invasões de robôs descontrolados, explorando o mapa e otimizando o arsenal do jogador.

### Mecânicas de Sobrevivência
O design do jogo força o jogador a tomar decisões estratégicas entre combate e economia:
* **Progressão por Rounds:** Inimigos surgem em ondas, e há um intervalo de 15 segundos entre os rounds para reabastecimento e planejamento.
* **Economia Dinâmica:** O combate gera dinheiro que deve ser investido para desbloquear novas áreas do mapa abrindo portas, ou na compra de equipamentos.
* **Melhorias de Arsenal:** Uma "Upgrade Station" permite evoluir as armas em até três níveis de potência, liberando mecânicas como o modo de disparo secundário no segundo nível.
* **Árvore de Habilidades (Perks):** Existem oito habilidades passivas e ativas disponíveis, como o "Replicante", que cria um clone do jogador para atrair inimigos, e a "Visão de Águia", que altera a renderização do mapa.
* **Névoa de Guerra (Fog of War):** O campo de visão é dinamicamente calculado utilizando o traçado de raios (Raycasting), ocultando áreas do mapa fora do alcance visual.
* **Combate Físico Escalonado:** O dano corpo a corpo do jogador recebe acréscimos naturais a cada round concluído.

### Arquitetura e Implementação Técnica
O projeto foi construído do zero focando na manipulação direta de gráficos rasterizados e vetoriais:
* **Tecnologia Base:** Desenvolvido puramente sobre a API Java 2D nativa, garantindo a execução independente de motores de terceiros.
* **Motor de Jogo Customizado:** Implementação de um *Game Loop* rigoroso em uma thread dedicada, mantendo as atualizações físicas travadas a 60 FPS.
* **Matrizes de Transformação:** Uso intensivo de `AffineTransform` para efeitos espaciais em menus e rotações em tempo real.
* **Tipografia e Interface:** O HUD é renderizado por meio de uma fonte externa pixelada e `FontMetrics` para o alinhamento de textos flutuantes, indicadores de dano e menus.
* **Gerenciamento de Estados:** Padrão arquitetural de máquina de estados controlando transições fluidas entre menus, tutorial, jogo e pausa.

### Como Baixar e Instalar

O jogo possui instaladores gerados automaticamente para **Windows** e **Linux (Debian/Ubuntu)**. Não é necessário instalar o Java separadamente, pois os pacotes já incluem tudo o que é necessário para rodar o jogo.

#### Download
Acesse a aba de **Releases** do repositório no GitHub e baixe os arquivos da versão mais recente (marcados com a tag `v*`).

#### Windows
1. Na aba de Releases, baixe o arquivo executável de instalação (`.exe`), com o nome semelhante a `Operacao-Retrolaser-*.exe`.
2. Dê um duplo clique no arquivo `.exe` baixado.
3. Siga os passos do instalador na tela.
4. O jogo será instalado e atalhos serão criados automaticamente no Menu Iniciar e na Área de Trabalho do Windows.

#### Linux (Ubuntu/Debian)
1. Na aba de Releases, baixe o pacote Debian (`.deb`), com o nome semelhante a `operacao-retrolaser_*.deb`.
2. Para instalar, você pode usar o gerenciador de pacotes da sua distribuição (geralmente dando um duplo clique no arquivo) ou abrir o terminal na pasta do download e executar:
   ```bash
   sudo apt install operacao-retrolaser_*.deb (recomendado)
    ```
   ou
   ```bash
   sudo dpkg -i operacao-retrolaser_*.deb
   sudo apt-get install -f
   ```
3. Após a instalação, o jogo estará disponível no menu de aplicativos do seu sistema operacional e também por meio de atalhos gerados (/opt/operacao-retrolaser/bin/"Operação Retrolaser").