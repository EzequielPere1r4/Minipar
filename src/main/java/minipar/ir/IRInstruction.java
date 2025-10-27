package minipar.ir;

public class IRInstruction {
    public OpCode opCode;
    public Operand result; // Onde o resultado é armazenado (pode ser var, temp, ou label)
    public Operand arg1;
    public Operand arg2;

    public IRInstruction(OpCode opCode, Operand result, Operand arg1, Operand arg2) {
        this.opCode = opCode;
        this.result = result;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    // Um toString() para facilitar a depuração e exibição
    @Override
    public String toString() {
        switch (opCode) {
            case STORE:
                return String.format("\t%s = %s", result, arg1);
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                return String.format("\t%s = %s %s %s", result, arg1, opCode, arg2);
            case LABEL:
                return String.format("%s:", result);
            case GOTO:
                return String.format("\tGOTO %s", result);
            case IF_FALSE_GOTO:
                return String.format("\tIF_FALSE %s GOTO %s", arg1, result);
            case PRINT:
                return String.format("\tPRINT %s", arg1);
            default:
                return opCode.toString();
        }
    }
}