package extractor.features;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import extractor.common.Common;

public class Property {
    private String rawType;
    private String type;
    private String name;
    private String splitName;
    private String operator;
    private static final HashSet<String> NUMERICAL_KEEP_VALUES = Stream.of("0", "1", "32", "64")
            .collect(Collectors.toCollection(HashSet::new));

    public Property(Node node, boolean isLeaf, boolean isGenericParent, int id) {
        Class<?> nodeClass = node.getClass();
        rawType = type = nodeClass.getSimpleName();
        if (node instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) node).isBoxedType()) {
            type = "PrimitiveType";
        }
        operator = "";
        if (node instanceof BinaryExpr) {
            operator = ((BinaryExpr) node).getOperator().toString();
        } else if (node instanceof UnaryExpr) {
            operator = ((UnaryExpr) node).getOperator().toString();
        } else if (node instanceof AssignExpr) {
            operator = ((AssignExpr) node).getOperator().toString();
        }
        if (operator.length() > 0) {
            type += ":" + operator;
        }

        String nameToSplit = node.toString();
        if (isGenericParent) {
            nameToSplit = ((ClassOrInterfaceType) node).getName();
            if (isLeaf) {
                type = "GenericClass";
            }
        }
        ArrayList<String> splitNameParts = Common.splitToSubtokens(nameToSplit);
        splitName = String.join(Common.INTERNAL_SEPARATOR, splitNameParts);

        name = Common.normalizeName(node.toString(), Common.BLANK);
        if (name.length() > Common.MAX_LABEL_LENGTH) {
            name = name.substring(0, Common.MAX_LABEL_LENGTH);
        } else if (node instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) node).isBoxedType()) {
            name = ((ClassOrInterfaceType) node).toUnboxedType().toString();
        }

        if (Common.isMethod(node, type)) {
            name = splitName = Common.METHOD_NAME;
        }

        if (splitName.length() == 0) {
            splitName = name;
            if (node instanceof IntegerLiteralExpr && !NUMERICAL_KEEP_VALUES.contains(splitName)) {
                splitName = "<NUM>";
            }
        }
    }

    String getRawType() {
        return rawType;
    }

    public String getType() {
        return type;
    }

    String getName() {
        return name;
    }
}
