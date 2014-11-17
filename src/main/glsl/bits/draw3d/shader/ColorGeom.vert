#version 330

uniform mat4 PROJ_VIEW_MAT;

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec4 inColor;

out VertData {
	vec4 color;
} data;

void main() {
	data.color = inColor;
	gl_Position = PROJ_VIEW_MAT * inVert;
}

