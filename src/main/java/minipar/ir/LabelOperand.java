package minipar.ir;

public class LabelOperand implements Operand {
    public String name;
    public LabelOperand(String name) { this.name = name; }
    @Override public String toString() { return name + ":"; }
}