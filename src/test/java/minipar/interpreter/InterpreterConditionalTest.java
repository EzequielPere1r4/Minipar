package minipar.interpreter;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterConditionalTest {

    private Interpreter interpretar(String codigo) {
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseProgram();

        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        Interpreter interpreter = new Interpreter();
        interpreter.execute(ast);
        return interpreter;
    }
    @Test
    void testIfElse() {
        String code = """
        programa_minipar
        SEQ
        a = 3
        if a > 5 {
            x = 1
        } else {
            x = 2
        }
        """;

        Interpreter interpreter = interpretar(code);
        assertEquals(2.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testIfTrue() {
        String code = """
            programa_minipar
            SEQ
            x = 10
            if x > 5{
                x = x + 1
            }
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(11.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testIfFalse() {
        String code = """
            programa_minipar
            SEQ
            x = 3
            if x > 5 {
                x = x + 1
            }
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(3.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testWhileLoop() {
        String code = """
            programa_minipar
            SEQ
            x = 0
            while x < 3 {
                x = x + 1
            }
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(3.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testWhileWithPrint() {
        String code = """
            programa_minipar
            SEQ
            x = 1
            while x <= 3 {
                print(x)
                x = x + 1
            }
            """;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        interpretar(code);
        String output = out.toString().trim();

        assertTrue(output.contains("1"));
        assertTrue(output.contains("2"));
        assertTrue(output.contains("3"));
    }
}
