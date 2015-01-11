/**
 * Copyright 2014-2015 Pawel Pastuszak
 * 
 * This file is part of VisEditor.
 * 
 * VisEditor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VisEditor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with VisEditor.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.kotcrab.vis.editor.ui.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.kotcrab.vis.ui.VisTable;
import pl.kotcrab.vis.editor.module.project.ProjectModuleContainer;
import pl.kotcrab.vis.editor.module.project.TextureCacheModule;
import pl.kotcrab.vis.editor.module.scene.*;
import pl.kotcrab.vis.editor.ui.tab.DragAndDropTarget;
import pl.kotcrab.vis.editor.ui.tab.Tab;
import pl.kotcrab.vis.editor.ui.tab.TabViewMode;

public class SceneTab extends Tab implements DragAndDropTarget {
	private EditorScene scene;

	private TextureCacheModule cacheModule;

	private SceneModuleContainer sceneMC;
	private CameraModule cameraModule;

	private VisTable content;
	private VisTable leftColumn;
	private VisTable rightColumn;

	// private SceneOutline outline;
	private ActorProperites actorProperties;

	private Target dropTarget;

	public SceneTab (EditorScene scene, ProjectModuleContainer projectMC) {
		this.scene = scene;

		cacheModule = projectMC.get(TextureCacheModule.class);

		sceneMC = new SceneModuleContainer(projectMC);

		// outline = new SceneOutline();
		actorProperties = new ActorProperites();

		content = new ContentTable();

		leftColumn = new VisTable(false);
		rightColumn = new VisTable(false);

		leftColumn.top();
		rightColumn.top();

		// dummy widgets allows content table to get input events
		content.add(leftColumn).width(300).fillY().expandY();
		content.add(new Widget()).fill().expand();
		content.add(rightColumn).width(300).fillY().expandY();

		leftColumn.top();
		// leftColumn.add(outline).height(300).fillX().expandX();
		leftColumn.row();
		leftColumn.add(new Widget()).fill().expand();

		rightColumn.top();
		rightColumn.add(actorProperties).height(300).expandX().fillX();
		rightColumn.row();
		rightColumn.add(new Widget()).fill().expand();

		sceneMC.add(new CameraModule());
		sceneMC.add(new RendererModule());
		sceneMC.add(new GridRendererModule());
		sceneMC.init();

		cameraModule = sceneMC.get(CameraModule.class);

		dropTarget = new Target(content) {
			@Override
			public void drop (Source source, Payload payload, float x, float y, int pointer) {
				dropped(payload);
			}

			@Override
			public boolean drag (Source source, Payload payload, float x, float y, int pointer) {
				return true;
			}
		};
	}

	private void dropped (Payload payload) {
		TextureRegion region = (TextureRegion)payload.getObject();

		Sprite sprite = new Sprite(region);
		sprite.setPosition(cameraModule.getInputX() - sprite.getWidth() / 2, cameraModule.getInputY() - sprite.getHeight() / 2);

		scene.objects.add(new Object2d(cacheModule.getRelativePath(region), sprite));
	}

	private void resize () {
		sceneMC.resize();
	}

	@Override
	public void render (Batch batch) {
		Color oldColor = batch.getColor().cpy();
		batch.setColor(1, 1, 1, 1);
		batch.begin();

		sceneMC.render(batch);
		for (SceneObject obj : scene.objects) {
			Object2d obj2d = (Object2d)obj;
			obj2d.sprite.draw(batch);
		}

		batch.end();
		batch.setColor(oldColor);
	}

	@Override
	public String getButtonText () {
		return scene.getFile().name();
	}

	@Override
	public Table getContentTable () {
		return content;
	}

	@Override
	public Target getDropTarget () {
		return dropTarget;
	}

	@Override
	public float getCameraZoom () {
		return cameraModule.getZoom();
	}

	@Override
	public TabViewMode getViewMode () {
		return TabViewMode.WITH_PROJECT_ASSETS_MANAGER;
	}

	public EditorScene getScene () {
		return scene;
	}

	private class ContentTable extends VisTable {
		public ContentTable () {
			super(false);
			addListener(new SceneInputListener(this, sceneMC));
		}

		@Override
		protected void sizeChanged () {
			super.sizeChanged();
			sceneMC.resize();
			resize();
		}
	}
}
