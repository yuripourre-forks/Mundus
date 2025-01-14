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

package com.mbrlabs.mundus.commons.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.mbrlabs.mundus.commons.Scene;

/**
 * @author Marcus Brummer
 * @version 16-01-2016
 */
public class SceneGraph {

    protected GameObject root;

    public Scene scene;

    private GameObject selected;

    public SceneGraph(Scene scene) {
        root = new GameObject(this, null, -1);
        root.initChildrenArray();
        root.active = false;
        this.scene = scene;
    }

    public void render(float delta) {
        for (GameObject go : root.getChildren()) {
            go.render(delta);
        }
    }

    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    public void update(float delta) {
        for (GameObject go : root.getChildren()) {
            go.update(delta);
        }
    }

    public Array<GameObject> getGameObjects() {
        return root.getChildren();
    }

    public void addGameObject(GameObject go) {
        root.addChild(go);
    }

    public GameObject getSelected() {
        return selected;
    }

    public void setSelected(GameObject selected) {
        this.selected = selected;
    }

}
