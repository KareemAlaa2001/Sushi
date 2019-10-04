package comp1206.sushi.common;

import java.util.HashMap;
import java.util.Map;

import comp1206.sushi.common.Dish;
import comp1206.sushi.common.Ingredient;

public class Dish extends Model {

	private String name;
	private String description;
	private Number price;
	private Map <Ingredient,Number> recipe;
	private Number restockThreshold;
	private Number restockAmount;
	private boolean needsRestock;
	private Number weight;

	public Dish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.restockThreshold = restockThreshold;
		this.restockAmount = restockAmount;
		this.recipe = new HashMap<Ingredient,Number>();
		this.needsRestock = true;
		calculateWeight();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.notifyUpdate("name", this.name, name);
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.notifyUpdate("description", this.description, description);
		this.description = description;
	}

	public Number getPrice() {
		return price;
	}

	public void setPrice(Number price) {
		this.notifyUpdate("price", this.price, price);
		this.price = price;
	}

	public Map <Ingredient,Number> getRecipe() {
		return recipe;
	}

	public void setRecipe(Map <Ingredient,Number> recipe) {
		this.notifyUpdate("recipe", this.recipe, recipe);
		this.recipe = recipe;
		calculateWeight();
	}

	public void setRestockThreshold(Number restockThreshold) {
		this.notifyUpdate("restockThreshold", this.restockThreshold, restockThreshold);
		this.restockThreshold = restockThreshold;
	}
	
	public void setRestockAmount(Number restockAmount) {
		this.notifyUpdate("restockAmount", this.restockAmount, restockAmount);
		this.restockAmount = restockAmount;
	}

	public Number getRestockThreshold() {
		return this.restockThreshold;
	}

	public Number getRestockAmount() {
		return this.restockAmount;
	}
	
	public boolean needsRestock()
	{
		return this.needsRestock;
	}
	
	public void setRestockNeed(boolean need)
	{
		this.notifyUpdate("needsRestock", this.needsRestock, need);
		this.needsRestock = need;
	}
	
	private void calculateWeight()
	{
		Double weight = 0.0;
		for(Map.Entry<Ingredient, Number> entry: getRecipe().entrySet())
		{
			weight +=(Integer) entry.getKey().getWeight() * (Integer) entry.getValue();
		}
		setWeight(weight);
	}
	
	public Number getWeight()
	{
		return this.weight;
	}
	
	private void setWeight(Number weight)
	{
		this.notifyUpdate("weight", this.weight, weight);
		this.weight = weight;
	}
	
	public void addIngredientToRecipe(Ingredient ing, Number num)
	{
		if ((Integer) num > 0)
		{
			Map<Ingredient,Number> oldRecipe = getRecipe();
			getRecipe().put(ing,num);
			notifyUpdate("recipe", oldRecipe,getRecipe());
			calculateWeight();
		}
		else throw new IllegalArgumentException("Number put in is not positive!");
	}
	
	public void removeIngredientFrommRecipe(Ingredient ing)
	{
		Map<Ingredient,Number> oldRecipe = getRecipe();
		getRecipe().remove(ing);
		notifyUpdate("recipe", oldRecipe,getRecipe());
		calculateWeight();
	}
}
