package minipar.dto;

public class RunRequest {
    private String code;
    private String variant; // "interpret" ou "compile"

    // Getters e Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
}