import processing.core.PImage;

import java.util.List;
import java.util.Optional;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public final class House extends Entity
{
    //Entity: -animation, -activity, -resources, -health, -movement

    public House(
            String id,
            Point position,
            List<PImage> images)
    {
        super(position, id, images, 0);
    }

    @Override
    protected String _storeLine(String line) {
        return line;
    }
}
