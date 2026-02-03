; Project Mirror - Gameplay Mechanics
; Crouch stealth, 096 avoidance, Tesla gates, MTF AI, Nuke silo

; === STEALTH SYSTEM ===
Global PlayerCrouching% = False
Global PlayerNoiseLevel# = 0.0
Global PlayerVisible% = True
Global StealthMeterValue# = 0.0

; === 096 MECHANIC ===
Global Player096LookingAt% = False
Global Player096LookTimer# = 0.0
Global Player096Triggered% = False
Global SCP096Entity.NPCs = Null
Global SCP096Enraged% = False

; === TESLA GATE CONTROL ===
Const MAX_TESLA_GATES% = 16
Type TeslaGateControl
	Field roomName$
	Field entity%
	Field enabled%
	Field timer#
	Field manualOverride%
End Type

Global TeslaGateCount% = 0

; === NUKE SILO ===
Global NukeSiloUnlocked% = False
Global NukeArmed% = False
Global NukeCountdown# = 0.0
Global NukeCountdownActive% = False
Global NukeFirstKeyInserted% = False
Global NukeSecondKeyInserted% = False

; warhead control room
Global WarheadRoomFound% = False
Global WarheadConsoleEntity% = 0

; === MTF BEHAVIOR ===
Const MTF_STATE_PATROL% = 0
Const MTF_STATE_SEARCH% = 1
Const MTF_STATE_CHASE% = 2
Const MTF_STATE_ATTACK% = 3
Const MTF_STATE_BETRAY% = 4

Global MTFBetrayalTriggered% = False
Global MTFAlertLevel# = 0.0

; === GUARD FRIENDSHIP SYSTEM ===
; Markus is a guard - other guards should be friendly to him
; MTF however are hostile (Day 3 - they hunt everyone during containment breach)
Global GuardFriendshipEnabled% = True

; === LOCKDOWN SYSTEM ===
Global FacilityLockdown% = False
Dim LockedSectors%(8)

Function InitGameplayMechanics()
	PlayerCrouching = False
	PlayerNoiseLevel = 0.0
	PlayerVisible = True

	Player096LookingAt = False
	Player096Triggered = False
	SCP096Enraged = False

	NukeSiloUnlocked = False
	NukeArmed = False
	NukeCountdownActive = False

	MTFBetrayalTriggered = False
	MTFAlertLevel = 0.0

	FacilityLockdown = False

	; Markus is a guard - enable friendly guards
	GuardFriendshipEnabled = True

	For i% = 0 To 7
		LockedSectors(i) = 0
	Next

	; naydyom tesla gates
	FindAllTeslaGates()

	; make existing guards friendly immediately
	MakeAllGuardsFriendly()

	DebugLog "Gameplay mechanics initialized"
End Function

; ============================================================================
; GUARD FRIENDSHIP SYSTEM
; Markus is a security guard - other guards recognize him as a colleague
; Guards will NOT attack the player (they're fellow guards)
; MTF are different - they follow orders from higher-ups and WILL attack during breach
; ============================================================================

Function UpdateGuardFriendship()
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeGuard Then
			; Check if guard is in hostile state towards player
			; State 1 = aiming and shooting at player
			; State 11 = shooting while patrolling
			If n\State = 1 Or n\State = 11 Then
				; Force guard to friendly idle state
				; State 4 = idle, looking around (non-hostile)
				; State 7 = stationary guard (non-hostile)
				n\State = 4
				n\CurrSpeed = 0.0

				; Reset any targeting on player
				n\State3 = 0  ; clear "spotted player" flag

				DebugLog "Guard made friendly - was in hostile state"
			EndIf

			; Also prevent guards from entering hostile state
			; by clearing their enemy tracking if targeting player position
			Local dist# = EntityDistance(n\Collider, Collider)
			If dist < 15.0 Then
				; Guard is close - make sure they stay friendly
				; Don't let them track player as enemy
				If n\EnemyX = EntityX(Collider) And n\EnemyZ = EntityZ(Collider) Then
					n\EnemyX = 0.0
					n\EnemyY = 0.0
					n\EnemyZ = 0.0
				EndIf
			EndIf
		EndIf

		; Note: MTF (NPCtypeMTF) are NOT affected
		; They follow Foundation orders and will attack during breach
		; This is intentional - MTF are the antagonists in Day 3
	Next
End Function

Function MakeAllGuardsFriendly()
	; Called at init - ensure all guards start in friendly state
	Local guardCount% = 0

	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeGuard Then
			; Set to friendly idle state
			If n\State = 1 Or n\State = 11 Then
				n\State = 4  ; idle
			EndIf

			; Clear any player targeting
			n\State3 = 0

			guardCount = guardCount + 1
		EndIf
	Next

	If guardCount > 0 Then
		DebugLog "Made " + guardCount + " guards friendly to Markus"
	EndIf
End Function

Function SetGuardFriendship(enabled%)
	GuardFriendshipEnabled = enabled

	If enabled Then
		MakeAllGuardsFriendly()
		DebugLog "Guard friendship ENABLED - guards are now friendly"
	Else
		DebugLog "Guard friendship DISABLED - guards may be hostile"
	EndIf
End Function

Function UpdateGameplayMechanics()
	; guard friendship - Markus is a guard, other guards are friendly
	If GuardFriendshipEnabled Then
		UpdateGuardFriendship()
	EndIf

	; stealth
	UpdateStealthSystem()

	; 096 mechanic
	Update096Mechanic()

	; tesla gates
	UpdateTeslaGates()

	; nuke countdown
	If NukeCountdownActive Then
		UpdateNukeCountdown()
	EndIf

	; MTF behavior
	UpdateMTFBehavior()

	; Elevator fast travel (Day 1 and 2 only)
	If ElevatorFastTravelEnabled And CurrentDay < 3 Then
		CheckElevatorFastTravel()
	EndIf
End Function

; ============================================================================
; STEALTH SYSTEM (dlya 939 zony)
; ============================================================================

Function UpdateStealthSystem()
	; crouch detection
	If KeyDown(29) Or KeyDown(157) Then  ; Ctrl
		If Not PlayerCrouching Then
			PlayerCrouching = True
			; umenshaem vysotu kollidera
			ScaleEntity Collider, 1.0, 0.6, 1.0
		EndIf
	Else
		If PlayerCrouching Then
			PlayerCrouching = False
			ScaleEntity Collider, 1.0, 1.0, 1.0
		EndIf
	EndIf

	; noise level calculation
	PlayerNoiseLevel = 0.0

	; beg = bol'shoy shum
	If KeyDown(42) Or KeyDown(54) Then  ; Shift (sprint)
		If Not PlayerCrouching Then
			PlayerNoiseLevel = PlayerNoiseLevel + 1.0
		EndIf
	EndIf

	; khodba = sredniy shum
	If KeyDown(17) Or KeyDown(31) Or KeyDown(30) Or KeyDown(32) Then  ; WASD
		If PlayerCrouching Then
			PlayerNoiseLevel = PlayerNoiseLevel + 0.1
		Else
			PlayerNoiseLevel = PlayerNoiseLevel + 0.4
		EndIf
	EndIf

	; stealth meter dlya UI
	StealthMeterValue = 1.0 - PlayerNoiseLevel
	If StealthMeterValue < 0.0 Then StealthMeterValue = 0.0

	; soobshchaem 939 o shume
	If PlayerNoiseLevel > 0.3 And CurrentAct = ACT_VOICES Then
		Alert939ToNoise(PlayerNoiseLevel)
	EndIf
End Function

Function Alert939ToNoise(noiseLevel#)
	; naydyom vsekh 939 i soobshchim im pozitsiyu igroka
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtype939 Then
			Local dist# = EntityDistance(n\Collider, Collider)
			Local hearingRange# = 15.0 * noiseLevel

			If dist < hearingRange Then
				; 939 slyshit igroka
				n\EnemyX = EntityX(Collider)
				n\EnemyY = EntityY(Collider)
				n\EnemyZ = EntityZ(Collider)
				n\State = 1  ; search state

				; esli ochen' gromko - agressiya
				If noiseLevel > 0.7 And dist < 5.0 Then
					n\State = 2  ; attack
				EndIf
			EndIf
		EndIf
	Next
End Function

Function IsPlayerHiddenFrom939%()
	If PlayerCrouching And PlayerNoiseLevel < 0.2 Then
		Return True
	EndIf
	Return False
End Function

; ============================================================================
; SCP-096 MECHANIC
; ============================================================================

Function Update096Mechanic()
	If SCP096Entity = Null Then
		Find096Entity()
	EndIf

	If SCP096Entity = Null Then Return
	If SCP096Enraged Then Return  ; uzhe triggered

	; proveryaem smotrit li igrok na 096
	Player096LookingAt = IsLookingAt096()

	If Player096LookingAt Then
		Player096LookTimer = Player096LookTimer + FPSfactor

		; 0.5 sekundy vzglyada = trigger
		If Player096LookTimer > 35.0 Then
			Trigger096Rage()
		EndIf

		; preduprezhdenie
		If Player096LookTimer > 15.0 And Player096LookTimer < 20.0 Then
			AddNotification("DON'T LOOK AT IT!")
		EndIf
	Else
		Player096LookTimer = Max(Player096LookTimer - FPSfactor * 2.0, 0.0)
	EndIf
End Function

Function Find096Entity()
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtype096 Then
			SCP096Entity = n
			Exit
		EndIf
	Next
End Function

Function IsLookingAt096%()
	If SCP096Entity = Null Then Return False

	Local dx# = EntityX(SCP096Entity\Collider) - EntityX(Collider)
	Local dy# = EntityY(SCP096Entity\Collider) - EntityY(Collider)
	Local dz# = EntityZ(SCP096Entity\Collider) - EntityZ(Collider)

	Local dist# = Sqr(dx * dx + dy * dy + dz * dz)
	If dist > 20.0 Then Return False  ; slishkom daleko

	; ugol mezhdu vzglyadom igroka i 096
	Local angleToTarget# = ATan2(dx, dz)
	Local playerYaw# = EntityYaw(Collider)

	Local angleDiff# = Abs(angleToTarget - playerYaw)
	While angleDiff > 180.0
		angleDiff = angleDiff - 360.0
	Wend
	angleDiff = Abs(angleDiff)

	; proveryaem pitch (nel'zya smotret' v pol)
	If CameraPitch > 45.0 Then Return False  ; smotrit v pol
	If CameraPitch < -60.0 Then Return False  ; smotrit vverkh

	; esli ugol men'she 30 gradusov - smotrim na nego
	If angleDiff < 30.0 Then
		; raycast dlya proverki vidimosti
		Local visible% = LinePick(EntityX(Collider), EntityY(Collider) + 1.5, EntityZ(Collider), dx, dy, dz)
		If visible = 0 Or PickedEntity() = SCP096Entity\Collider Then
			Return True
		EndIf
	EndIf

	Return False
End Function

Function Trigger096Rage()
	If SCP096Enraged Then Return

	SCP096Enraged = True
	Player096Triggered = True

	SetStoryFlag(FLAG_ACT5_079_TROLLED, 1)

	; 096 stanovitsya agressivnym
	If SCP096Entity <> Null Then
		SCP096Entity\State = 2  ; rage state
	EndIf

	; vizualnye effecty
	TriggerScreenShake(10.0, 5.0)
	StartVFXEffect(VFX_PSYCHO, 70.0, 0.8)

	; zvuk krika
	PlaySound LoadSound("SFX\SCP\096\Scream.ogg")

	AddNotification("096 AKTIVIROVAN!")

	; sanity udar
	ModifySanity(30)
End Function

Function Force096TriggerByMonitor()
	; 079 pokazyvaet litso 096 na monitore
	If SCP096Entity <> Null Then
		; trigger neizbezhен
		Trigger096Rage()
		StartDialog(284)  ; "Ups."
	EndIf
End Function

; ============================================================================
; TESLA GATES CONTROL
; ============================================================================

Function FindAllTeslaGates()
	; nakhodymo vse tesla gates v igre
	TeslaGateCount = 0

	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Instr(r\RoomTemplate\Name, "tesla") > 0 Then
				Local tg.TeslaGateControl = New TeslaGateControl
				tg\roomName = r\RoomTemplate\Name
				tg\entity = r\obj
				tg\enabled = True
				tg\timer = 0.0
				tg\manualOverride = False

				TeslaGateCount = TeslaGateCount + 1
			EndIf
		EndIf
	Next

	DebugLog "Found " + TeslaGateCount + " Tesla gates"
End Function

Function UpdateTeslaGates()
	For tg.TeslaGateControl = Each TeslaGateControl
		If tg\manualOverride Then
			; ruchnoy kontrol'
			tg\timer = tg\timer + FPSfactor
		EndIf
	Next
End Function

Function ActivateTeslaGate(roomName$)
	For tg.TeslaGateControl = Each TeslaGateControl
		If tg\roomName = roomName Then
			tg\enabled = True
			tg\manualOverride = True
			Exit
		EndIf
	Next
End Function

Function DeactivateTeslaGate(roomName$)
	For tg.TeslaGateControl = Each TeslaGateControl
		If tg\roomName = roomName Then
			tg\enabled = False
			Exit
		EndIf
	Next
End Function

Function ActivateAllTeslaGates()
	For tg.TeslaGateControl = Each TeslaGateControl
		tg\enabled = True
		tg\manualOverride = True
	Next

	AddNotification("Vse Tesla-vorota aktivirovany")
End Function

Function TeslaGateKillNPCsInRoom(roomName$)
	; ubivaem vsekh NPC v komnate s tesla gate
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeZombie Or n\NPCtype = NPCtypeMTF Then
			; proveryaem nahoditsya li v komnate
			For r.Rooms = Each Rooms
				If r\RoomTemplate <> Null Then
					If r\RoomTemplate\Name = roomName Then
						Local dist# = EntityDistance(n\Collider, r\obj)
						If dist < 5.0 Then
							; kill
							n\State = 6  ; dead
							n\IsDead = True

							DebugLog "Tesla gate killed: " + n\NPCtype
						EndIf
					EndIf
				EndIf
			Next
		EndIf
	Next
End Function

; ============================================================================
; NUKE SILO / WARHEAD
; ============================================================================

Function UnlockNukeSilo()
	NukeSiloUnlocked = True
	AddNotification "Dostup k silosu boegolovki otkryt"
End Function

Function InsertNukeKey(keyNum%)
	If keyNum = 1 Then
		NukeFirstKeyInserted = True
		AddNotification("Pervyi klyuch avtorizatsii vstavlen")
	ElseIf keyNum = 2 Then
		NukeSecondKeyInserted = True
		AddNotification("Vtoroi klyuch avtorizatsii vstavlen")
		SetStoryFlag(FLAG_USED_STEVE_BADGE, 1)
	EndIf

	If NukeFirstKeyInserted And NukeSecondKeyInserted Then
		; mozhno aktivirovat'
		ShowInteractionPrompt("Aktivirovat' boegolovku", "F")
	EndIf
End Function

Function ArmNuke()
	If (Not NukeFirstKeyInserted) Or (Not NukeSecondKeyInserted) Then
		AddNotification("Trebuetsya dva klyucha avtorizatsii")
		Return
	EndIf

	NukeArmed = True
	SetStoryFlag(FLAG_NUKE_ACTIVATED, 1)

	; nachalo otschyota
	StartNukeCountdown(90.0)  ; 90 sekund

	; blokiruem vse sektora
	LockdownAllSectors()

	AddNotification("ALPHA WARHEAD ACTIVATED")
End Function

Function StartNukeCountdown(seconds#)
	NukeCountdownActive = True
	NukeCountdown = seconds * 70.0  ; v FPSfactor units

	; zvuk sireny
	PlaySound LoadSound("SFX\Room\Nuke\Alarm.ogg")
End Function

Function UpdateNukeCountdown()
	If Not NukeCountdownActive Then Return

	NukeCountdown = NukeCountdown - FPSfactor

	; kazhdye 10 sekund - announce
	Local secondsLeft% = Int(NukeCountdown / 70.0)

	If NukeCountdown <= 0.0 Then
		; BOOM
		TriggerNukeDetonation()
	EndIf
End Function

Function TriggerNukeDetonation()
	NukeCountdownActive = False

	; belaya vspyshka
	TriggerWhiteFlash()

	; ending sequence
	StartEndingSequence(4)  ; Zero Protocol

	; kill all
	For n.NPCs = Each NPCs
		n\State = 6
		n\IsDead = True
	Next
End Function

Function GetNukeCountdownString$()
	If Not NukeCountdownActive Then Return ""

	Local seconds% = Int(NukeCountdown / 70.0)
	Local mins% = seconds / 60
	Local secs% = seconds Mod 60

	Local secStr$ = secs
	If secs < 10 Then secStr = "0" + secs

	Return "T-" + mins + ":" + secStr
End Function

; ============================================================================
; LOCKDOWN SYSTEM
; ============================================================================

Function LockdownAllSectors()
	FacilityLockdown = True

	For i% = 0 To 7
		LockedSectors(i) = 1
	Next

	; zakryvaem vse dveri
	For d.Doors = Each Doors
		If d\locked = False Then
			d\locked = 2  ; lockdown lock
		EndIf
	Next

	; soobshchaem MTF
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeMTF Then
			n\State = MTF_STATE_SEARCH  ; panika
		EndIf
	Next

	AddNotification("LOCKDOWN VSEKH SEKTOROV")
	StartDialog(357)  ; MTF radio panika
End Function

Function UnlockSector(sectorNum%)
	If sectorNum >= 0 And sectorNum < 8 Then
		LockedSectors(sectorNum) = 0
	EndIf
End Function

Function IsSectorLocked%(sectorNum%)
	If sectorNum >= 0 And sectorNum < 8 Then
		Return LockedSectors(sectorNum)
	EndIf
	Return 0
End Function

; ============================================================================
; MTF BEHAVIOR
; ============================================================================

Function UpdateMTFBehavior()
	If CurrentDay <> 3 Then Return
	If CurrentAct < ACT_SURFACE Then Return

	; proverka betrayal
	If GetStoryFlag(FLAG_ACT6_MTF_BETRAYAL) And (Not MTFBetrayalTriggered) Then
		TriggerMTFBetrayal()
	EndIf

	; update kazhdogo MTF
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeMTF Then
			UpdateSingleMTF(n)
		EndIf
	Next
End Function

Function TriggerMTFBetrayal()
	MTFBetrayalTriggered = True

	; vse MTF stanovyatsya vrazhdebnymi
	For n.NPCs = Each NPCs
		If n\NPCtype = NPCtypeMTF Then
			n\State = MTF_STATE_BETRAY
			n\EnemyX = EntityX(Collider)
			n\EnemyY = EntityY(Collider)
			n\EnemyZ = EntityZ(Collider)
		EndIf
	Next

	AddNotification("MTF POLUCHILI PRIKAZ NA USTRANENIE")
End Function

Function UpdateSingleMTF(n.NPCs)
	Select n\State
		Case MTF_STATE_PATROL
			; obychnyi patrol
			; upravlyaetsya osnovnoy igroy

		Case MTF_STATE_SEARCH
			; ishchut igroka
			; dvigayutsya k posledney izvestnoy pozitsii

		Case MTF_STATE_CHASE
			; presleduyut
			n\EnemyX = EntityX(Collider)
			n\EnemyY = EntityY(Collider)
			n\EnemyZ = EntityZ(Collider)

		Case MTF_STATE_BETRAY
			; activno okhotaytsya na igroka
			n\EnemyX = EntityX(Collider)
			n\EnemyY = EntityY(Collider)
			n\EnemyZ = EntityZ(Collider)

			; strelyayut esli vidyat
			Local dist# = EntityDistance(n\Collider, Collider)
			If dist < 30.0 Then
				; proverka line of sight
				If EntityVisible(n\Collider, Collider) Then
					; fire!
					; (upravlyaetsya osnovnoy igroy)
				EndIf
			EndIf
	End Select
End Function

Function SpawnMTFSquad(x#, y#, z#, count%)
	For i% = 0 To count - 1
		Local offsetX# = Sin(i * 90.0) * 2.0
		Local offsetZ# = Cos(i * 90.0) * 2.0

		Local mtf.NPCs = CreateNPC(NPCtypeMTF, x + offsetX, y, z + offsetZ)
		If mtf <> Null Then
			If MTFBetrayalTriggered Then
				mtf\State = MTF_STATE_BETRAY
			Else
				mtf\State = MTF_STATE_PATROL
			EndIf
		EndIf
	Next
End Function

; ============================================================================
; INTERACTION CHECKS
; ============================================================================

Function CheckWarheadConsoleInteraction()
	; proveryaem blizost' k konsoli boegolovki
	If Not WarheadRoomFound Then
		FindWarheadRoom()
	EndIf

	If WarheadConsoleEntity <> 0 Then
		Local dist# = EntityDistance(Collider, WarheadConsoleEntity)
		If dist < 2.0 Then
			If Not NukeFirstKeyInserted Then
				If HasO5Card() Then
					ShowInteractionPrompt("Vstavit' kartu O5", "E")
					If KeyHit(18) Then  ; E
						InsertNukeKey(1)
					EndIf
				Else
					ShowInteractionPrompt("Trebuetsya karta O5", "")
				EndIf
			ElseIf Not NukeSecondKeyInserted Then
				If HasSteveBadge() Then
					ShowInteractionPrompt("Ispolzovat' beidzh Stiva", "E")
					If KeyHit(18) Then
						InsertNukeKey(2)
					EndIf
				Else
					ShowInteractionPrompt("Trebuetsya vtoroy klyuch", "")
				EndIf
			ElseIf Not NukeArmed Then
				ShowInteractionPrompt("AKTIVIROVAT' BOEGOLOVKU", "F")
				If KeyHit(33) Then  ; F
					ArmNuke()
				EndIf
			EndIf
		Else
			HideInteractionPrompt()
		EndIf
	EndIf
End Function

Function FindWarheadRoom()
	For r.Rooms = Each Rooms
		If r\RoomTemplate <> Null Then
			If Instr(Lower(r\RoomTemplate\Name), "nuke") > 0 Or Instr(Lower(r\RoomTemplate\Name), "warhead") > 0 Then
				WarheadConsoleEntity = r\obj
				WarheadRoomFound = True
				Exit
			EndIf
		EndIf
	Next
End Function

Function HasO5Card%()
	; proverka nalichiya karty O5
	If GetStoryFlag(FLAG_ACT4_UPGRADED_CARD) Then Return True
	Return False
End Function

Function HasSteveBadge%()
	; proverka nalichiya beidzhika Stiva
	If GetStoryFlag(FLAG_ACT2_FOUND_DICTAPHONE) Then Return True
	Return False
End Function

; ============================================================================
; ELEVATOR FAST TRAVEL
; For Day 1 and 2 - skip the long walks through the facility
; ============================================================================

Function CheckElevatorFastTravel()
	If CurrentDay = 3 Then Return  ; Day 3 = full exploration needed
	If ElevatorTransitionActive Then Return

	; Check if player is in an elevator room
	If PlayerRoom = Null Then Return
	If PlayerRoom\RoomTemplate = Null Then Return

	Local roomName$ = Lower(PlayerRoom\RoomTemplate\Name)

	; Check for elevator rooms
	If Instr(roomName, "elevator") > 0 Or Instr(roomName, "lift") > 0 Then
		; Show fast travel prompt
		ShowInteractionPrompt("Use elevator (Fast Travel)", "F")

		; Check for F key press
		If KeyHit(33) Then  ; F key
			; Determine destination based on current objectives
			Local destination$ = GetElevatorDestination()
			If destination <> "" Then
				TriggerElevatorFastTravel(destination)
			EndIf
		EndIf
	EndIf
End Function

Function GetElevatorDestination$()
	; Day 1 destinations - proper sequence
	If CurrentDay = 1 Then
		If GetStoryFlag(FLAG_COFFEE_WITH_STEVE) = 1 And GetStoryFlag(FLAG_SAW_HELICOPTERS) = 0 Then
			; After coffee - go see helicopters at Gate B area
			SetStoryFlag(FLAG_SAW_HELICOPTERS, 1)  ; Mark as seen
			Return "exit1"  ; Gate B area (helipad)
		ElseIf GetStoryFlag(FLAG_SAW_HELICOPTERS) = 1 And GetStoryFlag(FLAG_ESCORTED_DCLASS) = 0 Then
			; After helicopters - go to D-Class cells
			Return "room2closets"  ; D-Class cells
		ElseIf GetStoryFlag(FLAG_ESCORTED_DCLASS) = 1 And GetStoryFlag(FLAG_SAW_999) = 0 Then
			; Escort D-Class to 999
			Return "room2sl"  ; Light containment with 999
		ElseIf GetStoryFlag(FLAG_SAW_999) = 1 Then
			; Back to cafeteria for evening
			Return "room2cafeteria"
		EndIf
	EndIf

	; Day 2 destinations
	If CurrentDay = 2 Then
		If GetStoryFlag(FLAG_DAY2_STARTED) = 1 And GetStoryFlag(FLAG_AT_173_CHAMBER) = 0 Then
			Return "room173"  ; 173 chamber
		ElseIf GetStoryFlag(FLAG_PROCEDURE_COMPLETE) = 1 Then
			Return "room2dorm"  ; Dorms
		EndIf
	EndIf

	Return ""
End Function

; ============================================================================
; CLEANUP
; ============================================================================

Function CleanupGameplayMechanics()
	For tg.TeslaGateControl = Each TeslaGateControl
		Delete tg
	Next

	TeslaGateCount = 0
	SCP096Entity = Null
	PlayerCrouching = False
	NukeCountdownActive = False
End Function
