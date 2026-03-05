package variables.personaje;

import java.util.ArrayList;
import estaticos.GestorSalida;
import estaticos.MainServidor;

public class GrupoKoliseo {
	private final ArrayList<Personaje> _kolis = new ArrayList<Personaje>();
	
	public GrupoKoliseo(final Personaje koli1) {
		_kolis.add(koli1);
	}
	
	public int getPuntuacion() {
		int punt = 0;
		for (final Personaje p : _kolis) {
			punt += p.getPuntoKoli();
		}
		return punt;
	}
	
	public void dejarGrupo(final Personaje p) {
		if (!_kolis.contains(p)) {
			return;
		}
		p.setGrupoKoliseo(null);
		_kolis.remove(p);
		try {
			if (_kolis.size() == 1) {
				_kolis.get(0).setGrupoKoliseo(null);
				GestorSalida.ENVIAR_kV_DEJAR_KOLISEO(_kolis.get(0));
			} else {
				GestorSalida.ENVIAR_kM_EXPULSAR_PJ_KOLISEO(this, p.getID());
			}
		} catch (final Exception e) {}
	}
	
	public void limpiarGrupo() {
		for (final Personaje p : _kolis) {
			p.setGrupoKoliseo(null);
			GestorSalida.ENVIAR_kV_DEJAR_KOLISEO(p);
		}
		_kolis.clear();
	}
	
	public int getCantPjs() {
		return _kolis.size();
	}
	
	public boolean addPersonaje(final Personaje koli) {
		if (_kolis.size() >= MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO) {
			return false;
		}
		if (MainServidor.RANGO_NIVEL_KOLISEO > 0) {
			for (final Personaje p : _kolis) {
				if (p.getNivel() > koli.getNivel() + MainServidor.RANGO_NIVEL_KOLISEO) {
					return false;
				}
				if (p.getNivel() < koli.getNivel() - MainServidor.RANGO_NIVEL_KOLISEO) {
					return false;
				}
			}
		}
		GestorSalida.ENVIAR_kM_AGREGAR_PJ_KOLISEO(this, koli.stringInfoGrupo());
		_kolis.add(koli);
		return true;
	}
	
	public ArrayList<Personaje> getMiembros() {
		final ArrayList<Personaje> grupo = new ArrayList<Personaje>();
		grupo.addAll(_kolis);
		return grupo;
	}
	
	public boolean contieneIPOtroGrupo(GrupoKoliseo grupo) {
		if (MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_KOLISEO) {
			return false;
		}
		for (final Personaje p : _kolis) {
			for (final Personaje p2 : grupo._kolis) {
				if (p.getCuenta().getActualIP().equalsIgnoreCase(p2.getCuenta().getActualIP())) {
					return true;
				}
			}
		}
		return false;
	}
}