#version 330

uniform sampler2D TEX_UNIT;
uniform float GAIN;

smooth in vec2 tex0;
out vec4 fragColor;

void main()
{
	vec4 c = texture( TEX_UNIT, tex0 );
	fragColor = c * vec4( GAIN, GAIN, GAIN, 1 );
}
