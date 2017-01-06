import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;
import java.awt.Graphics;
import java.awt.Graphics2D;

class Buttons{

    public Sprite LifeBunt, ThirstBunt, FoodBunt, HungerBunt;
    public boolean L, T, F, H;

    public Buttons( int WIDTH, int HEIGHT){

	try{

	    LifeBunt = new Sprite( "life", "imgs/life.png", 1, 1);
	    LifeBunt.setXY( WIDTH = LifeBunt.getWidth() - 10, - (LifeBunt.getHeight() + 10));
	    
	    HungerBunt = new Sprite( "hunger", "imgs/Hunger.png", 1, 1);
	    HungerBunt.setXY( WIDTH = LifeBunt.getWidth() - 10, 4 * - (LifeBunt.getHeight() + 10));

	    ThirstBunt = new Sprite( "thirst", "imgs/thirst.png", 1, 1);
	    ThirstBunt.setXY( WIDTH = LifeBunt.getWidth() - 10, 2 * - (LifeBunt.getHeight() + 10));

	    FoodBunt = new Sprite( "food", "imgs/Food.png", 1, 1);
	    FoodBunt.setXY( WIDTH = LifeBunt.getWidth() - 10, 3 * -(LifeBunt.getHeight() + 10));

	}catch(Exception e){

		System.out.println(" Button error");
	}	
    }

    public void drawButtons( Graphics g){

	g.drawImage( LifeBunt.getFrame(), LifeBunt.getPosX(), LifeBunt.getPosY(), LifeBunt.getWidth() , LifeBunt.getHeight(), null);
        g.drawImage( HungerBunt.getFrame(), HungerBunt.getPosX(), HungerBunt.getPosY(), HungerBunt.getWidth() , HungerBunt.getHeight(), null);
        g.drawImage( FoodBunt.getFrame(), FoodBunt.getPosX(), FoodBunt.getPosY(), FoodBunt.getWidth() , FoodBunt.getHeight(), null);
        g.drawImage( ThirstBunt.getFrame(), ThirstBunt.getPosX(), ThirstBunt.getPosY(), ThirstBunt.getWidth() , ThirstBunt.getHeight(), null);
    }	    
}
