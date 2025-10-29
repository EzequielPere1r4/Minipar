package minipar.ir;

import minipar.parser.ASTNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta é a versão correta do IRGenerator.
 * Ele NÃO usa o padrão Visitor, mas sim a mesma lógica de 'switch'
 * que o seu Interpreter.java usa.
 */
public class IRGenerator {

    private final List<IRInstruction> instructions = new ArrayList<>();
    private int tempCounter = 0;
    private int labelCounter = 0;

    // --- Métodos Auxiliares para criar Operandos ---

    private TemporaryOperand newTemp() {
        return new TemporaryOperand("t" + tempCounter++);
    }

    private LabelOperand newLabel() {
        return new LabelOperand("L" + labelCounter++);
    }

    private void addInstruction(OpCode opCode, Operand result, Operand arg1, Operand arg2) {
        instructions.add(new IRInstruction(opCode, result, arg1, arg2));
    }

    // Mapeia o operador string (ex: "+") para o nosso OpCode (ex: OpCode.ADD)
    private OpCode mapOperatorToOpCode(String op) {
        return switch (op) {
            case "+" -> OpCode.ADD;
            case "-" -> OpCode.SUB;
            case "*" -> OpCode.MUL;
            case "/" -> OpCode.DIV;

            // --- Suporte para Comparações ---
            case ">" -> OpCode.GT;
            case "<" -> OpCode.LT;
            case "==" -> OpCode.EQ;
            case "!=" -> OpCode.NEQ;
            case ">=" -> OpCode.GTE;
            case "<=" -> OpCode.LTE;
            // --- Fim Suporte ---

            default -> throw new RuntimeException("Operador desconhecido para IRGenerator: " + op);
        };
    }

    
    public List<IRInstruction> generate(ASTNode root) {
        if (!root.getType().equals("Programa")) {
            throw new RuntimeException("Raiz inválida. Esperado 'Programa'");
        }
        for (ASTNode bloco : root.getChildren()) {
            generateBlock(bloco);
        }
        return instructions;
    }

    /**
     * Imita o Interpreter.executeBlock()
     */
    public void generateBlock(ASTNode block) {
        // Para a geração de IR, "PAR" é tratado da mesma forma que "SEQ",
        // pois o IR em si é uma representação sequencial.
        switch (block.getType()) {
            case "SEQ", "Bloco", "PAR" -> generateSequential(block);
            default -> throw new RuntimeException("Tipo de bloco desconhecido: " + block.getType());
        }
    }

    /**
     * Imita o Interpreter.executeSequential()
     */
    private void generateSequential(ASTNode block) {
        for (ASTNode stmt : block.getChildren()) {
            generateStatement(stmt);
        }
    }

/**
     * Imita o Interpreter.executeStatement()
     */
    public void generateStatement(ASTNode stmt) {
        switch (stmt.getType()) {
            case "Atribuicao" -> generateAssignment(stmt);
            case "print" -> generatePrint(stmt);
            case "if" -> generateIf(stmt);
            case "while" -> generateWhile(stmt);

            // --- ESTA É A CORREÇÃO ---
            // Se uma instrução for, na verdade, um sub-bloco (como SEQ dentro de PAR)
            case "SEQ", "PAR", "Bloco" -> {
                generateBlock(stmt); // Chame o gerador de bloco recursivamente
            }
            // --- FIM DA CORREÇÃO ---

            // Ignorados (não geram código)
            case "Comentario", "c_channel", "def", "import" -> { 
                /* Não faz nada */ 
            }

            // Ainda não suportados pelo gerador de IR (mas pode adicionar)
            case "AtribuicaoIndice", "send", "receive", "return", "ChamadaFuncao", "for" -> {
                // System.err.println("Instrução não suportada pelo IRGenerator: " + stmt.getType());
            }

            default -> throw new RuntimeException("Instrução não suportada pelo IRGenerator: " + stmt.getType());
        }
    }
    // --- Implementações de Geração de Statement ---

    private void generateAssignment(ASTNode stmt) {
        // Criança 0: A variável (ex: ASTNode("Variavel", "x"))
        // Criança 1: A expressão (ex: ASTNode("OperacaoBinaria", "+"))
        
        // 1. Crie o operando da variável (LHS)
        VariableOperand lhs = new VariableOperand(stmt.getChildren().get(0).getValue());

        // 2. Gere o código para a expressão (RHS), que retorna onde o resultado está
        Operand rhs = generateExpression(stmt.getChildren().get(1));

        // 3. Gere a instrução de atribuição
        addInstruction(OpCode.STORE, lhs, rhs, null);
    }

    private void generatePrint(ASTNode stmt) {
        // Itera sobre todos os argumentos do 'print'
        for (ASTNode arg : stmt.getChildren()) {
            String tipo = arg.getType();
            String raw = arg.getValue();

            if (tipo.equals("Valor") && raw.startsWith("\"") && raw.endsWith("\"")) {
                // Imprimir strings literais não é suportado pelo nosso IR simples por enquanto
                // TODO: Adicionar suporte para strings no .data e syscall de print_string
            } else {
                // É uma expressão (número, variável, operação)
                Operand op = generateExpression(arg);
                addInstruction(OpCode.PRINT, null, op, null);
            }
        }
    }

    private void generateIf(ASTNode stmt) {
        // Criança 0: Condição
        // Criança 1: Bloco 'then'
        // Criança 2: Bloco 'else' (opcional)

        LabelOperand elseLabel = newLabel();
        LabelOperand endIfLabel = newLabel();

        // 1. Gere o código da condição
        Operand condition = generateExpression(stmt.getChildren().get(0));

        // 2. Gere o pulo condicional (se for falso, pule para o 'else')
        addInstruction(OpCode.IF_FALSE_GOTO, elseLabel, condition, null);

        // 3. Gere o bloco 'then'
        generateBlock(stmt.getChildren().get(1));
        
        // 4. Gere um pulo incondicional para o fim do 'if' (para pular o 'else')
        addInstruction(OpCode.GOTO, endIfLabel, null, null);

        // 5. Adicione o rótulo do 'else'
        addInstruction(OpCode.LABEL, elseLabel, null, null);

        // 6. Gere o bloco 'else' (se existir)
        if (stmt.getChildren().size() > 2) {
            generateBlock(stmt.getChildren().get(2));
        }

        // 7. Adicione o rótulo do 'end'
        addInstruction(OpCode.LABEL, endIfLabel, null, null);
    }

    private void generateWhile(ASTNode stmt) {
        // Criança 0: Condição
        // Criança 1: Bloco 'body'
        
        LabelOperand startLabel = newLabel();
        LabelOperand endLabel = newLabel();

        // 1. Adicione o rótulo do "início" (antes da condição)
        addInstruction(OpCode.LABEL, startLabel, null, null);

        // 2. Gere o código da condição
        Operand condition = generateExpression(stmt.getChildren().get(0));

        // 3. Gere o pulo condicional (se for falso, pule para o "fim")
        addInstruction(OpCode.IF_FALSE_GOTO, endLabel, condition, null);

        // 4. Gere o bloco 'body'
        generateBlock(stmt.getChildren().get(1));

        // 5. Gere um pulo incondicional de volta para o "início"
        addInstruction(OpCode.GOTO, startLabel, null, null);

        // 6. Adicione o rótulo do "fim"
        addInstruction(OpCode.LABEL, endLabel, null, null);
    }


    /**
     * Esta função imita o seu 'ExpressionEvaluator.java'.
     * Em vez de retornar um 'double', ela retorna um 'Operand'
     * (seja um Constante, Variável ou um Temporário tX).
     */
    private Operand generateExpression(ASTNode expr) {
        
        // --- LINHA DE DEBUG ---
        // Se o erro "Tipo de expressão não suportada" persistir,
        // olhe no console para ver o nome exato do tipo que está falhando.
        System.out.println("[DEBUG IRGenerator] Vendo o tipo: '" + expr.getType() + "'");
        // --- FIM DEBUG ---
        
        switch (expr.getType()) {
            
            // Corrigido de "BinOP" (do seu arquivo) para "BinOp" (do erro)
            case "BinOp": { 
                // 1. Gere o código para os filhos
                Operand left = generateExpression(expr.getChildren().get(0));
                Operand right = generateExpression(expr.getChildren().get(1));

                // 2. Crie um temporário para o resultado
                TemporaryOperand result = newTemp();

                // 3. Mapeie o operador
                OpCode opCode = mapOperatorToOpCode(expr.getValue());

                // 4. Adicione a instrução
                addInstruction(opCode, result, left, right);
                
                // 5. Retorne o temporário onde o resultado foi salvo
                return result;
            }

            case "Valor": {
                String value = expr.getValue();
                try {
                    // Tenta ser um número (ex: "10")
                    // Usamos Double.parseDouble para ser consistente com o interpretador
                    Double.parseDouble(value); 
                    return new ConstantOperand(value);
                } catch (NumberFormatException e) {
                    // É uma variável (ex: "x")
                    return new VariableOperand(value);
                }
            }

            // Adiciona um caso para o nó do tipo "input"
            case "input": {
                TemporaryOperand temp = newTemp();
                addInstruction(OpCode.READ, temp, null, null);
                return temp; // Retorna o temporário onde o valor lido será armazenado
            }

            case "ChamadaFuncao": {
                // Suporte para 'input()' pode estar aqui também, dependendo do Parser
                if (expr.getValue().equals("input")) { 
                    TemporaryOperand temp = newTemp();
                    addInstruction(OpCode.READ, temp, null, null);
                    return temp;
                }
                // TODO: Adicionar suporte a outras chamadas de função
                throw new RuntimeException("Chamadas de função não suportadas pelo IRGenerator: " + expr.getValue());
            }

            default:
                // Esta é a linha que causou o seu erro
                throw new RuntimeException("Tipo de expressão não suportada: " + expr.getType());
        }
    }
}