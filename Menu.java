
import java.util.ArrayList;
import java.awt.Graphics;


public class Menu{

    //array of button objects for menu
    public ArrayList<Sprite> buttons = new ArrayList<Sprite>();

    int WIDTH, HEIGHT;

    //create player sprite here, throws an exception as base class also does
    public Menu(int _width, int _height) throws Exception{

        WIDTH = _width;
        HEIGHT = _height;

        //sprites are created in try catch blocks
        try{

            //first thing to happen base class must be initialised
            buttons.add(new Sprite("run", "imgs/run.png", 1, 2));
	    buttons.add(new Sprite("exit", "imgs/exit.png", 1, 2));
	    buttons.add(new Sprite("pointer", "imgs/pointer.png", 1, 2));
            initMenu();

        }catch(Exception e){

            System.out.println("Error in Menu constructor: " + e.toString());
        }
    }

    //loops for each button initialising its y position and drawing at same x margin
    private void initMenu(){

    	for( int x = 0; x < buttons.size() ; x++){

            //they are a little big, half the size
            buttons.get(x).setWH( buttons.get(x).getWidth()/2, buttons.get(x).getHeight() / 2);

             //sets the positions relativley to the screen, draws them a little apart also to avoid pointer clicking two at once, that distance is calculated relativley
             buttons.get(x).setXY( (WIDTH/2) - (buttons.get(x).getWidth()/2), HEIGHT/60 + (buttons.get(x).getHeight() + HEIGHT/10 ) * (1 + x) );

    	}

    }

    public void drawMenu( Graphics gr2){

        for( Sprite x : buttons){
            for( Sprite y : buttons){

                if( x.checkCollision( y)){

                    gr2.drawImage( x.getFrame( 1), x.getPosX(), x.getPosY(), x.getWidth(), x.getHeight()-1, null);
                }else{

		      gr2.drawImage( x.getFrame( 0), x.getPosX(), x.getPosY(), x.getWidth(), x.getHeight()-1, null);
                }
            }
        }
    }

    public int getButton(){

        for( int x = 0; x < buttons.size(); x++){
            for( int y = 0; y < buttons.size(); y++){

                if( buttons.get(x).checkCollision( buttons.get(y))){

                    if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("run")){

                        return 1;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("credits")){

                        return 5;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("exit")){

                        return 6;
                    }
                }
            }
        }

        //returns minus one as an error message or null return
        return -1;
    }

}
