package Behaviours;

import java.awt.Graphics2D;

public interface Behaviour {
	public boolean isActive();
	public void activate();
	public void deActivate();
	public void update();

}