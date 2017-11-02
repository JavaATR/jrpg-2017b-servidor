package comandos;

import servidor.Servidor;

/**
 * Clase que administra la conexión entre los clientes. <br>
 */
public class ActualizarEnemigos extends ComandosServer {
	/**
	 * Ejecuta la conexión entre los clientes, los mantiene actualizados. <br>
	 */
	@Override
	public final void ejecutar() {
		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}
	}
}
