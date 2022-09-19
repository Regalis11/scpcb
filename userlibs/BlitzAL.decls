.lib "BlitzAL.dll"


;////////////////////////////////////////
;//// Init
;-------------------
alInit(rolloff_factor#, doppler_factor#) : "_alInit@8"
alGetAvailableDeviceCount%() : "_alGetAvailableDeviceCount@0"
alGetAvailableDeviceName$(index%) : "_alGetAvailableDeviceName@4"
alGetMaxNumSources%(index%) : "_alGetMaxNumSources@4"
alDeviceInit%(index%, eaxEffectSlots%, outputFrequency%) : "_alDeviceInit@12"
alGetNumSources%() : "_alGetNumSources@0"
alGetRolloffFactor#() : "_alGetRolloffFactor@0"
alDestroy() : "_alDestroy@0"
alUpdate() : "_alUpdate@0"


;////////////////////////////////////////
;//// Listener
;-------------------
alListenerSetPosition(x#, y#, z#) : "_alListenerSetPosition@12"
alListenerSetDirection(nx#, ny#, nz#) : "_alListenerSetDirection@12"
alListenerSetUp(nx#, ny#, nz#) : "_alListenerSetUp@12"
alListenerSetVelocity(nx#, ny#, nz#) : "_alListenerSetVelocity@12"
alListenerSetDopplerFactor(dopplerFactor#) : "_alListenerSetDopplerFactor@4"
alListenerSetMetersPerUnit(meters#) : "_alListenerSetMetersPerUnit@4"
alListenerSetMasterVolume(volume#) : "_alListenerSetMasterVolume@4"

alListenerGetDopplerFactor#() : "_alListenerGetDopplerFactor@0"
alListenerGetMetersPerUnit#() : "_alListenerSetMetersPerUnit@0"
alListenerGetMasterVolume#() : "_alListenerGetMasterVolume@0"


;////////////////////////////////////////
;//// Buffer
;-------------------
alCreateBuffer%(file$, extension$) : "_alCreateBuffer@8"
alFreeBuffer%(buffer%) : "_alFreeBuffer@4"
alFreeAllBuffers() : "_alFreeAllBuffers@0"


;////////////////////////////////////////
;////  Audio Data
;-------------------
alCreateAudioData%(file$, stream%) : "_alCreateAudioData@8"
alCreateAudioDataFromMemory%(buffer%, copy%) : "_alCreateAudioDataFromMemory@8"
alFreeAudioData%(audioData%) : "_alFreeAudioData@4"
alFreeAllAudioData() : "_alFreeAllAudioData@0"
alAudioDataSeek(audioData%, seconds#, relative%) : "_alAudioDataSeek@12"


;////////////////////////////////////////
;//// Audio Source
;-------------------
alCreateSourceFromMemory%(buffer%, sound3D%, copy%) : "_alCreateSourceFromMemory@12"
alCreateEmptySource%(sound3D%) : "_alCreateEmptySource@4"
alCreateSource_%(file$, stream%, sound3D%) : "_alCreateSource@12"
alCreateSource%(file$, stream%, sound3D%)

alFreeSource%(source%) : "_alFreeSource@4"
alFreeAllSources() : "_alFreeAllSources@0"

alSourcePlay_%(source%, restart%) : "_alSourcePlay@8"
alSourcePlay%(source%, restart%)

alSourceResume(source%) : "_alSourceResume@4"
alSourcePlay2D_%(source%, restart%) : "_alSourcePlay2D@8"
alSourcePlay2D%(source%, restart%)
alSourcePause(source%) : "_alSourcePause@4"
alSourceStop(source%) : "_alSourceStop@4"

alSourceIsLooping%(source%) : "_alSourceIsLooping@4"
alSourceIsPlaying%(source%) : "_alSourceIsPlaying@4"
alSourceIsPaused%(source%) : "_alSourceIsPaused@4"
alSourceIsStopped%(source%) : "_alSourceIsStopped@4"

alSourceSetData(source%, audioData%) : "_alSourceSetData@8"

alSourceSetRelReductionVolume(source%, delta#) : "_alSourceSetRelReductionVolume@8"
alSourceSetVolume(source%, volume#) : "_alSourceSetVolume@8"
alSourceSetPitch(source%, pitch#) : "_alSourceSetPitch@8"
alSourceSetLoop(source%, loop%) : "_alSourceSetLoop@8"
alSourceSeek%(source%, seconds#, relative%) : "_alSourceSeek@12"
alSourceSetMinVolume(source%, min_volume#) : "_alSourceSetMinVolume@8"
alSourceSetMaxVolume(source%, max_volume#) : "_alSourceSetMaxVolume@8"
alSourceSet3Dflag(source%, flag%) : "_alSourceSet3Dflag@8" 
alSourceSetUserData(source%, data%) : "_alSourceSetUserData@8"

alSourceGetLenght#(source%, type%) : "_alSourceGetLenght@8"
alSourceGetAudioTime#(source%, type%) : "_alSourceGetAudioTime@8"
alSourceGetNumEffectSlotAvaliable%(source%) : "_alSourceGetNumEffectSlotAvaliable@4"
alSourceGetVolume#(source%) : "_alSourceGetVolume@4"
alSourceGetMinVolume#(source%) : "_alSourceGetMinVolume@4"
alSourceGetMaxVolume#(source%) : "_alSourceGetMaxVolume@4"
alSourceIs3D%(source%) : "_alSourceGet3Dflag@4" 
alSourceGetRelReductionVolume#(source%) : "_alSourceGetRelReductionVolume@4"
alSourceGetUserData%(source%) : "_alSourceGetUserData@4"
alSourceGetStringError$(source%) : "_alSourceGetStringError@4"
alSourceGetPitch#(source%) : "_alSourceGetPitch@4"


;////////////////////////////////////////
;//// 3D Source
;-------------------
alSourcePlay3D_%(source%, restart%) : "_alSourcePlay3D@8"
alSourcePlay3D%(source%, restart%)
alSourceSetRolloffFactor(source%, rolloff_factor#) : "_alSourceSetRolloffFactor@8"
alSourceSetStrength(source%, strength#) : "_alSourceSetStrength@8"

alSourceSet3DPosition(source%, x#, y#, z#) : "_alSourceSet3DPosition@16"
alSourceSetDirection(source%, nx#, ny#, nz#) : "_alSourceSetDirection@16"
alSourceSetVelocity(source%, x#, y#, z#) : "_alSourceSetVelocity@16"
alSourceSetDistance(source%, min_distance#, max_distance#) : "_alSourceSetDistance@12"
alSourceSetConeAngle(source%, innerAngle#, outerAngle#, outerVolume#) : "_alSourceSetConeAngle@16"
alSourceSetDynamicVolume(source%, delta#, distance#) : "_alSourceSetDynamicVolume@12"

alSourceGetInnerConeAngle#(source%) : "_alSourceGetInnerConeAngle@4"
alSourceGetOuterConeAngle#(source%) : "_alSourceGetOuterConeAngle@4"
alSourceGetOuterConeVolume#(source%) : "_alSourceGetOuterConeVolume@4"
alSourceGetMinDistance#(source%) : "_alSourceGetMinDistance@4"
alSourceGetMaxDistance#(source%) : "_alSourceGetMaxDistance@4"
alSourceGetDopplerFactor#(source%) : "_alSourceGetDopplerFactor@4"
alSourceGetRolloffFactor#(source%) : "_alSourceGetRolloffFactor@4"
alSourceGetStrength#(source%) : "_alSourceGetStrength@4"
alSourceGetDeltaVolume#(source%) : "_alSourceGetDeltaVolume@4"


;//////////////////////////////////////
;//// Effect
;-------------------
alCreateEffect%() : "_alCreateEffect@0"
alFreeEffect%(effect%) : "_alFreeEffect@4"
alFreeAllEffects() : "_alFreeAllEffects@0"

alSourceAttachEffect%(source%, effect%, slot%) : "_alSourceAttachEffect@12"
alSourceRemoveEffect(source%, slot%) : "_alSourceRemoveEffect@8"

alEffectIsSupported%(effect%) : "_alEffectIsSupported@4"
alEffectSetEAXReverb(effect%,gain#,gain_hf#,gain_lf#,density#,decay_hfratio#,ref_gain#,ref_delay#,late_reverb_delay#,late_reverb_gain#,echo_time#,echo_depth#,modulation_depth#,modulation_time#,room_rollofffactor#,airgain_hf#,decay_hflimit%,decay_lfratio#,decay_time#,diffusion#,hf_reference#,lf_reference#) : "_alEffectSetEAXReverb@88"
alEffectSetReverb(effect%, airgain_hf#, decay_hflimit#, decay_hfratio#, decay_time#, density#, diffusion#, gain#, gain_hf#, late_reverb_delay#, late_reverb_gain#, ref_delay#, ref_gain#, room_rollofffactor#) : "_alEffectSetReverb@56"
alEffectSetAutowah(effect%, attack_time#, peak_gain#, release_time#, resonance#) : "_alEffectSetAutowah@20"
alEffectSetChorus(effect%, delay#, depth#, feedback#, phase#, rate#, waveform%) : "_alEffectSetChorus@28"
alEffectSetDistortion(effect%, edge#, eqband_width#, eq_center#, gain#, lowpass_cuttoff#) : "_alEffectSetDistortion@24"
alEffectSetEcho(effect%, damping#, delay_#, feedback#, lr_delay#, spread#) : "_alEffectSetEcho@24"
alEffectSetFlanger(effect%, delay_#, depth#, feedback#, phase#, rate#, waveform%) : "_alEffectSetFlanger@28"
alEffectSetEqualizer(effect%, high_cutoff#, high_gain#, low_cutoff#, low_gain#, mid1_center#, mid1_gain#, mid1_width#, mid2_center#, mid2_gain#, mid2_width#) : "_alEffectSetEqualizer@44"

alAddPreset%(effect%, presetName$) : "_alAddPreset@8"
alRemovePreset%(effect%, presetName$) : "_alRemovePreset@8"
alEffectSetPreset(effect%, presetName$) : "_alEffectSetPreset@8"

alPresetIsRegistered%(effect%, presetName$) : "_alPresetIsRegistered@8"
alEffectGetStringError$(effect%) : "_alEffectGetStringError@4"
alEffectIsValid%(effect%) : "_alEffectIsValid@4"


;////////////////////////////////////
;//// Filter
;-------------------
alCreateFilter%() : "_alCreateFilter@0"
alFreeFilter%(filter%) : "_alFreeFilter@4"
alFreeAllFilters() : "_alFreeAllFilters@0"

alSourceAttachFilter%(source%, filter%) : "_alSourceAttachFilter@8"
alSourceRemoveFilter(source%) : "_alSourceRemoveFilter@4"

alFilterGetType%(filter%) : "_alFilterGetType@4" 

alFilterSetType(filter%, type%) : "_alFilterSetType@8"
alFilterSetHighFrequencyVolume(filter%, volume#) : "_alFilterSetHighFrequencyVolume@8"
alFilterSetLowFrequencyVolume(filter%, volume#) : "_alFilterSetLowFrequencyVolume@8"
alFilterSetVolume(filer%, volume#) : "_alFilterSetVolume@8"


;////////////////////////////////////////
;//// Capture
;-------------------
alCaptureGetAvailableDeviceCount%() : "_alCaptureGetAvailableDeviceCount@0"
alCaptureGetAvailableDeviceName$(index%) : "_alCaptureGetAvailableDeviceName@4"
alCaptureInitialize%(deviceIndex%) : "_alCaptureInitialize@4"
alCaptureShutdown() : "_alCaptureShutdown@0"
alCaptureSetFrequency(frequency%) : "_alCaptureSetFrequency@4"
alCaptureSetFormat(format%) : "_alCaptureSetFormat@4"
alCaptureSetInternalBufferSize(size%) : "_alCaptureSetInternalBufferSize@4"
alCaptureIsSupported%() : "_alCaptureIsSupported@0"
alCaptureBegin%() : "_alCaptureBegin@0"
alCaptureStop() : "_alCaptureStop@0"
alCaptureGetCurrentAudioSize%() : "_alCaptureGetCurrentAudioSize@0"
alCaptureCreateSource%(sound3D%) : "_alCaptureCreateSource@4"
alCaptureGetSampleSize%() : "_alCaptureGetSampleSize@0"
alUpdateCaptureBuffer(force%) : "_alUpdateCaptureBuffer@4"