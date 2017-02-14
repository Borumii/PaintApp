//This is a plain text version of the project.

package lessons;

import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



@SuppressWarnings("serial")
public class PaintApp extends JFrame{

	//creating buttons for the program, and boxes to organize layers
	Box brush, line, rect, ellipse, stroke, fill, eraser, currentColors, 
	currentStrokeColor, currentFillColor, layerTray, layerButtons, layerMaster;
	
	//panel containing buttons
	JPanel buttonPanel;
	
	//variables to regulate actions and layers, and create proper spacing between layer interfaces
	int actionNum, strokeWidth, currentLayer, lastLayer, spacing, buffer;
	
	//initialize default colors
	Color strokeColor=Color.BLACK, fillColor=Color.BLACK, strokeColor2=Color.BLACK, fillColor2=Color.BLACK;
	
	//listener for keyboard input
	lforkeypress hotkeyListener = new lforkeypress();
	Graphics2D graphicsSettings;
	
	//ArrayLists to store shape and color data for each layer, and ArrayList2 to enable undo function
	@SuppressWarnings("rawtypes")
	ArrayList[] shapeLayer, sfillLayer, strokeColorLayer, fillLayer, transparencyLayer, strokeWidthLayer,
	shapeLayer2, sfillLayer2, strokeColorLayer2, fillLayer2, transparencyLayer2, strokeWidthLayer2;
	
	//Buttons for specific functions
	JButton fillToggle, layerWipe, clearScreen, undo, saveImage;
	
	//Regulates transparency value
	Float transparency;
	
	//Initial top level ArrayLists to store data as it gets created
	ArrayList<Shape> shapes = new ArrayList<Shape>();
	ArrayList<Shape> sfill = new ArrayList<Shape>();
	ArrayList<GradientPaint> strokeColors = new ArrayList<GradientPaint>();
	ArrayList<GradientPaint> fills = new ArrayList<GradientPaint>();
	ArrayList<Float> transparencies = new ArrayList<Float>();
	ArrayList<Integer> strokeWidths = new ArrayList<Integer>();
	
	//Sliders to manipulate transparency and stroke width
	JSlider transparencySlider, strokeSlider;
	
	JLabel strokeLabel, transparencyLabel, colorLabelStroke, colorLabelStroke2, 
	colorLabelFill, colorLabelFill2, keyboardStroke, keyboardTrans, dummyLabel;
	
	//Formats decimals used in sliders
	DecimalFormat dformat = new DecimalFormat("#.##");
	
	//Textfields to indicate current chosen color, and activeLayer displays the current layer number
	JTextField strokeColorBox, fillColorBox, activeLayer, strokeColorBox2, fillColorBox2;
	
	//Booleans for disabling fill and to help regulate layer functions
	boolean fillActive, lastReleased;
	
	//Array for hiding specific layers
	boolean[] layerShown;
	
	DrawingBoard board = new DrawingBoard();
	
	
	public static void main(String[] args) {
		
		new PaintApp();
	}

	public PaintApp(){
		
		//setting defaults
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Paint");
		this.setLocationRelativeTo(null);
		
		
		buttonPanel = new JPanel();
		actionNum=1;
		strokeWidth=2;
		transparency=1.0f;
		fillActive=true;
		lastReleased=false;
		lastLayer=0;
		
		//initialize ArrayLists
		shapeLayer = new ArrayList[10];
		sfillLayer = new ArrayList[10];
		strokeColorLayer = new ArrayList[10];
		fillLayer = new ArrayList[10];
		transparencyLayer = new ArrayList[10];
		strokeWidthLayer = new ArrayList[10];
		
		shapeLayer2 = new ArrayList[10];
		sfillLayer2 = new ArrayList[10];
		strokeColorLayer2 = new ArrayList[10];
		fillLayer2 = new ArrayList[10];
		transparencyLayer2 = new ArrayList[10];
		strokeWidthLayer2 = new ArrayList[10];
		
		for(int i=0; i<10; i++)
		{
			shapeLayer2[i] = new ArrayList<Shape>();
			sfillLayer2[i] = new ArrayList<Shape>();
			strokeColorLayer2[i] = new ArrayList<Color>();
			fillLayer2[i] = new ArrayList<Color>();
			transparencyLayer2[i] = new ArrayList<Float>();
			strokeWidthLayer2[i] = new ArrayList<Integer>();
		}
		
		
		
		//Create transparency and stroke sliders
		transparencySlider = new JSlider(0, 100, 100);
		strokeSlider = new JSlider (1, 12, 3);
		sliderListener sListener = new sliderListener();
		transparencySlider.addChangeListener(sListener);
		strokeSlider.addChangeListener(sListener);
		strokeLabel = new JLabel("Stroke thickness: 3");
		transparencyLabel = new JLabel("Transparency: 1");
		keyboardStroke = new JLabel("<- o      p ->");
		keyboardTrans = new JLabel("<- k      l ->");
		
		//Toggle fills on or off
		fillToggle = new JButton("Fill: On");
		fillToggle.addActionListener(new ActionListener(){

			
			public void actionPerformed(ActionEvent e) {
				if(fillToggle.getText() == "Fill: On")
				{
					fillToggle.setText("Fill: Off");
					fillActive=false;
				}
				else
				{
					fillToggle.setText("Fill: On");
					fillActive=true;
				}
				
			}
			
			
		});
		
		
		
		//Boxes for organization
		Box transparencyBox = Box.createVerticalBox();
		Box strokeBox = Box.createVerticalBox();
		Box currentColors = Box.createVerticalBox();
		Box currentStroke = Box.createHorizontalBox();
		Box currentFill = Box.createHorizontalBox();
		
		//Add components to boxes
		transparencyBox.add(transparencySlider);
		transparencyBox.add(transparencyLabel);
		transparencyBox.add(keyboardTrans);
		transparencySlider.setAlignmentX(CENTER_ALIGNMENT);
		transparencyLabel.setAlignmentX(CENTER_ALIGNMENT);
		keyboardTrans.setAlignmentX(CENTER_ALIGNMENT);
		
		
		strokeBox.add(strokeSlider);
		strokeBox.add(strokeLabel);
		strokeBox.add(keyboardStroke);
		strokeSlider.setAlignmentX(CENTER_ALIGNMENT);
		strokeLabel.setAlignmentX(CENTER_ALIGNMENT);
		keyboardStroke.setAlignmentX(CENTER_ALIGNMENT);
		
		//Creating buttons to select actions. Icon images are located in the directory specified below,
		//under the given file names.
		brush = createButton("./src/PaintIcons/brush.png", 1);
		line = createButton("./src/PaintIcons/Line.png", 2);
		rect = createButton("./src/PaintIcons/Rectangle.png", 3);
		ellipse = createButton("./src/PaintIcons/Ellipse.png", 4);
		eraser = createButton("./src/PaintIcons/Eraser.png", 5);
		stroke = createColorButton("./src/PaintIcons/Stroke.png", 6);
		fill = createColorButton("./src/PaintIcons/Fill.png", 8);
		
		
		//labels and color indicators for strokes and fills
		colorLabelFill = new JLabel("Primary fill:         ");
		colorLabelStroke = new JLabel("Primary stroke: ");
		strokeColorBox = new JTextField("", 2);
		fillColorBox = new JTextField("", 2);
		strokeColorBox.setBackground(Color.BLACK);
		fillColorBox.setBackground(Color.BLACK);		
		
		colorLabelFill2 = new JLabel("Secondary fill:         ");
		colorLabelStroke2 = new JLabel("Secondary stroke: ");
		strokeColorBox2 = new JTextField("", 2);
		fillColorBox2 = new JTextField("", 2);
		strokeColorBox2.setBackground(Color.BLACK);
		fillColorBox2.setBackground(Color.BLACK);
		
		currentStroke.add(colorLabelStroke);
		currentStroke.add(strokeColorBox);
		currentStroke.add(colorLabelStroke2);
		currentStroke.add(strokeColorBox2);
		
		currentFill.add(colorLabelFill);
		currentFill.add(fillColorBox);
		currentFill.add(colorLabelFill2);
		currentFill.add(fillColorBox2);
		
		currentColors.add(currentStroke);
		currentColors.add(currentFill);
		
		//clear the drawingboard
		clearScreen = new JButton("Clear All");
		clearScreen.addActionListener(new ActionListener(){
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void actionPerformed(ActionEvent e) {
				
				
				for(int i=0; i<10; i++)
				{
					shapeLayer2[i] = new ArrayList<Shape>(shapeLayer[i]);
					sfillLayer2[i] = new ArrayList<Shape>(sfillLayer[i]);
					strokeColorLayer2[i] = new ArrayList<Color>(strokeColorLayer[i]);
					fillLayer2[i] = new ArrayList<Color>(fillLayer[i]);
					transparencyLayer2[i] = new ArrayList<Float>(transparencyLayer[i]);
					strokeWidthLayer2[i] = new ArrayList<Integer>(strokeWidthLayer[i]);
				}
				
				
				for(int i=0; i<10; i++)
				{
					shapeLayer[i] = new ArrayList<Shape>();
					sfillLayer[i] = new ArrayList<Shape>();
					strokeColorLayer[i] = new ArrayList<GradientPaint>();
					fillLayer[i] = new ArrayList<GradientPaint>();
					transparencyLayer[i] = new ArrayList<Float>();
					strokeWidthLayer[i] = new ArrayList<Integer>();
					
				}
				
				shapes.clear();
				sfill.clear();
				strokeColors.clear();
				fills.clear();
				transparencies.clear();
				strokeWidths.clear();
				
				board.repaint();
				buttonPanel.requestFocus();
				
			}
		});
		
		//undo last action. Does not work more than once due to how the brush and eraser are implemented
		undo = new JButton("Undo");
		undo.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<10; i++)
				{
					shapeLayer[i] = new ArrayList<Shape>(shapeLayer2[i]);
					sfillLayer[i] = new ArrayList<Shape>(sfillLayer2[i]);
					strokeColorLayer[i] = new ArrayList<GradientPaint>(strokeColorLayer2[i]);
					fillLayer[i] = new ArrayList<GradientPaint>(fillLayer2[i]);
					transparencyLayer[i] = new ArrayList<Float>(transparencyLayer2[i]);
					strokeWidthLayer[i] = new ArrayList<Integer>(strokeWidthLayer2[i]);
				}
				
				
				if(lastLayer == currentLayer)
				{
					shapes = new ArrayList<Shape>(shapeLayer[currentLayer]);
					sfill = new ArrayList<Shape>(sfillLayer[currentLayer]);
					strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[currentLayer]);
					fills = new ArrayList<GradientPaint>(fillLayer[currentLayer]);
					transparencies = new ArrayList<Float>(transparencyLayer[currentLayer]);
					strokeWidths = new ArrayList<Integer>(strokeWidthLayer[currentLayer]);
				}
				
				
				
				
				board.repaint();
				buttonPanel.requestFocus();
			}
		
		});
		
		//add components
		buttonPanel.add(brush);
		buttonPanel.add(line);
		buttonPanel.add(rect);
		buttonPanel.add(ellipse);
		buttonPanel.add(eraser);
		buttonPanel.add(stroke);
		buttonPanel.add(fill);
		
		buttonPanel.add(transparencyBox);
		buttonPanel.add(strokeBox);
		buttonPanel.add(currentColors);
		
		//formatting buttons
		Dimension buttonSize = fillToggle.getMaximumSize();
		Dimension buttonSize2 = new Dimension(buttonSize.width+1, buttonSize.height);
		undo.setPreferredSize(buttonSize2);
		fillToggle.setPreferredSize(buttonSize2);
		undo.setMaximumSize(buttonSize2);
		fillToggle.setMaximumSize(buttonSize2);
		
		
		
		Box endButtons = Box.createVerticalBox();
		endButtons.add(fillToggle);
		endButtons.add(undo);
		
		buttonPanel.add(endButtons);
		buttonPanel.setFocusable(true);
		buttonPanel.addKeyListener(hotkeyListener);
		this.add(board, BorderLayout.CENTER);
		
		currentLayer=0;
		
		
		//Regulates the hiding of individual layers
		layerShown = new boolean[10];
		
		
		//Seperate button to clear the currently selected layer
		layerWipe = new JButton("Clear Current Layer");
		layerWipe.addActionListener(new ActionListener(){

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<10; i++)
				{
					shapeLayer2[i] = new ArrayList<Shape>(shapeLayer[i]);
					sfillLayer2[i] = new ArrayList<Shape>(sfillLayer[i]);
					strokeColorLayer2[i] = new ArrayList<Color>(strokeColorLayer[i]);
					fillLayer2[i] = new ArrayList<Color>(fillLayer[i]);
					transparencyLayer2[i] = new ArrayList<Float>(transparencyLayer[i]);
					strokeWidthLayer2[i] = new ArrayList<Integer>(strokeWidthLayer[i]);
				}
				
				shapeLayer[currentLayer] = new ArrayList<Shape>();
				sfillLayer[currentLayer] = new ArrayList<Shape>();
				strokeColorLayer[currentLayer] = new ArrayList<GradientPaint>();
				fillLayer[currentLayer] = new ArrayList<GradientPaint>();
				transparencyLayer[currentLayer] = new ArrayList<Float>();
				strokeWidthLayer[currentLayer] = new ArrayList<Integer>();
				shapes = new ArrayList<Shape>();
				sfill = new ArrayList<Shape>();
				strokeColors = new ArrayList<GradientPaint>();
				fills = new ArrayList<GradientPaint>();
				transparencies = new ArrayList<Float>();
				strokeWidths = new ArrayList<Integer>();
				
				board.repaint();
				buttonPanel.requestFocus();
				
			}
		});
		
		
		//creating and formatting the indicator for the current layer
		activeLayer = new JTextField("Current Layer: 1");
		activeLayer.setHorizontalAlignment(JTextField.CENTER);
		activeLayer.setEditable(false);
		activeLayer.setBackground(Color.WHITE);
		
		
		
		
		layerButtons = Box.createVerticalBox();
		layerTray = Box.createHorizontalBox();
		layerMaster = Box.createHorizontalBox();
		
		//label to be the title of the layer control panel on the left of the screen
		dummyLabel = new JLabel("Hold Shift to Switch Layers!");
		dummyLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		//formatting
		Dimension format = new Dimension(layerWipe.getMaximumSize().width + 20, layerWipe.getMaximumSize().height);
		
		layerWipe.setMaximumSize(format);
		clearScreen.setMaximumSize(format);
		activeLayer.setMaximumSize(format);
		
		layerWipe.setAlignmentX(CENTER_ALIGNMENT);
		clearScreen.setAlignmentX(CENTER_ALIGNMENT);
		activeLayer.setAlignmentX(CENTER_ALIGNMENT);
		
		layerButtons.add(dummyLabel);
		layerButtons.add(layerWipe);
		layerButtons.add(clearScreen);
		layerButtons.add(activeLayer);
		
		
		spacing = 10;
		buffer = 40;
		
		for(int i=0; i<9; i++)
		{
			layerTray.add(createLayer(i));
			layerTray.add(Box.createRigidArea(new Dimension(spacing,0)));
		}
		
		layerTray.add(createLayer(9));
		

		
		
		
		layerMaster.add(layerButtons);
		layerMaster.add(Box.createRigidArea(new Dimension(buffer,0)));
		layerMaster.add(layerTray);
		
		
		
		
		//more formatting
		Box organizer = Box.createVerticalBox();
		organizer.add(layerMaster);
		organizer.add(Box.createRigidArea(new Dimension(0,8)));
		organizer.add(buttonPanel);
		
		//save the current drawing as a png file
		saveImage = new JButton("Save Image");
		saveImage.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				BufferedImage image = null;
				JRootPane rootPane = board.getRootPane();
				Dimension f = organizer.getSize();
				Dimension d = new Dimension(rootPane.getSize().width, rootPane.getSize().height - f.height);
				
				Rectangle bounds = new Rectangle(d);
				bounds.setLocation(rootPane.getLocationOnScreen());
				try {
					image = new Robot().createScreenCapture(bounds);
				} catch (AWTException e2) {
					
					e2.printStackTrace();
				}
				
				
				
				try {
					//saves current image to the src directory, where this java file should be.
					//modify the below code to save to a different location.
					ImageIO.write(image, "png", new File("./src/pictureeee.png"));
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
			}
			
		});
		
		buttonPanel.add(saveImage);
		
		layerMaster.setAlignmentX(CENTER_ALIGNMENT);
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		this.add(organizer, BorderLayout.SOUTH);
		
		this.setVisible(true);
		buttonPanel.requestFocus();
		
	}
	
	//function to create buttons for the tools with labels, listeners and such 
	public Box createButton(String filePath, int actionNumber){
		JButton button = new JButton();
		Icon icon = new ImageIcon(filePath);
		button.setIcon(icon);
		
		button.addActionListener(new ActionListener()
				{

					public void actionPerformed(ActionEvent arg0) {
						
						actionNum=actionNumber;
						buttonPanel.requestFocus();
						
					}

				}
			);
		
		JLabel hotkey = new JLabel(Integer.toString(actionNumber));
		Box box = Box.createVerticalBox();
		box.add(button);
		box.add(hotkey);
		button.setAlignmentX(CENTER_ALIGNMENT);
		hotkey.setAlignmentX(CENTER_ALIGNMENT);
		return box;

	}
	
	//Separate function to create the buttons that control the colors because they work slightly differently
	public Box createColorButton(String filePath, int actionNumber){
		JButton button = new JButton();
		Icon icon = new ImageIcon(filePath);
		button.setIcon(icon);
		
		button.addActionListener(new ActionListener()
				{

					public void actionPerformed(ActionEvent arg0) {
						
						if(actionNumber == 6){
							Color tempColor = JColorChooser.showDialog(null,  "Pick a stroke", strokeColor);
							
							if(tempColor != null)
							{
								strokeColor = tempColor;
								strokeColorBox.setBackground(strokeColor);
							}
							
						}
						else
						{
							Color tempColor = JColorChooser.showDialog(null,  "Pick a fill", fillColor);
							
							if(tempColor != null)
							{
								fillColor = tempColor;
								fillColorBox.setBackground(fillColor);
							}
							
						}
						buttonPanel.requestFocus();
					}

				}
			);
		
		JLabel hotkey = new JLabel(Integer.toString(actionNumber) + "/" + Integer.toString(actionNumber+1));
		Box box = Box.createVerticalBox();
		box.add(button);
		box.add(hotkey);
		button.setAlignmentX(CENTER_ALIGNMENT);
		hotkey.setAlignmentX(CENTER_ALIGNMENT);
		return box;

	}
	
	//where all the drawing takes place
	public class DrawingBoard extends JComponent{
		
		Point drawStart, drawEnd;
		Shape drawShape;
		
		
		public DrawingBoard(){
			
			this.addMouseListener(new MouseAdapter(){
				
				public void mousePressed(MouseEvent e)
				{
					//Reset ArrayList2 every time mouse is pressed to keep up with undoing
					for(int i=0; i<10; i++)
					{
						shapeLayer2[i] = new ArrayList<Shape>(shapeLayer[i]);
						sfillLayer2[i] = new ArrayList<Shape>(sfillLayer[i]);
						strokeColorLayer2[i] = new ArrayList<GradientPaint>(strokeColorLayer[i]);
						fillLayer2[i] = new ArrayList<GradientPaint>(fillLayer[i]);
						transparencyLayer2[i] = new ArrayList<Float>(transparencyLayer[i]);
						strokeWidthLayer2[i] = new ArrayList<Integer>(strokeWidthLayer[i]);
					}
					
					
				
					//If current tool is either brush or eraser we must implement the shape differently
					//Add required data to top level ArrayLists
					if(actionNum == 1)
					{
						drawShape = drawBrush(e.getX(), e.getY(), strokeWidth);
						shapes.add(drawShape);
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						strokeColors.add(new GradientPaint(0,0, strokeColor, 0,0, strokeColor));
						fills.add(new GradientPaint(0,0, strokeColor, 0,0, strokeColor));
						transparencies.add(transparency);
						strokeWidths.add(strokeWidth);
						
					}
					else if(actionNum == 5)
					{
						drawShape = drawBrush(e.getX(), e.getY(), strokeWidth);
						shapes.add(drawShape);
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						strokeColors.add
						(new GradientPaint(0,0, new Color(238,238,238), 0,0, new Color(238,238,238)));
						fills.add(new GradientPaint(0,0, new Color(238,238,238), 0,0, new Color(238,238,238)));
						transparencies.add(1.0f);
						strokeWidths.add(strokeWidth);
						
					}
					//All actions but brush or eraser
					else
					{
						
						drawStart = new Point(e.getX(), e.getY());
						drawEnd=drawStart;
					
					}
					
					repaint();
				}
				
								
				
				public void mouseReleased(MouseEvent e)
				{
					//For each action, when mouse is released we draw the shape onto the board
					//Add required data to top level ArrayLists
					if(actionNum == 1)
					{
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						transparencies.add(transparency);
						strokeColors.add(new GradientPaint(0,0, strokeColor, 0,0, strokeColor));
					}
					
					else if(actionNum == 2)
					{
						drawShape = drawLine(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
						sfill.add(drawLine(drawStart.x + (strokeWidth/2), drawStart.y + (strokeWidth/2),
								drawEnd.x - (strokeWidth/2)+1,drawEnd.y - (strokeWidth/2)+1));
						
						transparencies.add(transparency);
						strokeColors.add
						(new GradientPaint(drawStart.x,drawStart.y, strokeColor, drawEnd.x,drawEnd.y, strokeColor2));
					}
					else if(actionNum == 3)
					{
						drawShape = drawRect(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
						sfill.add(drawRectfill(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y));
						
						transparencies.add(transparency);
						strokeColors.add
						(new GradientPaint(drawStart.x,drawStart.y, strokeColor, drawEnd.x,drawEnd.y, strokeColor2));
					}
					else if(actionNum == 4)
					{
						drawShape = drawEllipse(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
						sfill.add(drawEllipsefill(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y));
						
						transparencies.add(transparency);
						strokeColors.add
						(new GradientPaint(drawStart.x,drawStart.y, strokeColor, drawEnd.x,drawEnd.y, strokeColor2));
					}
					else
					{
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						
						transparencies.add(1.0f);
						strokeColors.add
						(new GradientPaint(0,0, new Color(238,238,238), 0, 0, new Color(238,238,238)));
					}
					
					//drawShape and strokeWidth can be placed outside the conditional statements
					shapes.add(drawShape);
					strokeWidths.add(strokeWidth);
					
					//If fills are enabled, put the appropriate fill, otherwise leave it empty
					if(fillActive)
					{
						if(actionNum == 1)
							fills.add(new GradientPaint
									(0,0, strokeColor, 0,0, strokeColor));
						else if(actionNum == 5)
							fills.add(new GradientPaint
									(0,0, new Color(238,238,238), 0,0, new Color(238,238,238)));
						else
							fills.add(new GradientPaint
									(drawStart.x,drawStart.y, fillColor, drawEnd.x,drawEnd.y, fillColor2));
					}
					else
						fills.add(null);
					
					
					
					//reset points
					drawStart=null;
					drawEnd=null;
					
					
					//update lastLayer
					lastLayer=currentLayer;
					repaint();
					buttonPanel.requestFocus();
					
				}

				
			});
			
			this.addMouseMotionListener(new MouseMotionAdapter(){
				
				//Draw while dragging mouse if current tool is brush or eraser, otherwise 
				//update drawEnd for drawing a temporary outline
				public void mouseDragged(MouseEvent e)
				{
					if(actionNum == 1)
					{
						drawShape = drawBrush(e.getX(), e.getY(), strokeWidth);
						shapes.add(drawShape);
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						strokeColors.add(new GradientPaint(0,0, strokeColor, 0,0, strokeColor2));
						fills.add(new GradientPaint(0,0, fillColor, 0,0, fillColor2));
						transparencies.add(transparency);
						strokeWidths.add(strokeWidth);
						
						
					}
					else if(actionNum == 5)
					{
						drawShape = drawBrush(e.getX(), e.getY(), strokeWidth);
						shapes.add(drawShape);
						sfill.add(drawBrush(e.getX() + (strokeWidth / 2), e.getY() + (strokeWidth / 2), 0));
						strokeColors.add
						(new GradientPaint(0,0, new Color(238,238,238), 0,0, new Color(238,238,238)));
						fills.add(new GradientPaint(0,0, new Color(238,238,238), 0,0, new Color(238,238,238)));
						transparencies.add(1.0f);
						strokeWidths.add(strokeWidth);
						
						
					}
					else
					{
						drawEnd = new Point(e.getX(), e.getY());
					}
					
					repaint();
				}	
			});
			
		}
		
		
		@SuppressWarnings("unchecked")
		public void paint (Graphics g){
			
			graphicsSettings = (Graphics2D)g;
			
			graphicsSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			
		
				setLayers();
			
			//for each layer paint all objects in the ArrayLists
			for(int i=0; i<10; i++)
			{
				
				if(layerShown[i] && shapeLayer[i] != null)
				{
					//iterators to draw shapes
					ArrayList<Shape> shapeList = shapeLayer[i];
					Iterator<GradientPaint> strokeColorCounter = strokeColorLayer[i].iterator();
					Iterator<GradientPaint> fillCounter = fillLayer[i].iterator();
					Iterator<Float> transparencyCounter = transparencyLayer[i].iterator();
					Iterator<Integer> strokeWidthCounter = strokeWidthLayer[i].iterator();
					Iterator<Shape> sfillCounter = sfillLayer[i].iterator();
					
						
					for(Shape s : shapeList)
					{
						
						graphicsSettings.setStroke(new BasicStroke((2 * strokeWidthCounter.next()) - 1));
						graphicsSettings.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, transparencyCounter.next()));
						
						graphicsSettings.setPaint(strokeColorCounter.next());
						graphicsSettings.draw(s);
						
						GradientPaint tempFill = fillCounter.next();
						if(tempFill != null)
							graphicsSettings.setPaint(tempFill);
						else
							graphicsSettings.setComposite(AlphaComposite.getInstance(
	                                AlphaComposite.SRC_OVER, 0f));
						
						graphicsSettings.fill(sfillCounter.next());
						
						
					}
					
				}
				
			}
			
			
			//while dragging mouse, create a temporary outline of the desired shape
			if(drawStart != null && drawEnd != null)
			{
				graphicsSettings.setStroke(new BasicStroke((2 * strokeWidth)-1));
				graphicsSettings.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.4f));
				graphicsSettings.setPaint(Color.LIGHT_GRAY);
				
				
				
				if(actionNum == 2)
				{
					graphicsSettings.draw(drawLine(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y));
				}
				else if(actionNum == 3)
				{
					graphicsSettings.draw(drawRect(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y));
				}
				else
				{
					graphicsSettings.draw(drawEllipse(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y));
				}
				
			}
			
			
			
			
			
		}
		
		//functions to define how the shapes are made
		private Ellipse2D.Float drawBrush(int x, int y, int thickness)
		{
			return new Ellipse2D.Float(
                    x, y, (2 * thickness)-1, (2 * thickness)-1);
		}
		
		private Rectangle2D.Float drawRect(int x1, int y1, int x2, int y2)
		{
			return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2),
					Math.abs(x1 - x2), Math.abs(y1 - y2));
		}
		
		private Ellipse2D.Float drawEllipse(int x1, int y1, int x2, int y2)
		{
			return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2),
					Math.abs(x1 - x2), Math.abs(y1 - y2));
		}
		
		private Line2D.Float drawLine(int x1, int y1, int x2, int y2)
		{
			return new Line2D.Float(x1, y1, x2, y2);
		}
		
		private Rectangle2D drawRectfill(int x1, int y1, int x2, int y2)
		{
			
				return new Rectangle2D.Float(Math.min(x1, x2)+(((2 * strokeWidth)-1)/2)+1,
						Math.min(y1, y2)+(((2 * strokeWidth)-1)/2)+1,
						Math.abs(x1 - x2)-((2 * strokeWidth)-1), Math.abs(y1 - y2)-((2 * strokeWidth)-1));	
			
			
		}
		
		private Ellipse2D.Float drawEllipsefill(int x1, int y1, int x2, int y2)
		{
			
			return new Ellipse2D.Float(Math.min(x1, x2)+(((2 * strokeWidth)-1)/2)+1,
					Math.min(y1, y2)+(((2 * strokeWidth)-1)/2)+1,
					Math.abs(x1 - x2)-((2 * strokeWidth)-1), Math.abs(y1 - y2)-((2 * strokeWidth)-1));	
		
			
		}
		
		//add data from the top level ArrayLists to the appropriate ArrayList depending on the
		//currently selected layer
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void setLayers(){
			
			int size = shapes.size();
			
			
			shapeLayer[currentLayer].clear();
			sfillLayer[currentLayer].clear();
			fillLayer[currentLayer].clear();
			strokeColorLayer[currentLayer].clear();
			transparencyLayer[currentLayer].clear();
			strokeWidthLayer[currentLayer].clear();
			
			
			for (int i= 0; i < size; i++)
			{
				Shape copy = shapes.get(i);
				Shape copy2 = sfill.get(i);
				GradientPaint copy3 = fills.get(i);
				GradientPaint copy4 = strokeColors.get(i);
				
				shapeLayer[currentLayer].add(copy);
				sfillLayer[currentLayer].add(copy2);
				fillLayer[currentLayer].add(copy3);
				strokeColorLayer[currentLayer].add(copy4);
				transparencyLayer[currentLayer].add(new Float (transparencies.get(i)));
				strokeWidthLayer[currentLayer].add(new Integer (strokeWidths.get(i)));
			}
			
		
		}
		
	}
	
	//key listener to enable keyboard hotkeys
	public class lforkeypress implements KeyListener{

		
		public void keyPressed(KeyEvent arg0) {
			
		}

		
		public void keyReleased(KeyEvent arg0) {
			
			
		}

		
		public void keyTyped(KeyEvent e) {
			
			int keyCode = e.getKeyChar();
			System.out.println(keyCode);
			
			if(e.getKeyChar() == 49)
			{
				actionNum=1;
			}
			else if(e.getKeyChar() == 50)
			{
				actionNum=2;
				
			}
			else if(e.getKeyChar() == 51)
			{
				actionNum=3;
				
			}
			else if(e.getKeyChar() == 52)
			{
				actionNum=4;
				
			}
			else if(e.getKeyChar() == 53)
			{
				actionNum=5;
			}
			
			else if(e.getKeyChar() == 54)
			{
				Color tempColor = JColorChooser.showDialog(null,  "Pick primary stroke", strokeColor);
				
				if(tempColor != null)
				{
					strokeColor = tempColor;
					strokeColorBox.setBackground(strokeColor);
				}
				
			}
			else if(e.getKeyChar() == 55)
			{
				Color tempColor = JColorChooser.showDialog(null,  "Pick secondary stroke", strokeColor2);
				
				if(tempColor != null)
				{
					strokeColor2 = tempColor;
					strokeColorBox2.setBackground(strokeColor2);
				}
				
			}
			else if(e.getKeyChar() == 56)
			{
				Color tempColor = JColorChooser.showDialog(null,  "Pick primary fill", fillColor);
				
				if(tempColor != null)
				{
					fillColor = tempColor;
					fillColorBox.setBackground(fillColor);
				}
				
			}
			else if(e.getKeyChar() == 57)
			{
				Color tempColor = JColorChooser.showDialog(null,  "Pick secondary fill", fillColor2);
				
				if(tempColor != null)
				{
					fillColor2 = tempColor;
					fillColorBox2.setBackground(fillColor2);
				}
				
			}
			else if(e.getKeyChar() == 111)
			{
				int a=strokeSlider.getValue();
				strokeSlider.setValue(a-1);
				
			}
			else if(e.getKeyChar() == 112)
			{
				int a=strokeSlider.getValue();
				strokeSlider.setValue(a+1);
				
			}
			else if(e.getKeyChar() == 107)
			{
				int a=transparencySlider.getValue();
				transparencySlider.setValue(a-1);
				
			}
			else if(e.getKeyChar() == 108)
			{
				int a=transparencySlider.getValue();
				transparencySlider.setValue(a+1);
			}
			//handle switching of layers when holding down shift
			else if(e.getKeyChar() == 33)
			{
				currentLayer = 0;
				activeLayer.setText("Current Layer: " + 1);
					
				shapes = new ArrayList<Shape>(shapeLayer[0]);
				sfill = new ArrayList<Shape>(sfillLayer[0]);
				fills = new ArrayList<GradientPaint>(fillLayer[0]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[0]);	
				transparencies = new ArrayList<Float>(transparencyLayer[0]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[0]);
				
			}
			else if(e.getKeyChar() == 64)
			{
				currentLayer = 1;
				activeLayer.setText("Current Layer: " + 2);
					
				shapes = new ArrayList<Shape>(shapeLayer[1]);
				sfill = new ArrayList<Shape>(sfillLayer[1]);
				fills = new ArrayList<GradientPaint>(fillLayer[1]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[1]);	
				transparencies = new ArrayList<Float>(transparencyLayer[1]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[1]);
				
			}
			else if(e.getKeyChar() == 35)
			{
				currentLayer = 2;
				activeLayer.setText("Current Layer: " + 3);
					
				shapes = new ArrayList<Shape>(shapeLayer[2]);
				sfill = new ArrayList<Shape>(sfillLayer[2]);
				fills = new ArrayList<GradientPaint>(fillLayer[2]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[2]);	
				transparencies = new ArrayList<Float>(transparencyLayer[2]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[2]);
				
			}
			else if(e.getKeyChar() == 36)
			{
				currentLayer = 3;
				activeLayer.setText("Current Layer: " + 4);
					
				shapes = new ArrayList<Shape>(shapeLayer[3]);
				sfill = new ArrayList<Shape>(sfillLayer[3]);
				fills = new ArrayList<GradientPaint>(fillLayer[3]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[3]);	
				transparencies = new ArrayList<Float>(transparencyLayer[3]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[3]);
				
			}
			else if(e.getKeyChar() == 37)
			{
				currentLayer = 4;
				activeLayer.setText("Current Layer: " + 5);
					
				shapes = new ArrayList<Shape>(shapeLayer[4]);
				sfill = new ArrayList<Shape>(sfillLayer[4]);
				fills = new ArrayList<GradientPaint>(fillLayer[4]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[4]);	
				transparencies = new ArrayList<Float>(transparencyLayer[4]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[4]);
				
			}
			else if(e.getKeyChar() == 94)
			{
				currentLayer = 5;
				activeLayer.setText("Current Layer: " + 6);
					
				shapes = new ArrayList<Shape>(shapeLayer[5]);
				sfill = new ArrayList<Shape>(sfillLayer[5]);
				fills = new ArrayList<GradientPaint>(fillLayer[5]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[5]);	
				transparencies = new ArrayList<Float>(transparencyLayer[5]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[5]);
				
			}
			else if(e.getKeyChar() == 38)
			{
				currentLayer = 6;
				activeLayer.setText("Current Layer: " + 7);
					
				shapes = new ArrayList<Shape>(shapeLayer[6]);
				sfill = new ArrayList<Shape>(sfillLayer[6]);
				fills = new ArrayList<GradientPaint>(fillLayer[6]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[6]);	
				transparencies = new ArrayList<Float>(transparencyLayer[6]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[6]);
				
			}
			else if(e.getKeyChar() == 42)
			{
				currentLayer = 7;
				activeLayer.setText("Current Layer: " + 8);
					
				shapes = new ArrayList<Shape>(shapeLayer[7]);
				sfill = new ArrayList<Shape>(sfillLayer[7]);
				fills = new ArrayList<GradientPaint>(fillLayer[7]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[7]);	
				transparencies = new ArrayList<Float>(transparencyLayer[7]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[7]);
				
			}
			else if(e.getKeyChar() == 40)
			{
				currentLayer = 8;
				activeLayer.setText("Current Layer: " + 9);
					
				shapes = new ArrayList<Shape>(shapeLayer[8]);
				sfill = new ArrayList<Shape>(sfillLayer[8]);
				fills = new ArrayList<GradientPaint>(fillLayer[8]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[8]);	
				transparencies = new ArrayList<Float>(transparencyLayer[8]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[8]);
				
			}
			else if(e.getKeyChar() == 41)
			{
				currentLayer = 9;
				activeLayer.setText("Current Layer: " + 10);
					
				shapes = new ArrayList<Shape>(shapeLayer[9]);
				sfill = new ArrayList<Shape>(sfillLayer[9]);
				fills = new ArrayList<GradientPaint>(fillLayer[9]);			
				strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[9]);	
				transparencies = new ArrayList<Float>(transparencyLayer[9]);
				strokeWidths = new ArrayList<Integer>(strokeWidthLayer[9]);
				
			}
			
			
		}
		
		
	}


	
	//listener for sliders
	public class sliderListener implements ChangeListener{

		
		public void stateChanged(ChangeEvent e) {
			
			if(e.getSource() == transparencySlider)
			{
				int a = transparencySlider.getValue();
				transparency = (float)(0.01 * a);
				transparencyLabel.setText("Transparency: " + dformat.format((0.01 * a)));
			}
			else
			{
				strokeWidth = strokeSlider.getValue();
				strokeLabel.setText("Stroke thickness: " + strokeWidth);
			}
			
		}

	}

	//function to create layers
	@SuppressWarnings("rawtypes")
	public Box createLayer(int layerNumber){
		
		Box mainBox = Box.createVerticalBox();
		int y=layerNumber+1;
		layerShown[layerNumber]=true;
		JLabel layerName = new JLabel("Layer " + y);
		mainBox.add(layerName);
		
		
		//ArrayLists for these layers
		shapeLayer[layerNumber] = new ArrayList<Shape>();
		sfillLayer[layerNumber] = new ArrayList<Shape>();
		strokeColorLayer[layerNumber] = new ArrayList<GradientPaint>();
		fillLayer[layerNumber] = new ArrayList<GradientPaint>();
		transparencyLayer[layerNumber] = new ArrayList<Float>();
		strokeWidthLayer[layerNumber] = new ArrayList<Integer>();
	
		//set this layer as the active layer
		JButton setActive = new JButton("Set as active");
		setActive.addActionListener(new ActionListener(){

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				
					lastLayer = currentLayer;
					currentLayer = layerNumber;
					int d = layerNumber+1;
					activeLayer.setText("Current Layer: " + d);
					
										
					shapes = new ArrayList<Shape>(shapeLayer[layerNumber]);
					sfill = new ArrayList<Shape>(sfillLayer[layerNumber]);
					fills = new ArrayList<GradientPaint>(fillLayer[layerNumber]);			
					strokeColors = new ArrayList<GradientPaint>(strokeColorLayer[layerNumber]);	
					transparencies = new ArrayList<Float>(transparencyLayer[layerNumber]);
					strokeWidths = new ArrayList<Integer>(strokeWidthLayer[layerNumber]);
					
					
			}
		});
		
		//Hide/show this layer
		JButton toggleLayer = new JButton("Hide Layer");
		toggleLayer.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
					if(toggleLayer.getText() == "Hide Layer")
					{
						toggleLayer.setText("Show Layer");
						layerShown[layerNumber]=false;
						
					}
					else
					{
						toggleLayer.setText("Hide Layer");
						layerShown[layerNumber]=true;
					}
					board.repaint();
					
			}
		});
		
		//clear all the shapes in this layer
		JButton deleteLayer = new JButton("Delete Layer");
		deleteLayer.addActionListener(new ActionListener(){

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void actionPerformed(ActionEvent e) {
				
				for(int i=0; i<10; i++)
				{
					shapeLayer2[i] = new ArrayList<Shape>(shapeLayer[i]);
					sfillLayer2[i] = new ArrayList<Shape>(sfillLayer[i]);
					strokeColorLayer2[i] = new ArrayList<Color>(strokeColorLayer[i]);
					fillLayer2[i] = new ArrayList<Color>(fillLayer[i]);
					transparencyLayer2[i] = new ArrayList<Float>(transparencyLayer[i]);
					strokeWidthLayer2[i] = new ArrayList<Integer>(strokeWidthLayer[i]);
				}
				
				shapeLayer[layerNumber].clear();
				sfillLayer[layerNumber].clear();
				strokeColorLayer[layerNumber].clear();
				fillLayer[layerNumber].clear();
				transparencyLayer[layerNumber].clear();
				strokeWidthLayer[layerNumber].clear();
				
				if(currentLayer == layerNumber)
				{
					shapes = new ArrayList<Shape>();
					sfill = new ArrayList<Shape>();
					fills = new ArrayList<GradientPaint>();			
					strokeColors = new ArrayList<GradientPaint>();	
					transparencies = new ArrayList<Float>();
					strokeWidths = new ArrayList<Integer>();
				}
			
				board.repaint();

			}
		});
		
		
		//formatting the layout
		Dimension bmin = setActive.getMaximumSize();
		toggleLayer.setMaximumSize(bmin);
		deleteLayer.setMaximumSize(bmin);
		
		mainBox.add(setActive);
		mainBox.add(toggleLayer);
		mainBox.add(deleteLayer);
		
		
		layerName.setAlignmentX(CENTER_ALIGNMENT);
		setActive.setAlignmentX(CENTER_ALIGNMENT);
		toggleLayer.setAlignmentX(CENTER_ALIGNMENT);
		deleteLayer.setAlignmentX(CENTER_ALIGNMENT);
		
		
		return mainBox;
		
	} 
	
	
	
}
