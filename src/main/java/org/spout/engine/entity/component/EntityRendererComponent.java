/*
 * This file is part of Spout.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Spout is licensed under the Spout License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spout is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.engine.entity.component;

import org.spout.api.Client;
import org.spout.api.Spout;
import org.spout.api.component.impl.ModelComponent;
import org.spout.api.component.type.EntityComponent;
import org.spout.api.math.Matrix;
import org.spout.api.render.Camera;
import org.spout.api.render.RenderMaterial;
import org.spout.api.render.effect.SnapshotEntity;
import org.spout.engine.mesh.BaseMesh;

public class EntityRendererComponent extends EntityComponent {
	
	private ModelComponent modelComponent;
	private SpoutAnimationComponent animationComponent;
	private ClientTextModelComponent textModelComponent;
	
	private RenderMaterial renderMaterial;
	private BaseMesh mesh;
	
	private boolean initialized = false;
	
	public void init(){
		modelComponent = getOwner().get(ModelComponent.class);
		animationComponent = getOwner().get(SpoutAnimationComponent.class);
		textModelComponent = getOwner().get(ClientTextModelComponent.class);

		if (modelComponent == null) {
			throw new IllegalStateException("Entity must have a ModelComponent to use EntityRendererComponent");
		}
		
		if (modelComponent.getModel() == null) {
			throw new IllegalStateException("Entity must have a model to use EntityRendererComponent");
		}
		
		if (modelComponent.getModel().getMesh() == null) {
			throw new IllegalStateException("Entity must have a mesh to use EntityRendererComponent");
		}
		
		renderMaterial = modelComponent.getModel().getRenderMaterial();
		mesh = (BaseMesh) modelComponent.getModel().getMesh();
		
		batch();
		initialized = true;
	}
	
	private void batch(){
		if(animationComponent != null){
			animationComponent.batchSkeleton(mesh);
		}

		mesh.batch();
	}

	public void update(float dt) {
		if(!initialized)
			init();
		
		if(animationComponent != null)
			animationComponent.updateAnimation(dt);
	}

	public void render() {
		Camera camera = ((Client)Spout.getEngine()).getActiveCamera();
		Matrix modelMatrix = getOwner().getScene().getRenderTransform().toMatrix();

		renderMaterial.getShader().setUniform("View", camera.getView());
		renderMaterial.getShader().setUniform("Projection", camera.getProjection());
		renderMaterial.getShader().setUniform("Model", modelMatrix);

		if(animationComponent != null){
			animationComponent.render();
		}

		SnapshotEntity snapshot = new SnapshotEntity(renderMaterial, getOwner());

		renderMaterial.preRenderEntity(snapshot);
		mesh.render(renderMaterial);
		renderMaterial.postRenderEntity(snapshot);

		if (textModelComponent != null) {
			textModelComponent.render(camera);
		}
	}
}
