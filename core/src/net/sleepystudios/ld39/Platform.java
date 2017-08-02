package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by tudor on 30/07/2017.
 */
public class Platform {
    LD39 game;
    float x, y;
    Sprite sprite;
    Rectangle box;
    float tempScale = 4f;

    boolean texInited;

    public Platform(LD39 game, float x, float y) {
        this.game = game;
        this.x = x;
        this.y = y;
    }

    public Platform(LD39 game, float x, float y, float scale) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.tempScale = scale;
    }

    public void initGraphics() {
        sprite = new Sprite(new Texture("plank.png"));
        sprite.setPosition(x, y);
        sprite.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        sprite.setScale(tempScale, 1f);
        box = new Rectangle(sprite.getBoundingRectangle());

        box.setHeight(box.getHeight()-50);
        box.setY(box.getY()+50);

        texInited = true;
    }

    public void render(SpriteBatch batch) {
        if(!texInited) initGraphics();

        sprite.draw(batch);

        if(game.showHitBoxes) {
            game.renderHitBox(boxToPoly().getTransformedVertices(), Color.BLACK);
        }
    }

    public Polygon boxToPoly() {
        return game.poly(box, sprite.getWidth(), sprite.getHeight(), 0, 0, 0);
    }
}
