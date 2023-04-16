package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;

import javax.swing.JFrame;
import javax.swing.JPanel;

 

public class TestFont extends JPanel{

    private Shape s;

    public TestFont() {
        Font f = getFont().deriveFont(Font.BOLD, 70);
        GlyphVector v = f.createGlyphVector( getFontMetrics(f).getFontRenderContext(), "Hello");
        s = v.getOutline();
        setPreferredSize(new Dimension(300,300));
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(100, 150);
        g2.rotate(0.4);
        g2.setPaint(Color.red);
        g2.fill(s);
        g2.setPaint(Color.black);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[]{1,0.4f,1.5f}, 0));
        g2.draw(s);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        Component c = new TestFont();
        f.getContentPane().add(c);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}