import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Random;

class Bunny extends Sprite{

    //these are the member variables that make up id number, vision catagory, amount of drink to take in, amount of food to take
    //at the end there are also bunny needs and bunny health and lifetime to store the number of cycles the bunny endured
    int bx, by, bunnyID, vision, grazecycles, hunger, thirst, health = 600000, cycles, penalty, foodEaten, presentNode, totalNodes, presentCycle;

    //when the bunny should stop considering self hungry or thristy and choose food or water over the other
    int watercapacity, foodcapacity, thirstyness, hungryness ,hungrynesspenalty ,thirstynesspenalty;

    //metabolism multiplyer for speeding up movement plus deterioration
    int metabolism;

    //sight kernel for preset sight shapes
    int[][] kernel;

    //this integer represents the chromosomes, 16 values represent eight xy coordinatates ( xyxyxyxyxyxyxyxy)
    //the bunny will walk in a pattern based on these coordinates around itself
    int[][] movement_chromosome = new int[8][2];

    //similar to above, a set of values determine the bunnies vision range in terms of eigh xy pairs, just within a smaller
    //range
    int[][] vision_chromosome = new int[8][2];

    //chromosomes for some options for the bunny such as eating and drinking times and movement speed
    //vision shape are worked out with a chromosome that adds or subtracts from a middle range
    int[] option_chromosome;

    //some important booleans for going to food and drink logic
    boolean busy, hungry, thirsty, alive, grazing, walk;

    //I plan to implement functions that will have the bunny locate these targets
    Tile collidingTile, centreOfBunnyMass, bestBunny, bestBunnyMass;

    //this array list will store the array of tiles the bunny will travel between grazing
    ArrayList<Tile>path = new ArrayList<Tile>();

    //this is the go to sprite, the bunny will head towards this sprite if busy is set to true and go to is called
    ArrayList<Tile>targets = new ArrayList<Tile>();

    //this array list just keeps track of the food eaten from which tile so as to subtract that amount of food from the
    //environment, it is returned at the bunnies location after a set amount of game cycles
    ArrayList<Tile>eatenTiles = new ArrayList<Tile>();

    //tile to home in on
    Tile choice;

    private Random gen = new Random();
    
    //constructor takes bunny id number, bunny X then Y locations, bunny is told the size of the world
    //in terms of x by y for logical reasons only, next is bunny vision type.
    public Bunny( int id, int bX, int bY, int v, Tile[][] map) throws Exception{

        super( "Bunny", "imgs/rabbit.png", 11, 15);

        bx = bX;
        by = bY;
	bunnyID = id;
	presentNode = 0;
	
        //bunnies are alive by default
        alive = true;
	
        // may used to store position of best bunny
        int[] pos;
	
	//generate random genes for the movement chromosome
        movement_chromosome = genMovement();

        //generate random genes for the vision chromosome

        //generate random genes for the option chromosome
        //(eattime, drinktime, grazecycles, metabolism, foodcapacity, watercapacity, hungeryness, thirstyness)
        option_chromosome = genOptions();
        genPath( map);

        hungryness = option_chromosome[0];
        thirstyness = option_chromosome[1];
        grazecycles = option_chromosome[2];
        metabolism = option_chromosome[3];
        foodcapacity = 100 * option_chromosome[4];
        watercapacity = 100 * option_chromosome[5];

	//bunny starts fed and watered
        hunger = foodcapacity;
        thirst = watercapacity;

	//find point between feeling full and time to start eating/drinkning as totalcapacity - (totalcapacity - hungeryness * 10 % of totalcapacity
	hungrynesspenalty = foodcapacity - (foodcapacity - ((foodcapacity/100)*(hungryness * 4)));
	thirstynesspenalty = watercapacity - (watercapacity - ((watercapacity/100)*(thirstyness * 4)));

        vision = v;
        kernel = getVision();
        
        //sprites are created in try catch blocks
        try{

	    gen = new Random();
	    gen.setSeed(1234);
            setXY(bx , by);
            setAngle(0);
            setAcceleration(1);
            setmaxVelocity(1 * metabolism);
            setVelocity(1 * metabolism);
            addAngleCondition( 247, 292, 98, 104); //UP
            addAngleCondition( 67, 112, 120, 128);//DOWN
            addAngleCondition( 157, 202, 144, 152);//LEFT
            addAngleCondition( 337, 361, 88, 96);//RIGHT higher bounds
            addAngleCondition( -1, 22, 88, 96);//RIGHT lower bounds
            addAngleCondition( 292, 337, 104, 112);//UPRIGHT
            addAngleCondition( 22, 67, 112, 120);//DOWNRIGHT
            addAngleCondition( 202, 247, 112, 120);//UPLEFT
            addAngleCondition( 112, 157, 136, 144);//DOWNLEFT
            pollConditions("ANGLE");
	    //System.out.println( "Bunny number "+ id+ " at posX: " + bX + " posY: "+ bY + " is alive " + alive);
        }catch( Exception e){

            System.out.println(" Bunny number " + bunnyID + " Is attempting to crash the program.");
        }
    }

    int[] genOptions(){

        System.out.print( " Options: ");
        int[] temp = new int[6];
        for (int i = 0; i < 6; i++) {

            temp[i] = gen.nextInt( 9) + 1;
            System.out.print( temp[i] + ", ");
        }
        System.out.println(".");
        return temp;
    }

    int[][] genMovement(){

        Random gen = new Random(System.currentTimeMillis());
        System.out.print("Movement: ");
        int[][] temp = new int[8][2];
        for (int i = 0; i < 8; i++) {

            temp[i][0] =  gen.nextInt( bx * 2) + 1;
        }

	for (int i = 0; i < 8; i++) {

            temp[i][1] =  gen.nextInt( by * 2) + 1;
        }

        for (int i = 0; i < 8; i++) {

	    System.out.print( "X " + temp[i][0] + " Y "+ temp[i][1] + ". " );
        }
	System.out.println();

        return temp;
    }

    //generates an array list out of the movement chromosomes to create a path to travel between
    void genPath( Tile[][] map){

        try{

            ArrayList<Tile>temparr = new ArrayList<Tile>();

            for( int x = 0; x < map.length; x++){
                for( int y = 0; y < map[x].length; y++){

                    temparr.add( map[x][y]);
                }
            }

            for (int i = 0; i < temparr.size(); i++) {

                for(int z = 0; z < 8; z++) {

                    //use any tile for now
                    Sprite temp = new Sprite("temp","imgs/pointer.png",1,1);
                    temp.setXY( movement_chromosome[z][1], movement_chromosome[z][0]);
                    temp.setWH( 1 ,1);
                    if( temparr.get(i).checkCollision( temp)){

			path.add( temparr.get(i));
			break;
                    }
                }
            }

        }catch( Exception e){

            System.out.println("failed to generate a path for bunny");
        }finally{

	    totalNodes = path.size();
            System.out.println(" path with " + totalNodes + " nodes successfuly created");
        }
    }

    //this function is essentially the bunnies vision, anything in its field of view
    //it will be given a choice of what to go for
    void updateVision( Tile[][] map){

        try{

            collidingTile = map[0][0];

            for( int x = 0; x < map.length; x++){
                for( int y = 0; y < map[x].length; y++){

                    if( this.checkCollision( map[x][y])){

                        collidingTile = map[x][y];
                        break;
                    }
                }
            }
            if(!busy){

                targets.clear();
                for (int i = 0; i < kernel.length; i++) {

                    //Bunny Vision X or Y to find which tiles the bunny can see
                    int bvx = collidingTile.xtile + kernel[i][0];
                    int bvy = collidingTile.ytile + kernel[i][1];

                    for (int x = 0; x < map.length; x++) {
                        for (int y = 0; y < map[x].length; y++) {

                            if( map[x][y].xtile == bvx && map[x][y].ytile == bvy){

                                targets.add( map[x][y]);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){

            System.out.println(e.toString());
        }
    }

    boolean grassInRange(){

        for (int i = 0; i < targets.size(); i++) {

            if( targets.get(i).getName() == "grass" && targets.get(i).food > 0){

                return true;
            }
        }
        return false;
    }

    boolean waterInRange(){

        for (int i = 0; i < targets.size(); i++) {

            if( targets.get(i).getName() == "water"){

                return true;
            }
        }
        return false;
    }

    //walk to 'nextNode', go through whats available within range of bunny vision and determine a priority - food or water
    //repeat for 'grazecycles' amount of time
    void priorities(){

        if( !graze()){

	    walk = true;
       
	}else{

	    walk = false;
	}

	if(walk){

	    if(nextNode()){

		presentCycle = 0;
		presentNode++;

		if (presentNode >= totalNodes) {

		    presentNode = 0;
		}
	    }
	}

    }

    //designed to work with nextNode function in priorities, if theres both food and water in range then chances of going to which
    //recource are determined by the bunny agents hungryness or thirstyness
    boolean graze(){

	if( presentCycle <= grazecycles){
	    
	    if(waterInRange() && !grassInRange()){
 
		gotoWater();
	    }else if(grassInRange() && !waterInRange()){

		gotoGrass();
	    }else if( grassInRange() && waterInRange()){

		//generate a chance of choosing water over grass based on hungryness or thirstyness
		int val = gen.nextInt( hungryness + thirstyness);
		if( val <= hungryness){

		    gotoGrass();
		}else{

		    gotoWater();
		}
		
	    }else{

		walk = true;
	    }

	    presentCycle++;
	    return true;
	}else{
	    
	    return false;
	}
    }

    boolean nextNode(){

        pointToTwo( path.get(presentNode));
	
	if( circularCollision(path.get(presentNode), 450)){

	    return true;
	}else{

	    return false;
	}
    }

    void gotoWater(){

	thirsty = false;

	if( thirst < (watercapacity - thirstynesspenalty))
	    if(choice == null){
		for (int i = 0; i < targets.size(); i++) {

		    if(targets.get(i).getName().equals("water")){

			choice = targets.get(i);
			break;
		    }
		}
	    }else if( choice != null &&  checkCollision(choice)){

		thirsty = true;

		if(penalty < thirstyness && !hungry){

		    penalty++;
		    thirst+=10;
		    setVelocity(0);
		    setAcceleration(0);

		}

		if(penalty >= thirstyness && !hungry){

		    choice = null;
		    thirsty = false;
		    setVelocity(1 * metabolism);
		    setAcceleration(2);
		    penalty = 0;
		}
	    }else{

		goTo(choice);
	    }
    }

    void gotoGrass(){

	hungry = false;

	if(hunger < (foodcapacity - hungrynesspenalty))
	    if(choice == null){
		for (int i = 0; i < targets.size(); i++) {

		    if(targets.get(i).getName().equals("grass") && targets.get(i).food > 0){

			choice = targets.get(i);
			break;
		    }
		}
	    }else if( choice != null &&  checkCollision(choice)){

		hungry = true;

		if(penalty < hungryness && !thirsty){

		    penalty++;
		    eatenTiles.add(choice);
		    hunger+=10;
		    foodEaten+=10;
		    setVelocity(0);
		    setAcceleration(0);

		}

		if(penalty >= hungryness && !thirsty){

		    choice = null;
		    hungry = false;
		    setVelocity(1 * metabolism);
		    setAcceleration(2);
		    penalty = 0;
		}
	    }else{

		goTo(choice);
	    }
    }

    //displays bunny stats
    void displayState( Graphics g){

	g.setColor(Color.BLACK);
	drawString(g, "ID: " + bunnyID + "H: " + health +"F: " + hunger + "T: " + thirst, -(getWidth()/2), 30);

	//draws a rectangle over the location the bunny is trying to get to if it is trying to get somewhere
	if(choice != null){

	    g.drawRect(choice.getPosX(), choice.getPosY(), 40, 40);
	}
    }

    //each cycle the bunny should get hungrier, thirstier and more needs will tick down
    //untill they either reach zero and the bunny dies or they are topped up with eating or
    //drinking, also, health goes down a certain amount based on how hungry or thirsty the bunny is.
    void bunnyFatigue(){

	cycles++;
	int thirstTax = Math.abs(400 - thirst);
	int hungerTax = Math.abs(400 - hunger);

	//reduces health depending on whether the bunny is eating or drinking or not
	if( hungry){

	    health -=  thirstTax * metabolism;
	    thirst--;
	}else if( thirsty){

	    health -= hungerTax * metabolism;
	    hunger--;
	}else{

	    health -= ( hungerTax + thirstTax) * metabolism;
	    thirst--;
	    hunger--;
	}

	//can have this detect the cause of death
	if( health <  0 || thirst < 0 || hunger < 0){

	    alive = false;
	    System.out.println( " bunny number " + bunnyID + " died, thirst = " + thirst +  " hunger " + hunger + " health " + health);
	}
    }

    //go to best bunny
    public Tile bestBunny(){

	return bestBunny;
    }

    //go to center of bunny mass
    public Tile centreOfBunnyMass(){

	return centreOfBunnyMass;
    }

    //bunny is busy until arrives at target sprite
    boolean goTo( Sprite choice){

	if( !busy){

	    pointToTwo(choice);
	    if( !circularCollision( choice, 80)){

		return true;
	    }
	}

	return false;
    }



    //this are matrices for a bunny facing north, I will have to remember how to either rotate or do four more kernels
    //in the meantime I will movmove on
    int[][] getVision(){

	switch( vision){

	case 1:

	    int[][] v1 = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1},{ 1, 1}};
	    return v1;
	case 2:

	    int[][] v2 = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, -2},{ 1, 1}};
	    return v2;
	case 3:

	    int[][] v3 = {{-1, -1},{ 0, -2},{ 1, -1},{-2, 0},{ 0, 0},{ 2, 0},{-1, 1},{ 0, 2},{ 1, 1}};
	    return v3;
	case 4:

	    int[][] v4 = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{ 0, 2},{ 0, 1},{ 0, 3}};
	    return v4;
	case 5:

	    int[][] v5 = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{ 0, -2},{ 0, 1},{ 0, -3}};
	    return v5;
	case 6:

	    int[][] v6 = {{ 0, -2},{ 0, -1},{ 2, 0},{-1, 0},{ 0, 0},{ 1, 0},{-2, 0},{ 0, 1},{ 0, 2}};
	    return v6;
	case 7:

	    int[][] v7 = {{ 0, -2},{ 0, -1},{ 0, 2},{-1, 0},{ 0, 0},{ 1, 0},{0, -4},{ 0, 1},{ 0, -3}};
	    return v7;

	case 8:

	    int[][] v8 = {{ 0, -2},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{0, -4},{ -1, -1},{ 0, -3}};
	    return v8;

	case 9:

	    int[][] v9 = {{ 0, -2},{ 0, -1},{ 1, -1},{-2, -2},{ 0, 0},{ 2, -2},{0, -4},{ -1, -1},{ 0, -3}};
	    return v9;

	case 10:

	    int[][] v0 = {{ 0, -2},{ 0, -1},{ 1, -1},{-1, -2},{ 0, 0},{ 1, -2},{0, -4},{ -1, -1},{ 0, -3}};
	    return v0;
	}

	//return default vision if theres a bug
	System.out.println("Bunny vision is not being assigned properly");
	int[][] vd = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1},{ 1, 1}};
	return vd;
    }

    /*/this function might a rotational matrix to return a rotated kernel based on the angle the bunny is facing
      int[][] rotateVision(){

      double angle = Math.toRadians(-90);
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);


      }
    */
}
