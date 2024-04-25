import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class StatementBuilder {
    private CompositeWord overAll;
    private boolean ignoreCase;

    public StatementBuilder(){
        createStatement();
    }


    /**
     *
     * @param consumer: a function to apply when statement is valid
     * @param s: text to convert into Statement object
     *         [keyword] keyword can't contain [ or ];
     *         {inputWord} or {inputWord type} or {inputWord type chars} input_word: a key name for variable input, contains only A~Z a~z 0~9 _,
     *         type can be D (DIGIT), A (ALPHA), T (TEXT), S (SPECIFIC), E (EXCEPT),
     *         chars: chars to add to characters set;
     *         [[keyword][keyword]{inputWord}] for composite words;
     *         | after open brackets for parallelism;
     *         (n) or (min,max) for range , (+) for >=1 , (?) for 0|1 , (*) for >=0;
     *         Ex:
     *         "[create][ ][table][ ]{tableName}[(][{column}[ ]{type}[,]](+){column}[ ]{type}[)]"
     * @return Statement object
     * @throws NotValidStatementException: when text is not recognizable
     */
    public Statement of(Consumer<StatementStream> consumer, String s, boolean ignoreCase) throws NotValidStatementException {
        this.ignoreCase = ignoreCase;
        Statement output = new Statement(consumer);
        StatementStream stream = new StatementStream();
        Statement statement = new Statement(st -> parse(st, output), overAll);
        statement.parse(stream, s);
        return output;
    }

    private void createStatement(){
        KeyWord bracketOpen = new KeyWord("["), bracketClosed = new KeyWord("]");
        KeyWord bracketInOpen = new KeyWord("{"), bracketInClosed = new KeyWord("}");
        KeyWord bracketNOpen = new KeyWord("("), bracketNClosed = new KeyWord(")");
        KeyWord parallel = new KeyWord("|", 0, 1);

        InWard min = new InWard("min");
        min.setType(WordType.DIGIT);
        InWard max = new InWard("max");
        max.setType(WordType.DIGIT);
        CompositeWord minMax = new CompositeWord("min_max", bracketNOpen, min, new KeyWord(","), max, bracketNClosed);
        minMax.parallel = true;

        InWard num = new InWard("num");
        num.setType(WordType.DIGIT);
        num.addChars('+', '?', '*');
        CompositeWord numComposite = new CompositeWord("num_composite", bracketNOpen, num, bracketNClosed);
        numComposite.parallel = true;

        CompositeWord numWord = new CompositeWord("bounds_composite" ,0, 1, numComposite, minMax);

        InWard keyWardName = new InWard("key_ward");
        keyWardName.setType(WordType.EXCEPT);
        keyWardName.addChars('[',']');

        InWard inWardName = new InWard("in_ward");
        inWardName.addChars('_');
        KeyWord space = new KeyWord(" ");
        CompositeWord type = new CompositeWord("", 0, 1, space, new InWard("type"));
        InWard inChars = new InWard("chars");
        inChars.setType(WordType.EXCEPT);
        inChars.addChars('{','}', ' ');
        CompositeWord chars = new CompositeWord("", 0, 1, space, inChars);
        
        CompositeWord keyWord = new CompositeWord("kw_composite" ,0, Integer.MAX_VALUE, bracketOpen, parallel, keyWardName, bracketClosed, numWord);

        CompositeWord inWard = new CompositeWord("in_composite" ,0, Integer.MAX_VALUE, bracketInOpen, parallel, inWardName, type, chars, bracketInClosed, numWord);

        CompositeWord compositeWord = new CompositeWord("composite" ,0, Integer.MAX_VALUE);

        CompositeWord overAll = new CompositeWord("overall" ,0, Integer.MAX_VALUE, keyWord, inWard, compositeWord);

        compositeWord.addWords(bracketOpen, parallel, overAll, bracketClosed, numWord);

        this.overAll = overAll;
    }

    private void parse(StatementStream stream, Statement statement){
        statement.addWords(wArrayCreate(stream).toArray(Word[]::new));
    }

    private ArrayList<Word> wArrayCreate(StatementStream stream){
        ArrayList<Word> words = new ArrayList<>();
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "kw_composite":
                    words.add(kwCreate((StatementStream) kv.value()));
                    break;
                case "in_composite":
                    words.add(inWardCreate((StatementStream) kv.value()));
                    break;
                case "composite":
                    words.add(compositeCreate((StatementStream) kv.value()));
                    break;
                case "overall":
                    words.addAll(wArrayCreate((StatementStream) kv.value()));
                    break;
            }
        }
        return words;
    }

    private KeyWord kwCreate(StatementStream stream){
        KeyWord keyWord = null;
        boolean parallel = false;
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "|":
                    parallel = (int) kv.value() > 0;
                    break;
                case "key_ward":
                    keyWord = new KeyWord(kv.value().toString());
                    keyWord.setParallel(parallel);
                    keyWord.setIgnoreCase(ignoreCase);
                    break;
                case "bounds_composite":
                    boundsCreate(keyWord, (StatementStream) kv.value());
                    break;
            }
        }
        return keyWord;
    }

    private Word inWardCreate(StatementStream stream) {
        InWard inWard = null;
        boolean parallel = false;
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "|":
                    parallel = (int) kv.value() > 0;
                    break;
                case "in_ward":
                    inWard = new InWard(kv.value().toString());
                    inWard.setParallel(parallel);
                    break;
                case "type":
                    WordType type = switch (kv.value().toString()) {
                        case "D" -> WordType.DIGIT;
                        case "T" -> WordType.TEXT;
                        case "S" -> WordType.SPECIFIC;
                        case "E" -> WordType.EXCEPT;
                        default -> WordType.ALPHA_DIGIT;
                    };
                    if(inWard != null){
                        inWard.setType(type);
                    }
                    break;
                case "chars":
                    if(inWard != null){
                        inWard.addChars(kv.value().toString().toCharArray());
                    }
                    break;
                case "bounds_composite":
                    boundsCreate(inWard, (StatementStream) kv.value());
                    break;
            }
        }
        return inWard;
    }

    private Word compositeCreate(StatementStream stream) {
        CompositeWord compositeWord = new CompositeWord("");
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "|":
                    compositeWord.setParallel((int) kv.value() > 0);
                    break;
                case "overall":
                    compositeWord.addWords(wArrayCreate((StatementStream) kv.value()).toArray(Word[]::new));
                    break;
                case "bounds_composite":
                    boundsCreate(compositeWord, (StatementStream) kv.value());
                    break;
            }
        }
        return compositeWord;
    }

    private void boundsCreate(Word word, StatementStream stream){
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "num_composite":
                    stream = (StatementStream) kv.value();
                    iterator = stream.iterator();
                    while (iterator.hasNext()){
                        kv = iterator.next();
                        if(kv.key().equals("num")){
                            switch (kv.value().toString()){
                                case "+":
                                    word.setBounds(1, Integer.MAX_VALUE);
                                    break;
                                case "?":
                                    word.setBounds(0, 1);
                                    break;
                                case "*":
                                    word.setBounds(0, Integer.MAX_VALUE);
                                    break;
                                default:
                                    word.setMin(Integer.parseInt(kv.value().toString()));
                                    break;
                            }
                            return;
                        }
                        }
                    return;
                case "min_max":
                    stream = (StatementStream) kv.value();
                    iterator = stream.iterator();
                    while (iterator.hasNext()){
                        kv = iterator.next();
                        switch (kv.key()){
                            case "min":
                                word.setMin(Integer.parseInt(kv.value().toString()));
                                break;
                            case "max":
                                word.setMax(Integer.parseInt(kv.value().toString()));
                                break;
                        }
                    }
                    return;
            }
        }
    }
}
