/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import javax.swing.JLabel;

/**
 *
 * @author ibodia
 */
public class LogicSingleton {
    private static FilesJoinerLogic logic;
    private static MainFrameGUI parent;
    public static FilesJoinerLogic getLogic() {
            if (logic == null && parent != null) {
                logic = new FilesJoinerLogic(parent);
            }
            return logic;
    }
    
    public static void initParent(MainFrameGUI inParent){
        parent = inParent;
    }
    
    public static void nullLogicObject() {
        logic = null;
    }
    
    public static void setCountToZero() {
        parent.getlblUrlsCountData().setText("0");
    }
    
    public static void setOutputPath(String path) {
        logic.setOutputPath(path);
    }
}
