package org.openjfx.engine;

import org.openjfx.controller.GameView;
import org.openjfx.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/** GameEngine handles the core of the game and the matches.
 * it acts as a controller of the game state, coordinating interactions between the
 * model layer ({@link GameState}, {@link Card}, {@link Player}) and the view ({@link GameView}).
 * It handles turn rotation and card mechanics (like skips, reverses, and penalties),
 * UNO declarations(and its failuire), and bots.
 * Its statistics are kept by {@link GameStats} and {@link MatchStats}
 * @author Lucia Annicchiarico
 * @author Andriy Chyzhevskyy
 * @author Leon Balbo
 */
public class GameEngine {

    private GameState state;
    private GameMode gameMode;
    private GameView view;
    private MatchSettings settings;
    private GameStats globalStats;
    private final int MIN_PLAYER = 2;
    private final int MAX_PLAYER = 6;
    private Card currentCard;
    private Color currentColor;
    private boolean hasDrawnThisTurn = false;
    private int pendingDrawPenalty = 0;
    private boolean hasDeclaredUno = false;
    public long simulationDurationMs = 0;
    private Random random = new Random();

    /** Sole constructor which initializes the engine with the required core components, specifies the settings
     * and initializes a new tracking instance for the global statistics.
     * @param nState state of the game
     * @param nGameMode whether it's ClassicMode or ScoreBasedGame
     * @param view
     * @param settings
     */
    public GameEngine(GameState nState, GameMode nGameMode, GameView view, MatchSettings settings) {
        this.state = nState;
        this.gameMode = nGameMode;
        this.view = view;
        this.settings = settings;
        setWinningScore(gameMode,settings);

        globalStats = GameStats.getInstance();
    }

    /** if the game mode is score based, then the target score is updated according to the chosen settings
     * @param gameMode
     * @param settings
     */
    public void setWinningScore(GameMode gameMode, MatchSettings settings){
        if (gameMode instanceof ScoreBasedGame scoreBasedGame) {
            scoreBasedGame.setTargetPoints(settings.WinningScore);
        }
    }

    public GameState getState() {
        return this.state;
    }

    /** dealHand() distributes a specified number of cards to each player.

     /** distributes a specified number of cards to each player.
     * @param cardsPerPlayer
     */
    public void dealHand(int cardsPerPlayer) {
        // da implementare
        for (Player player : state.players) {
            Stack<Card> deck = state.getTopCards(cardsPerPlayer);
            player.initiateHand(deck);
        }
    }

    /** prepareDeck() shuffles deck.
     */
    public void prepareDeck() {
        state.shuffle(state.drawPile);
    }

    /** startGame() lays the foundation of the game by preparing the deck, distributing the cards
     * and getting the turns started.
     */
    public void startGame() {
        state.setMatchStats(new MatchStats(state.players, this.settings));
        state.getMatchStats().setCustomScoring(settings.customScoringEnabled);

        //state.matchStats = new MatchStats(state.players);

        state.getMatchStats().incrementRounds();

        prepareDeck();
        dealHand(7);
        // distribuisci carte

        currentCard = state.getTopCards(1).pop(); // Prima carta da mettere a terra
        state.discardPile.add(currentCard);    // Aggiungiamo carta a terra
        currentColor = currentCard.getColor();
        if (!settings.simulationModeEnabled) {
            view.updateTopCard(currentCard);
            view.showMessage("CE LA FACCIAMOOOOO");
            startTurn(); }



    }

    /** resumeGame() restores the internal engine variables and the GUI from a loaded state.
     */
    public void resumeGame() {
        // Ripristina la carta e il colore correnti dal GameState caricato
        currentCard = state.discardPile.peek();
        currentColor = state.currentColor;

        // Aggiorna la vista
        view.updateTopCard(currentCard);
        view.showMessage("Partita caricata con successo!");

        // Fai ripartire il turno del giocatore corrente
        startTurn();
    }

    /** lets the player starts their turn, whether they are a human player or a bot
     */
    public void startTurn()
    {

        state.getMatchStats().incrementTurns();
        hasDrawnThisTurn = false;
        Player currentPlayer = state.getCurrentPlayer();

        //Debug extra

        System.out.println("\n--- INIZIO TURNO ---");
        System.out.println("Tavolo (Cima): " + currentCard.getColor() + " " + currentCard.getType() + " | Colore Corrente: " + currentColor);
        System.out.println("Tocca a: " + currentPlayer.getName());
        for (Player p : state.players) {
            System.out.println(" - " + p.getName() + " ha " + p.getHandSize() + " carte");
        }
        System.out.println("--------------------\n");

        //Controllo se c'è lo stacking all'inizio

        view.onTurnChanged(currentPlayer);
        view.updateOpponentsStatus(state.players, currentPlayer);
        if (state.pendingDrawPenalty > 0) {
            handleStackingPhase(currentPlayer);
            return; // Fermiamo l'esecuzione normale del turno
        }

        if (currentPlayer instanceof HumanPlayer) {
            // Implementazione Privacy Mode
            if (settings.privacyModeEnabled) {
                view.showPrivacyScreen(currentPlayer);
            } else {
                view.updatePlayerHand(currentPlayer.getHand());
                view.showMessage("È il tuo turno, " + currentPlayer.getName() + "Yep, this is the gorbino's quest of turns");
            }
        } else {
            view.showMessage(currentPlayer.getName() + " (Bot) sta calcolando l'entropia");
            executeBotTurn((BotPlayer) currentPlayer);
        }
    }

    /** checks if a player can respond to a stacking card.
     * In any way, the view shows the outcome.
     * @param currentPlayer
     */
    private void handleStackingPhase(Player currentPlayer) {
        Card defenseCard = null;
        for (Card c : currentPlayer.getHand()){
            if (c.getType() == state.activeStackType){
                defenseCard = c;
                break;
            }
        }

        if(defenseCard != null) {
            view.showMessage(currentPlayer.getName() + " C'è uno stack! Difenditi!");

            // FIX: Se è un Bot, lancia automaticamente la carta per difendersi!
            if (currentPlayer instanceof BotPlayer) {
                executeMove(currentPlayer, defenseCard);
            }
            // Se è umano, il metodo termina qui e aspetta il click dell'utente.

        } else {
            view.showMessage(currentPlayer.getName() + " Sei stato stackato! Peschi " + state.pendingDrawPenalty);
            forcedToDraw(currentPlayer, state.pendingDrawPenalty);
            state.pendingDrawPenalty = 0;
            state.activeStackType = null;

            endTurn();

        }

    }

    public Card getCurrentCard(){
        return new Card(currentColor, currentCard.getType(), currentCard.getValue());
    }

    /** humanPlayCard() checks if the card is playable and then executes the move.
     * @param chosenCard
     */
    public void humanPlayCard(Card chosenCard) {
        Player human = state.getCurrentPlayer();

        if (!(human instanceof HumanPlayer)) return;

        if (chosenCard.isPlayableOn(currentCard) || chosenCard.getColor() == currentColor) {
            // CONTROLLO NUMBER RUSH
            if (settings.numberRushEnabled && chosenCard.getType() == CardType.NUMBERS) {
                List<Card> sameNumbers = human.getHand().stream()
                        .filter(c -> c.getType() == CardType.NUMBERS && c.getValue() == chosenCard.getValue())
                        .toList();
                if (sameNumbers.size() > 1) {
                    // Li gioca tutti insieme
                    for (Card c : sameNumbers) {
                        state.getMatchStats().decrementPointsTo(human, c);
                        human.removeCard(c);
                        state.discardPile.add(c);
                    }
                    currentCard = sameNumbers.get(sameNumbers.size() - 1);
                    currentColor = currentCard.getColor();
                    view.updateTopCard(currentCard);
                    state.getMatchStats().addMove(human.getName(), "esegue un NUMBER RUSH scartando " + sameNumbers.size() + " carte di valore " + chosenCard.getValue());
                    endTurn();
                    return;
                }
            }
            state.getMatchStats().decrementPointsTo(human,chosenCard);

            executeMove(human, chosenCard);
        } else {
            view.showMessage("Mossa non valida!");
        }
    }

    /** humanDrawCard() checks conditions in drawing from the deck and acts accordingly.
     * The player can only draw once.
     * Forces the human to draw if no card can be currently played.
     * The turn ends if no card can be played even after drawing, otherwise continues.
     */
    public void humanDrawCard() {
        Player human = state.getCurrentPlayer();
        if (state.drawPile.isEmpty()) state.reshuffleDiscardIntoDraw();

        //COntrollo pescaggio
        if (hasDrawnThisTurn) {
            view.showMessage("Hai già pescato! Gioca una carta.");
            return;
        }

        if (state.drawPile.isEmpty()) state.reshuffleDiscardIntoDraw();

        Card drawn = state.drawPile.pop();
        human.receiveCard(drawn);
        // incrementa punti a causa della carta che hai pescato
        state.getMatchStats().incrementPointsTo(human,drawn);

        // avendo preso da terra, la sua condizione di uno si annulla
        resetUnoCondition(human);

        //HO PESCATO
        hasDrawnThisTurn = true;

        view.updatePlayerHand(human.getHand());
        view.showMessage("Hai pescato una carta.");


        //vicolo cieco
        if (!hasPlayableCards(human)) {
            view.showMessage("Nessuna mossa possibile. Turno passato!");
            endTurn(); // Passa in automatico
        } else {
            view.showMessage("Hai pescato. Ora scegli una carta da giocare!");
        }

    }

    /** actually plays the card, and evoke its properties if necessary.
     * It also handles the option to evoke a challenge and play it.
     * In this method, the player can also choose to accuse another player of failing to declare UNO
     * and punish them.
     * @param player
     * @param chosenCard
     */

    public void executeMove(Player player, Card chosenCard) {
        Color colorBeforePlay = currentColor;

        // cancelliamo carta dal mazzo
        player.removeCard(chosenCard);

        // buttiamo carta a terra
        state.discardPile.add(chosenCard);

        //Aggiorno la carta e la faccio ridisegnare
        currentCard = chosenCard;
        currentColor = chosenCard.getColor();
        view.updateTopCard(currentCard);

        //LOG MOSSA
        state.getMatchStats().addMove(player.getName(), "gioca " + chosenCard.getColor() + " " + chosenCard.getType());

        Player nextPlayer = state.getNextPlayer();

        // Regola 7-0
        if (settings.sevenZeroEnabled && chosenCard.getType() == CardType.NUMBERS) {
            if (chosenCard.getValue() == 0) {
                state.getMatchStats().addMove(player.getName(), "attiva la regola dello 0! Tutte le mani ruotano.");
                rotateHands();
            } else if (chosenCard.getValue() == 7) {
                state.getMatchStats().addMove(player.getName(), "attiva la regola del 7! Scambia la mano.");
                if (player instanceof HumanPlayer) {
                    Player target = view.choosePlayerToSwapHands(state.players, player);
                    swapHands(player, target);
                } else {
                    Player target = state.players.get((state.currentPlayerIndex + 1) % state.players.size());
                    swapHands(player, target);
                }
            }
        }

        if (chosenCard.getType() == CardType.DRAW_TWO) {
            if (settings.stackingEnabled) {
                state.pendingDrawPenalty += 2;
                state.activeStackType = CardType.DRAW_TWO;
                // Il giocatore successivo NON salta subito, toccherà a lui gestire il problema
                endTurn();
                return;
            } else {
                // Logica classica UNO
                forcedToDraw(nextPlayer, 2);
                state.nextTurn(); // Salta
            }
        } else if (chosenCard.getType() == CardType.WILD_DRAW) {
            // SCELTA COLORE
            if (player instanceof HumanPlayer) {
                chooseColor(view.chooseWildColor());
            } else {
                chooseColor(botChooseColor(((BotPlayer) nextPlayer))); // Stupid e clever, scelgono a caso, cheeky sceglie uno diverso da quello attuale
            }

            // GESTIONE STACKING PER IL +4
            if (settings.stackingEnabled) {
                state.pendingDrawPenalty += 4;
                state.activeStackType = CardType.WILD_DRAW;
                endTurn();
                return;
            } else {
                // Logica normale con Challenge se lo stacking è disabilitato
                boolean wantsToChallenge = false;
                if (nextPlayer instanceof HumanPlayer) {
                    wantsToChallenge = view.askForChallenge(player.getName(), nextPlayer.getName());
                } else {
                    wantsToChallenge = botChallenge((BotPlayer) player);
                }
                if (wantsToChallenge) {
                    evokeChallenge(nextPlayer, player, colorBeforePlay);
                } else {
                    forcedToDraw(nextPlayer, 4);
                }
                state.nextTurn();
            }
        } else if (chosenCard.getType() == CardType.SKIP) {
            state.nextTurn();
        } else if (chosenCard.getType() == CardType.REVERSE) {
            state.invertClock();

        } else if (chosenCard.getType() == CardType.WILD_JOLLY) {
            // SCELTA COLORE
            if (player instanceof HumanPlayer) {
                chooseColor(view.chooseWildColor());
            } else {
                chooseColor(botChooseColor((BotPlayer) player));
            }
        }

        // Controllo UNO dimenticato
        if (player.getHandSize() == 1 && !player.getHasCalledUno()) {
            boolean busted = false;
            for (Player p : state.players) {
                if (p instanceof BotPlayer && Math.random() > 0.5) {
                    state.getMatchStats().addMove(p.getName(), "ha sgamato " + player.getName() + " che non ha detto UNO!");
                    view.showMessage(p.getName() + " ha contestato l'UNO di " + player.getName() + "!");
                    forcedToDraw(player, 2);
                    busted = true;
                    break;
                }
            }
        }
        if (player instanceof BotPlayer && player.getHandSize() == 1) {
            if (Math.random() > 0.2) {
                player.setHasCalledUno(true);
                view.showMessage(player.getName() + " dichiara UNO!");
                state.getMatchStats().addMove(player.getName(), "dichiara UNO!");
            }
        }

        for (Player p : state.players) {
            if (p.getHandSize() == 1 && !p.getHasCalledUno()) {
                for (Player checker : state.players) {
                    if (checker instanceof BotPlayer && checker != p && Math.random() > 0.5) {
                        state.getMatchStats().addMove(checker.getName(), "ha contestato la mancata dichiarazione di " + p.getName());
                        view.showMessage(checker.getName() + " ha contestato l'UNO di " + p.getName() + "!");
                        forcedToDraw(p, 2);
                        p.setHasCalledUno(true);
                        break;
                    }
                }
            }
        }

        endTurn();
    }

    /** lets the player declare UNO when owning only one card in their hand.
     *  It checks if the claim is legitimate.
     * @param player declarer
     */
    /*public void callUno(Player player){
        if (canCallUno(player)) {
            // se il giocatore può chiamare l'uno (quindi se ha una carta in mano)
            player.setHasCalledUno(true);
            // altrimenti rimane false di default

            // giocatore successivo nel turno successivo può contesta
        }else
            System.out.println("Cazzo fai");
    }*/
    //^^^ Has deprecated, logic moved into handleUnoButtonClick()

    /** checks if a player is eligible to declare UNO.
     * @param player declarer
     * @return true if the player has one card in their hand, false otherwise
     */
    public boolean canCallUno(Player player){
        return player.hasUno();
    }


    /** resets a player's UNO declaration status if they no longer have exactly one card
     * @param player old declarer
     */
    public void resetUnoCondition(Player player){
        if (!player.hasUno())
            player.setHasCalledUno(false);
    }

    /** lets one player (the challenger) accuse another (the challenged) of
     * not declaring UNO when they were supposed to.
     *  If the claim is correct, the challenged is forced to draw 2 cards.
     * @param challenged accused player
     */
    /*public void disputeUnoCall(Player challenged){
        if (challenged.hasUno() && challenged.getHasCalledUno())
            System.out.println(challenged + " ha chiamato correttamente uno");
            // SLAVA: view per mostrare che aveva torto il giocatore chiamante
        else if(challenged.hasUno() && !challenged.getHasCalledUno()) {
            resetUnoCondition(challenged);
            forcedToDraw(challenged, 2);
        }
        // SLAVA: view puniscilo!!!

    }*/
    //^^^^ HAS DEPRECATED, moved into handleUnoButtonClick()

    /** checks if the match is over (according to whether it's a classic match or scoreBased).
     *  If one player has zero cards in their hand, there are two scenarios:
     *  <li>
     *      If the gameMode is ClassicGame, the game ends and the player wins immediately.
     *  </li>
     *  <li>
     *      If the game mode is ScoreBasedGame, it checks if the player has reached the target score
     *      <li>If that's the case, the player wins immediately.</li>
     *      <li>Otherwise, a new match starts and the current player receives the points from
     *      the remaining cards in the other players' hands. It continues until the target score is reached by one player
     *      </li>
     *  </li>
     */
    public void endTurn() {
        if (state.getCurrentPlayer().getHandSize() == 0) {

            if (gameMode.isMatchOver(state)) {
                view.showMessage("È FINITA! Ha vinto " + state.getCurrentPlayer().getName());

                saveStatsForAllPLayers();
                globalStats.registerMatch(state.getMatchStats());
                view.showPostMatchScreen(globalStats, state.getMatchStats()); // Chiamata UI Finale
                return;
            }else{
                // Caso in cui possiamo cadere solo nello ScoredBased,perché magari qualcuno
                // ha vinto l'uno ma non è ancora arrivato al punteggio...
                startGame(); // ricomincia e rimescola
                return;
            }

        }

        state.nextTurn();
        // Passa al prossimo e riavvia il loop
        if (!settings.simulationModeEnabled) {
            startTurn();
        }
    }

    /** Saves the final match statistics for all players.
     * Aggregates points, penalties, and challenges from the current match.
     * The winner receives the total points scored, while losers receive 0 points.
     * All players have their personal match history updated
     */
    public void saveStatsForAllPLayers(){
        int winningScore = state.getMatchStats().getPointsFromAllPlayers();
        Player winner = state.getCurrentPlayer();

        Map<Player,Integer> playerPenalties = state.getMatchStats().getPenaltiesPerPlayer();
        Map<Player,Integer> playerChallenges = state.getMatchStats().getChallengesPerPlayer();

        for(Player p : state.players){
            int numPenalties = playerPenalties.get(p);
            int numChallenges = playerChallenges.get(p);

            if(!p.equals(winner))
                p.registerMatch(false,0,numPenalties,numChallenges);
            else
                p.registerMatch(true,winningScore, numPenalties,numChallenges);
        }
    }

    /** displays the outcome (success or failure) of a UNO declaration by a human
     * in the user interface
     */

    public void handleUnoButtonClick() {
        Player currentPlayer = state.getCurrentPlayer();

        if (currentPlayer.getHandSize() <= 2) {
            currentPlayer.setHasCalledUno(true);
            view.showMessage(currentPlayer.getName() + " ha dichiarato UNO!");
            return;
        }

        boolean caughtSomeone = false;
        for (Player p : state.players) {
            if (p != currentPlayer && p.getHandSize() == 1 && !p.getHasCalledUno()) {
                view.showMessage(currentPlayer.getName() + " ha contestato la mancata dichiarazione di " + p.getName() + "!");
                state.getMatchStats().addMove(currentPlayer.getName(), "ha contestato l'UNO di " + p.getName());
                forcedToDraw(p, 2);
                p.setHasCalledUno(true); // Reset per non penalizzarlo all'infinito
                caughtSomeone = true;
            }
        }

        if (!caughtSomeone) {
            view.showMessage("Nessuno da contestare al momento!");
        }
    }

    public void rotateHands() {
        int dir = state.clockwisePhase ? 1 : -1;
        int size = state.players.size();
        List<List<Card>> allHands = new ArrayList<>();
        for (Player p : state.players) allHands.add(new ArrayList<>(p.getHand()));
        for (int i = 0; i < size; i++) {
            int targetIndex = (i + dir + size) % size;
            state.players.get(targetIndex).getHand().clear();
            state.players.get(targetIndex).getHand().addAll(allHands.get(i));
        }
    }

    public void swapHands(Player p1, Player p2) {
        List<Card> temp = new ArrayList<>(p1.getHand());
        p1.getHand().clear();
        p1.getHand().addAll(p2.getHand());
        p2.getHand().clear();
        p2.getHand().addAll(temp);
    }

    /** execute the turn of a bot according to its personality.
     * The bot attempts to play a valid card.
     * If no card can be played, the bot draws a card from the deck. If the drawn card
     * is playable, the bot plays it immediately. Otherwise, the turn is ended.
     * @param bot
     */
    public void executeBotTurn(BotPlayer bot) {
        Card chosen = bot.BotPlays(getCurrentCard());

        if (chosen != null) {
            executeMove(bot, chosen);
            return;
        }

        // Se arriva qui significa che non aveva carte giocabili
        if (state.drawPile.isEmpty()) state.reshuffleDiscardIntoDraw();
        Card drawn = state.drawPile.pop();
        bot.receiveCard(drawn);

        if (drawn.isPlayableOn(currentCard) || drawn.getColor() == currentColor) {
            executeMove(bot, drawn);
        } else {
            endTurn();
        }
    }

    /** chooseColor() sets given color as the current playable color.
     * @param newColor new current color
     */
    public void chooseColor(Color newColor){
        this.currentColor = newColor;
    }

    /** takes a hand from a player and checks if they have any playable Color card
     * at the moment.
     * @param handToCheck
     * @return true if there is at least one color card to play, false otherwise
     */
    public boolean checkHand(List<Card> handToCheck){
        for(Card card : handToCheck){
            if(card.getColor() == currentCard.getColor())
                return true;
        }
        return false;
    }

    /** can be evoked by a player (the challenger) if they believe
     * another player (challenged) has lied and played the WILD DRAW CARD while having a playable color card in their hand.
     * This is illegal in the game.
     * If the challenger is correct, the challenged has to draw 4 cards.
     * Otherwise, he has to draw himself 6 cards.
     * @param challenger the player who accuses
     * @param challenged the accused
     */
    public void evokeChallenge(Player challenger, Player challenged, Color previousColor){
        List<Card> handToCheck = challenged.getHand();
        if(checkHand(handToCheck, previousColor)) {
            view.showMessage("Challenge Vinto! " + challenged.getName() + " aveva il colore " + previousColor + "!");
            forcedToDraw(challenged, 4);
        } else {
            view.showMessage("Challenge Fallito! " + challenger.getName() + " pesca 6 carte!");
            forcedToDraw(challenger, 6);
        }

        state.getMatchStats().incrementChallenges(challenger);
        state.getMatchStats().incrementChallenges(challenged);
    }

    /** checks if there is at least one playable color card in a player's hand
     * @param handToCheck hand to check
     * @param previousColor
     * @return true if there is, false otherwise
     */
    public boolean checkHand(List<Card> handToCheck, Color previousColor){
        for(Card card : handToCheck){
            if(card.getColor() == previousColor)
                return true;
        }
        return false;
    }

    /** dds a specified amount of cards to the hand of a specific player, often as a result of a penalty.
     * @param player victim
     * @param amountCards number of cards the victim is forced to draw
     */
    public void forcedToDraw(Player player, int amountCards){
        Stack<Card> penaltyStack = state.getTopCards(amountCards);
        for(int i = 0; i < amountCards; i++){
            Card penaltyCard = penaltyStack.get(i);
            player.receiveCard(penaltyCard);
            state.getMatchStats().incrementPointsTo(player,penaltyCard);
        }
        state.getMatchStats().incrementPenalties(player);
        state.getMatchStats().addMove(player.getName(), "ha pescato " + amountCards + " carte di penalità.");
        resetUnoCondition(player);
    }

    //Controlla se ci sono carte giocabili

    /** checks if a player has any playable card in their hand, regardless of whether it's a color card or not.
     * @param player player whose hand is going to be checked
     * @return true if there is at least one (any) playable card, false otherwise
     */
    public boolean hasPlayableCards(Player player) {
        for (Card c : player.getHand()) {
            if (c.isPlayableOn(currentCard) || c.getColor() == currentColor) {
                return true;
            }
        }
        return false;
    }

    /**
     * Motore di simulazione Batch: esegue X partite consecutivamente senza aggiornare la GUI,
     * calcolando solo le statistiche. Gira su un Thread separato.
     */
    private void executeMoveSim(Player player, Card chosenCard) {
        player.removeCard(chosenCard);
        state.discardPile.add(chosenCard);
        currentCard = chosenCard;
        currentColor = chosenCard.getColor();
        Player nextPlayer = state.getNextPlayer();

        state.getMatchStats().addMove(player.getName(), "gioca " + chosenCard.getColor() + " " + chosenCard.getType());

        if (chosenCard.getType() == CardType.DRAW_TWO) {
            if (settings.stackingEnabled) {
                state.pendingDrawPenalty += 2;
                state.activeStackType = CardType.DRAW_TWO;
                return; // Il turno non avanza, tocca gestire lo stack
            } else {
                forcedToDrawSim(nextPlayer, 2);
                state.nextTurn();
            }
        } else if (chosenCard.getType() == CardType.WILD_DRAW) {
            // Il bot sceglie il colore usando la sua logica
            currentColor = ((BotPlayer)player).chooseColor(currentColor);

            // Simula il Challenge tra bot
            BotPlayer victim = (BotPlayer) nextPlayer;
            if (victim.wantsToChallenge()) {
                if (checkHand(player.getHand(), currentColor)) { // Fallo del giocatore
                    forcedToDrawSim(player, 4);
                    state.getMatchStats().addMove(victim.getName(), "ha vinto il challenge +4 contro " + player.getName());
                } else { // Challenge fallito
                    forcedToDrawSim(victim, 6);
                    state.getMatchStats().addMove(victim.getName(), "ha fallito il challenge +4 contro " + player.getName());
                }
            } else {
                forcedToDrawSim(victim, 4); // Nessun challenge
            }
            state.nextTurn();

        } else if(chosenCard.getType() == CardType.SKIP) {
            state.nextTurn(); state.nextTurn();
        } else if(chosenCard.getType() == CardType.REVERSE){
            state.invertClock();
        } else if (chosenCard.getType() == CardType.WILD_JOLLY) {
            currentColor = ((BotPlayer)player).chooseColor(currentColor);
        }

        // Controllo UNO in Sim (I bot si beccano al 50%)
        if (player.getHandSize() == 1 && Math.random() > 0.5) {
            forcedToDrawSim(player, 2);
            state.getMatchStats().addMove(player.getName(), "dimentica di chiamare UNO e viene beccato.");
        }
    }

    /**
     * Motore di simulazione Batch: esegue X partite consecutivamente senza aggiornare la GUI,
     * calcolando solo le statistiche. Gira su un Thread separato.
     */
    public void runSimulation(int numSimulations) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < numSimulations; i++) {
                startGame();

                while (settings.simulationModeEnabled) {
                    startTurn();

                    // Controlliamo se la partita è effettivamente finita (un giocatore ha vinto)
                    if (state.getCurrentPlayer().getHandSize() == 0 && gameMode.isMatchOver(state)) {
                        break; // Esce dal while, salva i dati e passa alla prossima simulazione
                    }
                }
            }
            simulationDurationMs = System.currentTimeMillis() - startTime;

            // Si ricollega alla UI di JavaFX in modo pulito
            javafx.application.Platform.runLater(() -> {
                view.showPostMatchScreen(globalStats, state.getMatchStats());
            });
        }).start();
    }

    // Versioni silenziose per non bloccare/crashare JavaFX
    private void forcedToDrawSim(Player player, int amountCards) {
        Stack<Card> penaltyStack = state.getTopCards(amountCards);
        for(int i = 0; i < amountCards; i++) {
            Card penaltyCard = penaltyStack.get(i);
            player.receiveCard(penaltyCard);
            state.getMatchStats().incrementPointsTo(player, penaltyCard);
        }
        state.getMatchStats().incrementPenalties(player);
        state.getMatchStats().addMove(player.getName(), "ha pescato " + amountCards + " carte di penalità (Simulazione).");
        resetUnoCondition(player);
    }

    /** when it is possible to change color, the bot chooses a specific color based on its personality
     * @param bot
     * @return chosen color
     */
    private Color botChooseColor(BotPlayer bot){
        return bot.chooseColor(currentColor);
    };

    /** bot determines whether it wants to raise a challenge or not based on its personality
     * @param bot
     * @return true if the bot chooses to challenge, false otherwise
     */
    private boolean botChallenge(BotPlayer bot){
        return bot.wantsToChallenge();
    }

}