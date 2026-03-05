package variables.mision;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import variables.personaje.Personaje;
import estaticos.Mundo;

public class Mision {
	public final static int ESTADO_COMPLETADO = 1, ESTADO_INCOMPLETO = 2, ESTADO_NO_TIENE = 0;
	private final int _id;
	private int _etapaID, _nivelEtapa = 0;
	private int _estadoMision = ESTADO_NO_TIENE;
	private final ConcurrentHashMap<Integer, Integer> _estadoObjetivos = new ConcurrentHashMap<>();
	
	public Mision(final int id, final int estado, final int etapaID, final int nivelEtapa, final String objetivos) {
		_id = id;
		_estadoMision = estado;
		if (!estaCompletada()) {
			_etapaID = etapaID;
			_nivelEtapa = nivelEtapa;
			setNuevosObjetivos();
			if (!objetivos.isEmpty()) {
				for (final String str : objetivos.split(";")) {
					try {
						if (str.isEmpty()) {
							continue;
						}
						final String[] duo = str.split(",");
						int objetivoID = Integer.parseInt(duo[0]);
						int estadoObj = Integer.parseInt(duo[1]);
						if (Mundo.getMisionObjetivoModelo(objetivoID) == null) {
							continue;
						}
						_estadoObjetivos.put(objetivoID, estadoObj);
					} catch (Exception e) {}
				}
			}
		}
	}
	
	public boolean estaCompletada() {
		return _estadoMision == ESTADO_COMPLETADO;
	}
	
	public boolean confirmarEtapaActual(Personaje perso, boolean preConfirma) {
		boolean p = false;
		try {
			Map<Integer, Integer> copia = new TreeMap<>();
			copia.putAll(_estadoObjetivos);
			for (Entry<Integer, Integer> e : copia.entrySet()) {
				if (e.getValue() == ESTADO_INCOMPLETO) {
					MisionObjetivoModelo mObjMod = Mundo.getMisionObjetivoModelo(e.getKey());
					perso.confirmarObjetivo(this, mObjMod, perso, null, preConfirma, 0);
				}
			}
			p = verificaSiCumplioEtapa();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public void setObjetivoCompletado(int objetivoID) {
		if (_estadoObjetivos.get(objetivoID) != null) {
			_estadoObjetivos.put(objetivoID, ESTADO_COMPLETADO);
		}
	}
	
	public ConcurrentHashMap<Integer, Integer> getObjetivos() {
		return _estadoObjetivos;
	}
	
	public int getIDModelo() {
		return _id;
	}
	
	public int getEtapaID() {
		return _etapaID;
	}
	
	public int getNivelEtapa() {
		return _nivelEtapa;
	}
	
	public int getEstadoMision() {
		return _estadoMision;
	}
	
	private void setNuevosObjetivos() {
		_estadoObjetivos.clear();
		MisionEtapaModelo etapa = Mundo.getEtapa(_etapaID);
		if (etapa == null) {
			return;
		}
		for (final MisionObjetivoModelo objMod : etapa.getObjetivosPorNivel(_nivelEtapa).values()) {
			_estadoObjetivos.put(objMod.getID(), ESTADO_INCOMPLETO);
		}
	}
	
	public boolean verificaSiCumplioEtapa() {
		boolean cumplioLosObjetivos = true;
		for (int estado : _estadoObjetivos.values()) {
			if (estado == ESTADO_INCOMPLETO) {
				cumplioLosObjetivos = false;
				break;
			}
		}
		return cumplioLosObjetivos && !verificaSiHayOtroNivelEtapa();
	}
	
	private boolean verificaSiHayOtroNivelEtapa() {
		MisionEtapaModelo etapa = Mundo.getEtapa(_etapaID);
		TreeMap<Integer, MisionObjetivoModelo> obj = etapa.getObjetivosPorNivel(_nivelEtapa + 1);
		if (obj == null) {
			return false;
		}
		_nivelEtapa++;
		setNuevosObjetivos();
		return true;
	}
	
	public boolean verificaFinalizoMision() {
		int sigEtapa = Mundo.getMision(_id).siguienteEtapa(_etapaID);
		_etapaID = sigEtapa;
		if (sigEtapa == -1) {
			_estadoMision = ESTADO_COMPLETADO;
			return true;
		}
		_nivelEtapa = 0;
		setNuevosObjetivos();
		return false;
	}
}