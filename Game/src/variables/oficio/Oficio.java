package variables.oficio;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Oficio {
	private final int _id;
	private final ArrayList<Integer> _herramientas = new ArrayList<Integer>();
	private final Map<Integer, ArrayList<Integer>> _recetas = new TreeMap<Integer, ArrayList<Integer>>();
	
	public Oficio(final int id, final String herramientas, final String recetas) {
		_id = id;
		if (!herramientas.isEmpty()) {
			for (final String str : herramientas.split(",")) {
				try {
					_herramientas.add(Integer.parseInt(str));
				} catch (final Exception e) {}
			}
		}
		if (!recetas.isEmpty()) {
			for (final String str : recetas.split(Pattern.quote("|"))) {
				try {
					final int trabajoID = Integer.parseInt(str.split(";")[0]);
					final ArrayList<Integer> list = new ArrayList<Integer>();
					for (final String str2 : str.split(";")[1].split(",")) {
						list.add(Integer.parseInt(str2));
					}
					_recetas.put(trabajoID, list);
				} catch (final Exception e) {}
			}
		}
	}
	
	public ArrayList<Integer> listaRecetaPorTrabajo(final int trabajo) {
		return _recetas.get(trabajo);
	}
	
	public boolean puedeReceta(final int trabajo, final int modelo) {
		if (_recetas.get(trabajo) != null) {
			for (final int a : _recetas.get(trabajo)) {
				if (a == modelo) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int getID() {
		return _id;
	}
	
	public boolean esHerramientaValida(final int idObjModelo) {
		return _herramientas.isEmpty() || _herramientas.contains(idObjModelo);
	}
}
