package Behaviours;

import javax.swing.JPanel;

import Entities.Enemies.AbstractEnemy;

public class EllipseBehaviour implements Behaviour {

	private static int START = 0;
	private static int END = 360;

	private static int CENTRE_X = 150;
	private static int CENTRE_Y = 100;

	private JPanel panel;

	private AbstractEnemy entity;

	private boolean active;

	private int a;
	private int b;
	private int degree;

	private int saveX;
	private int saveY;

	public EllipseBehaviour (JPanel panel, AbstractEnemy entity, int a, int b) {

		this.panel = panel;
		this.entity = entity;
		this.a = a;
		this.b = b;
		active = false;

		degree = START;
	}

	public boolean isActive() {
		return active;
	}

	public void activate() {
		active = true;
		saveX = entity.getX();
		saveY = entity.getY();
	}

	public void deActivate() {
		active = false;
		entity.setX(saveX);
		entity.setY(saveY);
	}

	public void update () {

		if (!active || !panel.isVisible ())
			return;

		degree = degree + 10;
		if (degree > END)
		degree = START;

		System.out.println("Degree = " + degree);

		double radians = (degree / 180.0) * Math.PI;

		double cosSq, sinSq, denom, rSq, r;

		cosSq = Math.cos (radians) * Math.cos (radians);
		sinSq = Math.sin (radians) * Math.sin (radians);

		denom = b * b * cosSq + a * a * sinSq;

		rSq = (a * a * b * b) / denom;

		r = Math.sqrt (rSq);

		int x = (int) (r * Math.cos(radians));
		int y = (int) (r * Math.sin(radians));

		x = x + CENTRE_X + 5;
		y = CENTRE_Y + 5 - y;

		entity.setX(x);
		entity.setY(y);
   }

	public int getDegree() {
		return degree;
	}
}