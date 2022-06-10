import processing.core.PImage;

import java.util.List;

/**
 * This class contains many functions written in a procedural style.
 * You will reduce the size of this class over the next several weeks
 * by refactoring this codebase to follow an OOP style.
 */
public final class Factory {

    public static Entity createHouse(
            String id, Point position, List<PImage> images) {
        return new House(id, position, images);
    }

    public static Entity createObstacle(
            String id, Point position, List<PImage> images, int animationPeriod) {
        // Creates a river obstacle
        return new Obstacle(id, position, images, animationPeriod);
    }

    public static Entity createTree(
            String id,
            Point position,
            List<PImage> images,
            int animationPeriod,
            int actionPeriod,
            int health) {
        return new Tree(id, position, images, animationPeriod, actionPeriod, health);
    }
}

