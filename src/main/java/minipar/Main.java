package minipar;

// Imports do Front-End (já existiam)
import minipar.lexer.Lexer;
import minipar.lexer.Token;
import minipar.parser.ASTNode;
import minipar.parser.Parser;
import minipar.semantic.SemanticAnalyzer;
import minipar.interpreter.Interpreter;

// --- NOVOS IMPORTS ---
// Para o Ponto de Variação 1 (Interface)
import minipar.gui.MiniParGUI;

// Para o Ponto de Variação 2 (Back-end)
import minipar.ir.IRGenerator;
import minipar.ir.IRInstruction;
import minipar.backend.ArmV7Generator;

// Imports de IO (já existiam)
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // --- PONTO DE VARIAÇÃO 1 (INTERFACE) ---
        // Lê o Config.java para decidir qual interface usar
        
        if (Config.INTERFACE == Config.InterfaceVariant.GUI) {
            
            // --- Variante GUI ---
            // Simplesmente lança a classe da Interface Gráfica
            MiniParGUI.main(args);

        } else {
            
            // --- Variante TERMINAL ---
            // Executa a lógica de linha de comando
            runTerminal(args);
        }
    }

  
    private static void runTerminal(String[] args) {
        
        // 1. Pega o nome do arquivo dos argumentos da linha de comando
        if (args.length == 0) {
            System.err.println("Erro: No modo TERMINAL, forneça o caminho do arquivo .mp.");
            System.err.println("Exemplo: mvn exec:java -Dexec.mainClass=\"minipar.Main\" -Dexec.args=\"programs/test5.mpr\"");
            return;
        }
        String caminho = args[0]; // Usa o argumento em vez de um caminho fixo
        
        System.out.println("=== Lendo arquivo: " + caminho + " ===");

        try {
            // Leitura do código
            String codigo = Files.readString(Path.of(caminho));

            // --- FASES COMUNS (FRONT-END) ---
            
            // Etapa 1 - Análise léxica
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.tokenize();

            // Etapa 2 - Análise sintática
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseProgram();

            // Etapa 3 - Análise semântica
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analyze(ast);
            
            System.out.println("=== Análise Front-End concluída com sucesso ===");


            // --- PONTO DE VARIAÇÃO 2 (BACK-END) ---
            // Lê o Config.java para decidir se compila ou interpreta
            
            if (Config.BACKEND == Config.BackendVariant.INTERPRETER) {
                
                // --- Variante "Não Gerar Código" (Interpretador) ---
                System.out.println("\n=== [MODO INTERPRETADOR] ===");
                Interpreter interpreter = new Interpreter();
                interpreter.execute(ast);
                System.out.println("\n=== Execução Concluída ===");

            } else {
                
                // --- Variante "Gerar Códigos" (Compilador) ---
                System.out.println("\n=== [MODO COMPILADOR] ===");

                // 4. Gerar Código Intermediário (IR)
                IRGenerator irGen = new IRGenerator();
                List<IRInstruction> irCode = irGen.generate(ast);
                
                System.out.println("\n--- CÓDIGO 3 ENDEREÇOS (IR) ---");
                for (IRInstruction instr : irCode) {
                    System.out.println(instr.toString());
                }

                // 5. Gerar Código Assembly
                ArmV7Generator asmGen = new ArmV7Generator(irCode);
                String assemblyCode = asmGen.generate();
                
                System.out.println("\n--- CÓDIGO ASSEMBLY (ARMv7) ---");
                System.out.println(assemblyCode);
                System.out.println("\n=== Compilação Concluída ===");
            }

        } catch (Exception e) {
            System.err.println("\nErro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}