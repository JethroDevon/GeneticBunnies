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
	    buttons.add(new Sprite("maptype", "imgs/maptype.png", 1, 2));
	    buttons.add(new Sprite("random", "imgs/random.png", 1, 2));
	    buttons.add(new Sprite("genetic", "imgs/genetic.png", 1, 2));
	    buttons.add(new Sprite("breeding", "imgs/breeding.png", 1, 2));
	    buttons.add(new Sprite("hungry", "imgs/hungry.png", 1, 2));
	    buttons.add(new Sprite("foodscarcity", "imgs/scarcity.png", 1, 2));
	    buttons.add(new Sprite("mutation", "imgs/mutation.png", 1, 2));
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
    	}

	//given up setting the buttons relitivley, im going to do it manually instead
	buttons.get(0).setXY( (WIDTH/2) - (buttons.get(0).getWidth()/2), 50);
        buttons.get(2).setXY( (WIDTH/2) - (buttons.get(2).getWidth()/2), 130);
        buttons.get(3).setXY( (WIDTH/2) - (buttons.get(3).getWidth()/2), 170);
        buttons.get(4).setXY( (WIDTH/2) - (buttons.get(4).getWidth()/2), 210);
	buttons.get(5).setXY( (WIDTH/2) - (buttons.get(5).getWidth()/2), 250);
	buttons.get(6).setXY( (WIDTH/2) - (buttons.get(6).getWidth()/2), 310);
	buttons.get(7).setXY( (WIDTH/2) - (buttons.get(7).getWidth()/2), 350);	
	buttons.get(8).setXY( (WIDTH/2) - (buttons.get(8).getWidth()/2), 480);
	buttons.get(1).setXY( (WIDTH/2) - (buttons.get(1).getWidth()/2), 380);

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
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("random")){

                        return 2;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("genetic")){

                        return 3;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("breeding")){

                        return 4;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("hungry")){

                        return 5;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("maptype")){

                        return 6;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("exit")){

                        return 7;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("foodscarcity")){

                        return 8;
                    }else  if( buttons.get(x).getName().equals("pointer") && buttons.get(y).getName().equals("mutation")){

                        return 9;
                    }
                }
            }
        }

        //returns minus one as an error message or null return
        return -1;
    }

}
