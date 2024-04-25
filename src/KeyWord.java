public class KeyWord extends Word {

    private boolean ignoreCase;

    public KeyWord(String key) {
        super(key);
    }

    public KeyWord(String key, int min) {
        super(key, min);
    }

    public KeyWord(String key, int min, int max) {
        super(key, min, max);
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    protected String read(StatementStream stream, String text) throws NotValidStatementException {
        for (int i = 0; i < min; i++) {
            if(text.regionMatches(ignoreCase, 0, key, 0, key.length())){
                text = text.substring(key.length());
            }
            else {
                throw new NotValidStatementException();
            }
        }
        int n = min;
        for (int i = min; i < max; i++) {
            if(text.regionMatches(ignoreCase, 0, key, 0, key.length())){
                text = text.substring(key.length());
                n++;
            }
            else {
                break;
            }
        }
        stream.add(key, n);
        return text;
    }
}
