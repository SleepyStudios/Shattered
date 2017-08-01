package net.sleepystudios.ld39;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Created by tudor on 31/07/2017.
 */
public class ActionMessage {
    public String text;
    private int size;
    private Color colour;
    private float y, tmrLife;
    private BitmapFont font, shadow;

    public ActionMessage(String text, int size, Color colour) {
        this.text = text;
        this.size = size;
        this.colour = colour;
    }

    private void initFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Freeroad.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;
        parameter.borderWidth = 0;
        parameter.borderColor = new Color(0.3f, 0.3f, 0.3f, 0.5f);
        parameter.spaceX--;
        parameter.color = colour;

        font = generator.generateFont(parameter);
        shadow = generator.generateFont(parameter);
        generator.dispose();
    }

    private void checkFont() {
        if(font==null) initFont();
    }

    public void render(SpriteBatch batch, LD39 game) {
        checkFont();

        int index = game.actionMessages.indexOf(this);
        float tar = game.getMe().y + 40 + (index*20);

        y+=(tar-y)*0.2f;
        tmrLife+=Gdx.graphics.getDeltaTime();

        if(tmrLife>=3) {
            if(font.getColor().a-0.1f>0) {
                font.getColor().a-=0.1f;
            } else {
                game.actionMessages.remove(this);
                return;
            }
        }

        shadow.setColor(0.6f, 0.6f, 0.6f, font.getColor().a);
        GlyphLayout gl = new GlyphLayout(font, text);
        float x = game.getMe().boxToPoly().getOriginX()-gl.width/2;

        shadow.draw(batch, text, x, y-32);
        font.draw(batch, text, x, y-30);
    }
}
