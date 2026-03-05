package variables.npc;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import variables.personaje.Personaje;
import estaticos.Condiciones;
import estaticos.MainServidor;
import estaticos.Mundo;
import sprites.Preguntador;

public class PreguntaNPC {
	private final int _id;
	private String _args, _strAlternos;
	private final ArrayList<Integer> _respuestas = new ArrayList<>();
	private final Map<String, Integer> _pregCondicionales = new TreeMap<>();
	
	public PreguntaNPC(final int id, final String respuestas, final String args, final String alternos) {
		_id = id;
		_args = args;
		setRespuestas(respuestas);
		setPreguntasCondicionales(alternos);
	}
	
	public void setPreguntasCondicionales(String alternos) {
		_strAlternos = alternos;
		_pregCondicionales.clear();
		String[] alt = alternos.replaceAll("\\],\\[", "¬").replaceAll("[\\[\\]]", "").split("¬");
		for (String s : alt) {
			try {
				String[] split = s.split(";");
				_pregCondicionales.put(split[0], Integer.parseInt(split[1]));
			} catch (Exception e) {}
		}
	}
	
	public String getStrAlternos() {
		return _strAlternos;
	}
	
	public void setRespuestas(final String respuestas) {
		_respuestas.clear();
		for (String s : respuestas.replace(";", ",").split(",")) {
			try {
				_respuestas.add(Integer.parseInt(s));
			} catch (Exception e) {}
		}
	}
	
	public void setParams(final String respuestas) {
		_args = respuestas;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getStrRespuestas() {
		StringBuilder str = new StringBuilder();
		for (int i : _respuestas) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(i);
		}
		return str.toString();
	}
	
	public ArrayList<Integer> getRespuestas() {
		return _respuestas;
	}
	
	public String getParams() {
		return _args;
	}
	
	public String stringArgParaDialogo(final Personaje perso, Preguntador preguntador) {
		final StringBuilder str = new StringBuilder(_id + "");
		try {
			for (Entry<String, Integer> entry : _pregCondicionales.entrySet()) {
				if (entry.getValue() == _id || entry.getValue() <= 0) {
					continue;
				}
				if (Condiciones.validaCondiciones(perso, entry.getKey())) {
					if (Mundo.getPreguntaNPC(entry.getValue()) == null) {
						Mundo.addPreguntaNPC(new PreguntaNPC(entry.getValue(), "", "", ""));
					}
					return Mundo.getPreguntaNPC(entry.getValue()).stringArgParaDialogo(perso, preguntador);
				}
			}
			str.append(preguntador.getArgsDialogo(_args));
			boolean b = true;
			for (int i : _respuestas) {
				if (i <= 0) {
					continue;
				}
				RespuestaNPC respuesta = Mundo.getRespuestaNPC(i);
				if (respuesta == null) {
					respuesta = new RespuestaNPC(i);
					Mundo.addRespuestaNPC(respuesta);
				}
				String cond = respuesta.getCondicion();
				if (!Condiciones.validaCondiciones(perso, cond)) {
					continue;
				}
				if (b) {
					str.append("|");
				} else {
					str.append(";");
				}
				b = false;
				str.append(i);
			}
			perso.setPreguntaID(_id);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Hay un error en el NPC Pregunta " + _id);
		}
		return str.toString();
	}
}