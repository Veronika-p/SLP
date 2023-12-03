public class Main {
    public static void main(String[] args) {
    }
    public static List<double> ConvertImageToFunctionSignal(byte[,] image)
    {
        List<double> functionSignal = new List<double>();

        for (int i = 0; i < image.GetLength(0); i++)
            for (int j = 0; j < image.GetLength(1); j++)
                functionSignal.Add(image[i, j] == 0 ? 0.0 : 1.0);

        return functionSignal;
    }
}
public class Neuron
{
    /// <summary>
    /// Весовые коэффициенты (синаптические связи)
    /// </summary>
    public List<double> Weights { get; }
    /// <summary>
    /// Пороговое значение
    /// </summary>
    public double Bias { get; }
    /// <summary>
    /// Функция активации
    /// </summary>
    private Func<double, double> ActivationFunction { get; }
    /// <summary>
    /// Индуцированное локальное поле
    /// </summary>
    public double InducedLocalField { get; private set; }
    /// <summary>
    /// Локальный градиент
    /// </summary>
    public double LocalGradient { get; private set; }
}
public class Layer
{
    /// <summary>
    /// Нейроны
    /// </summary>
    public List<Neuron> Neurons { get; }
    /// <summary>
    /// Входной сигнал
    /// </summary>
    public List<double> InputSignals { get; set; }
}
public class Network
{
    /// <summary>
    /// Скрытые слои
    /// </summary>
    public List<Layer> HiddenLayers { get; }
    /// <summary>
    /// Выходной слой
    /// </summary>
    public Layer OutputLayer { get; }
    public Network(int inputLayerDimension, int outputLayerNeuronsCount, Func<double, double> outputActivationFunction, int[] hiddenLayersDimensions = null,
                   Func<double, double>[] hiddenActivationFunctions = null, double randomMinValue = 0.0, double randomMaxValue = 1.0)
    {
        Random random = new Random();

        // Если есть скрытые слои
        if (hiddenLayersDimensions != null)
        {
            HiddenLayers = new List<Layer>();

            // Сначала инициализируем первый скрытый слой

            // Количество весовых коэффициентов у каждого нейрона первого скрытого слоя равно количеству нейронов входного слоя
            HiddenLayers.Add(new Layer(CreateNeurons(hiddenLayersDimensions[0], inputLayerDimension, randomMinValue, randomMaxValue, hiddenActivationFunctions[0], random)));

            // Если скрытых слоев больше 1
            if (hiddenLayersDimensions.Length > 1)
            {
                // Количество весовых коэффициентов на втором и последующих скрытых слоях равно количеству нейронов на предыдущем скрытом слое
                // Еще раз, первый скрытый слой уже проинициализирован, поэтому начинаем со второго (h = 1)
                for (int h = 1; h < hiddenLayersDimensions.Length; h++)
                    HiddenLayers.Add(new Layer(CreateNeurons(hiddenLayersDimensions[h], hiddenLayersDimensions[h - 1], randomMinValue, randomMaxValue, hiddenActivationFunctions[h], random)));
            }

        }

        // Если есть скрытые слои, то количество весовых коэффицинтов у нейронов выходного слоя равно количеству нейронов последнего скрытого слоя
        // Если скрытых слоев нет, то количество весовых коэффицинтов у нейронов выходного слоя равно количеству входов сети
        int outputWeightsCount = hiddenLayersDimensions != null ? hiddenLayersDimensions.Last() : inputLayerDimension;

        OutputLayer = new Layer(CreateNeurons(outputLayerNeuronsCount, outputWeightsCount, randomMinValue, randomMaxValue, outputActivationFunction, random));
    }
    private List<Neuron> CreateNeurons(int neuronsCount, int weightsCount, double weightsMinValue, double weightsMaxValue, Func<double, double> activationFunction, Random random)
    {
        List<Neuron> neurons = new List<Neuron>();

        for (int i = 0; i < neuronsCount; i++)
        {
            List<double> weights = CreateRandomWeights(weightsCount, weightsMinValue, weightsMaxValue, random);
            neurons.Add(new Neuron(activationFunction, weights, CreateRandomValue(random, weightsMinValue, weightsMaxValue, i)));
        }

        return neurons;
    }
    private List<double> CreateRandomWeights(int weightsCount, double minValue, double maxValue, Random random)
    {
        List<double> weights = new List<double>();

        for (int i = 0; i < weightsCount; i++)
            weights.Add(CreateRandomValue(random, minValue, maxValue, i));

        return weights;
    }
    private double CreateRandomValue(Random random, double minValue, double maxValue, int currentIndex)
    {
        double randomDouble = random.NextDouble() * (maxValue - minValue) + minValue;

        if (currentIndex % 2 == 0) // Будем чередовать знаки через один
            return -randomDouble;

        return randomDouble;
    }
    public Network(List<Layer> HiddenLayers, Layer OutputLayer)
    {
        this.HiddenLayers = HiddenLayers;
        this.OutputLayer = OutputLayer;
    }
    public void WriteHiddenWeightsToCSVFile(string fileName)
    {
        if (HiddenLayers == null)
            return;

        TextWriter textWriter = new StreamWriter(fileName);

        textWriter.WriteLine(string.Format("{0};{1}", "hiddenLayersDimensions", string.Join(";", HiddenLayers.Select(x => x.Neurons.Count))));

        foreach (Layer hiddenLayer in HiddenLayers)
        foreach (Neuron neuron in hiddenLayer.Neurons)
        textWriter.WriteLine("{0};{1}", neuron.Bias, string.Join(";", neuron.Weights));

        textWriter.Close();
    }

    /// <summary>
    /// Записывает пороговые значения и весовые коэффициенты выходного слоя сети в файл
    /// </summary>
    /// <param name="fileName">имя файла для записи</param>
    public void WriteOutputWeightsToCSVFile(string fileName)
    {
        TextWriter textWriter = new StreamWriter(fileName);

        foreach (Neuron neuron in OutputLayer.Neurons)
        textWriter.WriteLine("{0};{1}", neuron.Bias, string.Join(";", neuron.Weights));

        textWriter.Close();
    }
    public List<double> MakePropagateForward(List<double> functionSignal)
    {
        // Если имеются скрытые слои, то передаем сигнал по скрытым слоям
        if (HiddenLayers != null)
            foreach (Layer hiddenLayer in HiddenLayers)
        functionSignal = SetInputSignalAndInducedLocalFieldAndReturnOutputSignal(hiddenLayer, functionSignal);

        // Возвращаем сигнал от выходного слоя
        return SetInputSignalAndInducedLocalFieldAndReturnOutputSignal(OutputLayer, functionSignal);
    }
    private List<double> SetInputSignalAndInducedLocalFieldAndReturnOutputSignal(Layer layer, List<double> functionSignal)
    {
        layer.InputSignals = functionSignal;

        foreach (Neuron neuron in layer.Neurons)
        neuron.SetInducedLocalField(functionSignal);

        return layer.ProduceSignals();
    }
    public void Train(string imagesFileName, string labelsFileName, double learningRateParameter, int numberOfEpochs)
    {
        for (int e = 0; e < numberOfEpochs; e++)
        {
            // Получаем изображения
            IEnumerable<TestCase> testCases = FileReaderMNIST.LoadImagesAndLables(labelsFileName, imagesFileName);

            foreach (TestCase test in testCases)
            {
                // Конвертируем изображение в функциональный сигнал
                List<double> functionSignal = ImageHelper.ConvertImageToFunctionSignal(test.Image);
                // Получаем ожидаемый ответ
                List<double> desiredResponse = GetDesiredResponse(test.Label);
                // Получаем ответ от сети (прямой проход)
                List<double> outputSignal = MakePropagateForward(functionSignal);
                // Вычисляем сигнал ошибки как разность между ожидаемым и фактическим ответом нейросети
                List<double> errorSignal = GetErrorSignal(desiredResponse, outputSignal);
                // Запускаем алгоритм обратного распространения ошибки
                MakePropagateBackward(errorSignal, learningRateParameter);
            }

            Console.WriteLine("epoch " + e.ToString() + " finished"); // Выводим в консоль прогресс выполнения
        }
    }
}
