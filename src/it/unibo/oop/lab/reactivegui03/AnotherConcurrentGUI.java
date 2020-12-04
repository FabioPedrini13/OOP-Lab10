package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent1 agent1 = new Agent1();
        final Agent2 agent2 = new Agent2();
        new Thread(agent1).start();
        new Thread(agent2).start();
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(e -> agent1.stopCounting());
        up.addActionListener(e -> agent1.increasing());
        down.addActionListener(e -> agent1.decreasing());
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent1 implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private int counter;
        private boolean direction = true;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(() -> 
                        AnotherConcurrentGUI.this.display.setText(Integer.toString(this.counter)));
                    this.counter += this.direction ? +1 : -1;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
            AnotherConcurrentGUI.this.up.setEnabled(false);
            AnotherConcurrentGUI.this.down.setEnabled(false);
            AnotherConcurrentGUI.this.stop.setEnabled(false);
        }

        public void increasing() {
            this.direction = true;
        }

        public void decreasing() {
            this.direction = false;
        }
    }

    private class Agent2 implements Runnable {
        private static final int MILLISECONDS = 10_000;
        @Override
        public void run() {
            try {
                Thread.sleep(Agent2.MILLISECONDS);
                SwingUtilities.invokeAndWait(() -> this.stopCounting());
            } catch (InvocationTargetException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        public void stopCounting() {
            AnotherConcurrentGUI.this.stop.doClick();
            AnotherConcurrentGUI.this.up.setEnabled(false);
            AnotherConcurrentGUI.this.down.setEnabled(false);
            AnotherConcurrentGUI.this.stop.setEnabled(false);
        }
    }
}
