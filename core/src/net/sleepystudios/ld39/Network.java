package net.sleepystudios.ld39;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;

/**
 * Created by tudor on 29/07/2017.
 */
public class Network {
    public Client client;

    public Network(LD39 game, String ip, int tcp, int udp) {
        client = new Client();
        client.addListener(new Receiver(game));
        client.start();
        register();

        try {
            client.connect(5000, ip, tcp, udp);
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("Couldn't connect");
        }
    }

    private void register() {
        Kryo kryo = client.getKryo();
        kryo.register(Packets.NewPlayer.class);
        kryo.register(Packets.PlayerLeave.class);
        kryo.register(Packets.PlayerMove.class);
        kryo.register(Packets.PlayerDied.class);
        kryo.register(Packets.PlayerRespawned.class);
        kryo.register(Packets.PlayerEnergy.class);
        kryo.register(Packets.BaseUpdate.class);
        kryo.register(Packets.NewMatch.class);
    }
}
