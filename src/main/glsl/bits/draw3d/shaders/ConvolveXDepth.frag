#version 330

#define SAMP_DIM 1
uniform float     SOURCE_STEP;
uniform sampler2D SOURCE_UNIT;
uniform sampler1D KERNEL_UNIT;
uniform sampler2D DEPTH_UNIT;

smooth in vec2 tex0;
out vec4 fragColor;

void main() {
	vec4 col    = vec4( 0.0, 0.0, 0.0, 0.0 );
	float sum   = 0.0;
	vec2  tex   = tex0;
	float s0    = tex0.s;
	float depth = 1.0;
  
	for( int i = 0; i < SAMP_DIM; i++ ) {
		tex.s = s0 + float( i - SAMP_DIM / 2 ) * SOURCE_STEP;
		float kernelPos = (float( i ) + 0.5 ) / float( SAMP_DIM );
		float weight = texture( KERNEL_UNIT, kernelPos ).r;
	   	vec4 x0 = 
		   
		col += weight * texture( SOURCE_UNIT, tex ).r;
		sum += weight;

		float d = texture( DEPTH_UNIT, tex ).r;
		depth = min( depth, d );
	}
  
	fragColor = col / sum;
	gl_FragDepth = depth;
}

