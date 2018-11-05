package extractor.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import extractor.common.Common;
import extractor.common.MethodContent;
import extractor.visitors.FunctionVisitor;

@SuppressWarnings("StringEquality")
public class FeatureExtractor {
    private String code;
    private CompilationUnit compilationUnit;
    private static Set<String> parentTypeToAddChildId = Stream
            .of("AssignExpr", "ArrayAccessExpr", "FieldAccessExpr", "MethodCallExpr")
            .collect(Collectors.toCollection(HashSet::new));

    private final static String lparen = "(";
    private final static String rparen = ")";
    private final static String upSymbol = "^";
    private final static String downSymbol = "_";

    public FeatureExtractor(String code) {
        this.code = code;
    }

    public CompilationUnit getParsedFile() {
        return compilationUnit;
    }

    public ArrayList<ProgramFeatures> extractFeatures() throws ParseException, IOException {
        compilationUnit = parseFileWithRetries(code);
        FunctionVisitor functionVisitor = new FunctionVisitor();

        functionVisitor.visit(compilationUnit, null);

        ArrayList<MethodContent> methods = functionVisitor.getMethodContents();
        ArrayList<ProgramFeatures> programs = generatePathFeatures(methods);

        return programs;
    }

    private CompilationUnit parseFileWithRetries(String code) throws IOException {
        final String classPrefix = "public class A {";
        final String classSuffix = "}";
        final String methodPrefix = "SomeUnknownReturnType f() {";
        final String methodSuffix = "return noSuchReturnValue; }";

        String originalContent = code;
        String content = originalContent;
        CompilationUnit parsed = null;
        try {
            parsed = JavaParser.parse(content);
        } catch (ParseProblemException e1) {
            // Wrap with a class and method
            try {
                content = classPrefix + methodPrefix + originalContent + methodSuffix + classSuffix;
                parsed = JavaParser.parse(content);
            } catch (ParseProblemException e2) {
                // Wrap with a class only
                content = classPrefix + originalContent + classSuffix;
                parsed = JavaParser.parse(content);
            }
        }
        return parsed;
    }

    public ArrayList<ProgramFeatures> generatePathFeatures(ArrayList<MethodContent> methods) {
        ArrayList<ProgramFeatures> methodsFeatures = new ArrayList<>();
        for (MethodContent content : methods) {
            if (content.getLength() < 1
                    || content.getLength() > 10000)
                continue;
            ProgramFeatures singleMethodFeatures = generatePathFeaturesForFunction(content);
            if (!singleMethodFeatures.isEmpty()) {
                methodsFeatures.add(singleMethodFeatures);
            }
        }
        return methodsFeatures;
    }

    private ProgramFeatures generatePathFeaturesForFunction(MethodContent methodContent) {
        ArrayList<Node> functionLeaves = methodContent.getLeaves();
        ProgramFeatures programFeatures = new ProgramFeatures(methodContent.getName());

        for (int i = 0; i < functionLeaves.size(); i++) {
            for (int j = i + 1; j < functionLeaves.size(); j++) {
                String separator = Common.EMPTY_STRING;

                String path = generatePath(functionLeaves.get(i), functionLeaves.get(j), separator);
                if (path != Common.EMPTY_STRING) {
                    Property source = functionLeaves.get(i).getUserData(Common.PROPERTY_KEY);
                    Property target = functionLeaves.get(j).getUserData(Common.PROPERTY_KEY);
                    programFeatures.addFeature(source, path, target);
                }
            }
        }
        return programFeatures;
    }

    private static ArrayList<Node> getTreeStack(Node node) {
        ArrayList<Node> upStack = new ArrayList<>();
        Node current = node;
        while (current != null) {
            upStack.add(current);
            current = current.getParentNode();
        }
        return upStack;
    }

    private String generatePath(Node source, Node target, String separator) {
        String down = downSymbol;
        String up = upSymbol;
        String startSymbol = lparen;
        String endSymbol = rparen;
        int maxLength = 8;
        int maxWidth = 2;

        StringJoiner stringBuilder = new StringJoiner(separator);
        ArrayList<Node> sourceStack = getTreeStack(source);
        ArrayList<Node> targetStack = getTreeStack(target);

        int commonPrefix = 0;
        int currentSourceAncestorIndex = sourceStack.size() - 1;
        int currentTargetAncestorIndex = targetStack.size() - 1;
        while (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0
                && sourceStack.get(currentSourceAncestorIndex) == targetStack.get(currentTargetAncestorIndex)) {
            commonPrefix++;
            currentSourceAncestorIndex--;
            currentTargetAncestorIndex--;
        }

        int pathLength = sourceStack.size() + targetStack.size() - 2 * commonPrefix;
        if (pathLength > maxLength) {
            return Common.EMPTY_STRING;
        }

        if (currentSourceAncestorIndex >= 0 && currentTargetAncestorIndex >= 0) {
            int pathWidth = targetStack.get(currentTargetAncestorIndex).getUserData(Common.CHILD_ID)
                    - sourceStack.get(currentSourceAncestorIndex).getUserData(Common.CHILD_ID);
            if (pathWidth > maxWidth) {
                return Common.EMPTY_STRING;
            }
        }

        for (int i = 0; i < sourceStack.size() - commonPrefix; i++) {
            Node currentNode = sourceStack.get(i);
            String childId = Common.EMPTY_STRING;
            String parentRawType = currentNode.getParentNode().getUserData(Common.PROPERTY_KEY).getRawType();
            if (i == 0 || parentTypeToAddChildId.contains(parentRawType)) {
                childId = saturateChildId(currentNode.getUserData(Common.CHILD_ID))
                        .toString();
            }
            stringBuilder.add(String.format("%s%s%s%s%s", startSymbol,
                    currentNode.getUserData(Common.PROPERTY_KEY).getType(), childId, endSymbol, up));
        }

        Node commonNode = sourceStack.get(sourceStack.size() - commonPrefix);
        String commonNodeChildId = Common.EMPTY_STRING;
        Property parentNodeProperty = commonNode.getParentNode().getUserData(Common.PROPERTY_KEY);
        String commonNodeParentRawType = Common.EMPTY_STRING;
        if (parentNodeProperty != null) {
            commonNodeParentRawType = parentNodeProperty.getRawType();
        }
        if (parentTypeToAddChildId.contains(commonNodeParentRawType)) {
            commonNodeChildId = saturateChildId(commonNode.getUserData(Common.CHILD_ID))
                    .toString();
        }
        stringBuilder.add(String.format("%s%s%s%s", startSymbol,
                commonNode.getUserData(Common.PROPERTY_KEY).getType(), commonNodeChildId, endSymbol));

        for (int i = targetStack.size() - commonPrefix - 1; i >= 0; i--) {
            Node currentNode = targetStack.get(i);
            String childId = Common.EMPTY_STRING;
            if (i == 0 || parentTypeToAddChildId.contains(currentNode.getUserData(Common.PROPERTY_KEY).getRawType())) {
                childId = saturateChildId(currentNode.getUserData(Common.CHILD_ID))
                        .toString();
            }
            stringBuilder.add(String.format("%s%s%s%s%s", down, startSymbol,
                    currentNode.getUserData(Common.PROPERTY_KEY).getType(), childId, endSymbol));
        }

        return stringBuilder.toString();
    }

    private Integer saturateChildId(int childId) {
        return Math.min(childId, Integer.MAX_VALUE);
    }
}