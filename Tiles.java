import java.awt.Graphics;

class Tiles extends Sprite{

    //tiles across by tiles down
    int mwidth, mheight;

    //this will store each individual tile for logic and display
    Tile[][] tiles;

    //the kernel will allow an operation on surrounding tiles
    int[][] kernel = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1},{ 1, 1}};


    public Tiles( int map_width, int map_height) throws Exception{

        super( "tilesheet", "imgs/tileIMGs.png", 4, 3);
        mwidth = map_width;
        mheight = map_height;

        try{

            tiles = new Tile[ mwidth][ mheight];

            for (int x = 0; x < mwidth; x++) {
                for (int y = 0; y < mheight; y++) {

                    //get frame 25, the one with grass on it
                     tiles[x][y] = new Tile( "grass " + String.valueOf(x + ( map_width * y)), getFrame(0));
                }
            }
        }catch( Exception e){

            System.out.println("Error generating map tiles");
        }
    }

    //draws the grid onto a graphics object passed into args
    public void drawGrid(Graphics gr2){

        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[x].length; y++){

                //draws graphics to pased graphics variable, each tile is being drawn here
                gr2.drawImage( tiles[x][y].getFrame(0), tiles[x][y].getPosX(), tiles[x][y].getPosY(), tiles[x][y].getWidth()-1, tiles[x][y].getHeight()-1, null);
            }
        }
    }


}
