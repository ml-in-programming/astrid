package extractor.visitors;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import extractor.common.Common;
import extractor.common.MethodContent;

public class FunctionVisitor extends VoidVisitorAdapter<Object> {
    private ArrayList<MethodContent> methods = new ArrayList<>();

    @Override
    public void visit(MethodDeclaration node, Object arg) {
        visitMethod(node, arg);

        super.visit(node, arg);
    }

    private void visitMethod(MethodDeclaration node, Object obj) {
        LeavesCollectorVisitor leavesCollectorVisitor = new LeavesCollectorVisitor();
        leavesCollectorVisitor.visitDepthFirst(node);
        ArrayList<Node> leaves = leavesCollectorVisitor.getLeaves();

        String normalizedMethodName = Common.normalizeName(node.getName(), Common.BLANK);
        ArrayList<String> splitNameParts = Common.splitToSubtokens(node.getName());
        String splitName = normalizedMethodName;
        if (splitNameParts.size() > 0) {
            splitName = String.join(Common.INTERNAL_SEPARATOR, splitNameParts);
        }

        if (node.getBody() != null) {
            methods.add(new MethodContent(leaves, splitName, getMethodLength(node.getBody().toString())));
        }
    }

    private long getMethodLength(String code) {
        String cleanCode = code.replaceAll("\r\n", "\n").replaceAll("\t", " ");
        if (cleanCode.startsWith("{\n"))
            cleanCode = cleanCode.substring(3).trim();
        if (cleanCode.endsWith("\n}"))
            cleanCode = cleanCode.substring(0, cleanCode.length() - 2).trim();
        if (cleanCode.length() == 0) {
            return 0;
        }
        long codeLength = Arrays.stream(cleanCode.split("\n"))
                .filter(line -> (!line.trim().equals("{") && !line.trim().equals("}") && !line.trim().equals("")))
                .filter(line -> !line.trim().startsWith("/") && !line.trim().startsWith("*")).count();
        return codeLength;
    }

    public ArrayList<MethodContent> getMethodContents() {
        return methods;
    }
}
