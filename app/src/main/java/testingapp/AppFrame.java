package testingapp;

import java.awt.Container;
import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppFrame extends JFrame  {

    private static Logger logger = LoggerFactory.getLogger(DbLoad.class);
    private Container contentPanel;
    private JLabel title, inputLabel, outputLabel, dbLabel, busyLabel, title2, inputLabel2, outputLabel2 ,dbLabel2;
    private JTextField inputFolder, outputFolder, dbField, inputFolder2, outputFolder2, dbField2;
    private JButton inputButton, outputButton, submitButton, inputButton2, outputButton2, dbButton,submitButton2;
    private JRadioButton excelOption, csvOption;
    private ImageIcon redIcon;

    public AppFrame() {
        setTitle("APP");
        setBounds(300, 90, 900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        contentPanel = getContentPane();
        contentPanel.setLayout(null);

        /* DB Load Frame */

        title = new JLabel("LOAD DB");
        title.setFont(new Font("Arial", Font.PLAIN, 20));
        title.setSize(300, 30);
        title.setLocation(200, 30);
        contentPanel.add(title);

        inputLabel = new JLabel("Input Folder:");
        inputLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        inputLabel.setSize(150, 20);
        inputLabel.setLocation(100, 80);

        inputFolder = new JTextField("");
        inputFolder.setSize(190, 30);
        inputFolder.setLocation(250, 80);

        inputButton = new JButton();
        inputButton.setText("select");
        inputButton.setSize(80, 30);
        inputButton.setLocation(450, 80);
        inputButton.addActionListener((arg0) ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(contentPanel);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
               inputFolder.setText(file.getAbsolutePath());
            }else{
               inputFolder.setText("");
            }
        });

        excelOption = new JRadioButton();
        excelOption.setText("excel");
        excelOption.setSize(70,30);
        excelOption.setLocation(550, 80);
        excelOption.addActionListener((arg0) ->{
            if(excelOption.isSelected()){
                csvOption.setSelected(false);
            }
            else{
                csvOption.setSelected(true);
            }
        });
        csvOption = new JRadioButton();
        csvOption.setText("csv");
        csvOption.setSize(70,30);
        csvOption.setLocation(620, 80);
        csvOption.setSelected(true);
        csvOption.addActionListener((arg0) -> {
            if(csvOption.isSelected()){
                excelOption.setSelected(false);
            }
            else{
                excelOption.setSelected(true);
            }
        });
        
        contentPanel.add(excelOption);
        contentPanel.add(csvOption);
        contentPanel.add(inputLabel);
        contentPanel.add(inputButton);
        contentPanel.add(inputFolder);


        outputLabel = new JLabel("Output Folder:");
        outputLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        outputLabel.setSize(150, 20);
        outputLabel.setLocation(100, 130);

        outputFolder = new JTextField("");
        outputFolder.setSize(190, 30);
        outputFolder.setLocation(250, 130);

        outputButton = new JButton();
        outputButton.setText("select");
        outputButton.setSize(80, 30);
        outputButton.setLocation(450, 130);
        outputButton.addActionListener((arg0) ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(contentPanel);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
               outputFolder.setText(file.getAbsolutePath());
            }else{
               outputFolder.setText("");
            }
        });

        contentPanel.add(outputLabel);
        contentPanel.add(outputButton);
        contentPanel.add(outputFolder);


        dbLabel = new JLabel("Output DB:");
        dbLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        dbLabel.setSize(150, 20);
        dbLabel.setLocation(100, 180);

        dbField = new JTextField();
        dbField.setText("Test.db");
        dbField.setFont(new Font("Arial", Font.PLAIN, 20));
        dbField.setSize(190, 30);
        dbField.setLocation(250, 180);

        contentPanel.add(dbLabel);
        contentPanel.add(dbField);

        redIcon = createImageIcon("/waiting.gif", "Busy");      

        busyLabel = new JLabel(redIcon);
        busyLabel.setSize(100, 100);
        busyLabel.setLocation(450, 230);
        busyLabel.setVisible(false);
        contentPanel.add(busyLabel);

        submitButton = new JButton();
        submitButton.setText("SUBMIT");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 20));
        submitButton.setSize(120, 30);
        submitButton.setLocation(200, 230);
        submitButton.addActionListener((arg0) ->{
            if (inputFolder.getText().isBlank() || outputFolder.getText().isBlank() || dbField.getText().isBlank()){
                JOptionPane.showMessageDialog(this, "Enter all the fields");
            }
            else{
                int result = -1;
                String inputpath = inputFolder.getText(); 
                String outputpath = outputFolder.getText();    
                String dbname = dbField.getText();
                String dbpath = new File(outputpath, dbname).getAbsolutePath();
                File dir = new File(inputpath);
                if (excelOption.isSelected()){
                    File[] files = dir.listFiles((d, name)-> name.endsWith(".xlsx"));
                    if (files.length==0){
                        JOptionPane.showMessageDialog(this, "No Excel files found");
                    }
                    else{
                        system_offline();
                        result = DbLoad.run_excel(files, dbpath);
                        system_online();
                    }
                }
                else{
                    File[] files = dir.listFiles((d, name)-> name.endsWith(".csv"));
                    if (files.length==0){
                        JOptionPane.showMessageDialog(this, "No CSV files found");
                    }
                    else{
                        system_offline();
                        char delimiter = 'æ';
                        result = DbLoad.run_csv(files, dbpath, delimiter);
                        system_online();
                    }
                }
                if (result >= 0){
                    JOptionPane.showMessageDialog(this, "Action Completed");
                }
            } 
        });
        contentPanel.add(submitButton);


        /* Test Cycle Frame */

        title2 = new JLabel("RUN TESTS");
        title2.setFont(new Font("Arial", Font.PLAIN, 20));
        title2.setSize(300, 30);
        title2.setLocation(200, 300);
        contentPanel.add(title2);

        inputLabel2 = new JLabel("Input SQL(csv):");
        inputLabel2.setFont(new Font("Arial", Font.PLAIN, 20));
        inputLabel2.setSize(150, 20);
        inputLabel2.setLocation(100, 350);

        inputFolder2 = new JTextField("");
        inputFolder2.setSize(190, 30);
        inputFolder2.setLocation(250, 350);

        inputButton2 = new JButton();
        inputButton2.setText("select");
        inputButton2.setSize(80, 30);
        inputButton2.setLocation(450, 350);
        inputButton2.addActionListener((arg0) ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int option = fileChooser.showOpenDialog(contentPanel);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
               inputFolder2.setText(file.getAbsolutePath());
            }else{
               inputFolder2.setText("");
            }
        });
        contentPanel.add(inputLabel2);
        contentPanel.add(inputFolder2);
        contentPanel.add(inputButton2);


        outputLabel2 = new JLabel("Output Folder:");
        outputLabel2.setFont(new Font("Arial", Font.PLAIN, 20));
        outputLabel2.setSize(150, 20);
        outputLabel2.setLocation(100, 400);

        outputFolder2 = new JTextField("");
        outputFolder2.setSize(190, 30);
        outputFolder2.setLocation(250, 400);

        outputButton2 = new JButton();
        outputButton2.setText("select");
        outputButton2.setSize(80, 30);
        outputButton2.setLocation(450, 400);
        outputButton2.addActionListener((arg0) ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(contentPanel);
            if(option == JFileChooser.APPROVE_OPTION){
               File file = fileChooser.getSelectedFile();
               outputFolder2.setText(file.getAbsolutePath());
            }else{
               outputFolder2.setText("");
            }
        });

        contentPanel.add(outputLabel2);
        contentPanel.add(outputButton2);
        contentPanel.add(outputFolder2);



        dbLabel2 = new JLabel("DB Path:");
        dbLabel2.setFont(new Font("Arial", Font.PLAIN, 20));
        dbLabel2.setSize(150, 20);
        dbLabel2.setLocation(100, 450);

        dbField2 = new JTextField("");
        dbField2.setSize(190, 30);
        dbField2.setLocation(250, 450);

        dbButton = new JButton();
        dbButton.setText("select");
        dbButton.setSize(80, 30);
        dbButton.setLocation(450, 450);
        dbButton.addActionListener((arg0) ->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int option = fileChooser.showOpenDialog(contentPanel);
            if(option == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                dbField2.setText(file.getAbsolutePath());
            }else{
                dbField2.setText("");
            }
        });
        contentPanel.add(dbLabel2);
        contentPanel.add(dbField2);
        contentPanel.add(dbButton);


        submitButton2 = new JButton();
        submitButton2.setText("SUBMIT");
        submitButton2.setFont(new Font("Arial", Font.PLAIN, 20));
        submitButton2.setSize(120, 30);
        submitButton2.setLocation(200, 500);
        submitButton2.addActionListener((arg0) ->{
            if (inputFolder2.getText().isBlank() || dbField2.getText().isBlank() || outputFolder2.getText().isBlank()){
                JOptionPane.showMessageDialog(this, "Enter all the fields");
            }
            else{
                int result = -1;
                String inputpath = inputFolder2.getText(); 
                String outputfolder = outputFolder2.getText();    
                String dbpath = dbField2.getText();
                File file = new File(inputpath);
                system_offline();
                result = DbRead.run_queries(file, outputfolder, dbpath);
                system_online();
                if (result >= 0){
                    JOptionPane.showMessageDialog(this, "Action Completed");
                }
            } 
        });
        contentPanel.add(submitButton2);
        setVisible(true);
    }

    protected void system_online(){
        submitButton.setEnabled(true);
        submitButton2.setEnabled(true);
        busyLabel.setVisible(false);
    }

    protected void system_offline(){
        busyLabel.setVisible(true);
        submitButton.setEnabled(false);
        submitButton2.setEnabled(false);
    }

    protected ImageIcon createImageIcon(String path,
                                           String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            logger.error("Couldn't find file: " + path);
            return null;
        }
    }
}