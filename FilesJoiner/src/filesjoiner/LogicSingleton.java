/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

/**
 *
 * @author ibodia
 */
public class LogicSingleton {
    private static FilesJoinerLogic logic;
    public static FilesJoinerLogic getLogic() {
            if (logic == null) {
                logic = new FilesJoinerLogic();
            }
            return logic;
    }
}
