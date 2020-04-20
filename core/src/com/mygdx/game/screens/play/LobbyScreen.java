package com.mygdx.game.screens.play;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.InversePacman;
import com.mygdx.game.components.ButtonComponent;
import com.mygdx.game.components.TableComponent;
import com.mygdx.game.components.TextureComponent;
import com.mygdx.game.components.TransformComponent;
import com.mygdx.game.managers.EntityManager;
import com.mygdx.game.managers.GameScreenManager;
import com.mygdx.game.managers.NetworkManager;
import com.mygdx.game.screens.AbstractScreen;
import com.mygdx.game.systems.ButtonSystem;
import com.mygdx.game.systems.MusicSystem;
import com.mygdx.game.systems.RenderingSystem;
import com.mygdx.game.systems.TableSystem;

public class LobbyScreen extends AbstractScreen {
    private OrthographicCamera camera;
    private FitViewport viewport;

    private EntityManager entityManager;
    private NetworkManager networkManager;

    private SpriteBatch batch;
    private SpriteBatch bgBatch;

    private Engine engine;
    private Vector3 clickPosition;

    private ButtonSystem buttonSystem;
    private TableSystem tableSystem;
    private RenderingSystem renderingSystem;
    private MusicSystem musicSystem;

    private TextureRegion play;
    private TextureRegion multiplay;
    private TextureRegion highscore;
    private TextureRegion option;
    private TextureRegion bg;

    private TextureRegion ellipse;
    private TextureRegion front_ellipse;

    private Entity joinLobbyButton;
    private Entity createLobbyButton;
    private Entity multiplayerButton;
    private Entity highscoreButton;
    private Entity optionButton;
    private Entity ellipseEntity;
    private Entity front_ellipseEntity;
    private Entity bgEntity;
    private Entity tbEntity;


    private Sprite playsprite;
    private Sprite multisprite;
    private Sprite highscoresprite;
    private Sprite optionsprite;
    private Sprite ellipseSprite;
    private Sprite front_ellipseSprite;
    private Sprite bgSprite;



    private float scaleX;
    private float scaleY;

    private BitmapFont font;
    private GlyphLayout layout;

    public LobbyScreen(final InversePacman app) {
        super(app);

        /*this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, InversePacman.V_WIDTH, InversePacman.V_HEIGHT);*/
        networkManager = new NetworkManager();

        bg = new TextureRegion(new Texture("lobbyscreen/lobbyScreen.png"));
        play = new TextureRegion(new Texture("menuscreen/play.png"));
        multiplay = new TextureRegion(new Texture("menuscreen/multi.png"));
        highscore = new TextureRegion(new Texture("menuscreen/high.png"));
        option = new TextureRegion(new Texture("menuscreen/options.png"));

        ellipse = new TextureRegion(new Texture("menuscreen/ellipse_color_change_correct.png"));
        front_ellipse = new TextureRegion(new Texture("optionscreen/option_front_ellipse.png"));

        font = new BitmapFont(Gdx.files.internal("font/rubik_font_correct.fnt"));
        layout = new GlyphLayout(); //dont do this every frame! Store it as member

        scaleX = Gdx.graphics.getWidth() / (float)app.APP_WIDTH_MOBILE;
        scaleY = Gdx.graphics.getHeight() / (float)app.APP_HEIGHT_MOBILE;


    }

    @Override
    public void render(float delta){
        super.render(delta);
        engine.update(delta);
        /*batch.begin();
        font.setUseIntegerPositions(false);
        font.getData().setScale(scaleX / 32f, scaleY / 32f);
        layout.setText(font,"MENU");

        font.draw(batch,"menu", (Gdx.graphics.getWidth() / 64f - layout.width / 2f),
                (Gdx.graphics.getHeight() / (1.05f * 32f) - (layout.height / 2f)));

        batch.end();*/
    }



    @Override
    public void update(float delta) {
        handleInput();

    }

    public void handleInput() {
        String nickname = "PepsiCoke";
        String playerType = "pacman";
        String joinLobbyName = "Lobby1";


        if(joinLobbyButton.flags == 1) {
            System.out.println("JoinLobby pressed!");
            networkManager.joinLobby(joinLobbyName, nickname, playerType);
            joinLobbyButton.flags = 0;
        }
        if(createLobbyButton.flags == 1) {
            System.out.println("CreateLobby pressed!");
            networkManager.createLobby(nickname, playerType);
            createLobbyButton.flags = 0;
        }

    }

    @Override
    public void show() {
        this.camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        bgBatch = new SpriteBatch();

        ellipseSprite = new Sprite(ellipse);

        buttonSystem = new ButtonSystem(camera);
        tableSystem = new TableSystem();
        renderingSystem = new RenderingSystem(batch);
        musicSystem = new MusicSystem(Gdx.files.internal("music/menu"));


        engine = new Engine();
        engine.addSystem(buttonSystem);
        engine.addSystem(tableSystem);
        engine.addSystem(musicSystem);
        engine.addSystem(renderingSystem);

        bgSprite = new Sprite(bg);

        bgEntity = new Entity();

        //Don't know why but the background doesn't surround the whole screen, therefore I added some +/- on the parameters
        bgEntity.add(new TextureComponent(bgSprite, 0, -2, Gdx.graphics.getWidth() +2,
                Gdx.graphics.getHeight() +2,false,false))
                .add(new TransformComponent(0,0));
        engine.addEntity(bgEntity);

        tbEntity = new Entity();
        tbEntity.add(new TableComponent());
        engine.addEntity(tbEntity);


        playsprite = new Sprite(play);

        joinLobbyButton = new Entity();
        joinLobbyButton.add(new TextureComponent(playsprite,
                (Gdx.graphics.getWidth() / 2 - (playsprite.getRegionWidth() / 2 * scaleX)) * 0.2f,
                Gdx.graphics.getHeight() / 20f, playsprite.getRegionWidth() * scaleX ,
                playsprite.getRegionHeight() * scaleY, false, false))
                .add(new ButtonComponent((Gdx.graphics.getWidth() / 2 - playsprite.getRegionWidth() / 2 * scaleX) * 0.2f,
                        Gdx.graphics.getHeight() / 20f, playsprite.getRegionWidth() * scaleX , playsprite.getRegionHeight() * scaleY))
                .add(new TransformComponent((Gdx.graphics.getWidth() / 2 - playsprite.getRegionWidth() / 2 * scaleX) * 0.2f,
                        Gdx.graphics.getHeight() / 20f));
        engine.addEntity(joinLobbyButton);

        createLobbyButton = new Entity();
        createLobbyButton.add(new TextureComponent(playsprite,
                (Gdx.graphics.getWidth() / 2 - (playsprite.getRegionWidth() / 2 * scaleX)) * 1.8f,
                Gdx.graphics.getHeight() / 20f, playsprite.getRegionWidth() * scaleX,
                playsprite.getRegionHeight() * scaleY, false, false))
                .add(new ButtonComponent((Gdx.graphics.getWidth() / 2 - (playsprite.getRegionWidth() / 2 * scaleX)) * 1.8f, Gdx.graphics.getHeight() / 20f, playsprite.getRegionWidth() * scaleX, playsprite.getRegionHeight() * scaleY))
                .add(new TransformComponent((Gdx.graphics.getWidth() / 2 - (playsprite.getRegionWidth() / 2 * scaleX)) * 1.8f, Gdx.graphics.getHeight() / 20f));
        engine.addEntity(createLobbyButton);


    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose(){
        super.dispose();
    }
}
