package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by tudor on 29/07/2017.
 */
public class AnimGenerator {
    public static TextureRegion[] gen(String loc, int fw, int fh) {
        Texture tmpTex = new Texture(loc);
        TextureRegion[][] tmpFrames = TextureRegion.split(tmpTex, fw, fh);
        TextureRegion[] frames;

        int cols = tmpTex.getWidth()/fw;

        frames = new TextureRegion[cols];
        for(int i=0, index=0; i<cols; i++, index++) {
            frames[index] = tmpFrames[0][i];
        }

        return frames;
    }
}