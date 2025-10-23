package minipar.parser;

import minipar.lexer.*;
import java.util.*;

public class StatementParser {
    private final Parser parser;

    public StatementParser(Parser parser) {
        this.parser = parser;
    }

    public ASTNode parseIdentifierStatement() {
        Token next = parser.peek();
        if (next == null) throw parser.error("Token inesperado após identificador");

        return switch (next.getValue()) {
            case "=" -> parseAssignment();
            case "." -> parseChannelOperation();
            case "(" -> parseFunctionCall(parser.expect(TokenType.IDENTIFIER).getValue());
            case "[" -> parseIndexAssignment();
            default -> throw parser.error("Atribuição inválida ou comando desconhecido após '" + parser.current().getValue() + "'");
        };
    }

    public ASTNode parseKeywordStatement(String keyword) {
        return switch (keyword) {
            case "c_channel" -> parseChannelDeclaration();
            case "print"     -> parsePrint();
            case "if"        -> parseConditional("if");
            case "while"     -> parseConditional("while");
            case "def"       -> parseFunction();
            case "for"       -> parseFor();
            case "return"    -> parseReturn();
            case "import" -> parseImport();
            default -> throw parser.error("Palavra-chave desconhecida: " + keyword);
        };
    }

    private ASTNode parseAssignment() {
        Token var = parser.expect(TokenType.IDENTIFIER);

        if (parser.match("[")) {
            ASTNode index = parser.parseExpression();
            parser.expect(TokenType.DELIMITER, "]");
            parser.expect(TokenType.OPERATOR, "=");
            ASTNode expr = parser.parseExpression();
            return parser.createNode("AtribuicaoIndice", var.getValue(), index, expr);
        }

        parser.expect(TokenType.OPERATOR, "=");
        ASTNode expr = parser.parseExpression();
        ASTNode varNode = new ASTNode("Variavel", var.getValue());
        return parser.createNode("Atribuicao", "", varNode, expr);
    }

    private ASTNode parseChannelDeclaration() {
        parser.expect(TokenType.KEYWORD, "c_channel");
        Token canal = parser.expect(TokenType.IDENTIFIER);
        Token comp1 = parser.expect(TokenType.IDENTIFIER);
        Token comp2 = parser.expect(TokenType.IDENTIFIER);

        ASTNode node = new ASTNode("c_channel", canal.getValue());
        node.addChild(new ASTNode("Comp1", comp1.getValue()));
        node.addChild(new ASTNode("Comp2", comp2.getValue()));
        return node;
    }

    private ASTNode parseChannelOperation() {
        Token canal = parser.expect(TokenType.IDENTIFIER);
        parser.expect(TokenType.OPERATOR, ".");
        Token operacao = parser.expect(TokenType.IDENTIFIER);
        parser.expect(TokenType.DELIMITER, "(");
        Token argumento = parser.expect(TokenType.IDENTIFIER, TokenType.NUMBER);
        parser.expect(TokenType.DELIMITER, ")");

        ASTNode node = new ASTNode(operacao.getValue(), canal.getValue());
        node.addChild(new ASTNode(
                operacao.getValue().equals("send") ? "Valor" : "Variavel",
                argumento.getValue()
        ));
        return node;
    }

    private ASTNode parsePrint() {
        parser.expect(TokenType.KEYWORD, "print");
        parser.expect(TokenType.DELIMITER, "(");
        List<ASTNode> args = new ArrayList<>();
        if (!parser.peekIs(")")) {
            do args.add(parser.parseExpression()); while (parser.match(","));
        }
        parser.expect(TokenType.DELIMITER, ")");

        ASTNode node = new ASTNode("print", "");
        args.forEach(node::addChild);
        return node;
    }

    public ASTNode parseFunctionCall(String name) {
        parser.expect(TokenType.DELIMITER, "(");
        List<ASTNode> args = new ArrayList<>();
        if (!parser.peekIs(")")) {
            do args.add(parser.parseExpression()); while (parser.match(","));
        }
        parser.expect(TokenType.DELIMITER, ")");
        ASTNode node = new ASTNode("ChamadaFuncao", name);
        args.forEach(node::addChild);
        return node;
    }

    private ASTNode parseFunction() {
        parser.expect(TokenType.KEYWORD, "def");
        String name = parser.expect(TokenType.IDENTIFIER).getValue();
        parser.expect(TokenType.DELIMITER, "(");
        List<String> params = new ArrayList<>();
        if (!parser.peekIs(")"))
            do params.add(parser.expect(TokenType.IDENTIFIER).getValue()); while (parser.match(","));
        parser.expect(TokenType.DELIMITER, ")");
        parser.expect(TokenType.DELIMITER, "{");

        ASTNode body = new ASTNode("Bloco", "");
        while (!parser.peekIs("}")) body.addChild(parser.parseStatement());
        parser.expect(TokenType.DELIMITER, "}");

        ASTNode func = new ASTNode("def", name);
        params.forEach(p -> func.addChild(new ASTNode("param", p)));
        func.addChild(body);
        return func;
    }

    private ASTNode parseReturn() {
        parser.expect(TokenType.KEYWORD, "return");
        ASTNode expr = parser.parseExpression();
        ASTNode node = new ASTNode("return", "");
        node.addChild(expr);
        return node;
    }

    private ASTNode parseConditional(String type) {
        parser.expect(TokenType.KEYWORD, type);
        ASTNode condition = parser.parseExpression();
        parser.expect(TokenType.DELIMITER, "{");

        ASTNode block = new ASTNode("Bloco", "");
        while (!parser.peekIs("}")) block.addChild(parser.parseStatement());
        parser.expect(TokenType.DELIMITER, "}");

        ASTNode node = new ASTNode(type, "");
        node.addChild(condition);
        node.addChild(block);

        if (parser.peekIs("else")) {
            parser.expect(TokenType.KEYWORD, "else");
            parser.expect(TokenType.DELIMITER, "{");

            ASTNode elseBlock = new ASTNode("Bloco", "");
            while (!parser.peekIs("}")) {
                elseBlock.addChild(parser.parseStatement());
            }
            parser.expect(TokenType.DELIMITER, "}");

            node.addChild(elseBlock);
        }

        return node;
    }
    private ASTNode parseIndexAssignment() {
        String varName = parser.expect(TokenType.IDENTIFIER).getValue();
        parser.expect(TokenType.DELIMITER, "[");
        ASTNode index = parser.parseExpression();
        parser.expect(TokenType.DELIMITER, "]");
        parser.expect(TokenType.OPERATOR, "=");
        ASTNode value = parser.parseExpression();

        ASTNode node = new ASTNode("AtribuicaoIndice", varName);
        node.addChild(index);
        node.addChild(value);
        return node;
    }
    private ASTNode parseFor() {
        parser.expect(TokenType.KEYWORD, "for");
        String varName = parser.expect(TokenType.IDENTIFIER).getValue();
        parser.expect(TokenType.OPERATOR, "=");
        ASTNode valorInicial = parser.parseExpression();
        parser.expect(TokenType.KEYWORD, "to");
        ASTNode valorFinal = parser.parseExpression();

        parser.expect(TokenType.DELIMITER, "{");
        ASTNode corpo = new ASTNode("Bloco", "");
        while (!parser.peekIs("}")) {
            corpo.addChild(parser.parseStatement());
        }
        parser.expect(TokenType.DELIMITER, "}");

        ASTNode forNode = new ASTNode("for", varName);
        forNode.addChild(valorInicial);  // filho 0
        forNode.addChild(valorFinal);    // filho 1
        forNode.addChild(corpo);         // filho 2

        return forNode;
    }
    private ASTNode parseImport() {
        parser.expect(TokenType.KEYWORD, "import");
        String path = parser.expect(TokenType.STRING).getValue();
        return new ASTNode("import", path.replace("\"", ""));
    }


}
