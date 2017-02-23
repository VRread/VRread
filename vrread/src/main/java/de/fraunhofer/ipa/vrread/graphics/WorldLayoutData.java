/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fraunhofer.ipa.vrread.graphics;

/**
 * Contains vertex, normal and color data.
 */
public final class WorldLayoutData {

	/**
	 * Priv. ctor so the class can only be used in static fashon.
	 */
	private WorldLayoutData() {
		// no op.
	}

	// The grid lines on the floor are rendered procedurally and large polygons cause floating point
	// precision problems on some architectures. So we split the floor into 4 quadrants.
	public static final float[] PLANE_COORDS = new float[]{
			// +X, +Y quadrant
			2, -2, 0,
			-2, -2, 0,
			-2, 2, 0,
			2, -2, 0,
			-2, 2, 0,
			2, 2, 0};


	// S, T (or X, Y)
	// Texture coordinate data.
	// Because images have a Y axis pointing downward (values increase as you move down the image) while
	// OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
	// What's more is that the texture coordinates are the same for every face.
	public static final float[] PLANE_TEX_CORDS = new float[]{
			// Front face
			1.0f, 1.0f,
			0.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f};
}
