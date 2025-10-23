package minipar.parser;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private final String type;
    private final String value;
    private final List<ASTNode> children;

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }

    public void print(String prefix) {
        System.out.println(prefix + this);
        for (ASTNode child : children) {
            child.print(prefix + "  ");
        }
    }
    public void setChildren(List<ASTNode> children) {
        this.children.clear();
        this.children.addAll(children);
    }

}
