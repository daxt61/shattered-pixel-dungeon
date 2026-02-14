package com.shatteredpixel.shatteredpixeldungeon.scenes;

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
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.PointF;

import java.util.Arrays;

public class DungeonPreviewScene extends PixelScene {

    private String seed = "12345678";
    private int depth = 1;

    private Group levelGroup;
    private Group ui;

    private StyledButton btnSeed;
    private StyledButton btnDepth;
    private StyledButton btnZoomIn;
    private StyledButton btnZoomOut;
    private StyledButton btnBack;

    @Override
    public void create() {
        super.create();

        levelGroup = new Group();
        add(levelGroup);

        btnSeed = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Seed: " + seed) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().add(new WndTextInput("Enter Seed", "Type a seed to generate a new dungeon", seed, 20, false, "OK", "Cancel") {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive) {
                            seed = text;
                            btnSeed.text("Seed: " + seed);
                            generate();
                        }
                    }
                });
            }
        };
        btnSeed.setRect(10, 10, 120, 20);
        ui.add(btnSeed);

        btnDepth = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Depth: " + depth) {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.scene().add(new WndTextInput("Enter Depth", "Enter depth (1-26)", String.valueOf(depth), 2, false, "OK", "Cancel") {
                    @Override
                    public void onSelect(boolean positive, String text) {
                        if (positive) {
                            try {
                                int newDepth = Integer.parseInt(text);
                                if (newDepth >= 1 && newDepth <= 26) {
                                    depth = newDepth;
                                    btnDepth.text("Depth: " + depth);
                                    generate();
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                });
            }
        };
        btnDepth.setRect(10, 35, 120, 20);
        ui.add(btnDepth);

        btnZoomIn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "+ Zoom") {
            @Override
            protected void onClick() {
                Camera.main.zoom(Camera.main.zoom * 1.2f);
            }
        };
        btnZoomIn.setRect(10, 60, 60, 20);
        ui.add(btnZoomIn);

        btnZoomOut = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "- Zoom") {
            @Override
            protected void onClick() {
                Camera.main.zoom(Camera.main.zoom / 1.2f);
            }
        };
        btnZoomOut.setRect(70, 60, 60, 20);
        ui.add(btnZoomOut);

        btnBack = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Back") {
            @Override
            protected void onClick() {
                ShatteredPixelDungeon.switchScene(TitleScene.class);
            }
        };
        btnBack.setRect(10, 85, 120, 20);

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

        ui = new Group();
        ui.camera = uiCamera;
        add(ui);
        ui.add(btnSeed);
        ui.add(btnDepth);
        ui.add(btnZoomIn);
        ui.add(btnZoomOut);
        ui.add(btnBack);

        Camera.main.zoom(2.0f);
        generate();
    }

    private void generate() {
        levelGroup.clear();

        Level level = DungeonGenerator.createLevel(seed, depth);

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
    }
}
