package net.sleepystudios.ld39server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Created by tudor on 29/07/2017.
 */
public class Receiver extends Listener {
    LD39Server game;

    public Receiver(LD39Server game) {
        this.game = game;
    }

    @Override
    public void connected(Connection c) {
        System.out.println(game.getTimestamp() +  "Player id: " + c.getID() + " joined the game");

        game.players.add(new Player(game, c.getID(), game.chooseTeam()));
        Player p = game.players.get(game.players.size()-1);

        Packets.NewPlayer np = new Packets.NewPlayer();
        np.id = p.id;
        np.team = p.team;
        np.energy = p.energy;
        np.x = p.x;
        np.y = p.y;
        np.angle = p.angle;
        np.you = true;
        c.sendTCP(np);

        for(Base b : game.base) {
            Packets.BaseUpdate bu = new Packets.BaseUpdate();
            bu.team = b.team;
            bu.energy = b.energy;
            c.sendTCP(bu);
        }

        // send them to everyone else
        np.you = false;
        game.n.server.sendToAllExceptTCP(c.getID(), np);

        // send them everyone else
        for(Player other : game.players) {
            if(other.id!=c.getID()) {
                np = new Packets.NewPlayer();
                np.id = other.id;
                np.team = other.team;
                np.energy = other.energy;
                np.x = other.x;
                np.y = other.y;
                np.angle = other.angle;
                np.dead = other.dead;
                c.sendTCP(np);
            }
        }
    }

    @Override
    public void disconnected(Connection c) {
        super.disconnected(c);

        System.out.println(game.getTimestamp() + "Player id: " + c.getID() + " left the game");

        Player p = game.getPlayerByID(c.getID());
        if(p==null) return;

        game.players.remove(p);

        Packets.PlayerLeave pl = new Packets.PlayerLeave();
        pl.id = c.getID();
        game.n.server.sendToAllExceptUDP(c.getID(), pl);
    }

    @Override
    public void received(Connection c, Object o) {
        super.received(c, o);

        if(o instanceof Packets.PlayerMove) {
            Player p = game.getPlayerByID(c.getID());
            if(p==null) return;

            p.x = ((Packets.PlayerMove) o).x;
            p.y = ((Packets.PlayerMove) o).y;
            p.angle = ((Packets.PlayerMove) o).angle;

            game.n.server.sendToAllExceptUDP(c.getID(), o);
        }

        if(o instanceof Packets.PlayerDied) {
            Player p = game.getPlayerByID(((Packets.PlayerDied) o).id);
            if(p==null) return;

            int tempEnergy;

            // kill them
            p.dead = true;
            tempEnergy = p.energy;
            p.energy = 0;

            game.n.server.sendToAllTCP(o);

            // killer energy
            Player killer = game.getPlayerByID(c.getID());
            killer.energy+=tempEnergy;
            if(killer.energy>game.MAX_ENERGY) killer.energy = game.MAX_ENERGY;

            Packets.PlayerEnergy pe = new Packets.PlayerEnergy();
            pe.id = c.getID();
            pe.energy = killer.energy;
            game.n.server.sendToAllUDP(pe);
        }

        if(o instanceof Packets.PlayerEnergy) {
            Player p = game.getPlayerByID(((Packets.PlayerEnergy) o).id);
            if(p==null) return;

            if(((Packets.PlayerEnergy) o).energy>game.MAX_ENERGY) {
                return;
            }

            p.energy = ((Packets.PlayerEnergy) o).energy;
            game.n.server.sendToAllUDP(o);
        }

        if(o instanceof Packets.BaseUpdate) {
            if(((Packets.BaseUpdate) o).energy>game.MAX_BASE_ENERGY) return;

            game.base[((Packets.BaseUpdate) o).team].energy = ((Packets.BaseUpdate) o).energy;
            game.base[((Packets.BaseUpdate) o).team].tmr = 0;
            game.n.server.sendToAllUDP(o);
        }
    }
}
