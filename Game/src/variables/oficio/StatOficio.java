package variables.oficio;

import java.util.ArrayList;
import variables.mapa.Celda;
import variables.mapa.interactivo.ObjetoInteractivo;
import variables.mob.GrupoMob;
import variables.mob.MobModelo.TipoGrupo;
import variables.personaje.Personaje;
import variables.zotros.Almanax;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class StatOficio {
	private final byte _posicion;
	private byte _slotsPublico;
	private int _nivel, _adicional, _experiencia;
	private ArrayList<Trabajo> _trabajosPoderRealizar = new ArrayList<Trabajo>();
	private boolean _esPagable, _gratisSiFalla, _noProporcRecurso, _libroArtesano;
	private final Oficio _oficio;
	private Trabajo _tempTrabajo;
	
	public StatOficio(final byte posicion, final Oficio oficio, final int exp) {
		_posicion = posicion;
		_oficio = oficio;
		addExperiencia(null, exp, 0);
		if (_trabajosPoderRealizar.isEmpty()) {
			_trabajosPoderRealizar = Constantes.getTrabajosPorOficios(oficio.getID(), _nivel, this);
			_trabajosPoderRealizar.trimToSize();
		}
		_slotsPublico = Constantes.getIngMaxPorNivel(_nivel);
		_libroArtesano = false;
	}
	
	public ArrayList<Trabajo> trabajosARealizar() {
		return _trabajosPoderRealizar;
	}
	
	public int getAdicional() {
		return _adicional;
	}
	
	public boolean getLibroArtesano() {
		return _libroArtesano || !MainServidor.MODO_HEROICO; // FIXME solo para q se vean todos
	}
	
	public void setLibroArtesano(final boolean bo) {
		_libroArtesano = bo;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public boolean esPagable() {
		return _esPagable;
	}
	
	public boolean esGratisSiFalla() {
		return _gratisSiFalla;
	}
	
	public boolean noProveerRecuerso() {
		return _noProporcRecurso;
	}
	
	public void setSlotsPublico(final byte slots) {
		_slotsPublico = slots;
	}
	
	public byte getSlotsPublico() {
		return _slotsPublico;
	}
	
	public void subirNivel() {
		if (_posicion == 7) {
			return;
		}
		_nivel++;
		_trabajosPoderRealizar = Constantes.getTrabajosPorOficios(_oficio.getID(), _nivel, this);
		_trabajosPoderRealizar.trimToSize();
		_adicional = (int) Math.sqrt(_nivel / 2);
	}
	
	public String stringSKillsOficio() {
		final StringBuilder str = new StringBuilder();
		for (final Trabajo trabajo : _trabajosPoderRealizar) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(trabajo.getTrabajoID() + "~");
			if (trabajo.esCraft()) {
				str.append(trabajo.getCasillasMax() + "~" + "0~0~" + trabajo.getSuerte());
			} else {
				str.append(trabajo.getCasillasMin() + "~" + trabajo.getCasillasMax() + "~0~" + trabajo.getTiempo());
			}
		}
		return "|" + _oficio.getID() + ";" + str.toString();
	}
	
	public long getExp() {
		return _experiencia;
	}
	
	public synchronized boolean iniciarTrabajo(final int trabajoID, final Personaje perso, final ObjetoInteractivo OI,
	final int idUnica, final Celda celda) {
		for (final Trabajo trabajo : _trabajosPoderRealizar) {
			if (trabajo.getTrabajoID() == trabajoID) {
				_tempTrabajo = trabajo;
				// perso.setTrabajo(_tempTrabajo);
				return trabajo.iniciarTrabajo(perso, idUnica, celda);
			}
		}
		// no puede realizara el trabajo
		return false;
	}
	
	public synchronized boolean finalizarTrabajo(final Personaje perso) {
		if (_tempTrabajo == null) {
			return false;
		}
		boolean r = true;
		if (!_tempTrabajo.esCraft()) {
			// recolecta
			r = finalizarRecoleccion(perso);
		}
		if (r) {
			_tempTrabajo = null;
		}
		return r;
		// perso.setTrabajo(_tempTrabajo);
	}
	
	private synchronized boolean finalizarRecoleccion(final Personaje perso) {
		if (_tempTrabajo == null) {
			return false;
		}
		if (!_tempTrabajo.puedeFinalizarRecolecta()) {
			return false;
		}
		final int protector = Constantes.getProtectorRecursos(_tempTrabajo.getTrabajoID(), _oficio.getID());
		final boolean bProtector = (Formulas.getRandomInt(1, 100) < MainServidor.PROBABILIDAD_PROTECTOR_RECURSOS)
		&& protector != 0 && _nivel >= 20;
		final int experiencia = _tempTrabajo.getExpFinalizarRecoleccion();
		addExperiencia(perso, experiencia, 2);
		if (bProtector && perso.enLinea()) {
			final int nivel = Constantes.getNivelProtector(_nivel);
			final GrupoMob grupoMob = new GrupoMob(perso.getMapa(), perso.getCelda().getID(), protector + "," + nivel + ","
			+ nivel, TipoGrupo.SOLO_UNA_PELEA, "");
			perso.getMapa().iniciarPelea(perso, null, perso.getCelda().getID(), (short) -1,
			Constantes.PELEA_TIPO_PVM_NO_ESPADA, grupoMob);
		} else {
			_tempTrabajo.recogerRecolecta();
		}
		return true;
	}
	
	public void addExperiencia(final Personaje perso, int exp, final int tipo) {
		if (_posicion == 7 || _nivel >= MainServidor.NIVEL_MAX_OFICIO) {
			return;
		}
		switch (tipo) {
			case Constantes.OFICIO_EXP_TIPO_RECOLECCION :// recoleccion
				if (perso.realizoMisionDelDia()) {
					Almanax almanax = Mundo.getAlmanaxDelDia();
					if (almanax != null && almanax.getTipo() == Constantes.ALMANAX_BONUS_EXP_OFICIO_RECOLECCION) {
						exp += exp * almanax.getBonus() / 100;
					}
				}
				break;
			case Constantes.OFICIO_EXP_TIPO_CRAFT :// craft
				if (perso.realizoMisionDelDia()) {
					Almanax almanax = Mundo.getAlmanaxDelDia();
					if (almanax != null && almanax.getTipo() == Constantes.ALMANAX_BONUS_EXP_OFICIO_CRAFT) {
						exp += exp * almanax.getBonus() / 100;
					}
				}
				break;
		}
		final int exNivel = _nivel;
		_experiencia += exp;
		// _exp = Math.min(_exp + exp, Mundo.getExpOficio()(Bustemu.NIVEL_MAX_OFICIO)._oficio);
		while (_experiencia >= Mundo.getExpOficio(_nivel + 1) && _nivel < MainServidor.NIVEL_MAX_OFICIO) {
			subirNivel();
		}
		if (perso != null && perso.enLinea()) {
			if (_nivel > exNivel) {
				GestorSalida.ENVIAR_JS_SKILL_DE_OFICIO(perso, this);
				GestorSalida.ENVIAR_JN_OFICIO_NIVEL(perso, _oficio.getID(), _nivel);
				GestorSalida.ENVIAR_JO_OFICIO_OPCIONES(perso, this);
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(perso);
				GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
			}
			GestorSalida.ENVIAR_JX_EXPERINENCIA_OFICIO(perso, this);
		}
	}
	
	public String getExpString(final String s) {
		return Mundo.getExpOficio(_nivel) + s + _experiencia + s + Mundo.getExpOficio(_nivel + 1);
	}
	
	public Oficio getOficio() {
		return _oficio;
	}
	
	public boolean esValidoTrabajo(final int id) {
		for (final Trabajo AT : _trabajosPoderRealizar) {
			if (AT.getTrabajoID() == id) {
				return true;
			}
		}
		return false;
	}
	
	public int getOpcionBin() {
		int nro = 0;
		nro += _noProporcRecurso ? 4 : 0;
		nro += _gratisSiFalla ? 2 : 0;
		nro += _esPagable ? 1 : 0;
		return nro;
	}
	
	public void setOpciones(final int bin) {
		_noProporcRecurso = (bin & 4) == 4;
		_gratisSiFalla = (bin & 2) == 2;
		_esPagable = (bin & 1) == 1;
	}
	
	public byte getPosicion() {
		return _posicion;
	}
}
