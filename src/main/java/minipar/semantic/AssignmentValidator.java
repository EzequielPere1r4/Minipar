package minipar.semantic;

import minipar.parser.ASTNode;

public class AssignmentValidator {
    private final SymbolTable symbolTable;
    private final ExpressionValidator expressionValidator;

    public AssignmentValidator(SymbolTable symbolTable, ExpressionValidator expressionValidator) {
        this.symbolTable = symbolTable;
        this.expressionValidator = expressionValidator;
    }

    public void analyzeAssignment(ASTNode stmt) {
        String var = stmt.getChildren().get(0).getValue();
        if (!symbolTable.isDeclared(var)) {
            symbolTable.declare(var, "int");
        }
        expressionValidator.validateExpression(stmt.getChildren().get(1));
    }

    public void analyzeAssignmentIndex(ASTNode stmt) {
        String lista = stmt.getValue();
        if (!symbolTable.isDeclared(lista)) {
            throw new RuntimeException("Lista não declarada: " + lista);
        }
        expressionValidator.validateExpression(stmt.getChildren().get(0));
        expressionValidator.validateExpression(stmt.getChildren().get(1));
    }
}
