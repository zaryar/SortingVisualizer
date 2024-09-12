import javax.swing.*;
import java.awt.*;
import javax.sound.midi.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class SortingVisualizer extends JFrame {
    private int[] array;
    private JPanel arrayPanel;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int SLIDERMAX = 500;
    private final int SLIDERMIN = 1;
    private Synthesizer synth;
    private MidiChannel channel;
    private JTextField sizeField;
    private JSlider speedSlider;
    private JButton startButton;
    private JButton stopButton;
    private volatile boolean isSorting = false;
    private int highlightedIndex = -1; 
    private int highlightedIndex2 = -1; 
    private JComboBox<String> algorithmDropdown;

    public SortingVisualizer() {
        setTitle("Sorting Algorithm Visualizer");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeControls();
        initializeArrayPanel();
        initializeMIDI();

        pack();
    }

    private void initializeControls() {
        JPanel controlPanel = new JPanel();
        sizeField = new JTextField("50", 5);
        speedSlider = new JSlider(JSlider.HORIZONTAL, SLIDERMIN, SLIDERMAX, 100);
        startButton = new JButton("Start Sorting");
        stopButton = new JButton("Stop Sorting");

        controlPanel.add(new JLabel("Algorithm:"));
        String[] algorithms = { "Bubble Sort" , "Random Sort", "Insertion Sort", "Selection Sort", "Merge Sort", "Quick Sort", "Bucket Sort"};
        algorithmDropdown = new JComboBox<>(algorithms);

        controlPanel.add(new JLabel("Array Size:"));
        controlPanel.add(sizeField);
        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(speedSlider);
        
        controlPanel.add(algorithmDropdown);

        controlPanel.add(startButton);
        controlPanel.add(stopButton);


        startButton.addActionListener(e -> {
            if (!isSorting) {
                initializeArray();
                String selectedAlgorithm = (String) algorithmDropdown.getSelectedItem();
                if ("Bubble Sort".equals(selectedAlgorithm)) {
                    new Thread(this::bubbleSort).start();
                }
                if ("Random Sort".equals(selectedAlgorithm)) {
                    new Thread(this::randomSort).start();
                }
                if ("Insertion Sort".equals(selectedAlgorithm)) {
                    new Thread(this::insertionSort).start();
                }
                if("Selection Sort".equals(selectedAlgorithm)) {
                    new Thread(this::selectionSort).start();
                }
                if("Merge Sort".equals(selectedAlgorithm)) {
                    new Thread(this::mergeSort).start();
                }
                if("Quick Sort".equals(selectedAlgorithm)) {
                    new Thread(this::quickSort).start();
                }
                if("Bucket Sort".equals(selectedAlgorithm)) {
                    new Thread(this::bucketSort).start();
                }
            }
        });

        stopButton.addActionListener(e -> {
            isSorting = false;
        });

        add(controlPanel, BorderLayout.NORTH);
    }

    private void initializeArrayPanel() {
        arrayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (array != null) {
                    int barWidth = Math.max(1, getWidth() / array.length - 1);
                    for (int i = 0; i < array.length; i++) {
                        int x = i * (barWidth + 1);
                        int barHeight = array[i];
                        int y = getHeight() - barHeight;
                        if (i == highlightedIndex) {
                            g.setColor(Color.RED);
                            highlightedIndex = -1;
                        } 
                        else if (i == highlightedIndex2) {
                            g.setColor(Color.GREEN);
                            highlightedIndex2 = -1;
                        }
                        else {
                            g.setColor(Color.BLUE);
                        }
                        g.fillRect(x, y, barWidth, barHeight);
                    }
                }
            }
        };
        arrayPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT - 100));
        add(arrayPanel, BorderLayout.CENTER);
    }

    private void initializeArray() {
        int size = Integer.parseInt(sizeField.getText());
        array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = (int) (Math.random() * (arrayPanel.getHeight() - 20)) + 10;
        }
        arrayPanel.repaint();
    }

    private void initializeMIDI() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0); // Piano sound
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playNote(int height) {
        if (channel != null) {
            int note = 60 + (height * 36 / arrayPanel.getHeight());
            channel.noteOn(note, 64);
            try {
                Thread.sleep((SLIDERMAX+1 - speedSlider.getValue()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            channel.noteOff(note);
        }
    }

    public void bubbleSort() {
        isSorting = true;
        startButton.setEnabled(false);
        for (int i = 0; i < array.length - 1 && isSorting; i++) {
            for (int j = 0; j < array.length - i - 1 && isSorting; j++) {
                highlightedIndex = j + 1;
                arrayPanel.repaint();
                if (array[j] > array[j + 1]) {
                    arrayPanel.repaint();
                    swap(j, j + 1);
                    playNote(array[j]);
                    arrayPanel.repaint();
                    
                }
            }
        }
        for(int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }

    public void insertionSort() {
        isSorting = true;
        startButton.setEnabled(false);
        for (int i = 1; i < array.length && isSorting; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key && isSorting) {
                array[j + 1] = array[j];
                highlightedIndex = j;
                arrayPanel.repaint();
                playNote(array[j]);
                j--;
            }
            array[j + 1] = key;
            highlightedIndex = j + 1;
            arrayPanel.repaint();
            playNote(array[j + 1]);
        }
        for (int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }

    public void randomSort() {
        isSorting = true;
        startButton.setEnabled(false);
        boolean isSorted = false;
        while (!isSorted && isSorting) {
            isSorted = true;
            for (int i = 0; i < array.length - 1; i++) {
                highlightedIndex = i;
                arrayPanel.repaint();
                if (array[i] > array[i + 1]) {
                    isSorted = false;
                    break;
                }    
            }
            if (isSorted) {
                break;
            }
            
            int j = (int) (Math.random() * array.length);
            int i = (int) (Math.random() * array.length);
            highlightedIndex = i;
            arrayPanel.repaint();
            playNote(i);
            System.out.println(i + " " + j);
            swap(i, j);
            
        }
        for(int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }

    public void selectionSort() {
        isSorting = true;
        startButton.setEnabled(false);
        for (int i = 0; i < array.length - 1 && isSorting; i++) {
            int minIndex = i;
            for (int j = i + 1; j < array.length && isSorting; j++) {
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
                highlightedIndex = j;
                arrayPanel.repaint();
                playNote(array[j]);
            }
            swap(i, minIndex);
            highlightedIndex = i;
            arrayPanel.repaint();
            playNote(array[i]);
        }
        for (int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }

    public void mergeSort() {
        isSorting = true;
        startButton.setEnabled(false);
        mergeSortHelper(0, array.length - 1);
        for (int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }
    
    private void mergeSortHelper(int left, int right) {
        if (left < right && isSorting) {
            int mid = (left + right) / 2;
            mergeSortHelper(left, mid);
            mergeSortHelper(mid + 1, right);
            merge(left, mid, right);
        }
    }
    
    private void merge(int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
    
        int[] leftArray = new int[n1];
        int[] rightArray = new int[n2];
    
        for (int i = 0; i < n1; i++) {
            leftArray[i] = array[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = array[mid + 1 + j];
        }
    
        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2 && isSorting) {
            if (leftArray[i] <= rightArray[j]) {
                array[k] = leftArray[i];
                i++;
            } else {
                array[k] = rightArray[j];
                j++;
            }
            highlightedIndex = k;
            arrayPanel.repaint();
            playNote(array[k]);
            k++;
        }
    
        while (i < n1 && isSorting) {
            array[k] = leftArray[i];
            highlightedIndex = k;
            arrayPanel.repaint();
            playNote(array[k]);
            i++;
            k++;
        }
    
        while (j < n2 && isSorting) {
            array[k] = rightArray[j];
            highlightedIndex = k;
            arrayPanel.repaint();
            playNote(array[k]);
            j++;
            k++;
        }
    }

    public void quickSort() {
        isSorting = true;
        startButton.setEnabled(false);
        quickSortHelper(0, array.length - 1);
        for (int i = 0; i < array.length && isSorting; i++) {
            highlightedIndex = i;
            playNote(array[i]);
            arrayPanel.repaint();
        }
        isSorting = false;
        startButton.setEnabled(true);
    }
    
    private void quickSortHelper(int low, int high) {
        if (low < high && isSorting) {
            int pi = partition(low, high);
            quickSortHelper(low, pi - 1);
            quickSortHelper(pi + 1, high);
        }
    }
    
    private int partition(int low, int high) {
        int pivot = array[high];
        int i = (low - 1);
        for (int j = low; j < high && isSorting; j++) {
            if (array[j] < pivot) {
                i++;
                swap(i, j);
                highlightedIndex = j;
                arrayPanel.repaint();
                playNote(array[j]);
            }
        }
        swap(i + 1, high);
        highlightedIndex = i + 1;
        arrayPanel.repaint();
        playNote(array[i + 1]);
        return i + 1;
    }

    public void bucketSort() {
    isSorting = true;
    startButton.setEnabled(false);

    
    int maxValue = array[0];
    for (int i = 1; i < array.length; i++) {
        if (array[i] > maxValue) {
            maxValue = array[i];
        }
    }

    int bucketCount = maxValue / 10 + 1;
    @SuppressWarnings("unchecked")
    List<Integer>[] buckets = new ArrayList[bucketCount];
    for (int i = 0; i < bucketCount; i++) {
        buckets[i] = new ArrayList<>();
    }

    
    for (int i = 0; i < array.length && isSorting; i++) {
        int bucketIndex = array[i] / 10;
        buckets[bucketIndex].add(array[i]);
        highlightedIndex = i;
        arrayPanel.repaint();
        playNote(array[i]);
    }

    
    int index = 0;
    for (int i = 0; i < bucketCount && isSorting; i++) {
        Collections.sort(buckets[i]);
        for (int value : buckets[i]) {
            array[index++] = value;
            highlightedIndex = index - 1;
            arrayPanel.repaint();
            playNote(value);
        }
    }

    for (int i = 0; i < array.length && isSorting; i++) {
        highlightedIndex = i;
        playNote(array[i]);
        arrayPanel.repaint();
    }

    isSorting = false;
    startButton.setEnabled(true);
}

    private void swap(int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SortingVisualizer visualizer = new SortingVisualizer();
            visualizer.setVisible(true);
        });
    }

    @Override
    public void dispose() {
        isSorting = false;
        if (synth != null) {
            synth.close();
        }
        super.dispose();
    }
}