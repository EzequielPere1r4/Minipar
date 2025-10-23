package minipar.interpreter;

import minipar.parser.ASTNode;

import java.util.List;

public class ControlFlowExecutor {

    private final Interpreter interpreter;
    private final ExpressionEvaluator evaluator;

    public ControlFlowExecutor(Interpreter interpreter, ExpressionEvaluator evaluator) {
        this.interpreter = interpreter;
        this.evaluator = evaluator;
    }

    public void executeIf(ASTNode stmt) {
        ASTNode condition = stmt.getChildren().get(0);
        ASTNode block = stmt.getChildren().get(1);

        if (evaluateCondition(condition)) {
            executeBlock(block);
        } else if (stmt.getChildren().size() > 2) {
                ASTNode elseBlock = stmt.getChildren().get(2);
                executeBlock(elseBlock);
        }
    }

    public void executeWhile(ASTNode stmt) {
        ASTNode condition = stmt.getChildren().get(0);
        ASTNode block = stmt.getChildren().get(1);

        while (evaluateCondition(condition)) {
            executeBlock(block);
        }
    }

    private boolean evaluateCondition(ASTNode node) {
        if (!node.getType().equals("BinOp")) {
            throw new RuntimeException("Condição inválida");
        }

        double left = evaluator.evaluate(node.getChildren().get(0));
        double right = evaluator.evaluate(node.getChildren().get(1));

        return switch (node.getValue()) {
            case "==" -> left == right;
            case "!=" -> left != right;
            case "<"  -> left < right;
            case ">"  -> left > right;
            case "<=" -> left <= right;
            case ">=" -> left >= right;
            default -> throw new RuntimeException("Operador inválido em condição: " + node.getValue());
        };
    }
    private void executeBlock(ASTNode block) {
        for (ASTNode child : block.getChildren()) {
            interpreter.executeStatement(child);
        }
    }
    public void executeFor(ASTNode forNode) {
        String varName = forNode.getValue(); // Ex: "i"
        ASTNode inicioNode = forNode.getChildren().get(0);
        ASTNode fimNode = forNode.getChildren().get(1);
        ASTNode corpo = forNode.getChildren().get(2);

        double inicio = evaluator.evaluate(inicioNode);
        double fim = evaluator.evaluate(fimNode);

        for (int i = (int) inicio; i <= fim; i++) {
            interpreter.getMemory().put(varName, i);
            if (!interpreter.getSymbolTable().isDeclared(varName)) {
                interpreter.getSymbolTable().declare(varName, "int");
            }
            for (ASTNode stmt : corpo.getChildren()) {
                interpreter.executeStatement(stmt);
            }
        }
    }
}
