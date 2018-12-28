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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    String outputPath = null;
    HashMap<String, Integer> headers = new HashMap<String, Integer>();
    ExecutorService executorService;
    File propertiesFile;
    public Future<?> future;

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

        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            public void run() {
                LogicSingleton.setCountToZero();
                LogicSingleton.processTxtFilesInList();
                headers = new HashMap<String, Integer>();
                initHeaders();
                detectHeaders();
                for (ExtendedFile file : files) {
                    normalizeHeaders(file);
                }
                scrapeDataFromCsvFiles();
                if (parent.getCbRemoveDuplicates().isSelected()) {
                    removeDuplicates();
                }
                countItems();
                saveDataToFile();
                for (ExtendedFile file : files) {
                    file.separator = ',';
                }
            }
        });
        
        Thread seeker = new Thread() {
            public void run() {
                parent.getBtnProcessFiles().setEnabled(false);
                parent.getlblUrlsCountData().setText("Processing...");
                while (true) {
                    if (future.isDone()) {
                        break;
                    }
                }
                parent.getBtnProcessFiles().setEnabled(true);
            }
        };
        seeker.start();

        Thread producerThread = new Thread() {
            @Override
            public void run() {
               
            }
        };
        producerThread.start();
    }

    private void countItems() {
        ArrayList<String> urls = new ArrayList<String>();
        for (String[] row : resultList) {
            if (!StringUtils.isEmpty(row[0])) {
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
        try {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date date = new Date();
            String pathToSave = outputPath.replace(".", "") +File.separator+ "Merged data_"+sdf.format(date)+".csv";
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setOutputPath(String path) {
        if (outputPath == null) {
              outputPath = path;
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
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
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
                    String[] row = new String[maxEntry.getValue() + 1];
                    for (int i = 0; i < headers.size(); i++) {
                        Entry<String, Integer> itemFrom = getKeysByValue(file.headersPositionsFrom, i);
                        if (itemFrom != null) {
                            Integer index = 0;
                            try {
                                index = file.headersPositionsTo.get(itemFrom.getKey());
                                row[index] = nextRecord[itemFrom.getValue()];
                            } catch (Exception ex) {
                                Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                            }
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
                    for (int i = 0; i < file.headers.length; i++) {
                        if (file.headers[i].toLowerCase().contains("url")) {
                            file.headers[i] = "Website";
                        }
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void normalizeHeaders(ExtendedFile file) {
        int counter = 0;
        if (file.headers == null) {
            return;
        }
        for (String fileHeader : file.headers) {
            if (fileHeader.equalsIgnoreCase("")) {
                continue;
            }
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
        headers.put("Website", 0);
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
    
    private void removeDuplicates() {
        ArrayList<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < resultList.size(); i++) {
            if (!isContainsSameURL(resultList.get(i), result)) {
                result.add(resultList.get(i));
            }
        }
        resultList = result;
    }
    
    private boolean isContainsSameURL(String[] from, ArrayList<String[]> result){
        boolean flag = false;
        try {
            if (StringUtils.isEmpty(from[0])) {
                return true;
            }
            for (String[] strings : result) {
                for (String string : strings) {
                    if (string == null) {
                        continue;
                    }
                    if (string.equalsIgnoreCase(from[0])) {
                        flag = true;
                        return flag;
                    }
                }
            }
        } catch (NullPointerException ex) {
            System.out.print(ex);
        }
        return flag;
    }
}
