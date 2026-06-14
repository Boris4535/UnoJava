package org.openjfx.model;

import java.util.List;
import java.util.Random;

public class BotPlayer extends Player {

    private BotType personality;
    private Random random = new Random();

    public BotPlayer(BotType personality){
        this.personality = personality;
    }

    public Card BotPlays(Card current){
        List<Card> playable = getHand().stream().filter(c -> c.isPlayableOn(current)).toList();
        if(playable.isEmpty()) return null;
        if(playable.size() == 1) return playable.getFirst();
        return switch(personality){
            case STUPID -> StupidPlay(playable);
            case CLEVER -> CleverPlay(playable);
            case CHEEKY -> null;
        };
    }

    //Picks a random cards, but this way ensures that the last drawn card isn't always chosen
    private Card StupidPlay(List<Card> hand){
        int selected = random.nextInt(hand.size());
        return hand.get(selected);
    }

    private Card CheekyPlay(List<Card> hand, Card current){
        CardType currentType = current.getType();
        // Creates a list based on the playable cards, putting a number based on the card type in the same order
        List<Integer> types = hand.stream().map(BotPlayer::typeToPriority).toList();
        //Return specific card type based on the top card
        if (currentType == CardType.WILD_DRAW || currentType ==CardType.WILD_JOLLY){
            if(types.contains(6)) return hand.get(types.indexOf(6));
            else if (types.contains(5)) return hand.get(types.indexOf(5));
        }
        else if (currentType == CardType.SKIP ){
            if(types.contains(3)) return hand.get(types.indexOf(3));
        }
        else if (currentType == CardType.REVERSE ){
            if(types.contains(2)) return hand.get(types.indexOf(2));
        }
        else if (currentType == CardType.DRAW_TWO ){
            if(types.contains(4)) return hand.get(types.indexOf(4));
        }
        else{
            for(Card c: hand)
                if(c.getColor() != current.getColor() &&
                        (currentType == CardType.NUMBERS && c.getType() == CardType.NUMBERS) &&
                        current.getValue() == c.getValue()) return c;
        }
        //If all else fails, pick a numbered card
        if(types.contains(1)) return hand.get(types.indexOf(1));
        // If that  doesn't work either
        else return hand.getFirst();
    }

    //Picks a card based on a priority list which can later be altered if needed
    private Card CleverPlay(List<Card> hand){Integer[] types = new Integer[hand.size()];
        List<Card> priority = hand.stream().sorted(BotPlayer::CleverCompare).toList();
        return priority.getFirst();

    }

    //Compare() but with card types basically
    private static int CleverCompare(Card c1, Card c2){
        int c1value = typeToPriority(c1);
        int c2value = typeToPriority(c2);

        int diff = c1value - c2value;

        return Integer.compare(diff, 0);
    }

    //Determines the priority value based on type AND is reused for CheekyPlays to determine what type to play
    private static int typeToPriority(Card c){
        return switch(c.getType()){
            case NUMBERS -> 1;
            case SKIP -> 3;
            case REVERSE -> 2;
            case DRAW_TWO -> 4;
            case WILD_JOLLY -> 5;
            case WILD_DRAW -> 6;
        };
    }


}
