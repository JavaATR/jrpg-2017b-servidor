package servidor;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeEnemigos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteDeUbicacionEnemigos;

/**
 * Clase que esta atenta a la conexión de los clientes. <br>
 * Una vez que se conecta un nuevo cliente, se actualiza su conexión con los
 * demás clientes.
 * <p>
 * <i>En caso de ocurrir un error indica en el log un fallo de envío de
 * paqueteDePersonajes. </i><br>
 */
public class AtencionConexiones extends Thread {
	/**
	 * Gson. <br>
	 */
	private final Gson gson = new Gson();

	/**
	 * Crea un escucha de nuevas conexiones. <br>
	 */
	public AtencionConexiones() {

	}

	/**
	 * Ejecuta la escucha de nuevas conexiones. <br>
	 */
	public void run() {
		synchronized (this) {
			try {
				while (true) {
					// Espero a que se conecte alguien
					wait();
					// Le reenvio la conexion a todos
					for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
						if (conectado.getPaquetePersonaje().getEstado() != Estado.estadoOffline) {
							PaqueteDePersonajes pdp = (PaqueteDePersonajes) new PaqueteDePersonajes(
									Servidor.getPersonajesConectados()).clone();
							pdp.setComando(Comando.CONEXION);
							
							PaqueteDeEnemigos pde = (PaqueteDeEnemigos) new PaqueteDeEnemigos(
									Servidor.getEnemigosConectados()).clone();
							pde.setComando(Comando.CONEXIONENEMIGOS);
							
							PaqueteDeUbicacionEnemigos pdue = (PaqueteDeUbicacionEnemigos) new PaqueteDeUbicacionEnemigos(
									Servidor.getUbicacionEnemigos()).clone();
							pdue.setComando(Comando.UBICACIONENEMIGOS);
							
							synchronized (conectado) {
								conectado.getSalida().writeObject(gson.toJson(pdp));
								conectado.getSalida().writeObject(gson.toJson(pde));
								conectado.getSalida().writeObject(gson.toJson(pdue));
							}
						}
					}
				}
			} catch (Exception e) {
				Servidor.log.append("Falló al intentar enviar paqueteDePersonajes\n");
			}
		}
	}
}