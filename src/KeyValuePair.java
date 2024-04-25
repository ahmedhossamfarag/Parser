public record KeyValuePair(String key, Object value) {
    @Override
    public String toString() {
        return key + " : " + value;
    }
}
