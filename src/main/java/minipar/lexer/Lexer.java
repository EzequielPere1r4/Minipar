package minipar.lexer;

import java.util.*;
import java.util.regex.*;

public class Lexer {
    private final String source;
    private final Stack<Integer> indentStack = new Stack<>();
    private final List<Token> tokens = new ArrayList<>();
    private final Map<Pattern, TokenType> patterns = new LinkedHashMap<>();
    private final Set<String> keywords = Set.of(
            "programa_minipar", "SEQ", "PAR", "if", "else", "input", "while", "c_channel", "print", "def", "return", "for", "to", "import"
    );
    public Lexer(String source) {
        this.source = source;
        initPatterns();
    }

    private void initPatterns() {
        patterns.put(Pattern.compile("^#[^\n]*"), TokenType.COMMENT);
        patterns.put(Pattern.compile("^-?\\d+(\\.\\d+)?"), TokenType.NUMBER);
        patterns.put(Pattern.compile("^\"[^\"]*\""), TokenType.STRING);
        patterns.put(Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*"), TokenType.IDENTIFIER);
        patterns.put(Pattern.compile("^(==|!=|<=|>=|[+\\-*/=<>^])|^\\."), TokenType.OPERATOR); // ponto agora é operador
        patterns.put(Pattern.compile("^[\\[\\](){},]"), TokenType.DELIMITER);
        patterns.put(Pattern.compile("^\\s+"), TokenType.WHITESPACE);
    }

    public List<Token> tokenize() {
        String[] lines = source.split("\n");
        int lineNumber = 1;

        for (String line : lines) {
            String input = line;
            while (!input.isEmpty()) {
                boolean matched = false;
                for (Map.Entry<Pattern, TokenType> entry : patterns.entrySet()) {
                    Matcher matcher = entry.getKey().matcher(input);
                    if (matcher.find()) {
                        String value = matcher.group();
                        TokenType type = entry.getValue();

                        if (type == TokenType.IDENTIFIER && keywords.contains(value)) {
                            type = TokenType.KEYWORD;

                        }

                        if (type != TokenType.WHITESPACE) {
                            tokens.add(new Token(type, value, lineNumber));
                        }

                        input = input.substring(value.length());
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    throw new RuntimeException("Token inválido na linha " + lineNumber + ": " + input);
                }
            }
            lineNumber++;
        }

        tokens.add(new Token(TokenType.EOF, "EOF", lineNumber));
        return tokens;
    }
}
