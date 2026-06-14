package org.openjfx.model;

import org.openjfx.controller.GameView;

import java.util.List;
import java.util.Map;
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

        globalStats = GameStats.getInstance();
    }

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

        //state.matchStats = new MatchStats(state.players);

        state.getMatchStats().incrementRounds();

        prepareDeck();
        dealHand(7);
        // distribuisci carte

        currentCard = state.getTopCards(1).pop(); // Prima carta da mettere a terra
        state.discardPile.add(currentCard);    // Aggiungiamo carta a terra
        currentColor = currentCard.getColor();

        view.updateTopCard(currentCard);
        view.showMessage("CE LA FACCIAMOOOOO");

        startTurn();


    }

    /** lets the player starts their turn, whether they are a human player or a bot
     */
    public void startTurn()
    {
        state.getMatchStats().incrementTurns();
        hasDrawnThisTurn = false;
        Player currentPlayer = state.getCurrentPlayer();

        //Controllo se c'è lo stacking all'inizio

        view.onTurnChanged(currentPlayer);

        if (state.pendingDrawPenalty > 0) {
            handleStackingPhase(currentPlayer);
            return; // Fermiamo l'esecuzione normale del turno
        }

        if (currentPlayer instanceof HumanPlayer) {
            view.updatePlayerHand(currentPlayer.getHand());

            //se il giocatore è umano, lavora, sennò no
            //intanto si aspetta er click

            view.updatePlayerHand(currentPlayer.getHand());
            view.showMessage("È il tuo turno, " + currentPlayer.getName());
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
        boolean canDefend = false;

        for (Card c : currentPlayer.getHand()){
            if (c.getType() == state.activeStackType){
                canDefend = true;
                break;
            }
        }

        if(canDefend) {
            view.showMessage(currentPlayer.getName() + "C'è uno stack");
        }else{
            view.showMessage(currentPlayer.getName() + "Sei stato tutto stackkato");

            state.pendingDrawPenalty =0;
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

    public void executeMove(Player player, Card chosenCard){
        Color colorBeforePlay = currentColor;

        // cancelliamo carta dal mazzo
        player.removeCard(chosenCard);

        // buttiamo carta a terra
        state.discardPile.add(chosenCard);

        //Aggiorno la carta e la faccio ridisegnare
        currentCard = chosenCard;
        currentColor = chosenCard.getColor();
        view.updateTopCard(currentCard);

        Player nextPlayer = state.getNextPlayer();
        if (chosenCard.getType() == CardType.DRAW_TWO) {
            if (settings.stackingEnabled) {
                state.pendingDrawPenalty += 2;
                state.activeStackType = CardType.DRAW_TWO;
            // Il giocatore successivo NON salta subito, toccherà a lui gestire il problema
                state.nextTurn();
                startTurn();
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
                chooseColor(Color.RED); // Per ora il bot sceglie rosso fisso
            }
            boolean wantsToChallenge = false;
            if (nextPlayer instanceof HumanPlayer) {
                wantsToChallenge = view.askForChallenge(player.getName(), nextPlayer.getName());
            } else {
                // Per ora i Bot non contestano mai. Lo faranno nella versione definitiva.
                wantsToChallenge = false;
            }

            if (wantsToChallenge) {
                evokeChallenge(player, nextPlayer, colorBeforePlay); // player=chi ha lanciato, nextPlayer=chi subisce
            } else {
                forcedToDraw(nextPlayer, 4);
            }
            state.nextTurn();
        }else if(chosenCard.getType() == CardType.SKIP) {
            state.nextTurn();
            state.nextTurn(); //Real skip qui
        }else if(chosenCard.getType() == CardType.REVERSE){
            state.invertClock();

        } else if (chosenCard.getType() == CardType.WILD_JOLLY) {
            // SCELTA COLORE
            if (player instanceof HumanPlayer) {
                chooseColor(view.chooseWildColor());
            } else {
                chooseColor(Color.RED); // Bot sceglie rosso
            }
        }

        // SE SCHIACCIA IL PULSANTE UNO
        // SLAVA serve er click -> chiama funzione
        /* callUno(player) */

        // può anche contestare la mancata chiamata di uno
        // SLAVA ->
        /* disputeUnoCall(Player giocatoreDaIncolpare) */

        endTurn();
    }

    /** lets the player declare UNO when owning only one card in their hand.
     *  It checks if the claim is legitimate.
     * @param player declarer
     */
    public void callUno(Player player){
        if (canCallUno(player)) {
            // se il giocatore può chiamare l'uno (quindi se ha una carta in mano)
            player.setHasCalledUno(true);
            // altrimenti rimane false di default

            // giocatore successivo nel turno successivo può contesta
        }else
            System.out.println("Cazzo fai");
    }

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
    public void disputeUnoCall(Player challenged){
        if (challenged.hasUno() && challenged.getHasCalledUno())
            System.out.println(challenged + " ha chiamato correttamente uno");
            // SLAVA: view per mostrare che aveva torto il giocatore chiamante
        else if(challenged.hasUno() && !challenged.getHasCalledUno()) {
            resetUnoCondition(challenged);
            forcedToDraw(challenged, 2);
        }
            // SLAVA: view puniscilo!!!

    }

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
                return;
            }else{
                // Caso in cui possiamo cadere solo nello ScoredBased,perché magari qualcuno
                // ha vinto l'uno ma non è ancora arrivato al punteggio...
                startGame(); // ricomincia e rimescola
                return;
            }

        }
        // Passa al prossimo e riavvia il loop
        state.nextTurn();
        startTurn();
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
    public void humanCallUno() {
        Player human = state.getCurrentPlayer();

        if (human instanceof HumanPlayer) {
            callUno(human);

            if (human.getHasCalledUno()) {
                view.showMessage(human.getName() + " UNO");
            } else {
                view.showMessage("NO UNO");
            }
        }
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

}