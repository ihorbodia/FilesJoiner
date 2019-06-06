import Models.ColumnItem;
import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class CombineLogic {

    private ArrayList<ExtendedFile> inputFiles;
    private List<HeaderFileObject> columnsFiles;
    private File temporaryFolder;

    CombineLogic(ArrayList<ExtendedFile> inputFiles) {
        this.inputFiles = inputFiles;
        columnsFiles = new ArrayList<>();
        try {
            temporaryFolder = new File(new File(CombineLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile().getParent() + File.separator + "temp");
            temporaryFolder.deleteOnExit();
            temporaryFolder.mkdir();
        } catch (URISyntaxException e) {
            System.out.println("Cannot remove temporary folder");
        }
    }

    void cleanResources() {
        columnsFiles.forEach(x -> x.delete());
        columnsFiles.clear();
        temporaryFolder.delete();
    }

    void createAndPopulateOutputFile() {
        File f = inputFiles.get(0);
        File parent = f.getParentFile();
        File outputFile = new File(parent.getAbsolutePath() + File.separator + " merged data "+ FilenameUtils.getName(parent.getAbsolutePath())+".txt");

        StringBuilder headerRow = new StringBuilder();
        for (int i = 0; i < columnsFiles.size(); i++) {
            if (columnsFiles.size() == (i+1)) {
                headerRow.append(columnsFiles.get(i).getHeader());
            } else {
                headerRow.append(columnsFiles.get(i).getHeader()).append(",");
            }
        }

        try {
            outputFile.createNewFile();
            appendStringToFile(headerRow.toString(), outputFile);
            Path path = Paths.get(columnsFiles.get(0).getAbsolutePath());
            long lineCount = Files.lines(path).count();

            for (int i = 0; i < lineCount; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < columnsFiles.size(); j++) {
                    if (columnsFiles.size() == (j+1)) {
                        try (Stream<String> lines = Files.lines(Paths.get(columnsFiles.get(j).getAbsolutePath()))) {
                            String line = lines.skip(i).findFirst().get();
                            stringBuilder.append("\"").append(line).append("\"");
                        }
                    } else {
                        try (Stream<String> lines = Files.lines(Paths.get(columnsFiles.get(j).getAbsolutePath()))) {
                            String line = lines.skip(i).findFirst().get();
                            stringBuilder.append("\"").append(line).append("\"").append(",");
                        }
                    }

                }
                appendStringToFile(stringBuilder.toString(), outputFile);
            }
        } catch(IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static String removeLastCharRegex(String s) {
        return (s == null) ? null : s.replaceAll(".$", "");
    }

    private ArrayList<String> getEmptyData(int count) {
        ArrayList<String> emptyData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            emptyData.add("");
        }
        return emptyData;
    }

    private CsvParser getLogicParser() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setDelimiterDetectionEnabled(true, '\t', ',', '\n');
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setMaxCharsPerColumn(1000000);
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
                    System.out.println("ERROR");
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

    private void appendDataToTempFile(List<String> strings, File file) throws IOException {
        FileWriter fw = new FileWriter(file, true);

        for (String string : strings) {
            fw.write(string.replace("\"", "\"\"") + "\r\n");
        }
        fw.close();
    }

    private void appendDataToFile(List<String> strings, File file) throws IOException {
        FileWriter fw = new FileWriter(file, true);

        for (String string : strings) {
            fw.write(string + "\r\n");
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
            resultFile.setHeader(name);
            resultFile.deleteOnExit();
            resultFile.createNewFile();
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
            inputFiles.forEach(file -> getHeaderParser().parse(file));
            inputFiles.forEach(file -> getLogicParser().parse(getReader(file)));
            createAndPopulateOutputFile();
            cleanResources();
        });
        worker.start();

    }
}
