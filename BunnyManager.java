import java.util.Random;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;

class BunnyManager{

    int WIDTH, HEIGHT, hungerdeaths, thirstdeaths, totalFoodConsumption, mutationrate, matRange;

    //last bunny manager
    BunnyManager lastBM;

    public ArrayList<Bunny>longestlivedstock = new ArrayList<Bunny>();
    public ArrayList<Bunny>breedingstock = new ArrayList<Bunny>();
    public ArrayList<Bunny>bunnyswarm = new ArrayList<Bunny>();
    public ArrayList<Bunny>deadbunnies = new ArrayList<Bunny>();
    public ArrayList<Integer>averagesuccess = new ArrayList<Integer>();
    public ArrayList<Integer>populationsize = new ArrayList<Integer>();
    public ArrayList<Integer>bestbunny = new ArrayList<Integer>();

    boolean infinatespace, finitespace, extinct, breeding, breederstage;

    Bunny breeder;

    Tile[][] map;

    //the bunny IDs cant go backwards :/
    int bun_tracker;

    //this constructor is for random bunnies mode
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

            System.out.println(" Random Bunny manager constructor has failed: " + e.toString());
	}

	bun_tracker = bunnyswarm.size()+1;
    }

    //this constructor takes a previous constructor as an argument and finds the best bunnies to apply
    //genetic crossover with
    public BunnyManager( BunnyManager bm, Tile[][] _map, int _mutationrate){

	lastBM = bm;
	mutationrate = _mutationrate;
	map = _map;
	breedingstock = lastBM.breedingstock;

	//enables or disables breeding
	breeding = false;
	
	//avoid having a linked list going on too long
	lastBM.lastBM = null;
	WIDTH = bm.WIDTH;
	HEIGHT = bm.HEIGHT;

	//the following block is written in an ugly scripted way because a lot of experimentation has gone on here
	try{

	    //accumulate breeding stock for breeding bunnies mode
	    for (int i = 0; i < lastBM.deadbunnies.size(); i++) {

		if (lastBM.deadbunnies.get(i).births > 0) {

		    breedingstock.add( lastBM.deadbunnies.get(i));
		}
	    }	

	    //get rid of the ealiest bunny that has birthed the least amount of times from the
	    //breeding stock list
	    while( breedingstock.size() > 16){

		int temp = 999;
		int bun;
		
		for (int i = 0; i < breedingstock.size(); i++) {

		    if ( temp > breedingstock.get(i).births) {

			temp = breedingstock.get(i).births;
			bun = i;
		    }
		}
	    }
	 
	    //algorithm starts just by keeping the bunnies that live the longest
	    if( bunnyswarm.size() < 16){

		//this creates four bunnies from previous best bunnies by getting the best deadbunnies
		//by their IDs and popping them out the deadbunnies array	    
		Bunny Elitemother = popPastDeadBunny( bestPastLifeTimeID());
		Bunny Elitefather = popPastDeadBunny( bestPastLifeTimeID());
	    
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
	    

	    //top up with random bunnies
	    while( bunnyswarm.size() < 16){

		bunnyswarm.add( new Bunny( bunnyswarm.size(), WIDTH, HEIGHT, map));
		bunnyswarm.get( bunnyswarm.size()-1).alive = true;
	    }

	    /*/neuter all bunnies
	      for (int i = 0; i < bunnyswarm.size(); i++) {

	      bunnyswarm.get(i).canbreed = false;
	      }
	    */
	  
	    bun_tracker = bunnyswarm.size();
	   	  
	}catch(Exception e){

	    System.out.println(" Genetic Bunny manager failed: " + e.toString());
	    e.printStackTrace(System.out);
	}
    }

    
    //this is the breeding bunnies constructor
    public BunnyManager( BunnyManager bm, Tile[][] _map, int _mutationrate, boolean _breeding){

	lastBM = bm;
	mutationrate = _mutationrate;
	map = _map;

	//enables or disables breeding
	breeding = _breeding;
        breedingstock = lastBM.breedingstock;
	
	//avoid having a linked list going on too long
	lastBM.lastBM = null;

	//keep width and height of map
	WIDTH = bm.WIDTH;
	HEIGHT = bm.HEIGHT;

            //accumulate breeding stock for breeding bunnies mode
	    for (int i = 0; i < lastBM.deadbunnies.size(); i++) {

		if (lastBM.deadbunnies.get(i).births > 0) {

		    breedingstock.add( lastBM.deadbunnies.get(i));
		}
	    }	

	    //get rid of the ealiest bunny that has birthed the least amount of times from the
	    //breeding stock list
	    while( breedingstock.size() > 16){

		int temp = 999;
		int bun;
		
		for (int i = 0; i < breedingstock.size(); i++) {

		    if ( temp > breedingstock.get(i).births) {

			temp = breedingstock.get(i).births;
			bun = i;
		    }
		}
	    }
     
	try{

	    if (breedingstock.size() < 16){

		breederstage = true;
		for (int i = breedingstock.size(); i < 16; i++) {

		    bunnyswarm.add( new Bunny( i, WIDTH, HEIGHT, map));
		}
	    }else{

		//can get on with creating variants of all the bunnies that succeeded in breeding
		breederstage = false;
	        bunnyswarm = breedingstock;
	    }
	   
	    //these bunnies are based on past best bunnies that have lived and died before so they must be bought back to life
	    for (int i = 0; i < bunnyswarm.size(); i++) {

		bunnyswarm.get(i).alive = true;
	    }
	
	    bun_tracker = bunnyswarm.size()-1;
	   	  
	}catch(Exception e){

	    System.out.println(" Breeding Bunny manager failed: " + e.toString());
	}
    }

    //this is an experimental method that will keep the bunnies that mature the earliest for selection
    public BunnyManager( BunnyManager bm, Tile[][] _map, int _mutationrate, int _matRange){

	lastBM = bm;
	mutationrate = _mutationrate;
	map = _map;
	matRange = _matRange;
	breedingstock = lastBM.breedingstock;
	
	//enables or disables breeding
	breeding = true;
	
	//avoid having a linked list going on too long
	lastBM.lastBM = null;
	WIDTH = bm.WIDTH;
	HEIGHT = bm.HEIGHT;

	//accumulate breeding stock for breeding bunnies mode
	for (int i = 0; i < lastBM.deadbunnies.size(); i++) {

	    if (lastBM.deadbunnies.get(i).births > 0) {

		breedingstock.add( lastBM.deadbunnies.get(i));
	    }
	}

	
	//get rid of the earliest bunny that has birthed the least amount of times from the
	//breeding stock list
	while( breedingstock.size() > 16){

	    int temp = 999;
	    int bun;
		
	    for (int i = 0; i < breedingstock.size(); i++) {

		if ( temp > breedingstock.get(i).births) {

		    temp = breedingstock.get(i).births;
		    bun = i;
		}
	    }
	}

	//the following block is written in an ugly scripted way because a lot of experimentation has gone on here
	try{
	 

	    //this creates four bunnies from previous best bunnies by getting the best deadbunnies
	    //by their IDs and popping them out the deadbunnies array	    
	    Bunny Elitemother = popPastDeadBunny( bestPastFoodConsumptionID());
	    Bunny Elitefather = popPastDeadBunny( bestPastFoodConsumptionID());
	    
	    bunnyswarm.add(new Bunny( bunnyswarm.size(), Elitemother, Elitefather, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( bunnyswarm.size(), Elitefather, Elitemother, WIDTH, HEIGHT, map, mutationrate));
	  
	    Bunny mother = popPastDeadBunny( bestPastFoodConsumptionID());
	    Bunny father = popPastDeadBunny( bestPastFoodConsumptionID());

	    bunnyswarm.add(new Bunny( bunnyswarm.size(), mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( bunnyswarm.size(), father, mother, WIDTH, HEIGHT, map, mutationrate));

	    mother = popPastDeadBunny( bestPastFoodConsumptionID());
	    father = popPastDeadBunny( bestPastFoodConsumptionID());

	    bunnyswarm.add(new Bunny( bunnyswarm.size(), mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( bunnyswarm.size(), father, mother, WIDTH, HEIGHT, map, mutationrate));
		
	    mother = popPastDeadBunny( bestPastFoodConsumptionID());
	    father = popPastDeadBunny( bestPastFoodConsumptionID());

	    bunnyswarm.add(new Bunny( bunnyswarm.size(), mother, father, WIDTH, HEIGHT, map, mutationrate));
	    bunnyswarm.add(new Bunny( bunnyswarm.size(), father, mother, WIDTH, HEIGHT, map, mutationrate));

	    
	    	   	 
	    int bunnynum = bunnyswarm.size()+1;

	    //top up a lack of total bunnies so each round does start with sixteen
	    while( bunnyswarm.size() < 16){
		if( bunnyswarm.size() < 16){
		
		    //elite bunnies but with a much higher mutation rate
		    for (int i = 0; i < 4; i++) {
			for (int a = 0; a < 4; a++) {
			
			    bunnyswarm.add(new Bunny( bunnynum++, bunnyswarm.get(i), bunnyswarm.get(a), WIDTH, HEIGHT, map, mutationrate * 4));
			}
		    }	  
		}

		//must remove identical bunnies
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
	    }
	    
	    bun_tracker = bunnyswarm.size();
	   	  
	}catch(Exception e){

	    System.out.println(" Hungry Bunny manager failed: " + e.toString());
	}
    }


    boolean compareArray(int[] A, int[] B){

	if ( A.length-1 != B.length-1) {

	    return false;
	}

	for (int i = 0; i < A.length-1; i++) {

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

		//this is was interesting, multiplying food by water has the bunny eating the multiplier more but a little numerator
		temp = lastBM.deadbunnies.get(i).amountdrank * lastBM.deadbunnies.get(i).totalfoodconsumed ;
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

    //returns lifetime size of best bunny
    public int bestPastMaturityID(){

	int temp = 99999;
	int id = 0;

	for (int i = 0; i < lastBM.deadbunnies.size() ; i++) {

	    if( temp > lastBM.deadbunnies.get(i).ageatmaturity){

		temp = lastBM.deadbunnies.get(i).ageatmaturity;
		id = lastBM.deadbunnies.get(i).bunnyID;
	    }
	}

	return id;
    }

    Bunny assignNearest( Bunny startbun){

	Bunny temp = null;
	double distance = 9999;
	
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

    public void drawFields( Graphics g){

	for (int i = 0; i < bunnyswarm.size(); i++) {

	    if (bunnyswarm.get(i).selected) {

		bunnyswarm.get(i).showVision( g);
	    }
	}
    }

    //draws individual bunny
    public void drawBunny( Graphics g, Bunny barry){

	g.drawImage( barry.nextFrame(), barry.getPosX(), barry.getPosY(), barry.getWidth(), barry.getHeight(), null);
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

    //calls all vital bunny functions returns true when all bunnies are dead
    public boolean bunnyFunctions( Tile[][] tiles, Graphics g){

	g.setColor(Color.RED);
	g.drawRect(0,0,WIDTH,HEIGHT);
	
	try{

	    //makes sure  that each bunny doesnt wonder into the space between worlds	    
	    for (int i = 0; i < bunnyswarm.size(); i++) {

		if( offGrid( bunnyswarm.get(i), tiles)){

		    bunnyswarm.get(i).grazecycles = 100;
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

	        bunnyswarm.get(i).updateVision( tiles);
		drawFields(g);	     
		bunnyswarm.get(i).priorities( bunnyswarm);	
		bunnyswarm.get(i).pollConditions("ANGLE");
		bunnyswarm.get(i).displayState(g);		 	      
		bunnyswarm.get(i).moveSprite();
	        drawBunny( g, bunnyswarm.get(i));
		bunnyswarm.get(i).nearestBunny = assignNearest( bunnyswarm.get(i));
		breeder = bunnyswarm.get(i).breedcalculate();
		bunnyswarm.get(i).bunnyFatigue();

		//add a new bunny based on whether that bunny has given birth, 
		//handle breeding here to avoid out of range errors
		if (breeder != null && breeding) {

		    bunnyswarm.get(i).births++;
		    breedingstock.add( bunnyswarm.get(i));
		    bunnyswarm.add(new Bunny( bun_tracker++, breeder, bunnyswarm.get(i).father, WIDTH, HEIGHT, map, mutationrate));
		    bunnyswarm.get( bunnyswarm.size()-1).setAngle( bunnyswarm.get(i).getAngle());

		    //add that bunnies generation number to the generation array
		    bunnyswarm.get( bunnyswarm.size()-1).generation = bunnyswarm.get( bunnyswarm.size()-2).generation;
		    bunnyswarm.get( bunnyswarm.size()-1).generation++;

		    //put that bunny nearby where it gave birth
		    bunnyswarm.get(  bunnyswarm.size()-1).setXY( bunnyswarm.get(i).getPosX(), bunnyswarm.get(i).getPosY());

		    //give the bunny the weight that the mother saved, this will create longer lived bunnies
		    bunnyswarm.get(  bunnyswarm.size()-1).hunger += bunnyswarm.get(i).fat;
		    bunnyswarm.get(i).fat = 0;

		    System.out.print(bunnyswarm.get(  bunnyswarm.size()-1).bunnyID + "B!-");
		    
		}
              
		//if the bunny has chosen a potential father for breeding make sure he is not dead
	       	if (isSuitorDead( bunnyswarm.get(i))) {

	       	    bunnyswarm.get(i).suitor = null;
	       	}

		if(!bunnyswarm.get(i).alive){

		    deadbunnies.add( bunnyswarm.get(i));
		    bunnyswarm.remove(i);
		}

	    }
	}catch( Exception e){

	    // System.out.println( e.toString());
	    e.printStackTrace(System.out);
	}

	if( bunnyswarm.size() == 0){

	    //           System.out.println("all bunnies dead");
	    extinct = true;
	    return true;
	}

	return false;
    }
}
