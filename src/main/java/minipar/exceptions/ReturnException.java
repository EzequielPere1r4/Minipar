package minipar.exceptions;

public class ReturnException extends RuntimeException {
    public final double valor;
    public ReturnException(double valor) {
        this.valor = valor;
    }
}
