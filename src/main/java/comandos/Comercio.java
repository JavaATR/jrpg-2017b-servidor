package comandos;

import java.io.IOException;

import mensajeria.PaqueteComerciar;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra el comercio entre clientes. <br>
 */
public class Comercio extends ComandosServer {
	/**
	 * Ejecuta el comercio entre los clientes.
	 * <p>
	 * <i>En caso de que no se pueda realizar el comercio, se avisa.</i> <br>
	 */
	@Override
	public final void ejecutar() {
		PaqueteComerciar paqueteComerciar;
		paqueteComerciar = (PaqueteComerciar) gson.fromJson(
				cadenaLeida, PaqueteComerciar.class);
		// BUSCO EN LAS ESCUCHAS AL QUE SE LO TENGO QUE MANDAR
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if (conectado.getPaquetePersonaje().getId() == paqueteComerciar.getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(
							paqueteComerciar));
				} catch (IOException e) {
					Servidor.log.append(
							"Falló al intentar enviar comercio a: "
							 + conectado.getPaquetePersonaje().
							 getId() + ".\n");
				}
			}
		}
	}
}
