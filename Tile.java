import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


class Tile extends Sprite{

    public ArrayList<Tile> neighbours = new ArrayList<Tile>();

    //this will stand in for now
    String cache;

    int xtile, ytile, food = 100;

    String ID;

    public Tile(String _name, BufferedImage _img) throws Exception{

        //super takes the tile image as an argument
        super( _name, _img);
    }

    void changeTileType( String tileType, BufferedImage img){

        setName( tileType);
        replaceFrame( img, 0);
    }

    void setID( String _id){

        ID = _id;
    }

    String getID(){

        return ID;
    }
    //this takes food from the tiles when eaten is called on them
    int beEaten( int amount){

        if( amount < food){

            food -= amount;
            return amount;
        }else{

            int temp = food;
            food = 0;
            return temp;
        }
    }
}
