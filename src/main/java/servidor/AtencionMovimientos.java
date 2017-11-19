package servidor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;

/**
 * Clase que administra un escucha de movimientos general de todos los
 * personajes. <br>
 * Ante cualquier movimiento de algún personaje, acutaliza su posición en el
 * mapa de los demás.
 * <p>
 * <i>En caso de ocurrir un error indica en el log un fallo de envío de
 * paqueteDeMovimientos. </i><br>
 */
public class AtencionMovimientos extends Thread {
	/**
	 * Gson. <br>
	 */
	private final Gson gson = new Gson();

	/**
	 * Crea un escucha de movimientos general de personajes general. <br>
	 */
	public AtencionMovimientos() {

	}

	/**
	 * Corre el escucha de movimientos general. <br>
	 */
	public final void run() {
		synchronized (this) {
			try {
				while (true) {
					// Espero a que se conecte alguien
					wait();
					// Le reenvio la conexion a todos
					for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
						if (conectado.getPaquetePersonaje().getEstado() == Estado.estadoJuego) {
							PaqueteDeMovimientos pdp = (PaqueteDeMovimientos) new PaqueteDeMovimientos(
									Servidor.getUbicacionPersonajes()).clone();
							pdp.setComando(Comando.MOVIMIENTO);
							
							ArrayList<PaqueteMovimiento> personajesInvisibles = new ArrayList<PaqueteMovimiento>();
							// Si el personaje a quien le voy a actualizar los movimientos no está en modo invisible, no permito que vea a los que si son invisibles
							if (conectado.getPaquetePersonaje().getTrucosActivados().indexOf(4) == -1) {
								Iterator<Map.Entry<Integer, PaqueteMovimiento>> it = pdp.getPersonajes().entrySet().iterator();
								while (it.hasNext()) {
								    Map.Entry<Integer, PaqueteMovimiento> entry = it.next();
								    if(Servidor.getPersonajesConectados().get(entry.getValue().getIdPersonaje()).getTrucosActivados().indexOf(4) != -1){
								    	personajesInvisibles.add(Servidor.getUbicacionPersonajes().get(entry.getValue().getIdPersonaje()));
								        // Elimino las ubicaciones de los personajes invisibles momentáneamente, para fingir que no están para los jugadores visibles
								    	it.remove();
								    }
								}
							}
							
							synchronized (conectado) {
								conectado.getSalida().writeObject(gson.toJson(pdp));
							}
							
							// Vuelvo a insertar las ubicaciones de los jugadores invisibles.
							for (PaqueteMovimiento personajeInvisible : personajesInvisibles) {
								Servidor.getUbicacionPersonajes().put(personajeInvisible.getIdPersonaje(), personajeInvisible);
							}
						}
					}
				}
			} catch (Exception e) {
				Servidor.log.append("Falló al intentar enviar paqueteDeMovimientos \n");
			}
		}
	}
}
