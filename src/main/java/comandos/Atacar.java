package comandos;

import java.io.IOException;

import mensajeria.PaqueteAtacar;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra los turnos de ataque de los clientes. <br>
 */
public class Atacar extends ComandosServer {
	/**
	 * Realiza el ataque al otro personaje.
	 * <p>
	 * <i>En caso de que no se pueda realizar el ataque, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaqueteAtacar((PaqueteAtacar) gson
				.fromJson(cadenaLeida, PaqueteAtacar.class));
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if (conectado.getIdPersonaje() == escuchaCliente
					.getPaqueteAtacar().getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(
							escuchaCliente.getPaqueteAtacar()));
				} catch (IOException e) {
					Servidor.log.append(
							"Falló al intentar enviar ataque a:" + 
							conectado.getPaquetePersonaje().getId() + ".\n");
				}
			}
		}
	}
}
