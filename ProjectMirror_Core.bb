; Project Mirror - Core Integration
; главный контроллер мода "СТОРОЖ"
; Маркус = охранник, спавн в кафетерии, инвентарь: рация + ключ-карта Lvl 2

Include "ProjectMirror_Story.bb"
Include "ProjectMirror_Echo.bb"
Include "ProjectMirror_939.bb"
Include "ProjectMirror_914.bb"
Include "ProjectMirror_MTF.bb"
Include "ProjectMirror_VFX.bb"
Include "ProjectMirror_Cutscene.bb"
Include "ProjectMirror_UI.bb"
Include "ProjectMirror_Gameplay.bb"

Const MIRROR_VERSION$ = "2.0.0"
Const MIRROR_BUILD% = 20260131
Const MIRROR_CODENAME$ = "STOROZH"

Global ProjectMirrorInitialized% = False
Global ProjectMirrorEnabled% = True
Global ProjectMirrorDebugMode% = False

; перфоманс метрики (для дебага)
Global MirrorUpdateTime% = 0
Global MirrorRenderTime% = 0
Global MirrorFrameCount% = 0

; тоглы фич - можно отключать отдельные системы
Global MirrorStoryEnabled% = True
Global MirrorEchoEnabled% = True
Global Mirror939Enabled% = True
Global Mirror914SystemEnabled% = True
Global MirrorMTFEnabled% = True
Global MirrorVFXEnabled% = True
Global MirrorCutsceneEnabled% = True
Global MirrorUIEnabled% = True
Global MirrorGameplayEnabled% = True

; спавн охранника
Global MirrorSpawnRoom$ = "room2cafeteria"
Global MirrorUseCustomSpawn% = True

; Steve NPC (partner guard)
Global SteveNPC.NPCs = Null
Global SteveSpawned% = False

; Pre-breach lighting (Day 1 and 2 have normal lights)
Global PreBreachLightingEnabled% = True

; Navigation/Compass system
Global NavigationTargetX# = 0.0
Global NavigationTargetY# = 0.0
Global NavigationTargetZ# = 0.0
Global NavigationActive% = False
Global NavigationTargetName$ = ""

; Elevator fast travel
Global ElevatorFastTravelEnabled% = True
Global ElevatorTransitionActive% = False
Global ElevatorTransitionTimer# = 0.0
Global ElevatorDestination$ = ""

Function InitProjectMirror()
	If ProjectMirrorInitialized Then Return

	DebugLog "=== PROJECT MIRROR INIT ==="
	DebugLog "v" + MIRROR_VERSION + " [" + MIRROR_CODENAME + "]"

	Local startTime% = MilliSecs()

	; 1. Story System - база всего
	If MirrorStoryEnabled Then
		DebugLog "Story system..."
		InitStorySystem()
	EndIf

	; 2. Echo System - фантомы дня 3
	If MirrorEchoEnabled Then
		DebugLog "Echo system..."
		InitEchoSystem()
	EndIf

	; 3. 939 Voice Mimicry
	If Mirror939Enabled Then
		DebugLog "939 mimicry..."
		InitVoiceMimicrySystem()
	EndIf

	; 4. 914 Quest Logic
	If Mirror914SystemEnabled Then
		DebugLog "914 quest..."
		InitMirror914System()
	EndIf

	; 5. MTF Fox Tactics
	If MirrorMTFEnabled Then
		DebugLog "MTF tactics..."
		InitFoxTactics()
	EndIf

	; 6. VFX System
	If MirrorVFXEnabled Then
		DebugLog "VFX system..."
		InitVFXSystem()
	EndIf

	; 7. Cutscene System
	If MirrorCutsceneEnabled Then
		DebugLog "Cutscene system..."
		InitCutsceneSystem()
	EndIf

	; 8. UI System
	If MirrorUIEnabled Then
		DebugLog "UI system..."
		InitMirrorUI()
	EndIf

	; 9. Gameplay Mechanics
	If MirrorGameplayEnabled Then
		DebugLog "Gameplay mechanics..."
		InitGameplayMechanics()
	EndIf

	; --- GUARD SPAWN ---
	; Find cafeteria room and teleport player there
	If MirrorUseCustomSpawn Then
		If SpawnGuardAtCafeteria() Then
			GiveGuardEquipment()
			DebugLog "Guard spawn complete"
		Else
			DebugLog "WARNING: Guard spawn failed - will retry on first update"
		EndIf
	EndIf

	Local initTime% = MilliSecs() - startTime
	DebugLog "Mirror init: " + initTime + "ms"

	ProjectMirrorInitialized = True
End Function

; guard spawn in cafeteria - Day 1 morning
Global GuardSpawnComplete% = False

Function SpawnGuardAtCafeteria%()
	If GuardSpawnComplete Then Return True

	Local spawnRoom.Rooms = Null

	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Lower(r\RoomTemplate\Name) = Lower(MirrorSpawnRoom) Then
				spawnRoom = r
				Exit
			EndIf
		EndIf
	Next

	If spawnRoom <> Null Then
		; room center + slightly up to avoid getting stuck
		Local spawnX# = EntityX(spawnRoom\obj)
		Local spawnY# = 0.5
		Local spawnZ# = EntityZ(spawnRoom\obj)

		PositionEntity Collider, spawnX, spawnY, spawnZ
		ResetEntity Collider

		PlayerRoom = spawnRoom
		GuardSpawnComplete = True

		DebugLog "Spawned guard at " + MirrorSpawnRoom
		Return True
	Else
		DebugLog "WARNING: spawn room not found: " + MirrorSpawnRoom
		Return False
	EndIf
End Function

; Guard equipment - radio, level 3 keycard, night vision, battery
Function GiveGuardEquipment()
	Local it.Items
	Local slot% = 0

	; Find free inventory slots
	For i% = 0 To 9
		If Inventory(i) = Null Then
			slot = i
			Exit
		EndIf
	Next

	; Radio - main communication tool
	it = CreateItem("Radio Transceiver", "radio", 1, 1, 1)
	If it <> Null Then
		it\Picked = True
		it\Dropped = -1
		If it\itemtemplate <> Null Then it\itemtemplate\found = True
		Inventory(slot) = it
		HideEntity it\collider
		EntityType it\collider, HIT_ITEM
		EntityParent it\collider, 0
		ItemAmount = ItemAmount + 1
		slot = slot + 1
		DebugLog "Gave radio"
	EndIf

	; Level 3 keycard - security guard clearance (can open most doors before breach)
	For i% = slot To 9
		If Inventory(i) = Null Then
			slot = i
			Exit
		EndIf
	Next

	it = CreateItem("Level 3 Key Card", "key3", 1, 1, 1)
	If it <> Null Then
		it\Picked = True
		it\Dropped = -1
		If it\itemtemplate <> Null Then it\itemtemplate\found = True
		Inventory(slot) = it
		HideEntity it\collider
		EntityType it\collider, HIT_ITEM
		EntityParent it\collider, 0
		ItemAmount = ItemAmount + 1
		slot = slot + 1
		DebugLog "Gave keycard lvl 3"
	EndIf

	; Night Vision Goggles - for dark areas
	For i% = slot To 9
		If Inventory(i) = Null Then
			slot = i
			Exit
		EndIf
	Next

	it = CreateItem("Night Vision Goggles", "nvgoggles", 1, 1, 1)
	If it <> Null Then
		it\Picked = True
		it\Dropped = -1
		If it\itemtemplate <> Null Then it\itemtemplate\found = True
		Inventory(slot) = it
		HideEntity it\collider
		EntityType it\collider, HIT_ITEM
		EntityParent it\collider, 0
		ItemAmount = ItemAmount + 1
		slot = slot + 1
		DebugLog "Gave night vision goggles"
	EndIf

	; Navigator - GPS device (for compass/navigation)
	For i% = slot To 9
		If Inventory(i) = Null Then
			slot = i
			Exit
		EndIf
	Next

	it = CreateItem("S-NAV 300 Navigator", "nav", 1, 1, 1)
	If it <> Null Then
		it\Picked = True
		it\Dropped = -1
		If it\itemtemplate <> Null Then it\itemtemplate\found = True
		Inventory(slot) = it
		HideEntity it\collider
		EntityType it\collider, HIT_ITEM
		EntityParent it\collider, 0
		ItemAmount = ItemAmount + 1
		slot = slot + 1
		DebugLog "Gave navigator"
	EndIf

	; 9V Battery - spare battery for equipment
	For i% = slot To 9
		If Inventory(i) = Null Then
			slot = i
			Exit
		EndIf
	Next

	it = CreateItem("9V Battery", "bat", 1, 1, 1)
	If it <> Null Then
		it\Picked = True
		it\Dropped = -1
		If it\itemtemplate <> Null Then it\itemtemplate\found = True
		Inventory(slot) = it
		HideEntity it\collider
		EntityType it\collider, HIT_ITEM
		EntityParent it\collider, 0
		ItemAmount = ItemAmount + 1
		DebugLog "Gave battery"
	EndIf

	; Full stamina and health
	Stamina = 100.0

	DebugLog "Guard equipment ready"
End Function

; ============================================================================
; STEVE NPC - Partner guard who gives quests and guidance
; ============================================================================

; Steve states
Const STEVE_STATE_IDLE% = 0
Const STEVE_STATE_FOLLOWING% = 1
Const STEVE_STATE_LEADING% = 2
Const STEVE_STATE_WAITING% = 3

Global SteveState% = STEVE_STATE_IDLE
Global SteveTargetX# = 0.0
Global SteveTargetZ# = 0.0

Function SpawnSteveInCafeteria()
	If SteveSpawned Then Return
	If CurrentDay = 3 Then Return  ; Steve is dead on Day 3

	; Spawn Steve directly in front of player, facing the player
	Local playerYaw# = EntityYaw(Collider)
	Local spawnDist# = 2.0  ; 2 meters in front

	Local steveX# = EntityX(Collider) + Sin(playerYaw) * spawnDist
	Local steveY# = EntityY(Collider)
	Local steveZ# = EntityZ(Collider) + Cos(playerYaw) * spawnDist

	SteveNPC = CreateNPC(NPCtypeGuard, steveX, steveY, steveZ)

	; Make sure model is visible
	If SteveNPC <> Null And SteveNPC\obj <> 0 Then
		ShowEntity SteveNPC\obj
	EndIf

	If SteveNPC <> Null Then
		; Face the player
		PointEntity SteveNPC\Collider, Collider
		RotateEntity SteveNPC\Collider, 0, EntityYaw(SteveNPC\Collider), 0

		; Set Steve to friendly idle state
		SteveNPC\State = 7  ; stationary
		SteveNPC\State3 = 0  ; not targeting player
		SteveState = STEVE_STATE_IDLE

		SteveSpawned = True
		SetStoryFlag(FLAG_STEVE_MET, 1)

		DebugLog "Steve spawned in front of player"
	EndIf
End Function

Global SCPsDisabledOnce% = False
Global CorpsesRemovedOnce% = False

Function RemoveElevatorCorpses()
	; Remove dead D-Class from elevator rooms (unrealistic for pre-breach)
	If CurrentDay = 3 Then Return
	If CorpsesRemovedOnce Then Return

	For n.NPCs = Each NPCs
		If n <> Null Then
			; D-Class in dead state (State 8 = dead/corpse)
			If n\NPCtype = NPCtypeD And n\State = 8 Then
				; Remove corpse
				If n\obj <> 0 Then HideEntity n\obj
				If n\Collider <> 0 Then PositionEntity n\Collider, 0, -500, 0
				DebugLog "Removed D-Class corpse (pre-breach)"
			EndIf
		EndIf
	Next

	CorpsesRemovedOnce = True
End Function

Function DisableSCPsBeforeBreach()
	; Before the breach (Day 1 and 2), all SCPs should be contained
	If CurrentDay = 3 Then Return

	; Run every frame to catch newly spawned SCPs (like 066 from events)
	For n.NPCs = Each NPCs
		If n <> Null Then
			Select n\NPCtype
				Case NPCtype173, NPCtypeOldMan, NPCtype096, NPCtype049, NPCtype939, NPCtype066
					; Hide and move far away (don't delete!)
					If n\obj <> 0 Then HideEntity n\obj
					If n\Collider <> 0 Then PositionEntity n\Collider, 0, -500, 0
					n\State = 0
			End Select
		EndIf
	Next

	; Also hide SCP-106 corrosion decals (type 0 = black goo)
	For de.Decals = Each Decals
		If de <> Null And de\ID = 0 Then
			If de\obj <> 0 Then HideEntity de\obj
		EndIf
	Next

	; Lower checkpoint door keycard requirements on Day 1/2 (level 3 instead of 5)
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Instr(Lower(r\RoomTemplate\Name), "checkpoint") > 0 Then
				For i% = 0 To 3
					If r\RoomDoors[i] <> Null Then
						If r\RoomDoors[i]\KeyCard > 3 Then
							r\RoomDoors[i]\KeyCard = 3
						EndIf
					EndIf
				Next
			EndIf
		EndIf
	Next
End Function

Global SteveElevatorFound% = False

Function UpdateSteveNPC()
	If SteveNPC = Null Then Return
	If CurrentDay = 3 Then Return

	; Keep Steve friendly
	SteveNPC\State3 = 0  ; not hostile

	; DON'T MOVE while dialog is active - just face player and idle
	If DialogActive Then
		SteveNPC\State = 7
		SteveNPC\CurrSpeed = 0
		PointEntity SteveNPC\Collider, Collider
		RotateEntity SteveNPC\Collider, 0, EntityYaw(SteveNPC\Collider), 0
		Return
	EndIf

	Local distToPlayer# = EntityDistance(SteveNPC\Collider, Collider)

	; Determine Steve's state based on story progress
	If GetStoryFlag(FLAG_COFFEE_WITH_STEVE) = 0 Then
		; Before coffee dialog finished - IDLE
		SteveState = STEVE_STATE_IDLE
	ElseIf GetStoryFlag(FLAG_SAW_HELICOPTERS) = 0 Then
		; After coffee, before reaching elevator - LEAD to elevator
		SteveState = STEVE_STATE_LEADING

		; Find elevator ONCE
		If Not SteveElevatorFound Then
			Local elevRoom.Rooms = Null
			Local minDist# = 99999.0
			For r.Rooms = Each Rooms
				If r\RoomTemplate <> Null Then
					If Instr(Lower(r\RoomTemplate\Name), "elevator") > 0 Then
						Local d# = EntityDistance(SteveNPC\Collider, r\obj)
						If d < minDist Then
							minDist = d
							elevRoom = r
						EndIf
					EndIf
				EndIf
			Next
			If elevRoom <> Null Then
				SteveTargetX = EntityX(elevRoom\obj)
				SteveTargetZ = EntityZ(elevRoom\obj)
				SteveElevatorFound = True
				DebugLog "Steve target: elevator at " + SteveTargetX + ", " + SteveTargetZ
			EndIf
		EndIf
	Else
		; After helicopter scene - FOLLOW player
		SteveState = STEVE_STATE_FOLLOWING
	EndIf

	; Update Steve's behavior based on state
	Select SteveState
		Case STEVE_STATE_IDLE
			; Just stand and face player - idle animation
			SteveNPC\State = 7
			SteveNPC\CurrSpeed = 0
			PointEntity SteveNPC\Collider, Collider
			RotateEntity SteveNPC\Collider, 0, EntityYaw(SteveNPC\Collider), 0

		Case STEVE_STATE_FOLLOWING
			; Follow behind player
			If distToPlayer > 4.0 Then
				SteveNPC\State = 10  ; walking
				SteveNPC\CurrSpeed = 0.015
				PointEntity SteveNPC\Collider, Collider
				RotateEntity SteveNPC\Collider, 0, EntityYaw(SteveNPC\Collider), 0
			Else
				SteveNPC\State = 7
				SteveNPC\CurrSpeed = 0
			EndIf

		Case STEVE_STATE_LEADING
			Local distToTarget# = Sqr((EntityX(SteveNPC\Collider) - SteveTargetX)^2 + (EntityZ(SteveNPC\Collider) - SteveTargetZ)^2)

			; Wait if player is too far behind
			If distToPlayer > 8.0 Then
				SteveState = STEVE_STATE_WAITING
			ElseIf distToTarget > 2.0 Then
				SteveNPC\State = 10  ; walking
				SteveNPC\CurrSpeed = 0.012
				Local angleToTarget# = ATan2(SteveTargetX - EntityX(SteveNPC\Collider), SteveTargetZ - EntityZ(SteveNPC\Collider))
				RotateEntity SteveNPC\Collider, 0, angleToTarget, 0
			Else
				SteveNPC\State = 7
				SteveNPC\CurrSpeed = 0
				SteveState = STEVE_STATE_WAITING
			EndIf

		Case STEVE_STATE_WAITING
			; Wait for player to catch up - idle animation
			SteveNPC\State = 7
			SteveNPC\CurrSpeed = 0
			PointEntity SteveNPC\Collider, Collider
			RotateEntity SteveNPC\Collider, 0, EntityYaw(SteveNPC\Collider), 0

			If distToPlayer < 5.0 Then
				SteveState = STEVE_STATE_LEADING
			EndIf
	End Select

	; Simple obstacle avoidance - if Steve is stuck, try to go around
	If SteveNPC\State = 10 And SteveNPC\CurrSpeed > 0 Then
		SteveObstacleAvoidance()
	EndIf

	; Steve opens doors in front of him (has high-level keycard)
	SteveOpenDoorsInFront()
End Function

; Simple obstacle avoidance for Steve
Global StevePrevX# = 0.0
Global StevePrevZ# = 0.0
Global SteveStuckTimer# = 0.0

Function SteveObstacleAvoidance()
	If SteveNPC = Null Then Return

	Local curX# = EntityX(SteveNPC\Collider)
	Local curZ# = EntityZ(SteveNPC\Collider)

	; Check if Steve is stuck (hasn't moved much)
	Local moveAmount# = Sqr((curX - StevePrevX)^2 + (curZ - StevePrevZ)^2)

	If moveAmount < 0.001 Then
		SteveStuckTimer = SteveStuckTimer + FPSfactor
	Else
		SteveStuckTimer = 0.0
	EndIf

	; If stuck for too long, try to turn and go around
	If SteveStuckTimer > 35.0 Then  ; About 0.5 seconds
		; Turn slightly to try to go around obstacle
		Local currentYaw# = EntityYaw(SteveNPC\Collider)
		RotateEntity SteveNPC\Collider, 0, currentYaw + 45.0, 0
		SteveStuckTimer = 0.0
	EndIf

	StevePrevX = curX
	StevePrevZ = curZ
End Function

; Steve can open/unlock doors in front of him (he has security clearance)
Function SteveOpenDoorsInFront()
	If SteveNPC = Null Then Return
	If SteveState = STEVE_STATE_IDLE Then Return

	Local steveX# = EntityX(SteveNPC\Collider)
	Local steveY# = EntityY(SteveNPC\Collider)
	Local steveZ# = EntityZ(SteveNPC\Collider)

	For d.Doors = Each Doors
		If d\obj <> 0 Then
			Local doorDist# = EntityDistance(SteveNPC\Collider, d\obj)

			; If Steve is close to a door
			If doorDist < 2.5 Then
				; Unlock if locked (Steve has clearance up to level 3)
				If d\locked And d\KeyCard <= 3 Then
					d\locked = False
				EndIf

				; Open the door if unlocked and closed
				If d\locked = False And d\open = False Then
					d\open = True
				EndIf
			EndIf
		EndIf
	Next
End Function

; ============================================================================
; PRE-BREACH LIGHTING - Day 1 and 2 have normal facility lights
; ============================================================================

Function SetPreBreachLighting()
	If Not PreBreachLightingEnabled Then Return
	If CurrentDay >= 3 Then Return  ; Day 3 = breach = emergency lighting

	; Set ambient lighting to normal (not emergency red/dark)
	AmbientLight 80, 80, 80  ; Bright normal lighting

	; Set fog to minimal
	CameraFogMode Camera, 1
	CameraFogRange Camera, 5, 30
	CameraFogColor Camera, 40, 40, 45

	DebugLog "Pre-breach lighting enabled (Day " + CurrentDay + ")"
End Function

Function SetBreachLighting()
	; Emergency lighting for Day 3
	AmbientLight 30, 20, 20  ; Dark red tint

	CameraFogMode Camera, 1
	CameraFogRange Camera, 1, 15
	CameraFogColor Camera, 10, 5, 5

	DebugLog "Breach lighting enabled"
End Function

; ============================================================================
; NAVIGATION SYSTEM - Compass/GPS showing where to go
; ============================================================================

Function SetNavigationTarget(targetName$, x#, y#, z#)
	NavigationTargetName = targetName
	NavigationTargetX = x
	NavigationTargetY = y
	NavigationTargetZ = z
	NavigationActive = True

	DebugLog "Navigation target: " + targetName
End Function

Function SetNavigationToRoom(roomName$)
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Lower(r\RoomTemplate\Name) = Lower(roomName) Then
				SetNavigationTarget(roomName, EntityX(r\obj), EntityY(r\obj), EntityZ(r\obj))
				Return
			EndIf
		EndIf
	Next
End Function

Function ClearNavigation()
	NavigationActive = False
	NavigationTargetName = ""
End Function

Function GetNavigationAngle#()
	If Not NavigationActive Then Return 0.0

	Local dx# = NavigationTargetX - EntityX(Collider)
	Local dz# = NavigationTargetZ - EntityZ(Collider)

	Local targetAngle# = ATan2(dx, dz)
	Local playerAngle# = EntityYaw(Collider)

	Local relativeAngle# = targetAngle - playerAngle

	; Normalize to -180 to 180
	While relativeAngle > 180.0
		relativeAngle = relativeAngle - 360.0
	Wend
	While relativeAngle < -180.0
		relativeAngle = relativeAngle + 360.0
	Wend

	Return relativeAngle
End Function

Function GetNavigationDistance#()
	If Not NavigationActive Then Return 0.0

	Local dx# = NavigationTargetX - EntityX(Collider)
	Local dz# = NavigationTargetZ - EntityZ(Collider)

	Return Sqr(dx * dx + dz * dz)
End Function

; ============================================================================
; ELEVATOR FAST TRAVEL - Fade out and teleport instead of walking
; ============================================================================

Function TriggerElevatorFastTravel(destinationRoom$)
	If Not ElevatorFastTravelEnabled Then Return

	ElevatorTransitionActive = True
	ElevatorTransitionTimer = 0.0
	ElevatorDestination = destinationRoom

	; Disable player movement
	CanPlayerMove = False

	DebugLog "Elevator fast travel to: " + destinationRoom
End Function

Function UpdateElevatorFastTravel()
	If Not ElevatorTransitionActive Then Return

	ElevatorTransitionTimer = ElevatorTransitionTimer + FPSfactor

	; Phase 1: Fade to black (0-70 frames = 1 sec)
	If ElevatorTransitionTimer < 70.0 Then
		; Fading out handled in render
		Return
	EndIf

	; Phase 2: Teleport (at 70 frames)
	If ElevatorTransitionTimer >= 70.0 And ElevatorTransitionTimer < 75.0 Then
		TeleportToRoom(ElevatorDestination)
	EndIf

	; Phase 3: Fade in (70-140 frames)
	If ElevatorTransitionTimer >= 140.0 Then
		ElevatorTransitionActive = False
		CanPlayerMove = True
		DebugLog "Elevator fast travel complete"
	EndIf
End Function

Function TeleportToRoom(roomName$)
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Lower(r\RoomTemplate\Name) = Lower(roomName) Then
				PositionEntity Collider, EntityX(r\obj), 0.5, EntityZ(r\obj)
				ResetEntity Collider
				PlayerRoom = r
				DebugLog "Teleported to: " + roomName
				Return
			EndIf
		EndIf
	Next

	DebugLog "WARNING: Teleport room not found: " + roomName
End Function

Function RenderElevatorTransition()
	If Not ElevatorTransitionActive Then Return

	Local alpha# = 0.0

	; Fade out phase
	If ElevatorTransitionTimer < 70.0 Then
		alpha = ElevatorTransitionTimer / 70.0
	; Hold black
	ElseIf ElevatorTransitionTimer < 100.0 Then
		alpha = 1.0
	; Fade in phase
	Else
		alpha = 1.0 - ((ElevatorTransitionTimer - 100.0) / 40.0)
	EndIf

	If alpha > 0.0 Then
		Color 0, 0, 0
		Rect 0, 0, GraphicsWidth(), GraphicsHeight(), True

		; Show loading text
		If alpha > 0.5 Then
			Color 255, 255, 255
			Local txt$ = "Moving..."
			Text GraphicsWidth() / 2 - StringWidth(txt) / 2, GraphicsHeight() / 2, txt
		EndIf
	EndIf
End Function

Function UpdateProjectMirror()
	If Not ProjectMirrorInitialized Then Return
	If Not ProjectMirrorEnabled Then Return

	Local startTime% = MilliSecs()

	; Retry guard spawn if it failed during init
	If MirrorUseCustomSpawn And (Not GuardSpawnComplete) Then
		If SpawnGuardAtCafeteria() Then
			GiveGuardEquipment()
			DebugLog "Guard spawn completed on update"
		EndIf
	EndIf

	; Steve NPC management (Day 1 and 2)
	If CurrentDay < 3 Then
		If Not SteveSpawned Then
			SpawnSteveInCafeteria()
		EndIf
		DisableSCPsBeforeBreach()
		RemoveElevatorCorpses()
		UpdateSteveNPC()
	EndIf

	; Pre-breach lighting (Day 1 and 2)
	If CurrentDay < 3 Then
		SetPreBreachLighting()
	EndIf

	; Elevator fast travel
	UpdateElevatorFastTravel()

	; Story update
	If MirrorStoryEnabled Then
		UpdateDayTransition()
		UpdateDialog()

		; трек игрового времени
		If GStoryState <> Null Then
			GStoryState\playTime = GStoryState\playTime + FPSfactor / 70.0
		EndIf

		; чек триггеров диалогов в текущей комнате
		If PlayerRoom <> Null Then
			CheckDialogTriggers(PlayerRoom)
		EndIf

		; день 2 - катсцена у 173
		UpdateDay2Logic()

		; день 3 - катастрофа
		UpdateDay3Logic()
	EndIf

	; эхо система (день 3)
	If MirrorEchoEnabled Then
		UpdateEchoEvents()
	EndIf

	; 939 - перебираем всех NPC
	If Mirror939Enabled Then
		For n.NPCs = Each NPCs
			If n\NPCtype = NPCtype939 Then
				Integrate939VoiceMimicry(n)
			EndIf
		Next
	EndIf

	; MTF тактика
	If MirrorMTFEnabled Then
		UpdateFoxTactics()
	EndIf

	; VFX
	If MirrorVFXEnabled Then
		UpdateVFXSystem()
	EndIf

	; Cutscenes
	If MirrorCutsceneEnabled Then
		UpdateCutsceneSystem()
	EndIf

	; UI
	If MirrorUIEnabled Then
		UpdateMirrorUI()
	EndIf

	; Gameplay mechanics (stealth, 096, nuke)
	If MirrorGameplayEnabled Then
		UpdateGameplayMechanics()

		; warhead console interaction
		If CurrentAct = ACT_FINALE Then
			CheckWarheadConsoleInteraction()
		EndIf
	EndIf

	; Act title display on act change
	If MirrorUIEnabled And MirrorStoryEnabled Then
		CheckActTitleDisplay()
	EndIf

	MirrorUpdateTime = MilliSecs() - startTime
	MirrorFrameCount = MirrorFrameCount + 1
End Function

; pokazyvaem zagolovok akta pri smene
Global LastDisplayedAct% = 0

Function CheckActTitleDisplay()
	If CurrentDay <> 3 Then Return
	If CurrentAct = LastDisplayedAct Then Return

	LastDisplayedAct = CurrentAct

	Local title$ = ""
	Local subtitle$ = ""

	Select CurrentAct
		Case ACT_AWAKENING
			title = "ACT I"
			subtitle = "AWAKENING IN THE GRAVE"
		Case ACT_ECHO
			title = "ACT II"
			subtitle = "ECHOES OF THE PAST"
		Case ACT_VOICES
			title = "ACT III"
			subtitle = "VOICES OF FRIENDS"
		Case ACT_MACHINE
			title = "ACT IV"
			subtitle = "THE MACHINE AND THE PLAGUE"
		Case ACT_FLOOR
			title = "ACT V"
			subtitle = "LOOK AT THE FLOOR"
		Case ACT_SURFACE
			title = "ACT VI"
			subtitle = "THE SURFACE"
		Case ACT_FINALE
			title = "ACT VII"
			subtitle = "FINALE"
	End Select

	If title <> "" Then
		ShowActTitle(CurrentAct, title, subtitle)
	EndIf
End Function

Function RenderProjectMirror()
	If Not ProjectMirrorInitialized Then Return
	If Not ProjectMirrorEnabled Then Return

	; Reset font to avoid ESC menu issues
	AASetFont Font1

	Local startTime% = MilliSecs()

	; === VFX (первый слой) ===
	If MirrorVFXEnabled Then
		RenderVFXSystem()
	EndIf

	; оверлей перехода дня
	If MirrorStoryEnabled Then
		RenderDayTransition()
		RenderDialog()
		RenderSubtitles()
	EndIf

	; эффекты эха
	If MirrorEchoEnabled Then
		RenderEchoDistortion()
	EndIf

	; статус 914
	If Mirror914SystemEnabled Then
		Render914Status()
	EndIf

	; флешбенг эффект
	If MirrorMTFEnabled Then
		RenderFlashbangEffect()
	EndIf

	; === UI (верхний слой) ===
	If MirrorUIEnabled Then
		RenderMirrorUI()
	EndIf

	; === Nuke countdown ===
	If MirrorGameplayEnabled And NukeCountdownActive Then
		RenderNukeCountdown()
	EndIf

	; === Nuke sequence ===
	If MirrorVFXEnabled And NukeSequenceActive Then
		RenderNukeSequence()
	EndIf

	; === Elevator fast travel transition ===
	If ElevatorTransitionActive Then
		RenderElevatorTransition()
	EndIf

	; дебаг оверлей
	If ProjectMirrorDebugMode Then
		RenderMirrorDebug()
	EndIf

	MirrorRenderTime = MilliSecs() - startTime
End Function

Function RenderNukeCountdown()
	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()

	Local countStr$ = GetNukeCountdownString()
	If countStr = "" Then Return

	; bol'shoi taimer v tsentre verkha
	Color 255, 50, 50

	Local tw% = StringWidth(countStr) * 2  ; uvеlichennyy
	Text gw / 2 - tw / 2, 30, countStr

	; flashing DANGER
	If (MilliSecs() / 500) Mod 2 = 0 Then
		Color 255, 0, 0
		Local warn$ = "!!! WARHEAD ACTIVATED !!!"
		tw = StringWidth(warn)
		Text gw / 2 - tw / 2, 60, warn
	EndIf
End Function

Function CleanupProjectMirror()
	If Not ProjectMirrorInitialized Then Return

	DebugLog "=== MIRROR CLEANUP ==="

	If MirrorGameplayEnabled Then CleanupGameplayMechanics()
	If MirrorUIEnabled Then CleanupMirrorUI()
	If MirrorCutsceneEnabled Then CleanupCutsceneSystem()
	If MirrorVFXEnabled Then CleanupVFXSystem()
	If MirrorMTFEnabled Then CleanupFoxTactics()
	If Mirror914SystemEnabled Then Cleanup914System()
	If Mirror939Enabled Then CleanupVoiceMimicrySystem()
	If MirrorEchoEnabled Then CleanupEchoSystem()
	If MirrorStoryEnabled Then CleanupStorySystem()

	ProjectMirrorInitialized = False
	DebugLog "Cleanup done"
End Function

Function SaveProjectMirrorState(file%)
	If Not ProjectMirrorInitialized Then Return

	WriteInt file, MIRROR_BUILD
	WriteInt file, ProjectMirrorEnabled

	If MirrorStoryEnabled Then SaveStoryState(file)
	If Mirror939Enabled Then SaveVoiceMimicryState(file)
	If Mirror914SystemEnabled Then Save914State(file)
	If MirrorMTFEnabled Then SaveFoxTacticsState(file)
End Function

Function LoadProjectMirrorState(file%)
	Local savedBuild% = ReadInt(file)
	If savedBuild < 20240101 Then
		DebugLog "WARNING: old save format"
	EndIf

	ProjectMirrorEnabled = ReadInt(file)

	If Not ProjectMirrorInitialized Then
		InitProjectMirror()
	EndIf

	If MirrorStoryEnabled Then LoadStoryState(file)
	If Mirror939Enabled Then LoadVoiceMimicryState(file)
	If Mirror914SystemEnabled Then Load914State(file)
	If MirrorMTFEnabled Then LoadFoxTacticsState(file)
End Function

; --- EVENT HOOKS ---
; вызываются из оригинального кода

Function OnMirrorDialogPlayed(soundPath$, speakerName$, room.Rooms)
	If Not ProjectMirrorInitialized Then Return

	If MirrorStoryEnabled And MirrorEchoEnabled Then
		RecordDialogEvent(room, speakerName, soundPath)
	EndIf

	If Mirror939Enabled Then
		OnDialogPlayed(soundPath, speakerName, room)
	EndIf
End Function

Function OnMirrorItemPickup(item.Items)
	If Not ProjectMirrorInitialized Then Return

	If Mirror914SystemEnabled Then
		OnItemPickedUp(item)
	EndIf

	; Day 3: proverka PDA Harrisona
	If MirrorStoryEnabled And CurrentDay = 3 Then
		CheckHarrisonPDAPickup(item)
	EndIf
End Function

Function OnMirror914Use%(item.Items, setting$, x#, y#, z#)
	If Not ProjectMirrorInitialized Then Return False
	If Not Mirror914SystemEnabled Then Return False

	Return Use914Mirror(item, setting, x, y, z)
End Function

Function OnMirrorRoomEnter(room.Rooms)
	If Not ProjectMirrorInitialized Then Return
	If room = Null Then Return

	If Mirror914SystemEnabled Then
		If room\RoomTemplate <> Null Then
			If Instr(Lower(room\RoomTemplate\Name), "914") = 0 Then
				Clear914Status()
			EndIf
		EndIf
	EndIf

	ResetIntakeTracking()

	; чек триггеров
	If MirrorStoryEnabled Then
		CheckDialogTriggers(room)
	EndIf
End Function

Function OnMirrorNPCCreated(n.NPCs)
	If Not ProjectMirrorInitialized Then Return
	If n = Null Then Return

	If Mirror939Enabled And n\NPCtype = NPCtype939 Then
		CreateVoiceState(n)
	EndIf

	If MirrorMTFEnabled And n\NPCtype = NPCtypeMTF Then
		If ActiveFoxSquad = Null Then
			ActiveFoxSquad = CreateFoxSquad()
		EndIf

		Local role% = FOX_ROLE_POINTMAN
		If ActiveFoxSquad\memberCount = 0 Then
			role = FOX_ROLE_LEADER
		ElseIf ActiveFoxSquad\memberCount = 1 Then
			role = FOX_ROLE_FLANKER_L
		ElseIf ActiveFoxSquad\memberCount = 2 Then
			role = FOX_ROLE_FLANKER_R
		ElseIf ActiveFoxSquad\memberCount = 3 Then
			role = FOX_ROLE_SUPPORT
		ElseIf ActiveFoxSquad\memberCount = 4 Then
			role = FOX_ROLE_REAR
		EndIf

		AddFoxToSquad(ActiveFoxSquad, n, role)
	EndIf
End Function

Function OnMirrorSteveMet()
	If Not ProjectMirrorInitialized Then Return

	SetStoryFlag(FLAG_STEVE_MET, 1)

	If Mirror939Enabled Then
		LearnAllSteveVoices()
	EndIf
End Function

Function OnMirrorSteveDead()
	If Not ProjectMirrorInitialized Then Return

	SetStoryFlag(FLAG_STEVE_DEAD, 1)
	ModifyKarma(-15)

	If Mirror939Enabled Then
		For state.SCP939VoiceState = Each SCP939VoiceState
			state\preferredCategory = VOICE_CAT_STEVE
			state\aggressionLevel = Min(state\aggressionLevel + 30, 100)
		Next
	EndIf
End Function

Function OnMirrorSteveSaved()
	If Not ProjectMirrorInitialized Then Return

	SetStoryFlag(FLAG_STEVE_SAVED, 1)
	ModifyKarma(20)
End Function

; --- DEBUG ---

Function RenderMirrorDebug()
	Color 255, 255, 255
	Text 10, 400, "=== MIRROR v" + MIRROR_VERSION + " ==="
	Text 10, 415, "Update: " + MirrorUpdateTime + "ms  Render: " + MirrorRenderTime + "ms"
	Text 10, 430, "Frame: " + MirrorFrameCount

	If KeyHit(2) Then DebugStoryState()
	If KeyHit(3) Then DebugEchoSystem()
	If KeyHit(4) Then DebugVoiceMimicry()
	If KeyHit(5) Then Debug914System()
	If KeyHit(6) Then DebugFoxTactics()

	Color 200, 200, 0
	Text 10, 450, "Day: " + CurrentDay + "  Karma: " + CurrentKarma + "  Branch: " + GetBranchName(StoryBranch)

	; Day 3 specific
	If CurrentDay = 3 Then
		Color 255, 100, 100
		Text 10, 465, "ACT: " + GetActName(CurrentAct) + " (" + CurrentAct + ")"
		Text 10, 480, "Sanity: " + PlayerSanity + "% (Lvl " + GetSanityLevel() + ")"

		; gameplay states
		Color 200, 150, 50
		Local stealthStr$ = "Crouch:" + PlayerCrouching + " Noise:" + Int(PlayerNoiseLevel * 100) + "%"
		Text 10, 495, stealthStr

		If NukeCountdownActive Then
			Color 255, 50, 50
			Text 10, 510, "NUKE: " + GetNukeCountdownString()
		EndIf

		If SCP096Enraged Then
			Color 255, 0, 0
			Text 10, 525, "096 ENRAGED!"
		EndIf
	EndIf

	; flags
	Color 150, 150, 150
	Local flagsStr$ = ""
	If GetStoryFlag(FLAG_STEVE_MET) Then flagsStr = flagsStr + "SM "
	If GetStoryFlag(FLAG_STEVE_DEAD) Then flagsStr = flagsStr + "SD "
	If GetStoryFlag(FLAG_HARRISON_PDA) Then flagsStr = flagsStr + "HP "
	If GetStoryFlag(FLAG_ACT4_UPGRADED_CARD) Then flagsStr = flagsStr + "O5 "
	If GetStoryFlag(FLAG_DAY3_STARTED) Then flagsStr = flagsStr + "D3 "
	If GetStoryFlag(FLAG_ACT6_MTF_BETRAYAL) Then flagsStr = flagsStr + "BETRAY "
	If GetStoryFlag(FLAG_NUKE_ACTIVATED) Then flagsStr = flagsStr + "NUKE "

	Text 10, 545, "Flags: " + flagsStr

	; cutscene
	If CutsceneActive Then
		Color 100, 200, 100
		Text 10, 560, "CUTSCENE: " + CutsceneID + " Timer:" + Int(CutsceneTimer)
	EndIf
End Function

Function ToggleMirrorDebug()
	ProjectMirrorDebugMode = Not ProjectMirrorDebugMode
End Function

; --- UTILITY ---

Function GetMirrorVersion$()
	Return MIRROR_VERSION
End Function

Function GetMirrorBuild%()
	Return MIRROR_BUILD
End Function

Function IsMirrorInitialized%()
	Return ProjectMirrorInitialized
End Function

Function SetMirrorEnabled(enabled%)
	ProjectMirrorEnabled = enabled
End Function

Function SetMirrorFeature(feature$, enabled%)
	Select Lower(feature)
		Case "story"
			MirrorStoryEnabled = enabled
		Case "echo"
			MirrorEchoEnabled = enabled
		Case "939", "voice"
			Mirror939Enabled = enabled
		Case "914", "quest"
			Mirror914SystemEnabled = enabled
		Case "mtf", "fox"
			MirrorMTFEnabled = enabled
	End Select
End Function

; --- CONSOLE COMMANDS ---

Function ProcessMirrorCommand%(cmd$)
	Local parts$ = Lower(Trim$(cmd))

	If Left$(parts, 10) = "mirror_day" Then
		Local dayStr$ = Trim$(Mid$(parts, 11))
		Local newDay% = Int(dayStr)
		If newDay >= 1 And newDay <= 3 Then
			TriggerDayTransition(newDay)
			Return True
		EndIf
	EndIf

	If Left$(parts, 12) = "mirror_karma" Then
		Local karmaStr$ = Trim$(Mid$(parts, 13))
		ModifyKarma(Int(karmaStr))
		Return True
	EndIf

	If Left$(parts, 11) = "mirror_flag" Then
		Local flagParts$ = Trim$(Mid$(parts, 12))
		Local spacePos% = Instr(flagParts, " ")
		If spacePos > 0 Then
			Local flagIdx% = Int(Left$(flagParts, spacePos - 1))
			Local flagVal% = Int(Mid$(flagParts, spacePos + 1))
			SetStoryFlag(flagIdx, flagVal)
			Return True
		EndIf
	EndIf

	If parts = "mirror_debug" Then
		ToggleMirrorDebug()
		Return True
	EndIf

	If Left$(parts, 11) = "mirror_echo" Then
		Local echoId% = Int(Trim$(Mid$(parts, 12)))
		TriggerEchoByID(echoId)
		Return True
	EndIf

	If parts = "mirror_flash" Then
		TriggerFlashbangEffect(1.0)
		Return True
	EndIf

	If parts = "mirror_breach" Then
		TriggerBreachSequence()
		Return True
	EndIf

	If parts = "mirror_nuke" Then
		ArmNuke()
		Return True
	EndIf

	If Left$(parts, 10) = "mirror_act" Then
		Local actStr$ = Trim$(Mid$(parts, 11))
		Local newAct% = Int(actStr)
		ForceActChange(newAct)
		Return True
	EndIf

	If Left$(parts, 13) = "mirror_sanity" Then
		Local sanStr$ = Trim$(Mid$(parts, 14))
		PlayerSanity = Int(sanStr)
		Return True
	EndIf

	If Left$(parts, 13) = "mirror_ending" Then
		Local endStr$ = Trim$(Mid$(parts, 14))
		TriggerEnding(Int(endStr))
		Return True
	EndIf

	If parts = "mirror_096" Then
		Trigger096Rage()
		Return True
	EndIf

	If parts = "mirror_mtf_betray" Then
		TriggerMTFBetrayal()
		Return True
	EndIf

	If parts = "mirror_lockdown" Then
		LockdownAllSectors()
		Return True
	EndIf

	Return False
End Function

Function ForceActChange(act%)
	If act < 1 Or act > 7 Then Return

	CurrentAct = act

	; ustanavlivaem sootvetstvuyushchie flagi
	Select act
		Case ACT_ECHO
			SetStoryFlag(FLAG_ACT1_RADIO_LOOP, 1)
		Case ACT_VOICES
			SetStoryFlag(FLAG_ACT2_HEARD_STEVE_LAST, 1)
		Case ACT_MACHINE
			SetStoryFlag(FLAG_ACT3_READ_MIRROR_LOG, 1)
		Case ACT_FLOOR
			SetStoryFlag(FLAG_ACT4_UPGRADED_CARD, 1)
		Case ACT_SURFACE
			SetStoryFlag(FLAG_ACT5_ELEVATOR_ESCAPE, 1)
		Case ACT_FINALE
			SetStoryFlag(FLAG_ACT6_MTF_BETRAYAL, 1)
	End Select

	ShowActTitle(act, "ACT " + act, GetActName(act))
End Function

Function SetupMTFFoxSquad()
	Local squad.MTFFoxSquad = CreateFoxSquad()
	ActiveFoxSquad = squad
End Function
