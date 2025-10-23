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

class InterpreterFunctionTest {

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
    void testFuncaoSoma() {
        String code = """
            programa_minipar
            SEQ
            def soma(a, b) {
                resultado = a + b
                return resultado
            }

            x = soma(2, 3)
            """;

        Interpreter interpreter = interpretar(code);
        assertEquals(5.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testFuncaoMultiplaChamada() {
        String code = """
            programa_minipar
            SEQ
            def dobra(x) {
                return x * 2
            }

            a = dobra(3)
            b = dobra(10)
            """;

        Interpreter interpreter = interpretar(code);
        assertEquals(6.0, interpreter.getMemory().get("a"));
        assertEquals(20.0, interpreter.getMemory().get("b"));
    }

    @Test
    void testFuncaoSemReturn() {
        String code = """
            programa_minipar
            SEQ
            def nada(x) {
                y = x + 1
            }

            z = nada(5)
            """;

        Interpreter interpreter = interpretar(code);
        assertEquals(0.0, interpreter.getMemory().get("z")); // padrão sem return é 0
    }

    @Test
    void testFuncaoComPrint() {
        String code = """
            programa_minipar
            SEQ
            def ola(nome) {
                print(nome)
                return 0
            }

            ola("mundo")
            """;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        interpretar(code);
        String output = out.toString().trim();

        assertTrue(output.contains("mundo"));
    }

    @Test
    void testIsolamentoDeEscopo() {
        String code = """
            programa_minipar
            SEQ
            x = 100

            def usarInterno(a) {
                x = a + 5
                return x
            }

            y = usarInterno(1)
            """;

        Interpreter interpreter = interpretar(code);
        assertEquals(100.0, interpreter.getMemory().get("x")); // escopo externo preservado
        assertEquals(6.0, interpreter.getMemory().get("y"));
    }
}
