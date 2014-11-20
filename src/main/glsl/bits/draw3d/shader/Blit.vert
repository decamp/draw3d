#version 330

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec2 inTex0;

smooth out vec2 tex0;

void main() {
	gl_Position = inVert;
	tex0 = inTex0;
}
