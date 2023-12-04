import java.util.ArrayList;

public class Cognitron {
    private ArrayList<Double> inputLayer;
    private ArrayList<Double> outputLayer;
    private ArrayList<ArrayList<Double>> weights;

    public Cognitron(int inputSize, int outputSize) {
        inputLayer = new ArrayList<>();
        outputLayer = new ArrayList<>();
        weights = new ArrayList<>();
        for (int i = 0; i < inputSize; i++) {
            inputLayer.add(0.0);
        }
        for (int i = 0; i < outputSize; i++) {
            outputLayer.add(0.0);
            ArrayList<Double> row = new ArrayList<>();
            for (int j = 0; j < inputSize; j++) {
                row.add(0.0);
            }
            weights.add(row);
        }
    }

    public void train(ArrayList<Double> input, ArrayList<Double> output, double learningRate) {
        ArrayList<Double> outputCopy = new ArrayList<>(output);
        while (!outputCopy.equals(outputLayer)) {
            outputCopy = new ArrayList<>(outputLayer);
            ArrayList<Double> sumInputs = new ArrayList<>();
            for (int i = 0; i < outputLayer.size(); i++) {
                double sum = 0;
                for (int j = 0; j < inputLayer.size(); j++) {
                    sum += weights.get(i).get(j) * inputLayer.get(j);
                }
                sumInputs.add(sum);
            }
            for (int i = 0; i < outputLayer.size(); i++) {
                outputLayer.set(i, 1.0 / (1 + Math.exp(-sumInputs.get(i))));
                double error = output.get(i) - outputLayer.get(i);
                for (int j = 0; j < inputLayer.size(); j++) {
                    double delta = learningRate * error * outputLayer.get(i) * (1 - outputLayer.get(i)) * inputLayer.get(j);
                    double newWeight = weights.get(i).get(j) + delta;
                    weights.get(i).set(j, newWeight);
                }
            }
        }
    }

    public ArrayList<Double> predict(ArrayList<Double> input) {
        ArrayList<Double> output = new ArrayList<>();
        for (int i = 0; i < outputLayer.size(); i++) {
            double sum = 0;
            for (int j = 0; j < inputLayer.size(); j++) {
                sum += weights.get(i).get(j) * input.get(j);
            }
            output.add(1.0 / (1 + Math.exp(-sum)));
        }
        return output;
    }

    
}
