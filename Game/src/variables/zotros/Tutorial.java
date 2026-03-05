package variables.zotros;

import java.util.ArrayList;

public class Tutorial {
	private final int _id;
	private final ArrayList<Accion> _recompensa = new ArrayList<Accion>(4);
	private Accion _inicio, _final;
	
	public Tutorial(final int id, final String recompensa, final String inicio, final String fin) {
		_id = id;
		try {
			for (final String str : recompensa.split("\\$")) {
				if (str.isEmpty()) {
					_recompensa.add(null);
				} else {
					final String[] a = str.split("@");
					if (a.length >= 2) {
						_recompensa.add(new Accion(Integer.parseInt(a[0]), a[1], ""));
					} else {
						_recompensa.add(new Accion(Integer.parseInt(a[0]), "", ""));
					}
				}
			}
			if (inicio.isEmpty()) {
				_inicio = null;
			} else {
				final String[] b = inicio.split("@");
				if (b.length >= 2) {
					_inicio = new Accion(Integer.parseInt(b[0]), b[1], "");
				} else {
					_inicio = new Accion(Integer.parseInt(b[0]), "", "");
				}
			}
			if (fin.isEmpty()) {
				_final = null;
			} else {
				final String[] c = fin.split("@");
				if (c.length >= 2) {
					_final = new Accion(Integer.parseInt(c[0]), c[1], "");
				} else {
					_final = new Accion(Integer.parseInt(c[0]), "", "");
				}
			}
		} catch (final Exception e) {
			System.out.println("Ocurrio un error al cargar el tutorial " + id);
			System.exit(1);
		}
	}
	
	public ArrayList<Accion> getRecompensa() {
		return _recompensa;
	}
	
	public Accion getInicio() {
		return _inicio;
	}
	
	public Accion getFin() {
		return _final;
	}
	
	public int getID() {
		return _id;
	}
}
