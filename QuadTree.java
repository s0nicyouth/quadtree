package com.syouth;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by syouth on 26/01/2015.
 */
public class QuadTree {

    public static class QuadTreeNode {
        private double mMinimumSide = 5;
        private int mMaximumObjectsPerQuad = 1000;
        private ArrayList<QuadTreeNode> mChildren = null;
        private ArrayList<Shape> mObjects = null;
        private Rectangle2D.Double mBoundingRect = new Rectangle2D.Double();
        private QuadTreeNode mParent = null;

        public QuadTreeNode(Rectangle2D.Double boundingRect) {
            mBoundingRect = boundingRect;
        }

        public double getMinimumSide() {
            return mMinimumSide;
        }

        public void setMinimumSide(double mMinimumSide) {
            this.mMinimumSide = mMinimumSide;
        }

        public int getMaximumObjectsPerQuad() {
            return mMaximumObjectsPerQuad;
        }

        public void setmMaximumObjectsPerQuad(int mMaximumObjectsPerQuad) {
            this.mMaximumObjectsPerQuad = mMaximumObjectsPerQuad;
        }

        public void addShape(Shape shape) {
            insertShape(shape, this);
        }

        public void buildTree(List<Shape> shapesToAdd) {
            mChildren = null;
            mObjects = null;
            for (Shape s : shapesToAdd) {
                insertShape(s, this);
            }
        }

        public List<QuadTreeNode> containsShape(Shape s) {
            ArrayList<QuadTreeNode> nodes = new ArrayList<>();
            QuadTree.containsShape(s, this, nodes);
            return nodes;
        }

        public ArrayList<Shape> getObjects() {
            return mObjects;
        }

        public void removeShape(Shape s) {
            QuadTree.removeShape(s, this);
        }

        public void getRects(List<Rectangle2D.Double> rects) {
            rects.add(mBoundingRect);
            if (mChildren != null) {
                for (QuadTreeNode n : mChildren) {
                    n.getRects(rects);
                }
            }
        }
    }

    private static void getUniqueShapesInQuad(QuadTreeNode root, HashSet<Shape> set) {
        assert root != null;

        if (root.getObjects() != null) {
            set.addAll(root.getObjects());
            return;
        }

        if (root.mChildren != null) {
            for (QuadTreeNode n : root.mChildren) {
                getUniqueShapesInQuad(n, set);
            }
        }
    }

    private static void undivideRectangleIfNeeded(QuadTreeNode parent) {
        assert parent != null;

        HashSet<Shape> shapes = new HashSet<>();
        getUniqueShapesInQuad(parent, shapes);

        if (shapes.size() <= parent.getMaximumObjectsPerQuad()) {
            assert parent.mObjects == null;
            parent.mObjects = new ArrayList<>();
            parent.getObjects().addAll(shapes);
            for (QuadTreeNode n : parent.mChildren) {
                n.mParent = null;
            }
            parent.mChildren = null;

            if (parent.mParent != null) {
                undivideRectangleIfNeeded(parent.mParent);
            }
        }
    }

    private static void removeShape(Shape s, QuadTreeNode root) {
        ArrayList<QuadTreeNode> nodesContainingShape = new ArrayList<>();
        containsShape(s, root, nodesContainingShape);

        HashMap<QuadTreeNode, QuadTreeNode> parentChildMap = new HashMap<>();
        for (QuadTreeNode n : nodesContainingShape) {
            n.getObjects().remove(s);
            parentChildMap.put(n.mParent, n);
        }

        for (HashMap.Entry<QuadTreeNode, QuadTreeNode> e : parentChildMap.entrySet()) {
            if (e.getValue().mParent != null) {
                undivideRectangleIfNeeded(e.getKey());
            }
        }
    }

    private static void divideRectangle(QuadTreeNode node) {
        node.mChildren = new ArrayList<>();
        Rectangle2D.Double parentRect = node.mBoundingRect;
        double newWidth = parentRect.getWidth() / 2;
        double newHeight = parentRect.getHeight() / 2;
        // Right bottom
        Rectangle2D.Double rect1 = new Rectangle2D.Double(parentRect.getX() + newWidth,
                parentRect.getY(),
                newWidth,
                newHeight);
        QuadTreeNode child1 = new QuadTreeNode(rect1);
        child1.setMinimumSide(node.getMinimumSide());
        child1.setmMaximumObjectsPerQuad(node.getMaximumObjectsPerQuad());
        child1.mParent = node;
        node.mChildren.add(child1);
        // Right top
        Rectangle2D.Double rect2 = new Rectangle2D.Double(parentRect.getX() + newWidth,
                parentRect.getY() + newHeight,
                newWidth,
                newHeight);
        QuadTreeNode child2 = new QuadTreeNode(rect2);
        child2.setMinimumSide(node.getMinimumSide());
        child2.setmMaximumObjectsPerQuad(node.getMaximumObjectsPerQuad());
        child2.mParent = node;
        node.mChildren.add(child2);
        // Left top
        Rectangle2D.Double rect3 = new Rectangle2D.Double(parentRect.getX(),
                parentRect.getY() + newHeight,
                newWidth,
                newHeight);
        QuadTreeNode child3 = new QuadTreeNode(rect3);
        child3.setMinimumSide(node.getMinimumSide());
        child3.setmMaximumObjectsPerQuad(node.getMaximumObjectsPerQuad());
        child3.mParent = node;
        node.mChildren.add(child3);
        // Left bottom
        Rectangle2D.Double rect4 = new Rectangle2D.Double(parentRect.getX(),
                parentRect.getY(),
                newWidth,
                newHeight);
        QuadTreeNode child4 = new QuadTreeNode(rect4);
        child4.setMinimumSide(node.getMinimumSide());
        child4.setmMaximumObjectsPerQuad(node.getMaximumObjectsPerQuad());
        child4.mParent = node;
        node.mChildren.add(child4);
    }

    private static void containsShape(Shape s, QuadTreeNode root, List<QuadTreeNode> quads) {
        if (!root.mBoundingRect.contains(s.getBounds2D()) && !s.intersects(root.mBoundingRect)) {
            // Cant add because shape lies completly outside of the rectangle
            return;
        }

        if (root.mObjects != null && root.mObjects.contains(s)) {
            quads.add(root);
            return;
        }

        assert root.mChildren != null;
        for (QuadTreeNode n : root.mChildren) {
            containsShape(s, n, quads);
        }
    }

    private static void insertShape(Shape shape, QuadTreeNode root) {
        if (!root.mBoundingRect.contains(shape.getBounds2D()) && !shape.intersects(root.mBoundingRect)) {
            // Cant add because shape lies completly outside of the rectangle
            return;
        }

        if (root.mChildren == null) {
            if (root.mObjects == null || root.mObjects.size() < root.getMaximumObjectsPerQuad() ||
                    root.mBoundingRect.getWidth() / 2 <= root.mMinimumSide ||
                    root.mBoundingRect.getHeight() / 2 <= root.mMinimumSide) {
                if (root.mObjects == null) {
                    root.mObjects = new ArrayList<>();
                }
                root.mObjects.add(shape);
                return;
            } else {
                divideRectangle(root);
            }
        }

        for (QuadTreeNode child : root.mChildren) {
            if (root.mObjects != null) {
                for (Shape s : root.mObjects) {
                    insertShape(s, child);
                }
            }
            insertShape(shape, child);
        }

        root.mObjects = null;
    }
}
