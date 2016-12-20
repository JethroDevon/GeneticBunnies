import java.awt.Graphics;

class Tiles extends Sprite{

    //tiles across by tiles down, size of the tiles
    int mwidth, mheight, tilesize;

    //this will store each individual tile for logic and display
    Tile[][] tiles;

    //the kernel will allow an operation on surrounding tiles
    int[][] kernel = {{-1, -1},{ 0, -1},{ 1, -1},{-1, 0},{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1},{ 1, 1}};

    //constructor for tiles function
    public Tiles( int _width, int _height, int _size) throws Exception{

        super( "tilesheet", "imgs/tileIMGs.png", 4, 3);

        mwidth = _width;
        mheight = _height;
        tilesize = _size;

        try{

            tiles = new Tile[ mwidth][ mheight];

            for (int x = 0; x < mwidth; x++) {
                for (int y = 0; y < mheight; y++) {

                    //get frame 25, the one with grass on it
                    tiles[x][y] = new Tile( "grass " + String.valueOf(x + ( _width * y)), getFrame(0));

                    //sets x and y positions based on width and height in relation to other
                    tiles[x][y].setXY(x * tilesize, y * tilesize);
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
                gr2.drawImage( tiles[x][y].getFrame(0), tiles[x][y].getPosX(), tiles[x][y].getPosY(), tilesize, tilesize, null);
            }
        }
    }
}
