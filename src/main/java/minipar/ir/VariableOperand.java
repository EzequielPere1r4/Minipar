package minipar.ir;

public class VariableOperand implements Operand {
    public String name;
    public VariableOperand(String name) { this.name = name; }
    @Override public String toString() { return name; }
}