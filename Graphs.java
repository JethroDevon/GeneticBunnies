import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

class Graphs{

    //stores color of the graph
    Color color;

    //graph dimensions
    int sizeX, sizeY, x, y, scale;

    //this array list stores all the data to be displayed
    ArrayList<Integer> data = new ArrayList<Integer>();

    ArrayList<Integer> processedData = new ArrayList<Integer>();
    
    //title of the graph
    String title;
    
    //label for X
    String Xlabel;
    
    //label for Y
    String Ylabel;
    
    public Graphs( Color _c, String _title, String _xlab, String _ylab, int _x, int _y, int _sizex, int _sizey){

	color = _c;
	title = _title;
	Xlabel  = _xlab;
	Ylabel = _ylab;
	sizeX = _sizex;
	sizeY = _sizey;
	x = _x;
	y = _y;

	//scale is at ten by default
	scale = 10;
    }

    public void addEntry( int entry){

	data.add( new Integer( entry));
	
	processedData.clear();
	for (int i = 0; i < data.size(); i++) {

	    processedData.add(scaleData( data.get(i)));
	}
    }

    public void drawGraph( Graphics g){

	if(data.size() > 0){
	    	
	    int verticalSpace = sizeX/data.size();
	    int range = sizeY/100;
 	
	    g.setColor( color);
	    g.drawString( title, x + sizeX/2, y + 20 );

	    for (int i = 0; i  < processedData.size() -1; i++) {

		g.drawLine( verticalSpace * i, (y + sizeY) - (processedData.get(i) * range), verticalSpace * (i + 1), (y + sizeY) - (processedData.get(i + 1) * range));
	    }
	}

	//trim data array down so to as not to make it so hard to read
	if ( data.size() > 70) {

	    for (int i = 0; i < 10; i++) {

		data.remove(i);
	    }
	}
    }

    public int getLowest(){

	int temp = 999999;
	for (int i = 0; i < data.size(); i++) {

	    if( temp > data.get(i)){

		temp = data.get(i);
	    }
	}

	return temp;
    }

    public int getHighest(){

	int temp = 0;
	for (int i = 0; i < data.size(); i++) {

	    if ( temp < data.get(i)) {

		temp = data.get(i);
	    }
	}

	return temp;
    }

    //scales all data to a range between 0 and 100, this is so that the graphs lowest value becomes
    //zero and its highest becomes 100, I intend to multiply the results by a hundredth of the hight
    //of the graphs display height
    public int scaleData(int _x){

	int range = getHighest() - getLowest();
	int scale = 100 * (_x - getLowest());

	if (scale == 0) {

	    scale = 1;
	}
	if (range == 0) {

	    range = 1;
	}

	return scale/range;
    }

    public void resize( int sizex, int sizey){

    }
}
