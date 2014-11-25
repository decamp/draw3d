#version 330

#define ENABLE_TEX 1

uniform sampler1D TEX_UNIT0;

smooth in vec4 color;
smooth in vec2 tex0;

out vec4 fragColor;


void main()  {
	#if ENABLE_TEX
		fragColor = color * texture( TEX_UNIT0, tex0.x );
	#else 
		fragColor = color;
	#endif

	if( color.a <= 0.0 ) {
		discard;
	}
}
