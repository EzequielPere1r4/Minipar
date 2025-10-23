package minipar.interpreter;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterImportTest {

    private Interpreter interpretar(String codigo) {
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parseProgram();

        Interpreter interpreter = new Interpreter();
        interpreter.setupFunctionEvaluation();
        interpreter.execute(ast);
        return interpreter;
    }

    @Test
    void testImportaOutroArquivo() throws IOException {
        // Criação de um arquivo temporário com código MiniPar
        Path arquivoImportado = Files.createTempFile("importado", ".mpr");
        Files.writeString(arquivoImportado, """
            programa_minipar
            SEQ
            a = 5
            b = 7
            resultado = a + b
            """);

        String codigoPrincipal = """
            programa_minipar
            SEQ
            import "%s"
            x = resultado + 1
            """.formatted(arquivoImportado.toAbsolutePath().toString().replace("\\", "\\\\"));

        Interpreter interpreter = interpretar(codigoPrincipal);

        assertEquals(13.0, interpreter.getMemory().get("x"));
    }

    @Test
    void testErroArquivoImportInexistente() {
        String codigo = """
        programa_minipar
        SEQ
        import "nao_existe_arquivo.mpr"
        """;

        RuntimeException e = assertThrows(RuntimeException.class, () -> interpretar(codigo));

        Assertions.assertTrue(e.getMessage().contains("Erro ao importar arquivo"));
    }
    @Test
    void testErroSintaxeEmArquivoImportado() throws IOException {
        Path arquivoComErro = Files.createTempFile("com_erro", ".mpr");
        Files.writeString(arquivoComErro, """
        programa_minipar
        SEQ
        a = 
        """); // Erro de sintaxe: falta expressão após "="

        String codigo = """
        programa_minipar
        SEQ
        import "%s"
        """.formatted(arquivoComErro.toAbsolutePath().toString().replace("\\", "\\\\"));

        RuntimeException e = assertThrows(RuntimeException.class, () -> interpretar(codigo));
        Assertions.assertTrue(e.getMessage().contains("Erro sintatico"));
    }
}
