package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Created by tudor on 31/07/2017.
 */
public class Sparks {
    Player p;
    ArrayList<SparkBit> bits = new ArrayList<SparkBit>();
    boolean active;
    int maxBits = 6;

    public Sparks(Player p) {
        this.p = p;
    }

    public void render(SpriteBatch batch) {
        for(int i=0; i<bits.size(); i++) {
            if(bits.get(i).exists) {
                bits.get(i).render(batch);
            } else {
                bits.remove(i);
            }
        }

        if(active) {
            if(bits.size()<maxBits) bits.add(new SparkBit(p.boxToPoly().getOriginX(), p.boxToPoly().getOriginY()));
        }
    }

    private static class SparkBit {
        float x, y, maxVel=LD39.rand(1f, 2f), xVel, yVel, scale=LD39.randNoZero(0.2f, 1.5f), rotSpeed = LD39.rand(1f, 3f), alpha=5f;
        Sprite s;
        boolean exists = true;

        public SparkBit(float x, float y) {
            this.x = x;
            this.y = y;

            s = new Sprite(new Texture("spark.png"));
            s.setPosition(x, y);
            s.setScale(scale);

            xVel = LD39.randNoZero(-maxVel, maxVel);
            yVel = LD39.randNoZero(-maxVel, maxVel);
        }

        public void render(SpriteBatch batch) {
            s.setX(s.getX()+xVel);
            s.setY(s.getY()+yVel);
            s.setRotation(s.getRotation()+rotSpeed);
            alpha+=(0-alpha)*0.1f;
            s.setAlpha(alpha);

            s.draw(batch);

            if(alpha<=0.1f) exists = false;
        }
    }
}
