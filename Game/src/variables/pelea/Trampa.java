package variables.pelea;

import java.util.ArrayList;
import variables.hechizo.EfectoHechizo;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.hechizo.EfectoHechizo.TipoDaño;
import variables.mapa.Celda;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.GestorSalida;

public class Trampa implements Comparable<Trampa> {
	private final Luchador _lanzador;
	private final Celda _celda;
	private final byte _tamaño;
	private byte _paramEquipoDueño = -1;
	private final int _hechizoID, _color;
	private final StatHechizo _trampaSH;
	private final Pelea _pelea;
	private ArrayList<Integer> _visibles = new ArrayList<Integer>();
	private final ArrayList<Celda> _celdas;
	
	// private ArrayList<Luchador> _objetivos;
	public Trampa(final Pelea pelea, final Luchador lanzador, final Celda celda, final byte tamaño,
	final StatHechizo trampaHechizo, final int hechizoID, final ArrayList<Luchador> mostrar,
	final ArrayList<Celda> celdas, final int color) {
		_pelea = pelea;
		_lanzador = lanzador;
		_celda = celda;
		_tamaño = tamaño;
		_trampaSH = trampaHechizo;
		_hechizoID = hechizoID;
		_color = color;
		_paramEquipoDueño = lanzador.getParamEquipoAliado();
		_pelea.addTrampa(this);
		_celdas = celdas;
		for (Celda c : celdas) {
			c.addTrampa(this);
		}
		aparecer(mostrar);
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public int getParamEquipoDueño() {
		return _paramEquipoDueño;
	}
	
	public byte getTamaño() {
		return _tamaño;
	}
	
	public Luchador getLanzador() {
		return _lanzador;
	}
	
	// public void setObjetivos(ArrayList<Luchador> obj) {
	// _objetivos = obj;
	// }
	//
	// public ArrayList<Luchador> getObjetivos() {
	// return _objetivos;
	// }
	//
	public boolean esInvisiblePara(final int idMirador) {
		if (idMirador != 0) {
			if (_visibles.contains(idMirador)) {
				return false;
			}
		}
		return true;
	}
	
	public int getColor() {
		return _color;
	}
	
	public void activarTrampa(final Luchador victima) {
		if (_pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
			return;
		}
		_pelea.borrarTrampa(this);
		for (Celda c : _celdas) {
			c.borrarTrampa(this);
		}
		desaparecer();
		if (victima == null) {
			return;
		}
		GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, 307, victima.getID() + "", _hechizoID + "," + _celda.getID()
		+ ",0,1,1," + _lanzador.getID());
		// try {
		// Thread.sleep(100);
		// } catch (Exception e) {}
		if (!victima.estaMuerto()) {
			Hechizo.aplicaHechizoAPelea(_pelea, _lanzador, _celda, _trampaSH.getEfectosNormales(), TipoDaño.TRAMPA, false);
			if (_pelea.getLuchadorTurno().getIA() != null) {
				_pelea.getLuchadorTurno().getIA().nullear();
			}
		}
	}
	
	public void aparecer(final ArrayList<Luchador> luchadores) {
		for (Luchador luchador : luchadores) {
			if (!_visibles.contains(luchador.getID())) {
				_visibles.add(luchador.getID());
			}
		}
		GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADORES(luchadores, "+", _celda.getID(), _tamaño, _color, ' ');
		boolean[] permisos = new boolean[16];
		int[] valores = new int[16];
		permisos[2] = true;
		permisos[0] = true;
		valores[2] = 25;
		valores[0] = 1;
		GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_A_LUCHADORES(luchadores, _celda.getID(), Encriptador.stringParaGDC(
		permisos, valores), false);
	}
	
	public void desaparecer() {
		ArrayList<Luchador> luchadores = new ArrayList<>();
		for (int i : _visibles) {
			luchadores.add(_pelea.getLuchadorPorID(i));
		}
		GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADORES(luchadores, "-", _celda.getID(), _tamaño, _color, ' ');
		boolean[] permisos = new boolean[16];
		int[] valores = new int[16];
		permisos[2] = true;
		permisos[0] = true;
		GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_A_LUCHADORES(luchadores, _celda.getID(), Encriptador.stringParaGDC(
		permisos, valores), false);
	}
	
	private int getPrioridad() {
		int p = 0;
		for (EfectoHechizo eh : _trampaSH.getEfectosNormales()) {
			switch (eh.getEfectoID()) {
				case 5 :
					p = 10;
					break;
			}
		}
		return p;
	}
	
	@Override
	public int compareTo(Trampa o) {
		return new Integer(getPrioridad()).compareTo(new Integer(o.getPrioridad()));
	}
}