import java.util.Random;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;

class BunnyManager{

    int WIDTH, HEIGHT;

    public ArrayList<Bunny>bunnyswarm = new ArrayList<Bunny>();
    public ArrayList<Bunny>deadbunnies = new ArrayList<Bunny>();
    public ArrayList<Integer>averagesuccess = new ArrayList<Integer>();
    public ArrayList<Integer>populationsize = new ArrayList<Integer>();
    public ArrayList<Integer>bestbunny = new ArrayList<Integer>();

    boolean infinatespace, finitespace;

    //first value is population second two are map dimensions last is the type of search space to use
    public BunnyManager( int startsize, int _w, int _h, boolean torroidal, Tile[][] map){

        WIDTH = _w;
        HEIGHT = _h;

        try{
            for (int i = 0; i < startsize; i++) {

                bunnyswarm.add( new Bunny( bunnyswarm.size(), 0 + (int)(Math.random() * WIDTH), 0 + (int)(Math.random() * HEIGHT), 0, map));

                //add random angle
                bunnyswarm.get( bunnyswarm.size()-1).setAngle(0 + (Math.random() * 359));
            }

            if( torroidal){

                System.out.println("Torroidal search space selected");
                infinatespace = true;
            }else{

                System.out.println("Finite search space selected");
                finitespace = true;
            }
        }catch( Exception e){

            System.out.println(" bunny manager constructor has failed");
        }
    }

    //calculate centre of bunny mass
    public int[]centerOfBunnyMass(){

        int[] pos = new int[2];

        for (int i = 0; i < bunnyswarm.size(); i++) {

            pos[0] += bunnyswarm.get(i).getPosX();
            pos[1] += bunnyswarm.get(i).getPosX();
        }

        pos[0] = pos[0]/bunnyswarm.size();
        pos[1] = pos[1]/bunnyswarm.size();
        return pos;
    }


    //get the best bunny ( health + thirst + hunger)
    public Bunny getBestBunny(){

        Bunny temp = bunnyswarm.get(0);

        for (int i = 0; i < bunnyswarm.size();i++) {

            if( (bunnyswarm.get(i).health + bunnyswarm.get(i).thirst + bunnyswarm.get(i).hunger) >
                (temp.health + temp.thirst + temp.hunger)){

                temp = bunnyswarm.get(i);
            }
        }

        return temp;
    }

    //return bunnies back onto the search space by reversing their direction
    float returnBunny( Bunny bun){

        return Math.abs((int) (bun.getAngle() + 180) % 360);
    }

    int returnBunnyX( Bunny bun){

        if( bun.getPosX() > WIDTH){

            return 2;
        }else if( bun.getPosX() < 0){

            return WIDTH;
        }else{

            return bun.getPosX();
        }
    }

    int returnBunnyY( Bunny bun){

        if( bun.getPosY() > HEIGHT -  bun.getHeight()){

            return 2;
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

                    return false;
                }
            }
        }

        return true;
    }

    //monitors average health of bunnies per round

    //keeps track of average bunny population size

    //stores lifetime size of best bunny

    //draws bunny statistic chart

    //draws individual bunny
    public void drawBunny( Graphics g, Bunny barry){

        g.drawImage( barry.nextFrame(), barry.getPosX(), barry.getPosY(), barry.getWidth(), barry.getHeight(), null);
    }

    //calls all vital bunny functions returns true when all bunnies are dead
    public boolean bunnyFunctions( Tile[][] tiles, Graphics g){

        g.setColor(Color.RED);
        g.drawRect(0,0,WIDTH,HEIGHT);
        try{

            for (int i = 0; i < bunnyswarm.size(); i++) {

                //bunny functions called on each bunny here
                bunnyswarm.get(i).updateVision( tiles);
                bunnyswarm.get(i).priorities();
                bunnyswarm.get(i).bunnyFatigue();
                drawBunny( g, bunnyswarm.get(i));
                bunnyswarm.get(i).pollConditions("ANGLE");
                bunnyswarm.get(i).displayState(g);

                if( offGrid(bunnyswarm.get(i), tiles)){
                    if( finitespace){

                        bunnyswarm.get(i).setAngle( returnBunny( bunnyswarm.get(i)));
                    }else{


                        int bx = bunnyswarm.get(i).getPosX();
                        int by = bunnyswarm.get(i).getPosY();
                        if( bx < 0 || bx > WIDTH){

                            bx =  returnBunnyX(bunnyswarm.get(i));
                        }
                        if( by < 0 || by > HEIGHT){

                            by =  returnBunnyY(bunnyswarm.get(i));
                        }

                        bunnyswarm.get(i).setXY( bx, by);
                    }
                }

                //move after correction not before
                bunnyswarm.get(i).moveSprite();

                if(!bunnyswarm.get(i).alive){

                    deadbunnies.add( bunnyswarm.get(i));
                    bunnyswarm.remove(i);
                }
            }
        }catch( Exception e){

            System.out.println("a dead bunny caused an out of range error" + e.toString());
        }

        if( bunnyswarm.size() == 0){

            //           System.out.println("all bunnies dead");
            return true;
        }

        return false;
    }
}
