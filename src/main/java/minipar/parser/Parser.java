package minipar.parser;

import minipar.lexer.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;
    private final ExpressionParser expressionParser;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.expressionParser = new ExpressionParser(this);
    }

    public ASTNode parseProgram() {
        expect(TokenType.KEYWORD, "programa_minipar");
        ASTNode root = new ASTNode("Programa", "programa_minipar");
        while (!isAtEnd()) {
            root.addChild(parseBlock());
        }
        return root;
    }

    public ASTNode parseBlock() {
        Token token = current();
        String blockType = token.getValue(); // "SEQ" ou "PAR"
        expect(TokenType.KEYWORD, blockType);

        ASTNode blocoPrincipal = new ASTNode(blockType, "");

        // Se for SEQ: adiciona instruções diretamente
        if (blockType.equals("SEQ")) {
            while (!isAtEnd() && !(peekIs("SEQ") || peekIs("PAR") || peekIs("EOF"))) {
                blocoPrincipal.addChild(parseStatement());
            }
        }

        // Se for PAR: espera blocos SEQ internos
        else if (blockType.equals("PAR")) {
            while (!isAtEnd() && (peekIs("SEQ") || peekIs("PAR") || peekIs("EOF"))) {
                ASTNode seqBloco = parseBlock(); // Espera SEQ logo após PAR
                blocoPrincipal.addChild(seqBloco);
            }
        }

        return blocoPrincipal;
    }



    public ASTNode parseStatement() {
        Token token = current();
        if (token.getType() == TokenType.IDENTIFIER) {
            return new StatementParser(this).parseIdentifierStatement();
        }
        if (token.getType() == TokenType.KEYWORD) {
            return new StatementParser(this).parseKeywordStatement(token.getValue());
        }
        if (token.getType() == TokenType.COMMENT) {
            return new ASTNode("Comentario", consume().getValue());
        }
        throw error("Instrucao invalida: " + token.getValue());
    }

    // === DELEGAÇÃO PARA EXPRESSÕES ===
    public ASTNode parseExpression() {
        return expressionParser.parseExpression();
    }

    // ===================== Utilitários =========================
    public Token current() { return tokens.get(pos); }
    public Token previous() { return tokens.get(pos - 1); }
    public Token peek() { return pos + 1 < tokens.size() ? tokens.get(pos + 1) : null; }
    public Token consume() { return tokens.get(pos++); }

    public boolean match(String value) {
        if (!isAtEnd() && current().getValue().equals(value)) {
            consume(); return true;
        }
        return false;
    }

    public boolean peekIs(String value) { return current().getValue().equals(value); }

    public boolean peekNextIs(String value) {
        return pos + 1 < tokens.size() && tokens.get(pos + 1).getValue().equals(value);
    }

    public boolean isAtEnd() { return current().getType() == TokenType.EOF; }

    public Token expect(TokenType... types) {
        Token token = current();
        for (TokenType type : types)
            if (token.getType() == type) return consume();
        throw error("Esperado tipo: " + List.of(types) + ", encontrado: " + token.getType());
    }

    public Token expect(TokenType type, String value) {
        Token token = current();
        if (token.getType() == type && token.getValue().equals(value)) return consume();
        throw error("Esperado: " + value + ", encontrado: " + token.getValue());
    }

    public ASTNode createNode(String type, String value, ASTNode... children) {
        ASTNode node = new ASTNode(type, value);
        for (ASTNode child : children) node.addChild(child);
        return node;
    }

    public RuntimeException error(String message) {
        return new RuntimeException("Erro sintatico na linha " + current().getLine() + ": " + message);
    }

    public boolean isComparisonOperator(String op) {
        return switch (op) {
            case "==", "!=", "<", ">", "<=", ">=" -> true;
            default -> false;
        };
    }

    public int getPosition() { return pos; }
    public void setPosition(int pos) { this.pos = pos; }
    public List<Token> getTokens() { return tokens; }
}
