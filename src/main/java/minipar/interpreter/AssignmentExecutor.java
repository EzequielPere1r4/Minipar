package minipar.interpreter;

import minipar.parser.ASTNode;
import minipar.semantic.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentExecutor {

    private final Map<String, Object> memory;
    private final SymbolTable symbolTable;
    private final ExpressionEvaluator evaluator;

    public AssignmentExecutor(Map<String, Object> memory, SymbolTable symbolTable, ExpressionEvaluator evaluator) {
        this.memory = memory;
        this.symbolTable = symbolTable;
        this.evaluator = evaluator;
    }

    public void executeAssignment(ASTNode stmt) {
        String var = stmt.getChildren().get(0).getValue();
        ASTNode expr = stmt.getChildren().get(1);

        Object value;

        if (expr.getType().equals("Lista")) {
            List<Double> lista = new ArrayList<>();
            for (ASTNode item : expr.getChildren()) {
                lista.add(evaluator.evaluate(item));
            }
            value = lista;
        } else {
            value = evaluator.evaluate(expr); // int ou retorno de função
        }

        memory.put(var, value);

        if (!symbolTable.isDeclared(var)) {
            String tipo = (value instanceof List) ? "lista" : "int";
            symbolTable.declare(var, tipo);
        }
    }

    public void executeIndexAssignment(ASTNode stmt) {
        String nome = stmt.getValue();
        double index = evaluator.evaluate(stmt.getChildren().get(0));
        double valor = evaluator.evaluate(stmt.getChildren().get(1));

        if (!memory.containsKey(nome)) throw new RuntimeException("Lista não declarada: " + nome);

        Object objeto = memory.get(nome);
        if (!(objeto instanceof List<?>)) {
            throw new RuntimeException("Variável '" + nome + "' não é uma lista");
        }
        @SuppressWarnings("unchecked")
        List<Double> lista = (List<Double>) objeto;

        int i = (int) index;
        if (i < 0 || i >= lista.size()) {
            throw new RuntimeException("Índice fora dos limites da lista");
        }

        lista.set(i, valor);
    }
}
