package extractor.common;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.UserDataKey;
import extractor.features.Property;

public final class Common {
    public static final UserDataKey<Property> PROPERTY_KEY = new UserDataKey<Property>() {
    };

    public static final UserDataKey<Integer> CHILD_ID = new UserDataKey<Integer>() {
    };
    public static final String EMPTY_STRING = "";
    public static final String METHOD_DECLARATION = "METHOD_DECLARATION";
    public static final String NAME_EXPR = "NAME_EXPR";
    public static final String BLANK = "BLANK";
    public static final int MAX_LABEL_LENGTH = 50;
    public static final String METHOD_NAME = "METHOD_NAME";
    public static final String INTERNAL_SEPARATOR = "|";

    public static String normalizeName(String original, String defaultString) {
        original = original.toLowerCase().replaceAll("\\\\n", "") // escaped new
                // lines
                .replaceAll("//s+", "") // whitespaces
                .replaceAll("[\"',]", "") // quotes, apostrophes, commas
                .replaceAll("\\P{Print}", ""); // unicode weird characters
        String stripped = original.replaceAll("[^A-Za-z]", "");
        if (stripped.length() == 0) {
            String carefulStripped = original.replaceAll(" ", "_");
            if (carefulStripped.length() == 0) {
                return defaultString;
            } else {
                return carefulStripped;
            }
        } else {
            return stripped;
        }
    }

    public static boolean isMethod(Node node, String type) {
        Property parentProperty = node.getParentNode().getUserData(Common.PROPERTY_KEY);
        if (parentProperty == null) {
            return false;
        }

        String parentType = parentProperty.getType();
        return Common.NAME_EXPR.equals(type) && Common.METHOD_DECLARATION.equals(parentType);
    }

    public static ArrayList<String> splitToSubtokens(String str1) {
        String str2 = str1.trim();
        return Stream.of(str2.split("(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+"))
                .filter(s -> s.length() > 0).map(s -> Common.normalizeName(s, Common.EMPTY_STRING))
                .filter(s -> s.length() > 0).collect(Collectors.toCollection(ArrayList::new));
    }
}
