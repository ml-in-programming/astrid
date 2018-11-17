package extractor.features;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProgramFeatures {
    private String name;

    private ArrayList<ProgramRelation> features = new ArrayList<>();

    ProgramFeatures(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String resultString = name + " " +
                features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" "));
        return resultString;
    }

    void addFeature(Property source, String path, Property target) {
        ProgramRelation newRelation = new ProgramRelation(source, target, path);
        features.add(newRelation);
    }

    boolean isEmpty() {
        return features.isEmpty();
    }

}
