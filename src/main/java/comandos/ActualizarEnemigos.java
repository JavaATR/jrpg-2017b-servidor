package comandos;

import mensajeria.PaqueteDeEnemigos;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaqueteEnemigo;
import servidor.Servidor;

/**
 * Clase que administra la conexión entre los clientes. <br>
 */
public class ActualizarEnemigos extends ComandosServer {
	/**
	 * Ejecuta la conexión entre los clientes, los mantiene actualizados. <br>
	 */
	@Override
	public void ejecutar() {
		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}
	}
}
