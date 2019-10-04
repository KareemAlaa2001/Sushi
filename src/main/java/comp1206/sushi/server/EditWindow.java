package comp1206.sushi.server;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditWindow extends JFrame {

	JTable table;
	ServerWindow window;
	ServerInterface server;
	int selectedRow;
	
	public EditWindow(String title, JTable table, int selectedRow, ServerWindow window)
	{
		super(title);
		this.window = window;
		this.server = window.getServer();
		this.table = table;
		this.selectedRow = selectedRow;
		init();
	}
	
	public void init()
	{
		JPanel content = new JPanel();
		this.setContentPane(content);
		
		content.setLayout(new BorderLayout());
		
		JPanel main = new JPanel();
		main.setLayout(new FlowLayout());
		
		JPanel south = new JPanel();
		south.setLayout(new FlowLayout());
		
		
		JTextField thresh = new JTextField(30);
		JTextField amount = new JTextField(30);
		
		main.add(new JLabel("New Restock Threshold:"));
		main.add(thresh);	
	
		main.add(new JLabel("New Restock Amount:"));
		main.add(amount);
		
		JButton confirm = new JButton("Confirm Changes");
		
		class ConfirmListener implements ActionListener {
			
			EditWindow editWind;
			
			ConfirmListener(EditWindow window)
			{
				this.editWind = window;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(window.checkForRestocks(window.getColumnNames(table)))
				{
					if (((MyTableModel) table.getModel()).getColumnName(2).equals("Supplier"))
					{
						Ingredient ingToEdit = window.findIngredientByName(table.getValueAt(selectedRow, 0).toString());
							
						ingToEdit.setRestockThreshold(Integer.valueOf(thresh.getText()));
						ingToEdit.setRestockAmount(Integer.valueOf(amount.getText()));
					}
						
					else if (((MyTableModel) table.getModel()).getColumnName(2).equals("Price"))
					{
						Dish dishToEdit = window.findDishByName(table.getValueAt(selectedRow, 0).toString());
						dishToEdit.setRestockThreshold(Integer.valueOf(thresh.getText()));
						dishToEdit.setRestockAmount(Integer.valueOf(amount.getText()));

					}
				}
				
				
				editWind.setVisible(false);
				window.refreshAll();
			}
			
			
		}
		
		south.add(confirm);
		confirm.addActionListener(new ConfirmListener(this));
		
		content.add(main, BorderLayout.CENTER);
		content.add(south, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);
		this.setSize(400,300);
		this.setVisible(true);
	
	}
}
