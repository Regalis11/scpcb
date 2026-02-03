; Project Mirror - Visual Effects System
; Sanity effects, flashbacks, screen effects, ending visuals

; === SCREEN EFFECT TYPES ===
Const VFX_NONE% = 0
Const VFX_FADE_BLACK% = 1
Const VFX_FADE_WHITE% = 2
Const VFX_FLASHBACK% = 3
Const VFX_VIGNETTE% = 4
Const VFX_STATIC% = 5
Const VFX_REDPULSE% = 6
Const VFX_PSYCHO% = 7

; === GLOBALS ===
Global VFXCurrentEffect% = VFX_NONE
Global VFXIntensity# = 0.0
Global VFXTimer# = 0.0
Global VFXDuration# = 0.0
Global VFXCallback% = 0

; sanity visuals
Global SanityVignetteAlpha# = 0.0
Global SanityNoiseAlpha# = 0.0
Global SanityWaveOffset# = 0.0
Global SanityBreathTimer# = 0.0
Global SanityBreathPhase# = 0.0

; flashback
Global FlashbackActive% = False
Global FlashbackGrayscale# = 0.0
Global FlashbackGrain# = 0.0
Global FlashbackTimer# = 0.0

; ending
Global EndingFadeActive% = False
Global EndingFadeAlpha# = 0.0
Global EndingText$ = ""
Global EndingSubtext$ = ""
Global VFXEndingTimer# = 0.0

; phantom visuals
Global PhantomSprite% = 0
Global PhantomX# = 0.0
Global PhantomY# = 0.0
Global PhantomAlpha# = 0.0
Global PhantomFadeDir% = 0

; textures
Global VFXNoiseTexture% = 0
Global VFXVignetteTexture% = 0
Dim VFXStaticFrames%(8)

Function InitVFXSystem()
	; sozdaem tekstury dlya effectov

	; shum/zerno - 64x64 random pixels
	VFXNoiseTexture = CreateTexture(64, 64, 1 + 2)
	Local tb% = TextureBuffer(VFXNoiseTexture)
	SetBuffer tb
	For y% = 0 To 63
		For x% = 0 To 63
			Local gray% = Rand(0, 255)
			Color gray, gray, gray
			Plot x, y
		Next
	Next
	SetBuffer BackBuffer()

	; vin'etka - gradientnyi krug
	VFXVignetteTexture = CreateTexture(256, 256, 1 + 2)
	tb = TextureBuffer(VFXVignetteTexture)
	SetBuffer tb
	Local cx# = 128.0
	Local cy# = 128.0
	For y% = 0 To 255
		For x% = 0 To 255
			Local dist# = Sqr((x - cx) * (x - cx) + (y - cy) * (y - cy))
			Local alpha% = 0
			If dist > 80.0 Then
				alpha = Int((dist - 80.0) / 48.0 * 255.0)
				If alpha > 255 Then alpha = 255
			EndIf
			Color 0, 0, 0
			Plot x, y
		Next
	Next
	SetBuffer BackBuffer()

	; static frames dlya pomekh
	For i% = 0 To 7
		VFXStaticFrames(i) = CreateTexture(128, 128, 1 + 2)
		tb = TextureBuffer(VFXStaticFrames(i))
		SetBuffer tb
		For y% = 0 To 127
			For x% = 0 To 127
				gray = Rand(0, 255)
				Color gray, gray, gray
				Plot x, y
			Next
		Next
		SetBuffer BackBuffer()
	Next

	DebugLog "VFX System initialized"
End Function

Function UpdateVFXSystem()
	; obnovlyaem aktivnyy effekt
	If VFXCurrentEffect <> VFX_NONE Then
		VFXTimer = VFXTimer + FPSfactor

		If VFXDuration > 0.0 And VFXTimer >= VFXDuration Then
			EndVFXEffect()
		EndIf
	EndIf

	; sanity visuals
	UpdateSanityVisuals()

	; flashback
	If FlashbackActive Then
		UpdateFlashback()
	EndIf

	; ending fade
	If EndingFadeActive Then
		UpdateEndingFade()
	EndIf

	; phantom
	UpdatePhantomVisual()
End Function

Function RenderVFXSystem()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; === SANITY EFFECTS ===

	; vin'etka (vsegda pri sanity > 30)
	If SanityVignetteAlpha > 0.0 Then
		RenderVignette(SanityVignetteAlpha)
	EndIf

	; noise/grain overlay
	If SanityNoiseAlpha > 0.0 Then
		RenderNoise(SanityNoiseAlpha)
	EndIf

	; breathing effect (krai ekrana pulsiruyut)
	If SanityBreathPhase > 0.0 Then
		RenderBreathingEdges()
	EndIf

	; === FLASHBACK ===
	If FlashbackActive Then
		RenderFlashbackOverlay()
	EndIf

	; === CURRENT EFFECT ===
	Select VFXCurrentEffect
		Case VFX_FADE_BLACK
			RenderFadeBlack()
		Case VFX_FADE_WHITE
			RenderFadeWhite()
		Case VFX_STATIC
			RenderStaticNoise()
		Case VFX_REDPULSE
			RenderRedPulse()
		Case VFX_PSYCHO
			RenderPsychoEffect()
	End Select

	; === PHANTOM ===
	If PhantomAlpha > 0.0 Then
		RenderPhantom()
	EndIf

	; === ENDING ===
	If EndingFadeActive Then
		RenderEndingScreen()
	EndIf
End Function

; ============================================================================
; SANITY VISUAL UPDATES
; ============================================================================

Function UpdateSanityVisuals()
	Local level% = GetSanityLevel()

	Select level
		Case 0  ; normal
			SanityVignetteAlpha = Max(SanityVignetteAlpha - 0.02, 0.0)
			SanityNoiseAlpha = Max(SanityNoiseAlpha - 0.02, 0.0)
			SanityBreathPhase = Max(SanityBreathPhase - 0.02, 0.0)

		Case 1  ; trevoga (30-70%)
			SanityVignetteAlpha = Min(SanityVignetteAlpha + 0.01, 0.3)
			SanityNoiseAlpha = 0.0

			; dykhanie
			SanityBreathTimer = SanityBreathTimer + FPSfactor * 0.05
			SanityBreathPhase = Sin(SanityBreathTimer) * 0.15 + 0.15

		Case 2  ; paranoya (70-100%)
			SanityVignetteAlpha = Min(SanityVignetteAlpha + 0.01, 0.5)
			SanityNoiseAlpha = Min(SanityNoiseAlpha + 0.005, 0.15)

			; usilennoye dykhanie
			SanityBreathTimer = SanityBreathTimer + FPSfactor * 0.08
			SanityBreathPhase = Sin(SanityBreathTimer) * 0.25 + 0.25

			; wave distortion
			SanityWaveOffset = SanityWaveOffset + FPSfactor * 0.1

		Case 3  ; isteriya (100%)
			SanityVignetteAlpha = 0.7
			SanityNoiseAlpha = Min(SanityNoiseAlpha + 0.01, 0.3)

			; panika
			SanityBreathTimer = SanityBreathTimer + FPSfactor * 0.15
			SanityBreathPhase = Sin(SanityBreathTimer) * 0.4 + 0.4

			; random spikes
			If Rand(1, 70) = 1 Then
				SanityNoiseAlpha = 0.6
			EndIf
	End Select
End Function

; ============================================================================
; RENDER FUNCTIONS
; ============================================================================

Function RenderVignette(alpha#)
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; risuyem zatemnenie po krayam
	Local edgeSize% = Int(gw * 0.15 * alpha)
	Local maxAlpha% = Int(alpha * 200)

	; levyi kray
	For x% = 0 To edgeSize
		Local a% = Int((1.0 - Float(x) / Float(edgeSize)) * maxAlpha)
		Color 0, 0, 0
		Line x, 0, x, gh
	Next

	; pravyi kray
	For x% = gw - edgeSize To gw
		a = Int((Float(x - (gw - edgeSize)) / Float(edgeSize)) * maxAlpha)
		Color 0, 0, 0
		Line x, 0, x, gh
	Next

	; verkh
	For y% = 0 To edgeSize
		a = Int((1.0 - Float(y) / Float(edgeSize)) * maxAlpha)
		Color 0, 0, 0
		Line 0, y, gw, y
	Next

	; niz
	For y% = gh - edgeSize To gh
		a = Int((Float(y - (gh - edgeSize)) / Float(edgeSize)) * maxAlpha)
		Color 0, 0, 0
		Line 0, y, gw, y
	Next
End Function

Function RenderNoise(alpha#)
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; risuyem sluchainye tochki
	Local density% = Int(alpha * 5000)
	Local gray%

	For i% = 0 To density
		Local x% = Rand(0, gw - 1)
		Local y% = Rand(0, gh - 1)
		gray = Rand(30, 80)
		Color gray, gray, gray
		Plot x, y
	Next
End Function

Function RenderBreathingEdges()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; pulsiruyushchaya krasnaya ramka
	Local edgeWidth% = Int(SanityBreathPhase * 30)
	Local r% = Int(40 + SanityBreathPhase * 60)

	Color r, 0, 0

	; ramka
	Rect 0, 0, gw, edgeWidth, True
	Rect 0, gh - edgeWidth, gw, edgeWidth, True
	Rect 0, 0, edgeWidth, gh, True
	Rect gw - edgeWidth, 0, edgeWidth, gh, True
End Function

Function RenderFadeBlack()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; progressivnyy fade
	Local progress# = VFXTimer / VFXDuration
	If progress > 1.0 Then progress = 1.0

	VFXIntensity = progress

	Local alpha% = Int(progress * 255)
	Color 0, 0, 0
	Rect 0, 0, gw, gh, True
End Function

Function RenderFadeWhite()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local progress# = VFXTimer / VFXDuration
	If progress > 1.0 Then progress = 1.0

	VFXIntensity = progress

	Color 255, 255, 255
	Rect 0, 0, gw, gh, True
End Function

Function RenderStaticNoise()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; intensivnyy shum kak na starom TV
	Local density% = Int(VFXIntensity * 20000)

	For i% = 0 To density
		Local x% = Rand(0, gw - 1)
		Local y% = Rand(0, gh - 1)
		Local gray% = Rand(0, 255)
		Color gray, gray, gray
		Plot x, y
	Next

	; gorizontal'nye polosy
	For i% = 0 To 5
		Local lineY% = Rand(0, gh)
		Local lineH% = Rand(1, 4)
		gray = Rand(100, 200)
		Color gray, gray, gray
		Rect 0, lineY, gw, lineH, True
	Next
End Function

Function RenderRedPulse()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local pulse# = Sin(VFXTimer * 5.0) * 0.5 + 0.5
	Local alpha% = Int(pulse * VFXIntensity * 100)

	Color alpha, 0, 0
	Rect 0, 0, gw, gh, True
End Function

Function RenderPsychoEffect()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; kombinirovannyi effekt bezumiya
	RenderNoise(0.4)
	RenderBreathingEdges()

	; migayushchie obrazy
	If Rand(1, 10) = 1 Then
		Color 100, 0, 0
		Local fx% = Rand(0, gw - 100)
		Local fy% = Rand(0, gh - 50)
		Text fx, fy, "SMERT'"
	EndIf
End Function

; ============================================================================
; FLASHBACK SYSTEM
; ============================================================================

Function StartFlashback(duration#)
	FlashbackActive = True
	FlashbackGrayscale = 0.0
	FlashbackGrain = 0.0
	FlashbackTimer = 0.0
	VFXDuration = duration

	DebugLog "Flashback started, duration: " + duration
End Function

Function UpdateFlashback()
	FlashbackTimer = FlashbackTimer + FPSfactor

	; fade in grayscale
	If FlashbackTimer < 35.0 Then
		FlashbackGrayscale = FlashbackTimer / 35.0
		FlashbackGrain = FlashbackGrayscale * 0.3
	ElseIf FlashbackTimer > VFXDuration - 35.0 Then
		; fade out
		Local remaining# = VFXDuration - FlashbackTimer
		FlashbackGrayscale = remaining / 35.0
		FlashbackGrain = FlashbackGrayscale * 0.3
	Else
		FlashbackGrayscale = 1.0
		FlashbackGrain = 0.3
	EndIf

	If FlashbackTimer >= VFXDuration Then
		EndFlashback()
	EndIf
End Function

Function EndFlashback()
	FlashbackActive = False
	FlashbackGrayscale = 0.0
	FlashbackGrain = 0.0
End Function

Function RenderFlashbackOverlay()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; cherno-belyi overlay
	; v blitz3d net nastoyashchego grayscale, imitiruyem zatemneniem

	If FlashbackGrayscale > 0.5 Then
		; sepia/cold tint
		Local tint% = Int((FlashbackGrayscale - 0.5) * 40)
		Color 0, 0, tint
		; light overlay
	EndIf

	; film grain
	If FlashbackGrain > 0.0 Then
		RenderNoise(FlashbackGrain)
	EndIf

	; ramka "starogo fil'ma"
	Color 0, 0, 0
	Rect 0, 0, gw, 20, True
	Rect 0, gh - 20, gw, 20, True

	; miganie
	If Rand(1, 30) = 1 Then
		Color 30, 30, 30
		Rect 0, 0, gw, gh, True
	EndIf
End Function

; ============================================================================
; ENDING SCREEN
; ============================================================================

Function StartEndingSequence(endingType%)
	EndingFadeActive = True
	EndingFadeAlpha = 0.0
	VFXEndingTimer = 0.0

	Select endingType
		Case 1  ; Whistleblower
			EndingText = "WHISTLEBLOWER"
			EndingSubtext = "The truth is out. But the hunt has begun."
		Case 2  ; Symbiosis
			EndingText = "SYMBIOSIS"
			EndingSubtext = "079 is free. The world is doomed."
		Case 3  ; Death
			EndingText = "DEATH"
			EndingSubtext = "Another expendable asset."
		Case 4  ; Zero Protocol
			EndingText = "ZERO PROTOCOL"
			EndingSubtext = "Target neutralized. Thank you for your service."
	End Select
End Function

Function UpdateEndingFade()
	VFXEndingTimer = VFXEndingTimer + FPSfactor

	; fade in (3 seconds)
	If VFXEndingTimer < 210.0 Then
		EndingFadeAlpha = VFXEndingTimer / 210.0
	ElseIf VFXEndingTimer < 490.0 Then
		; hold
		EndingFadeAlpha = 1.0
	ElseIf VFXEndingTimer < 700.0 Then
		; pokazyvaem tekst
		EndingFadeAlpha = 1.0
	Else
		; konets
		EndingFadeActive = False
	EndIf
End Function

Function RenderEndingScreen()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; chernyi fon
	Local alpha% = Int(EndingFadeAlpha * 255)
	Color 0, 0, 0
	Rect 0, 0, gw, gh, True

	; tekst koncovki
	If VFXEndingTimer > 280.0 Then
		Local textAlpha# = (VFXEndingTimer - 280.0) / 140.0
		If textAlpha > 1.0 Then textAlpha = 1.0

		Local gray% = Int(textAlpha * 200)
		Color gray, gray, gray

		; zagolovok
		Local tw% = StringWidth(EndingText)
		Text (gw - tw) / 2, gh / 2 - 40, EndingText

		; podpis'
		If VFXEndingTimer > 420.0 Then
			Local subAlpha# = (VFXEndingTimer - 420.0) / 140.0
			If subAlpha > 1.0 Then subAlpha = 1.0
			gray = Int(subAlpha * 150)
			Color gray, gray, gray
			tw = StringWidth(EndingSubtext)
			Text (gw - tw) / 2, gh / 2 + 20, EndingSubtext
		EndIf

		; session link
		If VFXEndingTimer > 560.0 Then
			Color 80, 80, 80
			Local link$ = "claude.ai/code"
			tw = StringWidth(link)
			Text (gw - tw) / 2, gh - 50, link
		EndIf
	EndIf
End Function

; ============================================================================
; PHANTOM VISUAL
; ============================================================================

Function SpawnPhantomVisual(x#, y#)
	PhantomX = x
	PhantomY = y
	PhantomAlpha = 0.0
	PhantomFadeDir = 1
End Function

Function UpdatePhantomVisual()
	If PhantomFadeDir = 1 Then
		PhantomAlpha = PhantomAlpha + FPSfactor * 0.02
		If PhantomAlpha >= 0.7 Then
			PhantomFadeDir = -1
		EndIf
	ElseIf PhantomFadeDir = -1 Then
		PhantomAlpha = PhantomAlpha - FPSfactor * 0.03
		If PhantomAlpha <= 0.0 Then
			PhantomAlpha = 0.0
			PhantomFadeDir = 0
		EndIf
	EndIf
End Function

Function RenderPhantom()
	If PhantomAlpha <= 0.0 Then Return

	; risuyem prizrachnuyu figuru
	Local alpha% = Int(PhantomAlpha * 100)
	Color alpha, alpha, alpha

	; prostaya figura cheloveka
	Local x% = Int(PhantomX)
	Local y% = Int(PhantomY)

	; golova
	Oval x - 10, y - 60, 20, 25, False
	; telo
	Line x, y - 35, x, y + 20
	; ruki
	Line x - 20, y - 20, x + 20, y - 20
	; nogi
	Line x, y + 20, x - 15, y + 50
	Line x, y + 20, x + 15, y + 50
End Function

; ============================================================================
; EFFECT TRIGGERS
; ============================================================================

Function StartVFXEffect(effectType%, duration#, intensity# = 1.0)
	VFXCurrentEffect = effectType
	VFXTimer = 0.0
	VFXDuration = duration
	VFXIntensity = intensity
End Function

Function EndVFXEffect()
	VFXCurrentEffect = VFX_NONE
	VFXTimer = 0.0
	VFXIntensity = 0.0
End Function

Function TriggerScreenShake(intensity#, duration#)
	; kamera tryasotsya (hook dlya osnovnoy igry)
	; realizuetsya cherez CameraShake v Main.bb
	CameraShake = intensity
End Function

Function TriggerWhiteFlash()
	StartVFXEffect(VFX_FADE_WHITE, 35.0, 1.0)
End Function

Function TriggerBlackout(duration#)
	StartVFXEffect(VFX_FADE_BLACK, duration, 1.0)
End Function

Function TriggerStaticBurst(duration#)
	StartVFXEffect(VFX_STATIC, duration, 0.8)
End Function

; ============================================================================
; NUKE ENDING SPECIAL EFFECTS
; ============================================================================

Global NukeSequenceActive% = False
Global NukeSequencePhase% = 0
Global NukeSequenceTimer# = 0.0
Global NukeFlashbackIndex% = 0

Function StartNukeEndingSequence()
	NukeSequenceActive = True
	NukeSequencePhase = 0
	NukeSequenceTimer = 0.0
	NukeFlashbackIndex = 0

	; stop all sounds, play alarm
	StopChannel Day3AlarmChannel
End Function

Function UpdateNukeSequence()
	If Not NukeSequenceActive Then Return

	NukeSequenceTimer = NukeSequenceTimer + FPSfactor

	Select NukeSequencePhase
		Case 0  ; Markus saditsya
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 1
				NukeSequenceTimer = 0.0
				StartFlashback(420.0)  ; 6 seconds of flashbacks
			EndIf

		Case 1  ; flashback 1 - kofe
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 2
				NukeSequenceTimer = 0.0
				StartDialog(361)
			EndIf

		Case 2  ; flashback 2 - vertolety
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 3
				NukeSequenceTimer = 0.0
				StartDialog(362)
			EndIf

		Case 3  ; flashback 3 - ruka Stiva
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 4
				NukeSequenceTimer = 0.0
				StartDialog(363)
				EndFlashback()
			EndIf

		Case 4  ; sigarety
			If NukeSequenceTimer > 140.0 Then
				NukeSequencePhase = 5
				NukeSequenceTimer = 0.0
				StartDialog(364)
			EndIf

		Case 5  ; ulybka
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 6
				NukeSequenceTimer = 0.0
				StartDialog(365)
			EndIf

		Case 6  ; gallyutsinatsiya Stiva
			If NukeSequenceTimer > 105.0 Then
				NukeSequencePhase = 7
				NukeSequenceTimer = 0.0
				StartDialog(366)
			EndIf

		Case 7  ; belaya vspyshka
			TriggerWhiteFlash()
			If NukeSequenceTimer > 70.0 Then
				NukeSequencePhase = 8
				NukeSequenceTimer = 0.0
				StartEndingSequence(4)  ; Zero Protocol ending
			EndIf

		Case 8  ; ending screen
			; upravlyaetsya cherez EndingFade
			If Not EndingFadeActive Then
				NukeSequenceActive = False
			EndIf
	End Select
End Function

Function RenderNukeSequence()
	If Not NukeSequenceActive Then Return

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	; countdown timer
	If NukeSequencePhase < 7 Then
		Local timeLeft% = 90 - Int(NukeSequenceTimer / 70.0 * 10)
		If timeLeft < 0 Then timeLeft = 0

		Color 255, 50, 50
		Local timerStr$ = "T-" + timeLeft
		Text gw / 2 - StringWidth(timerStr) / 2, 30, timerStr
	EndIf
End Function

; ============================================================================
; CLEANUP
; ============================================================================

Function CleanupVFXSystem()
	If VFXNoiseTexture <> 0 Then
		FreeTexture VFXNoiseTexture
		VFXNoiseTexture = 0
	EndIf

	If VFXVignetteTexture <> 0 Then
		FreeTexture VFXVignetteTexture
		VFXVignetteTexture = 0
	EndIf

	For i% = 0 To 7
		If VFXStaticFrames(i) <> 0 Then
			FreeTexture VFXStaticFrames(i)
			VFXStaticFrames(i) = 0
		EndIf
	Next

	VFXCurrentEffect = VFX_NONE
	FlashbackActive = False
	EndingFadeActive = False
	NukeSequenceActive = False
End Function
