package helpers;

import org.tensorflow.Tensor;

import java.util.ArrayList;
import java.util.List;

public class TensorConverter {
    public static List<String> convertBinaryToString(Tensor tensor) {
        int predictionsCount = 10;
        int maxContextsSize = 200;
        byte[][][] predictionsMatrix = new byte[1][predictionsCount][maxContextsSize];
        tensor.copyTo(predictionsMatrix);
        ArrayList<String> predictions = new ArrayList<>();
        for (int i = 0; i < predictionsCount; i++) {
            String s = new String(predictionsMatrix[0][i]);
            if (!"".equals(s)) {
                predictions.add(s);
            }
        }
        return predictions;
    }
}
