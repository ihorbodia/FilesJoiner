/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import java.util.ArrayList;

public class ColumnItem {
    public String Header;
    public int Index;
    public ArrayList<String> rows;
    public boolean isValidColumn;
    
    public ColumnItem(String header, int index) {
        Header = header;
        Index = index;
    }
}
