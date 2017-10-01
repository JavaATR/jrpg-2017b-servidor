package servidor;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeMovimientos;

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
	public void run() {
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
							synchronized (conectado) {
								conectado.getSalida().writeObject(gson.toJson(pdp));
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