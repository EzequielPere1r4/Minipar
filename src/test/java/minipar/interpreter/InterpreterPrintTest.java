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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterpreterPrintTest {

    private Interpreter interpretar(String codigo, ByteArrayOutputStream out) {
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseProgram();

        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        Interpreter interpreter = new Interpreter();
        interpreter.execute(ast);

        System.setOut(originalOut);
        return interpreter;
    }

    @Test
    void testPrintComMultiplosArgumentos() {
        String codigo = """
            programa_minipar
            SEQ
            a = 3
            b = 4
            print("Soma de", a, "e", b, "é", a + b)
            """;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        interpretar(codigo, out);

        String saida = out.toString().trim();
        System.out.println(saida);
        assertTrue(saida.contains("Soma de 3.0 e 4.0 é 7.0"));
    }
}