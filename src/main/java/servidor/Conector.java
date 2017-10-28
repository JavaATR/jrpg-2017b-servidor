package servidor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Clase que administra el conector con la base de datos.
 * <p>
 * Todo lo relacionado con la base de datos del juego se realiza acá. <br>
 */
public class Conector {
	/**
	 * Nombre de la base de datos. <br>
	 */
	private String url = "primeraBase.bd";
	/**
	 * Conector. <br>
	 */
	Connection connect;

	/**
	 * Se conecta con la base de datos del juego.
	 * <p>
	 * <i>En caso de error indica que falló al intentar establecer una conexión
	 * con la base de datos. </i><br>
	 */
	public void connect() {
		try {
			Servidor.log.append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
			connect = DriverManager.getConnection("jdbc:sqlite:" + url);
			Servidor.log.append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	/**
	 * Cierra la base de datos.
	 * <p>
	 * <i>En caso de error indica un error al cerrar la conexión con la base de
	 * datos.</i> <br>
	 */
	public void close() {
		try {
			connect.close();
		} catch (SQLException ex) {
			Servidor.log.append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
			Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Registra un cliente en la base de datos.
	 * <p>
	 * <i>En caso de existir un usuario con ese nombre se le avisa. <br>
	 * En caso de ocurrir otro tipo de error se informa el error de registro del
	 * usuario.</i><br>
	 * @param user
	 *            Usuario a registrar. <br>
	 * @return <b>true</b> si se registra al usuario.<br>
	 *         <b>false</b> si no se lo registró. <br>
	 */
	public boolean registrarUsuario(final PaqueteUsuario user) {
		ResultSet result = null;
		try {
			PreparedStatement st1 = connect.prepareStatement("SELECT * FROM registro WHERE usuario= ? ");
			st1.setString(1, user.getUsername());
			result = st1.executeQuery();
			if (!result.next()) {
				PreparedStatement st = connect
						.prepareStatement("INSERT INTO registro (usuario, password, idPersonaje) VALUES (?,?,?)");
				st.setString(1, user.getUsername());
				st.setString(2, user.getPassword());
				st.setInt(3, user.getIdPj());
				st.execute();
				Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
				return true;
			} else {
				Servidor.log.append(
						"El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
				return false;
			}
		} catch (SQLException ex) {
			Servidor.log.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(ex.getMessage());
			return false;
		}
	}

	/**
	 * Registra a un personaje en el juego.
	 * <p>
	 * <i>En caso de que no se le pueda crear el inventario se avisa. <br>
	 * En caso de ocurrir otro tipo de error se informa el error de registro del
	 * personaje.</i><br>
	 * @param paquetePersonaje
	 *            Personaje del usuario. <br>
	 * @param paqueteUsuario
	 *            Usuario del personaje. <br>
	 * @return <b>true</b> si se registra al personaje.<br>
	 *         <b>false</b> si no se lo registró. <br>
	 */
	public boolean registrarPersonaje(final PaquetePersonaje paquetePersonaje, final PaqueteUsuario paqueteUsuario) {
		try {
			// Registro al personaje en la base de datos
			PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
					"INSERT INTO personaje (idInventario, idMochila,casta,raza,fuerza,destreza,inteligencia,saludTope,energiaTope,nombre,experiencia,nivel,idAlianza,puntosAsignar) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			stRegistrarPersonaje.setInt(1, -1);
			stRegistrarPersonaje.setInt(2, -1);
			stRegistrarPersonaje.setString(3, paquetePersonaje.getCasta());
			stRegistrarPersonaje.setString(4, paquetePersonaje.getRaza());
			stRegistrarPersonaje.setInt(5, paquetePersonaje.getFuerza());
			stRegistrarPersonaje.setInt(6, paquetePersonaje.getDestreza());
			stRegistrarPersonaje.setInt(7, paquetePersonaje.getInteligencia());
			stRegistrarPersonaje.setInt(8, paquetePersonaje.getSaludTope());
			stRegistrarPersonaje.setInt(9, paquetePersonaje.getEnergiaTope());
			stRegistrarPersonaje.setString(10, paquetePersonaje.getNombre());
			stRegistrarPersonaje.setInt(11, 0);
			stRegistrarPersonaje.setInt(12, 1);
			stRegistrarPersonaje.setInt(13, -1);
			stRegistrarPersonaje.setInt(14, 3);
			stRegistrarPersonaje.execute();
			// Recupero la última key generada
			ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();
			if (rs != null && rs.next()) {
				// Obtengo el id
				int idPersonaje = rs.getInt(1);
				// Le asigno el id al paquete personaje que voy a devolver
				paquetePersonaje.setId(idPersonaje);
				// Le asigno el personaje al usuario
				PreparedStatement stAsignarPersonaje = connect
						.prepareStatement("UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
				stAsignarPersonaje.setInt(1, idPersonaje);
				stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
				stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
				stAsignarPersonaje.execute();
				// Por ultimo registro el inventario y la mochila
				if (this.registrarInventarioMochila(idPersonaje)) {
					Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
							+ paquetePersonaje.getId() + System.lineSeparator());
					return true;
				} else {
					Servidor.log.append(
							"Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername()
									+ " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
					return false;
				}
			}
			return false;
		} catch (SQLException e) {
			Servidor.log.append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			return false;
		}
	}

	/**
	 * Registra el inventario de un personaje.
	 * <p>
	 * <i>En caso de que no se le pueda crear el inventario se avisa. <br>
	 * @param idInventarioMochila
	 *            ID del inventario del personaje. <br>
	 * @return <b>true</b> si se registra el inventario.<br>
	 *         <b>false</b> si no se lo registra. <br>
	 */
	public boolean registrarInventarioMochila(final int idInventarioMochila) {
		try {
			// Preparo la consulta para el registro el inventario en la base de
			// datos
			PreparedStatement stRegistrarInventario = connect.prepareStatement(
					"INSERT INTO inventario(idInventario,manos1,manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
			stRegistrarInventario.setInt(1, idInventarioMochila);
			// Preparo la consulta para el registro la mochila en la base de
			// datos
			PreparedStatement stRegistrarMochila = connect.prepareStatement(
					"INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,item16,item17,item18,item19,item20) VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
			stRegistrarMochila.setInt(1, idInventarioMochila);
			// Registro inventario y mochila
			stRegistrarInventario.execute();
			stRegistrarMochila.execute();
			// Le asigno el inventario y la mochila al personaje
			PreparedStatement stAsignarPersonaje = connect
					.prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
			stAsignarPersonaje.setInt(1, idInventarioMochila);
			stAsignarPersonaje.setInt(2, idInventarioMochila);
			stAsignarPersonaje.setInt(3, idInventarioMochila);
			stAsignarPersonaje.execute();
			Servidor.log.append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
			return true;
		} catch (SQLException e) {
			Servidor.log.append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
			return false;
		}
	}

	/**
	 * Loguea a un usuario al juego.
	 * <p>
	 * <i>En caso de que no se pueda loguear al usuario, se avisa. <br>
	 * En caso de ocurrir otro tipo de error se informa el error de logueo de
	 * usuario. </i><br>
	 * @param user
	 *            Usuario a conectar. <br>
	 * @return <b>true</b> si se logueó al usuario.<br>
	 *         <b>false</b> si no se lo logueó. <br>
	 */
	final public boolean loguearUsuario(final PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Busco usuario y contraseña
			PreparedStatement st = connect
					.prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
			st.setString(1, user.getUsername());
			st.setString(2, user.getPassword());
			result = st.executeQuery();
			// Si existe inicio sesion
			if (result.next()) {
				Servidor.log
						.append("El usuario " + user.getUsername() + " ha iniciado sesión." + System.lineSeparator());
				return true;
			}
			// Si no existe informo y devuelvo false
			Servidor.log.append("El usuario " + user.getUsername()
					+ " ha realizado un intento fallido de inicio de sesión." + System.lineSeparator());
			return false;
		} catch (SQLException e) {
			Servidor.log
					.append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
			return false;
		}
	}

	/**
	 * Actualiza el personaje al dejar la partida.
	 * <p>
	 * <i>En caso de que no se pueda actualizar al personaje, se avisa. <br>
	 * @param paquetePersonaje
	 *            Personaje a guardar sus estados. <br>
	 */
	public void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
		try {
			int i = 2;
			int j = 1;
			PreparedStatement stActualizarPersonaje = connect.prepareStatement(
					"UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=?, puntosAsignar=? "
							+ "  WHERE idPersonaje=?");
			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());
			stActualizarPersonaje.setInt(9, paquetePersonaje.getPuntosAsignar());
			stActualizarPersonaje.executeUpdate();
			PreparedStatement stDameItemsID = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
			stDameItemsID.setInt(1, paquetePersonaje.getId());
			ResultSet resultadoItemsID = stDameItemsID.executeQuery();
			PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");
			ResultSet resultadoDatoItem = null;
			paquetePersonaje.eliminarItems();
			while (j <= 9) {
				if (resultadoItemsID.getInt(i) != -1) {
					stDatosItem.setInt(1, resultadoItemsID.getInt(i));
					resultadoDatoItem = stDatosItem.executeQuery();
					paquetePersonaje.anadirItem(resultadoDatoItem.getInt("idItem"),
							resultadoDatoItem.getString("nombre"), resultadoDatoItem.getInt("wereable"),
							resultadoDatoItem.getInt("bonusSalud"), resultadoDatoItem.getInt("bonusEnergia"),
							resultadoDatoItem.getInt("bonusFuerza"), resultadoDatoItem.getInt("bonusDestreza"),
							resultadoDatoItem.getInt("bonusInteligencia"), resultadoDatoItem.getString("foto"),
							resultadoDatoItem.getString("fotoEquipado"));
				}
				i++;
				j++;
			}
			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
			;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}
	}

	/**
	 * Devuelve el personaje del cliente. <br>
	 * <i>En caso de que no se pueda obtener al personaje, se avisa y se le crea
	 * uno nuevo. <br>
	 * @param user
	 *            Cliente. <br>
	 * @return Personaje del cliente. En caso de no tener uno previo, se lo
	 *         crea. <br>
	 * @throws IOException
	 *             En caso de no poder crear el paquete de personaje tira error.
	 *             <br>
	 */
	public PaquetePersonaje getPersonaje(final PaqueteUsuario user) throws IOException {
		ResultSet result = null;
		ResultSet resultadoItemsID = null;
		ResultSet resultadoDatoItem = null;
		int i = 2;
		int j = 0;
		try {
			// Selecciono el personaje de ese usuario
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, user.getUsername());
			result = st.executeQuery();
			// Obtengo el id
			int idPersonaje = result.getInt("idPersonaje");
			// Selecciono los datos del personaje
			PreparedStatement stSeleccionarPersonaje = connect
					.prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
			stSeleccionarPersonaje.setInt(1, idPersonaje);
			result = stSeleccionarPersonaje.executeQuery();
			// Traigo los id de los items correspondientes a mi personaje
			PreparedStatement stDameItemsID = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
			stDameItemsID.setInt(1, idPersonaje);
			resultadoItemsID = stDameItemsID.executeQuery();
			// Traigo los datos del item
			PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");
			// Obtengo los atributos del personaje
			PaquetePersonaje personaje = new PaquetePersonaje();
			personaje.setId(idPersonaje);
			personaje.setRaza(result.getString("raza"));
			personaje.setCasta(result.getString("casta"));
			personaje.setFuerza(result.getInt("fuerza"));
			personaje.setInteligencia(result.getInt("inteligencia"));
			personaje.setDestreza(result.getInt("destreza"));
			personaje.setEnergiaTope(result.getInt("energiaTope"));
			personaje.setSaludTope(result.getInt("saludTope"));
			personaje.setNombre(result.getString("nombre"));
			personaje.setExperiencia(result.getInt("experiencia"));
			personaje.setNivel(result.getInt("nivel"));
			personaje.setPuntosAsignar(result.getInt("puntosAsignar"));
			while (j <= 9) {
				if (resultadoItemsID.getInt(i) != -1) {
					stDatosItem.setInt(1, resultadoItemsID.getInt(i));
					resultadoDatoItem = stDatosItem.executeQuery();
					personaje.anadirItem(resultadoDatoItem.getInt("idItem"), resultadoDatoItem.getString("nombre"),
							resultadoDatoItem.getInt("wereable"), resultadoDatoItem.getInt("bonusSalud"),
							resultadoDatoItem.getInt("bonusEnergia"), resultadoDatoItem.getInt("bonusFuerza"),
							resultadoDatoItem.getInt("bonusDestreza"), resultadoDatoItem.getInt("bonusInteligencia"),
							resultadoDatoItem.getString("foto"), resultadoDatoItem.getString("fotoEquipado"));
				}
				i++;
				j++;
			}
			// Devuelvo el paquete personaje con sus datos
			return personaje;
		} catch (SQLException ex) {
			Servidor.log
					.append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
		}
		return new PaquetePersonaje();
	}

	/**
	 * Obtiene el usuario del cliente.
	 * <p>
	 * <i>En caso de que no se pueda obtener al usuario, avisa y se le crea uno
	 * nuevo. <br>
	 * @param usuario
	 *            Usuario. <br>
	 * @return Usuario. <br>
	 */
	public PaqueteUsuario getUsuario(final String usuario) {
		ResultSet result = null;
		PreparedStatement st;
		try {
			st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, usuario);
			result = st.executeQuery();
			String password = result.getString("password");
			int idPersonaje = result.getInt("idPersonaje");
			PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
			paqueteUsuario.setUsername(usuario);
			paqueteUsuario.setPassword(password);
			paqueteUsuario.setIdPj(idPersonaje);
			return paqueteUsuario;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
			Servidor.log.append(e.getMessage() + System.lineSeparator());
		}
		return new PaqueteUsuario();
	}

	/**
	 * Actualiza el inventario del personaje. <br>
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
		int i = 0;
		PreparedStatement stActualizarMochila;
		try {
			stActualizarMochila = connect.prepareStatement(
					"UPDATE mochila SET item1=? ,item2=? ,item3=? ,item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
							+ ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,item18=? ,item19=? ,item20=? WHERE idMochila=?");
			while (i < paquetePersonaje.getCantItems()) {
				stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
				i++;
			}
			for (int j = paquetePersonaje.getCantItems(); j < 20; j++) {
				stActualizarMochila.setInt(j + 1, -1);
			}
			stActualizarMochila.setInt(21, paquetePersonaje.getId());
			stActualizarMochila.executeUpdate();
		} catch (SQLException e) {
		}
	}

	/**
	 * Actualiza el inventario del personaje.
	 * <p>
	 * <i>En caso de que no se pueda actualizar el inventario del personaje, se
	 * avisa. <br>
	 * @param idPersonaje
	 *            ID del personaje. <br>
	 */
	public void actualizarInventario(final int idPersonaje) {
		int i = 0;
		PaquetePersonaje paquetePersonaje = Servidor.getPersonajesConectados().get(idPersonaje);
		PreparedStatement stActualizarMochila;
		try {
			stActualizarMochila = connect.prepareStatement(
					"UPDATE mochila SET item1=? ,item2=? ,item3=? ,item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
							+ ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,item18=? ,item19=? ,item20=? WHERE idMochila=?");
			while (i < paquetePersonaje.getCantItems()) {
				stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
				i++;
			}
			if (paquetePersonaje.getCantItems() < 9) {
				int itemGanado = new Random().nextInt(29);
				itemGanado += 1;
				stActualizarMochila.setInt(paquetePersonaje.getCantItems() + 1, itemGanado);
				for (int j = paquetePersonaje.getCantItems() + 2; j < 20; j++) {
					stActualizarMochila.setInt(j, -1);
				}
			} else {
				for (int j = paquetePersonaje.getCantItems() + 1; j < 20; j++) {
					stActualizarMochila.setInt(j, -1);
				}
			}
			stActualizarMochila.setInt(21, paquetePersonaje.getId());
			stActualizarMochila.executeUpdate();
		} catch (SQLException e) {
			Servidor.log.append("Falló al intentar actualizar inventario de" + idPersonaje + "\n");
		}
	}

	/**
	 * Actualiza los stats del personaje que subió de nivel.
	 * <p>
	 * <i>En caso de que no se pueda actualizar al personaje, se avisa. <br>
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect.prepareStatement(
					"UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=?, puntosAsignar=? "
							+ "  WHERE idPersonaje=?");
			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());
			stActualizarPersonaje.setInt(9, paquetePersonaje.getPuntosAsignar());
			stActualizarPersonaje.executeUpdate();
			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}
	}
}