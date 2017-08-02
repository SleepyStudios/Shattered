package net.sleepystudios.ld39;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Created by tudor on 31/07/2017.
 */
public class Base {
    LD39 game;
    float x, y;
    int energy, team;

    Sprite sprite, overlay, pointer;
    Rectangle box;

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

    ArrayList<Vector2> vecs;
    public void initBoxes() {
        box = new Rectangle(x, y+25, 100, 20);

        float bx = x+10;
        float by = y+10;
        float bw = 80;
        float bh = 90;

        Bezier<Vector2> bezier = new Bezier<Vector2>(
                new Vector2(bx+bw, by+bh),
                new Vector2(bx+bw, by),
                new Vector2(bx, by),
                new Vector2(bx, by+bh));

        vecs = new ArrayList<Vector2>();
        for(int i=0; i<100; i++) {
            float t = i/100f;

            Vector2 st = new Vector2();
            st = bezier.valueAt(st, t);
            vecs.add(st);
        }
    }

    public boolean collides(Polygon p) {
        for(int i=0; i<vecs.size(); i++) {
            if(p.contains(vecs.get(i))) return true;
        }
        return false;
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

        renderHitBoxes(batch);

        renderHP(batch);
    }

    public void renderHitBoxes(SpriteBatch batch) {
        batch.end();

        if(game.showHitBoxes) {
            float[] floats = new float[vecs.size()*2];
            int i = 0;
            while(i<floats.length) {
                for(int j=0; j<vecs.size(); j++) {
                    floats[i] = vecs.get(j).x;
                    floats[i+1] = vecs.get(j).y;
                    i+=2;
                }
            }

            game.sr.begin(ShapeRenderer.ShapeType.Line);
            game.sr.setProjectionMatrix(game.cam.combined);
            game.sr.setColor(Color.PURPLE);
            game.sr.polyline(floats);

            game.sr.setColor(Color.RED);
            game.sr.rect(box.x, box.y, box.width, box.height);

            game.sr.end();
        }

        batch.begin();
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
