;This is the include file for the "Devil Particle System" by "bytecode77"
;The link to the system's BlitzBasic.com page: "http://www.blitzbasic.com/toolbox/toolbox.php?tool=174"
;The link to the original page: "https://bytecode77.com/coding/devilengines/devilparticlesystem"
;All rights for this file go to "bytecode77"
;
;This file also has been modified a bit to suit better for SCP:CB


Type Template
	Field sub_template.Template[7]                                  ;Sub templates
	Field emitter_blend                                             ;blendmode of emitter entity
	Field interval, particles_per_interval                          ;particle interval
	Field max_particles                                             ;max particles
	Field emitter_max_time                                          ;Emitter life time
	Field min_time, max_time                                        ;Particle life time
	Field tex, animtex, texframe#, maxtexframes, texspeed#          ;Texture
	Field min_ox#, max_ox#, min_oy#, max_oy#, min_oz#, max_oz#      ;Offset
	Field min_xv#, max_xv#, min_yv#, max_yv#, min_zv#, max_zv#      ;Velocity
	Field rot_vel1#, rot_vel2#, align_to_fall, align_to_fall_offset ;Rotation
	Field gravity#                                                  ;Gravity
	Field alpha#, alpha_vel                                         ;Alpha
	Field sx#, sy#, size_multiplicator1#, size_multiplicator2#      ;Size
	Field size_add#, size_mult#                                     ;Size velocity
	Field r1, g1, b1, r2, g2, b2                                    ;Colors
	Field Brightness                                                ;Brightness
	Field floor_y#, floor_bounce#                                   ;Floor
	Field pitch_fix, yaw_fix                                        ;Fix angles
	
	Field yaw#
End Type
Type Emitter
	Field fixed
	Field cnt_loop#, age#, max_time#
	Field tmp.Template
	Field owner, ent, surf
	Field del, frozen
End Type
Type Particle
	Field emitter.Emitter
	Field age, max_time  ;Life time
	Field x#, y#, z#     ;Position
	Field xv#, yv#, zv#  ;Velocity
	Field rot#, rot_vel# ;Rotation
	Field sx#, sy#       ;Size
End Type
Global ParticleCam, ParticlePiv

Function InitParticles(cam)
ParticleCam = cam
ParticlePiv = CreatePivot()
SeedRnd MilliSecs()
End Function

Function FreeParticles()
For tmp.Template = Each Template
	FreeTemplate(Handle(tmp))
Next
For e.Emitter = Each Emitter
	FreeEmitter(e\ent)
Next
Delete Each Template
Delete Each Emitter
Delete Each Particle
If ParticlePiv Then FreeEntity ParticlePiv
End Function

Function CreateTemplate()
tmp.Template = New Template
template = Handle(tmp)
SetTemplateEmitterBlend(template, 3)
SetTemplateInterval(template, 1)
SetTemplateParticlesPerInterval(template, 1)
SetTemplateMaxParticles(template, -1)
SetTemplateEmitterLifeTime(template, 100)
SetTemplateParticleLifeTime(template, 0, 20)
SetTemplateAlpha(template, 1)
SetTemplateSize(template, 1, 1)
SetTemplateSizeVel(template, 0, 1)
SetTemplateColors(template, $FFFFFF, $FFFFFF)
SetTemplateBrightness(template, 1)
SetTemplateFloor(template, -1000000)
SetTemplateFixAngles(template, -1, -1)
Return Handle(tmp)
End Function

Function FreeTemplate(template)
tmp.Template = Object.Template(template)
If tmp\tex Then FreeTexture tmp\tex
For i = 0 To 7
	If tmp\sub_template[i] <> Null Then FreeTemplate(Handle(tmp\sub_template[i]))
Next
Delete tmp
End Function

Function SetTemplateEmitterBlend(template, emitter_blend)
tmp.Template = Object.Template(template)
tmp\emitter_blend = emitter_blend
End Function

Function SetTemplateInterval(template, interval)
tmp.Template = Object.Template(template)
tmp\interval = interval
End Function

Function SetTemplateParticlesPerInterval(template, particles_per_interval)
tmp.Template = Object.Template(template)
tmp\particles_per_interval = particles_per_interval
End Function

Function SetTemplateMaxParticles(template, max_particles)
tmp.Template = Object.Template(template)
tmp\max_particles = max_particles
End Function

Function SetTemplateParticleLifeTime(template, min_time, max_time)
tmp.Template = Object.Template(template)
tmp\min_time = min_time
tmp\max_time = max_time
End Function

Function SetTemplateEmitterLifeTime(template, emitter_max_time)
tmp.Template = Object.Template(template)
tmp\emitter_max_time = emitter_max_time
End Function

Function SetTemplateTexture(template, path$, mode = 0, blend = 1)
tmp.Template = Object.Template(template)
tmp\tex = LoadTexture(path$, mode)
TextureBlend tmp\tex, blend
End Function

Function SetTemplateAnimTexture(template, path$, mode, blend, w, h, maxframes, speed# = 1)
tmp.Template = Object.Template(template)
tmp\animtex = True
tmp\maxtexframes = maxframes
tmp\texspeed# = speed#
tmp\tex = LoadAnimTexture(path$, mode, w, h, 0, tmp\maxtexframes)
TextureBlend tmp\tex, blend
End Function

Function SetTemplateOffset(template, min_ox#, max_ox#, min_oy#, max_oy#, min_oz#, max_oz#)
tmp.Template = Object.Template(template)
tmp\min_ox# = min_ox#
tmp\max_ox# = max_ox#
tmp\min_oy# = min_oy#
tmp\max_oy# = max_oy#
tmp\min_oz# = min_oz#
tmp\max_oz# = max_oz#
End Function

Function SetTemplateVelocity(template, min_xv#, max_xv#, min_yv#, max_yv#, min_zv#, max_zv#)
tmp.Template = Object.Template(template)
tmp\min_xv# = min_xv#
tmp\max_xv# = max_xv#
tmp\min_yv# = min_yv#
tmp\max_yv# = max_yv#
tmp\min_zv# = min_zv#
tmp\max_zv# = max_zv#
End Function

Function SetTemplateRotation(template, rot_vel1#, rot_vel2#)
tmp.Template = Object.Template(template)
tmp\rot_vel1# = rot_vel1#
tmp\rot_vel2# = rot_vel2#
End Function

Function SetTemplateAlignToFall(template, align_to_fall, align_to_fall_offset = 0)
tmp.Template = Object.Template(template)
tmp\align_to_fall = align_to_fall
tmp\align_to_fall_offset = align_to_fall_offset
End Function

Function SetTemplateGravity(template, gravity#)
tmp.Template = Object.Template(template)
tmp\gravity# = gravity#
End Function

Function SetTemplateSize(template, sx#, sy#, size_multiplicator1# = 1, size_multiplicator2# = 1)
tmp.Template = Object.Template(template)
tmp\sx# = sx#
tmp\sy# = sy#
tmp\size_multiplicator1# = size_multiplicator1#
tmp\size_multiplicator2# = size_multiplicator2#
End Function

Function SetTemplateSizeVel(template, size_add#, size_mult#)
tmp.Template = Object.Template(template)
tmp\size_add# = size_add#
tmp\size_mult# = size_mult#
End Function

Function SetTemplateAlpha(template, alpha#)
tmp.Template = Object.Template(template)
tmp\alpha# = alpha#
End Function

Function SetTemplateAlphaVel(template, alpha_vel)
tmp.Template = Object.Template(template)
tmp\alpha_vel = alpha_vel
End Function

Function SetTemplateColors(template, col1, col2)
tmp.Template = Object.Template(template)
tmp\r1 = (col1 And $FF0000) / $10000
tmp\g1 = (col1 And $FF00) / $100
tmp\b1 = col1 And $FF
tmp\r2 = (col2 And $FF0000) / $10000
tmp\g2 = (col2 And $FF00) / $100
tmp\b2 = col2 And $FF
End Function

Function SetTemplateBrightness(template, brightness)
tmp.Template = Object.Template(template)
tmp\brightness = brightness
End Function

Function SetTemplateFloor(template, floor_y#, floor_bounce# = .5)
tmp.Template = Object.Template(template)
tmp\floor_y# = floor_y#
tmp\floor_bounce# = floor_bounce#
End Function

Function SetTemplateFixAngles(template, pitch_fix, yaw_fix)
tmp.Template = Object.Template(template)
tmp\pitch_fix = pitch_fix
tmp\yaw_fix = yaw_fix
End Function

Function SetTemplateSubTemplate(template, sub_template, for_each_particle = False)
tmp.Template = Object.Template(template)
For i = 0 To 7
	If tmp\sub_template[i] = Null Then
		tmp\sub_template[i] = Object.Template(sub_template)
		Exit
	EndIf
Next
End Function

Function SetEmitter(owner, template, fixed = False)
e.Emitter = New Emitter
If fixed Then
	e\owner = CreatePivot()
	PositionEntity e\owner, EntityX(owner), EntityY(owner), EntityZ(owner)
	e\fixed = True
Else
	e\owner = owner
EndIf
e\ent = CreateMesh()
NameEntity(e\ent,"Emitter3")
e\surf = CreateSurface(e\ent)
e\tmp = Object.Template(template)
e\max_time = e\tmp\emitter_max_time
EntityBlend e\ent, e\tmp\emitter_blend
EntityFX e\ent, 34
If e\tmp\tex Then EntityTexture e\ent, e\tmp\tex
For i = 0 To 7
	If e\tmp\sub_template[i] <> Null Then
		If e\tmp\sub_template[i]\tex Then SetEmitter(owner, Handle(e\tmp\sub_template[i]), fixed)
	EndIf
Next
Return e\ent
End Function

Function FreeEmitter(ent, delete_particles = True)
For e.Emitter = Each Emitter
	If e\owner = ent Then
		If delete_particles Then
			For p.Particle = Each Particle
				If p\emitter = e Then Delete p
			Next
			FreeEntity e\ent
			If e\fixed And e\owner Then FreeEntity e\owner
			Delete e
		Else
			e\del = True
		EndIf
	EndIf
Next
End Function

Function FreezeEmitter(ent)
For e.Emitter = Each Emitter
	If e\owner = ent Then e\frozen = True
Next
End Function

Function UnfreezeEmitter(ent)
For e.Emitter = Each Emitter
	If e\owner = ent Then e\frozen = False
Next
End Function

Function SetTemplateYaw(template,yaw#)
	tmp.template = Object.Template(template)
	tmp\yaw = yaw#
End Function

Function UpdateParticles_Devil()
	Local e.Emitter,p.Particle
	
For e.Emitter = Each Emitter
	If e\tmp\max_particles > -1 Then
		cnt_particles = 0
		For p.Particle = Each Particle
			If p\emitter = e Then cnt_particles = cnt_particles + 1
		Next
	EndIf
	ClearSurface e\surf
	If e\max_time > -1 Then
		If e\age > e\max_time Then e\del = True Else e\age = e\age + 1
	EndIf
	If e\frozen = False Then
		e\cnt_loop = (e\cnt_loop + 1) Mod e\tmp\interval
		If e\cnt_loop = 0 And e\del = False Then
			For i = 1 To e\tmp\particles_per_interval
				If (e\tmp\max_particles > -1 And cnt_particles < e\tmp\max_particles) Or e\tmp\max_particles = -1 Then
					p.Particle = New Particle
					p\emitter = e
					p\max_time = Rand(e\tmp\min_time, e\tmp\max_time)
					p\x# = EntityX(e\owner, True) + Rnd(e\tmp\min_ox#, e\tmp\max_ox#)
					p\y# = EntityY(e\owner, True) + Rnd(e\tmp\min_oy#, e\tmp\max_oy#)
					p\z# = EntityZ(e\owner, True) + Rnd(e\tmp\min_oz#, e\tmp\max_oz#)
					p\xv# = Rnd(e\tmp\min_xv#, e\tmp\max_xv#)
					p\yv# = Rnd(e\tmp\min_yv#, e\tmp\max_yv#)
					p\zv# = Rnd(e\tmp\min_zv#, e\tmp\max_zv#)
					p\rot_vel# = Rnd(e\tmp\rot_vel1#, e\tmp\rot_vel2#)
					sm# = Rnd(e\tmp\size_multiplicator1#, e\tmp\size_multiplicator2#)
					p\sx# = p\emitter\tmp\sx# * sm#
					p\sy# = p\emitter\tmp\sy# * sm#
				EndIf
			Next
		EndIf
	EndIf
	If e\tmp\animtex Then
		e\tmp\texframe# = e\tmp\texframe# + e\tmp\texspeed#
		If e\tmp\texframe# > e\tmp\maxtexframes - 1 Then e\tmp\texframe# = 0
		EntityTexture e\ent, e\tmp\tex, e\tmp\texframe#
	EndIf
	frame = frame + texspeed#
	If e\del Then
		del = True
		For p.Particle = Each Particle
			If p\emitter = e Then del = False
		Next
		If del Then
			FreeEntity e\ent
			If e\fixed And e\owner Then FreeEntity e\owner
			Delete e
		EndIf
	EndIf
Next
PositionEntity ParticlePiv, EntityX(ParticleCam, True), EntityY(ParticleCam, True), EntityZ(ParticleCam, True)
Local cam_pitch# = EntityPitch(ParticleCam, True)
Local cam_yaw# = EntityYaw(ParticleCam, True)
Local cam_roll# = EntityRoll(ParticleCam, True)
For p.Particle = Each Particle
	If p\age > p\max_time Then
		Delete p
	Else
		If p\emitter\frozen = False Then
			p\age = p\age + 1
			If p\emitter\tmp\align_to_fall Then p\rot# = (p\emitter\tmp\align_to_fall_offset - ATan2(p\xv#, p\yv#)) Else p\rot# = (p\rot# + p\rot_vel#)
			p\yv# = p\yv# - p\emitter\tmp\gravity#
			p\x# = p\x# + p\xv#
			p\y# = p\y# + p\yv#
			p\z# = p\z# + p\zv#
			If p\y# < p\emitter\tmp\floor_y# Then p\yv# = p\yv# * -p\emitter\tmp\floor_bounce#
			p\sx# = (p\sx# + p\emitter\tmp\size_add#) * p\emitter\tmp\size_mult#
			p\sy# = (p\sy# + p\emitter\tmp\size_add#) * p\emitter\tmp\size_mult#
		EndIf
		RotateEntity ParticlePiv, cam_pitch#, cam_yaw#, cam_roll# + (p\rot# + p\emitter\tmp\align_to_fall_offset)
		If p\emitter\tmp\pitch_fix > -1 Then RotateEntity ParticlePiv, p\emitter\tmp\pitch_fix, EntityYaw(ParticlePiv), EntityRoll(ParticlePiv)
		If p\emitter\tmp\yaw_fix > -1 Then RotateEntity ParticlePiv, EntityPitch(ParticlePiv), p\emitter\tmp\yaw_fix, EntityRoll(ParticlePiv)
		x# = EntityX(p\emitter\ent) + p\x#
		y# = EntityY(p\emitter\ent) + p\y#
		z# = EntityZ(p\emitter\ent) + p\z#
		sx# = p\sx#
		sy# = p\sy#
		TFormVector sx#, -sy#, 0, ParticlePiv, 0
		v1x# = TFormedX() + x#
		v1y# = TFormedY() + y#
		v1z# = TFormedZ() + z#
		TFormVector -sx#, -sy#, 0, ParticlePiv, 0
		v2x# = TFormedX() + x#
		v2y# = TFormedY() + y#
		v2z# = TFormedZ() + z#
		TFormVector sx#, sy#, 0, ParticlePiv, 0
		v3x# = TFormedX() + x#
		v3y# = TFormedY() + y#
		v3z# = TFormedZ() + z#
		TFormVector -sx#, sy#, 0, ParticlePiv, 0
		v4x# = TFormedX() + x#
		v4y# = TFormedY() + y#
		v4z# = TFormedZ() + z#
		v1 = AddVertex(p\emitter\surf, v1x#, v1y#, v1z#, 0, 0)
		v2 = AddVertex(p\emitter\surf, v2x#, v2y#, v2z#, 1, 0)
		v3 = AddVertex(p\emitter\surf, v3x#, v3y#, v3z#, 0, 1)
		v4 = AddVertex(p\emitter\surf, v4x#, v4y#, v4z#, 1, 1)
		r = p\emitter\tmp\r1 + (p\emitter\tmp\r2 - p\emitter\tmp\r1) * Float(p\age) / Float(p\max_time)
		g = p\emitter\tmp\g1 + (p\emitter\tmp\g2 - p\emitter\tmp\g1) * Float(p\age) / Float(p\max_time)
		b = p\emitter\tmp\b1 + (p\emitter\tmp\b2 - p\emitter\tmp\b1) * Float(p\age) / Float(p\max_time)
		If p\emitter\tmp\alpha_vel Then a# = (1 - Float(p\age) / Float(p\max_time)) * p\emitter\tmp\alpha# Else a# = p\emitter\tmp\alpha#
		VertexColor p\emitter\surf, v1, r, g, b, a#
		VertexColor p\emitter\surf, v2, r, g, b, a#
		VertexColor p\emitter\surf, v3, r, g, b, a#
		VertexColor p\emitter\surf, v4, r, g, b, a#
		For i = 1 To p\emitter\tmp\Brightness
			AddTriangle p\emitter\surf, v1, v2, v3
			AddTriangle p\emitter\surf, v3, v2, v4
		Next
	EndIf
Next
End Function





;~IDEal Editor Parameters:
;~F#2F#35#42#55#5E#63#68#6D#72#78#7D#83#8C#96#A0#A6#AC#B1#B9#BF
;~F#C4#C9#D3#D8#DE#E4#EE#106#117#11D
;~C#Blitz3D