package net.sleepystudios.ld39;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/**
 * Created by tudor on 29/07/2017.
 */
public class Receiver extends Listener {
    private LD39 game;

    public Receiver(LD39 game) {
        this.game = game;
    }

    @Override
    public void connected(Connection c) {
        super.connected(c);
    }

    @Override
    public void disconnected(Connection c) {
        super.disconnected(c);

        game.me = -1;
        game.players.clear();
    }

    @Override
    public void received(Connection c, Object o) {
        super.received(c, o);

        if(o instanceof Packets.NewPlayer) {
            game.players.add(new Player(game, (Packets.NewPlayer) o));
            if(((Packets.NewPlayer) o).you) game.me = ((Packets.NewPlayer) o).id;
        }

        if(o instanceof Packets.PlayerMove) {
            Player p = game.getPlayerByID(((Packets.PlayerMove) o).id);
            if(p==null) return;

            p.x = ((Packets.PlayerMove) o).x;
            p.y = ((Packets.PlayerMove) o).y;

            if(p.texInited) {
                p.bulb.setRotation(((Packets.PlayerMove) o).angle);
                p.positionLight();
                p.updateHitBox();
            }
        }

        if(o instanceof Packets.PlayerLeave) {
            Player p = game.getPlayerByID(((Packets.PlayerLeave) o).id);
            if(p==null) return;

            game.players.remove(p);
        }

        if(o instanceof Packets.PlayerDied) {
            Player p = game.getPlayerByID(((Packets.PlayerDied) o).id);
            if(p==null) return;

            p.onDeath();

            if(p==game.getMe()) {
                game.addActionMessage("Shattered!", Color.WHITE);
                LD39.playSound("shatter.mp3");
            }
        }

        if(o instanceof Packets.PlayerRespawned) {
            Player p = game.getPlayerByID(((Packets.PlayerRespawned) o).id);
            if(p==null) return;

            p.dead = false;
            p.setEnergy(0);
        }

        if(o instanceof Packets.PlayerEnergy) {
            Player p = game.getPlayerByID(((Packets.PlayerEnergy) o).id);
            if(p==null) return;

            p.setEnergy(((Packets.PlayerEnergy) o).energy);
        }

        if(o instanceof Packets.BaseUpdate) {
            game.base[((Packets.BaseUpdate) o).team].energy = ((Packets.BaseUpdate) o).energy;
        }

        if(o instanceof Packets.NewMatch) {
            if(!game.newMatch) {
                if(game.getMe()!=null) {
                    if(((Packets.NewMatch) o).winner==game.getMe().team) {
                        game.addActionMessage("Your team won!", Color.WHITE);
                    } else {
                        game.addActionMessage("Your team lost", Color.WHITE);
                    }

                    for(int i=0; i<game.players.size(); i++) {
                        game.players.get(i).onDeath();
                    }
                }

                game.newMatch = true;
            } else {
                game.newMatch = false;
                game.explosions.clear();
                game.actionMessages.clear();
            }
        }
    }
}
