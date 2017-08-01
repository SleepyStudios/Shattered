package net.sleepystudios.ld39;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by tudor on 31/07/2017.
 */
public class Base {
    LD39 game;
    float x, y;
    int energy, team;

    Sprite sprite, overlay, pointer;
    Rectangle[] box = new Rectangle[5];

    public Base(LD39 game, float x, float y, int team) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.team = team;
    }

    public boolean texInited;
    public void initGraphics(String name) {
        sprite = new Sprite(new Texture(name + ".png"));
        overlay = new Sprite(new Texture(name + "overlay.png"));
        pointer = new Sprite(new Texture("pointer.png"));

        sprite.setPosition(x, y);
        overlay.setPosition(x, y);
        pointer.setPosition(x, Gdx.graphics.getHeight());

        initBoxes();

        texInited = true;
    }

    public void initBoxes() {
        box[0] = new Rectangle(x+25, y, 50, 40);

        box[1] = new Rectangle(x, y, 20, 100);
        box[2] = new Rectangle(x+sprite.getWidth()-box[1].getWidth(), box[1].getY(), box[1].getWidth(), 100);

        box[3] = new Rectangle(x, y+33, 30, 20);
        box[4] = new Rectangle(x+sprite.getWidth()-box[3].getWidth(), box[3].getY(), box[3].getWidth(), 20);
    }

    public void render(SpriteBatch batch) {
        if(!texInited) initGraphics("socket");

        sprite.draw(batch);
    }

    public Polygon boxToPoly(Rectangle r) {
        return game.poly(r, sprite.getWidth(), sprite.getHeight(), 0, 0, 0);
    }

    public void renderOverlay(SpriteBatch batch) {
        overlay.draw(batch);

        if(game.showHitboxes) {
            for(Rectangle r : box) {
                game.renderHitbox(boxToPoly(r).getTransformedVertices(), r==box[0] ? Color.RED : Color.PURPLE);
            }
        }

        renderHP(batch);
    }

    public void renderPointer(SpriteBatch batch) {
        pointer.draw(batch);
        pointer.setY(pointer.getY()+(y+100-pointer.getY())*0.1f);
    }

    float shownEnergy;
    public void renderHP(SpriteBatch batch) {
        Color col = Color.RED;
        if(game.getMe()!=null) {
            if(game.getMe().team == team) {
                col = Color.GREEN;
            }
        }

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_COLOR);

        game.sr.setProjectionMatrix(game.cam.combined);
        game.sr.begin(ShapeRenderer.ShapeType.Filled);

        if(energy>0) {
            game.sr.setColor(new Color(128/255f,128/255f,128/255f,0.3f));
            game.sr.rect(x, y, 100, 10);
        }

        shownEnergy+=(energy-shownEnergy)*0.1f;

        game.sr.setColor(col);
        game.sr.rect(x, y, shownEnergy, 10);

        game.sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }
}
