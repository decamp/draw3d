#version 330

uniform sampler2D COLOR_UNIT;
uniform sampler2D DEPTH_UNIT;

smooth in vec2 tex0;
out vec4 fragColor;

void main()
{
	fragColor = texture( COLOR_UNIT, tex0 );
	gl_FragDepth = texture( DEPTH_UNIT, tex0 ).r;
}
