/*

	Author: Jethro Holcroft.
	Start Date: 16/01/2016.

	The purpose of this class is to draw animated sprites that are easy to use in a game type environment, these sprites should be able to function as projectiles, pickups
	animated caracters that are controled by the player with keys or mouse, animations in the background or effects such as explosions, buttons, scrollable text boxes or even as a splash screen.

	This class will contain functions that allow detection of collisions with other objects of Sprite, returns the angle between this Sprite and other objects of type sprite

	Objects of this class will contain an array of images, these will be frames for animation, integers to store height width and positions.

	Each object will be initialised with a name, this is so the object can be assigned a unique ID for various reasons, it will also help identify this object in debug messages.

	Comments will reference changes of state or conditions, a sub class called stateData manages these,
	a state is the frames for an animation to loop between, the position to draw the next iteration, the width and height dimensions if they need to change,

	in this case a state will these details and addState() will contain arguments that will add a new state to sprite
	if no states are added, then only the default state will be used, the width and height of the object will be default and based on the size of the frames.
	A condition is an angle speed or velocity with data on which frames to loop through in case the sprites condition matches one saved in the array list 'conditions',
	'addAngle/speed/velocityCondition()' are three seperate functions that will add conditions, pollConditions() will do the checking to see if a condition has changed

	it is recomended that objects of this class will be stored in array lists or singularly in a wrapper class that is likley to be used in the main application loop using an MVC style functionality, however a lone
	object right at the start of a program can be used as a splash screen, a timer class and a start stop boolean will be included to aid extra functionality.

	Emitters are particle fountains, the instructions for setting up and using are near line 1000 and the comments more or less explain how they are used, further explantions also in documentation soon to be included

	an additional button and menu class will also be incuded in the sprite class along with a special constructor, this is to take advantage of some of the functionality of the sprite class to add a little
	gui support, therefore default font variables and font loading support will also be included.

	There will be a lot of debug messages and error handling, this is to help make this class easy to use.

	comments will aim to explain variables and functions as though the reader is learning how to use the code, and functions and variables will be written to best describe their purpose and allow as readable code
	as possible, however a lot of Javas paint methods and graphics functionality/overides are best loosley referenced to.

	Gravity and Thrust functions have been added, if yo uchoose gravity mode a gravity angle and strength has to be set, default is zero, adding thrust acceleration will temporarily cancel gravity effects
	and can be used for jumping or for lander style physics, setting gravity angle to be always at the center of another sprite using point to function will create a gravity well

																							TO-DO:

	add subclasses that will use this sprite class to create a menu button array, text buttons, consider having an inventory style menu!

	add sound functionality, call in state and condition change, have sound loaded in state data
*/

import java.util.BitSet;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.awt.Font;
import java.lang.Math;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.util.Random;


public class Sprite{

	//this stores a contact sheet image, this will contain all frames of animation in one image
	private BufferedImage contactSheet;

	//this contains each frame of animation in the form of an array.
	private ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();

	//stores various states for the sprite
	private ArrayList<stateData> states = new ArrayList<stateData>();

	//stores conditions of the sprite, calling 'pollState()' on each object in this array list
	//and inputing angle, speed or velocity of in args will return true or false if that condition has been stored,
	//and this sprites angle, speed or velocity matches
	public ArrayList<stateData> conditions = new ArrayList<stateData>();

	//this array will contain an array of different partical Emitters, particles or emiiters themselves can be accessed by their names with 'getPrticle()' or 'getEmitter()'
	//it is very important that particles and emitters use a strict naming convention when created and used in order to keep track of what is going on
	public ArrayList< Emitter> emitters = new ArrayList< Emitter>();

	//these store dimensions of the sprite, frame stores the present frameNum the sprite will draw, frameStart and frameEnd are the start and end loops the
	//animation will cycle through, default state has start at 0 and end at the number of frames, deltaX and Y are for 'deltaMove()' this function draws the sprite as normal but with added delta values
	//this can be used for moving grids of sprites in relation to something
	private int posX, posY, width, height, frameNum, frameStart, frameEnd, deltaX, deltaY;

	//maxVelocity, velocity, angle and acceleration are for smooth movement functions of the sprite, acceleration is to add a spot of easing to the movement functions
	//to create a natural movement maxVelocity is the set speed velocity will build up or down to based on acceleration
	private float maxVelocity, velocity, acceleration, gravityAcceleration;

	//angle variable, must be a double for easy conversion to degrees
	private double angle, gravityAngle, thrustAngle, gravity, thrustAcceleration;

	//these are to check if the sprite is colliding with another sprite/colliding has been set to true, if the mouse is over the spite and if the sprite
	//has been selected (mouse over sprite and a mouse click has been detected selected is toggled to true and false), gravity mode applies top down gravity
	//vector each time moveSPrite is called if it is set to true
	private boolean colliding, collideAbove, collideBelow, collideLeft, collideRight, mouseOver, constantSpeed, gravityMode;
	private int boundsLeft, boundsRight, boundsTop, boundsBottom;

	public boolean selected;

	//this stores the present state of the object, its set to default. name identifies object
	private String state = "default", name;

	//default font is 'serif'
	private Font font = new Font("serif", Font.PLAIN, 30);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS; Several different types of constructor for several different types of sprite, the first is a standard animated sprite, the second takes an argument that determines  //
	//	what kind of sprite to produce based on the argument containting 'functionality'																								  //
	//																																													  //
	//	The second two are for plain images one takes a path to an image the other an image passed into it, this is for dynamic initialisation											  //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//This constructor is for a standard animated sprite,
	//constructor requires the image name in the form of a string, a path to load the image from, and the rows and columns the contact sheet is split up into.
	//this constructor can throw an exception if it is not initialised properly.
	public Sprite( String _name, String _path, int _rows, int _cols) throws Exception{

		try{

			//assign name
			name = _name;

			//temporary load image function
			contactSheet = ImageIO.read( new File( _path));

			//init frames array
			initFrames( _rows, _cols);

			//set default state of sprite, if an uneven table this will need to be over written
			//to create an alternative default state that will not draw the blank frames
			addState( "default", 0, ( _rows * _cols) - 1, height, width, 0, 0, 0, 0);

			//activate the default state
			activateState("default");

		}catch(Exception e){

			System.out.println("Initialisation of " +  _name + " failed: " + e.toString());
		}

	}


	public Sprite(String _functionality, String _name, String _path, int _rows, int _cols) throws Exception{

		try{

			//assign name
			name = _name;

			//temporary load image function
			contactSheet = ImageIO.read(new File(_path));

			//init frames array
			initFrames(_rows, _cols);

			//set default state of sprite
			addState( "default", 0, ( _rows * _cols) - 1, height, width, 0, 0, 0, 0);

			//activate the default state
			activateState("default");

		}catch(Exception e){

			System.out.println("Initialisation of " +  _name + " failed: " + e.toString());
		}

	}

	//this constructor is for a non animated sprite, just a rectangular image, this constructor
	//takes a buffered image as an arg
	public Sprite(String _name, BufferedImage _img) throws Exception{

		try{

			//assign name
			name = _name;

			//initialises straight from args
			contactSheet = _img;

			//set default state of sprite
			addState( "default", 0, 0, height, width, 0, 0, 0, 0);

			//init frames array
			initFrames( 1, 1);

		}catch(Exception e){

			System.out.println("Initialisation of " +  _name + " failed: " + e.toString());
		}

	}

	//takes one image from path, no animation
	public Sprite(String _name, String _path) throws Exception{

		try{

			//assign name
			name = _name;

			//temporary load image function
			contactSheet = ImageIO.read(new File(_path));

			//set default state of sprite
			addState( "default", 0, 0, height, width, 0, 0, 0, 0);

			//init frames array
			initFrames( 0, 0);

		}catch(Exception e){

			System.out.println("Initialisation of " +  _name + " failed: " + e.toString());
		}

	}

	//this function initialises the array list frames with images cut from the contactSheet and will be called in the constructor,
	//rows and cols along with the dimensions of contactSheet are used to work out the size of each frame to cut out, this only works if the
	//contactSheet contains images divided evenly into rows and columns and also in the same relative position within it.
	//the function returns a boolean if it was successful or not.
	private void initFrames(int _rows, int _cols){

		//this initialises the width and heigh of each sub image for use cutting up the contact sheet, it does not have to be the width and height
		//of the sprite itself however it will remain so unless any states are added.
		width = contactSheet.getWidth() / _cols;
		height = contactSheet.getHeight() / _rows;

		for(int r = 0; r < _rows; r++ ){
			for(int c = 0; c < _cols; c++){

				//this function uses the available variables to slice up the contact sheet in even parts
				frames.add( contactSheet.getSubimage( c * width, r * height, width, height));
			}
		}
	}

	//this function adds a state to the sprite, the first state is always the default state, other states are added by the user, if you want
	//a value to stay the same when you add it use the state in args for example in height use sprite.getHeight() in your code.
	//this will also overwrite a state with the same name
	public void addState( String _stateName, int _fStart, int _fEnd, int _height, int _width, float _mvel, float _vel, float _acc, double _ang){


		//loops through states search for statename allready
		for(int x = 0; x < states.size() -1; x++){

			//if state name allrady exists
			if(states.get(x).dName == _stateName){

				//delete that state and break out the loop
				states.remove(x);

				//message alerts to name and state that is over written
				System.out.println(name + " state " + _stateName + " over written with new perameters!");

				break;
			}
		}

		//add state to
		states.add(new stateData( _stateName, _fStart, _fEnd, _height, _width, _mvel, _vel, _acc, _ang));
	}

	//this function activates the state with the same string passed in args, if it is not found, debug error message is returned
	//and default state is set instead
	public void activateState(String _setStateTo){

		//if found is never set to true, state was never found and default state is used
		boolean found = false;

		//loops for each state in states array
		for(stateData sd: states){

			//if state with same name in args is found then
			if(sd.dName == _setStateTo){

				//change frames and size of sprite
				frameStart = sd.dStart;
				frameEnd = sd.dEnd;
				width = sd.dWidth;
				height = sd.dHeight;
				maxVelocity = sd.dmaxVelocity;
				velocity = sd.dvelocity;
				acceleration = sd.dacceleration;
				angle = sd.dangle;

				//set to animate frames from new states animation loop
				frameNum = frameStart + 1;

				found = true;
				break;
			}
		}

		//if not found then set to default state
		if( !found){

			frameStart = 0;
			maxVelocity = 0;
			velocity = 0;
			angle = 0;
			acceleration = 0;
			frameEnd = states.get(0).dEnd;
			width = states.get(0).dWidth;
			height = states.get(0).dHeight;
		}
	}

	//as above function however if the state is allready active then the function is not called at all
	public void continue_activateState(String _setStateTo){

		if( !(getState().equals(_setStateTo))){

			activateState(_setStateTo);
		}
	}

	//this function also activates state almost the same as above however it does not change
	//velocity, maxVelocity, angle or speed, this is to allow for just changing the frames
	//if the conditions change, ideal for moving sprites with keys as keys are held down
	public void semi_activateState( String _setStateTo){

		//if found is never set to true, state was never found and default state is used
		boolean found = false;

		//set temp variables
		float _maxVelocity = getmaxVelocity();
		float _velocity = getVelocity();
		float _acceleration = getAcceleration();
		double _angle = getAngle();

		//call activate state as normal
		activateState(_setStateTo);

		//set member variables with tems as they were
		setmaxVelocity(_maxVelocity);
		setVelocity(_velocity);
		setAcceleration(_acceleration);
		setAngle(_angle);
	}

	//adds angle condition to conditions array
	public void addAngleCondition(double _startAngle, double _endAngle, int _frameStart, int _frameEnd){

		conditions.add(new stateData(_startAngle, _endAngle, _frameStart, _frameEnd));
	}

	//adds speed condition to the conditions array
	public void addSpeedCondition(int _speedStart, int _speedEnd, int _frameStart, int _frameEnd){

		conditions.add(new stateData(_speedStart, _speedEnd, _frameStart, _frameEnd));
	}

	//adds velocity conditions to the conditions array
	public void addVelocityCondition(boolean isIncreasing, int _velocity, int _frameStart, int _frameEnd){

		conditions.add(new stateData( isIncreasing, _velocity, _frameStart, _frameEnd));
	}

	//adds velocity conditions to the conditions array
	public void addJumpCondition( double _thrustAccelMin, double _thrustAngleMin, double _thrustAccelMax, double _thrustAngleMax, int _frameStart, int _frameEnd){

		conditions.add(new stateData(  _thrustAccelMin, _thrustAngleMin, _thrustAccelMax, _thrustAngleMax, _frameStart, _frameEnd));
	}

	//loops the conditions array to check if sprite matches any one of the saved conditions, if it does frame start and
	//frame end are updated
	public void pollConditions( String _condition){

		//for each connection
		for(int x = 0; x < conditions.size(); x++){

			//call each condition based on input
			conditions.get( x).checkCondition( _condition);
		}
	}

	//this function returns the next frame in the animation loop
	public BufferedImage nextFrame(){

		//if the frame is not at the end of the loop
		if(frameNum < frameEnd-1){

			//iterate present frame by one and return it
        	frameNum++;
        	return frames.get(frameNum);
    	}else{

    		//reset frame to first frame in loop and return it
        	frameNum = frameStart;
        	return frames.get(frameNum);
    	}
	}

	//gets present frame
	public BufferedImage getFrame(){

		return frames.get(frameNum);
	}

	//overloaded gets frame in args
	public BufferedImage getFrame(int _frame){

		if(_frame < frames.size()){

			return frames.get(_frame);
		}

		System.out.println( "Frame not found");
		return frames.get(0);
	}

	//iterates next frame in loop but does not return it
	public void nextFrame_noReturn(){

		//if the frame is not at the end of the loop
		if(frameNum > 0){

			//iterate present frame by one
        	frameNum++;
    	}else{

    		//reset frame to first frame in loop
        	frameNum = frameStart;
    	}
	}

	//this function returns the previous frame in the animation loop
	public BufferedImage previousFrame(){

		//if the frame is not at the end of the loop
		if(frameNum > 0){

			//iterate present frame by one and return it
        	frameNum--;
        	return frames.get(frameNum);
    	}else{

    		//reset frame to first frame in loop and return it
        	frameNum = frames.size();
        	return frames.get(frameNum);
    	}
	}

	//adds a new frame to frame array
	public void addFrame( BufferedImage _img){

		frames.add( _img);
	}

	//replaces a frame, first arg is frame second arg is position in array if frame to replace
	public void replaceFrame( BufferedImage _img, int _index){

		frames.set(_index, _img);
	}

	//this function detects a collision with the sprite that is passed in args, if there is a collision
	//colliding is set to true else false
	boolean checkCollision( Sprite _spr){

		//this is a standard collision dectetion algorithm using the sprites dimensions
		if(getPosX() + getWidth() > _spr.getPosX() && getPosX() < _spr.getPosX() + _spr.getWidth() &&
       getPosY() + getHeight() > _spr.getPosY() && getPosY() <  _spr.getPosY() + _spr.getHeight()){

			colliding = true;
			return true;
		}else{

			colliding = false;
			return false;
		}
	}

	//this function checks if a collision was just to he left of the sprite
	boolean checkCollisionLeft( Sprite _spr){

        //creates the dimenstions for a box to the right of the sprite
        int xbox = getPosX() -1;
        int ybox = getPosY();
        int wbox = 3;
        int hbox = getHeight();

        //checks if sprite passed in arguments is colliding and returns true or false respecivley
        if( xbox > _spr.getPosX() && xbox + wbox > _spr.getPosX() && ybox + hbox < _spr.getPosY() && ybox > _spr.getPosY()){

            return true;
        }else{

            return false;
        }
	}

	//this function checks if the collision was just to he left of the sprite
	boolean checkCollisionRight( Sprite _spr){

        //creates the dimenstions for a box to the right of the sprite
        int xbox = getPosX() +1;
        int ybox = getPosY();
        int wbox = 3;
        int hbox = getHeight();

        //checks if sprite passed in arguments is colliding and returns true or false respecivley
        if( xbox > _spr.getPosX() && xbox + wbox < _spr.getPosX() && ybox + hbox < _spr.getPosY() && ybox > _spr.getPosY()){

            return true;
        }else{

            return false;
        }

    }

	//this function checks if the collision was just to he left of the sprite
	boolean checkCollisionAbove( Sprite _spr){

        //creates the dimenstions for a box to the right of the sprite
        int xbox = getPosX();
        int ybox = getPosY() -1;
        int wbox = getWidth();
        int hbox = 3;

        //checks if sprite passed in arguments is colliding and returns true or false respecivley
        if( xbox + wbox < _spr.getPosX() && xbox > _spr.getPosX() && ybox + hbox < _spr.getPosY() && ybox > _spr.getPosY()){

            return true;
        }else{

            return false;
        }

	}

	//this function checks if the collision was just to he left of the sprite
	boolean checkCollisionBelow( Sprite _spr){

        //creates the dimenstions for a box to the right of the sprite
        int xbox = getPosX();
        int ybox = getPosY() +1;
        int wbox = getWidth();
        int hbox = 3;

        //checks if sprite passed in arguments is colliding and returns true or false respecivley
        if( xbox + wbox < _spr.getPosX() && xbox > _spr.getPosX() && ybox + hbox < _spr.getPosY() && ybox > _spr.getPosY()){

            return true;
        }else{

            return false;
        }

	}

	//the following collisions detect if the player has left bounds that can be set in the constructor after the sprite
	//object is created, this is meant for detecting if the sprite, is leaving left the bounds of the screen
	public boolean leftBound(){

		if( getPosX() <= boundsLeft){

			return true;
		}else{

			return false;
		}
	}

	public boolean rightBound(){

                if( getPosX() >= boundsRight){
                        return true;
                }else{

                        return false;
                }
        }

	public boolean bottomBound(){

                if( getPosY() <= boundsBottom){

                        return true;
                }else{

                        return false;
                }
        }

	public boolean topBound(){

                if( getPosY() >= boundsTop){

                        return true;
                }else{

                        return false;
                }
        }

	//this function detects a circular collision with the centre of the sprite that is passed in args, the second argument is the diameter of
	//the circle to collid with
	boolean circularCollision(Sprite _spr, int _radius){

		int squarex = deltaX + getPosX() + (getWidth()/2) - _spr.getPosX() + (getWidth()/2);
		int squarey = deltaY + getPosY() + (getHeight()/2) - _spr.getPosY() + (getHeight()/2);

		//this is a standard circular collision detetion algorithm using the sprites dimensions, the circular collision will collid with the center of
		//this sprite from a circle eminating from the center of the sprite to check
		if(Math.sqrt( (squarex * squarex) + (squarey * squarey)) < _radius){

			colliding = false;
			return true;
		}else{

			colliding = true;
			return false;
		}
	}

	//this function will set the angle to be pointing towards the sprite in args
	public void pointTo( Sprite _sprite){

		double angleTo = Math.toDegrees(Math.abs( Math.atan2( getPosX() - _sprite.getPosX(), getPosY() - _sprite.getPosY())));

		//inverse the angle as it is relative to
		setAngle( angleTo);
	}

	//this function will set the angle to be pointing towards the sprite in args
	public double getAngleTo( Sprite _sprite){

		return Math.toDegrees( Math.atan2( _sprite.getPosY() - getPosY(), _sprite.getPosX() - getPosX()));
	}


	//this function moves the sprite one unit towards its present vector each time it is called each time it is called
	public void moveSprite(){

		//set maxVelocity based on acceleration
		accelerate();

		//if gravity mode is not active
		if( gravityMode){

			//adds gravitational downward acceleration if sprite is not colliding with anything
			gravityAcceleration += gravity;

			if( colliding){

				gravityAcceleration = 0;
			}

			if( thrustAcceleration > 0){

				thrustAcceleration -= gravity;

				//zeros thrust if it goes under zero
				if(thrustAcceleration < 0){

					thrustAcceleration = 0;
				}
			}

			/// IF THIS WORKS CREATE VECTOR ADDING FUNCTION

			//adding thrust forces to movement vector
			setXY( posX +=  thrustAcceleration * Math.cos( Math.toRadians( thrustAngle)), posY +=  thrustAcceleration * Math.sin( Math.toRadians( thrustAngle)));

			//adding gravity to movement vector
			setXY( posX +=  gravityAcceleration * Math.cos( Math.toRadians( gravityAngle)), posY +=  gravityAcceleration* Math.sin( Math.toRadians( gravityAngle)));

			//increment positionX and Y using trig, I always found this link exceptionally usefull http://www.helixsoft.nl/articles/circle/sincos.htm
			//when first learning to use this
			setXY( posX += velocity * Math.cos(Math.toRadians(angle)), posY += velocity * Math.sin(Math.toRadians(angle)));
		}else{

			//increment positionX and Y using trig, I always found this link exceptionally usefull http://www.helixsoft.nl/articles/circle/sincos.htm
			//when first learning to use this
			setXY( posX += velocity * Math.cos(Math.toRadians(angle)), posY += velocity * Math.sin(Math.toRadians(angle)));
		}
	}



	//superficially moves the sprite with its present position plus a delta value
	public void deltaMove(){

		//adds delta value to existing value
		setXY( posX + deltaX, posY + deltaY);
	}

	//manages acceleration easing if speed has not been set to constant
	public void accelerate(){

		//is this less overhead than a single if statment with this block nested in?
		if(velocity < maxVelocity && !constantSpeed){

			velocity += acceleration;
		}else if(velocity > maxVelocity && !constantSpeed){

			velocity -= acceleration;
		}
	}

	//will display text at an x and y distance from the top left corner from the sprite
	//function takes a graphics object to draw to, the text to display and the the x and y
	//offsets, remember to call after the sprite has been drawn to not draw the sprite over the text
	public void drawString(Graphics g2, String _text, int _x, int _y){

		g2.drawString( _text, getPosX() + _x, getPosY() + _y);
	}

	//////////////////////////////
	//	Getters and Setters	   	//
	//////////////////////////////

	public int getFrameNumber(){

		return frameNum;
	}

	//setting  sprites bounds collisions
	public void setAllBounds(int _left, int _right, int _top, int _bottom){

		boundsLeft = _left;
		boundsRight = _right;
		boundsTop = _top;
		boundsBottom = _bottom;
	}

	//the object can just be set to collide for other collision situations
	public void setCollision(boolean _collide){

		colliding = _collide;
	}

	//this functin returns true of the object is colliding and false if it is not
	public boolean getCollision(){

		return colliding;
	}

	//returns width
	public int getWidth(){

		return width;
	}

	//returns height
	public int getHeight(){

		return height;
	}

	//returns position X
	public int getPosX(){

		return posX;
	}

	//returns position Y
	public int getPosY(){

		return posY;
	}

	//returns angle
	public double getAngle(){

		return angle;
	}

	//returns maxVelocity
	public float getmaxVelocity(){

		return maxVelocity;
	}

	//returns maxVelocity
	public float getVelocity(){

		return velocity;
	}

	//returns acceleration
	public float getAcceleration(){

		return acceleration;
	}

	//returns name
	public String getName(){

		return name;
	}

	//sets name
	public void setName(String _name){

		name = _name;
	}

	//sets angle
	public void setAngle(double _angle){

		angle = _angle;
	}

	//set width
	public void setWidth(int _width){

		width = _width;
	}

	//set height
	public void setHeight(int _height){

		height = _height;
	}

	//this re-sets the X and y positions
	public void setWH( int _w, int _h ){

		width = _w;
		height = _h;
	}

	//sets the present speed for the sprite
	public void setVelocity(float _Velocity){

			velocity = _Velocity;
	}

	//sets the max speed for the sprite
	public void setmaxVelocity(float _maxVelocity){

			maxVelocity = _maxVelocity;
	}

	//sets acceleration, there is a danger of dividing by zero if movement functions are not robustly made
	//please be observant
	public void setAcceleration(float _acceleration){

			acceleration = _acceleration;
	}

	//this function returns the present state of the object
	public String getState(){

		return state;
	}

	//set speed to be constant or eased with velocity
	public void toConstantSpeed(boolean _cspeed){

		constantSpeed = _cspeed;
	}

	//this re-sets the X and y positions
	public void setXY( int _x, int _y ){

		posX = _x;
		posY = _y;
	}

	//sets an offset for a sprite or a range of sprites
	public void setDelta( int _dx, int _dy){

		deltaX = _dx;
		deltaY = _dy;
	}

	public boolean getGravityMode(){

		return gravityMode;
	}

	public void setGravityMode( boolean _gm){

		gravityMode = _gm;
	}

	public void setGravity( double _gravity){

		gravity = _gravity;
	}

	public void setGravityAngle( double _gA){

		gravityAngle = _gA;
	}

	public double getGravity(){

		return gravity;
	}

	public double getGravityAngle(){

		return gravityAngle;
	}

	public void setThrustAngle( double _tA){

		thrustAngle = _tA;
	}

	public double getThrustAngle(){

		return thrustAngle;
	}

	public void setThrustAcceleration( double _tA){

		thrustAcceleration = _tA;
	}

	public double getThrustAcceleration(){

		return thrustAcceleration;
	}

	/*******************************************************************************************************************************
						Emitters are a sub class that manage an array of sprites to create a particle fountain or projectile launch effect

														Following functions will be related to following sub class 'Emitter'

		AddEmitter( String, String, double, int, int, double, int, boolean, int, int) is a function in sprite that should be called when the sprite is created,
		'fireEmitter("emitters name")'' or 'activateFountain(" emitters name")' will acivate preset behaviour that will be contained in the arguments of 'addEmitter()', 'drawEitter( "NAME OF EMITTER")'' will
		draw each of that emitters particals and 'detectParticleCollision( "NAME_OF_EMITTER", Sprite _sprite)' will check the emitter with the name
		in args one for collisions with each particle it owns against the sprite passed in args two.


		particals are still destroyed after a certain amount of distance from the emitter is reached, this is because even though java does do garbage collection
		java would still try and draw each and every particle unless it was removed at some point, it is also possible to give them a life time	with a get ticks object.

	**********************************************************************************************************************************/

	//adds a new emitter to the sprite
	public void addEmitter( String _name, String _path, int _error, int _burst, int _burstrate, double _angle, int _vel, boolean _grav){

		emitters.add( new Emitter(  _name, _path, _error, _burst, _burstrate, _angle, _vel, _grav));
	}

	//fires one particle from the emitter
	public void fireEmitter( String _name){

		for( int e = 0; e < emitters.size() ; e++){

			if( emitters.get( e).getEmitterName() == _name){

				emitters.get( e).fire();
			}
		}
	}

	//draws particles in Emitter
	public void drawEmitter( String _name, Graphics _gr2){

		for( int e = 0; e < emitters.size(); e++){

			if( emitters.get( e).getEmitterName() == _name){

				emitters.get( e).draw( _gr2);
			}
		}
	}

    public void changeEmitterAngle( String _name, double _angle){

        for( int e = 0; e < emitters.size(); e++){

            if( emitters.get( e).getEmitterName() == _name){

                emitters.get( e).setEmitter_angle(_angle);
            }
        }
    }

	//activates a fountain or burst of particles
	public void activateEmitterFountain( String _name){

		for( int e = 0; e < emitters.size(); e++){

			if( emitters.get( e).getEmitterName() == _name){

				emitters.get( e).runEmitter = true;
			}
		}
	}

	public boolean detectParticleCollision( String _name, Sprite _spr){

		for( int e = 0; e < emitters.size(); e++){

			if( emitters.get( e).checkParticleCollisions( _spr) && (emitters.get( e).getEmitterName() == _name)){

				return true;
			}
		}

		return false;
	}

  //an object that is essentialy an arraylist manager for an array of dynamically created sprites, if this sprite class was a fountain
	//then an Emitter will manage an array of sub sprites that create the water droblets ejecting from it
	//its X and Y position is that of the sprite that owns it, although it can be adjusted with setOffset()
	protected class Emitter{

		Random rand;

		//this array list will contain an array of sprites that Emitter will manage
		ArrayList<Sprite> particles = new ArrayList<Sprite>();

		//a timer class to manage particals being created at a set related
		Ticks ticks;

		String part_name;

		//partical code ; will append the number to the end of the sub sprites name on creation
		//explanation for following args above Emitter constructor, busrtNum is the particlal numebr the burst function is at
		//this is to keep track of how many particals its made so it can stop at burst value
		int particle_code, part_error, part_burstRate, particalvel, destruct_distance, offsetX, offsetY, part_burst, burstNum;
		double part_angle, last_emition;
		boolean grav_effected, runEmitter;

		//the image of the particals
		BufferedImage particle_image;

		//theres going to be a lot of arguments for many things, line number with explanation coresponds to arg position in constructor
		//most of which can be changed with Setters and Getters

		//  1, name of the emitter, particals created will have the same name plus the number which they are given
		//  2, path to the image in which to initialise the particals sprite data with
		//  3, range of randomness to the angle assigned to each partical
		//  4, number of particles to create for each time the 'fire()' function is called
		//  5, gap in time before a new partical can be created
		//  6, angle of direction for each partical when it is created
		//  7, velocity of each partical
		//  8, true if particle is effected by gravity, false if not
		protected Emitter( String _name, String _image, int _error, int _burst, int _burstRate, double _angle, int _particalvel, boolean _grav){

				//sets partical name for managment of particals and emiiters in the SPrite class
				part_name = _name;

				try{

					//initialise a particle image for creation of sprites
					particle_image = ImageIO.read( new File( _image));
				}catch( Exception e){

					System.out.println( "Error creating image for Emitter, check image exists in path: " + _image);
				}

				part_error = _error;
				part_burst = _burst;
				part_burstRate = _burstRate;

				//this rate can be changed with a setter
				ticks = new Ticks( _burstRate);

				part_angle = _angle;
				particalvel = _particalvel;
				grav_effected = _grav;

				//this is the destance in which to remove the partical from the array to avoid too much over head default is 1500 in either x or y direction
				//can be set to a different value
				destruct_distance = 700;

				//create the random number generator
				rand = new Random();

				//emitter starts in an off state
				runEmitter = false;

				//burstNUm is default 0;
				burstNum = 0;
		}

		//creates x amount of new particles
		public void createFountain(){

			if( runEmitter){
				if( burstNum != getEmitter_burst()){

					fire();
				}else{

					runEmitter = false;
					burstNum = 0;
				}
			}
		}

		//adds one particle to particle array
		public void fire(){

			//destroy particles after they have left the screen on creation of new ones to save a little overhead/java virtual memory
			garbage();

			//if enough time has passed to allow the creation of another particle
			if( ticks.getTicks()){

				try{

					//increases burstNum if fountain mode is activated
					burstNum++;

					//adds a slight error ot the angle when the particle is created
					double errored_angle = rand.nextInt( getEmitter_Error()) - getEmitter_Error();
								errored_angle = part_angle + errored_angle;

					//increases particle code number in order to have a unique name for it
					particle_code++;


					//initialises sprite array with a new sprite based on data passed in args
					particles.add( new Sprite( part_name, particle_image));

					//initialises that newly created sprite with all necissary properties to be a fully fledged working sprite
					particles.get( particles.size() -1).setXY( getPosX() + getOffsetX(), getPosY() + getOffsetY());
					particles.get( particles.size() -1).setAngle( errored_angle);
					particles.get( particles.size() -1).setVelocity( getEmitter_particalvel());
					particles.get( particles.size() -1).setmaxVelocity( getEmitter_particalvel());

					//if gravity is true for the sprite
					if( grav_effected){

						//initialise for gravity conditions
						particles.get( particles.size() -1).setGravityMode( grav_effected);
						particles.get( particles.size() -1).setGravityAngle( 90);
						particles.get( particles.size() -1).setGravity( 5);
						particles.get( particles.size() -1).setThrustAngle( errored_angle);
					}

				//all sprites must be created in try and catch blocks, particles are no Exception!
				}catch(Exception e){

					//outputs name of specific particle causing an error on construction
					System.out.println( "Error creating particle: " + part_name + particle_code + " :" + e.toString());
				}
			}
		}

		//draws and moves all particals, takes the graphics argument that will draw the images
		public void draw( Graphics gr2){

			//runs the emitter fountain if it has been activated, is in draw as draw will be called in the same way that emitter needs to be ran
			createFountain();

			for( int x = 0; x < particles.size() ; x++){

				particles.get( x).moveSprite();

                //System.out.println( "!" + getParticleName() + " " + particle_code);

                gr2.drawImage( particles.get( x).getFrame(0), particles.get( x).getPosX(), particles.get( x).getPosY(), particles.get( x).getWidth(), particles.get( x).getHeight(), null);
			}
		}

		//destroys a specific partical based on its name ( remember particle is name of Emitter plus number)
		public void destroyParticle( String _name){

			for( int x = 0; x < particles.size() ; x++){

				//fix later
				if( "getEmitterName() + toString(particle_code)" == _name){

					particles.remove( x);
				}
			}
		}

		//checks all particals for collisions against the sprite in args
		public boolean checkParticleCollisions( Sprite _spr){

			for( int x = 0; x < particles.size() ; x++){

					return particles.get( x).checkCollision( _spr);
			}

			return false;
		}

		//removes a particle when it is of a set distance from the emitter default is 1500 pixels
		public void garbage(){

			for( int x = 0; x < particles.size() ; x++){

				if ( Math.abs(particles.get( x).getPosX() - getPosX()) > get_destructDistance() || ( Math.abs(particles.get( x).getPosY() - getPosY()) > get_destructDistance())){

						particles.remove( x);
				}
			}
		}


		///////////////////////////////////////////////////////setters for each member variable\\\\\\\\\\\\\\\\\\\\\\\\\\\
		private void set_destructDistance(int _dd){

			destruct_distance = _dd;
		}

		public void setEmitter_Error( int _error){

			part_error = _error;
		}

		public void setEmitter_burst( int _burst){

			part_burst = _burst;
			ticks = new Ticks( _burst);
		}

		public void setEmitter_burstRate( int _burstRate){

			part_burstRate = _burstRate;
		}

		//setters and getters for each value
		public void setEmitter_angle( double _angle){

			part_angle = _angle;
		}

		//setters and getters for each value
		public void setEmitter_particalvel( int _particalvel){

			particalvel = _particalvel;
		}

		//sets the offset from the top left point of the sprite that owns emitter
		//to draw the sprite from
		public void setEmitter_particalvel( int _x, int _y){

			offsetX = _x;
			offsetY = _y;
		}

		/////////////////////////////////////////////////////////////////////getters for each member variable\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		public int getEmitter_Error(){

			return part_error;
		}

		public int getEmitter_burst(){

			return part_burst;
		}

		public int getEmitter_burstRate(){

			return part_burstRate;
		}

		//setters and getters for each value
		public double getEmitter_angle(){

			return part_angle;
		}

		//setters and getters for each value
		public int getEmitter_particalvel(){

			return particalvel;
		}

		private int get_destructDistance(){

			return destruct_distance;
		}

		public int getOffsetX(){

			return offsetX;
		}

		public int getOffsetY(){

			return offsetY;
		}

		public String getEmitterName(){

			return part_name;
		}

		public String getParticleName(){

			return getEmitterName() + particle_code;
		}

	}//end of Emitter class


	/**********************************************************************************************************************\\
						stateSata makes two sprite managment strategies possible, States and Conditions.

		CONDITIONS: if a sprite has reached a specific speed, angle or an increasing or decreasing velocity, the sprites
		animation states are made to change to ones that fit, this is done by adding a condition to the conditions array
		and having pollCOnditions in sprite check to see if the conditions of the sprite match any of the data, if they do the starting
		and ending frames in frame loop are changed, ie;

		function in sprite class is called on sprite object like this: my_sprite.addCondition( 0.0, 180.0, 10, 15),
		this function  adds a 'stateData' object to the 'conditions' array list, 'pollConditions()' will poll this array if it is not empty
		to check if condition of the sprite would return true, if true is returned from the object then the object also owns the
		correct frames to loop between

		STATES: If a state is added with add state, then when 'activateState()' is called, the name of the state is found and the
		conditions of the state are updated from the same object, ie;

		 'goLeft' state is added to the state array with 'addState()', its arguments in constructor were to animate between frames 10 and 15
		 ( a sequence of images creating a leftwards walking stickman) and the angle was 180 degrees with a speed of 2 a total speed of ten and
		a velocity of 2, when my_sprite.activate('goLEFT') is called 'moveSprite()' will move the sprite in units of vector passed in args
		with increasing velocity till max velocity is reached and 'nextFrame()' will return the next frame in a loop between 10 and 15

	\*************************************************************************************************************************/

	//this class manages the data for each state, objects of this class are to be saved in the stateArray
	protected class stateData{

		//variables to store state data
		String dName;
		int dStart, dEnd, dHeight, dWidth, speedStart, speedEnd;
		float dmaxVelocity, dvelocity, dacceleration;
		double dangle, angleStart, angleEnd, thrustAccelMin, thrustAngleMax, thrustAccelMax, thrustAngleMin;

		//initialise variables in constructor for automatic states, first arg is state name, second and third are animation frames to loop between, next is the height,
		//width, max velocity, velocity, acceleration speed and angle
		protected stateData(String _dname, int _dstart, int _dend, int _dheight, int _dwidth, float _dmvel, float _dvel, float _dacc, double _dangle){

			dName = _dname;
			dStart = _dstart;
			dEnd = _dend;
			dHeight = _dheight;
			dWidth = _dwidth;
			dmaxVelocity = _dmvel;
			dvelocity = _dvel;
			dacceleration = _dacc;
			dangle = _dangle;
		}

		//initialise variables in constructor for initialising an animation state based on the angle:
		//if the angle is between _angleStart and _angleEnd then loop the animation between _dstart and _dend
		protected stateData(double _aStart, double _aEnd, int _dstart, int _dend){



			//assign data from constructor
			dStart = _dstart;
			dEnd = _dend;
			angleStart = _aStart;
			angleEnd = _aEnd;
		}

		//initialise variables in constructor for initialising an animation state based on the speed:
		//if the speed is between _speedStart and _speedEnd then loop the animation between _dstart and _dend
		public stateData(int _speedStart, int _speedEnd, int _dstart, int _dend){


			//assign data from constructor
			dStart = _dstart;
			dEnd = _dend;
			speedStart = _speedStart;
			speedEnd = _speedEnd;
		}

		//initialise variables in constructor for initialising an animation state based on jumping logic
		//the activation of this state will apply the thrust and an angle between two points for jumping
		public stateData( double _thrustAccelMin, double _thrustAngleMin,  double _thrustAccelMax, double _thrustAngleMax, int _dstart, int _dend){

			//assign data from constructor
			dStart = _dstart;
			dEnd = _dend;
			thrustAccelMin = _thrustAccelMin;
			thrustAngleMin = _thrustAngleMin;
			thrustAccelMax = _thrustAccelMax;
			thrustAngleMax = _thrustAngleMax;
		}

		//initialise variables in constructor for initialising an animation state based on the velocity:
		//if first arg is true then the state will have changed if the sprites velocity is
		//greater than that in the second argument, if it is false the change will occue when it is less than
		//if more than one of these are set then there are conflicts, this can be resolved by calling pollState strategicaly
		public stateData(boolean _ifIncreasing, int _dvelocity, int _dstart, int _dend){

			//assign data from constructor
			dStart = _dstart;
			dEnd = _dend;
			dvelocity = _dvelocity;
		}


		//if called on object returns true if the conditions are met to return variables associated with the state
		//argument is for checking against saved conditions relating to which state condition is activated
		//takes a double as args to satisfy angle, speed and velocity can convert into integer
		public void checkCondition(String _name){

			switch( _name){

				case "ANGLE":

					double _condition = getAngle();

					if( (_condition > angleStart && _condition < angleEnd) ){

						//the best way to tell if the condition has changed to a new one is to check if the present frame
						//number is within the range of the old one
						if( !(dStart <= frameNum && frameNum <= dEnd)){

							frameNum = dStart;
							frameStart = dStart;
							frameEnd = dEnd;
						}
					}

					break;

				case "SPEED":

					 _condition = getVelocity();

					if( _condition > speedStart && _condition < speedEnd){

						if( !(dStart <= frameNum && frameNum <= dEnd)){

							frameNum = dStart;
							frameStart = dStart;
							frameEnd = dEnd;
						}
					}
						break;

				case "INCREASING":

					_condition = getAcceleration();

					if( _condition > dvelocity){

						if( !(dStart <= frameNum && frameNum <= dEnd)){

							frameNum = dStart;
							frameStart = dStart;
							frameEnd = dEnd;
						}
					}
						break;
				case "DECREASING":

					 _condition = getAcceleration();

					if( _condition < dvelocity){

						if( !(dStart <= frameNum && frameNum <= dEnd)){

							frameNum = dStart;
							frameStart = dStart;
							frameEnd = dEnd;
						}
					}
						break;

						case "JUMPING":

							  _condition = getThrustAcceleration();
							 double _condition2 = getThrustAngle();

							if( _condition < thrustAccelMax && _condition > thrustAccelMin && _condition2 < thrustAngleMax && _condition2 > thrustAngleMin){

								if( !(dStart <= frameNum && frameNum <= dEnd)){

									frameNum = dStart;
									frameStart = dStart;
									frameEnd = dEnd;
								}
							}
								break;

								//to compliment jumping
							/*	case "FALLING":

									 _condition = getThrustAcceleration();

									if( _condition < thrustAccelerationMax && _condition > thrustAccelerationMin && ){

										if( !(dStart <= frameNum && frameNum <= dEnd)){

											frameNum = dStart;
											frameStart = dStart;
											frameEnd = dEnd;
										}
									}
										break; */

				default:

					//no state conditions have been met
					break;
			}
		}

	}//end of stateData class

	//this class takes an integer value as arguments, this value represents milliseconds
	//the only function 'getTicks()' returns true if this amount of time has passed since it was
	//last called, else it returns false
	public class Ticks{

		private double gticks, startTime;

		public Ticks( double _timer){

			startTime = System.currentTimeMillis();
      gticks = _timer;
		}

		public boolean getTicks(){

			if( (System.currentTimeMillis() - startTime) > gticks){

				//System.out.println("yes" +  (System.currentTimeMillis() - startTime) + " ->" + gticks);
				startTime = System.currentTimeMillis();
				return true;
			}else{

					//System.out.println("no" +  (System.currentTimeMillis() - startTime) + " ->" + gticks);
				return false;
			}
		}
	}//end of ticks class
}//end of sprite class
