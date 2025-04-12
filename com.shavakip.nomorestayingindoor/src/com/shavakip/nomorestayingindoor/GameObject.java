package com.shavakip.nomorestayingindoor;

import java.awt.Graphics;

public abstract class GameObject {
	protected Position position;
	protected boolean present = true; // default: object is present


	public GameObject(Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public boolean isPresent() {
		return present;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public void update(float deltaTime) {
		// Default: do nothing
	}

	public void render(Graphics g, Camera camera) {
		// Default: do nothing
	}
}
