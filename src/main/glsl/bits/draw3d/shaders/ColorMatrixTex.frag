#version 330

uniform sampler2D TEX_UNIT0;
uniform mat4 COLOR_MAT;

smooth in vec2 tex0;
out vec4 fragColor;

void main()
{
	vec4 c = texture( TEX_UNIT0, tex0 );
	fragColor = COLOR_MAT * c;
	if( fragColor.a <= 0.0 ) {
		discard;
	}
}
