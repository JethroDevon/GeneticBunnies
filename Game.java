import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.MouseInfo;
import java.awt.event.KeyListener;
import java.awt.Image;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import javax.imageio.ImageIO;

/*

  This is an environment to run the sprite class and manage
  Program logic within, Important parts of the Genetic algorithm
  will be surrounded with a box of "////" and a number so that these
  parts can be refered to in a report.

*/

public class Game extends Canvas implements Runnable{

    //screen size variables initialised in constructor
    private int WIDTH, HEIGHT, mouseX, mouseY, maptype, foodscarcity;

    //thses two objects are the key lelements of the clipping blitting method
    private BufferStrategy bs;
    private Graphics graphics;
    private BunnyManager bmanager;

    //Menu class object
    Menu options;

    //Tiles will display nodes in simulation
    Tiles tiles;

    //Graph objects overlay statistics based on the bunny simulation
    Graphs BunnyLifeTime, WaterConsumption, FoodConsumption, ThirstDeaths, HungerDeaths, TotalLife, TotalMaturities, Births;

    //flag to end menu options
    boolean startFlag, menu, credits, simulation, L, H, T, F, X, M, B, random, genetic, breeding;

    //this stores the last up down left or right arrow to be pressed   //as lUP, lDown, lRight, lLeft,
    //or if the arrows are activley being pressed it stores
    //UP DOWN LEFT or RIGHT
    //starts with none as default
    private String direction = "none";

    //some operations on the program are detected here, escape will
    //set menu mode to true and pause the program
    private String operation = "none";

    //this stores the last key pressed default is dash
    private char lastPressed = '-';

    //this stores a typed keycode, it is returned with get typed,
    //default is zero
    private int typed = 0, round = 0;

    //this will store the background, an image loading class
    //will be used in the future, for now, the image
    //is initialised in the constructor, next is the image for the
    //background of the menu
    public Image background, menuBackground, creditsImage;

    //constructor for game class
    public Game( int _W, int _H){

	WIDTH = _W;
        HEIGHT = _H;
	
        //the game has been started
        startFlag = true;

        //adding key and mouse listener class to this window
        this.addMouseListener( new mouselisten());
        this.addMouseMotionListener( new mouseMotion());
        this.addKeyListener( new keyListen());
        this.setFocusable( true);

        //allows the buffer and window to be sized to args
        Dimension size = new Dimension( WIDTH, HEIGHT);
        setPreferredSize(size);

        //setting menu flag to default true,
        //this starts draw in menu mode
        menu = true;

	//initialise all graphs
        TotalLife = new Graphs( Color.WHITE, "TOTAL LIFETIMES", "", "", 10, 0, 500, 300);
	BunnyLifeTime = new Graphs( Color.YELLOW, "BUNNY LIFETIME", "", "", 10, 10, 500, 300);
	FoodConsumption = new Graphs( Color.GREEN, "FOOD CONSUMED", "", "", 10, 20, 500, 300);
	ThirstDeaths = new Graphs( Color.CYAN, "THIRST DEATHS", "", "", 10, 30, 500, 300);
	HungerDeaths = new Graphs( Color.ORANGE, "HUNGER DEATHS", "", "", 10, 40, 500, 300);
	TotalMaturities = new Graphs( Color.magenta, "MATURITIES", "", "", 10, 50, 500, 300);
	Births = new Graphs( Color.pink, "BIRTHS", "", "", 10, 60, 500, 300);
	
	try{

	    //initialise menu object bunny object and Tiles objects
	    options = new Menu( WIDTH, HEIGHT);	
            tiles = new Tiles( 10, 10, maptype, foodscarcity);
            bmanager = new BunnyManager( 16, tiles.getWidth(), tiles.getHeight(), true, tiles.tiles);
	         
            //initialise the bunny manager object with number of bunnies, screen bounds and finite or infinate search space
	    menuBackground = ImageIO.read( new File( "imgs/background.png"));

        }catch( Exception e){

            System.out.println( "Game failed to initialise correctly.");
        }
    }


    private void drawGame(){

        //manages multiple drawing to buffer
        bs = getBufferStrategy();

        //try catch block
        try {

            //if buffer is not initialised creates new buffer to draw over
            if( bs == null){

                //use one or two layers to buffer sprites if needed
                createBufferStrategy(2);

                //returns buffer to canvas for draw in jpanel
                return;
            }

            //initialises graphics with drawable objects from this class
            graphics = bs.getDrawGraphics();

        }catch(Exception e){

            System.out.println("Error in draw function of Game: " + e.toString());
        }

	//draw background and info
	graphics.setColor(Color.BLACK);
	graphics.fillRect( 0, 0, WIDTH, HEIGHT);
	graphics.setColor(Color.WHITE);

	graphics.drawString( "ROUND " + round, WIDTH - 115, 50);
	
	if (round < 2) {

	    graphics.drawString( "Graphs available after", WIDTH - 150, 100);
	    graphics.drawString( "round 2", WIDTH - 130, 113);
	}else{

	    graphics.drawString( "Press L F T H X M B", WIDTH - 150, 100);
	    graphics.drawString( "for graphs", WIDTH - 130, 113);
	}
	
        graphics.drawString( "Press numbers 1 - 9", WIDTH - 150, 135);
	graphics.drawString( "Bunny vision fields", WIDTH - 145, 147);
	graphics.drawString( "Hit ESC for menu", WIDTH - 140, 230);

	//draw display
	tiles.drawGrid(graphics);
        tiles.showFood(graphics, bmanager.bunnyswarm, bmanager.deadbunnies);
	graphics.drawImage( options.buttons.get(7).getFrame(), mouseX, mouseY, 20,20, null);
	
	//starts a new game after recording importand bunny data
	if( bmanager.bunnyFunctions(tiles.tiles, graphics)){

	    System.out.println();
	    System.out.println( "New round" );
	    System.out.println();
	    BunnyLifeTime.addEntry( bmanager.bestLifeTime());
	    HungerDeaths.addEntry( bmanager.totalHungerDeaths());
	    ThirstDeaths.addEntry( bmanager.totalThirstDeaths());
	    FoodConsumption.addEntry( bmanager.totalFood());
	    TotalLife.addEntry( bmanager.totalLife());
	    TotalMaturities.addEntry( bmanager.totalMaturities());
	    Births.addEntry( bmanager.totalBirths());
	    bmanager = new BunnyManager( bmanager, tiles.tiles, 30, breeding);
	    round ++;
	    
	    try{
		
		tiles = new Tiles( 10, 10, maptype, foodscarcity);
	    }catch( Exception e){

		System.out.println(e.toString());
	    }
	}

	//directional keys and space to show graphs
	if( direction == "UP" || L){

	    BunnyLifeTime.drawGraph( graphics);	   
	}
	if ( direction == "LEFT" || H) {

	    HungerDeaths.drawGraph( graphics);
	}
	if ( direction == "RIGHT" || T) {

	    ThirstDeaths.drawGraph( graphics);
	}
	if ( direction == "DOWN" || F) {

	    FoodConsumption.drawGraph( graphics);
	}
	if (direction == "JUMP" || X) {

	    TotalLife.drawGraph( graphics);
	}
        if (M) {

	    TotalMaturities.drawGraph( graphics);
	}
        if (B) {

	    Births.drawGraph( graphics);
	}

        //shows image from buffer
        bs.show();

        //clears graphics object once has been drawn to buffer to save memory leak
        graphics.dispose();

	//Synchronises drawring on the screen for smoother
        //graphics bliting, try commenting out to see difference -
        //its not so nice.
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawMenu(){

        //manages multiple drawing to buffer
        bs = getBufferStrategy();

        //try catch block
        try {

            //if buffer is not initialised ccreates new buffer to draw over
            if(bs == null){

                //two layers to buffer
                createBufferStrategy(1);

                //returns buffer to canvas for draw
                return;
            }

            //initialises graphics with drawable objects from this class
            graphics = bs.getDrawGraphics();

            ///////////////////////////////////////////////////////
            //                                                   //
            //          MENU FUNCTIONS AND UPDATES HERE          //
            //                                                   //
            ///////////////////////////////////////////////////////

            graphics.drawImage( menuBackground, 0, 0, WIDTH, HEIGHT, null);

            //sets last element of buttons array (an image of a mouse pointer) to be at same position as mouse x and y
            options.buttons.get( options.buttons.size()-1).setXY( mouseX - options.buttons.get(options.buttons.size()-1).getWidth()/2, mouseY - options.buttons.get(options.buttons.size()-1).getHeight()/2);

            //draws the menu buttons
            options.drawMenu( graphics);

            //shows image from buffer
            bs.show();

            //clears graphics object once has been drawn to buffer to save memory leak
            graphics.dispose();

        }catch(Exception e){

            System.out.println("Error in draw function of Menu: " + e.toString());
        }

        //Synchronises drawring on the screen with for smoother graphics bliting, try commenting out to see difference, seems
        //as though frames being drawn evenly in time but not without.
        Toolkit.getDefaultToolkit().sync();
    }////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////end of the menu and game functions \\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //\\//\\///\\///\\//\\///\\///\\///\\\////\\\\/////\\\\\/////\\\///\\\///\\///\\//\\///\\

    private void drawCredits(){

        //manages multiple drawing to buffer
        bs = getBufferStrategy();

        //try catch block
        try {

            //if buffer is not initialised ccreates new buffer to draw over
            if(bs == null){

                //two layers to buffer
                createBufferStrategy(2);

                //returns buffer to canvas for draw
                return;
            }

            //initialises graphics with drawable objects from this class
            graphics = bs.getDrawGraphics();


            graphics.drawImage( creditsImage, 0, 0, WIDTH, HEIGHT, null);

            //shows image from buffer
            bs.show();

            //clears graphics object once has been drawn to buffer to save memory leak
            graphics.dispose();

        }catch(Exception e){

            System.out.println("Error in draw function of Menu: " + e.toString());
        }

        //Synchronises drawring on the screen with for smoother graphics bliting, try commenting out to see difference, seems
        //as though frames being drawn evenly in time but not without.
        Toolkit.getDefaultToolkit().sync();
    }

    //run function can run in own thread, draws each frame
    public void run(){

        while(true){

            if( menu){

                //calls draw function of menu
                drawMenu();

                //resets operation to none, so that hitting escape can
                //bring up menu again
                operation = "NONE";
            }else if( credits){

                drawCredits();
                operation = "NONE";
            }else{

                drawGame();
                operation = "NONE";
            }
            try{

                //temporary delay, check if it is relative to
                //CPU speed of all users computer
                Thread.sleep(40);
            }catch(Exception e){

                System.out.println("error in main game thread");
            }
        }
    }

    //returns key code of last key that was typed
    public int getTyped(){

        return typed;
    }

    //returns last arrow key, if it is being pressed it will be UP, if it is not being pressed and the last key to be pressed was up it will return lUP or lRIGHT etc
    public String getDirection(){

        return direction;
    }

    //returns last key press in for of char pressed
    public char getPressed(){

        return lastPressed;
    }


    //Key press and mouse functions
    class mouselisten implements MouseListener {

        public void mouseClicked(MouseEvent e) {

	    //add a write to menu function one day
            if(options.getButton() == 1 && startFlag){

                menu = false;		
                credits = false;
                startFlag = false;
                simulation = true;
		
                System.out.println( "Starting Simulation");
            }else if( options.getButton() == 2 && startFlag){

                random = true;
	    	genetic = false;
	    	breeding = false;
                System.out.println( "Random Bunnies");
            }
	    else if( options.getButton() == 3 && startFlag){

                random = false;
	    	genetic = true;
	    	breeding = false;
                System.out.println( "Genetic Bunnies");
            }else if( options.getButton() == 4 && startFlag){

                random = false;
	    	genetic = false;
	    	breeding = true;
                System.out.println( "Breeding Bunnies");
            }else if( options.getButton() == 5 && startFlag){

                maptype++;

	        switch(maptype){
		case 0:

		    System.out.println("small watering hole");
		    break;

		case 1:

		    System.out.println("small large hole");
		    break;

		case 2:

		    System.out.println("two small watering holes");
		    break;
		  
	        case 3:

		    System.out.println("Bunny Island! :D");
		    break;
		}
		    
		if (maptype > 3) {

		    System.out.println("desert - no good for breeding bunnies");
		    maptype = 0;
		}
            }else if(options.getButton() == 6){

                System.out.println("Program deliberatley exited by user.");

                //potentially unclean exit but exit all the same,
                //if threads are present must kill threads
                //change if a network connection is implemented
                System.exit(0);
            }else if( options.getButton() == 7 && startFlag){

                foodscarcity++;

	        switch(foodscarcity){
		case 0:

		    System.out.println("scarce - 50 on half the tiles");
		    break;

		case 1:

		    System.out.println("50 per tile");
		    break;

		case 2:

		    System.out.println("100 per tile");
		    break;
		  
	        case 3:

		    System.out.println("150 per tile");
		    break;
		 case 4:

		    System.out.println("200 per tile");
		    break;
		 case 5:

		    System.out.println("250 per tile");
		    break;
		 case 6:

		    System.out.println("300 per tile");
		    break;
		}
		    
		if (foodscarcity > 6) {

		    System.out.println("scarce, 50 on half the tiles");
	            foodscarcity = 0;
		}
            }
        }

        public void mouseEntered(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub

        }

        public void mousePressed(MouseEvent e) {


        }

        public void mouseReleased(MouseEvent e) {


        }

    }

    class mouseMotion implements MouseMotionListener {

        public void mouseDragged( MouseEvent arg0) {
            // TODO Auto-generated method stub

        }

        public void mouseMoved( MouseEvent arg0) {

            mouseX = arg0.getX();
            mouseY = arg0.getY();
            //	System.out.println(mouseX + "  " + mouseY);
        }
    }


    //used oracle documentation for this keypress block -> https://docs.oracle.com/javase/tutorial/uiswing/events/keylistener.html
    public class keyListen extends KeyAdapter {

        public void keyPressed(KeyEvent e) {

            int id = e.getID();

            if(id == KeyEvent.KEY_TYPED){

                lastPressed = e.getKeyChar();

	    }else{

                int keyCode = e.getKeyCode();

                switch( keyCode ) {

		case KeyEvent.VK_0:

		    if( bmanager.checkBunnyID(0)){

			if( bmanager.bunnyswarm.get(0).selected){

			    bmanager.bunnyswarm.get(0).selected = false;
			}else{

			    bmanager.bunnyswarm.get(0).selected = true;
			}
		    }

		    break;
		    
		case KeyEvent.VK_1:

		    if( bmanager.checkBunnyID(1)){

			if( bmanager.bunnyswarm.get(1).selected){

			    bmanager.bunnyswarm.get(1).selected = false;
			}else{

			    bmanager.bunnyswarm.get(1).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_2:

		    if( bmanager.checkBunnyID(2)){

			if( bmanager.bunnyswarm.get(2).selected){

			    bmanager.bunnyswarm.get(2).selected = false;
			}else{

			    bmanager.bunnyswarm.get(2).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_3:

		    if( bmanager.checkBunnyID(3)){

			if( bmanager.bunnyswarm.get(3).selected){

			    bmanager.bunnyswarm.get(3).selected = false;
			}else{

			    bmanager.bunnyswarm.get(3).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_4:

		    if( bmanager.checkBunnyID(4)){

			if( bmanager.bunnyswarm.get(4).selected){

			    bmanager.bunnyswarm.get(4).selected = false;
			}else{

			    bmanager.bunnyswarm.get(4).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_5:

		    if( bmanager.checkBunnyID(5)){

			if( bmanager.bunnyswarm.get(5).selected){

			    bmanager.bunnyswarm.get(5).selected = false;
			}else{

			    bmanager.bunnyswarm.get(5).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_6:

		    if( bmanager.checkBunnyID(6)){

			if( bmanager.bunnyswarm.get(6).selected){

			    bmanager.bunnyswarm.get(6).selected = false;
			}else{

			    bmanager.bunnyswarm.get(6).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_7:

		    if( bmanager.checkBunnyID(7)){

			if( bmanager.bunnyswarm.get(7).selected){

			    bmanager.bunnyswarm.get(7).selected = false;
			}else{

			    bmanager.bunnyswarm.get(7).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_8:

		    if( bmanager.checkBunnyID(8)){

			if( bmanager.bunnyswarm.get(8).selected){

			    bmanager.bunnyswarm.get(8).selected = false;
			}else{

			    bmanager.bunnyswarm.get(8).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_9:

		    if( bmanager.checkBunnyID(9)){

			if( bmanager.bunnyswarm.get(9).selected){

			    bmanager.bunnyswarm.get(9).selected = false;
			}else{

			    bmanager.bunnyswarm.get(9).selected = true;
			}
		    }

		    break;

		case KeyEvent.VK_F:

		    if( !F){

			F = true;
		    }else{

			F = false;
		    }
		    System.out.println(" This chart will show food consumptions per round.");
		    break;

		
		case KeyEvent.VK_L:
		    if( !L){

			L = true;
		    }else{

			L = false;
		    }
		    System.out.println(" This chart will show the average life span of bunnies.");
		    break;

		
		case KeyEvent.VK_H:
		    if( !H){

			H = true;
		    }else{

			H = false;
		    }
		    System.out.println(" This chart will show total hunger deaths per round.");
		    break;

		
		case KeyEvent.VK_T:
		    if( !T){

			T = true;
		    }else{

			T = false;
		    }
		    System.out.println(" This chart will show total thirst deaths per round.");
		    break;

		case KeyEvent.VK_X:
		    if( !X){

			X = true;
		    }else{

			X = false;
		    }
		    System.out.println(" This chart will show the total life spans of all bunnies.");
		    break;

	        case KeyEvent.VK_M:
		    if( !M){

			M = true;
		    }else{

			M = false;
		    }
		    System.out.println(" This chart will show the total Maturities of all bunnies.");
		    break;

			case KeyEvent.VK_B:
		    if( !B){

			B = true;
		    }else{

			B = false;
		    }
		    System.out.println(" This chart will show the total Births.");
		    break;
	
                case KeyEvent.VK_UP:

                    direction = "UP";
                    break;

                case KeyEvent.VK_DOWN:

                    direction = "DOWN";
                    break;

                case KeyEvent.VK_LEFT:

                    direction = "LEFT";
                    break;

                case KeyEvent.VK_RIGHT :

                    direction = "RIGHT";
                    break;

                case KeyEvent.VK_SPACE :

                    operation = "JUMP";
                    break;

                case KeyEvent.VK_ESCAPE :

                    startFlag = true;
                    menu = true;
                    credits = false;

                    operation = "ESCAPE";
                    break;
                }
            }
        }

        public void keyReleased(KeyEvent e) {

            int keyCode = e.getKeyCode();

            switch( keyCode) {

            case KeyEvent.VK_UP:

                direction = "lUP";
                break;

            case KeyEvent.VK_DOWN:

                direction = "lDOWN";
                break;

            case KeyEvent.VK_LEFT:

                direction = "lLEFT";
                break;

            case KeyEvent.VK_RIGHT :

                direction = "lRIGHT";
                break;
            }
        }

        public void keyTyped(KeyEvent e) {

            typed = e.getKeyCode();
        }
    }

    //loads images from a file and returns a contact sheet
    public void loadImages( String _path, String _name, String _extension) {

        //get number of files in folder
        int _FILES = new File( _path).listFiles().length;

        //an array list of all all temporary Files
        ArrayList<BufferedImage> tmpimgar = new ArrayList<BufferedImage>();

        //stores total width of sheet
        int totalWidth = 0;

        ///stores height, this can be upgraded when loading from multiple sprite
        //image files later
        int height = 0;

        try {

            for( int x = 1; x < _FILES +1; x++) {

                String filename =  _path + _name + String.valueOf( x) + _extension;
                System.out.println( filename);
                BufferedImage temp = ImageIO.read( new File( filename));
                totalWidth += temp.getWidth();

                //uses the height from the bigest sprite
                if( height < temp.getHeight()){

                    height =  temp.getHeight();
                }

                //adds the temp image to the array list
                tmpimgar.add( temp);
            }

            //would add total height in another loop here when the code is upgraded to
            //load from multiple files

            //creates a temporary buffered image of a set size based on file data
            BufferedImage img = new BufferedImage( totalWidth, height, BufferedImage.TYPE_INT_ARGB);
            Graphics grd = (Graphics) img.getGraphics();

            //pastes sub images onto img
            for( int x = 0; x < tmpimgar.size(); x++){

                //y value is zero for now but this function may be altered to make a contact sheet out of all sub files
                grd.drawImage( tmpimgar.get(x), x * totalWidth / tmpimgar.size(), 0, null);
            }

            //saves the image as the name of hte images without hte numbers before them
            File output = new File( _name + _extension);

            //writes the file into existence
            ImageIO.write( img, _extension.substring( 1), output);

            System.out.println( img.getWidth());
        }catch (Exception e) {

            System.out.println( "error in function game/ loadImages: " + e.getMessage());
        }
    }
}
