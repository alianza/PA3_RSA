package sample;

import java.math.BigInteger;

public class Character {
    public char character;
    public BigInteger index;

    public Character(char character, BigInteger index){
        this.character = character;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Character{" +
                "character=" + character +
                ", index=" + index +
                '}';
    }
}
