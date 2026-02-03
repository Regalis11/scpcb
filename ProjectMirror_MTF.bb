; -----------------------------------------------
; Project Mirror - MTF "Лисицы" тактический AI
; Окружение, флэшбанги, LOS-проверки
; -----------------------------------------------

; Размер отряда
Const MTF_FOX_MAX_SQUAD% = 6
Const MTF_FOX_FLANK_DISTANCE# = 8.0
Const MTF_FOX_ENGAGE_DISTANCE# = 15.0
Const MTF_FOX_ENGAGE_DISTANCE_SQ# = 225.0
Const MTF_FOX_CLOSE_DISTANCE# = 4.0
Const MTF_FOX_CLOSE_DISTANCE_SQ# = 16.0

; Тактические состояния
Const FOX_STATE_PATROL% = 0
Const FOX_STATE_ALERT% = 1
Const FOX_STATE_FLANKING% = 2
Const FOX_STATE_ENGAGING% = 3
Const FOX_STATE_FLASHBANG% = 4
Const FOX_STATE_BREACH% = 5
Const FOX_STATE_CONTAIN% = 6

; Роли в отряде
Const FOX_ROLE_LEADER% = 0
Const FOX_ROLE_POINTMAN% = 1
Const FOX_ROLE_FLANKER_L% = 2
Const FOX_ROLE_FLANKER_R% = 3
Const FOX_ROLE_REAR% = 4
Const FOX_ROLE_SUPPORT% = 5

; Флэшбанг
Const FLASHBANG_RANGE# = 6.0
Const FLASHBANG_RANGE_SQ# = 36.0
Const FLASHBANG_DURATION# = 280.0  ; 4 сек
Const FLASHBANG_STUN_DURATION# = 140.0  ; 2 сек

; LOS
Const LOS_CHECK_INTERVAL# = 7.0
Const SHADOW_THRESHOLD# = 0.3

; Состояние бойца
Type MTFFoxState
	Field npcRef.NPCs
	Field tacticalState%
	Field role%
	Field squad.MTFFoxSquad
	Field targetX#, targetY#, targetZ#
	Field flankAngle#
	Field coverPoint%
	Field stateTimer#
	Field losCheckTimer#
	Field flashbangCooldown#
	Field repositionTimer#
	Field hasLOS%
	Field lastSeenX#, lastSeenY#, lastSeenZ#
	Field lastSeenTime%
	Field playerInShadow%
	Field signalSent%
	Field waitingForSignal%
End Type

; Отряд
Type MTFFoxSquad
	Field id%
	Field leader.MTFFoxState
	; members array replaced with individual fields (Blitz3D limitation)
	Field member0.MTFFoxState
	Field member1.MTFFoxState
	Field member2.MTFFoxState
	Field member3.MTFFoxState
	Field member4.MTFFoxState
	Field member5.MTFFoxState
	Field memberCount%
	Field alertLevel%
	Field playerLastKnownX#
	Field playerLastKnownY#
	Field playerLastKnownZ#
	Field engagementActive%
	Field encirclementPhase%   ; 0=нет, 1=расходимся, 2=сжимаем
	Field flashbangQueued%
	Field flashbangUser.MTFFoxState
	Field breachTarget.Doors
End Type

; Helper functions for squad member access (Blitz3D can't use arrays in Types)
Function GetSquadMember.MTFFoxState(squad.MTFFoxSquad, idx%)
	If squad = Null Then Return Null
	Select idx
		Case 0: Return squad\member0
		Case 1: Return squad\member1
		Case 2: Return squad\member2
		Case 3: Return squad\member3
		Case 4: Return squad\member4
		Case 5: Return squad\member5
	End Select
	Return Null
End Function

Function SetSquadMember(squad.MTFFoxSquad, idx%, fox.MTFFoxState)
	If squad = Null Then Return
	Select idx
		Case 0: squad\member0 = fox
		Case 1: squad\member1 = fox
		Case 2: squad\member2 = fox
		Case 3: squad\member3 = fox
		Case 4: squad\member4 = fox
		Case 5: squad\member5 = fox
	End Select
End Function

; Глобалы
Global FoxTacticsEnabled% = True
Global FoxSquadCount% = 0
Global ActiveFoxSquad.MTFFoxSquad = Null

; Флэшбанг эффект
Global FlashbangActive% = False
Global FlashbangIntensity# = 0.0
Global FlashbangTimer# = 0.0
Global PlayerStunned% = False
Global PlayerStunTimer# = 0.0

; Трекинг видимости
Global PlayerLightLevel# = 1.0
Global PlayerInCover% = False
Global PlayerCoverEntity% = 0

; Звуки
Global FlashbangSFX% = 0
Global FlashbangRingSFX% = 0
Dim TacticalRadioSFX%(4)

Dim FoxSquads.MTFFoxSquad(4)

; Инит
Function InitFoxTactics()
	FlashbangSFX = LoadSound("SFX\General\Flashbang.ogg")
	FlashbangRingSFX = LoadSound("SFX\General\EarRing.ogg")

	TacticalRadioSFX(0) = LoadSound("SFX\Character\MTF\TacticalGo.ogg")
	TacticalRadioSFX(1) = LoadSound("SFX\Character\MTF\TacticalFlank.ogg")
	TacticalRadioSFX(2) = LoadSound("SFX\Character\MTF\TacticalFlash.ogg")
	TacticalRadioSFX(3) = LoadSound("SFX\Character\MTF\TacticalContact.ogg")

	FoxSquadCount = 0
	ActiveFoxSquad = Null
	FlashbangActive = False
	FlashbangIntensity = 0.0
	PlayerStunned = False
	FoxTacticsEnabled = True
End Function

; --- Управление отрядом ---

Function CreateFoxSquad.MTFFoxSquad()
	If FoxSquadCount >= 4 Then Return Null

	Local squad.MTFFoxSquad = New MTFFoxSquad
	squad\id = FoxSquadCount
	squad\memberCount = 0
	squad\alertLevel = 0
	squad\engagementActive = False
	squad\encirclementPhase = 0
	squad\flashbangQueued = False
	squad\leader = Null

	For i% = 0 To MTF_FOX_MAX_SQUAD - 1
		SetSquadMember(squad, i, Null)
	Next

	FoxSquads(FoxSquadCount) = squad
	FoxSquadCount = FoxSquadCount + 1

	Return squad
End Function

Function AddFoxToSquad(squad.MTFFoxSquad, n.NPCs, role%)
	If squad = Null Or n = Null Then Return
	If squad\memberCount >= MTF_FOX_MAX_SQUAD Then Return
	If n\NPCtype <> NPCtypeMTF Then Return

	Local fox.MTFFoxState = New MTFFoxState
	fox\npcRef = n
	fox\squad = squad
	fox\role = role
	fox\tacticalState = FOX_STATE_PATROL
	fox\stateTimer = 0.0
	fox\losCheckTimer = 0.0
	fox\flashbangCooldown = 0.0
	fox\repositionTimer = 0.0
	fox\hasLOS = False
	fox\signalSent = False
	fox\waitingForSignal = False
	fox\playerInShadow = False
	fox\coverPoint = CreatePivot()

	SetSquadMember(squad, squad\memberCount, fox)
	squad\memberCount = squad\memberCount + 1

	If role = FOX_ROLE_LEADER Or squad\leader = Null Then
		squad\leader = fox
	EndIf
End Function

Function GetFoxState.MTFFoxState(n.NPCs)
	For squad.MTFFoxSquad = Each MTFFoxSquad
		For i% = 0 To squad\memberCount - 1
			Local memberCheck.MTFFoxState = GetSquadMember(squad, i)
			If memberCheck <> Null Then
				If memberCheck\npcRef = n Then Return memberCheck
			EndIf
		Next
	Next
	Return Null
End Function

; --- Главный апдейт ---

Function UpdateFoxTactics()
	If Not FoxTacticsEnabled Then Return
	If CurrentDay < 3 And (Not GetStoryFlag(FLAG_FINALE_TRIGGERED)) Then Return

	For squad.MTFFoxSquad = Each MTFFoxSquad
		UpdateFoxSquad(squad)
	Next

	UpdateFlashbangEffect()
	UpdatePlayerStun()
End Function

Function UpdateFoxSquad(squad.MTFFoxSquad)
	If squad = Null Then Return
	If squad\memberCount = 0 Then Return

	Local totalLOS% = 0
	Local closestDistSq# = 100000.0

	For i% = 0 To squad\memberCount - 1
		Local fox.MTFFoxState = GetSquadMember(squad, i)
		If fox <> Null And fox\npcRef <> Null Then
			UpdateFoxMember(fox)

			If fox\hasLOS Then totalLOS = totalLOS + 1

			Local distSq# = GetDistanceSquaredToPlayer(fox\npcRef)
			If distSq < closestDistSq Then closestDistSq = distSq
		EndIf
	Next

	; Алерт уровень
	If totalLOS > 0 Then
		squad\alertLevel = Min(squad\alertLevel + 5, 100)
		squad\playerLastKnownX = EntityX(Collider, True)
		squad\playerLastKnownY = EntityY(Collider, True)
		squad\playerLastKnownZ = EntityZ(Collider, True)
	Else
		squad\alertLevel = Max(squad\alertLevel - 1, 0)
	EndIf

	If squad\alertLevel >= 50 And (Not squad\engagementActive) Then
		InitiateEngagement(squad)
	EndIf

	If squad\engagementActive Then
		UpdateEncirclement(squad, closestDistSq)
	EndIf
End Function

; --- Апдейт отдельного бойца ---

Function UpdateFoxMember(fox.MTFFoxState)
	If fox = Null Then Return
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef

	fox\stateTimer = fox\stateTimer + FPSfactor
	fox\losCheckTimer = fox\losCheckTimer + FPSfactor

	If fox\flashbangCooldown > 0 Then fox\flashbangCooldown = fox\flashbangCooldown - FPSfactor
	If fox\repositionTimer > 0 Then fox\repositionTimer = fox\repositionTimer - FPSfactor

	; Периодический LOS чек
	If fox\losCheckTimer >= LOS_CHECK_INTERVAL Then
		fox\losCheckTimer = 0.0
		fox\hasLOS = CheckLineOfSight(n, Collider)
		fox\playerInShadow = CheckPlayerInShadow()
	EndIf

	; State machine
	Select fox\tacticalState

		Case FOX_STATE_PATROL
			If fox\hasLOS Then
				fox\tacticalState = FOX_STATE_ALERT
				fox\stateTimer = 0.0
				If fox\squad <> Null And (Not fox\signalSent) Then
					SignalSquad(fox\squad, fox)
					fox\signalSent = True
				EndIf
			EndIf

		Case FOX_STATE_ALERT
			If fox\stateTimer > 35.0 Then ; 0.5 сек реакции
				If fox\squad\engagementActive Then
					Select fox\role
						Case FOX_ROLE_LEADER
							fox\tacticalState = FOX_STATE_ENGAGING
						Case FOX_ROLE_FLANKER_L, FOX_ROLE_FLANKER_R
							fox\tacticalState = FOX_STATE_FLANKING
						Case FOX_ROLE_POINTMAN
							fox\tacticalState = FOX_STATE_ENGAGING
						Case FOX_ROLE_SUPPORT
							fox\tacticalState = FOX_STATE_FLASHBANG
						Default
							fox\tacticalState = FOX_STATE_ENGAGING
					End Select
					fox\stateTimer = 0.0
				EndIf
			EndIf

		Case FOX_STATE_FLANKING
			UpdateFlankingBehavior(fox)

		Case FOX_STATE_ENGAGING
			UpdateEngagingBehavior(fox)

		Case FOX_STATE_FLASHBANG
			UpdateFlashbangBehavior(fox)

		Case FOX_STATE_BREACH
			UpdateBreachBehavior(fox)

		Case FOX_STATE_CONTAIN
			UpdateContainBehavior(fox)
	End Select
End Function

; --- Тактические поведения ---

Function UpdateFlankingBehavior(fox.MTFFoxState)
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef
	Local squad.MTFFoxSquad = fox\squad

	If squad = Null Then
		fox\tacticalState = FOX_STATE_ENGAGING
		Return
	EndIf

	Local playerX# = squad\playerLastKnownX
	Local playerZ# = squad\playerLastKnownZ

	Local baseAngle# = ATan2(playerX - EntityX(n\Collider, True), playerZ - EntityZ(n\Collider, True))

	If fox\role = FOX_ROLE_FLANKER_L Then
		fox\flankAngle = baseAngle + 60.0
	ElseIf fox\role = FOX_ROLE_FLANKER_R Then
		fox\flankAngle = baseAngle - 60.0
	EndIf

	fox\targetX = playerX + Cos(fox\flankAngle) * MTF_FOX_FLANK_DISTANCE
	fox\targetZ = playerZ + Sin(fox\flankAngle) * MTF_FOX_FLANK_DISTANCE
	fox\targetY = EntityY(n\Collider, True)

	MoveTowardTarget(fox)

	Local dx# = fox\targetX - EntityX(n\Collider, True)
	Local dz# = fox\targetZ - EntityZ(n\Collider, True)
	Local distSq# = dx*dx + dz*dz

	If distSq < 4.0 Then
		fox\tacticalState = FOX_STATE_ENGAGING
		fox\stateTimer = 0.0
		If squad\leader <> Null Then squad\encirclementPhase = 2
	EndIf

	; Таймаут 10 сек
	If fox\stateTimer > 700.0 Then
		fox\tacticalState = FOX_STATE_ENGAGING
		fox\stateTimer = 0.0
	EndIf
End Function

Function UpdateEngagingBehavior(fox.MTFFoxState)
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef
	Local distSq# = GetDistanceSquaredToPlayer(n)

	; Игрок в тени - кидаем флэш
	If fox\playerInShadow And fox\flashbangCooldown <= 0 Then
		fox\tacticalState = FOX_STATE_FLASHBANG
		fox\stateTimer = 0.0
		Return
	EndIf

	fox\targetX = EntityX(Collider, True)
	fox\targetY = EntityY(Collider, True)
	fox\targetZ = EntityZ(Collider, True)

	; Ждём фланкеров
	If fox\squad\encirclementPhase = 1 Then
		If distSq < MTF_FOX_ENGAGE_DISTANCE_SQ Then Return
	EndIf

	MoveTowardTarget(fox)

	If distSq < MTF_FOX_CLOSE_DISTANCE_SQ Then
		fox\tacticalState = FOX_STATE_CONTAIN
		fox\stateTimer = 0.0
	EndIf
End Function

Function UpdateFlashbangBehavior(fox.MTFFoxState)
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef
	Local distSq# = GetDistanceSquaredToPlayer(n)

	If distSq > FLASHBANG_RANGE_SQ * 4.0 Then
		fox\targetX = EntityX(Collider, True)
		fox\targetY = EntityY(Collider, True)
		fox\targetZ = EntityZ(Collider, True)
		MoveTowardTarget(fox)
		Return
	EndIf

	If fox\stateTimer < 70.0 Then
		If fox\stateTimer < 5.0 Then
			If TacticalRadioSFX(2) <> 0 Then PlaySound TacticalRadioSFX(2)
		EndIf
		Return
	EndIf

	ThrowFlashbang(fox)

	fox\flashbangCooldown = 1400.0
	fox\tacticalState = FOX_STATE_ENGAGING
	fox\stateTimer = 0.0
End Function

Function UpdateBreachBehavior(fox.MTFFoxState)
	If fox\npcRef = Null Then Return
	; TODO: двери
	fox\tacticalState = FOX_STATE_ENGAGING
	fox\stateTimer = 0.0
End Function

Function UpdateContainBehavior(fox.MTFFoxState)
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef
	Local distSq# = GetDistanceSquaredToPlayer(n)

	If distSq > MTF_FOX_CLOSE_DISTANCE_SQ * 2.0 Then
		fox\tacticalState = FOX_STATE_ENGAGING
		fox\stateTimer = 0.0
		Return
	EndIf

	; Поворот к игроку
	Local playerX# = EntityX(Collider, True)
	Local playerZ# = EntityZ(Collider, True)
	Local npcX# = EntityX(n\Collider, True)
	Local npcZ# = EntityZ(n\Collider, True)
	Local angle# = ATan2(playerX - npcX, playerZ - npcZ)

	Local currentAngle# = EntityYaw(n\Collider)
	Local angleDiff# = angle - currentAngle

	While angleDiff > 180.0 : angleDiff = angleDiff - 360.0 : Wend
	While angleDiff < -180.0 : angleDiff = angleDiff + 360.0 : Wend

	RotateEntity n\Collider, 0, currentAngle + angleDiff * 0.1 * FPSfactor, 0
End Function

; --- Координация окружения ---

Function InitiateEngagement(squad.MTFFoxSquad)
	If squad = Null Then Return

	squad\engagementActive = True
	squad\encirclementPhase = 1

	If TacticalRadioSFX(3) <> 0 Then PlaySound TacticalRadioSFX(3)

	; Авто-назначение фланкеров
	Local flankerLAssigned% = False
	Local flankerRAssigned% = False
	Local fox.MTFFoxState

	For i% = 0 To squad\memberCount - 1
		fox = GetSquadMember(squad, i)
		If fox <> Null Then
			If fox\role = FOX_ROLE_FLANKER_L Then flankerLAssigned = True
			If fox\role = FOX_ROLE_FLANKER_R Then flankerRAssigned = True
		EndIf
	Next

	If (Not flankerLAssigned) Or (Not flankerRAssigned) Then
		Local assigned% = 0
		For i% = 0 To squad\memberCount - 1
			fox = GetSquadMember(squad, i)
			If fox <> Null And fox <> squad\leader Then
				If (Not flankerLAssigned) And assigned = 0 Then
					fox\role = FOX_ROLE_FLANKER_L
					flankerLAssigned = True
					assigned = assigned + 1
				ElseIf (Not flankerRAssigned) And assigned = 1 Then
					fox\role = FOX_ROLE_FLANKER_R
					flankerRAssigned = True
					assigned = assigned + 1
				EndIf
			EndIf
		Next
	EndIf

	For i% = 0 To squad\memberCount - 1
		fox = GetSquadMember(squad, i)
		If fox <> Null Then
			fox\tacticalState = FOX_STATE_ALERT
			fox\stateTimer = 0.0
		EndIf
	Next
End Function

Function UpdateEncirclement(squad.MTFFoxSquad, closestDistSq#)
	If squad = Null Then Return

	If squad\encirclementPhase = 2 Then
		If TacticalRadioSFX(0) <> 0 Then PlaySound TacticalRadioSFX(0)
		squad\encirclementPhase = 3
	EndIf

	If closestDistSq < MTF_FOX_CLOSE_DISTANCE_SQ Then
		squad\encirclementPhase = 4 ; окружён
	EndIf
End Function

Function SignalSquad(squad.MTFFoxSquad, spotter.MTFFoxState)
	If squad = Null Then Return

	For i% = 0 To squad\memberCount - 1
		Local fox.MTFFoxState = GetSquadMember(squad, i)
		If fox <> Null And fox <> spotter Then
			If fox\tacticalState = FOX_STATE_PATROL Then
				fox\tacticalState = FOX_STATE_ALERT
				fox\stateTimer = 0.0
			EndIf
		EndIf
	Next

	If TacticalRadioSFX(1) <> 0 Then PlaySound TacticalRadioSFX(1)
End Function

; --- Флэшбанг ---

Function ThrowFlashbang(fox.MTFFoxState)
	If fox = Null Then Return
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef

	If FlashbangSFX <> 0 Then PlaySound FlashbangSFX

	Local distSq# = GetDistanceSquaredToPlayer(n)

	If distSq <= FLASHBANG_RANGE_SQ Then
		TriggerFlashbangEffect(1.0)
	ElseIf distSq <= FLASHBANG_RANGE_SQ * 2.0 Then
		Local falloff# = 1.0 - ((distSq - FLASHBANG_RANGE_SQ) / FLASHBANG_RANGE_SQ)
		TriggerFlashbangEffect(falloff)
	EndIf
End Function

Function TriggerFlashbangEffect(intensity#)
	FlashbangActive = True
	FlashbangIntensity = intensity
	FlashbangTimer = FLASHBANG_DURATION * intensity

	PlayerStunned = True
	PlayerStunTimer = FLASHBANG_STUN_DURATION * intensity
	CanPlayerMove = False

	If FlashbangRingSFX <> 0 Then PlaySound FlashbangRingSFX
End Function

Function UpdateFlashbangEffect()
	If Not FlashbangActive Then Return

	FlashbangTimer = FlashbangTimer - FPSfactor

	If FlashbangTimer <= 0.0 Then
		FlashbangActive = False
		FlashbangIntensity = 0.0
		FlashbangTimer = 0.0
	Else
		FlashbangIntensity = FlashbangTimer / FLASHBANG_DURATION
	EndIf
End Function

Function UpdatePlayerStun()
	If Not PlayerStunned Then Return

	PlayerStunTimer = PlayerStunTimer - FPSfactor

	If PlayerStunTimer <= 0.0 Then
		PlayerStunned = False
		PlayerStunTimer = 0.0
		If Not DialogActive Then CanPlayerMove = True
	EndIf
End Function

Function RenderFlashbangEffect()
	If Not FlashbangActive Then Return
	If FlashbangIntensity <= 0.0 Then Return

	Local gw% = GraphicsWidth()
	Local gh% = GraphicsHeight()
	Local alpha% = Int(FlashbangIntensity * 255.0)

	If FlashbangIntensity > 0.5 Then
		Color 255, 255, 255
		Rect 0, 0, gw, gh, True
	Else
		; Scanlines
		For y% = 0 To gh - 1 Step 2
			Local lineAlpha% = Int(FlashbangIntensity * 255.0 * (1.0 - Float(y) / Float(gh)))
			If lineAlpha > 128 Then
				Color 255, 255, 255
				Line 0, y, gw, y
			EndIf
		Next
	EndIf

	; Шум при стане
	If PlayerStunned Then
		Local noiseCount% = Int(FlashbangIntensity * 1000.0)
		For i% = 0 To noiseCount
			Local nx% = Rand(0, gw - 1)
			Local ny% = Rand(0, gh - 1)
			Local nc% = Rand(200, 255)
			Color nc, nc, nc
			Plot nx, ny
		Next
	EndIf
End Function

; --- LOS и тени ---

Function CheckLineOfSight%(sourceNPC.NPCs, targetEntity%)
	If sourceNPC = Null Or targetEntity = 0 Then Return False
	If sourceNPC\Collider = 0 Then Return False

	Local srcX# = EntityX(sourceNPC\Collider, True)
	Local srcY# = EntityY(sourceNPC\Collider, True) + 1.5
	Local srcZ# = EntityZ(sourceNPC\Collider, True)

	Local tgtX# = EntityX(targetEntity, True)
	Local tgtY# = EntityY(targetEntity, True) + 0.8
	Local tgtZ# = EntityZ(targetEntity, True)

	Local dx# = tgtX - srcX
	Local dy# = tgtY - srcY
	Local dz# = tgtZ - srcZ

	Local pick% = LinePick(srcX, srcY, srcZ, dx, dy, dz)

	If pick = 0 Then Return True
	If PickedEntity() = targetEntity Then Return True

	Local pickDist# = Sqr(PickedX()*PickedX() + PickedY()*PickedY() + PickedZ()*PickedZ())
	Local targetDist# = Sqr(dx*dx + dy*dy + dz*dz)

	If pickDist >= targetDist - 0.5 Then Return True

	Return False
End Function

Function CheckPlayerInShadow%()
	If PlayerRoom = Null Then Return False

	Local lightCount% = 0
	Local totalLight# = 0.0

	For i% = 0 To MaxRoomLights - 1
		If PlayerRoom\Lights[i] <> 0 Then
			Local lightDist# = EntityDistance(Collider, PlayerRoom\Lights[i])
			If lightDist < 10.0 Then
				totalLight = totalLight + (10.0 - lightDist) / 10.0
				lightCount = lightCount + 1
			EndIf
		EndIf
	Next

	If lightCount > 0 Then
		PlayerLightLevel = totalLight / Float(lightCount)
	Else
		PlayerLightLevel = 0.1
	EndIf

	Return PlayerLightLevel < SHADOW_THRESHOLD
End Function

Function CheckPlayerInCover%()
	PlayerInCover = False
	PlayerCoverEntity = 0

	Local nearestFox.MTFFoxState = Null
	Local nearestDistSq# = 100000.0

	For fox.MTFFoxState = Each MTFFoxState
		If fox\npcRef <> Null Then
			Local distSq# = GetDistanceSquaredToPlayer(fox\npcRef)
			If distSq < nearestDistSq Then
				nearestDistSq = distSq
				nearestFox = fox
			EndIf
		EndIf
	Next

	If nearestFox <> Null And nearestFox\npcRef <> Null Then
		If Not nearestFox\hasLOS Then PlayerInCover = True
	EndIf

	Return PlayerInCover
End Function

; --- Хелперы движения ---

Function MoveTowardTarget(fox.MTFFoxState)
	If fox = Null Then Return
	If fox\npcRef = Null Then Return

	Local n.NPCs = fox\npcRef

	Local dx# = fox\targetX - EntityX(n\Collider, True)
	Local dz# = fox\targetZ - EntityZ(n\Collider, True)
	Local dist# = Sqr(dx*dx + dz*dz)

	If dist < 0.5 Then Return

	dx = dx / dist
	dz = dz / dist

	Local speed# = n\Speed * FPSfactor
	TranslateEntity n\Collider, dx * speed, 0, dz * speed

	Local angle# = ATan2(dx, dz)
	RotateEntity n\Collider, 0, angle, 0
End Function

Function GetDistanceSquaredToPlayer#(n.NPCs)
	If n = Null Then Return 100000.0
	If n\Collider = 0 Then Return 100000.0
	If Collider = 0 Then Return 100000.0

	Local dx# = EntityX(Collider, True) - EntityX(n\Collider, True)
	Local dy# = EntityY(Collider, True) - EntityY(n\Collider, True)
	Local dz# = EntityZ(Collider, True) - EntityZ(n\Collider, True)

	Return dx*dx + dy*dy + dz*dz
End Function

; --- Интеграция ---

Function IntegrateFoxTactics(n.NPCs)
	If Not FoxTacticsEnabled Then Return
	If CurrentDay < 3 Then Return

	Local fox.MTFFoxState = GetFoxState(n)
	If fox = Null Then Return
	; State machine выше управляет движением
End Function

; --- Save/Load ---

Function SaveFoxTacticsState(file%)
	WriteInt file, FoxTacticsEnabled
	WriteInt file, FoxSquadCount

	For squad.MTFFoxSquad = Each MTFFoxSquad
		WriteInt file, squad\id
		WriteInt file, squad\memberCount
		WriteInt file, squad\alertLevel
		WriteInt file, squad\engagementActive
		WriteInt file, squad\encirclementPhase
		WriteFloat file, squad\playerLastKnownX
		WriteFloat file, squad\playerLastKnownY
		WriteFloat file, squad\playerLastKnownZ
	Next

	WriteInt file, FlashbangActive
	WriteFloat file, FlashbangIntensity
	WriteFloat file, FlashbangTimer
	WriteInt file, PlayerStunned
	WriteFloat file, PlayerStunTimer
End Function

Function LoadFoxTacticsState(file%)
	FoxTacticsEnabled = ReadInt(file)
	FoxSquadCount = ReadInt(file)

	For i% = 0 To FoxSquadCount - 1
		Local squad.MTFFoxSquad = CreateFoxSquad()
		squad\id = ReadInt(file)
		squad\memberCount = ReadInt(file)
		squad\alertLevel = ReadInt(file)
		squad\engagementActive = ReadInt(file)
		squad\encirclementPhase = ReadInt(file)
		squad\playerLastKnownX = ReadFloat(file)
		squad\playerLastKnownY = ReadFloat(file)
		squad\playerLastKnownZ = ReadFloat(file)
		; ! NOTE: членов отряда линкуем после загрузки NPC
	Next

	FlashbangActive = ReadInt(file)
	FlashbangIntensity = ReadFloat(file)
	FlashbangTimer = ReadFloat(file)
	PlayerStunned = ReadInt(file)
	PlayerStunTimer = ReadFloat(file)
End Function

; --- Очистка ---

Function CleanupFoxTactics()
	For fox.MTFFoxState = Each MTFFoxState
		If fox\coverPoint <> 0 Then FreeEntity fox\coverPoint
		Delete fox
	Next

	For squad.MTFFoxSquad = Each MTFFoxSquad
		Delete squad
	Next

	For i% = 0 To 3
		FoxSquads(i) = Null
	Next

	FoxSquadCount = 0

	If FlashbangSFX <> 0 Then FreeSound FlashbangSFX : FlashbangSFX = 0
	If FlashbangRingSFX <> 0 Then FreeSound FlashbangRingSFX : FlashbangRingSFX = 0

	For i% = 0 To 3
		If TacticalRadioSFX(i) <> 0 Then
			FreeSound TacticalRadioSFX(i)
			TacticalRadioSFX(i) = 0
		EndIf
	Next
End Function

; --- Дебаг ---

Function DebugFoxTactics()
	Color 255, 100, 100
	Text 10, 300, "=== MTF FOX ==="
	Text 10, 315, "Squads: " + FoxSquadCount
	Text 10, 330, "Light: " + Int(PlayerLightLevel * 100) + "%"
	Text 10, 345, "Cover: " + PlayerInCover
	Text 10, 360, "Flash: " + FlashbangActive + " (" + Int(FlashbangIntensity * 100) + "%)"
	Text 10, 375, "Stun: " + PlayerStunned

	Local y% = 395
	For squad.MTFFoxSquad = Each MTFFoxSquad
		Text 10, y, "Squad" + squad\id + ": alert=" + squad\alertLevel + " phase=" + squad\encirclementPhase
		y = y + 15

		For i% = 0 To squad\memberCount - 1
			Local fox.MTFFoxState = GetSquadMember(squad, i)
			If fox <> Null Then
				Local stateName$ = GetFoxStateName(fox\tacticalState)
				Local roleName$ = GetFoxRoleName(fox\role)
				Text 20, y, roleName + ": " + stateName + " los=" + fox\hasLOS
				y = y + 12
			EndIf
		Next
	Next
End Function

Function GetFoxStateName$(state%)
	Select state
		Case FOX_STATE_PATROL : Return "PATROL"
		Case FOX_STATE_ALERT : Return "ALERT"
		Case FOX_STATE_FLANKING : Return "FLANK"
		Case FOX_STATE_ENGAGING : Return "ENGAGE"
		Case FOX_STATE_FLASHBANG : Return "FLASH"
		Case FOX_STATE_BREACH : Return "BREACH"
		Case FOX_STATE_CONTAIN : Return "CONTAIN"
	End Select
	Return "?"
End Function

Function GetFoxRoleName$(role%)
	Select role
		Case FOX_ROLE_LEADER : Return "LEAD"
		Case FOX_ROLE_POINTMAN : Return "POINT"
		Case FOX_ROLE_FLANKER_L : Return "FL-L"
		Case FOX_ROLE_FLANKER_R : Return "FL-R"
		Case FOX_ROLE_REAR : Return "REAR"
		Case FOX_ROLE_SUPPORT : Return "SUPP"
	End Select
	Return "?"
End Function
