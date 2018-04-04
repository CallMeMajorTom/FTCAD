/**
 *
 * @author brom
 */

package client;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JFrame;

import both.GObject;
import both.Message;
import both.Shape;

public class GUI extends JFrame implements WindowListener, ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	private JButton mOvalButton = new JButton("Oval");
	private JButton mRectangleButton = new JButton("Rect");
	private JButton mLineButton = new JButton("Line");
	private JButton mFilledOvalButton = new JButton("Filled oval");
	private JButton mFilledRectangleButton = new JButton("Filled Rect");
	private JButton mRedButton = new JButton("Red");
	private JButton mBlueButton = new JButton("Blue");
	private JButton mGreenButton = new JButton("Green");
	private JButton mWhiteButton = new JButton("White");
	private JButton mPinkButton = new JButton("Pink");

	private GObject mTemplate = new GObject(Shape.OVAL, Color.RED, 363, 65, 25, 25);
	private GObject mCurrent = null;

	private LinkedList<GObject> mObjectList = new LinkedList<GObject>();
	private CadClient mClient = null;

	public GUI(int xpos, int ypos, CadClient client) {
		setSize(xpos, ypos);
		setTitle("FTCAD");
		mClient = client;

		Container pane = getContentPane();
		pane.setBackground(Color.BLACK);

		pane.add(mOvalButton);
		pane.add(mRectangleButton);
		pane.add(mLineButton);
		pane.add(mFilledOvalButton);
		pane.add(mFilledRectangleButton);
		pane.add(mRedButton);
		pane.add(mBlueButton);
		pane.add(mGreenButton);
		pane.add(mWhiteButton);
		pane.add(mPinkButton);

		pane.setLayout(new FlowLayout());
		setVisible(true);
	}
	
	public void setObjectList(LinkedList<GObject> objectList) {
		this.mObjectList = objectList;
		repaint();
	}

	public void addToListener() {
		addWindowListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		mOvalButton.addActionListener(this);
		mRectangleButton.addActionListener(this);
		mLineButton.addActionListener(this);
		mFilledOvalButton.addActionListener(this);
		mFilledRectangleButton.addActionListener(this);
		mRedButton.addActionListener(this);
		mBlueButton.addActionListener(this);
		mGreenButton.addActionListener(this);
		mWhiteButton.addActionListener(this);
		mPinkButton.addActionListener(this);

	}

	// WindowListener methods
	public void windowActivated(WindowEvent e) {
		repaint();
	}

	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
		repaint();
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
		repaint();
	}

	// MouseListener methods
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			if (e.getX() > 0 && e.getY() > 91) {
				mCurrent = new GObject(mTemplate.getShape(), mTemplate.getColor(), e.getX(), e.getY(), 0, 0);
			} else mCurrent = null;
			break;
		case MouseEvent.BUTTON3:
			//TODO
			break;
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			if (mCurrent != null) {
				int id = mClient.increaseID();
				mClient.getM_FEConnection().sendChatMessage(id, "/draw",mCurrent);
				mCurrent = null;
			}
			break;
		case MouseEvent.BUTTON3://Rightclick undoes an operation by removing the most recently added object.
			if (mObjectList.size() > 0) {
				int id = mClient.increaseID();
				mClient.getM_FEConnection().sendChatMessage(id, "/remove", null);
			}
			break;
		}
		repaint();
	}

	// MouseMotionListener methods
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (mCurrent != null && e.getX() > 0 && e.getY() > 91) {
			mCurrent.setDimensions(e.getX() - mCurrent.getX(), e.getY() - mCurrent.getY());
		}
		repaint();
	}

	// ActionListener methods
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mOvalButton) {
			mTemplate.setShape(Shape.OVAL);
		} else if (e.getSource() == mRectangleButton) {
			mTemplate.setShape(Shape.RECTANGLE);
		} else if (e.getSource() == mLineButton) {
			mTemplate.setShape(Shape.LINE);
		} else if (e.getSource() == mFilledOvalButton) {
			mTemplate.setShape(Shape.FILLED_OVAL);
		} else if (e.getSource() == mFilledRectangleButton) {
			mTemplate.setShape(Shape.FILLED_RECTANGLE);
		} else if (e.getSource() == mRedButton) {
			mTemplate.setColor(Color.RED);
		} else if (e.getSource() == mBlueButton) {
			mTemplate.setColor(Color.BLUE);
		} else if (e.getSource() == mGreenButton) {
			mTemplate.setColor(Color.GREEN);
		} else if (e.getSource() == mWhiteButton) {
			mTemplate.setColor(Color.WHITE);
		} else if (e.getSource() == mPinkButton) {
			mTemplate.setColor(Color.PINK);
		}
		repaint();
	}

	public void update(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 60, getSize().width, getSize().height - 60);
		mTemplate.draw(g);

		for (ListIterator<GObject> itr = mObjectList.listIterator(); itr.hasNext();) {
			itr.next().draw(g);
		}

		if (mCurrent != null) {
			mCurrent.draw(g);
		}
	}

	public void paint(Graphics g) {
		super.paint(g); // The superclass (JFrame) paint function draws the GUI components.
		update(g);
	}
}
