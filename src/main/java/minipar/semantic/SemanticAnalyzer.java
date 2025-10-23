package minipar.semantic;

import minipar.parser.ASTNode;
import java.util.List;

public class SemanticAnalyzer {
    private final SymbolTable symbolTable = new SymbolTable();
    private final ExpressionValidator expressionValidator = new ExpressionValidator(symbolTable);
    private final AssignmentValidator assignmentValidator = new AssignmentValidator(symbolTable, expressionValidator);
    private final ChannelValidator channelValidator = new ChannelValidator(symbolTable);
    private final ControlFlowValidator controlFlowValidator = new ControlFlowValidator(expressionValidator, this);

    public void analyze(ASTNode root) {
        if (!"Programa".equals(root.getType())) {
            throw new RuntimeException("AST invalida: nó raiz não é 'Programa'");
        }
        root.getChildren().forEach(this::analyzeBlock);
    }

    public void analyzeBlock(ASTNode block) {
        block.getChildren().forEach(this::analyzeStatement);
    }

    private void analyzeStatement(ASTNode stmt) {
        switch (stmt.getType()) {
            case "Atribuicao"                   -> assignmentValidator.analyzeAssignment(stmt);
            case "AtribuicaoIndice"             -> assignmentValidator.analyzeAssignmentIndex(stmt);
            case "c_channel"                    -> channelValidator.analyzeChannel(stmt);
            case "send"                         -> channelValidator.analyzeSend(stmt);
            case "receive"                      -> channelValidator.analyzeReceive(stmt);
            case "print"                        -> analyzePrint(stmt);
            case "if", "while"                  -> controlFlowValidator.analyzeConditional(stmt);
            case "for"                          -> analyzeForLoop(stmt);
            case "def", "Comentario", "import"  -> {}
            case "SEQ", "PAR", "Bloco"          -> analyzeBlock(stmt);
            case "return"                       -> expressionValidator.validateExpression(stmt.getChildren().getFirst());
            case "ChamadaFuncao"                -> expressionValidator.validateExpressionList(stmt.getChildren());
            default -> throw new RuntimeException("Tipo de instrucao desconhecido: " + stmt.getType());
        }
    }

    private void analyzePrint(ASTNode stmt) {
        String arg = stmt.getChildren().get(0).getValue();
        if (!isLiteral(arg) && !symbolTable.isDeclared(arg)) {
            throw new RuntimeException("Variavel não declarada: " + arg);
        }
    }

    private boolean isLiteral(String val) {
        return val.matches("\\d+") || (val.startsWith("\"") && val.endsWith("\""));
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    private void analyzeForLoop(ASTNode stmt) {
        ASTNode iterable = stmt.getChildren().get(0);
        expressionValidator.validateExpression(iterable);

        String var = stmt.getValue();
        if (!symbolTable.isDeclared(var)) {
            symbolTable.declare(var, "int"); // variável do loop
        }

        analyzeBlock(stmt.getChildren().get(1)); // corpo do for
    }
}
