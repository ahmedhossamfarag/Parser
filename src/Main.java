import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws NotValidStatementException {
        StatementBuilder builder = new StatementBuilder();
        Statement statement = builder.of(Main::execute, "[create][ ][table][ ]{table_name AD _}[(][{column AD _}[ ]{type}[,]](+){column AD _}[ ]{type}[)]", true);

        StatementStream stream = new StatementStream();
        statement.parse(stream, "Create table person_(id_ int,age int,name str)");
    }

    public static void execute(StatementStream stream){
        Iterator<KeyValuePair> iterator = stream.iterator();
        while (iterator.hasNext()){
            KeyValuePair kv = iterator.next();
            switch (kv.key()){
                case "table_name":
                    System.out.println("Table" + " " + kv.value());
                    break;
                case "column":
                    System.out.println("Column" + " " + kv.value());
                    break;
                case "type":
                    System.out.println("Type" + " " + kv.value());
                    break;
            }
        }
    }
}