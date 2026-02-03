Const MAX_ECHO_EVENTS% = 32
Const MAX_ACTIVE_ECHOES% = 4
Const ECHO_TRIGGER_RADIUS# = 3.5
Const ECHO_TRIGGER_RADIUS_SQ# = 12.25

Const ECHO_STATE_DORMANT% = 0
Const ECHO_STATE_SPAWNING% = 1
Const ECHO_STATE_ACTIVE% = 2
Const ECHO_STATE_FADING% = 3
Const ECHO_STATE_COMPLETE% = 4

Const ECHO_TYPE_NPC% = 0
Const ECHO_TYPE_SOUND% = 1
Const ECHO_TYPE_PARTICLE% = 2
Const ECHO_TYPE_COMBINED% = 3

Type EchoEvent
	Field id%
	Field eventType%
	Field requiredDay%
	Field sourceDay%
	Field triggerRoomName$
	Field triggerX#, triggerY#, triggerZ#
	Field triggerRadiusSq#
	Field requiredFlag0%, requiredFlag1%, requiredFlag2%, requiredFlag3%
	Field requiredFlagVal0%, requiredFlagVal1%, requiredFlagVal2%, requiredFlagVal3%
	Field requiredFlagCount%
	Field phantomModelPath$
	Field phantomScale#
	Field phantomAnimStart%
	Field phantomAnimEnd%
	Field phantomAnimSpeed#
	Field phantomMoveTarget%
	Field phantomTargetX#, phantomTargetY#, phantomTargetZ#
	Field baseAlpha#
	Field glowR%, glowG%, glowB%
	Field particleType%
	Field soundPath$
	Field soundVolume#
	Field soundLoop%
	Field spawnDuration#
	Field activeDuration#
	Field fadeDuration#
	Field triggered%
	Field cooldown#
	Field linkedRecordDay%
	Field linkedRecordRoom$
End Type

Type ActiveEcho
	Field event.EchoEvent
	Field state%
	Field stateTimer#
	Field phantomEntity%
	Field phantomPivot%
	Field glowSprite%
	Field currentFrame#
	Field soundChannel%
	Field sound%
	Field currentAlpha#
	Field pulsePhase#
	Field worldX#, worldY#, worldZ#
	Field startX#, startY#, startZ#
	Field moveProgress#
	Field room.Rooms
End Type

Global EchoSystemInitialized% = False
Global EchoUpdateTimer# = 0.0
Global EchoCheckInterval# = 10.0
Global ActiveEchoCount% = 0

Global EchoPhantomTexture% = 0
Global EchoGlowTexture% = 0
Global EchoAmbientSound% = 0

Global EchoDistortionActive% = False
Global EchoDistortionIntensity# = 0.0

Dim EchoEventRegistry.EchoEvent(MAX_ECHO_EVENTS)
Global EchoEventCount% = 0

Function InitEchoSystem()
	If EchoSystemInitialized Then Return

	EchoPhantomTexture = LoadTexture("GFX\Effects\echo_phantom.png", 2)
	If EchoPhantomTexture <> 0 Then
		TextureBlend EchoPhantomTexture, 3
	EndIf

	EchoGlowTexture = LoadTexture("GFX\Effects\echo_glow.png", 2)
	If EchoGlowTexture <> 0 Then
		TextureBlend EchoGlowTexture, 3
	EndIf

	EchoAmbientSound = LoadSound("SFX\Ambient\echo_whisper.ogg")

	EchoEventCount = 0
	ActiveEchoCount = 0
	EchoDistortionActive = False
	EchoDistortionIntensity = 0.0

	RegisterPredefinedEchoes()

	EchoSystemInitialized = True
End Function

Function CreateEchoEvent.EchoEvent(id%, sourceDay%, roomName$, localX#, localY#, localZ#)
	If EchoEventCount >= MAX_ECHO_EVENTS Then Return Null

	Local e.EchoEvent = New EchoEvent

	e\id = id
	e\eventType = ECHO_TYPE_COMBINED
	e\requiredDay = 3
	e\sourceDay = sourceDay
	e\triggerRoomName = roomName
	e\triggerX = localX
	e\triggerY = localY
	e\triggerZ = localZ
	e\triggerRadiusSq = ECHO_TRIGGER_RADIUS_SQ

	e\phantomScale = 1.0
	e\phantomAnimStart = 0
	e\phantomAnimEnd = 100
	e\phantomAnimSpeed = 1.0
	e\phantomMoveTarget = False
	e\baseAlpha = 0.5
	e\glowR = 100
	e\glowG = 150
	e\glowB = 255
	e\particleType = 0
	e\soundVolume = 0.7
	e\soundLoop = False
	e\spawnDuration = 35.0
	e\activeDuration = 350.0
	e\fadeDuration = 70.0
	e\triggered = False
	e\cooldown = 0.0
	e\requiredFlagCount = 0

	EchoEventRegistry(EchoEventCount) = e
	EchoEventCount = EchoEventCount + 1

	Return e
End Function

Function SetEchoPhantom(e.EchoEvent, modelPath$, scale#, animStart%, animEnd%, animSpeed#)
	If e = Null Then Return
	e\phantomModelPath = modelPath
	e\phantomScale = scale
	e\phantomAnimStart = animStart
	e\phantomAnimEnd = animEnd
	e\phantomAnimSpeed = animSpeed
	e\eventType = ECHO_TYPE_NPC
End Function

Function SetEchoMovement(e.EchoEvent, targetX#, targetY#, targetZ#)
	If e = Null Then Return
	e\phantomMoveTarget = True
	e\phantomTargetX = targetX
	e\phantomTargetY = targetY
	e\phantomTargetZ = targetZ
End Function

Function SetEchoSound(e.EchoEvent, soundPath$, volume#, loop%)
	If e = Null Then Return
	e\soundPath = soundPath
	e\soundVolume = volume
	e\soundLoop = loop
	If e\eventType = ECHO_TYPE_NPC Then
		e\eventType = ECHO_TYPE_COMBINED
	Else
		e\eventType = ECHO_TYPE_SOUND
	EndIf
End Function

Function SetEchoVisuals(e.EchoEvent, alpha#, glowR%, glowG%, glowB%)
	If e = Null Then Return
	e\baseAlpha = alpha
	e\glowR = glowR
	e\glowG = glowG
	e\glowB = glowB
End Function

Function SetEchoDuration(e.EchoEvent, spawnFrames#, activeFrames#, fadeFrames#)
	If e = Null Then Return
	e\spawnDuration = spawnFrames
	e\activeDuration = activeFrames
	e\fadeDuration = fadeFrames
End Function

Function AddEchoRequirement(e.EchoEvent, flagIndex%, flagValue%)
	If e = Null Then Return
	If e\requiredFlagCount >= 4 Then Return
	Select e\requiredFlagCount
		Case 0: e\requiredFlag0 = flagIndex : e\requiredFlagVal0 = flagValue
		Case 1: e\requiredFlag1 = flagIndex : e\requiredFlagVal1 = flagValue
		Case 2: e\requiredFlag2 = flagIndex : e\requiredFlagVal2 = flagValue
		Case 3: e\requiredFlag3 = flagIndex : e\requiredFlagVal3 = flagValue
	End Select
	e\requiredFlagCount = e\requiredFlagCount + 1
End Function

Function GetEchoRequiredFlag%(e.EchoEvent, idx%)
	Select idx
		Case 0: Return e\requiredFlag0
		Case 1: Return e\requiredFlag1
		Case 2: Return e\requiredFlag2
		Case 3: Return e\requiredFlag3
	End Select
	Return 0
End Function

Function GetEchoRequiredFlagVal%(e.EchoEvent, idx%)
	Select idx
		Case 0: Return e\requiredFlagVal0
		Case 1: Return e\requiredFlagVal1
		Case 2: Return e\requiredFlagVal2
		Case 3: Return e\requiredFlagVal3
	End Select
	Return 0
End Function

Function LinkEchoToRecord(e.EchoEvent, recordDay%, recordRoom$)
	If e = Null Then Return
	e\linkedRecordDay = recordDay
	e\linkedRecordRoom = recordRoom
End Function

Function RegisterPredefinedEchoes()
	Local e.EchoEvent

	e = CreateEchoEvent(1, 1, "room2storage", 0.0, 0.5, 2.0)
	SetEchoPhantom(e, "GFX\NPCs\classd.b3d", 0.5, 26, 39, 0.8)
	SetEchoSound(e, "SFX\Character\Steve\LastWords1.ogg", 0.6, False)
	SetEchoVisuals(e, 0.4, 80, 120, 200)
	SetEchoDuration(e, 40.0, 420.0, 70.0)
	AddEchoRequirement(e, FLAG_STEVE_MET, 1)
	AddEchoRequirement(e, FLAG_STEVE_DEAD, 1)

	e = CreateEchoEvent(2, 1, "room2testroom", -1.5, 0.5, 0.0)
	SetEchoPhantom(e, "GFX\NPCs\guard.b3d", 0.5, 816, 919, 1.0)
	SetEchoSound(e, "SFX\Character\Guard\Execution.ogg", 0.7, False)
	SetEchoVisuals(e, 0.5, 200, 80, 80)
	SetEchoDuration(e, 35.0, 350.0, 70.0)
	AddEchoRequirement(e, FLAG_GUARD_SPARED, 0)

	e = CreateEchoEvent(3, 2, "room2offices", 2.0, 0.5, -1.0)
	SetEchoPhantom(e, "GFX\NPCs\scientist.b3d", 0.03, 78, 150, 0.6)
	SetEchoSound(e, "SFX\Character\Scientist\Plea.ogg", 0.5, False)
	SetEchoVisuals(e, 0.35, 100, 200, 100)
	SetEchoDuration(e, 50.0, 280.0, 60.0)
	AddEchoRequirement(e, FLAG_SCIENTIST_HELPED, 1)

	e = CreateEchoEvent(4, 2, "room2sl", 0.0, 0.5, 3.0)
	SetEchoPhantom(e, "GFX\NPCs\classd.b3d", 0.5, 155, 197, 1.5)
	SetEchoMovement(e, 0.0, 0.5, -5.0)
	SetEchoSound(e, "SFX\Character\Steve\Running.ogg", 0.4, False)
	SetEchoVisuals(e, 0.3, 50, 150, 255)
	SetEchoDuration(e, 30.0, 210.0, 50.0)
	AddEchoRequirement(e, FLAG_STEVE_SAVED, 1)

	e = CreateEchoEvent(5, 1, "room049", 0.0, 0.8, 2.5)
	SetEchoPhantom(e, "GFX\NPCs\scp-049.b3d", 0.22, 5, 45, 0.4)
	SetEchoSound(e, "SFX\SCP\049\Greeting.ogg", 0.8, False)
	SetEchoVisuals(e, 0.6, 50, 50, 50)
	SetEchoDuration(e, 70.0, 490.0, 100.0)
	AddEchoRequirement(e, FLAG_049_CURED, 1)

	e = CreateEchoEvent(6, 2, "room2offices2", 1.0, 1.2, 0.5)
	e\eventType = ECHO_TYPE_SOUND
	SetEchoSound(e, "SFX\Character\Harrison\Recording.ogg", 0.7, False)
	SetEchoVisuals(e, 0.0, 255, 200, 50)
	SetEchoDuration(e, 20.0, 560.0, 40.0)
	AddEchoRequirement(e, FLAG_HARRISON_PDA, 1)

	e = CreateEchoEvent(7, 2, "room2ccont", 0.0, 2.0, 0.0)
	e\eventType = ECHO_TYPE_SOUND
	SetEchoSound(e, "SFX\Character\MTF\AnnouncBefore.ogg", 0.5, False)
	SetEchoVisuals(e, 0.0, 255, 100, 100)
	SetEchoDuration(e, 10.0, 350.0, 30.0)
	AddEchoRequirement(e, FLAG_MTF_CONTACTED, 1)
End Function

Function UpdateEchoEvents()
	If CurrentDay <> 3 Then Return

	EchoUpdateTimer = EchoUpdateTimer + FPSfactor
	If EchoUpdateTimer < EchoCheckInterval Then
		UpdateActiveEchoes()
		Return
	EndIf
	EchoUpdateTimer = 0.0

	If Collider = 0 Then Return
	Local playerX# = EntityX(Collider, True)
	Local playerY# = EntityY(Collider, True)
	Local playerZ# = EntityZ(Collider, True)

	For i% = 0 To EchoEventCount - 1
		Local e.EchoEvent = EchoEventRegistry(i)
		Local skipEcho% = False

		If e = Null Then skipEcho = True

		If (Not skipEcho) Then
			If e\triggered Then skipEcho = True
			If e\cooldown > 0.0 Then
				e\cooldown = e\cooldown - EchoCheckInterval
				skipEcho = True
			EndIf
		EndIf

		If (Not skipEcho) And ActiveEchoCount >= MAX_ACTIVE_ECHOES Then skipEcho = True
		If (Not skipEcho) And PlayerRoom = Null Then skipEcho = True
		If (Not skipEcho) And PlayerRoom\RoomTemplate = Null Then skipEcho = True

		If (Not skipEcho) Then
			Local roomName$ = PlayerRoom\RoomTemplate\Name
			If Lower(roomName) <> Lower(e\triggerRoomName) Then skipEcho = True

			If (Not skipEcho) Then
				Local triggerWorldX# = EntityX(PlayerRoom\obj) + e\triggerX
				Local triggerWorldY# = e\triggerY
				Local triggerWorldZ# = EntityZ(PlayerRoom\obj) + e\triggerZ

				Local dx# = playerX - triggerWorldX
				Local dy# = playerY - triggerWorldY
				Local dz# = playerZ - triggerWorldZ
				Local distSq# = dx*dx + dy*dy + dz*dz

				If distSq > e\triggerRadiusSq Then skipEcho = True

				If (Not skipEcho) Then
					Local flagsOk% = True
					For f% = 0 To e\requiredFlagCount - 1
						If GetStoryFlag(GetEchoRequiredFlag(e, f)) <> GetEchoRequiredFlagVal(e, f) Then
							flagsOk = False
							Exit
						EndIf
					Next

					If (Not flagsOk) Then skipEcho = True

					If (Not skipEcho) And e\linkedRecordDay > 0 Then
						Local rec.DialogEventRecord = GetDialogEventsForRoom(PlayerRoom, e\linkedRecordDay)
						If rec = Null Then skipEcho = True
					EndIf

					If (Not skipEcho) Then
						SpawnEcho(e, PlayerRoom)
					EndIf
				EndIf
			EndIf
		EndIf
	Next

	UpdateActiveEchoes()
	UpdateEchoDistortion()
End Function

Function SpawnEcho(e.EchoEvent, room.Rooms)
	If e = Null Or room = Null Then Return
	If ActiveEchoCount >= MAX_ACTIVE_ECHOES Then Return

	Local echo.ActiveEcho = New ActiveEcho

	echo\event = e
	echo\state = ECHO_STATE_SPAWNING
	echo\stateTimer = 0.0
	echo\room = room
	echo\currentAlpha = 0.0
	echo\pulsePhase = 0.0
	echo\currentFrame = Float(e\phantomAnimStart)
	echo\moveProgress = 0.0

	echo\worldX = EntityX(room\obj) + e\triggerX
	echo\worldY = e\triggerY
	echo\worldZ = EntityZ(room\obj) + e\triggerZ
	echo\startX = echo\worldX
	echo\startY = echo\worldY
	echo\startZ = echo\worldZ

	echo\phantomPivot = CreatePivot()
	PositionEntity echo\phantomPivot, echo\worldX, echo\worldY, echo\worldZ

	If e\phantomModelPath <> "" Then
		echo\phantomEntity = LoadAnimMesh(e\phantomModelPath)
		If echo\phantomEntity <> 0 Then
			ScaleEntity echo\phantomEntity, e\phantomScale, e\phantomScale, e\phantomScale
			PositionEntity echo\phantomEntity, echo\worldX, echo\worldY, echo\worldZ
			EntityParent echo\phantomEntity, echo\phantomPivot

			EntityAlpha echo\phantomEntity, 0.0
			EntityFX echo\phantomEntity, 1 + 8

			If EchoPhantomTexture <> 0 Then
				EntityTexture echo\phantomEntity, EchoPhantomTexture, 0, 1
			EndIf

			Local px# = EntityX(Collider, True)
			Local pz# = EntityZ(Collider, True)
			Local angle# = ATan2(px - echo\worldX, pz - echo\worldZ)
			RotateEntity echo\phantomEntity, 0, angle, 0

			EntityType echo\phantomEntity, 0
			EntityPickMode echo\phantomEntity, 0
		EndIf
	EndIf

	If EchoGlowTexture <> 0 Then
		echo\glowSprite = CreateSprite(echo\phantomPivot)
		EntityTexture echo\glowSprite, EchoGlowTexture
		ScaleSprite echo\glowSprite, 1.5, 1.5
		SpriteViewMode echo\glowSprite, 1
		EntityFX echo\glowSprite, 1
		EntityAlpha echo\glowSprite, 0.0
		EntityColor echo\glowSprite, e\glowR, e\glowG, e\glowB
		PositionEntity echo\glowSprite, 0, 0.8, 0
	EndIf

	If e\soundPath <> "" Then
		echo\sound = LoadSound(e\soundPath)
		If echo\sound <> 0 Then
			echo\soundChannel = EmitSound(echo\sound, echo\phantomPivot)
			If echo\soundChannel <> 0 Then
				ChannelVolume echo\soundChannel, 0.0
			EndIf
		EndIf
	EndIf

	e\triggered = True

	Select e\id
		Case 1
			SetStoryFlag(FLAG_ECHO_STEVE_SEEN, 1)
		Case 2
			SetStoryFlag(FLAG_ECHO_GUARD_SEEN, 1)
		Case 3
			SetStoryFlag(FLAG_ECHO_SCIENTIST_SEEN, 1)
	End Select

	EchoDistortionActive = True
	EchoDistortionIntensity = 0.3

	If EchoAmbientSound <> 0 Then
		PlaySound EchoAmbientSound
	EndIf

	ActiveEchoCount = ActiveEchoCount + 1
End Function

Function UpdateActiveEchoes()
	For echo.ActiveEcho = Each ActiveEcho
		If echo\event = Null Then
			DestroyEcho(echo)
		Else
			Local e.EchoEvent = echo\event

		Select echo\state

			Case ECHO_STATE_SPAWNING
				echo\stateTimer = echo\stateTimer + FPSfactor
				Local spawnProgress# = echo\stateTimer / e\spawnDuration

				If spawnProgress >= 1.0 Then
					spawnProgress = 1.0
					echo\state = ECHO_STATE_ACTIVE
					echo\stateTimer = 0.0
				EndIf

				echo\currentAlpha = e\baseAlpha * spawnProgress

				If echo\phantomEntity <> 0 Then
					EntityAlpha echo\phantomEntity, echo\currentAlpha
				EndIf
				If echo\glowSprite <> 0 Then
					EntityAlpha echo\glowSprite, echo\currentAlpha * 0.5
				EndIf
				If echo\soundChannel <> 0 Then
					ChannelVolume echo\soundChannel, e\soundVolume * spawnProgress
				EndIf

			Case ECHO_STATE_ACTIVE
				echo\stateTimer = echo\stateTimer + FPSfactor

				echo\pulsePhase = echo\pulsePhase + FPSfactor * 0.05
				Local pulse# = 0.8 + 0.2 * Sin(echo\pulsePhase * 360.0)
				echo\currentAlpha = e\baseAlpha * pulse

				If echo\phantomEntity <> 0 Then
					EntityAlpha echo\phantomEntity, echo\currentAlpha

					echo\currentFrame = echo\currentFrame + e\phantomAnimSpeed * FPSfactor
					If echo\currentFrame > Float(e\phantomAnimEnd) Then
						echo\currentFrame = Float(e\phantomAnimStart)
					EndIf
					SetAnimTime echo\phantomEntity, echo\currentFrame

					If e\phantomMoveTarget Then
						echo\moveProgress = echo\moveProgress + FPSfactor * 0.005
						If echo\moveProgress > 1.0 Then echo\moveProgress = 1.0

						Local newX# = echo\startX + (e\phantomTargetX - echo\startX) * echo\moveProgress
						Local newY# = echo\startY + (e\phantomTargetY - echo\startY) * echo\moveProgress
						Local newZ# = echo\startZ + (e\phantomTargetZ - echo\startZ) * echo\moveProgress

						PositionEntity echo\phantomPivot, newX, newY, newZ

						If echo\moveProgress < 1.0 Then
							Local moveAngle# = ATan2(e\phantomTargetX - newX, e\phantomTargetZ - newZ)
							RotateEntity echo\phantomEntity, 0, moveAngle, 0
						EndIf
					EndIf
				EndIf

				If echo\glowSprite <> 0 Then
					EntityAlpha echo\glowSprite, echo\currentAlpha * 0.5
					Local glowScale# = 1.5 + 0.3 * Sin(echo\pulsePhase * 180.0)
					ScaleSprite echo\glowSprite, glowScale, glowScale
				EndIf

				If echo\stateTimer >= e\activeDuration Then
					echo\state = ECHO_STATE_FADING
					echo\stateTimer = 0.0
				EndIf

				If echo\soundChannel <> 0 And (Not e\soundLoop) Then
					If (Not ChannelPlaying(echo\soundChannel)) Then
						If echo\stateTimer > e\activeDuration * 0.5 Then
							echo\state = ECHO_STATE_FADING
							echo\stateTimer = 0.0
						EndIf
					EndIf
				EndIf

			Case ECHO_STATE_FADING
				echo\stateTimer = echo\stateTimer + FPSfactor
				Local fadeProgress# = echo\stateTimer / e\fadeDuration

				If fadeProgress >= 1.0 Then
					fadeProgress = 1.0
					echo\state = ECHO_STATE_COMPLETE
				EndIf

				echo\currentAlpha = e\baseAlpha * (1.0 - fadeProgress)

				If echo\phantomEntity <> 0 Then
					EntityAlpha echo\phantomEntity, echo\currentAlpha
				EndIf
				If echo\glowSprite <> 0 Then
					EntityAlpha echo\glowSprite, echo\currentAlpha * 0.5
				EndIf
				If echo\soundChannel <> 0 Then
					ChannelVolume echo\soundChannel, e\soundVolume * (1.0 - fadeProgress)
				EndIf

			Case ECHO_STATE_COMPLETE
				e\cooldown = 700.0
				DestroyEcho(echo)
		End Select
		EndIf
	Next
End Function

Function DestroyEcho(echo.ActiveEcho)
	If echo = Null Then Return

	If echo\soundChannel <> 0 Then StopChannel echo\soundChannel
	If echo\sound <> 0 Then FreeSound echo\sound
	If echo\phantomEntity <> 0 Then FreeEntity echo\phantomEntity
	If echo\glowSprite <> 0 Then FreeEntity echo\glowSprite
	If echo\phantomPivot <> 0 Then FreeEntity echo\phantomPivot

	ActiveEchoCount = ActiveEchoCount - 1
	If ActiveEchoCount < 0 Then ActiveEchoCount = 0

	Delete echo
End Function

Function UpdateEchoDistortion()
	If Not EchoDistortionActive Then Return
	EchoDistortionIntensity = EchoDistortionIntensity - FPSfactor * 0.002
	If EchoDistortionIntensity <= 0.0 Then
		EchoDistortionIntensity = 0.0
		EchoDistortionActive = False
	EndIf
End Function

Function RenderEchoDistortion()
	If Not EchoDistortionActive Then Return
	If EchoDistortionIntensity <= 0.0 Then Return

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()
	Local intensity% = Int(EchoDistortionIntensity * 100.0)

	Color 20, 40, intensity

	For y% = 0 To 30
		Local alpha% = 30 - y
		Color alpha/2, alpha, intensity + alpha
		Line 0, y, gw, y
	Next

	For y% = 0 To 30
		Local alpha2% = 30 - y
		Color alpha2/2, alpha2, intensity + alpha2
		Line 0, gh - y - 1, gw, gh - y - 1
	Next

	If EchoDistortionIntensity > 0.1 Then
		Local dots% = Int(EchoDistortionIntensity * 500.0)
		For i% = 0 To dots
			Local nx% = Rand(0, gw - 1)
			Local ny% = Rand(0, gh - 1)
			Local nc% = Rand(50, 150)
			Color nc, nc, nc + 50
			Plot nx, ny
		Next
	EndIf
End Function

Function TriggerEchoByID(echoID%)
	For i% = 0 To EchoEventCount - 1
		Local e.EchoEvent = EchoEventRegistry(i)
		If e <> Null And e\id = echoID Then
			If Not e\triggered And PlayerRoom <> Null Then
				SpawnEcho(e, PlayerRoom)
				Return True
			EndIf
		EndIf
	Next
	Return False
End Function

Function ResetEchoTriggers()
	For e.EchoEvent = Each EchoEvent
		e\triggered = False
		e\cooldown = 0.0
	Next
End Function

Function CleanupEchoSystem()
	For echo.ActiveEcho = Each ActiveEcho
		DestroyEcho(echo)
	Next

	For e.EchoEvent = Each EchoEvent
		Delete e
	Next

	For i% = 0 To MAX_ECHO_EVENTS - 1
		EchoEventRegistry(i) = Null
	Next
	EchoEventCount = 0

	If EchoPhantomTexture <> 0 Then FreeTexture EchoPhantomTexture : EchoPhantomTexture = 0
	If EchoGlowTexture <> 0 Then FreeTexture EchoGlowTexture : EchoGlowTexture = 0
	If EchoAmbientSound <> 0 Then FreeSound EchoAmbientSound : EchoAmbientSound = 0

	EchoSystemInitialized = False
End Function

Function DebugEchoSystem()
	Color 255, 255, 0
	Text 10, 200, "=== ECHO DEBUG ==="
	Text 10, 215, "Active: " + ActiveEchoCount + "/" + MAX_ACTIVE_ECHOES
	Text 10, 230, "Distort: " + Int(EchoDistortionIntensity * 100) + "%"

	Local y% = 250
	For echo.ActiveEcho = Each ActiveEcho
		Local stateName$ = ""
		Select echo\state
			Case ECHO_STATE_SPAWNING : stateName = "SPAWN"
			Case ECHO_STATE_ACTIVE : stateName = "ACTIVE"
			Case ECHO_STATE_FADING : stateName = "FADE"
			Case ECHO_STATE_COMPLETE : stateName = "DONE"
		End Select
		Text 10, y, "Echo #" + echo\event\id + ": " + stateName + " a=" + Int(echo\currentAlpha * 100) + "%"
		y = y + 15
	Next
End Function
