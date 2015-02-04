import java.applet.*;
import java.net.*;

public class Sound {
		  private AudioClip song; // Sound player
		  private URL songPath; // Sound path
		  Sound(String filename) {
		     try {
		    	 songPath = new URL("file:"+filename); // Get the Sound URL
		    	 song = Applet.newAudioClip(songPath); // Load the Sound
		     }
		     catch(Exception e){}
		  }
		  public void playSound() {
		     song.loop(); // Play
		  }
		  public void stopSound() {
		     song.stop(); // Stop
		  }
		  public void playSoundOnce() {
		     song.play(); // Play only once
		  }
	}