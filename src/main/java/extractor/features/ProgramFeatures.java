package extractor.features;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProgramFeatures {
    private String name;

    private ArrayList<ProgramRelation> features = new ArrayList<>();

    public ProgramFeatures(String name) {
        this.name = name;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(" ");
        stringBuilder.append(features.stream().map(ProgramRelation::toString).collect(Collectors.joining(" ")));

        return stringBuilder.toString();
    }

    public void addFeature(Property source, String path, Property target) {
        ProgramRelation newRelation = new ProgramRelation(source, target, path);
        features.add(newRelation);
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

}