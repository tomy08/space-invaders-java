import java.awt.*;

public class Bala {
	public int x, y;
	public int ancho = 4, alto = 10;
	public int velocidad = 8;

	public Bala(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void mover() {
		y -= velocidad;
	}

	public void dibujar(Graphics g) {
		g.setColor(Color.YELLOW);
		g.fillRect(x, y, ancho, alto);
	}

	public Rectangle getRect() {
		return new Rectangle(x, y, ancho, alto);
	}
}