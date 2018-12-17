/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author ibodia
 */
public class FilesJoinerLogic {
    ArrayList<ExtendedFile> files;
    ArrayList<String[]> resultList;
    HashMap<String, Integer> headers = new HashMap<String, Integer>();
    
    public FilesJoinerLogic(){
        
    }
    
    public void initFilesList(ArrayList<ExtendedFile> inputFiles) {
        if (inputFiles != null) {
            files = inputFiles;
        }
    }
    
    public void StartRun() {
        if (files == null) {
            return;
        }
        initHeaders();
        detectHeaders();
        for (ExtendedFile file : files) {
            normalizeHeaders(file);
        }
        ExtendedFile selectedFile = files.stream()
                .filter((file) -> 
                        FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("xlsx") || 
                        FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("xls"))
                .findFirst()
                .orElse(ExtendedFile.DEFAULT);
        convertExceltoCSV(selectedFile);
        scrapeDataFromFiles();
        saveDataToFile();
    }
    
    public static <String, Integer> Entry<String, Integer> getKeysByValue(Map<String, Integer> map, Integer value) {
    Entry<String, Integer> resEntry = null;
    for (Entry<String, Integer> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                resEntry = entry;
            }
        }
        return resEntry;
    }
    
    private void saveDataToFile() {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Integer> sortedHeaders = sortHashMapByValues(headers);
        for (Map.Entry<String, Integer> entry : sortedHeaders.entrySet()) {
            sb.append("\"" + entry.getKey() + "\"");
            sb.append(",");
        }
        sb.append("\n");
        for (String[] row : resultList) {
            for (String string : row) {
                String content = StringUtils.isEmpty(string) ? "" : string;
                sb.append("\"" + content + "\"");
                sb.append(",");
            }
            sb.append("\n");
        }
        
          File f = new File(".");
          String path = f.getAbsolutePath();
        try {
            String pathToSave = path.replace(".", "")+"Merged data.csv";
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void echoAsCSV(Sheet sheet) {
        Row row = null;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                System.out.print("\"" + row.getCell(j) + "\";");
            }
            System.out.println();
        }
    }
    
    private void convertExceltoCSV(File xlsxFile) {
       InputStream inp = null;
        try {
            inp = new FileInputStream(xlsxFile);
            Workbook wb = WorkbookFactory.create(inp);

            for(int i=0;i<wb.getNumberOfSheets();i++) {
                System.out.println(wb.getSheetAt(i).getSheetName());
                echoAsCSV(wb.getSheetAt(i));
            }
            inp.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private LinkedHashMap<String, Integer> sortHashMapByValues(
            HashMap<String, Integer> passedMap) {
        ArrayList<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        ArrayList<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap
                = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
    
    private void scrapeDataFromFiles() {
      resultList = new ArrayList<String[]>();
      for (ExtendedFile file : files) {
            try {
                Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
                CSVReader csvReader = new CSVReader(reader, file.separator);
                String[] nextRecord;
                int counter = 0;
                while ((nextRecord = csvReader.readNext()) != null) {
                    if (counter == 0 && file.hasHeader) {
                        counter = -1;
                        continue;
                    }
                    String[] row = new String[headers.size()];
                    for (int i = 0; i < headers.size(); i++) {
                        Entry<String, Integer> itemFrom = getKeysByValue(file.headersPositionsFrom, i);
                        if (itemFrom != null) {
                            Integer index = file.headersPositionsTo.get(itemFrom.getKey());
                            row[index] = nextRecord[itemFrom.getValue()];
                        }
                    }
                    resultList.add(row);
                }
            } catch (IOException ex) {
                Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void detectHeaders() {
        Reader reader = null;
        CSVReader csvReader = null;
        try {
            for (ExtendedFile file : files) {
                reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
                csvReader = new CSVReader(reader);
                String[] nextRecord;
                while ((nextRecord = csvReader.readNext()) != null) {
                    if (nextRecord.length == 1) {
                        String str = nextRecord[0];
                        if (str.split("\t").length > 1) {
                            file.separator = "\t".charAt(0);
                            file.headers = str.split("\t");
                        }
                        if (str.contains("http") || str.contains("www.")) {
                            file.separator = ",".charAt(0);
                            file.headers = new String[] {"Website"};
                            file.hasHeader = false;
                        }
                    }
                    else {
                        file.separator = ",".charAt(0);
                        file.headers = nextRecord;
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void normalizeHeaders(ExtendedFile file){
        int counter = 0;
        if (file.headers == null) {
            return;
        }
        for (String header : file.headers) {
            file.headersPositionsFrom.put(header, counter);
            counter++;
            Object value = null;
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase().contains(header.replace(" ", "").toLowerCase())) {
                    value = entry;
                    file.headersPositionsTo.put(header, entry.getValue());
                    break;
                }
            }
           
            if (value == null) {
                Map.Entry<String, Integer> maxEntry = null;
                for (Map.Entry<String, Integer> entry : headers.entrySet())
                {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                    {
                        maxEntry = entry;
                    }
                }
                file.headersPositionsTo.put(header, maxEntry.getValue() + 1);
                headers.put(header, maxEntry.getValue() + 1);
            }
        }
    }
    
    private void initHeaders() {
        headers.put("URL", 0);
        headers.put("Email", 1);
        headers.put("First Name", 2);
        headers.put("Last Name", 3);
        headers.put("Full Name", 4);
        headers.put("Position", 5);
        headers.put("Telephone", 6);
        headers.put("Address", 7);
        headers.put("City", 8);
        headers.put("Country", 9);
        headers.put("Company Name", 10);
        headers.put("Industry", 11);
        headers.put("Yearly Revenue", 12);
        headers.put("Notes", 13);
        headers.put("Instagram", 14);
        headers.put("LinkedIn", 15);
        headers.put("VerifyStatus", 16);
        headers.put("Company Size", 17);
    }
}


