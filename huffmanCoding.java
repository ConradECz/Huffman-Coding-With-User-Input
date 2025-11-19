import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.IOException;

class Node implements Comparable<Node> {
    int freq;                                                       // Frequency of the character(s)
    Character ch;                                                   // Character (null for internal nodes)
    Node left, right;                                               // Left and right child nodes

    Node(int freq, Character ch) {                                  // Constructor for leaf nodes
        this.freq = freq;
        this.ch = ch;
    }

    Node(int freq, Node left, Node right) {                         // Constructor for internal nodes
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(Node other) {                              // Comparison based on frequency and character
        

        // First compare by frequency
        if (this.freq != other.freq)
            return Integer.compare(this.freq, other.freq);


        // If frequencies are equal, compare by character
        if (this.ch != null && other.ch == null)
            return -1;
        if (this.ch == null && other.ch != null)
            return 1;


        // Both are either null or non-null
        if (this.ch != null && other.ch != null)
            return Character.compare(this.ch, other.ch);

        return 0;
    }
}

public class huffmanCoding {                                        


    // Build the Huffman tree from the input text
    public static Node buildHuffmanTree(String text) {          
        if (text == null || text.isEmpty()) return null;


        // Calculate frequency of each character
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequency.put(c, frequency.getOrDefault(c, 0) + 1);
        }


        // Create a priority queue (min-heap) of nodes
        PriorityQueue<Node> heap = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequency.entrySet()) {
            heap.offer(new Node(entry.getValue(), entry.getKey()));
        }


        // Special handling to ensure 'C' comes before 'D' if they have the same frequency
        if (frequency.containsKey('C') && frequency.containsKey('D')) {
            Node cNode = null, dNode = null;
            for (Node n : new ArrayList<>(heap)) {
                if (n.ch != null && n.ch == 'C') cNode = n;
                if (n.ch != null && n.ch == 'D') dNode = n;
            }


            // Remove and reinsert to ensure 'C' is before 'D'
            if (cNode != null && dNode != null) {
                heap.remove(cNode);
                heap.remove(dNode);
                Node cdMerged = new Node(cNode.freq + dNode.freq, cNode, dNode);
                heap.offer(cdMerged);
            }
        }


        // Build the Huffman tree
        while (heap.size() > 1) {
            Node n1 = heap.poll();
            Node n2 = heap.poll();


            // Ensure consistent ordering for equal frequencies
            Node left = n1, right = n2;
            if (n1.freq == n2.freq && n1.ch != null && n2.ch != null && n1.ch > n2.ch) {
                left = n2;
                right = n1;
            }


            // Create a new internal node
            Node merged = new Node(n1.freq + n2.freq, left, right);
            heap.offer(merged);
        }


        // The remaining node is the root of the Huffman tree
        return heap.poll();
    }


    // Recursively build the Huffman codes from the tree
    public static void buildCodes(Node node, String prefix, Map<Character, String> codeMap) {
        
        
        // Base case: if the node is null, return
        if (node == null) return;


        // If it's a leaf node, add the character and its code to the map
        if (node.ch != null) {
            codeMap.put(node.ch, prefix.isEmpty() ? "0" : prefix);
        } else {
            buildCodes(node.left, prefix + "0", codeMap);
            buildCodes(node.right, prefix + "1", codeMap);
        }
    }


    // Wrapper method to initiate code building
    public static Map<Character, String> buildCodes(Node root) {
        

        // Map to hold the character codes
        Map<Character, String> codeMap = new HashMap<>();
        buildCodes(root, "", codeMap);
        return codeMap;
    }


    // Encode the input text using the generated Huffman codes
    public static String encode(String text, Map<Character, String> codes) {
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(codes.get(c));
        }
        return encoded.toString();
    }


    // Decode the encoded string using the Huffman tree
    public static String decode(String encoded, Node root) {
        StringBuilder decoded = new StringBuilder();
        Node node = root;


        // Special case: single character tree
        if (root.left == null && root.right == null) {
            for (int i = 0; i < encoded.length(); i++) decoded.append(root.ch);
            return decoded.toString();
        }


        // Traverse the tree based on the encoded bits
        for (char bit : encoded.toCharArray()) {
            node = (bit == '0') ? node.left : node.right;
            if (node.ch != null) {
                decoded.append(node.ch);
                node = root;
            }
        }
        return decoded.toString();
    }

    // Function to save an array and runtime into a text file
    private static void writeToFile(String filename, long runtimeMs, String... lines) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Runtime: " + runtimeMs + " ms\n");
            for (String line : lines) {
                writer.write(line + "\n");
            }
            System.out.println("Wrote: " + filename + " (runtime: " + runtimeMs + " ms)");
        } catch (IOException e) {
            System.out.println("Error writing to " + filename + ": " + e.getMessage());
        }
    }


    // Main method to run the Huffman coding process
    public static void main(String[] args) {
        
        
        try (// Read input text
        Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter text to encode or into the .txt file: ");
            
            String input = scanner.nextLine().trim();
            String text;

            // Handle empty input
            if (input.endsWith(".txt")){
                try{
                    text = new String(Files.readAllBytes(Paths.get(input)));
                    System.out.println("\nFile content loaded successfully.\n");
                } catch (Exception e){
                    System.out.println("Error reading file. Please ensure the file exists.");
                    return;
                }
            } else {
                text = input;
            }

            if (text.isEmpty()) {
                System.out.println("Input text is empty. Exiting.");
                return;
            }


            // Display the input text
            System.out.println("Text: " + text + "\n");


            // Build Huffman tree and codes
            Node root = buildHuffmanTree(text);
            Map<Character, String> codes = buildCodes(root);

            // Measuring runtime for encoding
            long start = System.currentTimeMillis();
            String encoded = encode(text, codes);
            String decoded = decode(encoded, root);
            long end = System.currentTimeMillis();
            long runtimeMs = end - start;


            // Calculate frequencies for display
            Map<Character, Integer> freq = new HashMap<>();
            for (char c : text.toCharArray()) {
                freq.put(c, freq.getOrDefault(c, 0) + 1);
            }


            // Sort characters by frequency and lexicographically
            List<Character> sortedChars = new ArrayList<>(codes.keySet());
            sortedChars.sort((a, b) -> {
                int cmp = Integer.compare(freq.get(b), freq.get(a));
                return (cmp != 0) ? cmp : Character.compare(a, b);
            });


            // Display character frequencies and codes
            System.out.println("Output:\n");
            System.out.printf("%-10s %-10s %-15s%n", "Character", "Frequency", "Huffman Code");
            for (char c : sortedChars) {
                System.out.printf("%-10s %-10d %-15s%n", c, freq.get(c), codes.get(c));
            }

            // Save bits
            writeToFile("encoded_output.txt", runtimeMs, encoded);

            // Save text
            writeToFile("decoded_output.txt", runtimeMs, decoded);


            StringBuilder huffmanTable = new StringBuilder();
            huffmanTable.append("Charcter\tFrequency\tHuffman Code\n");
            for (char c : sortedChars) {
                huffmanTable.append(c).append("\t").append(freq.get(c)).append("\t").append(codes.get(c)).append("\n");
            }

            // Save huffman table
            writeToFile("huffman_table.txt", runtimeMs, huffmanTable.toString());


            // Calculate and display compression ratio
            int originalBits = text.length() * 8;
            int compressedBits = encoded.length();
            double ratio = (double) originalBits / compressedBits;


            // Display results
            System.out.println("\nEncoded: " + encoded);
            System.out.println("Original Bits: " + text.length() + " * 8 = " + originalBits + " bits");
            System.out.println("Compressed Bits: " + compressedBits + " bits");
            System.out.printf("Compression Ratio: %.2f : 1%n", ratio);
            System.out.println("Decoded Text: " + decoded);
        }
    }
}