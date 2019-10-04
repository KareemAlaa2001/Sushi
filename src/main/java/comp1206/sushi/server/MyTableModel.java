package comp1206.sushi.server;

import javax.swing.table.DefaultTableModel;


public class MyTableModel extends DefaultTableModel {

	public MyTableModel(Object[] columns, int i) {
		super(columns,i);
	}

	@Override
    public boolean isCellEditable(int row, int column) {
       //all cells false
       return false;
    }
}
