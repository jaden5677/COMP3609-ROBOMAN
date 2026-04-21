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

	private boolean active;		// to activate or deactivate behaviour

	private int a;			// semi-major axis of ellipse
	private int b;			// semi-minor axis of ellipse
	private int degree;
 
	private int saveX;		// x-coordinate of game entity before behaviour starts
	private int saveY;		// y-coordinate of game entity before behaviour starts

	public EllipseBehaviour (JPanel panel, AbstractEnemy entity, int a, int b) {

		this.panel = panel;
		this.entity = entity;
		this.a = a;
		this.b = b;
		active = false;		// behaviour is inactive by default

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

      		// Draws circle in anti-clockwise direction from 0. To 
      		// draw circle in clockwise direction, make radians negative.

      		//radians = radians * -1;  // multiply by -1 for clockwise movement

      		// Formula for ellipse is (x^2 / a^2) + (y^2 / b^2) = 1 (if centred at (0, 0))
      		// Putting x = r * cos (theta) and y = r * sin (theta) and solving for r: 
      		// r^2 = (a^2 * b^2) / (b^2 * cos (theta) + a^2 * sin (theta))

      		double cosSq, sinSq, denom, rSq, r;

      		cosSq = Math.cos (radians) * Math.cos (radians);
      		sinSq = Math.sin (radians) * Math.sin (radians);

      		denom = b * b * cosSq + a * a * sinSq;

      		rSq = (a * a * b * b) / denom;

      		r = Math.sqrt (rSq);

      		int x = (int) (r * Math.cos(radians));	// x-coordinate to move to
      		int y = (int) (r * Math.sin(radians));	// y-coordinate to move to

      		x = x + CENTRE_X + 5;
      		y = CENTRE_Y + 5 - y;

		entity.setX(x);
		entity.setY(y);
   }


   	public int getDegree() {
		return degree;
   	}
}