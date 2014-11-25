#version 330

smooth in vec4 color;

out vec4 fragColor;

void main()  {
	fragColor = color;
	if( color.a <= 0.0 ) {
		discard;
	}
}
