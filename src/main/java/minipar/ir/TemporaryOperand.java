package minipar.ir;

public class TemporaryOperand implements Operand {
    public String name;
    public TemporaryOperand(String name) { this.name = name; }
    @Override public String toString() { return name; }
}