import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class StatementStream {
    protected final ArrayList<KeyValuePair> pairs;

    public StatementStream(){
        pairs = new ArrayList<>();
    }

    protected void add(String key, Object value){
        pairs.add(new KeyValuePair(key, value));
    }

    /**
     * for keywords: key = keyword itself, value = no of occurrences as int
     * for inputWords: key = key defined for it, value = its value in the statement as String
     * for compositeWords: key = if not direct, compositeWord key, value = another StatementStream over it
     * @return Iterator over key-value pairs
     *
     */
    public Iterator<KeyValuePair> iterator(){
        return pairs.iterator();
    }

    @Override
    public String toString() {
        return String.join("\n", pairs.stream().map(Objects::toString).toArray(String[]::new));
    }
}
