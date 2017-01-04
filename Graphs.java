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
    }

    public void drawGraph( Graphics g){

	if(data.size() > 1){
	    
	    g.setColor( Color.BLACK);
	    g.fillRect( x , y, sizeX, sizeY);
	    g.setColor( Color.WHITE);
	    g.drawString( title, x + sizeX/2, y - 20);	    
	
	    //horizontal lines are just the height of the graph divided by ten however the vertically lines
	    //will be the same as the number of data entries until there is ten
	    int horizontalSpace = sizeX/10;
	    int verticalSpace = sizeY/data.size();
	
	    for (int i = 0; i < sizeX; i += horizontalSpace) {

		g.drawLine( x, y + i, sizeX, y + i );
	    }

	    //only draw the vertical lines if there is less than sizeX/4, other wise the graph would look bad
	    if( data.size() < sizeX/4){
		for (int i = 0; i < sizeY; i += verticalSpace) {

		    g.drawLine( i, y, i, sizeY);
		}
	    }

	    //the graph displayed will use the existing data to show each point based on the highest so far as at 100%
	    if( data.size() > 0){

		//gets highest point of all data for the graphs scale
		for (int i = 0; i < data.size(); i++) {

		    if( data.get(i) > scale){

			scale = data.get(i);
		    }
		}   
	    }

	
	    int scaleratio = sizeX/scale;
	
	    //create an array with a set of scaled points
	    int[] point = new int[ data.size()];

	    for (int i = 0; i < data.size(); i++) {

		point[i] = scaleratio * data.get(i);
	    }
	
	    g.setColor( color);

	    for (int i = 0; i < data.size() -1; i++) {

		g.drawLine( point[i], verticalSpace * i, point[i+1], verticalSpace * (i + 1));
	    }
	}
    }

    public void resize( int sizex, int sizey){

    }
}
