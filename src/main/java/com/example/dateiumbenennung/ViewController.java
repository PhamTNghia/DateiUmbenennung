package com.example.dateiumbenennung;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class ViewController {
    @FXML
    AnchorPane anchorPane;

    private boolean hasCSVFile = false;
    ArrayList<String> CSVvalues = new ArrayList<>();
    File selectedDirectory;

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public void readCSVFile(File file){
        CSVvalues = new ArrayList<>();
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                CSVvalues.add(line);
                String[] splitValues = line.split(",");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void renamePDFFilesWithCondition(File directory, ArrayList<String> CSVvalues, String beginString){
        File[] pdfFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf") && name.contains(beginString);
            }
        });

        if (pdfFiles != null && pdfFiles.length > 0) {
            System.out.println("Renaming PDF files in directory: " + directory.getAbsolutePath());

            for (File pdfFile : pdfFiles) {
                String originalName = pdfFile.getName();
                String personalNumber = extractPersonalNumberFromFileName(originalName);

                if (personalNumber != null) {
                    // Compare extracted personal number with CSV values
                    for (int i = 0; i < CSVvalues.size(); i++) {
                        String csvEntry = CSVvalues.get(i);
                        String[] csvParts = csvEntry.split(",");

                        if (csvParts.length == 3 && csvParts[0].equals(personalNumber)) {
                            String vorname = csvParts[1];
                            String nachname = csvParts[2];

                            String newFileName = vorname + "_" + nachname + "_"+beginString+".pdf";
                            File newFile = new File(directory, newFileName);

                            // Execute renaming
                            if (pdfFile.renameTo(newFile)) {
                                System.out.println("Renamed " + originalName + " to " + newFileName);
                            } else {
                                System.out.println("Failed to rename " + originalName);
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            System.out.println("No PDF files found starting with"+ beginString +"in the directory.");
        }
    }

    public static void renamePDFFiles(File directory, ArrayList<String> CSVvalues) {
        renamePDFFilesWithCondition(directory,CSVvalues,"Meldebescheinigung");
        renamePDFFilesWithCondition(directory,CSVvalues,"Verdienstabrechnung");
    }

    public static String extractPersonalNumberFromFileName(String fileName) {
        String marker = "PersonalNr=";
        int startIndex = fileName.indexOf(marker);

        if (startIndex != -1) {
            String numberPart = fileName.substring(startIndex + marker.length()).replace(".pdf", "").trim();

            if (numberPart.matches("\\d+")) {
                return numberPart; //
            }
        }
        return null;
    }

    public void onChooseCSVClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose File!");
        Stage stage = (Stage)anchorPane.getScene().getWindow();
        File csvFile = fileChooser.showOpenDialog(stage);
        String fileType = getFileExtension(csvFile);
        if (fileType.equals("csv")){
            hasCSVFile=true;
            readCSVFile(csvFile);
        }else{
            hasCSVFile=false;
        }
    }

    public void onChooseFileClick(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        Stage stage = (Stage)anchorPane.getScene().getWindow();
        selectedDirectory = directoryChooser.showDialog(stage);
    }

    public void onExecuteClick(ActionEvent event) {
        renamePDFFiles(selectedDirectory, CSVvalues);
    }
}
