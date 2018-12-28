/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Ihor
 */
public class ExtendedFile extends File {

    static ExtendedFile DEFAULT;
    private List<String[]> lines;
    private String txtSeparator = ",";
    public String getFileExtension() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }
    
    public ExtendedFile(String pathname) {
        super(pathname);
        headersPositionsTo = new HashMap<String, Integer>();
        headersPositionsFrom = new HashMap<String, Integer>();
        hasHeader = true;
    }
    public Map<String, Integer> headersPositionsTo;
    public Map<String, Integer> headersPositionsFrom;
    public boolean hasHeader;
    public char separator = ',';
    public String[] headers;

    public com.opencsv.CSVReader getCsvReader() {
        Reader reader;
        com.opencsv.CSVReader csvReader = null;
        try {
            if (getFileExtension().equalsIgnoreCase("csv")) {
               
            } else if (getFileExtension().equalsIgnoreCase("xlsx")
                    || getFileExtension().equalsIgnoreCase("xls")) {

                InputStream inp = new FileInputStream(this);
                Workbook wb = WorkbookFactory.create(inp);
                csvReader = new com.opencsv.CSVReader(new StringReader(echoAsCSV(wb.getSheetAt(0))));
                inp.close();

            } else if (getFileExtension().equalsIgnoreCase("txt")) {
                initTxtFile();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return csvReader;
    }
    

    
    public void initFile() {
        if (getFileExtension().equalsIgnoreCase("csv")) {
            initCsvFile();
        } else if (getFileExtension().equalsIgnoreCase("xlsx")
                || getFileExtension().equalsIgnoreCase("xls")) {
            initExcelFile();
        } else if (getFileExtension().equalsIgnoreCase("txt")) {
            try {
                initTxtFile();
            } catch (IOException ex) {
                Logger.getLogger(ExtendedFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String echoAsCSV(Sheet sheet) {
        String result = "";
        Row row = null;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                result += "\"" + row.getCell(j) + "\",";
            }
            result += "\n";
        }
        return result;
    }
    
    private void initExcelFile() {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(this.getAbsolutePath()));
            CSVParser csvParser = new org.apache.commons.csv.CSVParser(reader, CSVFormat.EXCEL
                    .withSkipHeaderRecord()
                    .withTrim());
             for (CSVRecord csvRecord : csvParser) {
                 System.out.println(csvRecord.get(0));
             }
        } catch (IOException ex) {
            Logger.getLogger(ExtendedFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initCsvFile() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();
        
        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);
        

        CsvParser parser = new CsvParser(settings);
        lines = parser.parseAll(this);
        CsvFormat format = parser.getDetectedFormat();
        String[] headers = rowProcessor.getHeaders();
        
    }
      
    private void initTxtFile() throws IOException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();
        
        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);
        

        CsvParser parser = new CsvParser(settings);
        lines = parser.parseAll(this);
        CsvFormat format = parser.getDetectedFormat();
        String[] headers = rowProcessor.getHeaders();
        
//        if (!isTxtFileHasHeaders()) {
//            addAndDetectHeaders();
//        }
//        final Path txt = Paths.get(this.getAbsolutePath());
//        final Path csv = Paths.get(this.getAbsolutePath().replace(".txt", "_tmp.csv"));
//        try (
//                final Stream<String> lines = Files.lines(txt);
//                final PrintWriter pw = new PrintWriter(Files.newBufferedWriter(csv, StandardOpenOption.CREATE_NEW))) {
//                    lines.map((line) -> line.split(txtSeparator)).
//                    map((line) -> Stream.of(line).collect(Collectors.joining(","))).
//                    forEach(pw::println);
//        }
    }
    
    private String detectSeparator() throws IOException {
        String result = "";
        final Path txt = Paths.get(this.getAbsolutePath());
        final Stream<String> lines = Files.lines(txt);
        String header = lines.findFirst().orElse(null);
        if (header != null) {
            if (header.split("\\t{1,5}").length > 1) {
                result = "\\t{1,5}";
            }
            if (header.split(" {2,5}").length > 1) {
                result = " {2,5}";
            }
            if (header.split("\\,{1,5}").length > 1) {
                result = "\\,{1,5}";
            }
            System.out.print(header);
        }
        return result;
    }

    private void addAndDetectHeaders() throws IOException {
        ArrayList<String> headers = new ArrayList<String>();
        BufferedReader brTest = new BufferedReader(new FileReader(this.getAbsoluteFile()));
        String firstRow = brTest.readLine();
        int counter = 0;
        for (String cell : firstRow.split(txtSeparator)) {
            if (DataHelper.validateURLs(cell)) {
                headers.add("Website");
            } else if (DataHelper.validateEmail(cell)) {
                headers.add("Email");
            } else {
                headers.add("UnknownHeader" + (counter + 1));
            }
            counter++;
        }
        this.headers = headers.toArray(new String[headers.size()]);
    }
    
    private boolean isTxtFileHasHeaders() throws IOException {
        txtSeparator = detectSeparator();
        BufferedReader brTest = new BufferedReader(new FileReader(this.getAbsoluteFile()));
        String firstRow = brTest.readLine();
        for (String cell : firstRow.split(txtSeparator)) {
            if (LogicSingleton.getLogic().headers.containsKey(cell)) {
                return true;
            }
        }
        return false;
    }
}
