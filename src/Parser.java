import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final ArrayList<Statement> statements;

    public Parser(Statement ...statements){
        this.statements = new ArrayList<>();
        addStatements(statements);
    }

    public void addStatements(Statement ...statements){
        this.statements.addAll(List.of(statements));
    }

    public void parse(String text) throws NotValidStatementException {
        for (Statement st: statements) {
            StatementStream stream = new StatementStream();
            try {
                st.parse(stream, text);
                return;
            }catch(NotValidStatementException ignored){
            }
        }
        throw new NotValidStatementException();
    }
}
