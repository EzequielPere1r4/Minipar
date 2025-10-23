package minipar.semantic;

import minipar.parser.ASTNode;

public class ChannelValidator {
    private final SymbolTable symbolTable;

    public ChannelValidator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void analyzeChannel(ASTNode stmt) {
        String name = stmt.getValue();
        if (symbolTable.isDeclared(name)) {
            throw new RuntimeException("Canal '" + name + "' ja declarado.");
        }
        symbolTable.declare(name, "canal");

        for (ASTNode compNode : stmt.getChildren()) {
            String comp = compNode.getValue();
            if (!symbolTable.isDeclared(comp)) {
                symbolTable.declare(comp, "computador");
            }
        }
    }

    public void analyzeSend(ASTNode stmt) {
        String canal = stmt.getValue();
        String valor = stmt.getChildren().get(0).getValue();

        if (!symbolTable.isDeclared(canal)) {
            throw new RuntimeException("Canal não declarado: " + canal);
        }
        if (!isLiteral(valor) && !symbolTable.isDeclared(valor)) {
            throw new RuntimeException("Valor a ser enviado não declarado: " + valor);
        }
    }

    public void analyzeReceive(ASTNode stmt) {
        String canal = stmt.getValue();
        String var = stmt.getChildren().get(0).getValue();

        if (!symbolTable.isDeclared(canal)) {
            throw new RuntimeException("Canal não declarado: " + canal);
        }
        if (!symbolTable.isDeclared(var)) {
            symbolTable.declare(var, "int");
        }
    }

    private boolean isLiteral(String val) {
        return val.matches("\\d+") || (val.startsWith("\"") && val.endsWith("\""));
    }
}
