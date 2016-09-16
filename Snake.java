
import java.applet.Applet;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;
import java.util.Iterator;

public class Snake implements Runnable, ActionListener
{
	
	Random generator = new Random();
	int width;
	int height; 
	int dots;
	int radie;

	long timeDiff;
	
	TextField textrad = new TextField("4");
	TextField textdots = new TextField("1000");
	TextField textheight = new TextField("500");
	TextField textwidth = new TextField("800");

	
	Checkbox väggBox;
    Checkbox varannBox; 
    Checkbox mittBox;
    JPanel boxPanel = new JPanel();
    boolean vägg, varann, mitt;
    
    JButton startButton = new JButton("startButton!");
	Thread thread;
	
	JFrame counterFrame = new JFrame();
	JLabel counterLabel = new JLabel();
	
	JFrame optionFrame = new JFrame("Choose settings!");
	
	punkt mittP;
	
	ArrayList<punkt> notMoving = new ArrayList<punkt>();
	ArrayList<punkt> moving = new ArrayList<punkt>();

	JPanel myJPanel;

	ExecutorService executorService = Executors.newCachedThreadPool();
	CountDownLatch latch;

	public class ListSegmentRunnable implements Runnable {

		int start, end;

		public ListSegmentRunnable(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			processDots(start, end);
			latch.countDown();
		}
	}

    public Snake() {
        init();
    }

	  /**
     * Initialize GUI and components (including ActionListeners etc)
     */
    private void showSnakeJFrame() {
        JFrame jFrame = new JFrame();

		myJPanel = new MyPanel();
        jFrame.add(myJPanel);

        //pack frame (size JFrame to match preferred sizes of added components and set visible
        jFrame.pack();
        jFrame.setVisible(true);

        
    }

    class MyPanel extends JPanel {

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
			for(int i = 0; i<notMoving.size(); i++)
			{
				g.setColor(notMoving.get(i).färg);
				g.fillOval(notMoving.get(i).x, notMoving.get(i).y,radie,radie);
			}
			for(int i = 0; i<moving.size(); i++)
			{
				g.setColor(moving.get(i).färg);
				g.fillOval(moving.get(i).x,moving.get(i).y,radie,radie);
			}

	    }

	    //so our panel is the corerct size when pack() is called on Jframe
	    @Override
	    public Dimension getPreferredSize() {
	        return new Dimension(width, height);
	    }
	}
	
	public class punkt
	{
		int x, y, xold, yold;
		//Boolean moving;
		Color färg; 

		boolean frozen;
		
		public punkt()
		{
			x = generator.nextInt(width);
			y = generator.nextInt(height);
			
			färg = new Color((int) (Math.random()*255) ,(int) (Math.random()*255), (int) (Math.random()*255));
			
		//	färg = new Color((int) ((x+y)*(255.0/(2*width))), (int) ((int) (x)*(255.0/(height+width))) ,(int) (y*x)*(255/(height*width)));
		}
		
		public punkt(int x, int y)
		{
			this.x = x; 
			this.y = y;
			färg = new Color(x*(255/width),x*(255/width) , y*(255/height));
		}
		
		public void move(int randx, int randy)
		{	
				if(x > 0 && y > 0 && x < width && y < height)
					x += randx; y += randy; //x = x + förflyttning
		}

		public void setFrozen(boolean frozen) {
			this.frozen = frozen;
		}

		public boolean isFrozen() {
			return frozen;
		}
	}
	

	public void init()
	{

        väggBox = new Checkbox("Väggar, och andra stillaståend",true);
        // same but selected 
        varannBox = new Checkbox("Varann", false); 
		mittBox = new Checkbox("Mittpunkten", false);
        boxPanel.setLayout(new GridLayout(3,1));
        boxPanel.add(väggBox);
        boxPanel.add(varannBox);
        boxPanel.add(mittBox);
		
		optionFrame.setLayout(new GridLayout(6,2));
		optionFrame.add(new JLabel("Antal prickar:"));
		optionFrame.add(textdots);
		
		optionFrame.add(new JLabel("Radie"));
		optionFrame.add(textrad);
	
		optionFrame.add(new JLabel("Fönsterhöjd"));
		optionFrame.add(textheight);
		
		optionFrame.add(new JLabel("FönsterBredd"));
		optionFrame.add(textwidth);
		
		optionFrame.add(new JLabel("Prickarna ska fastna på:"));
		optionFrame.add(boxPanel);
		
		startButton.addActionListener( this );
		optionFrame.add(startButton);
	
		optionFrame.pack();
		optionFrame.setVisible(true);

	}
	
	public double dist(punkt p1, punkt p2)
	{
		double tal1 = Math.pow((p1.x - p2.x),2);
		double tal2 = Math.pow((p1.y - p2.y),2);
		return Math.sqrt(tal1 + tal2);
	}
	
	@Override
	public void run()
	{
		while(moving.size()>0)
		{
			long t1 = System.currentTimeMillis();

			final int segmentSize = 500;
			int numberOfSegments = (int) Math.ceil(moving.size() / (float) segmentSize);
			latch = new CountDownLatch(numberOfSegments);

			List<Runnable> runnables = new ArrayList<Runnable>();

			for (int i = 0; i < moving.size(); i += segmentSize) {

				int start = i;
				int end = Math.min(moving.size(), i + segmentSize);

				runnables.add(new ListSegmentRunnable(start, end));
				System.err.println("Adding Runnable from: " + start + " to " + end);
			}

			for (Runnable runnable : runnables) {
				executorService.execute(runnable);
			}
			
			try {
			  latch.await();
			} catch (InterruptedException E) {
			   // handle
			}

			Iterator<punkt> iter = moving.iterator();
			while(iter.hasNext()) {
				punkt p  = iter.next();
				if (p.isFrozen()) {
					iter.remove();
					notMoving.add(p);
				}
			}

			long t2 = System.currentTimeMillis();
			timeDiff = t2-t1;
			myJPanel.repaint();
		}
		
	}

	public void processDots(int start, int end) {

		for(int i = start; i<end; i++)
		{
			moving.get(i).move(generator.nextInt(11)-5, generator.nextInt(11)-5);
		}

		for(int i = start; i<end; i++)
		{
			//Kolla om krock med vägg
			if((moving.get(i).x < radie || moving.get(i).y < radie
					||moving.get(i).x > width-radie || moving.get(i).y > height-radie) && vägg)
			{
				moving.get(i).setFrozen(true);
			}
			
			//Kolla efter krock med stillstående
			for(int j = 0; j<notMoving.size() && i < moving.size(); j++)
			{
				if((dist(moving.get(i),notMoving.get(j)) < radie) && i != j)
				{
					moving.get(i).setFrozen(true);
					break;
				}
			}

			//Om krock mellan 2 stycken rörande
			if(varann && moving.size()>0) 
			{
				for(int j = 0; j<moving.size() && i <moving.size(); j++)
				{
					if((dist(moving.get(i),moving.get(j)) < radie*1.2) && i != j)
					{
						moving.get(i).setFrozen(true);
						moving.get(j).setFrozen(true);
					}
				}

			}
			counterLabel.setText("" + moving.size() + "\ndrawTime: " + timeDiff);	
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		thread = new Thread( this );
		if(notMoving.size() > 1)
			notMoving.clear();
		moving.clear();
		
		radie = Integer.parseInt(textrad.getText());
		dots = Integer.parseInt(textdots.getText());
		height = Integer.parseInt(textheight.getText());
		width = Integer.parseInt(textwidth.getText());
		vägg = väggBox.getState();
		varann = varannBox.getState();
		mitt = mittBox.getState();
		
		if(mitt)
		{
			mittP = new punkt(width/2, height/2);
			notMoving.add(mittP);
		}
		
		//Skapar dots antal punkter och lägger dem i en array
		for(int i = 0; i<dots; i++)
			moving.add(new punkt());
		
		counterLabel.setText("" + moving.size() + "\ndrawTime: " + timeDiff);	
		counterFrame.add(counterLabel);
		counterFrame.pack();
		counterFrame.setVisible(true);
		
		thread.start();

		showSnakeJFrame();
	}

	public static void main(String[] args) {

        /**
         * Create GUI and components on Event-Dispatch-Thread
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Snake snaker = new Snake();
            }
        });
    }
		
}


