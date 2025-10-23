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

class InterpreterExpressionTest {

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
    void testAtribuicaoSimples() {
        String code = """
            programa_minipar
            SEQ
            x = 5
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(5.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testAtribuicaoComExpressao() {
        String code = """
            programa_minipar
            SEQ
            x = 2 + 3 * 4
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(14.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testExpressaoComParenteses() {
        String code = """
            programa_minipar
            SEQ
            x = (2 + 3) * 4
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(20.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testPrintVariavelENumero() {
        String code = """
            programa_minipar
            SEQ
            a = 4 + 6
            print(a)
            print("Fim")
            """;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        interpretar(code);
        String output = out.toString();

        assertTrue(output.contains("10"));
        assertTrue(output.contains("Fim"));
    }

    @Test
    void testDivisaoEPrecedencia() {
        String code = """
            programa_minipar
            SEQ
            resultado = 12 / 2 + 3
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(9.0, interpreter.getMemory().get("resultado"));
    }

    @Test
    void testMultiplaAtribuicaoComVariaveis() {
        String code = """
            programa_minipar
            SEQ
            a = 5
            b = 3
            c = a * b + 2
            """;
        Interpreter interpreter = interpretar(code);
        assertEquals(17.0, interpreter.getMemory().get("c"));
    }
}
