package variables.mision;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Pattern;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.personaje.Personaje;
import variables.zotros.Accion;
import estaticos.Constantes;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class MisionEtapaModelo {
	private final String[] _recompensas = new String[7];
	private final String _nombre;
	private String _recompensa;
	private final int _id;
	private final ArrayList<TreeMap<Integer, MisionObjetivoModelo>> _objetivos = new ArrayList<>();
	private String _strObjetivos = "";
	
	public MisionEtapaModelo(final int id, final String recompensas, final String objetivos, final String nombre) {
		_id = id;
		_nombre = nombre;
		setRecompensa(recompensas);
		setObjetivos(objetivos);
	}
	
	public String getRecompensa() {
		return _recompensa;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public void setObjetivos(final String objetivos) {
		_objetivos.clear();
		_strObjetivos = objetivos;
		for (final String s : objetivos.split(Pattern.quote("|"))) {
			TreeMap<Integer, MisionObjetivoModelo> map = new TreeMap<>();
			for (final String str : s.split(",")) {
				try {
					int idObjetivo = Integer.parseInt(str);
					final MisionObjetivoModelo objetivo = Mundo.getMisionObjetivoModelo(idObjetivo);
					if (objetivo != null) {
						map.put(idObjetivo, objetivo);
					}
				} catch (final Exception e) {}
			}
			_objetivos.add(map);
		}
	}
	
	public String strObjetivos() {
		return _strObjetivos;
	}
	
	public void setRecompensa(final String recompensas) {
		_recompensa = recompensas;
		byte i = 0;
		for (final String str : recompensas.split(Pattern.quote("|"))) {
			try {
				if (!str.equalsIgnoreCase("null")) {
					_recompensas[i] = str;
				}
			} catch (final Exception e) {}
			i++;
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public TreeMap<Integer, MisionObjetivoModelo> getObjetivosPorNivel(int nivel) {
		if (_objetivos.size() <= nivel) {
			return null;
		}
		return _objetivos.get(nivel);
	}
	
	// XP, KAMAS, ITEMS, EMOTES, OFICIOS, HECHIZO = 1000|5000|
	// 311,15;9336,5;....etc|8,9,....|51,52,....|145,966,....
	public void darRecompensa(final Personaje perso) {
		for (byte i = 0; i < 7; i++) {
			try {
				if (_recompensas[i] != null) {
					switch (i) {
						case 0 :// dar xp
							perso.addExperiencia(Integer.parseInt(_recompensas[0]), true);
							break;
						case 1 :// dar kamas
							perso.addKamas(Integer.parseInt(_recompensas[1]), true, true);
							break;
						case 2 :// dar objetos
							for (final String str : _recompensas[2].split(";")) {
								if (str.isEmpty()) {
									continue;
								}
								try {
									final int id = Integer.parseInt(str.split(",")[0]);
									final int cant = Integer.parseInt(str.split(",")[1]);
									perso.addObjIdentAInventario(Mundo.getObjetoModelo(id).crearObjeto(cant,
									Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), false);
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cant + "~" + id);
								} catch (final Exception e) {}
							}
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
							break;
						case 3 :// dar emotes
							for (final String str : _recompensas[3].split(",")) {
								try {
									if (str.isEmpty()) {
										continue;
									}
									new Accion(65, str, "").realizarAccion(perso, perso, -1, (short) -1);
								} catch (final Exception e) {}
							}
							break;
						case 4 :// oficios
							for (final String str : _recompensas[4].split(",")) {
								try {
									if (str.isEmpty()) {
										continue;
									}
									new Accion(6, str, "").realizarAccion(perso, perso, -1, (short) -1);
								} catch (final Exception e) {}
							}
							break;
						case 5 :// hechizo
							for (final String str : _recompensas[5].split(",")) {
								try {
									if (str.isEmpty()) {
										continue;
									}
									new Accion(9, str, "").realizarAccion(perso, perso, -1, (short) -1);
								} catch (final Exception e) {}
							}
							break;
						case 6 :// acciones
							for (final String str : _recompensas[6].split(Pattern.quote("*"))) {
								try {
									if (str.isEmpty()) {
										continue;
									}
									int accion = Integer.parseInt(str.split("@")[0]);
									String arg = "";
									try {
										arg = str.split("@")[1];
									} catch (Exception e) {}
									new Accion(accion, arg, "").realizarAccion(perso, perso, -1, (short) -1);
								} catch (final Exception e) {}
							}
							break;
					}
				}
			} catch (final Exception e) {}
		}
	}
}