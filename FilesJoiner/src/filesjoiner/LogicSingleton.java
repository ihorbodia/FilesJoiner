/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.util.ArrayList;

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
    
    public static void recreateLogicObject(ArrayList<ExtendedFile> files) {
        String op = logic.outputPath;
        logic = null;
        logic = new FilesJoinerLogic(parent);
        logic.initFilesList(files);
        if (files.size() > 0) {
            logic.outputPath = files.get(0).getParentFile().getAbsolutePath();
        }
    }
      
    public static void setCountToZero() {
        parent.getlblUrlsCountData().setText("0");
    }
}
