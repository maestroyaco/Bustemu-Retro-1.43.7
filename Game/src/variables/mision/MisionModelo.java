package variables.mision;

import java.util.ArrayList;
import estaticos.Mundo;

public class MisionModelo {
	private final int _id;
	private final boolean _puedeRepetirse;
	private final String _nombre;
	private final ArrayList<Integer> _etapas = new ArrayList<Integer>();
	private MisionPregunta[] _preguntas = new MisionPregunta[3];
	
	public MisionModelo(final int id, final String etapas, final String nombre, final String pregDarMision,
	final String pregMisCumplida, final String pregMisIncompleta, boolean puedeRepetirse) {
		_id = id;
		setEtapas(etapas);
		_nombre = nombre;
		setPreguntas(pregDarMision, Mision.ESTADO_NO_TIENE);
		setPreguntas(pregMisCumplida, Mision.ESTADO_COMPLETADO);
		setPreguntas(pregMisIncompleta, Mision.ESTADO_INCOMPLETO);
		_puedeRepetirse = puedeRepetirse;
	}
	
	public boolean getPuedeRepetirse() {
		return _puedeRepetirse;
	}
	
	public void setPreguntas(String pregunta, int estado) {
		try {
			String[] s = pregunta.split(";");
			int npc = 0, pregID = 0;
			String condicion = "";
			try {
				npc = Integer.parseInt(s[0]);
			} catch (Exception e) {}
			try {
				pregID = Integer.parseInt(s[1]);
			} catch (Exception e) {}
			try {
				condicion = s[2];
			} catch (Exception e) {}
			_preguntas[estado] = new MisionPregunta(pregID, npc, condicion);
			if (npc > 0) {
				Mundo.getNPCModelo(npc).addMision(this);
			}
		} catch (final Exception e) {}
	}
	
	public MisionPregunta getMisionPregunta(int estado) {
		return _preguntas[estado];
	}
	
	public String strMisionPregunta(int estado) {
		MisionPregunta preg = _preguntas[estado];
		if (preg == null) {
			return "null";
		}
		String str = preg.getNPCID() + ";" + preg.getPreguntaID();
		if (!preg.getCondicion().isEmpty()) {
			str += ";" + preg.getCondicion();
		}
		return str;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getID() {
		return _id;
	}
	
	public ArrayList<Integer> getEtapas() {
		return _etapas;
	}
	
	public String strEtapas() {
		String s = "";
		for (int i : _etapas) {
			if (!s.isEmpty()) {
				s += ",";
			}
			s += i;
		}
		return s;
	}
	
	public void setEtapas(final String etapas) {
		_etapas.clear();
		for (final String str : etapas.split(",")) {
			try {
				_etapas.add(Integer.parseInt(str));
			} catch (final Exception e) {}
		}
	}
	
	public int siguienteEtapa(final int id) {
		try {
			return _etapas.get(_etapas.indexOf(id) + 1);
		} catch (final Exception e) {
			return -1;
		}
	}
}
