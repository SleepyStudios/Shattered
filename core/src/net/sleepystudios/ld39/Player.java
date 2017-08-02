package net.sleepystudios.ld39;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;

/**
 * Created by tudor on 29/07/2017.
 */
public class Player {
    private LD39 game;

    int id, team, energy;
    float x, y, tempAngle;
    boolean texInited;

    Sprite bulb;
    Sprite light;

    float shownCamX, shownCamY, camX, camY, tmrShakeScreen, tmrEnergy;
    boolean firstUpdate, shakeScreen;

    final int LEFT = 1;
    final int RIGHT = -1;
    int dir = LEFT;

    final int GROUNDED = 0;
    final int JUMPING = 1;
    final int FALLING = 2;
    final int THRUSTING = 3;
    int state = GROUNDED;

    Vector2 vel = new Vector2();
    Vector2 thrust = new Vector2();
    final float JUMP_VELOCITY = 10f;
    final float UPTHRUST = 0.15f;
    final float GRAVITY = 0.5f;
    final float ACCEL_X = 0.1f;
    final float MAX_VEL_X = 10f;
    final float MAX_VEL_Y = 1f;
    final float ROT_SPEED = 8f;
    final float THRUST_SPEED = 30f;

    Rectangle box, headBox;

    boolean dead, moving;

    Sparks sparks;

    public Player(LD39 game, Packets.NewPlayer np) {
        this.game = game;
        this.id = np.id;
        this.team = np.team;
        this.energy = np.energy;
        this.x = np.x;
        this.y = np.y;
        this.dead = np.dead;

        tempAngle = np.angle;
        sparks = new Sparks(this);
    }

    private void initGraphics() {
        bulb = new Sprite(new Texture("bulb.png"));
        bulb.setRotation(tempAngle);
        light = new Sprite(new Texture("light.png"));
        setEnergy(this.energy);
        positionLight();
        updateHitBox();

        texInited = true;
    }

    public void render(SpriteBatch batch) {
        if(!texInited) initGraphics();

        if(dead) return;

        bulb.draw(batch);

        if(game.showHitboxes) {
            game.renderHitbox(boxToPoly().getTransformedVertices(), Color.RED);
            game.renderHitbox(headBoxToPoly().getTransformedVertices(), Color.YELLOW);
        }

        update();
    }

    public void renderLight(SpriteBatch batch) {
        if(dead) return;

        light.draw(batch);
        sparks.render(batch);
    }

    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        if(shakeScreen) {
            tmrShakeScreen+=delta;
            if(tmrShakeScreen>=0.2) {
                shakeScreen = false;
                tmrShakeScreen = 0;
            }
        }

        tmrEnergy+=delta;
        if(tmrEnergy>=0.5) {
            if(game.me==id && inBase()==team && energy>0 && game.base[team].energy<100) {
                transferEnergy();
                LD39.playSound("chargeup.mp3");
            }

            if(inControlPoint()==1 && energy<60) {
                sparks.active = true;
                if(game.me==id) {
                    siphonEnergy();
                    LD39.playSound("chargeup.wav");
                }
            } else {
                sparks.active = false;
            }

            tmrEnergy = 0;
        }

        if(game.me==id && !dead) {
            float panSpeed = 0.08f;

            game.cam.position.set(shownCamX+=(camX-shownCamX)*panSpeed, shownCamY+=(camY-shownCamY)*0.1f, 0);
            updateCam();

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                if(!isBlockedX()) vel.x += (-MAX_VEL_X - vel.x) * ACCEL_X * (state!=GROUNDED ? 0.8f : 1f);
                if(canRotate()) bulb.setRotation(bulb.getRotation() + (dir * ROT_SPEED));
                dir = LEFT;
                moving = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                if(!isBlockedX()) vel.x += (MAX_VEL_X - vel.x) * ACCEL_X * (state!=GROUNDED ? 0.8f : 1f);
                if(canRotate()) bulb.setRotation(bulb.getRotation() + (dir * ROT_SPEED));
                dir = RIGHT;
                moving = true;
            } else {
                if (state == GROUNDED) vel.x += (0 - vel.x) * (ACCEL_X * 2);
                moving = false;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if(state==GROUNDED) {
                    state = JUMPING;
                    vel.y = MAX_VEL_Y;
                //} else {
                //    thrust();
                }
            }

            // jumping
            if (state == JUMPING) {
                vel.y += (JUMP_VELOCITY + MAX_VEL_Y - vel.y) * UPTHRUST;

                if (vel.y >= JUMP_VELOCITY + MAX_VEL_Y - 0.1f) {
                    state = FALLING;
                }
            }

            // gravity
            if (state == FALLING) {
                vel.y += (-JUMP_VELOCITY - vel.y) * GRAVITY;
            }
            if(state==GROUNDED && !isBlocked()) {
                state = FALLING;
            }

            // rotate in the air
            if(state==JUMPING || state==FALLING) {
                if(!moving) {
                    bulb.setRotation(bulb.getRotation() + (dir * ROT_SPEED));
                }
            }

            if(state!=THRUSTING) {
                move(x+vel.x, y+vel.y);
            } else {
                move(x + thrust.x, y + thrust.y);

                float factor = 0.01f;
                if(thrust.y>0) factor = 0.1f;
                thrust.y+=(0-thrust.y)*factor;

                if((int) thrust.y==0) {
                    vel.x = thrust.x;
                    thrust.set(0, 0);
                    state = FALLING;
                }
            }
        }
    }

    public void thrust() {
        if(state==GROUNDED || state==FALLING || state==THRUSTING) return;

        state = THRUSTING;
        thrust.x = MathUtils.sin(MathUtils.degRad * bulb.getRotation()) * THRUST_SPEED;
        thrust.y = -MathUtils.cos(MathUtils.degRad * bulb.getRotation()) * THRUST_SPEED;

        thrust.x = MathUtils.clamp(thrust.x, -THRUST_SPEED, THRUST_SPEED);
        thrust.y = MathUtils.clamp(thrust.y, -THRUST_SPEED, 5);
    }

    public void updateCam() {
        // get the map properties to find the height/width, etc
        int w = game.MAP_W;
        int h = Gdx.graphics.getHeight();

        float minX = game.cam.zoom * (game.cam.viewportWidth / 2);
        float maxX = (w) - minX;
        float minY = game.cam.zoom * (game.cam.viewportHeight / 2);
        float maxY = (h) - minY;

        float x = this.x;
        camX = Math.min(maxX, Math.max(x, minX));
        camY = Math.min(maxY, Math.max(y, minY));

        if(!firstUpdate) {
            shownCamX = camX;
            shownCamY = camY;
            game.cam.position.set(shownCamX, shownCamY, 0);

            firstUpdate = true;
        }

        float ox=0, oy=0;
        if(shakeScreen) {
            ox = LD39.rand(2, 6);
            oy = LD39.rand(2, 6);
        }

        game.cam.position.set(shownCamX+ox, shownCamY+oy, 0);
    }

    private void move(float x, float y) {
        this.x = x;
        this.y = y;

        isBlocked();
        positionLight();

        Packets.PlayerMove pm = new Packets.PlayerMove();
        pm.id = id;
        pm.x = x;
        pm.y = y;
        pm.angle = bulb.getRotation();
        game.n.client.sendUDP(pm);

        updateHitBox();
    }

    private boolean canRotate() {
        return inBase()==-1 && inControlPoint()==-1;
    }

    private int inBase() {
        // bases
        for(Base b : game.base) {
            if(b.texInited) {
                if(intersectsBottom(b.boxToPoly(b.box))) return b.team;
                if(b.collides(boxToPoly()) || b.collides(headBoxToPoly())) return 2;
            }
        }

        return -1;
    }

    private int inControlPoint() {
        for(ControlPoint c : game.controls) {
            if(c.texInited) {
                if(intersectsBottom(c.boxToPoly(c.box))) return 1;
                if(c.collides(boxToPoly()) || c.collides(headBoxToPoly())) return 2;
            }
        }

        return -1;
    }

    private boolean isBlockedX() {
        if((x+vel.x <= 0 || x+vel.x >= game.MAP_W - bulb.getWidth()) && state==GROUNDED) {
            thrust.x = 0;
            vel.x = 0;

            return true;
        }

        // bases
        for(Base b : game.base) {
            if(b.texInited) {
                if(b.collides(boxToPoly()) || intersects(b.boxToPoly((b.box)))) return true;
            }
        }

        // control points
        for(ControlPoint c : game.controls) {
            if (c.texInited) {
                if(c.collides(boxToPoly()) || intersects(c.boxToPoly((c.box)))) return true;
            }
        }

        return false;
    }

    private boolean isBlocked() {
        if(y+vel.y <= 0) {
            vel.y = 0;
            state = GROUNDED;

            return true;
        }

        if((x+vel.x <= 0 || x+vel.x >= game.MAP_W - bulb.getWidth()) && state!=GROUNDED) {
            thrust.x = 0;
            vel.x = 0;
            if(state==THRUSTING) state = FALLING;

            return true;
        }

        if(texInited) {
            if(state!=JUMPING) {
                // platforms
                for(int i=0; i<game.platforms.size(); i++) {
                    if(game.platforms.get(i).texInited) {
                        if(intersects(game.platforms.get(i).boxToPoly())) {
                            collideWithThing();
                            return true;
                        }
                    }
                }

                // bases
                for(Base b : game.base) {
                    if(b.texInited) {
                        if(b.collides(boxToPoly()) || b.collides(headBoxToPoly())) {
                            collideWithThing();
                            return true;
                        }
                    }
                }

                // control points
                for(ControlPoint c : game.controls) {
                    if (c.texInited) {
                        if(c.collides(boxToPoly()) || c.collides(headBoxToPoly())) {
                            collideWithThing();
                            return true;
                        }
                    }
                }
            }

            for(int i=0; i<game.players.size(); i++) {
                Player p = game.players.get(i);
                if(p.id!=id && !p.dead && p.texInited) {
                    if(Intersector.overlapConvexPolygons(p.boxToPoly(), boxToPoly())) {
                        // head to head
                        vel.x = -vel.x*0.75f;
                        vel.y = 0;
                        return true;
                    }
                    if(Intersector.overlapConvexPolygons(p.headBoxToPoly(), boxToPoly())) {
                        if(state!=GROUNDED) {
                            // kill
                            Packets.PlayerDied pd = new Packets.PlayerDied();
                            pd.id = p.id;
                            game.n.client.sendTCP(pd);
                            shakeScreen = true;

                            p.dead = true;

                            if(p.energy>0) {
                                game.addActionMessage("Energy acquired!", Color.WHITE);
                            } else {
                                game.addActionMessage("Enemy shattered!", Color.WHITE);
                            }

                            LD39.playSound("shatter.mp3");
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void collideWithThing() {
        if(state!=GROUNDED) {
            vel.x = 0;
            thrust.x = 0;
        }

        vel.y = MathUtils.clamp(vel.y-0.1f, 0, vel.y);
        thrust.y = MathUtils.clamp(thrust.y-0.1f, 0, thrust.y);

        state = GROUNDED;
    }

    public boolean intersects(Polygon poly) {
        return Intersector.overlapConvexPolygons(poly, headBoxToPoly()) /*|| Intersector.overlapConvexPolygons(poly, boxToPoly())*/;
    }

    public boolean intersectsBottom(Polygon poly) {
        return Intersector.overlapConvexPolygons(poly, boxToPoly()) /*|| Intersector.overlapConvexPolygons(poly, boxToPoly())*/;
    }

    public boolean intersectsBoth(Polygon poly) {
        return Intersector.overlapConvexPolygons(poly, headBoxToPoly()) || Intersector.overlapConvexPolygons(poly, boxToPoly());
    }

    public void positionLight() {
        bulb.setPosition(x, y);

        light.setScale(0.6f);
        light.setRotation(bulb.getRotation());

        float wDiff = light.getWidth() - bulb.getWidth();
        float hDiff = light.getHeight() - bulb.getHeight();
        light.setPosition(x-wDiff/2, y-hDiff/2);
    }

    public static int FW = 51, FH = 90;
    private int hOX = 10, hOY = 20, OX = 16, OY = 4;
    public void updateHitBox() {
        headBox = new Rectangle(x+hOX, y+hOY, FW-(hOX*2), FH-(hOY*2));
        box = new Rectangle(x+OX, y+OY, FW-(OX*2), 14);
    }

    public Polygon boxToPoly() {
        if(box==null) return null;
        return game.poly(box, FW, FH, OX, OY, bulb.getRotation());
    }

    public Polygon headBoxToPoly() {
        if(headBox==null) return null;
        return game.poly(headBox, FW, FH, hOX, hOY, bulb.getRotation());
    }

    public void setEnergy(int energy) {
        this.energy = energy;
        light.setAlpha(energy/100f);
    }

    public void onDeath() {
        dead = true;
        state = GROUNDED;
        vel.set(0,0);
        thrust.set(0,0);
        game.explosions.add(new Explosion(boxToPoly().getOriginX(), boxToPoly().getOriginY()));
    }

    public void transferEnergy() {
        Packets.PlayerEnergy pe = new Packets.PlayerEnergy();
        pe.id = id;
        pe.energy = energy-5;
        game.n.client.sendUDP(pe);

        Packets.BaseUpdate bu = new Packets.BaseUpdate();
        bu.team = team;
        bu.energy = game.base[team].energy+5;
        game.n.client.sendUDP(bu);
    }

    public void siphonEnergy() {
        Packets.PlayerEnergy pe = new Packets.PlayerEnergy();
        pe.id = id;
        pe.energy = energy+5;
        game.n.client.sendUDP(pe);
    }
}
