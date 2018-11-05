package extractor.features;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import extractor.common.Common;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExtractFeaturesFromCodeBlock {
    String code;
    CompilationUnit m_CompilationUnit;

    public ExtractFeaturesFromCodeBlock(String codeBlock) {
        this.code = codeBlock;
    }

    public ArrayList<ProgramFeatures> extractFromCodeBlock() throws IOException, ParseException {
        FeatureExtractor featureExtractor = new FeatureExtractor(code);
        ArrayList<ProgramFeatures> features = featureExtractor.extractFeatures();
        m_CompilationUnit = featureExtractor.getParsedFile();
        return features;
    }

    public String processCodeBlock() {
        ArrayList<ProgramFeatures> features;
        try {
            features = extractFromCodeBlock();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return "";
        }
        if (features == null) {
            return "";
        }

        String toPrint = featuresToString(features);
        if (toPrint.length() > 0) {
            return toPrint;
        }
        return "";
    }

    public String featuresToString(ArrayList<ProgramFeatures> features) {
        if (features == null || features.isEmpty()) {
            return Common.EMPTY_STRING;
        }

        List<String> methodsOutputs = new ArrayList<>();

        for (ProgramFeatures singleMethodfeatures : features) {
            StringBuilder builder = new StringBuilder();

            String toPrint = Common.EMPTY_STRING;
            toPrint = singleMethodfeatures.toString();
            builder.append(toPrint);
            methodsOutputs.add(builder.toString());
        }
        return StringUtils.join(methodsOutputs, "\n");
    }
}

