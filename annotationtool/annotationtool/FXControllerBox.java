package annotationtool;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import util.GridBagConstraintBuilder;

public class FXControllerBox extends JFrame {

    private AnnotationToolApplication annotationTool;
    private static final int SWATCH_SIZE = 24;

    private Point initialClick;
    private FXControllerBox thisBox = this;



    private ComponentListener thisListener = new ComponentListener() {
        @Override
        public void componentResized(ComponentEvent e) {

        }

        @Override
        public void componentMoved(ComponentEvent e)
        {
            //annotationTool.setLocation(new Point(thisBox.getX() -1300, thisBox.getY()));

        }

        @Override
        public void componentShown(ComponentEvent e)
        {

        }

        @Override
        public void componentHidden(ComponentEvent e)
        {

        }
    };




    private static class SwatchIcon implements Icon {

        private Paint paint;

        public SwatchIcon(Paint p) {
            this.paint = p;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Dimension size = c.getSize();
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(paint);
            g2d.fillRect(x, y, size.width, size.height);
            if (((AbstractButton) c).isSelected()) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                g2d.drawRect(x, y, size.width, size.height);
            }
        }

        @Override
        public int getIconWidth() {
            return SWATCH_SIZE;
        }

        @Override
        public int getIconHeight() {
            return SWATCH_SIZE;
        }
    }

    private static final Color[] penColors = {
            new Color(255, 0, 0, 255),
            new Color(255, 128, 0, 255),
            new Color(255, 255, 0, 255),
            new Color(0, 255, 0, 255),
            new Color(0, 0, 255, 255),
            new Color(255, 0, 255, 255),
            new Color(0, 0, 0, 255),
            new Color(255, 255, 255, 255),};

    private static final Color[] highlighterColors = {
            new Color(255, 0, 0, 128),
            new Color(255, 128, 0, 128),
            new Color(255, 255, 0, 128),
            new Color(0, 255, 0, 128),
            new Color(0, 0, 255, 128),
            // new Color(255, 255, 255, 10)
            new Color(0f,0f,0f,0.1f)
    };



    private static class PaintPalletteActionListener implements ActionListener {

        private AnnotationToolApplication annotationTool;
        private Color paint;

        public PaintPalletteActionListener(AnnotationToolApplication at, Color ppi)
        {
            annotationTool = at;
            paint = ppi;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            annotationTool.setPaint(paint);
        }
    }

    private JRadioButton thinLine;
    private JRadioButton mediumLine;
    private JRadioButton thickLine;
    private JRadioButton hugeLine;


    public FXControllerBox(AnnotationToolApplication at) {
        super("Tools");

        this.addComponentListener(thisListener);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        annotationTool = at;
        setLayout(new GridBagLayout());
        GridBagConstraintBuilder gbcb = new GridBagConstraintBuilder(6);
        this.setAlwaysOnTop(true);

        ButtonGroup toolGroup = new ButtonGroup();
        ActionListener setMakingTextBoxFalse = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setMakingTextBox(false);
            }
        };

        add(new JLabel("Pens"), gbcb.fullWidth().build());
        gbcb.nextY().singleWidth();
        boolean first = true;
        for (Color ppi : penColors) {
            JRadioButton jrb = new JRadioButton(null, new SwatchIcon(ppi), first);
            jrb.addActionListener(new PaintPalletteActionListener(at, ppi));
            jrb.addActionListener(setMakingTextBoxFalse);
            add(jrb, gbcb.build());
            gbcb.nextX();
            toolGroup.add(jrb);
            if (first) {
                jrb.doClick();
                first = false;
            }
        }
        add(new JLabel("Highlighters"), gbcb.fullWidth().nextX().build());
        gbcb.nextY().singleWidth();

        for (Color ppi : highlighterColors) {
            JRadioButton jrb = new JRadioButton(null, new SwatchIcon(ppi), first);
            jrb.addActionListener(new PaintPalletteActionListener(at, ppi));
            jrb.addActionListener(setMakingTextBoxFalse);
            add(jrb, gbcb.build());
            gbcb.nextX();
            toolGroup.add(jrb);
        }
        add(new JLabel("Pen Sizes"), gbcb.fullWidth().nextY().build());

        thinLine = new JRadioButton("Thin");
        thinLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setStroke(
                        new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
        });
        thinLine.addActionListener(setMakingTextBoxFalse);
        add(thinLine, gbcb.nextY().build());
        gbcb.nextY();

        thinLine.doClick();

        mediumLine = new JRadioButton("Medium");
        mediumLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setStroke(
                        new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
        });
        mediumLine.addActionListener(setMakingTextBoxFalse);
        add(mediumLine, gbcb.build());
        gbcb.nextY();

        thickLine = new JRadioButton("Thick");
        thickLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setStroke(
                        new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
        });
        thickLine.addActionListener(setMakingTextBoxFalse);
        add(thickLine, gbcb.build());
        gbcb.nextY();

        hugeLine = new JRadioButton("Huge");
        hugeLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setStroke(
                        new BasicStroke(70, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
        });
        hugeLine.addActionListener(setMakingTextBoxFalse);
        add(hugeLine, gbcb.build());
        gbcb.nextY();

        ButtonGroup thicknessGroup = new ButtonGroup();
        thicknessGroup.add(thinLine);
        thicknessGroup.add(mediumLine);
        thicknessGroup.add(thickLine);
        thicknessGroup.add(hugeLine);

        add(new JLabel("----------"), gbcb.build());
        gbcb.nextY();

        JButton eraseButton = new JButton("Erase Transparent");
        eraseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.doClear();
            }
        });
        add(eraseButton, gbcb.build());
        gbcb.nextY();

        JButton eraseWhiteButton = new JButton("Erase White");
        eraseWhiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.doClear(new Color(255, 255, 255, 255));
            }
        });
        add(eraseWhiteButton, gbcb.build());
        gbcb.nextY();

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.undo();
            }
        });
        add(undoButton, gbcb.build());
        gbcb.nextY();

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.redo();
            }
        });
        add(redoButton, gbcb.build());
        gbcb.nextY();

        JButton killHistoryButton = new JButton("Clear History");
        killHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.clearHistory();
            }
        });
        add(killHistoryButton, gbcb.build());
        gbcb.nextY();

        JButton toggleClickableButton = new JButton("Toggle Clickable");
        toggleClickableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.toggleClickable();
            }
        });
        add(toggleClickableButton, gbcb.build());
        gbcb.nextY();


        add(new JLabel("----------"), gbcb.build());
        gbcb.nextY();

        JButton textBoxAdder = new JButton("Add Text");
        textBoxAdder.addActionListener(new ActionListener()
                                       {
                                           @Override
                                           public void actionPerformed(ActionEvent e)
                                           {
                                               annotationTool.setMakingTextBox(true);
                                           }
                                       }

        );
        add(textBoxAdder, gbcb.build());
        gbcb.nextY();

        add(new JLabel("Text Size:"), gbcb.build());
        gbcb.nextY();

        JComboBox textSizes = new JComboBox
                (
                        new Integer[]{25,50,75,100,125,150,175,200}
                );
        textSizes.setSelectedItem(100);
        textSizes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                annotationTool.setTextSize((Integer) textSizes.getSelectedItem());
            }
        });
        add(textSizes, gbcb.build());
        gbcb.nextY();

        add(new JLabel("Text Color:"), gbcb.build());
        gbcb.nextY();

        JComboBox textColors = new JComboBox(penColors);                                // could replace with a separate array if desired.
        textColors.setSelectedItem(Color.BLACK);
        textColors.setRenderer(new MyCellRenderer());
        textColors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setTextColor((Color) textColors.getSelectedItem());
            }
        });
        add(textColors, gbcb.build());
        gbcb.nextY();


        add(new JLabel("----------"), gbcb.build());
        gbcb.nextY();

        JButton bringToTop = new JButton("Bring to top");
        bringToTop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.toFront();
                annotationTool.setAlwaysOnTop(true);
            }
        });
        add(bringToTop, gbcb.build());
        gbcb.nextY();

        JButton sendBack = new JButton("Send to back");
        sendBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.setAlwaysOnTop(false);
                annotationTool.toBack();
            }
        });
        add(sendBack, gbcb.build());
        gbcb.nextY();

        JButton save = new JButton("Save image");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                annotationTool.doSave();
            }
        });
        add(save, gbcb.build());
        gbcb.nextY();


        JButton quit = new JButton("Exit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(
                        FXControllerBox.this, "Confirm quit?", "Confirm quit",
                        JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        add(quit, gbcb.build());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(
                        FXControllerBox.this, "Confirm quit?", "Confirm quit",
                        JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        gbcb.nextY();
    }

    /**
     * https://stackoverflow.com/questions/18830098/pick-color-with-jcombobox-java-swing
     */
    private class MyCellRenderer extends JButton implements ListCellRenderer {
        public MyCellRenderer() {
            setOpaque(true);

        }
        boolean b=false;
        @Override
        public void setBackground(Color bg) {
            // TODO Auto-generated method stub
            if(!b)
            {
                return;
            }

            super.setBackground(bg);
        }
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,

                boolean isSelected,
                boolean cellHasFocus)
        {

            b=true;
            setText(" ");
            setBackground((Color)value);
            b=false;
            return this;
        }
    }


}
