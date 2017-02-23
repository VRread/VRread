package de.fraunhofer.ipa.vrread;

import de.fraunhofer.ipa.vrread.datasource.Datasource;
import de.fraunhofer.ipa.vrread.graphics.ScrollingTextureShader;

/**
 * The read controller is holding the current reading position and reacts upon control requests. It will calculate a new
 * reading position, see if it can just correct the visual appearance at the shader or if it needs to send a whole new
 * texture to the the rendering shader.
 * <p>
 * Created by Thomas Felix on 23.02.2017.
 */

public class ReadController {

	private ScrollingTextureShader textShader;
	private Datasource datasource;

	/**
	 * Sets the datasource to a new one. This will usually reset the current reading position.
	 *
	 * @param datasource The new datasource which will be queried for rendered textures.
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

}
