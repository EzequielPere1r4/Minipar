package minipar.lexer;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void testTokenizeSimpleProgram() {
        String code = """
            programa_minipar
            SEQ
            x = 5
            y = x + 3
            print("Resultado")
            """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType()); // programa_minipar
        assertEquals("programa_minipar", tokens.get(0).getValue());

        assertEquals(TokenType.KEYWORD, tokens.get(1).getType()); // SEQ

        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType()); // x
        assertEquals("x", tokens.get(2).getValue());

        assertEquals(TokenType.OPERATOR, tokens.get(3).getType()); // =

        assertEquals(TokenType.NUMBER, tokens.get(4).getType()); // 5

        assertEquals(TokenType.IDENTIFIER, tokens.get(5).getType()); // y
        assertEquals(TokenType.OPERATOR, tokens.get(6).getType()); // =
        assertEquals(TokenType.IDENTIFIER, tokens.get(7).getType()); // x
        assertEquals(TokenType.OPERATOR, tokens.get(8).getType()); // +
        assertEquals(TokenType.NUMBER, tokens.get(9).getType()); // 3
        // A string "Resultado" seria analisada em uma próxima linha, se quiser testar também

        Token last = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.EOF, last.getType()); // Token final
        assertEquals(15, tokens.size());
    }

    @Test
    void testTokenizeWithComment() {
        String code = """
            # Isso é um comentário
            x = 10
            """;

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.COMMENT));
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("10")));
    }

    @Test
    void testInvalidTokenThrowsError() {
        String code = "x = @";

        Lexer lexer = new Lexer(code);

        Exception exception = assertThrows(RuntimeException.class, lexer::tokenize);
        assertTrue(exception.getMessage().contains("Token inválido"));
    }
}
