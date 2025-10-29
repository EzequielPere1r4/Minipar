package minipar;

public class Config {

    // --- PONTO DE VARIAÇÃO 1: INTERFACE ---
    // Mude para .GUI ou .TERMINAL para escolher a interface

    public static final InterfaceVariant INTERFACE = InterfaceVariant.GUI;
    //public static final InterfaceVariant INTERFACE = InterfaceVariant.TERMINAL;


    public enum InterfaceVariant {
        GUI,
        TERMINAL
    }

    // --- PONTO DE VARIAÇÃO 2: BACK-END ---
    // Mude para .COMPILER ou .INTERPRETER para escolher o back-end
   public static final BackendVariant BACKEND = BackendVariant.COMPILER; 
   //public static final BackendVariant BACKEND = BackendVariant.INTERPRETER; 

    // (A Variante "não gerar código nenhum" é o nosso Interpretador)

    public enum BackendVariant {
        INTERPRETER, // Não gera código, só executa
        COMPILER     // Gera Cód. 3 Endereços e Assembly
    }
}