
import java.awt.*;

public class BalaEnemiga {
	public int x, y;
	public int ancho = 4, alto = 10;
	public int velocidad = 5;

	public BalaEnemiga(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void mover() {
		y += velocidad;
	}

	public Rectangle getRect() {
		return new Rectangle(x, y, ancho, alto);
	}

	public void dibujar(Graphics g) {
		g.fillRect(x, y, ancho, alto);
	}
}
