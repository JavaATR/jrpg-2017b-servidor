package dominio;

import java.util.LinkedList;

import javax.sound.midi.Synthesizer;

public class Orco extends Personaje {

	public Orco(String nombre, Casta casta, int id) {
		super(nombre, casta, id);
		saludTope += 10;
		salud = saludTope;
		energia = energiaTope;
		nombreRaza="Orco";
		
		habilidadesRaza = new String[2];
		habilidadesRaza[0] = "Golpe Defensa";
		habilidadesRaza[1] = "Mordisco de Vida";
	}

	public Orco(String nombre, int salud, int energia, int fuerza, int destreza, int inteligencia, Casta casta,
			LinkedList<Item> itemsEquipados, LinkedList<Item> itemsGuardados, int experiencia, int nivel,
			int idPersonaje) {
		super(nombre, salud, energia, fuerza, destreza, inteligencia, casta, itemsEquipados, itemsGuardados,
				experiencia, nivel, idPersonaje);
		nombreRaza="Orco";
		
		habilidadesRaza = new String[2];
		habilidadesRaza[0] = "Golpe Defensa";
		habilidadesRaza[1] = "Mordisco de Vida";
	}

	public boolean habilidadRaza1(Peleable atacado) { // Golpe Defensa
		if (this.getEnergia() > 10){
			this.setEnergia(this.getEnergia() - 10);
			atacado.serAtacado(this.getDefensa()*2);
			return true;
			}
		return false;
	}

	public boolean habilidadRaza2(Peleable atacado) {// Mordisco de vida
		if (this.getEnergia() > 10) {
			this.setEnergia(this.getEnergia() - 10);
			int da�o_causado = atacado.serAtacado(this.getFuerza());
			if (da�o_causado != 0) {
				this.serCurado(da�o_causado);
				return true;
			}
		}
		return false;
	}

}
