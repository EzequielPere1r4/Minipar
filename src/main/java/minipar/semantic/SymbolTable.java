package minipar.semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, String> table = new HashMap<>();

    public void declare(String name, String type) {
        if (table.containsKey(name)) {
            throw new RuntimeException("Simbolo '" + name + "' ja declarado.");
        }
        table.put(name, type);
    }

    public boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public String getType(String name) {
        if (!table.containsKey(name)) {
            throw new RuntimeException("Simbolo '" + name + "' nao declarado.");
        }
        return table.get(name);
    }

    public void print() {
        System.out.println("== Tabela de Simbolos ==");
        for (Map.Entry<String, String> entry : table.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
