package minipar.interpreter;

import minipar.exceptions.ReturnException;
import minipar.parser.ASTNode;
import minipar.semantic.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionExecutor {

    private final Map<String, ASTNode> functions;
    private final Map<String, Object> memory;
    private final SymbolTable symbolTable;
    private final Interpreter interpreter;
    private final ExpressionEvaluator evaluator;

    public FunctionExecutor(Map<String, ASTNode> functions,
                            Map<String, Object> memory,
                            SymbolTable symbolTable,
                            Interpreter interpreter,
                            ExpressionEvaluator evaluator) {
        this.functions = functions;
        this.memory = memory;
        this.symbolTable = symbolTable;
        this.interpreter = interpreter;
        this.evaluator = evaluator;
    }

    public void register(ASTNode stmt) {
        String name = stmt.getValue();
        functions.put(name, stmt);
    }

    public double call(ASTNode node) {
        String nome = node.getValue();
        ASTNode func = functions.get(nome);
        if (func == null) throw new RuntimeException("Função não declarada: " + nome);

        List<String> parametros = new ArrayList<>();
        for (ASTNode paramNode : func.getChildren()) {
            if (paramNode.getType().equals("param")) {
                parametros.add(paramNode.getValue());
            }
        }

        List<ASTNode> argumentos = node.getChildren();
        if (parametros.size() != argumentos.size()) {
            throw new RuntimeException("Número de argumentos inválido para função " + nome);
        }

        // Salvar escopo atual
        Map<String, Object> backup = new HashMap<>(memory);

        // Novo escopo local
        for (int i = 0; i < parametros.size(); i++) {
            double val = evaluator.evaluate(argumentos.get(i));
            memory.put(parametros.get(i), val);
            if (!symbolTable.isDeclared(parametros.get(i))) {
                symbolTable.declare(parametros.get(i), "int");
            }
        }

        try {
            ASTNode corpo = func.getChildren().get(parametros.size());
            interpreter.executeBlock(corpo);
        } catch (ReturnException ret) {
            memory.clear();
            memory.putAll(backup);
            return ret.valor;
        }

        memory.clear();
        memory.putAll(backup);
        return 0;
    }
}
