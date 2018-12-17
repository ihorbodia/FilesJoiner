/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ihor
 */
public class ExtendedFile extends File {

    static ExtendedFile DEFAULT;
    
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
}
