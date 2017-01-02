import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;

class Tiles extends Sprite{

    //tiles across by tiles down, size of the tiles
    int mwidth, mheight, tilesize;

    //this will store each individual tile for logic and display
    Tile[][] tiles;

    //this kernel is for work with place water function, it alter tiles at an x y cordinate and
    //draw a watering hole in their place
    int[][] kernel = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1},{ 1, 1}};

    //constructor 1 for tiles function, has default water location
    public Tiles( int _width, int _height) throws Exception{

        super( "tilesheet", "imgs/tileIMGs.png", 4, 3);

        mwidth = _width;
        mheight = _height;

        try{

            tiles = new Tile[ mwidth][ mheight];

            for (int x = 0; x < mwidth; x++) {
                for (int y = 0; y < mheight; y++) {

                    //get frame 25, the one with grass on it
                    tiles[x][y] = new Tile( "grass", getFrame(0));
                    tiles[x][y].setID(String.valueOf(x + ( _width * y)));
                    //give the tiles their index numbers
                    tiles[x][y].xtile = x;
                    tiles[x][y].ytile = y;

                    tilesize = tiles[x][y].getWidth();

                    //sets x and y positions based on width and height in relation to other
                    tiles[x][y].setXY(x * tilesize, y * tilesize);
                    tiles[x][y].setWH( tilesize, tilesize);
                }
            }

            createWateringHole( mwidth/3, mheight/3);
        }catch( Exception e){

            System.out.println("Error generating map tiles");
        }
    }

    //draws the grid onto a graphics object passed into args
    public void drawGrid(Graphics gr2){

        gr2.setColor( Color.red);
        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[x].length; y++){


                //draws graphics to pased graphics variable, each tile is being drawn here
                gr2.drawImage( tiles[x][y].getFrame(0), tiles[x][y].getPosX(), tiles[x][y].getPosY(), tilesize, tilesize, null);
                //tiles[x][y].drawString(gr2,tiles[x][y].getName(),0,0);
                //gr2.drawRect( tiles[x][y].getPosX(), tiles[x][y].getPosY(), tiles[x][y].getWidth(), tiles[x][y].getHeight());
            }
        }
    }
    public void showFood( Graphics g, ArrayList<Bunny>bswarm, ArrayList<Bunny>deadbun){

        //shows all food on the map
        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[x].length; y++){

                if(tiles[x][y].getName() == "grass" && tiles[x][y].food > 0)
                tiles[x][y].drawString(g, String.valueOf(tiles[x][y].food), 0, 20);
            }
        }

        //subtracts eaten food from the map
        for (int i = 0; i < bswarm.size(); i++) {
            for( int f = 0; f < bswarm.get(i).eatenTiles.size(); f++){
                for(int x = 0; x < tiles.length; x++){
                    for(int y = 0; y < tiles[x].length; y++){

                        if( bswarm.get(i).eatenTiles.get(f)!= null && tiles[x][y].checkCollision(bswarm.get(i).eatenTiles.get(f))){

                            tiles[x][y].food -= 10;
                        }
                    }
                }
            }
            bswarm.get(i).eatenTiles.clear();
        }

        //returns food onto the map from dead bunnies
        for (int i = 0; i < deadbun.size(); i++) {

            if(deadbun.get(i).foodEaten > 0){
                for(int x = 0; x < tiles.length; x++){
                    for(int y = 0; y < tiles[x].length; y++){

                        if(tiles[x][y].checkCollision(deadbun.get(i))){

                            tiles[x][y].food += deadbun.get(i).foodEaten;
                            deadbun.get(i).foodEaten = 0;
                        }
                    }
                }
            }
        }
    }

    public void createWateringHole( int _x, int _y){

        try{

            tiles[_x][_y].changeTileType( "water", getFrame(3));
            tiles[_x+1][_y].changeTileType( "water", getFrame(6));
            tiles[_x+2][_y].changeTileType( "water", getFrame(1));
            tiles[_x][_y+1].changeTileType( "water", getFrame(7));
            tiles[_x+1][_y+1].changeTileType( "water", getFrame(5));
            tiles[_x+2][_y+1].changeTileType( "water", getFrame(8));
            tiles[_x][_y+2].changeTileType( "water", getFrame(4));
            tiles[_x+1][_y+2].changeTileType( "water", getFrame(9));
            tiles[_x+2][_y+2].changeTileType( "water", getFrame(2));
        }catch(Exception e){

            System.out.println("Error making sandPatch!");
        }

    }

    public int getWidth(){

        return tilesize * mwidth;
    }

    public int getHeight(){

        return tilesize * mheight;
    }
}
