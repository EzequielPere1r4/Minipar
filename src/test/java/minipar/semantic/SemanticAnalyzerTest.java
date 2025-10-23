package minipar.semantic;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {

    private ASTNode gerarAST(String code) {
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parseProgram();
    }

    @Test
    void testDeclaracaoEUsoDeVariavel() {
        String code = """
            programa_minipar
            SEQ
            x = 10
            y = x
            """;

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(gerarAST(code));

        assertTrue(analyzer.getSymbolTable().isDeclared("x"));
        assertTrue(analyzer.getSymbolTable().isDeclared("y"));
        assertEquals("int", analyzer.getSymbolTable().getType("x"));
    }

    @Test
    void testVariavelNaoDeclaradaEmExpressao() {
        String code = """
            programa_minipar
            SEQ
            x = y
            """;

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        Exception e = assertThrows(RuntimeException.class, () -> analyzer.analyze(gerarAST(code)));
        System.out.println(e);
        assertTrue(e.getMessage().contains("Variável não declarada"));
    }

    @Test
    void testCanalValido() {
        String code = """
            programa_minipar
            SEQ
            c_channel calc c1 c2
            """;

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(gerarAST(code));

        assertTrue(analyzer.getSymbolTable().isDeclared("calc"));
        assertTrue(analyzer.getSymbolTable().isDeclared("c1"));
        assertTrue(analyzer.getSymbolTable().isDeclared("c2"));
        assertEquals("canal", analyzer.getSymbolTable().getType("calc"));
    }

    @Test
    void testCanalDuplicado() {
        String code = """
            programa_minipar
            SEQ
            c_channel calc comp1 comp2
            c_channel calc outro1 outro2
            """;

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        Exception e = assertThrows(RuntimeException.class, () -> analyzer.analyze(gerarAST(code)));
        assertTrue(e.getMessage().contains("ja declarado"));
    }
}
