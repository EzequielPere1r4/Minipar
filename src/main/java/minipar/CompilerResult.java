package minipar;

// Um POJO (Plain Old Java Object) para guardar o resultado
public class CompilerResult {
    private boolean success;
    private String consoleOutput;
    private String irCode;
    private String assemblyCode;
    private String errorMessage;

 // Construtor PRIVADO (ninguém chama diretamente)
    private CompilerResult() {} 

    // --- MÉTODOS ESTÁTICOS DE FÁBRICA ---

    // Cria um resultado de sucesso para o Interpretador
    public static CompilerResult successInterpreter(String consoleOutput) {
        CompilerResult result = new CompilerResult();
        result.success = true;
        result.consoleOutput = consoleOutput;
        return result;
    }

    // Cria um resultado de sucesso para o Compilador
    public static CompilerResult successCompiler(String irCode, String assemblyCode) {
        CompilerResult result = new CompilerResult();
        result.success = true;
        result.irCode = irCode;
        result.assemblyCode = assemblyCode;
        result.consoleOutput = "Compilação concluída com sucesso.";
        return result;
    }

    // Cria um resultado de erro
    public static CompilerResult error(String errorMessage) {
        CompilerResult result = new CompilerResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    
    // Getters
    public boolean isSuccess() { return success; }
    public String getConsoleOutput() { return consoleOutput; }
    public String getIrCode() { return irCode; }
    public String getAssemblyCode() { return assemblyCode; }
    public String getErrorMessage() { return errorMessage; }
}