package variables.zotros;

import estaticos.GestorSQL;
import estaticos.GestorSalida;
import variables.personaje.Personaje;

public class Titulo {
	private final int _id;
	private String _nombre;
	private int _creditos, _ogrinas, _kamas;
	private boolean _vender, _valido;
	
	public Titulo(int _id, String _nombre, int _creditos, int _ogrinas, int _kamas, boolean _vender, boolean _valido) {
		this._id = _id;
		this._nombre = _nombre;
		this._creditos = _creditos;
		this._ogrinas = _ogrinas;
		this._kamas = _kamas;
		this._vender = _vender;
		this._valido = _valido;
	}
	
	public boolean adquirirTitulo(Personaje _perso) {
		if (!_valido) {
			return false;
		}
		if (_perso.tieneTitulo(_id)) {
			return true;
		}
		if (_vender) {
			if (_creditos > 0) {
				if (!GestorSQL.RESTAR_CREDITOS(_perso.getCuenta(), _creditos, _perso)) {
					return false;
				}
			} else if (_ogrinas > 0) {
				if (!GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), _ogrinas, _perso)) {
					return false;
				}
			} else if (_kamas > 0) {
				if (_perso.getKamas() < _kamas) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + _kamas);
					return false;
				}
				_perso.addKamas(-_kamas, true, true);
			}
		}
		return true;
	}
	
	public String getPrecioStr() {
		if (_creditos > 0) {
			return "C" + _creditos;
		} else if (_ogrinas > 0) {
			return "O" + _ogrinas;
		}
		return "K" + _kamas;
	}
	
	public int getID() {
		return _id;
	}
	
	public String get_nombre() {
		return _nombre;
	}
	
	public void set_nombre(String _nombre) {
		this._nombre = _nombre;
	}
	
	public int get_creditos() {
		return _creditos;
	}
	
	public void set_creditos(int _creditos) {
		this._creditos = _creditos;
	}
	
	public int get_ogrinas() {
		return _ogrinas;
	}
	
	public void set_ogrinas(int _ogrinas) {
		this._ogrinas = _ogrinas;
	}
	
	public int get_kamas() {
		return _kamas;
	}
	
	public void set_kamas(int _kamas) {
		this._kamas = _kamas;
	}
	
	public boolean esParaVender() {
		return _vender;
	}
	
	public boolean esValido() {
		return _valido;
	}
}