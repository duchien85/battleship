package dev.ky3he4ik.battleship.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.ky3he4ik.battleship.World;
import dev.ky3he4ik.battleship.ai.AI;
import dev.ky3he4ik.battleship.ai.AIDummy;
import dev.ky3he4ik.battleship.logic.GameConfig;
import dev.ky3he4ik.battleship.logic.PlayerFinished;
import dev.ky3he4ik.battleship.utils.Constants;

public class GameStage extends Stage implements PlayerFinished {
    public final static int TURN_LEFT = 0;
    public final static int TURN_RIGHT = 1;

    @NotNull
    private Field leftPlayer;
    @NotNull
    private Field rightPlayer;
    @NotNull
    private final GameConfig config;

    @Nullable
    private AI ai = null;

    private int turn = TURN_LEFT;
    private int readyCnt = 0;
    private boolean aiReady = false;
    private int aiX = -1;
    private int aiY = -1;

    GameStage(@NotNull final GameConfig config, final World leftWorld, final World rightWorld) {
        super(new ExtendViewport(Constants.APP_WIDTH, Constants.APP_HEIGHT));
        this.config = config;
        float cellSize = Math.min(getWidth() / (config.getWidth() * 2 + 4), getHeight() / (config.getHeight() + 3));
        float redundantX = (getWidth() - cellSize * (config.getWidth() * 2 + 4)) / 2;
        float redundantY = (getHeight() - cellSize * (config.getHeight() + 3)) / 2;

        Gdx.app.debug("GameStage/init", "cellSize = " + cellSize);

        leftPlayer = new Field(leftWorld, cellSize, config.getGameType() != GameConfig.GameType.LOCAL_2P);
        rightPlayer = new Field(rightWorld, cellSize, false);

        leftPlayer.setPosition(redundantX + cellSize, redundantY + cellSize);
        leftPlayer.setSize(cellSize * config.getWidth(), cellSize * config.getHeight());
        leftPlayer.setVisible(true);
        addActor(leftPlayer);

        rightPlayer.setPosition(redundantX + cellSize * (config.getWidth() + 3), redundantY + cellSize);
        rightPlayer.setSize(cellSize * config.getWidth(), cellSize * config.getHeight());
        rightPlayer.setVisible(true);
        addActor(rightPlayer);

        if (config.getGameType() == GameConfig.GameType.AI) {
            ai = new AIDummy(this, leftPlayer.getWorld(), rightPlayer.getWorld(), TURN_RIGHT);
            ai.start();
            ai.setPlaceShips();
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (aiReady && turn == TURN_RIGHT) {
            aiReady = false;
            if (leftPlayer.open(aiX, aiY))
                turn();
            if (ai != null)
                ai.setTurn();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        leftPlayer.dispose();
        leftPlayer.clearActions();
        leftPlayer.clearListeners();
        rightPlayer.clearActions();
        rightPlayer.clearListeners();
        rightPlayer.dispose();
    }

    public void resize(int width, int height) {
        getViewport().update(width, height, true);
        float cellSize = Math.min(getWidth() / (config.getWidth() * 2 + 4), getHeight() / (config.getHeight() + 3));
        float redundantX = (getWidth() - cellSize * (config.getWidth() * 2 + 4)) / 2;
        float redundantY = (getHeight() - cellSize * (config.getHeight() + 3)) / 2;

        leftPlayer.setPosition(redundantX + cellSize, redundantY + cellSize);
        leftPlayer.setSize(cellSize * config.getWidth(), cellSize * config.getHeight());
        rightPlayer.setPosition(redundantX + cellSize * (config.getWidth() + 3), redundantY + cellSize);
        rightPlayer.setSize(cellSize * config.getWidth(), cellSize * config.getHeight());
    }

    @Override
    public void turnFinished(int playerId, int i, int j) {
        if (isMyTurn(playerId)) {
            if (getOpponent(playerId).open(i, j))
                turn();
        } else if (playerId == TURN_RIGHT && config.getGameType() == GameConfig.GameType.AI) {
            aiReady = true;
            aiX = i;
            aiY = j;
        }
    }

    @Override
    public void shipsPlaced(int playerId) {
        readyCnt++;
    }

    public boolean isMyTurn(int playerId) {
        return playerId == turn;
    }

    private void turn() {
        if (turn == TURN_LEFT)
            turn = TURN_RIGHT;
        else
            turn = TURN_LEFT;
    }

    private Field getPlayer(int playerId) {
        if (playerId == TURN_LEFT)
            return rightPlayer;
        else
            return leftPlayer;
    }

    private Field getOpponent(int playerId) {
        if (playerId == TURN_LEFT)
            return leftPlayer;
        else
            return rightPlayer;
    }
}
