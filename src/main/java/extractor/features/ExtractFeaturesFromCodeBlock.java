package extractor.features;

import extractor.common.Common;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExtractFeaturesFromCodeBlock {
    private String code;

    public ExtractFeaturesFromCodeBlock(String codeBlock) {
        this.code = codeBlock;
    }

    private ArrayList<ProgramFeatures> extractFromCodeBlock() throws IOException {
        FeatureExtractor featureExtractor = new FeatureExtractor(code);
        return featureExtractor.extractFeatures();
    }

    public String processCodeBlock() {
        ArrayList<ProgramFeatures> features;
        try {
            features = extractFromCodeBlock();
        } catch (IOException e) {
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

    private String featuresToString(ArrayList<ProgramFeatures> features) {
        if (features == null || features.isEmpty()) {
            return Common.EMPTY_STRING;
        }

        List<String> methodsOutputs = new ArrayList<>();

        for (ProgramFeatures singleMethodFeatures : features) {
            StringBuilder builder = new StringBuilder();
            String toPrint = Common.EMPTY_STRING;
            toPrint = singleMethodFeatures.toString();
            builder.append(toPrint);
            methodsOutputs.add(builder.toString());
        }
        return StringUtils.join(methodsOutputs, "\n");
    }
}

