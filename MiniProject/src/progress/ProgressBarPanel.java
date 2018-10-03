/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progress;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author S401-28
 */
public class ProgressBarPanel extends JPanel {    
    private int max;
    private int progress;
    private static final int GAP = 1;

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        
        if(max != 0) {
        	
            int width = (getWidth() - GAP * 2) * progress / max;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.decode("#33cc33"));
            g2d.fillRect(GAP, GAP, width, getHeight() - GAP*2);
        }
    }
    
    public void notifyProgressChanged() {
        this.repaint();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
    
}
