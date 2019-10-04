package comp1206.sushi.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Supplier;
import comp1206.sushi.common.User;
import comp1206.sushi.server.ServerInterface.UnableToDeleteException;

class RemoveListener implements ActionListener {
		
		JTable table;
		ServerWindow window;
		ServerInterface server;
		public RemoveListener(JTable table,ServerWindow window) {
			
			this.table = table;
			this.window = window;
			this.server = window.getServer();
		}
		
		
		public void actionPerformed(ActionEvent e) {
			removeSelectedRows(table);
		}
		
		public void removeSelectedRows(JTable table){
			MyTableModel model = (MyTableModel) table.getModel();		   
			int[] rows = table.getSelectedRows();
			try {
				removeRowsFromServer(model, rows);
				window.refreshAll();
			} catch (UnableToDeleteException e) {
				JOptionPane.showMessageDialog(window,e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		private void removeRowsFromServer(MyTableModel model, int[] rows) throws UnableToDeleteException {
			Object[][] itemsToRemove = new Object [rows.length][model.getColumnCount()];
			
			for(int i=0;i<rows.length;i++)
			{
				for (int j = 0; j < model.getColumnCount(); j++)
				{
					itemsToRemove[i][j] = model.getValueAt(rows[i], j);
				} 
			}
			String idColName = model.getColumnName(1);
			for (int i = 0; i < rows.length; i++)
			{
				switch(idColName) {
				
					case "Latitude":
						Postcode pcToRm;
						
						for (int j = 0; j < server.getPostcodes().size(); j++)
						{
							if (server.getPostcodes().get(j).getName().equals(itemsToRemove[i][0]))
							{
								pcToRm = server.getPostcodes().get(j);
								for (Supplier supp: server.getSuppliers())
								{
									if (supp.getPostcode().equals(pcToRm))
									{
										throw new UnableToDeleteException("This postcode is in use by a supplier!");
									}
								}
								
								for (User user: server.getUsers())
								{
									if (user.getPostcode().equals(pcToRm))
									{
										throw new UnableToDeleteException("This postcode is in use by a user!");
									}
								}
								
								server.removePostcode(pcToRm);
							}
						}
						break;
						
					case "Status":
						for (int j = 0; j < server.getStaff().size(); j++)
						{
							if (server.getStaff().get(j).getName().equals(itemsToRemove[i][0]))
							{
								server.removeStaff(server.getStaff().get(j));
							}
						}
						break;
						
					case "Speed":
						for (int j = 0; j < server.getDrones().size(); j++)
						{
							if (server.getDrones().get(j).getSpeed().equals(itemsToRemove[i][1]))
							{
								server.removeDrone(server.getDrones().get(j));
								break;
							}
						}
						break;
						
					case "Unit":
						Ingredient ingToRm;
						
						for (int j = 0; j < server.getIngredients().size(); j++)
						{
							if (server.getIngredients().get(j).getName().equals(itemsToRemove[i][0]))
							{
								ingToRm = server.getIngredients().get(j);
								for (Dish dish: server.getDishes())
								{
									if (dish.getRecipe().containsKey(ingToRm))
									{
										throw new UnableToDeleteException("This ingredient is in use by a recipe in a dish!");
									}
								}
								server.removeIngredient(ingToRm);
								break;
							}
						}
						break;
					case "Description":
						
						server.removeDish(window.findDishByName(itemsToRemove[i][0].toString()));
						break;
						
					case "PostCode":
						Supplier suppToRm;
						
						for (int j = 0; j < server.getSuppliers().size(); j++)
						{
							if (server.getSuppliers().get(j).getName().equals(itemsToRemove[i][0]))
							{
								suppToRm = server.getSuppliers().get(j);
								for (Ingredient ing: server.getIngredients())
								{
									if (ing.getSupplier().equals(suppToRm))
									{
										throw new UnableToDeleteException("This supplier is used for an ingredient!");
									}
								}
								server.removeSupplier(suppToRm);
								break;
							}
						}
						break;
				}
		}
	}
		
}