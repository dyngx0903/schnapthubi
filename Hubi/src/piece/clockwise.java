package piece;

import java.awt.geom.AffineTransform;

public class clockwise extends Piece {

	public clockwise(int player, int col, int row) {
		super(player, col, row);
		// TODO Auto-generated constructor stub
		this.name = "clockwise";
		image = getImage("/piece/clockwise");
	}

}
