package minipar.semantic;

import minipar.parser.ASTNode;
import java.util.List;

public class ExpressionValidator {
    private final SymbolTable symbolTable;

    public ExpressionValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void validateExpression(ASTNode expr) {
        switch (expr.getType()) {
            case "Valor" -> validateLiteralOrVariable(expr.getValue());
            case "BinOp" -> {
                validateExpression(expr.getChildren().get(0));
                validateExpression(expr.getChildren().get(1));
            }
            case "ChamadaFuncao", "Lista" -> expr.getChildren().forEach(this::validateExpression);
            case "Indexacao" -> validateIndexAccess(expr);
            case "input" -> {}
            default -> throw new RuntimeException("Expressão inválida: " + expr.getType());
        }
    }

    public void validateExpressionList(List<ASTNode> exprs) {
        exprs.forEach(this::validateExpression);
    }

    private void validateLiteralOrVariable(String val) {
        if (val.matches("-?\\d+(\\.\\d+)?")) return; // número literal
        if (val.startsWith("\"") && val.endsWith("\"")) return; // string
        if (!symbolTable.isDeclared(val)) {
            throw new RuntimeException("Variável não declarada: " + val);
        }
    }
    private void validateIndexAccess(ASTNode expr) {
        String varName = expr.getValue(); // Ex: nome da lista
        if (!symbolTable.isDeclared(varName)) {
            throw new RuntimeException("Lista não declarada: " + varName);
        }
        validateExpression(expr.getChildren().get(0)); // índice
    }

    public void validateAll(List<ASTNode> nodes) {
        nodes.forEach(this::validateExpression);
    }
}
