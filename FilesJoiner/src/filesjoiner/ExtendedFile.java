/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
    }
    public Map<String, Integer> headersPositionsTo;
    public Map<String, Integer> headersPositionsFrom;
    public String[] headers;
    
    public List<String[]> getLines() {
        return lines;
    }

    public void initFile() throws IOException {
        CsvParserSettings settings = new CsvParserSettings();
        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.detectFormatAutomatically('\t', ' ', ',');
        settings.setIgnoreLeadingWhitespacesInQuotes(true);
        settings.setIgnoreTrailingWhitespacesInQuotes(true);
        CsvParser parser = new CsvParser(settings);
        
        parser.parseAll(getStream());
        if (isFileHasHeaders(rowProcessor.getHeaders())) {
            settings.setHeaderExtractionEnabled(true);
            this.headers = rowProcessor.getHeaders();
        }
        else
        {
            detectHeaders(parser.parseAll(getStream()).get(0));
        }
        lines = parser.parseAll(getStream());
        initHeaderPositionsFrom();
    }
    
    private InputStream getStream() throws IOException {
        InputStream objToRead = null;
         if (getFileExtension().equalsIgnoreCase("xlsx") || getFileExtension().equalsIgnoreCase("xls")) {
            InputStream inp = new FileInputStream(this);
            Workbook wb = WorkbookFactory.create(inp);
            StringReader strReader = new StringReader(echoAsCSV(wb.getSheetAt(0)));
            objToRead = new ByteArrayInputStream(IOUtils.toString(strReader).getBytes());
        }
        else if (getFileExtension().equalsIgnoreCase("csv") || getFileExtension().equalsIgnoreCase("txt")) {
            objToRead = new FileInputStream(this);
        }
         return objToRead;
    }
    
    private boolean isFileHasHeaders(String[] scrapedHeaders) {
        for (String scrapedHeader : scrapedHeaders) {
            String fileHeader =  scrapedHeader.toLowerCase().replace(" ", "");
            for (Map.Entry<String, Integer> entry : LogicSingleton.getLogic().headers.entrySet()) {
                String defaultHeadaer = entry.getKey().toLowerCase().replace(" ", "");
                if ((defaultHeadaer.contains(fileHeader) || fileHeader.contains(defaultHeadaer)) && !StringUtils.isBlank(fileHeader)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void detectHeaders(String[] firstRow) {
        ArrayList<String> headers = new ArrayList<String>();
        int counter = 0;
        for (String cell : firstRow) {
            if (DataHelper.validateURLs(cell)) {
                headers.add("Website");
            } else if (DataHelper.validateEmail(cell)) {
                headers.add("Email");
            }
            else {
                headers.add("UnknownHeader"+counter);
                counter++;
            }
        }
        Object[] objHeaders = headers.toArray();
        this.headers = Arrays.copyOf(objHeaders, objHeaders.length, String[].class);
    }

    private String echoAsCSV(Sheet sheet) {
        String result = "";
        Row row = null;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                result += "\"" + row.getCell(j).toString().replaceAll("\r", "").replaceAll("\n", "") + "\",";
            }
            result += "\n";
        }
        return result;
    }
    
    public  String getCutterFilename() {
        String result = "";
        String name = FilenameUtils.removeExtension(FilenameUtils.getName(getAbsolutePath()));
        result = name.substring(0, Math.min(name.length(), 25));
        return result;
    }

    private void initHeaderPositionsFrom() {
        int counter = 0;
        for (String header : this.headers) {
            if (StringUtils.isEmpty(header)) {
                this.headersPositionsFrom.put("UnknownHeader"+counter, counter);
                counter++;
            } else {
                this.headersPositionsFrom.put(header, counter);
                counter++;
            }
        }
    }
}
