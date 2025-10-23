
package minipar.interpreter;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterInputTest {

    private Interpreter interpretar(String codigo) {
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseProgram();

        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        Interpreter interpreter = new Interpreter();
        interpreter.setupFunctionEvaluation();
        interpreter.execute(ast);
        return interpreter;
    }

    @Test
    void testInputSimples() {
        String codigo = """
            programa_minipar
            SEQ
            x = input()
            print(x)
        """;

        System.setIn(new ByteArrayInputStream("42\n".getBytes()));
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));

        Interpreter interpreter = interpretar(codigo);

        String output = saida.toString().trim();
        System.out.println(output);
        assertTrue(output.contains("42"));
        assertEquals(42.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testInputUsadoEmExpressao() {
        String codigo = """
            programa_minipar
            SEQ
            a = input()
            b = a + 5
            print(b)
        """;

        System.setIn(new ByteArrayInputStream("7\n".getBytes()));
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));

        Interpreter interpreter = interpretar(codigo);
        String output = saida.toString().trim();
        assertTrue(output.contains("12"));
        assertEquals(12.0, interpreter.getMemory().get("b"));
    }

    @Test
    void testDoisInputs() {
        String codigo = """
            programa_minipar
            SEQ
            a = input()
            b = input()
            soma = a + b
            print(soma)
        """;

        ByteArrayInputStream simulatedInput = new ByteArrayInputStream("3\n4\n".getBytes());
        System.setIn(simulatedInput);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));

        Interpreter interpreter = interpretar(codigo);
        String output = saida.toString().trim();
        assertTrue(output.contains("7"));
        assertEquals(7.0, interpreter.getMemory().get("soma"));
    }
}
