package comp1206.sushi.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Drone;
import comp1206.sushi.common.Ingredient;
import comp1206.sushi.common.Order;
import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Staff;
import comp1206.sushi.common.Supplier;
import comp1206.sushi.common.User;

public class DataPopulator {
	
	ServerInterface server;
	ServerWindow window;
	
	public DataPopulator(ServerWindow window) {
		
		this.window = window;
		this.server = window.getServer();
	}

	private void populateMockPostCodes(MyTableModel model) {
		
		for (int i = 0; i < server.getPostcodes().size(); i++)
		{
			model.addRow(new Object[] {server.getPostcodes().get(i).getName(), server.getPostcodes().get(i).getLat().toString(),
					server.getPostcodes().get(i).getLong(), server.getPostcodes().get(i).getDistance()});
		}
	}
	
	private void populateMockStaff(MyTableModel model) {
		
		List<Staff> stfList = server.getStaff();
		
		for (int i = 0; i < stfList.size(); i++)
		{
			model.addRow(new Object[] {stfList.get(i).getName(),server.getStaffStatus(stfList.get(i)),stfList.get(i).getFatigue()});
		}
	}
	
	private void populateMockDrones(MyTableModel model) {
	
		List<Drone> drnList = server.getDrones();
	
		for (int i = 0; i < drnList.size(); i++)
		{
			Drone currDrn = drnList.get(i);
			model.addRow(new Object[] {currDrn.getName(), server.getDroneSpeed(currDrn),currDrn.getCapacity(),currDrn.getBattery(),
					server.getDroneStatus(currDrn),server.getDroneSource(currDrn),server.getDroneDestination(currDrn),currDrn.getProgress()});
		}
	}
	
	private void populateMockSuppliers(MyTableModel model) {
		
		List<Supplier> suppList = server.getSuppliers();
		
		for (int i = 0; i < suppList.size(); i++)
		{
			model.addRow(new Object[] {suppList.get(i).getName(),suppList.get(i).getPostcode(),server.getSupplierDistance(suppList.get(i))});
		}
	}
	
	private void populateMockIngredients(MyTableModel model) {
		
		List<Ingredient> ingList = server.getIngredients();
		
		for (Ingredient ing: ingList)
		{
			
			model.addRow(new Object[] {ing.getName(),ing.getUnit(),ing.getSupplier(),ing.getRestockAmount(),ing.getRestockThreshold(), server.getIngredientStockLevels().get(ing)});
		}
	}
	
	private void populateMockDishes(MyTableModel dishModel, List<MyTableModel> recipeModels) {
		MyTableModel recipe;
		for (Dish dish: server.getDishes())
		{
			dishModel.addRow(new Object[] {dish.getName(),dish.getDescription(),dish.getPrice(),dish.getRestockThreshold(),dish.getRestockAmount(), server.getDishStockLevels().get(dish)});
			recipe = new MyTableModel(new Object[] {"Ingredient","Number"},0);
			Iterator<Entry<Ingredient, Number>> it = dish.getRecipe().entrySet().iterator();
			
			while (it.hasNext()) {
				Map.Entry<Ingredient,Number> pair = (Map.Entry<Ingredient,Number>) it.next();
				recipe.addRow(new Object[] {pair.getKey(),pair.getValue()});
			}
			recipeModels.add(recipe);
		}
	}
	
	private void populateMockUsers(MyTableModel model) {
		
		List<User> userList = server.getUsers();
		
		for (User user: userList)
		{
			model.addRow(new Object[] {user.getName(),"","",user.getPostcode(),user.getDistance()});
		}
	}
	
	private void populateMockOrders(MyTableModel model) {
		
		List<Order> orderList = server.getOrders();
	
		for (Order order: orderList)
		{
			model.addRow(new Object[] {order.getName(),server.getOrderStatus(order),server.getOrderDistance(order),server.getOrderCost(order)});
		}
	}
	
	public void populateServerData() {
		
		populateMockPostCodes((MyTableModel) window.getPcPanel().getTable().getModel());
		populateMockStaff((MyTableModel) window.getStaffPanel().getTable().getModel());
		populateMockDrones((MyTableModel) window.getDronesPanel().getTable().getModel());
		populateMockSuppliers((MyTableModel) window.getSuppliersPanel().getTable().getModel());
		populateMockIngredients((MyTableModel) window.getIngredientsPanel().getTable().getModel());
		populateMockDishes((MyTableModel) window.getDishesPanel().getDishTable().getModel(), window.getDishesPanel().getRecipeModels());
		populateMockUsers((MyTableModel) window.getUsersPanel().getTable().getModel());
		populateMockOrders((MyTableModel) window.getOrdersPanel().getTable().getModel());
	}
}
