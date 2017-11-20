package servidor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Clase que administra el conector con la base de datos.
 * <p>
 * Todo lo relacionado con la base de datos del juego se realiza acá. <br>
 */
public class Conector {
	/**
	 * Configuración de la conexión con la base de datos. <br>
	 */
	Configuration configuration;
	/**
	 * Session Factory de la configuración. <br>
	 */
	SessionFactory sessionFactory;
	/**
	 * Sesión de la base de datos. <br>
	 */
	Session session;
	/**
	 * Conector. <br>
	 */
	Connection connect;
	/**
	 * Tres. <br>
	 */
	private static final int TRES = 3;
	/**
	 * Cuatro. <br>
	 */
	private static final int CUATRO = 4;
	/**
	 * Cinco. <br>
	 */
	private static final int CINCO = 5;
	/**
	 * Seis. <br>
	 */
	private static final int SEIS = 6;
	/**
	 * Siete. <br>
	 */
	private static final int SIETE = 7;
	/**
	 * Ocho. <br>
	 */
	private static final int OCHO = 8;
	/**
	 * Nueve. <br>
	 */
	private static final int NUEVE = 9;

	/**
	 * Se conecta con la base de datos del juego.
	 * <p>
	 * <i>En caso de error indica que falló al intentar establecer una conexión
	 * con la base de datos. </i><br>
	 */
	public final void connect() {
		try {
			Servidor.log.append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
			this.configuration = new Configuration().configure("hibernate.cfg.xml");
			this.sessionFactory = this.configuration.buildSessionFactory();
			this.session = this.sessionFactory.openSession();
			Servidor.log.append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
		} catch ( HibernateException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		} 
	}

	/**
	 * Cierra la base de datos. <br>
	 */
	public final void close() {
		this.sessionFactory.close();
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
	public final boolean registrarUsuario(final PaqueteUsuario user) {
		Query query = this.session.getNamedQuery("HQL_OBTENER_USUARIO");
		query.setParameter("usuario", user.getUsername());
		if (query.list().isEmpty()) {
			this.session.save(user);
			this.session.beginTransaction().commit();
			Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
			return true;
		}
		Servidor.log.append("El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
		return false;
	}

	/**
	 * Registra a un personaje en el juego.
	 * <p>
	 * <i>En caso de que no se le pueda crear el inventario se avisa. <br>
	 * En caso de ocurrir otro tipo de error se informa el error de registro del
	 * personaje.</i><br>
	 * 
	 * @param paquetePersonaje
	 *            Personaje del usuario. <br>
	 * @param paqueteUsuario
	 *            Usuario del personaje. <br>
	 * @return <b>true</b> si se registra al personaje.<br>
	 *         <b>false</b> si no se lo registró. <br>
	 */
	public final boolean registrarPersonaje(final PaquetePersonaje paquetePersonaje,
			final PaqueteUsuario paqueteUsuario) {
		this.session.save(paquetePersonaje);
		this.session.beginTransaction().commit();
		Query<Integer> query = this.session.getNamedQuery("HQL_ID_PERSONAJE");
		query.setParameter("id", paquetePersonaje.getId());
		List<Integer> idPersonaje = query.list();
		if (!idPersonaje.isEmpty()) {
			paqueteUsuario.setIdPj(idPersonaje.get(1));
			this.session.update(paqueteUsuario);
			this.session.beginTransaction().commit();
			if (this.registrarInventarioMochila(idPersonaje.get(1))) {
				Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
						+ paquetePersonaje.getId() + System.lineSeparator());
				return true;
			} else {
				Servidor.log.append(
						"Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername()
								+ " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
			}
		}
		return false;
	}

	/**
	 * Registra el inventario de un personaje.
	 * <p>
	 * <i>En caso de que no se le pueda crear el inventario se avisa.</i> <br>
	 * 
	 * @param idInventarioMochila
	 *            ID del inventario del personaje. <br>
	 * @return <b>true</b> si se registra el inventario.<br>
	 *         <b>false</b> si no se lo registra. <br>
	 */
	public final boolean registrarInventarioMochila(final int idInventarioMochila) {
//		try {
			// Preparo la consulta para el registro el inventario en la base de
			// datos
			Query<Integer> query = this.session.getNamedQuery("HQL_INSERTAR_INVENTARIO");
			query.setParameter("id", idInventarioMochila);
			query.list();
//			PreparedStatement stRegistrarInventario = connect.prepareStatement(
//					"INSERT INTO inventario(idInventario,manos1,manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
//			stRegistrarInventario.setInt(1, idInventarioMochila);
			// Preparo la consulta para el registro la mochila en la base de
			// datos
			
		SQLQuery mochilaSql = session.createSQLQuery(
				"INSERT INTO mochila(idMochila, item1, item2, item3, item4, item5, item6, item7, item8, item9, item10, item11, item12, item13, item14, item15, item16, item17, item18, item19, item20) VALUES( "
						+ idInventarioMochila
						+ ", -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)");
			
			mochilaSql.executeUpdate();
			
//			PreparedStatement stRegistrarMochila = connect.prepareStatement(
//					"INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,item16,item17,item18,item19,item20) VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
//			stRegistrarMochila.setInt(1, idInventarioMochila);
//			// Registro inventario y mochila
//			stRegistrarInventario.execute();
//			stRegistrarMochila.execute();
			// Le asigno el inventario y la mochila al personaje
//			PreparedStatement stAsignarPersonaje = connect
//					.prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
//			stAsignarPersonaje.setInt(1, idInventarioMochila);
//			stAsignarPersonaje.setInt(2, idInventarioMochila);
//			stAsignarPersonaje.setInt(TRES, idInventarioMochila);
			Query queryPersonaje = this.session.getNamedQuery("HQL_GET_PERSONAJE");
			queryPersonaje.setParameter("id", idInventarioMochila);
			PaquetePersonaje personaje = (PaquetePersonaje) queryPersonaje.list().iterator().next();
			personaje.setIdMochila(idInventarioMochila);
			this.session.update(personaje);
			this.session.beginTransaction().commit();
//			stAsignarPersonaje.execute();
			Servidor.log.append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
			return true;
//		} catch (SQLException e) {
//			Servidor.log.append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
//			return false;
//		}
	}

	/**
	 * Loguea a un usuario al juego.
	 * <p>
	 * <i>En caso de que no se pueda loguear al usuario, se avisa. <br>
	 * En caso de ocurrir otro tipo de error se informa el error de logueo de
	 * usuario. </i><br>
	 * 
	 * @param user
	 *            Usuario a conectar. <br>
	 * @return <b>true</b> si se logueó al usuario.<br>
	 *         <b>false</b> si no se lo logueó. <br>
	 */
	public final boolean loguearUsuario(final PaqueteUsuario user) {
		Query query = this.session.createNamedQuery("HQL_LOGUEAR_USUARIO");
		query.setParameter("usuario", user.getUsername());
		query.setParameter("password", user.getPassword());
		if (!query.list().isEmpty()) {
			Servidor.log.append("El usuario " + user.getUsername() + " ha iniciado sesión." + System.lineSeparator());
			return true;
		}
		Servidor.log.append("El usuario " + user.getUsername() + " ha realizado un intento fallido de inicio de sesión."
				+ System.lineSeparator());
		return false;
	}

	/**
	 * Actualiza el personaje al dejar la partida.
	 * <p>
	 * <i>En caso de que no se pueda actualizar al personaje, se avisa.</i> <br>
	 * 
	 * @param paquetePersonaje
	 *            Personaje a guardar sus estados. <br>
	 */
	public final void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
		
		this.session.save(paquetePersonaje);
		this.session.beginTransaction().commit();
		
		
		try {
			int i = 2;
			int j = 1;
			PreparedStatement stActualizarPersonaje = connect.prepareStatement(
					"UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=?, puntosAsignar=? "
							+ "  WHERE idPersonaje=?");
			
			this.session.update(paquetePersonaje);
			this.session.beginTransaction().commit();
//			
//			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
//			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
//			stActualizarPersonaje.setInt(TRES, paquetePersonaje.getInteligencia());
//			stActualizarPersonaje.setInt(CUATRO, paquetePersonaje.getSaludTope());
//			stActualizarPersonaje.setInt(CINCO, paquetePersonaje.getEnergiaTope());
//			stActualizarPersonaje.setInt(SEIS, paquetePersonaje.getExperiencia());
//			stActualizarPersonaje.setInt(SIETE, paquetePersonaje.getNivel());
//			stActualizarPersonaje.setInt(OCHO, paquetePersonaje.getId());
//			stActualizarPersonaje.setInt(NUEVE, paquetePersonaje.getPuntosAsignar());
//			stActualizarPersonaje.executeUpdate();
			PreparedStatement stDameItemsID = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
			stDameItemsID.setInt(1, paquetePersonaje.getId());
			ResultSet resultadoItemsID = stDameItemsID.executeQuery();
			PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");
			ResultSet resultadoDatoItem = null;
			paquetePersonaje.eliminarItems();
			while (j <= NUEVE) {
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
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}
	}

	/**
	 * Devuelve el personaje del cliente. <br>
	 * <i>En caso de que no se pueda obtener al personaje, se avisa y se le crea
	 * uno nuevo. </i><br>
	 * 
	 * @param user
	 *            Cliente. <br>
	 * @return Personaje del cliente. En caso de no tener uno previo, se lo
	 *         crea. <br>
	 * @throws IOException
	 *             En caso de no poder crear el paquete de personaje tira error.
	 *             <br>
	 */
	public final PaquetePersonaje getPersonaje(final PaqueteUsuario user) throws IOException {
		ResultSet result = null;
		ResultSet resultadoItemsID = null;
		ResultSet resultadoDatoItem = null;
		int i = 2;
		int j = 0;
		try {

			Query query = this.session.createQuery("SELECT idPj FROM mensajeria.PaqueteUsuario WHERE usuario = '"
					+ user.getUsername() + "'");
			query = this.session.createQuery("SELECT id FROM mensajeria.PaquetePersonaje WHERE id = " + query.getFirstResult());
			
			SQLQuery mochilaSql = session.createSQLQuery("SELECT * FROM mochila WHERE idMochila = " + query.getFirstResult());
			
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
			
			
			Query itemQuery = this.session.createNamedQuery("HQL_GET_ITEM");
			
			
			while (j <= NUEVE) {
				if (resultadoItemsID.getInt(i) != -1) {
					stDatosItem.setInt(1, resultadoItemsID.getInt(i));
					
					// itemQuery.setParameter("idItem", )
					
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
	 * nuevo.</i> <br>
	 * 
	 * @param usuario
	 *            Nombre de usuario. <br>
	 * @return Usuario. <br>
	 */
	public final PaqueteUsuario getUsuario(final String usuario) {
//		ResultSet result = null;
//		PreparedStatement st;
//		try {
		Query<Integer> query = this.session.getNamedQuery("HQL_OBTENER_USUARIO");
		if(!query.list().isEmpty()){
		query.setParameter("usuario", usuario);
		return new PaqueteUsuario(Integer.parseInt(query.getParameter("idPj").toString()), query.getParameter("usuario").toString(),
				query.getParameter("password").toString());
		}
		return new PaqueteUsuario();
	}

	/**
	 * Actualiza el inventario del personaje. <br>
	 * 
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public final void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
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
	 * avisa. </i><br>
	 * 
	 * @param idPersonaje
	 *            ID del personaje. <br>
	 */
	public final void actualizarInventario(final int idPersonaje) {
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
			if (paquetePersonaje.getCantItems() < NUEVE) {
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
	 * <i>En caso de que no se pueda actualizar al personaje, se avisa. </i><br>
	 * 
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public final void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect.prepareStatement(
					"UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=?, puntosAsignar=? "
							+ "  WHERE idPersonaje=?");
			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(TRES, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(CUATRO, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(CINCO, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(SEIS, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(SIETE, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(OCHO, paquetePersonaje.getId());
			stActualizarPersonaje.setInt(NUEVE, paquetePersonaje.getPuntosAsignar());
			stActualizarPersonaje.executeUpdate();
			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		}
	}
}
