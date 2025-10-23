package minipar.interpreter;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterForTest{

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
    public void testForSimples() {
        String codigo = """
            programa_minipar
            SEQ
            soma = 0
            for i = 1 to 3 {
                soma = soma + i
            }
            """;
        Interpreter interpreter = interpretar(codigo);
        assertEquals(6.0, interpreter.getMemory().get("soma")); // 1 + 2 + 3
    }

    @Test
    public void testForSemExecucao() {
        String codigo = """
            programa_minipar
            SEQ
            soma = 0
            for i = 3 to 1 {
                soma = soma + i
            }
            """;
        Interpreter interpreter = interpretar(codigo);
        assertEquals(0.0, interpreter.getMemory().get("soma")); // não executa nenhuma vez
    }

    @Test
    public void testForComVariavelFinal() {
        String codigo = """
            programa_minipar
            SEQ
            fim = 4
            soma = 0
            for i = 1 to fim {
                soma = soma + i
            }
            """;
        Interpreter interpreter = interpretar(codigo);
        assertEquals(10.0, interpreter.getMemory().get("soma")); // 1 + 2 + 3 + 4
    }

    @Test
    public void testForComPrint() {
        String codigo = """
            programa_minipar
            SEQ
            for i = 1 to 2 {
                print(i)
            }
            """;
        Interpreter interpreter = interpretar(codigo);
        // não precisa de assert se só verificar execução sem erro, mas pode capturar saída se quiser
        assertTrue(interpreter.getMemory().containsKey("i"));
    }
}