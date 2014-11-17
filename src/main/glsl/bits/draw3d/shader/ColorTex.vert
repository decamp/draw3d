#version 330

uniform mat4 PROJ_VIEW_MAT;

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec4 inColor;
layout( location = 2 ) in vec4 inTex0;

smooth out vec4 color;
smooth out vec4 tex0;

void main() {
	color = inColor;
	tex0 = inTex0;
	gl_Position = PROJ_VIEW_MAT * inVert;
}

