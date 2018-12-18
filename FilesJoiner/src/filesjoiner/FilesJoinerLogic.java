/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.IOException;
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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ibodia
 */
public class FilesJoinerLogic {

    ArrayList<ExtendedFile> files;
    MainFrameGUI parent;
    ArrayList<String[]> resultList;
    HashMap<String, Integer> headers = new HashMap<String, Integer>();

    public FilesJoinerLogic(MainFrameGUI parent) {
        this.parent = parent;
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
        headers = new HashMap<String, Integer>();
        initHeaders();
        detectHeaders();
        for (ExtendedFile file : files) {
            normalizeHeaders(file);
        }
        scrapeDataFromCsvFiles();
        countItems();
        saveDataToFile();
    }

    private void countItems() {
        ArrayList<String> urls = new ArrayList<String>();
        for (String[] row : resultList) {
            if (StringUtils.isEmpty(row[0])) {
                urls.add(row[0]);
            }
        }
        parent.getlblUrlsCountData().setText(Integer.toString(urls.size()));
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
        int counter = sortedHeaders.entrySet().size();
        for (Map.Entry<String, Integer> entry : sortedHeaders.entrySet()) {
            sb.append("\"" + entry.getKey() + "\"");
            counter--;
            if (counter != 0) {
                sb.append(",");
            }
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
            String pathToSave = path.replace(".", "") + "Merged data.csv";
            Files.deleteIfExists(Paths.get(path.replace(".", "") + "Merged data.csv"));
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
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

    private void scrapeDataFromCsvFiles() {
        resultList = new ArrayList<String[]>();
        for (ExtendedFile file : files) {
            try {
                String[] nextRecord;
                int counter = 0;
                CSVReader csvReader = file.getCsvReader();
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
        try {
            for (ExtendedFile file : files) {
                String[] nextRecord;
                CSVReader csvReader = file.getCsvReader();
                while ((nextRecord = csvReader.readNext()) != null) {
                    if (nextRecord.length == 1) {
                        String str = nextRecord[0];
                        if (str.split("\t").length > 1) {
                            file.separator = "\t".charAt(0);
                            file.headers = str.split("\t");
                        }
                        if (str.contains("http") || str.contains("www.")) {
                            file.separator = ",".charAt(0);
                            file.headers = new String[]{"Website"};
                            file.hasHeader = false;
                        }
                    } else {
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

    private void normalizeHeaders(ExtendedFile file) {
        int counter = 0;
        if (file.headers == null) {
            return;
        }
        for (String fileHeader : file.headers) {
            file.headersPositionsFrom.put(fileHeader, counter);
            counter++;
            Object value = null;
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                if (fileHeader.replace(" ", "").toLowerCase().contains(entry.getKey().toLowerCase())
                        || entry.getKey().replace(" ", "").toLowerCase().contains(fileHeader.toLowerCase())) {
                    value = entry;
                    file.headersPositionsTo.put(fileHeader, entry.getValue());
                    break;
                }
            }

            if (value == null) {
                Map.Entry<String, Integer> maxEntry = null;
                for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                }
                file.headersPositionsTo.put(fileHeader, maxEntry.getValue() + 1);
                headers.put(fileHeader, maxEntry.getValue() + 1);
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
