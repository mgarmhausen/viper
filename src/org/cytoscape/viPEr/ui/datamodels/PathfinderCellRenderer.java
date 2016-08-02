package org.cytoscape.viPEr.ui.datamodels;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import javax.swing.UIManager;
import java.awt.Component;
import javax.swing.JTable;


/**
 *
 * @author MGarmhausen
 * Renders table cells with switching row colors
 */
public class PathfinderCellRenderer {
    
    public static class RowRenderer extends DefaultTableCellRenderer{
        private Color rowColors[];
        
        public RowRenderer(){
            rowColors = new Color[1];
            rowColors[0] = UIManager.getColor("table.background");
        }
                
        public RowRenderer(Color colors[]){
            super();
            setRowColors(colors);
        }
        
        public void setRowColors(Color colors[]){
            rowColors = colors;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,  int column){
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value != null ? value.toString() : "unknown");
            if (!isSelected){
                setBackground(rowColors[row % rowColors.length]);
            }
            return this;
        }
        
        public boolean isOpaque() {
            return true;
        }
    }
}
