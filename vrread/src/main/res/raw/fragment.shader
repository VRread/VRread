precision mediump float;		// Set the default precision to medium.

uniform sampler2D u_Texture;    // The input texture.
uniform float u_Scale;			// Scale of the texture.
uniform vec2 u_Offset;			// uv Offset of the texture.

varying vec2 v_TexCoordinate;


// The entry point for our fragment shader.
void main()
{
	// Background color could be implemented as a uniform for external manipuliation.
	vec4 bgColor = vec4(1.0);
	vec4 texColor = texture2D(u_Texture, (u_Scale * v_TexCoordinate) + u_Offset);

	gl_FragColor = vec4(texColor.a) * texColor + vec4(1.0 - texColor.a) * bgColor;
}
