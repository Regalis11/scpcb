; Project Mirror - Cutscene Camera System
; Scripted camera movements, cinematic sequences

; === CAMERA MODES ===
Const CAM_PLAYER% = 0
Const CAM_FIXED% = 1
Const CAM_FOLLOW% = 2
Const CAM_PATH% = 3
Const CAM_ORBIT% = 4
Const CAM_SHAKE% = 5

; === CUTSCENE STATES ===
Const CUT_INACTIVE% = 0
Const CUT_PLAYING% = 1
Const CUT_PAUSED% = 2
Const CUT_FINISHED% = 3

; === GLOBALS ===
Global CutsceneActive% = False
Global CutsceneState% = CUT_INACTIVE
Global CutsceneTimer# = 0.0
Global CutsceneDuration# = 0.0
Global CutsceneID% = 0

; kamera
Global CutCameraMode% = CAM_PLAYER
Global CutCameraEntity% = 0
Global CutCameraPivot% = 0

; pozitsii kamery
Global CutCamX#, CutCamY#, CutCamZ#
Global CutCamPitch#, CutCamYaw#, CutCamRoll#
Global CutCamTargetX#, CutCamTargetY#, CutCamTargetZ#

; interpolatsiya
Global CutCamStartX#, CutCamStartY#, CutCamStartZ#
Global CutCamStartPitch#, CutCamStartYaw#
Global CutCamEndX#, CutCamEndY#, CutCamEndZ#
Global CutCamEndPitch#, CutCamEndYaw#

; shake
Global CutShakeIntensity# = 0.0
Global CutShakeDecay# = 0.0

; path movement
Const MAX_PATH_POINTS% = 32
Type CameraPathPoint
	Field x#, y#, z#
	Field pitch#, yaw#
	Field timeAt#
End Type

Global PathPointCount% = 0
Global CurrentPathPoint% = 0
Global PathProgress# = 0.0

; saved player camera state
Global SavedCamX#, SavedCamY#, SavedCamZ#
Global SavedCamPitch#, SavedCamYaw#

Function InitCutsceneSystem()
	; sozdaem pivot dlya kamery katstseny
	CutCameraPivot = CreatePivot()

	DebugLog "Cutscene system initialized"
End Function

Function StartCutscene(cutsceneID%, duration#)
	If CutsceneActive Then
		EndCutscene()
	EndIf

	CutsceneActive = True
	CutsceneState = CUT_PLAYING
	CutsceneTimer = 0.0
	CutsceneDuration = duration
	CutsceneID = cutsceneID

	; sokhranayem pozitsiyu igroka
	SavePlayerCameraState()

	; blokiruem upravlenie igrokom
	CanPlayerMove = False

	DebugLog "Cutscene started: " + cutsceneID
End Function

Function UpdateCutsceneSystem()
	If Not CutsceneActive Then Return
	If CutsceneState <> CUT_PLAYING Then Return

	CutsceneTimer = CutsceneTimer + FPSfactor

	; obnovlyaem kameru v zavisimosti ot rezhima
	Select CutCameraMode
		Case CAM_FIXED
			UpdateFixedCamera()
		Case CAM_FOLLOW
			UpdateFollowCamera()
		Case CAM_PATH
			UpdatePathCamera()
		Case CAM_ORBIT
			UpdateOrbitCamera()
		Case CAM_SHAKE
			UpdateShakeCamera()
	End Select

	; primenyaem pozitsiyu k igroku/kamere
	ApplyCutsceneCamera()

	; proverka zaversheniya
	If CutsceneDuration > 0.0 And CutsceneTimer >= CutsceneDuration Then
		EndCutscene()
	EndIf

	; obnovlyaem specificheskuyu katstsenu
	UpdateCutsceneScript()
End Function

Function EndCutscene()
	If Not CutsceneActive Then Return

	CutsceneActive = False
	CutsceneState = CUT_FINISHED

	; vosstanovlyaem upravlenie
	CanPlayerMove = True

	; vosstanovlyaem kameru
	RestorePlayerCameraState()

	; chistim path points
	ClearCameraPath()

	CutCameraMode = CAM_PLAYER

	DebugLog "Cutscene ended: " + CutsceneID
End Function

; ============================================================================
; CAMERA MODES
; ============================================================================

Function SetCameraFixed(x#, y#, z#, pitch#, yaw#)
	CutCameraMode = CAM_FIXED
	CutCamX = x
	CutCamY = y
	CutCamZ = z
	CutCamPitch = pitch
	CutCamYaw = yaw
End Function

Function SetCameraFollow(targetEntity%, distance#, height#)
	CutCameraMode = CAM_FOLLOW
	CutCameraEntity = targetEntity
	CutCamTargetX = distance
	CutCamTargetY = height
End Function

Function SetCameraOrbit(centerX#, centerY#, centerZ#, radius#, speed#)
	CutCameraMode = CAM_ORBIT
	CutCamTargetX = centerX
	CutCamTargetY = centerY
	CutCamTargetZ = centerZ
	CutCamX = radius
	CutCamYaw = speed
End Function

Function SetCameraShake(intensity#, decay#)
	CutCameraMode = CAM_SHAKE
	CutShakeIntensity = intensity
	CutShakeDecay = decay
End Function

Function SetCameraInterpolate(startX#, startY#, startZ#, startPitch#, startYaw#, endX#, endY#, endZ#, endPitch#, endYaw#, duration#)
	CutCameraMode = CAM_PATH

	CutCamStartX = startX
	CutCamStartY = startY
	CutCamStartZ = startZ
	CutCamStartPitch = startPitch
	CutCamStartYaw = startYaw

	CutCamEndX = endX
	CutCamEndY = endY
	CutCamEndZ = endZ
	CutCamEndPitch = endPitch
	CutCamEndYaw = endYaw

	CutsceneDuration = duration
	CutsceneTimer = 0.0
End Function

; ============================================================================
; UPDATE FUNCTIONS
; ============================================================================

Function UpdateFixedCamera()
	; kamera ne dvigayetsya
End Function

Function UpdateFollowCamera()
	If CutCameraEntity = 0 Then Return

	Local tx# = EntityX(CutCameraEntity)
	Local ty# = EntityY(CutCameraEntity)
	Local tz# = EntityZ(CutCameraEntity)

	; pozitsiya pozadi i vyshe tseli
	Local yaw# = EntityYaw(CutCameraEntity)
	Local dist# = CutCamTargetX
	Local height# = CutCamTargetY

	CutCamX = tx - Sin(yaw) * dist
	CutCamY = ty + height
	CutCamZ = tz - Cos(yaw) * dist

	; smotrim na tsel'
	Local dx# = tx - CutCamX
	Local dy# = ty - CutCamY
	Local dz# = tz - CutCamZ

	CutCamYaw = ATan2(dx, dz)
	CutCamPitch = ATan2(dy, Sqr(dx * dx + dz * dz))
End Function

Function UpdatePathCamera()
	If CutsceneDuration <= 0.0 Then Return

	; linear interpolation
	Local t# = CutsceneTimer / CutsceneDuration
	If t > 1.0 Then t = 1.0

	; smooth step
	t = t * t * (3.0 - 2.0 * t)

	CutCamX = CutCamStartX + (CutCamEndX - CutCamStartX) * t
	CutCamY = CutCamStartY + (CutCamEndY - CutCamStartY) * t
	CutCamZ = CutCamStartZ + (CutCamEndZ - CutCamStartZ) * t
	CutCamPitch = CutCamStartPitch + (CutCamEndPitch - CutCamStartPitch) * t
	CutCamYaw = CutCamStartYaw + (CutCamEndYaw - CutCamStartYaw) * t
End Function

Function UpdateOrbitCamera()
	Local angle# = CutsceneTimer * CutCamYaw * 0.01
	Local radius# = CutCamX

	CutCamX = CutCamTargetX + Sin(angle) * radius
	CutCamZ = CutCamTargetZ + Cos(angle) * radius
	CutCamY = CutCamTargetY + 1.5

	; smotrim v tsentr
	Local dx# = CutCamTargetX - CutCamX
	Local dz# = CutCamTargetZ - CutCamZ

	CutCamYaw = ATan2(dx, dz)
	CutCamPitch = -15.0
End Function

Function UpdateShakeCamera()
	; zatukhaniye
	CutShakeIntensity = CutShakeIntensity - CutShakeDecay * FPSfactor * 0.01
	If CutShakeIntensity < 0.0 Then
		CutShakeIntensity = 0.0
		CutCameraMode = CAM_PLAYER
	EndIf
End Function

Function ApplyCutsceneCamera()
	; shake offset
	Local shakeX# = 0.0
	Local shakeY# = 0.0
	Local shakePitch# = 0.0

	If CutShakeIntensity > 0.0 Then
		shakeX = Rnd(-CutShakeIntensity, CutShakeIntensity) * 0.1
		shakeY = Rnd(-CutShakeIntensity, CutShakeIntensity) * 0.1
		shakePitch = Rnd(-CutShakeIntensity, CutShakeIntensity) * 2.0
	EndIf

	; primenyaem k kamere igry (Collider v SCP:CB)
	If CutCameraMode <> CAM_PLAYER Then
		PositionEntity Collider, CutCamX + shakeX, CutCamY + shakeY, CutCamZ
		RotateEntity Collider, 0, CutCamYaw, 0
		; dlya kamery
		CameraPitch = CutCamPitch + shakePitch
	EndIf
End Function

; ============================================================================
; PATH SYSTEM
; ============================================================================

Function AddCameraPathPoint(x#, y#, z#, pitch#, yaw#, timeAt#)
	Local p.CameraPathPoint = New CameraPathPoint
	p\x = x
	p\y = y
	p\z = z
	p\pitch = pitch
	p\yaw = yaw
	p\timeAt = timeAt

	PathPointCount = PathPointCount + 1
End Function

Function ClearCameraPath()
	For p.CameraPathPoint = Each CameraPathPoint
		Delete p
	Next
	PathPointCount = 0
	CurrentPathPoint = 0
End Function

Function UpdateCameraPath()
	If PathPointCount < 2 Then Return

	; nahodim tekushchiy segment
	Local prevPoint.CameraPathPoint = Null
	Local nextPoint.CameraPathPoint = Null
	Local idx% = 0

	For p.CameraPathPoint = Each CameraPathPoint
		If CutsceneTimer >= p\timeAt Then
			prevPoint = p
			CurrentPathPoint = idx
		Else
			nextPoint = p
			Exit
		EndIf
		idx = idx + 1
	Next

	If prevPoint = Null Or nextPoint = Null Then Return

	; interpoliruem mezhdu tochkami
	Local segmentDuration# = nextPoint\timeAt - prevPoint\timeAt
	Local segmentProgress# = (CutsceneTimer - prevPoint\timeAt) / segmentDuration

	If segmentProgress > 1.0 Then segmentProgress = 1.0

	; smooth interpolation
	Local t# = segmentProgress * segmentProgress * (3.0 - 2.0 * segmentProgress)

	CutCamX = prevPoint\x + (nextPoint\x - prevPoint\x) * t
	CutCamY = prevPoint\y + (nextPoint\y - prevPoint\y) * t
	CutCamZ = prevPoint\z + (nextPoint\z - prevPoint\z) * t
	CutCamPitch = prevPoint\pitch + (nextPoint\pitch - prevPoint\pitch) * t
	CutCamYaw = prevPoint\yaw + (nextPoint\yaw - prevPoint\yaw) * t
End Function

; ============================================================================
; PLAYER STATE SAVE/RESTORE
; ============================================================================

Function SavePlayerCameraState()
	SavedCamX = EntityX(Collider)
	SavedCamY = EntityY(Collider)
	SavedCamZ = EntityZ(Collider)
	SavedCamPitch = CameraPitch
	SavedCamYaw = EntityYaw(Collider)
End Function

Function RestorePlayerCameraState()
	PositionEntity Collider, SavedCamX, SavedCamY, SavedCamZ
	RotateEntity Collider, 0, SavedCamYaw, 0
	CameraPitch = SavedCamPitch
End Function

; ============================================================================
; PREDEFINED CUTSCENES
; ============================================================================

; Cutscene IDs
Const CUTSCENE_DAY2_173% = 1
Const CUTSCENE_DAY3_FLASHBACK% = 2
Const CUTSCENE_DAY3_STEVE_BODY% = 3
Const CUTSCENE_DAY3_939_CHASE% = 4
Const CUTSCENE_DAY3_096_CORRIDOR% = 5
Const CUTSCENE_MTF_BETRAYAL% = 6
Const CUTSCENE_NUKE_ENDING% = 7

Function UpdateCutsceneScript()
	Select CutsceneID
		Case CUTSCENE_DAY2_173
			UpdateCutscene_Day2_173()
		Case CUTSCENE_DAY3_FLASHBACK
			UpdateCutscene_Day3_Flashback()
		Case CUTSCENE_DAY3_STEVE_BODY
			UpdateCutscene_Day3_SteveBody()
		Case CUTSCENE_DAY3_939_CHASE
			UpdateCutscene_Day3_939Chase()
		Case CUTSCENE_DAY3_096_CORRIDOR
			UpdateCutscene_Day3_096()
		Case CUTSCENE_MTF_BETRAYAL
			UpdateCutscene_MTF_Betrayal()
		Case CUTSCENE_NUKE_ENDING
			UpdateCutscene_NukeEnding()
	End Select
End Function

; --- Day 2: 173 Chamber Scene ---
Function StartCutscene_Day2_173(room.Rooms)
	If room = Null Then Return

	StartCutscene(CUTSCENE_DAY2_173, 1400.0)  ; 20 seconds

	Local roomX# = EntityX(room\obj)
	Local roomY# = 0.5
	Local roomZ# = EntityZ(room\obj)

	; nachalnaya pozitsiya - u vkhoda
	CutCamStartX = roomX
	CutCamStartY = roomY + 1.5
	CutCamStartZ = roomZ - 5.0
	CutCamStartPitch = 0.0
	CutCamStartYaw = 0.0

	; konechnaya - blizhe k kamere 173
	CutCamEndX = roomX
	CutCamEndY = roomY + 1.8
	CutCamEndZ = roomZ + 2.0
	CutCamEndPitch = -5.0
	CutCamEndYaw = 0.0

	CutCameraMode = CAM_PATH
End Function

Function UpdateCutscene_Day2_173()
	; logika katstseny u 173
	; upravlyaetsya cherez Update173Scene v Story.bb
End Function

; --- Day 3: Flashback at 173 ---
Function StartCutscene_Day3_Flashback()
	StartCutscene(CUTSCENE_DAY3_FLASHBACK, 350.0)

	; aktiviruem flashback effect
	StartFlashback(350.0)

	; fiksiruyem kameru na 173
	If PlayerRoom <> Null Then
		Local rx# = EntityX(PlayerRoom\obj)
		Local ry# = 1.5
		Local rz# = EntityZ(PlayerRoom\obj)

		SetCameraFixed(rx, ry, rz + 3.0, -10.0, 0.0)
	EndIf
End Function

Function UpdateCutscene_Day3_Flashback()
	; upravlyaetsya cherez flashback timer
	If Not FlashbackActive Then
		EndCutscene()
	EndIf
End Function

; --- Day 3: Finding Steve's Body ---
Function StartCutscene_Day3_SteveBody()
	StartCutscene(CUTSCENE_DAY3_STEVE_BODY, 280.0)

	; medlennoye priblizhenie k telu
	If SteveCorpse <> Null Then
		Local sx# = EntityX(SteveCorpse\Collider)
		Local sy# = EntityY(SteveCorpse\Collider)
		Local sz# = EntityZ(SteveCorpse\Collider)

		SetCameraInterpolate(sx, sy + 2.0, sz - 3.0, -20.0, 0.0, sx, sy + 0.8, sz - 1.0, -45.0, 0.0, 280.0)
	EndIf

	; sanity hit
	ModifySanity(20)
End Function

Function UpdateCutscene_Day3_SteveBody()
	UpdatePathCamera()
End Function

; --- Day 3: 939 Chase ---
Function StartCutscene_Day3_939Chase()
	StartCutscene(CUTSCENE_DAY3_939_CHASE, 210.0)

	; shake kamera
	SetCameraShake(15.0, 0.5)

	; trigger sprint
	CanPlayerMove = True  ; razreshaem beg

	; spawn 939 pozadi
	Spawn939Chaser()
End Function

Function UpdateCutscene_Day3_939Chase()
	; kamera tryasyotsya poka bezhim
	If CutShakeIntensity > 0.0 Then
		UpdateShakeCamera()
	Else
		EndCutscene()
	EndIf
End Function

; --- Day 3: 096 Corridor ---
Function StartCutscene_Day3_096(corridor.Rooms)
	StartCutscene(CUTSCENE_DAY3_096_CORRIDOR, 0.0)  ; beskonechnaya, do triggera

	; fiksiruyem vzglyad vniz
	If corridor <> Null Then
		Local cx# = EntityX(corridor\obj)
		Local cy# = 1.5
		Local cz# = EntityZ(corridor\obj)

		SetCameraFixed(cx, cy, cz, 60.0, 0.0)  ; smotrim v pol
	EndIf
End Function

Function UpdateCutscene_Day3_096()
	; igrok dolzhen proiti koridor ne glyadya vverkh
	; proverka osushchestvlyaetsya v Gameplay module
End Function

; --- MTF Betrayal ---
Function StartCutscene_MTF_Betrayal()
	StartCutscene(CUTSCENE_MTF_BETRAYAL, 350.0)

	; kamera sleduy za MTF
	; naydyom blizhayshego MTF
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeMTF Then
			SetCameraFollow(n\Collider, 5.0, 2.0)
			Exit
		EndIf
	Next
End Function

Function UpdateCutscene_MTF_Betrayal()
	UpdateFollowCamera()
End Function

; --- Nuke Ending ---
Function StartCutscene_NukeEnding()
	StartCutscene(CUTSCENE_NUKE_ENDING, 0.0)  ; upravlyaetsya vruchnuyu

	; nachalo nuke posledovatel'nosti
	StartNukeEndingSequence()
End Function

Function UpdateCutscene_NukeEnding()
	UpdateNukeSequence()

	If Not NukeSequenceActive Then
		EndCutscene()
	EndIf
End Function

; ============================================================================
; HELPER FUNCTIONS
; ============================================================================

Function Spawn939Chaser()
	; spawn 939 dlya pogoni
	Local px# = EntityX(Collider)
	Local py# = 0.5
	Local pz# = EntityZ(Collider)

	; spawn pozadi igroka
	Local yaw# = EntityYaw(Collider)
	Local spawnX# = px - Sin(yaw) * 8.0
	Local spawnZ# = pz - Cos(yaw) * 8.0

	Local chaser.NPCs = CreateNPC(NPCtype939, spawnX, py, spawnZ)
	If chaser <> Null Then
		chaser\State = 2  ; agressivnyi
		chaser\EnemyX = px
		chaser\EnemyY = py
		chaser\EnemyZ = pz
	EndIf
End Function

Function GetRoomCenterPosition(room.Rooms, outX#, outY#, outZ#)
	If room = Null Then Return

	outX = EntityX(room\obj)
	outY = 1.0
	outZ = EntityZ(room\obj)
End Function

; ============================================================================
; CLEANUP
; ============================================================================

Function CleanupCutsceneSystem()
	If CutsceneActive Then
		EndCutscene()
	EndIf

	ClearCameraPath()

	If CutCameraPivot <> 0 Then
		FreeEntity CutCameraPivot
		CutCameraPivot = 0
	EndIf
End Function
