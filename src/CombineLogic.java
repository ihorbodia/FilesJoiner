import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

class CombineLogic {

    private ArrayList<ExtendedFile> inputFiles;
    private List<HeaderFileObject> columnsFiles;
    private File temporaryFolder;
    private File outputFile;
    private long itemsCount = 0;
    private MainFrameGUI mainFrameGUI;

    CombineLogic(ArrayList<ExtendedFile> inputFiles, MainFrameGUI mainFrameGUI) {
        this.mainFrameGUI = mainFrameGUI;
        this.inputFiles = inputFiles;
        columnsFiles = new ArrayList<>();
        try {
            temporaryFolder = new File(new File(CombineLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile().getParent() + File.separator + "temp");
            temporaryFolder.mkdir();
        } catch (URISyntaxException e) {
            System.out.println("Cannot remove temporary folder");
        }
    }

    private void createAndPopulateOutputFile() {
        File f = inputFiles.stream().findFirst().get();
        File parent = f.getParentFile();
        try {
            outputFile = new File(parent.getAbsolutePath() + File.separator + " merged data "+ FilenameUtils.getName(parent.getAbsolutePath())+".csv");
            if (outputFile.exists()) {
                outputFile.delete();
                outputFile.createNewFile();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        StringBuilder headerRow = new StringBuilder();
        for (HeaderFileObject item : columnsFiles) {
            if(columnsFiles.indexOf(item) == (columnsFiles.size() -1)) {
                headerRow.append(item.getHeader());
            } else {
                headerRow.append(item.getHeader()).append(",");
            }
        }

        try {
            appendStringToFile(headerRow.toString(), outputFile);
            for (int i = 0; i < itemsCount; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                for (HeaderFileObject headerFileObject : columnsFiles) {
                    if(columnsFiles.indexOf(headerFileObject) == (columnsFiles.size() -1)) {
                        String data = headerFileObject.getBufferedReader().readLine();
                        stringBuilder.append("\"").append(data).append("\"");
                    } else {
                        String data = headerFileObject.getBufferedReader().readLine();
                        stringBuilder.append("\"").append(data).append("\"").append(",");
                    }
                }
                appendStringToFile(stringBuilder.toString(), outputFile);
            }
            for (HeaderFileObject headerFileObject : columnsFiles) {
                headerFileObject.getBufferedReader().close();
            }
            FileUtils.forceDelete(temporaryFolder);
        } catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private ArrayList<String> getEmptyData(int count) {
        ArrayList<String> emptyData = new ArrayList<>();
        for (int i = 0; i < count - 1; i++) {
            emptyData.add("");
        }
        return emptyData;
    }

    public void stripDuplicatesFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Set<String> lines = new HashSet<String>(10000); // maybe should be bigger
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String unique : lines) {
            writer.write(unique);
            writer.newLine();
        }
        writer.close();
    }

    private CsvParser getLogicParser() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setDelimiterDetectionEnabled(true, '\t', ' ', ',', '\n');
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setIgnoreLeadingWhitespacesInQuotes(true);
        settings.setIgnoreTrailingWhitespacesInQuotes(true);
        settings.setMaxCharsPerColumn(500000);
        settings.setHeaderExtractionEnabled(true);
        BatchedColumnProcessor batchedColumnProcessor = new BatchedColumnProcessor(100) {

            @Override
            public void batchProcessed(int rowsInThisBatch) {
                List<List<String>> columnValues = getColumnValuesAsList();
                System.out.println("Batch " + getBatchesProcessed() + ":");
                try {
                    for (int i = 0; i < columnsFiles.size(); i++) {
                        File f = columnsFiles.get(i);
                        if (i >= columnValues.size()) {
                            appendDataToTempFile(getEmptyData(columnValues.size()), f);
                        } else {
                            appendDataToTempFile(columnValues.get(i), f);
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        settings.setProcessor(batchedColumnProcessor);
        return new CsvParser(settings);
    }

    private CsvParser getHeaderParser() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setDelimiterDetectionEnabled(true, '\t', ' ', ',', '\n');
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setIgnoreLeadingWhitespacesInQuotes(true);
        settings.setIgnoreTrailingWhitespacesInQuotes(true);
        settings.setMaxCharsPerColumn(500000);
        BatchedColumnProcessor batchedColumnProcessor = new BatchedColumnProcessor(100) {
            boolean isHeaderExtracted = false;
            @Override
            public void batchProcessed(int rowsInThisBatch) {
                if (!isHeaderExtracted) {
                    extractHeaders(getHeaders());
                    isHeaderExtracted = true;
                }
            }
        };

        settings.setProcessor(batchedColumnProcessor);
        return new CsvParser(settings);
    }

    private void getRowsCount() {
        for (HeaderFileObject file : columnsFiles) {
            long fileRows = file.getRowsCount();
            if (itemsCount < fileRows) {
                itemsCount = fileRows;
            }
        }
    }

    private void appendDataToTempFile(List<String> strings, File file) throws IOException {
        FileWriter fw = new FileWriter(file, true);

        for (String string : strings) {
            fw.write(string.replace("\"", "\"\"") + "\r\n");
        }
        fw.close();
    }

    private void appendStringToFile(String string, File file) throws IOException {
        FileWriter fw = new FileWriter(file, true);
        fw.write(string + "\r\n");
        fw.close();
    }

    private void extractHeaders(String[] headers) {
        for (String header : headers) {
            if(columnsFiles.stream().noneMatch(item -> item.getHeader().equalsIgnoreCase(header))) {
                columnsFiles.add(createNewFile(header));
            }
        }
    }

    private HeaderFileObject createNewFile(String name) {
        HeaderFileObject resultFile = null;
        try {
            resultFile = new HeaderFileObject(temporaryFolder.getAbsolutePath() + File.separator + name + ".csv");
            if (resultFile.exists()) {
                resultFile.delete();
                resultFile.createNewFile();
            }
            resultFile.setHeader(name);
        } catch (IOException e) {
            System.out.println(e);
        }
        return resultFile;
    }

    private Reader getReader(File file) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        return reader;
    }

    void processFiles() {
        Thread worker = new Thread(() -> {
            this.mainFrameGUI.getlblUrlsCountData().setText("Processing");
            inputFiles.forEach(file -> getHeaderParser().parse(file));
            inputFiles.forEach(file -> {
                Reader reader = getReader(file);
                getLogicParser().parse(reader);
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
            getRowsCount();
            createAndPopulateOutputFile();
            this.mainFrameGUI.getlblUrlsCountData().setText("Finished");
        });
        worker.start();
    }
}
