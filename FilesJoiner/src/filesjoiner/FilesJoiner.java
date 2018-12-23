/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author ibodia
 */
public class FilesJoiner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(FilesJoiner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                }
                MainFrameGUI gui = new MainFrameGUI();
                gui.setTitle("Files joiner v1.3");
                gui.setResizable(false);
                gui.setVisible(true);
                LogicSingleton.initParent(gui);
            }
        });
    }
}
