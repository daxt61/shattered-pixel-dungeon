package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.DungeonGenerator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

import java.util.Arrays;

public class DungeonPreviewScene extends PixelScene {

    private String seed = "SHATTERED";
    private int depth = 1;

    private Group levelGroup;
    private Group ui;

    private StyledButton btnSeed;
    private StyledButton btnRandom;
    private StyledButton btnDepth;
    private StyledButton btnPrevDepth;
    private StyledButton btnNextDepth;
    private StyledButton btnBack;

    private Signal.Listener<KeyEvent> keyListener;
    private Signal.Listener<ScrollEvent> scrollListener;

    @Override
    public void create() {
        super.create();

        levelGroup = new Group();
        add(levelGroup);

        ui = new Group();
        ui.camera = uiCamera;
        add(ui);

        btnSeed = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Seed: " + seed) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().add(new WndTextInput("Enter Seed", "Type a seed to generate a new dungeon", seed, 20, false, "OK", "Cancel") {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive) {
                            seed = text.toUpperCase();
                            btnSeed.text("Seed: " + seed);
                            generate();
                        }
                    }
                });
            }
        };
        btnSeed.setRect(10, 10, 120, 20);
        ui.add(btnSeed);

        btnRandom = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "") {
            @Override
            protected void onClick() {
                seed = DungeonSeed.convertToCode(DungeonSeed.randomSeed());
                btnSeed.text("Seed: " + seed);
                generate();
            }
        };
        btnRandom.icon(Icons.get(Icons.SHUFFLE));
        btnRandom.setRect(132, 10, 20, 20);
        ui.add(btnRandom);

        btnDepth = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Depth: " + depth) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().add(new WndTextInput("Enter Depth", "Enter depth (1-26)", String.valueOf(depth), 2, false, "OK", "Cancel") {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive) {
                            try {
                                int newDepth = Integer.parseInt(text);
                                changeDepth(newDepth);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                });
            }
        };
        btnDepth.setRect(10, 35, 120, 20);
        ui.add(btnDepth);

        btnPrevDepth = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "-") {
            @Override
            protected void onClick() {
                changeDepth(depth - 1);
            }
        };
        btnPrevDepth.setRect(132, 35, 20, 20);
        ui.add(btnPrevDepth);

        btnNextDepth = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "+") {
            @Override
            protected void onClick() {
                changeDepth(depth + 1);
            }
        };
        btnNextDepth.setRect(154, 35, 20, 20);
        ui.add(btnNextDepth);

        btnBack = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Back") {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.switchScene(TitleScene.class);
            }
        };
        btnBack.setRect(10, 60, 120, 20);
        ui.add(btnBack);

        // Setup free camera controls
        add(new PointerArea(0, 0, Camera.main.width, Camera.main.height) {
            private PointF lastPos;

            @Override
            protected void onPointerDown(PointerEvent event) {
                lastPos = new PointF(event.current.x, event.current.y);
            }

            @Override
            protected void onDrag(PointerEvent event) {
                if (lastPos != null) {
                    Camera.main.scroll.x += (lastPos.x - event.current.x) / Camera.main.zoom;
                    Camera.main.scroll.y += (lastPos.y - event.current.y) / Camera.main.zoom;
                    lastPos.set(event.current.x, event.current.y);
                }
            }

            @Override
            protected void onPointerUp(PointerEvent event) {
                lastPos = null;
            }
        });

        KeyEvent.addKeyListener( keyListener = new Signal.Listener<KeyEvent>() {
            @Override
            public boolean onSignal( KeyEvent event ) {
                if (event.pressed) {
                    float moveStep = 16 / Camera.main.zoom;
                    switch (event.code) {
                        case com.badlogic.gdx.Input.Keys.UP:
                        case com.badlogic.gdx.Input.Keys.W:
                            Camera.main.scroll.y -= moveStep;
                            return true;
                        case com.badlogic.gdx.Input.Keys.DOWN:
                        case com.badlogic.gdx.Input.Keys.S:
                            Camera.main.scroll.y += moveStep;
                            return true;
                        case com.badlogic.gdx.Input.Keys.LEFT:
                        case com.badlogic.gdx.Input.Keys.A:
                            Camera.main.scroll.x -= moveStep;
                            return true;
                        case com.badlogic.gdx.Input.Keys.RIGHT:
                        case com.badlogic.gdx.Input.Keys.D:
                            Camera.main.scroll.x += moveStep;
                            return true;
                        case com.badlogic.gdx.Input.Keys.EQUALS:
                        case com.badlogic.gdx.Input.Keys.PLUS:
                            adjustZoom(1.2f);
                            return true;
                        case com.badlogic.gdx.Input.Keys.MINUS:
                            adjustZoom(1/1.2f);
                            return true;
                        case com.badlogic.gdx.Input.Keys.PAGE_UP:
                            changeDepth(depth - 1);
                            return true;
                        case com.badlogic.gdx.Input.Keys.PAGE_DOWN:
                            changeDepth(depth + 1);
                            return true;
                    }
                }
                return false;
            }
        } );

        ScrollEvent.addScrollListener( scrollListener = new Signal.Listener<ScrollEvent>() {
            @Override
            public boolean onSignal( ScrollEvent event ) {
                if (event.amount > 0) {
                    adjustZoom(1/1.1f);
                } else if (event.amount < 0) {
                    adjustZoom(1.1f);
                }
                return true;
            }
        });

        Camera.main.zoom(2.0f);
        generate();
    }

    @Override
    public void destroy() {
        KeyEvent.removeKeyListener( keyListener );
        ScrollEvent.removeScrollListener( scrollListener );
        super.destroy();
    }

    private void adjustZoom(float factor) {
        float newZoom = Camera.main.zoom * factor;
        if (newZoom < 0.2f) newZoom = 0.2f;
        if (newZoom > 10f) newZoom = 10f;
        Camera.main.zoom(newZoom);
    }

    private void changeDepth(int newDepth) {
        if (newDepth >= 1 && newDepth <= 26) {
            depth = newDepth;
            btnDepth.text("Depth: " + depth);
            generate();
        }
    }

    private void generate() {
        levelGroup.clear();

        Level level = DungeonGenerator.createLevel(seed, depth);
        Dungeon.level = level;

        // Ensure everything is "visited" so it shows up
        Arrays.fill(level.visited, true);

        DungeonTileSheet.setupVariance(level.length(), Dungeon.seedCurDepth());

        DungeonTerrainTilemap tiles = new DungeonTerrainTilemap();
        tiles.map(level.map, level.width());
        levelGroup.add(tiles);

        TerrainFeaturesTilemap terrainFeatures = new TerrainFeaturesTilemap(level.plants, level.traps);
        terrainFeatures.map(level.map, level.width());
        levelGroup.add(terrainFeatures);

        DungeonWallsTilemap walls = new DungeonWallsTilemap();
        walls.map(level.map, level.width());
        levelGroup.add(walls);

        for (Heap heap : level.heaps.valueList()) {
            ItemSprite sprite = new ItemSprite();
            sprite.link(heap);
            levelGroup.add(sprite);
        }

        for (Mob mob : level.mobs) {
            levelGroup.add(mob.sprite());
            mob.sprite().link(mob);
            mob.sprite().visible = true;
        }

        Camera.main.scroll.set(
            (level.width() * DungeonTilemap.SIZE - Camera.main.width / Camera.main.zoom) / 2,
            (level.height() * DungeonTilemap.SIZE - Camera.main.height / Camera.main.zoom) / 2
        );

        Sample.INSTANCE.play(Assets.Sounds.DESCEND);
    }
}
