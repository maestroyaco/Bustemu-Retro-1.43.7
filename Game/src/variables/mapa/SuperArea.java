package variables.mapa;

import java.util.ArrayList;

public class SuperArea {
	private final int _id;
	private final ArrayList<Area> _areas = new ArrayList<Area>();
	
	public SuperArea(final int id) {
		_id = id;
	}
	
	public void addArea(final Area area) {
		_areas.add(area);
	}
	
	public int getID() {
		return _id;
	}
}