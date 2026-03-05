package variables.hechizo;

import java.util.ArrayList;
import variables.mapa.Celda;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Buff extends EfectoHechizo {
	private final Luchador _lanzador;
	private int _turnoRestantes;
	private boolean _desbuffeable;
	private String _condicionBuff = "";
	// por el momento las condiciones son
	// SOIN, BN,DN,DE,DA,DW,DF,-PA,-PM,PA,PM
	private TipoDaño _tipo;
	
	public Buff(final int efectoID, final int hechizoID, final boolean desbufeable, final int turnos,
	final Luchador lanzador, final String args, final TipoDaño tipo) {
		super(hechizoID);
		_efectoID = efectoID;
		_desbuffeable = desbufeable;
		_turnoRestantes = turnos <= -1 ? -3 : turnos;
		_duracion = 0;
		_lanzador = lanzador;
		if (tipo == TipoDaño.GLIFO || tipo == TipoDaño.TRAMPA) {
			_tipo = tipo;
		} else {
			_tipo = TipoDaño.POST_TURNOS;
		}
		setArgs(args);
	}
	
	@Override
	public void setArgs(String args) {
		_args = args;
		final String[] split = _args.split(",");
		try {
			_primerValor = Integer.parseInt(split[0]);// valor
		} catch (final Exception e) {}
		try {
			_segundoValor = Integer.parseInt(split[1]);// valor max
		} catch (final Exception e) {}
		try {
			_tercerValor = Integer.parseInt(split[2]);
		} catch (final Exception e) {}
	}
	
	public void setCondBuff(String condicion) {
		_condicionBuff = condicion.toUpperCase();
	}
	
	public String getCondicionBuff() {
		return _condicionBuff;
	}
	
	public int getTurnosRestantes(boolean puedeJugar) {
		if (!puedeJugar || _turnoRestantes <= -1) {
			return _turnoRestantes;
		}
		return _turnoRestantes - 1;
	}
	
	public int getTurnosRestantesOriginal() {
		return _turnoRestantes;
	}
	
	public int disminuirTurnosRestantes() {
		if (_turnoRestantes > 0) {
			_turnoRestantes--;
		}
		return _turnoRestantes;
	}
	
	public boolean esDesbufeable() {
		return _desbuffeable;
	}
	
	public void setDesbufeable(boolean b) {
		_desbuffeable = b;
	}
	
	public Luchador getLanzador() {
		return _lanzador;
	}
	
	public void aplicarBuffDeInicioTurno(final Pelea pelea, Luchador objetivo) {
		try {
			ArrayList<Luchador> obj2 = new ArrayList<Luchador>();
			obj2.add(objetivo);
			switch (_efectoID) {
				case 85 :// Daños Agua %vida del atacante
				case 86 :// Daños Tierra %vida del atacante
				case 87 :// Daños Aire %vida del atacante
				case 88 :// Daños Fuego %vida del atacante
				case 89 :// Daños Neutral %vida del atacante
					efecto_Daños_Porc_Elemental(obj2, pelea, _lanzador, _tipo, false);
					return;
				case 91 :// Robar Vida(agua)
				case 92 :// Robar Vida(tierra)
				case 93 :// Robar Vida(aire)
				case 94 :// Robar Vida(fuego)
				case 95 :// Robar Vida(neutral)
					efecto_Roba_PDV_Elemental(obj2, pelea, _lanzador, _tipo, false);
					return;
				case 96 :// Daños Agua
				case 97 :// Daños Tierra
				case 98 :// Daños Aire
				case 99 :// Daños Fuego
				case 100 :// Daños Neutral
					efecto_Daños_Elemental(obj2, pelea, _lanzador, _tipo, false);
					return;
				case 81 :
				case 108 :// Cura, PDV devueltos
					efecto_Cura(obj2, pelea, _lanzador, _tipo);
					return;
				case 301 :
					aplicarHechizoDeBuff(pelea, objetivo, objetivo.getCeldaPelea());
					return;
				case 787 :// activa un hechizo despues de varios turnos
					aplicarHechizoDeBuff(pelea, objetivo, objetivo.getCeldaPelea());
					return;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION BUFF INICIO, HECHIZO:" + _hechizoID + ", ARGS:" + _args);
			e.printStackTrace();
		}
	}
	
	public void aplicarBuffCondicional(Luchador objetivo) {
		ArrayList<Luchador> objetivos = new ArrayList<Luchador>();
		objetivos.add(objetivo);
		String c = _condicionBuff;
		_condicionBuff = "";
		aplicarEfecto(objetivo.getPelea(), objetivo, objetivos, objetivo.getCeldaPelea(), _tipo, false);
		_condicionBuff = c;
	}
	
	public void aplicarHechizoDeBuff(Pelea pelea, Luchador objetivo, Celda celdaObjetivo) {
		final Hechizo hechizo = Mundo.getHechizo(_primerValor);
		if (hechizo == null) {
			return;
		}
		StatHechizo sh = hechizo.getStatsPorNivel(_segundoValor);
		if (sh == null) {
			return;
		}
		Hechizo.aplicaHechizoAPelea(pelea, objetivo, celdaObjetivo, sh.getEfectosNormales(), TipoDaño.NORMAL, false);
	}
}