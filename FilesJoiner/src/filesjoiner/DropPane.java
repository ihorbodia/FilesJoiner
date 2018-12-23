/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author ibodia
 */
public class DropPane extends JPanel {

    private JTable table;
    private JScrollPane scroll;
    private DefaultTableModel tm = new DefaultTableModel(new String[]{"File", "File Type", "Size(Kb)"}, 0);
    
    public DropPane() {
        table = new JTable();
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(Color.GRAY);

        table.setModel(tm);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        
        this.setLayout(new GridLayout());
        Border padding = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        this.setBorder(padding);
        scroll = new JScrollPane(table);

        table.setDropTarget(new DropTarget() {
            @Override
            public synchronized void dragOver(DropTargetDragEvent dtde) {
                Point point = dtde.getLocation();
                int row = table.rowAtPoint(point);
                if (row < 0) {
                    table.clearSelection();
                } else {
                    table.setRowSelectionInterval(row, row);
                }
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }

            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                tm = new DefaultTableModel(new String[]{"File", "File Type", "Size(Kb)"}, 0);
                table.setModel(tm);
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = dtde.getTransferable();
                    ArrayList<ExtendedFile> fileList = null;
                    ArrayList<ExtendedFile> fileListToTransfer = new ArrayList<ExtendedFile>();
                    try {
                        ArrayList<ArrayList<ExtendedFile>> arr = new ArrayList(Arrays.asList(t.getTransferData(DataFlavor.javaFileListFlavor)));
                        fileList = new ArrayList(arr.get(0));
                        fileList = getFiles(fileList);
                        if (fileList.size() > 0) {
                            LogicSingleton.setCountToZero();
                            table.clearSelection();
                            Point point = dtde.getLocation();
                            int row = table.rowAtPoint(point);
                            DefaultTableModel model = (DefaultTableModel) table.getModel();
                            for (Object value : fileList) {
                                if (value instanceof File) {
                                    File f = (File) value;
                                    fileListToTransfer.add(new ExtendedFile(f.getAbsolutePath()));
                                    if (FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("csv")
                                            || FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("txt")
                                            || FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("xlsx")
                                            || FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("xls")) {
                                        if (row < 0) {
                                            model.addRow(new Object[]{f.getName(), FilenameUtils.getExtension(f.getAbsolutePath()), (int)Math.ceil(f.length() / 1024.0)});
                                        } else {
                                            model.insertRow(row, new Object[]{f.getName(), FilenameUtils.getExtension(f.getAbsolutePath()), (int)Math.ceil(f.length() / 1024.0)});
                                            row++;
                                        }
                                    }
                                }
                            }
                            LogicSingleton.getLogic().initFilesList(fileListToTransfer);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DropPane.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedFlavorException e) {
                        e.printStackTrace();
                    }
                } else {
                    dtde.rejectDrop();
                }
            }

        });
        add(scroll, BorderLayout.CENTER);
    }
    
    public ArrayList<ExtendedFile> getFiles(ArrayList<ExtendedFile> files){
        ArrayList<File> allFiles = new ArrayList<File>();
        String[] exts = {"xlsx", "xls", "csv", "txt"};
        for (File file : files) {
            LogicSingleton.getLogic().setOutputPath(file.getParentFile().getAbsolutePath());
            if (file.isDirectory()) {
                allFiles.addAll(FileUtils.listFiles(file, exts, true));
            }
            else {
                allFiles.add(file);
            }
        }
        ArrayList<ExtendedFile> result = new ArrayList<ExtendedFile>();
        for (File file : allFiles) {
            if (FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("xlsx") ||
                FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("xls") ||
                FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("txt") || 
                FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("csv")) {
                result.add(new ExtendedFile(file.getAbsolutePath()));
            }
        }
        return result;
    }
}
