import processing.core.PImage;

import java.util.List;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class Obstacle extends Entity
{
    //Entity: +Animation, -activity, -resources, -health, -movement
    private int animationPeriod;

    public Obstacle(
            String id,
            Point position,
            List<PImage> images,
            int animationPeriod
            )
    {
        super(position, id, images, 0);
        this.animationPeriod = animationPeriod;
    }

    @Override
    protected String _storeLine(String line) {
        return line + " " + this.animationPeriod;
    }
}
