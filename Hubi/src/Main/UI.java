package Main;

import java.text.DecimalFormat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.FlowLayout;
public class UI {

    GamePanel gp;
    Graphics2D g2;

    Font maruMonica, purisaB;
//    BufferedImage keyImage;
    public boolean messageOn = false;
    public String message = "";
    int messageCounter = 0;
    public boolean gameFinished = false;
    public String currentDialogue = "";
    public int commandNum = 0;
    public int titleScreenState = 0; // 0: the first screen, 1: the second screen
    double playTime = 0.0;
    DecimalFormat dFormat = new DecimalFormat("#0.00");
	BufferedImage image = getImage("/piece/b");
	
	BufferedImage end_image = getImage("/piece/end_game");

	

    public UI(GamePanel gp) {
        this.gp = gp;

        try {
            InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
            maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
            is = getClass().getResourceAsStream("/font/Purisa Bold.ttf");
            purisaB = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   

    public void showMessage(String text) {

        message = text;
        messageOn = true;
    }
    public void draw(Graphics2D g2) {
    	

        this.g2 = g2;
        
        g2.drawImage(image, 0, 0 ,GamePanel.WIDTH, GamePanel.HEIGHT,null);


//        g2.setFont(arial_40);
        g2.setFont(maruMonica);
//        g2.setFont(purisaB);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.white);
        // TITLE STATE
        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }

        // PLAY STATE
        if (gp.gameState == gp.playState) {
            // Do playState stuff later

        }
        // PAUSE STATE
        if (gp.gameState == gp.pauseState) {
            drawPauseScreen();
        }
        
        //END GAME STATE
        if (gp.gameState == gp.endState) {
            drawEndScreen(g2,playTime);
        }
    }
    
    public void drawEndScreen(Graphics2D g2, double playTime) {
    	g2.drawImage(end_image, 0, 0 ,GamePanel.WIDTH, GamePanel.HEIGHT,null);
        
        g2.setColor(Color.white);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        //draw text at center
        String message = "Congratulations!";
        int messageWidth = g2.getFontMetrics().stringWidth(message);
        int xMessage = (GamePanel.WIDTH - messageWidth) / 2;
        
        g2.drawString(message, xMessage, 200);
        //TIME PLAYED
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.setColor(Color.white);
        String timePlayedStr = "Time Played: " + dFormat.format(playTime) + " seconds";
        int timeWidth = g2.getFontMetrics().stringWidth(timePlayedStr);
        int xTimePlayed = (GamePanel.WIDTH - timeWidth) / 2;
        
        g2.drawString(timePlayedStr, xTimePlayed, 250);


    }
    
    
    public void drawPauseScreen() {

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN,80F));
        String text = "PAUSED";
        int x = getXforCenteredText(text);

        int y = gp.HEIGHT/2;

        g2.drawString(text, x, y);
    }

    public void drawTitleScreen() {


        if (titleScreenState == 0) {


            // TITLE NAME
            g2.setFont(g2.getFont().deriveFont(Font.BOLD,96F));
            String text = "";
            int x = getXforCenteredText(text);
            int y = gp.tileSize*9;

            // SHADOW
            g2.setColor(Color.gray);
            g2.drawString(text, x+5, y+5);
            // MAIN COLOR
            g2.setColor(Color.black);
            g2.drawString(text, x, y);



            // MENU
            g2.setFont(g2.getFont().deriveFont(Font.BOLD,48F));
            g2.setColor(Color.BLACK);
            text = "START GAME";
            x = getXforCenteredText(text);
            y += gp.tileSize*3.5;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString(">", x-gp.tileSize, y);
            }

            text = "QUIT";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString(">", x-gp.tileSize, y);
            }
            
        }

    }
    

    public void drawSubWindow(int x, int y, int width, int height) {

        Color c = new Color(0,0,0,210);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255,255,255);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x+5, y+5, width-10, height-10, 25, 25);
    }
    public int getXforCenteredText(String text) {

        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gp.WIDTH/2 - length/2;
        return  x;
    }
    
    
    // buffer image
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
    
}