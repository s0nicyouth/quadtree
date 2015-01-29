package com.syouth;

import com.sun.javafx.geom.Vec2d;
import com.sun.tools.javac.util.Pair;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Main {

    private static final double FIELD_X = 0.0;
    private static final double FIELD_Y = 0.0;
    private static final double FIELD_SIDE_X = 1920.0;
    private static final double FIELD_SIDE_Y = 1024.0;
    private static final double MAX_RECT_SIDE = 50.0;
    private static final double MIN_RECT_SIDE = 10.0;

    public static List<Shape> generateCircles(int num) {
        ArrayList<Shape> shapes = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            double x = Math.random() * FIELD_SIDE_X - FIELD_X;
            double y = Math.random() * FIELD_SIDE_Y - FIELD_Y;
            double w = Math.random() * (MAX_RECT_SIDE - MIN_RECT_SIDE) + MIN_RECT_SIDE;
            shapes.add(new java.awt.geom.Rectangle2D.Double(x, y, w, w));
        }

        return shapes;
    }

    public static HashSet<Pair<Shape, Shape>> getCollisions(List<Shape> list, Shape s) {
        HashSet<Pair<Shape, Shape>> pairs = new HashSet<>();
        for (Shape c : list) {
            if (s.equals(c)) {
                continue;
            }
            Vec2d v1 = new Vec2d(c.getBounds2D().getCenterX(), c.getBounds2D().getCenterY());
            Vec2d v2 = new Vec2d(s.getBounds2D().getCenterX(), s.getBounds2D().getCenterY());
            double distance = v1.distance(v2);
            if (distance <= c.getBounds2D().getHeight() / 2 + s.getBounds2D().getHeight() / 2) {
                pairs.add(new Pair<>(s, c));
            }
        }

        return pairs;
    }

    public static void main(String[] args) {
        Object syncObject = new Object();
  /*      for (int i = 1; i < 5000000; i += 100000) {
            double middle1 = 0;
            double middle2 = 0;
            for (int j = 0; j < 5; j++) {
                List<Shape> shapes = generateCircles(i);
                long start1 = System.nanoTime();
                HashSet<Pair<Shape, Shape>> collisions = getCollisions(shapes, shapes.get(0));
                long end1 = System.nanoTime();
                middle1 += (end1 - start1) / 1000000000.0;

                QuadTree.QuadTreeNode root = new QuadTree.QuadTreeNode(new Rectangle2D.Double(FIELD_X, FIELD_Y, FIELD_SIDE_X, FIELD_SIDE_Y));
                root.setMinimumSide(30);
                root.buildTree(shapes);

                long start2 = System.nanoTime();
                List<QuadTree.QuadTreeNode> quadTreeNodes = root.containsShape(shapes.get(0));
                ArrayList<Shape> shapesToCheck = new ArrayList<>();
                for (QuadTree.QuadTreeNode n : quadTreeNodes) {
                    shapesToCheck.addAll(n.getObjects());
                }
                HashSet<Pair<Shape, Shape>> collisions1 = getCollisions(shapesToCheck, shapes.get(0));
                long end2 = System.nanoTime();
                middle2 += (end2 - start2) / 1000000000.0;
                if (collisions.size() != collisions1.size()) {
                    System.out.print("DCHECK fail");
                    return;
                }
            }
            System.out.printf("%d %f %f\n", i, middle1 / 5, middle2 / 5);
        }*/

        List<Shape> shapes1;
        ArrayList<Shape> shapesToCheck;
        final QuadTree.QuadTreeNode root = new QuadTree.QuadTreeNode(new Rectangle2D.Double(FIELD_X, FIELD_Y, FIELD_SIDE_X, FIELD_SIDE_Y));
        do {
            shapes1 = generateCircles(10000);
            root.setmMaximumObjectsPerQuad(10);
            root.setMinimumSide(10);
            root.buildTree(shapes1);
            List<QuadTree.QuadTreeNode> quadTreeNodes = root.containsShape(shapes1.get(0));
            shapesToCheck = new ArrayList<>();
            for (QuadTree.QuadTreeNode n : quadTreeNodes) {
                shapesToCheck.addAll(n.getObjects());
            }
        } while (getCollisions(shapesToCheck, shapes1.get(0)).size() == 0 && true == false);

        List<Shape> shapes = shapes1;

        JFrame frame = new JFrame("Testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new JPanel() {

            private long lastFrameDrawn = System.nanoTime();

            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int)FIELD_SIDE_X, (int)FIELD_SIDE_Y);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                synchronized (syncObject) {
                    g.setColor(Color.RED);
                    ArrayList<Rectangle2D.Double> rects = new ArrayList<Rectangle2D.Double>();
                    root.getRects(rects);
                    for (Rectangle2D.Double r : rects) {
                        g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
                    }

                    g.setColor(Color.PINK);
                    List<QuadTree.QuadTreeNode> quadTreeNodes = root.containsShape(shapes.get(0));
                    ArrayList<Shape> shapesToCheck = new ArrayList<>();
                    for (QuadTree.QuadTreeNode n : quadTreeNodes) {
                        shapesToCheck.addAll(n.getObjects());
                    }
                    HashSet<Pair<Shape, Shape>> collisions1 = getCollisions(shapesToCheck, shapes.get(0));

                    Rectangle2D bounds2D = shapes.get(0).getBounds2D();
                    g.drawOval((int) bounds2D.getX(), (int) bounds2D.getY(), (int) bounds2D.getWidth(), (int) bounds2D.getHeight());
                    g.setColor(Color.CYAN);
                    for (Pair<Shape, Shape> s : collisions1) {
                        Rectangle2D bounds2D1 = s.snd.getBounds2D();
                        g.drawOval((int) bounds2D1.getX(), (int) bounds2D1.getY(), (int) bounds2D1.getWidth(), (int) bounds2D1.getHeight());
                    }

                    g.setColor(Color.BLACK);
                    for (Shape s : shapes) {
                        if (collisions1.contains(new Pair<>(shapes.get(0), s)) || s.equals(shapes.get(0))) {
                            continue;
                        }
                        Rectangle2D shpeBounds = s.getBounds2D();
                        g.drawOval((int) shpeBounds.getX(), (int) shpeBounds.getY(), (int) shpeBounds.getWidth(), (int) shpeBounds.getHeight());
                    }
                }
                long curDrawnTime = System.nanoTime();
                System.out.printf("%f\n", 1 / ((System.nanoTime() - lastFrameDrawn) / 1000000000.0));
                lastFrameDrawn = curDrawnTime;
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread() {
            private long lastTimeRun = System.nanoTime();
            @Override
            public void run() {
                super.run();
                while (true) {
                    long thisTimeRun = System.nanoTime();
                    double passed_x = Math.random() * 10 * (thisTimeRun - lastTimeRun) / 1000000000.0;
                    double passed_y = Math.random() * 10 * (thisTimeRun - lastTimeRun) / 1000000000.0;
                    synchronized (syncObject) {
                        for (Shape s : shapes) {
                            passed_x = -passed_x;
                            passed_y = -passed_y;
                            RectangularShape rectShape = (RectangularShape) s;
                            root.removeShape(rectShape);
                            rectShape.setFrame(passed_x + rectShape.getX(), passed_y + rectShape.getY(), rectShape.getWidth(), rectShape.getHeight());
                            root.addShape(rectShape);
                        }
                        lastTimeRun = thisTimeRun;
                    }
                    frame.repaint();
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
