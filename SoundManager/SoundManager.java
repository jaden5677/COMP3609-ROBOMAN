package SoundManager;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;

public class SoundManager {
	HashMap<String, Clip> clips;

	private static SoundManager instance = null;

	private float volume;

	private SoundManager () {

		Clip clip;

		clips = new HashMap<String, Clip>();

		clip = loadClip("BulletFire.wav");
		clips.put("BulletFire", clip);

		clip = loadClip("BulletHit.wav");
		clips.put("BulletHit", clip);

        clip = loadClip("GrassWalking.wav");
		clips.put("GrassWalking", clip);
        clip = loadClip("Level1Ambience.wav");
		clips.put("Level1Ambience", clip);
        clip = loadClip("MetalWalking.wav");
		clips.put("MetalWalking", clip);
        clip = loadClip("PickUp.wav");
		clips.put("PickUp", clip);
        clip = loadClip("PlayerDamage.wav");
		clips.put("PlayerDamage", clip);
        clip = loadClip("PowerUp.wav");
		clips.put("PowerUp", clip);
        clip = loadClip("RobomanBombExplosion.wav");
		clips.put("RobomanBombExplosion", clip);

		volume = 1.0f;
	}

	public static SoundManager getInstance() {
		if (instance == null)
			instance = new SoundManager();

		return instance;
	}

	public Clip loadClip (String fileName) {
		AudioInputStream audioIn;
		Clip clip = null;

		try {
			File file = new File(fileName);
			audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		}
		catch (Exception e) {
			System.out.println ("Error opening sound files: " + e);
		}
		return clip;
	}

	public Clip getClip (String title) {

		return clips.get(title);
	}

	public void playClip(String title, boolean looping) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
	}

	public void stopClip(String title) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.stop();
		}
	}

}