precision mediump float;

uniform float u_Size;			// Size of the line in percent.

varying vec2 v_TexCoordinate;

/**
 * The helper line will draw a red line to the center of the the fragment.
 * All other parts of the fragement will be transparent.
 */
void main()
{
	// Check where we currently are.
	if(v_TexCoordinate[0] > 0.5f) {
		gl_FragColor = vec4(1f, 0f, 0f, 1f);
	} else {
		gl_FragColor = vec4(0f, 0f, 0f, 0f);
	}
}
