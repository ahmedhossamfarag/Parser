import java.util.ArrayList;
import java.util.List;

public class CompositeWord extends Word{
    private final ArrayList<Word> words;

    public CompositeWord(String key ,Word ...words) {
        this(key, 1, 1, words);
    }

    public CompositeWord(String key ,int min, Word ...words) {
        this(key, min, min, words);
    }

    public CompositeWord(String key ,int min, int max, Word ...words) {
        super(key, min, max);
        this.words = new ArrayList<>();
        addWords(words);
    }

    public void addWords(Word ...words) {
        this.words.addAll(List.of(words));
    }

    @Override
    protected String read(StatementStream stream, String text) throws NotValidStatementException {
        if(key.isBlank()){
            for (int i = 0; i < min; i++) {
                text = Statement.read(words, stream, text);
            }
        }else{
            for (int i = 0; i < min; i++) {
                StatementStream tempStream = new StatementStream();
                text = Statement.read(words, tempStream, text);
                stream.add(key, tempStream);
            }
        }

        for (int i = min; i < max; i++) {
            String temp = text;
            StatementStream tempStream = new StatementStream();
            try {
                temp = Statement.read(words, tempStream, temp);
            }catch (NotValidStatementException ignored){
                break;
            }
            if(text.equals(temp)){
                break;
            }
            text = temp;
            if(key.isBlank())
                stream.pairs.addAll(tempStream.pairs);
            else
                stream.add(key, tempStream);
        }
        return text;
    }
}
