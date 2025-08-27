import java.awt.*;

public class Enemigo {
	public int x, y;
	public int ancho = 30, alto = 20;
	private int velocidad = 2;
	private int direccion = 1; // 1 derecha, -1 izquierda

	public Enemigo(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void mover() {
		x += velocidad * direccion;
		if (x <= 0 || x + ancho >= PanelJuego.ANCHO) {
			direccion *= -1;
			y += 20; // bajan un poco al rebotar
		}
	}

	public void dibujar(Graphics g) {
		g.setColor(Color.GREEN);
		g.fillRect(x, y, ancho, alto);
	}

	public Rectangle getRect() {
		return new Rectangle(x, y, ancho, alto);
	}
}