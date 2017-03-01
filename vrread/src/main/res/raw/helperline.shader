precision mediump float;

uniform float u_Size;			// Size of the line in percent.

varying vec2 v_TexCoordinate;

const float THICKNESS = 0.01;

/**
 * The helper line will draw a red line to the center of the the fragment.
 * All other parts of the fragement will be transparent.
 */
void main()
{
	// Check where we currently are.
	if(v_TexCoordinate.y > (0.5 - THICKNESS / 2.0) && v_TexCoordinate.y < (0.5 + THICKNESS / 2.0)) {
		gl_FragColor = vec4(1, 0, 0, 1);
	} else {
		gl_FragColor = vec4(0, 0, 0, 0);
	}
}
