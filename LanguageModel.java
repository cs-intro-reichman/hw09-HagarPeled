import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        In in = new In(fileName);
        StringBuilder window = new StringBuilder();

        for (int i= 0; i < windowLength && !in.isEmpty(); i++) {
            char c = in.readChar();
            window.append(c);
        }
        while (!in.isEmpty()) {
            char nextChar = in.readChar();
            if (window.length() < windowLength) {
                window.append(nextChar);
            } else {
                String currentWindow = window.toString();
                List probs = CharDataMap.getOrDefault(currentWindow, new List());
                probs.update(nextChar);
                CharDataMap.put(currentWindow, probs);

                window.deleteCharAt(0);
                window.append(nextChar);
            }
        }
        
        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
    }
	


    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
    double count = 0;
    for (int i = 0; i <probs.getSize(); i++) {
        CharData ch = probs.get(i);
        if (ch != null) {
            count += ch.count;
        }
    }
    
    double totalCp = 0;
    for (int i = 0; i < probs.getSize(); i++) {
        CharData ch = probs.get(i);
        if (ch != null) {
            ch.p = (double) (ch.count / count);
            ch.cp = (double) (totalCp + ch.p);
            totalCp = ch.cp;
        }
    }
}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double randomValue = randomGenerator.nextDouble();

        ListIterator iterator = probs.listIterator(0);
    
        while (iterator.hasNext()) {
            CharData currentCharData = iterator.next();
            if (randomValue < currentCharData.cp) {
                return currentCharData.chr;
            }
        }
    
        return probs.get(probs.getSize() - 1).chr;
    }

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        int length = initialText.length(), i = 0;
        if (length < windowLength) return initialText;
        String generatedText = initialText, window = "";
        List probs;
        while (generatedText.length() - length < textLength) {
            window = generatedText.substring(i, i + windowLength);
            probs = CharDataMap.get(window);
            if (probs == null) return generatedText;
            generatedText += getRandomChar(probs);
            i++;
        }
    
        return generatedText;
    }


    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
    
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
    
        // Trains the model, creating the map.
        lm.train(fileName);
    
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
    }

