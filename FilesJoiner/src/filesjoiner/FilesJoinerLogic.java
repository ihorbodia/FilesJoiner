/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ibodia
 */
public class FilesJoinerLogic {
    ArrayList<File> files;
    ArrayList<String[]> resultList;
    Map<String, Integer> headers = new HashMap<String, Integer>();
    
    public FilesJoinerLogic(){
        
    }
    
    public void initFilesList(ArrayList<File> inputFiles) {
        if (inputFiles != null) {
            files = inputFiles;
        }
    }
    

    public void StartRun() {
        resultList = new ArrayList<String[]>();
        initHeaders();
        
        for (File file : files) {
            try {
                Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
                CSVReader csvReader = new CSVReader(reader);
                String[] nextRecord;
                CSVReader csvReaderAll = new CSVReaderBuilder(reader).withSkipLines(1).build();
                
                int counter = 0;
                while ((nextRecord = csvReader.readNext()) != null) {
                    if (counter == 0) {
                       normalizeHeaders(nextRecord); 
                    }
//                    System.out.println("Name : " + nextRecord[0]);
//                    System.out.println("Email : " + nextRecord[1]);
//                    System.out.println("Phone : " + nextRecord[2]);
//                    System.out.println("Country : " + nextRecord[3]);
//                    System.out.println("==========================");
                    counter++;
                }
                
                String[] headerss = csvReaderAll.peek();
            } catch (IOException ex) {
                Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void normalizeHeaders(String[] headerRow){
        if (headerRow.length == 1) {
            return;
        }
        for (String header : headerRow) {
            Object value = null;
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(header.replace(" ", "").toLowerCase())) {
                    value = entry;
                    break;
                }
            }
            
            if (value == null) {
                int lastKey = 0;
                for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                    lastKey = entry.getValue();
                }
                headers.put(header, lastKey++);
            }
        }
        //Set<String> s = new HashSet<String>(headersToCompare.keySet());
        //s.retainAll(fileHeaders.keySet());
        System.out.println(headers);
        //Maps.differences(fileHeaders, headers);
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


