package Main;

import java.util.List;

import java.util.ArrayList;

import java.awt.AlphaComposite; 

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Collections;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;
import javazoom.jl.player.Player;



import Main.Mouse;
import piece.Carrot;
import piece.Chesse;
import piece.Piece;
import piece.Rabbit;
import piece.hubi;
import piece.magic_door;
import piece.mouse_door;
import piece.normal_door;
import piece.nothubi;
import piece.rabbit_door;
import piece.wall;
import piece.wallcover;
import piece.mouse;
import Main.Board;





public class GamePanel extends JPanel implements Runnable {
	// set up screen size
	public static final int WIDTH = 700;
	public static final int HEIGHT = 800;
	
	final int FPS = 60;
	Thread gameThread; // Thread : keep game run in loop -> implement Runnable

	Board board = new Board();
	Mouse mouse = new Mouse();

	
	public static final int R = 0;  // rabbit
	public static final int M = 1;  // mouse
	public static final int NP = -1; // not a player
	int currentPlayer = R; // first play is rabbit
	
	public boolean magicKeyCondition = true;
	public boolean checkWin = false;
	public boolean found = false;
	Piece standwithHubi;
	
	
	Piece activeP;
	//TIME
	double playTime = 0; 
    DecimalFormat dFormat = new DecimalFormat("#0.00");
	//boolean 
		boolean canMove;
		boolean validSquare;
		
		
	//game state
	    public int gameState;
	    public final int titleState = 0;
	    public final int playState = 1;
	    public final int pauseState = 2;
	    public final int endState = 3;
	    public final int helpState = 4;
	    
	 // SCREEN SETTINGS
	    final int originalTileSize = 16; // 16x16 tile
	    final int scale = 3;

	    public final int tileSize = originalTileSize * scale; // 48x48 tile
	    public final int maxScreenCol = 16;
	    public final int maxScreenRow = 12;
	    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels
	    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels

	    // WORLD SETTINGS
	    public final int maxWorldCol = 50;
	    public final int maxWorldRow = 50;

	    public final int worldWidth = tileSize * maxWorldCol;
	    public final int worldHeight = tileSize * maxWorldRow;
	    
	    
	    // system
	    public UI ui = new UI(this);
	    public KeyHandler keyH = new KeyHandler(this);
		
	    
	    // screen image 
	    BufferedImage image = getImage("/piece/game");
	    
	    //sound
	    Sound sound = new Sound();
	
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		this.setDoubleBuffered(true);
	    this.addKeyListener(keyH);
	    this.setFocusable(true);
	    
	    
		addMouseMotionListener(mouse);
		addMouseListener(mouse);

		setPieces();
		copyPieces(pieces,simPieces);
		playMusic(playState);
	}

	public void setupGame() {

        gameState = titleState;
       // playMusic(gameState);
    }
	
	private void update() {
		// Mouse button pressed
				if(mouse.pressed) {
					if(activeP == null) {
					// If the activeP is null , check if you can pick up a piece
					for(Piece piece : simPieces) {
						// If a mouse is on an ally piece, pick it up as acriveP
						if(     piece.player == currentPlayer &&
								piece.col == mouse.x/Board.SQUARE_SIZE &&
								piece.row == mouse.y/Board.SQUARE_SIZE &&
								(piece.name == "Rabbit" || piece.name == "Mouse"))
							activeP = piece;
					}
					}
					else {
						// if the player is holding piece, simulate the move
						simulate();
					}
				}
				if (mouse.pressed ==false) {
					if(activeP != null) {
						if(validSquare) {
							// move confirmed
							
							//update the pieces list in case a piece has been captured
							// and removed during the simulation
							copyPieces(simPieces, pieces);
							activeP.updatePosition();
							changePlayer();
						}
						else {
							// the move is not valid so reset everything
							copyPieces(pieces,simPieces);
							activeP.resetPosition();
							activeP = null;
						}
						
					}
				}
	}
	public void simulate() {
		canMove = false;
		validSquare = false;
		
		// reset the piece list in every loop
		// this is basically for restoring the removed pieced during the simulation
		copyPieces(pieces,simPieces);
		
		// if the piece is being held, update its position
		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);
		
		if(activeP.canMove(activeP.col, activeP.row)) {
			canMove = true;
			
			// if hitting a wall, remove it from the list
			if(activeP.wallCover(activeP.col,activeP.row)== true) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			
			// remove magic door
			for (Piece piece : pieces) {
				if(piece.name == "Magic_door" && magicKeyCheck(piece.col,piece.row)==false)
				{
					simPieces.remove(piece.getIndex());
					//pieces.remove(piece.getIndex());
					magicKeyCondition = false;
				}
			}
			
				// delete token
				if(magicKeyCondition==false) {
					if(activeP.isToken(activeP.col,activeP.row) ==true) {
						simPieces.remove(activeP.hittingP.getIndex());
					}
					if(activeP.isToken(activeP.col, activeP.row)==false && isWin(activeP.col,activeP.row)==true) {
						checkWin = true;
					}
					if(hubiFound(activeP.col,activeP.row)==true) {
						found = true;
					}
					 
				}
				
				validSquare=true;
		}
	}
	
	// magic key condition
	public boolean magicKeyCheck(int targetCol, int targetRow) {

		for (Piece piece1 : simPieces) {
			if(piece1.isWall(targetCol, targetRow)==false) {
			for(Piece piece2 : simPieces) {
			if( (piece1.name=="Rabbit" && piece1.col==(targetCol-1) && piece1.row==targetRow) && (piece2.name== "Mouse" && piece2.col==(targetCol+1) && piece2.row==targetRow ) ){
				return false;
			}
			else if((piece1.name=="Mouse" && piece1.col==(targetCol-1) && piece1.row==targetRow) && (piece2.name== "Rabbit" && piece2.col==(targetCol+1) && piece2.row==targetRow) ) {
				return false;
			}
			else if((piece1.name=="Mouse" && piece1.row==(targetRow-1) && piece1.col==targetCol) && (piece2.name== "Rabbit" && piece2.row==(targetRow+1) && piece2.col==targetCol ) ) {
				return false;
			}
			else if((piece1.name=="Rabbit" && piece1.row==(targetRow-1) && piece1.col==targetCol) && (piece2.name== "Mouse" && piece2.row==(targetRow+1) && piece2.col==targetCol) ) {
				return false;
			}
			}
		}
		}
		return true;
	}
	
	// check win condition
	public boolean isWin(int targetCol, int targetRow) {
			for(Piece piece1 : simPieces) {
				for(Piece piece2 : simPieces) {
					if((piece1.name == "Rabbit" && piece2.name=="Mouse"  )&& 
							(piece1.row ==targetRow && piece2.row == targetRow )&&
							(piece2.col == targetCol && piece1.col == targetCol )) {
					for(Piece piece3 : simPieces) {
					if(piece3.name == "hubi" && piece3.row == piece1.row && piece3.col == piece1.col) {
						return true;
					}
					}
				}}
			}
			return false;
		}
	
	// check find hubi
	public boolean hubiFound(int targetCol, int targetRow) {
		for(Piece piece1 : simPieces) {
			for (Piece piece2 : simPieces) {
				if( ((piece1.name == "Rabbit" || piece1.name=="Mouse") && piece2.name=="hubi") &&
						(piece1.row ==targetRow && piece2.row == targetRow )&&
						(piece2.col == targetCol && piece1.col == targetCol ) ){
					 return true;
				}
			}
		}
		return false;
	}



	
	// paintComponent is a method in Jcomponent that JPanel inherits and is
		// used to draw objects on the panel
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// convert Graphics to Graphics2D
			Graphics2D g2 = (Graphics2D)g;
			
	        // TITLE SCREEN
	        if (gameState == titleState) {
	            ui.draw(g2);
	        }
	        else if(gameState == endState) {
	        	double finalPlayTime = playTime;
		    	ui.drawEndScreen(g2,finalPlayTime);
		    }
	        	else {
	        		//draw board
	        		g2.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
	        		//board.draw(g2);
	        		
	        		// draw piece
	        		// Create a temporary list for safe iteration
	                List<Piece> tempSimPieces = new ArrayList<>(simPieces);
	                for (Piece p : tempSimPieces) {
	                    p.draw(g2);
	                }
	    			
	    			if (activeP !=null) {
	    				if(canMove)
	    				{
	    				g2.setColor(Color.green);
	    				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.7f));
	    				g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE
	    						, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
	    				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f));
	    				}
	    				activeP.draw(g2);
	    			}
	    	        
	    	        
	    			// status message
	    			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    			g2.setFont(new Font("Book Antiqua",Font.PLAIN,35));
	    			g2.setColor(Color.white);
	    			
	    			if(currentPlayer == R) {
	    				g2.drawString("Rabbit turn", 0, 100);
	    			}
	    			else {
	    				g2.drawString("Mouse turn", 500,100);
	    			}
	    		
	    		    if (magicKeyCondition == false) {
	    		    	g2.drawString("Magic door is opened, find hubi ", 100,650);
	    		    }

	    		    if(found==true) {
	    		    	g2.drawString("Hubi is found", 350, 750);
	    		    }
	    		    if(checkWin == true) {

	    		    	gameState = endState;
	    		        repaint(); 
	    		       
	    		    }
	    		    //TIME
	    		    if(gameState == playState) {
	    		    	g2.setFont(new Font("Poppins", Font.BOLD, 40)); 
		    		    g2.setColor(Color.BLACK); 
		    		    g2.drawString("Time: " + dFormat.format(playTime), 250, 70);
	    		    }
	        	}
	        g2.dispose();		
}	
		
		public void LaunchGame() {
			gameThread = new Thread(this);
			gameThread.start();
			
		}
		
		public static ArrayList<Piece> pieces = new ArrayList<>();
		public static ArrayList<Piece> simPieces = new ArrayList<>();
		
		public void setPieces() {
			Random rand = new Random();
			int random = rand.nextInt(4);
			
			if(random==1) {
				//add normal door
				pieces.add(new normal_door(NP,1,2));
				pieces.add(new normal_door(NP,1,4));
				pieces.add(new normal_door(NP,2,1));
				pieces.add(new normal_door(NP,2,5));
				pieces.add(new normal_door(NP,4,3));
				pieces.add(new normal_door(NP,5,2));
				pieces.add(new normal_door(NP,5,4));
				
				// add mouse door
				pieces.add(new mouse_door(NP,2,3));
				pieces.add(new mouse_door(NP,4,5));
				
				
				// add rabbit door
				pieces.add(new rabbit_door(NP,3,2));
				pieces.add(new rabbit_door(NP,4,1));

				// add Magic 
				pieces.add(new magic_door(NP,3,4));
			}
			
			if(random==0) {
				//add normal door
				pieces.add(new normal_door(NP,2,1));
				pieces.add(new normal_door(NP,4,1));
				pieces.add(new normal_door(NP,1,2));
				pieces.add(new normal_door(NP,5,2));
				pieces.add(new normal_door(NP,3,4));
				pieces.add(new normal_door(NP,2,5));
				pieces.add(new normal_door(NP,4,5));
				
				// add mouse door
				pieces.add(new mouse_door(NP,3,2));
				pieces.add(new mouse_door(NP,5,4));
				
				
				// add rabbit door
				pieces.add(new rabbit_door(NP,2,3));
				pieces.add(new rabbit_door(NP,1,4));

				// add Magic 
				pieces.add(new magic_door(NP,4,3));
			}
			
			if(random==2) {
				//add normal door
				pieces.add(new normal_door(NP,6-1,6-2));
				pieces.add(new normal_door(NP,6-1,6-4));
				pieces.add(new normal_door(NP,6-2,6-1));
				pieces.add(new normal_door(NP,6-2,6-5));
				pieces.add(new normal_door(NP,6-4,6-3));
				pieces.add(new normal_door(NP,6-5,6-2));
				pieces.add(new normal_door(NP,6-5,6-4));
				
				// add mouse door
				pieces.add(new mouse_door(NP,6-2,6-3));
				pieces.add(new mouse_door(NP,6-4,6-5));
				
				
				// add rabbit door
				pieces.add(new rabbit_door(NP,6-3,6-2));
				pieces.add(new rabbit_door(NP,6-4,6-1));

				// add Magic 
				pieces.add(new magic_door(NP,6-3,6-4));
			}
			
			if(random==3) {
			//add normal door
			pieces.add(new normal_door(NP,6-2,6-1));
			pieces.add(new normal_door(NP,6-4,6-1));
			pieces.add(new normal_door(NP,6-1,6-2));
			pieces.add(new normal_door(NP,6-5,6-2));
			pieces.add(new normal_door(NP,6-3,6-4));
			pieces.add(new normal_door(NP,6-2,6-5));
			pieces.add(new normal_door(NP,6-4,6-5));
			
			// add mouse door
			pieces.add(new mouse_door(NP,6-3,6-2));
			pieces.add(new mouse_door(NP,6-5,6-4));
			
			
			// add rabbit door
			pieces.add(new rabbit_door(NP,6-2,6-3));
			pieces.add(new rabbit_door(NP,6-1,6-4));

			// add Magic 
			pieces.add(new magic_door(NP,6-4,6-3));
			}

			
			// add wall cover
			for(int i=1;i<=5;i++) {
				if(i%2!=0) {
					for(int j=2;j<=5;j+=2) {
						pieces.add(new wallcover(NP,i,j));
					}
				}
				else {
					for(int j=1;j<=5;j+=2) {
						pieces.add(new wallcover(NP,i,j));
					}
				}
			}
			pieces.add(new wall(NP,2,2));
			pieces.add(new wall(NP,2,4));
			pieces.add(new wall(NP,4,4));
			pieces.add(new wall(NP,4,2));
			
			//add hubi
			int rand2 = rand.nextInt(3) * 2 +1; // random 1 3 5
			int rand3 = rand.nextInt(3) * 2 +1;
			pieces.add(new hubi(NP,rand2,rand3));
			
				
			//add token
			int count1=0;
			int count2=0;
			for(int i=1 ; i<=5 ;i+=2) {
				for(int j=1 ;j<=5 ;j+=2){
					if(random<=4 ) {
						if(count1<5) {
							pieces.add(new Carrot(NP,i,j));
							count1++;
						}
						else {
							pieces.add(new Chesse(NP,i,j));
						}
						
					}
					else{
						if(count2 <5) {
							pieces.add(new Chesse(NP,i,j));
							count2++;
						}
						else {
							pieces.add(new Carrot(NP,i,j));
						}
						
					}
				}
			}
			
			int rand4 = (rand.nextInt(2) * 4) +1;
			int rand5 = (rand.nextInt(2) * 4) +1;
			//add rabbit
			pieces.add(new Rabbit(R,rand4,rand5));
			//add mouse
			rand4 = (rand.nextInt(2) * 4) +1;
			rand5 = (rand.nextInt(2) * 4) +1;
			pieces.add(new mouse(M,rand5,rand4));

			
		}
		
		private void copyPieces(ArrayList<Piece> source, ArrayList<Piece>target) {
			target.clear();
			for(int i=0;i<source.size();i++) {
				target.add(source.get(i));
			}
		}
		
		// change turn
		private void changePlayer() {
			if(currentPlayer==R ) {
				currentPlayer = M;
			}
			else if (currentPlayer==M ) {
				currentPlayer = R;
			}

			activeP = null;
		}
		
		// image 
		public BufferedImage getImage(String imagePath) {
			BufferedImage image = null;
			try {
				image = ImageIO.read(getClass().getResourceAsStream(imagePath+".png"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
			return image;
		}
		


		
		
		// create a game loop that keeps calling these 2 methods at a certain interval
		public void run() {

			double drawInterval = 1000000000 / FPS;
			double delta = 0;
			long lastTime = System.nanoTime();
			long currentTime;

			
			while (gameThread != null) {

				currentTime = System.nanoTime();
				delta += (currentTime - lastTime) / drawInterval;
				lastTime = currentTime;
				
				
				while (delta >1) {
					update();
					delta--;
					if(gameState == playState) {
						playTime += 1.0 / FPS;
					}
					}
					repaint();
					if(checkWin == true) {
						sound.stop();
						break;
				}
			}
		}
		
		public void playMusic(int i) {
			sound.setFile(i);
			sound.play();
			sound.loop();

		}
		public void stopMusic() {
			sound.stop();
		}

}