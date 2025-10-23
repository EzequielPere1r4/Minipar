package minipar.interpreter;

import minipar.lexer.*;
import minipar.parser.*;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private ASTNode gerarAST(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parseProgram();
    }

    private Interpreter interpretar(String code) {
        ASTNode ast = gerarAST(code);
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);

        Interpreter interpreter = new Interpreter();
        interpreter.execute(ast);
        return interpreter;
    }

    @Test
    void testExecucaoSEQComVariaveis() {
        String code = """
            programa_minipar
            SEQ
            x = 10
            y = x
            """;

        Interpreter interpreter = interpretar(code);
        Map<String, Object> memory = interpreter.getMemory();

        assertEquals(10.0, memory.get("x"));
        assertEquals(10.0, memory.get("y"));
    }

    @Test
    void testDeclaracaoDeCanal() {
        String code = """
            programa_minipar
            SEQ
            c_channel canal1 compA compB
            """;

        Interpreter interpreter = interpretar(code);

        assertTrue(interpreter.getSymbolTable().isDeclared("canal1"));
        assertTrue(interpreter.getSymbolTable().isDeclared("compA"));
        assertTrue(interpreter.getSymbolTable().isDeclared("compB"));
    }

    @Test
    void testExecucaoPAR() {
        String code = """
        programa_minipar
        PAR
        SEQ
        x = 1
        SEQ
        y = 2
        """;

        Interpreter interpreter = interpretar(code);
        Map<String, Object> memory = interpreter.getMemory();

        assertEquals(1.0, memory.get("x"));
        assertEquals(2.0, memory.get("y"));
    }


    @Test
    void testErroVariavelNaoDeclarada() {
        String code = """
            programa_minipar
            SEQ
            z = a
            """;

        RuntimeException e = assertThrows(RuntimeException.class, () -> interpretar(code));
        assertTrue(e.getMessage().contains("Variável não declarada"));
    }
}
