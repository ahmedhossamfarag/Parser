import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Statement {
    private final ArrayList<Word> words;
    private final Consumer<StatementStream> function;

    public Statement(Consumer<StatementStream> function, Word ...words){
        this.function = function;
        this.words = new ArrayList<>();
        addWords(words);
    }

    public void addWords(Word ...words) {
        this.words.addAll(List.of(words));
    }

    public void  parse(StatementStream stream, String text) throws NotValidStatementException {
        if(text == null){
            throw  new NotValidStatementException();
        }
        text = read(words, stream, text);
        if(text.isBlank()){
            function.accept(stream);
        }
        else{
            throw new NotValidStatementException();
        }
    }

    protected static String read(ArrayList<Word> words, StatementStream stream, String text) throws NotValidStatementException {
        boolean parallel = false;

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);

            if(word.parallel && parallel){
                continue;
            }

            if(word.parallel){
                try {
                    text = word.read(stream, text);
                    parallel = true;
                    continue;
                }catch (NotValidStatementException ex){
                    if(i == words.size() - 1 || !words.get(i).parallel){
                        throw ex;
                    }
                    continue;
                }
            }

            parallel = word.parallel;

            text = word.read(stream, text);
        }

        return text;
    }
}
