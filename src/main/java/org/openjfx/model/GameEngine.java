package org.openjfx.model;


import org.openjfx.controller.GameView;
import org.openjfx.model.GameState;
import org.openjfx.model.Player;

import java.util.List;
import java.util.Stack;

public class GameEngine {

    private GameState state;
    private GameMode gameMode;
    private GameView view;
    private final int MIN_PLAYER = 2;
    private final int MAX_PLAYER = 6;
    private Card currentCard;
    private Color currentColor;
    private boolean hasDrawnThisTurn = false;

    public GameEngine(GameState nState, GameMode nGameMode, GameView view) {
        this.state = nState;
        this.gameMode = nGameMode;
        this.view = view;
    }

    public void dealHand(int cardsPerPlayer) {
        // da implementare
        for (Player player : state.players) {
            Stack<Card> deck = state.getTopCards(cardsPerPlayer);
            player.initiateHand(deck);
        }
    }

    public void prepareDeck() {
        state.shuffle(state.drawPile);
    }

    public void startGame() {

        prepareDeck();
        dealHand(7);
        // distribuisci carte

        currentCard = state.getTopCards(1).pop(); // Prima carta da mettere a terra
        state.discardPile.add(currentCard);    // Aggiungiamo carta a terra
        currentColor = currentCard.getcolor();

        view.updateTopCard(currentCard);
        view.showMessage("CE LA FACCIAMOOOOO");

        startTurn();


    }

    public void startTurn() {
        hasDrawnThisTurn = false;

        Player currentPlayer = state.getCurrentPlayer();
        view.onTurnChanged(currentPlayer);

        if (currentPlayer instanceof HumanPlayer) {
            view.updatePlayerHand(currentPlayer.getHand());

            //se il giocatore è umano, lavora, sennò no
            //intanto si aspetta er click

            view.updatePlayerHand(currentPlayer.getHand());
            view.showMessage("È il tuo turno, " + currentPlayer.name);
        } else {

            view.showMessage(currentPlayer.name + " (Bot) sta calcolando l'entropua");

            executeBotTurn(currentPlayer);
        }
    }



    public void humanPlayCard(Card chosenCard) {
        Player human = state.getCurrentPlayer();

        if (!(human instanceof HumanPlayer)) return;

        if (chosenCard.isPlayableOn(currentCard) || chosenCard.getcolor() == currentColor) {
            executeMove(human, chosenCard);
        } else {
            view.showMessage("Mossa non valida!");
        }
    }

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


    public void executeMove(Player player, Card chosenCard){

        // cancelliamo carta dal mazzo
        player.removeCard(chosenCard);

        // buttiamo carta a terra
        state.discardPile.add(chosenCard);

        //Aggiorno la carta e la faccio ridisegnare
        currentCard = chosenCard;
        currentColor = chosenCard.getcolor();
        view.updateTopCard(currentCard);

        Player nextPlayer = state.getNextPlayer();
        if(chosenCard.getType() == CardType.DRAW_TWO) {
            forcedToDraw(nextPlayer,2);
            /*  ATTENZIONE : il giocatore nextPlayer salta il turno */
            state.nextTurn();
            endTurn();
        }else if (chosenCard.getType() == CardType.WILD_DRAW){
            chooseColor(Color.BLUE); // placeholder value
            // SLAVA: mi serve un click qui, se il giocatore successivo clicca sulla challenge
            // if
                evokeChallenge(nextPlayer, state.getCurrentPlayer());
            // else
            forcedToDraw(nextPlayer,4);
        }else if(chosenCard.getType() == CardType.SKIP) {
            state.nextTurn();
        }else if(chosenCard.getType() == CardType.REVERSE){
            state.invertClock();
        }else if(chosenCard.getType() == CardType.WILD_JOLLY){
            // SLAVA: mi serve il click qui
            chooseColor(Color.BLUE); // placeholder value
        }

        endTurn();

    }

    public void endTurn() {
        //Controllo se ci sono vincitori
        if (gameMode.isMatchOver(state.getCurrentPlayer())) {
            view.showMessage("È FINITA! Ha vinto " + state.getCurrentPlayer().name);
            return;
        }

        // Passa al prossimo e riavvia il loop
        state.nextTurn();
        startTurn();
    }

    public void executeBotTurn(Player bot) {
        // Anche mr Bot ora cerca le carte!
        for (Card c : bot.getHand()) {
            if (c.isPlayableOn(currentCard) || c.getcolor() == currentColor) {
                executeMove(bot, c);
                return; // Ha giocato, fine del suo turno
            }
        }

        // Se non ha trovato niente, pesca una carta
        if (state.drawPile.isEmpty()) state.reshuffleDiscardIntoDraw();
        Card drawn = state.drawPile.pop();
        bot.receiveCard(drawn);

        // Controlla se la carta appena pescata (o le altre) sono giocabili ora
        if (drawn.isPlayableOn(currentCard) || drawn.getcolor() == currentColor) {
            executeMove(bot, drawn);
        } else {
            // Niente da fare, passa il turno
            endTurn();
        }
    }

    public void chooseColor(Color newColor){
        this.currentColor = newColor;
    }

    /** checkHand() takes a hand from a player and checks if they have any playable Color card
     * at the moment.
     * @param handToCheck
     * @return
     */
    public boolean checkHand(List<Card> handToCheck){
        for(Card card : handToCheck){
            if(card.getcolor() == currentCard.getcolor())
                return true;
        }
        return false;
    }

    /** evokeChallenge() can be evoked by the nextPlayer (challenger) if they believe
     * the current player (challenged) has lied and played the WILD DRAW CARD while having a playable color card in their hand.
     * This is illegal in the game.
     * If the challenger is correct, the challenged has to draw 4 cards.
     * Otherwise, he has to draw himself 6 cards.
     * @param challenger
     * @param challenged
     */
    public void evokeChallenge(Player challenger, Player challenged){
        List<Card> handToCheck = challenged.getHand();
        if(checkHand(handToCheck))
            forcedToDraw(challenged,4);
        else
            forcedToDraw(challenger,6);
    }

    /** forcedToDraw() adds a specified amount of cards to the hand of a specific player
     * @param player
     * @param amountCards
     */
    public void forcedToDraw(Player player, int amountCards){
        Stack<Card> penaltyStack = state.getTopCards(amountCards);
        for(int i = 0; i < amountCards; i++){
            player.receiveCard(penaltyStack.get(i));
        }
    }

    //Controlla se ci sono carte giocabili
    public boolean hasPlayableCards(Player player) {
        for (Card c : player.getHand()) {
            if (c.isPlayableOn(currentCard) || c.getcolor() == currentColor) {
                return true;
            }
        }
        return false;
    }
}