import com.univocity.parsers.common.AbstractParser;
import com.univocity.parsers.common.processor.BatchedColumnProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExtendedFile extends File {

    ExtendedFile(String pathname) {
        super(pathname);
        columnFiles = new ArrayList<>();
    }

    String getFileExtension() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }

    private File temporaryFolderPath;
    private File temporaryFolder;

    public List<HeaderFileObject> columnFiles;

    void removeTemporaryFolder() throws IOException {
        FileUtils.forceDelete(temporaryFolder);
    }

    void initColumnFiles(File temporaryFolder) {
        temporaryFolderPath = temporaryFolder;
        Reader headerReader = getReader(this);
        getHeaderParser().parse(headerReader);
        Reader contentReader = getReader(this);
        getLogicParser(this.getFileExtension()).parse(contentReader);
        try {
            headerReader.close();
            contentReader.close();
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private Reader getReader(ExtendedFile file) {
        Reader reader = null;
        if (file.getFileExtension().equalsIgnoreCase("xlsx") || file.getFileExtension().equalsIgnoreCase("xls")) {
            try {
                InputStream inp = new FileInputStream(file);
                Workbook wb = WorkbookFactory.create(inp);
                reader = new StringReader(DataHelper.echoAsCSV(wb.getSheetAt(0)));
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

    private synchronized void extractHeaders(String[] headers) {
        try {
            temporaryFolder = new File(temporaryFolderPath.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(this.getAbsolutePath()));
            Files.createDirectory(temporaryFolder.toPath());
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }

        for (String header : headers) {
            String headerName = header.replaceAll(":", "꞉");
            if(columnFiles.stream().noneMatch(item -> item.getHeader().equalsIgnoreCase(headerName))) {
                HeaderFileObject headerFileObject = createNewFile(headerName);
                columnFiles.add(headerFileObject);
            }
        }
    }

    private HeaderFileObject createNewFile(String name) {
        HeaderFileObject resultFile = null;
        try {

            resultFile = new HeaderFileObject(temporaryFolderPath.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(this.getAbsolutePath()) + File.separator + name + ".csv");
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

    private AbstractParser getLogicParser(String extension) {
        if (extension.equalsIgnoreCase("txt")) {
            return DataHelper.getTsvParser(columnFiles);
        }
        CsvParserSettings settings = DataHelper.getCsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(DataHelper.getBatchedColumnProcessor(columnFiles));
        return new CsvParser(settings);
    }
}
