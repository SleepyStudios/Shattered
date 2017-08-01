package net.sleepystudios.ld39server;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tudor on 29/07/2017.
 */
public class LD39Server {
    Network n;
    ArrayList<Player> players = new ArrayList<>();
    Base base[] = new Base[2];

    final int MAX_ENERGY = 60;

    public LD39Server() {
        n = new Network(this);
        initMatch(false);
        loop();
    }

    public void initMatch(boolean newMatch) {
        base[0] = new Base(0, 100);
        base[1] = new Base(1, 100);

        if(newMatch) {
            for(Base b : base) {
                Packets.BaseUpdate bu = new Packets.BaseUpdate();
                bu.team = b.team;
                bu.energy = b.energy;
                n.server.sendToAllTCP(bu);
            }

            for(Player p : players) {
                p.energy = 0;
                p.respawn();
            }

            Packets.NewMatch nm = new Packets.NewMatch();
            n.server.sendToAllTCP(nm);
        }
    }

    public void loop() {
        long lastLoopTime = System.nanoTime();
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        while (true)
        {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            double delta = updateLength / ((double)OPTIMAL_TIME);

            update((float) delta / 60f);

            try{
                Thread.sleep((lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean newMatch;
    float tmrNewMatch;
    public void update(float delta) {
        for(int i=0; i<players.size(); i++) {
            players.get(i).update(delta);
        }

        if(players.size()>0 && !newMatch) {
            for(Base b : base) {
                b.tmr+=delta;
                if(b.tmr>=5 && b.energy>0 && !newMatch) {
                    b.energy-=5;

                    // did someone lose?
                    if(b.energy<=0 && !newMatch) {
                        Packets.NewMatch nm = new Packets.NewMatch();
                        nm.winner = 1-b.team;
                        n.server.sendToAllTCP(nm);
                        newMatch = true;

                        System.out.println("Team " + nm.winner + " won!");
                    }

                    Packets.BaseUpdate bu = new Packets.BaseUpdate();
                    bu.team = b.team;
                    bu.energy = b.energy;
                    n.server.sendToAllTCP(bu);
                    b.tmr = 0;
                }
            }
        }

        if(newMatch) {
            tmrNewMatch+=delta;
            if(tmrNewMatch>=4) {
                initMatch(true);
                newMatch = false;
                tmrNewMatch=0;
            }
        }
    }

    public Player getPlayerByID(int id) {
        for(int i=0; i<players.size(); i++) {
            if(players.get(i).id==id) return players.get(i);
        }
        return null;
    }

    public int chooseTeam() {
        int count[] = {getTeamCount(0), getTeamCount(1)};

        if(count[0] < count[1]) return 0;
        if(count[0] > count[1]) return 1;
        return rand(0, 1);
    }

    public int getTeamCount(int team) {
        int count = 0;

        for(Player p: players) {
            if(p.team==team) count++;
        }
        return count;
    }

    // generates a random number
    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
    public static float rand(float min, float max) {
        return min + new Random().nextFloat() * (max - min);
    }

    // random number that cannot be 0
    public static float randNoZero(float min, float max) {
        float r = rand(min, max);
        return r != 0 ? r : randNoZero(min, max);
    }

    public static void main(String[] args) {
        new LD39Server();
    }
}
