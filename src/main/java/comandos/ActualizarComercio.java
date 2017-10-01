package comandos;

import java.io.IOException;

import mensajeria.PaqueteComerciar;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra el actualizado del comercio. <br>
 */
public class ActualizarComercio extends ComandosServer {
	/**
	 * Ejecuta el actualizado del comercio.
	 * <p>
	 * <i>En caso de que no se pueda actualizar el comercio, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		PaqueteComerciar paqueteComerciar;
		paqueteComerciar = (PaqueteComerciar) gson.fromJson(cadenaLeida, PaqueteComerciar.class);
		// BUSCO EN LAS ESCUCHAS AL QUE SE LO TENGO QUE MANDAR
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if (conectado.getPaquetePersonaje().getId() == paqueteComerciar.getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(paqueteComerciar));
				} catch (IOException e) {
					Servidor.log.append("Falló al intentar enviar paqueteComerciar a:"
							+ conectado.getPaquetePersonaje().getId() + ".\n");
				}
			}
		}
	}
}