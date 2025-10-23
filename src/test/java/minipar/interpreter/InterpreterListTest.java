package minipar.interpreter;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterListTest {

    private Interpreter interpreter;

    private ASTNode interpretar(String codigo) {
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseProgram();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(ast);
        interpreter = new Interpreter();
        interpreter.execute(ast);
        return ast;
    }

    @Test
    public void testCriacaoListaEAcesso() {
        String codigo = """
            programa_minipar
            SEQ
            x = [1, 2, 3]
            print(x[0])
            print(x[2])
            """;
        interpretar(codigo);

        Object x = interpreter.getMemory().get("x");
        assertNotNull(x);
        assertTrue(x instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<Double> lista = (List<Double>) x;

        assertEquals(1.0, lista.get(0));
        assertEquals(3.0, lista.get(2));
    }

    @Test
    public void testAtribuicaoPorIndice() {
        String codigo = """
            programa_minipar
            SEQ
            x = [10, 20, 30]
            x[1] = 99
            """;
        interpretar(codigo);
        Object x = interpreter.getMemory().get("x");

        assertNotNull(x);
        assertInstanceOf(List.class, x);

        List<?> lista = (List<?>) x;

        assertEquals(10.0, lista.get(0));
        assertEquals(99.0, lista.get(1)); // valor alterado
        assertEquals(30.0, lista.get(2));
    }

    @Test
    public void testListaComExpressao() {
        String codigo = """
            programa_minipar
            SEQ
            a = 5
            b = 10
            x = [a, b, a + b]
            """;
        interpretar(codigo);
        Object x = interpreter.getMemory().get("x");

        assertNotNull(x);
        assertTrue(x instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<Double> lista = (List<Double>) x;

        assertEquals(3.0, lista.size());
        assertEquals(5.0, lista.get(0));
        assertEquals(10.0, lista.get(1));
        assertEquals(15.0, lista.get(2));
    }

    @Test
    public void testErroIndiceInvalido() {
        String codigo = """
            programa_minipar
            SEQ
            x = [1, 2, 3]
            y = x[5]
            """;
        RuntimeException  e = assertThrows(RuntimeException.class, () -> interpretar(codigo));
        assertTrue(e.getMessage().contains("Expressão inválida: Indexacao"));
    }

    @Test
    public void testErroAtribuicaoIndiceInvalido() {
        String codigo = """
            programa_minipar
            SEQ
            x = [1, 2, 3]
            x[5] = 10
            """;
        RuntimeException e = assertThrows(RuntimeException.class, () -> interpretar(codigo));
        assertTrue(e.getMessage().toLowerCase().contains("índice fora dos limites"));
    }
}
