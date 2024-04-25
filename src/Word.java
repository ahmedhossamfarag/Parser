import java.security.InvalidParameterException;

public abstract class Word {
    protected final String key;
    protected int min, max;

    protected boolean parallel;

    public Word(String key){
        this(key, 1, 1);
    }

    public Word(String key, int min) {
       this(key, min, min);
    }

    public Word(String key, int min, int max) {
        this.key = key;
        this.min = min;
        this.max = max;
    }

    public void setMin(int min) {
        if(min < 0){
            throw new InvalidParameterException();
        }
        this.min = min;
        this.max = Math.max(this.max, min);
    }

    public void setMax(int max) {
        if(max < 0){
            throw new InvalidParameterException();
        }
        this.max = max;
        this.min = Math.min(this.min, max);
    }

    public  void setBounds(int min, int max){
        if(min < 0 || max < min){
            throw new InvalidParameterException();
        }
        this.min = min;
        this.max = max;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    protected abstract String read(StatementStream stream, String text) throws NotValidStatementException ;

    @Override
    public String toString() {
        return key;
    }
}
