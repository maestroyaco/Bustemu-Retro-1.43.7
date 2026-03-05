package variables.npc;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ObjetoTrueque implements Comparable<ObjetoTrueque> {
	private int _id;
	private Map<Integer, Integer> _necesita = new TreeMap<Integer, Integer>();
	private int _prioridad;
	private ArrayList<Integer> _npcs;
	
	public ObjetoTrueque(int id, String necesita, int prioridad, String npcs) {
		_id = id;
		for (String s : necesita.split(";")) {
			try {
				_necesita.put(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
			} catch (Exception e) {}
		}
		_prioridad = prioridad;
		if (!npcs.isEmpty()) {
			for (String s : npcs.split(",")) {
				if (s.isEmpty()) {
					continue;
				}
				try {
					if (_npcs == null) {
						_npcs = new ArrayList<>();
					}
					_npcs.add(Integer.parseInt(s));
				} catch (Exception e) {}
			}
		}
	}
	
	public Map<Integer, Integer> getNecesita() {
		return _necesita;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getPrioridad() {
		return _prioridad;
	}
	
	public boolean permiteNPC(int id) {
		return _npcs == null || _npcs.isEmpty() || _npcs.contains(id);
	}
	
	public int compareTo(final ObjetoTrueque obj) {
		final long otro = obj.getPrioridad();
		if (otro > _prioridad) {
			return 1;
		}
		if (otro == _prioridad) {
			return 0;
		}
		if (otro < _prioridad) {
			return -1;
		}
		return 0;
	}
}
