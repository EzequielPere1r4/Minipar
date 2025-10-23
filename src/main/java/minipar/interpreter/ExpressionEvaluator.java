package minipar.interpreter;

import minipar.parser.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExpressionEvaluator {

    private final Map<String, Object> memory;
    private List<Double> lastEvaluatedList;
    private FunctionExecutor functionExecutor;
    private final Scanner scanner;

    public ExpressionEvaluator(Map<String, Object> memory, FunctionExecutor functionExecutor, Scanner scanner) {
        this.memory = memory;
        this.functionExecutor = functionExecutor;
        this.scanner = scanner;
    }

    public double evaluate(ASTNode node) {
        return switch (node.getType()) {
            case "Valor" -> evaluateLiteral(node.getValue());
            case "Lista" -> { yield evaluateList(node); }
            case "input" -> { yield evaluateInput(); }
            case "BinOp" -> evaluateBinary(node);
            case "ChamadaFuncao" -> functionExecutor.call(node);
            default -> throw new RuntimeException("Expressão inválida: " + node.getType());
        };
    }

    private double evaluateList(ASTNode node) {
        lastEvaluatedList = new ArrayList<>();
        for (ASTNode item : node.getChildren()) {
            lastEvaluatedList.add(evaluate(item));
        }
        return 0; // valor da lista não é usado diretamente
    }
    private double evaluateInput() {
        System.out.print("Entrada: ");
        String line = scanner.nextLine();
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor de entrada inválido: " + line);
        }
    }

    private double evaluateLiteral(String val) {
        if (val.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(val);
        }
        if (val.startsWith("\"") && val.endsWith("\"")) {
            System.out.println(val.substring(1, val.length() - 1));
            return 0;
        }
        if (memory.containsKey(val)) {
            Object value = memory.get(val);
            if (value instanceof Double d) return d;
            if (value instanceof Integer i) return i.doubleValue();
            throw new RuntimeException("Valor da variável '" + val + "' não é inteiro");
        }
        throw new RuntimeException("Variável não declarada: " + val);
    }

    private double evaluateBinary(ASTNode node) {
        double left = evaluate(node.getChildren().get(0));
        double right = evaluate(node.getChildren().get(1));
        return switch (node.getValue()) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "^" -> Math.pow(left, right);
            case "/" -> right == 0 ? 0 : left / right;
            case "==" -> left == right ? 1 : 0;
            case "!=" -> left != right ? 1 : 0;
            case ">"  -> left > right ? 1 : 0;
            case "<"  -> left < right ? 1 : 0;
            case ">=" -> left >= right ? 1 : 0;
            case "<=" -> left <= right ? 1 : 0;
            default -> throw new RuntimeException("Operador inválido: " + node.getValue());
        };
    }

    public void setFunctionExecutor(FunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
    }
}
