/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.List;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
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
        //table.setPreferredSize(new Dimension(1500, 300));
        
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
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable t = dtde.getTransferable();
                    ArrayList<File> fileList = null;
                    try {
                        ArrayList<ArrayList<File>> arr = new ArrayList(Arrays.asList(t.getTransferData(DataFlavor.javaFileListFlavor)));
                        fileList = new ArrayList(arr.get(0));
                        if (fileList.size() > 0) {
                            table.clearSelection();
                            Point point = dtde.getLocation();
                            int row = table.rowAtPoint(point);
                            DefaultTableModel model = (DefaultTableModel) table.getModel();
                            for (Object value : fileList) {
                                if (value instanceof File) {
                                    File f = (File) value;
                                    if (FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("csv")
                                            || FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("txt")
                                            || FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("xlsx")) {
                                        if (row < 0) {
                                            model.addRow(new Object[]{f.getName(), FilenameUtils.getExtension(f.getAbsolutePath()), f.length()});
                                        } else {
                                            model.insertRow(row, new Object[]{f.getName(), FilenameUtils.getExtension(f.getAbsolutePath()), f.length()});
                                            row++;
                                        }
                                    }
                                }
                            }
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
}
