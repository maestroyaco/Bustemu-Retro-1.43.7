package variables.gremio;

import java.util.Map;
import java.util.TreeMap;
import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.Mundo;

public class MiembroGremio {
	private int _porcXpDonada;
	private final int _id;
	private int _rango, _derechos;
	private long _xpDonada;
	private final Gremio _gremio;
	private final Personaje _perso;
	private final Map<Integer, Boolean> _tieneDerecho = new TreeMap<Integer, Boolean>();
	
	public MiembroGremio(final int id, final Gremio gremio, final int rango, final long xpDonada, final byte porcXp,
	final int derechos) {
		_id = id;
		_perso = Mundo.getPersonaje(id);
		_gremio = gremio;
		_rango = rango;
		_xpDonada = xpDonada;
		_porcXpDonada = porcXp;
		convertirDerechosAInt(derechos);
		_perso.setMiembroGremio(this);
	}
	
	public int getID() {
		return _id;
	}
	
	public int getRango() {
		if (_derechos == 1)
			return 1;
		return _rango;
	}
	
	public Gremio getGremio() {
		return _gremio;
	}
	
	public String analizarDerechos() {
		return Integer.toString(_derechos, 36);
	}
	
	public int getDerechos() {
		return _derechos;
	}
	
	public long getXpDonada() {
		return _xpDonada;
	}
	
	public int getPorcXpDonada() {
		return _porcXpDonada;
	}
	
	public int getGfx() {
		return _perso.getGfxID(false);
	}
	
	public int getNivel() {
		return _perso.getNivel();
	}
	
	public String getNombre() {
		return _perso.getNombre();
	}
	
	public String getUltimaConexion() {
		return _perso.getCuenta().getUltimaConexion();
	}
	
	public Personaje getPersonaje() {
		return _perso;
	}
	
	public int getHorasDeUltimaConeccion() {
		try {
			final String[] strFecha = getUltimaConexion().split("~");
			final LocalDateTime ultConeccion = new LocalDateTime(Integer.parseInt(strFecha[0]), Integer.parseInt(strFecha[1]),
			Integer.parseInt(strFecha[2]), Integer.parseInt(strFecha[3]), Integer.parseInt(strFecha[4]), Integer.parseInt(
			strFecha[5]));
			final LocalDateTime ahora = new LocalDateTime();
			return Hours.hoursBetween(ultConeccion, ahora).getHours();
		} catch (final Exception e) {
			return 0;
		}
	}
	
	public void setRango(final int rango) {
		_rango = rango;
	}
	
	public boolean puede(final int derecho) {
		if (_tieneDerecho.get(Constantes.G_TODOS_LOS_DERECHOS) || _rango == 1) {
			return true;
		}
		return _tieneDerecho.get(derecho);
	}
	
	public void darXpAGremio(final long xp) {
		_xpDonada += xp;
		_gremio.addExperiencia(xp, false);
	}
	
	public void setTodosDerechos(int rango, int porcXpdonar, int derechos) {
		if (rango != -1) {
			_rango = rango;
		}
		if (porcXpdonar != -1) {
			if (porcXpdonar < 0) {
				porcXpdonar = 0;
			}
			if (porcXpdonar > 90) {
				porcXpdonar = 90;
			}
			_porcXpDonada = porcXpdonar;
		}
		if (derechos != -1) {
			convertirDerechosAInt(derechos);
		}
	}
	
	private void convertirDerechosAInt(int derechos) {
		// derechosIniciales();
		int newDerechos = 0;
		for (int i = 0; i <= 14; i++) {
			int elevado = (int) Math.pow(2, i);
			boolean permiso = (derechos & elevado) == elevado;
			if (_rango == 1) {
				permiso = true;
				newDerechos = 1;
			} else {
				if (derechos == 1) {
					permiso = elevado != 1;
				}
				if (permiso) {
					newDerechos += elevado;
				}
			}
			_tieneDerecho.put(elevado, permiso);
		}
		_derechos = newDerechos;
	}
	// private void derechosIniciales() {
	// _tieneDerecho.put(Informacion.G_TODOS_LOS_DERECHOS, false);
	// _tieneDerecho.put(Informacion.G_MODIF_BOOST, false);
	// _tieneDerecho.put(Informacion.G_MODIF_DERECHOS, false);
	// _tieneDerecho.put(Informacion.G_INVITAR, false);
	// _tieneDerecho.put(Informacion.G_BANEAR, false);
	// _tieneDerecho.put(Informacion.G_TODAS_XP_DONADAS, false);
	// _tieneDerecho.put(Informacion.G_MODIF_RANGOS, false);
	// _tieneDerecho.put(Informacion.G_PONER_RECAUDADOR, false);
	// _tieneDerecho.put(Informacion.G_SU_XP_DONADA, false);
	// _tieneDerecho.put(Informacion.G_RECOLECTAR_RECAUDADOR, false);
	// _tieneDerecho.put(Informacion.G_USAR_CERCADOS, false);
	// _tieneDerecho.put(Informacion.G_MEJORAR_CERCADOS, false);
	// _tieneDerecho.put(Informacion.G_OTRAS_MONTURAS, false);
	// }
}
