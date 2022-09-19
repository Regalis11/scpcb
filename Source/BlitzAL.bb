;*****************************************************************
;*** 
;***	OpenAL Wrapper for Blitz3D
;***  http://tools.mirage-lab.com
;***
;*****************************************************************

; Filter Types
Const ALFT_NULL=0
Const ALFT_LOWPASS=1
Const ALFT_HIGHPASS=2
Const ALFT_BANDPASS=3
Const ALFT_COUNT=4



Function alInitialise(numDevice=0, rolloffFactor#=0.1, dopplerFactor#=1.0)
	
	alInit(rolloffFactor, dopplerFactor)
	alDeviceInit(numDevice,4,-1)

End Function

;Update position and orientation listener
Function alListenerUpdate(entity)
	
	lx#=EntityX(entity,1)
	ly#=EntityY(entity,1)
	lz#=EntityZ(entity,1)
	
	alListenerSetPosition(lx,ly,lz)
	
	TFormVector 0,0,-10,entity,0
	nx = TFormedX() 
	ny = TFormedY() 
	nz = TFormedZ() 
	
	alListenerSetDirection(nx,ny,nz)

	TFormVector 0,10,0,entity,0 
	nx = TFormedX() 
	ny = TFormedY() 
	nz = TFormedZ() 
	
	alListenerSetUp(nx,ny,nz)

End Function


;-----------------------------------------------------------------
;------------	Redefinition functions

Function alCreateSource%(file$, stream%=False, sound3D%=True)
	Return alCreateSource_(file, stream, sound3D)	
End Function

Function alSourcePlay(source%, restart%=True)
	Return alSourcePlay_(source, restart)	
End Function

Function alSourcePlay3D%(source%, restart=True)
	Return alSourcePlay3D_(source, restart)	
End Function

Function alSourcePlay2D%(source%, restart=True)
	Return alSourcePlay2D_(source, restart)	
End Function


;-----------------------------------------------------------------
;------------	Effects

; gain -                          range: 0.0 to 1.0
; gain_hf -                       range: 0.0 to 1.0
; gain_lf -                       range: 0.0 to 1.0
; density -                       range: 0.0 to 1.0
; decay_hfratio -				  range: 0.1 to 2.0
; ref_gain - reflection gain.     range: 0.0 to 3.16
; ref_delay - reflection delay.   range: 0.0 to 0.3
; late_reverb_delay -             range: 0.0 to 0.1
; late_reverb_gain -              range: 0.0 to 10.0
; echo_time - 					  range: 0.075 to 0.25
; echo_depth - 					  range: 0.1 to 1.0
; modulation_depth - 			  range: 0.1 to 1.0
; modulation_time - 			  range: 0.004 to 4.0
; room_rollofffactor -			  range: 0.0 to 10.0
; airgain_hf - air absorption GainHF. range: 0.892 to 1.0
; decay_hflimit -				  range: false / true
; decay_lfratio - 				  range: 0.1 to 2.0
; decay_time - 					  range: 0.1 to 20.0
; diffusion - 					  range: 0.0 to 1.0
; hf_reference - 				  range: 1000.0 to 20000.0
; lf_reference - 				  range: 20.0 to 1000.0


;peresets: Generic; Padded Cell; Room; Bath Room; Living Room; Living Room; Stone Room; Auditorium; Concert Hall; Cave; Arena; Hangar; Carpeted Hallway; Hallway; Stone Corridor;
; Alley; Forest; City; Mountains; Quarry; Plain; Parking Lot; Sewer Pipe; Under Water; Drugged; Dizzy; Psychotic
Function alEffectSetEAXReverbParam(effect%, gain#=0.316,gain_hf#=0.25,gain_lf#=1,density#=0.017,decay_hfratio#=0.54,ref_gain#=0.653,ref_delay#=0.01,late_reverb_delay#=0.01,late_reverb_gain#=3.273,echo_time#=0.1,echo_depth#=0.01,modulation_depth#=0.1,modulation_time#=0.1,room_rollofffactor#=1,airgain_hf#=0.9,decay_hflimit%=True,decay_lfratio#=0.1,decay_time#=0.1,diffusion#=0.04,hf_reference#=1000, lf_reference#=20)

alEffectSetEAXReverb(effect,gain,gain_hf,gain_lf,density,decay_hfratio,ref_gain,ref_delay,late_reverb_delay,late_reverb_gain,echo_time,echo_depth,modulation_depth,modulation_time,room_rollofffactor,airgain_hf,decay_hflimit,decay_lfratio,decay_time,diffusion,hf_reference,lf_reference)

End Function

; airgain_hf - air absorption GainHF. range: 0.892 to 1.0 
; decay_hflimit - 				  range: false / true
; decay_hfratio -       		  range: 0.1 to 2
; decay_time -          		  range: 0.1 to 20
; density -                       range: 0.0 to 1.0
; diffusion -                     range: 0.0 to 1.0
; gain -                          range: 0.0 to 1.0
; gain_hf -                       range: 0.0 to 1.0
; late_reverb_delay -             range: 0.0 to 0.1
; late_reverb_gain -              range: 0.0 to 10.0
; ref_delay - reflection delay.   range: 0.0 to 0.3
; ref_gain - reflection gain.     range: 0.0 to 3.16
; room_rollofffactor -			  range: 0.0 to 10.0
Function alEffectSetReverbParam(effect%, airgain_hf#=0.251, decay_hflimit%=True, decay_hfratio#=0.54, decay_time#=0.9, density#=0.17, diffusion#=0.04, gain#=0.316, gain_hf#=0.3, late_reverb_delay#=0.01, late_reverb_gain#=3.273, ref_delay#=0.01, ref_gain#=0.03, room_rollofffactor#=0.2)	
	alEffectSetReverb(effect, airgain_hf, decay_hflimit, decay_hfratio, decay_time, density, diffusion, gain, gain_hf , late_reverb_delay, late_reverb_gain, ref_delay, ref_gain, room_rollofffactor)																					
End Function

; attack_time - 				  range: 0.0001 to 1.0
; peak_gain - 					  range: 0.00003 to 31621.0
; release_time - 				  range: 0.0001 to 1.0
; resonance - 					  range: 2.0 to 1000.0
Function alEffectSetAutowahParam(effect%, attack_time#=0, peak_gain#=11, release_time#=0, resonance#=1000) 
	alEffectSetAutowah(effect, attack_time, peak_gain, release_time, resonance) 
End Function

; delay - 						  range: 0.0 to 0.016
; depth -						  range: 0.0 to 1.0
; feedback - 					  range: -1.0 to 1.0		
; phase - 						  range: -180 to 180
; rate -						  range: 0.0 to 10.0
; waveform - 					  range: 0, 1, 2
Function alEffectSetChorusParam(effect%, delay_#=0, depth#=0, feedback#=0, phase#=90, rate#=1, waveform%=1)
	alEffectSetChorus(effect, delay_, depth, feedback, phase, rate, waveform)
End Function

; edge - 						  range: 0.0 to 1.0
; eqband_width - 				  range: 80.0 to 24000.0
; eq_center - 					  range: 80.0 to 24000.0
; gain -						  range: 0.01 to 1.0
; lowpass_cuttoff - 			  range: 80.0 to 24000.0
Function alEffectSetDistortionParam(effect%, edge#=0, eqband_width#=3600, eq_center#=36000, gain#=0, lowpass_cuttoff#=8000)
	alEffectSetDistortion(effect, edge, eqband_width, eq_center, gain, lowpass_cuttoff)
End Function

; damping -						  range: 0.0 to 0.99
; delay - 						  range: 0.0 to 0.207
; feedback - 					  range: 0.0 to 1.0
; lr_delay - 					  range: 0.0 to 0.404
; spread -						  range: -1.0 to 1.0
Function alEffectSetEchoParam(effect%, damping#=0, delay_#=0, feedback#=0, lr_delay#=0, spread#=-1)
	alEffectSetEcho(effect, damping, delay_, feedback, lr_delay, spread)
End Function

; delay - 						  range: 0.0 to 0.004
; depth -						  range: 0.0 to 1.0
; feedback -					  range: 0.0 to 1.0
; phase - 						  range: -180 to 180
; rate -						  range: 0.0 to 10.0
; waveform - 					  range: 0, 1, 2
Function alEffectSetFlangerParam(effect%, delay_#=0, depth#=1, feedback#=0, phase#=0, rate#=0, waveform%=1)
	alEffectSetFlanger(effect, delay_, depth, feedback, phase, rate, waveform)
End Function

; high_cutoff - 				  range: 4000.0 to 16000.0
; high_gain -					  range: 0.126 to 7.943
; low_cutoff -					  range: 50.0 to 800.0
; low_gain - 					  range: 0.126 to 7.943
; mid1_center - 				  range: 200.0 to 3000.0
; mid1_gain -					  range: 0.126 to 7.943
; mid1_width - 					  range: 0.01 to 1.0
; mid2_center - 				  range: 1000.0 to 8000.0
; mid2_gain -					  range: 0.126 to 7.943
; mid2_width -					  range: 0.01 to 1.0
Function alEffectSetEqualizerParam(effect%, high_cutoff#=6000, high_gain#=1, low_cutoff#=200, low_gain#=1, mid1_center#=500, mid1_gain#=1, mid1_width#=1, mid2_center#=3000, mid2_gain#=1, mid2_width#=1)
	alEffectSetEqualizer(effect, high_cutoff, high_gain, low_cutoff, low_gain, mid1_center, mid1_gain, mid1_width, mid2_center, mid2_gain, mid2_width)
End Function


