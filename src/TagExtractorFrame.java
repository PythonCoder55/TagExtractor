import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class TagExtractorFrame extends JFrame {
    JPanel mainPanel, topPanel, centerPanel, bottomPanel;

    JButton chooseFileBtn, exitBtn, chooseFilterBtn, saveBtn, filterBtn;

    JTextArea tagsTextArea;
    JScrollPane scrollPane;

    JFileChooser fileChooser = new JFileChooser();
    File selectedFile;
    String record = "";
    String stopWord = "";
    File currentDirectory = new File(System.getProperty("user.dir"));
    Path outputFile = Paths.get(currentDirectory.getPath() + "\\filteredWords.txt");
    ArrayList<String> savedRecords = new ArrayList<>();
    ArrayList<String> tagsList = new ArrayList<>();
    ArrayList<String> stopWordsList = new ArrayList<>();

    public TagExtractorFrame() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
    }

    public void createTopPanel() {
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 2));
        chooseFileBtn = new JButton("Choose File");
        chooseFilterBtn = new JButton("Choose Filter File");
        chooseFilterBtn.setEnabled(false);
        filterBtn = new JButton("Run");
        filterBtn.setEnabled(false);

        chooseFileBtn.addActionListener((ActionEvent ae) -> {
            try {
                currentDirectory = new File(System.getProperty("user.dir"));
                fileChooser.setCurrentDirectory(currentDirectory);
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    Path filePath = selectedFile.toPath();
                    InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath, CREATE));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while (bufferedReader.ready()) {
                        record = bufferedReader.readLine();
                        String cleanedRecord = record.replace("_", "");
                        String[] words = cleanedRecord.split("[^\\w']+");
                        if (cleanedRecord.length() != 0) {
                            for (String word : words) {
                                if (word.length() != 0) {
                                    String lowercaseWord = word.toLowerCase();
                                    tagsList.add(lowercaseWord);
                                }
                            }
                        }
                    }
                    bufferedReader.close();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to select a file to process.\nPlease run the program again.");
                    System.exit(0);
                }
                String fileName = String.valueOf(selectedFile);
                String[] fileNameParts = fileName.split("\\\\");
                int length = fileNameParts.length;

                tagsTextArea.setText("File Name: " + fileNameParts[length - 1] + "\n\n\n");
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "File not found!");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            chooseFilterBtn.setEnabled(true);
        });

        chooseFilterBtn.addActionListener((ActionEvent ae) -> {
            try {
                currentDirectory = new File(System.getProperty("user.dir"));
                fileChooser.setCurrentDirectory(currentDirectory);
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    Path filePath = selectedFile.toPath();
                    InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath, CREATE));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while (bufferedReader.ready()) {
                        stopWord = bufferedReader.readLine();
                        stopWordsList.add(stopWord);
                    }
                    bufferedReader.close();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to select a file to process.\nPlease run the program again.");
                    System.exit(0);
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "File not found!");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            filterBtn.setEnabled(true);
        });

        filterBtn.addActionListener((ActionEvent ae) ->
        {
            Map<String, Integer> wordFrequency = new TreeMap<>();

            for(String w : tagsList)
            {
                if(!stopWordsList.contains(w))
                {
                    if(wordFrequency.get(w) == null) {wordFrequency.put(w,1);}
                    else {wordFrequency.put(w, wordFrequency.get(w) + 1);}
                }
            }
            for(String keyString : wordFrequency.keySet())
            {
                tagsTextArea.append(keyString + " = " + wordFrequency.get(keyString) + "\n");
                savedRecords.add(keyString + " = " + wordFrequency.get(keyString));
            }
        });

        topPanel.add(chooseFileBtn);
        topPanel.add(chooseFilterBtn);
        topPanel.add(filterBtn);
    }

    public void createBottomPanel() {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 2));
        saveBtn = new JButton("Save File");
        exitBtn = new JButton("Exit");

        saveBtn.addActionListener((ActionEvent ae) -> {
            try {
                // Open the file for writing, truncating it if it already exists
                OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFile, CREATE, TRUNCATE_EXISTING));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                // Write the records to the file
                for (String savedRecord : savedRecords) {
                    String recordString = savedRecord;
                    bufferedWriter.write(recordString, 0, recordString.length());
                    bufferedWriter.newLine();
                }
                // Close the writer and show a message
                bufferedWriter.close();
                JOptionPane.showMessageDialog(null, "Data file saved!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        exitBtn.addActionListener((ActionEvent ae) -> System.exit(0));

        bottomPanel.add(saveBtn);
        bottomPanel.add(exitBtn);
    }

    public void createCenterPanel() {
        centerPanel = new JPanel();
        tagsTextArea = new JTextArea(30, 30);
        tagsTextArea.setEditable(false);
        scrollPane = new JScrollPane(tagsTextArea);
        centerPanel.add(scrollPane);
    }
}
