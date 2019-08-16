import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class CombineLogic {

    private ArrayList<ExtendedFile> inputFiles;
    private List<HeaderFileObject> columnsFiles;
    private List<HeaderFileObject> commonColumnsFiles;
    private File temporaryFolder;
    private File commonDataFolder;
    private File outputFile;
    private long itemsCount = 0;
    private MainFrameGUI mainFrameGUI;

    private String headerRow;
    private List<String> headerRows;

    CombineLogic(ArrayList<ExtendedFile> inputFiles, MainFrameGUI mainFrameGUI) {
        this.mainFrameGUI = mainFrameGUI;
        this.inputFiles = inputFiles;
        columnsFiles = new ArrayList<>();
        headerRows = new ArrayList<>();
        commonColumnsFiles = new ArrayList<>();
        try {
            temporaryFolder = new File(new File(CombineLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile().getParent() + File.separator + "temp");
            commonDataFolder = new File(new File(CombineLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile().getParent() + File.separator + "temp"+File.separator+"commonData");
            temporaryFolder.mkdir();
            commonDataFolder.mkdir();
        } catch (URISyntaxException e) {
            System.out.println("Cannot remove temporary folder");
        }
    }

    private synchronized void createOutputFile() {
        File f = inputFiles.stream().findFirst().get();
        File parent = f.getParentFile();
        try {
            outputFile = new File(parent.getAbsolutePath() + File.separator + " merged data "+ FilenameUtils.getName(parent.getAbsolutePath())+".csv");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private synchronized void initHeaderRow() {
        StringBuilder headerRow = new StringBuilder();
        inputFiles.forEach(file -> {
            file.columnFiles.forEach(columnFile -> {
                if(!headerRows.contains(columnFile.getHeader().toLowerCase())) {
                    headerRow.append(columnFile.getHeader()).append(",");
                    headerRows.add(columnFile.getHeader().toLowerCase());
                }
            });
        });
        this.headerRow = headerRow.toString().replaceAll("꞉", ":");
    }

    private HeaderFileObject createNewCommonFile(String headerName) {
        HeaderFileObject resultFile = null;
        try {
            resultFile = new HeaderFileObject(commonDataFolder + File.separator + headerName + ".csv");
            if (resultFile.exists()) {
                resultFile.delete();
                resultFile.createNewFile();
            }
            resultFile.setHeader(headerName);
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return resultFile;
    }

    private synchronized void mergeData() {
        try {
            headerRows.forEach(header -> {
                commonColumnsFiles.add(createNewCommonFile(header));
            });
            commonColumnsFiles.forEach(commonDataFile -> {
                long recordsCount = 0;
                for (ExtendedFile inputFile : inputFiles) {
                    Optional<HeaderFileObject> item = inputFile.columnFiles.stream()
                            .filter(columnFile -> columnFile.getHeader().equalsIgnoreCase(commonDataFile.getHeader()))
                            .findFirst();
                    try {
                        if (item.isPresent()) {
                            recordsCount = item.get().getRowsCount();
                            LineIterator it = null;
                            it = FileUtils.lineIterator(item.get(), "UTF-8");
                            while (it.hasNext()) {
                                String line = it.nextLine();
                                appendStringToFile(line, commonDataFile);
                            }
                        } else {
                            for (int i = 0; i < recordsCount; i++) {
                                appendStringToFile(" ", commonDataFile);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(Arrays.toString(e.getStackTrace()));
                        System.out.println(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
    }

    private synchronized void populateOutputFile() {
//        if(!this.mainFrameGUI.getCbRemoveDuplicates().isSelected()) {
//            appendStringToFile(this.headerRow, outputFile);
//        }
//        try {
//            for (int i = 0; i < itemsCount; i++) {
//                String resultString = null;
//                StringBuilder stringBuilder = new StringBuilder();
//                for (HeaderFileObject headerFileObject : columnsFiles) {
//                    String data = headerFileObject.getBufferedReader().readLine();
//                    if (data != null) {
//                        resultString = stringBuilder.append("\"").append(data).append("\"").append(",").toString();
//                    } else {
//                        resultString = "";
//                    }
//                }
//                //String dataRow = DataHelper.normalizeDataString(prevCounter, resultString);
//                appendStringToFile(resultString, outputFile);
//            }
//            for (HeaderFileObject headerFileObject : columnsFiles) {
//                headerFileObject.getBufferedReader().close();
//            }
//        } catch (IOException ex) {
//            System.out.println(Arrays.toString(ex.getStackTrace()));
//            System.out.println(ex.getMessage());
//        }
    }

    private void stripDuplicatesFromFile() throws IOException, OutOfMemoryError {
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        Set<String> lines = new HashSet<>(5000000);
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        boolean isHeaderAdded = false;
        for (String unique : lines) {
            if (!isHeaderAdded) {
                writer.write(this.headerRow);
                writer.newLine();
                isHeaderAdded = true;
            }
            writer.write(unique);
            writer.newLine();
        }
        writer.close();
    }

    private void getRowsCount() {
        itemsCount = 0;
        for (HeaderFileObject file : columnsFiles) {
            long fileRows = file.getRowsCount();
            if (itemsCount < fileRows) {
                itemsCount = fileRows;
            }
        }
    }

    private synchronized void appendStringToFile(String string, HeaderFileObject file) {
        if (StringUtils.isEmpty(string)) {
            return;
        }
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(string);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    void processFiles() {
        Thread worker = new Thread(() -> {
            this.mainFrameGUI.getlblUrlsCountData().setText("Processing");
            this.mainFrameGUI.getCbRemoveDuplicates().setEnabled(false);
            this.mainFrameGUI.getBtnProcessFiles().setEnabled(false);
            try {
                inputFiles.forEach(file -> {
                   file.initColumnFiles(temporaryFolder);
                });
                getRowsCount();
                createOutputFile();
                initHeaderRow();

                mergeData();
                populateOutputFile();
                if(this.mainFrameGUI.getCbRemoveDuplicates().isSelected()) {
                    stripDuplicatesFromFile();
                }
                inputFiles.forEach(file -> {
                    try {
                        file.removeTemporaryFolder();
                    } catch (IOException e) {
                        this.mainFrameGUI.getlblUrlsCountData().setText("Finished with error: " + e.getMessage());
                    }
                });
                FileUtils.forceDelete(temporaryFolder);
                this.mainFrameGUI.getlblUrlsCountData().setText("Finished: " + (itemsCount + 1) + " items processed.");
            } catch (Exception ex) {
                this.mainFrameGUI.getlblUrlsCountData().setText("Finished with error: " + ex.getMessage());
            }
            this.mainFrameGUI.getCbRemoveDuplicates().setEnabled(true);
            this.mainFrameGUI.getBtnProcessFiles().setEnabled(true);
        });
        worker.start();
    }
}
