package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by tudor on 31/07/2017.
 */
public class ControlPoint extends Base {
    public ControlPoint(LD39 game, float x, float y) {
        super(game, x, y, -1);
    }

    @Override
    public void render(SpriteBatch batch) {
        if(!texInited) initGraphics("hole");

        sprite.draw(batch);
    }

    @Override
    public void renderOverlay(SpriteBatch batch) {
        overlay.draw(batch);

        renderHitBoxes(batch);
    }
}
