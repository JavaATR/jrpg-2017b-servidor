package comandos;

import mensajeria.Comando;
import servidor.EscuchaCliente;

/**
 * Clase que administra la forma de escuchar los comandos desde el servidor.
 * <br>
 */
public abstract class ComandosServer extends Comando {
	/**
	 * Escucha del cliente. <br>
	 */
	protected EscuchaCliente escuchaCliente;

	/**
	 * Escucha el comando ejecutado por el cliente. <br>
	 * 
	 * @param escuchaCliente
	 *            Escucha del cliente. <br>
	 */
	public void setEscuchaCliente(final EscuchaCliente escuchaCliente) {
		this.escuchaCliente = escuchaCliente;
	}
}
