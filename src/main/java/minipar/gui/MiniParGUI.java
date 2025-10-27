package minipar.gui;

import minipar.lexer.*;
import minipar.lexer.Token;
import minipar.parser.*;
import minipar.semantic.*;
import minipar.interpreter.*;

// --- NOVOS IMPORTS ---
import minipar.Config; // Importa a classe de configuração
import minipar.ir.*;      // Importa o pacote de Geração de IR
import minipar.backend.*; // Importa o pacote de Geração de Assembly
// --- FIM NOVOS IMPORTS ---

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.border.EmptyBorder;

public class MiniParGUI extends JFrame {

    private RSyntaxTextArea codeArea;
    private JTextArea astArea;
    private JTextArea outputArea;
    private JTextArea irArea; // NOVO: Para Código de 3 Endereços
    private JTextArea assemblyArea; // NOVO: Para Código Assembly
    private File currentFile = null;
    private JLabel statusLabel;

    public MiniParGUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("MiniPar - Compilador/Interpretador"); // Título atualizado
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
        irArea = criarTextArea(false); // NOVO
        assemblyArea = criarTextArea(false); // NOVO

        // Painel com abas para Código + AST + IR + Assembly
        JTabbedPane topTabs = new JTabbedPane();
        topTabs.addTab("Código Fonte", codeScrollPane);
        topTabs.addTab("AST (Árvore Sintática)", new JScrollPane(astArea));
        topTabs.addTab("Cód. 3 Endereços (IR)", new JScrollPane(irArea)); // NOVO
        topTabs.addTab("Cód. Assembly (ARMv7)", new JScrollPane(assemblyArea)); // NOVO

        // Painel inferior com botão e saída
        JPanel bottomPanel = new JPanel(new BorderLayout());
        outputArea.setBackground(new Color(30, 30, 30));
        outputArea.setForeground(Color.GREEN);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);

        JPanel runPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // Texto do botão atualizado para refletir os dois modos
        JButton executarBtn = new JButton("▶ Executar / Compilar");
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

    // --- MÉTODO EXECUTAR ATUALIZADO ---
    private void executarCodigo() {
        // Limpa todas as áreas de saída
        astArea.setText("");
        outputArea.setText("");
        irArea.setText("");       // NOVO
        assemblyArea.setText(""); // NOVO
        outputArea.setForeground(Color.GREEN); // Reseta a cor do console

        String codigoFonte = codeArea.getText();

        if (codigoFonte.isBlank()) {
            mostrarErro("Nenhum código fornecido.");
            return;
        }

        try {
            // --- FASES COMUNS (FRONT-END) ---
            // 1. Análise Léxica
            Lexer lexer = new Lexer(codigoFonte);
            List<Token> tokens = lexer.tokenize();

            // 2. Análise Sintática (Parsing)
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parseProgram();
            astArea.setText(astToString(ast, "")); // Exibe a AST

            // 3. Análise Semântica
            SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analyze(ast);

            // --- PONTO DE VARIAÇÃO (BACK-END) ---
            // Decide se vai INTERPRETAR ou COMPILAR
            if (Config.BACKEND == Config.BackendVariant.INTERPRETER) {
                
                // --- MODO INTERPRETADOR (CÓDIGO ANTIGO) ---
                statusLabel.setText("Modo: Interpretador");

                // Redireciona entradas simuladas, se houver input()
                InputStream simulatedIn = simulateInputs(codigoFonte);
                InputStream originalIn = System.in;
                System.setIn(simulatedIn);

                // Captura a saída do console
                PrintStream originalOut = System.out;
                ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
                System.setOut(new PrintStream(outputCapture));

                // 4. Executa o interpretador
                Interpreter interpreter = new Interpreter();
                interpreter.execute(ast);

                // Restaura saídas e entradas
                System.setOut(originalOut);
                System.setIn(originalIn);
                outputArea.setText(outputCapture.toString());

            } else if (Config.BACKEND == Config.BackendVariant.COMPILER) {
                
                // --- MODO COMPILADOR (CÓDIGO NOVO) ---
                statusLabel.setText("Modo: Compilador (ARMv7)");
                
                // 4. Gerar Código Intermediário (IR)
                IRGenerator irGen = new IRGenerator();
                List<IRInstruction> irCode = irGen.generate(ast);
                irArea.setText(irToString(irCode)); // Exibe o IR

                // 5. Gerar Código Assembly
                // **** LINHAS CORRIGIDAS ****
                ArmV7Generator asmGen = new ArmV7Generator(irCode);
                String assemblyCode = asmGen.generate();
                // **** FIM DA CORREÇÃO ****
                assemblyArea.setText(assemblyCode); // Exibe o Assembly

                // 6. Mostrar mensagem no console
                outputArea.setForeground(Color.CYAN); // Muda a cor para status
                outputArea.setText("Compilação concluída com sucesso.\n");
                outputArea.append("Código de 3 Endereços e Assembly ARMv7 gerados.");
            }

        } catch (Exception e) {
            mostrarErro("Erro ao executar: " + e.getMessage());
            e.printStackTrace(System.err); // Ajuda na depuração
        }
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // --- NOVO MÉTODO HELPER ---
    // Converte a lista de IR para uma String formatada
    private String irToString(List<IRInstruction> instructions) {
        StringBuilder sb = new StringBuilder();
        if (instructions == null) return "";
        for (IRInstruction instr : instructions) {
            sb.append(instr.toString()).append("\n");
        }
        return sb.toString();
    }
    // --- FIM NOVO MÉTODO HELPER ---

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