precision mediump float;		// Set the default precision to medium.

uniform sampler2D u_Texture;    // The input texture.
uniform float u_Scale;			// Scale of the texture.
uniform vec2 u_Offset;			// uv Offset of the texture.
uniform int u_ContrastMode;		// 0: Normal 1: Inverted, 2: YellowGreen

varying vec2 v_TexCoordinate;

// Background color could be implemented as a uniform for external manipulation.
// currently its only white.
vec4 bgColor = vec4(1.0);
vec4 yellowColor = vec4(1.0, 1.0, 0.0, 1.0);
vec4 blueColor = vec4(0.0, 0.0, 1.0, 1.0);

// The entry point for our fragment shader.
void main()
{
	vec4 texColor = texture2D(u_Texture, (u_Scale * v_TexCoordinate) + u_Offset);
	vec4 blendColor = vec4(texColor.a) * texColor + vec4(1.0 - texColor.a) * bgColor;

	if(u_ContrastMode == 0) {
		// Normal mode.
		gl_FragColor = blendColor;
	} else if(u_ContrastMode == 1) {
		// Inverted colors.
		gl_FragColor = vec4(1.0 - blendColor.r, 1.0 - blendColor.g, 1.0 - blendColor.b, 1.0);
	} else if(u_ContrastMode == 2) {
		// Black/Yellow
		// White index gives us a indicator how much we want to see a yellow color.
		float whiteIndex = (blendColor.r + blendColor.g + blendColor.b) / 3.0;
      	gl_FragColor = yellowColor * (1.0 - whiteIndex);
      	gl_FragColor.a = 1.0;
      	//gl_FragColor = yellowColor * whiteIndex;
	} else if(u_ContrastMode == 3) {
      	// Blue/Yellow
      	float whiteIndex = (blendColor.r + blendColor.g + blendColor.b) / 3.0;
      	gl_FragColor = yellowColor * whiteIndex + blueColor * (1.0 - whiteIndex);
    } else {
    	// Not implemented.
    	gl_FragColor = vec4(0, 1.0, 0, 1.0);
    }
}
