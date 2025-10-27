package minipar.ir;

public class ConstantOperand implements Operand {
    public Object value;
    public ConstantOperand(Object value) { this.value = value; }
    @Override public String toString() { return value.toString(); }
}