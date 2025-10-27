package minipar.backend;

import minipar.ir.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArmV7Generator {

    private final List<IRInstruction> irCode;
    private final StringBuilder dataSection;
    private final StringBuilder textSection;
    private final Set<String> variables;
    private int instructionCounter = 0; // --- NOVO --- (Para rótulos únicos)

    public ArmV7Generator(List<IRInstruction> irCode) {
        this.irCode = irCode;
        this.dataSection = new StringBuilder();
        this.textSection = new StringBuilder();
        this.variables = new HashSet<>();
    }

    // Método de entrada principal
    public String generate() {
        // Cabeçalho da seção de dados
        dataSection.append(".data\n");
        
        // Passo 1: Encontrar todas as variáveis para alocar na memória
        findAllVariables();

        // Passo 2: Gerar o código de instrução
        generateInstructionCode();

        // Passo 3: Montar o arquivo final
        StringBuilder finalCode = new StringBuilder();
        finalCode.append(".global _start\n\n");
        
        finalCode.append(dataSection); // Variáveis
        
        finalCode.append("\n.text\n"); // Código
        finalCode.append("_start:\n");
        finalCode.append(textSection);  // Instruções
        
        // Loop final para encerrar o programa (como no exemplo do fatorial)
        finalCode.append("\nend:\n");
        finalCode.append("\tB end\n"); // B = Branch (pulo)

        return finalCode.toString();
    }

    /**
     * PASSO 1: Passa por todo o IR e encontra todas as variáveis
     * e temporários para declará-los na seção .data
     */
    private void findAllVariables() {
        for (IRInstruction instr : irCode) {
            if (instr.result instanceof VariableOperand || instr.result instanceof TemporaryOperand) {
                variables.add(instr.result.toString());
            }
            if (instr.arg1 instanceof VariableOperand || instr.arg1 instanceof TemporaryOperand) {
                variables.add(instr.arg1.toString());
            }
            if (instr.arg2 instanceof VariableOperand || instr.arg2 instanceof TemporaryOperand) {
                variables.add(instr.arg2.toString());
            }
        }

        // Declara cada variável na seção .data com valor inicial 0
        for (String var : variables) {
            dataSection.append(String.format("%s: .word 0\n", var));
        }
    }

    /**
     * PASSO 2: Itera pela lista de IR e traduz cada instrução.
     */
    private void generateInstructionCode() {
        for (IRInstruction instr : irCode) {
            switch (instr.opCode) {
                case STORE: // ex: x = 10  (result = arg1)
                    generateStore(instr);
                    break;
                case ADD: // ex: t0 = a + b (result = arg1 + arg2)
                case SUB:
                case MUL:
                case DIV:
                // --- NOVO ---
                // Adiciona os casos de comparação
                case GT:
                case LT:
                case EQ:
                case NEQ:
                case GTE:
                case LTE:
                // --- FIM NOVO ---
                    generateBinaryOp(instr);
                    break;
                case LABEL: // ex: L1: (result)
                    textSection.append(String.format("%s:\n", ((LabelOperand) instr.result).name));
                    break;
                case GOTO: // ex: GOTO L1 (result)
                    textSection.append(String.format("\tB %s\n", ((LabelOperand) instr.result).name));
                    break;
                case IF_FALSE_GOTO: // ex: IF_FALSE t0 GOTO L1 (result, arg1)
                    // 1. Carrega a condição (t0) para R0
                    appendLoad(instr.arg1, "R0");
                    // 2. Compara R0 com 0 (falso)
                    textSection.append("\tCMP R0, #0\n");
                    // 3. Pula se for igual (BEQ = Branch if Equal)
                    textSection.append(String.format("\tBEQ %s\n", ((LabelOperand) instr.result).name));
                    break;
                case PRINT: // ex: PRINT x (arg1)
                    // 1. Carrega o valor de 'x' em R1 (CPULator usa R1 para 'print_int')
                    appendLoad(instr.arg1, "R1");
                    // 2. Chama a syscall 1 (print_int)
                    textSection.append("\tSWI 1\n");
                    break;
                case READ: // ex: READ x (result)
                    // 1. Chama a syscall 2 (read_int). O valor lido vai para R1
                    textSection.append("\tSWI 2\n");
                    // 2. Salva o valor de R1 na variável 'x'
                    appendStore("R1", instr.result);
                    break;
            }
        }
    }

    /**
     * Gera código para uma instrução STORE (result = arg1)
     * ex: x = 10  OU  x = y
     */
    private void generateStore(IRInstruction instr) {
        // 1. Carrega o valor de arg1 (seja constante ou variável) para R0
        appendLoad(instr.arg1, "R0");
        // 2. Salva o valor de R0 na memória no endereço de 'result'
        appendStore("R0", instr.result);
    }

    /**
     * Gera código para uma operação binária (ADD, SUB, MUL, DIV)
     * ex: result = arg1 + arg2
     */
    private void generateBinaryOp(IRInstruction instr) {
        // 1. Carrega arg1 para R0
        appendLoad(instr.arg1, "R0");
        // 2. Carrega arg2 para R1
        appendLoad(instr.arg2, "R1");

        // 3. Executa a operação
        String opAsm = "";
        switch (instr.opCode) {
            case ADD: opAsm = "ADD"; break;
            case SUB: opAsm = "SUB"; break;
            case MUL: opAsm = "MUL"; break;
            case DIV: opAsm = "SDIV"; break; // Divisão de inteiros com sinal
            
            // --- NOVO ---
            // Se for uma comparação, delega para o novo método
            case GT:
            case LT:
            case EQ:
            case NEQ:
            case GTE:
            case LTE:
                generateComparison(instr); // Passa a instrução inteira
                return; // O método de comparação cuida de tudo
            // --- FIM NOVO ---
        }
        // Coloca o resultado (R0 + R1) em R2
        textSection.append(String.format("\t%s R2, R0, R1\n", opAsm));

        // 4. Salva o resultado (R2) na variável 'result'
        appendStore("R2", instr.result);
    }
    
    // --- NOVO MÉTODO ---
    /**
     * Gera código para uma operação de comparação (GT, LT, EQ, etc.)
     * O resultado (em 'result') será 1 se for true, 0 se for false.
     * Assume que R0 já contém arg1 e R1 já contém arg2.
     */
    private void generateComparison(IRInstruction instr) {
        // 1. Compara R0 e R1 (que foram carregados no generateBinaryOp)
        textSection.append("\tCMP R0, R1\n");

        // 2. Prepara os rótulos únicos para este 'if'
        String falseLabel = String.format(".L_false_%d", instructionCounter);
        String endLabel = String.format(".L_end_%d", instructionCounter);
        instructionCounter++; // Garante que o próximo 'if' tenha rótulos diferentes

        // 3. Descobre qual instrução de pulo usar (a lógica é *inversa*)
        // Ex: Para 'GT' (maior que), nós pulamos se for 'BLE' (menor ou igual)
        String conditionCode = "";
        switch (instr.opCode) {
            case GT:  conditionCode = "BLE"; break; // Branch if Less or Equal
            case LT:  conditionCode = "BGE"; break; // Branch if Greater or Equal
            case EQ:  conditionCode = "BNE"; break; // Branch if Not Equal
            case NEQ: conditionCode = "BEQ"; break; // Branch if Equal
            case GTE: conditionCode = "BLT"; break; // Branch if Less Than
            case LTE: conditionCode = "BGT"; break; // Branch if Greater Than
            default: // Para satisfazer o compilador, embora não deva acontecer
                return;
        }
        
        // 4. Pula para o rótulo 'false' se a condição inversa for verdadeira
        textSection.append(String.format("\t%s %s\n", conditionCode, falseLabel));

        // 5. Se não pulou (bloco 'true'): move 1 para R2
        textSection.append("\tMOV R2, #1\n");
        textSection.append(String.format("\tB %s\n", endLabel)); // Pula para o fim

        // 6. Rótulo 'false': move 0 para R2
        textSection.append(String.format("%s:\n", falseLabel));
        textSection.append("\tMOV R2, #0\n");

        // 7. Rótulo 'end'
        textSection.append(String.format("%s:\n", endLabel));
        
        // 8. Salva o resultado (0 ou 1) que está em R2 na variável 'result'
        appendStore("R2", instr.result);
    }
    // --- FIM NOVO MÉTODO ---

    // --- MÉTODOS AUXILIARES ---

    /**
     * Auxiliar que gera código para CARREGAR um operando (var/const)
     * da memória para um registrador.
     */
    private void appendLoad(Operand op, String register) {
        if (op instanceof ConstantOperand) {
            // Carga imediata de um número
            // ex: MOV R0, #10
            textSection.append(String.format("\tMOV %s, #%s\n", register, ((ConstantOperand) op).value));
        } else {
            // Carga da memória
            // ex: LDR R0, =x   (Coloca o ENDEREÇO de x em R0)
            textSection.append(String.format("\tLDR %s, =%s\n", register, op.toString()));
            // ex: LDR R0, [R0] (Coloca o VALOR no endereço [R0] em R0)
            textSection.append(String.format("\tLDR %s, [%s]\n", register, register));
        }
    }

    /**
     * Auxiliar que gera código para SALVAR um valor de um registrador
     * de volta para a memória (em uma variável/temporário).
     */
    private void appendStore(String register, Operand destination) {
        // Usamos R10 como um registrador "rascunho" para não sujar R0-R2
        
        // 1. Carrega o ENDEREÇO de 'destination' em R10
        // ex: LDR R10, =x
        textSection.append(String.format("\tLDR R10, =%s\n", destination.toString()));
        
        // 2. Salva o VALOR do 'register' (ex: R0) no ENDEREÇO [R10]
        // ex: STR R0, [R10]
        textSection.append(String.format("\tSTR %s, [R10]\n", register));
    }
}