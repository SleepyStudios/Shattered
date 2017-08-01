package net.sleepystudios.ld39server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by tudor on 29/07/2017.
 */
public class Network {
    LD39Server game;
    Server server;

    int tcp, udp;

    public Network(LD39Server game) {
        try {
            readConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.game = game;

        server = new Server();
        try {
            server.bind(tcp, udp);
            server.addListener(new Receiver(game));
            server.start();

            register();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server running on tcp: " + tcp + ", udp: " + udp);
    }

    public void readConfig() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("server_config.dat"));
        try {
            String line;
            while((line = br.readLine()) != null) {
                if(!line.startsWith("#")) {
                    // tcp
                    if (line.startsWith("tcp")) tcp = Integer.valueOf(line.split(":")[1]);

                    // udp
                    if (line.startsWith("udp")) udp = Integer.valueOf(line.split(":")[1]);
                }
            }
        } finally {
            br.close();
        }
    }

    private void register() {
        Kryo kryo = server.getKryo();
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
