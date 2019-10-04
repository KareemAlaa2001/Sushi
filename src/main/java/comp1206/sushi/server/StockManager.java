package comp1206.sushi.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

public class StockManager {

	private Map<Dish,Number> dishStocks;
	private Map<Ingredient, Number> ingredientStocks;
	boolean ingRestockEnabled, dishRestockEnabled;
	
	public StockManager()
	{
		dishStocks = new HashMap<>();
		ingredientStocks = new HashMap<>();
		ingRestockEnabled = true; dishRestockEnabled = true;
	}
	
	public Map<Dish,Number> getDishes()
	{
		return this.dishStocks;
	}
	
	public Map<Ingredient,Number> getIngredients()
	{
		return this.ingredientStocks;
	}
	
	public boolean getDishRestockPermission()
	{
		return this.dishRestockEnabled;
	}
	
	public boolean getIngRestockPermission()
	{
		return this.ingRestockEnabled;
	}
	
	public void setDishStocks(Map<Dish,Number> stocks)
	{
		synchronized(this.dishStocks)
		{
			this.dishStocks = stocks;
		}
	}
	
	public void setIngredientStocks(Map<Ingredient,Number> stocks)
	{
		this.ingredientStocks = stocks;
	}
	
	public void setDishRestockPermission(boolean allowed)
	{
		this.dishRestockEnabled = allowed;
	}
	
	public void setIngRestockPermission(boolean allowed)
	{
		this.ingRestockEnabled = allowed;
	}
	
	public void increaseStock(Dish dish, Number number)
	{
		synchronized(getDishes())
		{
			if(dishRestockEnabled)
			{
				setStock(dish,(Integer) getStock(dish) +(Integer) number);
			}
			else throw new IllegalArgumentException("Dish restocking not enabled!");
		}
	}
	
	public void increaseStock(Ingredient ingredient, Number number)
	{
		synchronized(getIngredients()) 
		{
			synchronized(getDishes())
			{
				if (ingRestockEnabled)
				{
					System.out.println("Ing " + ingredient.getName() + " stock increased by " + number);
					setStock(ingredient,(Integer) getStock(ingredient) +(Integer) number);
				}			
				else throw new IllegalArgumentException("Ingredient restocking not enabled!");
			
				notifyRestockableDishes();
			}
		}
		
	}
	
	public void setStock(Dish dish, Number newStock)
	{
		synchronized(getDishes())
		{
			getDishes().remove(dish);
			getDishes().put(dish,newStock);
			notifyRestockableDishes();
		}
	}
	
	public void setStock(Ingredient ingredient, Number newStock) {
		synchronized(getIngredients())
		{
			getIngredients().remove(ingredient);
			getIngredients().put(ingredient, newStock);
			notifyRestockableDishes();
			notifyRestockableIngredients();
		}
		
	}
	
	private void notifyRestockableDishes()
	{
		synchronized(getDishes()) 
		{
			for (Dish dish: getDishList()) 
			{
				if (underThreshold(dish))
				{
					synchronized(dish)
					{
						if (isRestockable(dish))
						{
							dish.setRestockNeed(true);
							System.out.println("Dish " + dish.getName() + " restockable, staff member notified");
							this.getDishes().notify();
						}
					}
				} 
			}
		}
	}
	
	private void notifyRestockableIngredients()
	{
		synchronized(getIngredients()) 
		{
			for (Ingredient ing: getIngredientList()) 
			{
				synchronized(ing)
				{
					if (underThreshold(ing))
					{
						ing.setRestockNeed(true);
						System.out.println("Ingredient " + ing.getName() + " restockable, drone notified");
						this.getIngredients().notify();
					}
				}
			}
		}
	}
	public  Number getStock(Dish dish)
	{
		synchronized (dishStocks) 
		{
			return dishStocks.get(dish);
		}
	}
	 
	public  Number getStock(Ingredient ingredient)
	{
		synchronized (ingredientStocks) 
		{
			return ingredientStocks.get(ingredient);
		}
	}
	
	public void addNew(Dish dish)
	{
		synchronized(getDishes())
		{
			getDishes().put(dish, 0);
			getDishes().notify();
		}
	}

	public  void consumeDishes(Dish dish, Number num)
	{
		synchronized(getDishes())
		{
			this.setStock(dish, (Integer) getStock(dish) - (Integer) num);
			notifyRestockableDishes();
		}
	}
	
	public  void consumeIngredients(Ingredient ing, Number num)
	{
		synchronized(getIngredients())
		{
			this.setStock(ing, (Integer) getStock(ing) - (Integer) num);
			notifyRestockableIngredients();
		}
	}

	public void setDishRestockingEnabled(boolean isEnabled)
	{
		this.dishRestockEnabled = isEnabled;
	}
	
	public void setIngredientRestockingEnabled(boolean isEnabled)
	{
		this.ingRestockEnabled = isEnabled;
	}
	
	public boolean getDishRestockingEnabled()
	{
		return this.dishRestockEnabled;
	}
	
	public boolean getIngredientRestockingEnabled()
	{
		return this.ingRestockEnabled;
	}
	
	public void addNew(Ingredient ingredient)
	{
		synchronized(getIngredients())
		{
			this.getIngredients().put(ingredient, 0);
			this.getIngredients().notify();
		}
	}
	
	public void remove(Dish dish)
	{
		synchronized(getDishes())
		{
			this.getDishes().remove(dish);
		}
	}
	
	public void remove(Ingredient ingredient)
	{
		synchronized(getIngredients())
		{
			this.getIngredients().remove(ingredient);
		}
	}
	
	private List<Dish> getDishList()
	{
		List<Dish> dishes = new ArrayList<>();
		
		for (Map.Entry<Dish, Number> entry: getDishes().entrySet())
		{
			dishes.add(entry.getKey());
		}
		
		return dishes;
	}
	
	private List<Ingredient> getIngredientList()
	{
		List<Ingredient> ingredients = new ArrayList<>();
		
		for (Map.Entry<Ingredient, Number> entry: getIngredients().entrySet())
		{
			ingredients.add(entry.getKey());
		}
		
		return ingredients;
	}
	
	private boolean underThreshold(Dish dish)
	{
		if ((Integer) getStock(dish) < (Integer) dish.getRestockThreshold())
		{
			return true;
		}
		else return false;
	}
	
	private boolean underThreshold(Ingredient ingredient)
	{
		if ((Integer) getStock(ingredient) < (Integer) ingredient.getRestockThreshold())
		{
			return true;
		}
		else return false;	
	} 
		
	public boolean allDishesAboveThresholds()
	{
		synchronized (getDishes()) 
		{
			for (Dish dish: getDishList()) { 
				if (underThreshold(dish)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean allIngredientsAboveThresholds()
	{
		synchronized(getIngredients())
		{
			for (Ingredient ingredient : getIngredientList()) 
			{
				if (underThreshold(ingredient)) return false;
			} 
			return true;
		}
	}

	public List<Dish> getDishesNeedingRestock()
	{
		List<Dish> lowDishes = new ArrayList<>();
		synchronized(getDishes())
		{
			for (Dish dish: getDishList()) 
			{ 
				if (underThreshold(dish) && dish.needsRestock()) lowDishes.add(dish);
			}
		}

		return lowDishes;
	}
	
	public List<Ingredient> getIngredientsNeedingRestock()
	{
		List<Ingredient> lowIngs = new ArrayList<>();
		synchronized(getIngredients())
		{
			for (Ingredient ing: getIngredientList()) 
			{ 
				if (underThreshold(ing) && ing.needsRestock()) lowIngs.add(ing);
			}
		}
		
		return lowIngs;
	}

	public boolean isRestockable(Dish dish)
	{
		boolean restockable = true;
		Map<Ingredient, Number> recipe = dish.getRecipe();
		for (Map.Entry<Ingredient, Number> entry: recipe.entrySet())
		{
			synchronized(getIngredients())
			{
				if ((Integer) getStock(entry.getKey()) < (Integer) entry.getValue() * (Integer) dish.getRestockAmount()) restockable = false;
			}
		}
		return restockable;
	}

	public void clearAll() {
		this.dishStocks = new HashMap<>();
		this.ingredientStocks = new HashMap<>();
	}
}
