import java.awt.*;

public class Jugador {
	public int x, y;
	public int ancho = 40, alto = 20;
	public int velocidad = 5;

	public Jugador(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void moverIzquierda() {
		x -= velocidad;
		if (x < 0)
			x = 0;
	}

	public void moverDerecha() {
		x += velocidad;
		if (x + ancho > PanelJuego.ANCHO)
			x = PanelJuego.ANCHO - ancho;
	}

	public int getCentroX() {
		return x + ancho / 2;
	}

	public void dibujar(Graphics g) {
		g.setColor(Color.CYAN);
		g.fillRect(x, y, ancho, alto);
	}
}