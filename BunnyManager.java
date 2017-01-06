import java.util.Random;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;

class BunnyManager{

    int WIDTH, HEIGHT, hungerdeaths, thirstdeaths, totalFoodConsumption;

    //previous bunny manager
    BunnyManager lastBM;

    public ArrayList<Bunny>bunnyswarm = new ArrayList<Bunny>();
    public ArrayList<Bunny>deadbunnies = new ArrayList<Bunny>();
    public ArrayList<Integer>averagesuccess = new ArrayList<Integer>();
    public ArrayList<Integer>populationsize = new ArrayList<Integer>();
    public ArrayList<Integer>bestbunny = new ArrayList<Integer>();

    boolean infinatespace, finitespace, extinct;

    //first value is population second two are map dimensions last is the type of search space to use
    public BunnyManager( int startsize, int _w, int _h, boolean torroidal, Tile[][] map){

	
        WIDTH = _w;
        HEIGHT = _h;
	
        try{
	    for (int i = 0; i < startsize; i++) {

	    	bunnyswarm.add( new Bunny( i, WIDTH, HEIGHT, map));      	    
	    }

            if( torroidal){

		//  System.out.println("Torroidal search space selected");
                infinatespace = true;
            }else{

		//  System.out.println("Finite search space selected");
                finitespace = true;
            }
        }catch( Exception e){

            System.out.println(" bunny manager constructor has failed: " + e.toString());
	}
	
    }

    //this constructor takes a previous constructor as an argument and finds the best bunnies to apply
    //genetic crossover with
    public BunnyManager( BunnyManager bm, Tile[][] map, int mutationrate){

	lastBM = bm;
	
	//avoid having a linked list going on too long
	lastBM.lastBM = null;
	WIDTH = bm.WIDTH;
	HEIGHT = bm.HEIGHT;

	try{
   
	    //this creates four bunnies from previous best bunnies by getting the best deadbunnies
	    //by their IDs and popping them out the deadbunnies array
	    
	    Bunny mother = popPastDeadBunny( bestPastLifeTimeID());
	    Bunny father = popPastDeadBunny( bestPastLifeTimeID());
	    
	    bunnyswarm.add(new Bunny( 0, mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( 1, father, mother, WIDTH, HEIGHT, map, mutationrate));
	  
	    mother = popPastDeadBunny( bestPastLifeTimeID());
	    father = popPastDeadBunny( bestPastLifeTimeID());

	    bunnyswarm.add(new Bunny( 2, mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( 3, father, mother, WIDTH, HEIGHT, map, mutationrate));

	    mother = popPastDeadBunny( bestPastLifeTimeID());
	    father = popPastDeadBunny( bestPastLifeTimeID());

	    bunnyswarm.add(new Bunny( 2, mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( 3, father, mother, WIDTH, HEIGHT, map, mutationrate));

	    mother = popPastDeadBunny( bestPastLifeTimeID());
	    father = popPastDeadBunny( bestPastLifeTimeID());

	    bunnyswarm.add(new Bunny( 2, mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( 3, father, mother, WIDTH, HEIGHT, map, mutationrate));

	    //and will add four more random bunnies to bring total to 16
	    for (int i = 0; i < 8; i++) {

		 bunnyswarm.add( new Bunny( bunnyswarm.size(), WIDTH, HEIGHT, map));
	    }
	   	  
	}catch(Exception e){

	    System.out.println(" Bunny manager failed: " + e.toString());
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

    //returns total amount of food consumed that round
    public int totalFood(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size(); i++) {

	    temp += deadbunnies.get(i).totalfoodconsumed;
	}

	return temp;
    }

    //returns total thirst related deaths that round
    public int totalThirstDeaths(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size(); i++) {

	    if( deadbunnies.get(i).diedofthirst){
		temp++;
	    }
	}

	return temp;
    }

    //returns total hunger related deaths that round
    public int totalHungerDeaths(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size(); i++) {

	    if( deadbunnies.get(i).diedofhunger){
		
		temp++;
	    }
	}

	return temp;
    }

    //returns true if an integer applies to a bunny that is still alive
    public boolean checkBunnyID( int id){

	for (int i = 0; i < bunnyswarm.size(); i++) {

	    if (bunnyswarm.get(i).bunnyID == id) {

		return true;
	    }
	}

	return false;
    }

    //returns lifetime size of best bunny
    public int bestLifeTime(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size() ; i++) {

	    if( deadbunnies.get(i).cycles > temp){

		temp = deadbunnies.get(i).cycles;
	    }
	}

	return temp;
    }

    //returns lifetime size of best bunny
    public int bestPastLifeTimeID(){

	int temp = 0;
	int id = 0;

	for (int i = 0; i < lastBM.deadbunnies.size() ; i++) {

	    if( lastBM.deadbunnies.get(i).cycles > temp){

		temp = lastBM.deadbunnies.get(i).cycles;
		id = lastBM.deadbunnies.get(i).bunnyID;
	    }
	}

	return id;
    }

    //removes a bunny from the deadbunny array list by ID
    public Bunny popPastDeadBunny( int id){

	Bunny temp = lastBM.deadbunnies.get(0);
	for (int i = 0; i < lastBM.deadbunnies.size(); i++) {

	    if ( lastBM.deadbunnies.get(i).bunnyID == id) {

		temp = lastBM.deadbunnies.get(i);
		lastBM.deadbunnies.remove(i);
		break;
	    }
    	}

	return temp;
    }

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
		bunnyswarm.get(i).showVision( g, tiles);

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
	    extinct = true;
            return true;
        }

        return false;
    }
}
