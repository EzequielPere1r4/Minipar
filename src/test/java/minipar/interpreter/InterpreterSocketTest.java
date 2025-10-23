package minipar.interpreter;

import minipar.lexer.*;
import minipar.parser.*;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterSocketTest {

    private ASTNode gerarAST(String codigo) {
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parseProgram();
    }

    private Interpreter interpretar(ASTNode ast) {
        SemanticAnalyzer sem = new SemanticAnalyzer();
        sem.analyze(ast);
        Interpreter interpreter = new Interpreter();
        interpreter.execute(ast);
        return interpreter;
    }

    @Test
    void testSendReceiveSimplesComPAR() throws InterruptedException {
        String codigo = """
        programa_minipar
        SEQ
        c_channel canal1 pc1 pc2
        PAR
        SEQ
        canal1.send(99)
        SEQ
        canal1.receive(resultado)
        """;

        ASTNode ast = gerarAST(codigo);
        ast.print("");
        Interpreter interpreter = interpretar(ast);
        Map<String, Object> mem = interpreter.getMemory();
        assertTrue(mem.containsKey("resultado"),  "Variável 'resultado' não encontrada na memória");
        assertEquals(99.0, mem.get("resultado"));
    }

}
