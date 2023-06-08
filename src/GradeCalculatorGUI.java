import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class GradeCalculatorGUI extends JFrame {

    private JTextField nameField;
    private JTabbedPane tabbedPane;
    private JButton loadButton;
    private JButton saveButton;
    private JButton addButton;
    private JButton editButton;

    private String name;
    private File file;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean savedStatus = false;

    public GradeCalculatorGUI() {
        super("Grade Calculator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        nameField = new JTextField(20);
        topPanel.add(new JLabel("Name:"));
        topPanel.add(nameField);
        add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        loadButton = new JButton("Load Grades");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadGrades();
            }
        });
        buttonPanel.add(loadButton);

        saveButton = new JButton("Save Grades");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGrades();
            }
        });
        buttonPanel.add(saveButton);

        addButton = new JButton("Add Class");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addClass();
            }
        });
        buttonPanel.add(addButton);

        editButton = new JButton("Edit Class");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editClass();
            }
        });
        buttonPanel.add(editButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        setSize(450, 300);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    private void loadGrades() {
        name = nameField.getText();
        if (name.isEmpty()) {
            showMessage("Please enter a name.");
            return;
        }

        file = new File(name + "Grades.txt");
        if (!file.exists()) {
            showMessage("No previous file found for this name.");
            return;
        }

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            tabbedPane.removeAll();
            while ((line = reader.readLine()) != null) {
                String[] classData = line.split("\t");
                String className = classData[0];
                String minorGrades = classData[1];
                String majorGrades = classData[2];
                double minorWeight = Double.parseDouble(classData[3]);
                double majorWeight = Double.parseDouble(classData[4]);

                JTextArea classTextArea = new JTextArea();
                classTextArea.setEditable(false);
                appendInfoToTextArea(classTextArea,minorGrades,majorGrades,minorWeight,majorWeight);

                JScrollPane scrollPane = new JScrollPane(classTextArea);
                tabbedPane.addTab(className, scrollPane);
            }
            showMessage("Grades loaded successfully.");
            savedStatus = true;
        } catch (IOException e) {
            showMessage("Error loading grades: " + e.getMessage());
        } finally {
            closeReader();
        }
    }

    private void saveGrades() {
        name = nameField.getText();
        if (name.isEmpty()) {
            showMessage("Please enter a name.");
            return;
        }

        file = new File(name + "Grades.txt");
        try {
            writer = new PrintWriter(new FileWriter(file));
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                String className = tabbedPane.getTitleAt(i);
                JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
                JTextArea classTextArea = (JTextArea) scrollPane.getViewport().getView();
                String minorGrades = getGradesFromTextArea(classTextArea, "Minor Assessment Grades:");
                String majorGrades = getGradesFromTextArea(classTextArea, "Major Assessment Grades:");
                double minorWeight = getWeightFromTextArea(classTextArea, "Weight for Minor Assessments:");
                double majorWeight = getWeightFromTextArea(classTextArea, "Weight for Major Assessments:");
                double classAverage = getClassAverageFromTextArea(classTextArea);

                writer.write(className + "\t" + minorGrades + "\t" + majorGrades + "\t" +
                        minorWeight + "\t" + majorWeight + "\t" + classAverage + "\n");
            }
            showMessage("Grades saved successfully.");
            savedStatus = true;
        } catch (IOException e) {
            showMessage("Error saving grades: " + e.getMessage());
        } finally {
            closeWriter();
        }
    }

    private void addClass() {
        name = nameField.getText();
        if (name.isEmpty()) {
            showMessage("Please enter a name.");
            return;
        }

        String className = JOptionPane.showInputDialog(this, "Class Name:");
        if (className == null || className.isEmpty()) {
            showMessage("Please enter a class name.");
            return;
        }

        String minorGrades = JOptionPane.showInputDialog(this, "Minor Assessment Grades (comma-separated):");
        if (minorGrades == null) {
            return;
        }

        String majorGrades = JOptionPane.showInputDialog(this, "Major Assessment Grades (comma-separated):");
        if (majorGrades == null) {
            return;
        }

        String minorWeightString = JOptionPane.showInputDialog(this, "Weight for Minor Assessments (in percentage):");
        if (minorWeightString == null) {
            return;
        }

        double minorWeight;
        try {
            minorWeight = Double.parseDouble(minorWeightString);
            if (minorWeight < 0 || minorWeight > 100) {
                showMessage("Invalid weight. Weight must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showMessage("Invalid weight format. Please enter a number.");
            return;
        }

        double majorWeight;
        majorWeight = 100-minorWeight;
        showMessage("Major Weight Automatically Set as " + majorWeight + "%");

        JTextArea classTextArea = new JTextArea();
        classTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(classTextArea);
        tabbedPane.addTab(className, scrollPane);
        appendInfoToTextArea(classTextArea,minorGrades,majorGrades,minorWeight,majorWeight);
        showMessage("Class added successfully.");
        savedStatus = false;
    }

    private void editClass() {
        name = nameField.getText();
        if (name.isEmpty()) {
            showMessage("Please enter a name.");
            return;
        }

        String className = JOptionPane.showInputDialog(this, "Class Name (Case Sensitive):");
        if (className == null || className.isEmpty()) {
            showMessage("Please enter a class name.");
            return;
        }

        if(tabbedPane.indexOfTab(className) == -1){
            showMessage(className + " does not exist. Check capitalization.");
            return;
        }

        Object[] options = {"Delete", "Add Grades"};
        int result = JOptionPane.showOptionDialog(this,"What to do with " + className + "?", className, JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[1]);
        if(result == JOptionPane.YES_OPTION){
            result = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete " + className + "?", "Delete",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if(result == JOptionPane.YES_OPTION){
                int index = tabbedPane.indexOfTab(className);
                JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(index);
                tabbedPane.removeTabAt(index);
                showMessage("Successfully deleted " + className + "!");
                result = JOptionPane.showConfirmDialog(this, "Undo?", className, JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION){
                    tabbedPane.insertTab(className, null, scrollPane, null, index);
                }
            }
        }else if(result == JOptionPane.NO_OPTION) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                String tabClassName = tabbedPane.getTitleAt(i);
                if (tabClassName.equals(className)) {
                    JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(i);
                    JTextArea classTextArea = (JTextArea) scrollPane.getViewport().getView();
                    String pastMinorGrades = getGradesFromTextArea(classTextArea, "Minor Assessment Grades:");
                    String pastMajorGrades = getGradesFromTextArea(classTextArea, "Major Assessment Grades:");
                    double minorWeight = getWeightFromTextArea(classTextArea, "Weight for Minor Assessments:");
                    double majorWeight = getWeightFromTextArea(classTextArea, "Weight for Major Assessments:");
                    String minorGrades = JOptionPane.showInputDialog(this, "New Minor Assessment Grades (comma-separated):", pastMinorGrades);
                    if (minorGrades == null) {
                        return;
                    }
                    String majorGrades = JOptionPane.showInputDialog(this, "New Major Assessment Grades (comma-separated):", pastMajorGrades);
                    if (majorGrades == null) {
                        return;
                    }
                    classTextArea.setText("");
                    appendInfoToTextArea(classTextArea, minorGrades, majorGrades, minorWeight, majorWeight);
                    showMessage("Class edited successfully.");
                    savedStatus = false;
                    return;
                }
            }
        }
    }

    private void quit() {
        if(savedStatus == false && nameField.getText().length() > 0) {
            int result = JOptionPane.showConfirmDialog(this, "Do you want to save before quitting?", "Confirm Quit", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                name = nameField.getText();
                if (!name.isEmpty()) {
                    saveGrades();
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                } else {
                    showMessage("Please enter a name.");
                }
            } else if (result == JOptionPane.NO_OPTION) {
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            } else if (result == JOptionPane.CANCEL_OPTION) {
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        }else{
            int result = JOptionPane.showConfirmDialog(this,
                    "No changes are made. Do you want to quit now?",
                    "Confirm Quit",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            } else if (result == JOptionPane.NO_OPTION) {
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        }
    }

    private int countGrades(String grades) {
        if (grades.isEmpty()) {
            return 0;
        }
        return grades.split(",").length;
    }

    private double calculateAverage(String grades) {
        if (grades.isEmpty()) {
            return 0;
        }

        String[] gradeArray = grades.split(",");
        double sum = 0;
        for (String grade : gradeArray) {
            sum += Double.parseDouble(grade);
        }

        return Math.round((sum / gradeArray.length)*100.0)/100.0;
    }

    private void appendInfoToTextArea(JTextArea classTextArea, String minorGrades, String majorGrades, double minorWeight, double majorWeight){
        classTextArea.append("# Minor Assessments: " + countGrades(minorGrades) + "\n");
        classTextArea.append("# Major Assessments: " + countGrades(majorGrades) + "\n");
        classTextArea.append("Minor Assessment Grades: " + minorGrades + "\n");
        classTextArea.append("Major Assessment Grades: " + majorGrades + "\n");
        classTextArea.append("Weight for Minor Assessments: " + minorWeight + "%\n");
        classTextArea.append("Weight for Major Assessments: " + majorWeight + "%\n");
        double minorAverage = calculateAverage(minorGrades);
        double majorAverage = calculateAverage(majorGrades);
        double classAverage = Math.round(((minorAverage * minorWeight + majorAverage * majorWeight) / (minorWeight + majorWeight)) * 100.0) / 100;
        classTextArea.append("Class Average: " + classAverage + "%\n");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private double getWeightFromTextArea(JTextArea classTextArea, String weightLabel) {
        String text = classTextArea.getText();
        int weightStartIndex = text.indexOf(weightLabel) + weightLabel.length();
        int weightEndIndex = text.indexOf("%", weightStartIndex);
        String weightString = text.substring(weightStartIndex, weightEndIndex).trim();
        return Double.parseDouble(weightString);
    }

    private double getClassAverageFromTextArea(JTextArea classTextArea) {
        String text = classTextArea.getText();
        int averageIndex = text.indexOf("Class Average:") + "Class Average:".length();
        int percentageIndex = text.indexOf("%", averageIndex);
        String averageString = text.substring(averageIndex, percentageIndex).trim();
        return Double.parseDouble(averageString);
    }

    private String getGradesFromTextArea(JTextArea classTextArea, String gradesLabel) {
        String text = classTextArea.getText();
        int gradesStartIndex = text.indexOf(gradesLabel) + gradesLabel.length();
        int gradesEndIndex = text.indexOf("\n", gradesStartIndex);
        String gradesString = text.substring(gradesStartIndex, gradesEndIndex).trim();
        return gradesString;
    }

    private void closeReader() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                showMessage("Error closing reader: " + e.getMessage());
            }
        }
    }

    private void closeWriter() {
        if (writer != null) {
            writer.close();
        }
    }
}