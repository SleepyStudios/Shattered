package net.sleepystudios.ld39server;

/**
 * Created by tudor on 29/07/2017.
 */
public class Player {
    LD39Server game;

    int id, team, energy;
    int MAP_W = 2000;
    float x, y, angle;

    boolean dead;

    float tmrRespawn; int respawnTime=3;

    public Player(LD39Server game, int id, int team) {
        this.game = game;
        this.id = id;
        this.team = team;

        x = setX();
        y = 100;
    }

    public void update(float delta) {
        if(dead && !game.newMatch) {
            tmrRespawn+=delta;
            if(tmrRespawn>=respawnTime) {
                respawn();
                tmrRespawn = 0;
            }
        } else {
            tmrRespawn = 0;
        }
    }

    public float setX() {
        return team == 0 ? LD39Server.rand(200, MAP_W/2-MAP_W/4-100) : LD39Server.rand(MAP_W/2+MAP_W/4+100, MAP_W-200);
    }

    public void respawn() {
        Packets.PlayerRespawned pr = new Packets.PlayerRespawned();
        pr.id = id;
        game.n.server.sendToAllTCP(pr);

        dead = false;
        x = setX();
        y = 100;

        Packets.PlayerMove pm = new Packets.PlayerMove();
        pm.id = id;
        pm.x = x;
        pm.y = y;
        pm.angle = angle;
        game.n.server.sendToAllUDP(pm);
    }
}
