package variables.hechizo;

import variables.pelea.Luchador;

public class HechizoLanzado {
	final int _hechizoID;
	private final int _idObjetivo;
	private int _sigLanzamiento;
	
	public HechizoLanzado(final Luchador lanzador, final StatHechizo sHechizo, final int idObjetivo) {
		_idObjetivo = idObjetivo;
		_hechizoID = sHechizo.getHechizoID();
		if (lanzador.getPersonaje() != null && lanzador.getPersonaje().tieneModfiSetClase(_hechizoID)) {
			_sigLanzamiento = sHechizo.getSigLanzamiento() - lanzador.getPersonaje().getModifSetClase(_hechizoID, 286);
		} else {
			_sigLanzamiento = sHechizo.getSigLanzamiento();
		}
	}
	
	public int getObjetivo() {
		return _idObjetivo;
	}
	
	public void actuSigLanzamiento() {
		_sigLanzamiento -= 1;
	}
	
	public int getSigLanzamiento() {
		return _sigLanzamiento;
	}
	
	public int getHechizoID() {
		return _hechizoID;
	}
	
	public static int poderSigLanzamiento(final Luchador lanzador, final int idHechizo) {
		for (final HechizoLanzado HL : lanzador.getHechizosLanzados()) {
			if (HL._hechizoID == idHechizo && HL._sigLanzamiento > 0) {
				return HL._sigLanzamiento;
			}
		}
		return 0;
	}
	
	public static int getNroLanzamientos(final Luchador lanzador, final int idHechizo) {
		int nro = 0;
		for (final HechizoLanzado HL : lanzador.getHechizosLanzados()) {
			if (HL._hechizoID == idHechizo) {
				nro++;
			}
		}
		return nro;
	}
	
	public static int getNroLanzPorObjetivo(final Luchador lanzador, final int idObjetivo, final int idHechizo) {
		int nro = 0;
		if (idObjetivo != 0) {
			for (final HechizoLanzado HL : lanzador.getHechizosLanzados()) {
				if (HL._hechizoID == idHechizo && HL._idObjetivo == idObjetivo) {
					nro++;
				}
			}
		}
		return nro;
	}
	
	public static boolean puedeLanzPorObjetivo(final Luchador lanzador, final int idObjetivo, final StatHechizo SH) {
		if (SH.getMaxLanzPorObjetivo() <= 0) {
			return true;
		}
		int nro = 0;
		if (idObjetivo != 0) {
			for (final HechizoLanzado HL : lanzador.getHechizosLanzados()) {
				if (HL._hechizoID == SH.getHechizoID() && HL._idObjetivo == idObjetivo) {
					nro++;
				}
			}
		}
		return nro < SH.getMaxLanzPorObjetivo();// 0 < 1
	}
}