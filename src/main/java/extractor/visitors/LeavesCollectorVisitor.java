package extractor.visitors;

import java.util.ArrayList;
import java.util.List;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.TreeVisitor;
import extractor.common.Common;
import extractor.features.Property;

public class LeavesCollectorVisitor extends TreeVisitor {
    private ArrayList<Node> leaves = new ArrayList<>();
    private int currentId = 1;

    @Override
    public void process(Node node) {
        if (node instanceof Comment) {
            return;
        }
        boolean isLeaf = false;
        boolean isGenericParent = isGenericParent(node);
        if (hasNoChildren(node) && isNotComment(node)) {
            if (!node.toString().isEmpty() && (!"null".equals(node.toString()) || (node instanceof NullLiteralExpr))) {
                leaves.add(node);
                isLeaf = true;
            }
        }

        int childId = getChildId(node);
        node.setUserData(Common.CHILD_ID, childId);
        Property property = new Property(node, isLeaf, isGenericParent, currentId++);
        node.setUserData(Common.PROPERTY_KEY, property);
    }

    private boolean isGenericParent(Node node) {
        return (node instanceof ClassOrInterfaceType)
                && ((ClassOrInterfaceType)node).getTypeArguments() != null
                && ((ClassOrInterfaceType)node).getTypeArguments().size() > 0;
    }

    private boolean hasNoChildren(Node node) {
        return node.getChildrenNodes().size() == 0;
    }

    private boolean isNotComment(Node node) {
        return !(node instanceof Comment) && !(node instanceof Statement);
    }

    public ArrayList<Node> getLeaves() {
        return leaves;
    }

    private int getChildId(Node node) {
        Node parent = node.getParentNode();
        List<Node> parentsChildren = parent.getChildrenNodes();
        int childId = 0;
        for (Node child: parentsChildren) {
            if (child.getRange().equals(node.getRange())) {
                return childId;
            }
            childId++;
        }
        return childId;
    }
}
