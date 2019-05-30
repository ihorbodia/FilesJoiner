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
        logic = null;
        logic = new FilesJoinerLogic(parent);
        logic.initFilesList(files);
        if (files != null && files.size() > 0) {
            logic.outputPath = files.get(0).getParentFile().getAbsolutePath();
        }
    }
      
    public static void setCountToZero() {
        parent.getlblUrlsCountData().setText("0");
    }
}
