package minipar.dto;

import minipar.CompilerResult; // Importa do seu 'core'

public class RunResponse {
    private boolean success;
    private String output;
    private String irCode;
    private String assemblyCode;
    private String error;


    public RunResponse() {
        }

public RunResponse(CompilerResult result) {
        this.success = result.isSuccess();
        this.output = result.getConsoleOutput();
        this.irCode = result.getIrCode();
        this.assemblyCode = result.getAssemblyCode();
        this.error = result.getErrorMessage();
    }

    // Getters (necessários para o Jackson serializar E deserializar)
    public boolean isSuccess() { return success; }
    public String getOutput() { return output; }
    public String getIrCode() { return irCode; }
    public String getAssemblyCode() { return assemblyCode; }
    public String getError() { return error; }

    // --- OPCIONAL: Adicionar Setters (Boa prática para Jackson) ---
    // Embora Jackson possa acessar campos privados, adicionar setters é mais robusto.
    public void setSuccess(boolean success) { this.success = success; }
    public void setOutput(String output) { this.output = output; }
    public void setIrCode(String irCode) { this.irCode = irCode; }
    public void setAssemblyCode(String assemblyCode) { this.assemblyCode = assemblyCode; }
    public void setError(String error) { this.error = error; }
    // --- FIM OPCIONAL ---
}