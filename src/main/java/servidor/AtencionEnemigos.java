package servidor;

import com.google.gson.Gson;

import mensajeria.Comando;
import mensajeria.PaqueteDeEnemigos;
import mensajeria.PaqueteDeUbicacionEnemigos;

/**
 * Clase que esta atenta al estado de los enemigos. <br>
 * Una vez que se derrota un enemigo, se actualiza la lista de enemigos con los
 * demás clientes.
 * <p>
 * <i>En caso de ocurrir un error indica en el log un fallo de envío de
 * paqueteDeEnemigos. </i><br>
 */
public class AtencionEnemigos extends Thread {
	/**
	 * Gson. <br>
	 */
	private final Gson gson = new Gson();

	/**
	 * Crea un escucha de nuevas conexiones. <br>
	 */
	public AtencionEnemigos() {

	}

	/**
	 * Ejecuta la escucha de nuevas conexiones. <br>
	 */
	public final void run() {
		synchronized (this) {
			try {
				while (true) {
					wait();
					for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
						PaqueteDeEnemigos pde = (PaqueteDeEnemigos) new PaqueteDeEnemigos(
								Servidor.getEnemigosConectados()).clone();
						pde.setComando(Comando.CONEXIONENEMIGOS);

						PaqueteDeUbicacionEnemigos pdue = (PaqueteDeUbicacionEnemigos) new PaqueteDeUbicacionEnemigos(
								Servidor.getUbicacionEnemigos()).clone();
						pdue.setComando(Comando.UBICACIONENEMIGOS);
						synchronized (conectado) {
							conectado.getSalida().writeObject(gson.toJson(pde));
							conectado.getSalida().writeObject(gson.toJson(pdue));
						}
					}
				}
			} catch (Exception e) {
				Servidor.log.append("Falló al intentar enviar paqueteDeEnemigos\n");
			}
		}
	}
}
