package variables.montura;

import java.util.HashMap;
import java.util.Map;

public class MonturaModelo {
	private String _color = "";
	private int _colorID, _certificadoID;
	private byte _generacion;
	private Map<Integer, Integer> _stats = new HashMap<Integer, Integer>();
	
	public MonturaModelo(int idColor, String stats, String color, int idCertificado, byte generacion) {
		_colorID = idColor;
		_certificadoID = idCertificado;
		_color = color;
		_generacion = generacion;
		if (stats.isEmpty()) {
			return;
		}
		for (String str : stats.split(";")) {
			try {
				String[] s = str.split(",");
				_stats.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
			} catch (Exception e) {}
		}
	}
	
	public String getStrColor() {
		return _color;
	}
	
	public int getColorID() {
		return _colorID;
	}
	
	public int getCertificadoModeloID() {
		return _certificadoID;
	}
	
	public byte getGeneracionID() {
		return _generacion;
	}
	
	public Map<Integer, Integer> getStats() {
		return _stats;
	}
}
