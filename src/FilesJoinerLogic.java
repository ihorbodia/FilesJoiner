/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Models.ColumnItem;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    String pathToSave;
    ArrayList<ColumnItem> columns;

    public FilesJoinerLogic(MainFrameGUI parent) {
        this.parent = parent;
        initHeaders();
    }


    public void newMethod() {
        try {
            String[] headers = null;
            //String firstFile = "/path/to/firstFile.dat";

            File firstFile = files.get(0);
            files.remove(0);

            Scanner scanner = new Scanner(firstFile);

            if (scanner.hasNextLine()) {
                headers = scanner.nextLine().split(",");
            }

            scanner.close();

            Iterator<ExtendedFile> iterFiles = files.iterator();
            BufferedWriter writer = new BufferedWriter(new FileWriter(firstFile, true));

            while (iterFiles.hasNext()) {
                File nextFile = iterFiles.next();
                BufferedReader reader = new BufferedReader(new FileReader(nextFile));

                String line = null;
                String[] firstLine = null;
                if ((line = reader.readLine()) != null)
                    firstLine = line.split(",");

                if (!Arrays.equals(headers, firstLine))
                    throw new IOException("Header mis-match between CSV files: '" +
                            firstFile + "' and '" + nextFile.getAbsolutePath());

                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                reader.close();
            }
            writer.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
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
        future = executorService.submit(() -> {
            LogicSingleton.setCountToZero();
            
            files.forEach(file -> {
                try {
                    file.initFile();
                } catch (IOException ex) {
                    Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            normalizeHeaders();
            scrapeDataFromCsvFiles();
            if (parent.getCbRemoveDuplicates().isSelected()) {
                removeDuplicates();
            }
            countItems();
            saveDataToFile();
            removeEmptyColumns();
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
                parent.getlblUrlsCountData().setText("Finished");
            }
        };
        seeker.start();
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
            sb.append("\"").append(entry.getKey()).append("\"");
            counter--;
            if (counter != 0) {
                sb.append(",");
            }
        }
        sb.append("\n");
        for (String[] row : resultList) {
            for (int i = 0; i < row.length; i++) {
                String content = StringUtils.isEmpty(row[i]) ? "" : row[i];
                sb.append("\"").append(content).append("\"");
                if (i != (row.length - 1)) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        try {
            String names = "    ";
            switch (files.size()) {
                case 1:
                    names += files.get(0).getCutterFilename();
                    break;
                case 2:
                    names += files.get(0).getCutterFilename() + "+" + files.get(1).getCutterFilename();
                    break;
                default:
                    for (int i = 0; i < 3; i++) {
                        if (i < files.size() && files.get(i) != null) {
                            names += files.get(i).getCutterFilename();
                            if (i < 2 && i < files.size()) {
                                names += "+";
                            }
                        }
                    }   break;
            }
            if ((files.size() - 3) > 0) {
                names += "...+";
                names += (files.size() - 3) + "_more";
            }
            pathToSave = outputPath + File.separator + names + ".csv";
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            System.out.println(ex);
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOutputPath(String path) {
            outputPath = path;
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
            for (String[] record : file.getLines()) {

                String[] row = new String[maxEntry.getValue() + 1];
                for (int i = 0; i < headers.size(); i++) {
                    Entry<String, Integer> itemFrom = getKeysByValue(file.headersPositionsFrom, i);
                    if (itemFrom != null) {
                        Integer index = 0;
                        try {
                            index = file.headersPositionsTo.get(itemFrom.getKey());
                            if (record.length > itemFrom.getValue()) {
                                row[index] = record[itemFrom.getValue()];
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                String[] newRow = Arrays.copyOf(row, row.length + 1);
                newRow[row.length] = file.getAbsolutePath();
                resultList.add(newRow);
            }
        }
        headers.put("File path", headers.size());
    }

    private void normalizeHeaders() {
        for (ExtendedFile file : files) {
            for (Map.Entry<String, Integer> fileHeaderEntry : file.headersPositionsFrom.entrySet()) {
                Object value = null;
                String fileHeaderEntryItem = fileHeaderEntry.getKey().replace(" ", "").toLowerCase();
                for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                    String existsHeaders = entry.getKey().replace(" ", "").toLowerCase();
                    if (fileHeaderEntryItem.contains(existsHeaders) || existsHeaders.contains(fileHeaderEntryItem) && fileHeaderEntryItem.length() == existsHeaders.length()) {
                        value = entry;
                        file.headersPositionsTo.put(fileHeaderEntry.getKey(), entry.getValue());
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
                    file.headersPositionsTo.put(fileHeaderEntry.getKey(), maxEntry.getValue() + 1);
                    headers.put(fileHeaderEntry.getKey(), maxEntry.getValue() + 1);
                }
            }
        }
    }

    private void initHeaders() {
        headers = new HashMap<String, Integer>();
        headers.put("Website", 0);
        headers.put("Email", 1);
//        headers.put("First Name", 2);
//        headers.put("Last Name", 3);
//        headers.put("Full Name", 4);
//        headers.put("Position", 5);
//        headers.put("Telephone", 6);
//        headers.put("Address", 7);
//        headers.put("City", 8);
//        headers.put("Country", 9);
//        headers.put("Company Name", 10);
//        headers.put("Industry", 11);
//        headers.put("Yearly Revenue", 12);
//        headers.put("Notes", 13);
//        headers.put("Instagram", 14);
//        headers.put("LinkedIn", 15);
//        headers.put("VerifyStatus", 16);
//        headers.put("Company Size", 17);
    }

    private void removeDuplicates() {
        parent.getlblUrlsCountData().setText("Removing duplicates...");
        ArrayList<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < resultList.size(); i++) {
            if (!isContainsSameURL(resultList.get(i)[0], result)) {
                result.add(resultList.get(i));
            }
        }
        resultList = result;
    }
    
    public String normalizeURL(String URL) {
        String result = "";
        int pointIndex = URL.indexOf(".");
        if (pointIndex > 0) {
            result = URL.substring(pointIndex + 1);
        }
        
        int backslashIndex = URL.indexOf("/");
        if (backslashIndex > 0) {
            result = URL.substring(backslashIndex + 1);
        }
        return result;
    }

    private boolean isContainsSameURL(String URL, ArrayList<String[]> result) {
        boolean flag = false;
        try {
            if (URL != null && StringUtils.isEmpty(URL)) {
                return true;
            }
            else if (URL == null) {
                return false;
            }
            for (String[] strings : result) {
                for (String string : strings) {
                    if (string == null) {
                        continue;
                    }
                    if (normalizeURL(string).equalsIgnoreCase(normalizeURL(URL))) {
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
    
    public void removeEmptyColumns() {
        parent.getlblUrlsCountData().setText("Removing empty columns...");
        ExtendedFile file = null;
        try {
            file = new ExtendedFile(pathToSave);
            file.initFile();

            columns = new ArrayList<>();
            for (int i = 0; i < file.headers.length; i++) {
                columns.add(new ColumnItem(file.headers[i], i));
            }
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (ColumnItem column : columns) {
            column.rows = new ArrayList<>();
            for (String[] line : file.getLines()) {
                column.rows.add(line[column.Index]);
            }
        }
        checkColumns(file);
    }
    
    public void checkColumns(ExtendedFile file) {
        boolean allEqual = false;
        for (ColumnItem column : columns) {
           allEqual = column.rows.stream().distinct().limit(2).count() <= 1 ;
            if (!allEqual) {
                column.isValidColumn = !allEqual;
            }
        }
        StringBuilder sb = new StringBuilder();
        
        String headersString = "";
        for (ColumnItem column : columns) {
            if (column.isValidColumn) {
                headersString += "\"" + column.Header + "\"" + ",";
            }
        }
        sb.append(StringUtils.removeEnd(headersString, ","));
        
        sb.append("\n");
        for (int i = 0; i < file.getLines().size(); i++) {
            String regularDataString = "";
            for (ColumnItem column : columns) {
                if (column.isValidColumn) {
                    regularDataString += "\"" + column.rows.get(i) + "\"" + ",";
                }
            }
            sb.append(StringUtils.removeEnd(regularDataString, ","));
            sb.append("\n");
        }
        
        try {
            Files.deleteIfExists(Paths.get(pathToSave));
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
               
    }
    
    
    
    
}