package minipar;

// Importe todos os seus pacotes
import minipar.lexer.Lexer;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import minipar.interpreter.Interpreter;
import minipar.ir.IRGenerator;
import minipar.ir.IRInstruction;
import minipar.backend.ArmV7Generator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Fachada (Facade) que esconde a complexidade do compilador
 * e fornece uma interface limpa para o serviço web.
 */
public class CompilerServiceFacade {

    /**
     * Ponto de entrada unificado para o serviço.
     * AVISO: A função 'input()' NÃO É SUPORTADA neste modo.
     */
    public CompilerResult run(String sourceCode, String variant) {
        
        // 1. Capturar a saída do console
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        try {
            // --- FASES COMUNS (FRONT-END) ---
            Lexer lexer = new Lexer(sourceCode);
            Parser parser = new Parser(lexer.tokenize());
            ASTNode ast = parser.parseProgram();
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analyze(ast);

            // --- PONTO DE VARIAÇÃO (BACK-END) ---
            if ("interpret".equalsIgnoreCase(variant)) {
                
                // --- Variante Interpretador ---
                // AVISO: input() irá falhar aqui (NoSuchElementException)
                Interpreter interpreter = new Interpreter();
                interpreter.execute(ast);
                System.setOut(originalOut); // Restaurar console
                
                // --- ATUALIZADO ---
                // Chama o método estático para sucesso do interpretador
                return CompilerResult.successInterpreter(outputCapture.toString());
                // --- FIM ATUALIZAÇÃO ---

            } else {
                
                // --- Variante Compilador ---
                IRGenerator irGen = new IRGenerator();
                List<IRInstruction> irCode = irGen.generate(ast);
                
                ArmV7Generator asmGen = new ArmV7Generator(irCode);
                String assemblyCode = asmGen.generate();
                
                // Formatar o IR para String
                StringBuilder irText = new StringBuilder();
                for (IRInstruction instr : irCode) {
                    irText.append(instr.toString()).append("\n");
                }
                
                System.setOut(originalOut); // Restaurar console

                // --- ATUALIZADO ---
                // Chama o método estático para sucesso do compilador
                return CompilerResult.successCompiler(irText.toString(), assemblyCode);
                // --- FIM ATUALIZAÇÃO ---
            }

        } catch (Exception e) {
            System.setOut(originalOut); // Restaurar console em caso de erro

            // --- ATUALIZADO ---
            // Chama o método estático para erro
            return CompilerResult.error("Erro ao executar: " + e.getMessage());
            // --- FIM ATUALIZAÇÃO ---
        }
    }
}