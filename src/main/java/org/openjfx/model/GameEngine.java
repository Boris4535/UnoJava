package org.openjfx.model;

import org.openjfx.model.GameState;
import org.openjfx.model.Player;

import java.util.List;
import java.util.Stack;

public class GameEngine {

    private GameState state;
    private GameMode gameMode;
    private final int MIN_PLAYER = 2;
    private final int MAX_PLAYER = 6;
    private Card currentCard;
    private Color currentColor;

    public GameEngine(GameState nState, GameMode nGameMode){
        this.state = nState;
        this.gameMode = nGameMode;
    }

    public void dealHand(int cardsPerPlayer){
        // da implementare
        for (Player player : state.players){
            Stack<Card> deck = state.getTopCards(cardsPerPlayer);
            player.initiateHand(deck);
        }
    }

    public void prepareDeck(){
        state.shuffle(state.drawPile);
    }

    public void startGame(){

        prepareDeck();
        dealHand(7);
        // distribuisci carte

        currentCard = state.getTopCards(1).pop(); // Prima carta da mettere a terra
        state.discardPile.add(currentCard);    // Aggiungiamo carta a terra

        Player currentPlayer = null;
        do{
            // prendi il primo giocatore
            currentPlayer = state.getCurrentPlayer();

            if(currentPlayer instanceof HumanPlayer){
                humanGame(currentPlayer);

            }else if(currentPlayer instanceof  Player){
                /* IN REALTA' SAREBBE BotPlayer, NON Player*/
            }

            // sposta il turno al prossimo giocatore
            state.nextTurn();

        }while(!gameMode.isMatchOver(currentPlayer));


    }

    public void humanGame(Player human){
        // si vedono carte in mano

        // e carta a terra

        List<Card> hand = human.getHand();

        // SLAVA
        // scegli una carta dal mazzo dalll clickkkk
        Card chosenCard = hand.get(0); // (0) per prova.. placeholder value

        // ci serve la seconda condizione in caso il colore è stato cambiato tramite carta WILD JOLLY
        if (chosenCard.isPlayableOn(currentCard) || chosenCard.getcolor() == currentColor)
            executeMove(human,chosenCard);

    }

    public void executeMove(Player player, Card chosenCard){

        // cancelliamo carta dal mazzo
        player.removeCard(chosenCard);

        // buttiamo carta a terra
        state.discardPile.add(chosenCard);

        Player nextPlayer = state.getNextPlayer();
        if(chosenCard.getType() == CardType.DRAW_TWO) {
            forcedToDraw(nextPlayer,2);
            /*  ATTENZIONE : il giocatore nextPlayer salta il turno */
            state.nextTurn();
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


}
