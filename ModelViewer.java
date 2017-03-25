import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.lang.*;

public class ModelViewer {

    private static final int INIT_WIDTH = 1024;
    private static final int INIT_HEIGHT = 768;
    private static float SCALE;
    private JFrame m_frame;

    private Canvas m_canvas;

    private JSlider m_sliderRotateX;
    private JSlider m_sliderRotateY;
    private JSlider m_sliderRotateZ;

    private JButton m_btnScaleUp;
    private JButton m_btnScaleDown;
    private JButton m_btnIncrX;
    private JButton m_btnDecrX;
    private JButton m_btnIncrY;
    private JButton m_btnDecrY;
    private JButton m_btnIncrZ;
    private JButton m_btnDecrZ;

    private JCheckBox m_chkRenderWireframe;
    private JCheckBox m_chkRenderSolid;
    private JCheckBox m_chkCullBackFaces;

    private JMenuItem m_menuOpenModelFile;

    private Model m_currentModel;
    private Timer timer;

    private static float rotateXValuePrevious;
    private static float rotateYValuePrevious;
    private static float rotateZValuePrevious;

    //////////////////////////////////////////////////////////////////////////////
    private ChangeListener m_sliderChangeListener = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
            final JSlider source = (JSlider) e.getSource();
            if (m_currentModel != null) {
                //X slider has been moved
                if (source == m_sliderRotateX) {
                    float value = (float) Math.toRadians(m_sliderRotateX.getValue() - rotateXValuePrevious); //current value - previous value
                    m_currentModel.transform(Matrix4F.createRotateXInstance(value));
                    rotateXValuePrevious = m_sliderRotateX.getValue(); //update previous value

                    //Y slider has been moved
                } else if (source == m_sliderRotateY) {
                    float value = (float) Math.toRadians(m_sliderRotateY.getValue() - rotateYValuePrevious); //current value - previous value
                    m_currentModel.transform(Matrix4F.createRotateYInstance(value));
                    rotateYValuePrevious = m_sliderRotateY.getValue(); //update previous value

                    //Z slider has been moved
                } else if (source == m_sliderRotateZ) {
                    float value = (float) Math.toRadians(m_sliderRotateZ.getValue() - rotateZValuePrevious); //current value - previous value
                    m_currentModel.transform(Matrix4F.createRotateZInstance(value));
                    rotateZValuePrevious = m_sliderRotateZ.getValue(); //update previous value
                }
                m_canvas.repaint();
            }
        }
    };

    private ActionListener m_btnActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            final Object source = e.getSource();
            if (m_currentModel != null) {
                // scale changes
                if (source == m_btnScaleUp) {
                    SCALE = 1.1f;   //increase scale
                    m_currentModel.transform(Matrix4F.createScaleInstance(SCALE, SCALE, SCALE));
                } else if (source == m_btnScaleDown) {
                    SCALE = 0.9f;   //decrease scale
                    m_currentModel.transform(Matrix4F.createScaleInstance(SCALE, SCALE, SCALE));
                }
                // translation changes
                else if (source == m_btnIncrX) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(1, 0, 0));
                } else if (source == m_btnDecrX) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(-1, 0, 0));
                } else if (source == m_btnIncrY) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(0, 1, 0));
                } else if (source == m_btnDecrY) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(0, -1, 0));
                } else if (source == m_btnIncrZ) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(1, 1, 1));
                } else if (source == m_btnDecrZ) {
                    m_currentModel.transform(Matrix4F.createTranslateInstance(-1, -1, -1));
                }
                m_canvas.repaint();
            }
        }
    };

    private ActionListener m_chkActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            final Object source = e.getSource();
            boolean selected;
            //Wireframe
            if (source == m_chkRenderWireframe) {
                selected = m_chkRenderWireframe.getModel().isSelected();
                m_canvas.setRenderWireFrame(selected);

                //Render Solid
            } else if (source == m_chkRenderSolid) {
                selected = m_chkRenderSolid.getModel().isSelected();
                m_canvas.setRenderSolid(selected);

                //Cull Backfaces
            } else if (source == m_chkCullBackFaces) {
                selected = m_chkCullBackFaces.getModel().isSelected();
                m_canvas.setCullBackFaces(selected);
            }
            m_canvas.repaint();
        }
    };

    private ActionListener m_menuActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == m_menuOpenModelFile) {
                final Model model = loadModelFile();
                if (model != null) {
                    //setting model up once file loaded
                    m_currentModel = model;
                    m_canvas.setModel(model);
                    startRenderLoop(10);
                }
            }
        }
    };

    //Render loop used to continuously update normals
    //modified from Arno demo
    private void startRenderLoop(final int frameRate) {
        if (timer != null) timer.stop();

        final float delay = 1000.f / frameRate;
        timer = new Timer((int) delay, e -> renderFrame());
        timer.setInitialDelay(0);
        timer.start();
    }

    private void renderFrame() {
        update();
        m_canvas.repaint();
    }

    private void update() {
        m_currentModel.updateNormals();
    }

    //////////////////////////////////////////////////////////////////////////////
    private ModelViewer() {
    }

    /**
     * Factory method for {@link ModelViewer}.
     */
    private static ModelViewer create() {
        final ModelViewer viewer = new ModelViewer();
        SwingUtilities.invokeLater(viewer::createGui);
        return viewer;
    }

    /**
     * Creates the GUI. Must be called from the EDT.
     */
    private void createGui() {
        m_frame = new JFrame("Model Viewer");
        m_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // setup the content pane
        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        m_frame.setContentPane(contentPane);

        final Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

        // setup the canvas and control panel
        m_canvas = new Canvas();
        m_canvas.setBorder(border);
        contentPane.add(m_canvas, BorderLayout.CENTER);
        final JComponent controlPanel = createControlPanel();
        controlPanel.setBorder(border);
        contentPane.add(controlPanel, BorderLayout.LINE_START);
        // add the menu
        final JMenuBar menuBar = new JMenuBar();
        m_frame.setJMenuBar(menuBar);
        final JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        m_menuOpenModelFile = new JMenuItem("Open");
        m_menuOpenModelFile.addActionListener(m_menuActionListener);
        fileMenu.add(m_menuOpenModelFile);

        // register a key event dispatcher to get a turn in handling all
        // key events, independent of which component currently has the focus
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            System.exit(0);
                            return true; // consume the event
                        default:
                            return false; // do not consume the event
                    }
                });

        m_frame.setSize(new Dimension(INIT_WIDTH, INIT_HEIGHT));
        m_frame.setVisible(true);
    }

    /**
     * Creates and returns the control panel. Must be called from the EDT.
     */
    private JComponent createControlPanel() {
        final JPanel toolbar = new JPanel(new GridBagLayout());

        final GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.gridx = 0;
        gbcDefault.gridy = GridBagConstraints.RELATIVE;
        gbcDefault.gridwidth = 2;
        gbcDefault.fill = GridBagConstraints.HORIZONTAL;
        gbcDefault.insets = new Insets(5, 10, 5, 10);
        gbcDefault.anchor = GridBagConstraints.FIRST_LINE_START;
        gbcDefault.weightx = 0.5;

        final GridBagConstraints gbcLabels =
                (GridBagConstraints) gbcDefault.clone();
        gbcLabels.insets = new Insets(5, 10, 0, 10);

        final GridBagConstraints gbcTwoCol =
                (GridBagConstraints) gbcDefault.clone();
        gbcTwoCol.gridwidth = 1;
        gbcTwoCol.gridx = 0;
        gbcTwoCol.insets.right = 5;

        GridBagConstraints gbc;

        // setup the rotation sliders
        m_sliderRotateX = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
        m_sliderRotateY = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
        m_sliderRotateZ = new JSlider(JSlider.HORIZONTAL, 0, 360, 0);
        m_sliderRotateX.setPaintLabels(true);
        m_sliderRotateY.setPaintLabels(true);
        m_sliderRotateZ.setPaintLabels(true);
        m_sliderRotateX.setPaintTicks(true);
        m_sliderRotateY.setPaintTicks(true);
        m_sliderRotateZ.setPaintTicks(true);
        m_sliderRotateX.setMajorTickSpacing(90);
        m_sliderRotateY.setMajorTickSpacing(90);
        m_sliderRotateZ.setMajorTickSpacing(90);
        m_sliderRotateX.addChangeListener(m_sliderChangeListener);
        m_sliderRotateY.addChangeListener(m_sliderChangeListener);
        m_sliderRotateZ.addChangeListener(m_sliderChangeListener);
        gbc = gbcDefault;
        toolbar.add(new JLabel("Rotation X:"), gbcLabels);
        toolbar.add(m_sliderRotateX, gbc);
        toolbar.add(new JLabel("Rotation Y:"), gbcLabels);
        toolbar.add(m_sliderRotateY, gbc);
        toolbar.add(new JLabel("Rotation Z:"), gbcLabels);
        toolbar.add(m_sliderRotateZ, gbc);

        m_btnScaleDown = new JButton("- size");
        m_btnScaleUp = new JButton("+ size");
        m_btnScaleDown.addActionListener(m_btnActionListener);
        m_btnScaleUp.addActionListener(m_btnActionListener);
        gbc = (GridBagConstraints) gbcTwoCol.clone();
        toolbar.add(m_btnScaleDown, gbc);
        gbc.gridx = 1;
        gbc.insets.left = gbc.insets.right;
        gbc.insets.right = gbcDefault.insets.right;
        toolbar.add(m_btnScaleUp, gbc);

        m_btnIncrX = new JButton("+ x");
        m_btnDecrX = new JButton("- x");
        m_btnIncrY = new JButton("+ y");
        m_btnDecrY = new JButton("- y");
        m_btnIncrZ = new JButton("+ z");
        m_btnDecrZ = new JButton("- z");
        m_btnIncrX.addActionListener(m_btnActionListener);
        m_btnDecrX.addActionListener(m_btnActionListener);
        m_btnIncrY.addActionListener(m_btnActionListener);
        m_btnDecrY.addActionListener(m_btnActionListener);
        m_btnIncrZ.addActionListener(m_btnActionListener);
        m_btnDecrZ.addActionListener(m_btnActionListener);

        gbc = (GridBagConstraints) gbcTwoCol.clone();
        toolbar.add(m_btnDecrX, gbc);
        gbc.gridx = 1;
        gbc.insets.left = gbc.insets.right;
        gbc.insets.right = gbcDefault.insets.right;
        toolbar.add(m_btnIncrX, gbc);

        gbc = (GridBagConstraints) gbcTwoCol.clone();
        toolbar.add(m_btnDecrY, gbc);
        gbc.gridx = 1;
        gbc.insets.left = gbc.insets.right;
        gbc.insets.right = gbcDefault.insets.right;
        toolbar.add(m_btnIncrY, gbc);

        gbc = (GridBagConstraints) gbcTwoCol.clone();
        toolbar.add(m_btnDecrZ, gbc);
        gbc.gridx = 1;
        gbc.insets.left = gbc.insets.right;
        gbc.insets.right = gbcDefault.insets.right;
        toolbar.add(m_btnIncrZ, gbc);

        // add check boxes
        gbc = gbcDefault;

        m_chkRenderWireframe = new JCheckBox("Render Wireframe");
        m_chkRenderWireframe.setSelected(true);
        m_chkRenderWireframe.addActionListener(m_chkActionListener);
        toolbar.add(m_chkRenderWireframe, gbc);

        m_chkRenderSolid = new JCheckBox("Render Solid");
        m_chkRenderSolid.setSelected(true);
        m_chkRenderSolid.addActionListener(m_chkActionListener);
        toolbar.add(m_chkRenderSolid, gbc);

        m_chkCullBackFaces = new JCheckBox("Cull Back Faces");
        m_chkCullBackFaces.setSelected(true);
        m_chkCullBackFaces.addActionListener(m_chkActionListener);
        gbc = (GridBagConstraints) gbcDefault.clone();
        gbc.weighty = 1.;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        toolbar.add(m_chkCullBackFaces, gbc);

        return toolbar;
    }

    /**
     * Displays a chooser dialog and loads the selected model.
     *
     * @return The model, or null if the user cancels the action or something
     * goes wrong.
     */
    private Model loadModelFile() {
        // show a file chooser for model files
        JFileChooser chooser = new JFileChooser("./");
        chooser.setFileFilter(new FileNameExtensionFilter(
                ".dat model files", "dat"));
        int retVal = chooser.showOpenDialog(m_frame);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String fileName = file.getName();
            // try to load the model from the selected file
            final Model model = Model.loadModel(file);

            if (model != null) {
                // initialise the scale value so that the model fits into the
                // render window
                if(fileName.equals("dinosaur.dat") || fileName.equals("teapot.dat")) {
                    SCALE = 100.0f;
                } else {
                    SCALE = 40.0f;
                }
                //resetting sliders to 0 when new dat file loaded
                m_sliderRotateX.setValue(0);
                m_sliderRotateY.setValue(0);
                m_sliderRotateZ.setValue(0);
                model.transform(Matrix4F.createScaleInstance(SCALE, SCALE, SCALE));
                m_canvas.repaint();
            }
            return model;
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("-------------------------------------");
        System.out.println("159.235 Assignment 3, Semester 2 2016");
        System.out.println("Submitted by: Pitts, Kelly, 09098321");
        System.out.println("-------------------------------------");

        ModelViewer.create();
    }
}

/**
 * This class can load model data from files and manage it.
 */
class Model {

    //Setting up datatypes (ArrayLists) to store the vertices and triangles
    private final ArrayList<Vector4F> vertices = new ArrayList<>();
    private final ArrayList<Triangle> triangles = new ArrayList<>();
    private ArrayList<Triangle> original = new ArrayList<>();
    private ArrayList<Triangle> transformed = new ArrayList<>();

    // the largest absolute coordinate value of the untransformed model data
    private float m_maxSize;
    private Matrix4F modelMat = new Matrix4F();

    private Model() {
    }

    /**
     * Creates a {@link Model} instance for the data in the specified file.
     *
     * @param file The file to load.
     * @return The {@link Model}, or null if an error occurred.
     */
    public static Model loadModel(final File file) {
        final Model model = new Model();

        // read the data from the file
        if (!model.loadModelFromFile(file)) {
            return null;
        }
        return model;
    }

    private void initArrayLists() {
        original = triangles;

        // Create a copy of the polygon mesh.
        transformed = new ArrayList<>(triangles.size());
        for (final Triangle triangle : triangles) {
            transformed.add(new Triangle(
                    new Vector4F(triangle.v[0]),
                    new Vector4F(triangle.v[1]),
                    new Vector4F(triangle.v[2])
            ));
        }
    }

    /**
     * Reads model data from the specified file.
     *
     * @param file The file to load.
     * @return True on success, false otherwise.
     */
    protected boolean loadModelFromFile(final File file) {
        m_maxSize = 0.f;

        int m_numVertices;
        int m_numTriangles;

        try (final Scanner scanner = new Scanner(file)) {
            // the first line specifies the vertex count
            m_numVertices = scanner.nextInt();

            // read all vertex coordinates
            float x, y, z;
            for (int i = 0; i < m_numVertices; ++i) {
                // advance the position to the beginning of the next line
                scanner.nextLine();

                // read the vertex coordinates
                x = scanner.nextFloat();
                y = scanner.nextFloat();
                z = scanner.nextFloat();

                // TODO store the vertex data
                vertices.add(new Vector4F(x, y, z, 1));

                // determine the max value in any dimension in the model file
                m_maxSize = Math.max(m_maxSize, Math.abs(x));
                m_maxSize = Math.max(m_maxSize, Math.abs(y));
                m_maxSize = Math.max(m_maxSize, Math.abs(z));
            }

            // the next line specifies the number of triangles
            scanner.nextLine();
            m_numTriangles = scanner.nextInt();

            // read all polygon data (assume triangles); these are indices into
            // the vertex array
            int v1, v2, v3;
            for (int i = 0; i < m_numTriangles; ++i) {
                scanner.nextLine();

                // the model files start with index 1, we start with 0
                v1 = scanner.nextInt() - 1;
                v2 = scanner.nextInt() - 1;
                v3 = scanner.nextInt() - 1;

                // TODO store the triangle data in a suitable data structure

                triangles.add(new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3)));
            }

        } catch (FileNotFoundException e) {
            System.err.println("No such file " + file.toString() + ": "
                    + e.getMessage());
            return false;
        } catch (NoSuchElementException e) {
            System.err.println("Invalid file format: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Something went wrong while reading the"
                    + " model file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        initArrayLists();
        System.out.println("Number of vertices in model: " + m_numVertices);
        System.out.println("Number of triangles in model: " + m_numTriangles);
        return true;
    }

    public void transform(final Matrix4F transform) {
        // Update the model transform.
        modelMat = modelMat.multiply(transform);

        // Update the transformed model with the original model transformed by the
        // current transform.
        //Taken from Arno demo
        for (int i = 0; i < original.size(); ++i) {
            final Triangle tIn = original.get(i);
            final Triangle tOut = transformed.get(i);
            for (int vertex = 0; vertex < tIn.v.length; ++vertex) {
                modelMat.multiply(tIn.v[vertex], tOut.v[vertex]);
            }
        }
    }

    public void updateNormals() {
        transformed.forEach(Triangle::calculateNormal);
    }

    /**
     * Returns the largest absolute coordinate value of the original,
     * untransformed model data.
     */
    public float getMaxSize() {
        return m_maxSize;
    }

    ArrayList<Triangle> getTriangles() {
        return transformed;
    }
}

/**
 * The draw area.
 */
class Canvas extends JPanel {

    private static final long serialVersionUID = 1L;
    private Model m_model;

    private Paint modelColour = Color.BLUE;

    private boolean renderWireframe = true;
    private boolean renderSolid = true;
    private boolean cullBackFaces = true;

    Canvas() {
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    void setModel(final Model model) {
        m_model = model;
    }

    //Set boolean variables to true or false depending on if checkbox checked
    void setRenderWireFrame(boolean x) {
        renderWireframe = x;
    }
    void setRenderSolid(boolean x) {
        renderSolid = x;
    }
    void setCullBackFaces(boolean x) {
        cullBackFaces = x;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (m_model == null) return;

        final ArrayList<Triangle> triangles = m_model.getTriangles();

        //Checking arraylist not empty
        if (triangles == null || triangles.size() == 0) return;
        //Checking what checkboxes are checked
        if (!renderSolid && !renderWireframe) return;

        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // Move the origin to the center of the canvas and flip the y-axis.
        g2.translate(getWidth() / 2., getHeight() / 2.);
        g2.transform(AffineTransform.getScaleInstance(1., -1.));

        //Modified from Arno demo
        Polygon poly = new Polygon(new int[3], new int[3], 3);
        for (final Triangle triangle : triangles) {
            if (cullBackFaces && triangle.normal.z <= 0.f) continue;
            poly.xpoints[0] = (int) triangle.v[0].x;
            poly.xpoints[1] = (int) triangle.v[1].x;
            poly.xpoints[2] = (int) triangle.v[2].x;
            poly.ypoints[0] = (int) triangle.v[0].y;
            poly.ypoints[1] = (int) triangle.v[1].y;
            poly.ypoints[2] = (int) triangle.v[2].y;

            //draws solid
            if(renderSolid && !renderWireframe) {
                g2.setPaint(modelColour);
                g2.draw(poly);
                g2.fill(poly);
            }
            //draws solid with wireframe
            else if(renderSolid) {
                g2.setPaint(modelColour);
                g2.fill(poly);
            }
            //draws wireframe
            if (renderWireframe) {
                g2.setPaint(Color.BLACK);
                g2.draw(poly);
            }
        }

        g2.dispose();
    }
}