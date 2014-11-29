#version 330


#define LIGHT_NUM 1

uniform mat3 NORM_MAT;
uniform mat4 VIEW_MAT;
uniform mat4 PROJ_VIEW_MAT;

layout(std140) uniform LIGHTS {
	vec4 AMBIENT     [ LIGHT_NUM ];
	vec4 DIFFUSE     [ LIGHT_NUM ];
	vec4 SPECULAR    [ LIGHT_NUM ];
	vec4 POS         [ LIGHT_NUM ]; // w == 0.0 if directional, 1.0 if positional.
	vec3 DIR         [ LIGHT_NUM ]; // MUST be normalized.
	vec3 ATTENUATION [ LIGHT_NUM ]; // x = constant; y = linear; z = quadratic
	vec4 SHAPE       [ LIGHT_NUM ]; // x = cos( cutoff_ang ); y = angular exponent
									// for non-spotlights, use [ 2.0, 0.0 ]
} LIGHTS;

layout(std140) uniform MATERIALS {
	vec4  AMBIENT[2];
	vec4  DIFFUSE[2];
	vec4  SPECULAR[2];
	vec4  EMISSIVE[2];
	float SHININESS[2];
} MATERIALS;

smooth in vec3 vertEye;
smooth in vec3 vertNorm;
out vec4 fragColor;

void main() {
	int side = int(gl_FrontFacing);
	vec4 finalColor = MATERIALS.EMISSIVE[side];

	vec3 unitSurfaceNorm = normalize( vertNorm );
	vec3 E = normalize( vertEye  );

	for( int i = 0; i < LIGHT_NUM; i++ ) {
		// Two possible directions: 
		// 1. For spotlights and pointlights, delta is vector from light to fragment
		//    In this case, LIGHTS.POS[i].w == 1
		// 2. For directional lights, delta is direction of light.
		//    In this case, LIGHTS.POS[i].w == 0
		vec3 lightDelta = vertEye - LIGHTS.POS[i].xyz;
		vec3 unitLightDir = normalize( mix( lightDelta, LIGHTS.DIR[i], LIGHTS.POS[i].w ) );
		//vec3 unitLightDir = normalize( lightDelta ); 

		float dist  = length( lightDelta );
		float atten = 1.0 / dot( vec3( 1.0, dist, dist * dist ), LIGHTS.ATTENUATION[i] );

		// Ambient
		vec4 lightSum = MATERIALS.AMBIENT[side] * LIGHTS.AMBIENT[i];

		// Diffuse and specular.
		float lambertTerm = dot( unitSurfaceNorm, -unitLightDir );
		// Spotlight cutoff
		float cosAng = dot( unitLightDir, LIGHTS.DIR[i] );
		if( cosAng < LIGHTS.SHAPE[i][0] ) {
			lambertTerm = 0.0;
		} else {
			lambertTerm *= pow( cosAng, LIGHTS.SHAPE[i][1] );
		}

		if( lambertTerm > 0.0 ) {
			lightSum += lambertTerm * LIGHTS.DIFFUSE[i] * MATERIALS.DIFFUSE[side];
			float specular = pow( max( 0.0, dot( reflect( unitLightDir, unitSurfaceNorm ), E ) ), MATERIALS.SHININESS[side] );
			lightSum += specular * LIGHTS.SPECULAR[i] * MATERIALS.SPECULAR[side];
		}

		finalColor += atten * lightSum;
		//finalColor = vec4( cosAng * 0.5 + 0.5 );
		//finalColor = vec4( 0.5 * unitLightDir + 0.5, 1.0 );
		//finalColor.z = 1.0 - finalColor.z;
		//finalColor.xyz *= atten;
		
		//finalColor = lightSum;
		//vec3 tmp = 0.5 * ( normalize( vertNorm ) + 1.0 );
		//finalColor = vec4( tmp, 1.0 );
	}
	
	fragColor = finalColor;
	fragColor.a = MATERIALS.DIFFUSE[side].a;
	//fragColor = min( vec4( 1.0,1.0,1.0,1.0), fragColor + .5 );
	//fragColor = vec4( 1.0, fragColor.g, 1.0, 1.0 );
}

