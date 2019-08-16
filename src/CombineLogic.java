import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

    private String headerRow;

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
        for (HeaderFileObject item : columnsFiles) {
            if (columnsFiles.indexOf(item) == (columnsFiles.size() - 1)) {
                headerRow.append(item.getHeader());
            } else {
                headerRow.append(item.getHeader()).append(",");
            }
        }
        this.headerRow = headerRow.toString();
    }

    private synchronized void populateOutputFile() {
        if(!this.mainFrameGUI.getCbRemoveDuplicates().isSelected()) {
            appendStringToFile(this.headerRow, outputFile);
        }
        try {
            for (int i = 0; i < itemsCount; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                for (HeaderFileObject headerFileObject : columnsFiles) {
                    if (columnsFiles.indexOf(headerFileObject) == (columnsFiles.size() - 1)) {
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
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            System.out.println(ex.getMessage());
        }
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

    private TsvParser getTsvParser() {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\r\n");
        settings.setProcessor(DataHelper.getBatchedColumnProcessor(columnsFiles));
        return new TsvParser(settings);
    }

    private AbstractParser getLogicParser(String extension) {
        if (extension.equalsIgnoreCase("txt")) {
            return getTsvParser();
        }
        CsvParserSettings settings = DataHelper.getCsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(DataHelper.getBatchedColumnProcessor(columnsFiles));
        return new CsvParser(settings);
    }

    private CsvParser getHeaderParser() {
        CsvParserSettings settings = DataHelper.getCsvParserSettings();
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
        itemsCount = 0;
        for (HeaderFileObject file : columnsFiles) {
            itemsCount =+ file.getRowsCount();
        }
    }

    private synchronized void appendStringToFile(String string, File file) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(string);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private synchronized void extractHeaders(String[] headers) {
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
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return resultFile;
    }

    private Reader getReader(ExtendedFile file) {
        Reader reader = null;
        if (file.getFileExtension().equalsIgnoreCase("xlsx") || file.getFileExtension().equalsIgnoreCase("xls")) {
            try {
                InputStream inp = new FileInputStream(file);
                Workbook wb = WorkbookFactory.create(inp);
                reader = new StringReader(echoAsCSV(wb.getSheetAt(0)));
            } catch (IOException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        else {
            try {
                reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
        return reader;
    }

    private String echoAsCSV(Sheet sheet) {
        StringBuilder result = new StringBuilder();
        Row row;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                result.append("\"").append(row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().replaceAll("\r", "").replaceAll("\n", "")).append("\",");
            }
            if(result.toString().endsWith(","))
            {
                result = new StringBuilder(result.substring(0, result.length() - 1));
            }
            result.append("\n");
        }
        return result.toString();
    }

    void processFiles() {
        Thread worker = new Thread(() -> {
            this.mainFrameGUI.getlblUrlsCountData().setText("Processing");
            this.mainFrameGUI.getCbRemoveDuplicates().setEnabled(false);
            this.mainFrameGUI.getBtnProcessFiles().setEnabled(false);
            try {
                inputFiles.forEach(file -> {
                    Reader reader = getReader(file);
                    getHeaderParser().parse(reader);
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println(Arrays.toString(e.getStackTrace()));
                    }
                });
                inputFiles.forEach(file -> {
                    Reader reader = getReader(file);
                    getLogicParser(file.getFileExtension()).parse(reader);
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println(Arrays.toString(e.getStackTrace()));
                    }
                });
                getRowsCount();
                createOutputFile();
                initHeaderRow();

                populateOutputFile();
                if(this.mainFrameGUI.getCbRemoveDuplicates().isSelected()) {
                    stripDuplicatesFromFile();
                }
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
