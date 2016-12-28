import java.util.Random;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.Graphics;

class BunnyManager{

    int WIDTH, HEIGHT;

    public ArrayList<Bunny>bunnyswarm = new ArrayList<Bunny>();
    public ArrayList<Bunny>deadbunnies = new ArrayList<Bunny>();
    public ArrayList<Integer>averagesuccess = new ArrayList<Integer>();
    public ArrayList<Integer>populationsize = new ArrayList<Integer>();
    public ArrayList<Integer>bestbunny = new ArrayList<Integer>();

    boolean infinatespace, finitespace;

    //first value is population second two are map dimensions last is the type of search space to use
    public BunnyManager( int startsize, int _w, int _h, boolean torroidal){

        WIDTH = _w;
        HEIGHT = _h;

        try{
            for (int i = 0; i < startsize; i++) {

                bunnyswarm.add( new Bunny( bunnyswarm.size(), 0 + (int)(Math.random() * WIDTH), 0 + (int)(Math.random() * HEIGHT), 0 + (int)Math.random() +9));

                //add random angle
                bunnyswarm.get( bunnyswarm.size()-1).setAngle(0 + (Math.random() * 359));
            }

            if( torroidal){

                infinatespace = true;
            }else{

                finitespace = true;
            }
        }catch( Exception e){

            System.out.println(" bunny manager constructor has failed");
        }
    }

    //return bunnies back onto the search space by reversing their diriection
    float returnBunny( Bunny bun){

        return Math.abs((int) (bun.getAngle() + 180) % 360);
    }

    int returnBunnyX( Bunny bun){

        if( bun.getPosX() > WIDTH){

            return 0;
        }else if( bun.getPosX() < 0){

            return WIDTH;
        }else{

            return bun.getPosX();
        }
    }

    int returnBunnyY( Bunny bun){

        if( bun.getPosY() > HEIGHT + bun.getHeight()){

            return 0;
        }else if( bun.getPosY() < 0){

            return HEIGHT;
        }else{

            return bun.getPosY();
        }
    }

    //checks if a bun is off grid
    boolean offGrid( Bunny bun, Tile[][] tiles){

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {

                if( bun.checkCollision( tiles[x][y])){

                    return true;
                }
            }
        }

        return false;
    }

    //monitors average health of bunnies per round

    //keeps track of averaage bunny population size

    //stores lifetime size of best bunny

    //draws bunny statistic chart

    //draws individual bunny
    public void drawBunny( Graphics g, Bunny barry){

        g.drawImage( barry.nextFrame(), barry.getPosX(), barry.getPosY(), barry.getWidth(), barry.getHeight(), null);
    }

    //calls all vital bunny functions
    public void bunnyFunctions( Tile[][] tiles, Graphics g){

        try{

            for (int i = 0; i < bunnyswarm.size(); i++) {

                bunnyswarm.get(i).updateVision( tiles);
                bunnyswarm.get(i).priorities();
                bunnyswarm.get(i).bunnyFatigue();
                drawBunny( g, bunnyswarm.get(i));
                bunnyswarm.get(i).pollConditions("ANGLE");
                bunnyswarm.get(i).moveSprite();

                if( offGrid(bunnyswarm.get(i), tiles)){
                    if( finitespace){
                    }else{

                        bunnyswarm.get(i).setXY( returnBunnyX(bunnyswarm.get(i)), returnBunnyY(bunnyswarm.get(i)) );
                    }
                }

                if(!bunnyswarm.get(i).alive){

                    deadbunnies.add( bunnyswarm.get(i));
                    bunnyswarm.remove(i);
                }else{

                    System.out.println("all bunnies dead");
                }
            }
        }catch( Exception e){

            System.out.println("a dead bunny caused an out of range error" + e.toString());
        }
    }
}
