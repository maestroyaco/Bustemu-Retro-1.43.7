package variables.npc;

import sprites.Exchanger;
import variables.objeto.Objeto;
import variables.personaje.Personaje;

public class NPC implements Exchanger {
	private final NPCModelo _npcModelo;
	private byte _orientacion;
	private short _celdaID;
	private final int _id;
	
	public NPC(final NPCModelo npcModelo, final int id, final short celda, final byte o) {
		_npcModelo = npcModelo;
		_id = id;
		_celdaID = celda;
		_orientacion = o;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getModeloID() {
		return _npcModelo.getID();
	}
	
	public NPCModelo getModelo() {
		return _npcModelo;
	}
	
	public int getPreguntaID(Personaje perso) {
		if (_npcModelo == null) {
			return 0;
		}
		return _npcModelo.getPreguntaID(perso);
	}
	
	public short getCeldaID() {
		return _celdaID;
	}
	
	public void setCeldaID(final short id) {
		_celdaID = id;
	}
	
	public void setOrientacion(final byte o) {
		_orientacion = o;
	}
	
	public byte getOrientacion() {
		return _orientacion;
	}
	
	public String strinGM(final Personaje perso) {
		final StringBuilder str = new StringBuilder();
		str.append(_celdaID + ";");
		str.append(_orientacion + ";");
		str.append("0" + ";");
		str.append(_id + ";");
		str.append(_npcModelo.getID() + ";");
		str.append("-4" + ";");// tipo = NPC
		str.append(_npcModelo.getGfxID() + "^" + _npcModelo.getTallaX() + "x" + _npcModelo.getTallaY() + ";");
		str.append(_npcModelo.getSexo() + ";");
		str.append((_npcModelo.getColor1() != -1 ? Integer.toHexString(_npcModelo.getColor1()) : "-1") + ";");
		str.append((_npcModelo.getColor2() != -1 ? Integer.toHexString(_npcModelo.getColor2()) : "-1") + ";");
		str.append((_npcModelo.getColor3() != -1 ? Integer.toHexString(_npcModelo.getColor3()) : "-1") + ";");
		str.append(_npcModelo.getAccesoriosHex() + ";");
		str.append(_npcModelo.getExtraClip(perso) + ";");
		str.append(_npcModelo.getFoto());
		return str.toString();
	}
	
	@Override
	public void addKamas(long k, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		return _npcModelo.listaObjetosAVender();
	}
}