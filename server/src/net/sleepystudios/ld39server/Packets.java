package net.sleepystudios.ld39server;

/**
 * Created by tudor on 29/07/2017.
 */
public class Packets {
    public static class NewPlayer {
        public int id, team, energy;
        public float x, y, angle;
        public boolean you, dead;
    }

    public static class PlayerLeave {
        public int id;
    }

    public static class PlayerMove {
        public int id;
        public float x, y, angle;
    }

    public static class PlayerDied {
        public int id;
    }

    public static class PlayerRespawned {
        public int id;
    }

    public static class PlayerEnergy {
        public int id, energy;
    }

    public static class BaseUpdate {
        public int team, energy;
    }

    public static class NewMatch {
        public int winner;
    }
}