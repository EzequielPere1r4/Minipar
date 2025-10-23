package minipar.gui;

import minipar.lexer.*;
import minipar.lexer.Token;
import minipar.parser.*;
import minipar.semantic.*;
import minipar.interpreter.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import com.formdev.flatlaf.FlatDarkLaf;

// ... [imports mantidos]
import javax.swing.border.EmptyBorder;

public class MiniParGUI extends JFrame {

    private RSyntaxTextArea codeArea;
    private JTextArea astArea;
    private JTextArea outputArea;
    private File currentFile = null;
    private JLabel statusLabel;

    public MiniParGUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("MiniPar - Interpretador Visual");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        codeArea = new RSyntaxTextArea(20, 60);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        RTextScrollPane codeScrollPane = new RTextScrollPane(codeArea);

        astArea = criarTextArea(false);
        outputArea = criarTextArea(false);

        // Painel com abas para Código + AST
        JTabbedPane topTabs = new JTabbedPane();
        topTabs.addTab("Código Fonte", codeScrollPane);
        topTabs.addTab("AST (Árvore Sintática)", new JScrollPane(astArea));

        // Painel inferior com botão e saída
        JPanel bottomPanel = new JPanel(new BorderLayout());
        outputArea.setBackground(new Color(30, 30, 30));
        outputArea.setForeground(Color.GREEN);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton executarBtn = new JButton("▶ Executar Código");
        executarBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        executarBtn.setFocusPainted(false);
        executarBtn.setBackground(new Color(0, 153, 76));
        executarBtn.setForeground(Color.WHITE);
        executarBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        executarBtn.addActionListener(e -> executarCodigo());

        runPanel.setBackground(new Color(45, 45, 45));
        runPanel.add(executarBtn);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setBorder(new EmptyBorder(4, 10, 4, 0));

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(runPanel, BorderLayout.EAST);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topTabs, outputScrollPane);
        splitPane.setDividerLocation(430);
        splitPane.setResizeWeight(0.8);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setJMenuBar(criarMenuBar());
    }

    private JTextArea criarTextArea(boolean editavel) {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Consolas", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(editavel);
        return area;
    }

    private JMenuBar criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem abrir = new JMenuItem("Abrir");
        JMenuItem salvar = new JMenuItem("Salvar como");
        JMenuItem sair = new JMenuItem("Sair");

        abrir.addActionListener(e -> carregarCodigo());
        salvar.addActionListener(e -> salvarCodigo());
        sair.addActionListener(e -> System.exit(0));

        menuArquivo.add(abrir);
        menuArquivo.add(salvar);
        menuArquivo.addSeparator();
        menuArquivo.add(sair);

        JMenu menuAjuda = new JMenu("Ajuda");
        JMenuItem sobre = new JMenuItem("Sobre");
        sobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "MiniPar IDE\nDesenvolvido em Java\nVersão 1.0", "Sobre", JOptionPane.INFORMATION_MESSAGE));
        menuAjuda.add(sobre);

        menuBar.add(menuArquivo);
        menuBar.add(menuAjuda);

        return menuBar;
    }

    private void carregarCodigo() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try {
                String codigoFonte = Files.readString(currentFile.toPath());
                codeArea.setText(codigoFonte);
                setTitle("MiniPar - " + currentFile.getName());
            } catch (IOException ex) {
                mostrarErro("Erro ao ler o arquivo: " + ex.getMessage());
            }
        }
    }

    private void salvarCodigo() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                Files.writeString(file.toPath(), codeArea.getText());
                JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso.");
            } catch (IOException ex) {
                mostrarErro("Erro ao salvar o arquivo: " + ex.getMessage());
            }
        }
    }

    private void executarCodigo() {
        astArea.setText("");
        outputArea.setText("");
        String codigoFonte = codeArea.getText();

        if (codigoFonte.isBlank()) {
            mostrarErro("Nenhum código fornecido.");
            return;
        }

        try {
            // Redireciona entradas simuladas, se houver input()
            InputStream simulatedIn = simulateInputs(codigoFonte);
            InputStream originalIn = System.in;
            System.setIn(simulatedIn);

            Lexer lexer = new Lexer(codigoFonte);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseProgram();
            astArea.setText(astToString(ast, ""));

            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analyze(ast);

            PrintStream originalOut = System.out;
            ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputCapture));

            Interpreter interpreter = new Interpreter();
            interpreter.execute(ast);

            System.setOut(originalOut);
            System.setIn(originalIn); // restaurar entrada
            outputArea.setText(outputCapture.toString());

        } catch (Exception e) {
            mostrarErro("Erro ao executar: " + e.getMessage());
        }
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private String astToString(ASTNode node, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(node.getType());
        if (!node.getValue().isEmpty()) {
            sb.append(" (").append(node.getValue()).append(")");
        }
        sb.append("\n");
        for (ASTNode child : node.getChildren()) {
            sb.append(astToString(child, indent + "  "));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MiniParGUI::new);
    }
    private InputStream simulateInputs(String code) {
        int inputCount = countOccurrences(code, "input()");
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= inputCount; i++) {
            String value = JOptionPane.showInputDialog(this, "Entrada " + i + ":");
            if (value == null) value = ""; // usuário cancelou
            sb.append(value).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private int countOccurrences(String str, String sub) {
        int count = 0, idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}

