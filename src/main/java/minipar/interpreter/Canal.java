package minipar.interpreter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Canal {
    private final String nome;
    private final int porta;
    private final BlockingQueue<String> fila = new LinkedBlockingQueue<>();

    public Canal(String nome, int porta) {
        this.nome = nome;
        this.porta = porta;
    }

    public void send(String mensagem) {
        try {
            fila.put(mensagem);
            System.out.println("[CANAL] " + nome + " enviou: " + mensagem);
        } catch (InterruptedException e) {
            throw new RuntimeException("Erro ao enviar no canal '" + nome + "'", e);
        }
    }

    public String receive() {
        try {
            String recebido = fila.take(); // bloqueia até chegar mensagem
            System.out.println("[CANAL] " + nome + " recebeu: " + recebido);
            return recebido;
        } catch (InterruptedException e) {
            throw new RuntimeException("Erro ao receber no canal '" + nome + "'", e);
        }
    }

    public int getPorta() {
        return porta;
    }
}
