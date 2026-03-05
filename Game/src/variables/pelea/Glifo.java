package variables.pelea;

import java.util.ArrayList;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.hechizo.EfectoHechizo.TipoDaño;
import variables.mapa.Celda;
import estaticos.Constantes;
import estaticos.GestorSalida;

public class Glifo {
	private final boolean _inicioTurno;
	private final Luchador _lanzador;
	private final Celda _celda;
	private final byte _tamaño;
	private int _duracion;
	private final int _hechizoID, _color;
	private final StatHechizo _glifoSH;
	private final Pelea _pelea;
	private final ArrayList<Celda> _celdas;
	private final char _forma;
	
	public Glifo(final Pelea pelea, final Luchador lanzador, final Celda celda, final byte tamaño,
	final StatHechizo glifoHechizo, final int _duracion2, final int hechizoID, final boolean inicioTurno,
	final ArrayList<Celda> celdas, final int color, char forma) {
		_pelea = pelea;
		_lanzador = lanzador;
		_celda = celda;
		_hechizoID = hechizoID;
		_tamaño = tamaño;
		_glifoSH = glifoHechizo;
		_duracion = _duracion2;
		_color = color;
		_inicioTurno = inicioTurno;
		_pelea.addGlifo(this);
		_celdas = celdas;
		_forma = forma;
		for (Celda c : celdas) {
			c.addGlifo(this);
		}
		GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_EN_PELEA(pelea, 7, "+", celda.getID(), _tamaño, _color, _forma);
		// GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_EN_PELEA(pelea, 7, celda.getID(), "Haaaaaaaaa3005",
		// false);
	}
	
	public boolean esInicioTurno() {
		return _inicioTurno;
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public char getForma() {
		return _forma;
	}
	
	public byte getTamaño() {
		return _tamaño;
	}
	
	public int getColor() {
		return _color;
	}
	
	public Luchador getLanzador() {
		return _lanzador;
	}
	
	public int getDuracion() {
		return _duracion;
	}
	
	public int disminuirDuracion() {
		_duracion--;
		return _duracion;
	}
	
	public void activarGlifo(final Luchador glifeado) {
		if (_pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
			return;
		}
		GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, 307, glifeado.getID() + "", _hechizoID + "," + _celda.getID()
		+ ",0,1,1," + _lanzador.getID());
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		Hechizo.aplicaHechizoAPelea(_pelea, _lanzador, glifeado.getCeldaPelea(), _glifoSH.getEfectosNormales(),
		TipoDaño.GLIFO, false);
		// _pelea.acaboPelea((byte) 3);
	}
	
	public void desaparecer() {
		_pelea.borrarGlifo(this);
		for (Celda c : _celdas) {
			c.borrarGlifo(this);
		}
		GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_EN_PELEA(_pelea, 7, "-", _celda.getID(), _tamaño, _color, ' ');
		// GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_EN_PELEA(_pelea, 7, _celda.getID(),
		// "Haaaaaaaaa3005", false);
	}
}