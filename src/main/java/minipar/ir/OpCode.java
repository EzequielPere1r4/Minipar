package minipar.ir;

public enum OpCode {
    // Aritmética
    ADD,  // result = arg1 + arg2
    SUB,  // result = arg1 - arg2
    MUL,  // result = arg1 * arg2
    DIV,  // result = arg1 / arg2
    
    // --- NOVO ---
    // Comparações (result = 1 se for true, 0 se for false)
    GT,   // Maior que (>)
    LT,   // Menor que (<)
    EQ,   // Igual (==)
    NEQ,  // Diferente (!=)
    GTE,  // Maior ou Igual (>=)
    LTE,  // Menor ou Igual (<=)
    // --- FIM NOVO ---
    
    // Memória
    STORE, // result = arg1 (Ex: x = 10)
    
    // ... (o resto do arquivo)

    // Controle de Fluxo
    LABEL, // Define um rótulo (Ex: L1:)
    GOTO,  // Pula incondicionalmente para um rótulo
    IF_FALSE_GOTO, // Se arg1 for falso (ou 0), pula para 'result' (rótulo)

    // E/S
    PRINT, // Imprime arg1
    READ   // Lê um valor para 'result'

    // (Adicione mais conforme sua linguagem precisar, ex: AND, OR, NOT, etc.)
}