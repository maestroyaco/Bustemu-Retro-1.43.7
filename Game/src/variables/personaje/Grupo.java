package variables.personaje;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import estaticos.GestorSalida;
import variables.pelea.Pelea;

public class Grupo {
	private Personaje _rastrear;
	private final CopyOnWriteArrayList<Personaje> _integrantes = new CopyOnWriteArrayList<Personaje>();
	private final CopyOnWriteArrayList<Personaje> _alumnos = new CopyOnWriteArrayList<Personaje>();
	private String _packet;
	private boolean _autoUnir = true;
	
	public boolean esLiderGrupo(final Personaje perso) {
		if (_integrantes.isEmpty()) {
			return false;
		}
		return _integrantes.get(0).getID() == perso.getID();
	}
	
	public void activarMaestro(boolean slaveON, boolean soloVIP) {
		for (Personaje perso : _integrantes) {
			if (esLiderGrupo(perso)) {
				continue;
			}
			if (soloVIP && !perso.esAbonado()) {
				continue;
			}
			if (slaveON) {
				if (!_alumnos.contains(perso)) {
					_alumnos.add(perso);
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1SLAVE_ON");
				}
			} else {
				if (_alumnos.remove(perso)) {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1SLAVE_OFF");
				}
			}
		}
	}
	
	public void addIntegrante(final Personaje perso) {
		if (_integrantes.contains(perso)) {
			return;
		}
		_integrantes.add(perso);
		perso.setGrupo(this);
	}
	
	public boolean addAlumno(final Personaje perso) {
		if (!_integrantes.contains(perso)) {
			return false;
		}
		if (esLiderGrupo(perso)) {
			return false;
		}
		if (_alumnos.contains(perso)) {
			_alumnos.remove(perso);
			return false;
		} else {
			_alumnos.add(perso);
			return true;
		}
	}
	
	public ArrayList<Integer> getIDsPersos() {
		final ArrayList<Integer> lista = new ArrayList<Integer>();
		for (final Personaje perso : _integrantes) {
			lista.add(perso.getID());
			if (perso.getCompañero() != null && perso.getCompañero().esMultiman()) {
				lista.add(perso.getCompañero().getID());
			}
		}
		return lista;
	}
	
	public int getNivelGrupo() {
		int nivel = 0;
		for (final Personaje p : _integrantes) {
			nivel += p.getNivel();
		}
		return nivel;
	}
	
	public CopyOnWriteArrayList<Personaje> getMiembros() {
		return _integrantes;
	}
	
	public Personaje getLiderGrupo() {
		if (_integrantes.isEmpty()) {
			return null;
		}
		return _integrantes.get(0);
	}
	
	public Personaje getRastrear() {
		return _rastrear;
	}
	
	public void setRastrear(Personaje seguir) {
		_rastrear = seguir;
	}
	
	public void dejarGrupo(final Personaje expulsado, boolean expLider) {
		if (!_integrantes.contains(expulsado)) {
			return;
		}
		if (_rastrear == expulsado) {
			_rastrear = null;
			for (Personaje perso : _integrantes) {
				GestorSalida.ENVIAR_IC_BORRAR_BANDERA_COMPAS(perso);
				GestorSalida.ENVIAR_PF_SEGUIR_PERSONAJE(perso, "-");
			}
		}
		if (expulsado.enLinea()) {
			GestorSalida.ENVIAR_PV_DEJAR_GRUPO(expulsado, expLider ? getLiderGrupo().getID() + "" : "");
			GestorSalida.ENVIAR_IH_COORDENADAS_UBICACION(expulsado, "");
		}
		if (getLiderGrupo() == expulsado) {
			_alumnos.clear();
		}
		expulsado.setGrupo(null);
		_integrantes.remove(expulsado);
		_alumnos.remove(expulsado);
		if (_integrantes.size() == 1) {
			dejarGrupo(_integrantes.get(0), false);
		} else if (_integrantes.size() >= 2) {
			GestorSalida.ENVIAR_PM_EXPULSAR_PJ_GRUPO(this, expulsado.getID());
		}
	}
	
	private synchronized void ejecutarPacket(String packet) {
		if (getLiderGrupo() == null) {
			return;
		}
		for (Personaje p : _alumnos) {
			switch (packet.charAt(0)) {
				case 'G' :
					if (p.getMapa() != getLiderGrupo().getMapa()) {
						continue;
					}
					switch (packet.charAt(1)) {
						case 'A' :
							if (p.getCelda() != getLiderGrupo().getCelda()) {
								p.setCelda(getLiderGrupo().getCelda());
								GestorSalida.ENVIAR_GM_REFRESCAR_PJ_EN_MAPA_SIN_HUMO(p.getMapa(), p);
							}
							break;
						case 'K' :
							break;
					}
					break;
				case 'W' :// zaaps
				case 'w' :// zaapi
				case 'D' :// dialogos
					break;
			}
			try {
				p.getCuenta().getSocket().analizar_Packets(packet);
			} catch (Exception e) {}
		}
	}
	
	public boolean tieneAlumnos() {
		return !_alumnos.isEmpty();
	}
	
	public void setAutoUnir(boolean b) {
		_autoUnir = b;
	}
	
	public boolean getAutoUnir() {
		return _autoUnir;
	}
	
	private synchronized void teleport(short id, short cell) {
		if (getLiderGrupo() == null) {
			return;
		}
		for (Personaje p : _alumnos) {
			try {
				if (p.getID() == getLiderGrupo().getID()) {
					continue;
				}
				p.teleport(id, cell);
			} catch (Exception e) {}
		}
	}
	
	public synchronized void teleportATodos(short id, short cell) {
		if (_alumnos.isEmpty()) {
			return;
		}
		Thread thread = new Thread() {
			public void run() {
				teleport(id, cell);
			}
		};
		thread.start();
	}
	
	public synchronized void unirAPelea(Pelea pelea) {
		if (_alumnos.isEmpty() || !_autoUnir) {
			return;
		}
		_packet = "GA903" + pelea.getID() + ";" + getLiderGrupo().getID();
		Thread thread = new Thread() {
			String packet2 = _packet;
			
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {}
				ejecutarPacket(packet2);
			}
		};
		thread.start();
	}
	
	public synchronized void packetSeguirLider(String packet) {
		if (_alumnos.isEmpty()) {
			return;
		}
		_packet = packet;
		Thread thread = new Thread() {
			String packet2 = _packet;
			
			public void run() {
				ejecutarPacket(packet2);
			}
		};
		thread.start();
	}
}