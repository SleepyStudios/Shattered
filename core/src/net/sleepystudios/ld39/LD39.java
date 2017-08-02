package net.sleepystudios.ld39;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class LD39 extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Network n; int tcp, udp; String ip;
	OrthographicCamera cam;

	ArrayList<Player> players = new ArrayList<Player>();
	int me = -1;

	ShapeRenderer sr;
	boolean showHitBoxes;

	final int MAP_W = 2000;
	Base[] base = new Base[2];
	ArrayList<Platform> platforms = new ArrayList<Platform>();
	ArrayList<ControlPoint> controls = new ArrayList<ControlPoint>();
	ArrayList<Explosion> explosions = new ArrayList<Explosion>();
	ArrayList<ActionMessage> actionMessages = new ArrayList<ActionMessage>();

	boolean newMatch;

	public LD39() {
		base[0] = new Base(this, 0, 0, 0);
		base[1] = new Base(this, MAP_W-100, 0, 1);
	}

	public void readConfig() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("config.dat"));
		try {
			String line;
			while((line = br.readLine()) != null) {
				if(!line.startsWith("#")) {
					// ip
					if (line.startsWith("ip")) {
						if (line.split(":")[1].contains("auto")) {
							String[] server = getMasterIP().split(":");

							ip = server[0];
							tcp = Integer.valueOf(server[1]);
							udp = Integer.valueOf(server[2]);
							return;
						} else {
							ip = line.split(":")[1];
						}
					}

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

	public String getMasterIP() throws IOException {
		Waker waker = new Waker("shattered");
		return waker.getServer();
	}

	@Override
	public void create () {
		batch = new SpriteBatch();

		try {
			readConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}

		n = new Network(this, ip, tcp, udp);
		cam = new OrthographicCamera(Gdx.graphics.getWidth() , Gdx.graphics.getHeight());
		font = new BitmapFont();
		sr = new ShapeRenderer();

		initFont();

		float scale = ((MAP_W-200)/2)/100 - 0.5f;
		platforms.add(new Platform(this, (scale*100)/2+50, 0, scale));
		platforms.add(new Platform(this, (scale*100)*2-275, 0, scale));

		platforms.add(new Platform(this, 550, 200));
		platforms.add(new Platform(this, MAP_W-650, 200));

		platforms.add(new Platform(this, MAP_W/2-150, 450, 1));
		platforms.add(new Platform(this, MAP_W/2+50, 450, 1));

		controls.add(new ControlPoint(this, MAP_W/2-50, 0));
		controls.add(new ControlPoint(this, MAP_W/2-50, 450));

		Music m = Gdx.audio.newMusic(Gdx.files.internal("lighting the way.mp3"));
		m.setVolume(1f);
		m.setLooping(true);
		m.play();

		Gdx.input.setInputProcessor(this);
	}

	private BitmapFont font;
	private void initFont() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Freeroad.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

		parameter.size = 36;
		parameter.borderWidth = 1;
		parameter.borderColor = new Color(0.3f, 0.3f, 0.3f, 0.5f);
		parameter.spaceX--;
		parameter.color = Color.WHITE;

		font = generator.generateFont(parameter);
		generator.dispose();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(121/255f, 100/255f, 77/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if(me==-1) {
			batch.setProjectionMatrix(resetBatch());
			batch.begin();
			font.setColor(Color.WHITE);

			String text = "Couldn't connect\ncheck twitter.com/waker_bot\nfor server status\n\nPress Space to try again";
			GlyphLayout gl = new GlyphLayout(font, text);
			font.draw(batch, text, Gdx.graphics.getWidth()/2-gl.width/2, Gdx.graphics.getHeight()-200);
			batch.end();
			return;
		}

		cam.update();

		batch.setProjectionMatrix(cam.combined);
		batch.begin();

		for(int i=0; i<platforms.size(); i++) {
			platforms.get(i).render(batch);
		}

		for(Base b : base) b.render(batch);

		for(ControlPoint c : controls) c.render(batch);

		for(int i=0; i<players.size(); i++) {
			Player p = players.get(i);
			p.render(batch);
		}

		for(Base b : base) {
			b.renderOverlay(batch);
			if(getMe()!=null && (messageNum==3 || messageNum==5) && b.team == getMe().team) {
				b.renderPointer(batch);
			} else {
				b.pointer.setY(Gdx.graphics.getHeight());
			}
		}

		for(ControlPoint c : controls) {
			c.renderOverlay(batch);
			if(messageNum==2) c.renderPointer(batch);
		}

		for(int i=0; i<players.size(); i++) {
			Player p = players.get(i);
			p.renderLight(batch);
		}

		for(int i=0; i<explosions.size(); i++) {
			Explosion e = explosions.get(i);
			e.render(batch);
		}

		for(int i=0; i<actionMessages.size(); i++) {
			actionMessages.get(i).render(batch, this);
		}

		batch.end();

		update();
	}

	public Matrix4 resetBatch() {
		Matrix4 uiMatrix = cam.combined.cpy();
		uiMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		return uiMatrix;
	}

	float tmrRespawn, tmrMessages, tmrFireworks; int messageNum;
	public void update() {
		float delta = Gdx.graphics.getDeltaTime();

		if(getMe()!=null && getMe().dead) {
			tmrRespawn+=delta;
			if(tmrRespawn>3) tmrRespawn = 3;
		} else {
			tmrRespawn = 0;
		}

		if(getMe()!=null && getMe().texInited && !newMatch) {
			tmrMessages+=delta;
			int msgDelay = 3;
			if(tmrMessages>=msgDelay) {
				if(messageNum<6) messageNum++;
				tmrMessages = 0;
			}

			if(messageNum==0) addActionMessage("Use 'A' and 'D' to Move", Color.WHITE);
			if(messageNum==1) addActionMessage("Use 'Space' to Jump", Color.WHITE);
			if(messageNum==2) addActionMessage("Siphon power from control points", Color.WHITE);
			if(messageNum==3) addActionMessage("Return power to your base", Color.WHITE);
			if(messageNum==4) addActionMessage("Shatter enemy players to steal their power", Color.WHITE);
			if(messageNum==5) addActionMessage("Stop your base from running out of power", Color.WHITE);
		}

		if(newMatch) {
			tmrFireworks+=delta;
			if(tmrFireworks>=0.1) {
				explosions.add(new Explosion(rand(0, MAP_W), rand(0, Gdx.graphics.getHeight())));
				tmrFireworks = 0;
			}
		}
	}

	public void addActionMessage(String message, Color colour) {
		// make sure its not like any others
		for(ActionMessage am : actionMessages) {
			if(message.equals(am.text)) return;
		}

		int size = 24;
		if(actionMessages.size()>=1) {
			actionMessages.add(new ActionMessage(message, size, colour));
			actionMessages.remove(0);
		} else {
			actionMessages.add(new ActionMessage(message, size, colour));
		}
	}

	public Polygon poly(Rectangle r, float fw, float fh, int ox, int oy, float rot) {
		Polygon poly = new Polygon(new float[] {
				r.getX(), r.getY(),
				r.getX(), r.getY()+r.getHeight(),
				r.getX()+r.getWidth(), r.getY()+r.getHeight(),
				r.getX()+r.getWidth(), r.getY()});
		poly.setOrigin(r.getX()+fw/2-ox, r.getY()+fh/2-oy);
		poly.rotate(rot);

		return poly;
	}

	public void renderHitBox(float[] verts, Color color) {
		batch.end();

		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.setProjectionMatrix(cam.combined);
		sr.setColor(color);
		sr.polygon(verts);

		sr.end();

		batch.begin();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

	public static void playSound(String s) {
		Sound sound = Gdx.audio.newSound(Gdx.files.internal(s));
		sound.play(1f);
	}

	public Player getMe() {
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).id==me) return players.get(i);
		}
		return null;
	}

	public Player getPlayerByID(int id) {
		for(int i=0; i<players.size(); i++) {
			if(players.get(i).id==id) return players.get(i);
		}
		return null;
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

	@Override
	public boolean keyDown(int keycode) {
		if(keycode==Input.Keys.B) {
			showHitBoxes = !showHitBoxes;
		}

		if(keycode==Input.Keys.SPACE) {
			if(me==-1) {
				n = new Network(this, ip, tcp, udp);
			}
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
