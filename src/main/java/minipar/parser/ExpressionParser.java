package minipar.parser;

import minipar.lexer.*;
import java.util.*;

public class ExpressionParser {
    private final Parser parser;

    public ExpressionParser(Parser parser) {
        this.parser = parser;
    }

    public ASTNode parseExpression() {
        ASTNode left = parseTerm();
        while (!parser.isAtEnd() && parser.isComparisonOperator(parser.current().getValue())) {
            String op = parser.consume().getValue();
            ASTNode right = parseTerm();
            left = parser.createNode("BinOp", op, left, right);
        }
        return left;
    }

    public ASTNode parseTerm() {
        ASTNode node = parseFactor();
        while (!parser.isAtEnd() && (parser.match("+") || parser.match("-"))) {
            String op = parser.previous().getValue();
            node = parser.createNode("BinOp", op, node, parseFactor());
        }
        return node;
    }

    public ASTNode parseFactor() {
        ASTNode node = parseExponent();
        while (!parser.isAtEnd() && (parser.match("*") || parser.match("/"))) {
            String op = parser.previous().getValue();
            node = parser.createNode("BinOp", op, node, parseExponent());
        }
        return node;
    }

    // ✅ NOVO: tratamento do operador de exponenciação "^"
    private ASTNode parseExponent() {
        ASTNode node = parsePrimary();
        while (!parser.isAtEnd() && parser.match("^")) {
            String op = parser.previous().getValue();
            node = parser.createNode("BinOp", op, node, parsePrimary());
        }
        return node;
    }

    public ASTNode parsePrimary() {
        Token token = parser.current();

        if (parser.match("(")) {
            ASTNode expr = parseExpression();
            parser.expect(TokenType.DELIMITER, ")");
            return expr;
        }

        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("input")) {
            parser.consume(); // consome 'input'
            parser.expect(TokenType.DELIMITER, "(");
            parser.expect(TokenType.DELIMITER, ")");
            return new ASTNode("input", "");
        }

        if (token.getType() == TokenType.IDENTIFIER) {
            String name = parser.consume().getValue();
            if (parser.match("[")) {
                ASTNode index = parseExpression();
                parser.expect(TokenType.DELIMITER, "]");
                return parser.createNode("Indexacao", name, index);
            }
            if (parser.peekIs("(")) {
                return new StatementParser(parser).parseFunctionCall(name);
            }
            return new ASTNode("Valor", name);
        }

        if (parser.match("[")) {
            List<ASTNode> elements = new ArrayList<>();
            if (!parser.peekIs("]")) {
                do elements.add(parseExpression());
                while (parser.match(","));
            }
            parser.expect(TokenType.DELIMITER, "]");
            ASTNode node = new ASTNode("Lista", "");
            node.setChildren(elements);
            return node;
        }

        if (token.getType() == TokenType.NUMBER || token.getType() == TokenType.STRING) {
            parser.consume();
            return new ASTNode("Valor", token.getValue());
        }

        throw parser.error("Expressão inválida");
    }
}
