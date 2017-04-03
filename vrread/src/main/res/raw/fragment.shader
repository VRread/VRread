precision mediump float;		// Set the default precision to medium.

uniform sampler2D u_Texture;    // The input texture.
uniform float u_Scale;			// Scale of the texture.
uniform vec2 u_Offset;			// uv Offset of the texture.

varying vec2 v_TexCoordinate;


// The entry point for our fragment shader.
void main()
{
	vec4 texColor = texture2D(u_Texture, (u_Scale * v_TexCoordinate) + u_Offset);
	//gl_FragColor = texColor;

	if(texColor.a == 0.0) {
		gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
	} else {
		gl_FragColor = texColor;
	}
}
