package org.openjfx.model;

public class MainTest {
    public static void main(String[] args) {
        Card c = new Card(Color.BLUE,CardType.NUMBERS,2);
        System.out.println(c.getValue());
    }
}
