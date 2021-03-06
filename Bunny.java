import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Random;

class Bunny extends Sprite{

    //these are the member variables that make up id number, vision catagory, amount of drink to take in, amount of food to take
    //at the end there are also bunny needs and bunny health and lifetime to store the number of cycles the bunny endured
    int bx, by, bunnyID, vision, grazecycles, hunger, thirst, health = 3900000, cycles, penalty, foodEaten, gestation, pregnancyCycle, socialness, concentration, totalcapacity;

    //when the bunny should stop considering self hungry or thristy and choose food or water over the other
    int watercapacity, foodcapacity, thirstyness, hungryness ,hungrynesspenalty ,thirstynesspenalty, births, generation, ageatmaturity, amountdrank, fat, maturitydrink, maturityeat;

    //metabolism multiplyer for speeding up movement plus deterioration
    int metabolism, totalfoodconsumed;

    //sight kernel for preset sight shapes
    int[][] kernel;

    //similar to above, a set of values determine the bunnies vision range in terms of eigh xy pairs, just within a smaller
    //range
    int[][] vision_chromosome = new int[8][2];

    //chromosomes for some options for the bunny such as eating and drinking times and movement speed
    //vision shape are worked out with a chromosome that adds or subtracts from a middle range
    int[] option_chromosome;

    //experimental chromosome for options
    int[][] option_chromosome_exp;

    //some important booleans for going to food and drink logic
    boolean busy, hungry, thirsty, alive, grazing, diedofthirst, diedofhunger, canbreed, drank, pregnant, matured, cameofage, breeding;

    //I plan to implement functions that will have the bunny locate these targets
    Tile collidingTile, centreOfBunnyMass, bestBunny, bestBunnyMass;

    Tile[][] map;

    //this array list will store the array of tiles the bunny will travel between grazing
    ArrayList<Tile>path = new ArrayList<Tile>();

    //this is the go to sprite, the bunny will head towards this sprite if busy is set to true and go to is called
    ArrayList<Tile>targets = new ArrayList<Tile>();

    //this array list just keeps track of the food eaten from which tile so as to subtract that amount of food from the
    //environment, it is returned at the bunnies location after a set amount of game cycles
    ArrayList<Tile>eatenTiles = new ArrayList<Tile>();

    //tile to home in on
    Tile choice;

    //a bunny suitor to home in on
    Bunny suitor = null, father = null, nearestBunny = null;

    Random gen = new Random(System.currentTimeMillis());

    //constructor takes bunny id number, bunny X then Y locations, bunny is told the size of the world
    //in terms of x by y for logical reasons only, next is bunny vision type.
    public Bunny( int id, int _bx, int _by ,Tile[][] _map) throws Exception{

        super( "Bunny", "imgs/rabbit.png", 11, 15);

        map = _map;
        bx = _bx;
        by = _by;
        bunnyID = id;
        generation = 0;
        breeding = true;

        //assign random starting position and angle
        setXY( gen.nextInt(bx), gen.nextInt(by));
        setAngle( gen.nextInt(359));

        //bunnies are dead by default! this is to solve the round = problem! much lazy so clever
        alive = false;

        // may used to store position of best bunny
        int[] pos;

        //generate random genes for the vision chromosome
        vision_chromosome = genVision();
        kernel = vision_chromosome;

        //generate random genes for the option chromosome
        //(eattime, drinktime, grazecycles, metabolism, foodcapacity, watercapacity, hungeryness, thirstyness)
        vision_chromosome = genVision();
        kernel = vision_chromosome;
        option_chromosome = genOptions();

        hungryness = option_chromosome[0];
        thirstyness = option_chromosome[1];
        grazecycles = option_chromosome[2];
        metabolism = option_chromosome[3];
        foodcapacity = 100 * option_chromosome[4];
        watercapacity = 100 * option_chromosome[5];
        socialness = 75 * (option_chromosome[6] + 1);
        gestation = 200 * option_chromosome[7] + 1;
        maturitydrink = option_chromosome[8];
        maturityeat = option_chromosome[9];

        //bunny starts fed and watered
        hunger = foodcapacity;
        thirst = watercapacity;
        totalcapacity = hungryness + thirstyness;

        //find point between feeling full and time to start eating/drinkning as
        //totalcapacity - (totalcapacity - hungeryness * N% of totalcapacity
        hungrynesspenalty = foodcapacity - (foodcapacity - ((foodcapacity/100)*(hungryness)));
        thirstynesspenalty = watercapacity - (watercapacity - ((watercapacity/100)*(thirstyness)));

        //sprites are created in try catch blocks
        try{

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
            addAngleCondition( 202, 247, 128, 136);//UPLEFT
            addAngleCondition( 112, 157, 136, 144);// //DOWNLEFT
            pollConditions("ANGLE");
            //System.out.println( "Bunny number " + bunnyID + " is alive " + alive);
        }catch( Exception e){

            System.out.println(" Bunny number " + bunnyID + " Is attempting to crash the program.");
        }
    }

    //this is the constructor for a child bunny
    public Bunny( int id, Bunny mother, Bunny father, int _bx, int _by, Tile[][] _map, int mutationrate) throws Exception{

        super( "Bunny", "imgs/rabbit.png", 11, 15);

        gen = new Random(System.currentTimeMillis());

        //size of the map
        bx = _bx;
        by = _by;
        map = _map;
        bunnyID = id;
        breeding = true;
        selected = false;

        //assign random starting position and angle
        setXY( gen.nextInt(bx), gen.nextInt(by));
        setAngle( gen.nextInt(359));

        //bunnies are alive by default
        alive = true;

        // may used to store position of best bunny
        int[] pos;

        //create genes based on mother and father
        vision_chromosome = visionChildrenDouble( mother, father, mutationrate);
        option_chromosome = optionChildrenDouble( mother, father, mutationrate);
        kernel = vision_chromosome;

        //CAN UNCOMENT REGION TO SEE CHROMOSOMES OF BUNNIES EACH ROUND
        /*System.out.println(" ~ BUNNY NUMBER " + bunnyID + " ~");

        //show vision to see if it has worked
        System.out.print( " vision: ");
        for (int i = 0; i < kernel.length; i++) {

        System.out.print( "(" +kernel[i][0] + ") - (" + kernel[i][1]+ ") ");
        }
        System.out.println();

        //same for options
        System.out.print(  " options: ");
        for (int i = 0; i < option_chromosome.length; i++) {

        System.out.print( option_chromosome[i] + " - ");
        }

        System.out.println();
        System.out.println();
        */

        hungryness = option_chromosome[0];
        thirstyness = option_chromosome[1];
        grazecycles = option_chromosome[2];
        metabolism = option_chromosome[3];
        foodcapacity = 100 * option_chromosome[4];
        watercapacity = 100 * option_chromosome[5];
        socialness = 60 * (option_chromosome[6] + 1);
        gestation = 200 * option_chromosome[7] + 1;
        maturitydrink = option_chromosome[8];
        maturityeat = option_chromosome[9];

        //find point between feeling full and time to start eating/drinkning as
        //totalcapacity - (totalcapacity - hungeryness * 10 % of totalcapacity
        hungrynesspenalty = foodcapacity - (foodcapacity - ((foodcapacity/100)*(hungryness)));
        thirstynesspenalty = watercapacity - (watercapacity - ((watercapacity/100)*(thirstyness)));

        //bunny starts fed and watered
        hunger = foodcapacity;
        thirst = watercapacity;
        totalcapacity = hungryness + thirstyness;

        //sprites are created in try catch blocks
        try{

            gen.setSeed(1234);
            setAcceleration(1);
            setmaxVelocity(1 * metabolism);
            setVelocity(1 * metabolism);
            addAngleCondition( 247, 292, 98, 104); //UP
            addAngleCondition( 67, 112, 120, 128);//DOWN
            addAngleCondition( 157, 202, 144, 152);//LEFT
            addAngleCondition( 337, 361, 88, 96);//RIGHT higher bounds
            addAngleCondition( -1, 22, 88, 96);//RIGHT lower bounds
            addAngleCondition( 292, 337, 104, 112);//UPRIGHT
            addAngleCondition( 22, 67, 128, 136);//DOWNRIGHT
            addAngleCondition( 202, 247, 112, 128);//UPLEFT
            addAngleCondition( 112, 157, 136, 144);//DOWNLEFT
            pollConditions("ANGLE");

            //System.out.println( "Bunny number " + bunnyID + " is alive " + alive);
        }catch( Exception e){

            System.out.println(" Bunny " + bunnyID + " Is attempting to crash the program.");
        }
    }

    int[] genOptions(){

        int[] temp = new int[10];
        for (int i = 0; i < 10; i++) {

            temp[i] = gen.nextInt(9) + 1;
        }

        return temp;
    }

    int[][] genOptionsExperimental(){

        int[][] temp = new int[10][8];
        for (int i = 0; i < temp.length; i++) {
            for (int d = 0; d < temp[i].length; d++) {

                temp[i][d] = gen.nextInt(9) + 1;
            }
        }

        return temp;
    }

    //initialy the bunnies sight be generated
    int[][] genVision(){

        int[][] temp = new int[8][2];
        for (int i = 0; i < 8; i++) {

            temp[i][0] =  gen.nextInt( 5) -2;
        }

        for (int i = 0; i < 8; i++) {

            temp[i][1] =  gen.nextInt( 5) -2;
        }

        return temp;
    }

    int getExperimentalGene( int g){

        int temp= 0;

        for (int i = 0; i < option_chromosome_exp[g].length; i++) {

            temp += option_chromosome_exp[g][i];
        }

        if ( temp != 0) {

            return temp/9;
        }else{

            return 0;
        }
    }

    public int[][] visionChildrenSingle( Bunny mother, Bunny father, int mutationrate){

        int[][] temp = new int[8][2];

        for (int i = 0; i < father.option_chromosome.length/2; i++) {

            temp[i][0] = father.vision_chromosome[i][0];
            temp[i][1] = father.vision_chromosome[i][1];
        }

        for (int i = mother.option_chromosome.length/2; i < 6; i++) {

            temp[i][0] = mother.vision_chromosome[i][0];
            temp[i][1] = mother.vision_chromosome[i][1];
        }

        //get a one in a thousand value
        int chance = gen.nextInt( 1000);

        //generate a random range - between this plus mutationrate
        int range = gen.nextInt( 1000 - mutationrate);

        //if chance is within the range then there is a collision and a mutation must be applied to
        // a random option
        if ( chance > range && chance < range + mutationrate) {

            //System.out.print("MUTATION!" + bunnyID + " (vision) ");
            int genetomutate = gen.nextInt( 8);
            int mutategenetox = gen.nextInt( 5) -2;
            int mutategenetoy = gen.nextInt( 5) -2;
            temp[genetomutate][0] = mutategenetox;
            temp[genetomutate][1] = mutategenetoy;
        }

        return temp;
    }

    //as above but with double crossover points
    public int[][] visionChildrenDouble( Bunny mother, Bunny father, int mutationrate){

        int[][] temp = new int[8][2];

        for (int i = 0; i < 3; i++) {

            temp[i][0] = father.vision_chromosome[i][0];
            temp[i][1] = father.vision_chromosome[i][1];
        }

        for (int i = 3; i < 6; i++) {

            temp[i][0] = mother.vision_chromosome[i][0];
            temp[i][1] = mother.vision_chromosome[i][1];
        }

        for (int i = 6; i < 8; i++) {

            temp[i][0] = father.vision_chromosome[i][0];
            temp[i][1] = father.vision_chromosome[i][1];
        }

        //get a one in a thousand value
        int chance = gen.nextInt( 1000);

        //generate a random range - between this plus mutationrate
        int range = gen.nextInt( 1000 - mutationrate);

        //if chance is within the range then there is a collision and a mutation must be applied to
        // a random option
        if ( chance > range && chance < range + mutationrate) {

            //System.out.println("MUTATION! IN BUNNY " + bunnyID + " (vision) ");
            //mutate a gene within range of the middle genes
            int genetomutate = gen.nextInt( 6) + 2;
            int mutategenetox = gen.nextInt( 5) -2;
            int mutategenetoy = gen.nextInt( 5) -2;

            temp[genetomutate][0] = mutategenetox;
            temp[genetomutate][1] = mutategenetoy;
        }

        return temp;
    }

    //combines two parents to return option genes with a single crossover point, the mutation rate is mutationrate in one thousand
    public int[] optionChildrenSingle( Bunny mother, Bunny father, int mutationrate){

        int[] temp = new int[10];

        for (int i = 0; i < father.option_chromosome.length/2; i++) {

            temp[i] = father.option_chromosome[i];
        }

        for (int i = mother.option_chromosome.length/2; i < 10; i++) {

            temp[i] = mother.option_chromosome[i];
        }

        //get a one in a thousand value
        int chance = gen.nextInt( 1000);

        //generate a random range - between this plus mutationrate
        int range = gen.nextInt( 1000 - mutationrate);

        //if the chance is within the range then there is a collision and a mutation must be applied to
        // a random option
        if ( chance > range && chance < range + mutationrate) {

            //System.out.println("MUTATION! IN BUNNY " + bunnyID + "( options )");
            int genetomutate = gen.nextInt( 10);
            int mutategeneto = gen.nextInt( 2);

            if( mutategeneto != 1){

                mutategeneto = -1;
            }

            if ( temp[genetomutate] + mutategeneto <= 9 &&  temp[genetomutate] + mutategeneto >= 0) {

                temp[genetomutate] = temp[genetomutate] + mutategeneto;
            }
        }

        return temp;
    }

    //as above but with double crossover points
    public int[] optionChildrenDouble( Bunny mother, Bunny father, int mutationrate){

        int[] temp = new int[10];

        for (int i = 0; i < 3; i++) {

            temp[i] = father.option_chromosome[i];
        }

        for (int i = 2; i < 6; i++) {

            temp[i] = mother.option_chromosome[i];
        }

        for (int i = 4; i < 10; i++) {

            temp[i] = father.option_chromosome[i];
        }

        //get a one in a thousand value
        int chance = gen.nextInt( 1000);

        //generate a random range - between this plus mutationrate
        int range = gen.nextInt( 1000 - mutationrate);

        //if the chance is within the range then there is a collision and a mutation must be applied to
        // a random option
        if ( chance > range && chance < range + mutationrate) {

            //System.out.println("MUTATION! IN BUNNY " + bunnyID + "( options )");
            int genetomutate = gen.nextInt( 10);
            int mutategeneto = gen.nextInt( 2);

            if( mutategeneto != 1){

                mutategeneto = -1;
            }

            if ( temp[genetomutate] + mutategeneto <= 9 &&  temp[genetomutate] + mutategeneto >= 0) {

                temp[genetomutate] = temp[genetomutate] + mutategeneto;
            }
        }

        return temp;
    }

    /* int [][] optionChildrenDouble_Exp( Bunny mother, Bunny father, int mutationrate){

        int[] temp = new int[10][8];

        for (int i = 0; i < 3; i++) {

            temp[i] = father.option_chromosome_exp[i];
        }

        for (int i = 2; i < 6; i++) {

            temp[i] = mother.option_chromosome[i];
        }

        for (int i = 4; i < 10; i++) {

            temp[i] = father.option_chromosome[i];
        }
    }
    */

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

    Bunny bestBreederInRange( ArrayList<Bunny> bunnyswarm){

        ArrayList<Bunny> suitors = new ArrayList<Bunny>();

        for (int i = 0; i < targets.size(); i++) {

            for (int n = 0; n < bunnyswarm.size(); n++) {

                //if that target is colliding with a bunny that can breed
                if (targets.get(i).circularCollision( bunnyswarm.get(n), 20) && bunnyswarm.get(n).canbreed == true) {

                    //bunny must not choose itself as a suitor
                    if( bunnyID != bunnyswarm.get(n).bunnyID)
                        suitors.add( bunnyswarm.get(n));
                }
            }
        }

        if( suitors.size() == 0){

            return null;
        }else{

            Bunny temp = suitors.get(0);

            for (int i = 0; i < suitors.size(); i++) {

                if( temp.health < suitors.get(i).health){

                    temp = suitors.get(i);
                }
            }

            return temp;
        }
    }

    //calculates maturity days to birth and birth, energy is passed on possibly killing the bunny
    //however drinking once in the past is saved
    Bunny breedcalculate(){

        if ( !pregnant && foodEaten > maturityeat && amountdrank > maturitydrink && !cameofage) {

            canbreed = breeding;
            matured = true;
            cameofage = true;
            ageatmaturity = cycles;
            System.out.print("M");

        }else if( !pregnant && foodEaten > maturityeat && amountdrank > maturitydrink && matured){

            canbreed = true;
        }

        if ( pregnant) {

            pregnancyCycle++;
        }

        if ( pregnancyCycle >= gestation) {

            System.out.print("- " + bunnyID);

            if (generation > 0) {

                System.out.println(" generation " + generation + " ");
            }

            System.out.print("B!-");
            maturityeat = 0;
            maturitydrink = 0;
            pregnant = false;
            pregnancyCycle = 0;
            births++;

            //returns this bunny as the mother for processing in bunny manager
            return this;
        }

        return null;
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


    //puts breeding before grazing, grazing works out the priority of eating to drinking
    void priorities( ArrayList<Bunny>swarm){

        //make sure that if the bunny can breed then that bunny is looking for suitors
        if( canbreed){

            suitor = bestBreederInRange( swarm);
        }

        //if the suitor is present go to that suitor untill there is a collision
        //and babies!, the suitor has to still be alive however
        if (suitor != null && father == null) {

            if ( goTo(suitor)) {

                pregnant = true;
                father = suitor;
                suitor = null;
                canbreed = false;

                System.out.print("P");
            }
        }else{

            graze();
        }
    }

    //designed to work with nextNode function in priorities, if theres both food and water in range then chances of going to which
    //recource are determined by the bunny agents hungryness or thirstyness
    void graze(){

        if( cycles <= grazecycles ){

            if(waterInRange() && !grassInRange()){

                gotoWater();
                cycles++;
            }else if(grassInRange() && !waterInRange()){

                gotoGrass();
                cycles++;
            }else if( grassInRange() && waterInRange()){

                //generate a chance of going to drink or eat based on
                //thirstyness of bunny
                boolean[] chanceofdrink = new boolean[totalcapacity];

                for (int i = 0; i < thirstyness; i++) {

                    chanceofdrink[i] = true;
                }

                int chance = gen.nextInt( totalcapacity);

                if( chanceofdrink[chance]){

                    gotoWater();
                    cycles++;
                }else if( !chanceofdrink[chance]){

                    gotoGrass();
                    cycles++;
                }else if(nearestBunny != null){

                    cycles++;
                }else{

                    goTo( targets.get(0));
                    cycles++;
                }
            }
        }else{

            if (nearestBunny == null || concentration > 80) {

                cycles = 0;
                goTo( targets.get(gen.nextInt(targets.size())));
            }else{

                if(circularCollision( nearestBunny, socialness)) {

                    cycles = 0;
                }else{

                    goTo( nearestBunny);

                    //this is just to avoid bunnies getting stuck searching for another bunny
                    concentration++;
                }
            }
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
                    thirst = watercapacity;
                    setVelocity(0);
                    setAcceleration(0);
                    drank = true;
                }

                if(penalty >= thirstyness && !hungry){

                    choice = null;
                    thirsty = false;
                    setVelocity(1 * metabolism);
                    setAcceleration(2);
                    penalty = 0;
                    grazecycles = 100;
                    amountdrank++;
                    System.out.print("'");
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

                //what penalty does is make sure that the bunny eats untill its full and does not want to eat more
                if(penalty < hungryness && !thirsty){

                    penalty++;
                    eatenTiles.add(choice);

                    if( !pregnant){

                        hunger += 10 + metabolism/2;
                        foodEaten += 10 + metabolism/2;
                        totalfoodconsumed += 10 + metabolism/2;
                    }else{

                        hunger += 5 + metabolism/2;
                        fat += 5;
                        foodEaten += 10 + metabolism/2;
                        totalfoodconsumed += 10 + metabolism/2;
                    }

                    setVelocity(0);
                    setAcceleration(0);

                }

                if(penalty >= hungryness && !thirsty){

                    choice = null;
                    hungry = false;
                    setVelocity(1 * metabolism);
                    setAcceleration(2);
                    penalty = 0;
                    System.out.print("#");
                }
            }else{

                goTo(choice);
            }
    }

    //displays bunny stats
    void displayState( Graphics g){

        g.setColor(Color.WHITE);
        drawString( g, "ID: " + bunnyID + " G: " + generation + " M: " + matured + " P: " + pregnant + " F: " + hunger + " T: " + thirst, - (getWidth()/2), 30);

        //draws a rectangle over the location the bunny is trying to get to if it is trying to get somewhere
        if(choice != null){

            g.drawRect(choice.getPosX()+5, choice.getPosY()+5, bx/10 -10, by/10 -10);
        }
    }

    //each cycle the bunny should get hungrier, thirstier and more needs will tick down
    //untill they either reach zero and the bunny dies or they are topped up with eating or
    //drinking, also, health goes down a certain amount based on how hungry or thirsty the bunny is.
    void bunnyFatigue(){

        cycles++;

        int thirstTax = Math.abs( watercapacity - thirst);
        int hungerTax = Math.abs( foodcapacity - hunger);

        health -= ( hungerTax + (thirstTax/2)) * (1 + metabolism/2);
        thirst-=2;
        hunger--;

        //can have this detect the cause of death
        if( health <  0){

            alive = false;
            System.out.print( "E!");
        }else if( thirst < 0){

            alive = false;
            diedofthirst = true;
            System.out.print( "T");
        }else if( hunger < 0 && fat > 10){

            hunger = fat;
            fat = 0;
        }else if( hunger < 0 && fat < 10){

            alive = false;
            diedofhunger = true;
            System.out.print( "H");
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
    boolean goTo( Tile choice){

        if( !busy){

            pointToTwo(choice);
            if( !checkCollision( choice)){

                return true;
            }
        }

        return false;
    }

    //bunny is busy until arrives at target sprite
    boolean goTo( Bunny choice){

        pointToTwo(choice);
        if( !checkCollision( choice)){

            return true;
        }

        return false;
    }

    //bunny is busy until arrives at target sprite
    boolean goTo( Sprite choice){

        pointToTwo(choice);
        if( !checkCollision( choice)){

            return true;
        }

        return false;
    }

    public void showVision( Graphics g){

        if( selected){

            g.setColor(Color.RED);
            for (int i = 0; i < targets.size(); i++) {

                g.drawRect( targets.get(i).getPosX()+10, targets.get(i).getPosY()+10, bx/10 -20, by/10 -20);
            }
        }
    }
}
