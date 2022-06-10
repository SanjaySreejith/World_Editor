import processing.core.PApplet;
import processing.core.PImage;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;


/*
    World Editor

    This program makes map editing easier by displaying the map and allowing you to 'paint' on it. Run the program
    for further instructions on controls. They will be printed in the console.

    This program can edit the map using all background tiles initially included with the codebase, and the following static entities:
    - Tree
    - Obstacle
    - House

    This means that any entities not listed above in world.sav will NOT be copied when the program writes to its output file.
    Save your entities elsewhere and paste them into the resulting world file to avoid this.

    To add functionality:
        - Everything you may need to change in this program to add functionality or support for more tiles and entities are contained in the following:
            - mousePressed()
            - keyPressed()
            - setup()
            - setupBgArray()
            - writeOutFile()
            - EditMode
            To add entities, one must also:
            - Add any requisite classes and methods
                - NOTE: The only data and methods required to be added to this program are the ones that will be stored. Other methods will not be run.
        - Comments throughout the rest of this program should help in reading it
            - I apologize for the unreadability of setupBgArray()
 */






public final class VirtualWorld extends PApplet
{

    // Background tile constants. Add new background tiles here.
    private static final int GRASS_CONSTANT = 0;
    private static final int FLOWERS_CONSTANT = 1;
    private static final int DIRT_CONSTANT = 2;
    private static final int DIRT_LEFT_CONSTANT = 3;
    private static final int DIRT_RIGHT_CONSTANT = 4;
    private static final int DIRT_HORIZ_CONSTANT = 5;
    private static final int DIRT_LEFT_CORNER_CONSTANT = 6;
    private static final int DIRT_RIGHT_CORNER_CONSTANT = 7;
    private static final int DIRT_CORNER_CONSTANT = 8;
    private static final int BRIDGE_CONSTANT = 9;

    // Texture Keys. New entity and background textures must be listed here and correspond with the constants above.
    List<String> textureKeys = new ArrayList<>(
            Arrays.asList(
                    "grass",
                    "flowers",
                    "dirt",
                    "dirt_vert_left",
                    "dirt_vert_right",
                    "dirt_horiz",
                    "dirt_vert_left_bot",
                    "dirt_bot_right_up",
                    "dirt_bot_left_corner",
                    "bridge"
            )
    );


    // The following 8 variables specify the lower and upper bounds for various constructor parameters that will be
    // randomly generated for newly placed entities.
    private static final int TREE_ANIMATION_PERIOD_LBOUND = 0;
    private static final int TREE_ANIMATION_PERIOD_UBOUND = 1100;

    private static final int TREE_ACTION_PERIOD_LBOUND = 1000;
    private static final int TREE_ACTION_PERIOD_UBOUND = 1100;

    private static final int TREE_HEALTH_LBOUND = 1;
    private static final int TREE_HEALTH_UBOUND = 3;

    private static final int OBSTACLE_ANIMATION_PERIOD_LBOUND = 1000;
    private static final int OBSTACLE_ANIMATION_PERIOD_UBOUND = 1200;



    private static final int TIMER_ACTION_PERIOD = 100;

    private static final int VIEW_WIDTH = 640;
    private static final int VIEW_HEIGHT = 480;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 32;
    private static final int WORLD_WIDTH_SCALE = 2;
    private static final int WORLD_HEIGHT_SCALE = 2;

    private static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    private static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;
    private static final int WORLD_COLS = VIEW_COLS * WORLD_WIDTH_SCALE;
    private static final int WORLD_ROWS = VIEW_ROWS * WORLD_HEIGHT_SCALE;

    private static final String IMAGE_LIST_FILE_NAME = "imagelist";

    private static final int DEFAULT_IMAGE_COLOR = 0x808080;

    private static final String FAST_FLAG = "-fast";
    private static final String FASTER_FLAG = "-faster";
    private static final String FASTEST_FLAG = "-fastest";
    private static final double FAST_SCALE = 0.5;
    private static final double FASTER_SCALE = 0.25;
    private static final double FASTEST_SCALE = 0.10;

    private static double timeScale = 1.0;


    // This is the name of the file the program will display. Change it to test new maps.
    private static String LOAD_FILE_NAME = "world.sav";


    // outFile is the output file. inFile is the input.
    // WARNING: BEFORE TESTING A NEW MAP, MAKE SURE "LOAD_FILE_NAME" AND "outFile" ARE DIFFERENT. YOUR DISPLAY FILE WILL
    // BE WIPED IF THEY ARE THE SAME
    private static final String outFile = "output.txt";
    private static String inFile = "world.sav";


    // Check the EditMode enum for more information
    private static EditMode editMode = EditMode.DEFAULT;

    private static final File saveFile = new File(outFile);
    private static FileWriter fileWriter;
    private static BufferedReader bufferedReader;
    private static List<String> worldSavLines = new ArrayList<>();

    private static int[][] bgArray = new int[WORLD_ROWS][WORLD_COLS];

    private ImageStore imageStore;
    private WorldModel world;
    private WorldView view;
    private Parser parser;

    private long nextTime;

    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        this.imageStore = new ImageStore(
                createImageColored(TILE_WIDTH, TILE_HEIGHT,
                        DEFAULT_IMAGE_COLOR));
        this.world = new WorldModel(WORLD_ROWS, WORLD_COLS, imageStore.createDefaultBackground());
        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH,
                TILE_HEIGHT);
        this.parser = new Parser();


        try {
            fileWriter = new FileWriter(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bufferedReader = new BufferedReader(new FileReader(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Stream<String> lines = bufferedReader.lines();
        worldSavLines = lines.toList();

        setupBgArray(worldSavLines);

        printIntro();

        loadImages(IMAGE_LIST_FILE_NAME, imageStore, this);
        loadWorld(world, LOAD_FILE_NAME, parser, imageStore);

        nextTime = System.currentTimeMillis() + TIMER_ACTION_PERIOD;
    }

    public void printIntro() {
        System.out.println("\nWelcome to World Editor\n");
        System.out.println("The following modes are available:");
        System.out.println("-- Default (Q) NOTE: Does nothing");
        System.out.println("-- Add House (H)");
        System.out.println("-- Add Obstacle (O)");
        System.out.println("-- Add Flowers (F)");
        System.out.println("-- Add Grass (G)");
        System.out.println("-- Add Tree (T)");
        System.out.println("-- Add Dirt (D) NOTE: The dirt textures cycle for each click. Paths can be added in this way");
        System.out.println("-- Remove Entity (R)\n");
        System.out.print("NOTE: Adding a background tile at a spot will replace what was already there.\n" +
                "      Removing an entity will remove the entity without affecting the background tile.\n" +
                "      Hit S to save edited map to output file.\n");
    }

    public void draw() {
        view.drawViewport();
    }

    private void setupBgArray(List<String> lines) {
        // bgArray is not strictly necessary, but it helps keep controls concise by making dirt texture cycling easier
        // Deleting this WILL cause problems
        for (String line : lines) {
            String[] attrs = line.split("\\s");
            if (Objects.equals(attrs[0], "background")) {
                if (Objects.equals(attrs[1], textureKeys.get(GRASS_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = GRASS_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(FLOWERS_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = FLOWERS_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_LEFT_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_LEFT_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_RIGHT_CORNER_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_RIGHT_CORNER_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_RIGHT_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_RIGHT_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_LEFT_CORNER_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_LEFT_CORNER_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_HORIZ_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_HORIZ_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_CORNER_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_CORNER_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(DIRT_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = DIRT_CONSTANT;
                } else if (Objects.equals(attrs[1], textureKeys.get(BRIDGE_CONSTANT))) {
                    bgArray[Integer.parseInt(attrs[3])][Integer.parseInt(attrs[2])] = BRIDGE_CONSTANT;
                }
            }
        }
    }

    private void writeOutFile(FileWriter fileWriter) throws IOException {

        Background[][] bg = world.getBackground();

        for (int i = 0; i < WORLD_ROWS; i++) {
            for (int j = 0; j < WORLD_COLS; j++) {
                String bgLine;
                String entityLine;

                bgLine = bg[i][j].getStoreLine() + " " + j + " " + i;
                Optional<Entity> e = world.getOccupant(new Point(j, i));

                fileWriter.write(bgLine);
                fileWriter.write("\n");

                if (e.isPresent()) {
                    entityLine = e.get().createStoreLine();
                    fileWriter.write(entityLine);
                    fileWriter.write("\n");
                }
            }
        }
    }

    public void mousePressed() {

        Point pressed = mouseToPoint(mouseX, mouseY);
        System.out.println("CLICK! " + pressed.getX() + ", " + pressed.getY());

        switch (editMode) {
            case GRASS:
                bgArray[pressed.y][pressed.x] = GRASS_CONSTANT;
                world.setBackground(pressed, new Background("grass", imageStore.getImageList("grass")));
                break;
            case FLOWERS:
                bgArray[pressed.y][pressed.x] = FLOWERS_CONSTANT;
                world.setBackground(pressed, new Background("flowers", imageStore.getImageList("flowers")));
                break;
            case DIRT:
                System.out.println(bgArray[pressed.y][pressed.x]);
                if (bgArray[pressed.y][pressed.x] < 2 || bgArray[pressed.y][pressed.x] > 8) {
                    world.setBackground(pressed, new Background("dirt", imageStore.getImageList("dirt")));
                    bgArray[pressed.y][pressed.x] = DIRT_CONSTANT;
                } else {
                    bgArray[pressed.y][pressed.x] = (((bgArray[pressed.y][pressed.x] - 2) + 1) % 8) + 2;
                    world.setBackground(pressed, new Background(textureKeys.get(bgArray[pressed.y][pressed.x]),
                            imageStore.getImageList(textureKeys.get(bgArray[pressed.y][pressed.x]))));
                }
                break;
            case BRIDGE:
                bgArray[pressed.y][pressed.x] = BRIDGE_CONSTANT;
                world.setBackground(pressed, new Background("bridge", imageStore.getImageList("bridge")));
                break;
            case HOUSE:
                if (!world.isOccupied(pressed)) {
                    String house_id = "house_" + pressed.x + "_" + pressed.y;
                    House house = (House) Factory.createHouse(house_id, pressed, imageStore.getImageList("house"));
                    world.tryAddEntity(house);
                }
                break;
            case OBSTACLE:
                if (!world.isOccupied(pressed)) {
                    String obstacle_id = "obstacle_" + pressed.x + "_" + pressed.y;
                    int animPeriod = Util.rand.nextInt(OBSTACLE_ANIMATION_PERIOD_LBOUND, OBSTACLE_ANIMATION_PERIOD_UBOUND);
                    Obstacle obstacle = (Obstacle) Factory.createObstacle(obstacle_id, pressed, imageStore.getImageList("obstacle"), animPeriod);
                    world.tryAddEntity(obstacle);
                }
                break;
            case TREE:
                if (!world.isOccupied(pressed)) {
                    String tree_id = "tree_" + pressed.x + "_" + pressed.y;
                    int animPeriod = Util.rand.nextInt(TREE_ANIMATION_PERIOD_LBOUND, TREE_ANIMATION_PERIOD_UBOUND);
                    int actPeriod = Util.rand.nextInt(TREE_ACTION_PERIOD_LBOUND, TREE_ACTION_PERIOD_UBOUND);
                    int health = Util.rand.nextInt(TREE_HEALTH_LBOUND, TREE_HEALTH_UBOUND);
                    Tree tree = (Tree) Factory.createTree(tree_id, pressed, imageStore.getImageList("tree"), animPeriod, actPeriod, health);
                    world.tryAddEntity(tree);
                }
                break;
            case REMOVE:
                if (world.isOccupied(pressed)) {
                    world.removeEntity(world.getOccupancyCell(pressed));
                }
                break;
            case DEFAULT:
                break;
        }

    }

    private Point mouseToPoint(int x, int y)
    {
        return view.getViewport().viewportToWorld(mouseX/TILE_WIDTH, mouseY/TILE_HEIGHT);
    }

    public void keyPressed() {
        int dx = 0;
        int dy = 0;

        if (key == 'h' || key == 'H') {
            System.out.println("Current edit mode: House");
            editMode = EditMode.HOUSE;
        } else if (key == 'o' || key == 'O') {
            editMode = EditMode.OBSTACLE;
            System.out.println("Current edit mode: Obstacle");
        } else if (key == 'g' || key == 'G') {
            editMode = EditMode.GRASS;
            System.out.println("Current edit mode: Grass");
        } else if (key == 't' || key == 'T') {
            editMode = EditMode.TREE;
            System.out.println("Current edit mode: Tree");
        } else if (key == 'f' || key == 'F') {
            editMode = EditMode.FLOWERS;
            System.out.println("Current edit mode: Flowers");
        } else if (key == 'b' || key == 'B') {
            editMode = EditMode.BRIDGE;
            System.out.println("Current edit mode: Bridge");
        } else if (key == 'd' || key == 'D') {
            editMode = EditMode.DIRT;
            System.out.println("Current edit mode: Dirt");
        } else if (key == 'r' || key == 'R') {
            editMode = EditMode.REMOVE;
            System.out.println("Current edit mode: Remove Entity");
        } else if (key == 's' || key == 'S') {

            System.out.println("\nSaving output. Do not close program...");
            try {
                writeOutFile(fileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Save complete. Program may be closed.\n");
        }


        if (key == CODED) {
            switch (keyCode) {
                case UP:
                    dy = -1;
                    break;
                case DOWN:
                    dy = 1;
                    break;
                case LEFT:
                    dx = -1;
                    break;
                case RIGHT:
                    dx = 1;
                    break;
            }
        }

        view.shiftView(dx, dy);
    }


    public static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            img.pixels[i] = color;
        }
        img.updatePixels();
        return img;
    }

    public void loadImages(
            String filename, ImageStore imageStore, PApplet screen)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            parser.loadImages(in, screen, imageStore);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void loadWorld(
            WorldModel world, String filename, Parser parser, ImageStore imageStore)
    {
        try {
            Scanner in = new Scanner(new File(filename));
            parser.load(in, world, imageStore);
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }


    public static void parseCommandLine(String[] args) {
        if (args.length > 1)
        {
            if (args[0].equals("file"))
            {

            }
        }
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG:
                    timeScale = Math.min(FAST_SCALE, timeScale);
                    break;
                case FASTER_FLAG:
                    timeScale = Math.min(FASTER_SCALE, timeScale);
                    break;
                case FASTEST_FLAG:
                    timeScale = Math.min(FASTEST_SCALE, timeScale);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        parseCommandLine(args);
        PApplet.main(VirtualWorld.class);
    }
}
