import processing.core.PImage;

import java.util.List;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class Tree extends Entity
{
    public static final String TREE_KEY = "tree";
    private final int animationPeriod;
    private final int actionPeriod;
    private final int health;

    //Entity: +Animation, +activity, -movement, +health, -resources

    public Tree(
            String id,
            Point position,
            List<PImage> images,
            int animationPeriod,
            int actionPeriod,
            int health)
    {
        super(position, id, images, 0);
        this.animationPeriod = animationPeriod;
        this.actionPeriod = actionPeriod;
        this.health = health;
    }

    @Override
    protected String _storeLine(String line) {
        return line + " " + animationPeriod + " " + actionPeriod + " " + health;
    }
}
