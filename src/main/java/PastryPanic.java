import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Main class for the Pastry Panic game.
 */
public class PastryPanic extends JPanel implements ActionListener, KeyListener {

    // --- Game constants ---
    private static final int GAME_WIDTH = 900;
    private static final int GAME_HEIGHT = 600;
    private static final int FPS = 360;
    private static final double ENERGY_START = 100.0;
    private static final double ENERGY_DECAY_PER_SEC = 5.0;
    private static final double PLAYER_SPEED = 420.0;
    private static final double BASE_FALL_SPEED = 180.0;
    private static final double BASE_SPAWN_PER_SEC = 1.5;
    private static final double LOW_ENERGY_THRESHOLD = 25.0;
    private static final int PLAYER_W = 120;
    private static final int PLAYER_H = 130;
    private static final int GROUND_Y = GAME_HEIGHT - 80;

    private enum GameState {
        MENU,
        CHARACTER_SELECTION,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private enum CharacterType {
        MERT_AHMETOGLU("Mert Ahmetoglu"),
        BERA_UZUN("Bera Uzun");

        final String name;
        CharacterType(String name) {
            this.name = name;
        }
    }

    // --- Game State Variables ---
    private double energy;
    private double playerX;
    private boolean leftHeld;
    private boolean rightHeld;
    private double timeSurvived;
    private double highScore;
    private double spawnAccumulator;
    private GameState gameState;

    // --- Difficulty & Notification ---
    private String notificationText;
    private double notificationTimer;
    private float notificationAlpha;
    private int currentDifficultyLevel;
    private int nextSpeedUpIndex;
    private static final double[] SPEED_UP_TIMES = {4.0, 8.0, 12.0, 15.0, 17.0, 18.5, 19.5, 20.0};
    private static final String[] SPEED_UP_MESSAGES = {
        "Getting Faster!", 
        "Heating Up!", 
        "Don't Blink!", 
        "MAXIMUM PANIC!", 
        "LUDICROUS SPEED!"
    };
    private static final int PERSISTENT_NOTIFICATION_LEVEL = 3;
    private static final double NOTIFICATION_DURATION = 2.5;
    private static final double NOTIFICATION_FADE_TIME = 0.5;

    // --- Character Selection ---
    private CharacterType selectedCharacter = CharacterType.MERT_AHMETOGLU;
    private CharacterType currentSelection = CharacterType.MERT_AHMETOGLU;

    // --- Core Components ---
    private final List<FallingItem> items = new ArrayList<>();
    private final Random rng = new Random();
    private final Timer timer;
    private long lastUpdateTime;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    // --- Assets ---
    private BufferedImage mertImg;
    private BufferedImage beraImg;
    private BufferedImage backgroundImg;
    private Clip backgroundMusic;
    private SoundManager.Sound eatSound;
    private SoundManager.Sound rottenObjectSound;
    private SoundManager.Sound missSound;
    private SoundManager.Sound gameOverSound;

    /**
     * Constructor for PastryPanic.
     */
    public PastryPanic() {
        this.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        this.setBackground(new Color(245, 240, 235));
        this.setFocusable(true);
        this.loadHighScore();
        this.loadAssets();
        this.addKeyListener(this);
        this.timer = new Timer(1000 / FPS, this);
        this.showMenu();
        this.timer.start();
    }

    private void loadAssets() {
        this.mertImg = Assets.load("/assets/characters/player.png");
        this.beraImg = Assets.load("/assets/characters/berauzun.png");
        this.backgroundImg = Assets.load("/assets/bg/background.png");
        this.backgroundMusic = SoundManager.loadClip("/assets/sfx/bgsound.wav");
        this.eatSound = SoundManager.loadSound("/assets/sfx/eatingsound.wav");
        this.rottenObjectSound = SoundManager.loadSound(
            "/assets/sfx/rottenobjectsound.wav");
        this.missSound = SoundManager.loadSound("/assets/sfx/miss.wav");
        this.gameOverSound = SoundManager.loadSound("/assets/sfx/game_over.wav");
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                this.highScore = Double.parseDouble(line);
            }
        } catch (IOException | NumberFormatException e) {
            this.highScore = 0.0;
            System.err.println("Could not load high score, starting with 0. " + e.getMessage());
        }
    }

    private void saveHighScore() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORE_FILE, false))) {
            writer.println(this.highScore);
        } catch (IOException e) {
            System.err.println("Could not save high score. " + e.getMessage());
        }
    }

    private void showMenu() {
        this.gameState = GameState.MENU;
        SoundManager.stop(this.backgroundMusic);
    }

    private void showCharacterSelection() {
        this.gameState = GameState.CHARACTER_SELECTION;
        this.currentSelection = CharacterType.MERT_AHMETOGLU;
    }

    private void restartGame() {
        this.energy = ENERGY_START;
        this.playerX = (GAME_WIDTH - PLAYER_W) / 2.0;
        this.timeSurvived = 0;
        this.leftHeld = false;
        this.rightHeld = false;
        this.items.clear();
        this.spawnAccumulator = 0.0;
        this.notificationText = null;
        this.notificationTimer = 0.0;
        this.notificationAlpha = 0.0f;
        this.currentDifficultyLevel = 0;
        this.nextSpeedUpIndex = 0;
        this.gameState = GameState.PLAYING;
        this.lastUpdateTime = System.nanoTime();

        if (this.backgroundMusic != null && !this.backgroundMusic.isRunning()) {
            SoundManager.loop(this.backgroundMusic);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.gameState == GameState.PLAYING) {
            this.updateGame();
        }
        this.repaint();
    }

    private void updateGame() {
        long currentTime = System.nanoTime();
        double dt = (currentTime - this.lastUpdateTime) / 1_000_000_000.0;
        this.lastUpdateTime = currentTime;

        this.timeSurvived += dt;
        this.energy -= ENERGY_DECAY_PER_SEC * dt;
        this.checkDifficultyIncrease();
        this.updateNotification(dt);

        double currentSpeed = PLAYER_SPEED;
        if (this.energy < LOW_ENERGY_THRESHOLD) {
            currentSpeed *= 0.6;
        }
        if (this.leftHeld) {
            this.playerX -= currentSpeed * dt;
        }
        if (this.rightHeld) {
            this.playerX += currentSpeed * dt;
        }
        this.playerX = Math.max(0, Math.min(this.playerX, GAME_WIDTH - PLAYER_W));

        // Increase spawn rate as difficulty increases
        double spawnMultiplier = 1.0 + (this.currentDifficultyLevel * 0.3); 
        double currentSpawnRate = BASE_SPAWN_PER_SEC * spawnMultiplier;
        this.spawnAccumulator += currentSpawnRate * dt;
        while (this.spawnAccumulator >= 1.0) {
            this.spawnAccumulator -= 1.0;
            this.spawnNewItem();
        }

        this.updateItems(dt);

        if (this.energy <= 0) {
            this.energy = 0;
            this.gameState = GameState.GAME_OVER;
            if (this.timeSurvived > this.highScore) {
                this.highScore = this.timeSurvived;
                this.saveHighScore();
            }
            SoundManager.stop(this.backgroundMusic);
            SoundManager.play(this.gameOverSound);
        }
    }

    private void checkDifficultyIncrease() {
        if (this.nextSpeedUpIndex < SPEED_UP_TIMES.length 
                && this.timeSurvived >= SPEED_UP_TIMES[this.nextSpeedUpIndex]) {
            int messageIndex = Math.min(this.currentDifficultyLevel, SPEED_UP_MESSAGES.length - 1);
            String message = SPEED_UP_MESSAGES[messageIndex];
            this.triggerNotification(message);
            this.currentDifficultyLevel++;
            this.nextSpeedUpIndex++;
        }
    }

    private void updateNotification(double dt) {
        if (this.notificationTimer > 0) {
            this.notificationTimer -= dt;
            if (this.currentDifficultyLevel > PERSISTENT_NOTIFICATION_LEVEL) {
                this.notificationAlpha = 1.0f;
            } else {
                if (this.notificationTimer > NOTIFICATION_DURATION - NOTIFICATION_FADE_TIME) {
                    double timeIntoFade = NOTIFICATION_DURATION - this.notificationTimer;
                    this.notificationAlpha =
                         (float) Math.min(1.0, timeIntoFade / NOTIFICATION_FADE_TIME);
                } else if (this.notificationTimer < NOTIFICATION_FADE_TIME) {
                    this.notificationAlpha =
                         (float) Math.max(0.0, this.notificationTimer / NOTIFICATION_FADE_TIME);
                } else {
                    this.notificationAlpha = 1.0f;
                }
                if (this.notificationTimer <= 0) {
                    this.notificationText = null;
                }
            }
        }
    }

    private void triggerNotification(String text) {
        this.notificationText = text;
        this.notificationTimer = NOTIFICATION_DURATION;
    }

    private void spawnNewItem() {
        ItemType type;
        double baseRottenChance = 0.20;
        double rottenChanceIncreasePerLevel = 0.05;
        double currentRottenChance = Math.min(0.55, 
            baseRottenChance + (this.currentDifficultyLevel * rottenChanceIncreasePerLevel));

        if (this.rng.nextDouble() < currentRottenChance) {
            type = ItemType.ROTTEN_OBJECT;
        } else {
            do {
                type = ItemType.random(this.rng);
            } while (type == ItemType.ROTTEN_OBJECT);
        }
        double x = this.rng.nextDouble() * (GAME_WIDTH - 40) + 20;
        this.items.add(new FallingItem(type, x, -20));
    }

    private void updateItems(double dt) {
        double speedMultiplier = 1.0 + (this.currentDifficultyLevel * 0.25);
        double currentFallSpeed = BASE_FALL_SPEED * speedMultiplier;
        for (int i = this.items.size() - 1; i >= 0; i--) {
            FallingItem item = this.items.get(i);
            item.y += currentFallSpeed * dt;

            if (item.y - item.radius > GAME_HEIGHT) {
                this.items.remove(i);
                SoundManager.play(this.missSound);
                continue;
            }

            if (this.collidesWithPlayer(item)) {
                int energyChange = item.type.energyGain;
                this.energy += energyChange;
                this.energy = Math.min(this.energy, 100.0);
                if (item.type == ItemType.ROTTEN_OBJECT) {
                    SoundManager.play(this.rottenObjectSound);
                } else {
                    SoundManager.play(this.eatSound);
                }
                this.items.remove(i);
            }
        }
    }

    private boolean collidesWithPlayer(FallingItem item) {
        Rectangle playerBounds = new Rectangle((int) this.playerX, 
            GROUND_Y - PLAYER_H, PLAYER_W, PLAYER_H);
        return playerBounds.intersects(item.getBounds());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.backgroundImg != null) {
            g2.drawImage(this.backgroundImg, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        }

        if (this.gameState == GameState.PLAYING) {
            BufferedImage currentPlayerImage = getCurrentPlayerImage();
            if (currentPlayerImage != null) {
                g2.drawImage(currentPlayerImage, (int) this.playerX, 
                    GROUND_Y - PLAYER_H, PLAYER_W, PLAYER_H, null);
            }
            for (FallingItem item : this.items) {
                item.draw(g2);
            }
        }

        this.drawHUD(g2);

        if (this.gameState == GameState.GAME_OVER) {
            this.drawGameOverScreen(g2);
        } else if (this.gameState == GameState.CHARACTER_SELECTION) {
            this.drawCharacterSelectionScreen(g2);
        } else if (this.gameState == GameState.PAUSED) {
            this.drawPauseScreen(g2);
        } else if (this.gameState == GameState.MENU) {
            this.drawMenuScreen(g2);
        } else if (this.gameState == GameState.PLAYING) {
            this.drawNotification(g2);
        }
    }

    private BufferedImage getCurrentPlayerImage() {
        return this.selectedCharacter == CharacterType.MERT_AHMETOGLU ? this.mertImg : this.beraImg;
    }

    private void drawHUD(Graphics2D g2) {
        if (this.gameState != GameState.PLAYING && this.gameState != GameState.PAUSED 
                && this.gameState != GameState.GAME_OVER) {
            return;
        }
        int barX = 20;
        int barY = 20;
        int barW = 200;
        int barH = 25;
        Color energyColor = (this.energy < LOW_ENERGY_THRESHOLD) 
            ? new Color(210, 60, 60) : new Color(60, 180, 90);
        int fillW = (int) (barW * (this.energy / 100.0));

        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(barX, barY, barW, barH, 10, 10);
        g2.setColor(energyColor);
        g2.fillRoundRect(barX, barY, fillW, barH, 10, 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(barX, barY, barW, barH, 10, 10);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("Energy: %d", (int) this.energy), barX + 5, barY + 18);
        g2.drawString(String.format(Locale.US, "Time: %.1fs", this.timeSurvived), 20, 70);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        String msg = "You ran out of energy!";
        g2.drawString(msg, (GAME_WIDTH - fm.stringWidth(msg)) / 2, GAME_HEIGHT / 2 - 80);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g2.getFontMetrics();
        String summary = String.format(Locale.US, 
            "You survived for %.1f seconds.", this.timeSurvived);
        g2.drawString(summary, (GAME_WIDTH - fm.stringWidth(summary)) / 2, GAME_HEIGHT / 2 - 10);
        String highScoreMsg = String.format(Locale.US, "High Score: %.1f", this.highScore);
        g2.drawString(highScoreMsg, 
            (GAME_WIDTH - fm.stringWidth(highScoreMsg)) / 2, GAME_HEIGHT / 2 + 20);
        if (this.timeSurvived >= this.highScore && this.timeSurvived > 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString("New High Score!", 
                (GAME_WIDTH - fm.stringWidth("New High Score!")) / 2, GAME_HEIGHT / 2 + 50);
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.ITALIC, 20));
        fm = g2.getFontMetrics();
        String restartMsg = "Press 'R' to return to Menu";
        g2.drawString(restartMsg, (GAME_WIDTH - fm.stringWidth(restartMsg))
             / 2, GAME_HEIGHT / 2 + 90);
    }

    private void drawMenuScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Pastry Panic";
        g2.drawString(title, (GAME_WIDTH - fm.stringWidth(title)) / 2, GAME_HEIGHT / 2 - 50);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g2.getFontMetrics();
        String startMsg = "Press Enter to Start";
        g2.drawString(startMsg, (GAME_WIDTH - fm.stringWidth(startMsg)) / 2, GAME_HEIGHT / 2 + 30);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        fm = g2.getFontMetrics();
        String highScoreMsg = String.format(Locale.US, "High Score: %.1f", this.highScore);
        g2.drawString(highScoreMsg, 
            (GAME_WIDTH - fm.stringWidth(highScoreMsg)) / 2, GAME_HEIGHT / 2 + 70);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Paused";
        g2.drawString(title, (GAME_WIDTH - fm.stringWidth(title)) / 2, GAME_HEIGHT / 2 - 50);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        fm = g2.getFontMetrics();
        String resumeMsg = "Press Escape to Resume";
        g2.drawString(resumeMsg, (GAME_WIDTH - fm.stringWidth(resumeMsg))
             / 2, GAME_HEIGHT / 2 + 20);
    }

    private void drawCharacterSelectionScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Choose Your Character";
        g2.drawString(title, (GAME_WIDTH - fm.stringWidth(title)) / 2, 100);
        int mertX = GAME_WIDTH / 4;
        int beraX = GAME_WIDTH * 3 / 4;
        int charY = GAME_HEIGHT / 2 - PLAYER_H / 2;
        if (this.mertImg != null) {
            g2.drawImage(this.mertImg, mertX - PLAYER_W / 2, charY, PLAYER_W, PLAYER_H, null);
        }
        if (this.beraImg != null) {
            g2.drawImage(this.beraImg, beraX - PLAYER_W / 2, charY, PLAYER_W, PLAYER_H, null);
        }
        g2.setFont(new Font("Impact", Font.PLAIN, 32));
        fm = g2.getFontMetrics();
        String mertName = CharacterType.MERT_AHMETOGLU.name;
        g2.drawString(mertName, mertX - fm.stringWidth(mertName) / 2, charY + PLAYER_H + 40);
        String beraName = CharacterType.BERA_UZUN.name;
        g2.drawString(beraName, beraX - fm.stringWidth(beraName) / 2, charY + PLAYER_H + 40);

        int selectionX = (this.currentSelection == CharacterType.MERT_AHMETOGLU) ? mertX : beraX;
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(selectionX - PLAYER_W / 2 - 10, charY - 10, PLAYER_W + 20, PLAYER_H + 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        fm = g2.getFontMetrics();
        String instructions = "Use Arrow Keys to Select, Enter to Confirm";
        g2.drawString(instructions, (GAME_WIDTH - fm.stringWidth(instructions))
             / 2, GAME_HEIGHT - 100);
    }

    private void drawNotification(Graphics2D g2) {
        if (this.notificationText == null || this.notificationAlpha <= 0) {
            return;
        }
        AffineTransform oldTransform = g2.getTransform();
        g2.setFont(new Font("Impact", Font.PLAIN, 52));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(this.notificationText);
        int textX = (GAME_WIDTH - textWidth) / 2;
        int textY = GAME_HEIGHT / 2 - 100;
        if (this.currentDifficultyLevel > PERSISTENT_NOTIFICATION_LEVEL) {
            double scale = 1.0 + 0.05 * Math.sin(this.timeSurvived * 10);
            g2.translate(textX + textWidth / 2, textY + fm.getAscent() / 2.0);
            g2.scale(scale, scale);
            g2.translate(-(textX + textWidth / 2), -(textY + fm.getAscent() / 2.0));
        }
        g2.setColor(new Color(1.0f, 1.0f, 0.2f, this.notificationAlpha));
        g2.drawString(this.notificationText, textX, textY);
        g2.setTransform(oldTransform);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (this.gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                this.leftHeld = true;
            }
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                this.rightHeld = true;
            }
        }
        if (key == KeyEvent.VK_R && this.gameState == GameState.GAME_OVER) {
            this.showMenu();
        }
        if (key == KeyEvent.VK_ENTER) {
            if (this.gameState == GameState.MENU) {
                this.showCharacterSelection();
            } else if (this.gameState == GameState.CHARACTER_SELECTION) {
                this.selectedCharacter = this.currentSelection;
                this.restartGame();
            }
        }
        if (this.gameState == GameState.CHARACTER_SELECTION 
                && (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT)) {
            this.currentSelection = (this.currentSelection == CharacterType.MERT_AHMETOGLU)
                 ? CharacterType.BERA_UZUN : CharacterType.MERT_AHMETOGLU;
        }
        if (key == KeyEvent.VK_ESCAPE) {
            if (this.gameState == GameState.PLAYING) {
                this.gameState = GameState.PAUSED;
            } else if (this.gameState == GameState.PAUSED) {
                this.gameState = GameState.PLAYING;
                this.lastUpdateTime = System.nanoTime();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            this.leftHeld = false;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            this.rightHeld = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pastry Panic");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new PastryPanic());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
