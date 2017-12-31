package github.scarsz.examinator.util;

public class ReactionUtil {

    public static String getEmojiFromLetter(Character character) {
        switch (character.toString().toUpperCase().toCharArray()[0]) {
            case 'A': return "\uD83C\uDDE6";
            case 'B': return "\uD83C\uDDE7";
            case 'C': return "\uD83C\uDDE8";
            case 'D': return "\uD83C\uDDE9";
            case 'E': return "\uD83C\uDDEA";
            case 'F': return "\uD83C\uDDEB";
            case 'G': return "\uD83C\uDDEC";
            case 'H': return "\uD83C\uDDED";
            case 'I': return "\uD83C\uDDEE";
            case 'J': return "\uD83C\uDDEF";
            case 'K': return "\uD83C\uDDF0";
            case 'L': return "\uD83C\uDDF1";
            case 'M': return "\uD83C\uDDF2";
            case 'N': return "\uD83C\uDDF3";
            case 'O': return "\uD83C\uDDF4";
            case 'P': return "\uD83C\uDDF5";
            case 'Q': return "\uD83C\uDDF6";
            case 'R': return "\uD83C\uDDF7";
            case 'S': return "\uD83C\uDDF8";
            case 'T': return "\uD83C\uDDF9";
            case 'U': return "\uD83C\uDDFA";
            case 'V': return "\uD83C\uDDFB";
            case 'W': return "\uD83C\uDDFC";
            case 'X': return "\uD83C\uDDFD";
            case 'Y': return "\uD83C\uDDFE";
            case 'Z': return "\uD83C\uDDFF";
        }
        return null;
    }

    public static char getLetterFromEmoji(String emoji) {
        switch (emoji) {
            case "\uD83C\uDDE6": return 'A';
            case "\uD83C\uDDE7": return 'B';
            case "\uD83C\uDDE8": return 'C';
            case "\uD83C\uDDE9": return 'D';
            case "\uD83C\uDDEA": return 'E';
            case "\uD83C\uDDEB": return 'F';
            case "\uD83C\uDDEC": return 'G';
            case "\uD83C\uDDED": return 'H';
            case "\uD83C\uDDEE": return 'I';
            case "\uD83C\uDDEF": return 'J';
            case "\uD83C\uDDF0": return 'K';
            case "\uD83C\uDDF1": return 'L';
            case "\uD83C\uDDF2": return 'M';
            case "\uD83C\uDDF3": return 'N';
            case "\uD83C\uDDF4": return 'O';
            case "\uD83C\uDDF5": return 'P';
            case "\uD83C\uDDF6": return 'Q';
            case "\uD83C\uDDF7": return 'R';
            case "\uD83C\uDDF8": return 'S';
            case "\uD83C\uDDF9": return 'T';
            case "\uD83C\uDDFA": return 'U';
            case "\uD83C\uDDFB": return 'V';
            case "\uD83C\uDDFC": return 'W';
            case "\uD83C\uDDFD": return 'X';
            case "\uD83C\uDDFE": return 'Y';
            case "\uD83C\uDDFF": return 'Z';
        }
        return 0;
    }

}
