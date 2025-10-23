package minipar.parser;

import minipar.lexer.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private List<Token> tokenize(String code) {
        Lexer lexer = new Lexer(code);
        return lexer.tokenize();
    }

    @Test
    void testProgramaSEQComAtribuicao() {
        String code = """
        programa_minipar
        SEQ
        x = 5
        """;

        Parser parser = new Parser(tokenize(code));
        ASTNode ast = parser.parseProgram();

        assertEquals("Programa", ast.getType());
        assertEquals("programa_minipar", ast.getValue());
        assertEquals(1, ast.getChildren().size());

        ASTNode seqNode = ast.getChildren().get(0);
        assertEquals("SEQ", seqNode.getType());
        assertEquals(1, seqNode.getChildren().size());

        ASTNode atribNode = seqNode.getChildren().get(0);
        assertEquals("Atribuicao", atribNode.getType());
        assertEquals(2, atribNode.getChildren().size());

        assertEquals("Variavel", atribNode.getChildren().get(0).getType());
        assertEquals("x", atribNode.getChildren().get(0).getValue());

        ASTNode valorNode = atribNode.getChildren().get(1);
        assertEquals("Valor", valorNode.getType());
        assertEquals("5", valorNode.getValue());
    }

    @Test
    void testCanalComunicacao() {
        String code = """
            programa_minipar
            SEQ
            c_channel calc comp1 comp2
            """;

        Parser parser = new Parser(tokenize(code));
        ASTNode ast = parser.parseProgram();

        ASTNode canalNode = ast.getChildren().get(0).getChildren().get(0);
        assertEquals("c_channel", canalNode.getType());
        assertEquals("calc", canalNode.getValue());
        assertEquals(2, canalNode.getChildren().size());
        assertEquals("comp1", canalNode.getChildren().get(0).getValue());
        assertEquals("comp2", canalNode.getChildren().get(1).getValue());
    }

    @Test
    void testComentarioIgnoradoNoErro() {
        String code = """
            programa_minipar
            SEQ
            # Comentário de teste
            x = 10
            """;

        Parser parser = new Parser(tokenize(code));
        ASTNode ast = parser.parseProgram();

        ASTNode seqNode = ast.getChildren().get(0);
        assertEquals(2, seqNode.getChildren().size());

        assertEquals("Comentario", seqNode.getChildren().get(0).getType());
        assertEquals("x", seqNode.getChildren().get(1).getChildren().get(0).getValue());
    }

    @Test
    void testErroSintatico() {
        String code = """
            programa_minipar
            SEQ
            x 5
            """;

        Parser parser = new Parser(tokenize(code));
        Exception exception = assertThrows(RuntimeException.class, parser::parseProgram);
        System.out.println(exception);
        assertTrue(exception.getMessage().contains("Atribuição inválida ou comando desconhecido após"));
    }
}
