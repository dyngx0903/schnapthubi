package piece;

import java.io.IOException;

import javax.imageio.ImageIO;

import Main.GamePanel;

public class Rabbit extends Piece {

	public Rabbit(int player ,int col, int row) {
		super(player, col, row);
		this.name = "Rabbit";
		// TODO Auto-generated constructor stub
		image = getImage("/piece/rabbit");
	}
	
	public boolean canMove(int targetCol, int targetRow) {
		if(isWithinBoard(targetCol,targetRow) && isSamSquare(targetCol, targetRow)==false) {
			
			// move before the curtain
//			if (Math.abs(targetRow - preRow) + Math.abs(targetCol - preCol) == 2
//					&& Math.abs(targetRow - preRow) * Math.abs(targetCol - preCol) == 0 && 
//					wallCover(targetCol,targetRow)==true ) {
//				return true;
//			}
			
			// move after open the curtain
			if(Math.abs(targetRow - preRow) + Math.abs(targetCol - preCol) ==2 
					&& Math.abs(targetRow - preRow) * Math.abs(targetCol - preCol) == 0  ){

				if( PieceIsOnStraightLine(targetCol,targetRow,"Mouse_door")==false ) {
					return true;
				}
			}
		}
			return false;
	}

}
