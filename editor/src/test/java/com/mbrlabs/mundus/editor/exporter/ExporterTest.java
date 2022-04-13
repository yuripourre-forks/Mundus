package com.mbrlabs.mundus.editor.exporter;

import com.badlogic.gdx.utils.JsonWriter;
import com.mbrlabs.mundus.commons.dto.GameObjectDTO;
import com.mbrlabs.mundus.commons.dto.SceneDTO;
import com.mbrlabs.mundus.commons.dto.TerrainComponentDTO;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.editor.core.kryo.KryoManager;
import com.mbrlabs.mundus.editor.core.project.ProjectContext;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ExporterTest {

    private Exporter exporter;

    @Before
    public void setUp() {
        KryoManager manager = mock(KryoManager.class);
        ProjectContext context = mock(ProjectContext.class);

        exporter = new Exporter(manager, context);
    }

    @Test
    public void testExportEmptyScene() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);

        SceneDTO scene = new SceneDTO();
        exporter.exportScene(scene, writer, JsonWriter.OutputType.json);

        String result = baos.toString();
        assertEquals("{\"id\":0,\"name\":null,\"gos\":[]}", result);
    }

    @Test
    public void testExportSingleTerrainScene() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);

        SceneDTO scene = new SceneDTO();

        List<GameObjectDTO> terrains = new ArrayList<>();
        terrains.add(buildTerrain("Terrain 1"));
        scene.setTerrains(terrains);

        exporter.exportScene(scene, writer, JsonWriter.OutputType.json);

        String result = baos.toString();
        assertEquals("{\"id\":0,\"name\":null,\"gos\":[],\"ter\":[{\"i\":0,\"n\":\"Terrain 1\",\"a\":false,\"t\":[0,0,0,0,0,0,0,0,0,0],\"g\":[\"grass\"]}]}", result);
    }

    private GameObjectDTO buildTerrain(String name) {
        GameObjectDTO terrain = new GameObjectDTO();
        terrain.setName(name);

        TerrainComponentDTO terrainComponent = new TerrainComponentDTO();
        terrain.setTerrainComponent(terrainComponent);

        List<String> tags = new ArrayList<>();
        tags.add("grass");
        terrain.setTags(tags);

        return terrain;
    }

}
