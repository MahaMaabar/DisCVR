package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class TableHyperLink {

    public static void main(String[] args) {
        new TableHyperLink();
    }

    public TableHyperLink() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                try {
                    MyModel model = new MyModel();
                    JTable table = new JTable(model);
                    table.setDefaultRenderer(URL.class, new URLTableCellRenderer());
                    table.setDefaultEditor(URL.class, new URLTableCellEditor());

                    JFrame frame = new JFrame("Testing");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setLayout(new BorderLayout());
                    frame.add(new JScrollPane(table));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                } catch (MalformedURLException exp) {
                    exp.printStackTrace();
                }
            }
        });
    }

    public class MyModel extends DefaultTableModel {

        public MyModel() throws MalformedURLException {
            super(new Object[][]{{new URL("http://stackoverflow.com")}}, 
                    new Object[]{"Link"});
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return URL.class; // Only have one column :P
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

    }

    public class URLTableCellRenderer extends DefaultTableCellRenderer {

        public URLTableCellRenderer() {
            setForeground(Color.BLUE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
            if (value instanceof URL) {
                value = "<html><u>" + ((URL)value).toString() + "</u></html>";
                setText(value.toString());
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }

    }

    public class URLTableCellEditor extends AbstractCellEditor implements TableCellEditor {

        private URL url;

        @Override
        public Object getCellEditorValue() {
        	
            return url;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JLabel editor = new JLabel("Clicked");
            if (value instanceof URL) {
                url = (URL) value;
                editor.setText("<html><ul>" + url.toString() + "</ul></html>");
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    stopCellEditing();
                    try {
                        Desktop.getDesktop().browse(url.toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            return editor;
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            boolean editable = false;
            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                if (me.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(me)) {
                    editable = true;
                }
            }
            return editable;
        }
    }        
}
