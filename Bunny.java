import java.awt.Graphics;

class Bunny extends Sprite{

    //these are the member variables that make up
    int bunnyID;

    //constructor takes bunny id number, bunny X then Y locations
    //
    public Bunny( int id, int bX, int bY) throws Exception{

        super( "Bunny", "/imgs/rabbit.png", 14, 8);

        bunnyID = id;

        //sprites are created in try catch blocks
        try{

            setXY( bX, bY);
            addState( "STATION", 3, 4, 40, 40, 0, 0, 0, 0);
            addState( "UP", 90, 98, 40, 40, 2, 1, 2, 90);
            addState( "DOWN", 90, 98, 40, 40, 2, 1, 2, 90);
            addState( "LEFT", 90, 98, 40, 40, 2, 1, 2, -90);
            addState( "RIGHT", 90, 98, 40, 40, 2, 1, 2, 0);
            addState( "UPLEFT", 90, 98, 40, 40, 2, 1, 2, 180);
            addState( "UPRIGHT", 90, 98, 40, 40, 2, 1, 2, 135);
            addState( "DOWNLEFT", 90, 98, 40, 40, 2, 1, 2, -45);
            addState( "DOWNRIGHT", 90, 98, 40, 40, 2, 1, 2, -135);
            
        }catch( Exception e){

            System.out.println(" Bunny number " + bunnyID + " Is attempting to crash the program.");
        }

    }
}
