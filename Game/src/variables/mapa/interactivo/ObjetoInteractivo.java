package variables.mapa.interactivo;

import variables.mapa.Celda;
import variables.mapa.Mapa;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class ObjetoInteractivo {
	private boolean _esInteractivo;
	private byte _estado = Constantes.OI_ESTADO_LLENO;
	private int _bonusEstrellas = -1;
	private int _gfxID, _milisegundosRecarga;
	private long _tiempoProxRecarga = -1, _tiempoProxSubidaEstrella = -1, _tiempoFinalizarRecolecta = 0;
	private Mapa _mapa;
	private Celda _celda;
	private ObjetoInteractivoModelo _objInterModelo;
	
	public ObjetoInteractivo(final Mapa mapa, final Celda celda, final int id) {
		_gfxID = id;
		_mapa = mapa;
		_celda = celda;
		_estado = Constantes.OI_ESTADO_LLENO;
		_objInterModelo = Mundo.getObjIntModeloPorGfx(_gfxID);
		_esInteractivo = true;
		if (_objInterModelo != null) {
			_milisegundosRecarga = _objInterModelo.getTiempoRecarga(); // milis
			if (_objInterModelo.getTipo() == 1) {
				// solo recursos para recoger
				mapa.getObjetosInteractivos().add(this);
				if (MainServidor.PARAM_ESTRELLAS_RECURSOS) {
					_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_RECURSOS;
					restartSubirEstrellas();
				}
			}
		}
	}
	
	public boolean puedeFinalizarRecolecta() {
		boolean b = System.currentTimeMillis() >= _tiempoFinalizarRecolecta;
		if (b) {
			_tiempoFinalizarRecolecta = 0;
		}
		return b;
	}
	
	public void setTiempoInicioRecolecta(long t) {
		_tiempoFinalizarRecolecta = t;
	}
	
	public int getTipoObjInteractivo() {
		if (_objInterModelo == null) {
			return -1;
		}
		return _objInterModelo.getTipo();
	}
	
	public String getInfoPacket() {
		return _estado + ";" + (_esInteractivo ? "1" : "0") + ";" + _bonusEstrellas;
	}
	
	public int getBonusEstrellas() {
		setBonusEstrellas(_bonusEstrellas);
		return Math.max(0, _bonusEstrellas);
	}
	
	public int realBonusEstrellas() {
		return _bonusEstrellas;
	}
	
	public void subirBonusEstrellas(int cant) {
		if (!MainServidor.PARAM_ESTRELLAS_RECURSOS) {
			return;
		}
		setBonusEstrellas(_bonusEstrellas + cant);
	}
	
	public void setBonusEstrellas(final int estrellas) {
		_bonusEstrellas = estrellas;
		if (_bonusEstrellas < MainServidor.INICIO_BONUS_ESTRELLAS_RECURSOS) {
			_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_RECURSOS;
		}
		if (_bonusEstrellas > MainServidor.MAX_BONUS_ESTRELLAS_RECURSOS) {
			if (MainServidor.PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX) {
				_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_RECURSOS;
			} else {
				_bonusEstrellas = MainServidor.MAX_BONUS_ESTRELLAS_RECURSOS;
			}
		}
	}
	
	public void subirEstrella() {
		if (!MainServidor.PARAM_ESTRELLAS_RECURSOS || _tiempoProxSubidaEstrella <= 0
		|| MainServidor.SEGUNDOS_ESTRELLAS_RECURSOS <= 0) {
			return;
		}
		if (System.currentTimeMillis() - _tiempoProxSubidaEstrella >= 0) {
			subirBonusEstrellas(20);
			restartSubirEstrellas();
		}
	}
	
	private void restartSubirEstrellas() {
		if (!MainServidor.PARAM_ESTRELLAS_RECURSOS) {
			return;
		}
		if (getTipoObjInteractivo() == 1) {// recursos para recoger
			_tiempoProxSubidaEstrella = System.currentTimeMillis() + (MainServidor.SEGUNDOS_ESTRELLAS_RECURSOS * 1000);
		}
	}
	
	public int getGfxID() {
		return _gfxID;
	}
	
	public ObjetoInteractivoModelo getObjIntModelo() {
		return _objInterModelo;
	}
	
	// public boolean esInteractivo() {
	// return _esInteractivo;
	// }
	// private void setInteractivo(final boolean b) {
	// _esInteractivo = b;
	// }
	public int getDuracion() {
		int duracion = 1500;
		if (_objInterModelo != null) {
			duracion = _objInterModelo.getDuracion();
		}
		return duracion;
	}
	
	public int getAnimacionPJ() {
		int animacionID = 4;
		if (_objInterModelo != null) {
			animacionID = _objInterModelo.getAnimacionPJ();
		}
		return animacionID;
	}
	
	public byte getEstado() {
		return _estado;
	}
	
	public synchronized void iniciarRecolecta(long t) {
		if (_milisegundosRecarga <= 0) {
			return;
		}
		_tiempoFinalizarRecolecta = System.currentTimeMillis() + t;
		_esInteractivo = false;
	}
	
	public void forzarActivarRecarga(int milis) {
		if (milis <= 0) {
			return;
		}
		_milisegundosRecarga = milis;
		activandoRecarga(Constantes.OI_ESTADO_LLENANDO, Constantes.OI_ESTADO_LLENO);
	}
	
	public boolean puedeIniciarRecolecta() {
		// if (!_esInteractivo) {
		// return false;
		// }
		if (_tiempoFinalizarRecolecta > 0) {
			return false;
		}
		if (_milisegundosRecarga <= 0) {
			return true;
		}
		if (_tiempoProxRecarga <= 0) {
			return true;
		}
		return (System.currentTimeMillis() - _tiempoProxRecarga >= 0);
	}
	
	public void activandoRecarga(byte vaciando, byte vacio) {
		if (_milisegundosRecarga <= 0) {
			return;
		}
		_esInteractivo = false;
		_estado = vaciando;
		GestorSalida.ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(_mapa, _celda);
		_estado = vacio;
		_tiempoProxRecarga = System.currentTimeMillis() + _milisegundosRecarga;
		_tiempoProxSubidaEstrella = -1;
	}
	
	public void recargando(final boolean forzado) {
		if (_tiempoProxRecarga <= 0) {
			return;
		}
		if (forzado || (System.currentTimeMillis() - _tiempoProxRecarga >= 0)) {
			_esInteractivo = true;
			_estado = Constantes.OI_ESTADO_LLENANDO;
			if (MainServidor.PARAM_ESTRELLAS_RECURSOS && getTipoObjInteractivo() == 1) {
				_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_RECURSOS;
				restartSubirEstrellas();
			}
			if (!forzado) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						GestorSalida.ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(_mapa, _celda);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {}
						_estado = Constantes.OI_ESTADO_LLENO;
						_tiempoProxRecarga = -1;
					}
				});
				t.setDaemon(true);
				t.start();
			} else {
				_estado = Constantes.OI_ESTADO_LLENO;
				_tiempoProxRecarga = -1;
			}
		}
	}
}