/**
 * Created by yinboli on 1/16/16.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout {

    private JFrame frame;
    private Container pane;
    private View view;
    private Model model;
    private boolean gameStarted = false;
    private boolean lost = false;
    private boolean won = false;

    static int fps;
    static int speed;
    final int UPDATE_PERIOD = 20 / speed;
    final int REPAINT_PERIOD = 1000/fps;
    final int BALL_RADIUS = 5;
    final int PADDLE_WIDTH = 80;
    final int PADDLE_HEIGHT = 10;
    int PANEL_WIDTH = 400;
    int PANEL_HEIGHT = 400;
    int BLOCK_WIDTH = 60;
    int BLOCK_HEIGHT = 20;
    int score = 0;

    // constructor for the game
    // instantiates all of the top-level classes (model, view)
    // and tells the model to start the game
    Breakout() {
        frame = new JFrame("Breakout");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        frame.setResizable(true);
        pane = frame.getContentPane();
        view = new View();
        pane.add(view);
        frame.setVisible(true);

        model = new Model();
    }

    // game elements
    class Block {
        private Color color;
        private int xCoord, yCoord;
        private int width = BLOCK_WIDTH, height = BLOCK_HEIGHT;
        private boolean destroyed = false;
        private boolean hit = false;

        private int health = (int)(Math.random() * 3 + 1);

        public Block(int x, int y) {
            xCoord = x;
            yCoord = y;
            if (health == 3) {
                color = Color.BLUE;
            } else if (health == 2) {
                color = Color.green;
            } else if (health == 1) {
                color = Color.CYAN;
            }
        }

        public int get_xCoord() {
            return xCoord;
        }

        public void set_xCoord(int x) {
            xCoord = x;
        }

        public int get_yCoord() {
            return yCoord;
        }

        public void set_yCoord(int y) {
            yCoord = y;
        }

        public int get_width() {
            return width;
        }

        public void set_width(int w) {
            width = w;
        }

        public int get_height() {
            return height;
        }

        public void set_height(int h) {
            height = h;
        }

        public boolean get_destroyed() { return destroyed;}

        public void set_destroyed(boolean bool) { destroyed = bool; }

        public boolean get_hit () { return hit; }

        public void set_hit(boolean bool) { hit = bool; }

        public void drawBlock(Graphics g) {
            if (!destroyed) {
                g.setColor(color);
                g.fillRect(xCoord, yCoord, width, height);
            }
        }

        public boolean hitLeftOrRight(int ball_xCoord, int ball_yCoord) {
            return hit && (ball_yCoord >= yCoord) && (ball_yCoord <= (yCoord + BLOCK_HEIGHT));
                    //&& ((ball_xCoord <= xCoord+1) || (ball_xCoord >= (xCoord + BLOCK_WIDTH-1)));
        }

        public boolean hitTopOrBottom(int ball_xCoord, int ball_yCoord) {
            return hit && (ball_xCoord <= (xCoord + BLOCK_WIDTH)) && (ball_xCoord >= xCoord);
                    //&& ((ball_yCoord <= yCoord+1) || (ball_yCoord >= (ball_yCoord + BLOCK_HEIGHT - 1)));
        }
    }

    class Ball {
        private Color color;
        private int xCoord, yCoord, radius = BALL_RADIUS, xDir = 1, yDir = 1; // coordinate of center

        public Ball(int x, int y, Color c) {
            xCoord = x;
            yCoord = y;
            color = c;
        }

        public int get_xCoord() {
            return xCoord;
        }

        public void set_xCoord(int x) {
            xCoord = x;
        }

        public int get_yCoord() {
            return yCoord;
        }

        public void set_yCoord(int y) {
            yCoord = y;
        }

        public int get_xDir() {
            return xDir;
        }

        public void set_xDir(int x) {
            xDir = x;
        }

        public int get_yDir() {
            return yDir;
        }

        public void set_yDir(int y) {
            yDir = y;
        }

        public int get_radius() {
            return radius;
        }

        public void drawBall(Graphics g) {
            g.setColor(color);
            g.fillOval(xCoord - radius, yCoord - radius, radius * 2, radius * 2);
        }

        public void ballMove() {
            if (!gameStarted) {
                view.ball.set_xCoord(view.paddle.get_xCoord()+(PADDLE_WIDTH/2));
            } else {
                xCoord += xDir;
                yCoord += yDir;
            }
        }
    }

    class Paddle {
        private Color color;
        private int xCoord, yCoord, width = PADDLE_WIDTH, height = PADDLE_HEIGHT, change_xCoord;

        public Paddle (int x, int y, Color c) {
            xCoord = x;
            yCoord = y;
            color = c;
        }

        public int get_xCoord() {
            return xCoord;
        }

        public void set_xCoord(int x) {
            xCoord = x;
        }

        public int get_yCoord() {
            return yCoord;
        }

        public void set_yCoord(int y) {
            yCoord = y;
        }

        public int get_width() {
            return width;
        }

        public int get_height() {
            return height;
        }

        public void drawPaddle(Graphics g) {
            g.setColor(color);
            g.fillRect(xCoord, yCoord, width, height);
        }

        public void paddleMove() {
            xCoord += change_xCoord;
            if (xCoord < 0) { xCoord=0; }
            else if (xCoord > PANEL_WIDTH - PADDLE_WIDTH) { xCoord = PANEL_WIDTH - PADDLE_WIDTH; }
        }
    }

    // model keeps track of game state (objects in the game)
    // contains a Timer that ticks periodically to advance the game
    // AND calls an update() method in the View to tell it to redraw
    class Model {
        private Timer updateTimer;
        private Timer repaintTimer;

        ActionListener updateTimer_taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.update();
            }
        };

        ActionListener repaintTimer_taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.myRepaint();
            }
        };

        public Model() {
            updateTimer = new Timer(UPDATE_PERIOD, updateTimer_taskPerformer);
            repaintTimer = new Timer(REPAINT_PERIOD, repaintTimer_taskPerformer);
            updateTimer.start();
            repaintTimer.start();
        }
    }

    // game window
    // draws everything based on the game state
    // receives notification from the model when something changes, and
    // draws components based on the model.
    class View extends JComponent {

        Block[][] blocks = new Block[5][5];
        int num_avlBlocks = 25;
        Ball ball;
        Paddle paddle;

        // Object properties
        private int mouseX;
        private int mouseY;

        public View() {
            for (int i=0; i<5; i++) {
                for (int j=0; j<5; j++) {
                    blocks[i][j] = new Block(40 + j * (BLOCK_WIDTH+5),
                            40 + i * (BLOCK_HEIGHT+5));
                }
            }
            paddle = new Paddle((int)frame.getHeight()*3/8, (int)frame.getWidth()*7/8,Color.blue);
            ball = new Ball(paddle.get_xCoord() + (paddle.get_width()/2),
                    paddle.get_yCoord() - BALL_RADIUS,Color.red);

            // set available to use keyboard
            this.setFocusable(true);

            // Use left and right arrow to move paddle
            this.addKeyListener(new KeyAdapter() {
                int keyCode;

                @Override
                public void keyPressed(KeyEvent e) {
                    keyCode = e.getKeyCode();
                    if (!gameStarted && keyCode == KeyEvent.VK_UP) gameStarted = true;

                    if (keyCode == KeyEvent.VK_LEFT) {
                        paddle.change_xCoord = -1;
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        paddle.change_xCoord = 1;
                    }
                    paddle.paddleMove();
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    keyCode = e.getKeyCode();
                    if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT) {
                        paddle.change_xCoord = 0;
                    }
                    paddle.paddleMove();
                }
            });

            // Use mouse to move paddle
            this.addMouseMotionListener( new MouseAdapter() {
                public void mouseMoved(MouseEvent e) {
                    paddle.set_xCoord(e.getX() - 40);
                    if (paddle.get_xCoord() < 0) {
                        paddle.set_xCoord(0);
                    } else if (paddle.get_xCoord() > PANEL_WIDTH - PADDLE_WIDTH) {
                        paddle.set_xCoord(PANEL_WIDTH - PADDLE_WIDTH);
                    }
                }
            });

            this.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    PANEL_WIDTH = frame.getWidth();
                    PANEL_HEIGHT = frame.getHeight();
                    int dis_to_left = (int) frame.getWidth() / 10;
                    int dis_to_top = (int) frame.getHeight() / 10;
                    int width = (int) frame.getWidth() * 3 / 20;
                    int height = (int) frame.getHeight() / 20;
                    int hori_dis = (int) frame.getWidth() / 80;
                    int vert_dis = (int) frame.getHeight() / 80;
                    for (int i=0; i<5; i++) {
                        for (int j=0; j<5; j++) {
                            blocks[i][j].set_xCoord(dis_to_left + j*(width+hori_dis));
                            blocks[i][j].set_yCoord(dis_to_top + i*(height+vert_dis));
                            blocks[i][j].set_width(width);
                            blocks[i][j].set_height(height);
                        }
                    }

                    paddle.set_yCoord((int) frame.getHeight()*7/8);
                    ball.set_yCoord((int) paddle.get_yCoord()-5);
                }
            });
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;               // 2D drawing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!gameStarted) {
                String name = "Name: Yinbo Li";
                g2.setColor(Color.black);
                g2.drawString(name, 10, PANEL_HEIGHT / 2);
                String userid = "Userid: ybli";
                g2.setColor(Color.black);
                g2.drawString(userid, 10, PANEL_HEIGHT / 2 + 15);
                String guide1 = "Use left and right arrow and mouse to move the paddle.";
                g2.setColor(Color.black);
                g2.drawString(guide1, 10, PANEL_HEIGHT / 2 + 35);
                String guide2 = "Press up arrow to launch the ball.";
                g2.setColor(Color.black);
                g2.drawString(guide2, 10, PANEL_HEIGHT / 2 + 50);
            }

            g2.setColor(Color.black);
            g2.drawString("Score: " + score, 10, 15);

            for (int i=0; i<5; i++) {
                for (int j=0; j<5; j++) {
                    blocks[i][j].drawBlock(g2);
                }
            }
            ball.drawBall(g2);
            paddle.drawPaddle(g2);

            if (lost) {
                String msg = "DEFEATED";
                g2.setColor(Color.RED);
                g2.drawString(msg, getWidth()*3/8, getHeight()*7/8);
            }

            if (won) {
                String msg = "VICTORY";
                g2.setColor(Color.RED);
                g2.drawString(msg, getWidth()*3/8, getHeight()*7/8);
            }
        }

        // repaint
        public void update() {
            checkHitWall();
            checkHitPaddle();
            checkHitBlock();
            ball.ballMove();
            paddle.paddleMove();
        }

        public void myRepaint() {
            repaint();
        }

        // change ball's move direction if it hits walls
        public void checkHitWall() {
            if (ball.get_xCoord() == 0) ball.set_xDir(1);
            else if (ball.get_xCoord() == frame.getWidth()) ball.set_xDir(-1);

            if (ball.get_yCoord() == 0)  ball.set_yDir(1);
            else if (ball.get_yCoord() == frame.getHeight())  ball.set_yDir(-1);

            if (ball.get_yCoord() == frame.getHeight()) {
                model.updateTimer.stop();
                lost = true;
            }
        }

        // change ball's move direction if it hits paddle
        public void checkHitPaddle() {
            Rectangle tempBall = new Rectangle(ball.get_xCoord()-BALL_RADIUS,
                    ball.get_yCoord()-BALL_RADIUS, BALL_RADIUS, BALL_RADIUS);
            Rectangle tempPaddle = new Rectangle(paddle.get_xCoord(),
                    paddle.get_yCoord(), paddle.get_width(), paddle.get_height());

            if (tempBall.intersects(tempPaddle)) {
                ball.set_yDir(-(ball.get_yDir()));
            }
        }

        // let destroyed blocks invisible and set ball's move direction
        public void checkHitBlock() {
            for (int i=0; i<5; i++) {
                for (int j=0; j<5; j++) {
                    if (!blocks[i][j].get_destroyed()) {
                        Rectangle tempBlock = new Rectangle(blocks[i][j].get_xCoord(),
                                blocks[i][j].get_yCoord(), blocks[i][j].get_width(), blocks[i][j].get_height());
                        Rectangle tempBall = new Rectangle(ball.get_xCoord() - BALL_RADIUS,
                                ball.get_yCoord() - BALL_RADIUS, BALL_RADIUS, BALL_RADIUS);

                        Point right = new Point(ball.get_xCoord()+6, ball.get_yCoord());
                        Point left = new Point(ball.get_xCoord()-6, ball.get_yCoord());
                        Point top = new Point(ball.get_xCoord(), ball.get_yCoord()-6);
                        Point bottom = new Point(ball.get_xCoord(), ball.get_yCoord()+6);

                        if (tempBlock.intersects(tempBall) && !blocks[i][j].get_hit()) {
                            blocks[i][j].set_hit(true);
                            for (int x=0; x<5; x++) {
                                for (int y = 0; y < 5; y++) {
                                    if (x != i || y != j) blocks[x][y].set_hit(false);
                                }
                            }
                            blocks[i][j].health--;
                            if (blocks[i][j].health == 2) {
                                blocks[i][j].color = Color.green;
                            } else if (blocks[i][j].health == 1) {
                                blocks[i][j].color = Color.CYAN;
                            }
                            score++;

//                            if ((tempBlock.contains(top) || tempBlock.contains(bottom))) {
//                                ball.set_yDir(-ball.get_yDir());
//                            } else if (tempBlock.contains(left) || tempBlock.contains(right)) {
//                                ball.set_xDir(-ball.get_xDir());
//                            }
                            if (blocks[i][j].hitTopOrBottom(ball.get_xCoord(), ball.get_yCoord())) {
                                ball.set_yDir(-ball.get_yDir());
                            } else if (blocks[i][j].hitLeftOrRight(ball.get_xCoord(), ball.get_yCoord())) {
                                ball.set_xDir(-ball.get_xDir());
                            }

                            if (blocks[i][j].health == 0) {
                                num_avlBlocks--;
                                blocks[i][j].set_destroyed(true);
                            }
                        }
                    }
                }
            }
            if (num_avlBlocks == 0) {
                won = true;
                model.updateTimer.stop();
            }
        }
    }

    // entry point for the application
    public static void main(String[] args) {
        fps = Integer.parseInt(args[0]);
        speed = Integer.parseInt(args[1]);
        Breakout game = new Breakout();
    }

}
