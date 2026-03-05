package variables.personaje;

import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;

public class HechizoPersonaje {
	private char _posicion;
	private final Hechizo _hechizo;
	private int _nivel;
	
	public HechizoPersonaje(char _posicion, Hechizo _hechizo, int _nivel) {
		this._posicion = _posicion;
		this._hechizo = _hechizo;
		this._nivel = _nivel;
	}
	
	public Hechizo getHechizo() {
		return _hechizo;
	}
	
	public StatHechizo getStatHechizo() {
		if (_hechizo == null) {
			return null;
		}
		return _hechizo.getStatsPorNivel(_nivel);
	}
	
	public void setPosicion(char posicion) {
		_posicion = posicion;
	}
	
	public char getPosicion() {
		return _posicion;
	}
	
	public void setNivel(int nivel) {
		_nivel = nivel;
	}
	
	public int getNivel() {
		return _nivel;
	}
}
