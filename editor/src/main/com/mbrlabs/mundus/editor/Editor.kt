/*
 * Copyright (c) 2016. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.editor

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.utils.GdxRuntimeException
import com.mbrlabs.mundus.editor.core.project.ProjectContext
import com.mbrlabs.mundus.editor.core.project.ProjectManager
import com.mbrlabs.mundus.editor.core.registry.Registry
import com.mbrlabs.mundus.editor.events.ProjectChangedEvent
import com.mbrlabs.mundus.editor.events.SceneChangedEvent
import com.mbrlabs.mundus.editor.input.FreeCamController
import com.mbrlabs.mundus.editor.input.InputManager
import com.mbrlabs.mundus.editor.input.ShortcutController
import com.mbrlabs.mundus.editor.shader.Shaders
import com.mbrlabs.mundus.editor.tools.ToolManager
import com.mbrlabs.mundus.editor.ui.UI
import com.mbrlabs.mundus.editor.utils.Compass
import com.mbrlabs.mundus.editor.utils.GlUtils
import com.mbrlabs.mundus.editor.utils.UsefulMeshs
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

/**
 * @author Marcus Brummer
 * @version 07-06-2016
 */
class Editor : Lwjgl3WindowAdapter(), ApplicationListener,
        ProjectChangedEvent.ProjectChangedListener,
        SceneChangedEvent.SceneChangedListener {

    private lateinit var axesInstance: ModelInstance
    private lateinit var compass: Compass

    private lateinit var camController: FreeCamController
    private lateinit var shortcutController: ShortcutController
    private lateinit var inputManager: InputManager
    private lateinit var projectManager: ProjectManager
    private lateinit var registry: Registry
    private lateinit var toolManager: ToolManager

    override fun create() {
        Mundus.registerEventListener(this)

        camController = Mundus.inject()
        shortcutController = Mundus.inject()
        inputManager = Mundus.inject()
        projectManager = Mundus.inject()
        registry = Mundus.inject()
        toolManager = Mundus.inject()
        setupInput()

        // TODO dispose this
        val axesModel = UsefulMeshs.createAxes()
        axesInstance = ModelInstance(axesModel)

        // open last edited project or create default project
        var context: ProjectContext? = projectManager.loadLastProject()
        if (context == null) {
            context = createDefaultProject()
        }

        if(context == null) {
            throw GdxRuntimeException("Couldn't open a project")
        }

        compass = Compass(context.currScene.cam)

        // change project; this will fire a ProjectChangedEvent
        projectManager.changeProject(context)
    }

    private fun setupInput() {
        // NOTE: order in wich processors are added is important: first added,
        // first executed!
        inputManager.addProcessor(shortcutController)
        inputManager.addProcessor(UI)
        // when user does not click on a ui element -> unfocus UI
        inputManager.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                UI.unfocusAll()
                return false
            }
        })
        inputManager.addProcessor(toolManager)
        inputManager.addProcessor(camController)
        toolManager.setDefaultTool()
    }

    private fun setupSceneWidget() {
        val batch = Mundus.inject<ModelBatch>()
        val context = projectManager.current()
        val scene = context.currScene
        val sg = scene.sceneGraph

        UI.sceneWidget.setCam(context.currScene.cam)
        UI.sceneWidget.setRenderer {
            if (scene.skybox != null) {
                batch.begin(scene.cam)
                batch.render(scene.skybox.skyboxInstance, scene.environment, Shaders.skyboxShader)
                batch.end()
            }

            sg.update()
            sg.render(Gdx.graphics.deltaTime)

            toolManager.render()
            compass.render(batch)
        }

        compass.setWorldCam(context.currScene.cam)
        camController.setCamera(context.currScene.cam)
        UI.sceneWidget.setCam(context.currScene.cam)
        context.currScene.viewport = UI.sceneWidget.viewport
    }

    override fun render() {
        GlUtils.clearScreen(Color.WHITE)
        UI.act()
        camController.update()
        toolManager.act()
        UI.draw()
    }

    override fun onProjectChanged(event: ProjectChangedEvent) {
        setupSceneWidget()
    }

    override fun onSceneChanged(event: SceneChangedEvent) {
        setupSceneWidget()
    }

    private fun createDefaultProject(): ProjectContext? {
        if (registry.lastOpenedProject == null || registry.projects.size == 0) {
            val name = "Default Project"
            var path = FileUtils.getUserDirectoryPath()
            path = FilenameUtils.concat(path, "MundusProjects")

            return projectManager.createProject(name, path)
        }

        return null
    }

    override fun closeRequested(): Boolean {
        UI.showDialog(UI.exitDialog)
        return false
    }

    override fun resize(width: Int, height: Int) {
        UI.viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}

    override fun dispose() {
        Mundus.dispose()
    }

}
