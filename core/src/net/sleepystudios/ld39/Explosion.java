package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Created by tudor on 31/07/2017.
 */
public class Explosion {
    float x, y;
    ArrayList<Bit> bits = new ArrayList<Bit>();
    boolean active = true;
    int totParticles, maxParticles=10;

    public Explosion(float x, float y) {
        this.x = x;
        this.y = y;
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
            bits.add(new Bit(x, y));
            totParticles++;

            if(totParticles>=maxParticles) {
                active = false;
            }
        }
    }

    private static class Bit {
        float x, y, maxVel=LD39.rand(0.1f, 5f), xVel, yVel, scale=LD39.randNoZero(0.1f, 2f), rotSpeed = LD39.rand(3f, 16f), alpha=1f;
        Sprite s;
        boolean exists = true;

        public Bit(float x, float y) {
            this.x = x;
            this.y = y;

            String particle = "";
            switch(LD39.rand(0, 2)) {
                case 0:
                    particle = "spark";
                    break;
                case 1:
                    particle = "shatter";
                    break;
                case 2:
                    particle = "shard";
                    break;
            }

            s = new Sprite(new Texture(particle + ".png"));
            s.setPosition(x, y);
            s.setScale(scale);

            xVel = LD39.randNoZero(-maxVel, maxVel);
            yVel = LD39.randNoZero(-maxVel, maxVel);
        }

        public void render(SpriteBatch batch) {
            s.setX(s.getX()+xVel);
            s.setY(s.getY()+yVel);
            s.setRotation(s.getRotation()+rotSpeed);
            alpha+=(0-alpha)*0.05f;
            s.setAlpha(alpha);

            s.draw(batch);

            if(alpha<=0.1f) {
                exists = false;
            }
        }
    }
}
