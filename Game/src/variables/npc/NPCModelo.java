package variables.npc;

import java.util.ArrayList;
import variables.mision.Mision;
import variables.mision.MisionModelo;
import variables.mision.MisionPregunta;
import variables.objeto.ObjetoModelo;
import variables.personaje.Personaje;
import estaticos.Condiciones;
import estaticos.GestorSQL;
import estaticos.Mundo;

public class NPCModelo {
	private byte _sexo;
	private short _escalaX, _escalaY;
	private final int _id;
	private final int _foto;
	private int _gfxID, _color1, _color2, _color3;
	private int _arma, _sombrero, _capa, _mascota, _escudo;
	private int _preguntaID;
	private String _accesoriosHex;
	private final String _nombre;
	private String _listaObjetos = "";
	private final ArrayList<ObjetoModelo> _objVender = new ArrayList<ObjetoModelo>();
	private final ArrayList<MisionModelo> _misiones = new ArrayList<MisionModelo>();
	
	public NPCModelo(final int id, final int gfxID, final short escalaX, final short escalaY, final byte sexo,
	final int color1, final int color2, final int color3, final int foto, final int preguntaID, final String objVender,
	final String nombre, final int arma, final int sombrero, final int capa, final int mascota, final int escudo) {
		// super();
		_id = id;
		_gfxID = gfxID;
		_escalaX = escalaX;
		_escalaY = escalaY;
		_sexo = sexo;
		_color1 = color1;
		_color2 = color2;
		_color3 = color3;
		setAccesorios(arma, sombrero, capa, mascota, escudo);
		_foto = foto;
		_preguntaID = preguntaID;
		_nombre = nombre;
		if (!objVender.isEmpty()) {
			ObjetoModelo objModelo;
			for (final String obj : objVender.split(",")) {
				try {
					objModelo = Mundo.getObjetoModelo(Integer.parseInt(obj));
					if (objModelo == null) {
						continue;
					}
					_objVender.add(objModelo);
				} catch (final Exception e) {}
			}
			actualizarObjetosAVender();
		}
	}
	
	public void setAccesorios(int arma, int sombrero, int capa, int mascota, int escudo) {
		_arma = arma;
		_sombrero = sombrero;
		_capa = capa;
		_mascota = mascota;
		_escudo = escudo;
		_accesoriosHex = Integer.toHexString(arma) + "," + Integer.toHexString(sombrero) + "," + Integer.toHexString(capa)
		+ "," + Integer.toHexString(mascota) + "," + Integer.toHexString(escudo);
	}
	
	public String getAccesoriosInt() {
		return _arma + "," + _sombrero + "," + _capa + "," + _mascota + "," + _escudo;
	}
	
	public void addMision(final MisionModelo mision) {
		if (!_misiones.contains(mision)) {
			_misiones.add(mision);
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getGfxID() {
		return _gfxID;
	}
	
	public short getTallaX() {
		return _escalaX;
	}
	
	public short getTallaY() {
		return _escalaY;
	}
	
	public byte getSexo() {
		return _sexo;
	}
	
	public int getColor1() {
		return _color1;
	}
	
	public int getColor2() {
		return _color2;
	}
	
	public int getColor3() {
		return _color3;
	}
	
	public String getAccesoriosHex() {
		return _accesoriosHex;
	}
	
	public String getExtraClip(final Personaje perso) {
		if (perso == null) {
			return "";
		}
		for (final MisionModelo mision : _misiones) {
			if (perso.tieneMision(mision.getID())) {
				continue;
			}
			return "4";// signo de admiracion
		}
		return "";
	}
	
	public int getFoto() {
		return _foto;
	}
	
	public void setPreguntaID(final int pregunta) {
		_preguntaID = pregunta;
	}
	
	public int getPreguntaID(final Personaje perso) {
		if (perso != null) {
			int completado = -1, noTiene = -1, incompleto = -1;
			MisionPregunta preg;
			for (final MisionModelo misionMod : _misiones) {
				switch (perso.getEstadoMision(misionMod.getID())) {
					case Mision.ESTADO_COMPLETADO :
						if (misionMod.getPuedeRepetirse()) {
							perso.borrarMision(misionMod.getID());
						}
						preg = misionMod.getMisionPregunta(Mision.ESTADO_COMPLETADO);
						if (preg.getNPCID() != _id || preg.getPreguntaID() == 0) {
							continue;
						}
						if (Condiciones.validaCondiciones(perso, preg.getCondicion())) {
							completado = preg.getPreguntaID();
						}
						break;
					case Mision.ESTADO_INCOMPLETO :
						preg = misionMod.getMisionPregunta(Mision.ESTADO_INCOMPLETO);
						if (preg.getNPCID() != _id || preg.getPreguntaID() == 0) {
							continue;
						}
						if (Condiciones.validaCondiciones(perso, preg.getCondicion())) {
							incompleto = preg.getPreguntaID();
						}
						break;
					case Mision.ESTADO_NO_TIENE :
						preg = misionMod.getMisionPregunta(Mision.ESTADO_NO_TIENE);
						if (preg.getNPCID() != _id || preg.getPreguntaID() == 0) {
							continue;
						}
						if (Condiciones.validaCondiciones(perso, preg.getCondicion())) {
							noTiene = preg.getPreguntaID();
						}
						break;
				}
			}
			if (incompleto != -1) {
				return incompleto;
			}
			if (noTiene != -1) {
				return noTiene;
			}
			if (completado != -1) {
				return completado;
			}
		}
		return _preguntaID;
	}
	
	public void modificarNPC(final byte sexo, final short escalaX, final short escalaY, final int gfxID,
	final int color1, final int color2, final int color3) {
		_sexo = sexo;
		_escalaX = escalaX;
		_escalaY = escalaY;
		_gfxID = gfxID;
		_color1 = color1;
		_color2 = color2;
		_color3 = color3;
		// GestorSQL.ACTUALIZAR_NPC_COLOR_SEXO(this);
	}
	
	public void actualizarObjetosAVender() {
		if (_objVender.isEmpty()) {
			_listaObjetos = "";
		}
		final StringBuilder objetos = new StringBuilder();
		for (final ObjetoModelo obj : _objVender) {
			objetos.append(obj.stringDeStatsParaTienda() + "|");
		}
		_listaObjetos = objetos.toString();
	}
	
	public ArrayList<ObjetoModelo> getObjAVender() {
		return _objVender;
	}
	
	public String actualizarStringBD() {
		if (_objVender.isEmpty()) {
			return "";
		}
		final StringBuilder objetos = new StringBuilder();
		for (final ObjetoModelo obj : _objVender) {
			objetos.append(obj.getID() + ",");
		}
		return objetos.toString();
	}
	
	public String listaObjetosAVender() {
		return _listaObjetos;
	}
	
	public void addObjetoAVender(final ArrayList<ObjetoModelo> objetos) {
		boolean retorna = false;
		for (final ObjetoModelo obj : objetos) {
			if (_objVender.contains(obj)) {
				continue;
			}
			_objVender.add(obj);
			retorna = true;
		}
		if (!retorna) {
			return;
		}
		actualizarObjetosAVender();
		GestorSQL.ACTUALIZAR_NPC_VENTAS(this);
	}
	
	public void borrarObjetoAVender(final ArrayList<ObjetoModelo> objetos) {
		boolean retorna = false;
		for (final ObjetoModelo obj : objetos) {
			if (_objVender.remove(obj)) {
				retorna = true;
			}
		}
		if (!retorna) {
			return;
		}
		actualizarObjetosAVender();
		GestorSQL.ACTUALIZAR_NPC_VENTAS(this);
	}
	
	public void borrarTodosObjVender() {
		_objVender.clear();
		actualizarObjetosAVender();
		GestorSQL.ACTUALIZAR_NPC_VENTAS(this);
	}
	
	public boolean tieneObjeto(final int idModelo) {
		for (final ObjetoModelo OM : _objVender) {
			if (OM.getID() == idModelo) {
				return true;
			}
		}
		return false;
	}
}
