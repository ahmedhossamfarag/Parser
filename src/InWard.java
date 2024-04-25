import java.util.ArrayList;

public class InWard extends Word{
    private final ArrayList<Character> characters;

    private WordType type;

    public InWard(String key) {
        this(key, 1, Integer.MAX_VALUE);
    }

    public InWard(String key, int min) {
        this(key, min, Integer.MAX_VALUE);
    }

    public InWard(String key, int min, int max) {
        super(key, min, max);
        characters = new ArrayList<>();
        type = WordType.ALPHA_DIGIT;
    }


    public void setType(WordType type) {
        this.type = type;
    }

    public void addChars(char ...chars){
        for (char c : chars){
            if(!characters.contains(c)){
                characters.add(c);
            }
        }
    }

    @Override
    protected String read(StatementStream stream, String text) throws NotValidStatementException {
        if(text.length() < min){
            throw new NotValidStatementException();
        }
        for (int i = 0; i < min; i++) {
            if(!match(text.charAt(i))){
                throw new NotValidStatementException();
            }
        }
        StringBuilder out = new StringBuilder(text.substring(0, min));
        text = text.substring(min);
        for (int i = min; i < max; i++) {
            if(!text.isEmpty() && match(text.charAt(0))){
                out.append(text.charAt(0));
                text = text.substring(1);
            }
            else {
                break;
            }
        }
        stream.add(key, out.toString());
        return text;
    }

    private boolean match(char c){
        return ((type == WordType.ALPHA || type == WordType.ALPHA_DIGIT) && Character.isAlphabetic(c)) ||
                ((type == WordType.DIGIT || type == WordType.ALPHA_DIGIT) && Character.isDigit(c)) ||
                (type == WordType.TEXT && c != '\"') ||
                (type == WordType.EXCEPT && !characters.contains(c)) ||
                (type != WordType.EXCEPT && characters.contains(c));
    }
}
