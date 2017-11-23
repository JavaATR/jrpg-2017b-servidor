package servidor;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import dominio.Item;
import dominio.Mochila;
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
	private Configuration configuration;
	/**
	 * this.session Factory de la configuración. <br>
	 */
	private SessionFactory sessionFactory;
	/**
	 * Sesión de la base. <br>
	 */
	private Session session;

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
		} catch (HibernateException ex) {
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
		if (this.session.getNamedQuery("HQL_GET_USUARIO").setParameter("usuario", user.getUsername()).list()
				.isEmpty()) {
			this.session.save(user);
			this.session.beginTransaction().commit();
			Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
			this.session.clear();
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
			paqueteUsuario.setIdPj(idPersonaje.get(0));
			this.session.update(paqueteUsuario);
			this.session.beginTransaction().commit();
			this.session.clear();
			if (this.registrarMochila(idPersonaje.get(0))) {
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
	 * Registra la mochila de un personaje.
	 * <p>
	 * <i>En caso de que no se le pueda crear la mochila se avisa.</i> <br>
	 * @param idMochila
	 *            ID de la mochila del personaje. <br>
	 * @return <b>true</b> si se registra la mochila.<br>
	 *         <b>false</b> si no se lo registra. <br>
	 */
	public final boolean registrarMochila(final int idMochila) {
		// Genero la mochila con el id del personaje.
		Mochila mochila = new Mochila();
		mochila.setIdMochila(idMochila);
		this.session.save(mochila);
		this.session.beginTransaction().commit();
		// Le asigno la mochila al personaje.
		PaquetePersonaje personaje = (PaquetePersonaje) this.session.getNamedQuery("HQL_GET_PERSONAJE")
				.setParameter("id", idMochila).list().iterator().next();
		personaje.setIdMochila(idMochila);
		this.session.update(personaje);
		this.session.beginTransaction().commit();
		Servidor.log.append("Se ha registrado la mochila de " + idMochila + System.lineSeparator());
		this.session.clear();
		return true;
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
	public final boolean loguearUsuario(final PaqueteUsuario user) {
		if (!this.session.createNamedQuery("HQL_LOGUEAR_USUARIO").setParameter("usuario", user.getUsername())
				.setParameter("password", user.getPassword()).list().isEmpty()) {
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
	 * @param paquetePersonaje
	 *            Personaje a guardar sus estados. <br>
	 */
	public final void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
		this.session.clear();
		this.session.update(paquetePersonaje);
		this.session.beginTransaction().commit();
		Mochila mochila = (Mochila) this.session.createNamedQuery("HQL_GET_MOCHILA")
				.setParameter("idMochila", paquetePersonaje.getId()).list().iterator().next();
		paquetePersonaje.eliminarItems();
		Query queryItem = this.session.createNamedQuery("HQL_GET_ITEM");
		Item item;
		if (mochila.getItem1() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem1()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem2() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem2()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem3() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem3()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem4() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem4()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem5() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem5()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem6() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem6()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem7() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem7()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem8() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem8()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem9() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem9()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
				+ System.lineSeparator());
	}

	/**
	 * Devuelve el personaje del cliente. <br>
	 * <i>En caso de que no se pueda obtener al personaje, se avisa y se le crea
	 * uno nuevo. </i><br>
	 * @param user
	 *            Cliente. <br>
	 * @return Personaje del cliente. En caso de no tener uno previo, se lo
	 *         crea. <br>
	 * @throws IOException
	 *             En caso de no poder crear el paquete de personaje tira error.
	 *             <br>
	 */
	public final PaquetePersonaje getPersonaje(final PaqueteUsuario user) throws IOException {
		PaqueteUsuario paqueteUsuario = (PaqueteUsuario) this.session.getNamedQuery("HQL_GET_USUARIO")
				.setParameter("usuario", user.getUsername()).list().iterator().next();
		// Obtengo el personaje del usuario.
		PaquetePersonaje paquetePersonaje = (PaquetePersonaje) this.session.getNamedQuery("HQL_GET_PERSONAJE")
				.setParameter("id", paqueteUsuario.getIdPj()).list().iterator().next();
		// Obtengo la mochila del usuario.
		Mochila mochila = (Mochila) this.session.getNamedQuery("HQL_GET_MOCHILA")
				.setParameter("idMochila", paquetePersonaje.getId()).list().iterator().next();
		Query queryItem = this.session.createNamedQuery("HQL_GET_ITEM");
		Item item;
		if (mochila.getItem1() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem1()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem2() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem2()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem3() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem3()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem4() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem4()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem5() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem5()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem6() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem6()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem7() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem7()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem8() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem8()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		if (mochila.getItem9() != -1) {
			item = (Item) queryItem.setParameter("idItem", mochila.getItem9()).list().iterator().next();
			paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getBonusSalud(),
					item.getBonusEnergia(), item.getBonusFuerza(), item.getBonusDestreza(), item.getBonusInteligencia(),
					item.getFotoEquipado(), item.getFotoEquipado());
		}
		return paquetePersonaje;
	}

	/**
	 * Obtiene el usuario del cliente.
	 * <p>
	 * <i>En caso de que no se pueda obtener al usuario, avisa y se le crea uno
	 * nuevo.</i> <br>
	 * @param usuario
	 *            Nombre de usuario. <br>
	 * @return Usuario. <br>
	 */
	public final PaqueteUsuario getUsuario(final String usuario) {
		Query query = this.session.getNamedQuery("HQL_GET_USUARIO").setParameter("usuario", usuario);
		if (!query.list().isEmpty()) {
			PaqueteUsuario paqueteUsuario = (PaqueteUsuario) query.list().iterator().next();
			this.session.clear();
			return new PaqueteUsuario(paqueteUsuario.getIdPj(), paqueteUsuario.getUsername(),
					paqueteUsuario.getPassword());
		}
		return new PaqueteUsuario();
	}

	/**
	 * Actualiza el inventario del personaje. <br>
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public final void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
		Mochila mochila = (Mochila) this.session.createNamedQuery("HQL_GET_MOCHILA")
				.setParameter("idMochila", paquetePersonaje.getIdMochila()).list().iterator().next();
		this.session.update(mochila);
		this.session.beginTransaction().commit();
		this.session.clear();
	}

	/**
	 * Actualiza el inventario del personaje.
	 * <p>
	 * <i>En caso de que no se pueda actualizar el inventario del personaje, se
	 * avisa. </i><br>
	 * @param idPersonaje
	 *            ID del personaje. <br>
	 */
	public final void actualizarInventario(final int idPersonaje) {
		Mochila mochila = (Mochila) this.session.createNamedQuery("HQL_GET_MOCHILA")
				.setParameter("idMochila", idPersonaje).list().iterator().next();
		if (mochila.cantidadItems() < 9) {
			int itemGanado = new Random().nextInt(29) + 1;
			mochila.agregarItem(itemGanado, mochila.cantidadItems() + 1);
			Servidor.log.append("El personaje " + idPersonaje + " ha obtenido el item " + itemGanado + "."
					+ System.lineSeparator());
		}
		this.session.update(mochila);
		this.session.beginTransaction().commit();
	}

	/**
	 * Actualiza los stats del personaje que subió de nivel.
	 * <p>
	 * <i>En caso de que no se pueda actualizar al personaje, se avisa. </i><br>
	 * @param paquetePersonaje
	 *            Personaje del cliente. <br>
	 */
	public final void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
		this.session.update(paquetePersonaje);
		this.session.beginTransaction().commit();
		this.session.clear();
	}
}
