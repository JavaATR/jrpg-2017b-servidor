package comandos;

import java.io.IOException;

import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteUsuario;
import servidor.Servidor;

/**
 * Clase que administra el registro de usuarios. <br>
 */
public class Registro extends ComandosServer {
	/**
	 * Ejecuta el registro de un nuevo usuario.
	 * <p>
	 * <i>En caso de que no se pueda registrar, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		Paquete paqueteSv = new Paquete(null, 0);
		paqueteSv.setComando(Comando.REGISTRO);
		escuchaCliente.setPaqueteUsuario((PaqueteUsuario) (gson.fromJson(cadenaLeida, PaqueteUsuario.class)).clone());
		// Si el usuario se pudo registrar le envio un msj de exito
		try {
			if (Servidor.getConector().registrarUsuario(escuchaCliente.getPaqueteUsuario())) {
				paqueteSv.setMensaje(Paquete.msjExito);
				escuchaCliente.getSalida().writeObject(gson.toJson(paqueteSv));
				// Si el usuario no se pudo registrar le envio un msj de fracaso
			} else {
				paqueteSv.setMensaje(Paquete.msjFracaso);
				escuchaCliente.getSalida().writeObject(gson.toJson(paqueteSv));
			}
		} catch (IOException e) {
			Servidor.log.append("Falló al intentar enviar registro\n");
		}
	}
}