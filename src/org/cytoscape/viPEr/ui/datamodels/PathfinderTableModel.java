package org.cytoscape.viPEr.ui.datamodels;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import org.cytoscape.viPEr.ui.datamodels.ResultEntry;

/**
 * Tablemodel for Resultstab
 * @author MGarmhausen
 */
public class PathfinderTableModel extends AbstractTableModel{
    public static final int SCORE_COLUMN = 0;
    public static final int PATH_COLUMN = 1;
    public static final int PATH_LENGTH = 2;
    public static final int COLUMN_COUNT = 3;
    public static final int PATH_WITH_NAMES = 4;
    
    private final List<ResultEntry> entries = new ArrayList<ResultEntry>();
    
    public Object getValueAt(int row, int column){
        ResultEntry resultEntry = entries.get(row);
        switch(column){
            case SCORE_COLUMN:
                return resultEntry.getScore();
            case PATH_COLUMN:
                return resultEntry.getPath();
            case PATH_LENGTH:
                return resultEntry.getPathLength();
            case PATH_WITH_NAMES:
                return resultEntry.getPathWithNames();
        }
        return null;
    }
    
    public int getColumnCount(){
        return COLUMN_COUNT;
    }
    
    public int getRowCount(){
        return entries.size();
    }
    
    public void add (ResultEntry entry){
        int index = entries.size();
        entries.add(entry);
        fireTableRowsInserted(index, index);
    }
    
    public void add (List<ResultEntry> newEntries){
        int first = entries.size();
        int last = first + newEntries.size() -1;
        entries.addAll(newEntries);
        fireTableRowsInserted(first, last);
    }
    
    @Override
    public Class getColumnClass(int column){
        return getValueAt(0, column).getClass();
    }
    
    public ResultEntry getEntry(int row){
        return entries.get(row);   
    }
    
            
    
}
