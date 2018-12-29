/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private List<String[]> lines;
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
    public String[] headers;
    
    public List<String[]> getLines() {
        return lines;
    }

    public void initFile() {
        try {
            if (getFileExtension().equalsIgnoreCase("csv") || getFileExtension().equalsIgnoreCase("txt")) {
                initPlainTextFile();
            } else if (getFileExtension().equalsIgnoreCase("xlsx") || getFileExtension().equalsIgnoreCase("xls")) {
                initExcelFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(ExtendedFile.class.getName()).log(Level.SEVERE, null, ex);
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

    private void initExcelFile() throws IOException {
        InputStream inp = new FileInputStream(this);
        Workbook wb = WorkbookFactory.create(inp);
        StringReader strReader = new StringReader(echoAsCSV(wb.getSheetAt(0)));

        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();

        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);
        settings.setNullValue("");
	settings.setEmptyValue("");

        CsvParser parser = new CsvParser(settings);
        lines = parser.parseAll(strReader);
        String[] headers = rowProcessor.getHeaders();
        if (headers.length > 0) {
            this.headers = headers;
            initHeaderPositionsFrom();
        }
        inp.close();
    }

    private void initPlainTextFile() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();

        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setHeaderExtractionEnabled(true);
        settings.setNullValue("");
	settings.setEmptyValue("");

        CsvParser parser = new CsvParser(settings);
        lines = parser.parseAll(this);
        String[] headers = rowProcessor.getHeaders();
        if (headers.length > 0) {
            this.headers = headers;
            initHeaderPositionsFrom();
        }
    }
    
    private void initHeaderPositionsFrom() {
        int counter = 0;
        for (String header : this.headers) {
            if (header == null) {
                this.headersPositionsFrom.put("UnknownHeader"+counter, counter);
            } else {
                this.headersPositionsFrom.put(header, counter);
            }
            counter++;
        }
    }
    
    private void detectHeaders() {
        ArrayList<String> headers = new ArrayList<String>();
        int counter = 0;
        for (String[] line : lines) {
            for (String cell : line) {
                if (DataHelper.validateURLs(cell)) {
                    headers.add("Website");
                } else if (DataHelper.validateEmail(cell)) {
                    headers.add("Email");
                }
            }
        }
    }
}
