import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Color;

class Bunny extends Sprite{

    //these are the member variables that make up id number, vision catagory, amount of drink to take in, amount of food to take
    //at the end there are also bunny needs and bunny health and lifetime to store the number of cycles the bunny endured
    int bunnyID, vision, drinktime, eattime, hunger = 1000, thirst = 1000, health = 900000, cycles, penalty;
    int[][] kernel;
    boolean busy, hungry, thirsty, alive;
    Tile collidingTile, centreOfBunnyMass, bestBunny, bestBunnyMass;

    //this is the go to sprite, the bunny will head towards this sprite if busy is set to true and go to is called
    ArrayList<Tile>targets = new ArrayList<Tile>();
    Tile choice;

    //constructor takes bunny id number, bunny X then Y locations, bunny is told the size of the world
    //in terms of x by y for logical reasons only, next is bunny vision type.
    public Bunny( int id, int bX, int bY, int v) throws Exception{

        super( "Bunny", "imgs/rabbit.png", 11, 15);

        vision = v;
        bunnyID = id;
        kernel = getVision();

        //position of best bunny
        int[] pos;

        //bunnies are alive by default
        alive = true;
        System.out.println( "Bunny number "+ id+ " at posX: " + bX + " posY: "+ bY + " is alive " + alive);
        //sprites are created in try catch blocks
        try{

            setXY( bX, bY);
            setAngle(0);
            setAcceleration(1);
            setmaxVelocity(3);
            setVelocity(1);
            eattime = 10;
            drinktime = 10;
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
        }catch( Exception e){

            System.out.println(" Bunny number " + bunnyID + " Is attempting to crash the program.");
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

    //go through whats available within range of bunny vision and determine a priority
    void priorities(){

        if( thirst >= hunger){

            for (int i = 0; i < targets.size(); i++) {

                if( targets.get(i).getName().equals("water")){

                    drink( targets.get(i));
                    break;
                }
            }
        }
        if( hunger >= thirst){
            for (int i = 0; i < targets.size(); i++) {

                //only if theres food will a bunny see that theres food present on the tile
                if( targets.size() != 0 &&  targets.get(i).getName().equals("grass")  && targets.get(i).food > 0){

                    eat( targets.get(i));
                    break;
                }
            }
        }
    }

    //displays bunny stats
    void displayState( Graphics g){

        g.setColor(Color.BLACK);
        drawString(g, "ID: " + bunnyID + "H: " + health +"F: " + hunger + "T: " + thirst, -(getWidth()/2), 0);
    }

    //each cycle the bunny should get hungrier, thirstier and more needs will tick down
    //untill they either reach zero and the bunny dies or they are topped up with eating or
    //drinking, also, health goes down a certain amount based on how hungry or thirsty the bunny is.
    void bunnyFatigue(){

        cycles++;

        int thirstTax = (1000 - thirst);
        int hungerTax = (1000 - hunger);

        //reduces health depending on whether the bunny is eating or drinking or not
        if( hungry){

            health -=  thirstTax;
            thirst--;
        }else if( thirsty){

            health -= hungerTax;
            hunger--;
        }else{

            health -= ( hungerTax + thirstTax);
        }

        //can have this detect the cause of death
        if( health <  0 || thirst < 0 || hunger < 0){

            alive = false;
            System.out.println( " bunny number " + bunnyID + " died, thirst = " + thirst +  " hunger " + hunger + " health " + health);
        }
    }

    //this function takes a few parameters that has the bunny taking
    //a random walk through the map
    void wander(){

    }

    //go to best bunny
    public Tile bestBunny(){

        return bestBunny;
    }

    //go to center of bunny mass
    public Tile centreOfBunnyMass(){

        return centreOfBunnyMass;
    }

    void eat( Sprite choice){

        if( goTo( choice)){

            //loop here x amount of cycles so that eating a specific amount has a specific cost on each bunny regarding
            //time, however bunny must not pay hunger cost while eating
            hungry = true;
            penalty ++;
            hunger++;
            setAcceleration(0);

            if( penalty > eattime){

                penalty = 0;
                hungry = false;
                busy = false;
                setAcceleration(2);
                choice = null;
            }
        }
    }

    //this function tells the bunny to go and get a drink
    void drink( Sprite choice){

        if( goTo( choice)){

            thirsty = true;
            penalty++;
            thirst++;
            setAcceleration(0);

            if( penalty > drinktime){

                System.out.println("done");
                penalty = 0;
                thirsty = false;
                busy = false;
                setAcceleration(2);
                choice = null;
            }
        }
    }

    //bunny is busy until arrives at target sprite
    boolean goTo( Sprite choice){

        if( !busy){

            pointToTwo(choice);
            if( !circularCollision( choice, 50)){

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
