package minipar;

import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import minipar.interpreter.Interpreter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Caminho do arquivo de teste
            String caminho = "programs/test5.mpr";

            // Leitura do código
            String codigo = Files.readString(Path.of(caminho));

            // Etapa 1 - Análise léxica
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.tokenize();

            // Etapa 2 - Análise sintática
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseProgram();

            // Opcional: imprimir AST
            System.out.println("=== Árvore Sintática (AST) ===");
            ast.print("");

            // Etapa 3 - Análise semântica
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analyze(ast);

            // Etapa 4 - Interpretação
            System.out.println("\n=== Execução ===");
            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
