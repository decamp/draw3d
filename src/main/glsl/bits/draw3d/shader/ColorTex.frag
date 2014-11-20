#version 330

uniform sampler2D TEX_UNIT0;

smooth in vec4 color;
smooth in vec4 tex0;

out vec4 fragColor;


vec4 sampleMipmap( sampler2D unit, in vec2 tex ) {
	vec2 size = textureSize( unit, 0 );
	vec2 dx = dFdx( tex ) * size;
	vec2 dy = dFdy( tex ) * size;
	float level = max( dot( dx, dx ), dot( dy, dy ) );
	level = 0.5 * log2( level );	
	return textureLod( unit, tex.st, level );
}

void main() {
	fragColor = color * sampleMipmap( TEX_UNIT0, tex0.st );
	if( fragColor.a <= 0.0 ) {
		discard;
	}
}
