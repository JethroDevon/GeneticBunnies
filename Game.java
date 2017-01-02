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
    private int WIDTH, HEIGHT, mouseX, mouseY;

    //thses two objects are the key lelements of the clipping blitting method
    private BufferStrategy bs;
    private Graphics graphics;
    private BunnyManager bmanager;

    //Menu class object
    Menu options;

    //Tiles will display nodes in simulation
    Tiles tiles;

    //Bunny object, the bunny is called barry
    Bunny barry;

    //flag to end menu options
    boolean startFlag, menu, credits, simulation;

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
    private int typed = 0;

    //this will store the background, an image loading class
    //will be used in the future, for now, the image
    //is initialised in the constructor, next is the image for the
    //background of the menu
    public Image background, menuBackground, creditsImage;

    //constructor for game class
    public Game( int _W, int _H){

        //the game has been started
        startFlag = true;

        WIDTH = _W;
        HEIGHT = _H;
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

        try{

            //initialise menu object bunny object and Tiles objects
            options = new Menu( WIDTH, HEIGHT);
            tiles = new Tiles( 10, 10);

            //initialise the bunny manager object with number of bunnies, screen bounds and finite or infinate search space
            bmanager = new BunnyManager( 4, tiles.getWidth(), tiles.getHeight(), true, tiles.tiles);

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

            //if buffer is not initialised ccreates new buffer to draw over
            if( bs == null){

                //use one or two layers to buffer sprites if needed
                createBufferStrategy(1);

                //returns buffer to canvas for draw in jpanel
                return;
            }

            //initialises graphics with drawable objects from this class
            graphics = bs.getDrawGraphics();

        }catch(Exception e){

            System.out.println("Error in draw function of Game: " + e.toString());
        }

        //Sprite operations
        graphics.fillRect( 0, 0, WIDTH, HEIGHT);
        tiles.drawGrid(graphics);
        bmanager.bunnyFunctions(tiles.tiles, graphics);
        tiles.showFood(graphics, bmanager.bunnyswarm, bmanager.deadbunnies);

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
                createBufferStrategy(2);

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
                createBufferStrategy(3);

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

            if(options.getButton() == 1 && startFlag){

                menu = false;
                credits = false;
                startFlag = false;
                simulation = true;
                System.out.println( "Starting Simulation");
            }else if( options.getButton() == 5 && startFlag){

                credits = true;
                startFlag = false;
                System.out.println( "Credits");
            }else if(options.getButton() == 6){

                System.out.println("Program deliberatley exited by user.");

                //potentially unclean exit but exit all the same,
                //if threads are present must kill threads
                //change if a network connection is implemented
                System.exit(0);
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
