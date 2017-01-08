import java.util.Random;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;

class BunnyManager{

    int WIDTH, HEIGHT, hungerdeaths, thirstdeaths, totalFoodConsumption, mutationrate;

    //last bunny manager
    BunnyManager lastBM;

    public ArrayList<Bunny>bunnyswarm = new ArrayList<Bunny>();
    public ArrayList<Bunny>deadbunnies = new ArrayList<Bunny>();
    public ArrayList<Integer>averagesuccess = new ArrayList<Integer>();
    public ArrayList<Integer>populationsize = new ArrayList<Integer>();
    public ArrayList<Integer>bestbunny = new ArrayList<Integer>();

    boolean infinatespace, finitespace, extinct, breeding;

    Bunny breeder;

    Tile[][] map;

    //the bunny IDs cant go backwards :/
    int bun_tracker;

    //first value is population second two are map dimensions last is the type of search space to use
    public BunnyManager( int startsize, int _w, int _h, boolean torroidal, Tile[][] _map){

        WIDTH = _w;
        HEIGHT = _h;
	map = _map;
	
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

	bun_tracker = bunnyswarm.size()+1;
    }

    //this constructor takes a previous constructor as an argument and finds the best bunnies to apply
    //genetic crossover with
    public BunnyManager( BunnyManager bm, Tile[][] _map, int _mutationrate, boolean _breeding){

	lastBM = bm;
	mutationrate = _mutationrate;
	map = _map;

	//enables or disables breeding
	breeding = _breeding;
	
	//avoid having a linked list going on too long
	lastBM.lastBM = null;
	WIDTH = bm.WIDTH;
	HEIGHT = bm.HEIGHT;

	//the following block is written in an ugly scripted way because a lot of experimentation goes on here
	try{

	    ArrayList<Bunny> specialbuns = new ArrayList<Bunny>();
	    
	    //adds each bunny that gave birth to an array of bunnies for breeding in the next round
	    for (int i = 0; i < lastBM.deadbunnies.size(); i++) {

		int past = lastBirthsID();
		if (past != -1) {

		    specialbuns.add( popPastDeadBunny(past));
		}
            }

	    System.out.println(" pregnancies last round:" + specialbuns.size());

	    if( specialbuns.size() > 1){

		System.out.println( specialbuns.size() + " special breeding bunnies selected");
		for (int i = 0; i < specialbuns.size(); i++) {
		    for (int x = 0; x < specialbuns.size(); x++) {

			if( i != x){

			    bunnyswarm.add(new Bunny( bunnyswarm.size(), specialbuns.get(i), specialbuns.get(x), WIDTH, HEIGHT, map, mutationrate));
			}
		    }
		}
	    }
	   
	    if (bunnyswarm.size() > 16) {

		bunnyswarm.clear();
		ArrayList<Bunny>elites = new ArrayList<Bunny>();
		
		//select the bunnies who have had the most births
		for (int i = 0; i < specialbuns.size(); i++) {

		    if (specialbuns.get(i).births > 1) {

			elites.add(specialbuns.get(i));
		    }
		}

		System.out.println( specialbuns.size() + "elite breeding bunnies selected");
		
		for (int i = 0; i < specialbuns.size(); i++) {
		    for (int x = 0; x < specialbuns.size(); x++) {

			if( i != x && elites.size() < 16){

			   elites.add(new Bunny( bunnyswarm.size(), specialbuns.get(i), specialbuns.get(x), WIDTH, HEIGHT, map, mutationrate));
			}
		    }
		}

		for (int i = 0; i < elites.size(); i++) {

		    bunnyswarm.add( elites.get(i));
		}
		
		for (int i = 0; i < specialbuns.size(); i++) {
		    for (int x = 0; x < specialbuns.size(); x++) {

			if( i != x && bunnyswarm.size() < 16){

			    bunnyswarm.add(new Bunny( bunnyswarm.size(), specialbuns.get(i), specialbuns.get(x), WIDTH, HEIGHT, map, mutationrate));
			}
		    }
		}
		
	    }

	    //algorithm starts just by keeping the bunnies that live the longest
	    if( bunnyswarm.size() < 16){

		//this creates four bunnies from previous best bunnies by getting the best deadbunnies
		//by their IDs and popping them out the deadbunnies array	    
		Bunny Elitemother = popPastDeadBunny( bestPastFoodConsumptionID());
		Bunny Elitefather = popPastDeadBunny( bestPastFoodConsumptionID());
	    
		bunnyswarm.add(new Bunny( bunnyswarm.size(), Elitemother, Elitefather, WIDTH, HEIGHT, map, mutationrate));
		bunnyswarm.add(new Bunny( bunnyswarm.size(), Elitefather, Elitemother, WIDTH, HEIGHT, map, mutationrate));
	  
		Bunny mother = popPastDeadBunny( bestPastLifeTimeID());
		Bunny father = popPastDeadBunny( bestPastLifeTimeID());

		bunnyswarm.add(new Bunny( bunnyswarm.size(), mother, father, WIDTH, HEIGHT, map, mutationrate));
		bunnyswarm.add(new Bunny( bunnyswarm.size(), father, mother, WIDTH, HEIGHT, map, mutationrate));

		mother = popPastDeadBunny( bestPastLifeTimeID());
		father = popPastDeadBunny( bestPastLifeTimeID());

		bunnyswarm.add(new Bunny( bunnyswarm.size(), mother, father, WIDTH, HEIGHT, map, mutationrate));
		bunnyswarm.add(new Bunny( bunnyswarm.size(), father, mother, WIDTH, HEIGHT, map, mutationrate));
	    }
	   	 
	    int bunnynum = bunnyswarm.size() + 1;

	    if( bunnyswarm.size() < 16){
		
		//elite bunnies but with a much higher mutation rate
		for (int i = 0; i < 4; i++) {
		    for (int a = 0; a < 4; a++) {
			
			bunnyswarm.add(new Bunny( bunnynum, bunnyswarm.get(i), bunnyswarm.get(a), WIDTH, HEIGHT, map, mutationrate * 4));
			bunnynum++;
		    }
		}	  
	    }

	    //must remove identialcal bunnies
	    for (int i = 0; i < bunnyswarm.size(); i++) {
		for (int q = 0; q < bunnyswarm.size(); q++) {
		    if (i != q) {
			if ( compareArray( bunnyswarm.get(i).option_chromosome, bunnyswarm.get(q).option_chromosome) &&
			    compare2DArray(  bunnyswarm.get(i).vision_chromosome,  bunnyswarm.get(q).vision_chromosome)) {

			    //  System.out.println("Duplicate bunny "+ bunnyswarm.get(q).bunnyID +" removed");
			    bunnyswarm.remove(q);
			}			
		    }
		}
	    }

	    bun_tracker = bunnyswarm.size();
	   	  
	}catch(Exception e){

	    System.out.println(" Bunny manager failed: " + e.toString());
	}
    }

    boolean compareArray(int[] A, int[] B){

	if ( A.length != B.length) {

	    return false;
	}

	for (int i = 0; i < A.length; i++) {

	    if (A[i]!=B[i]) {

		return false;
	    }
	}

	return true;
    }

    boolean compare2DArray(int[][] A, int[][] B){

	if ( (A.length != B.length) || (A[A.length-1].length != B[B.length-1].length)) {

	    return false;
	}

	for (int i = 0; i < A.length; i++) {
	    for (int d = 0; d < B[B.length-1].length; d++) {
		
		if (A[i][d]!=B[i][d]) {

		    return false;
		}
	    }
	}

	return true;
    }

    //calculate centre of bunny mass
    public int[] centerOfBunnyMass(){

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
    public int totalLife(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size() ; i++) {	  

	    temp += deadbunnies.get(i).cycles;
	}

	return temp;
    }

    //returns the bunny that ate the most food
    public int bestPastFoodConsumptionID(){

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

    //returns the bunny that ate the most food
    public int lastBirthsID(){

	int id = 0;

	for (int i = 0; i < lastBM.deadbunnies.size() ; i++) {

	    if( lastBM.deadbunnies.get(i).births > 0){

		return lastBM.deadbunnies.get(i).bunnyID;
	    }
	}

	return -1;
    }

     public int lastMaturedID(){

	int id = 0;

	for (int i = 0; i < lastBM.deadbunnies.size() ; i++) {

	    if( lastBM.deadbunnies.get(i).matured){

		return lastBM.deadbunnies.get(i).bunnyID;
	    }
	}

	return -1;
    }

    public int totalMaturities(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size(); i++) {

	    if (deadbunnies.get(i).matured) {

		temp++;
	    }
	   
	}

	return temp;
    }

    public int totalBirths(){

	int temp = 0;

	for (int i = 0; i < deadbunnies.size(); i++) {

	    temp += deadbunnies.get(i).births;
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

    Bunny assignNearest( Bunny startbun){

	Bunny temp = null;
        double distance = 99999;
	
	for (int i = 0; i < bunnyswarm.size(); i++) {

	    if( distance > startbun.getDistance( bunnyswarm.get(i)) && bunnyswarm.get(i).bunnyID != startbun.bunnyID ) {

		temp = bunnyswarm.get(i);
		distance = startbun.getDistance( bunnyswarm.get(i));
	    }
	}
	
	return temp;
    }

    public boolean isSuitorDead( Bunny hopefull){

	if (hopefull.suitor == null) {

	    return true;
	}
	
	for (int i = 0; i < deadbunnies.size(); i++) {

	    if ( deadbunnies.get(i).bunnyID == hopefull.suitor.bunnyID) {

		return true;
	    }
	}

	return false;
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
		breeder = bunnyswarm.get(i).breedcalculate();
                bunnyswarm.get(i).priorities( bunnyswarm);
                bunnyswarm.get(i).bunnyFatigue();
                drawBunny( g, bunnyswarm.get(i));
                bunnyswarm.get(i).pollConditions("ANGLE");
                bunnyswarm.get(i).displayState(g);
		bunnyswarm.get(i).showVision( g, tiles);
		bunnyswarm.get(i).nearestBunny = assignNearest( bunnyswarm.get(i));

		//if the bunny has chosen a potential father for breeding make sure he is not dead
		if (isSuitorDead( bunnyswarm.get(i))) {

		    bunnyswarm.get(i).suitor = null;
		}	

                if( offGrid(bunnyswarm.get(i), tiles)){
                    if( finitespace){

                        bunnyswarm.get(i).setAngle( returnBunny( bunnyswarm.get(i)));
                    }else{


                        int bx1 = bunnyswarm.get(i).getPosX();
                        int by1 = bunnyswarm.get(i).getPosY();
			
                        if( bx1 < 0){

                            bx1 = WIDTH;
                        }
                        if( by1 < 0 ){

                            by1 =  HEIGHT;
                        }
			if( bx1 > WIDTH){

                            bx1 = 0;
                        }
                        if( by1 > HEIGHT ){

                            by1 =  0;
                        }

                        bunnyswarm.get(i).setXY( bx1, by1);
                    }
                }

                //move after correction not before
                bunnyswarm.get(i).moveSprite();

		//handle breeding here to avoid out of range errors
		if (breeder != null && breeding) {

		    bunnyswarm.add(new Bunny( bun_tracker++, bunnyswarm.get(i), breeder, WIDTH, HEIGHT, map, mutationrate));
		    bunnyswarm.get( bunnyswarm.size()).setAngle( bunnyswarm.get(i).getAngle());
		    bunnyswarm.get(  bunnyswarm.size()).setXY( bunnyswarm.get(i).getPosX(), bunnyswarm.get(i).getPosY());

		    bunnyswarm.add(new Bunny( bun_tracker++, breeder, bunnyswarm.get(i), WIDTH, HEIGHT, map, mutationrate));
		    bunnyswarm.get( bunnyswarm.size()).setAngle( bunnyswarm.get(i).getAngle());
		    bunnyswarm.get(  bunnyswarm.size()).setXY( bunnyswarm.get(i).getPosX(), bunnyswarm.get(i).getPosY());

		    System.out.println( "BABIES!!");
		}

                if(!bunnyswarm.get(i).alive){

                    deadbunnies.add( bunnyswarm.get(i));
                    bunnyswarm.remove(i);
                }
            }
        }catch( Exception e){

            System.out.println("a new or dead bunny caused an out of range error" + e.toString());
        }

        if( bunnyswarm.size() == 0){

            //           System.out.println("all bunnies dead");
	    extinct = true;
            return true;
        }

        return false;
    }
}
