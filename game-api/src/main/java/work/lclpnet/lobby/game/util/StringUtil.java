package work.lclpnet.lobby.game.util;

import java.util.Random;

public class StringUtil {

    public static String getRandomString(String alphabet, int length, Random random) {
        char[] chars = new char[length];
        int alphabetLength = alphabet.length();

        for (int i = 0; i < length; i++) {
            chars[i] = alphabet.charAt(random.nextInt(alphabetLength));
        }

        return String.valueOf(chars);
    }
}
