Function UpdateEvents()
	CatchErrors("Uncaught (UpdateEvents)")
	Local dist#, i%, temp%, pvt%, strtemp$, j%, k%
	
	Local p.Particles, n.NPCs, r.Rooms, e.Events, e2.Events, it.Items, it2.Items, em.Emitters, sc.SecurityCams, sc2.SecurityCams
	
	Local CurrTrigger$ = ""
	
	Local x#, y#, z#
	
	Local angle#
	
	CurrStepSFX = 0
	
	UpdateRooms()
	
	For e.Events = Each Events
		Select e\EventName
			Case "exit1"
				;[Block]
				If RemoteDoorOn=False Then
					e\room\RoomDoors[4]\locked=True
				ElseIf RemoteDoorOn And e\EventState3=0
					e\room\RoomDoors[4]\locked=False
					If e\room\RoomDoors[4]\open Then 
						If e\room\RoomDoors[4]\openstate > 50 Or EntityDistance(Collider, e\room\RoomDoors[4]\frameobj)<0.5 Then
							e\room\RoomDoors[4]\openstate = Min(e\room\RoomDoors[4]\openstate,50)
							e\room\RoomDoors[4]\open = False
							PlaySound2 (LoadTempSound("SFX\Door\DoorError.ogg"), Camera, e\room\RoomDoors[4]\frameobj)
						EndIf							
					EndIf
				Else
					e\room\RoomDoors[4]\locked=False
					
					If Curr096 <> Null Then
						If Curr096\State = 0 Or Curr096\State = 5 Then
							e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1], e\room\Objects[8], e\room\Objects[9], e)
						Else
							e\EventState2 = Update096ElevatorEvent(e,e\EventState2,e\room\RoomDoors[0],e\room\Objects[8])
						EndIf
					Else
						e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1], e\room\Objects[8], e\room\Objects[9], e)
					EndIf
					
					EntityAlpha Fog, 1.0						
				EndIf
				;[End Block]
			Case "alarm" ;the alarm in the starting room
				;[Block]
				
				If e\room\RoomDoors[5]=Null Then
					For i=0 To 3
						If e\room\AdjDoor[i]<>Null Then
							e\room\RoomDoors[5] = e\room\AdjDoor[i]
							e\room\RoomDoors[5]\open = True
							Exit
						EndIf
					Next
				EndIf
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then
						
						e\room\RoomDoors[2]\open=True
						
						ShowEntity Fog
						AmbientLight Brightness, Brightness, Brightness
						CameraFogRange(Camera, CameraFogNear, CameraFogFar)
						CameraFogMode(Camera, 1)
						If SelectedDifficulty\saveType = SAVEANYWHERE Then
							Msg = "Press "+KeyName(KEY_SAVE)+" to save."
							MsgTimer = 70*4
							;SetSaveMSG("Press "+KeyName(KEY_SAVE)+" to save.")
						ElseIf SelectedDifficulty\saveType = SAVEONSCREENS Then
							Msg = "Saving is only permitted on clickable monitors scattered throughout the facility."
							MsgTimer = 70 * 8
							;SetSaveMSG("Saving is only permitted on clickable monitors scattered throughout the facility.")
						EndIf
						
						Curr173\Idle=False
						
						While e\room\RoomDoors[1]\openstate < 180
							e\room\RoomDoors[1]\openstate = Min(180, e\room\RoomDoors[1]\openstate + 0.8)
							MoveEntity(e\room\RoomDoors[1]\obj, Sin(e\room\RoomDoors[1]\openstate) / 180.0, 0, 0)
							MoveEntity(e\room\RoomDoors[1]\obj2, -Sin(e\room\RoomDoors[1]\openstate) / 180.0, 0, 0)
						Wend
						
						If e\room\NPC[0] <> Null Then SetNPCFrame(e\room\NPC[0], 74) : e\room\NPC[0]\State = 8
						
						If e\room\NPC[1] = Null Then
							e\room\NPC[1] = CreateNPC(NPCtypeD, 0,0,0)
							ChangeNPCTextureID(e\room\NPC[1],3)
							;tex = LoadTexture_Strict("GFX\npcs\scientist2.jpg")
							e\room\NPC[1]\texture = "GFX\npcs\scientist2.jpg"
							;EntityTexture e\room\NPC[1]\obj, tex
							;FreeTexture tex
							ChangeNPCTextureID(e\room\NPC[1],3)
						EndIf
						PositionEntity e\room\NPC[1]\Collider, e\room\x, 0.5, e\room\z-1.0, True
						ResetEntity e\room\NPC[1]\Collider
						SetNPCFrame(e\room\NPC[1], 210)
						
						If e\room\NPC[2] = Null Then
							e\room\NPC[2] = CreateNPC(NPCtypeGuard, 0,0,0)
						EndIf
						;x-240
						PositionEntity e\room\NPC[2]\Collider, e\room\x, 0.5, e\room\z+528*RoomScale, True
						ResetEntity e\room\NPC[2]\Collider
						e\room\NPC[2]\State = 7
						PointEntity e\room\NPC[2]\Collider,e\room\NPC[1]\Collider
						
						If e\room\NPC[0]=Null
							e\room\NPC[3] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[2], True), EntityY(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True))
							;e\room\NPC[3]\Angle = 180
							RotateEntity e\room\NPC[3]\Collider,0,90,0
							SetNPCFrame(e\room\NPC[3], 286) : e\room\NPC[3]\State = 8
							MoveEntity e\room\NPC[3]\Collider,1,0,0
							
							e\room\NPC[4] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[3], True), 0.5, EntityZ(e\room\Objects[3], True))
							;PointEntity(e\room\NPC[4]\Collider, e\room\Objects[7])
							SetNPCFrame(e\room\NPC[4], 19) : e\room\NPC[4]\State = 3
							RotateEntity e\room\NPC[4]\Collider,0,270,0
							MoveEntity e\room\NPC[4]\Collider,0,0,2.65
							
							e\room\NPC[5] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[4], True), 0.5, EntityZ(e\room\Objects[4], True))
							ChangeNPCTextureID(e\room\NPC[5],6)
							;PointEntity(e\room\NPC[5]\Collider, e\room\Objects[7])
							SetNPCFrame(e\room\NPC[5], 19) : e\room\NPC[5]\State = 3
							RotateEntity e\room\NPC[5]\Collider,0,270,0
							;tex = LoadTexture_Strict("GFX\npcs\classd2.jpg")
							
						;	e\room\NPC[5]\texture = "GFX\npcs\classd2.jpg"
							;EntityTexture e\room\NPC[5]\obj, tex
							;FreeTexture tex
							
							MoveEntity e\room\NPC[5]\Collider,0.25,0,3.0
							RotateEntity e\room\NPC[5]\Collider,0,0,0
							
							x# = EntityX(e\room\obj, True)+3712*RoomScale
							y# = 384.0*RoomScale
							z# = EntityZ(e\room\obj, True)+1312*RoomScale
							
							For i = 3 To 5
								PositionEntity(e\room\NPC[i]\Collider, x + (EntityX(e\room\NPC[i]\Collider) - EntityX(e\room\obj)), y+EntityY(e\room\NPC[i]\Collider)+0.4, z + (EntityZ(e\room\NPC[i]\Collider) - EntityZ(e\room\obj)))
								ResetEntity(e\room\NPC[i]\Collider)
							Next
						EndIf
						
						e\EventState = 1
					EndIf
				Else
					
					If e\room\NPC[0] <> Null Then AnimateNPC(e\room\NPC[0], 249, 286, 0.4, False)
					
					CurrTrigger = CheckTriggers()
					
					If (CurrTrigger = "173scene_timer") Then
						e\EventState = e\EventState+FPSfactor
					Else If (CurrTrigger = "173scene_activated")
						e\EventState = Max(e\EventState, 500)
					EndIf
					
					If e\EventState < 850
						PositionEntity Curr173\Collider, e\room\x+32*RoomScale, 0.31, e\room\z+1072*RoomScale, True
						HideEntity Curr173\obj
					EndIf
					
					If e\EventState >= 500 Then
						e\EventState = e\EventState+FPSfactor
						
						If e\EventState2 = 0 Then
							;CanSave = False
							ShowEntity Curr173\obj
							If e\EventState > 900 And e\room\RoomDoors[5]\open Then
								If e\EventState - FPSfactor <= 900 Then 
									e\room\NPC[1]\Sound = LoadSound_Strict("SFX\Room\Intro\WhatThe.ogg")
									e\room\NPC[1]\SoundChn = PlaySound2(e\room\NPC[1]\Sound, Camera, e\room\NPC[1]\Collider)
								EndIf
								e\room\NPC[1]\State = 3
								e\room\NPC[1]\CurrSpeed = CurveValue(-0.008, e\room\NPC[1]\CurrSpeed, 5.0)
								AnimateNPC(e\room\NPC[1], 260, 236, e\room\NPC[1]\CurrSpeed * 18)
								RotateEntity e\room\NPC[1]\Collider, 0, 0, 0
								
								If e\EventState > 900+2.5*70 Then
									If e\room\NPC[2]\State <> 1
										e\room\NPC[2]\CurrSpeed = CurveValue(-0.012, e\room\NPC[2]\CurrSpeed, 5.0)
										AnimateNPC(e\room\NPC[2], 39, 76, e\room\NPC[2]\CurrSpeed*40)
										MoveEntity e\room\NPC[2]\Collider, 0,0,e\room\NPC[2]\CurrSpeed*FPSfactor
										e\room\NPC[2]\State = 8
										
										;z-512
										If EntityZ(e\room\NPC[2]\Collider) < e\room\z Then
											PointEntity(e\room\NPC[2]\obj, e\room\NPC[1]\Collider)
											RotateEntity e\room\NPC[2]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[2]\obj)-180,EntityYaw(e\room\NPC[2]\Collider),15.0), 0
										Else
											RotateEntity e\room\NPC[2]\Collider, 0, 0, 0
										EndIf
									EndIf
								EndIf
								
								If e\EventState < 900+4*70 Then
									PositionEntity Curr173\Collider, e\room\x+32*RoomScale, 0.31, e\room\z+1072*RoomScale, True
									RotateEntity Curr173\Collider,0,190,0
									
									If e\EventState > 900+70 And e\EventState < 900+2.5*70 Then
										AnimateNPC(e\room\NPC[2], 1539, 1553, 0.2, False)
										PointEntity(e\room\NPC[2]\obj, Curr173\Collider)
										RotateEntity e\room\NPC[2]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[2]\obj),EntityYaw(e\room\NPC[2]\Collider),15.0), 0
									EndIf
									
								Else
									If e\EventState-FPSfactor < 900+4*70 Then 
										PlaySound_Strict(IntroSFX(11)) : LightBlink = 3.0
										PlaySound2 (StoneDragSFX, Camera, Curr173\Collider)
										PointEntity Curr173\Collider, e\room\NPC[2]\Collider
										If EntityY(Collider)<320*RoomScale Then BlinkTimer = -10
									EndIf
									
									PositionEntity Curr173\Collider, e\room\x-96*RoomScale, 0.31, e\room\z+592*RoomScale, True
									RotateEntity Curr173\Collider,0,190,0
									
									If e\room\NPC[2]\State <> 1 And KillTimer >= 0
										If EntityZ(e\room\NPC[2]\Collider) < e\room\z-1150*RoomScale Then
											e\room\RoomDoors[5]\open = False
											LightBlink = 3.0
											PlaySound_Strict(IntroSFX(11))
											BlinkTimer = -10
											PlaySound2 (StoneDragSFX, Camera, Curr173\Collider)
											If EntityDistance(Curr173\Collider,Collider)<2.5 And Abs(EntityY(Collider)-EntityY(Curr173\Collider))<1.0 Then
                                                PositionEntity Curr173\Collider, EntityX(Collider),EntityY(Collider),EntityZ(Collider)
                                            Else
                                                PositionEntity Curr173\Collider, 0,0,0
                                            EndIf
											ResetEntity Curr173\Collider
											Msg = "Hold "+KeyName(KEY_SPRINT)+" to run."
											MsgTimer = 70*8
										EndIf
									EndIf
								EndIf
								
								;If Ulgrin can see the player then start shooting at them.
								If (CurrTrigger = "173scene_end") And EntityVisible(e\room\NPC[2]\Collider, Collider) And (Not NoTarget) Then
									e\room\NPC[2]\State = 1
									e\room\NPC[2]\State3 = 1
								ElseIf e\room\NPC[2]\State = 1 And (Not EntityVisible(e\room\NPC[2]\Collider, Collider))
									e\room\NPC[2]\State = 0
									e\room\NPC[2]\State3 = 0
								EndIf
								
								If e\room\NPC[2]\State = 1 Then e\room\RoomDoors[5]\open = True
							Else
								CanSave = True
								If e\room\NPC[2]\State <> 1
									If EntityX(Collider)<(e\room\x+1384*RoomScale) Then e\EventState = Max(e\EventState,900)
									
									If e\room\RoomDoors[5]\openstate = 0 Then 
										;HideEntity e\room\NPC[1]\obj
										;HideEntity e\room\NPC[1]\Collider
										;
										;HideEntity e\room\NPC[2]\obj
										;HideEntity e\room\NPC[2]\Collider
										
										If e\room\NPC[1] <> Null Then RemoveNPC(e\room\NPC[1])
										If e\room\NPC[2] <> Null Then RemoveNPC(e\room\NPC[2])
										
										e\EventState2=1
									EndIf
								EndIf
							EndIf
						EndIf
						
						
					;	If e\EventState > 900+3*70 And e\EventState < 900+4*70 Then 
					;		CameraShake = 0.2
					;	ElseIf e\EventState > 900+32.3*70 And e\EventState < 900+34*70
					;		CameraShake = 0.4
					;	ElseIf e\EventState > 900+51*70 And e\EventState < 900+53.5*70
					;		CameraShake = 1.0
					;	ElseIf e\EventState > 900+57.5*70 And e\EventState < 900+58.5*70
					;		CameraShake = 0.4
					;	EndIf
						
						PositionEntity e\room\Objects[0], EntityX(e\room\Objects[0],True), -Max(e\EventState-1300,0)/4500, EntityZ(e\room\Objects[0],True), True
						RotateEntity e\room\Objects[0], -Max(e\EventState-1320,0)/130, 0, -Max(e\EventState-1300,0)/40, True
						
						PositionEntity e\room\Objects[1], EntityX(e\room\Objects[1],True), -Max(e\EventState-1800,0)/5000, EntityZ(e\room\Objects[1],True), True
						RotateEntity e\room\Objects[1], -Max(e\EventState-2040,0)/135, 0, -Max(e\EventState-2040,0)/43, True
						
						If EntityDistance(e\room\Objects[0],Collider)<2.5 Then
							If Rand(300)=2 Then PlaySound2(DecaySFX(Rand(1,3)),Camera,e\room\Objects[0], 3.0)
						EndIf
					EndIf
					
					If (e\EventState < 2000) Then
						If e\SoundCHN = 0 Then
							e\SoundCHN = PlaySound_Strict(AlarmSFX(0))
						Else
							If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound_Strict(AlarmSFX(0))
						End If
					EndIf
					
					If (e\EventState3<11) Then
						If (Not ChannelPlaying(e\SoundCHN2)) Then
							e\EventState3 = e\EventState3+1
							
							If (e\Sound2 <> 0) Then
								FreeSound_Strict(e\Sound2)
								e\Sound2 = 0
							EndIf
							
							e\Sound2 = LoadSound_Strict("SFX\Alarm\Alarm2_"+Int(e\EventState3)+".ogg")
							e\SoundCHN2 = PlaySound_Strict(e\Sound2)
						Else
							If Int(e\EventState3) = 8 Then CameraShake = 1.0
						EndIf
					EndIf
					
					If ((e\EventState Mod 600 > 300) And ((e\EventState+FPSfactor) Mod 600 < 300)) Then
						i = Floor((e\EventState-5000)/600)+1
						
						If i = 0 Then PlaySound_Strict(LoadTempSound("SFX\Room\Intro\PA\scripted\scripted6.ogg"))
						
						If (i>0 And i<26) Then
							If Not CommotionState(i) Then ;Prevents the same commotion file from playing more then once.
								PlaySound_Strict(LoadTempSound("SFX\Room\Intro\Commotion\Commotion"+i+".ogg"))
								CommotionState(i) = True
							EndIf
						EndIf
						
						If (i>26) Then
							If e\room\NPC[0] <> Null Then RemoveNPC(e\room\NPC[0])
							;If e\room\NPC[1] <> Null Then RemoveNPC(e\room\NPC[1])
							;If e\room\NPC[2] <> Null Then RemoveNPC(e\room\NPC[2])
							
							FreeEntity e\room\Objects[0]
							FreeEntity e\room\Objects[1]
							e\room\Objects[0]=0
							e\room\Objects[1]=0
							
							DebugLog "delete alarm"
							
							RemoveEvent(e)							
						EndIf
					EndIf					
				End If
				;[End Block]
			Case "173" ;the intro sequence
				;[Block]
				
				If KillTimer >= 0 And e\EventState2 = 0 Then
					
					PlayerZone = 0
					
					If e\EventState3>0 Then
						
						ShouldPlay = 13
						
						;slow the player down to match his speed to the guards
						CurrSpeed = Min(CurrSpeed - (CurrSpeed * (0.008/EntityDistance(e\room\NPC[3]\Collider, Collider)) * FPSfactor), CurrSpeed)
						
						If e\EventState3 < 170 Then
							If e\EventState3 = 1.0 Then
								PositionEntity Camera, x, y, z
								HideEntity Collider
								PositionEntity Collider, x, 0.302, z
								RotateEntity Camera, -70, 0, 0								
								
								CurrMusicVolume = MusicVolume
								
								StopStream_Strict(MusicCHN)
								MusicCHN = StreamSound_Strict("SFX\Music\"+Music(13)+".ogg",CurrMusicVolume,Mode)
								NowPlaying = ShouldPlay
								
								PlaySound_Strict(IntroSFX(11))
								BlurTimer = 500
								ShowEntity Light
								EntityAlpha(Light, 0.5)
							EndIf
							
							If e\EventState3 < 3 Then
								e\EventState3 = e\EventState3+FPSfactor/100.0
							ElseIf e\EventState3 < 15 Or e\EventState3 >= 50 Then
								e\EventState3 = e\EventState3+FPSfactor/30.0
							EndIf
							
							If e\EventState3 < 15 Then
								
								x = EntityX(e\room\obj)-(3224.0+1024.0)*RoomScale
								y = 136.0*RoomScale
								z = EntityZ(e\room\obj)+8.0*RoomScale	
								
								If e\EventState3 < 14 Then
									mouse_x_speed_1#=0
									mouse_y_speed_1#=0
									
									If e\EventState3-FPSfactor/30.0 < 12 And e\EventState3 > 12 Then PlaySound2(StepSFX(0,0,0), Camera, Collider, 8, 0.3)
									
									ShowEntity Light
									EntityAlpha(Light, 0.9-(e\EventState3/2.0))
									
									x = x + (EntityX(e\room\obj)-(3048.0+1024.0)*RoomScale - x) * Max((e\EventState3-10.0)/4.0,0.0) 
									
									If e\EventState3 < 10 Then
										y = y + (0.2) * Min(Max((e\EventState3-3.0)/5.0, 0.0), 1.0)
									Else
										y = (y+0.2) + (0.302+0.6 - (y+0.2)) * Max((e\EventState3-10)/4.0,0.0) 
									EndIf
									
									z = z + (EntityZ(e\room\obj)+104.0*RoomScale - z) * Min(Max((e\EventState3-3)/5.0, 0), 1.0)
									
									;I'm sorry you have to see this
									RotateEntity Camera, -70.0 + 70.0*Min(Max((e\EventState3-3.0)/5.0,0.0),1.0)+Sin(e\EventState3*12.857)*5.0, -60.0*Max((e\EventState3-10.0)/4.0,0.0), Sin(e\EventState3*25.7)*8.0
									
									PositionEntity Camera, x, y, z
									HideEntity Collider
									PositionEntity Collider, x, 0.302, z	
									DropSpeed = 0
								Else
									HideEntity Light
									
									PositionEntity Collider, EntityX(Collider), 0.302, EntityZ(Collider)
									ResetEntity Collider
									ShowEntity Collider
									DropSpeed = 0
									e\EventState3 = 15
									Msg = "Pick up the paper on the desk."
									MsgTimer=70*7
								EndIf
								
								user_camera_pitch = 0
								RotateEntity Collider, 0, EntityYaw(Camera), 0
								
							ElseIf e\EventState3 < 40
								If Inventory(0)<>Null Then
									Msg = "Press "+KeyName(KEY_INV)+" to open the inventory."
									MsgTimer=70*7
									e\EventState3 = 40
									Exit
								EndIf
							EndIf
							
							If SelectedItem <> Null Then
								e\EventState3 = e\EventState3+FPSfactor/5.0
							EndIf							
							
						ElseIf e\EventState3 => 150.0 And e\EventState3 < 700
							If e\room\NPC[3]\State = 7 Then
								If e\room\NPC[3]\Sound2 = 0
									e\room\NPC[3]\Sound2 = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\BeforeDoorOpen.ogg")
									e\room\NPC[3]\SoundChn2 = PlaySound2(e\room\NPC[3]\Sound2,Camera,e\room\NPC[3]\Collider)
									DebugLog "Playing guard sound before cell opening"
								EndIf
								
								UpdateSoundOrigin(e\room\NPC[3]\SoundChn2,Camera,e\room\NPC[3]\Collider)
								
								If (Not ChannelPlaying(e\room\NPC[3]\SoundChn2))
									;BlinkTimer = -10
									
									e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\ExitCell.ogg")
									e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
									
									;e\room\NPC[3]\State = 7
									;e\room\NPC[4]\State = 7
									;e\room\NPC[5]\State = 7
									e\room\NPC[3]\State = 9
									e\room\NPC[4]\State = 9
									e\room\NPC[5]\State = 9
									
									e\room\RoomDoors[6]\locked = False		
									UseDoor(e\room\RoomDoors[6], False)
									e\room\RoomDoors[6]\locked = True
								EndIf
							Else
								FreeSound_Strict e\room\NPC[3]\Sound2
								
								;PointEntity e\room\NPC[3]\obj, Collider
								;RotateEntity e\room\NPC[3]\Collider, 0, EntityYaw(e\room\NPC[3]\obj), 0
								
								;PointEntity e\room\NPC[4]\obj, Collider
								;RotateEntity e\room\NPC[4]\Collider, 0, EntityYaw(e\room\NPC[4]\obj), 0
								
								e\EventState3 = Min(e\EventState3+FPSfactor/4,699)
								
								;outside the cell
								If Distance(EntityX(Collider),EntityZ(Collider),PlayerRoom\x-(3072+1024)*RoomScale, PlayerRoom\z+192.0*RoomScale)>1.5 Then
									;e\room\NPC[3]\State = 5
									;e\room\NPC[3]\EnemyX = EntityX(Collider)
									;e\room\NPC[3]\EnemyY = EntityY(Collider)
									;e\room\NPC[3]\EnemyZ = EntityZ(Collider)
									
									If e\EventState3 > 250 Then
										If e\room\NPC[3]\SoundChn<>0 Then
											If ChannelPlaying(e\room\NPC[3]\SoundChn) Then StopChannel e\room\NPC[3]\SoundChn
										EndIf
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\Escort"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
										
										;e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],PlayerRoom\x-1584*RoomScale, 0.3, PlayerRoom\z-1040*RoomScale)
										;e\room\NPC[4]\PathStatus = FindPath(e\room\NPC[4],PlayerRoom\x-1584*RoomScale, 0.3, PlayerRoom\z-1040*RoomScale)
										e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],PlayerRoom\x-320*RoomScale, 0.3, PlayerRoom\z-704*RoomScale)
										e\room\NPC[4]\PathStatus = FindPath(e\room\NPC[4],PlayerRoom\x-320*RoomScale, 0.3, PlayerRoom\z-704*RoomScale)
										
										e\EventState3 = 710
									EndIf
								Else ;inside the cell
									e\room\NPC[3]\State = 9
									;PointEntity e\room\NPC[3]\Collider, Collider		
									;RotateEntity e\room\NPC[3]\Collider, 0, EntityYaw(e\room\NPC[3]\Collider), 0
									
									If e\EventState3-(FPSfactor/4) < 350 And e\EventState3=>350 Then
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\ExitCellRefuse"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
									ElseIf e\EventState3-(FPSfactor/4) < 550 And e\EventState3=>550 
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\CellGas"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
									ElseIf e\EventState3>630
										PositionEntity Collider, EntityX(Collider), EntityY(Collider), Min(EntityZ(Collider), EntityZ(e\room\obj,True)+490*RoomScale)
										If e\room\RoomDoors[6]\open = True Then 
											e\room\RoomDoors[6]\locked = False		
											UseDoor(e\room\RoomDoors[6],False)
											e\room\RoomDoors[6]\locked = True
											
											em.Emitters = CreateEmitter(PlayerRoom\x - (2976.0+1024) * RoomScale, 373.0 * RoomScale, PlayerRoom\z + 204.0 * RoomScale, 0)
											TurnEntity(em\Obj, 90, 0, 0, True)
											em\RandAngle = 7
											em\Speed = 0.03
											em\SizeChange = 0.003
											em\Room = PlayerRoom
											
											em.Emitters = CreateEmitter(PlayerRoom\x - (3168.0+1024) * RoomScale, 373.0 * RoomScale, PlayerRoom\z + 204.0 * RoomScale, 0)
											TurnEntity(em\Obj, 90, 0, 0, True)
											em\RandAngle = 7
											em\Speed = 0.03
											em\SizeChange = 0.003
											em\Room = PlayerRoom
										EndIf
										
										EyeIrritation=Max(EyeIrritation+FPSfactor * 4, 1.0)
									EndIf
									
								EndIf
							EndIf
						ElseIf e\EventState3 < 800
							e\EventState3 = e\EventState3+FPSfactor/4.0
							
							;e\room\NPC[3]\State = 5
							;e\room\NPC[3]\EnemyX = EntityX(Collider)
							;e\room\NPC[3]\EnemyY = EntityY(Collider)
							;e\room\NPC[3]\EnemyZ = EntityZ(Collider)
							
							If e\room\NPC[5]\State <> 11
								If EntityDistance(e\room\NPC[3]\Collider,e\room\NPC[5]\Collider)>5.0 And EntityDistance(e\room\NPC[4]\Collider,e\room\NPC[5]\Collider)
									If EntityDistance(e\room\NPC[5]\Collider,Collider)<3.5
										e\room\NPC[5]\State = 11
										e\room\NPC[5]\State3 = 1
										e\room\NPC[5]\SoundChn2 = PlaySound2(e\room\NPC[5]\Sound2,Camera,e\room\NPC[5]\Collider)
										e\room\NPC[5]\Reload = 70*3
									EndIf
								EndIf
							EndIf
						ElseIf e\EventState3 < 900
							e\room\NPC[4]\Angle = 0
							
							If EntityX(Collider)<EntityX(e\room\obj,True)-5376*RoomScale And e\EventStr = "" Then
								If Rand(3)=1 Then
									;e\EventStr = "scripted\scripted"+Rand(1,6)+".ogg|off.ogg|"
									e\EventStr = "scripted\scripted"+Rand(1,5)+".ogg|off.ogg|"
								Else
									;generate the PA message
									e\EventStr = "1\attention"+Rand(1,2)+".ogg"
									Select Rand(3)
										Case 1
											strtemp = "crew"
											e\EventStr = e\EventStr + "|2\crew"+Rand(0,5)+".ogg"
										Case 2
											strtemp = "scientist"
											e\EventStr = e\EventStr + "|2\scientist"+Rand(0,19)+".ogg"
										Case 3
											strtemp = "security"	
											e\EventStr = e\EventStr + "|2\security"+Rand(0,5)+".ogg"
									End Select
									If Rand(2)=1 And strtemp = "scientist" Then ;call on line...
										e\EventStr = e\EventStr + "|3\callonline.ogg"
										
										e\EventStr = e\EventStr + "|numbers\"+Rand(1,9)+".ogg"
										If Rand(2)=1 Then e\EventStr = e\EventStr + "|numbers\"+Rand(1,9)+".ogg"
									Else
										e\EventStr = e\EventStr + "|3\report"+Rand(0,1)+".ogg"
										
										Select strtemp
											Case "crew"
												e\EventStr = e\EventStr + "|4\crew"+Rand(0,6)+".ogg"
												If Rand(2)=1 Then e\EventStr = e\EventStr + "|5\crew"+Rand(0,6)+".ogg"
											Case "scientist"
												e\EventStr = e\EventStr + "|4\scientist"+Rand(0,7)+".ogg"
												If Rand(2)=1 Then e\EventStr = e\EventStr + "|5\scientist0.ogg"
											Case "security"
												e\EventStr = e\EventStr + "|4\security"+Rand(0,5)+".ogg"
												If Rand(2)=1 Then e\EventStr = e\EventStr + "|5\security"+Rand(1,2)+".ogg"
										End Select
									EndIf
									e\EventStr = e\EventStr + "|off.ogg|"
								EndIf
							EndIf
							
							If e\room\NPC[6]<>Null Then ;the scientist
								If e\room\NPC[6]\State = 0 Then 
									If e\room\RoomDoors[7]\open Then 
										If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj,True)-3328*RoomScale, EntityZ(e\room\obj,True)-1232*RoomScale)<5.0 Then
											e\room\NPC[6]\State = 1
											If e\EventStr = "done" Then 
												PlaySound_Strict LoadTempSound("SFX\Room\Intro\PA\scripted\announcement"+Rand(1,7)+".ogg")
											EndIf
										EndIf
									EndIf
								Else
									If EntityZ(e\room\NPC[6]\Collider)>EntityZ(e\room\obj,True)-64.0*RoomScale Then
										RotateEntity e\room\NPC[6]\Collider, 0, CurveAngle(90,EntityYaw(e\room\NPC[6]\Collider),15.0),0
										If e\room\RoomDoors[7]\open Then UseDoor(e\room\RoomDoors[7],False)
										If e\room\RoomDoors[7]\openstate < 1.0 Then e\room\NPC[6]\State = 0
									EndIf
								EndIf
							EndIf
							
							If e\room\NPC[8]<>Null Then ;the 2 guards and ClassD
								If e\room\NPC[8]\State = 7 Then
									;If e\room\RoomDoors[7]\open Then 
									If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj,True)-6688*RoomScale, EntityZ(e\room\obj,True)-1252*RoomScale)<2.5 Then
										e\room\NPC[8]\State = 10
										e\room\NPC[9]\State = 1
										e\room\NPC[10]\State = 10
									EndIf
								Else
									If EntityX(e\room\NPC[8]\Collider)<EntityX(e\room\obj,True)-7100.0*RoomScale Then
										For i = 8 To 10
											e\room\NPC[i]\State = 0
										Next
									EndIf
								EndIf
							EndIf
							
							e\room\NPC[5]\SoundChn = LoopSound2(e\room\NPC[5]\Sound,e\room\NPC[5]\SoundChn,Camera,e\room\NPC[5]\obj,2,0.5)
							
							If e\EventStr <> "" And e\EventStr <> "done" Then
								If e\SoundCHN = 0 Then 
									e\SoundCHN = PlaySound_Strict(LoadTempSound("SFX\Room\Intro\PA\on.ogg"))
								EndIf
								If ChannelPlaying(e\SoundCHN)=False Then
									strtemp = Left(e\EventStr, Instr(e\EventStr, "|", 1)-1)
									e\SoundCHN = PlaySound_Strict (LoadTempSound("SFX\Room\Intro\PA\"+strtemp))
									e\EventStr = Right(e\EventStr, Len(e\EventStr)-Len(strtemp)-1)
									If e\EventStr = "" Then 
										FreeSound_Strict e\room\NPC[3]\Sound
										temp = Rand(1,5)
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Conversation"+temp+"a.ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[4]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Conversation"+temp+"b.ogg")
										e\room\NPC[4]\SoundChn = PlaySound2(e\room\NPC[4]\Sound, Camera, e\room\NPC[4]\Collider)
										e\EventStr = "done"
									EndIf
								EndIf
							EndIf
							
							dist = Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\NPC[3]\Collider), EntityZ(e\room\NPC[3]\Collider))
							
							If dist < 3.0 Then
								e\room\NPC[3]\State3 = Min(Max(e\room\NPC[3]\State3-FPSfactor,0),50)
							Else
								e\room\NPC[3]\State3 = Max(e\room\NPC[3]\State3+FPSfactor,50)
								If e\room\NPC[3]\State3 => 70*8 And e\room\NPC[3]\State3-FPSfactor < 70*8 And e\room\NPC[3]\State=7 Then
									If e\room\NPC[4]\SoundChn <> 0 Then
										If ChannelPlaying(e\room\NPC[4]\SoundChn) Then StopChannel(e\room\NPC[4]\SoundChn)
									EndIf
									
									If e\room\NPC[3]\State2 < 2 Then
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\EscortRefuse"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3=50
										e\room\NPC[3]\State2=3
									ElseIf e\room\NPC[3]\State2=3
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\EscortPissedOff"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3=50
										e\room\NPC[3]\State2=4
									ElseIf e\room\NPC[3]\State2=4
										FreeSound_Strict e\room\NPC[3]\Sound
										e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\EscortKill"+Rand(1,2)+".ogg")
										e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
										e\room\NPC[3]\State3 = 50+70*2.5
										e\room\NPC[3]\State2=5
									ElseIf e\room\NPC[3]\State2=5
										e\room\NPC[3]\State = 11
										e\room\NPC[4]\State = 11
										e\room\NPC[5]\State = 11
										e\room\NPC[3]\State3 = 1
										e\room\NPC[4]\State3 = 1
										e\room\NPC[5]\State3 = 1
									EndIf
								EndIf
								If e\room\NPC[5]\State <> 11
									If EntityDistance(e\room\NPC[3]\Collider,e\room\NPC[5]\Collider)>5.0 And EntityDistance(e\room\NPC[4]\Collider,e\room\NPC[5]\Collider)
										If EntityDistance(e\room\NPC[5]\Collider,Collider)<3.5
											e\room\NPC[5]\State = 11
											e\room\NPC[5]\State3 = 1
											e\room\NPC[5]\SoundChn2 = PlaySound2(e\room\NPC[5]\Sound2,Camera,e\room\NPC[5]\Collider)
											e\room\NPC[5]\Reload = 70*3
										EndIf
									EndIf
								EndIf
							EndIf
							
							If e\room\NPC[5]\State = 11
								UpdateSoundOrigin(e\room\NPC[5]\SoundChn2,Camera,e\room\NPC[5]\Collider)
							EndIf
							
							If e\room\NPC[3]\State <> 11 Then
								If dist < Min(Max(4.0-e\room\NPC[3]\State3*0.05, 1.5),4.0) Then
									If e\room\NPC[3]\PathStatus <> 1 Then
										e\room\NPC[3]\State = 7
										PointEntity e\room\NPC[3]\obj, Collider
										RotateEntity e\room\NPC[3]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True
										
										If e\room\NPC[3]\PathStatus = 2 Then
											;e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],EntityX(e\room\obj,True)-1584*RoomScale, 0.3, EntityZ(e\room\obj,True)-1040*RoomScale)
											e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],PlayerRoom\x-320*RoomScale, 0.3, PlayerRoom\z-704*RoomScale)
											e\room\NPC[4]\PathStatus = FindPath(e\room\NPC[4],PlayerRoom\x-320*RoomScale, 0.3, PlayerRoom\z-704*RoomScale)
											e\room\NPC[3]\State = 3
										EndIf
									Else
										e\room\NPC[3]\State = 3
									EndIf
								Else
									e\room\NPC[3]\State = 7
									PointEntity e\room\NPC[3]\obj, Collider
									RotateEntity e\room\NPC[3]\Collider,0,CurveValue(EntityYaw(e\room\NPC[3]\obj),EntityYaw(e\room\NPC[3]\Collider),20.0),0,True		
									
									If dist > 5.5 Then
										e\room\NPC[3]\PathStatus = 2
										If e\room\NPC[3]\State2=0 Then
											FreeSound_Strict e\room\NPC[3]\Sound
											e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\EscortRun.ogg")
											e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
											PlaySound2(e\Sound, Camera, e\room\NPC[3]\Collider)
											e\room\NPC[3]\State2=1
										EndIf
										
										e\room\NPC[3]\State = 5
										e\room\NPC[3]\EnemyX = EntityX(Collider)
										e\room\NPC[3]\EnemyY = EntityY(Collider)
										e\room\NPC[3]\EnemyZ = EntityZ(Collider)
										;e\room\NPC[3]\PathStatus = FindPath(e\room\NPC[3],EntityY(Collider), 0.3, EntityZ(Collider))
									EndIf
								EndIf	
								
								;PointEntity e\room\NPC[5]\obj, Collider
								;RotateEntity e\room\NPC[5]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[5]\obj),EntityYaw(e\room\NPC[5]\Collider),30), 0
								
								dist = EntityDistance(Collider, e\room\NPC[4]\Collider)
								If dist > 1.5 And EntityDistance(e\room\NPC[3]\Collider, Collider)<EntityDistance(e\room\NPC[3]\Collider,e\room\NPC[4]\Collider) Then
									e\room\NPC[4]\State = 3	
									
								Else
									e\room\NPC[4]\State = 5
									e\room\NPC[4]\EnemyX = EntityX(Collider)
									e\room\NPC[4]\EnemyY = EntityY(Collider)
									e\room\NPC[4]\EnemyZ = EntityZ(Collider)
								EndIf
								
							EndIf
							
							dist = Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\RoomDoors[2]\frameobj,True), EntityZ(e\room\RoomDoors[2]\frameobj,True))
							
							If Distance(EntityX(e\room\NPC[3]\Collider), EntityZ(e\room\NPC[3]\Collider), EntityX(e\room\RoomDoors[2]\frameobj,True), EntityZ(e\room\RoomDoors[2]\frameobj,True)) < 4.5 And dist < 5.0 Then
								e\room\NPC[0] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[0], True), EntityY(e\room\Objects[0], True), EntityZ(e\room\Objects[0], True))
								e\room\NPC[0]\Angle = 180
								
								e\room\NPC[1] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[1], True), 0.5, EntityZ(e\room\Objects[1], True))
								PointEntity(e\room\NPC[1]\Collider, e\room\Objects[5])
								
								e\room\NPC[2] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[2], True), 0.5, EntityZ(e\room\Objects[2], True))
								PointEntity(e\room\NPC[2]\Collider, e\room\Objects[5])
								ChangeNPCTextureID(e\room\NPC[2],6)
								;EntityTexture e\room\NPC[2]\obj, DTextures[7]
								;FreeTexture DTextures[7]
								e\room\NPC[3]\State = 9								
								
								If e\room\NPC[7]\SoundChn<>0 Then
									If ChannelPlaying(e\room\NPC[7]\SoundChn) Then
										StopChannel(e\room\NPC[7]\SoundChn)
										FreeSound_Strict e\room\NPC[7]\Sound 
										e\room\NPC[7]\Sound=0											
									EndIf
								EndIf
								
								FreeEntity(e\room\Objects[9])
								e\room\Objects[9]=0
								FreeEntity(e\room\Objects[10])
								e\room\Objects[10]=0
								
								If e\room\NPC[5]<>Null Then
									RemoveNPC(e\room\NPC[5])
								EndIf
								
								For i = 8 To 10
									If e\room\NPC[i]<>Null Then
										RemoveNPC(e\room\NPC[i])
									EndIf
								Next
								
								FreeSound_Strict e\room\NPC[3]\Sound
								e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\EscortDone"+Rand(1,5)+".ogg")
								e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
								
								PositionEntity e\room\NPC[6]\Collider, EntityX(e\room\obj,True)-1190*RoomScale, 450*RoomScale, EntityZ(e\room\obj, True)+456*RoomScale, True
								ResetEntity e\room\NPC[6]\Collider
								PointEntity e\room\NPC[6]\Collider, e\room\obj
								e\room\NPC[6]\CurrSpeed = 0
								e\room\NPC[6]\State = 0
								
								e\EventState3 = 905 ;910
								
								e\room\RoomDoors[3]\locked = False
								UseDoor(e\room\RoomDoors[3],False)
								e\room\RoomDoors[3]\locked = True
								
								e\room\NPC[4]\State = 9
								
								;e\room\RoomDoors[2]\locked = False
								;UseDoor(e\room\RoomDoors[2],False)
								;e\room\RoomDoors[2]\locked = True
							EndIf
						ElseIf e\EventState3 <= 905
							If (Not ChannelPlaying(e\room\NPC[3]\SoundChn)) And e\room\NPC[3]\Frame < 358.0 Then
								e\room\NPC[3]\State = 8
								FreeSound_Strict e\room\NPC[3]\Sound
								e\room\NPC[3]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Ulgrin\OhAndByTheWay.ogg")
								e\room\NPC[3]\SoundChn = PlaySound2(e\room\NPC[3]\Sound, Camera, e\room\NPC[3]\Collider)
								SetNPCFrame(e\room\NPC[3],358)
							ElseIf e\room\NPC[3]\Frame >= 358.0 Then
								PointEntity e\room\NPC[3]\Collider,Collider
								RotateEntity e\room\NPC[3]\Collider,0,EntityYaw(e\room\NPC[3]\Collider),0
								
								If e\room\NPC[3]\Frame <= 481.5 Then
									Local prevAnimFrame# = e\room\NPC[3]\Frame
									AnimateNPC(e\room\NPC[3],358.0,482.0,0.4,False)
								Else
									AnimateNPC(e\room\NPC[3],483.0,607.0,0.2,True)
									If (EntityDistance(Collider, e\room\NPC[3]\Collider)<1.5) Then
										If EntityInView(e\room\NPC[3]\obj, Camera) Then
											DrawHandIcon = True
											
											If MouseHit1 Then
												SelectedItem = CreateItem("Document SCP-173", "paper", 0.0, 0.0, 0.0)
												EntityType SelectedItem\collider,HIT_ITEM
												
												PickItem(SelectedItem)
												
												e\room\RoomDoors[2]\locked = False
												UseDoor(e\room\RoomDoors[2],False)
												e\room\RoomDoors[2]\locked = True
												e\EventState3 = 910
												SetNPCFrame(e\room\NPC[3],608)
											EndIf
										EndIf
									EndIf
								EndIf
							EndIf
						Else
							If e\room\NPC[3]\Frame <= 620.5 And e\room\NPC[3]\State = 8 Then
								AnimateNPC(e\room\NPC[3],608,621,0.4,False)
							Else
								e\room\NPC[3]\Angle = EntityYaw(e\room\NPC[3]\Collider)
								e\room\NPC[3]\State = 9
								e\room\NPC[4]\State = 9
								If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj), EntityZ(e\room\obj)) < 4.0 Then
									e\room\RoomDoors[2]\locked = False
									UseDoor(e\room\RoomDoors[2],False)
									e\room\RoomDoors[2]\locked = True
									e\EventState3 = 0
									e\room\NPC[3]\State = 0
									e\room\NPC[4]\State = 0
									
									UseDoor(e\room\RoomDoors[1],False)
								EndIf
							EndIf
						EndIf
						
						;the scientist sitting at his desk
						If e\room\NPC[7]<>Null Then
							RotateEntity e\room\NPC[7]\Collider,0,180+Sin(MilliSecs2()/20)*3,0,True
							PositionEntity e\room\NPC[7]\Collider, EntityX(e\room\obj,True)-3361*RoomScale,-315*RoomScale,EntityZ(e\room\obj,True)-2165*RoomScale
							ResetEntity e\room\NPC[7]\Collider
							
							e\room\NPC[7]\State = 6
							SetNPCFrame(e\room\NPC[7], 182)
							
							If e\room\NPC[6]\State=1 And e\room\NPC[7]\Sound<>0 Then 
								If e\room\NPC[7]\SoundChn<>0 Then
									If (Not ChannelPlaying(e\room\NPC[7]\SoundChn)) Then FreeSound_Strict e\room\NPC[7]\Sound : e\room\NPC[7]\Sound=0	
									
								EndIf
								
								If e\room\NPC[7]\Sound<>0 Then e\room\NPC[7]\SoundChn = LoopSound2(e\room\NPC[7]\Sound, e\room\NPC[7]\SoundChn, Camera, e\room\NPC[7]\Collider, 7.0)
							EndIf
						EndIf
						
						For i = 3 To 4
							If e\room\NPC[i]\Sound <> 0 Then
								If ChannelPlaying(e\room\NPC[i]\SoundChn)=False Then
									FreeSound_Strict e\room\NPC[i]\Sound 
									e\room\NPC[i]\Sound=0
								Else
									e\room\NPC[i]\SoundChn=LoopSound2(e\room\NPC[i]\Sound, e\room\NPC[i]\SoundChn, Camera, e\room\NPC[i]\Collider)
								EndIf
							EndIf
						Next
						
					Else
						
						;ambience inside the chamber
						If IntroSFX(18)<>0 Then e\SoundCHN2 = LoopSound2(IntroSFX(18), e\SoundCHN2, Camera, e\room\Objects[4], 6)
						
						;[Block]
						If e\EventState = 0 Then
							If PlayerRoom = e\room Then
								IntroSFX(0) = LoadSound_Strict("SFX\Room\Intro\Scientist\Franklin\EnterChamber.ogg")
								IntroSFX(1) = LoadSound_Strict("SFX\Room\Intro\Scientist\Franklin\Approach173.ogg")
								IntroSFX(2) = LoadSound_Strict("SFX\Room\Intro\Scientist\Franklin\Problem.ogg")
								For i = 4 To 6
									IntroSFX(i) = LoadSound_Strict("SFX\Room\Intro\Scientist\Franklin\Refuse" + (i - 3) + ".ogg")
								Next
								IntroSFX(16) = LoadSound_Strict("SFX\Room\Intro\Horror.ogg")
								IntroSFX(17) = LoadSound_Strict("SFX\Room\Intro\See173.ogg")
								IntroSFX(18) = LoadSound_Strict("SFX\Room\Intro\173Chamber.ogg")
								
								Curr173\Idle = True
								
								e\room\NPC[3] = CreateNPC(NPCtypeGuard, e\room\x-4096*RoomScale+Rnd(-0.3,0.3), 0.3, e\room\z+Rand(860,896)*RoomScale)
								RotateEntity e\room\NPC[3]\Collider,0,e\room\angle+180,0
								e\room\NPC[3]\State = 7
								e\room\NPC[4] = CreateNPC(NPCtypeGuard, e\room\x-3840*RoomScale, 0.3, e\room\z+768*RoomScale)
								RotateEntity e\room\NPC[4]\Collider,0,e\room\angle+135,0
								e\room\NPC[4]\State = 7
								;SetNPCFrame(e\room\NPC[4], Rnd(1035, 1326))
								e\room\NPC[5] = CreateNPC(NPCtypeGuard, e\room\x-8288*RoomScale, 0.3, e\room\z+1096*RoomScale)
								e\room\NPC[5]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Music"+Rand(1,5)+".ogg")
								RotateEntity e\room\NPC[5]\Collider, 0, e\room\angle+180, 0, True
								e\room\NPC[5]\State = 7
								e\room\NPC[5]\Sound2 = LoadSound_Strict("SFX\Room\Intro\Guard\PlayerEscape.ogg")
								e\room\NPC[6] = CreateNPC(NPCtypeD, e\room\x-3712*RoomScale, -0.3, e\room\z-2208*RoomScale)
								;tex = LoadTexture_Strict("GFX\npcs\scientist2.jpg")
								;EntityTexture e\room\NPC[6]\obj, DTextures[4]
								;FreeTexture tex
								ChangeNPCTextureID(e\room\NPC[6],3)
								e\room\NPC[7] = CreateNPC(NPCtypeD, e\room\x-3712*RoomScale, -0.3, e\room\z-2208*RoomScale)
								;tex = LoadTexture_Strict("GFX\npcs\scientist.jpg")
								e\room\NPC[7]\Sound = LoadSound_Strict("SFX\Room\Intro\Scientist\Conversation.ogg")
								;EntityTexture e\room\NPC[7]\obj, DTextures[3]
								;FreeTexture tex
								ChangeNPCTextureID(e\room\NPC[7],2)
								pvt = CreatePivot()
								RotateEntity pvt,90,0,0
								
								e\room\NPC[8] = CreateNPC(NPCtypeGuard, e\room\x-3800.0*RoomScale, 1.0, e\room\z-3900.0*RoomScale)
								e\room\NPC[8]\State = 7
								e\room\NPC[9] = CreateNPC(NPCtypeD, e\room\x-4000.0*RoomScale, 1.1, e\room\z-3900.0*RoomScale)
								e\room\NPC[9]\State2 = 1.0
								tex = LoadTexture_Strict("GFX\npcs\classd3.jpg")
								EntityTexture e\room\NPC[9]\obj, tex
								FreeTexture tex
								e\room\NPC[10] = CreateNPC(NPCtypeGuard, e\room\x-4200.0*RoomScale, 1.0, e\room\z-3900.0*RoomScale)
								e\room\NPC[10]\State = 7
								
								For i = 8 To 10
									PositionEntity pvt,EntityX(e\room\NPC[i]\Collider),EntityY(e\room\NPC[i]\Collider),EntityZ(e\room\NPC[i]\Collider)
									EntityPick pvt,20.0
									If PickedEntity() <> 0
										PositionEntity e\room\NPC[i]\Collider,PickedX(),PickedY(),PickedZ(),True
										AlignToVector e\room\NPC[i]\Collider,-PickedNX(),-PickedNY(),-PickedNZ(),3
										RotateEntity e\room\NPC[i]\Collider,0,90,0
									EndIf
								Next
								
								FreeEntity pvt
								
								PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True), 0.5, EntityZ(e\room\Objects[5], True))
								ResetEntity(Curr173\Collider)
								
								PositionEntity Collider, PlayerRoom\x-(3072+1024)*RoomScale, 0.3, PlayerRoom\z+192.0*RoomScale
								ResetEntity Collider
								
								e\EventState = 1
								e\EventState3 = 1
							EndIf
						ElseIf e\EventState < 10000
							If e\room\NPC[6]\SoundChn<>0 Then 
								If ChannelPlaying (e\room\NPC[6]\SoundChn) Then
									e\room\NPC[6]\State = 6
									;PointEntity e\room\NPC[6]\Collider, e\room\obj
									If AnimTime(e\room\NPC[6]\obj)=>325 Then
										Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),326,328, 0.02, False)
									Else
										Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),320,328, 0.05, False)
									EndIf
								Else
									Animate2(e\room\NPC[6]\obj,AnimTime(e\room\NPC[6]\obj), 328,320,-0.02, False)
								EndIf
							EndIf
							
							If IntroSFX(17)<>0 Then
								If EntityVisible(Curr173\Collider, Collider) Then
									If EntityInView(Curr173\obj, Camera) Then
										Msg = "Press "+KeyName(KEY_BLINK)+" to blink."
										MsgTimer = 70*4
										PlaySound_Strict IntroSFX(17)
										IntroSFX(17)=0
									EndIf
								EndIf
							EndIf
							
							e\EventState = Min(e\EventState + (FPSfactor / 3), 5000)
							If e\EventState >= 130 And e\EventState - (FPSfactor/3) < 130 Then
								e\room\NPC[6]\SoundChn = PlaySound_Strict(IntroSFX(0))
							ElseIf e\EventState > 230
								temp = True
								For i = 1 To 2
									If Distance(EntityX(e\room\NPC[i]\Collider), EntityZ(e\room\NPC[i]\Collider), EntityX(e\room\Objects[i + 2], True), EntityZ(e\room\Objects[i + 2], True)) > 0.3 Then
										
										PointEntity(e\room\NPC[i]\obj, e\room\Objects[i + 2])
										RotateEntity(e\room\NPC[i]\Collider, 0, CurveValue(EntityYaw(e\room\NPC[i]\obj),EntityYaw(e\room\NPC[i]\Collider),15.0),0)
										;moveentity(e\room\npc(i).Collider, 0, 0, 0.015 * FPSfactor)
										If e\EventState > (200 + i * 30) Then e\room\NPC[i]\State = 1
										temp = False
									Else
										e\room\NPC[i]\State = 0
										
										PointEntity(e\room\NPC[i]\obj, e\room\Objects[5])
										RotateEntity(e\room\NPC[i]\Collider, 0, CurveValue(EntityYaw(e\room\NPC[i]\obj),EntityYaw(e\room\NPC[i]\Collider),15.0), 0)
										
									EndIf
								Next
								
								If EntityX(Collider) < (EntityX(e\room\obj)) + 408.0 * RoomScale Then
									If e\EventState => 450 And e\EventState - (FPSfactor/3) < 450 Then ;"mene huoneeseen"
										e\room\NPC[6]\SoundChn = PlaySound_Strict(IntroSFX(4))
									ElseIf e\EventState => 650 And e\EventState - (FPSfactor/3) < 650 ;"viimeinen varoitus, 5 sek aikaa"
										e\room\NPC[6]\SoundChn = PlaySound_Strict(IntroSFX(5))
									ElseIf e\EventState => 850 And e\EventState - (FPSfactor/3) < 850 ;"fire at will"
										UseDoor(e\room\RoomDoors[1],False)
										;e\room\RoomDoors[1]\open = False
										e\room\NPC[6]\SoundChn = PlaySound_Strict(IntroSFX(6))
									ElseIf e\EventState > 1000
										e\room\NPC[0]\State = 1
										e\room\NPC[0]\State2= 10
										e\room\NPC[0]\State3= 1
										e\room\NPC[3]\State = 11
										e\room\RoomDoors[2]\locked = False
										UseDoor(e\room\RoomDoors[2],False)
										e\room\RoomDoors[2]\locked = True
										e\EventState2 = 1
										Exit
									EndIf
									
									If e\EventState > 850 Then
										PositionEntity(Collider, Min(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
									End If
								ElseIf temp = True ;pelaaja ja molemmat npc:t huoneessa
									e\EventState = 10000
									UseDoor(e\room\RoomDoors[1],False)
								End If
							End If
							
							e\room\NPC[6]\State = 7
							PointEntity e\room\NPC[6]\obj, Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),20.0),0,True	
							
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True), EntityY(Curr173\Collider), EntityZ(e\room\Objects[5], True))
							RotateEntity(Curr173\Collider, 0, 0, 0, True)
							ResetEntity(Curr173\Collider)
						ElseIf e\EventState < 14000 ; player is inside the room
							e\EventState = Min(e\EventState + FPSfactor, 13000)
							
							If e\EventState < 10300 Then
								PositionEntity(Collider, Max(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
							End If
							
							e\room\NPC[6]\State = 6
							PointEntity e\room\NPC[6]\obj, Curr173\Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),50.0),0,True	
							
							If e\EventState => 10300 And e\EventState - FPSfactor < 10300 Then ;"please approach SCP-173..."
								e\SoundCHN = PlaySound_Strict(IntroSFX(1))
								PositionEntity(Collider, Max(EntityX(Collider), EntityX(e\room\obj) + 352.0 * RoomScale), EntityY(Collider), EntityZ(Collider))
							ElseIf e\EventState => 10440 And e\EventState - FPSfactor < 10440 ;the door opens
								UseDoor(e\room\RoomDoors[1],False)
								e\SoundCHN = PlaySound_Strict(IntroSFX(7)) ;bang
							ElseIf e\EventState => 10740 And e\EventState - FPSfactor < 10740 ;"there seems to be a problem..."
								e\SoundCHN = PlaySound_Strict(IntroSFX(2))
							ElseIf e\EventState => 11145 And e\EventState - FPSfactor < 11145;"I don't like this"
								e\SoundCHN = PlaySound_Strict(IntroSFX(10))
								e\room\NPC[1]\Sound = LoadSound_Strict("SFX\Room\Intro\ClassD\DontLikeThis.ogg")
								PlaySound2(e\room\NPC[1]\Sound, Camera, e\room\NPC[2]\Collider)
							ElseIf e\EventState => 11561 And e\EventState - FPSfactor < 11561 ;lights go out
								e\EventState = 14000
								PlaySound_Strict IntroSFX(16)
								e\room\NPC[2]\Sound = LoadSound_Strict("SFX\Room\Intro\ClassD\Breen.ogg")
								PlaySound2(e\room\NPC[2]\Sound, Camera, e\room\NPC[1]\Collider)
							End If
							
							;Guard Alert
							If e\EventState => 10440 And e\EventState - FPSfactor < 11561
								If EntityX(Collider) < EntityX(e\room\RoomDoors[1]\frameobj, True)
									If e\room\NPC[0]\State <> 12
										e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Balcony\Alert"+Rand(1,2)+".ogg")
										e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound,Camera,e\room\NPC[0]\Collider,20)
										e\room\NPC[0]\State = 12
										e\room\NPC[0]\State2 = 1
									EndIf
								EndIf
							EndIf
							
							If e\EventState > 10300 Then 
								
								If e\EventState > 10560 Then
									If e\EventState < 10750 Then
										e\room\NPC[1]\State = 1
										e\room\NPC[1]\CurrSpeed = 0.005										
									Else
										e\room\NPC[1]\State = 0
										e\room\NPC[1]\CurrSpeed = CurveValue(0,e\room\NPC[1]\CurrSpeed,10)	
									EndIf
									
								EndIf
								
								If AnimTime(e\room\NPC[6]\obj)=>325 Then
									Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),326,328, 0.02, False)
								Else
									Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),320,328, 0.05, False)
								EndIf
							EndIf
							
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[5], True),EntityY(Curr173\Collider), EntityZ(e\room\Objects[5], True))
							RotateEntity(Curr173\Collider, 0, 0, 0, True)
							ResetEntity(Curr173\Collider)
						ElseIf e\EventState < 20000
							pvt% = CreatePivot()
							PositionEntity pvt, EntityX(Camera), EntityY(Curr173\Collider,True)-0.05, EntityZ(Camera)
							PointEntity(pvt, Curr173\Collider)
							RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 40), 0)
							
							TurnEntity(pvt, 90, 0, 0)
							user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, 40)
							user_camera_pitch=user_camera_pitch-90
							FreeEntity pvt
							
							e\room\NPC[6]\State = 6
							PointEntity e\room\NPC[6]\obj, Curr173\Collider
							RotateEntity e\room\NPC[6]\Collider,0,CurveValue(EntityYaw(e\room\NPC[6]\obj),EntityYaw(e\room\NPC[6]\Collider),20.0),0,True	
							Animate2(e\room\NPC[6]\obj, AnimTime(e\room\NPC[6]\obj),357,381, 0.05)
							
							e\EventState = Min(e\EventState + FPSfactor, 19000)
							If e\EventState < 14100 Then ;lights go out and 173 kills the first Class D
								
								;14000-14030
								If e\EventState < 14060 Then
									BlinkTimer = Max((14000-e\EventState)/2-Rnd(0,1.0),-10)
									;0-60,   90-640
									If BlinkTimer = -10 Then
										PointEntity Curr173\Collider, e\room\NPC[1]\obj
										RotateEntity(Curr173\Collider, 0, EntityYaw(Curr173\Collider),0)
										MoveEntity Curr173\Collider, 0,0,Curr173\Speed*0.6*FPSfactor
										
										Curr173\SoundChn = LoopSound2(StoneDragSFX, Curr173\SoundChn, Camera, Curr173\Collider, 10.0, Curr173\State)
										
										Curr173\State = CurveValue(1.0, Curr173\State, 3)
										
									Else
										Curr173\State = Max(0, Curr173\State - FPSfactor / 20)
									EndIf
								ElseIf e\EventState < 14065
									BlinkTimer = -10
									If e\room\NPC[1]\State = 0 Then PlaySound2(NeckSnapSFX(Rand(0, 2)),Camera,Curr173\Collider)
									
									;e\room\NPC[0]\State=8
									SetAnimTime e\room\NPC[1]\obj, 0
									e\room\NPC[1]\State = 6
									PositionEntity(Curr173\Collider, EntityX(e\room\NPC[1]\obj), EntityY(Curr173\Collider), EntityZ(e\room\NPC[1]\obj))
									ResetEntity(Curr173\Collider)
									PointEntity(Curr173\Collider, e\room\NPC[2]\Collider)
									
									e\room\NPC[2]\State = 3
									RotateEntity e\room\NPC[2]\Collider, 0, EntityYaw(e\room\NPC[2]\Collider), 0
									Animate2(e\room\NPC[2]\obj, AnimTime(e\room\NPC[2]\obj),406,382,-0.01*15)
									MoveEntity e\room\NPC[2]\Collider, 0,0,-0.01*FPSfactor
									
									;Guard WTF
									e\room\NPC[0]\State = 12
									If e\room\NPC[0]\Sound<>0
										StopChannel(e\room\NPC[0]\SoundChn)
										FreeSound_Strict(e\room\NPC[0]\Sound)
										e\room\NPC[0]\Sound = 0
									EndIf
									e\room\NPC[0]\Angle = 180
									e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Balcony\WTF"+Rand(1,2)+".ogg")
									e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound,Camera,e\room\NPC[0]\Collider,20)
									e\room\NPC[0]\State2 = 0
								Else
									Animate2(e\room\NPC[1]\obj, AnimTime(e\room\NPC[1]\obj), 0, 19, 0.2, False)
									If e\room\NPC[2]\Sound=0 Then 
										e\room\NPC[2]\Sound = LoadSound_Strict("SFX\Room\Intro\ClassD\Gasp.ogg")
										PlaySound2 (e\room\NPC[2]\Sound, Camera, e\room\NPC[2]\Collider, 8.0)	
									EndIf									
								EndIf
								
								If e\EventState > 14080 And e\EventState - FPSfactor < 14080 Then PlaySound_Strict(IntroSFX(12))
								CameraShake = 3
							ElseIf e\EventState < 14200 ;kills the other class d
								Animate2(e\room\NPC[1]\obj, AnimTime(e\room\NPC[1]\obj), 0, 19, 0.2, False)
								
								;Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 110, 120, 0.2, False)
								e\room\NPC[0]\State=8
								If e\EventState > 14105 Then
									If e\room\NPC[2]\State<>6 Then PlaySound2 (NeckSnapSFX(1), Camera, e\room\NPC[2]\Collider, 8.0)
									e\room\NPC[2]\State = 6
									PositionEntity(Curr173\Collider, EntityX(e\room\NPC[2]\obj), EntityY(Curr173\Collider), EntityZ(e\room\NPC[2]\obj))
									ResetEntity(Curr173\Collider)
									PointEntity(Curr173\Collider, Collider)
								EndIf
								If e\EventState < 14130 Then 
									SetAnimTime e\room\NPC[2]\obj,50
									BlinkTimer = -10 : LightBlink = 1.0
								Else 
									Animate2(e\room\NPC[2]\obj, AnimTime(e\room\NPC[2]\obj), 50, 60, 0.2, False)
									Curr173\Idle = False
								EndIf
								If e\EventState > 14100 And e\EventState - FPSfactor < 14100 Then PlaySound_Strict(IntroSFX(8))
								If e\EventState < 14150 Then CameraShake = 5
							Else
								Animate2(e\room\NPC[2]\obj, AnimTime(e\room\NPC[2]\obj), 45, 60, 0.2, False)
								If e\EventState > 14300 Then 
									If e\EventState > 14600 And e\EventState < 14700 Then BlinkTimer = -10 : LightBlink = 1.0
									If EntityX(Collider) < (EntityX(e\room\obj)) + 448.0 * RoomScale Then e\EventState = 20000
								EndIf
							End If
						ElseIf e\EventState < 30000
							e\EventState = Min(e\EventState + FPSfactor, 30000)
							If e\EventState < 20100 Then
								CameraShake = 2
							Else
								If e\EventState < 20200 Then ;lights go out again and 173 teleports next to the guard
									If e\EventState > 20105 And e\EventState - FPSfactor < 20105 Then 
										PlaySound_Strict(IntroSFX(9))
										PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\obj) - 160.0 * RoomScale, EntityY(e\room\NPC[0]\Collider) + 0.1, EntityZ(e\room\obj) + 1280.0 * RoomScale)
										ResetEntity(e\room\NPC[0]\Collider)										
										
										;Guard OhShit
										If e\room\NPC[0]\Sound<>0
											StopChannel(e\room\NPC[0]\SoundChn)
											FreeSound_Strict(e\room\NPC[0]\Sound)
											e\room\NPC[0]\Sound = 0
										EndIf
										e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Room\Intro\Guard\Balcony\OhSh.ogg")
										e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound,Camera,e\room\NPC[0]\Collider,20)
									EndIf
									If e\EventState > 20105 Then
										Curr173\Idle = True
										PointEntity(e\room\NPC[0]\Collider, Curr173\obj)
										PositionEntity(Curr173\Collider, EntityX(e\room\obj) - 608.0 * RoomScale, EntityY(e\room\obj) + 480.0 * RoomScale, EntityZ(e\room\obj) + 1312.0 * RoomScale)
										ResetEntity(Curr173\Collider)
										PointEntity(Curr173\Collider, e\room\NPC[0]\Collider)
									EndIf
									
									BlinkTimer = -10 : LightBlink = 1.0
									CameraShake = 3
								ElseIf e\EventState < 20300 ;lights on, the guard starts shooting at 173
									PointEntity(e\room\NPC[0]\Collider, Curr173\Collider)
									MoveEntity(e\room\NPC[0]\Collider, 0, 0, -0.002)
									e\room\NPC[0]\State = 2
									UpdateSoundOrigin(e\room\NPC[0]\SoundChn,Camera,e\room\NPC[0]\Collider,20)
									If e\EventState > 20260 And e\EventState - FPSfactor < 20260 Then PlaySound_Strict(IntroSFX(12))
								Else ;lights out, guard dies
									
									If e\EventState - FPSfactor < 20300 Then
										BlinkTimer = -10 : LightBlink = 1.0
										CameraShake = 3
										PlaySound_Strict(IntroSFX(11))
										PlaySound2 (NeckSnapSFX(1), Camera, e\room\NPC[0]\Collider, 8.0)
										
										Curr173\Idle = False
										
										e\SoundCHN = PlaySound_Strict(IntroSFX(15))
										
										PositionEntity(Curr173\Collider, EntityX(PlayerRoom\obj) - 400.0 * RoomScale, 100.0, EntityZ(PlayerRoom\obj) + 1072.0 * RoomScale)
										ResetEntity(Curr173\Collider)
										
										For r.Rooms = Each Rooms
											If r\RoomTemplate\Name = "start" Then
												DebugLog "tostart"
												;Msg = "Press "+KeyName(KEY_SAVE)+" to save."
												;MsgTimer = 70*8
												
												PlayerRoom = r
												
												x# = EntityX(r\obj, True)+3712*RoomScale
												y# = 384.0*RoomScale
												z# = EntityZ(r\obj, True)+1312*RoomScale
												
												PositionEntity(Collider, x  + (EntityX(Collider) - EntityX(e\room\obj)), y+EntityY(Collider)+0.4, z + (EntityZ(Collider) - EntityZ(e\room\obj)))
												DropSpeed = 0
												ResetEntity(Collider)
												
												For i = 0 To 2
													PositionEntity(e\room\NPC[i]\Collider, x + (EntityX(e\room\NPC[i]\Collider) - EntityX(e\room\obj)), y+EntityY(e\room\NPC[i]\Collider)+0.4, z + (EntityZ(e\room\NPC[i]\Collider) - EntityZ(e\room\obj)))
													ResetEntity(e\room\NPC[i]\Collider)
												Next
												
												ShouldPlay = 0
												
												For i = 0 To 9
													FreeSound_Strict IntroSFX(i)
												Next
												For i = 16 To 18
													FreeSound_Strict IntroSFX(i)
												Next
												
												r\NPC[0]=e\room\NPC[0]
												r\NPC[0]\State=8
												
												For do.doors = Each Doors
													If do\room = e\room Then
														RemoveDoor(do)
													EndIf
												Next
												
												For w.waypoints = Each WayPoints
													If w\room = e\room Then 
														FreeEntity w\obj
														Delete w
													EndIf
												Next
												
												For i = 3 To 4
													RemoveNPC(e\room\NPC[i])
												Next
												r\NPC[1]=e\room\NPC[6]
												;RemoveNPC(e\room\NPC[7])
												
												FreeEntity e\room\obj
												Delete e\room
												
												For sc.SecurityCams = Each SecurityCams
													If sc\room = e\room Then Delete sc
												Next
												
												ShowEntity Fog
												AmbientLight Brightness, Brightness, Brightness
												CameraFogRange(Camera, CameraFogNear, CameraFogFar)
												CameraFogMode(Camera, 1)
												
												e\EventState2 = 1
												
												;For i = 8 To 10
												;	RemoveNPC(e\room\NPC[i])
												;Next
												
												Exit
											EndIf
										Next
									EndIf
									
								EndIf
								
							EndIf
						EndIf
						
						;[End block]
						
					EndIf
					
				Else
					If KillTimer<0 Then
						If e\room\NPC[3]\State = 1 Then 
							LoadEventSound(e,"SFX\Room\Intro\Guard\Ulgrin\EscortTerminated.ogg")
							PlaySound_Strict e\Sound
						EndIf
					EndIf
					
					For i = 0 To 6
						If IntroSFX(i)<>0 Then FreeSound_Strict IntroSFX(i) : IntroSFX(i)=0
					Next
					FreeSound_Strict IntroSFX(16) : IntroSFX(16)=0
					
					e\EventState2 = 1
				EndIf
				
				If PlayerRoom = e\room Then
					If e\EventState >= 10 Then
						CameraRange(Camera, 0.05, 15)
						If e\room\NPC[7]<>Null Then
							RemoveNPC(e\room\NPC[7])
						EndIf
					Else															
						CameraRange(Camera, 0.05, 40)
					EndIf	
					CameraFogMode(Camera, 0)
	 	            AmbientLight (140, 140, 140)
	   				HideEntity(Fog)
					
					LightVolume = 4.0
					TempLightVolume = 4.0			
				Else
					DebugLog "delete intro event"
					RemoveEvent(e)		
				EndIf	
				;[End Block]
			Case "buttghost"
				;[Block]
				If PlayerRoom = e\room Then
					If EntityDistance(Collider, e\room\Objects[0]) < 1.8 Then
						If e\EventState = 0
							GiveAchievement(Achv789)
							e\SoundCHN = PlaySound2(ButtGhostSFX, Camera,e\room\Objects[0])
							e\EventState = 1
						Else
							If (Not ChannelPlaying(e\SoundCHN))
								RemoveEvent(e)
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "checkpoint"
				;[Block]
				If PlayerRoom = e\room Then
					;If e\room\RoomDoors[0]\open <> e\EventState Then
					;	If e\Sound = 0 Then LoadEventSound(e,"SFX\Door\DoorCheckpoint.Ogg")
					;	PlaySound_Strict e\Sound
					;EndIf
					
					;play a sound clip when the player passes through the gate
					If e\EventState2 = 0 Then
						If EntityZ(Collider) < e\room\z Then
							If PlayerZone = 1 Then
								PlaySound_Strict(LoadTempSound("SFX\Ambient\ToZone2.ogg"))
							Else
								PlaySound_Strict(LoadTempSound("SFX\Ambient\ToZone3.ogg"))
							EndIf
							e\EventState2 = 1
						EndIf
					EndIf
					
					If e\EventState3=0 Then
						If Rand(2)=1 Then
							GiveAchievement(Achv1048)
							e\room\Objects[1]=LoadAnimMesh_Strict("GFX\npcs\scp-1048.b3d")
							ScaleEntity e\room\Objects[1], 0.05,0.05,0.05
							PositionEntity(e\room\Objects[1],EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True))
							SetAnimTime e\room\Objects[1],267	
						EndIf
						
						e\EventState3 = 1
					ElseIf e\room\Objects[1]<>0
						If e\EventState3 = 1 Then
							PointEntity e\room\Objects[1], Collider
							RotateEntity e\room\Objects[1], -90, EntityYaw(e\room\Objects[1]),0
							angle = WrapAngle(DeltaYaw(Collider, e\room\Objects[1]))
							If angle<40 Or angle > 320 Then e\EventState3=2
						ElseIf e\EventState3 = 2
							PointEntity e\room\Objects[1], Collider
							RotateEntity e\room\Objects[1], -90, EntityYaw(e\room\Objects[1]),0
							Animate2(e\room\Objects[1],AnimTime(e\room\Objects[1]),267,283,0.3,False)
							If AnimTime(e\room\Objects[1])=283 Then e\EventState3=3
						ElseIf e\EventState3 = 3
							Animate2(e\room\Objects[1],AnimTime(e\room\Objects[1]),283,267,-0.2,False)
							If AnimTime( e\room\Objects[1])=267 Then e\EventState3=4
						ElseIf e\EventState3 = 4
							angle = WrapAngle(DeltaYaw(Collider, e\room\Objects[1]))
							If angle>90 And angle < 270 Then 
								FreeEntity(e\room\Objects[1])
								e\room\Objects[1]=0
								e\EventState3=5
							EndIf
						EndIf
					EndIf
				EndIf
				
				If e\room\RoomTemplate\Name = "checkpoint2"
					For e2.Events = Each Events
						If e2\EventName = "008"
							If e2\EventState = 2
								If e\room\RoomDoors[0]\locked
									TurnCheckpointMonitorsOff(1)
									e\room\RoomDoors[0]\locked = False
									e\room\RoomDoors[1]\locked = False
								EndIf
							Else
								If e\room\dist < 12
									UpdateCheckpointMonitors(1)
									e\room\RoomDoors[0]\locked = True
									e\room\RoomDoors[1]\locked = True
								EndIf
							EndIf
						EndIf
					Next
				Else
					For e2.Events = Each Events
						If e2\EventName = "room2sl"
							If e2\EventState3 = 0
								If e\room\dist < 12
									TurnCheckpointMonitorsOff(0)
									e\room\RoomDoors[0]\locked = False
									e\room\RoomDoors[1]\locked = False
								EndIf
							Else
								If e\room\dist < 12
									UpdateCheckpointMonitors(0)
									e\room\RoomDoors[0]\locked = True
									e\room\RoomDoors[1]\locked = True
								EndIf
							EndIf
						EndIf
					Next
				EndIf
				
				If e\room\RoomDoors[0]\open <> e\EventState Then
					If e\Sound = 0 Then LoadEventSound(e,"SFX\Door\DoorCheckpoint.ogg")
					e\SoundCHN = PlaySound2(e\Sound,Camera,e\room\RoomDoors[0]\obj)
					e\SoundCHN2 = PlaySound2(e\Sound,Camera,e\room\RoomDoors[1]\obj)
				EndIf
				
				e\EventState = e\room\RoomDoors[0]\open
				
				If ChannelPlaying(e\SoundCHN)
					UpdateSoundOrigin(e\SoundCHN,Camera,e\room\RoomDoors[0]\obj)
				EndIf
				If ChannelPlaying(e\SoundCHN2)
					UpdateSoundOrigin(e\SoundCHN2,Camera,e\room\RoomDoors[1]\obj)
				EndIf
				;[End Block]
			Case "coffin", "coffin106"
				;[Block]
				
				If e\EventState < MilliSecs2() Then
					;SCP-079 starts broadcasting 895 camera feed on monitors after leaving the first zone
					If PlayerZone > 0 Then 
						If EntityPitch(e\room\Levers[0],True) > 0 Then ;camera feed on
							For sc.SecurityCams = Each SecurityCams
								If sc\CoffinEffect=0 And sc\room\RoomTemplate\Name<>"room106" And sc\room\RoomTemplate\Name<>"room205" Then sc\CoffinEffect = 2
								If sc\room = e\room Then sc\Screen = True
							Next
						Else ;camera feed off
							For sc.SecurityCams = Each SecurityCams
								If sc\CoffinEffect<>1 Then sc\CoffinEffect = 0
								If sc\room = e\room Then sc\Screen = False
							Next
						EndIf						
					EndIf
					
					e\EventState = MilliSecs2()+3000
				EndIf
				
				If PlayerRoom = e\room Then
					CoffinDistance = EntityDistance(Collider, e\room\Objects[1])
					If CoffinDistance < 1.5 Then 
						GiveAchievement(Achv895)
						If (Not Contained106) And e\EventName="coffin106" And e\EventState2 = 0 Then
							de.Decals = CreateDecal(0, EntityX(e\room\Objects[1],True), -1531.0*RoomScale, EntityZ(e\room\Objects[1],True), 90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals()
							
							If Curr106\State > 0 Then
								PositionEntity Curr106\Collider, EntityX(e\room\Objects[1],True), -10240*RoomScale, EntityZ(e\room\Objects[1],True)
								Curr106\State = -0.1
								ShowEntity Curr106\obj
								e\EventState2 = 1
							EndIf
						EndIf
					ElseIf CoffinDistance < 3.0 Then
						If e\room\NPC[0]=Null Then
							e\room\NPC[0]=CreateNPC(NPCtypeGuard,e\room\x,e\room\y,e\room\z)
							RotateEntity e\room\NPC[0]\Collider,0,e\room\angle+90,0
							e\room\NPC[0]\State = 8
							;270, 286, 0.4, False
							SetNPCFrame(e\room\NPC[0],270)
							e\room\NPC[0]\GravityMult = 0.0
							e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Room\895Chamber\GuardIdle"+Rand(1,3)+".ogg")
							e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound,Camera,e\room\NPC[0]\Collider)
							e\room\NPC[0]\IsDead = True
							e\room\NPC[0]\FallingPickDistance = 0.0
						EndIf
					ElseIf CoffinDistance > 5.0 Then
						If e\room\NPC[0]<>Null Then
							If e\room\NPC[0]\PrevState = 0 Then
								If ChannelPlaying(e\room\NPC[0]\SoundChn) Then
									StopChannel e\room\NPC[0]\SoundChn
								EndIf
								FreeSound_Strict e\room\NPC[0]\Sound
								e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Room\895Chamber\GuardScream"+Rand(1,3)+".ogg")
								e\room\NPC[0]\SoundChn = PlaySound2(e\room\NPC[0]\Sound,Camera,e\room\NPC[0]\Collider,100)
								e\room\NPC[0]\PrevState = 1
								e\room\NPC[0]\State2 = 0.0
							EndIf
						EndIf
					EndIf
					
					If e\room\NPC[0]<>Null Then
						UpdateSoundOrigin(e\room\NPC[0]\SoundChn,Camera,e\room\NPC[0]\Collider,100)
						If e\room\NPC[0]\PrevState = 0 Then
							e\room\NPC[0]\GravityMult = 0.0
						ElseIf e\room\NPC[0]\PrevState = 1 Then
							If e\room\NPC[0]\State2 < 70*1 Then
								e\room\NPC[0]\State2 = e\room\NPC[0]\State2 + FPSfactor
								e\room\NPC[0]\GravityMult = 0.0
							Else
								e\room\NPC[0]\GravityMult = 1.0
							EndIf
							If EntityY(e\room\NPC[0]\Collider)>(-1531.0*RoomScale)+0.35 Then
								dist# = EntityDistance(Collider,e\room\NPC[0]\Collider)
								If dist<0.8 Then ;get the player out of the way
									fdir# = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\NPC[0]\Collider,True),EntityZ(e\room\NPC[0]\Collider,True))
									TranslateEntity Collider,Cos(-fdir+90)*(dist-0.8)*(dist-0.8),0,Sin(-fdir+90)*(dist-0.8)*(dist-0.8)
								EndIf
								
								If EntityY(e\room\NPC[0]\Collider)>0.6 Then EntityType e\room\NPC[0]\Collider,0
							Else
								e\EventState=e\EventState+FPSfactor
								AnimateNPC(e\room\NPC[0], 270, 286, 0.4, False)
								If e\Sound=0 Then
									LoadEventSound(e,"SFX\General\BodyFall.ogg")
									e\SoundCHN = PlaySound_Strict(e\Sound)
									
									de.Decals = CreateDecal(3, EntityX(e\room\obj), -1531.0*RoomScale, EntityZ(e\room\obj), 90, Rand(360), 0)
									de\Size = 0.4 : ScaleSprite(de\obj,de\Size,de\Size) : UpdateDecals()
								EndIf
								If e\room\NPC[0]\Frame = 286.0 Then
									e\room\NPC[0]\PrevState = 2
								EndIf
							EndIf
							If e\room\NPC[0]\SoundChn2 = 0 Then
								e\room\NPC[0]\Sound2 = LoadSound_Strict("SFX\Room\895Chamber\GuardRadio.ogg")
								e\room\NPC[0]\SoundChn2 = LoopSound2(e\room\NPC[0]\Sound2,e\room\NPC[0]\SoundChn2,Camera,e\room\NPC[0]\Collider,5)
							EndIf
						ElseIf e\room\NPC[0]\PrevState = 2 Then
							If (Not ChannelPlaying(e\SoundCHN)) And e\Sound<>0 Then
								FreeSound_Strict e\Sound : e\Sound = 0
								e\SoundCHN = 0
							EndIf
							If (Not ChannelPlaying(e\room\NPC[0]\SoundChn)) And e\room\NPC[0]\Sound<>0 Then
								FreeSound_Strict e\room\NPC[0]\Sound : e\room\NPC[0]\Sound = 0
								e\room\NPC[0]\SoundChn = 0
							EndIf
							If e\room\NPC[0]\Sound2 = 0 Then
								e\room\NPC[0]\Sound2 = LoadSound_Strict("SFX\Room\895Chamber\GuardRadio.ogg")
							EndIf
							e\room\NPC[0]\SoundChn2 = LoopSound2(e\room\NPC[0]\Sound2,e\room\NPC[0]\SoundChn2,Camera,e\room\NPC[0]\Collider,5)
						EndIf
					EndIf
					
					If WearingNightVision > 0 Then
						Local hasBatteryFor895% = 0
						For i% = 0 To MaxItemAmount - 1
							If (Inventory(i) <> Null) Then
								If (WearingNightVision = 1 And Inventory(i)\itemtemplate\tempname = "nvgoggles") Or (WearingNightVision = 2 And Inventory(i)\itemtemplate\tempname = "supernv") Or (WearingNightVision = 3 And Inventory(i)\itemtemplate\tempname = "finenvgoggles") Then
									If Inventory(i)\state > 0.0 Or WearingNightVision=3 Then
										hasBatteryFor895 = 1
										Exit
									EndIf
								EndIf
							EndIf
						Next
						If (CoffinDistance < 4.0) And (hasBatteryFor895) And (Not Wearing714) Then
							tempF# = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True))
							tempF2# = EntityYaw(Collider)
							tempF3# = angleDist(tempF+90+Sin(WrapAngle(e\EventState3/10)),tempF2)
							TurnEntity Collider, 0,tempF3/4,0,True
							tempF# = Abs(point_distance(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True)))
							tempF2# = -60.0 * Min(Max((2.0-tempF)/2.0,0.0),1.0)
							user_camera_pitch=(user_camera_pitch * 0.8)+(tempF2 * 0.2)
							
							Sanity = Sanity-(FPSfactor*1.1/WearingNightVision)
							RestoreSanity = False
							BlurTimer = Sin(MilliSecs2()/10)*Abs(Sanity)
							
							If VomitTimer < 0 Then
								RestoreSanity = False
								Sanity = -1010
							EndIf
							
							If Sanity < -1000 Then
								If WearingNightVision > 1
									DeathMSG = Chr(34)+"Class D viewed SCP-895 through a pair of digital night vision goggles, presumably enhanced by SCP-914. It might be possible that the subject "
									DeathMSG = DeathMSG + "was able to resist the memetic effects partially through these goggles. The goggles have been stored for further study."+Chr(34)
								Else
									DeathMSG = Chr(34)+"Class D viewed SCP-895 through a pair of digital night vision goggles, killing him."+Chr(34)
								EndIf
								EntityTexture(NVOverlay, NVTexture)
								If VomitTimer < -10 Then
									Kill()
								EndIf
							ElseIf Sanity < - 800 Then
								If Rand(3) = 1 Then EntityTexture(NVOverlay, NVTexture)
								If Rand(6) < 5 Then
									EntityTexture(NVOverlay, GorePics(Rand(0, 5)))
									For i% = 0 To MaxItemAmount - 1
										If (Inventory(i) <> Null) Then
											If (WearingNightVision = 1 And Inventory(i)\itemtemplate\tempname = "nvgoggles") Or (WearingNightVision = 2 And Inventory(i)\itemtemplate\tempname = "supernv") Or (WearingNightVision = 3 And Inventory(i)\itemtemplate\tempname = "finenvgoggles") Then
												If Inventory(i)\state2 = 1 Then PlaySound_Strict(HorrorSFX(1))
												Inventory(i)\state2 = 2
												Exit
											EndIf
										EndIf
									Next
								EndIf
								BlurTimer = 1000
								If VomitTimer = 0 Then
									VomitTimer = 1
								EndIf
							ElseIf Sanity < - 500 Then
								If Rand(7) = 1 Then EntityTexture(NVOverlay, NVTexture)
								If Rand(50) = 1 Then
									EntityTexture(NVOverlay, GorePics(Rand(0, 5)))
									For i% = 0 To MaxItemAmount - 1
										If (Inventory(i) <> Null) Then
											If (WearingNightVision = 1 And Inventory(i)\itemtemplate\tempname = "nvgoggles") Or (WearingNightVision = 2 And Inventory(i)\itemtemplate\tempname = "supernv") Or (WearingNightVision = 3 And Inventory(i)\itemtemplate\tempname = "finenvgoggles") Then
												If Inventory(i)\state2 = 0 Then PlaySound_Strict(HorrorSFX(0))
												Inventory(i)\state2 = 1
												Exit
											EndIf
										EndIf
									Next
								EndIf
							Else
								EntityTexture(NVOverlay, NVTexture)
								For i% = 0 To MaxItemAmount - 1
									If (Inventory(i) <> Null) Then
										If (WearingNightVision = 1 And Inventory(i)\itemtemplate\tempname = "nvgoggles") Or (WearingNightVision = 2 And Inventory(i)\itemtemplate\tempname = "supernv") Or (WearingNightVision = 3 And Inventory(i)\itemtemplate\tempname = "finenvgoggles") Then
											Inventory(i)\state2 = 0
										EndIf
									EndIf
								Next
							EndIf
						EndIf
					EndIf
					
					If e\EventState3>0.0 Then e\EventState3=Max(e\EventState3-FPSfactor,0.0)
					If e\EventState3=0.0 Then
						e\EventState3=-1.0
						EntityTexture(NVOverlay, NVTexture)
						If WearingNightVision = 1 Then
							EntityColor(NVOverlay, 0,255,0)
						ElseIf WearingNightVision = 2 Then
							EntityColor(NVOverlay, 0,100,255)
						EndIf
					EndIf
					
					ShouldPlay = 66
					
					If UpdateLever(e\room\Levers[0]) Then
						For sc.SecurityCams = Each SecurityCams
							If sc\CoffinEffect=0 And sc\room\RoomTemplate\Name<>"room106" Then sc\CoffinEffect = 2
							If sc\CoffinEffect = 1 Then EntityBlend(sc\ScrOverlay, 3)
							If sc\room = e\room Then sc\Screen = True
						Next
					Else
						For sc.SecurityCams = Each SecurityCams
							If sc\CoffinEffect <> 1 Then sc\CoffinEffect = 0
							If sc\CoffinEffect = 1 Then EntityBlend(sc\ScrOverlay, 0)
							If sc\room = e\room Then sc\Screen = False
						Next
					EndIf
				Else
					CoffinDistance = e\room\dist
				EndIf
				;[End Block]
			Case "endroom106"
				;[Block]
				If (Not Contained106) Then
					If e\EventState = 0 Then
						If e\room\dist < 8 And e\room\dist > 0 Then
							If Curr106\State < 0 Then 
								RemoveEvent(e)
							Else
								e\room\RoomDoors[0]\open = True
								
								e\room\NPC[0]=CreateNPC(NPCtypeD, EntityX(e\room\RoomDoors[0]\obj,True), 0.5, EntityZ(e\room\RoomDoors[0]\obj,True))
								
								ChangeNPCTextureID(e\room\NPC[0],4)
								
								PointEntity e\room\NPC[0]\Collider, e\room\obj
								RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider),0, True
								MoveEntity e\room\NPC[0]\Collider, 0,0,0.5 
								
								e\room\RoomDoors[0]\open = False
								PlaySound2(LoadTempSound("SFX\Door\EndroomDoor.ogg"), Camera, e\room\obj, 15)
								
								e\EventState = 1							
							EndIf
						EndIf
					ElseIf e\EventState = 1
						If PlayerRoom = e\room Then
							;PlaySound_Strict(CloseDoorSFX(1,0))
							;PlaySound_Strict(DecaySFX(0))
							;e\room\RoomDoors[0]\open = False
							e\room\NPC[0]\State = 1
							e\EventState = 2
							
							e\Sound = LoadSound_Strict("SFX\Character\Janitor\106Abduct.ogg")
							PlaySound_Strict(e\Sound)		
							
							If e\SoundCHN<>0 Then StopChannel e\SoundCHN
						ElseIf e\room\dist < 8
							If e\Sound = 0 Then e\Sound = LoadSound_Strict("SFX\Character\Janitor\Idle.ogg")
							e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\NPC[0]\obj, 15.0)
						EndIf
					ElseIf e\EventState = 2
						dist = EntityDistance(e\room\NPC[0]\Collider, e\room\obj)
						If dist<1.5 Then
							de.Decals = CreateDecal(0, EntityX(e\room\obj), 0.01, EntityZ(e\room\obj), 90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.008 : de\timer=10000 : UpdateDecals
							e\EventState = 3
							
							;PlaySound_Strict(DecaySFX(1))
						EndIf					
					Else
						dist = Distance(EntityX(e\room\NPC[0]\Collider),EntityZ(e\room\NPC[0]\Collider), EntityX(e\room\obj),EntityZ(e\room\obj))
						PositionEntity(Curr106\obj, EntityX(e\room\obj, True), 0.0, EntityZ(e\room\obj, True))
						;ResetEntity(Curr106\Collider)
						PointEntity(Curr106\obj, e\room\NPC[0]\Collider)
						RotateEntity(Curr106\obj, 0, EntityYaw(Curr106\obj), 0, True)
						
						Curr106\Idle = True
						
						If dist<0.4 Then
							If e\room\NPC[0]\State=1 Then 
								;PlaySound_Strict(HorrorSFX(10))
								SetNPCFrame(e\room\NPC[0],41)
							EndIf
							e\EventState = e\EventState+FPSfactor/2
							e\room\NPC[0]\State = 6
							e\room\NPC[0]\CurrSpeed = CurveValue(0.0, e\room\NPC[0]\CurrSpeed, 25.0)
							PositionEntity(e\room\NPC[0]\Collider, CurveValue(EntityX(e\room\obj, True), EntityX(e\room\NPC[0]\Collider), 25.0), 0.3-e\EventState/70, CurveValue(EntityZ(e\room\obj, True), EntityZ(e\room\NPC[0]\Collider), 25.0))
							ResetEntity(e\room\NPC[0]\Collider)
							
							;TurnEntity(e\room\NPC[0]\Collider,0,0,0.5*FPSfactor)
							AnimateNPC(e\room\NPC[0], 41, 58, 0.1, False)
							
							AnimateNPC(Curr106, 206,112, -1.0, False)
						Else
							AnimateNPC(Curr106, 112,206, 1.5, False)
						EndIf
						CurrSpeed = Min(CurrSpeed - (CurrSpeed * (0.15/EntityDistance(e\room\NPC[0]\Collider, Collider)) * FPSfactor), CurrSpeed)
						If e\EventState > 100 Then
							;PlaySound2(OldManSFX(Rand(1,2)), Camera, e\room\NPC[0]\Collider)
							
							PositionEntity(Curr106\obj, EntityX(Curr106\Collider), -100.0, EntityZ(Curr106\Collider), True)
							PositionEntity(Curr106\Collider, EntityX(Curr106\Collider), -100.0, EntityZ(Curr106\Collider), True)
							
							Curr106\Idle = False
							If EntityDistance(Collider, e\room\obj)<2.5 Then Curr106\State=-0.1
							
							RemoveNPC(e\room\NPC[0])
							
							RemoveEvent(e)
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "gateaentrance"
				;[Block]
				If PlayerRoom = e\room Then 
					If RemoteDoorOn=False Then
						e\room\RoomDoors[1]\locked=True
					ElseIf RemoteDoorOn And e\EventState3=0
						e\room\RoomDoors[1]\locked=False
						If e\room\RoomDoors[1]\open Then 
							If e\room\RoomDoors[1]\openstate > 50 Or EntityDistance(Collider, e\room\RoomDoors[1]\frameobj)<0.5 Then
								e\room\RoomDoors[1]\openstate = Min(e\room\RoomDoors[1]\openstate,50)
								e\room\RoomDoors[1]\open = False
								PlaySound2 (LoadTempSound("SFX\Door\DoorError.ogg"), Camera, e\room\RoomDoors[1]\frameobj)
							EndIf							
						EndIf
					Else
						e\room\RoomDoors[1]\locked=False
						Local gatea.Rooms =Null
						For r.Rooms = Each Rooms
							If r\RoomTemplate\Name = "gatea" Then
								gatea = r 
								Exit
							EndIf
						Next
						
						If Curr096 <> Null Then
							If Curr096\State = 0 Or Curr096\State = 5 Then
								e\EventState = UpdateElevators(e\EventState, e\room\RoomDoors[0], gatea\RoomDoors[1], e\room\Objects[0], e\room\Objects[1], e)
							Else
								e\EventState = Update096ElevatorEvent(e,e\EventState,e\room\RoomDoors[0],e\room\Objects[0])
							EndIf
						Else
							e\EventState = UpdateElevators(e\EventState, e\room\RoomDoors[0], gatea\RoomDoors[1], e\room\Objects[0], e\room\Objects[1], e)
						EndIf
						If Contained106 = False Then 
							If e\EventState < -1.5 And e\EventState+FPSfactor=> -1.5 Then
								PlaySound_Strict(OldManSFX(3))
							EndIf
						EndIf
						
						If EntityDistance(Collider, e\room\Objects[1])<4.0 Then
							gatea\RoomDoors[1]\locked = True
							PlayerRoom = gatea
							RemoveEvent(e)
						EndIf						
					EndIf
				EndIf
				;[End Block]
			Case "lockroom173"
				;[Block]
				If e\room\dist < 6.0  And e\room\dist > 0 Then
					If Curr173\Idle > 1 Then
						RemoveEvent(e)
					Else
						If (Not EntityInView(Curr173\Collider, Camera)) Or EntityDistance(Curr173\Collider, Collider)>15.0 Then 
							PositionEntity(Curr173\Collider, e\room\x + Cos(225-90 + e\room\angle) * 2, 0.6, e\room\z + Sin(225-90 + e\room\angle) * 2)
							ResetEntity(Curr173\Collider)
							RemoveEvent(e)
						EndIf						
					EndIf
				EndIf
				;[End Block]
			Case "lockroom096"
				;[Block]
				If PlayerRoom = e\room Then
					If Curr096=Null Then
						Curr096 = CreateNPC(NPCtype096, EntityX(e\room\obj,True), 0.3, EntityZ(e\room\obj,True))
						RotateEntity Curr096\Collider, 0, e\room\angle+45, 0, True
					EndIf
					RemoveEvent(e)
				End If
				;[End Block]
			Case "pj"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState = 0 Then
						If EntityDistance(Collider, e\room\obj) < 2.5 Then
							PlaySound_Strict(RustleSFX(Rand(0,2)))
							CreateNPC(NPCtype372, 0, 0, 0)
							e\EventState = 1
							RemoveEvent(e)
						EndIf					
					EndIf
				EndIf
				;[End Block]
			Case "pocketdimension"
				;[Block]
				
				;eventstate: a timer for scaling the tunnels in the starting room
				;eventstate2:
					;0 if the player is in the starting room
					;1 if in the room with the throne, moving pillars, plane etc
					;12-15 if player is in the room with the tall pillars 
						;(goes down from 15 to 12 And 106 teleports from pillar to another, pillars being room\objects[12 to 15])
				;eventstate3:
					;1 when appearing in the tunnel that looks like the tunnels in hcz
					;2 after opening the door in the tunnel
					;otherwise 0
				
				If PlayerRoom = e\room Then
					ShowEntity e\room\obj
					
					PlayerFallingPickDistance = 0.0
					
					Injuries = Injuries+FPSfactor*0.00005
					PrevSecondaryLightOn = SecondaryLightOn : SecondaryLightOn = True
					
					If (EntityY(Collider)<2000*RoomScale Or EntityY(Collider)>2608*RoomScale) Then CurrStepSFX = 1
					
					If e\Sound = 0 Then LoadEventSound(e,"SFX\Room\PocketDimension\Rumble.ogg")
					If e\Sound2 = 0 Then e\Sound2 = LoadEventSound(e,"SFX\Room\PocketDimension\PrisonVoices.ogg",1)
					
					If e\EventState = 0 Then
						CameraFogColor Camera, 0,0,0
						CameraClsColor Camera, 0,0,0
						e\EventState = 0.1
					EndIf
					
					If EntityY(Collider)<2000*RoomScale Or e\EventState3=0 Or EntityY(Collider)>2608*RoomScale Then 
						ShouldPlay = 3
					Else 
						ShouldPlay = 0
					EndIf
					
					ScaleEntity(e\room\obj,RoomScale, RoomScale*(1.0 + Sin(e\EventState/14.0)*0.2), RoomScale)
					For i = 0 To 7
						ScaleEntity(e\room\Objects[i],RoomScale*(1.0 + Abs(Sin(e\EventState/21.0+i*45.0)*0.1)),RoomScale*(1.0 + Sin(e\EventState/14.0+i*20.0)*0.1), RoomScale,True)
					Next
					ScaleEntity(e\room\Objects[9],RoomScale*(1.5 + Abs(Sin(e\EventState/21.0+i*45.0)*0.1)),RoomScale*(1.0 + Sin(e\EventState/14.0+i*20.0)*0.1), RoomScale,True)
					
					e\EventState = e\EventState + FPSfactor
					
					If e\EventState2 = 0 Then 
						e\room\RoomDoors[0]\open = False
						e\room\RoomDoors[1]\open = False
						
						If Curr106\State > 0 ;106 circles around the starting room
							angle = (e\EventState/10 Mod 360)
							PositionEntity(Curr106\Collider, EntityX(e\room\obj), 0.2+0.35+Sin(e\EventState/14.0+i*20.0)*0.4, EntityX(e\room\obj))
							RotateEntity(Curr106\Collider, 0,angle,0)
							MoveEntity(Curr106\Collider,0,0,6.0-Sin(e\EventState/10.0))
							AnimateNPC(Curr106, 55, 104, 0.5)
							RotateEntity(Curr106\Collider, 0,angle+90,0)
							Curr106\Idle = True
							ShowEntity Curr106\obj
							ShowEntity Curr106\Collider
							ResetEntity Curr106\Collider
							Curr106\GravityMult = 0.0
							Curr106\DropSpeed = 0
							PositionEntity(Curr106\obj, EntityX(Curr106\Collider), EntityY(Curr106\Collider) - 0.15, EntityZ(Curr106\Collider))
							RotateEntity Curr106\obj, 0, EntityYaw(Curr106\Collider), 0
							
							If e\EventState > 65*70 Then
								If Rand(800)=1 Then	
									PlaySound_Strict HorrorSFX(8)
									Curr106\State = -0.1
									Curr106\Idle = False
									e\EventState = 601
								EndIf
							EndIf
						EndIf
					EndIf 
					
					If EntityDistance(Collider, Curr106\Collider) < 0.3 Then ;106 attacks if close enough to player
						Curr106\Idle = False
						Curr106\State = -10
					EndIf
					
					If e\EventState2 = 1 Then ;in the second room
						
						PositionEntity(e\room\Objects[9], EntityX(e\room\Objects[8],True)+3384*RoomScale, 0.0, EntityZ(e\room\Objects[8],True))
						
						TranslateEntity e\room\Objects[9], Cos(e\EventState*0.8)*5, 0, Sin(e\EventState*1.6)*4, True
						RotateEntity e\room\Objects[9],0,e\EventState * 2,0
						
						PositionEntity(e\room\Objects[10], EntityX(e\room\Objects[8],True), 0.0, EntityZ(e\room\Objects[8],True)+3384*RoomScale)
						
						TranslateEntity e\room\Objects[10], Sin(e\EventState*1.6)*4, 0, Cos(e\EventState*0.8)*5, True
						RotateEntity e\room\Objects[10],0,e\EventState * 2,0
						
						If e\EventState3 = 1 Or e\EventState3 = 2 Then ;the "trick room"
							If e\EventState3 = 1 And (e\room\RoomDoors[0]\openstate>150 Or e\room\RoomDoors[1]\openstate>150) Then
								PlaySound_Strict LoadTempSound("SFX\Horror\Horror16.ogg")
								BlurTimer = 800
								e\EventState3=2
							EndIf
							
							If EntityY(Collider)<5.0 Then e\EventState3 = 0
						Else
							;the trenches
							If EntityY(Collider)>6.0 Then
								ShouldPlay = 15
								
								CameraFogColor Camera, 38, 55, 47
								CameraClsColor Camera, 38, 55, 47
								
								If EntityX(e\room\Objects[20],True)<EntityX(e\room\Objects[8],True)-4000*RoomScale Then
									e\SoundCHN2 = PlaySound_Strict(e\Sound2)
									
									PositionEntity e\room\Objects[20], EntityX(Collider,True)+4000*RoomScale, 12.0, EntityZ(Collider,True)
								EndIf
								
								MoveEntity(Collider, 0, Min((12.0 - EntityY(Collider)),0.0)*FPSfactor, 0)
								
								x = -FPSfactor*RoomScale*4.0
								y = (17.0-Abs(EntityX(Collider)-EntityX(e\room\Objects[20]))*0.5)-EntityY(e\room\Objects[20])
								z = EntityZ(Collider,True)-EntityZ(e\room\Objects[20])
								TranslateEntity e\room\Objects[20], x, y, z,True
								RotateEntity e\room\Objects[20], -90-(EntityX(Collider)-EntityX(e\room\Objects[20]))*1.5, -90.0, 0.0, True
								
								
								;check if the plane can see the player
								Local safe=False
								For i = 0 To 2
									Select i
										Case 0
											x = -1452*RoomScale
											z = -37*RoomScale
										Case 1
											x = -121*RoomScale
											z = 188*RoomScale
										Case 2
											x = 1223*RoomScale
											z = -196*RoomScale							
									End Select
									
									x = x + EntityX(e\room\Objects[8],True)
									z = z + EntityZ(e\room\Objects[8],True)
									
									If Distance(EntityX(Collider), EntityZ(Collider), x, z) < 200*RoomScale Then safe = True : Exit
								Next
								
								dist = EntityDistance(Collider, e\room\Objects[20])
								
								If e\SoundCHN2<>0 And ChannelPlaying(e\SoundCHN2)
									e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, Camera, 10.0, 0.3+(Not safe)*0.6)
								EndIf	
								
								If safe Then
									EntityTexture e\room\Objects[20], e\room\Objects[18]
								ElseIf dist < 8.0
									e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[20], 8.0)
									EntityTexture e\room\Objects[20], e\room\Objects[19]
									Injuries=Injuries+(8.0-dist)*FPSfactor*0.0003
									
									If dist<7.0 Then 
										pvt% = CreatePivot()
										PositionEntity pvt, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
										PointEntity(pvt, e\room\Objects[20])
										TurnEntity(pvt, 90, 0, 0)
										user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, 10.0)
										user_camera_pitch=user_camera_pitch-90
										RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 10), 0)
										FreeEntity pvt
									EndIf
								EndIf
								
								CameraShake = Max(4.0+((Not safe) * 4.0) - dist, 0.0)
								
								;check if player is at the sinkhole (the exit from the trench room)
								If EntityY(Collider)<8.5 Then
									LoadEventSound(e,"SFX\Room\PocketDimension\Rumble.ogg")
									LoadEventSound(e,"SFX\Room\PocketDimension\PrisonVoices.ogg",1)
									
									;move to the "exit room"
									BlurTimer = 1500
									e\EventState2=1
									BlinkTimer = -10
									
									PositionEntity(Collider, EntityX(e\room\Objects[8],True)-400*RoomScale, -304*RoomScale, EntityZ(e\room\Objects[8],True))
									ResetEntity Collider
									
									CameraFogColor Camera, 0,0,0
									CameraClsColor Camera, 0,0,0
								EndIf
								
							Else
								e\EventState3 = 0
								
								For i = 9 To 10
									dist = Distance(EntityX(Collider), EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True))
									If dist<6.0 Then 
										If dist<100.0*RoomScale Then
											pvt=CreatePivot()
											PositionEntity pvt, EntityX(e\room\Objects[i],True),EntityY(Collider),EntityZ(e\room\Objects[i],True)
											
											PointEntity pvt, Collider
											RotateEntity pvt, 0, Int(EntityYaw(pvt)/90)*90,0,True
											MoveEntity pvt, 0,0,100*RoomScale
											PositionEntity Collider, EntityX(pvt),EntityY(Collider),EntityZ(pvt)
											
											FreeEntity pvt
											
											If KillTimer = 0 Then
												DeathMSG = "In addition to the decomposed appearance typical of SCP-106's victims, the body exhibits injuries that have not been observed before: "
												DeathMSG = DeathMSG + "massive skull fracture, three broken ribs, fractured shoulder and multiple heavy lacerations."
												
												PlaySound_Strict LoadTempSound("SFX\Room\PocketDimension\Impact.ogg")
												KillTimer=-1.0
											EndIf
										EndIf
										If Float(e\EventStr) < 1000.0 Then
											e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[i], 6.0)
										EndIf
									EndIf
								Next
								
								pvt=CreatePivot()
								PositionEntity pvt, EntityX(e\room\Objects[8],True)-1536*RoomScale,500*RoomScale,EntityZ(e\room\Objects[8],True)+608*RoomScale
								If EntityDistance(pvt, Collider)<5.0 Then 
									e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, pvt, 3.0)
								EndIf
								FreeEntity pvt
								
								;106's eyes
								ShowEntity e\room\Objects[17]
								PositionEntity e\room\Objects[17], EntityX(e\room\Objects[8],True),1376*RoomScale,EntityZ(e\room\Objects[8],True)-2848*RoomScale
								PointEntity e\room\Objects[17], Collider
								TurnEntity e\room\Objects[17], 0, 180, 0
								
								temp = EntityDistance(Collider, e\room\Objects[17])
								If temp < 2000*RoomScale Then
									Injuries = Injuries + (FPSfactor/4000)
									e\EventStr = Float(e\EventStr)+(FPSfactor/1000.0)
									
									;If Injuries > 1.0 Then
									If Float(e\EventStr) > 1.0 And Float(e\EventStr) < 1000.0 Then
										PlaySound_Strict LoadTempSound("SFX\Room\PocketDimension\Kneel.ogg")
										LoadEventSound(e,"SFX\Room\PocketDimension\Screech.ogg")
										e\EventStr = Float(1000.0)
										DebugLog "Loaded screech sound"
									EndIf
									
									Sanity = Max(Sanity - FPSfactor / temp / 8,-1000)
									
									;e\SoundCHN = LoopSound2(OldManSFX(4), e\SoundCHN, Camera, e\room\Objects[17], 5.0, 0.6)
									
									CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2()) / 20.0)+1.0)*15.0*Max((6.0-temp)/6.0,0.0))
									
									pvt% = CreatePivot()
									PositionEntity pvt, EntityX(Camera), EntityY(Camera), EntityZ(Camera)
									PointEntity(pvt, e\room\Objects[17])
									TurnEntity(pvt, 90, 0, 0)
									user_camera_pitch = CurveAngle(EntityPitch(pvt), user_camera_pitch + 90.0, Min(Max(15000.0 / (-Sanity), 15.0), 500.0))
									user_camera_pitch=user_camera_pitch-90
									RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), Min(Max(15000.0 / (-Sanity), 15.0), 500.0)), 0)
									FreeEntity pvt
									
									;teleport the player to the trenches
									If Crouch Then
										BlinkTimer = -10
										PositionEntity Collider, EntityX(e\room\Objects[8],True)-1344*RoomScale,2944*RoomScale,EntityZ(e\room\Objects[8],True)-1184*RoomScale
										ResetEntity Collider
										Crouch = False
										
										LoadEventSound(e,"SFX\Room\PocketDimension\Explosion.ogg")
										LoadEventSound(e,"SFX\Room\PocketDimension\TrenchPlane.ogg",1)
										PositionEntity e\room\Objects[20], EntityX(e\room\Objects[8],True)-1000,0,0,True
										
										e\EventStr = Float(0)
									EndIf
								ElseIf EntityY(Collider)<-180*RoomScale ;the "exit room"
									temp = Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[8],True)+1024*RoomScale,EntityZ(e\room\Objects[8],True))
									If temp<640*RoomScale
										BlurTimer = (640*RoomScale-temp)*3000
										
										e\SoundCHN2 = LoopSound2(DecaySFX(Rand(1, 3)), e\SoundCHN2, Camera, Collider, 2.0, (640*RoomScale-temp)*Abs(CurrSpeed)*100)
										CurrSpeed = CurveValue(0.0, CurrSpeed, temp*10)
										
										If temp < 130*RoomScale Then
											
											For r.Rooms = Each Rooms
												If r\RoomTemplate\Name = "room2shaft" Then
													GiveAchievement(AchvPD)
													e\EventState = 0
													e\EventState2 = 0
													
													SecondaryLightOn = PrevSecondaryLightOn
													PrevSecondaryLightOn = 0.0
													
													BlinkTimer = -10
													LightBlink = 5
													
													BlurTimer = 1500
													
													PlayerRoom = r
													
													PlaySound_Strict(LoadTempSound("SFX\Room\PocketDimension\Exit.ogg"))
													
													TeleportEntity(Collider,EntityX(r\Objects[0],True),0.4,EntityZ(r\Objects[0],True),0.3,True)
													
													UpdateRooms()
													UpdateDoors()
													Curr106\State = 10000
													Curr106\Idle = False
													
													de.decals = CreateDecal(0, EntityX(r\Objects[0],True),EntityY(r\Objects[0],True),EntityZ(r\Objects[0],True), 270, Rand(360), 0)
													TeleportEntity(de\obj,EntityX(r\Objects[0],True),EntityY(r\Objects[0],True)+0.6,EntityZ(r\Objects[0],True),0.0,True,4,1)
													
													For e2.Events = Each Events
														If e2\EventName = "room2sl"
															e2\EventState3 = 0
															UpdateLever(e2\room\Levers[0])
															RotateEntity e2\room\Levers[0],0,EntityYaw(e2\room\Levers[0]),0
															TurnCheckpointMonitorsOff(0)
															Exit
														EndIf
													Next
													Exit
													Return
												EndIf
											Next
										EndIf
									EndIf
								EndIf
							EndIf	
						EndIf
						
						
						
						If EntityY(Collider) < -1600*RoomScale Then
							If EntityDistance(Collider, e\room\Objects[8]) > 4750*RoomScale Then
								CameraFogColor Camera, 0,0,0
								CameraClsColor Camera, 0,0,0
								
								DropSpeed = 0
								BlurTimer = 500
								BlurTimer = 1500
								PositionEntity(Collider, EntityX(e\room\obj,True), 0.4, EntityX(e\room\obj,True))
								For r.Rooms = Each Rooms
									If r\RoomTemplate\Name = "room106" Then
										e\EventState = 0
										e\EventState2 = 0
										
										TeleportEntity(Collider,EntityX(r\Objects[10],True),0.4,EntityZ(r\Objects[10],True),0.3,True)
										
										GiveAchievement(AchvPD)
										SecondaryLightOn = PrevSecondaryLightOn
										PrevSecondaryLightOn = 0.0
										
										Curr106\State = 10000
										Curr106\Idle = False
										
										For e2.Events = Each Events
											If e2\EventName = "room2sl"
												e2\EventState3 = 0
												UpdateLever(e2\room\Levers[0])
												RotateEntity e2\room\Levers[0],0,EntityYaw(e2\room\Levers[0]),0
												TurnCheckpointMonitorsOff(0)
												Exit
											EndIf
										Next
										Exit
										Return
									EndIf
								Next
								ResetEntity Collider
								
								e\EventState2 = 0
								UpdateDoorsTimer = 0
								UpdateDoors()
								UpdateRooms()
							Else ;the player is not at the exit, must've fallen down
								
								If KillTimer => 0 Then 
									PlaySound_Strict HorrorSFX(8)
									DeathMSG = "In addition to the decomposed appearance typical of the victims of SCP-106, the subject seems to have suffered multiple heavy fractures to both of his legs."
									
								EndIf
								KillTimer = Min(-1, KillTimer)	
								BlurTimer = 3000
							EndIf
						EndIf
						
						UpdateDoorsTimer = 0
						UpdateDoors()
						UpdateRooms()
						
					ElseIf e\EventState2 = 0
						dist# = EntityDistance(Collider, e\room\obj)	
						
						If dist > 1700*RoomScale Then
							BlinkTimer = -10
							
							Select Rand(25)
								Case 1,2,3,4
									PlaySound_Strict(OldManSFX(3))
									
									pvt = CreatePivot()
									PositionEntity(pvt, EntityX(Collider), EntityY(Collider), EntityZ(Collider))
									
									PointEntity(pvt, e\room\obj)
									MoveEntity pvt, 0,0,dist*1.9
									PositionEntity(Collider, EntityX(pvt), EntityY(Collider), EntityZ(pvt))
									ResetEntity Collider
									
									MoveEntity pvt, 0,0,0.8
									PositionEntity(e\room\Objects[10], EntityX(pvt), 0.0, EntityZ(pvt))
									RotateEntity e\room\Objects[10], 0, EntityYaw(pvt), 0, True	
									
									FreeEntity pvt
								Case 5,6,7,8,9,10 
									e\EventState2=1
									BlinkTimer = -10
									PlaySound_Strict(OldManSFX(3))
									
									PositionEntity(Collider, EntityX(e\room\Objects[8],True), 0.5, EntityZ(e\room\Objects[8],True))
									ResetEntity Collider
								Case 11,12 ;middle of the large starting room
									BlurTimer = 500
									PositionEntity Collider,EntityX(e\room\obj), 0.5, EntityZ(e\room\obj)
								Case 13,14,15 ;"exit room"
									BlurTimer = 1500
									e\EventState2=1
									BlinkTimer = -10
									
									PositionEntity(Collider, EntityX(e\room\Objects[8],True)-400*RoomScale, -304*RoomScale, EntityZ(e\room\Objects[8],True))
									ResetEntity Collider
								Case 16,17,18,19
									BlurTimer = 1500
									For r.Rooms = Each Rooms
										If r\RoomTemplate\Name = "tunnel" Then
											GiveAchievement(AchvPD)
											e\EventState = 0
											e\EventState2 = 0
											
											SecondaryLightOn = PrevSecondaryLightOn
											PrevSecondaryLightOn = 0.0
											TeleportEntity(Collider,EntityX(r\obj,True),0.4,EntityZ(r\obj,True),0.3,True)
											Curr106\State = 250
											Curr106\Idle = False
											
											For e2.Events = Each Events
												If e2\EventName = "room2sl"
													e2\EventState3 = 0
													UpdateLever(e2\room\Levers[0])
													RotateEntity e2\room\Levers[0],0,EntityYaw(e2\room\Levers[0]),0
													TurnCheckpointMonitorsOff(0)
													Exit
												EndIf
											Next
											Exit
											Return
										EndIf
									Next
								Case 20,21,22 ;the tower room
									BlinkTimer = -10
									PositionEntity(Collider, EntityX(e\room\Objects[12],True), 0.6, EntityZ(e\room\Objects[12],True))
									ResetEntity Collider
									e\EventState2 = 15
								Case 23,24,25
									BlurTimer = 1500
									e\EventState2=1
									e\EventState3=1
									BlinkTimer = -10
									
									PlaySound_Strict(OldManSFX(3))
									
									PositionEntity(Collider, EntityX(e\room\Objects[8],True), 2288*RoomScale, EntityZ(e\room\Objects[8],True))
									ResetEntity Collider
							End Select 
							
							UpdateDoorsTimer = 0
							UpdateDoors()
							UpdateRooms()
						EndIf					
					Else ;pillar room
						CameraFogColor Camera, 38*0.5, 55*0.5, 47*0.5
						CameraClsColor Camera, 38*0.5, 55*0.5, 47*0.5
						
						If ParticleAmount > 0
							If Rand(800)=1 Then 
								angle = EntityYaw(Camera,True)+Rnd(150,210)
								p.Particles = CreateParticle(EntityX(Collider)+Cos(angle)*7.5, 0.0, EntityZ(Collider)+Sin(angle)*7.5, 3, 4.0, 0.0, 2500)
								EntityBlend(p\obj, 2)
								;EntityFX(p\obj, 1)
								p\speed = 0.01
								p\SizeChange = 0
								PointEntity(p\pvt, Camera)
								TurnEntity(p\pvt, 0, 145, 0, True)
								TurnEntity(p\pvt, Rand(10,20), 0, 0, True)
							EndIf
						EndIf
						
						If e\EventState2 > 12 Then 
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[e\EventState2],True),0.27, EntityZ(e\room\Objects[e\EventState2],True))
							
							PointEntity(Curr106\Collider, Camera)
							TurnEntity(Curr106\Collider, 0, Sin(MilliSecs2() / 20) * 6.0, 0, True)
							MoveEntity(Curr106\Collider, 0, 0, Sin(MilliSecs2() / 15) * 0.06)
							
							ShowEntity Curr106\obj
							ShowEntity Curr106\Collider
							ResetEntity Curr106\Collider
							Curr106\GravityMult = 0.0
							Curr106\DropSpeed = 0
							PositionEntity(Curr106\obj, EntityX(Curr106\Collider), EntityY(Curr106\Collider) - 0.15, EntityZ(Curr106\Collider))
							RotateEntity Curr106\obj, 0, EntityYaw(Curr106\Collider), 0
							
							If Rand(750)=1 And e\EventState2 > 12 Then
								BlinkTimer = -10
								e\EventState2 = e\EventState2-1
								PlaySound_Strict HorrorSFX(8)
							EndIf
							
							If e\EventState2 = 12 Then
								CameraShake = 1.0
								PositionEntity(Curr106\Collider, EntityX(e\room\Objects[e\EventState2],True),-1.0, EntityZ(e\room\Objects[e\EventState2],True))
								Curr106\State = -10
								ResetEntity Curr106\Collider
							EndIf
							
						Else 
							Curr106\State = -10
							Curr106\Idle = False
						EndIf
						
						If EntityY(Collider) < -1600*RoomScale Then
							;player is at the exit
							If Distance(EntityX(e\room\Objects[16],True),EntityZ(e\room\Objects[16],True),EntityX(Collider),EntityZ(Collider))<144*RoomScale Then
								
								CameraFogColor Camera, 0,0,0
								CameraClsColor Camera, 0,0,0
								
								DropSpeed = 0
								BlurTimer = 500
								PositionEntity(Collider, EntityX(e\room\obj), 0.5, EntityZ(e\room\obj))
								ResetEntity Collider
								e\EventState2 = 0
								UpdateDoorsTimer = 0
								UpdateDoors()
								UpdateRooms()
							Else ;somewhere else -> must've fallen down
								If KillTimer => 0 Then PlaySound_Strict HorrorSFX(8)
								KillTimer = Min(-1, KillTimer)	
								BlurTimer = 3000
							EndIf
						EndIf 
						
					EndIf
					
				Else
					HideEntity e\room\obj
					;CameraClsColor Camera, 0,0,0
					e\EventState = 0
					e\EventState2 = 0
					e\EventState3 = 0
					e\EventStr = Float(0)
				EndIf
				;[End Block]
			Case "room2cafeteria"
				;[Block]
				If PlayerRoom = e\room Then
					If Not Using294 Then
						If EntityDistance(e\room\Objects[0], Collider)<1.5 Then
							GiveAchievement(Achv294)
							If EntityInView(e\room\Objects[0], Camera) Then
								DrawHandIcon = True
								If MouseHit1 Then
									temp = True
									For it.Items = Each Items
										If it\Picked=False Then
											If EntityX(it\collider)-EntityX(e\room\Objects[1],True)=0 Then
												If EntityZ(it\collider)-EntityZ(e\room\Objects[1],True)=0 Then
													temp = False
													Exit
												EndIf
											EndIf
										EndIf
									Next
									Local inserted% = False
									If e\EventState2 < 2 Then
										If SelectedItem<>Null Then
											If SelectedItem\itemtemplate\tempname="25ct" Or SelectedItem\itemtemplate\tempname="coin" Then
												RemoveItem(SelectedItem)
												SelectedItem=Null
												e\EventState2 = e\EventState2 + 1
												PlaySound_Strict LoadTempSound("SFX\SCP\294\coin_drop.ogg")
												inserted = True
											EndIf
										EndIf
									EndIf
									If e\EventState2 = 2 Then
										Using294=temp
										If Using294 Then MouseHit1=False
									ElseIf e\EventState2 = 1 And (Not inserted) Then
										Using294=False
										Msg = "You need to insert another Quarter in order to use this machine."
										MsgTimer = 70*5
									ElseIf (Not inserted) Then
										Using294=False
										Msg = "You need to insert two Quarters in order to use this machine."
										MsgTimer = 70*5
									EndIf
								EndIf
							EndIf
						EndIf
					EndIf		
				EndIf
				
				If e\EventState = 0 Then
					CreateNPC(NPCtype066, EntityX(e\room\obj), 0.5, EntityZ(e\room\obj))
					e\EventState = 1
				EndIf
				;[End Block]
			Case "room2ccont"
				;[Block]
				If PlayerRoom = e\room Then
					
					EntityPick(Camera, 1.5)
					
					If PickedEntity() = e\room\Objects[3]
						If e\EventState = 0
							e\EventState = Max(e\EventState,1)
							PlaySound_Strict HorrorSFX(7)
							PlaySound_Strict LeverSFX
						EndIf 
					EndIf
					
					;Primary Lighting
					UpdateLever(e\room\Objects[1])
					
					;Secondary Lighting
					Local prevstate2 = e\EventState2
					e\EventState2 = UpdateLever(e\room\Objects[3])
					If (prevstate2 <> e\EventState2) And e\EventState>0 Then PlaySound2(LightSFX, Camera, e\room\Objects[3])
					If e\EventState2
						SecondaryLightOn = CurveValue(1.0, SecondaryLightOn, 10.0)
					Else
						SecondaryLightOn = CurveValue(0.0, SecondaryLightOn, 10.0)
					EndIf
					
					;Remote Door Control
					RemoteDoorOn = UpdateLever(e\room\Objects[5])
					
					If e\EventState > 0 And e\EventState < 200 Then
						e\EventState = e\EventState + FPSfactor
						RotateEntity(e\room\Objects[3], CurveValue(-85, EntityPitch(e\room\Objects[3]), 5), EntityYaw(e\room\Objects[3]), 0)
					EndIf 
					
				EndIf
				;[End Block]
			Case "room2closets"
				;[Block]
				If e\EventState = 0 Then
					If PlayerRoom = e\room And Curr173\Idle<2 Then
						If e\EventStr = "" And QuickLoadPercent = -1
							QuickLoadPercent = 0
							QuickLoad_CurrEvent = e
							e\EventStr = "load0"
						EndIf
					EndIf
				Else
					e\EventState=e\EventState+FPSfactor
					If e\EventState < 70*3.5 Then
						RotateEntity(e\room\NPC[1]\Collider,0,CurveAngle(e\room\angle+90,EntityYaw(e\room\NPC[1]\Collider),100.0),0,True)
						
						e\room\NPC[0]\State=1
						If e\EventState > 70*3.2 And e\EventState-FPSfactor =< 70*3.2 Then PlaySound2(IntroSFX(15),Camera,e\room\obj,15.0)
					ElseIf e\EventState < 70*6.5
						If e\EventState-FPSfactor < 70*3.5 Then
							e\room\NPC[0]\State=0
							e\room\NPC[1]\SoundChn = PlaySound2(e\room\NPC[1]\Sound, Camera, e\room\NPC[1]\Collider,12.0)
						EndIf
						
						If e\EventState > 70*4.5 Then
							PointEntity e\room\NPC[0]\obj, e\room\obj
							RotateEntity(e\room\NPC[0]\Collider,0,CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30.0),0,True)
						EndIf
						PointEntity e\room\NPC[1]\obj, e\room\obj
						TurnEntity e\room\NPC[1]\obj, 0, Sin(e\EventState)*25, 0
						RotateEntity(e\room\NPC[1]\Collider,0,CurveAngle(EntityYaw(e\room\NPC[1]\obj),EntityYaw(e\room\NPC[1]\Collider),30.0),0,True)
					Else
						If e\EventState-FPSfactor < 70*6.5 Then 
							PlaySound_Strict (HorrorSFX(0))
							PlaySound_Strict (LightSFX)
						EndIf
						BlinkTimer = Max((70*6.5-e\EventState)/5.0 - Rnd(0.0,2.0),-10)
						If BlinkTimer =-10 Then
							If e\EventState > 70*7.5 And e\EventState-FPSfactor =< 70*7.5 Then
								PlaySound2(NeckSnapSFX(0),Camera,e\room\NPC[0]\Collider,8.0)
								;Wallet spawning (with 3 coins)
								it.Items = CreateItem("Wallet","wallet",EntityX(e\room\NPC[0]\Collider,True),EntityY(e\room\NPC[0]\Collider,True),EntityZ(e\room\NPC[0]\Collider,True))
								EntityType(it\collider, HIT_ITEM)
								PointEntity it\collider,e\room\NPC[1]\Collider
								MoveEntity it\collider,-0.4,0,-0.2
								TeleportEntity(it\collider,EntityX(it\collider),EntityY(it\collider),EntityZ(it\collider),-0.02,True,10)
								For i = 0 To 1
									it2.Items = CreateItem("Quarter","25ct",1,1,1)
									it2\Picked = True
									it2\Dropped = -1
									it2\itemtemplate\found=True
									it\SecondInv[i] = it2
									HideEntity(it2\collider)
									EntityType(it2\collider, HIT_ITEM)
								Next
							EndIf
							If e\EventState > 70*8.0 And e\EventState-FPSfactor =< 70*8.0 Then
								PlaySound2(NeckSnapSFX(1),Camera,e\room\NPC[1]\Collider,8.0)
							EndIf
							SetNPCFrame e\room\NPC[0], 60
							e\room\NPC[0]\State=8
							
							SetNPCFrame e\room\NPC[1], 19
							e\room\NPC[1]\State = 6
						EndIf
						
						If e\EventState > 70*8.5 Then
							PositionEntity Curr173\Collider, (EntityX(e\room\Objects[0],True)+EntityX(e\room\Objects[1],True))/2,EntityY(e\room\Objects[0],True),(EntityZ(e\room\Objects[0],True)+EntityZ(e\room\Objects[1],True))/2
							PointEntity Curr173\Collider, Collider
							ResetEntity Curr173\Collider
							RemoveEvent(e)
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room2doors173"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState = 0 And Curr173\Idle = 0 Then
						If (Not EntityInView(Curr173\obj, Camera)) Then
							e\EventState = 1
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[0], True), 0.5, EntityZ(e\room\Objects[0], True))
							ResetEntity(Curr173\Collider)
							RemoveEvent(e)
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room2elevator"
				;[Block]
				If e\EventState = 0 Then
					If e\room\dist < 8.0 And e\room\dist > 0 Then
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\obj,True), 0.5, EntityZ(e\room\obj,True))
						PointEntity e\room\NPC[0]\Collider, Collider
						RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider),0, True	
						
						e\EventState = 1
					EndIf
				Else
					If e\EventState = 1 Then
						If e\room\dist<5.0 Or Rand(700)=1 Then 
							e\EventState = 2
							
							e\room\NPC[0]\State = 5
							e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[1],True)
							e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[1],True)
							e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[1],True)
						EndIf
					ElseIf e\EventState = 2
						If EntityDistance(e\room\NPC[0]\Collider,e\room\Objects[1])<2.0 Then
							e\room\RoomDoors[0]\open = False
							PlaySound2(CloseDoorSFX(3, 0), Camera, e\room\RoomDoors[0]\obj, 8.0)			
							
							PlaySound_Strict (LoadTempSound("SFX\Room\Room2ElevatorDeath.ogg"))
							
							e\EventState = 2.05
						EndIf
					ElseIf e\EventState < 13*70
						e\EventState = e\EventState+FPSfactor
						;6.7 - 7.4
						;8.6 - 10
						If e\EventState > 6.7*70 And e\EventState < 7.4*70 Then
							CameraShake = 7.4-(e\EventState/70.0)
						ElseIf e\EventState > 8.6*70 And e\EventState < 10.6*70 
							CameraShake = 10.6-(e\EventState/70.0)
						ElseIf e\EventState > 12.6*70
							CameraShake = 0
							If e\EventState-FPSfactor < 12.6*70 And e\room\NPC[0]<>Null Then
								RemoveNPC(e\room\NPC[0])
								e\room\NPC[0]=Null
								
								de.Decals = CreateDecal(3, EntityX(e\room\Objects[0],True), 0.0005, EntityZ(e\room\Objects[0],True),90,Rnd(360),0)
								
								de.Decals = CreateDecal(17, EntityX(e\room\Objects[0],True), 0.002, EntityZ(e\room\Objects[0],True),90,Rnd(360),0)
								de\size = 0.5
								
								de.Decals = CreateDecal(3, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True), EntityZ(e\room\Objects[1],True),0,e\room\angle+270,0)
								de\size = 0.9
							EndIf
							e\room\RoomDoors[0]\locked = False
						EndIf
					Else
						If e\room\RoomDoors[0]\open Then e\room\RoomDoors[0]\locked = True : RemoveEvent(e)
					EndIf
				EndIf
				;[End Block]
			Case "room2elevator2"
				;[Block]
				If e\room\dist < 8.0 And e\room\dist > 0 Then
					
					de.Decals = CreateDecal(3, EntityX(e\room\Objects[0],True), 0.0005, EntityZ(e\room\Objects[0],True),90,Rnd(360),0)
					
					e\room\NPC[0]=CreateNPC(NPCtypeD, EntityX(e\room\Objects[0],True), 0.5, EntityZ(e\room\Objects[0],True))
					ChangeNPCTextureID(e\room\NPC[0],0)
					
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\obj)-80,0, True	
					
					SetNPCFrame e\room\NPC[0], 19
					e\room\NPC[0]\State=8
					
					RemoveEvent(e)
				EndIf
				;[End Block]
			Case "room2fan"
				;[Block]
				;eventstate1 = timer for turning the fan on/off
				;eventstate2 = fan on/off
				;eventstate3 = the speed of the fan
				If PlayerRoom = e\room Then
					TurnEntity (e\room\Objects[0], e\EventState3*FPSfactor, 0, 0)
					If e\EventState3 > 0.01 Then
						e\room\SoundCHN = LoopSound2 (RoomAmbience[9], e\room\SoundCHN, Camera, e\room\Objects[0], 5.0, (e\EventState3/4.0))
					EndIf
					e\EventState3 = CurveValue(e\EventState2*5, e\EventState3, 150.0)			
				EndIf
				
				If e\room\dist < 16.0 Then 
					If e\EventState < 0 Then
						e\EventState = Rand(15,30)*70
						temp = e\EventState2
						e\EventState2 = Rand(0,1)
						If PlayerRoom<>e\room Then
							e\EventState3 = e\EventState2*5
						Else
							If temp = 0 And e\EventState2 = 1.0 Then ;turn on the fan
								PlaySound2 (LoadTempSound("SFX\ambient\Room ambience\FanOn.ogg"), Camera, e\room\Objects[0], 8.0)
							ElseIf temp = 1 And e\EventState2 = 0.0 ;turn off the fan
								PlaySound2 (LoadTempSound("SFX\ambient\Room ambience\FanOff.ogg"), Camera, e\room\Objects[0], 8.0)
							EndIf
						EndIf
					Else
						e\EventState = e\EventState-FPSfactor
					EndIf					
				EndIf
				;[End Block]
			Case "room2nuke"
				;[Block]
				If PlayerRoom = e\room Then
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1], e\room\Objects[4], e\room\Objects[5], e)
					
					e\EventState = UpdateLever(e\room\Objects[1])
					UpdateLever(e\room\Objects[3])
				EndIf
				
				If e\EventState3 = 0 Then
					n.NPCs = CreateNPC(NPCtypeD,EntityX(e\room\Objects[6],True),0.5,EntityZ(e\room\Objects[6],True))
					RotateEntity n\Collider,0,e\room\angle+90,0
					n\State = 3
					SetNPCFrame(n,40)
					n\IsDead = True
					n\texture = "GFX\npcs\body2.jpg"
					tex = LoadTexture_Strict(n\texture)
					EntityTexture(n\obj, tex)
					FreeTexture tex
					e\EventState3 = 1
				EndIf
				;[End Block]
			Case "room2offices2"
				;[Block]
				If PlayerRoom = e\room Then
					If BlinkTimer<-8 And BlinkTimer >-12 Then
						temp = Rand(1,4)
						PositionEntity e\room\Objects[0], EntityX(e\room\Objects[temp],True),EntityY(e\room\Objects[temp],True),EntityZ(e\room\Objects[temp],True),True
						RotateEntity e\room\Objects[0], 0, Rnd(360), 0
					EndIf
				EndIf
				;[End Block]
			Case "room2offices3"
				;[Block]
				If PlayerRoom = e\room Then
					e\EventState = e\EventState+FPSfactor
					If e\EventState > 700 Then
						If EntityDistance(e\room\RoomDoors[0]\obj, Collider)>0.5 Then 
							If EntityInView(e\room\RoomDoors[0]\obj, Camera)=False Then
								DebugLog "%@@= \ {2E6C2=FD gi`h]c"
								e\room\RoomDoors[0]\open = False
								RemoveEvent(e)
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room2tesla"
				;[Block]
				temp = True
				If e\EventState2 > 70*3.5 And e\EventState2 < 70*90 Then temp = False
				
				If temp And EntityY(Collider, True) > EntityY(e\room\obj,True) And EntityY(Collider, True) < 4.0 Then
					
					If e\Sound = 0 Then e\Sound = LoadSound_Strict("SFX\Room\Tesla\Shock.ogg")
					
					If e\EventState = 0 Then
						If e\room\dist < 8 Then
							HideEntity e\room\Objects[3]
							If (MilliSecs2() Mod 1500) < 800 Then
								ShowEntity e\room\Objects[4]
							Else
								HideEntity e\room\Objects[4]
							EndIf			
							
							If e\SoundCHN = 0 Then ;humming when the player isn't close
								e\SoundCHN = PlaySound2(TeslaIdleSFX, Camera, e\room\Objects[3],4.0,0.5)
							Else
								If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound2(TeslaIdleSFX, Camera, e\room\Objects[3],4.0,0.5)
							EndIf
							
							For i = 0 To 2
								If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 300.0*RoomScale Then
									;play the activation sound
									If KillTimer => 0 Then 
										PlayerSoundVolume = Max(8.0,PlayerSoundVolume)
										StopChannel(e\SoundCHN)
										e\SoundCHN = PlaySound2(TeslaActivateSFX, Camera, e\room\Objects[3],4.0,0.5)
										e\EventState = 1
										Exit
									EndIf
								EndIf
							Next
							
							Local temp2 = True
							For e2.Events = Each Events
								If e2\EventName = e\EventName And e2 <> e
									If e2\EventStr <> ""
										temp2 = False
										e\EventStr = "done"
										Exit
									EndIf
								EndIf
							Next
							
							Local temp3 = 0
							If temp2
								If e\EventStr = "" And PlayerRoom = e\room
									If EntityDistance(e\room\Objects[5],Collider)<EntityDistance(e\room\Objects[6],Collider)
										temp3 = 6
									Else
										temp3 = 5
									EndIf
									
									e\room\NPC[0] = CreateNPC(NPCtypeClerk,EntityX(e\room\Objects[temp3],True),0.5,EntityZ(e\room\Objects[temp3],True))
									PointEntity e\room\NPC[0]\Collider,e\room\Objects[2]
									e\room\NPC[0]\State = 2
									e\EventStr = "step1"
									e\EventState = 0
									e\EventState2 = 0
									e\EventState3 = 0
								EndIf
							EndIf
						Else
							HideEntity e\room\Objects[4]
						EndIf
						
						If Curr106\State < -10 And e\EventState = 0 Then 
							For i = 0 To 2
								If Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 300.0*RoomScale Then
									;play the activation sound
									If KillTimer => 0 Then 
										StopChannel(e\SoundCHN)
										e\SoundCHN = PlaySound2(TeslaActivateSFX, Camera, e\room\Objects[3],4.0,0.5)
										HideEntity e\room\Objects[4]
										e\EventState = 1
										Curr106\State = 70 * 60 * Rand(10,13)
										GiveAchievement(AchvTesla)
										Exit
									EndIf
								EndIf
							Next
						EndIf
					Else
						e\EventState = e\EventState+FPSfactor
						If e\EventState =< 40 Then
							HideEntity e\room\Objects[3]
							If (MilliSecs2() Mod 100) < 50 Then
								ShowEntity e\room\Objects[4]
							Else
								HideEntity e\room\Objects[4]
							EndIf
						Else
							If e\room\dist < 2
							If e\EventState-FPSfactor =< 40 Then PlaySound_Strict(e\Sound)	
							Else
								If e\EventState-FPSfactor =< 40 Then PlaySound2(e\Sound,Camera,e\room\Objects[2])
							EndIf
							If e\EventState < 70 Then 
								
								If KillTimer => 0 Then 
									For i = 0 To 2
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 250.0*RoomScale Then
											ShowEntity Light
											LightFlash = 0.4
											CameraShake = 1.0
											Kill()
											DeathMSG = "Subject D-9341 killed by the Tesla gate at [REDACTED]."
										EndIf
									Next
								EndIf
								
								If e\EventStr = "step1"
									e\room\NPC[0]\State = 3
								EndIf
								
								If Curr106\State < -10 Then
									For i = 0 To 2
										If Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 250.0*RoomScale Then
											ShowEntity Light
											LightFlash = 0.3
											If ParticleAmount > 0
												For i = 0 To 5+(5*(ParticleAmount-1))
													p.Particles = CreateParticle(EntityX(Curr106\Collider, True), EntityY(Curr106\Collider, True), EntityZ(Curr106\Collider, True), 0, 0.015, -0.2, 250)
													p\size = 0.03
													p\gravity = -0.2
													p\lifetime = 200
													p\SizeChange = 0.005
													p\speed = 0.001
													RotateEntity(p\pvt, Rnd(360), Rnd(360), 0, True)
												Next
											EndIf
											Curr106\State = -20000
											TranslateEntity(Curr106\Collider,0,-50.0,0,True)
										EndIf
									Next								
								EndIf
								
								HideEntity e\room\Objects[3]
								HideEntity e\room\Objects[4]
								
								If Rand(5)<5 Then 
									PositionTexture TeslaTexture,0.0,Rnd(0,1.0)
									ShowEntity e\room\Objects[3]								
								EndIf
							Else 
								If e\EventState-FPSfactor < 70 Then 
									StopChannel(e\SoundCHN)	
									e\SoundCHN = PlaySound2(TeslaPowerUpSFX, Camera, e\room\Objects[3],4.0,0.5)
								EndIf 
								HideEntity e\room\Objects[3]
								
								If e\EventState > 150 Then e\EventState = 0
							EndIf
						EndIf
					EndIf
				Else
					HideEntity e\room\Objects[4]
				EndIf
				
				If e\room\NPC[0] <> Null
					If e\EventStr = "step1" And e\room\NPC[0]\State <> 3
						If e\EventState = 0
							For i = 0 To 2
								If Distance(EntityX(e\room\NPC[0]\Collider),EntityZ(e\room\NPC[0]\Collider),EntityX(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)) < 400.0*RoomScale
									StopChannel(e\SoundCHN)
									e\SoundCHN = PlaySound2(TeslaActivateSFX, Camera, e\room\Objects[3],4.0,0.5)
									HideEntity e\room\Objects[4]
									e\EventState = 1
									Exit
								EndIf
							Next
						EndIf
					ElseIf e\EventStr = "step1" And e\room\NPC[0]\State = 3
						e\room\NPC[0]\CurrSpeed = 0
						AnimateNPC(e\room\NPC[0],41,60,0.5,False)
						If e\room\NPC[0]\Frame = 60
							e\room\NPC[0]\IsDead = True
							e\EventStr = "step2"
							SetNPCFrame(e\room\NPC[0],57)
						EndIf
					ElseIf e\EventStr = "step2"
						AnimateNPC(e\room\NPC[0],57,60,0.5,False)
						If e\room\NPC[0]\Frame = 60
							e\EventStr = "0"
						EndIf
					ElseIf e\EventStr <> "" And e\EventStr <> "step1" And e\EventStr <> "done"
						If Float(e\EventStr) < 70*10 Then
							If ParticleAmount > 0 Then
								If Rand(20-(10*(ParticleAmount-1)))=1 Then
									;p.Particles = CreateParticle(EntityX(e\room\NPC[0]\Collider),EntityY(e\room\NPC[0]\obj)+0.05,EntityZ(e\room\NPC[0]\Collider),6,0.05,0,60)
									p.Particles = CreateParticle(EntityX(e\room\NPC[0]\Collider),EntityY(e\room\NPC[0]\obj)+0.05,EntityZ(e\room\NPC[0]\Collider),0,0.05,0,60)
									p\speed = 0.002
									RotateEntity(p\pvt, 0, EntityYaw(e\room\NPC[0]\Collider), 0)
									MoveEntity p\pvt,Rnd(-0.1,0.1),0,0.1+Rnd(0,0.5)
									RotateEntity(p\pvt, -90, EntityYaw(e\room\NPC[0]\Collider), 0)
									p\Achange = -0.02
								EndIf
							EndIf
							e\EventStr = Float(e\EventStr) + FPSfactor
						Else
							e\EventStr = "done"
						EndIf
					EndIf
				EndIf
				
				If PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "room860" Then
					If e\EventState2 = 0 Then
						If e\EventState3 <=0 Then 
							temp = False
							For n.NPCs = Each NPCs
								If n\NPCtype = NPCtypeMTF Then
									If Abs(EntityX(n\Collider)-EntityX(e\room\obj,True))<4.0 Then
										If Abs(EntityZ(n\Collider)-EntityZ(e\room\obj,True))<4.0 Then
											temp = True
											If e\EventState2 = 0 Then
												n\Sound = LoadSound_Strict("SFX\Character\MTF\Tesla0.ogg")
												PlayMTFSound(n\Sound,n)
												
												;LoadEventSound(e,"SFX\Character\MTF\Tesla1.ogg")
												;e\SoundCHN = PlaySound_Strict (e\Sound)
												;PlayAnnouncement("SFX\Character\MTF\Tesla"+Rand(1,3)+".ogg")
												n\Idle = 70*10
												e\EventState2 = 70*100
											EndIf
										EndIf
									EndIf
								EndIf
							Next
							If temp = False Then e\EventState2=70*3.5
							e\EventState3=e\EventState3+140
						Else
							e\EventState3=e\EventState3-FPSfactor
						EndIf
					Else
						If e\EventState2 => 70*92 And e\EventState2-FPSfactor < 70*92
							PlayAnnouncement("SFX\Character\MTF\Tesla"+Rand(1,3)+".ogg")
						EndIf
						
						e\EventState2 = Max(e\EventState2-FPSfactor,0)
					EndIf					
				EndIf
				
				;[End Block]
			Case "room2trick"
				;[Block]
				If PlayerRoom = e\room Then
					If EntityDistance(e\room\obj,Collider)<2.0 Then
						If EntityDistance(Collider, Curr173\obj)<6.0 Or EntityDistance(Collider, Curr106\obj)<6.0 Then
							RemoveEvent(e)
						Else
							DebugLog "%@@= \ {2E6C2=FD gi`h]c"
							
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(Collider),EntityY(Collider),EntityZ(Collider)
							PointEntity pvt, e\room\obj
							RotateEntity pvt, 0, EntityYaw(pvt),0,True
							MoveEntity pvt, 0,0,EntityDistance(pvt,e\room\obj)*2
							
							;CameraShake = 5.0
							;BlurTimer = 100
							
							BlinkTimer = -10
							
							PlaySound_Strict HorrorSFX(11);DripSFX(0)
							
							PositionEntity Collider, EntityX(pvt),EntityY(pvt)+0.05,EntityZ(pvt)
							UpdateWorld()
							;ResetEntity Collider
							
							TurnEntity Collider, 0,180,0
							
							FreeEntity pvt
							RemoveEvent(e)							
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room2tunnel"	
				;[Block]
				
				If EntityY(Collider,True)>=8.0 And EntityY(Collider,True)<=12.0 Then
					If (EntityX(Collider,True)>=e\room\x-6.0) And (EntityX(Collider,True)<=(e\room\x+2.0*gridsz+6.0)) Then
						If (EntityZ(Collider,True)>=e\room\z-6.0) And (EntityZ(Collider,True)<=(e\room\z+2.0*gridsz+6.0)) Then
							PlayerRoom=e\room
						EndIf
					EndIf
				EndIf
				
				If PlayerRoom = e\room Then
					
					;[Block]
					Local Meshes%[7]
					Local tempStr$
					
					Local ia%,ib%,ic%,id%
					Local dr.Doors
					
					Local tempInt%,tempInt2%
					Local ix%,iy%
					
					If I_Zone\HasCustomMT Then
						If e\room\grid\Meshes[0]=0 Then
							PlaceGrid_MapCreator(e\room)
						EndIf
					EndIf
					
					If e\room\grid = Null Then
						
						e\room\grid = New Grids
						
						oldSeed% = RndSeed()
						SeedRnd GenerateSeedNumber(RandomSeed)
						
						Local dir%
						
						dir=Rand(0,1) Shl 1
						;0 = right
						;1 = up
						;2 = left
						;3 = down
						
						ix=gridsz/2+Rand(-2,2)
						iy=gridsz/2+Rand(-2,2)
						
						e\room\grid\grid[ix+(iy*gridsz)]=1
						
						If dir=2 Then e\room\grid\grid[(ix+1)+(iy*gridsz)]=1 Else e\room\grid\grid[(ix-1)+(iy*gridsz)]=1
						
						Local count% = 2
						
						While count<100
							tempInt=Rand(1,5) Shl Rand(1,2)
							For i=1 To tempInt
								
								tempInt2=True
								
								Select dir
									Case 0
										If ix<gridsz-2-(i Mod 2) Then ix=ix+1 Else tempInt2=False
									Case 1
										If iy<gridsz-2-(i Mod 2) Then iy=iy+1 Else tempInt2=False
									Case 2
										If ix>1+(i Mod 2) Then ix=ix-1 Else tempInt2=False
									Case 3
										If iy>1+(i Mod 2) Then iy=iy-1 Else tempInt2=False
								End Select
								
								If tempInt2 Then
									If e\room\grid\grid[ix+(iy*gridsz)]=0 Then
										e\room\grid\grid[ix+(iy*gridsz)]=1
										count=count+1
									EndIf
								Else
									Exit
								EndIf
							Next
							dir=dir+((Rand(0,1) Shl 1)-1)
							While dir<0
								dir=dir+4
							Wend
							While dir>3
								dir=dir-4
							Wend
						Wend
						
						;generate the tunnels
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\grid[ix+(iy*gridsz)]>0 Then
									e\room\grid\grid[ix+(iy*gridsz)]=(e\room\grid\grid[(ix)+((iy+1)*gridsz)]>0)+(e\room\grid\grid[(ix)+((iy-1)*gridsz)]>0)+(e\room\grid\grid[(ix+1)+((iy)*gridsz)]>0)+(e\room\grid\grid[(ix-1)+((iy)*gridsz)]>0)
								EndIf
							Next
						Next
						
						Local maxX%=gridsz-1
						Local canRetry%=0
						
						For ix=0 To maxX
							For iy=0 To gridsz-1
								If e\room\grid\grid[ix+1+(iy*gridsz)]>0 Then
									maxX=ix
									If (e\room\grid\grid[ix+1+((iy+1)*gridsz)]<3) And (e\room\grid\grid[ix+1+((iy-1)*gridsz)]<3) Then
										canRetry=1
										If Rand(0,1)=1 Then
											e\room\grid\grid[ix+1+((iy)*gridsz)]=e\room\grid\grid[ix+1+((iy)*gridsz)]+1
											e\room\grid\grid[ix+((iy)*gridsz)]=7 ;generator room
											canRetry=0
											Exit
										EndIf
									EndIf
								EndIf
							Next
							If canRetry Then ix=ix-1
						Next
						
						Local firstX%,lastX%
						Local firstY%,lastY%
						
						firstX=-1
						lastY=-1
						firstX=-1
						lastY=-1
						
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\grid[ix+(iy*gridsz)]=2 Then
									If e\room\grid\grid[(ix+1)+((iy)*gridsz)]>0 And e\room\grid\grid[(ix-1)+((iy)*gridsz)]>0 Then ;horizontal
										If firstX=-1 Or firstY=-1 Then
											If e\room\grid\grid[ix-1+(iy*gridsz)]<3 And e\room\grid\grid[ix+1+(iy*gridsz)]<3 And e\room\grid\grid[ix+((iy-1)*gridsz)]<3 And e\room\grid\grid[ix+((iy+1)*gridsz)]<3 Then
												If e\room\grid\grid[ix-1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix-1+((iy+1)*gridsz)]<1 Then
													firstX=ix : firstY=iy
												EndIf
											EndIf
										EndIf
										If e\room\grid\grid[ix-1+(iy*gridsz)]<3 And e\room\grid\grid[ix+1+(iy*gridsz)]<3 And e\room\grid\grid[ix+((iy-1)*gridsz)]<3 And e\room\grid\grid[ix+((iy+1)*gridsz)]<3 Then
											If e\room\grid\grid[ix-1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix-1+((iy+1)*gridsz)]<1 Then
												lastX=ix : lastY=iy
											EndIf
										EndIf
									ElseIf e\room\grid\grid[(ix)+((iy+1)*gridsz)]>0 And e\room\grid\grid[(ix)+((iy-1)*gridsz)]>0 Then ;vertical
										If firstX=-1 Or firstY=-1 Then
											If e\room\grid\grid[ix-1+(iy*gridsz)]<3 And e\room\grid\grid[ix+1+(iy*gridsz)]<3 And e\room\grid\grid[ix+((iy-1)*gridsz)]<3 And e\room\grid\grid[ix+((iy+1)*gridsz)]<3 Then
												If e\room\grid\grid[ix-1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix-1+((iy+1)*gridsz)]<1 Then
													firstX=ix : firstY=iy
												EndIf
											EndIf
										EndIf
										If e\room\grid\grid[ix-1+(iy*gridsz)]<3 And e\room\grid\grid[ix+1+(iy*gridsz)]<3 And e\room\grid\grid[ix+((iy-1)*gridsz)]<3 And e\room\grid\grid[ix+((iy+1)*gridsz)]<3 Then
											If e\room\grid\grid[ix-1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix+1+((iy-1)*gridsz)]<1 And e\room\grid\grid[ix-1+((iy+1)*gridsz)]<1 Then
												lastX=ix : lastY=iy
											EndIf
										EndIf
									EndIf
								EndIf
							Next
						Next
						
						If lastX=firstX And lastY=firstY Then
							RuntimeError("The maintenance tunnels could not be generated properly!")
						EndIf
						
						;place the tunnels
						
						For i=0 To 6
					;		Select True
					;			Case i=2
					;				tempStr="2c"
					;			Case i>2
					;				tempStr=Str(i)
					;			Default
					;				tempStr=Str(i+1)
					;		End Select
							Meshes[i]=CopyEntity(OBJTunnel(i))
							;Meshes[i]=LoadRMesh("GFX\map\mt"+tempStr+".rmesh",Null)
							DebugLog i
							HideEntity Meshes[i]
						Next
					;	Meshes[5]=CopyEntity(OBJTunnel(5))
					;	Meshes[5]=LoadRMesh("GFX\map\mt_elevator.rmesh",Null)
					;	HideEntity Meshes[5]
					;	Meshes[6]=CopyEntity(OBJTunnel(6))
					;	Meshes[6]=LoadRMesh("GFX\map\mt_generator.rmesh",Null)
					;	HideEntity Meshes[6]
						
						FreeTextureCache
						
						tempInt=0
						
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\grid[ix+(iy*gridsz)]>0 Then
									
									Select e\room\grid\grid[ix+(iy*gridsz)]
										Case 1,7
											
											tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
											
											If e\room\grid\grid[(ix+1)+((iy)*gridsz)]>0 Then
												RotateEntity tempInt,0,90,0
												e\room\grid\angles[ix+(iy*gridsz)]=1
											ElseIf e\room\grid\grid[(ix-1)+((iy)*gridsz)]>0 Then
												RotateEntity tempInt,0,270,0
												e\room\grid\angles[ix+(iy*gridsz)]=3
											ElseIf e\room\grid\grid[(ix)+((iy+1)*gridsz)]>0 Then
												RotateEntity tempInt,0,180,0
												e\room\grid\angles[ix+(iy*gridsz)]=2
											Else
												RotateEntity tempInt,0,0,0
												e\room\grid\angles[ix+(iy*gridsz)]=0
											EndIf
										Case 2
											
											If (ix=firstX And iy=firstY) Or (ix=lastX And iy=lastY) Then
												e\room\grid\grid[ix+(iy*gridsz)]=6
											EndIf
											
											If e\room\grid\grid[(ix+1)+((iy)*gridsz)]>0 And e\room\grid\grid[(ix-1)+((iy)*gridsz)]>0 Then ;horizontal
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
												
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
												
												tempInt2=Rand(0,1)
												RotateEntity tempInt,0.0,tempInt2*180.0+90,0.0
												
												e\room\grid\angles[ix+(iy*gridsz)]=(tempInt2*2)+1
											ElseIf e\room\grid\grid[(ix)+((iy+1)*gridsz)]>0 And e\room\grid\grid[(ix)+((iy-1)*gridsz)]>0 Then ;vertical
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
												
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
												
												tempInt2=Rand(0,1)
												RotateEntity tempInt,0.0,tempInt2*180.0,0.0
												e\room\grid\angles[ix+(iy*gridsz)]=(tempInt2*2)
											Else
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]])
												
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(412.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
												
												ia=e\room\grid\grid[(ix)+((iy+1)*gridsz)]
												ib=e\room\grid\grid[(ix)+((iy-1)*gridsz)]
												ic=e\room\grid\grid[(ix+1)+((iy)*gridsz)]
												id=e\room\grid\grid[(ix-1)+((iy)*gridsz)]
												
												If ia>0 And ic>0 Then
													RotateEntity tempInt,0,0,0
													e\room\grid\angles[ix+(iy*gridsz)]=0
												ElseIf ia>0 And id>0 Then
													RotateEntity tempInt,0,90,0
													e\room\grid\angles[ix+(iy*gridsz)]=1
												ElseIf ib>0 And ic>0 Then
													RotateEntity tempInt,0,270,0
													e\room\grid\angles[ix+(iy*gridsz)]=3
												Else
													RotateEntity tempInt,0,180,0
													e\room\grid\angles[ix+(iy*gridsz)]=2
												EndIf
											EndIf
											
											If (ix=firstX And iy=firstY) Then
												e\room\grid\grid[ix+(iy*gridsz)]=5
											EndIf
											
										Case 3
											tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]])
											
											ia=e\room\grid\grid[(ix)+((iy+1)*gridsz)]
											ib=e\room\grid\grid[(ix)+((iy-1)*gridsz)]
											ic=e\room\grid\grid[(ix+1)+((iy)*gridsz)]
											id=e\room\grid\grid[(ix-1)+((iy)*gridsz)]
											If ia>0 And ic>0 And id>0 Then
												RotateEntity tempInt,0,90,0
												e\room\grid\angles[ix+(iy*gridsz)]=1
											ElseIf ib>0 And ic>0 And id>0 Then
												RotateEntity tempInt,0,270,0
												e\room\grid\angles[ix+(iy*gridsz)]=3
											ElseIf ic>0 And ia>0 And ib>0 Then
												RotateEntity tempInt,0,0,0
												e\room\grid\angles[ix+(iy*gridsz)]=0
											Else
												RotateEntity tempInt,0,180,0
												e\room\grid\angles[ix+(iy*gridsz)]=2
											EndIf
										Case 4
											tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]])
											
											tempInt2=Rand(0,3)
											RotateEntity tempInt,0,tempInt2*90.0,0
											
											e\room\grid\angles[ix+(iy*gridsz)]=tempInt2
									End Select
									
									ScaleEntity tempInt,RoomScale,RoomScale,RoomScale,True
									PositionEntity tempInt,e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
									
									Select e\room\grid\grid[ix+(iy*gridsz)]
										Case 1;,5,6
											AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
										Case 3,4
											AddLight%(Null, e\room\x+ix*2.0, 8.0+(412.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
										Case 7
											AddLight%(Null, e\room\x+ix*2.0-(Sin(EntityYaw(tempInt,True))*504.0*RoomScale)+(Cos(EntityYaw(tempInt,True))*16.0*RoomScale), 8.0+(396.0*RoomScale), e\room\z+iy*2.0+(Cos(EntityYaw(tempInt,True))*504.0*RoomScale)+(Sin(EntityYaw(tempInt,True))*16.0*RoomScale), 2, 500.0 * RoomScale, 255, 200, 200)
											it = CreateItem("SCP-500-01","scp500",e\room\x+ix*2.0+(Cos(EntityYaw(tempInt,True))*(-208.0)*RoomScale)-(Sin(EntityYaw(tempInt,True))*1226.0*RoomScale),8.0+(80.0*RoomScale),e\room\z+iy*2.0+(Sin(EntityYaw(tempInt,True))*(-208.0)*RoomScale)+(Cos(EntityYaw(tempInt,True))*1226.0*RoomScale))
											EntityType (it\collider, HIT_ITEM)
											
											it = CreateItem("Night Vision Goggles", "nvgoggles",e\room\x+ix*2.0-(Sin(EntityYaw(tempInt,True))*504.0*RoomScale)+(Cos(EntityYaw(tempInt,True))*16.0*RoomScale), 8.0+(80.0*RoomScale), e\room\z+iy*2.0+(Cos(EntityYaw(tempInt,True))*504.0*RoomScale)+(Sin(EntityYaw(tempInt,True))*16.0*RoomScale))
											EntityType (it\collider, HIT_ITEM)
									End Select
									
									If e\room\grid\grid[ix+(iy*gridsz)]=6 Or e\room\grid\grid[ix+(iy*gridsz)]=5 Then
										dr=CreateDoor(e\room\zone,e\room\x+(ix*2.0)+(Cos(EntityYaw(tempInt,True))*240.0*RoomScale),8.0,e\room\z+(iy*2.0)+(Sin(EntityYaw(tempInt,True))*240.0*RoomScale),EntityYaw(tempInt,True)+90.0,Null,False,3,False,"")
										PositionEntity dr\buttons[0],EntityX(dr\buttons[0],True)+(Cos(EntityYaw(tempInt,True))*0.05),EntityY(dr\buttons[0],True)+0.0,EntityZ(dr\buttons[0],True)+(Sin(EntityYaw(tempInt,True))*0.05),True
										
										AddLight%(Null, e\room\x+ix*2.0+(Cos(EntityYaw(tempInt,True))*555.0*RoomScale), 8.0+(469.0*RoomScale), e\room\z+iy*2.0+(Sin(EntityYaw(tempInt,True))*555.0*RoomScale), 2, 600.0 * RoomScale, 255, 255, 255)
										
										tempInt2=CreatePivot()
										RotateEntity tempInt2,0,EntityYaw(tempInt,True)+180.0,0,True
										PositionEntity tempInt2,e\room\x+(ix*2.0)+(Cos(EntityYaw(tempInt,True))*552.0*RoomScale),8.0+(240.0*RoomScale),e\room\z+(iy*2.0)+(Sin(EntityYaw(tempInt,True))*552.0*RoomScale)
										If e\room\grid\grid[ix+(iy*gridsz)]=6 Then
											If e\room\RoomDoors[1]=Null Then
												dr\open = (Not e\room\RoomDoors[0]\open)
												e\room\RoomDoors[1]=dr
											Else
												RemoveDoor(dr)
											EndIf
											If e\room\Objects[3]=0 Then
												e\room\Objects[3]=tempInt2
												PositionEntity e\room\Objects[1],e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
											Else
												FreeEntity tempInt2
											EndIf
										Else
											If e\room\RoomDoors[3]=Null Then
												dr\open = (Not e\room\RoomDoors[2]\open)
												e\room\RoomDoors[3]=dr
											Else
												RemoveDoor(dr)
											EndIf
											If e\room\Objects[5]=0 Then
												e\room\Objects[5]=tempInt2
												PositionEntity e\room\Objects[0],e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
											Else
												FreeEntity tempInt2
											EndIf
										EndIf
									EndIf
									
									e\room\grid\Entities[ix+(iy*gridsz)]=tempInt
									
									wayp.WayPoints = CreateWaypoint(e\room\x+(ix*2.0),8.2,e\room\z+(iy*2.0),Null,e\room)
									
									e\room\grid\waypoints[ix+(iy*gridsz)]=wayp
									
									If iy<gridsz-1 Then
										If e\room\grid\waypoints[ix+((iy+1)*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+((iy+1)*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy+1)*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy+1)*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+((iy+1)*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If iy>0 Then
										If e\room\grid\waypoints[ix+((iy-1)*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+((iy-1)*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy-1)*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy-1)*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+((iy-1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+((iy-1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+((iy-1)*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If ix>0 Then
										If e\room\grid\waypoints[ix-1+(iy*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix-1+(iy*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix-1+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix-1+(iy*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix-1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix-1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix-1+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If ix<gridsz-1 Then
										If e\room\grid\waypoints[ix+1+(iy*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+1+(iy*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+1+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+1+(iy*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+1+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									
								EndIf
							Next
						Next
						
						For i=0 To 6
							e\room\grid\Meshes[i]=Meshes[i]
							;FreeEntity Meshes[i]
						Next
						
						PositionEntity e\room\Objects[0],e\room\x+firstX*2.0,8.0,e\room\z+firstY*2.0,True
						PositionEntity e\room\Objects[1],e\room\x+lastX*2.0,8.0,e\room\z+lastY*2.0,True
						
					Else If e\room\grid\Meshes[0]=0 Then
						
						;place the tunnels
						For i=0 To 6
					;		Select True
					;			Case i=2
					;				tempStr="2c"
					;			Case i>2
					;				tempStr=Str(i)
					;			Default
					;				tempStr=Str(i+1)
					;		End Select
							Meshes[i]=CopyEntity(OBJTunnel(i))
							DebugLog i
							HideEntity Meshes[i]
						Next
					;	Meshes[5]=CopyEntity(OBJTunnel(5))
					;	HideEntity Meshes[5]
					;	Meshes[6]=CopyEntity(OBJTunnel(6))
					;	HideEntity Meshes[6]
						
					;	For i=0 To 4
					;		Select True
					;			Case i=2
					;				tempStr="2c"
					;			Case i>2
					;				tempStr=Str(i)
					;			Default
					;				tempStr=Str(i+1)
					;		End Select
					;		Meshes[i]=LoadRMesh("GFX\map\mt"+tempStr+".rmesh",Null)
					;		DebugLog i
					;		HideEntity Meshes[i]
					;	Next
					;	
					;	Meshes[5]=LoadRMesh("GFX\map\mt_elevator.rmesh",Null)
					;	HideEntity Meshes[5]
					;	Meshes[6]=LoadRMesh("GFX\map\mt_generator.rmesh",Null)
					;	HideEntity Meshes[6]
						
						FreeTextureCache
						
						tempInt=0
						
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\grid[ix+(iy*gridsz)]>0 Then
									Select e\room\grid\grid[ix+(iy*gridsz)]
										Case 1,7
											tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
										Case 2
											If e\room\grid\grid[(ix+1)+((iy)*gridsz)]>0 And e\room\grid\grid[(ix-1)+((iy)*gridsz)]>0 Then ;horizontal
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
											ElseIf e\room\grid\grid[(ix)+((iy+1)*gridsz)]>0 And e\room\grid\grid[(ix)+((iy-1)*gridsz)]>0 Then ;vertical
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]-1])
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
											Else
												tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]])
												AddLight%(Null, e\room\x+ix*2.0, 8.0+(412.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
											EndIf
										Case 3,4
											tempInt%=CopyEntity(Meshes[e\room\grid\grid[ix+(iy*gridsz)]])
										Case 5,6
											tempInt%=CopyEntity(Meshes[5])
									End Select
									
									ScaleEntity tempInt,RoomScale,RoomScale,RoomScale,True
									
									RotateEntity tempInt,0,e\room\grid\angles[ix+(iy*gridsz)]*90.0,0
									PositionEntity tempInt,e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
									
									Select e\room\grid\grid[ix+(iy*gridsz)]
										Case 1,5,6
											AddLight%(Null, e\room\x+ix*2.0, 8.0+(368.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
										Case 3,4
											AddLight%(Null, e\room\x+ix*2.0, 8.0+(412.0*RoomScale), e\room\z+iy*2.0, 2, 500.0 * RoomScale, 255, 255, 255)
										Case 7
											AddLight%(Null, e\room\x+ix*2.0-(Sin(EntityYaw(tempInt,True))*504.0*RoomScale)+(Cos(EntityYaw(tempInt,True))*16.0*RoomScale), 8.0+(396.0*RoomScale), e\room\z+iy*2.0+(Cos(EntityYaw(tempInt,True))*504.0*RoomScale)+(Sin(EntityYaw(tempInt,True))*16.0*RoomScale), 2, 500.0 * RoomScale, 255, 200, 200)
									End Select
									
									If e\room\grid\grid[ix+(iy*gridsz)]=6 Or e\room\grid\grid[ix+(iy*gridsz)]=5 Then
										dr=CreateDoor(e\room\zone,e\room\x+(ix*2.0)+(Cos(EntityYaw(tempInt,True))*240.0*RoomScale),8.0,e\room\z+(iy*2.0)+(Sin(EntityYaw(tempInt,True))*240.0*RoomScale),EntityYaw(tempInt,True)+90.0,Null,False,3,False,"")
										
										AddLight%(Null, e\room\x+ix*2.0+(Cos(EntityYaw(tempInt,True))*555.0*RoomScale), 8.0+(469.0*RoomScale), e\room\z+iy*2.0+(Sin(EntityYaw(tempInt,True))*555.0*RoomScale), 2, 600.0 * RoomScale, 255, 255, 255)
										
										PositionEntity dr\buttons[0],EntityX(dr\buttons[0],True)+(Cos(EntityYaw(tempInt,True))*0.05),EntityY(dr\buttons[0],True)+0.0,EntityZ(dr\buttons[0],True)+(Sin(EntityYaw(tempInt,True))*0.05),True
										tempInt2=CreatePivot()
										RotateEntity tempInt2,0,EntityYaw(tempInt,True)+180.0,0,True
										PositionEntity tempInt2,e\room\x+(ix*2.0)+(Cos(EntityYaw(tempInt,True))*552.0*RoomScale),8.0+(240.0*RoomScale),e\room\z+(iy*2.0)+(Sin(EntityYaw(tempInt,True))*552.0*RoomScale)
										If e\room\grid\grid[ix+(iy*gridsz)]=6 Then
											If e\room\RoomDoors[1]=Null Then
												dr\open = (Not e\room\RoomDoors[0]\open)
												e\room\RoomDoors[1]=dr
											Else
												RemoveDoor(dr)
											EndIf
											If e\room\Objects[3]=0 Then
												e\room\Objects[3]=tempInt2
												PositionEntity e\room\Objects[1],e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
											Else
												FreeEntity tempInt2
											EndIf
										Else
											If e\room\RoomDoors[3]=Null Then
												dr\open = (Not e\room\RoomDoors[2]\open)
												e\room\RoomDoors[3]=dr
											Else
												RemoveDoor(dr)
											EndIf
											If e\room\Objects[5]=0 Then
												e\room\Objects[5]=tempInt2
												PositionEntity e\room\Objects[0],e\room\x+ix*2.0,8.0,e\room\z+iy*2.0,True
											Else
												FreeEntity tempInt2
											EndIf
										EndIf
									EndIf
									
									e\room\grid\Entities[ix+(iy*gridsz)]=tempInt
									
									wayp.WayPoints = CreateWaypoint(e\room\x+(ix*2.0),8.2,e\room\z+(iy*2.0),Null,e\room)
									
									e\room\grid\waypoints[ix+(iy*gridsz)]=wayp
									
									If iy<gridsz-1 Then
										If e\room\grid\waypoints[ix+((iy+1)*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+((iy+1)*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy+1)*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy+1)*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+((iy+1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+((iy+1)*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If iy>0 Then
										If e\room\grid\waypoints[ix+((iy-1)*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+((iy-1)*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy-1)*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+((iy-1)*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+((iy-1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+((iy-1)*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+((iy-1)*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If ix>0 Then
										If e\room\grid\waypoints[ix-1+(iy*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix-1+(iy*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix-1+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix-1+(iy*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix-1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix-1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix-1+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
									If ix<gridsz-1 Then
										If e\room\grid\waypoints[ix+1+(iy*gridsz)]<>Null Then
											dist=EntityDistance(e\room\grid\waypoints[ix+(iy*gridsz)]\obj,e\room\grid\waypoints[ix+1+(iy*gridsz)]\obj)
											For i=0 To 3
												If e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+1+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+1+(iy*gridsz)]
													e\room\grid\waypoints[ix+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
											For i=0 To 3
												If e\room\grid\waypoints[ix+1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)] Then
													Exit
												ElseIf e\room\grid\waypoints[ix+(iy*gridsz)]\connected[i]=Null Then
													e\room\grid\waypoints[ix+1+(iy*gridsz)]\connected[i]=e\room\grid\waypoints[ix+(iy*gridsz)]
													e\room\grid\waypoints[ix+1+(iy*gridsz)]\dist[i]=dist
													Exit
												EndIf
											Next
										EndIf
									EndIf
								EndIf
							Next
						Next
						
						For i=0 To 6
							e\room\grid\Meshes[i]=Meshes[i]
						Next
						
						SeedRnd oldSeed
						
						For it.Items = Each Items
							If (EntityY(it\collider,True)>=8.0) And (EntityY(it\collider,True)<=12.0) Then
								DebugLog it\name+" is within Y limits"
								If (EntityX(it\collider,True)>=e\room\x-6.0) And (EntityX(it\collider,True)<=(e\room\x+(2.0*gridsz)+6.0)) Then
									DebugLog "and within X limits"
								EndIf
								If (EntityZ(it\collider,True)>=e\room\z-6.0) And (EntityZ(it\collider,True)<=(e\room\z+(2.0*gridsz)+6.0)) Then
									DebugLog "and within Z limits"
								EndIf
							EndIf
							
							If (EntityY(it\collider,True)>=8.0) And (EntityY(it\collider,True)<=12.0) And (EntityX(it\collider,True)>=e\room\x-6.0) And (EntityX(it\collider,True)<=(e\room\x+(2.0*gridsz)+6.0)) And (EntityZ(it\collider,True)>=e\room\z-6.0) And (EntityZ(it\collider,True)<=(e\room\z+(2.0*gridsz)+6.0)) Then
								DebugLog it\name
								TranslateEntity it\collider,0.0,0.3,0.0,True
								ResetEntity it\collider
							EndIf
						Next
						
					EndIf
					;[End Block]
					
					If EntityY(Collider,True)>4.0 Then
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\Entities[ix+(iy*gridsz)]<>0
									ShowEntity e\room\grid\Entities[ix+(iy*gridsz)]
								EndIf
							Next
						Next
						
						For r.Rooms = Each Rooms
							If r <> e\room
								HideEntity r\obj
							EndIf
						Next
						EntityAlpha(GetChild(e\room\obj,2),0)
						
						ShouldPlay = 7
						
						If e\EventState = 0 Then
							If EntityDistance(Collider, e\room\Objects[0]) < EntityDistance(Collider, e\room\Objects[1]) Then
								temp = 0
							Else
								temp = 1
							EndIf
							e\EventState = 2
							
							If (Not Contained106) Then 	
								de.Decals = CreateDecal(0, EntityX(e\room\Objects[temp],True), EntityY(e\room\Objects[temp],True)+0.05, EntityZ(e\room\Objects[temp],True), 90, Rand(360), 0)
								de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals
								
								DebugLog "updateevents collider: "+EntityY(Collider,True)
								PositionEntity Curr106\Collider, EntityX(e\room\Objects[temp],True), EntityY(Collider,True)-3.0, EntityZ(e\room\Objects[temp],True)
								SetAnimTime Curr106\obj, 110
								Curr106\State = -0.1	
								Curr106\PrevY = EntityY(Collider)
							EndIf
							
							For i = 0 To 1
								Local spawnPoint.WayPoints = Null
								For x = i*((gridsz*gridsz)/5.0) To (gridsz*gridsz-1)
									DebugLog("spawn 966 X: "+x)
									If Rand(2)=1 And e\room\grid\waypoints[x]<>Null Then 
										spawnPoint = e\room	\grid\waypoints[x]
										x = gridsz*gridsz
									EndIf
								Next 
								If (spawnPoint<>Null) Then
									e\room\NPC[i] =CreateNPC(NPCtype966, EntityX(spawnPoint\obj,True), EntityY(spawnPoint\obj,True), EntityZ(spawnPoint\obj,True))
								EndIf
							Next
						EndIf
					Else
						For iy=0 To gridsz-1
							For ix=0 To gridsz-1
								If e\room\grid\Entities[ix+(iy*gridsz)]<>0
									HideEntity e\room\grid\Entities[ix+(iy*gridsz)]
								EndIf
							Next
						Next
					EndIf ;entityy(collider) >4
					
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[2],e\room\Objects[3], e, False)
					e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[4],e\room\Objects[5], e, False)
				Else
					If e\room\grid <> Null
						If e\room\grid\Meshes[0]<>0
							For iy=0 To gridsz-1
								For ix=0 To gridsz-1
									If e\room\grid\Entities[ix+(iy*gridsz)]<>0
										HideEntity e\room\grid\Entities[ix+(iy*gridsz)]
									EndIf
								Next
							Next
						EndIf
					EndIf
				EndIf 
				;[End Block]
			Case "room2pipes106"
				;[Block]
				If (Not Contained106) Then 
					If e\EventState = 0 Then
						If PlayerRoom = e\room Then e\EventState = 1
					Else
						e\EventState=(e\EventState+FPSfactor*0.7)
						;0-50 = walk to the middle
						;50-200 = look around
						;200-250 = leave
						If e\EventState < 50 Then
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[0], True), EntityY(Collider) - 0.15, EntityZ(e\room\Objects[0], True))
							PointEntity(Curr106\Collider, e\room\Objects[1])
							MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1])*0.5 * (e\EventState / 50.0))
							AnimateNPC(Curr106, 284, 333, 0.02*35)
						ElseIf e\EventState < 200
							Curr106\Idle = True
							AnimateNPC(Curr106, 334, 494, 0.2)
							
							PositionEntity(Curr106\Collider, (EntityX(e\room\Objects[0], True)+EntityX(e\room\Objects[1], True))/2, EntityY(Collider) - 0.15, (EntityZ(e\room\Objects[0], True)+EntityZ(e\room\Objects[1], True))/2)
							;MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1])*0.5)
							RotateEntity(Curr106\Collider,0, CurveValue(e\EventState,EntityYaw(Curr106\Collider),30.0),0,True)
							If EntityDistance(Curr106\Collider, Collider)<4.0 Then
								pvt = CreatePivot()
								PositionEntity(pvt, EntityX(Curr106\Collider),EntityY(Curr106\Collider),EntityZ(Curr106\Collider))
								PointEntity pvt, Collider
								If WrapAngle(EntityYaw(pvt)-EntityYaw(Curr106\Collider))<80 Then
									Curr106\State = -11
									Curr106\Idle = False
									PlaySound_Strict(HorrorSFX(10))
									e\EventState = 260
								EndIf
								FreeEntity pvt
							EndIf
						ElseIf e\EventState < 250
							Curr106\Idle = True
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[0], True), EntityY(Collider) - 0.15, EntityZ(e\room\Objects[0], True))
							PointEntity(Curr106\Collider, e\room\Objects[1])
							;200-250     (- 150)      50-100
							MoveEntity(Curr106\Collider, 0, 0, EntityDistance(e\room\Objects[0], e\room\Objects[1]) * ((e\EventState-150.0) / 100.0))
							AnimateNPC(Curr106, 284, 333, 0.02*35)
						EndIf
						ResetEntity(Curr106\Collider)
						
						PositionEntity(Curr106\obj, EntityX(Curr106\Collider), EntityY(Curr106\Collider) - 0.15, EntityZ(Curr106\Collider))
						RotateEntity Curr106\obj, 0, EntityYaw(Curr106\Collider), 0
						
						;PositionEntity(Curr106\Collider, EntityX(Curr106\Collider), EntityY(Collider) - 0.20, EntityZ(Curr106\Collider))
						
						If (e\EventState / 250.0) > 0.3 And ((e\EventState - FPSfactor*0.7) / 250.0) <= 0.3 Then
							e\SoundCHN = PlaySound_Strict(HorrorSFX(6))
							BlurTimer = 800
							d.Decals = CreateDecal(0, EntityX(e\room\Objects[2], True), EntityY(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True), 0, e\room\angle - 90, Rnd(360)) ;90, Rnd(360), 0
							d\Timer = 90000
							d\Alpha = 0.01 : d\AlphaChange = 0.005
							d\Size = 0.1 : d\SizeChange = 0.003
						EndIf
						
						If (e\EventState / 250.0) > 0.65 And ((e\EventState - FPSfactor*0.7) / 250.0) <= 0.65 Then
							d.Decals = CreateDecal(0, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True), EntityZ(e\room\Objects[3], True), 0, e\room\angle + 90, Rnd(360))
							d\Timer = 90000
							d\Alpha = 0.01 : d\AlphaChange = 0.005
							d\Size = 0.1 : d\SizeChange = 0.003
						EndIf						
						
						If e\EventState > 250 Then Curr106\Idle = False :RemoveEvent(e)
						
					End If
				EndIf
				;[End Block]
			Case "room2pit106"
                ;[Block]
                If (Not Contained106) And Curr106\State>0 Then 
                    If e\EventState = 0 Then
                        If PlayerRoom = e\room Then e\EventState = 1
                    Else
                        e\EventState = e\EventState + 1
                        PositionEntity(Curr106\Collider, EntityX(e\room\Objects[7], True), EntityY(e\room\Objects[7], True), EntityZ(e\room\Objects[7], True))
                        ResetEntity(Curr106\Collider)
                        
                        PointEntity(Curr106\Collider, Camera)
                        TurnEntity(Curr106\Collider, 0, Sin(MilliSecs2() / 20) * 6.0, 0, True)
                        MoveEntity(Curr106\Collider, 0, 0, Sin(MilliSecs2() / 15) * 0.06)
                        PositionEntity(Curr106\obj, EntityX(Curr106\Collider), EntityY(Curr106\Collider) - 0.15, EntityZ(Curr106\Collider))
                        
                        RotateEntity Curr106\obj, 0, EntityYaw(Curr106\Collider), 0
                        Curr106\Idle = True
                        AnimateNPC(Curr106, 334, 494, 0.3)
                        If e\EventState > 800 Then
                            If BlinkTimer < - 5 Then Curr106\Idle = False : RemoveEvent(e)
                        EndIf
                    EndIf
                End If
                ;[End Block]
			Case "room2pit"
				;[Block]
				If Curr173\Idle = 0 Then 
					If e\room\dist < 8.0  And e\room\dist > 0 Then			
						If (Not EntityVisible(Curr173\Collider, Camera)) And (Not EntityVisible(e\room\Objects[6], Camera)) Then 
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[6], True), 0.5, EntityZ(e\room\Objects[6], True))
							ResetEntity(Curr173\Collider)
							RemoveEvent(e)
						EndIf
					End If
				EndIf
				;[End Block]
			Case "room3pitduck"
				;[Block]
				If PlayerRoom = e\room Then
					If e\room\Objects[2] = 0 Then
						e\room\Objects[2] =	LoadMesh_Strict("GFX\npcs\duck_low_res.b3d")
						ScaleEntity(e\room\Objects[2], 0.07, 0.07, 0.07)
						tex = LoadTexture_Strict("GFX\npcs\duck1.png")
						EntityTexture e\room\Objects[2], tex
						FreeTexture tex
						PositionEntity (e\room\Objects[2], EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True))
						PointEntity e\room\Objects[2], e\room\obj
						RotateEntity(e\room\Objects[2], 0, EntityYaw(e\room\Objects[2],True),0, True)
						
						LoadEventSound(e,"SFX\SCP\Joke\Saxophone.ogg")
					Else
						If EntityInView(e\room\Objects[2],Camera)=False Then
							e\EventState = e\EventState + FPSfactor
							If Rand(200)=1 And e\EventState > 300 Then
								e\EventState = 0
								e\SoundCHN = PlaySound2(e\Sound, Camera, e\room\Objects[2],6.0)
							EndIf
						Else
							If e\SoundCHN <> 0 Then
								If ChannelPlaying(e\SoundCHN) Then StopChannel e\SoundCHN
							EndIf
						EndIf						
					EndIf
				EndIf
				;[End Block]
			Case "room3pit1048"
				;[Block]
				If PlayerRoom = e\room Then
					If e\room\Objects[2] = 0 Then
						e\room\Objects[2] =	LoadAnimMesh_Strict("GFX\npcs\scp-1048pp.b3d")
						ScaleEntity e\room\Objects[2], 0.05,0.05,0.05
						SetAnimTime(e\room\Objects[2], 414)
						
						Local imgPath$ = "GFX\items\1048\1048_"+Rand(1,20)+".jpg"
						
						Local itt.ItemTemplates
						For itt.ItemTemplates = Each ItemTemplates
							If itt\name = "Drawing" Then
								If itt\img<>0 Then FreeImage itt\img	
								itt\img = LoadImage_Strict(imgPath)
								MaskImage(itt\img, 255,0,255)
								itt\imgpath = imgPath
								
								Exit
							EndIf
						Next
						
						tex% = LoadTexture_Strict(imgPath)
						Local brush% = LoadBrush_Strict(imgPath, 1)
						
						For i = 1 To CountSurfaces(e\room\Objects[2])
							sf% = GetSurface(e\room\Objects[2],i)
							b% = GetSurfaceBrush( sf )
							t% = GetBrushTexture(b, 0)
							texname$ = StripPath(TextureName(t))
							DebugLog "texname: "+texname
							If Lower(texname) = "1048_1.jpg" Then
								PaintSurface sf, brush
							EndIf
							;MAV???
							;If texname<>"" Then FreeTexture t
							FreeBrush b
						Next
						
						FreeTexture tex
						FreeBrush brush
						
						PositionEntity (e\room\Objects[2], EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True))
						
						;e\Sound = LoadSound_Strict("SFX\SCP\Joke\Saxophone.ogg")
					Else
						PointEntity e\room\Objects[2], Collider
						RotateEntity(e\room\Objects[2], -90, EntityYaw(e\room\Objects[2],True),0, True)
						
						If e\EventState=0 Then
							If (EntityDistance(Collider, e\room\Objects[2])<3.0) Then
								If EntityInView(e\room\Objects[2],Camera) Then 
									e\EventState = 1
									GiveAchievement(Achv1048)
								EndIf
							EndIf
						Else If e\EventState=1
							Animate2(e\room\Objects[2], AnimTime(e\room\Objects[2]), 1, 205, 0.5, False)
							If AnimTime(e\room\Objects[2])=205 Then e\EventState=2
						Else If e\EventState = 2
							Animate2(e\room\Objects[2], AnimTime(e\room\Objects[2]), 205, 353, 1.0)	
							If (EntityDistance(Collider, e\room\Objects[2])<1.5) Then
								DrawHandIcon = True
								
								If MouseHit1 Then
									If ItemAmount >= MaxItemAmount Then
										Msg = "You cannot carry any more items."
										MsgTimer = 70 * 5
									Else
										SelectedItem = CreateItem("Drawing", "paper", 0.0, 0.0, 0.0)
										EntityType SelectedItem\collider,HIT_ITEM
										
										PickItem(SelectedItem)
										
										FreeEntity(e\room\Objects[2])
										e\room\Objects[2] = 0
										
										e\EventState = 3
										RemoveEvent(e)
									EndIf
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room2poffices2"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState = 0 Then
						If e\room\RoomDoors[0]\open = True Then 
							If e\room\RoomDoors[0]\openstate = 180 Then 
								e\EventState = 1
								PlaySound_Strict HorrorSFX(5)
							EndIf
						Else
							If (EntityDistance(Collider, e\room\RoomDoors[0]\obj)<1.5) And (RemoteDoorOn) Then
								e\room\RoomDoors[0]\open = True
							EndIf
						EndIf
					Else
						If EntityDistance(e\room\Objects[0], Collider) < 2.0 Then
							HeartBeatVolume = CurveValue(0.5, HeartBeatVolume, 5)
							HeartBeatRate = CurveValue(120, HeartBeatRate, 150) 
							e\SoundCHN = LoopSound2(OldManSFX(4), e\SoundCHN, Camera, e\room\obj, 5.0, 0.3)
							Curr106\State=Curr106\State-FPSfactor*3
						EndIf
						
					EndIf
				EndIf
				;[End Block]
			Case "room2servers"
				;[Block]
				If e\EventState=0 Then
					If PlayerRoom = e\room Then
						;close the doors when the player enters the room
						UseDoor(e\room\RoomDoors[0],False)
						e\room\RoomDoors[0]\locked = True
						UseDoor(e\room\RoomDoors[1],False)
						e\room\RoomDoors[1]\locked = True
						
						If Curr096=Null Then
							Curr096 = CreateNPC(NPCtype096, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True)+0.1,EntityZ(e\room\Objects[6],True))
						Else
							PositionEntity Curr096\Collider, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True)+0.1,EntityZ(e\room\Objects[6],True),True
						EndIf
						
						RotateEntity Curr096\Collider, 0, e\room\angle+270, 0, True
						ResetEntity Curr096\Collider
						Curr096\State=6
						Curr096\State2=70*10
						
						LoadEventSound(e,"SFX\Character\Guard\096ServerRoom1.ogg")
						e\SoundCHN = PlaySound_Strict (e\Sound)
						
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True),EntityZ(e\room\Objects[7],True))
						
						GiveAchievement(Achv096)
						
						e\EventState=1
					EndIf
				ElseIf e\EventState < 70*45
					If	Rand(200)<5 And PlayerRoom = e\room Then 
						LightBlink = Rnd(1.0,2.0)
						If Rand(5)=1 Then PlaySound2(IntroSFX(Rand(10,12)), Camera, e\room\obj, 8.0, Rnd(0.1,0.3))
					EndIf
					
					e\EventState=Min(e\EventState+FPSfactor,70*43)
					
					If e\room\NPC[0]<>Null Then
						;[Block]
;						If e\EventState < 70*13 Then
;							
;							If e\EventState > 70*8 Then
;								Curr096\State=Min(Max(2,Curr096\State),3)
;								Curr096\State2=70*10
;							Else
;								Curr096\State = 5
;								PointEntity Curr096\Collider, e\room\Objects[9]
;								RotateEntity Curr096\Collider, 0, EntityYaw(Curr096\Collider,True),0,True								
;								If EntityDistance(Curr096\Collider, e\room\Objects[8])<2.4 Then
;									Curr096\State2 = 0
;								Else
;									Curr096\State2 = 1001
;								EndIf	
;							EndIf
;							
;							e\room\NPC[0]\State=13
;							;SetAnimTime e\room\NPC[0]\obj, 115
;							PointEntity e\room\NPC[0]\Collider, Curr096\Collider								
;						ElseIf e\EventState-FPSfactor =< 70*15 Then ;walk to the doorway
;							If e\EventState > 70*15 Then
;								e\room\NPC[0]\State=14
;								;e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(e\room\Objects[8],True),0.5,EntityZ(e\room\Objects[8],True))
;								e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(Curr096\Collider,True),0.4,EntityZ(Curr096\Collider,True))
;								e\room\NPC[0]\PathTimer=300
;							EndIf
;						ElseIf e\EventState<70*20 Then
;							If e\room\NPC[0]\PathStatus=0 Then
;								e\room\RoomDoors[2]\open = False
;								
;								e\room\NPC[0]\State=13
;								;SetAnimTime e\room\NPC[0]\obj, 115
;								PointEntity e\room\NPC[0]\obj, Curr096\Collider
;								RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
;								
;							EndIf
;						Else ;start walking away
;							
;							If Curr096\State = 4 Then ;shoot at 096 when it starts attacking
;								Curr096\LastSeen=1
;								
;								e\room\NPC[0]\State = 2
;								PointEntity e\room\NPC[0]\obj, Curr096\Collider
;								RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
;								If PlayerRoom = e\room Then LightBlink = (e\room\NPC[0]\Reload)+Rnd(0.5,2.0)
;								Curr096\Target = e\room\NPC[0]
;							Else
;								If e\EventState>70*22 Then Curr096\State = 4
;								If e\room\NPC[0]\State=13 Then
;									e\room\NPC[0]\State=14
;									e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(e\room\obj,True),0.4,EntityZ(e\room\obj,True))
;									e\room\NPC[0]\PathTimer=300
;									e\room\NPC[0]\Speed = e\room\NPC[0]\Speed*1.8 ;Making the guard walking a bit faster
;								EndIf
;							EndIf
;						EndIf
;						
;						Curr096\Target = e\room\NPC[0]
;						If AnimTime(Curr096\obj)>25 And AnimTime(Curr096\obj)<150 Then
;							FreeSound_Strict e\Sound : e\Sound = 0
;							e\Sound=LoadSound_Strict("SFX\Character\Guard\096ServerRoom2.ogg")
;							e\SoundCHN=PlaySound_Strict(e\Sound)
;							
;							Curr096\CurrSpeed = 0
;							
;							For i = 0 To 6
;								If e\room\angle = 0 Or e\room\angle = 180 Then
;									de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle+90,Rnd(360))
;									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
;									de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle-90,Rnd(360))
;									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
;								Else
;									de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle+90,Rnd(360))
;									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
;									de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle-90,Rnd(360))
;									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
;								EndIf
;								de.Decals = CreateDecal(Rand(2,3), EntityX(e\room\NPC[0]\Collider)+Rnd(-2,2),Rnd(0.001,0.003),EntityZ(e\room\NPC[0]\Collider)+Rnd(-2,2),90,Rnd(360),0)
;								
;							Next
;							de\Size = Rnd(0.5,0.7)
;							ScaleSprite(de\obj, de\Size,de\Size)
;							
;							Curr096\State=5
;							
;							RemoveNPC(e\room\NPC[0])
;							e\room\NPC[0]=Null
;							
;						EndIf
						;[End Block]
						
						Curr096\Target = e\room\NPC[0]
						
						If e\EventState < 70*8
							AnimateNPC(Curr096,472,520,0.25)
							PointEntity e\room\NPC[0]\Collider, Curr096\Collider
						ElseIf e\EventState >= 70*8 And e\EventState < 70*10
							;Checking at which side the player is
							If EntityDistance(Collider,e\room\RoomDoors[0]\frameobj)<EntityDistance(Collider,e\room\RoomDoors[1]\frameobj)
								AnimateNPC(Curr096,521,555,0.25,False)
								If Curr096\Frame=>554.5
									e\EventState=70*10
									Curr096\Frame = 677
									SetNPCFrame(Curr096,Curr096\Frame)
									Curr096\State = 1
									TurnEntity Curr096\Collider,0,180,0
									MoveEntity Curr096\Collider,0,0,0.3
								EndIf
							Else
								AnimateNPC(Curr096,556,590,0.25,False)
								If Curr096\Frame=>589.5
									e\EventState=70*10
									Curr096\Frame = 677
									SetNPCFrame(Curr096,Curr096\Frame)
									Curr096\State = 1
									TurnEntity Curr096\Collider,0,180,0
									MoveEntity Curr096\Collider,0,0,0.3
								EndIf
							EndIf
							PointEntity e\room\NPC[0]\Collider, Curr096\Collider
						ElseIf e\EventState >= 70*10 And e\EventState < 70*20
							Curr096\State=Min(Max(1,Curr096\State),3)
							Curr096\State2=Max(Curr096\State2,70*12)
							If e\EventState-FPSfactor =< 70*15 Then ;walk to the doorway
								If e\EventState > 70*15 Then
									e\room\NPC[0]\State=14
									e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(Curr096\Collider,True),0.4,EntityZ(Curr096\Collider,True))
									e\room\NPC[0]\PathTimer=300
								Else
									PointEntity e\room\NPC[0]\Collider, Curr096\Collider
								EndIf
							EndIf
							;If e\room\NPC[0]\PathStatus=0 Then
							If EntityVisible(e\room\NPC[0]\Collider,Curr096\Collider)
								e\room\RoomDoors[2]\open = False
								e\room\NPC[0]\State=13
								PointEntity e\room\NPC[0]\obj, Curr096\Collider
								RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
							EndIf
						Else
							If Curr096\State = 4 Then ;shoot at 096 when it starts attacking
								Curr096\LastSeen=1
								e\room\NPC[0]\State = 2
								PointEntity e\room\NPC[0]\obj, Curr096\Collider
								RotateEntity (e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj),EntityYaw(e\room\NPC[0]\Collider),30),0)
								If PlayerRoom = e\room Then LightBlink = (e\room\NPC[0]\Reload)+Rnd(0.5,2.0)
								Curr096\Target = e\room\NPC[0]
							Else
								If e\EventState>70*22 Then Curr096\State = 4
								If e\room\NPC[0]\State=13 Then
									e\room\NPC[0]\State=14
									e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0], EntityX(e\room\obj,True),0.4,EntityZ(e\room\obj,True))
									e\room\NPC[0]\PathTimer=300
									e\room\NPC[0]\Speed = e\room\NPC[0]\Speed*1.8 ;Making the guard walking a bit faster
								EndIf
							EndIf
						EndIf
						
						If AnimTime(Curr096\obj)>25 And AnimTime(Curr096\obj)<150 Then
							FreeSound_Strict e\Sound : e\Sound = 0
							e\Sound=LoadSound_Strict("SFX\Character\Guard\096ServerRoom2.ogg")
							e\SoundCHN=PlaySound_Strict(e\Sound)
							
							Curr096\CurrSpeed = 0
							
							For i = 0 To 6
								If e\room\angle = 0 Or e\room\angle = 180 Then
									de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle+90,Rnd(360))
									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
									de.Decals = CreateDecal(Rand(2,3), e\room\x-Rnd(197,199)*Cos(e\room\angle)*RoomScale, 1.0, e\room\z+(140.0*(i-3))*RoomScale,0,e\room\angle-90,Rnd(360))
									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
								Else
									de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle+90,Rnd(360))
									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
									de.Decals = CreateDecal(Rand(2,3), e\room\x+(140.0*(i-3))*RoomScale, 1.0, e\room\z-Rnd(197,199)*Sin(e\room\angle)*RoomScale-Rnd(0.001,0.003),0,e\room\angle-90,Rnd(360))
									de\size = Rnd(0.8,0.85) : de\sizechange = 0.001
								EndIf
								de.Decals = CreateDecal(Rand(2,3), EntityX(e\room\NPC[0]\Collider)+Rnd(-2,2),Rnd(0.001,0.003),EntityZ(e\room\NPC[0]\Collider)+Rnd(-2,2),90,Rnd(360),0)
								
							Next
							de\Size = Rnd(0.5,0.7)
							ScaleSprite(de\obj, de\Size,de\Size)
							
							Curr096\State=5
							StopStream_Strict(Curr096\SoundChn)
							Curr096\SoundChn=0
							
							RemoveNPC(e\room\NPC[0])
							e\room\NPC[0]=Null
						EndIf
					Else
						
						If e\EventState >= 70*40 And e\EventState-FPSfactor < 70*40 Then ;open them again to let the player in
							e\room\RoomDoors[0]\locked=False
							e\room\RoomDoors[1]\locked=False
							UseDoor(e\room\RoomDoors[0],False)
							UseDoor(e\room\RoomDoors[1],False)
							FreeSound_Strict e\Sound : e\Sound = 0
							e\room\RoomDoors[0]\locked=True
							e\room\RoomDoors[1]\locked=True
						EndIf
						
						If PlayerRoom = e\room Then
							If e\SoundCHN<>0 Then
								If	ChannelPlaying(e\SoundCHN) Then 
									LightBlink = Rnd(0.5,6.0)
									If Rand(50)=1 Then PlaySound2(IntroSFX(Rand(10,12)), Camera, e\room\obj, 8.0, Rnd(0.1,0.3))
								EndIf
							EndIf						
							
							If (e\room\angle = 0 Or e\room\angle = 180) Then ;lock the player inside
								If Abs(EntityX(Collider)-EntityX(e\room\obj,True))> 1.3 Then 
									e\EventState = 70*50
									e\Sound=0
								EndIf
							Else
								If Abs(EntityZ(Collider)-EntityZ(e\room\obj,True))> 1.3 Then 
									e\EventState = 70*50
									e\Sound=0
								EndIf
							EndIf	
						EndIf
						
					EndIf
					
				ElseIf PlayerRoom = e\room
					temp = UpdateLever(e\room\Objects[1]) ;power switch
					x = UpdateLever(e\room\Objects[3]) ;fuel pump
					z = UpdateLever(e\room\Objects[5]) ;generator
					
					;fuel pump on
					If x Then
						e\EventState2 = Min(1.0, e\EventState2+FPSfactor/350)
						
						;generator on
						If z Then
							If e\Sound2=0 Then LoadEventSound(e,"SFX\General\GeneratorOn.ogg",1)
							e\EventState3 = Min(1.0, e\EventState3+FPSfactor/450)
						Else
							e\EventState3 = Min(0.0, e\EventState3-FPSfactor/450)
						EndIf
					Else
						e\EventState2 = Max(0, e\EventState2-FPSfactor/350)
						e\EventState3 = Max(0, e\EventState3-FPSfactor/450)
					EndIf
					
					If e\EventState2>0 Then e\SoundCHN=LoopSound2(RoomAmbience[8], e\SoundCHN, Camera, e\room\Objects[3], 5.0, e\EventState2*0.8)
					If e\EventState3>0 Then e\SoundCHN2=LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\Objects[5], 6.0, e\EventState3)
					
					If temp=0 And x And z Then
						e\room\RoomDoors[0]\locked = False
						e\room\RoomDoors[1]\locked = False
					Else
						If	Rand(200)<5 Then LightBlink = Rnd(0.5,1.0)
						
						If e\room\RoomDoors[0]\open Then 
							e\room\RoomDoors[0]\locked = False
							UseDoor(e\room\RoomDoors[0],False) 
						EndIf
						If e\room\RoomDoors[1]\open Then 
							e\room\RoomDoors[1]\locked = False
							UseDoor(e\room\RoomDoors[1],False)
						EndIf
						e\room\RoomDoors[0]\locked=True
						e\room\RoomDoors[1]\locked=True							
					EndIf 
				EndIf
				
				;[End Block]
			Case "room2storage"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState2 <= 0 Then
						e\room\RoomDoors[1]\locked = False
						e\room\RoomDoors[4]\locked = False
						
						If EntityDistance(Collider, Curr173\obj)<8.0 Or EntityDistance(Collider, Curr106\obj)<8.0 Then
							e\room\RoomDoors[1]\locked = True
							e\room\RoomDoors[4]\locked = True
						Else
							For n.NPCs = Each NPCs
								If n\NPCtype = NPCtypeMTF Then 
									If EntityDistance(Collider, Curr173\obj)<8.0 Then 
										e\room\RoomDoors[1]\locked = True
										e\room\RoomDoors[4]\locked = True
										Exit
									EndIf
								EndIf
							Next
						EndIf
						e\EventState2 = 70*5
					Else
						e\EventState2 = e\EventState2 - FPSfactor
					EndIf
					
					LightVolume = TempLightVolume*0.5
					
					TFormPoint EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,e\room\obj
					
					temp = 0
					If TFormedX()>730 Then
						GiveAchievement(Achv970)
						
						UpdateWorld()
						TFormPoint EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,e\room\obj
						
						;1->3, 2->4
						;3->0, 4->0
						For i = 1 To 2
							e\room\RoomDoors[i]\open = e\room\RoomDoors[i+2]\open
							e\room\RoomDoors[i]\openstate = e\room\RoomDoors[i+2]\openstate
							PositionEntity e\room\RoomDoors[i]\obj, EntityX(e\room\RoomDoors[i+2]\obj),EntityY(e\room\RoomDoors[i+2]\obj),EntityZ(e\room\RoomDoors[i+2]\obj)
							PositionEntity e\room\RoomDoors[i]\obj2, EntityX(e\room\RoomDoors[i+2]\obj2),EntityY(e\room\RoomDoors[i+2]\obj2),EntityZ(e\room\RoomDoors[i+2]\obj2)							
							
							e\room\RoomDoors[i+2]\open = False
							e\room\RoomDoors[i+2]\openstate = 0
							PositionEntity e\room\RoomDoors[i+2]\obj, EntityX(e\room\RoomDoors[0]\obj),EntityY(e\room\RoomDoors[0]\obj),EntityZ(e\room\RoomDoors[0]\obj)
							PositionEntity e\room\RoomDoors[i+2]\obj2, EntityX(e\room\RoomDoors[0]\obj2),EntityY(e\room\RoomDoors[0]\obj2),EntityZ(e\room\RoomDoors[0]\obj2)							
						Next	
						
						TFormPoint TFormedX()-1024, TFormedY(), TFormedZ(),e\room\obj,0
						HideEntity Collider
						PositionEntity Collider, TFormedX(), EntityY(Collider), TFormedZ(), True
						ShowEntity Collider
						DebugLog "tformedx()>720"
						temp = True
						
					ElseIf TFormedX()<-730
						GiveAchievement(Achv970)
						
						UpdateWorld()
						TFormPoint EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,e\room\obj
						
						;3->1, 4->2
						;1->0, 2->0
						For i = 1 To 2
							e\room\RoomDoors[i+2]\open = e\room\RoomDoors[i]\open
							e\room\RoomDoors[i+2]\openstate = e\room\RoomDoors[i]\openstate
							PositionEntity e\room\RoomDoors[i+2]\obj, EntityX(e\room\RoomDoors[i]\obj),EntityY(e\room\RoomDoors[i]\obj),EntityZ(e\room\RoomDoors[i]\obj)
							PositionEntity e\room\RoomDoors[i+2]\obj2, EntityX(e\room\RoomDoors[i]\obj2),EntityY(e\room\RoomDoors[i]\obj2),EntityZ(e\room\RoomDoors[i]\obj2)							
							
							e\room\RoomDoors[i]\open = False
							e\room\RoomDoors[i]\openstate = 0
							PositionEntity e\room\RoomDoors[i]\obj, EntityX(e\room\RoomDoors[0]\obj),EntityY(e\room\RoomDoors[0]\obj),EntityZ(e\room\RoomDoors[0]\obj)
							PositionEntity e\room\RoomDoors[i]\obj2, EntityX(e\room\RoomDoors[0]\obj2),EntityY(e\room\RoomDoors[0]\obj2),EntityZ(e\room\RoomDoors[0]\obj2)							
						Next
						
						TFormPoint TFormedX()+1024, TFormedY(), TFormedZ(),e\room\obj,0
						HideEntity Collider
						PositionEntity Collider, TFormedX(), EntityY(Collider), TFormedZ(), True
						ShowEntity Collider
						
						DebugLog "tformedx()<720"
						
						temp = True
					EndIf
					
					If temp = True Then 
						
						e\EventState=e\EventState+1;Rand(1,2)
						
						For it.Items = Each Items
							If EntityDistance(it\collider,Collider)<5.0 Then
								
								TFormPoint EntityX(it\collider),EntityY(it\collider),EntityZ(it\collider),0,e\room\obj
								x = TFormedX() : y = TFormedY() : z = TFormedZ()
								If TFormedX()>264 Then
									TFormPoint x-1024,y,z,e\room\obj,0
									PositionEntity it\collider, TFormedX(), TFormedY(), TFormedZ()
									ResetEntity it\collider
								ElseIf TFormedX()<-264
									TFormPoint x+1024,y,z,e\room\obj,0
									PositionEntity it\collider, TFormedX(), TFormedY(), TFormedZ()
									ResetEntity it\collider
								EndIf
								
							EndIf
						Next
						
						Select e\EventState 
							Case 2
								i = Rand(MaxItemAmount)
								If Inventory(i)<>Null Then RemoveItem(Inventory(i))								
							Case 5
								Injuries = Injuries + 0.3
							Case 10
								de.Decals = CreateDecal(3, EntityX(e\room\obj)+Cos(e\room\angle-90)*760*RoomScale, 0.0005, EntityZ(e\room\obj)+Sin(e\room\angle-90)*760*RoomScale,90,Rnd(360),0)
							Case 14
								For i = 0 To MaxItemAmount-1
									If Inventory(i)<> Null Then
										If Inventory(i)\itemtemplate\tempname = "paper" Then
											RemoveItem(Inventory(i))
											For itt.ItemTemplates = Each ItemTemplates
												If itt\tempname = "paper" And Rand(6)=1 Then
													Inventory(i) = CreateItem(itt\name, itt\tempname, 1,1,1)
													HideEntity Inventory(i)\collider
													Inventory(i)\Picked = True
													Exit
												EndIf
											Next
											Exit
										EndIf
									EndIf
								Next
							Case 18
								TFormPoint -344,176, 272, e\room\obj,0
								it.Items = CreateItem("Strange Note", "paper", TFormedX(), TFormedY(), TFormedZ())
								EntityType(it\collider, HIT_ITEM)
							Case 25
								e\room\NPC[0]=CreateNPC(NPCtypeD, EntityX(e\room\obj)+Cos(e\room\angle-90)*760*RoomScale, 0.35, EntityZ(e\room\obj)+Sin(e\room\angle-90)*760*RoomScale)
								RotateEntity e\room\NPC[0]\Collider, 0, e\room\angle-200, 0, True
								tex=LoadTexture("GFX\NPCs\corpse.jpg")
								e\room\NPC[0]\texture = "GFX\NPCs\corpse.jpg"
								EntityTexture e\room\NPC[0]\obj, tex
								FreeTexture tex
								SetAnimTime(e\room\NPC[0]\obj,80)
								e\room\NPC[0]\State=10
							Case 30
								i = Rand(0,MaxItemAmount-1)
								If Inventory(i)<>Null Then RemoveItem(Inventory(i))
								Inventory(i) = CreateItem("Strange Note", "paper", 1,1,1)
								HideEntity Inventory(i)\collider
								Inventory(i)\Picked = True
							Case 35
								For i = 0 To 3
									de.Decals = CreateDecal(17, e\room\x+Rnd(-2,2), 700*RoomScale, e\room\z+Rnd(-2,2), 270, Rand(360), 0)
									de\Size = 0.05 : de\SizeChange = 0.0005 : EntityAlpha(de\obj, 0.8) : UpdateDecals
								Next
							Case 40
								PlaySound_Strict(LoadTempSound("SFX\radio\franklin4.ogg"))
							Case 50
								e\room\NPC[1]=CreateNPC(NPCtypeGuard, EntityX(e\room\obj)+Cos(e\room\angle+90)*600*RoomScale, 0.35, EntityZ(e\room\obj)+Sin(e\room\angle+90)*600*RoomScale)
								e\room\NPC[1]\State=7
							Case 52
								If e\room\NPC[1] <> Null Then
									RemoveNPC(e\room\NPC[1])
									e\room\NPC[1]=Null
								EndIf
							Case 60
								If (Not HalloweenTex) Then
									Local tex970 = LoadTexture_Strict("GFX\npcs\173h.pt", 1)
									EntityTexture Curr173\obj, tex970, 0, 0
									FreeTexture tex970
								EndIf
						End Select
						
						If Rand(10)=1 Then
							temp = Rand(0,2)
							PlaySound_Strict(AmbientSFX(temp, Rand(0,AmbientSFXAmount(temp)-1)))
						EndIf
					Else
						If e\room\NPC[0] <> Null Then
							If EntityDistance(Collider, e\room\NPC[0]\Collider)<3.0 Then
								If EntityInView(e\room\NPC[0]\obj, Camera) Then
									CurrCameraZoom = (Sin(Float(MilliSecs2())/20.0)+1.0)*15.0
									HeartBeatVolume = Max(CurveValue(0.3, HeartBeatVolume, 2.0), HeartBeatVolume)
									HeartBeatRate = Max(HeartBeatRate, 120)
								EndIf
							EndIf
						EndIf
						
						If e\room\NPC[1] <> Null Then
							PointEntity e\room\NPC[1]\obj, Collider
							RotateEntity e\room\NPC[1]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[1]\obj),EntityYaw(e\room\NPC[1]\Collider),35),0
						EndIf
						
						;If Abs(TFormedX())<264 Then 
						For it.Items = Each Items
							If (it\Dropped=1 And Abs(TFormedX())<264) Or it\Dropped=-1 Then
								DebugLog "dropping/picking: "+it\Dropped+" - "+EntityX(it\collider)+", "+EntityY(it\collider)+", "+EntityZ(it\collider)
								
								TFormPoint EntityX(it\collider),EntityY(it\collider),EntityZ(it\collider),0,e\room\obj
								x = TFormedX() : y = TFormedY() : z = TFormedZ()
								
								If it\Dropped=1 Then
									For i = - 1 To 1 Step 2
										TFormPoint x+1024*i,y,z,e\room\obj,0
										it2.items = CreateItem(it\name, it\itemtemplate\tempname, TFormedX(), EntityY(it\collider), TFormedZ())
										RotateEntity(it2\collider, EntityPitch(it\collider),EntityYaw(it\collider),0)
										EntityType(it2\collider, HIT_ITEM)
									Next
								Else
									For it2.items = Each Items
										If it2<>it And it2\dist < 15.0 Then
											
											TFormPoint EntityX(it2\collider),EntityY(it2\collider),EntityZ(it2\collider),0,e\room\obj
											DebugLog TFormedZ()+" - "+z
											
											If TFormedZ()=z Then RemoveItem(it2) : DebugLog "item removed"									
										EndIf
									Next
								EndIf
								
								Exit
							EndIf
						Next
						;EndIf						
					EndIf
					
				EndIf
				
				
				If e\EventState > 26 Then
					If Abs(EntityX(Collider)-e\room\x)<8.0 Then
						If Abs(EntityZ(Collider)-e\room\z)<8.0 Then
							If e\Sound = 0 Then
								e\Sound = LoadSound_Strict("SFX\SCP\970\Corpse.ogg")
							EndIf
							e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\NPC[0]\obj);
							If e\EventState < 30 Then
								LightVolume = TempLightVolume*0.4
							ElseIf e\EventState > 60
								AnimateNPC(e\room\NPC[0], 80, 61, -0.02, False)
								
								e\room\NPC[0]\DropSpeed = 0
								y = CurveValue(1.5+Sin(Float(MilliSecs2())/20.0)*0.1,EntityY(e\room\NPC[0]\Collider),50.0)
								
								PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\NPC[0]\Collider),y,EntityZ(e\room\NPC[0]\Collider)
								TurnEntity e\room\NPC[0]\Collider,0,0.1*FPSfactor,0
							EndIf 								
						EndIf
						
					EndIf
				EndIf					
				
				
				;[End Block]
			Case "room2test1074"
				;[Block]
				
				If PlayerRoom=e\room Then
					If e\Sound=0 Then e\Sound=LoadSound_Strict("SFX\music\420J.ogg") ;tell me if you can find a better alternative to this					
					
					GiveAchievement(Achv1074)
					If (Wearing714=1) Or (WearingNightVision>0) Then
						Playable = True : e\EventState = 0 : e\EventState2 = 0
						If EntityInView(e\room\Objects[0],Camera) And EntityVisible(e\room\Objects[0],Camera) Then GiveAchievement(Achv1074)
					EndIf					
					
					;check if 714 is being used, and choose the right texture---------------					
					If (e\EventState3 < 1.0 And ((Wearing714=1) Or (WearingNightVision>0))) Then
						ForceAngle = 0.0
						e\EventState3 = 1.0
						If EntityInView(e\room\Objects[0],Camera) And EntityVisible(e\room\Objects[0],Camera) Then BlinkTimer = -2 : BlurTimer = 500
						If e\SoundCHN <> 0 Then StopChannel e\SoundCHN : e\SoundCHN = 0
						aa=GetSurfaceBrush(e\room\NonFreeAble[0])
						BrushTexture aa, e\room\Textures[0], 0, 1
						PaintSurface e\room\NonFreeAble[0],aa
						FreeBrush aa
						HeartBeatVolume = 0.0
					EndIf
					If (e\EventState3 > 0.0 And (Not ((Wearing714=1) Or (WearingNightVision>0)))) Then
						e\EventState3 = 0.0
						If EntityInView(e\room\Objects[0],Camera) And EntityVisible(e\room\Objects[0],Camera) Then BlinkTimer = -2 : BlurTimer = 500
						aa=GetSurfaceBrush(e\room\NonFreeAble[0])
						BrushTexture aa, e\room\Textures[1], 0, 1
						PaintSurface e\room\NonFreeAble[0],aa
						FreeBrush aa
					EndIf
					
					;end with that------------
					
					
					bb# = point_direction(EntityX(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True),EntityX(Collider,True),EntityZ(Collider,True))
					cc# = point_direction(EntityX(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True))
					
					
					If EntityInView(e\room\Objects[0],Camera) And EntityVisible(e\room\Objects[0],Camera) And BlinkTimer > 0 And (Not (Wearing714=1)) And (Not (WearingNightVision>0)) And Abs(angleDist(bb,cc))<95.0 Then
						If e\EventState = 0.0 Then e\EventState = 1.0
						;
					EndIf
					If e\EventState > 0.0 Then
						HeartBeatRate = 150
						HeartBeatVolume = 0.5
						If e\EventState = 1.0 Then e\SoundCHN = PlaySound_Strict(e\Sound) : ChannelVolume e\SoundCHN,1.0
						;Player saw 1074: walk towards r\Objects[1]
						tempF# = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True))
						tempF2# = EntityYaw(Collider)
						tempF3# = angleDist(tempF+90+Sin(WrapAngle(e\EventState/10)),tempF2)
						TurnEntity Collider, 0,tempF3/8,0,True
						If e\EventState < 100.0 Or e\EventState > 105.0 Then
							e\EventState = e\EventState + Min(FPSfactor,1.99)
						Else
							e\EventState2 = Min(e\EventState2+(0.01*Min(FPSfactor,1.99)),0.5) ;If e\EventState2<0.5 Then 
							ForceMove = e\EventState2
							Crouch = 0
							If point_distance(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True)) <= RoomScale*800.0 Then
								ForceAngle = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True))-tempF2+90.0
							Else
								ForceAngle = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True))-tempF2+90.0
							EndIf
							If point_distance(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\Objects[1],True),EntityZ(e\room\Objects[1],True)) < RoomScale*8.0 Then
								e\EventState = 106.0
								Playable = False
								ForceMove = 0
								ForceAngle = 0
								e\EventState2 = 0
							EndIf
						EndIf
						
						If (Not EntityVisible(e\room\Objects[0],Camera)) Then Playable=True : e\EventState = 0 : e\EventState2 = 0
						
						Select Floor(e\EventState)
							Case 250.0
								Msg = "Hey there!"
								MsgTimer = 70*5
							Case 500.0
								Msg = "If you're seeing this, post a bug report."
								MsgTimer = 70*5
							Case 700.0
								Msg = "Seriously, this SCP should not spawn." ; shouldn't --> should not why juan why --Mark
								MsgTimer = 70*5
							Case 800.0
								BlinkTimer = -2
								BlurTimer = 800
							Case 1200.0
								Msg = "This event sucks."
								MsgTimer = 70*5
							Case 1400.0
								Msg = "Thanks a lot, Juan."
								MsgTimer = 70*5
							Case 1500.0
								DeathMSG = "God DAMMIT, Juan. What were you thinking?"
								Kill()
						End Select
						If e\EventState >= 2500.0 Then
							;BlinkTimer = -20 : LightBlink = 1.0
							ChannelVolume e\SoundCHN,(Max(0,2600.0-e\EventState))/100.0
						EndIf
						If PlayerRoom <> e\room Then
							;e\EventState = 2300.0
							If PlayerRoom\RoomTemplate\Name = "pocketdimension" Then DeathMSG = "Subject D-9341 was captured and killed by SCP-106. Body was disposed of via incineration."
							Kill()
							Playable = True
						EndIf
						If KillTimer < 0 Then
							e\EventState = 2300.0
							ForceAngle = 0.0
							ForceMove = 0.0
							If e\SoundCHN <> 0 Then StopChannel e\SoundCHN : e\SoundCHN = 0
							Playable = True
							;FreeTexture r\Objects[2]
							;FreeTexture r\Objects[3]
							;FreeEntity r\Objects[0]
							;FreeEntity r\Objects[1]
							;FreeSound_Strict e\Sound
							;e\Sound = 0
						EndIf
						user_camera_pitch = user_camera_pitch-((user_camera_pitch-10.0)/8)
					Else
						If e\SoundCHN <> 0 Then StopChannel e\SoundCHN : e\SoundCHN = 0
					EndIf
					
				EndIf
				;[End Block]
			Case "room3door"
				;[Block]
				If PlayerRoom = e\room Then
					If EntityDistance(e\room\obj,Collider)<2.5 Then
						For do.doors = Each Doors
							If Abs(EntityX(do\obj,True)-EntityX(Collider))<2.0 Then
								If Abs(EntityZ(do\obj,True)-EntityZ(Collider))<2.0 Then
									If (Not EntityInView(do\obj,Camera)) Then
										If do\open Then
											DebugLog "%@@= \ {2E6C2=FD gi`h]c"
											do\open = False
											do\openstate = 0
											BlurTimer = 100
											CameraShake = 3.0											
										EndIf
										
									EndIf
									Exit
								EndIf
							EndIf
						Next
						RemoveEvent(e)
					EndIf
				EndIf
				;[End Block]
			Case "room3servers"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState3=0 And Curr173\Idle = 0 Then
						If BlinkTimer < -10 Then 
							temp = Rand(0,2)
							PositionEntity Curr173\Collider, EntityX(e\room\Objects[temp],True),EntityY(e\room\Objects[temp],True),EntityZ(e\room\Objects[temp],True)
							ResetEntity Curr173\Collider
							e\EventState3=1
						EndIf
					EndIf
					
					If e\room\Objects[3]>0 Then 
						If BlinkTimer<-8 And BlinkTimer >-12 Then
							PointEntity e\room\Objects[3], Camera
							RotateEntity(e\room\Objects[3], 0, EntityYaw(e\room\Objects[3],True),0, True)
						EndIf
						If e\EventState2 = 0 Then 
							e\EventState = CurveValue(0, e\EventState, 15.0)
							If Rand(800)=1 Then e\EventState2 = 1
						Else
							e\EventState = e\EventState+(FPSfactor*0.5)
							If e\EventState > 360 Then e\EventState = 0	
							
							If Rand(1200)=1 Then e\EventState2 = 0
						EndIf
						
						PositionEntity e\room\Objects[3], EntityX(e\room\Objects[3],True), (-608.0*RoomScale)+0.05+Sin(e\EventState+270)*0.05, EntityZ(e\room\Objects[3],True), True
					EndIf
				EndIf
				;[End Block]
			Case "room3storage"
				;[Block]
				If PlayerRoom = e\room Then
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[0],e\room\Objects[1], e)
					
					e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[2],e\room\Objects[3], e)
					
					If EntityY(Collider)<-4600*RoomScale Then
						
						GiveAchievement(Achv939)
						
						ShouldPlay = 7
						
						If e\room\NPC[0]=Null Or e\room\NPC[1]=Null Or e\room\NPC[2]=Null Then
							If QuickLoadPercent = -1 Then
								QuickLoadPercent = 0
								QuickLoad_CurrEvent = e
							EndIf
						Else
							If e\EventState = 0 Then
								;Instance 1
								PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True)+0.2,EntityZ(e\room\Objects[4],True))
								ResetEntity e\room\NPC[0]\Collider
								e\room\NPC[0]\State = 2
								e\room\NPC[0]\State2 = 5
								e\room\NPC[0]\PrevState = 7
								;Instance 2
								PositionEntity(e\room\NPC[1]\Collider, EntityX(e\room\Objects[9],True),EntityY(e\room\Objects[9],True)+0.2,EntityZ(e\room\Objects[9],True))
								ResetEntity e\room\NPC[1]\Collider
								e\room\NPC[1]\State = 2
								e\room\NPC[1]\State2 = 10
								e\room\NPC[1]\PrevState = 12
								;Instance 3
								PositionEntity(e\room\NPC[2]\Collider, EntityX(e\room\Objects[13],True),EntityY(e\room\Objects[13],True)+0.2,EntityZ(e\room\Objects[13],True))
								ResetEntity e\room\NPC[2]\Collider
								e\room\NPC[2]\State = 2
								e\room\NPC[2]\State2 = 14
								e\room\NPC[2]\PrevState = 16
								;Other
								e\EventState = 1
							EndIf
							
							If e\room\RoomDoors[4]\open = False
								If UpdateLever(e\room\Levers[0])
									e\room\RoomDoors[4]\open = True
									If e\Sound2 <> 0 Then FreeSound_Strict e\Sound2 : e\Sound2=0
									e\Sound2 = LoadSound_Strict("SFX\Door\Door2Open1_dist.ogg")
									e\SoundCHN2 = PlaySound2(e\Sound2,Camera,e\room\RoomDoors[4]\obj,400)
								EndIf
								If UpdateLever(e\room\Levers[1])
									e\room\RoomDoors[4]\open = True
									If e\Sound2 <> 0 Then FreeSound_Strict e\Sound2 : e\Sound2=0
									e\Sound2 = LoadSound_Strict("SFX\Door\Door2Open1_dist.ogg")
									e\SoundCHN2 = PlaySound2(e\Sound2,Camera,e\room\RoomDoors[4]\obj,400)
								EndIf
							EndIf
							
							UpdateLever(e\room\Levers[0],e\room\RoomDoors[4]\open)
							UpdateLever(e\room\Levers[1],e\room\RoomDoors[4]\open)
							
							e\room\NPC[0]\IgnorePlayer = False
							e\room\NPC[2]\IgnorePlayer = False
							
							CurrTrigger$ = CheckTriggers()
							
							Select CurrTrigger$
								Case "939-1_fix"
									e\room\NPC[0]\IgnorePlayer = True
								Case "939-3_fix"
									e\room\NPC[2]\IgnorePlayer = True
							End Select
							
							If ChannelPlaying(e\SoundCHN2)
								UpdateSoundOrigin(e\SoundCHN2,Camera,e\room\RoomDoors[4]\obj,400)
							EndIf
							
							PlayerFallingPickDistance = 0.0
							
							If EntityY(Collider)<-6400*RoomScale And KillTimer >= 0 And FallTimer >= 0 Then
								DeathMSG=""
								PlaySound_Strict LoadTempSound("SFX\Room\PocketDimension\Impact.ogg")
								KillTimer=-1.0
							EndIf
						EndIf
					Else
						e\EventState = 0
						If e\room\NPC[0]<>Null Then e\room\NPC[0]\State = 66
						If e\room\NPC[1]<>Null Then e\room\NPC[1]\State = 66
						If e\room\NPC[2]<>Null Then e\room\NPC[2]\State = 66
					EndIf
				Else
					If e\room\NPC[0]<>Null Then e\room\NPC[0]\State = 66
					If e\room\NPC[1]<>Null Then e\room\NPC[1]\State = 66
					If e\room\NPC[2]<>Null Then e\room\NPC[2]\State = 66
				EndIf 
				;[End Block]
			Case "room3tunnel"
				;[Block]
				If e\EventState = 0 Then
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True)+0.5, EntityZ(e\room\Objects[0],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)+Rnd(-20,20),0, True
					SetNPCFrame (e\room\NPC[0], 288)
					e\room\NPC[0]\State = 8
					
					e\EventState = 1
					RemoveEvent(e)
				EndIf
				;[End Block]
			Case "room4"
				;[Block]
				If e\EventState < MilliSecs2() Then
					If PlayerRoom <> e\room Then
						If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\obj),EntityZ(e\room\obj))<16.0 Then
							For n.NPCs = Each NPCs
								If n\NPCtype = NPCtype049 Then
									If n\State = 2 And EntityDistance(Collider,n\Collider)>16.0 Then
										TFormVector(368, 528, 176, e\room\obj, 0)
										PositionEntity n\Collider, EntityX(e\room\obj)+TFormedX(), TFormedY(), EntityZ(e\room\obj)+TFormedZ()
										DebugLog TFormedX()+", "+ TFormedY()+", "+ TFormedZ()
										ResetEntity n\Collider
										n\PathStatus = 0
										n\State = 4
										n\State2 = 0
										n\State3 = 0
										RemoveEvent(e)
									EndIf
									Exit
								EndIf
							Next
						EndIf
					EndIf
					If e<>Null Then e\EventState = MilliSecs2()+5000
				EndIf
				;[End Block]
			Case "room012"
				;[Block]
				If PlayerRoom = e\room Then
					
					If e\EventState=0 Then
						If EntityDistance(Collider, e\room\RoomDoors[0]\obj)<2.5 And RemoteDoorOn Then
							GiveAchievement(Achv012)
							PlaySound_Strict HorrorSFX(7)
							PlaySound2 (LeverSFX,Camera,e\room\RoomDoors[0]\obj) 
							e\EventState=1
							e\room\RoomDoors[0]\locked = False
							UseDoor(e\room\RoomDoors[0],False)
							e\room\RoomDoors[0]\locked = True
						EndIf
					Else
						
						If e\Sound=0 Then LoadEventSound(e,"SFX\Music\012Golgotha.ogg")
						e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\Objects[3], 5.0)
						
						If e\Sound2=0 Then LoadEventSound(e,"SFX\Music\012.ogg",1)
						
						If e\EventState<90 Then e\EventState=CurveValue(90,e\EventState,500)
						PositionEntity e\room\Objects[2], EntityX(e\room\Objects[2],True),(-130-448*Sin(e\EventState))*RoomScale,EntityZ(e\room\Objects[2],True),True
						
						If e\EventState2 > 0 And e\EventState2 < 200 Then
							e\EventState2 = e\EventState2 + FPSfactor
							RotateEntity(e\room\Objects[1], CurveValue(85, EntityPitch(e\room\Objects[1]), 5), EntityYaw(e\room\Objects[1]), 0)
						Else
							e\EventState2 = e\EventState2 + FPSfactor
							If e\EventState2<250 Then
								ShowEntity e\room\Objects[3] 
							Else
								HideEntity e\room\Objects[3] 
								If e\EventState2>300 Then e\EventState2=200
							EndIf
						EndIf
						
						If Wearing714=False And WearingGasMask<3 And WearingHazmat<3 Then
							;temp = False
							If EntityVisible(e\room\Objects[2],Camera) Then 							
							;012 not visible, walk to the door														
								
								
							;DebugLog "WHERE IS IT?!"	
								e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\Objects[3], 10, e\EventState3/(86.0*70.0))
								
								pvt% = CreatePivot()
								PositionEntity pvt, EntityX(Camera), EntityY(e\room\Objects[2],True)-0.05, EntityZ(Camera)
								PointEntity(pvt, e\room\Objects[2])
								RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 80-(e\EventState3/200.0)), 0)
								
								TurnEntity(pvt, 90, 0, 0)
								user_camera_pitch = CurveAngle(EntityPitch(pvt)+25, user_camera_pitch + 90.0, 80-(e\EventState3/200.0))
								user_camera_pitch=user_camera_pitch-90
								
								dist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[2],True),EntityZ(e\room\Objects[2],True))
								
								HeartBeatRate = 150
								HeartBeatVolume = Max(3.0-dist,0.0)/3.0
								BlurVolume = Max((2.0-dist)*(e\EventState3/800.0)*(Sin(Float(MilliSecs2()) / 20.0 + 1.0)),BlurVolume)
								CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2()) / 20.0)+1.0)*8.0*Max((3.0-dist),0.0))
								
								If BreathCHN <> 0 Then
									If ChannelPlaying(BreathCHN) Then StopChannel(BreathCHN)
								EndIf
								
								If dist < 0.6 Then
									e\EventState3=Min(e\EventState3+FPSfactor,86*70)
									If e\EventState3>70 And e\EventState3-FPSfactor=<70 Then
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech1.ogg")
									ElseIf e\EventState3>13*70 And e\EventState3-FPSfactor=<13*70
										Msg="You start pushing your nails into your wrist, drawing blood."
										MsgTimer = 7*70
										Injuries=Injuries+0.5
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech2.ogg")
									ElseIf e\EventState3>31*70 And e\EventState3-FPSfactor=<31*70
										tex = LoadTexture_Strict("GFX\map\scp-012_1.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)
										FreeTexture tex
										
										Msg="You tear open your left wrist and start writing on the composition with your blood."
										MsgTimer = 7*70
										Injuries=Max(Injuries,1.5)
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech"+Rand(3,4)+".ogg")
									ElseIf e\EventState3>49*70 And e\EventState3-FPSfactor=<49*70
										Msg="You push your fingers deeper into the wound."
										MsgTimer = 8*70
										Injuries=Injuries+0.3
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech5.ogg")
									ElseIf e\EventState3>63*70 And e\EventState3-FPSfactor=<63*70
										tex = LoadTexture_Strict("GFX\map\scp-012_2.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)	
										FreeTexture tex
										
										Injuries=Injuries+0.5
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech6.ogg")
									ElseIf e\EventState3>74*70 And e\EventState3-FPSfactor=<74*70
										tex = LoadTexture_Strict("GFX\map\scp-012_3.jpg")
										EntityTexture (e\room\Objects[4], tex,0,1)
										FreeTexture tex
										
										Msg="You rip the wound wide open. Grabbing scoops of blood pouring out."
										MsgTimer = 7*70
										Injuries=Injuries+0.8
										PlaySound_Strict LoadTempSound("SFX\SCP\012\Speech7.ogg")
										Crouch = True
										
										de.Decals = CreateDecal(17,  EntityX(Collider), -768*RoomScale+0.01, EntityZ(Collider),90,Rnd(360),0)
										de\Size = 0.1 : de\maxsize = 0.45 : de\sizechange = 0.0002 : UpdateDecals()
									ElseIf e\EventState3>85*70 And e\EventState3-FPSfactor=<85*70	
										DeathMSG = "Subject D-9341 found in a pool of blood next to SCP-012. Subject seems to have ripped open his wrists and written three extra "
										DeathMSG = DeathMSG + "lines to the composition before dying of blood loss."
										Kill()
									EndIf
									
									RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(Collider)+Sin(e\EventState3*(e\EventState3/2000))*(e\EventState3/300), EntityYaw(Collider), 80), 0)
									
								Else
									angle = WrapAngle(EntityYaw(pvt)-EntityYaw(Collider))
									If angle<40.0 Then
										ForceMove = (40.0-angle)*0.02
									ElseIf angle > 310.0
										ForceMove = (40.0-Abs(360.0-angle))*0.02
									EndIf
								EndIf								
								
								FreeEntity pvt								
							Else
								
							;	If EntityY(Collider)=-768.0*RoomScale Then
								If (Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\RoomDoors[0]\frameobj), EntityZ(e\room\RoomDoors[0]\frameobj))<4.5) And  EntityY(Collider)<-2.5 Then
									pvt% = CreatePivot()
									PositionEntity pvt, EntityX(Camera), EntityY(Collider), EntityZ(Camera)
									PointEntity(pvt, e\room\RoomDoors[0]\frameobj)
									;TurnEntity(pvt, 90, 0, 0)
									user_camera_pitch = CurveAngle(90, user_camera_pitch+90, 100)
									user_camera_pitch=user_camera_pitch-90
									RotateEntity(Collider, EntityPitch(Collider), CurveAngle(EntityYaw(pvt), EntityYaw(Collider), 150), 0)
									
									angle = WrapAngle(EntityYaw(pvt)-EntityYaw(Collider))
									If angle<40.0 Then
										ForceMove = (40.0-angle)*0.008
									ElseIf angle > 310.0
										ForceMove = (40.0-Abs(360.0-angle))*0.008
									EndIf
									FreeEntity pvt	
									
								EndIf
								DebugLog Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\RoomDoors[0]\frameobj), EntityZ(e\room\RoomDoors[0]\frameobj))
								DebugLog EntityY(Collider)
							EndIf
							
						EndIf
						
					EndIf
				EndIf
				;[End Block]
			Case "room035"
				;[Block]
				If PlayerRoom = e\room Then
					;eventstate2 = has 035 told the code to the storage room (true/false)
					;eventstate3 = has the player opened the gas valves (0=no, 0<x<35*70 yes, x>35*70 the host has died)
					
					If e\EventState = 0 Then
						If EntityDistance(Collider, e\room\Objects[3])<2 Then
							n.NPCs = CreateNPC(NPCtypeD, EntityX(e\room\Objects[4],True),0.5,EntityZ(e\room\Objects[4],True))
							
							n\texture = "GFX\NPCs\035victim.jpg"
							n\Model = "GFX\NPCs\035.b3d"
							HideEntity n\obj
							
							SetAnimTime(n\obj, 501)
							n\Frame = 501
							
							n\State = 6
							
							e\EventState=1
						EndIf
						
					ElseIf e\EventState > 0
						If e\room\NPC[0]=Null Then
							For n.NPCs = Each NPCs
								If n\texture = "GFX\NPCs\035victim.jpg" Then
									e\room\NPC[0]=n
									
									temp = e\room\NPC[0]\Frame
									
									FreeEntity e\room\NPC[0]\obj
									e\room\NPC[0]\obj = LoadAnimMesh_Strict("GFX\NPCs\035.b3d")
									x = 0.5 / MeshWidth(e\room\NPC[0]\obj)
									e\room\NPC[0]\ModelScaleX = x
									e\room\NPC[0]\ModelScaleY = x
									e\room\NPC[0]\ModelScaleZ = x
									ScaleEntity e\room\NPC[0]\obj, x,x,x
									SetAnimTime(e\room\NPC[0]\obj, temp)
									ShowEntity e\room\NPC[0]\obj
									
									RotateEntity n\Collider, 0, e\room\angle+270, 0, True
									
									Exit
								EndIf
							Next
						EndIf
						
						If e\room\NPC[0]\SoundChn <> 0 Then
							If ChannelPlaying(e\room\NPC[0]\SoundChn) Then
								e\room\NPC[0]\SoundChn=LoopSound2(e\room\NPC[0]\Sound, e\room\NPC[0]\SoundChn, Camera, e\room\obj, 6.0)
							EndIf
						EndIf
						
						If e\EventState=1 Then
							If EntityDistance(Collider, e\room\Objects[3])<1.2 
								If EntityInView(e\room\NPC[0]\obj, Camera) Then
									GiveAchievement(Achv035)
									PlaySound_Strict(LoadTempSound("SFX\SCP\035\GetUp.ogg"))
									e\EventState = 1.5
								EndIf
							EndIf
						Else
							
							If e\room\RoomDoors[3]\open Then e\EventState2 = Max(e\EventState2, 1)
							
							;the door is closed
							If UpdateLever(e\room\Levers[0],(e\EventState2=20)) = 0 Then
								;the gas valves are open
								temp = UpdateLever(e\room\Levers[1],False)
								If temp Or (e\EventState3>25*70 And e\EventState3<50*70) Then 
									If temp Then 
										PositionEntity(e\room\Objects[5], EntityX(e\room\Objects[5],True), 424.0*RoomScale, EntityZ(e\room\Objects[5],True),True)
										PositionEntity(e\room\Objects[6], EntityX(e\room\Objects[6],True), 424.0*RoomScale, EntityZ(e\room\Objects[6],True),True)
									Else
										PositionEntity(e\room\Objects[5], EntityX(e\room\Objects[5],True), 10, EntityZ(e\room\Objects[5],True),True)
										PositionEntity(e\room\Objects[6], EntityX(e\room\Objects[6],True), 10, EntityZ(e\room\Objects[6],True),True)
										
									EndIf
									
									If e\EventState3 >-30*70 Then 
										e\EventState3=Abs(e\EventState3)+FPSfactor
										If e\EventState3 > 1 And e\EventState3-FPSfactor=<1 Then
											e\room\NPC[0]\State = 0
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Gased1.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
										ElseIf e\EventState3>15*70 And e\EventState3<25*70
											If e\EventState3-FPSfactor=<15*70 Then
												If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Gased2.ogg")
												e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												SetNPCFrame( e\room\NPC[0],553)
											EndIf
											e\room\NPC[0]\State = 6
											
											AnimateNPC(e\room\NPC[0], 553, 529, -0.12, False)
										ElseIf e\EventState3>25*70 And e\EventState3<35*70
											e\room\NPC[0]\State = 6
											AnimateNPC(e\room\NPC[0], 529, 524, -0.08, False)
										ElseIf e\EventState3>35*70
											If e\room\NPC[0]\State = 6 Then
												Sanity = -150*Sin(AnimTime(e\room\NPC[0]\obj)-524)*9
												AnimateNPC(e\room\NPC[0], 524, 553, 0.08, False)
												If e\room\NPC[0]\Frame=553 Then e\room\NPC[0]\State = 0
											EndIf
											
											If e\EventState3-FPSfactor=<35*70 Then 
												If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedKilled1.ogg")
												e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												PlaySound_Strict(LoadTempSound("SFX\SCP\035\KilledGetUp.ogg"))
												e\EventState = 60*70
											EndIf
										EndIf
									EndIf
								Else ;gas valves closed
									
									If e\room\NPC[0]\State = 6 Then
										If e\room\NPC[0]\Frame>=501 And e\room\NPC[0]\Frame<=523 Then
											e\room\NPC[0]\Frame = Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 501, 523, 0.08, False)
											If e\room\NPC[0]\Frame=523 Then e\room\NPC[0]\State = 0
										EndIf	
										
										If e\room\NPC[0]\Frame>=524 And e\room\NPC[0]\Frame<=553 Then
											e\room\NPC[0]\Frame = Animate2(e\room\NPC[0]\obj, AnimTime(e\room\NPC[0]\obj), 524, 553, 0.08, False)
											If e\room\NPC[0]\Frame=553 Then e\room\NPC[0]\State = 0
										EndIf	
									EndIf
									
									PositionEntity(e\room\Objects[5], EntityX(e\room\Objects[5],True), 10, EntityZ(e\room\Objects[5],True),True)
									PositionEntity(e\room\Objects[6], EntityX(e\room\Objects[6],True), 10, EntityZ(e\room\Objects[6],True),True)
									
									If e\room\NPC[0]\State = 0 Then
										PointEntity e\room\NPC[0]\obj, Collider
										RotateEntity e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj), EntityYaw(e\room\NPC[0]\Collider), 15.0), 0
										
										If Rand(500)=1 Then
											If EntityDistance(e\room\NPC[0]\Collider, e\room\Objects[4])>2 Then
												e\room\NPC[0]\State2 = 1
											Else
												e\room\NPC[0]\State2 = 0
											EndIf
											e\room\NPC[0]\State = 1
										EndIf
									ElseIf e\room\NPC[0]\State = 1
										If e\room\NPC[0]\State2 = 1 Then
											PointEntity e\room\NPC[0]\obj, e\room\Objects[4]
											If EntityDistance(e\room\NPC[0]\Collider, e\room\Objects[4])<0.2 Then e\room\NPC[0]\State = 0
										Else
											RotateEntity e\room\NPC[0]\obj, 0, e\room\angle-180, 0, True
											If EntityDistance(e\room\NPC[0]\Collider, e\room\Objects[4])>2 Then e\room\NPC[0]\State = 0
										EndIf
										
										RotateEntity e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj), EntityYaw(e\room\NPC[0]\Collider), 15.0), 0
										
									EndIf
									
									If e\EventState3 > 0 Then
										e\EventState3=-e\EventState3
										If e\EventState3<-35*70 Then ;the host is dead
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedKilled2.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
											e\EventState = 60*70
										Else 
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											If e\EventState3<-20*70 Then
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedStop2.ogg")
											Else
												e\EventState3=-21*70
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedStop1.ogg")
											EndIf
											
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
											e\EventState = 61*70
										EndIf
									Else
										
										e\EventState = e\EventState+FPSfactor
										If e\EventState > 4*70 And e\EventState-FPSfactor =<4*70 Then
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Help1.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
											e\EventState = 10*70
										ElseIf e\EventState > 20*70 And e\EventState-FPSfactor =<20*70
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Help2.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
										ElseIf e\EventState > 40*70 And e\EventState-FPSfactor =<40*70
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle1.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
										ElseIf e\EventState > 50*70 And e\EventState-FPSfactor =<50*70
											If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
											e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle2.ogg")
											e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
										ElseIf e\EventState > 80*70 And e\EventState-FPSfactor =<80*70
											If e\EventState2 Then ;skip the closet part if player has already opened it
												e\EventState = 130*70
											Else
												If e\EventState3<-30*70 Then ;the host is dead
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedCloset.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												ElseIf e\EventState3 = 0 ;the gas valves haven't been opened
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Closet1.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												Else ;gas valves have been opened but 035 isn't dead
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedCloset.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												EndIf												
											EndIf
										ElseIf e\EventState > 80*70
											If e\EventState2 Then e\EventState = Max(e\EventState,100*70)
											If e\EventState>110*70 And e\EventState-FPSfactor =<110*70 Then
												If e\EventState2 Then
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Closet2.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
													e\EventState = 130*70
												Else
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle3.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												EndIf
											ElseIf e\EventState>125*70 And e\EventState-FPSfactor =<125*70
												If e\EventState2 Then
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Closet2.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												Else
													If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
													e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle4.ogg")
													e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												EndIf
											ElseIf e\EventState>150*70 And e\EventState-FPSfactor =<150*70
												If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle5.ogg")
												e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
											ElseIf e\EventState>200*70 And e\EventState-FPSfactor =<200*70
												If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
												e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Idle6.ogg")
												e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
												
											EndIf
										EndIf
										
									EndIf
									
								EndIf								
								
							Else ;the player has opened the door
								If e\EventState2 < 10 Then
									e\room\RoomDoors[2]\open = False
									e\room\RoomDoors[2]\locked = True
									
									If e\room\RoomDoors[1]\open = False Then 
										e\room\RoomDoors[0]\locked = False
										e\room\RoomDoors[1]\locked = False
										UseDoor(e\room\RoomDoors[1])
										e\room\RoomDoors[0]\locked = True
										e\room\RoomDoors[1]\locked = True
										
									EndIf
									
									If e\EventState3=0 Then
										If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
										e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\Escape.ogg")
										e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
									ElseIf Abs(e\EventState3)>35*70
										If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
										e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\KilledEscape.ogg")
										e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
									Else
										If e\room\NPC[0]\Sound<>0 Then FreeSound_Strict(e\room\NPC[0]\Sound) : e\room\NPC[0]\Sound = 0
										e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\035\GasedEscape.ogg")
										e\room\NPC[0]\SoundChn = PlaySound_Strict(e\room\NPC[0]\Sound)
									EndIf
									e\EventState2 = 20
								EndIf
								
								If e\EventState2 = 20 Then
									dist = EntityDistance(e\room\RoomDoors[0]\frameobj, e\room\NPC[0]\Collider)
									
									e\room\NPC[0]\State = 1
									If dist > 2.5 Then
										PointEntity e\room\NPC[0]\obj, e\room\RoomDoors[1]\frameobj
										RotateEntity e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj), EntityYaw(e\room\NPC[0]\Collider), 15.0), 0
									ElseIf dist > 0.7
										If ChannelPlaying (e\room\NPC[0]\SoundChn) Then
											e\room\NPC[0]\State = 0
											PointEntity e\room\NPC[0]\obj, Collider
											RotateEntity e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj), EntityYaw(e\room\NPC[0]\Collider), 15.0), 0
										Else
											PointEntity e\room\NPC[0]\obj, e\room\RoomDoors[0]\frameobj
											RotateEntity e\room\NPC[0]\Collider, 0, CurveAngle(EntityYaw(e\room\NPC[0]\obj), EntityYaw(e\room\NPC[0]\Collider), 15.0), 0
										EndIf
									Else
										RemoveNPC(e\room\NPC[0])
										e\room\NPC[0]=Null
										e\EventState = -1
										e\EventState2 = 0
										e\EventState3 = 0
										e\room\RoomDoors[0]\locked = False										
										e\room\RoomDoors[1]\locked = False
										e\room\RoomDoors[2]\locked = False
										UseDoor(e\room\RoomDoors[1],False)
										For do.doors = Each Doors
											If do\dir = 2 Then
												If Abs(EntityX(e\room\obj)-EntityX(do\frameobj,True))<4.5 Then 
													If Abs(EntityZ(e\room\obj)-EntityZ(do\frameobj,True))<4.5 Then 
														UseDoor(do,False)
														Exit
													EndIf
												EndIf
											EndIf
										Next
									EndIf
								EndIf
								
							EndIf
							
						EndIf ;eventstate > 1
						
					Else ;eventstate < 0 (SCP-035 has left)
						
						If UpdateLever(e\room\Levers[1],False) Then 
							PositionEntity(e\room\Objects[5], EntityX(e\room\Objects[5],True), 424.0*RoomScale, EntityZ(e\room\Objects[5],True),True)
							PositionEntity(e\room\Objects[6], EntityX(e\room\Objects[6],True), 424.0*RoomScale, EntityZ(e\room\Objects[6],True),True)
						Else
							PositionEntity(e\room\Objects[5], EntityX(e\room\Objects[5],True), 10, EntityZ(e\room\Objects[5],True),True)
							PositionEntity(e\room\Objects[6], EntityX(e\room\Objects[6],True), 10, EntityZ(e\room\Objects[6],True),True)
						EndIf
						
						;If UpdateLever(e\room\Levers[0]) Then
						;	If e\room\RoomDoors[0]\open = True Then UseDoor(e\room\RoomDoors[1])
						;Else
						;	If e\room\RoomDoors[0]\open = False Then UseDoor(e\room\RoomDoors[1])
						;EndIf
						
						temp = False
						
						;player is inside the containment chamber
						If EntityX(Collider)>Min(EntityX(e\room\Objects[7],True),EntityX(e\room\Objects[8],True)) Then
							If EntityX(Collider)<Max(EntityX(e\room\Objects[7],True),EntityX(e\room\Objects[8],True)) Then
								If EntityZ(Collider)>Min(EntityZ(e\room\Objects[7],True),EntityZ(e\room\Objects[8],True)) Then
									If EntityZ(Collider)<Max(EntityZ(e\room\Objects[7],True),EntityZ(e\room\Objects[8],True)) Then
										
										ShouldPlay = 0
										
										If e\room\NPC[0]=Null Then
											If e\room\NPC[0]=Null Then e\room\NPC[0] = CreateNPC(NPCtypeTentacle, 0,0,0)
										EndIf
										
										PositionEntity e\room\NPC[0]\Collider, EntityX(e\room\Objects[4],True), 0, EntityZ(e\room\Objects[4],True)
										
										If e\room\NPC[0]\State > 0 Then 
											If e\room\NPC[1]=Null Then
												If e\room\NPC[1]=Null Then e\room\NPC[1] = CreateNPC(NPCtypeTentacle, 0,0,0)
											EndIf
										EndIf
										
										Stamina = CurveValue(Min(60,Stamina), Stamina, 20.0)
										
										temp = True
										
										If e\Sound = 0 Then LoadEventSound(e,"SFX\Room\035Chamber\Whispers1.ogg")
										If e\Sound2 = 0 Then LoadEventSound(e,"SFX\Room\035Chamber\Whispers2.ogg",1)
										
										e\EventState2 = Min(e\EventState2+(FPSfactor/6000),1.0)
										e\EventState3 = CurveValue(e\EventState2, e\EventState3, 50)
										
										If (Not Wearing714) And WearingHazmat<3 And WearingGasMask<3 Then
											Sanity=Sanity-FPSfactor*1.1
											BlurTimer = Sin(MilliSecs2()/10)*Abs(Sanity)
										EndIf
										
										If (Not WearingHazmat) Then
											Injuries = Injuries + (FPSfactor/5000)
										Else
											Injuries = Injuries + (FPSfactor/10000)
										EndIf
										
										If KillTimer < 0 And Bloodloss =>100 Then
											DeathMSG = "Class D Subject D-9341 found dead inside SCP-035's containment chamber. "
											DeathMSG = DeathMSG + "The subject exhibits heavy hemorrhaging of blood vessels around the eyes and inside the mouth and nose. "
											DeathMSG = DeathMSG + "Sent for autopsy."
										EndIf
									EndIf
								EndIf
							EndIf
						EndIf
						
						If e\room\NPC[1]<>Null Then 
							PositionEntity e\room\NPC[1]\Collider, EntityX(e\room\obj,True), 0, EntityZ(e\room\obj,True)
							angle = WrapAngle(EntityYaw(e\room\NPC[1]\Collider)-e\room\angle)
							
							If angle>90 Then 
								If angle < 225 Then 
									RotateEntity e\room\NPC[1]\Collider, 0, e\room\angle-89-180, 0
								Else
									RotateEntity e\room\NPC[1]\Collider, 0, e\room\angle-1, 0	
								EndIf
							EndIf
						EndIf
						
						If temp = False Then 
							e\EventState2 = Max(e\EventState2-(FPSfactor/2000),0)
							e\EventState3 = Max(e\EventState3-(FPSfactor/100),0)
						EndIf
						
						If e\EventState3 > 0 And (Not Wearing714) And WearingHazmat<3 And WearingGasMask<3 Then 
							e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\obj, 10, e\EventState3)
							e\SoundCHN2 = LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\obj, 10, (e\EventState3-0.5)*2)
						EndIf
						
					EndIf
					
				Else	
					If e\EventState=0 Then	
						If e\Sound = 0 Then
							If EntityDistance(Collider, e\room\obj) < 20 Then
								LoadEventSound(e,"SFX\Room\035Chamber\InProximity.ogg")
								PlaySound_Strict e\Sound
							EndIf
						EndIf
					ElseIf e\EventState < 0
						For i = 0 To 1
							If e\room\NPC[i]<>Null Then 
								RemoveNPC(e\room\NPC[i])
								e\room\NPC[i]=Null
							EndIf						
						Next						
					EndIf
					
				EndIf
				;[End Block]
			Case "room049"
				;[Block]
				If PlayerRoom = e\room Then
					If EntityY(Collider) > -2848*RoomScale Then
						e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[0],e\room\Objects[1], e)
						e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[2],e\room\Objects[3], e)
					Else
						
						ShouldPlay = 25
						
						If e\EventState = 0 Then
							If e\EventStr = "" And QuickLoadPercent = -1
								QuickLoadPercent = 0
								QuickLoad_CurrEvent = e
								e\EventStr = "load0"
							EndIf
							PlaySound_Strict LoadTempSound("SFX\Room\Blackout.ogg")
							If EntityDistance(e\room\Objects[11],Collider)<EntityDistance(e\room\Objects[12],Collider) Then
								it = CreateItem("Research Sector-02 Scheme", "paper", EntityX(e\room\Objects[11],True),EntityY(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True))
								EntityType it\collider,HIT_ITEM
							Else
								it = CreateItem("Research Sector-02 Scheme", "paper", EntityX(e\room\Objects[12],True),EntityY(e\room\Objects[12],True),EntityZ(e\room\Objects[12],True))
								EntityType it\collider,HIT_ITEM
							EndIf
						ElseIf e\EventState > 0
							
							Local prevGenLever
							If EntityPitch(e\room\Objects[9],True) > 0 Then
								prevGenLever = True
							Else
								prevGenLever = False
							EndIf
							temp = Not UpdateLever(e\room\Objects[7]) ;power feed
							x = UpdateLever(e\room\Objects[9]) ;generator
							
							e\room\RoomDoors[1]\locked = True
							e\room\RoomDoors[3]\locked = True
							e\room\RoomDoors[1]\IsElevatorDoor = 0
							e\room\RoomDoors[3]\IsElevatorDoor = 0
							
							If (prevGenLever <> x) Then
								If x=False Then
									PlaySound_Strict LightSFX
								Else
									PlaySound_Strict TeslaPowerUpSFX
								EndIf
							EndIf
							
							If e\EventState >= 70 Then
								If x Then
									ShouldPlay = 8
									e\EventState = Max(e\EventState,70*180)
									SecondaryLightOn = CurveValue(1.0, SecondaryLightOn, 10.0)
									If e\Sound2=0 Then LoadEventSound(e,"SFX\Ambient\Room ambience\fuelpump.ogg",1)
									e\SoundCHN2=LoopSound2(e\Sound2, e\SoundCHN2, Camera, e\room\Objects[10], 6.0)
									For i = 4 To 6
										e\room\RoomDoors[i]\locked = False
									Next
								Else
									SecondaryLightOn = CurveValue(0.0, SecondaryLightOn, 10.0)
									If ChannelPlaying(e\SoundCHN2) Then
										StopChannel(e\SoundCHN2)
									EndIf
									For i = 4 To 6
										e\room\RoomDoors[i]\locked = True
									Next
								EndIf
							Else
								e\EventState = Min(e\EventState+FPSfactor,70)
							EndIf
							
							If temp And x Then
								e\room\RoomDoors[1]\locked = False
								e\room\RoomDoors[3]\locked = False
								e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[0],e\room\Objects[1], e)
								e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[2],e\room\Objects[3], e)
								
								If e\room\NPC[0]\Idle > 0
									i = 0
									If EntityDistance(Collider,e\room\RoomDoors[1]\frameobj)<3.0
										i = 1
									ElseIf EntityDistance(Collider,e\room\RoomDoors[3]\frameobj)<3.0
										i = 3
									EndIf
									If i > 0
										PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\Objects[i],True),EntityY(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)
										ResetEntity e\room\NPC[0]\Collider
										PlaySound2(ElevatorBeepSFX, Camera, e\room\Objects[i], 4.0)
										e\room\RoomDoors[i]\locked = False
										UseDoor(e\room\RoomDoors[i],False,True)
										e\room\RoomDoors[i-1]\open = False
										e\room\RoomDoors[i]\open = True
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(Collider),EntityY(Collider),EntityZ(Collider))
										If e\room\NPC[0]\Sound2 <> 0 Then FreeSound_Strict(e\room\NPC[0]\Sound2)
										e\room\NPC[0]\Sound2 = LoadSound_Strict("SFX\SCP\049\DetectedInChamber.ogg")
										e\room\NPC[0]\SoundChn2 = LoopSound2(e\room\NPC[0]\Sound2,e\room\NPC[0]\SoundChn2,Camera,e\room\NPC[0]\obj)
										e\room\NPC[0]\Idle = 0
										e\room\NPC[0]\HideFromNVG = False
										e\room\NPC[0]\PrevState = 2
										e\room\NPC[0]\State = 2
									EndIf
								EndIf
							EndIf
							
							If e\EventState < 70*190 Then
								If e\EventState >= 70*180 Then
									e\room\RoomDoors[1]\open = False
									e\room\RoomDoors[3]\open = False
									e\room\RoomDoors[0]\open = True
									e\room\RoomDoors[2]\open = True
									
									e\EventState= 70*190
								EndIf
							ElseIf e\EventState < 70*240
								For n.NPCs = Each NPCs ;awake the zombies
									If n\NPCtype = NPCtypeZombie And n\State = 0 Then
										n\State = 1
										SetNPCFrame(n, 155)
									EndIf
								Next
								e\EventState=70*241
							EndIf
						EndIf
					EndIf
				Else
					e\EventState2 = UpdateElevators(e\EventState2, e\room\RoomDoors[0], e\room\RoomDoors[1],e\room\Objects[0],e\room\Objects[1], e)
					e\EventState3 = UpdateElevators(e\EventState3, e\room\RoomDoors[2], e\room\RoomDoors[3],e\room\Objects[2],e\room\Objects[3], e)
				EndIf 
				
				If e\EventState < 0 Then
					If e\EventState > -70*4 Then
						Infect = 0
						If FallTimer => 0 Then 
							FallTimer = Min(-1, FallTimer)
							PositionEntity(Head, EntityX(Camera, True), EntityY(Camera, True), EntityZ(Camera, True), True)
							ResetEntity (Head)
							RotateEntity(Head, 0, EntityYaw(Camera) + Rand(-45, 45), 0)
						ElseIf FallTimer < -230
							FallTimer = -231
							BlinkTimer = 0
							e\EventState = e\EventState-FPSfactor
							
							If e\EventState =< -70*4 Then 
								UpdateDoorsTimer = 0
								UpdateDoors()
								UpdateRooms()
								ShowEntity Collider
								DropSpeed = 0
								BlinkTimer = -10
								FallTimer = 0
								PositionEntity Collider, EntityX(e\room\obj,True), EntityY(e\room\Objects[5],True)+0.2, EntityZ(e\room\obj,True)
								ResetEntity Collider										
								
								PositionEntity e\room\NPC[0]\Collider, EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True),True
								ResetEntity e\room\NPC[0]\Collider
								
								For n.NPCs = Each NPCs
									If n\NPCtype = NPCtypeZombie Then
										PositionEntity n\Collider, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True),True
										ResetEntity n\Collider
										n\State = 4
										DebugLog "moving zombie"
									EndIf
								Next
								
								n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[5],True), EntityY(e\room\Objects[5],True)+0.2, EntityZ(e\room\Objects[5],True))
								n\State = 6
								n\Reload = 6*70
								PointEntity n\Collider,Collider
								e\room\NPC[1] = n
								
								n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[5],True), EntityY(e\room\Objects[5],True)+0.2, EntityZ(e\room\Objects[5],True))
								n\State = 6
								n\Reload = (6*70)+Rnd(15,30)
								RotateEntity n\Collider,0,EntityYaw(e\room\NPC[1]\Collider),0
								MoveEntity n\Collider,0.5,0,0
								PointEntity n\Collider,Collider
								
								n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[5],True), EntityY(e\room\Objects[5],True)+0.2, EntityZ(e\room\Objects[5],True))
								n\State = 6
								n\Reload = 6*70+Rnd(15,30)
								RotateEntity n\Collider,0,EntityYaw(e\room\NPC[1]\Collider),0
								n\State2 = EntityYaw(n\Collider)
								MoveEntity n\Collider,-0.65,0,0
								
								MoveEntity e\room\NPC[1]\Collider,0,0,0.1
								PointEntity Collider, e\room\NPC[1]\Collider
								
								PlaySound_Strict LoadTempSound("SFX\Character\MTF\049\Player0492_1.ogg")
								
								LoadEventSound(e,"SFX\SCP\049\0492Breath.ogg")
								
								IsZombie = True
							EndIf
						EndIf
					Else
						BlurTimer = 800
						ForceMove = 0.5
						Injuries = Max(2.0,Injuries)
						Bloodloss = 0
						Infect = 0
						
						;Msg = ""
						
;						If e\room\NPC[2]\State = 7
;							If e\room\NPC[2]\State3 < 70*1.75
;								e\room\NPC[2]\State3 = e\room\NPC[2]\State3 + FPSfactor
;							Else
;								e\room\NPC[2]\State = 6
;								e\room\NPC[2]\Reload = e\room\NPC[1]\Reload+Rnd(5,10)
;							EndIf
;						ElseIf e\room\NPC[2]\State = 6 And e\room\NPC[2]\Reload > 70*4
;							If e\room\NPC[2]\State3 > -(70*4)
;								e\room\NPC[2]\State3 = e\room\NPC[2]\State3 - FPSfactor
;							Else
;								e\room\NPC[2]\State3 = 0.0
;								e\room\NPC[2]\Reload = 45
;								e\room\NPC[2]\State = 7
;							EndIf
;						EndIf
						
						pvt% = CreatePivot()
						PositionEntity pvt%,EntityX(e\room\NPC[1]\Collider),EntityY(e\room\NPC[1]\Collider)+0.2,EntityZ(e\room\NPC[1]\Collider)
						
						PointEntity Collider, e\room\NPC[1]\Collider
						PointEntity Camera, pvt%,EntityRoll(Camera)
						
						FreeEntity pvt%
						
						If KillTimer < 0 Then
							PlaySound_Strict LoadTempSound("SFX\Character\MTF\049\Player0492_2.ogg")
							RemoveEvent(e)
						Else
							If e\SoundCHN = 0 Then
								e\SoundCHN = PlaySound_Strict (e\Sound)
							Else
								If (Not ChannelPlaying(e\SoundCHN)) Then e\SoundCHN = PlaySound_Strict(e\Sound)
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room079"
				;[Block]
				If PlayerRoom = e\room Then
					
					If e\EventState = 0 Then
						
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[2],True), EntityY(e\room\Objects[2],True)+0.5, EntityZ(e\room\Objects[2],True))
						PointEntity e\room\NPC[0]\Collider, e\room\obj
						RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider),0, True
						;SetAnimTime e\room\NPC[0]\obj, 906
						SetNPCFrame(e\room\NPC[0], 288)
						e\room\NPC[0]\State = 8
						
						e\EventState = 1
					EndIf
					
					ShouldPlay = 4
					If RemoteDoorOn Then 
						If e\room\RoomDoors[0]\open Then 
							If e\room\RoomDoors[0]\openstate > 50 Or EntityDistance(Collider, e\room\RoomDoors[0]\frameobj)<0.5 Then
								e\room\RoomDoors[0]\openstate = Min(e\room\RoomDoors[0]\openstate,50)
								e\room\RoomDoors[0]\open = False
								PlaySound_Strict (LoadTempSound("SFX\Door\DoorError.ogg"))
							EndIf							
						EndIf
					ElseIf e\EventState < 10000
						If e\EventState = 1 Then 
							e\EventState = 2
						ElseIf e\EventState = 2
							If EntityDistance(e\room\Objects[0], Collider)<3.0 Then 
								GiveAchievement(Achv079)
								e\EventState = 3
								e\EventState2 = 1
								;e\Sound = LoadSound_Strict("SFX\SCP\079\Speech.ogg")
								;LoadEventSound(e,"SFX\SCP\079\Speech.ogg")
								;e\SoundCHN = PlaySound_Strict (e\Sound)
								e\SoundCHN = StreamSound_Strict("SFX\SCP\079\Speech.ogg",SFXVolume,0)
								e\SoundCHN_isStream = True
							EndIf							
						;ElseIf e\EventState = 3
						ElseIf e\EventState < 2000 Then ;3500
							;If ChannelPlaying(e\SoundCHN) Then
							If IsStreamPlaying_Strict(e\SoundCHN)
								If Rand(3) = 1 Then
									EntityTexture(e\room\Objects[1], OldAiPics(0))
									ShowEntity (e\room\Objects[1])
								ElseIf Rand(10) = 1 
									HideEntity (e\room\Objects[1])							
								EndIf							
							Else
								;If e\Sound <> 0 Then FreeSound_Strict e\Sound : e\Sound = 0
								If e\SoundCHN<>0
									StopStream_Strict(e\SoundCHN) : e\SoundCHN=0
								EndIf
								EntityTexture(e\room\Objects[1], OldAiPics(1))
								ShowEntity (e\room\Objects[1])
								e\EventState = e\EventState + FPSfactor
							EndIf
						Else
							If EntityDistance(e\room\Objects[0], Collider)<2.5 Then 
								e\EventState = 10001
								;e\Sound = LoadSound_Strict("SFX\SCP\079\Refuse.ogg")
								;LoadEventSound(e,"SFX\SCP\079\Refuse.ogg")
								;e\SoundCHN = PlaySound_Strict (e\Sound)
								If e\SoundCHN<>0
									StopStream_Strict(e\SoundCHN) : e\SoundCHN=0
								EndIf
								e\SoundCHN = StreamSound_Strict("SFX\SCP\079\Refuse.ogg",SFXVolume,0)
								;EntityTexture(e\room\Objects[1], OldAiPics(1))
								;ShowEntity (e\room\Objects[1])
							EndIf
						EndIf
					Else
						If e\SoundCHN<>0
							If (Not IsStreamPlaying_Strict(e\SoundCHN))
								e\SoundCHN = 0
								EntityTexture(e\room\Objects[1], OldAiPics(1))
								ShowEntity (e\room\Objects[1])
							Else
								If Rand(3) = 1 Then
									EntityTexture(e\room\Objects[1], OldAiPics(0))
									ShowEntity (e\room\Objects[1])
								ElseIf Rand(10) = 1 
									HideEntity (e\room\Objects[1])							
								EndIf
							EndIf
						EndIf
					EndIf
					
				EndIf
				
				
				If e\EventState2 = 1 Then
					If RemoteDoorOn Then 	
						;LoadEventSound(e,"SFX\SCP\079\GateB.ogg")
						;e\SoundCHN = PlaySound_Strict (e\Sound)
						If e\SoundCHN<>0
							StopStream_Strict(e\SoundCHN) : e\SoundCHN=0
						EndIf
						e\SoundCHN = StreamSound_Strict("SFX\SCP\079\GateB.ogg",SFXVolume,0)
						e\SoundCHN_isStream = True
						e\EventState2 = 2
						
						For e2.Events = Each Events
							If e2\EventName="exit1" Or e2\EventName="gateaentrance" Then
								e2\EventState3 = 1
							EndIf
						Next
					EndIf	
				EndIf
				
				;[End Block]
			Case "room106"
				;[Block]
				
				;eventstate2 = are the magnets on
				
				If SoundTransmission Then 
					If e\EventState = 1 Then
						e\EventState3 = Min(e\EventState3+FPSfactor,4000)
					EndIf
					If ChannelPlaying(e\SoundCHN) = False Then e\SoundCHN = PlaySound_Strict(RadioStatic)   
				EndIf
				
				If e\room\NPC[0]=Null Then ;add the lure subject
					TFormPoint(1088, 1096, 1728, e\room\obj, 0)
					e\room\NPC[0] = CreateNPC(NPCtypeD, TFormedX(), TFormedY(), TFormedZ())
					TurnEntity e\room\NPC[0]\Collider,0,e\room\angle+90,0,True
					e\room\NPC[0]\HideFromNVG = True
				EndIf
				
				If PlayerRoom = e\room And e\room\NPC[0]<>Null Then
					
					;ShowEntity e\room\NPC[0]\obj
					
					ShouldPlay = 66
					
					e\room\NPC[0]\State=6
					If e\room\NPC[0]\Idle = 0 Then
						AnimateNPC(e\room\NPC[0], 17.0, 19.0, 0.01, False)
						If e\room\NPC[0]\Frame = 19.0 Then e\room\NPC[0]\Idle = 1
					Else
						AnimateNPC(e\room\NPC[0], 19.0, 17.0, -0.01, False)	
						If e\room\NPC[0]\Frame = 17.0 Then e\room\NPC[0]\Idle = 0
					EndIf
					
					PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\Objects[5],True),EntityY(e\room\Objects[5],True)+0.1,EntityZ(e\room\Objects[5],True),True)
					RotateEntity(e\room\NPC[0]\Collider,EntityPitch(e\room\Objects[5],True),EntityYaw(e\room\Objects[5],True),0,True)
					ResetEntity(e\room\NPC[0]\Collider)
					
					temp = e\EventState2
					
					Local leverstate = UpdateLever(e\room\Objects[1],((EntityY(e\room\Objects[6],True)<-990*RoomScale) And (EntityY(e\room\Objects[6],True)>-1275.0*RoomScale)))
					If GrabbedEntity = e\room\Objects[1] And DrawHandIcon = True Then e\EventState2 = leverstate
					
					If e\EventState2 <> temp Then 
						If e\EventState2 = False Then
							PlaySound_Strict(MagnetDownSFX)
						Else
							PlaySound_Strict(MagnetUpSFX)	
						EndIf
					EndIf
					
					If ((e\EventState3>3200) Or (e\EventState3<2500)) Or (e\EventState<>1) Then
						SoundTransmission% = UpdateLever(e\room\Objects[3])
					EndIf
					If (Not SoundTransmission) Then
						If (e\SoundCHN2<>0) Then
							If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
						EndIf
						If (e\SoundCHN<>0) Then
							If ChannelPlaying(e\SoundCHN) Then StopChannel e\SoundCHN
						EndIf
					EndIf
					
					If e\EventState = 0 Then 
						If SoundTransmission And Rand(100)=1 Then
							If e\SoundCHN2 = 0 Then
								LoadEventSound(e,"SFX\Character\LureSubject\Idle"+Rand(1,6)+".ogg",1)
								e\SoundCHN2 = PlaySound_Strict(e\Sound2)								
							EndIf
							If ChannelPlaying(e\SoundCHN2) = False Then
								LoadEventSound(e,"SFX\Character\LureSubject\Idle"+Rand(1,6)+".ogg",1)
								e\SoundCHN2 = PlaySound_Strict(e\Sound2)
							EndIf
						EndIf
						
						If SoundTransmission Then
							UpdateButton(e\room\Objects[4])
							If ClosestButton = e\room\Objects[4] And MouseHit1 Then
								e\EventState = 1 ;start the femur breaker
								If SoundTransmission = True Then ;only play sounds if transmission is on
									If e\SoundCHN2 <> 0 Then
										If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
									EndIf 
									FemurBreakerSFX = LoadSound_Strict("SFX\Room\106Chamber\FemurBreaker.ogg")
									e\SoundCHN2 = PlaySound_Strict (FemurBreakerSFX)
								EndIf
							EndIf
						EndIf
					ElseIf e\EventState = 1 ;bone broken
						If SoundTransmission And e\EventState3 < 2000 Then 
							If e\SoundCHN2 = 0 Then 
								LoadEventSound(e,"SFX\Character\LureSubject\Sniffling.ogg",1)
								e\SoundCHN2 = PlaySound_Strict(e\Sound2)								
							EndIf
							If ChannelPlaying(e\SoundCHN2) = False Then
								LoadEventSound(e,"SFX\Character\LureSubject\Sniffling.ogg",1)
								e\SoundCHN2 = PlaySound_Strict(e\Sound2)
							EndIf
						EndIf
						
						If e\EventState3 => 2500 Then
							
							If e\EventState2 = 1 And e\EventState3-FPSfactor < 2500 Then
								PositionEntity(Curr106\Collider, EntityX(e\room\Objects[6], True), EntityY(e\room\Objects[6], True), EntityZ(e\room\Objects[6], True))
								Contained106 = False
								ShowEntity Curr106\obj
								Curr106\Idle = False
								Curr106\State = -11
								e\EventState = 2
								Exit
							EndIf
							
							ShouldPlay = 10
							
							PositionEntity(Curr106\Collider, EntityX(e\room\Objects[5], True), (700.0 + 108.0*(Min(e\EventState3-2500.0,800)/320.0))*RoomScale , EntityZ(e\room\Objects[5], True))
							HideEntity Curr106\obj2
							
							;PointEntity(Curr106\Collider, Camera)
							RotateEntity(Curr106\Collider,0, EntityYaw(e\room\Objects[5],True)+180.0, 0, True)
							Curr106\State = -11
							AnimateNPC(Curr106, 206, 250, 0.1)
							Curr106\Idle = True	
							
							If e\EventState3-FPSfactor < 2500 Then 
								d.Decals = CreateDecal(0, EntityX(e\room\Objects[5], True), 936.0*RoomScale, EntityZ(e\room\Objects[5], True), 90, 0, Rnd(360)) 
								d\Timer = 90000
								d\Alpha = 0.01 : d\AlphaChange = 0.005
								d\Size = 0.1 : d\SizeChange = 0.003	
								
								If e\SoundCHN2 <> 0 Then
									If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
								EndIf 
								LoadEventSound(e,"SFX\Character\LureSubject\106Bait.ogg",1)
								e\SoundCHN2=PlaySound_Strict(e\Sound2)
							ElseIf e\EventState3-FPSfactor < 2900 And e\EventState3 => 2900 Then
								If FemurBreakerSFX <> 0 Then FreeSound_Strict FemurBreakerSFX : FemurBreakerSFX = 0
								
								d.Decals = CreateDecal(0, EntityX(e\room\Objects[7], True), EntityY(e\room\Objects[7], True) , EntityZ(e\room\Objects[7], True), 0, 0, 0) 
								RotateEntity(d\obj, EntityPitch(e\room\Objects[7], True)+Rand(10,20), EntityYaw(e\room\Objects[7], True)+30, EntityRoll(d\obj))
								MoveEntity d\obj, 0,0.05,0.2 ;0.15
								RotateEntity(d\obj, EntityPitch(e\room\Objects[7], True), EntityYaw(e\room\Objects[7], True), EntityRoll(d\obj))
								
								EntityParent d\obj, e\room\Objects[7]
								;TurnEntity (d\obj, 0, 180, 0)
								
								d\Timer = 90000
								d\Alpha = 0.01 : d\AlphaChange = 0.005
								d\Size = 0.05 : d\SizeChange = 0.002
							ElseIf e\EventState3 > 3200 Then
								;PositionEntity e\room\Objects[8], 0, 1000.0, 0, True 
								;PositionEntity e\room\Objects[7], 0, 1000.0, 0, True 
								
								If e\EventState2 = True Then ;magnets off -> 106 caught
									Contained106 = True
								Else ;magnets off -> 106 comes out and attacks
									PositionEntity(Curr106\Collider, EntityX(e\room\Objects[6], True), EntityY(e\room\Objects[6], True), EntityZ(e\room\Objects[6], True))
									
									Contained106 = False
									ShowEntity Curr106\obj
									Curr106\Idle = False
									Curr106\State = -11
									
									e\EventState = 2
									Exit
								EndIf
							EndIf
							
						EndIf 
						
					EndIf
					
					If e\EventState2 Then
						PositionEntity (e\room\Objects[6],EntityX(e\room\Objects[6],True),CurveValue(-980.0*RoomScale + Sin(Float(MilliSecs2())*0.04)*0.07,EntityY(e\room\Objects[6],True),200.0),EntityZ(e\room\Objects[6],True),True)
						RotateEntity(e\room\Objects[6], Sin(Float(MilliSecs2())*0.03), EntityYaw(e\room\Objects[6],True), -Sin(Float(MilliSecs2())*0.025), True)
					Else
						PositionEntity (e\room\Objects[6],EntityX(e\room\Objects[6],True),CurveValue(-1280.0*RoomScale,EntityY(e\room\Objects[6],True),200.0),EntityZ(e\room\Objects[6],True),True)
						RotateEntity(e\room\Objects[6], 0, EntityYaw(e\room\Objects[6],True), 0, True)
					EndIf
				Else
					If PlayerRoom\RoomTemplate\Name = "pocketdimension" Or PlayerRoom\RoomTemplate\Name = "dimension1499" Then
						If (e\SoundCHN2<>0) Then
							If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
						EndIf
						If (e\SoundCHN<>0) Then
							If ChannelPlaying(e\SoundCHN) Then StopChannel e\SoundCHN
						EndIf
					ElseIf PlayerRoom\RoomTemplate\Name = "room860" Then
						For e2.Events = Each Events
							If e2\EventName = "room860" Then
								If e2\EventState = 1.0 Then
									If (e\SoundCHN2<>0) Then
										If ChannelPlaying(e\SoundCHN2) Then StopChannel e\SoundCHN2
									EndIf
									If (e\SoundCHN<>0) Then
										If ChannelPlaying(e\SoundCHN) Then StopChannel e\SoundCHN
									EndIf
								EndIf
								Exit
							EndIf
						Next
					EndIf
				EndIf
				;[End Block]
			Case "room205"
				;[Block]
				If PlayerRoom = e\room Then
					If e\EventState=0 Or e\room\Objects[0]=0 Then
						If e\EventStr = "" And QuickLoadPercent = -1
							QuickLoadPercent = 0
							QuickLoad_CurrEvent = e
							e\EventStr = "load0"
						EndIf
						
						If e\room\Objects[3]<>0
							HideEntity(e\room\Objects[3])
							HideEntity(e\room\Objects[4])
							HideEntity(e\room\Objects[5])
							HideEntity(e\room\Objects[6])
						EndIf
						
						If e\room\RoomDoors[1]\open = True
							e\EventState = 1
							GiveAchievement(Achv205)
						EndIf
					Else
						ShouldPlay = 16
						If (e\EventState<65) Then
							If (Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True))<2.0) Then
								PlaySound_Strict(LoadTempSound("SFX\SCP\205\Enter.ogg"))
								
								e\EventState = Max(e\EventState, 65)
								
								ShowEntity(e\room\Objects[3])
								ShowEntity(e\room\Objects[4])
								ShowEntity(e\room\Objects[5])
								HideEntity(e\room\Objects[6])
								
								SetAnimTime(e\room\Objects[3], 492)
								SetAnimTime(e\room\Objects[4], 434)
								SetAnimTime(e\room\Objects[5], 434)
								
								e\room\RoomDoors[0]\open = False
							EndIf
							
							If e\EventState>7 Then
								If (Rand(0,300)=1) Then
									e\room\RoomDoors[0]\open = Not e\room\RoomDoors[0]\open
								EndIf
							EndIf 
							
							e\EventState2 = e\EventState2 + FPSfactor							
						EndIf
						
						
						Select e\EventState
							Case 1
								ShowEntity e\room\Objects[1]
								HideEntity(e\room\Objects[5])
								HideEntity(e\room\Objects[4])
								HideEntity(e\room\Objects[3])
								;sitting
								ShowEntity(e\room\Objects[6])
								Animate2(e\room\Objects[6], AnimTime(e\room\Objects[6]), 526, 530, 0.2)
								If e\EventState2 > 20*70 Then e\EventState = e\EventState+1
							Case 3
								ShowEntity e\room\Objects[1]
								HideEntity(e\room\Objects[5])
								HideEntity(e\room\Objects[4])
								HideEntity(e\room\Objects[3])
								;laying down
								ShowEntity(e\room\Objects[6])
								Animate2(e\room\Objects[6], AnimTime(e\room\Objects[6]), 377, 525, 0.2)
								If e\EventState2 > 30*70 Then e\EventState = e\EventState+1
							Case 5
								ShowEntity e\room\Objects[1]
								HideEntity(e\room\Objects[5])
								HideEntity(e\room\Objects[4])
								HideEntity(e\room\Objects[3])
								;standing
								ShowEntity(e\room\Objects[6])
								Animate2(e\room\Objects[6], AnimTime(e\room\Objects[6]), 228, 376, 0.2)
								If e\EventState2 > 40*70 Then 
									e\EventState = e\EventState+1
									PlaySound2(LoadTempSound("SFX\SCP\205\Horror.ogg"), Camera, e\room\Objects[6], 10, 0.3)
								EndIf	
							Case 7
								ShowEntity e\room\Objects[1]
								ShowEntity(e\room\Objects[6])
								HideEntity(e\room\Objects[4])
								HideEntity(e\room\Objects[3])
								;first demon appears
								ShowEntity(e\room\Objects[5])
								;le sexy demon pose
								Animate2(e\room\Objects[5], AnimTime(e\room\Objects[5]), 500, 648, 0.2)
								;TurnEntity(e\room\Objects[6], 0.0, DeltaYaw(e\room\Objects[6], e\room\Objects[5])*0.01, 0.0, True)
								If e\EventState2 > 60*70 Then 
									e\EventState = e\EventState+1
									PlaySound2(LoadTempSound("SFX\SCP\205\Horror.ogg"), Camera, e\room\Objects[6], 10, 0.5)
								EndIf
							Case 9
								ShowEntity e\room\Objects[1]
								ShowEntity(e\room\Objects[6])
								ShowEntity(e\room\Objects[5])
								HideEntity(e\room\Objects[3])
								;second demon appears
								ShowEntity(e\room\Objects[4])
								;idle
								Animate2(e\room\Objects[4], AnimTime(e\room\Objects[4]), 2, 200, 0.2)
								Animate2(e\room\Objects[5], AnimTime(e\room\Objects[5]), 4, 125, 0.2)
								
								;TurnEntity(e\room\Objects[6], 0.0, DeltaYaw(e\room\Objects[6], e\room\Objects[4])*0.01, 0.0, True)
								
								If e\EventState2 > 80*70 Then 
									e\EventState = e\EventState+1
									PlaySound_Strict(LoadTempSound("SFX\SCP\205\Horror.ogg"))
								EndIf
							Case 11
								ShowEntity e\room\Objects[1]
								ShowEntity(e\room\Objects[6])
								ShowEntity(e\room\Objects[5])
								ShowEntity(e\room\Objects[4])
								;third demon
								ShowEntity(e\room\Objects[3])
								;idle
								Animate2(e\room\Objects[3], AnimTime(e\room\Objects[3]), 2, 226, 0.2)
								Animate2(e\room\Objects[4], AnimTime(e\room\Objects[4]), 2, 200, 0.2)
								Animate2(e\room\Objects[5], AnimTime(e\room\Objects[5]), 4, 125, 0.2)
								
								;TurnEntity(e\room\Objects[6], 0.0, DeltaYaw(e\room\Objects[5], e\room\Objects[4])*0.01, 0.0, True)
								
								If e\EventState2 > 85*70 Then e\EventState = e\EventState+1
							Case 13
								ShowEntity e\room\Objects[1]
								ShowEntity(e\room\Objects[6])
								ShowEntity(e\room\Objects[5])
								ShowEntity(e\room\Objects[4])
								ShowEntity(e\room\Objects[3])
								If (AnimTime(e\room\Objects[6])<>227) Then SetAnimTime(e\room\Objects[6], 227)
								
								Animate2(e\room\Objects[3], AnimTime(e\room\Objects[3]), 2, 491, 0.05)
								Animate2(e\room\Objects[4], AnimTime(e\room\Objects[4]), 197, 433, 0.05)
								Animate2(e\room\Objects[5], AnimTime(e\room\Objects[5]), 2, 433, 0.05)
							Case 66
								ShowEntity e\room\Objects[1]
								Animate2(e\room\Objects[3], AnimTime(e\room\Objects[3]), 492, 534, 0.1, False)
								Animate2(e\room\Objects[4], AnimTime(e\room\Objects[4]), 434, 466, 0.1, False)
								Animate2(e\room\Objects[5], AnimTime(e\room\Objects[5]), 434, 494, 0.1, False)
								
								If AnimTime(e\room\Objects[3])>515 Then
									;MoveEntity(e\room\Objects[3], 0,-FPSfactor*0.001,0)
									;MoveEntity(e\room\Objects[4], 0,-FPSfactor*0.001,0)
									;MoveEntity(e\room\Objects[5], 0,-FPSfactor*0.001,0)
									If AnimTime(e\room\Objects[3])>533 Then 
										e\EventState = 67
										e\EventState2 = 0										
										e\EventState3 = 0
										HideEntity e\room\Objects[1]
									EndIf
								EndIf
							Case 67
								If (Rand(150)=1) Then
									DeathMSG = "The SCP-205 cycle seems to have resumed its normal course after the anomalies observed during "
									DeathMSG = DeathMSG + "[REDACTED]. The body of subject D-9341 was discovered inside the chamber. "
									DeathMSG = DeathMSG + "The subject exhibits signs of blunt force trauma typical for personnel who have "
									DeathMSG = DeathMSG + "entered the chamber when the lights are off."
									
									Injuries=Injuries+Rnd(0.4,0.8)
									PlaySound_Strict DamageSFX(Rand(2,3))
									CameraShake = 0.5
									
									e\EventState2 = Rnd(-0.1, 0.1)
									e\EventState3 = Rnd(-0.1, 0.1)
									
									If (Injuries>5.0) Kill()
								EndIf
								
								TranslateEntity(Collider, e\EventState2,0,e\EventState3)
								e\EventState2 = CurveValue(e\EventState2, 0, 10.0)								
								e\EventState3 = CurveValue(e\EventState3, 0, 10.0)
							Default
								If (Rand(3)=1) Then
									HideEntity e\room\Objects[1]
								Else
									ShowEntity e\room\Objects[1]
								EndIf
								
								e\EventState3 = e\EventState3 + FPSfactor
								If (e\EventState3>50) Then
									ShowEntity e\room\Objects[1]
									e\EventState = e\EventState+1
									e\EventState3=0
								EndIf
						End Select
					EndIf
				Else If (e\room\Objects[3]<>0)
					HideEntity(e\room\Objects[3])
					HideEntity(e\room\Objects[4])
					HideEntity(e\room\Objects[5])
					HideEntity(e\room\Objects[6])
				Else
					e\EventState = 0
					e\EventStr = ""
				EndIf
				;[End block]
			Case "room860"
				;[Block]
				
				;e\EventState = is the player in the forest
				;e\EventState2 = which side of the door did the player enter from
				;e\EventState3 = monster spawn timer
				
				Local fr.Forest=e\room\fr
				
				If PlayerRoom = e\room And fr<>Null Then 
					
					;Local dp.DrawPortal
					
					If e\EventState=1.0 Then ;the player is in the forest
						CurrStepSFX = 2
						
						Curr106\Idle = True
						
						;ShowEntity fr\DetailEntities[0]
						;ShowEntity fr\DetailEntities[1]
						
						UpdateForest(fr,Collider)
						
						If e\EventStr = "" And QuickLoadPercent = -1
							QuickLoadPercent = 0
							QuickLoad_CurrEvent = e
							e\EventStr = "load0"
						EndIf
						
						If e\room\NPC[0]<>Null Then
							If (e\room\NPC[0]\State2 = 1 And e\room\NPC[0]\State>1) Or e\room\NPC[0]\State>2 ;the monster is chasing the player
								ShouldPlay = 12
							Else
								ShouldPlay = 9
							EndIf
						EndIf
						
						;the player fell
						If (Not NoClip)
							If EntityY(Collider)<=28.5 Then 
								Kill() 
								BlinkTimer=-2
							ElseIf EntityY(Collider)>EntityY(fr\Forest_Pivot,True)+0.5
								MoveEntity(Collider, 0, ((EntityY(fr\Forest_Pivot,True)+0.5) - EntityY(Collider))*FPSfactor, 0)
							EndIf
						EndIf
						
						If e\room\NPC[0]<>Null
							If e\room\NPC[0]\State = 0 Or EntityDistance(Collider, e\room\NPC[0]\Collider)>20.0 Then
								e\EventState3 = e\EventState3 + (1+CurrSpeed)* FPSfactor
								If (e\EventState3 Mod 500) < 10.0 And ((e\EventState3-FPSfactor) Mod 500) > 490.0 Then
									;If e\EventState3 > 3500 And Rnd(10000)<e\EventState3 Then
									If e\EventState3 > 3000-(500*SelectedDifficulty\aggressiveNPCs) And Rnd(10000+(500*SelectedDifficulty\aggressiveNPCs)) < e\EventState3
										e\room\NPC[0]\State=2
										PositionEntity e\room\NPC[0]\Collider, 0,-110,0
										;e\EventState3=e\EventState3-Rnd(2000,3000)
										e\EventState3=e\EventState3-Rnd(1000,2000-(500*SelectedDifficulty\aggressiveNPCs))
										DebugLog "attack"
									Else
										e\room\NPC[0]\State=1
										PositionEntity e\room\NPC[0]\Collider, 0,-110,0
										DebugLog "spawn"
									EndIf
								EndIf
							EndIf
						EndIf
						
						;If KeyHit(25) Then
						;	e\room\NPC[0]\State=2
						;	PositionEntity e\room\NPC[0]\Collider, 0,-110,0
						;	e\EventState3=e\EventState3-Rnd(2000,3000)
						;EndIf
						
						For i = 0 To 1
							If EntityDistance(fr\Door[i], Collider)<0.5 Then
								If EntityInView(fr\Door[i], Camera) Then
									DrawHandIcon = True
									If MouseHit1 Then
										If i=e\EventState2 Then
											BlinkTimer = -10
											
											PlaySound_Strict(LoadTempSound("SFX\Door\WoodenDoorOpen.ogg"))
											
											RotateEntity e\room\Objects[3], 0, 0, 0
											RotateEntity e\room\Objects[4], 0, 180, 0
											;SetAnimTime e\room\Objects[3], 0.0
											;SetAnimTime e\room\Objects[4], 0.0
											
											;dp.DrawPortal=e\room\dp;Object.DrawPortal(e\room\Objects[0])
											PositionEntity Collider, EntityX(e\room\Objects[2],True),0.5,EntityZ(e\room\Objects[2],True)
											
											RotateEntity Collider, 0, EntityYaw(e\room\obj,True)+e\EventState2*180, 0
											MoveEntity Collider, 0,0,1.5
											
											ResetEntity Collider
											
											UpdateDoorsTimer = 0
											UpdateDoors()
											
											SecondaryLightOn = PrevSecondaryLightOn
											;SecondaryLightOn = 0.0
											
											e\EventState = 0.0
											e\EventState3 = 0.0
										Else
											PlaySound_Strict(LoadTempSound("SFX\Door\WoodenDoorBudge.ogg"))
											Msg = "The door will not budge."
											MsgTimer = 70*5
										EndIf
									EndIf
								EndIf
							EndIf
						Next
						
						If e\room\NPC[0]<>Null
							x = Max(1.0-(e\room\NPC[0]\State3/300.0),0.1)
						Else
							x = 2.0
						EndIf
						
						If (Not DebugHUD)
							CameraClsColor Camera,98*x,133*x,162*x
							CameraRange Camera,RoomScale,8.5
							CameraFogRange Camera,0.5,8.0
							CameraFogColor Camera,98*x,133*x,162*x
						EndIf
						
					Else
						
						If (Not Contained106) Then Curr106\Idle = False
						
						;dp.DrawPortal=e\room\dp;Object.DrawPortal(e\room\Objects[0])
						
						;HideEntity fr\DetailEntities[0]
						;HideEntity fr\DetailEntities[1]
						
						If EntityYaw(e\room\Objects[3])=0.0 Then
							HideEntity fr.Forest\Forest_Pivot
							If (Abs(Distance(EntityX(e\room\Objects[3],True),EntityZ(e\room\Objects[3],True),EntityX(Collider,True),EntityZ(Collider,True)))<1.0) Then
								DrawHandIcon = True
								
								If SelectedItem = Null Then
									If MouseHit1 Then
										PlaySound_Strict(LoadTempSound("SFX\Door\WoodenDoorBudge.ogg"))
										Msg = "The door will not budge."
										MsgTimer = 5*70
									EndIf
								ElseIf SelectedItem\itemtemplate\tempname="scp860" 
									If MouseHit1 Then
										PlaySound_Strict(LoadTempSound("SFX\Door\WoodenDoorOpen.ogg"))
										ShowEntity fr.Forest\Forest_Pivot
										SelectedItem = Null
										
										BlinkTimer = -10
										
										e\EventState=1.0
										
										;reset monster spawn timer
										e\EventState3 = 0.0
										
										If e\room\NPC[0]<>Null Then
											;reset monster to the (hidden) idle state
											e\room\NPC[0]\State = 0
										EndIf
										
										PrevSecondaryLightOn = SecondaryLightOn
										SecondaryLightOn = True
										
										pvt = CreatePivot()
										PositionEntity pvt, EntityX(Camera),EntityY(Camera),EntityZ(Camera)
										PointEntity pvt, e\room\obj
										ang# = WrapAngle(EntityYaw(pvt)-EntityYaw(e\room\obj,True))
										If ang > 90 And ang < 270 Then
											PositionEntity Collider,EntityX(fr\Door[0],True),EntityY(fr\Door[0],True)+EntityY(Collider,True)+0.5,EntityZ(fr\Door[0],True),True
											RotateEntity Collider, 0.0, EntityYaw(fr\Door[0],True)-180, 0.0, True
											MoveEntity Collider, -0.5,0.0,0.5
											e\EventState2 = 1
										Else
											PositionEntity Collider,EntityX(fr\Door[1],True),EntityY(fr\Door[1],True)+EntityY(Collider,True)+0.5,EntityZ(fr\Door[1],True),True
											RotateEntity Collider, 0.0, EntityYaw(fr\Door[1],True)-180, 0.0, True
											MoveEntity Collider, -0.5,0.0,0.5
											e\EventState2 = 0
										EndIf
										FreeEntity pvt
										
										ResetEntity Collider
									EndIf
								EndIf
							EndIf
						EndIf
						
					EndIf
					
				Else
					If (fr=Null) Then
						RemoveEvent(e)
					Else
						If (fr\Forest_Pivot<>0) Then HideEntity fr\Forest_Pivot
					EndIf
				EndIf
				
				;[End Block]
				
			Case "room966"
				;[Block]
				If PlayerRoom = e\room Then
					Select e\EventState
						Case 0
							;a dirty workaround to hide the pause when loading 966 model
							If QuickLoadPercent = -1
								;LightBlink = 5.0
								e\EventState = 1
								;PlaySound_Strict LightSFX
								QuickLoadPercent = 0
								QuickLoad_CurrEvent = e
							EndIf
						Case 2
							e\EventState = 2
							RemoveEvent (e)
					End Select
				EndIf
				;[End Block]
			Case "room1123"
				;[Block]
				If PlayerRoom = e\room Then
					;the event is started when the player picks up SCP-1123 (in Items.bb/UpdateItems())
					If e\EventState>0 And e\EventState<7 Then
						CanSave = False
					EndIf
					If e\EventState = 1 Then
						;If e\SoundCHN = 0 Then
						;	e\SoundCHN = 
						;Else
						;	If Not ChannelPlaying(e\SoundCHN) Then e\SoundCHN = PlaySound_Strict(moddedambience)
						;End If
						
						;Saving Injuries and Bloodloss, so that the player won't be healed automatically
						PrevInjuries = Injuries
						PrevBloodloss = Bloodloss
						PrevSecondaryLightOn = SecondaryLightOn
						SecondaryLightOn = True
						
						e\room\NPC[0] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[6],True),EntityY(e\room\Objects[6],True),EntityZ(e\room\Objects[6],True))
						;e\room\NPC[1] = CreateNPC(NPCtypeD, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True),EntityZ(e\room\Objects[7],True))
						
						nazi = LoadAnimMesh_Strict("GFX\npcs\naziofficer.b3d")
						scale# = 0.5 / MeshWidth(nazi)
						
						FreeEntity e\room\NPC[0]\obj
						e\room\NPC[0]\obj = CopyEntity(nazi)
						ScaleEntity e\room\NPC[0]\obj, scale, scale, scale
						
						;FreeEntity e\room\NPC[1]\obj
						;e\room\NPC[1]\obj = CopyEntity(nazi)
						;ScaleEntity e\room\NPC[1]\obj, scale, scale, scale
						
						FreeEntity nazi
						PositionEntity Collider, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True),True
						ResetEntity Collider
						;PlaySound_Strict(HorrorSFX(9))
						CameraShake = 1.0
						BlurTimer = 1200
						Injuries = 1.0
						e\EventState = 2
						
					ElseIf e\EventState = 2
						e\EventState2 = e\EventState2 + FPSfactor
						
						PointEntity e\room\NPC[0]\Collider, Collider
						BlurTimer = Max(BlurTimer, 100)
						
						If e\EventState2>200 And e\EventState2-FPSfactor=<200 Then 							
							e\Sound = LoadSound_Strict("SFX\Music\1123.ogg");
							e\SoundCHN = PlaySound_Strict(e\Sound)
						EndIf
						
						If e\EventState2 > 1000 Then
							If e\Sound2=0 Then
								e\Sound2 = LoadSound_Strict("SFX\Door\1123DoorOpen.ogg")
								e\SoundCHN2 = PlaySound_Strict(e\Sound2)
							EndIf
							RotateEntity e\room\Objects[11], 0, CurveAngle(10, EntityYaw(e\room\Objects[11],0), 40), 0,False
							If e\EventState2=>1040 And e\EventState2-FPSfactor<1040 Then 
								PlaySound2(LoadTempSound("SFX\SCP\1123\Officer1.ogg"), Camera, e\room\NPC[0]\obj)
							ElseIf e\EventState2=>1400 And e\EventState2-FPSfactor<1400 Then 
								PlaySound2(LoadTempSound("SFX\SCP\1123\Officer2.ogg"), Camera, e\room\NPC[0]\obj)
							EndIf
							e\room\NPC[0]\State = 3
							AnimateNPC(e\room\NPC[0],3,26,0.2,True)
							;Animate2(e\room\Objects[4], AnimTime(e\room\Objects[4]), 0, 8, 0.1, False)
							If EntityDistance(Collider, e\room\Objects[4])>392*RoomScale Then
								BlinkTimer = -10
								BlurTimer = 500
								PositionEntity Collider, EntityX(e\room\Objects[5],True),EntityY(e\room\Objects[5],True),EntityZ(e\room\Objects[5],True),True
								RotateEntity Collider, 0, EntityYaw(e\room\obj,True)+180, 0
								ResetEntity(Collider)
								e\EventState = 3
							EndIf
							
						EndIf
					ElseIf e\EventState=3
						If e\room\RoomDoors[0]\openstate>160 Then
							If e\Sound=0 Then e\Sound = LoadSound_Strict("SFX\Music\1123.ogg")
							e\SoundCHN = PlaySound_Strict(e\Sound)
							
							PositionEntity e\room\NPC[0]\Collider, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True),EntityZ(e\room\Objects[7],True)
							ResetEntity e\room\NPC[0]\Collider
							
							e\EventState=4
						EndIf
					ElseIf e\EventState=4
						
						;PointEntity e\room\NPC[1]\Collider, Collider
						
						TFormPoint EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,e\room\obj
						
						If TFormedX()<256 And TFormedZ()>-480 Then
							e\room\RoomDoors[0]\open = False
						EndIf
						
						If EntityYaw(e\room\Objects[13],False)=0 Then
							DebugLog "aaaaaaaa"
							If EntityDistance(Collider, e\room\Objects[12])<1.0 Then
								DrawHandIcon = True
								If MouseHit1 Then
									RotateEntity e\room\Objects[13], 0, 1, 0, False
									RotateEntity e\room\Objects[11], 0, 90, 0, False
									PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Horror.ogg"))
								EndIf
							EndIf							
						Else
							DebugLog "bbbbbbb"
							RotateEntity e\room\Objects[13], 0, CurveAngle(90, EntityYaw(e\room\Objects[13], False), 40), 0
							If EntityYaw(e\room\Objects[13], False)>30 Then
								e\room\NPC[0]\State = 3
								PointEntity e\room\NPC[0]\Collider, Collider
								AnimateNPC(e\room\NPC[0], 27, 54, 0.5, False)
								If e\room\NPC[0]\Frame => 54 Then
									e\EventState = 5
									e\EventState2 = 0
									PositionEntity Collider, EntityX(e\room\obj,True),0.3,EntityZ(e\room\obj,True),True
									ResetEntity Collider									
									BlinkTimer = -10
									BlurTimer = 500	
									Injuries = 1.5
									Bloodloss = 70
								;PlaySound_Strict(LoadTempSound("SFX\Door\WoodenDoorClose.ogg"))							
								EndIf								
							EndIf
							
						EndIf
						
						;RemoveNPC(e\room\NPC[0])
						;RemoveNPC(e\room\NPC[1])
						;RemoveEvent(e)
					ElseIf e\EventState = 5
						e\EventState2 = e\EventState2 + FPSfactor
						If e\EventState2 > 500 Then 
							RotateEntity e\room\Objects[9],0,90,0,False
							RotateEntity e\room\Objects[13],0,0,0,False
							
							x = (EntityX(e\room\Objects[8], True)+EntityX(e\room\Objects[12], True))/2
							y = EntityY(e\room\Objects[5], True)
							z = (EntityZ(e\room\Objects[8], True)+EntityZ(e\room\Objects[12], True))/2
							PositionEntity Collider, x,y,z, True
							ResetEntity(Collider)
							
							x = (EntityX(Collider, True)+EntityX(e\room\Objects[12], True))/2
							z = (EntityZ(Collider, True)+EntityZ(e\room\Objects[12], True))/2
							
							PositionEntity e\room\NPC[0]\Collider, x,y+0.2,z
							ResetEntity e\room\NPC[0]\Collider
							
							Injuries = 1.5
							Bloodloss = 70
							
							BlinkTimer = -10
							
							de.Decals = CreateDecal(3, EntityX(Collider), 512*RoomScale + 0.0005, EntityZ(Collider),90,Rnd(360),0)
							de\size = 0.5 : ScaleSprite de\obj, de\size, de\size
							
							e\room\NPC[0]\Sound = LoadSound_Strict("SFX\SCP\1123\Officer3.ogg")
							
							e\EventState = 6
						EndIf
					ElseIf e\EventState = 6
						PointEntity e\room\NPC[0]\Collider, Collider
						AnimateNPC(e\room\NPC[0], 75, 128, 0.04, True)	
						If e\room\NPC[0]\Sound<>0 Then 
							If e\room\NPC[0]\SoundChn<>0 Then
								If (Not ChannelPlaying(e\room\NPC[0]\SoundChn)) Then 
									PlaySound_Strict(LoadTempSound("SFX\SCP\1123\Gunshot.ogg"))
									e\EventState = 7
									FreeSound_Strict e\room\NPC[0]\Sound : e\room\NPC[0]\Sound=0	
								EndIf
							EndIf
							
							If e\room\NPC[0]\Sound<>0 Then e\room\NPC[0]\SoundChn = LoopSound2(e\room\NPC[0]\Sound, e\room\NPC[0]\SoundChn, Camera, e\room\NPC[0]\Collider, 7.0)
						EndIf
					ElseIf e\EventState=7
						PositionEntity Collider, EntityX(e\room\obj,True),0.3,EntityZ(e\room\obj,True),True
						ResetEntity Collider
						ShowEntity Light
						LightFlash = 6
						BlurTimer = 500	
						Injuries = PrevInjuries
						Bloodloss = PrevBloodloss
						SecondaryLightOn = PrevSecondaryLightOn
						RotateEntity e\room\Objects[9],0,0,0,False
						
						PrevInjuries = 0
						PrevBloodloss = 0
						PrevSecondaryLightOn = 0.0
						Crouch = False
						CanSave = True
						For i = 0 To MaxItemAmount-1
							If Inventory(i) <> Null Then
								If Inventory(i)\itemtemplate\name = "Leaflet"
									RemoveItem(Inventory(i))
									Exit
								EndIf
							EndIf
						Next
						
						GiveAchievement(Achv1123)
						
						RemoveNPC(e\room\NPC[0])
						RemoveEvent(e)						
					End If
				EndIf
				;[End Block]
				
				
				
			Case "testroom"
				;[Block]
				If e <> Null Then
					If PlayerRoom = e\room Then
						If e\EventState = 0 Then
							e\room\Objects[7]=LoadAnimMesh_Strict("GFX\npcs\scp-1048.b3d")
							ScaleEntity e\room\Objects[7], 0.05,0.05,0.05
							
							TFormPoint EntityX(Collider),EntityY(Collider),EntityZ(Collider),0,e\room\obj
							If TFormedZ()=0 Then temp = -1 Else temp = -Sgn(TFormedZ())
							TFormPoint -720,0,816*temp,e\room\obj,0
							PositionEntity(e\room\Objects[7],TFormedX(),0,TFormedZ())
							
							RotateEntity e\room\Objects[7],-90,e\room\angle-90,0
							SetAnimTime e\room\Objects[7],297
							e\EventState = 1
						EndIf
						If EntityDistance(Collider, e\room\Objects[6]) < 2.5 And e\EventState > 0 Then
							PlaySound_Strict(LoadTempSound("SFX\SCP\079\TestroomWarning.ogg"))
							For i = 0 To 5
								em.Emitters = CreateEmitter(EntityX(e\room\Objects[i], True), EntityY(e\room\Objects[i], True), EntityZ(e\room\Objects[i], True), 0)
								TurnEntity(em\Obj, 90, 0, 0, True)
								;entityParent(em\obj, e\room\obj)
								em\RandAngle = 5
								em\Speed = 0.042
								em\SizeChange = 0.0025									
							Next
							;Delete e
							e\EventState = e\EventState * -1
						EndIf
						If e\room\Objects[7]<>0 Then
							Animate2(e\room\Objects[7],AnimTime(e\room\Objects[7]),284,295,0.3)
							MoveEntity e\room\Objects[7],0,-0.008*FPSfactor,0
							TFormPoint EntityX(e\room\Objects[7]),EntityY(e\room\Objects[7]),EntityZ(e\room\Objects[7]),0,e\room\obj
							
							If Abs(TFormedX())>725 Then
								FreeEntity(e\room\Objects[7])
								e\room\Objects[7]=0
								e\EventState = e\EventState *2
							EndIf
						EndIf
						If e\EventState = -2 Then RemoveEvent(e)
					EndIf
				End If
				;[End Block]
			Case "tunnel2smoke"
				;[Block]
				If PlayerRoom = e\room Then
					If e\room\dist < 3.5 Then
						PlaySound2(BurstSFX, Camera, e\room\obj) 
						For i = 0 To 1
							em.Emitters = CreateEmitter(EntityX(e\room\Objects[i],True),EntityY(e\room\Objects[i],True), EntityZ(e\room\Objects[i],True),0)
							TurnEntity(em\Obj, 90, 0, 0, True)
							EntityParent(em\Obj, e\room\obj)
							em\Size = 0.05
							em\RandAngle = 10
							em\Speed = 0.06
							em\SizeChange = 0.007
							;EntityParent(em\Obj, e\room\obj)
							
							For z = 0 To Ceil(3.3333*(ParticleAmount+1))
								p.Particles = CreateParticle(EntityX(em\Obj, True), 448*RoomScale, EntityZ(em\Obj, True), Rand(em\MinImage, em\MaxImage), em\Size, em\Gravity, em\LifeTime)
								p\speed = em\Speed
								RotateEntity(p\pvt, Rnd(360), Rnd(360), 0, True)
								p\size = 0.05
								p\SizeChange = 0.008
							Next
							
						Next
						RemoveEvent(e)
					End If					
				EndIf
				
				;[End Block]
			Case "tunnel2"
				;[Block]
				If PlayerRoom = e\room Then
					If Curr173\Idle > 1 Then
						RemoveEvent(e)
						Exit
					Else		
						If e\EventState = 0 Then
							If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\obj), EntityZ(e\room\obj)) < 3.5 Then
								PlaySound_Strict(LightSFX)
								
								LightBlink = Rnd(0.0,1.0)*(e\EventState/200)
								e\EventState = 1
							End If
						End If	
					EndIf
				EndIf
				
				If e\EventState > 0 And e\EventState < 200 Then
					BlinkTimer = -10
					If e\EventState > 30 Then 
						LightBlink = 1.0 
						If e\EventState-FPSfactor =< 30 Then 
							PlaySound_Strict LoadTempSound("SFX\ambient\general\ambient3.ogg")
						EndIf
					EndIf
					If e\EventState-FPSfactor =< 100 And e\EventState > 100 Then
						PlaySound_Strict LoadTempSound("SFX\ambient\general\ambient6.ogg")
						PositionEntity(Curr173\Collider, EntityX(e\room\obj), 0.6, EntityZ(e\room\obj))
						ResetEntity(Curr173\Collider)					
						Curr173\Idle = True		
					EndIf
					LightBlink = 1.0
					e\EventState = e\EventState + FPSfactor
				ElseIf e\EventState <> 0 Then
					BlinkTimer = BLINKFREQ
					
					Curr173\Idle = False
					RemoveEvent(e)
				EndIf
				;[End Block]
			Case "tunnel106"
				;[Block]
				If e\EventState = 0 Then
					If e\room\dist < 5.0 And e\room\dist > 0 Then
						If Curr106\State >= 0 Then
							e\EventState = 1
						Else
							If Curr106\State <= -10 And EntityDistance(Curr106\Collider,Collider)>5 And (Not EntityInView(Curr106\obj,Camera)) Then
								e\EventState = 1
								e\EventState2 = 1
							EndIf
						EndIf
					ElseIf Contained106
						RemoveEvent(e)
					EndIf
				ElseIf e\EventState = 1
					
					If e\room\dist < 3.0 Or Rand(7000)=1 Then
						e\EventState = 2
						d.Decals = CreateDecal(0, EntityX(e\room\obj), 445.0*RoomScale, EntityZ(e\room\obj), -90, Rand(360), 0)
						d\Size = Rnd(0.5, 0.7) : EntityAlpha(d\obj, 0.7) : d\ID = 1 : ScaleSprite(d\obj, d\Size, d\Size)
						EntityAlpha(d\obj, Rnd(0.7, 0.85))
						
						PlaySound_Strict HorrorSFX(10)
					ElseIf e\room\dist > 8.0
						If Rand(5) = 1 Then
							Curr106\Idle = False
							RemoveEvent(e)
						Else
							Curr106\Idle = False
							Curr106\State = -10000
							RemoveEvent(e)
						End If
					EndIf
				Else
					If e\EventState2 = 1 Then
						ShouldPlay = 10
					EndIf
					e\EventState = e\EventState+FPSfactor
					If e\EventState <= 180 Then
						PositionEntity(Curr106\Collider, EntityX(e\room\obj, True), EntityY(Collider) + 1.0 - Min(Sin(e\EventState)*1.5,1.1), EntityZ(e\room\obj, True), True)
						PointEntity(Curr106\Collider, Camera)
						AnimateNPC(Curr106, 55, 104, 0.1)
						Curr106\Idle = True
						Curr106\State = 1
						ResetEntity(Curr106\Collider)
						Curr106\DropSpeed = 0
						PositionEntity(Curr106\obj, EntityX(Curr106\Collider), EntityY(Curr106\Collider) - 0.15, EntityZ(Curr106\Collider))
						RotateEntity Curr106\obj, 0, EntityYaw(Curr106\Collider), 0
						ShowEntity Curr106\obj
					ElseIf e\EventState > 180 And e\EventState < 300 Then
						Curr106\Idle = False
						Curr106\State = -10
						PositionEntity(Curr106\Collider, EntityX(e\room\obj, True), -3.0, EntityZ(e\room\obj, True), True)
						Curr106\PathTimer = 70*10
						Curr106\PathStatus = 0
						Curr106\PathLocation = 0
						de.Decals = CreateDecal(0, EntityX(e\room\obj, True), 0.01, EntityZ(e\room\obj, True), 90, Rand(360), 0)
						de\Size = 0.05 : de\SizeChange = 0.01 : EntityAlpha(de\obj, 0.8) : UpdateDecals
						e\EventState = 300
					ElseIf e\EventState < 800
						If EntityY(Curr106\Collider)>=EntityY(Collider)-0.05 Then
							RemoveEvent(e)
						Else
							TranslateEntity Curr106\Collider, 0, ((EntityY(Collider,True) - 0.11) - EntityY(Curr106\Collider)) / 50.0, 0
							If EntityY(Curr106\Collider)<-0.1 Then
								Curr106\CurrSpeed = 0.0
							EndIf
						EndIf
					Else
						RemoveEvent(e)
					EndIf
					
				EndIf
				;[End Block]
				
			Case "testroom173"
				;[Block]
				If PlayerRoom = e\room	
					If Curr173\Idle = 0 Then 
						If e\EventState = 0 Then
							If e\room\RoomDoors[0]\open = True
							PositionEntity(Curr173\Collider, EntityX(e\room\Objects[0], True), 0.5, EntityZ(e\room\Objects[0], True))
							ResetEntity(Curr173\Collider)
							e\EventState = 1
							EndIf
						Else
							If e\room\Objects[2]=0
								Local Glasstex = LoadTexture_Strict("GFX\map\glass.png",1+2)
								e\room\Objects[2] = CreateSprite()
								EntityTexture(e\room\Objects[2],Glasstex)
								SpriteViewMode(e\room\Objects[2],2)
								ScaleSprite(e\room\Objects[2],182.0*RoomScale*0.5, 192.0*RoomScale*0.5)
								pvt% = CreatePivot(e\room\obj)
								;PositionEntity pvt%,-595.0,224.0,-208.0,False
								PositionEntity pvt%,-632.0,224.0,-208.0,False
								PositionEntity(e\room\Objects[2], EntityX(pvt,True), EntityY(pvt,True), EntityZ(pvt,True))
								FreeEntity pvt
								RotateEntity e\room\Objects[2],0,e\room\angle,0
								TurnEntity(e\room\Objects[2],0,180,0)
								EntityParent(e\room\Objects[2], e\room\obj)
								FreeTexture Glasstex
							EndIf
							
							ShowEntity (e\room\Objects[2])
							;start a timer for 173 breaking through the window
							e\EventState = e\EventState + 1
							dist# = EntityDistance(Collider, e\room\Objects[1])
							If dist <1.0 Then
								;if close, increase the timer so that 173 is ready to attack
								e\EventState = Max(e\EventState, 70*12)
							ElseIf dist > 1.4
								;if the player moves a bit further and blinks, 173 attacks
								If e\EventState > 70*12 And BlinkTimer =< -10 Then
									If (EntityDistance(Curr173\Collider, e\room\Objects[0]) > 5.0) Then
										;if 173 is far away from the room (perhaps because the player 
										;left and 173 moved to some other room?) -> disable the event
										RemoveEvent(e)
									Else
										PlaySound2(LoadTempSound("SFX\General\GlassBreak.ogg"), Camera, Curr173\obj) 
										FreeEntity(e\room\Objects[2])
										e\room\Objects[2]=0
										PositionEntity(Curr173\Collider, EntityX(e\room\Objects[1], True), 0.5, EntityZ(e\room\Objects[1], True))
										ResetEntity(Curr173\Collider)
										RemoveEvent(e)
									EndIf
								EndIf	
							EndIf
						End If
					EndIf
				End If	
				;[End Block]
			Case "toiletguard"
				;[Block]
				If e\EventState = 0 Then
					If e\room\dist < 8.0  And e\room\dist > 0 Then e\EventState = 1
				ElseIf e\EventState = 1
					e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True)+0.5, EntityZ(e\room\Objects[1],True))
					PointEntity e\room\NPC[0]\Collider, e\room\obj
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider)-20,0, True
					
					SetNPCFrame (e\room\NPC[0], 287)
					e\room\NPC[0]\State = 8
					
					e\EventState = 2	
				Else
					If e\Sound = 0 Then e\Sound = LoadSound_Strict("SFX\Character\Guard\SuicideGuard1.ogg")
					If e\room\dist < 15.0 And e\room\dist >= 4.0 Then 
						e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\NPC[0]\Collider, 15.0)
						
					ElseIf e\room\dist<4.0 And PlayerSoundVolume > 1.0
						If e\EventState2=0
							;Y=0.01
							de.Decals = CreateDecal(3,  EntityX(e\room\Objects[2],True), EntityY(e\room\Objects[2],True), EntityZ(e\room\Objects[2],True),0,e\room\angle+270,0)
							de\Size = 0.3 : ScaleSprite (de\obj, de\size, de\size)
							
							;de.Decals = CreateDecal(17,  EntityX(e\room\Objects[2],True), 0.01, EntityZ(e\room\Objects[2],True),90,Rnd(360),0)
							;de\Size = 0.1 : de\maxsize = 0.45 : de\sizechange = 0.0002 : UpdateDecals()
							
							e\EventState2 = 1
						EndIf
						If e\SoundCHN2 = 0
							StopChannel(e\SoundCHN)
							FreeSound_Strict(e\Sound)
							e\room\NPC[0]\Sound = LoadSound_Strict("SFX\Character\Guard\SuicideGuard2.ogg")
							e\SoundCHN2 = PlaySound2(e\room\NPC[0]\Sound, Camera, e\room\NPC[0]\Collider, 15.0)
						EndIf
						UpdateSoundOrigin(e\SoundCHN2,Camera,e\room\NPC[0]\Collider,15.0)
						If (Not ChannelPlaying(e\SoundCHN2)) Then RemoveEvent(e)
					EndIf
				EndIf
				;[End Block]
				
			Case "008"
				;[Block]
				If PlayerRoom = e\room Then	
					GiveAchievement(Achv008)=True
					;container open
					If e\EventState = 0 Then
						If Curr173\Idle<2 And EntityDistance(Curr173\Collider,Collider)>HideDistance ;Just making sure that 173 is far away enough to spawn him to this room
							PositionEntity Curr173\Collider, EntityX(e\room\Objects[3],True),0.5,EntityZ(e\room\Objects[3],True),True
							ResetEntity Curr173\Collider
						EndIf
						e\EventState = 1
					ElseIf e\EventState = 1
						e\SoundCHN = LoopSound2(AlarmSFX(0), e\SoundCHN, Camera, e\room\Objects[0], 5.0)
						
						If (MilliSecs2() Mod 1000)<500 Then
							ShowEntity e\room\Objects[5] 
						Else
							HideEntity e\room\Objects[5]
						EndIf
						
						dist = EntityDistance(Collider, e\room\Objects[0])
						If dist<2.0 Then 
							e\room\RoomDoors[0]\locked = True
							e\room\RoomDoors[1]\locked = True
							
							If e\EventState2=0 Then
								ShowEntity e\room\Objects[2]
								If EntityDistance(Curr173\Collider,e\room\Objects[4])<3.0
									If (BlinkTimer<-10 Or (Not EntityInView(Curr173\obj,Camera))) And Curr173\Idle = 0 Then
										PositionEntity Curr173\Collider, EntityX(e\room\Objects[4],True),0.5,EntityZ(e\room\Objects[4],True),True
										ResetEntity Curr173\Collider
										
										HideEntity e\room\Objects[2]
										
										If (Not WearingHazmat) Then
											Injuries=Injuries+0.1
											If Infect=0 Then Infect=1
											Msg = "The window shattered and a piece of glass cut your arm."
											MsgTimer = 70*8
										EndIf
										
										PlaySound2(LoadTempSound("SFX\General\GlassBreak.ogg"), Camera, e\room\Objects[0]) 
										
										e\EventState2=1
									EndIf
								EndIf
							EndIf
							
							If dist<1.0 Then
								If EntityInView(e\room\Objects[0], Camera) Then
									DrawHandIcon = True
									
									If MouseDown1 Then
										DrawArrowIcon(2) = True
										RotateEntity(e\room\Levers[0], Max(Min(EntityPitch(e\room\Levers[0])+Max(Min(-mouse_y_speed_1,10.0),-10), 89), 35), EntityYaw(e\room\Levers[0]), 0)
									EndIf
								EndIf
							EndIf
							
							If (Not WearingHazmat) And Bloodloss>0.0
								If Infect=0
									Infect=1
									DebugLog "Infected player"
								EndIf
							EndIf
						EndIf
						
						If EntityPitch(e\room\Levers[0],True)<40 Then 
							e\EventState = 2
							PlaySound_Strict LeverSFX
						Else
							p.Particles = CreateParticle(EntityX(e\room\Objects[0],True),EntityY(e\room\Objects[0],True),EntityZ(e\room\Objects[0],True), 6, 0.02, -0.12)
							RotateEntity (p\pvt,-90,0,0,True)
							TurnEntity(p\pvt, Rnd(-26,26), Rnd(-26,26), Rnd(360))
							
							p\SizeChange = 0.012
							p\Achange = -0.015
						EndIf		
					Else
						HideEntity e\room\Objects[5]
						e\room\RoomDoors[0]\locked = False
						e\room\RoomDoors[1]\locked = False
						e\room\RoomDoors[2]\locked = False
						
						RotateEntity (e\room\Levers[0],CurveAngle(1,EntityPitch(e\room\Levers[0],True),15.0),EntityYaw(e\room\Levers[0],True),0,True)
						
						If EntityPitch(e\room\Levers[0],True)=<1.0 Then
							RemoveEvent(e)
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "106victim"
				;[Block]
				If (Not Contained106) Then
					If PlayerRoom = e\room Then
						If e\EventState = 0 Then
							de.Decals = CreateDecal(0, EntityX(e\room\obj), 799.0*RoomScale, EntityZ(e\room\obj), -90, Rand(360), 0)
							de\Size = 0.05 : de\SizeChange = 0.0015 : EntityAlpha(de\obj, 0.8) : UpdateDecals()			
							PlaySound2(DecaySFX(3), Camera, de\obj, 15.0)
							e\EventState=1
						EndIf
					EndIf
					
					If e\EventState > 0 Then 
						If e\room\NPC[0]=Null Then
							e\EventState=e\EventState+FPSfactor
						EndIf
						If e\EventState>200 Then
							If e\room\NPC[0]=Null Then
								e\room\NPC[0]=CreateNPC(NPCtypeD, EntityX(e\room\obj), 900.0*RoomScale, EntityZ(e\room\obj))
								RotateEntity e\room\NPC[0]\Collider, 0, Rnd(360), 0, True
								ChangeNPCTextureID(e\room\NPC[0],5)
								e\room\NPC[0]\State=6
								
								PlaySound_Strict HorrorSFX(0)
								PlaySound2(DecaySFX(2), Camera, e\room\NPC[0]\Collider, 15.0)
							EndIf
							
							e\room\NPC[0]\FallingPickDistance = 0.0
							EntityType e\room\NPC[0]\Collider,HIT_PLAYER
							If EntityY(e\room\NPC[0]\Collider)>0.35 Then
								AnimateNPC(e\room\NPC[0], 1, 10, 0.12, False)
								dist# = EntityDistance(Collider,e\room\NPC[0]\Collider)
								If dist<0.8 Then ;get the player out of the way
									fdir# = point_direction(EntityX(Collider,True),EntityZ(Collider,True),EntityX(e\room\NPC[0]\Collider,True),EntityZ(e\room\NPC[0]\Collider,True))
									TranslateEntity Collider,Cos(-fdir+90)*(dist-0.8)*(dist-0.8),0,Sin(-fdir+90)*(dist-0.8)*(dist-0.8)
								EndIf
								
								If EntityY(e\room\NPC[0]\Collider)>0.6 Then EntityType e\room\NPC[0]\Collider,0
							Else
								e\EventState=e\EventState+FPSfactor
								AnimateNPC(e\room\NPC[0], 11, 19, 0.25, False)
								If e\Sound=0 Then 
									;e\Sound = LoadSound("SFX\General\BodyFall.ogg")
									LoadEventSound(e,"SFX\General\BodyFall.ogg")
									PlaySound_Strict e\Sound
									
									de.Decals = CreateDecal(0, EntityX(e\room\obj), 0.001, EntityZ(e\room\obj), 90, Rand(360), 0)
									de\Size = 0.4 : EntityAlpha(de\obj, 0.8) : UpdateDecals()			
								EndIf
								
								If e\EventState>400 Then
									If e\Sound<>0 Then FreeSound_Strict e\Sound : e\Sound=0
									RemoveEvent(e)
								EndIf								
							EndIf
							
						EndIf
					EndIf
					
				EndIf
				
				;[End Block]
			Case "106sinkhole"
				;[Block]
				If e\EventState=0 Then
					de.Decals = CreateDecal(0, EntityX(e\room\obj)+Rnd(-0.5,0.5), 0.01, EntityZ(e\room\obj)+Rnd(-0.5,0.5), 90, Rand(360), 0)
					de\Size = 2.5 : ScaleSprite(de\obj, de\Size, de\Size);
					
					e\EventState=1
				ElseIf PlayerRoom = e\room
					If e\Sound=0 Then
						e\Sound=LoadSound_Strict("SFX\Room\Sinkhole.ogg")
					Else
						e\SoundCHN = LoopSound2(e\Sound, e\SoundCHN, Camera, e\room\obj, 4.5, 1.5)
					EndIf
					dist = Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\obj),EntityZ(e\room\obj))
					If dist < 2.0 Then
						CurrStepSFX=1
						CurrSpeed = CurveValue(0.0, CurrSpeed, Max(dist*50,1.0))	
						CrouchState = (2.0-dist)/2.0
						
						If dist<0.5 Then
							If e\EventState2 = 0 Then
								PlaySound_Strict(LoadTempSound("SFX\Room\SinkholeFall.ogg"))
							EndIf
							
							CurrSpeed = CurveValue(0.0, CurrSpeed, Max(dist*50,1.0))
							
							x = CurveValue(EntityX(e\room\obj),EntityX(Collider),10.0)
							y = CurveValue(EntityY(e\room\obj)-e\EventState2,EntityY(Collider),25.0)
							z = CurveValue(EntityZ(e\room\obj),EntityZ(Collider),10.0)
							PositionEntity Collider, x, y, z, True
							
							DropSpeed = 0
							
							ResetEntity Collider
							
							e\EventState2=Min(e\EventState2+FPSfactor/200.0,2.0)
							
							LightBlink = Min(e\EventState2*5,10.0)
							BlurTimer = e\EventState2*500
							
							If e\EventState2 = 2.0 Then MoveToPocketDimension()
						EndIf
					EndIf
				Else 
					e\EventState2=0
				EndIf
				
				;[End Block]
				
			Case "682roar"
				;[Block]
				If e\EventState = 0 Then
					If PlayerRoom = e\room Then e\EventState = 70 * Rand(300,1000)
				ElseIf PlayerRoom\RoomTemplate\Name <> "pocketdimension" And PlayerRoom\RoomTemplate\Name <> "room860" And PlayerRoom\RoomTemplate\Name <> "room1123" And PlayerRoom\RoomTemplate\Name <> "dimension1499" 
					e\EventState = e\EventState-FPSfactor
					
					If e\EventState < 17*70 Then
						If	e\EventState+FPSfactor => 17*70 Then LoadEventSound(e,"SFX\SCP\682\Roar.ogg") : e\SoundCHN = PlaySound_Strict(e\Sound) ;e\Sound = LoadSound_Strict("SFX\SCP\682\Roar.ogg")
						If e\EventState > 17*70 - 3*70 Then CameraShake = 0.5
						If e\EventState < 17*70 - 7.5*70 And e\EventState > 17*70 - 11*70 Then CameraShake = 2.0				
						If e\EventState < 70 Then 
							If e\Sound<>0 Then FreeSound_Strict (e\Sound) 
							RemoveEvent(e)
						EndIf
					EndIf
				EndIf
				;[End Block]
				
			Case "914"
				;[Block]
				If PlayerRoom = e\room Then
					;GiveAchievement(Achv914)
					
					If e\room\RoomDoors[2]\open
						GiveAchievement(Achv914)
						e\EventState2=1
					EndIf
					
					If e\EventState2=1
						ShouldPlay = 22
					EndIf
					
					EntityPick(Camera, 1.0)
					If PickedEntity() = e\room\Objects[0] Then
						DrawHandIcon = True
						If MouseHit1 Then GrabbedEntity = e\room\Objects[0]
					ElseIf PickedEntity() = e\room\Objects[1]
						DrawHandIcon = True
						If MouseHit1 Then GrabbedEntity = e\room\Objects[1]
					EndIf
					
					If MouseDown1 Or MouseHit1 Then
						If GrabbedEntity <> 0 Then ;avain
							If GrabbedEntity = e\room\Objects[0] Then
								If e\EventState = 0 Then
									DrawHandIcon = True
									TurnEntity(GrabbedEntity, 0, 0, -mouse_x_speed_1 * 2.5)
									
									angle = WrapAngle(EntityRoll(e\room\Objects[0]))
									If angle > 181 Then DrawArrowIcon(3) = True
									DrawArrowIcon(1) = True
									
									If angle < 90 Then
										RotateEntity(GrabbedEntity, 0, 0, 361.0)
									ElseIf angle < 180
										RotateEntity(GrabbedEntity, 0, 0, 180)
									EndIf
									
									If angle < 181 And angle > 90 Then
										For it.Items = Each Items
											If it\collider <> 0 And it\Picked = False Then
												If Abs(EntityX(it\collider) - (e\room\x - 712.0 * RoomScale)) < 200.0 Then
													If Abs(EntityY(it\collider) - (e\room\y + 648.0 * RoomScale)) < 104.0 Then
														e\EventState = 1
														e\SoundCHN = PlaySound2(MachineSFX, Camera, e\room\Objects[1])
														e\room\RoomDoors[1]\SoundCHN = PlaySound2(LoadTempSound("SFX\SCP\914\DoorClose.ogg"), Camera, e\room\RoomDoors[1]\obj)
														Exit
													EndIf
												End If
											End If
										Next
									EndIf
								End If
							ElseIf GrabbedEntity = e\room\Objects[1]
								If e\EventState = 0 Then
									DrawHandIcon = True
									TurnEntity(GrabbedEntity, 0, 0, -mouse_x_speed_1 * 2.5)
									
									angle# = WrapAngle(EntityRoll(e\room\Objects[1]))
									DrawArrowIcon(3) = True
									DrawArrowIcon(1) = True
									
									If angle > 90 Then
										If angle < 180 Then
											RotateEntity(GrabbedEntity, 0, 0, 90.0)
										ElseIf angle < 270
											RotateEntity(GrabbedEntity, 0, 0, 270)
										EndIf
									EndIf
									
								End If
							End If
						End If
					Else
						GrabbedEntity = 0
					End If
					
					Local setting$ = ""
					
					If GrabbedEntity <> e\room\Objects[1] Then
						angle# = WrapAngle(EntityRoll(e\room\Objects[1]))
						If angle < 22.5 Then
							angle = 0
							setting = "1:1"
						ElseIf angle < 67.5
							angle = 40
							setting = "coarse"
						ElseIf angle < 180
							angle = 90
							setting = "rough"
						ElseIf angle > 337.5
							angle = 359 - 360
							setting = "1:1"
						ElseIf angle > 292.5
							angle = 320 - 360
							setting = "fine"
						Else
							angle = 270 - 360
							setting = "very fine"
						End If
						RotateEntity(e\room\Objects[1], 0, 0, CurveValue(angle, EntityRoll(e\room\Objects[1]), 20))
					EndIf
					
					For i% = 0 To 1
						If GrabbedEntity = e\room\Objects[i] Then
							If Not EntityInView(e\room\Objects[i], Camera) Then
								GrabbedEntity = 0
							ElseIf EntityDistance(e\room\Objects[i], Camera) > 1.0
								GrabbedEntity = 0
							End If
						End If
					Next
					
					If e\EventState > 0 Then
						e\EventState = e\EventState + FPSfactor
						
						
						e\room\RoomDoors[1]\open = False
						If e\EventState > 70 * 2 Then
							If e\room\RoomDoors[0]\open=True Then
								e\room\RoomDoors[0]\SoundCHN = PlaySound2(LoadTempSound("SFX\SCP\914\DoorClose.ogg"), Camera, e\room\RoomDoors[0]\obj)
							EndIf
							
							e\room\RoomDoors[0]\open = False
						EndIf
						
						If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (170.0 * RoomScale) Then
							
							If setting = "rough" Or setting = "coarse" Then
								If e\EventState > 70 * 2.6 And e\EventState - FPSfactor2 < 70 * 2.6 Then PlaySound_Strict Death914SFX
							EndIf
							
							If e\EventState > 70 * 3 Then
								Select setting
									Case "rough"
										KillTimer = Min(-1, KillTimer)
										BlinkTimer = -10
										If e\SoundCHN <> 0 Then StopChannel e\SoundCHN
										DeathMSG = Chr(34)+"A heavily mutilated corpse found inside the output booth of SCP-914. DNA testing identified the corpse as Class D Subject D-9341. "
										DeathMSG = DeathMSG + "The subject had obviously been "+Chr(34)+"refined"+Chr(34)+" by SCP-914 on the "+Chr(34)+"Rough"+Chr(34)+" setting, but we are still confused as to how he "
										DeathMSG = DeathMSG + "ended up inside the intake booth and who or what wound the key."+Chr(34)
									Case "coarse"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound_Strict Use914SFX
									Case "1:1"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound_Strict Use914SFX
									Case "fine", "very fine"
										BlinkTimer = -10
										If e\EventState - FPSfactor2 < 70 * 3 Then PlaySound_Strict Use914SFX	
								End Select
							End If
						EndIf
						
						If e\EventState > (6 * 70) Then	
							RotateEntity(e\room\Objects[0], EntityPitch(e\room\Objects[0]), EntityYaw(e\room\Objects[0]), CurveAngle(0, EntityRoll(e\room\Objects[0]),10.0))
						Else
							RotateEntity(e\room\Objects[0], EntityPitch(e\room\Objects[0]), EntityYaw(e\room\Objects[0]), 180)
						EndIf
						
						If e\EventState > (12 * 70) Then							
							For it.Items = Each Items
								If it\collider <> 0 And it\Picked = False Then
									If Distance(EntityX(it\collider), EntityZ(it\collider), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (180.0 * RoomScale) Then
										Use914(it, setting, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True), EntityZ(e\room\Objects[3], True))
										
									End If
								End If
							Next
							
							If Distance(EntityX(Collider), EntityZ(Collider), EntityX(e\room\Objects[2], True), EntityZ(e\room\Objects[2], True)) < (160.0 * RoomScale) Then
								Select setting
									Case "coarse"
										Injuries = 4.0
										Msg = "You notice countless small incisions all around your body. They are bleeding heavily."
										MsgTimer = 70*8
									Case "1:1"
										InvertMouse = (Not InvertMouse)
									Case "fine", "very fine"
										SuperMan = True
								End Select
								BlurTimer = 1000
								PositionEntity(Collider, EntityX(e\room\Objects[3], True), EntityY(e\room\Objects[3], True) + 1.0, EntityZ(e\room\Objects[3], True))
								ResetEntity(Collider)
								DropSpeed = 0
							EndIf								
							
							e\room\RoomDoors[0]\open = True
							e\room\RoomDoors[1]\open = True
							RotateEntity(e\room\Objects[0], 0, 0, 0)
							e\EventState = 0
							
							Local opensfx914 = LoadTempSound("SFX\SCP\914\DoorOpen.ogg")
							e\room\RoomDoors[0]\SoundCHN = PlaySound2(opensfx914, Camera, e\room\RoomDoors[0]\obj)
							e\room\RoomDoors[1]\SoundCHN = PlaySound2(opensfx914, Camera, e\room\RoomDoors[1]\obj)
						End If
					End If
					
				EndIf
				UpdateSoundOrigin(e\SoundCHN,Camera,e\room\Objects[1])
				;[End Block]
			Case "1048a"
				;[Block]
				
				If e\room\Objects[0]=0 Then
					If PlayerRoom<>e\room And BlinkTimer<-10 Then
						dist = Distance(EntityX(Collider),EntityZ(Collider), EntityX(e\room\obj),EntityZ(e\room\obj))
						If (dist<16.0) Then
							For e2.Events = Each Events
								If e2\EventName = e\EventName Then
									If e2\room <> e\room Then
										If e2\room\Objects[0]<>0 Then
											e\room\Objects[0]=CopyEntity(e2\room\Objects[0])
											Exit
										EndIf
									EndIf
								EndIf
							Next
							If e\room\Objects[0]=0 Then
								e\room\Objects[0] =	LoadAnimMesh_Strict("GFX\npcs\scp-1048a.b3d")
							EndIf
							ScaleEntity e\room\Objects[0], 0.05,0.05,0.05
							SetAnimTime(e\room\Objects[0], 2)
							PositionEntity(e\room\Objects[0], EntityX(e\room\obj), 0.0, EntityZ(e\room\obj))
							
							RotateEntity(e\room\Objects[0], -90.0, Rnd(0.0, 360.0), 0.0)
							
							e\Sound = LoadSound_Strict("SFX\SCP\1048A\Shriek.ogg")
							e\Sound2 = LoadSound_Strict("SFX\SCP\1048A\Growth.ogg")
							
							e\EventState = 1
						EndIf
					EndIf
				Else
					e\EventState3 = 1
					Select e\EventState
						Case 1
							Animate2(e\room\Objects[0], AnimTime(e\room\Objects[0]), 2.0, 395.0, 1.0)
							
							If (EntityDistance(Collider, e\room\Objects[0])<2.5) Then e\EventState = 2
						Case 2
							Local prevFrame# = AnimTime(e\room\Objects[0]) 
							Animate2(e\room\Objects[0], prevFrame, 2.0, 647.0, 1.0, False)
							
							If (prevFrame <= 400.0 And AnimTime(e\room\Objects[0])>400.0) Then
								e\SoundCHN = PlaySound_Strict(e\Sound)
							EndIf
							
							Local volume# = Max(1.0 - Abs(prevFrame - 600.0)/100.0, 0.0)
							
							BlurTimer = volume*1000.0
							CameraShake = volume*10.0
							
							PointEntity(e\room\Objects[0], Collider)
							RotateEntity(e\room\Objects[0], -90.0, EntityYaw(e\room\Objects[0]), 0.0)
							
							If (prevFrame>646.0) Then
								If (PlayerRoom = e\room) Then
									e\EventState = 3	
									PlaySound_Strict e\Sound2
									
									Msg = "Something is growing all around your body."
									MsgTimer = 70.0 * 3.0
								Else
									e\EventState3 = 70*30
								EndIf
							EndIf
						Case 3
							e\EventState2 = e\EventState2 + FPSfactor
							
							BlurTimer = e\EventState2*2.0
							
							If (e\EventState2>250.0 And e\EventState2-FPSfactor <= 250.0) Then
								Select Rand(3)
									Case 1
										Msg = "Ears are growing all over your body."
									Case 2
										Msg = "Ear-like organs are growing all over your body."
									Case 3
										Msg = "Ears are growing all over your body. They are crawling on your skin."
								End Select
								
								MsgTimer = 70.0 * 3.0
							Else If (e\EventState2>600.0 And e\EventState2-FPSfactor <= 600.0)
								Select Rand(4)
									Case 1
										Msg = "It is becoming difficult to breathe."
									Case 2
										Msg = "You have excellent hearing now. Also, you are dying."
									Case 3
										Msg = "The ears are growing inside your body."
									Case 4
										Msg = Chr(34)+"Can't... Breathe..."+Chr(34)
								End Select
								
								MsgTimer = 70.0 * 5.0
							EndIf
							
							If (e\EventState2>70*15) Then
								DeathMSG = "A dead body covered in ears was found in [REDACTED]. Subject was presumably attacked by an instance of SCP-1048-A and suffocated to death by the ears. "
								DeathMSG = DeathMSG + "Body was sent for autopsy."
								Kill()
								e\EventState = 4
								RemoveEvent(e)
							EndIf
					End Select 
					
					If (e <> Null) Then
						If PlayerRoom <> e\room Then
							If e\EventState3>0 Then
								e\EventState3 = e\EventState3+FPSfactor
								
								If e\EventState3>70*25 Then
									FreeEntity(e\room\Objects[0])
									e\room\Objects[0]=0
									RemoveEvent(e)
								EndIf
							EndIf
						EndIf
					EndIf
				EndIf
				
				;[End Block]
			;New Events in SCP:CB version 1.3 - ENDSHN
			Case "room4tunnels"
				;[Block]
				If e\room\dist < 10.0 And e\room\dist > 0 Then
					e\room\NPC[0]=CreateNPC(NPCtypeD, EntityX(e\room\obj,True)+1.0, 0.5, EntityZ(e\room\obj,True)+1.0)
					e\room\NPC[0]\texture = "GFX\npcs\body1.jpg"
					tex = LoadTexture_Strict(e\room\NPC[0]\texture)
					EntityTexture(e\room\NPC[0]\obj, tex)
					FreeTexture tex
					;EntityTexture e\room\NPC[0]\obj,NTF_BodyTextures(0)
					
					RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\obj)-(Rand(20,60)),0, True	
					
					SetNPCFrame e\room\NPC[0], 19
					e\room\NPC[0]\State=8
					
					;Delete e
					RemoveEvent(e)
				EndIf
				;[End Block]
			Case "room2gw_b"
				;[Block]
				If e\room\dist < 8
					If e\EventState = 0 Then
						e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[2],True), EntityY(e\room\Objects[2],True)+0.5, EntityZ(e\room\Objects[2],True))
						PointEntity e\room\NPC[0]\Collider, e\room\obj
						RotateEntity e\room\NPC[0]\Collider, 0, EntityYaw(e\room\NPC[0]\Collider),0, True
						SetNPCFrame(e\room\NPC[0], 288)
						e\room\NPC[0]\State = 8
						
						e\EventState = 1
					EndIf
					
					;WIP
					p.Particles = CreateParticle(EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True), 6, 0.2, 0, 10)
					p\speed = 0.01
					RotateEntity(p\pvt, -60, e\room\angle-90, 0)
					
					p\Achange = -0.02
					
					e\SoundCHN = LoopSound2(AlarmSFX(3),e\SoundCHN,Camera,e\room\Objects[3],5)
				EndIf
				;[End Block]
			Case "room2scps2"
				;[Block]
				;If PlayerRoom = e\room
				If e\room\dist < 15
					If Contained106 Then e\EventState = 2.0
					If Curr106\State < 0 Then e\EventState = 2.0
					
					If e\EventState < 2.0
						If e\EventState = 0.0
							LoadEventSound(e,"SFX\Character\Scientist\EmilyScream.ogg")
							e\SoundCHN = PlaySound2(e\Sound, Camera, e\room\Objects[0], 100, 1.0)
							de.Decals = CreateDecal(0, EntityX(e\room\Objects[0],True), e\room\y+2.0*RoomScale, EntityZ(e\room\Objects[0],True), 90, Rand(360), 0)
							de\Size = 0.5 : EntityAlpha(de\obj, 0.8)
							EntityFX de\obj,1
							e\EventState = 1.0
						ElseIf e\EventState = 1.0
							If (Not ChannelPlaying(e\SoundCHN))
								e\EventState = 2.0
								e\room\RoomDoors[0]\locked = False
							Else
								UpdateSoundOrigin(e\SoundCHN,Camera,e\room\Objects[0],100,1.0)
							EndIf
						EndIf
					Else
						DebugLog "Removed 'room2scps2' event"
						e\room\RoomDoors[0]\locked = False
						de.Decals = CreateDecal(0, EntityX(e\room\Objects[0],True), e\room\y+2.0*RoomScale, EntityZ(e\room\Objects[0],True), 90, Rand(360), 0)
						de\Size = 0.5 : EntityAlpha(de\obj, 0.8)
						EntityFX de\obj,1
						RemoveEvent(e)
					EndIf
				EndIf
				;[End Block]
			Case "room1162"
				;[Block]
				;e\EventState = A variable to determine the "nostalgia" items
				;- 0.0 = No nostalgia item
				;- 1.0 = Lost key
				;- 2.0 = Disciplinary Hearing DH-S-4137-17092
				;- 3.0 = Coin
				;- 4.0 = Movie Ticket
				;- 5.0 = Old Badge
				;e\EventState2 = Defining which slot from the Inventory should be picked
				;e\EventState3 = A check for if a item should be removed
				;- 0.0 = no item "trade" will happen
				;- 1.0 = item "trade" will happen
				;- 2.0 = the player doesn't has any items in the Inventory, giving him heavily Injuries and giving him a random item
				;- 3.0 = player got a memorial item (to explain a bit D-9341's background)
				;- 3.1 = player got a memorial item + injuries (because he didn't had any item in his inventory before)
				If PlayerRoom = e\room
					
					GrabbedEntity = 0
					
					e\EventState = 0
					
					Local Pick1162% = True
					Local pp% = CreatePivot(e\room\obj)
					PositionEntity pp,976,128,-640,False
					
					For it.Items = Each Items
						If (Not it\Picked)
							If EntityDistance(it\collider,e\room\Objects[0])<0.75
								Pick1162% = False
							EndIf
						EndIf
					Next
					
					If EntityDistance(e\room\Objects[0],Collider)<0.75 And Pick1162%
						DrawHandIcon = True
						If MouseHit1 Then GrabbedEntity = e\room\Objects[0]
					EndIf
					
					If GrabbedEntity <> 0
						e\EventState2 = Rand(0,MaxItemAmount-1)
						If Inventory(e\EventState2)<>Null
							;randomly picked item slot has an item in it, using this slot
							e\EventState3 = 1.0
							DebugLog "pick1"
						Else
							;randomly picked item slot is empty, getting the first available slot
							For i = 0 To MaxItemAmount-1
								Local isSlotEmpty% = (Inventory((i+e\EventState2) Mod MaxItemAmount) = Null)
								
								If (Not isSlotEmpty) Then
									;successful
									e\EventState2 = (i+e\EventState2) Mod MaxItemAmount
								EndIf
								
								If Rand(8)=1 Then
									If isSlotEmpty Then
										e\EventState3 = 3.1
									Else
										e\EventState3 = 3.0
									EndIf
									
									e\EventState = Rand(1,5)
									
									;Checking if the selected nostalgia item already exists or not
									Local itemName$ = ""
									Select (e\EventState)
										Case 1
											itemName = "Lost Key"
										Case 2
											itemName = "Disciplinary Hearing DH-S-4137-17092"
										Case 3
											itemName = "Coin"
										Case 4
											itemName = "Movie Ticket"
										Case 5
											itemName = "Old Badge"
									End Select
									
									Local itemExists% = False
									For it.Items = Each Items
										If (it\name = itemName) Then
											itemExists = True
											e\EventState3 = 1.0
											e\EventState = 0.0
											Exit
										EndIf
									Next
									
									If ((Not itemExists) And (Not isSlotEmpty)) Exit
								Else
									If isSlotEmpty Then
										e\EventState3 = 2.0
									Else
										e\EventState3 = 1.0
										Exit
									EndIf
								EndIf
							Next
						EndIf
					EndIf
					
					
					;trade successful
					If e\EventState3 = 1.0
						Local shouldCreateItem% = False
						
						For itt.ItemTemplates = Each ItemTemplates
							If (IsItemGoodFor1162(itt)) Then
								Select Inventory(e\EventState2)\itemtemplate\tempname
									Case "key"
										If itt\tempname = "key1" Or itt\tempname = "key2" And Rand(2)=1
											shouldCreateItem = True
											DebugLog "lostkey"
										EndIf
									Case "paper","oldpaper"
										If itt\tempname = "paper" And Rand(12)=1 Then
											shouldCreateItem = True
											DebugLog "paper"
										EndIf
									Case "gasmask","gasmask3","supergasmask","hazmatsuit","hazmatsuit2","hazmatsuit3"
										If itt\tempname = "gasmask" Or itt\tempname = "gasmask3" Or itt\tempname = "supergasmask" Or itt\tempname = "hazmatsuit" Or itt\tempname = "hazmatsuit2" Or itt\tempname = "hazmatsuit3" And Rand(2)=1
											shouldCreateItem = True
											DebugLog "gasmask hazmat"
										EndIf
									Case "key1","key2","key3"
										If itt\tempname = "key1" Or itt\tempname = "key2" Or itt\tempname = "key3" Or itt\tempname = "misc" And Rand(6)=1
											shouldCreateItem = True
											DebugLog "key"
										EndIf
									Case "vest","finevest"
										If itt\tempname = "vest" Or itt\tempname = "finevest" And Rand(1)=1
											shouldCreateItem = True
											DebugLog "vest"
										EndIf
									Default
										If itt\tempname = "misc" And Rand(6)=1
											shouldCreateItem = True
											DebugLog "default"
										EndIf
								End Select
							EndIf
							
							If (shouldCreateItem) Then
								RemoveItem(Inventory(e\EventState2))
								it=CreateItem(itt\name,itt\tempname,EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
								EntityType(it\collider, HIT_ITEM)
								PlaySound_Strict LoadTempSound("SFX\SCP\1162\Exchange"+Rand(0,4)+".ogg")
								e\EventState3 = 0.0
								
								
								GiveAchievement(Achv1162)
								MouseHit1 = False
								Exit
							EndIf
						Next
					;trade not sucessful (player got in return to injuries a new item)
					ElseIf e\EventState3 = 2.0
						Injuries = Injuries + 5.0
						pvt = CreatePivot()
						PositionEntity pvt, EntityX(Collider),EntityY(Collider)-0.05,EntityZ(Collider)
						TurnEntity pvt, 90, 0, 0
						EntityPick(pvt,0.3)
						de.decals = CreateDecal(3, PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
						de\size = 0.75 : ScaleSprite de\obj, de\size, de\size
						FreeEntity pvt
						For itt.ItemTemplates = Each ItemTemplates
							If IsItemGoodFor1162(itt) And Rand(6)=1
								it = CreateItem(itt\name, itt\tempname, EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
								EntityType(it\collider, HIT_ITEM)
								GiveAchievement(Achv1162)
								MouseHit1 = False
								e\EventState3 = 0.0
								If Injuries > 15
									DeathMSG = "A dead Class D subject was discovered within the containment chamber of SCP-1162."
									DeathMSG = DeathMSG + " An autopsy revealed that his right lung was missing, which suggests"
									DeathMSG = DeathMSG + " interaction with SCP-1162."
									PlaySound_Strict LoadTempSound("SFX\SCP\1162\BodyHorrorExchange"+Rand(1,4)+".ogg")
									LightFlash = 5.0
									Kill()
								Else
									PlaySound_Strict LoadTempSound("SFX\SCP\1162\BodyHorrorExchange"+Rand(1,4)+".ogg")
									LightFlash = 5.0
									Msg = "You feel a sudden overwhelming pain in your chest."
									MsgTimer = 70*5
								EndIf
								Exit
							EndIf
						Next
					;trade with nostalgia item
					ElseIf e\EventState3 >= 3.0
						If e\EventState3 < 3.1
							PlaySound_Strict LoadTempSound("SFX\SCP\1162\Exchange"+Rand(0,4)+".ogg")
							RemoveItem(Inventory(e\EventState2))
						Else
							Injuries = Injuries + 5.0
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(Collider),EntityY(Collider)-0.05,EntityZ(Collider)
							TurnEntity pvt, 90, 0, 0
							EntityPick(pvt,0.3)
							de.decals = CreateDecal(3, PickedX(), PickedY()+0.005, PickedZ(), 90, Rand(360), 0)
							de\size = 0.75 : ScaleSprite de\obj, de\size, de\size
							FreeEntity pvt
							If Injuries > 15
								DeathMSG = "A dead Class D subject was discovered within the containment chamber of SCP-1162."
								DeathMSG = DeathMSG + " An autopsy revealed that his right lung was missing, which suggests"
								DeathMSG = DeathMSG + " interaction with SCP-1162."
								PlaySound_Strict LoadTempSound("SFX\SCP\1162\BodyHorrorExchange"+Rand(1,4)+".ogg")
								LightFlash = 5.0
								Kill()
							Else
								PlaySound_Strict LoadTempSound("SFX\SCP\1162\BodyHorrorExchange"+Rand(1,4)+".ogg")
								LightFlash = 5.0
								Msg = "You notice something moving in your pockets and a sudden pain in your chest."
								MsgTimer = 70*5
							EndIf
							e\EventState2 = 0.0
						EndIf
						Select e\EventState
							Case 1
								it = CreateItem("Lost Key","key",EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
							Case 2
								it = CreateItem("Disciplinary Hearing DH-S-4137-17092","oldpaper",EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
							Case 3
								it = CreateItem("Coin","coin",EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
							Case 4
								it = CreateItem("Movie Ticket","ticket",EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
							Case 5
								it = CreateItem("Old Badge","badge",EntityX(pp,True),EntityY(pp,True),EntityZ(pp,True))
						End Select
						EntityType(it\collider, HIT_ITEM)
						GiveAchievement(Achv1162)
						MouseHit1 = False
						e\EventState3 = 0.0
					EndIf
					FreeEntity pp
				EndIf
				;[End Block]
			Case "room_gw"
				;[Block]
				;e\EventState: Determines if the airlock is in operation or not
				;e\EventState2: The timer for the airlocks
				;e\EventState3: Checks if the player had left the airlock or not
				
				e\room\RoomDoors[0]\locked = True
				e\room\RoomDoors[1]\locked = True
				
				Local brokendoor% = False
				If e\room\Objects[1]<>0 Then brokendoor% = True
				
				If PlayerRoom = e\room
					If e\EventState = 0.0
						If EntityDistance(e\room\Objects[0],Collider)<1.4 And e\EventState3 = 0.0
							e\EventState = 1.0
							If brokendoor
								If e\Sound2 <> 0 Then FreeSound_Strict(e\Sound2) : e\Sound2 = 0
								e\Sound2 = LoadSound_Strict("SFX\Door\DoorSparks.ogg")
								e\SoundCHN2 = PlaySound2(e\Sound2,Camera,e\room\Objects[1],5)
							EndIf
							StopChannel e\SoundCHN
							e\SoundCHN = 0
							If e\Sound <> 0 Then FreeSound_Strict(e\Sound) : e\Sound = 0
							e\Sound = LoadSound_Strict("SFX\Door\Airlock.ogg")
							e\room\RoomDoors[0]\locked = False
							e\room\RoomDoors[1]\locked = False
							UseDoor(e\room\RoomDoors[0])
							UseDoor(e\room\RoomDoors[1])
							PlaySound_Strict(AlarmSFX(4))
						ElseIf EntityDistance(e\room\Objects[0],Collider)>2.4
							e\EventState3 = 0.0
						EndIf
					Else
						If e\EventState2 < 70*7
							e\EventState2 = e\EventState2 + FPSfactor
							e\room\RoomDoors[0]\open = False
							e\room\RoomDoors[1]\open = False
							If e\EventState2 < 70*1
								
								If brokendoor
									pvt% = CreatePivot()
									Local d_ent% = e\room\Objects[1]
									PositionEntity(pvt, EntityX(d_ent%,True), EntityY(d_ent%,True)+Rnd(0.0,0.05), EntityZ(d_ent%,True))
									RotateEntity(pvt, 0, EntityYaw(d_ent%,True)+90, 0)
									MoveEntity pvt,0,0,0.2
									
									If ParticleAmount > 0
										For i = 0 To (1+(2*(ParticleAmount-1)))
											p.Particles = CreateParticle(EntityX(pvt), EntityY(pvt), EntityZ(pvt), 7, 0.002, 0, 25)
											p\speed = Rnd(0.01,0.05)
											;RotateEntity(p\pvt, Rnd(-20, 20), Rnd(360), 0)
											RotateEntity(p\pvt, Rnd(-45,0), EntityYaw(pvt)+Rnd(-10.0,10.0), 0)
											
											p\size = 0.0075
											ScaleSprite p\obj,p\size,p\size
											
											;EntityOrder p\obj,-1
											
											p\Achange = -0.05
										Next
									EndIf
									
									FreeEntity pvt
								EndIf
								
							ElseIf e\EventState2 > 70*3 And e\EventState < 70*5.5
								pvt% = CreatePivot(e\room\obj)								
								For i = 0 To 1
									If e\room\RoomTemplate\Name$ = "room3gw"
										If i = 0
											PositionEntity pvt%,-288.0,416.0,320.0,False
										Else
											PositionEntity pvt%,192.0,416.0,320.0,False
										EndIf
									Else
										If i = 0
											PositionEntity pvt%,312.0,416.0,-128.0,False
										Else
											PositionEntity pvt%,312.0,416.0,224.0,False
										EndIf
									EndIf
									
									p.Particles = CreateParticle(EntityX(pvt,True), EntityY(pvt,True), EntityZ(pvt,True),  6, 0.8, 0, 50)
									p\speed = 0.025
									RotateEntity(p\pvt, 90, 0, 0)
									
									p\Achange = -0.02
								Next
								
								FreeEntity pvt
								If e\SoundCHN = 0 Then e\SoundCHN = PlaySound2(e\Sound,Camera,e\room\Objects[0],5)
							EndIf
						Else
							e\EventState = 0.0
							e\EventState2 = 0.0
							e\EventState3 = 1.0
							If e\room\RoomDoors[0]\open = False
								e\room\RoomDoors[0]\locked = False
								e\room\RoomDoors[1]\locked = False
								UseDoor(e\room\RoomDoors[0])
								UseDoor(e\room\RoomDoors[1])
							EndIf
						EndIf
					EndIf
					
					If brokendoor
						If ChannelPlaying(e\SoundCHN2)
							UpdateSoundOrigin(e\SoundCHN2,Camera,e\room\Objects[1],5)
						EndIf
					EndIf
					If ChannelPlaying(e\SoundCHN)
						UpdateSoundOrigin(e\SoundCHN,Camera,e\room\Objects[0],5)
					EndIf
				Else
					e\EventState3 = 0.0
				EndIf
				;[End Block]
			Case "room2sl"
				;[Block]
				;e\EventState: Determines if the player already entered the room or not (0 = No, 1 = Yes)
				;e\EventState2: Variable used for the SCP-049 event
				;e\EventState3: Checks if Lever is activated or not
				
				;Camera-Spawning Code + SCP-049-Spawning (will now be loaded in the QuickLoadEvents function)
				;[Block]
				If PlayerRoom = e\room
					If e\EventStr = "" And QuickLoadPercent = -1
						QuickLoadPercent = 0
						QuickLoad_CurrEvent = e
						e\EventStr = 0
					EndIf
				EndIf
				;[End Block]
				
				;SCP-049
				;[Block]
				If e\EventState = 1
					If e\EventState2 < 0
						If e\EventState2 = -(70*5)
							For sc.SecurityCams = Each SecurityCams
								If sc\room = e\room
									If EntityDistance(sc\ScrObj,Camera)<5.0
										If EntityVisible(sc\ScrObj,Camera)
											e\EventState2 = Min(e\EventState2+FPSfactor,0)
											Exit
										EndIf
									EndIf
								EndIf
							Next
						Else
							e\EventState2 = Min(e\EventState2+FPSfactor,0)
						EndIf
					ElseIf e\EventState2 = 0
						If e\room\NPC[0] <> Null
							;PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\NPC[0]\Collider),EntityY(e\room\Objects[7],True),EntityZ(e\room\NPC[0]\Collider)
							Local AdjDist1# = 0.0
							Local AdjDist2# = 0.0
							Local Adj1% = -1
							Local Adj2% = -1
							For i = 0 To 3
								If e\room\AdjDoor[i]<>Null
									If Adj1 = -1
										AdjDist1# = EntityDistance(e\room\Objects[7],e\room\AdjDoor[i]\frameobj)
										Adj1 = i
									Else
										AdjDist2# = EntityDistance(e\room\Objects[7],e\room\AdjDoor[i]\frameobj)
										Adj2 = i
									EndIf
								EndIf
							Next
							If AdjDist1# > AdjDist2#
								PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\AdjDoor[Adj1]\frameobj),EntityY(e\room\Objects[7],True),EntityZ(e\room\AdjDoor[Adj1]\frameobj)
							Else
								PositionEntity e\room\NPC[0]\Collider,EntityX(e\room\AdjDoor[Adj2]\frameobj),EntityY(e\room\Objects[7],True),EntityZ(e\room\AdjDoor[Adj2]\frameobj)
							EndIf
							PointEntity e\room\NPC[0]\Collider,e\room\obj
							MoveEntity e\room\NPC[0]\Collider,0,0,-1
							ResetEntity e\room\NPC[0]\Collider
							e\room\NPC[0]\HideFromNVG = False
							e\room\NPC[0]\PathX = EntityX(e\room\NPC[0]\Collider)
							e\room\NPC[0]\PathZ = EntityZ(e\room\NPC[0]\Collider)
							e\room\NPC[0]\State = 5
							DebugLog "aaaaaaaaa"
							e\EventState2 = 1
						EndIf
					ElseIf e\EventState2 = 1
						If e\room\NPC[0]\PathStatus <> 1
							e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(e\room\Objects[15],True),EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True))
						Else
							DebugLog "bbbbbbbbb"
							e\EventState2 = 2
						EndIf
					ElseIf e\EventState2 = 2
						If e\room\NPC[0]\PathStatus <> 1
							e\room\NPC[0]\State3 = 1.0
							e\EventState2 = 3
							e\room\NPC[0]\PathTimer# = 0.0
							DebugLog "ccccccccc"
						Else
							If EntityDistance(e\room\NPC[0]\Collider,e\room\RoomDoors[0]\frameobj) < 5.0
								e\room\RoomDoors[0]\locked = True
								e\room\RoomDoors[1]\locked = True
								If e\room\NPC[0]\Reload = 0
									PlaySound_Strict LoadTempSound("SFX\Door\DoorOpen079.ogg")
									DebugLog "079 - OPEN DOORS IN ROOM2SL"
									e\room\NPC[0]\Reload = 1
								EndIf
								If (Not e\room\RoomDoors[0]\open)
									e\room\RoomDoors[0]\open = True
									sound=Rand(0, 2)
									PlaySound2(OpenDoorSFX(0,sound),Camera,e\room\RoomDoors[0]\obj)
								EndIf
								If (Not e\room\RoomDoors[1]\open)
									e\room\RoomDoors[1]\open = True
									sound=Rand(0, 2)
									PlaySound2(OpenDoorSFX(0,sound),Camera,e\room\RoomDoors[1]\obj)
								EndIf
							EndIf
							If e\room\NPC[0]\Reload = 1
								e\room\NPC[0]\DropSpeed = 0
							EndIf
						EndIf
						
						If e\room\NPC[0]\State <> 5
							e\EventState2 = 7
							DebugLog "fffffffff"
						EndIf
					ElseIf e\EventState2 = 3
						If e\room\NPC[0]\State <> 5
							e\EventState2 = 7
							DebugLog "fffffffff"
						EndIf
						
						If MeNPCSeesPlayer(e\room\NPC[0],True)=2
							e\EventState2 = 4
							DebugLog "ddddddddd"
						EndIf
						
						If e\room\NPC[0]\PathStatus <> 1
							;If e\room\NPC[0]\PathTimer# < 70*3
							If e\room\NPC[0]\PathTimer# = 0.0
								;e\room\NPC[0]\PathTimer# = e\room\NPC[0]\PathTimer# + FPSfactor
								If e\room\NPC[0]\PrevState = 1 Then
									If (e\room\NPC[0]\SoundChn2 = 0) Then
										e\room\NPC[0]\Sound2 = LoadSound_Strict("SFX\SCP\049\Room2SL1.ogg")
										e\room\NPC[0]\SoundChn2 = PlaySound2(e\room\NPC[0]\Sound2, Camera, e\room\NPC[0]\Collider)
									Else
										If (Not ChannelPlaying(e\room\NPC[0]\SoundChn2))
											e\room\NPC[0]\PathTimer# = 1.0
											e\room\NPC[0]\SoundChn2 = 0
										EndIf
									EndIf
								ElseIf e\room\NPC[0]\PrevState = 2
									If e\room\NPC[0]\State3 = 3 Then
										If (e\room\NPC[0]\SoundChn2 = 0) Then
											e\room\NPC[0]\Sound2 = LoadSound_Strict("SFX\SCP\049\Room2SL2.ogg")
											e\room\NPC[0]\SoundChn2 = PlaySound2(e\room\NPC[0]\Sound2, Camera, e\room\NPC[0]\Collider)
										Else
											If (Not ChannelPlaying(e\room\NPC[0]\SoundChn2))
												e\room\NPC[0]\PathTimer# = 1.0
												e\room\NPC[0]\SoundChn2 = 0
											EndIf
										EndIf
									Else
										If e\room\NPC[0]\Frame >= 1118
											e\room\NPC[0]\PathTimer# = 1.0
										EndIf
									EndIf
								EndIf
							Else
								Select e\room\NPC[0]\State3
									Case 1
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(e\room\Objects[16],True),EntityY(e\room\Objects[16],True),EntityZ(e\room\Objects[16],True))
										e\room\NPC[0]\PrevState = 1
										DebugLog "Path1"
									Case 2
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(e\room\Objects[15],True),EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True))
										e\room\NPC[0]\PrevState = 2
										DebugLog "Path2"
									Case 3
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(e\room\Objects[17],True),EntityY(e\room\Objects[17],True),EntityZ(e\room\Objects[17],True))
										e\room\NPC[0]\PrevState = 2
										DebugLog "Path3"
									Case 4
										e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],e\room\NPC[0]\PathX,0.1,e\room\NPC[0]\PathZ)
										e\room\NPC[0]\PrevState = 2
										DebugLog "Path4"
									Case 5
										e\EventState2 = 5
								End Select
								e\room\NPC[0]\PathTimer# = 0.0
								e\room\NPC[0]\State3 = e\room\NPC[0]\State3 + 1
							EndIf
						EndIf
					ElseIf e\EventState2 = 4
						If e\room\NPC[0]\State <> 5
							e\EventState2 = 7
							e\room\NPC[0]\State3 = 6.0
							DebugLog "fffffffff"
						EndIf
					ElseIf e\EventState2 = 5
						;RemoveNPC(e\room\NPC[0])
						DebugLog "ddddddddd"
						e\room\NPC[0]\State = 2
						For r.Rooms = Each Rooms
							If r <> PlayerRoom
								If (EntityDistance(r\obj,e\room\NPC[0]\Collider)<HideDistance*2 And EntityDistance(r\obj,e\room\NPC[0]\Collider)>HideDistance)
									e\room\NPC[0]\PathStatus = FindPath(e\room\NPC[0],EntityX(r\obj),EntityY(r\obj),EntityZ(r\obj))
									e\room\NPC[0]\PathTimer = 0.0
									If e\room\NPC[0]\PathStatus = 1 Then e\EventState2 = 6
									Exit
								EndIf
							EndIf
						Next
					ElseIf e\EventState2 = 6
						If MeNPCSeesPlayer(e\room\NPC[0],True) Or e\room\NPC[0]\State2 > 0 Or e\room\NPC[0]\LastSeen > 0
							DebugLog "fffffffff"
							e\EventState2 = 7
						Else
							;Still playing the Music for SCP-049 (in the real, SCP-049's State will be set to 2, causing it to stop playing the chasing track)
							If PlayerRoom = e\room Then
								ShouldPlay = 20
							EndIf
							If e\room\NPC[0]\PathStatus<>1
								e\room\NPC[0]\Idle = 70*60 ;(Making SCP-049 idle for one minute (twice as fast for aggressive NPCs = True))
								PositionEntity e\room\NPC[0]\Collider,0,500,0
								ResetEntity e\room\NPC[0]\Collider
								DebugLog "eeeeeeeee"
								e\EventState2 = 7
							EndIf
						EndIf
					;ElseIf e\EventState2 = 7
					;	e\room\RoomDoors[0]\locked = False
					;	e\room\RoomDoors[1]\locked = False
					;	e\EventState2 = 8
					EndIf
					
					If e\room\NPC[0]<>Null
						If e\EventState2 < 7
							If e\EventState2 > 2
								If Abs(EntityY(e\room\RoomDoors[0]\frameobj)-EntityY(e\room\NPC[0]\Collider))>1.0
									If Abs(EntityY(e\room\RoomDoors[0]\frameobj)-EntityY(Collider))<1.0
										If e\room\RoomDoors[0]\open
											e\room\RoomDoors[0]\open = False
											e\room\RoomDoors[0]\fastopen = 1
											PlaySound_Strict LoadTempSound("SFX\Door\DoorClose079.ogg")
											DebugLog "079 - CLOSE DOOR AT HALLWAY IN ROOM2SL"
										EndIf
									EndIf
								Else
									If e\room\RoomDoors[0]\open = False
										e\room\RoomDoors[0]\fastopen = 0
										e\room\RoomDoors[0]\open = True
										sound=Rand(0, 2)
										PlaySound2(OpenDoorSFX(0,sound),Camera,e\room\RoomDoors[0]\obj)
										PlaySound_Strict LoadTempSound("SFX\Door\DoorOpen079.ogg")
										DebugLog "079 - OPEN DOOR AT HALLWAY IN ROOM2SL"
									EndIf
								EndIf
							EndIf
							
							;If e\EventState2 > 0 Then CanSave% = False
						Else
							If e\room\RoomDoors[0]\open = False
								e\room\RoomDoors[0]\fastopen = 0
								e\room\RoomDoors[0]\open = True
								sound=Rand(0, 2)
								PlaySound2(OpenDoorSFX(0,sound),Camera,e\room\RoomDoors[0]\obj)
								PlaySound_Strict LoadTempSound("SFX\Door\DoorOpen079.ogg")
								DebugLog "079 - OPEN DOOR AT HALLWAY IN ROOM2SL"
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
				
				;Other code
				;[Block]
				If PlayerRoom = e\room Then
					;Lever for checkpoint locking (might have a function in the future for the case if the checkpoint needs to be locked again)
					e\EventState3 = UpdateLever(e\room\Levers[0])
					If e\EventState3 = 1 Then
						UpdateCheckpointMonitors(0)
						If MonitorTimer# < 50 Then
							EntityTexture e\room\Objects[20],e\room\Textures[0],1
						Else
							EntityTexture e\room\Objects[20],e\room\Textures[0],2
						EndIf
					Else
						TurnCheckpointMonitorsOff(0)
						EntityTexture e\room\Objects[20],e\room\Textures[0],0
					EndIf
					
					;Checking if the monitors and such should be rendered or not
					If Abs(EntityY(e\room\RoomDoors[0]\frameobj)-EntityY(Collider))>1.0 Then
						For i = 0 To 14
							If e\room\Objects[i]<>0 And i<>7 Then
								ShowEntity e\room\Objects[i]
							EndIf
						Next
						For sc.SecurityCams = Each SecurityCams
							If sc\room = e\room Then
								If sc\ScrObj<>0
									ShowEntity sc\ScrObj
								EndIf
								If sc\ScrOverlay<>0
									ShowEntity sc\ScrOverlay
								EndIf
								Exit
							EndIf
						Next
						For i = 0 To 3
							If PlayerRoom\Adjacent[i]<>Null Then
								EntityAlpha(GetChild(PlayerRoom\Adjacent[i]\obj,2),0)
							EndIf
						Next
					Else
						For i = 0 To 14
							If e\room\Objects[i]<>0 And i<>7 Then
								HideEntity e\room\Objects[i]
							EndIf
						Next
						For sc.SecurityCams = Each SecurityCams
							If sc\room = e\room Then
								If sc\ScrObj<>0
									HideEntity sc\ScrObj
								EndIf
								If sc\ScrOverlay<>0
									HideEntity sc\ScrOverlay
								EndIf
								Exit
							EndIf
						Next
					EndIf
				EndIf
				
				For e2.Events = Each Events
					If e2\EventName = "008"
						If e2\EventState = 2
							EntityTexture e\room\Objects[21],e\room\Textures[0],3
						Else
							EntityTexture e\room\Objects[21],e\room\Textures[1],6
						EndIf
					EndIf
				Next
				;[End Block]
				
				;[End Block]
			Case "096spawn"
				;[Block]
				Local xspawn#,zspawn#,place%
				If e\room\dist < HideDistance
					;Checking some statements in order to determine if SCP-096 can spawn in this room
					;[Block]
					If e\EventState <> 2
						If Curr096<>Null
							If EntityDistance(Curr096\Collider,Collider)<40
								e\EventState = 2
								DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
								DebugLog "- SCP-096 too close to player"
							EndIf
							
							For e2.Events = Each Events
								If e2\EventName = "room2servers"
									If e2\EventState > 0 And e2\room\NPC[0]<>Null
										e\EventState = 2
										DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
										DebugLog "- room2servers event still in progress"
										Exit
									EndIf
								EndIf
							Next
							
							For r.Rooms = Each Rooms
   								If r\RoomTemplate\Name = "checkpoint1"
									If r\dist < 10
										e\EventState = 2
										DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
										DebugLog "- too close to checkpoint1"
										Exit
									EndIf
								EndIf
							Next
							
							If Curr096\State <> 5
								e\EventState = 2
								DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
								DebugLog "- SCP-096 enraged"
							EndIf
							
							If EntityDistance(Curr096\Collider,e\room\obj)>EntityDistance(Curr096\Collider,Collider)
								e\EventState = 2
								DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
								DebugLog "- Room is too far away"
							EndIf
						EndIf
						For e2.Events = Each Events
							If e2\EventName = "room2servers"
								If e2\EventState = 0 ;And (Abs(e2\room\dist-e\room\dist)<HideDistance)
									e\EventState = 2
									DebugLog "Failed to spawn SCP-096 in room "+e\room\RoomTemplate\Name$
									DebugLog "- room2servers event not activated + room is too close to room2servers"
									Exit
								EndIf
							EndIf
						Next
						If PlayerRoom = e\room Then e\EventState = 2
					EndIf
					
					;[End Block]
					
					If e\EventState = 0
						Select e\room\RoomTemplate\Name
							Case "room4pit","room3pit","room3z2","room4tunnels","room3tunnel"
								If e\room\RoomTemplate\Name$ = "room4pit" Or e\room\RoomTemplate\Name$ = "room4tunnels"
									place% = Rand(0,3)
								Else
									place% = Rand(0,2)
								EndIf
								
								If place% = 0
									xspawn# = -608.0
									zspawn# = 0.0
								ElseIf place% = 1
									xspawn# = 0.0
									zspawn# = -608.0
								ElseIf place% = 2
									xspawn# = 608.0
									zspawn# = 0.0
								Else
									xspawn# = 0.0
									zspawn# = 608.0
								EndIf
							Default
								xspawn# = Rnd(-100,100)
								zspawn# = Rnd(-100,100)
						End Select
						pvt% = CreatePivot(e\room\obj)
						PositionEntity pvt%,xspawn#,0,zspawn#
						If Curr096 = Null
							Curr096 = CreateNPC(NPCtype096,EntityX(pvt%,True),e\room\y+0.5,EntityZ(pvt%,True))
							DebugLog EntityY(Curr096\Collider)
						Else
							PositionEntity Curr096\Collider,EntityX(pvt%,True),e\room\y+0.5,EntityZ(pvt%,True)
							ResetEntity Curr096\Collider
						EndIf
						PointEntity Curr096\Collider,Collider
						RotateEntity Curr096\Collider,0,EntityYaw(Curr096\Collider)+180,0
						FreeEntity pvt%
						Curr096\State = 5
						
						DebugLog "SCP-096 successfully placed in "+Chr(34)+e\room\RoomTemplate\Name+Chr(34)
						e\EventState = 1
					ElseIf e\EventState = 1
						PointEntity Curr096\Collider,Collider
						RotateEntity Curr096\Collider,0,EntityYaw(Curr096\Collider)+180,0
						
						If EntityDistance(Curr096\Collider,Collider)<HideDistance*0.5
							If EntityVisible(Curr096\Collider,Camera)
								PointEntity Curr096\Collider,Collider
								RotateEntity Curr096\Collider,0,EntityYaw(Curr096\Collider)+Rnd(170,190),0
								e\EventState = 2
							EndIf
						EndIf
					ElseIf e\EventState = 3
						e\EventState = 2
					EndIf
				Else
					If e\EventState = 2
						If Rand(-1,1+(2*SelectedDifficulty\aggressiveNPCs))>0 Then
							e\EventState = 0
						Else
							e\EventState = 3
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "medibay"
				;[Block]
				;e\EventState: Determines if the player has entered the room or not
				;	- 0 : Not entered
				;	- 1 : Has entered
				;e\EventState2: A timer for the zombie wake up
				
				;Hiding/Showing the props in this room
				If PlayerRoom <> e\room
					HideEntity e\room\Objects[0]
				Else
					ShowEntity e\room\Objects[0]
					;Setup
					If e\EventState = 0
						e\room\NPC[0] = CreateNPC(NPCtype008,EntityX(e\room\Objects[3],True),0.5,EntityZ(e\room\Objects[3],True))
						RotateEntity e\room\NPC[0]\Collider,0,e\room\angle-90,0
						e\EventState = 1
					EndIf
					
					If EntityDistance(e\room\NPC[0]\Collider,Collider)<1.2
						If e\EventState2 = 0
							LightBlink = 12.0
							PlaySound_Strict LightSFX
							e\EventState2 = FPSfactor
						EndIf
					EndIf
				EndIf
				
				If e\EventState2 > 0 And e\EventState2 < 70*4
					e\EventState2 = e\EventState2 + FPSfactor
				ElseIf e\EventState2 >= 70*4
					If e\room\NPC[0]\State = 0
						e\room\NPC[0]\State = 2
					EndIf
				EndIf
				;[End Block]
			Case "dimension1499"
				;[Block]
				If PlayerRoom<>e\room
					If e\room\Objects[0]<>0
						For i = 1 To 15
							HideEntity e\room\Objects[i]
						Next
					EndIf
					If EntityY(Collider)>EntityY(e\room\obj)-0.5
						PlayerRoom = e\room
					EndIf
				EndIf
				If e\EventState = 2.0
					If e\SoundCHN<>0 Then
						StopStream_Strict(e\SoundCHN)
						StopChannel(e\SoundCHN2)
						e\SoundCHN = 0
						e\SoundCHN2 = 0
					EndIf
					HideEntity NTF_1499Sky
					HideChunks()
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype1499
							RemoveNPC(n)
							;n\Idle = True
						EndIf
					Next
					For du.Dummy1499 = Each Dummy1499
						FreeEntity(du\obj)
						Delete du
					Next
					If e\EventState3 < 70*30 Then
						e\EventState3 = 0.0
					EndIf
					e\EventState = 1.0
					If e\Sound2 <> 0 Then
						FreeSound_Strict e\Sound2
						e\Sound2 = 0
					EndIf
				EndIf
				;[End Block]
			Case "room2offices035"
				;[Block]
				Local is035released = False
				
				For e2.Events = Each Events
					If e2<>e And e2\EventName="room035"
						If e2\EventState<0.0
							is035released=True
							Exit
						EndIf
					EndIf
				Next
				
				If is035released
					If e\room\dist < 8
						If e\room\NPC[0]=Null
							e\room\NPC[0]=CreateNPC(NPCtypeD,e\room\x,0.5,e\room\z)
							RotateEntity e\room\NPC[0]\Collider,0,e\room\angle+180,0
							MoveEntity e\room\NPC[0]\Collider,0,0,-0.5
							e\room\NPC[0]\State = 3
							e\room\NPC[0]\texture = "GFX\npcs\035victim.jpg"
							ChangeNPCTextureID(e\room\NPC[0],7)
							SetNPCFrame(e\room\NPC[0],19)
						EndIf
						If e\room\NPC[1]=Null
							If EntityDistance(e\room\NPC[0]\Collider,Collider)<2.5
								e\room\NPC[1]=CreateNPC(NPCtypeTentacle,EntityX(e\room\NPC[0]\Collider),0.0,EntityZ(e\room\NPC[0]\Collider))
								RotateEntity e\room\NPC[1]\Collider,0,e\room\angle,0
								MoveEntity e\room\NPC[1]\Collider,0,0,0.6
							EndIf
						EndIf
					Else
						If e\room\dist>HideDistance
							If e\room\NPC[1]<>Null
								RemoveNPC(e\room\NPC[1])
								e\room\NPC[1]=Null
							EndIf
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "room1archive"
				;[Block]
				If e\EventState = 0
					e\EventState = Rand(1,3)
				Else
					e\room\RoomDoors[0]\KeyCard = e\EventState
				EndIf
				;[End Block]
			Case "room2shaft"
                ;[Block]
                If e\EventState = 0 Then
                    e\room\NPC[0]=CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[1],True), EntityY(e\room\Objects[1],True)+0.5, EntityZ(e\room\Objects[1],True))
                    RotateEntity e\room\NPC[0]\Collider, 0, e\room\angle+180,0, True
					
                    SetNPCFrame (e\room\NPC[0], 286)
                    e\room\NPC[0]\State = 8
                    
                    e\EventState = 1
                EndIf
				
				If PlayerRoom=e\room Then
					UpdateButton(e\room\Objects[2])
					If ClosestButton = e\room\Objects[2] And MouseHit1 Then
						Msg = "The elevator appears to be broken."
						PlaySound2(ButtonSFX2, Camera, e\room\Objects[2])
						MsgTimer = 5*70
						MouseHit1=0
					EndIf
				EndIf
                ;[End Block]
			Case "room1lifts"
				;[Block]
				If PlayerRoom=e\room Then
					For i = 0 To 1
						UpdateButton(e\room\Objects[i])
						If ClosestButton = e\room\Objects[i] And MouseHit1 Then
							Msg = "The elevator appears to be broken."
							PlaySound2(ButtonSFX2, Camera, e\room\Objects[i])
							MsgTimer = 5*70
							MouseHit1=0
						EndIf
					Next
				EndIf
				;[End Block]
		End Select
		
		If e<>Null Then
			CatchErrors(Chr(34)+e\EventName+Chr(34)+" event")
		Else
			CatchErrors("Deleted event")
		EndIf
	Next
	
	;This here is necessary because the 294 drinks with explosion effect didn't worked anymore - ENDSHN
	If ExplosionTimer > 0 Then
		ExplosionTimer = ExplosionTimer+FPSfactor
		
		If ExplosionTimer < 140.0 Then
			If ExplosionTimer-FPSfactor < 5.0 Then
				ExplosionSFX = LoadSound_Strict("SFX\Ending\GateB\Nuke1.ogg")
				PlaySound_Strict ExplosionSFX
				CameraShake = 10.0
				ExplosionTimer = 5.0
			EndIf
			
			CameraShake = CurveValue(ExplosionTimer/60.0,CameraShake, 50.0)
		Else
			CameraShake = Min((ExplosionTimer/20.0),20.0)
			If ExplosionTimer-FPSfactor < 140.0 Then
				BlinkTimer = 1.0
				ExplosionSFX = LoadSound_Strict("SFX\Ending\GateB\Nuke2.ogg")
				PlaySound_Strict ExplosionSFX				
				For i = 0 To (10+(10*(ParticleAmount+1)))
					p.Particles = CreateParticle(EntityX(Collider)+Rnd(-0.5,0.5),EntityY(Collider)-Rnd(0.2,1.5),EntityZ(Collider)+Rnd(-0.5,0.5),0, Rnd(0.2,0.6), 0.0, 350)	
					RotateEntity p\pvt,-90,0,0,True
					p\speed = Rnd(0.05,0.07)
				Next
			EndIf
			LightFlash = Min((ExplosionTimer-140.0)/10.0,5.0)
			
			If ExplosionTimer > 160 Then KillTimer = Min(KillTimer,-0.1)
			If ExplosionTimer > 500 Then ExplosionTimer = 0
			
			;a dirty workaround to prevent the collider from falling down into the facility once the nuke goes off,
			;causing the UpdateEvent function to be called again and crashing the game
			PositionEntity Collider, EntityX(Collider), 200, EntityZ(Collider)
		EndIf
	EndIf
	
End Function

Function UpdateDimension1499()
	Local e.Events,n.NPCs,n2.NPCs,r.Rooms,it.Items,i%,j%,du.Dummy1499,du2.Dummy1499,temp%,scale#,x%,y%
	
	For e.Events = Each Events
		If e\EventName = "dimension1499"
			;[Block]
			;e\EventState: If player entered dimension (will be resetted after the player leaves it)
				;0: The player never entered SCP-1499
				;1: The player had already entered the dimension at least once
				;2: The player is in dimension
			;e\EventState2: Used to count the amount of times the player has entered the 1499 dimension (for a little spawning event)
			;e\EventState3: Variable used for the 1499 church event
			If PlayerRoom = e\room Then
				If e\EventState < 2.0
					;1499 random generator
					;[Block]
					If e\EventState = 0.0
						If e\EventStr = "" And QuickLoadPercent = -1
							QuickLoadPercent = 0
							QuickLoad_CurrEvent = e
							e\EventStr = "load0"
						EndIf
					Else
						e\EventState = 2.0
					EndIf
					;[End Block]
					;For n.NPCs = Each NPCs
					;	If n\NPCtype = NPCtype1499
					;		n\Idle = False
					;		n\State = 0
					;		;If Rand(2)=1 Then n\State2 = 500*3
					;		n\Angle = Rnd(360)
					;		PositionEntity n\Collider,EntityX(n\Collider)+Rnd(-60.0,60.0),EntityY(n\Collider)+0.05,EntityZ(n\Collider)+Rnd(-60.0,60.0)
					;		ResetEntity n\Collider
					;	EndIf
					;Next
					Local value% = Rand(2,3)
					If e\EventState2 = value% Or e\EventState2=4 Then
						For i = -1 To 1
							For j = -1 To 1
								If i<>0 And j<>0 Then
									n.NPCs = CreateNPC(NPCtype1499,EntityX(Collider)+(0.75*i),EntityY(Collider)+0.05,EntityZ(Collider)+(0.75*j))
									PointEntity n\Collider,Collider
									RotateEntity n\Collider,0,EntityYaw(n\Collider),0
									n\State = 2
								ElseIf i<>0 Or j<>0 Then
									n.NPCs = CreateNPC(NPCtype1499,EntityX(Collider)+i,EntityY(Collider)+0.05,EntityZ(Collider)+j)
									PointEntity n\Collider,Collider
									RotateEntity n\Collider,0,EntityYaw(n\Collider),0
									n\State = 2
								EndIf
							Next
						Next
						e\EventState2 = 5
					EndIf
					If e\EventState3 < 70*30 Then
						;Guards at the entrance to church
						n.NPCs = CreateNPC(NPCtype1499,e\room\x+4055.0*RoomScale,e\room\y+240.0*RoomScale,e\room\z+1884.0*RoomScale)
						n\PrevState = 3
						n\Angle = 270
						RotateEntity n\Collider,0,n\Angle,0
						n2.NPCs = CreateNPC(NPCtype1499,e\room\x+4055.0*RoomScale,e\room\y+240.0*RoomScale,e\room\z+2876.0*RoomScale)
						n2\PrevState = 3
						n2\Angle = 270
						RotateEntity n2\Collider,0,n2\Angle,0
						n\Target = n2
						n2\Target = n
						e\room\NPC[2] = n
						e\room\NPC[3] = n2
						;More guards
						n.NPCs = CreateNPC(NPCtype1499,e\room\x-1877.0*RoomScale,e\room\y+192.0*RoomScale,e\room\z+1071.0*RoomScale)
						n\PrevState = 3
						n\Angle = 270
						RotateEntity n\Collider,0,n\Angle,0
						n2.NPCs = CreateNPC(NPCtype1499,e\room\x-1877.0*RoomScale,e\room\y+192.0*RoomScale,e\room\z+3503.0*RoomScale)
						n2\PrevState = 3
						n2\Angle = 270
						RotateEntity n2\Collider,0,n2\Angle,0
						n\Target = n2
						n2\Target = n
						e\room\NPC[4] = n
						e\room\NPC[5] = n2
						;Guard at stairs
						n.NPCs = CreateNPC(NPCtype1499,e\room\x-2761.0*RoomScale,e\room\y+240.0*RoomScale,e\room\z+3204.0*RoomScale)
						n\PrevState = 1
						n\Angle = 180
						RotateEntity n\Collider,0,n\Angle,0
						n\Speed = 0.0
						;King
						n.NPCs = CreateNPC(NPCtype1499,e\room\x-1917.0*RoomScale,e\room\y+1904.0*RoomScale,e\room\z+2308.0*RoomScale)
						n\PrevState = 2
						n\Angle = 270
						RotateEntity n\Collider,0,n\Angle,0
						tex = LoadTexture_Strict("GFX\npcs\1499_King.jpg")
						EntityTexture n\obj,tex
						FreeTexture tex
						e\room\NPC[0] = n
						;Guard next to king
						n.NPCs = CreateNPC(NPCtype1499,e\room\x-1917.0*RoomScale,e\room\y+1904.0*RoomScale,e\room\z+2052.0*RoomScale)
						n\PrevState = 1
						n\Angle = 270
						RotateEntity n\Collider,0,n\Angle,0
						e\room\NPC[1] = n
						;1499-1 instances praying in church
						;Zone 1
						For x=0 To 7
							For y=0 To 2
								du = New Dummy1499
								For n.NPCs = Each NPCs
									If n\NPCtype = NPCtype1499 And n\PrevState<>2 Then
										du\obj = CopyEntity(n\obj)
										Exit
									EndIf
								Next
								scale# = (GetINIFloat("DATA\NPCs.ini", "SCP-1499-1", "scale") / 4.0) * Rnd(0.8,1.0)
								ScaleEntity du\obj, scale#,scale#,scale#
								EntityFX du\obj,1
								du\anim = Rand(0,1)
								;2560=x		768=z
								;1687.0
								PositionEntity du\obj,Max(Min((e\room\x+(1887.0-((2560.0/7.0)*x))*RoomScale)+Rnd(-0.5,0.5),e\room\x+1887.0*RoomScale),e\room\x-873.0*RoomScale),e\room\y,Max(Min((e\room\z+(1796.0-(384.0*y))*RoomScale)+Rnd(-0.5,0.5),e\room\z+1796.0*RoomScale),e\room\z+1028.0*RoomScale)
								RotateEntity du\obj,0,270,0
								EntityAutoFade du\obj,25,39
							Next
						Next
						;Zone 2
						For x=0 To 6
							For y=0 To 2
								du = New Dummy1499
								For n.NPCs = Each NPCs
									If n\NPCtype = NPCtype1499 And n\PrevState<>2 Then
										du\obj = CopyEntity(n\obj)
										Exit
									EndIf
								Next
								scale# = (GetINIFloat("DATA\NPCs.ini", "SCP-1499-1", "scale") / 4.0) * Rnd(0.8,1.0)
								ScaleEntity du\obj, scale#,scale#,scale#
								EntityFX du\obj,1
								du\anim = Rand(0,1)
								;2048=x		768=z
								;1175.0
								PositionEntity du\obj,Max(Min((e\room\x+(1375.0-((2048.0/6.0)*x))*RoomScale)+Rnd(-0.5,0.5),e\room\x+1375.0*RoomScale),e\room\x-873.0*RoomScale),e\room\y,Max(Min((e\room\z+(3588-(384.0*y))*RoomScale)+Rnd(-0.5,0.5),e\room\z+3588.0*RoomScale),e\room\z+2820.0*RoomScale)
								RotateEntity du\obj,0,270,0
								EntityAutoFade du\obj,25,39
							Next
						Next
						;4055, 240, 2084
						;4055, 240, 3076
						;-2761, 240, 3204 for stairs guard
						;-1449, 240, 1092
						;-1449, 240, 3524
						;-1917, 1904, 2052 - guard
						;-1917, 1904, 2308 - king
						
						;1687, 240, 1028
						;1687, 240, 1796
						;-873, 240, 1796
						;-873, 240, 1028
						;that's the First zone
						;1175, 240, 2820
						;1175, 240, 3588
						;-873, 240, 3588
						;-873, 240, 2820
						;that's For second zone
					Else
						HideEntity e\room\Levers[1]
					EndIf
					
					For i = 0 To 14
						n.NPCs = CreateNPC(NPCtype1499,EntityX(Collider)+Rnd(-20,20),EntityY(Collider)+0.1,EntityZ(Collider)+Rnd(-20,20))
						If Rand(2)=1 Then n\State2 = 500*3
						n\Angle = Rnd(360)
						n\State2 = 0
						If EntityDistance(n\Collider,Collider)<10.0 Then
							n\State = 2
						EndIf
					Next
				EndIf
				
				;PositionEntity e\room\Objects[0],0,800,0
				If (Not DebugHUD)
					CameraFogRange Camera,40,80
					CameraFogColor Camera,96,97,104
					CameraClsColor Camera,96,97,104
					CameraRange Camera,0.05,90
				Else
					CameraFogRange Camera,120,120
					CameraFogColor Camera,96,97,104
					CameraClsColor Camera,96,97,104
					CameraRange Camera,0.05,120
				EndIf
				
				For r.Rooms = Each Rooms
					HideEntity r\obj
				Next
				ShowEntity e\room\obj
				If QuickLoadPercent = 100 Or QuickLoadPercent = -1
					UpdateChunks(e\room,15)
					ShowEntity NTF_1499Sky
					Update1499Sky()
					ShouldPlay = 18
					If EntityY(Collider)<800.0
						PositionEntity Collider,EntityX(Collider),800.5,EntityZ(Collider),True
						ResetEntity Collider
					EndIf
					;A hacky fix to make items not fall that are in dimension1499
					For it.Items = Each Items
						If EntityY(it\collider)>750.0
							If EntityY(it\collider)<800.0
								PositionEntity it\collider,EntityX(it\collider),800.5,EntityZ(it\collider)
								ResetEntity it\collider
							EndIf
						EndIf
					Next
					For du = Each Dummy1499
						If e\EventState3 < 70*30 Then
							If du\anim=0 Then
								;321-361
								If AnimTime(du\obj)<=360.5 Then
									Animate2(du\obj,AnimTime(du\obj),321,361,0.2,False)
								;362-402
								ElseIf AnimTime(du\obj)>361.5 And AnimTime(du\obj)<=401.5 Then
									Animate2(du\obj,AnimTime(du\obj),362,402,0.2,False)
								Else
									temp = Rand(0,1)
									If temp=0 Then
										SetAnimTime(du\obj,321)
									Else
										SetAnimTime(du\obj,362)
									EndIf
								EndIf
							Else
								;413-453
								If AnimTime(du\obj)<=452.5 Then
									Animate2(du\obj,AnimTime(du\obj),413,453,0.2,False)
								;454-498
								ElseIf AnimTime(du\obj)>453.5 And AnimTime(du\obj)<=497.5 Then
									Animate2(du\obj,AnimTime(du\obj),454,498,0.2,False)
								Else
									temp = Rand(0,1)
									If temp=0 Then
										SetAnimTime(du\obj,413)
									Else
										SetAnimTime(du\obj,454)
									EndIf
								EndIf
							EndIf
						Else
							If du\anim=0 Then
								If AnimTime(du\obj)<=411.5 And AnimTime(du\obj)>320.5 Then
									Animate2(du\obj,AnimTime(du\obj),403,412,0.2,False)
								Else
									Animate2(du\obj,AnimTime(du\obj),296,320,0.2,True)
								EndIf
							Else
								If AnimTime(du\obj)<=507.5 And AnimTime(du\obj)>320.5 Then
									Animate2(du\obj,AnimTime(du\obj),499,508,0.2,False)
								Else
									Animate2(du\obj,AnimTime(du\obj),296,320,0.2,True)
								EndIf
							EndIf
							Local pvt = CreatePivot()
							PositionEntity pvt,EntityX(du\obj),EntityY(du\obj),EntityZ(du\obj),True
							PointEntity pvt,Collider
							RotateEntity du\obj,0,CurveAngle(EntityYaw(pvt),EntityYaw(du\obj)-180,10.0)+180,0
							FreeEntity pvt
						EndIf
					Next
					;-56,0,2287
					;X distance: 2160
					;Z distance: 1408
					
					;Player is inside the church
					If e\EventState3 < 70*10 Then
						If Abs(EntityX(Collider)-(e\room\x-56.0*RoomScale))<2160.0*RoomScale Then
							If Abs(EntityZ(Collider)-(e\room\z+2287.0*RoomScale))<1408.0*RoomScale Then
								e\EventState3 = e\EventState3 + FPSfactor
								;CurrMusicVolume = 1.0
							EndIf
						EndIf
					ElseIf e\EventState3 >= 70*10 And e\EventState3 < 70*20 Then
						For i = 0 To 1
							e\room\NPC[i]\Reload = 1
						Next
						e\EventState3 = 70*20
					ElseIf e\EventState3 = 70*20
						If e\room\NPC[0]\Frame > 854.5 Then
							For i = 2 To 5
								If i = 2
									If e\room\NPC[i]\Sound <> 0 Then FreeSound_Strict e\room\NPC[i]\Sound : e\room\NPC[i]\Sound = 0
									e\room\NPC[i]\Sound = LoadSound_Strict("SFX\SCP\1499\Triggered.ogg")
									e\room\NPC[i]\SoundChn = PlaySound2(e\room\NPC[i]\Sound, Camera, e\room\NPC[i]\Collider,50.0)
								EndIf
								e\room\NPC[i]\State = 1
								e\room\NPC[i]\Frame = 203
							Next
							e\EventState3 = 70*30
						EndIf
					EndIf
					
					;If Abs(EntityX(Collider)-(e\room\x-56.0*RoomScale))<2160.0*RoomScale Then
					;	If Abs(EntityZ(Collider)-(e\room\z+2287.0*RoomScale))<1408.0*RoomScale Then
					;		ShouldPlay = 66
					;	EndIf
					;EndIf
					
					If e\room\NPC[0]<>Null Then
						ShowEntity e\room\Levers[1]
						If e\EventState3 < 70*30 Then
							ShouldPlay = 66
							If NowPlaying = 66 Then
								If e\SoundCHN = 0 Then
									e\Sound2 = LoadSound_Strict("SFX\Music\HaveMercyOnMe(Choir).ogg")
									e\SoundCHN = StreamSound_Strict("SFX\Music\HaveMercyOnMe(NoChoir).ogg",MusicVolume)
									e\SoundCHN_isStream = True
								EndIf
								;If e\SoundCHN2<>0 Then
								;	UpdateStreamSoundOrigin(e\SoundCHN2,Camera,e\room\Levers[0])
								;EndIf
							EndIf
							If e\Sound2<>0 Then
								e\SoundCHN2 = LoopSound2(e\Sound2,e\SoundCHN2,Camera,e\room\Levers[0],10,MusicVolume)
							EndIf
						Else
							ShouldPlay = 19
							If e\SoundCHN<>0 Then
								StopStream_Strict(e\SoundCHN)
								StopChannel(e\SoundCHN2)
								e\SoundCHN = 0
								e\SoundCHN2 = 0
							EndIf
							If e\Sound2 <> 0 Then
								FreeSound_Strict e\Sound2
								e\Sound2 = 0
							EndIf
						EndIf
					EndIf
					
					If EntityDistance(Collider,e\room\obj)>40.0
						For du.Dummy1499 = Each Dummy1499
							HideEntity du\obj
						Next
					Else
						For du.Dummy1499 = Each Dummy1499
							ShowEntity du\obj
						Next
					EndIf
				Else
					DropSpeed = 0
				EndIf
				CurrStepSFX=3
				PlayerFallingPickDistance = 0.0
			Else
				If e\EventState = 2.0
					If e\SoundCHN<>0 Then
						StopStream_Strict(e\SoundCHN)
						StopChannel(e\SoundCHN2)
						e\SoundCHN = 0
						e\SoundCHN2 = 0
					EndIf
					HideEntity NTF_1499Sky
					HideChunks()
					For n.NPCs = Each NPCs
						If n\NPCtype = NPCtype1499
							RemoveNPC(n)
							;n\Idle = True
						EndIf
					Next
					For du.Dummy1499 = Each Dummy1499
						FreeEntity du\obj
						Delete du
					Next
					If e\EventState3 < 70*30 Then
						e\EventState3 = 0.0
					EndIf
					e\EventState = 1.0
					If e\Sound2 <> 0 Then
						FreeSound_Strict e\Sound2
						e\Sound2 = 0
					EndIf
				EndIf
			EndIf
			;[End Block]
		EndIf
	Next
	
End Function

Function UpdateEndings()
	Local e.Events,n.NPCs,r.Rooms,i,pvt,p.Particles
	
	For e.Events = Each Events
		Select e\EventName
			Case "exit1"
				;[Block]
				If PlayerRoom = e\room Then
					
					If EntityY(Collider)>1040.0*RoomScale Then
						
						;For r.Rooms = Each Rooms
						;	HideEntity r\obj
						;Next					
						ShowEntity e\room\obj
						
						;Curr106\State = 20000
						;Curr106\Idle = True
						
						If e\EventState = 0 Then
							DrawLoading(0,True)
							
							For i = 0 To MaxRoomLights-1
								If e\room\LightSprites[i]<>0 Then 
									EntityFX e\room\LightSprites[i], 1+8
								EndIf
							Next
							
							For n.NPCs = Each NPCs
								RemoveNPC(n)
							Next
							Curr173 = Null
							Curr106 = Null
							Curr096 = Null
							Curr5131 = Null
							
							CameraFogMode(Camera, 0)
							SecondaryLightOn = True
							
							;Music(5) = LoadSound_Strict("SFX\Music\GateB1.ogg")
							DrawLoading(60,True)
							;Music(6) = LoadSound_Strict("SFX\Music\GateB2.ogg")
							DrawLoading(90,True)
							
							e\room\NPC[0] = CreateNPC(NPCtypeApache, e\room\x, 100.0, e\room\z)
							e\room\NPC[0]\State = 1
							
							e\room\NPC[1] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True)+0.2,EntityZ(e\room\Objects[4],True))
							e\room\NPC[1]\State = 0
							e\room\NPC[1]\State2 = 10
							
							
							pvt = CreatePivot()
							PositionEntity pvt, EntityX(e\room\Objects[0],True), EntityY(e\room\Objects[0],True), EntityZ(e\room\Objects[0],True)
							
							e\room\Objects[0] = LoadMesh_Strict("GFX\map\exit1terrain.b3d", e\room\obj)
							ScaleEntity e\room\Objects[0],RoomScale,RoomScale,RoomScale,True
							RotateEntity e\room\Objects[0],0,e\room\angle,0,True
							PositionEntity(e\room\Objects[0], EntityX(pvt), EntityY(pvt), EntityZ(pvt), True)
							
							FreeEntity pvt
							
							Delay 100
							
							Sky = sky_CreateSky("GFX\map\sky\sky")
							RotateEntity Sky,0,e\room\angle-90,0
							
							e\EventState = 1.0
							
							For n.NPCs = Each NPCs
								If n\NPCtype = NPCtypeMTF
									RemoveNPC(n)
								EndIf
							Next
							
							DrawLoading(100,True)
						Else
							
							UpdateSky()
							
							If e\EventState < 2.0 And SelectedEnding = "" Then 
								If e\room\NPC[0]\State = 2 Then
									ShouldPlay = 6
								Else
									e\EventState2=(e\EventState2+FPSfactor) Mod 3600
									PositionEntity(e\room\NPC[0]\Collider, EntityX(e\room\obj,True)+Cos(e\EventState2/10)*6000.0*RoomScale,14000*RoomScale,EntityZ(e\room\obj,True)+Sin(e\EventState2/10)*6000.0*RoomScale)
									RotateEntity e\room\NPC[0]\Collider,7.0,(e\EventState2/10),20.0											
									ShouldPlay = 5
								EndIf
								
								If EntityDistance(Collider, e\room\Objects[10])<320*RoomScale Then
									e\EventState = 2.0
									e\room\RoomDoors[2]\open = False
									e\room\RoomDoors[2]\locked = 6
									e\room\RoomDoors[3]\open = False
									e\room\RoomDoors[3]\locked = 6
									
									e\room\NPC[2] = CreateNPC(NPCtypeApache, EntityX(e\room\Objects[9],True),EntityY(e\room\Objects[9],True)+0.5,EntityZ(e\room\Objects[9],True))
									e\room\NPC[2]\State = 3
									
									e\room\NPC[3] = CreateNPC(NPCtypeApache, EntityX(e\room\Objects[7],True),EntityY(e\room\Objects[7],True)-2.0,EntityZ(e\room\Objects[7],True))
									e\room\NPC[3]\State = 3
									
									e\room\NPC[0]\State = 3
									
									;e\SoundCHN = PlaySound_Strict (LoadTempSound("SFX\Ending\GateB\682Battle.ogg"))
									PlayAnnouncement("SFX\Ending\GateB\682Battle.ogg")
								EndIf								
							Else
								ShouldPlay = 6
								e\EventState=e\EventState+FPSfactor
								
								If e\EventState < 40.0*70 Then 	
									e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs2()/25.0)*3
									e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/85.0)+9.0
									e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/25.0)*3
									
									e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs2()/23.0)*3
									e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/83.0)+5.0
									e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/23.0)*3
									
									If e\room\NPC[3]\State = 3 Then 
										e\room\NPC[3]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs2()/20.0)*3
										e\room\NPC[3]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/80.0)+3.5
										e\room\NPC[3]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/20.0)*3
									EndIf
								EndIf
							EndIf
							
							
							If e\EventState > 0.6*70 And e\EventState < 42.2*70 Then 
								If e\EventState < 0.7*70 Then
									CameraShake = 0.5
								ElseIf e\EventState > 3.2*70 And e\EventState < 3.3*70	
									CameraShake = 0.5
								ElseIf e\EventState > 6.1*70 And e\EventState < 6.2*70	
									CameraShake = 0.5
								ElseIf e\EventState < 10.8*70 And e\EventState < 10.9*70	
									CameraShake = 0.5
								ElseIf e\EventState > 12.1*70 And e\EventState < 12.3*70
									CameraShake = 1.0
								ElseIf e\EventState > 13.3*70 And e\EventState < 13.5*70
									CameraShake = 1.5
								ElseIf e\EventState > 16.5*70 And e\EventState < 18.5*70
									CameraShake = 3.0
								ElseIf e\EventState > 21.5*70 And e\EventState < 24.0*70	
									CameraShake = 2.0
								ElseIf e\EventState > 25.5*70 And e\EventState < 27.0*70	
									CameraShake = 2.0	
								ElseIf e\EventState > 31.0*70 And e\EventState < 31.5*70	
									CameraShake = 0.5	
								ElseIf e\EventState > 35.0*70 And e\EventState < 36.5*70	
									CameraShake = 1.5		
									If e\EventState-FPSfactor =< 35.0*70 Then
										e\SoundCHN = StreamSound_Strict("SFX\Ending\GateB\DetonatingAlphaWarheads.ogg",SFXVolume,0)
										e\SoundCHN_isStream = True
									EndIf									
								ElseIf e\EventState > 39.5*70 And e\EventState < 39.8*70		
									CameraShake = 1.0
								ElseIf e\EventState > 42.0*70
									CameraShake = 0.5
									
									;helicopters leave
									e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[19],True)+4.0
									e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[19],True)+4.0
									e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[19],True)+4.0
									
									e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[19],True)
									e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[19],True)
									e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[19],True)
									
								EndIf
							EndIf
							
							If e\EventState => 45.0*70 Then
								If e\EventState < 75.0*70 Then
									If e\SoundCHN2=0
										e\SoundCHN2 = StreamSound_Strict("SFX\Ending\GateB\Siren.ogg",SFXVolume,Mode)
										e\SoundCHN2_isStream = True
									EndIf
								Else
									If SelectedEnding = "" Then
									    ShouldPlay = 66
										
										StopStream_Strict(e\SoundCHN)
										StopStream_Strict(e\SoundCHN2)
										
										temp = True
										For e2.Events = Each Events
											If e2\EventName = "room2nuke" Then
												temp = e2\EventState
												Exit
											EndIf
										Next
										
										If temp = 1 Then ;remote detonation on -> explode
											ExplosionTimer = Max(ExplosionTimer, 0.1)
											SelectedEnding = "B2"
										Else
											;LoadEventSound(e,"SFX\Ending\GateB\AlphaWarheadsFail.ogg")
											;e\SoundCHN = PlaySound_Strict(e\Sound)
											PlayAnnouncement("SFX\Ending\GateB\AlphaWarheadsFail.ogg")
											
											For i = 0 To 1
												n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[18],True)+(i*0.4),EntityY(e\room\Objects[18],True)+0.29,EntityZ(e\room\Objects[18],True)+(i*0.4))
											Next
											
											n.NPCs = CreateNPC(NPCtypeMTF, EntityX(e\room\RoomDoors[2]\obj,True),EntityY(e\room\RoomDoors[2]\obj,True)+0.29,(EntityZ(e\room\RoomDoors[2]\obj,True)+EntityZ(e\room\RoomDoors[3]\obj,True))/2)
											
											For n.NPCs = Each NPCs
												If n\NPCtype = NPCtypeMTF Then
													n\LastSeen = (70*Rnd(30,35))
													n\State = 3
													n\State2 = 10
													n\EnemyX = EntityX(Collider)
													n\EnemyY = EntityY(Collider)
													n\EnemyZ = EntityZ(Collider)
												EndIf
											Next
											
											DebugLog "MTF Units spawned!"
											
											e\EventState = 85.0*70
											
											SelectedEnding = "B3"
										EndIf
										
										;EndIf										
									Else
										If SelectedEnding = "B3" Then
											e\room\NPC[0]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs2()/25.0)*3
											e\room\NPC[0]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/85.0)+9.0
											e\room\NPC[0]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/25.0)*3
											
											e\room\NPC[2]\EnemyX = EntityX(e\room\Objects[11],True)+Sin(MilliSecs2()/23.0)*3
											e\room\NPC[2]\EnemyY = EntityY(e\room\Objects[11],True)+Cos(MilliSecs()/83.0)+5.0
											e\room\NPC[2]\EnemyZ = EntityZ(e\room\Objects[11],True)+Cos(MilliSecs()/23.0)*3
											
											e\room\RoomDoors[5]\open = True
											
											;Update the MTF Units everytime they cannot detect the player
											If e\EventState3 = 0.0
												For n.NPCs = Each NPCs
													If n\NPCtype = NPCtypeMTF
														If n\State = 5
															n\State = 3
															n\PathStatus = FindPath(n, EntityX(Collider),EntityY(Collider),EntityZ(Collider))
															n\PathTimer = 70*Rand(15,20)
															n\LastSeen = 70*300
														EndIf
														If EntityDistance(n\Collider,Collider)<3.0
															n\State = 5
															n\PathStatus = 0
															n\PathTimer = 0
															n\CurrSpeed = 0
														EndIf
													EndIf
												Next
											EndIf
											
											For n.NPCs = Each NPCs
												If n\NPCtype = NPCtypeMTF
													If n\State = 5 And EntityDistance(n\Collider,Collider)<3.0
														If e\EventState3 = 0.0
															PlaySound_Strict LoadTempSound("SFX\Ending\GateB\PlayerDetect.ogg")
															e\EventState3 = e\EventState3 + FPSfactor
															For n2.NPCs = Each NPCs
																If n2\NPCtype = n\NPCtype
																	n2\State = 5
																	n2\PathStatus = 0
																	n2\PathTimer = 0
																	n2\CurrSpeed = 0
																EndIf
															Next
															Exit
														EndIf
													EndIf
												EndIf
											Next
											
											If e\EventState3 > 0.0 And e\EventState3 <= 500.0
												e\EventState3 = e\EventState3 + FPSfactor
												UnableToMove% = True
												For n.NPCs = Each NPCs
													If n\NPCtype = NPCtypeMTF
														n\EnemyX = EntityX(Collider)
														n\EnemyY = EntityY(Collider)
														n\EnemyZ = EntityZ(Collider)
														n\BoneToManipulate = "spine"
														n\ManipulateBone = True
														n\ManipulationType = 1
														n\Gravity = 0
														n\GravityMult = 0
													EndIf
												Next
											ElseIf e\EventState3 > 500.0
												ent% = LoadSprite("GFX\blooddrop1.png",1+2)
												EntityFX ent%,1+2+8
												ScaleSprite ent%,1.5,1.5
												ShouldPlay = 0
												CurrSpeed = 0
												PlaySound_Strict LoadTempSound("SFX\Ending\GateB\Gunshot.ogg")
												GodMode = 0
												NoClip = 0
												KillTimer = -0.1
												DeathMSG = ""
												Kill()
												BlinkTimer = -10
												
												For n.NPCs = Each NPCs
													If n\NPCtype = NPCtypeMTF
														RemoveNPC(n)
													EndIf
												Next
												RemoveEvent(e)
												Exit
											EndIf
											
										EndIf
									EndIf
									
									
								EndIf
							EndIf
							
							If e\EventState > 26.5*70 Then
								If e\room\Objects[12] = 0 Then
									e\room\Objects[12] = LoadMesh_Strict("GFX\NPCs\682arm.b3d")
									ScaleEntity e\room\Objects[12], 0.15,0.15,0.15
									temp = (Min(((EntityDistance(e\room\NPC[3]\Collider,Collider)/RoomScale)-3000.0)/4,1000)+12192.0)*RoomScale
									PositionEntity e\room\Objects[12],EntityX(e\room\NPC[3]\Collider),12192.0*RoomScale,EntityZ(e\room\NPC[3]\Collider)
									RotateEntity e\room\Objects[12],0,e\room\angle+Rnd(-10,10),0,True
									TurnEntity e\room\Objects[12], 0,0,180
								Else
									If WrapAngle(EntityRoll(e\room\Objects[12]))<340.0 Then 
										angle# = WrapAngle(EntityRoll(e\room\Objects[12]))
										TurnEntity e\room\Objects[12], 0,0,(5.0+Abs(Sin(angle))*2)*FPSfactor
										If angle < 270 And WrapAngle(EntityRoll(e\room\Objects[12]))=> 270 Then
											PlaySound_Strict LoadTempSound("SFX\Character\Apache\Crash1.ogg")
											e\room\NPC[3]\State = 4
											e\room\NPC[3]\State2 = 1.0
											e\room\NPC[3]\EnemyX = EntityX(e\room\Objects[7],True)
											e\room\NPC[3]\EnemyY = EntityY(e\room\Objects[7],True)-2.5
											e\room\NPC[3]\EnemyZ = EntityZ(e\room\Objects[7],True)
											
											em.Emitters = CreateEmitter(EntityX(e\room\NPC[3]\Collider), EntityY(e\room\NPC[3]\Collider), EntityZ(e\room\NPC[3]\Collider),0)
											em\Room = PlayerRoom
											em\RandAngle = 45
											em\Gravity = -0.18
											em\LifeTime = 400
											em\SizeChange = Rnd(0.005,0.007)
											em\Achange = -0.004
											TurnEntity(em\Obj, -80+20*i, 0, 0)
											EntityParent em\Obj, e\room\NPC[3]\Collider
											
											If ParticleAmount > 0
												For i = 0 To (3+(4*(ParticleAmount-1)))
													p.Particles = CreateParticle(EntityX(e\room\NPC[3]\Collider),EntityY(e\room\NPC[3]\Collider),EntityZ(e\room\NPC[3]\Collider), 0, Rnd(0.5,1.0), -0.1, 200)
													p\speed = 0.01
													p\SizeChange = 0.01
													p\A = 1.0
													p\Achange = -0.005
													RotateEntity p\pvt, Rnd(360),Rnd(360),0
													MoveEntity p\pvt, 0,0,0.3
												Next
												
												For i = 0 To (6+(6*(ParticleAmount-1)))
													p.Particles = CreateParticle(EntityX(e\room\NPC[3]\Collider),EntityY(e\room\NPC[3]\Collider),EntityZ(e\room\NPC[3]\Collider), 0, 0.02, 0.003, 200)
													p\speed = 0.04
													p\A = 1.0
													p\Achange = -0.005
													RotateEntity p\pvt, Rnd(360),Rnd(360),0
												Next
											EndIf
										EndIf
									Else
										HideEntity e\room\Objects[12]
									EndIf
								EndIf
							EndIf
							
							
							;0.5
							;2.1
							;3.3
							;6.5 - 8.5
							;11.5-14
							;15.5-17
							;21
							;25-26.5
							;29.5
							;32
						EndIf
						
						;dist = Max(Min(EntityDistance(Collider, e\room\objects[3])/10000.0,1.0),0.0)
						;EntityAlpha Fog, 1.0-dist
						HideEntity Fog
						CameraFogRange Camera, 5,45
						
						angle = Max(Sin(EntityYaw(Collider)),0.0)
						;250,230,200
						CameraFogColor (Camera,200+(angle*50),200+(angle*30),200)
						CameraClsColor (Camera,200+(angle*50),200+(angle*30),200)					
						CameraRange(Camera, 0.05, 60)
						
						AmbientLight (140, 140, 140)
						
						If ParticleAmount > 0
							If Rand(3) = 1 Then
								p.Particles = CreateParticle(EntityX(Camera)+Rnd(-2.0,2.0), EntityY(Camera)+Rnd(0.9,2.0), EntityZ(Camera)+Rnd(-2.0,2.0), 2, 0.006, 0, 300)
								p\speed = Rnd(0.002,0.003)
								RotateEntity(p\pvt, Rnd(-20, 20), e\room\angle-90+Rnd(-15,15),0, 0)
								
								p\SizeChange = -0.00001
							EndIf
						EndIf
						
						;Helicopter spots or player is within range. --> Start shooting.
						If (e\room\NPC[1]\State <> 1) Then
							If ((EntityDistance(e\room\NPC[1]\Collider,Collider) < 15.0) Or EntityVisible(e\room\NPC[0]\Collider,Collider)) Then
								e\room\NPC[1]\State = 1
								e\room\NPC[1]\State3 = 1
							EndIf
						EndIf
						
						;Below roof or inside catwalk. --> Stop shooting.
						If (EntityDistance(e\room\NPC[1]\Collider,Collider) < 8.9) Or (EntityDistance(e\room\Objects[5],Collider) < 16.9) Then
							e\room\NPC[1]\State3 = 0
						Else
							e\room\NPC[1]\State3 = 1
						EndIf
					EndIf
				EndIf
				;[End Block]
			Case "gatea"
				;[Block]
				If PlayerRoom = e\room Then 
					For r.Rooms = Each Rooms
						HideEntity r\obj
					Next					
					ShowEntity e\room\obj
					
					If e\EventState = 0 Then
						DrawLoading(0)
						e\room\Objects[0] = LoadMesh_Strict("GFX\MAP\gateatunnel.b3d")
						PositionEntity e\room\Objects[0], EntityX(e\room\obj,True),EntityY(e\room\obj,True),EntityZ(e\room\obj,True)
						ScaleEntity (e\room\Objects[0],RoomScale,RoomScale,RoomScale)
						EntityType e\room\Objects[0], HIT_MAP
						EntityPickMode e\room\Objects[0], 3
						EntityParent(e\room\Objects[0],e\room\obj)
						
						DrawLoading(30)
						
						For i = 0 To e\room\MaxLights
							If e\room\LightSprites[i]<>0 Then 
								EntityFX e\room\LightSprites[i], 1+8
							EndIf
						Next
						
						For n.NPCs = Each NPCs
							If n <> Curr106 And n <> Curr173
								RemoveNPC(n)
							EndIf
						Next
						Curr173\Idle = True
						Curr096 = Null
						Curr5131 = Null
						
						CameraFogMode(Camera, 0)
						SecondaryLightOn = True
						
						HideDistance = 35.0
						
						For i = 2 To 4
							e\room\NPC[i] = CreateNPC(NPCtypeApache, e\room\x, e\room\y+11, e\room\z)
							e\room\NPC[i]\State = (Not Contained106)
						Next
						
						CreateConsoleMsg("WARNING! Teleporting away from this area may cause bugs or crashing.")
						
						Sky = sky_CreateSky("GFX\map\sky\sky")
						RotateEntity Sky,0,e\room\angle,0
						
						DrawLoading(60)
						
						For i = 0 To 1
							e\room\NPC[i] = CreateNPC(NPCtypeGuard, EntityX(e\room\Objects[i+5],True),EntityY(e\room\Objects[i+5],True),EntityZ(e\room\Objects[i+5],True))
							e\room\NPC[i]\State = 0
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next
						
						For i = 7 To 8
							e\room\NPC[i] = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[i],True)+0.8,EntityY(e\room\Objects[i],True),EntityZ(e\room\Objects[i],True)+0.8)
							e\room\NPC[i]\State = 5
							e\room\NPC[i]\PrevState = 1
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next	
						
						For i = 5 To 6
							e\room\NPC[i] = CreateNPC(NPCtypeMTF, EntityX(e\room\Objects[i+2],True),EntityY(e\room\Objects[i+2],True),EntityZ(e\room\Objects[i+2],True))
							e\room\NPC[i]\State = 5
							e\room\NPC[i]\PrevState = 1
							PointEntity e\room\NPC[i]\Collider, e\room\Objects[3]
						Next		
						
						If Contained106 Then
							e\room\RoomDoors[2]\locked = True
							
							PositionEntity e\room\NPC[5]\Collider, EntityX(e\room\Objects[15],True)+(i-6)*0.2,EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True)+(i-6)*0.2, True
							ResetEntity e\room\NPC[5]\Collider
							
						EndIf
						
						xtemp#=EntityX(e\room\Objects[9],True)
						ztemp#=EntityZ(e\room\Objects[9],True)
						FreeEntity e\room\Objects[9]
						
						e\room\Objects[9] = LoadMesh_Strict("GFX\map\lightgunbase.b3d")
						ScaleEntity e\room\Objects[9], RoomScale,RoomScale,RoomScale
						EntityFX(e\room\Objects[9],0)
						PositionEntity(e\room\Objects[9], xtemp, e\room\y+992.0*RoomScale, ztemp)
						e\room\Objects[10] = LoadMesh_Strict("GFX\map\lightgun.b3d")
						EntityFX(e\room\Objects[10],0)
						ScaleEntity e\room\Objects[10], RoomScale,RoomScale,RoomScale
						PositionEntity(e\room\Objects[10], xtemp, e\room\y+(992.0+288.0)*RoomScale, ztemp-176.0*RoomScale,True)
						EntityParent e\room\Objects[10],e\room\Objects[9]
						RotateEntity e\room\Objects[9], 0, 48, 0
						RotateEntity e\room\Objects[10], 40, 0, 0
						
						For temp = 0 To 20
							For i = 0 To 1
								TranslateEntity e\room\NPC[i]\Collider, 0, -0.04, 0
							Next							
							For i = 5 To 8
								TranslateEntity e\room\NPC[i]\Collider, 0, -0.04, 0
							Next
						Next
						
						ResetEntity Collider
						e\EventState = 1.0
						
						RotateEntity Collider,0,EntityYaw(Collider)+(e\room\angle+180),0
						
						If (Not Contained106) Then PlaySound_Strict LoadTempSound("SFX\Ending\GateA\106Escape.ogg") 
						
						DrawLoading(100)
					Else
						
						ShouldPlay = 17
						
						e\EventState = e\EventState+FPSfactor
						HideEntity Fog
						CameraFogRange Camera, 5,30
						
						angle = Max(Sin(EntityYaw(Collider)+90),0.0)
						;240,220,200
						CameraFogColor (Camera,200+(angle*40),200+(angle*20),200)
						CameraClsColor (Camera,200+(angle*40),200+(angle*20),200)		
						CameraRange(Camera, 0.05, 30)
						
						AmbientLight (140, 140, 140)
						
						For i = 2 To 4
							If e\room\NPC[i]<>Null Then 
								If e\room\NPC[i]\State < 2 Then 
									PositionEntity(e\room\NPC[i]\Collider, EntityX(e\room\Objects[3],True)+Cos(e\EventState/10+(120*i))*6000.0*RoomScale,e\room\y+11,EntityZ(e\room\Objects[3],True)+Sin(e\EventState/10+(120*i))*6000.0*RoomScale)
									RotateEntity e\room\NPC[i]\Collider,7.0,(e\EventState/10+(120*i)),20.0
								EndIf
							EndIf
						Next
						
						UpdateSky()
						
						If e\EventState=>350 Then
							If Contained106=False Then
								If e\EventState-FPSfactor < 350
									Curr106\State = -0.1
									;Curr106\Idle = True
									SetNPCFrame(Curr106, 110.0)
									PositionEntity (Curr106\Collider, EntityX(e\room\Objects[3],True),EntityY(Collider)-50.0,EntityZ(e\room\Objects[3],True),True)
									PositionEntity (Curr106\obj, EntityX(e\room\Objects[3],True),EntityY(Collider)-50.0,EntityZ(e\room\Objects[3],True),True)
									de.Decals = CreateDecal(0, EntityX(e\room\Objects[3],True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(e\room\Objects[3],True), 90, Rand(360), 0)
									de\Size = 0.05 : de\SizeChange = 0.001 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 
									PlaySound_Strict (HorrorSFX(5))
									PlaySound_Strict DecaySFX(0)
								ElseIf Curr106\State < 0
									HideEntity Curr106\obj2
									Curr106\PathTimer = 70*100
									
									If Curr106\State3 = 0 Then
										If Curr106\PathStatus <> 1 Then
											PositionEntity Curr106\Collider,EntityX(e\room\Objects[3],True),EntityY(Curr106\Collider),EntityZ(e\room\Objects[3],True),True
											If Curr106\State =< -10 Then
												dist# = EntityY(Curr106\Collider)
												PositionEntity Curr106\Collider,EntityX(Curr106\Collider),EntityY(e\room\Objects[3],True),EntityZ(Curr106\Collider),True
												;Curr106\PathStatus = FindPath(Curr106, EntityX(e\room\Objects[4],True),EntityY(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
												Curr106\PathStatus = FindPath(Curr106,EntityX(e\room\NPC[5]\Collider,True),EntityY(e\room\NPC[5]\Collider,True),EntityZ(e\room\NPC[5]\Collider,True))
												Curr106\PathTimer = 70*200
												PositionEntity Curr106\Collider,EntityX(Curr106\Collider),dist,EntityZ(Curr106\Collider),True
												ResetEntity Curr106\Collider
												Curr106\PathLocation = 1
												;Curr106\Idle = False
											;Else	
												;PositionEntity (Curr106\Collider, EntityX(e\room\Objects[3],True),EntityY(e\room\Objects[3],True),EntityZ(e\room\Objects[3],True),True)
												;Curr106\Idle = True
												;Animate2(Curr106\obj, AnimTime(Curr106\obj), 110, 259, 0.15, False)
												;If AnimTime(Curr106\obj)=>259 Then Curr106\Idle = False													
												
											EndIf
										Else
											Curr106\PathTimer = 70*200
											For i = 2 To 4 ;helicopters start attacking 106
												e\room\NPC[i]\State = 3 
												e\room\NPC[i]\EnemyX = EntityX(Curr106\obj,True)
												e\room\NPC[i]\EnemyY = EntityY(Curr106\obj,True)+5.0
												e\room\NPC[i]\EnemyZ = EntityZ(Curr106\obj,True)
											Next
											
											For i = 5 To 8
												e\room\NPC[i]\State = 5
												e\room\NPC[i]\EnemyX = EntityX(Curr106\obj,True)
												e\room\NPC[i]\EnemyY = EntityY(Curr106\obj,True)+0.4
												e\room\NPC[i]\EnemyZ = EntityZ(Curr106\obj,True)											
											Next
											
											pvt=CreatePivot()
											PositionEntity pvt, EntityX(e\room\Objects[10],True),EntityY(e\room\Objects[10],True),EntityZ(e\room\Objects[10],True)
											PointEntity pvt, Curr106\Collider
											RotateEntity(e\room\Objects[9],0,CurveAngle(EntityYaw(pvt),EntityYaw(e\room\Objects[9],True),150.0),0,True)
											RotateEntity(e\room\Objects[10],CurveAngle(EntityPitch(pvt),EntityPitch(e\room\Objects[10],True),200.0),EntityYaw(e\room\Objects[9],True),0, True)
											
											FreeEntity pvt
											
											If FPSfactor > 0 Then ;decals under 106
												If ((e\EventState-FPSfactor) Mod 100.0)=<50.0 And (e\EventState Mod 100.0)>50.0 Then
													de.Decals = CreateDecal(0, EntityX(Curr106\Collider,True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(Curr106\Collider,True), 90, Rand(360), 0)
													de\Size = 0.2 : de\SizeChange = 0.004 : de\timer = 90000 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 											
												EndIf
											EndIf
										EndIf
									EndIf
									
									dist# = Distance(EntityX(Curr106\Collider),EntityZ(Curr106\Collider),EntityX(e\room\Objects[4],True),EntityZ(e\room\Objects[4],True))
									
									Curr106\CurrSpeed = CurveValue(0, Curr106\CurrSpeed, Max(5*dist,2.0))
									If dist < 15.0 Then
										If e\SoundCHN2 = 0 Then
											e\SoundCHN2 = PlaySound_Strict (LoadTempSound("SFX\Ending\GateA\Franklin.ogg"))
										EndIf
										
										If dist<0.4 Then
											Curr106\PathStatus = 0
											Curr106\PathTimer = 70*200
											If Curr106\State3=0 Then 
												SetNPCFrame(Curr106, 259.0)
												If e\Sound <> 0 Then FreeSound_Strict e\Sound : e\Sound = 0
												LoadEventSound(e,"SFX\Ending\GateA\106Retreat.ogg")
												e\SoundCHN = PlaySound2(e\Sound, Camera, Curr106\Collider, 35.0)
											EndIf
											
											If FPSfactor > 0 Then ;106:n alle ilmestyy decaleita
												If ((e\EventState-FPSfactor) Mod 160.0)=<50.0 And (e\EventState Mod 160.0)>50.0 Then
													de.Decals = CreateDecal(0, EntityX(Curr106\Collider,True),EntityY(e\room\Objects[3],True)+0.01,EntityZ(Curr106\Collider,True), 90, Rand(360), 0)
													de\Size = 0.05 : de\SizeChange = 0.004 : de\timer = 90000 : EntityAlpha(de\obj, 0.8) : UpdateDecals() 											
												EndIf
											EndIf
											
											AnimateNPC(Curr106, 259, 110, -0.1, False)
											
											Curr106\State3 = Curr106\State3+FPSfactor
											PositionEntity(Curr106\Collider, EntityX(Curr106\Collider,True),CurveValue(EntityY(e\room\Objects[3],True)-(Curr106\State3/4500.0),EntityY(Curr106\Collider,True),100.0),EntityZ(Curr106\Collider,True))
											If Curr106\State3>700.0 Then
												Curr106\State = 100000
												e\EventState2 = 0
												For i = 5 To 8
													e\room\NPC[i]\State = 1
												Next
												For i = 2 To 4 ;helicopters attack the player
													e\room\NPC[i]\State = 2
												Next
												HideEntity Curr106\obj
											EndIf
										Else
											If dist < 8.5 Then 
												If e\EventState2=0;ChannelPlaying(e\SoundCHN2) = 0 Then
													e\SoundCHN2 = PlaySound_Strict (LoadTempSound("SFX\Ending\GateA\HIDTurret.ogg"))
													e\EventState2 = 1
												ElseIf e\EventState2>0
													e\EventState2=e\EventState2+FPSfactor
													If e\EventState2=> 7.5*70 Then
														If e\EventState2-FPSfactor < 7.5*70 Then
															p.Particles = CreateParticle(EntityX(Curr106\obj,True),EntityY(Curr106\obj,True)+0.4, EntityZ(Curr106\obj,True), 4, 7.0, 0, (6.7*70))
															p\speed = 0.0
															p\A = 1.0
															EntityParent p\pvt, Curr106\Collider, True
															
															p.Particles = CreateParticle(EntityX(e\room\Objects[10],True),EntityY(e\room\Objects[10],True),EntityZ(e\room\Objects[10],True), 4, 2.0, 0, (6.7*70))
															RotateEntity p\pvt, EntityPitch(e\room\Objects[10],True),EntityYaw(e\room\Objects[10],True),0,True
															MoveEntity p\pvt, 0, 92.0*RoomScale, 512.0*RoomScale
															p\speed = 0.0
															p\A = 1.0
															EntityParent p\pvt, e\room\Objects[10], True
														ElseIf e\EventState2 < 14.3*70
															CameraShake = 0.5
															LightFlash = 0.3+EntityInView(e\room\Objects[10],Camera)*0.5
														EndIf
													EndIf
												EndIf
												
												If ParticleAmount > 0
													For i = 0 To Rand(2,2+(6*(ParticleAmount-1)))-Int(dist)
														p.Particles = CreateParticle(EntityX(Curr106\obj,True),EntityY(Curr106\obj,True)+Rnd(0.4,0.9), EntityZ(Curr106\obj), 0, 0.006, -0.002, 40)
														p\speed = 0.005
														p\A = 0.8
														p\Achange = -0.01
														RotateEntity p\pvt, -Rnd(70,110), Rnd(360),0	
													Next										
												EndIf
											EndIf
											
											
										EndIf
									EndIf
								EndIf
								
								If e\EventState3 = 0.0 Then 
									If Abs(EntityY(Collider)-EntityY(e\room\Objects[11],True))<1.0 Then
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True)) < 12.0 Then
											Curr106\State = 100000
											HideEntity Curr106\obj
											
											;MTF spawns at the tunnel entrance
											For i = 5 To 8
												e\room\NPC[i]\State = 3
												PositionEntity e\room\NPC[i]\Collider, EntityX(e\room\Objects[15],True)+(i-6)*0.3,EntityY(e\room\Objects[15],True),EntityZ(e\room\Objects[15],True)+(i-6)*0.3, True
												ResetEntity e\room\NPC[i]\Collider
												
												e\room\NPC[i]\PathStatus = FindPath(e\room\NPC[i], EntityX(Collider),EntityY(Collider)+0.2,EntityZ(Collider))
												e\room\NPC[i]\PathTimer = 70*2
												e\room\NPC[i]\LastSeen = 70*100
											Next
											e\room\NPC[5]\Sound = LoadSound_Strict("SFX\Character\MTF\ThereHeIs1.ogg")
											PlaySound2(e\room\NPC[5]\Sound, Camera, e\room\NPC[5]\Collider, 25.0)
											
											e\room\RoomDoors[2]\open = True
											
											For i = 2 To 4
												RemoveNPC(e\room\NPC[i])
												e\room\NPC[i]=Null
											Next
											
											e\EventState3 = 1.0
										EndIf
									EndIf
								ElseIf e\EventState3 = 1.0
									
									For i = 5 To 8
										If EntityDistance(e\room\NPC[i]\Collider,Collider)> 4.0 Then
											e\room\NPC[i]\State = 3
										Else
											e\room\NPC[i]\State = 1
										EndIf
									Next
									
									If Abs(EntityY(Collider)-EntityY(e\room\Objects[11],True))<1.0 Then
										If Distance(EntityX(Collider),EntityZ(Collider),EntityX(e\room\Objects[11],True),EntityZ(e\room\Objects[11],True)) < 7.0 Then
											e\room\Objects[12] = LoadMesh_Strict("GFX\npcs\s2.b3d")
											EntityColor e\room\Objects[12], 0,0,0
											ScaleMesh (e\room\Objects[12], 0.32/21.3, 0.32/21.3, 0.32/21.3)
											PositionEntity e\room\Objects[12], EntityX(e\room\Objects[11],True), EntityY(e\room\Objects[11],True), EntityZ(e\room\Objects[11],True)
											
											e\room\Objects[17] = CopyEntity(e\room\Objects[12])
											PositionEntity e\room\Objects[17], EntityX(e\room\obj,True)-3968*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-1920*RoomScale
											
											obj = CopyEntity(e\room\Objects[12])
											PositionEntity obj, EntityX(e\room\obj,True)-4160*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-1920*RoomScale
											EntityParent obj,e\room\Objects[17]
											
											obj = CopyEntity(e\room\Objects[12])
											PositionEntity obj, EntityX(e\room\obj,True)-4064*RoomScale, EntityY(e\room\Objects[11],True), EntityZ(e\room\obj,True)-2112*RoomScale
											EntityParent obj,e\room\Objects[17]
											
											e\SoundCHN = PlaySound2(LoadTempSound("SFX\Ending\GateA\Bell1.ogg"), Camera, e\room\Objects[12])
											
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.15
											p\A = 0.5
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.25
											p\A = 0.5
											PointEntity p\pvt, Collider
											
											CameraShake = 1.0
											LightFlash = 1.0
											
											e\EventState3 = 2.0
										EndIf
									EndIf
								Else
									e\EventState3=e\EventState3+FPSfactor
									PointEntity e\room\Objects[12], Collider
									RotateEntity e\room\Objects[12], 0, EntityYaw(e\room\Objects[12]), 0
									
									Stamina = -5.0
									
									BlurTimer = Sin(e\EventState3*0.7)*1000.0
									
									If KillTimer = 0 Then 
										CameraZoom(Camera, 1.0+Sin(e\EventState3*0.8)*0.2)
										
										dist = EntityDistance(Collider,e\room\Objects[11])
										If dist < 6.5 Then
											PositionEntity(Collider, CurveValue(EntityX(e\room\Objects[11],True),EntityX(Collider),dist*80),EntityY(Collider),CurveValue(EntityZ(e\room\Objects[0],True),EntityZ(Collider),dist*80))
										EndIf
									EndIf
									
									;tunneli menee umpeen
									If e\EventState3>50 And e\EventState3<230 Then
										CameraShake = Sin(e\EventState3-50)*3
										TurnEntity e\room\Objects[13], 0, (Sin(e\EventState3-50)*-0.85)*FPSfactor, 0, True
										TurnEntity e\room\Objects[14], 0, (Sin(e\EventState3-50)*0.85)*FPSfactor, 0, True
										
										For i = 5 To 8
											PositionEntity (e\room\NPC[i]\Collider, CurveValue(EntityX(e\room\RoomDoors[2]\frameobj,True), EntityX(e\room\NPC[i]\Collider,True),50.0),EntityY(e\room\NPC[i]\Collider,True),CurveValue(EntityZ(e\room\RoomDoors[2]\frameobj,True), EntityZ(e\room\NPC[i]\Collider,True),50.0),True)
											ResetEntity e\room\NPC[i]\Collider
										Next
									EndIf
									
									If e\EventState3=>230.0 Then
										If e\EventState3-FPSfactor<230.0 Then
											e\SoundCHN = PlaySound_Strict(LoadTempSound("SFX\Ending\GateA\CI.ogg"))
										EndIf
										
										If ChannelPlaying(e\SoundCHN)=False And SelectedEnding="" Then
											PlaySound_Strict LoadTempSound("SFX\Ending\GateA\Bell2.ogg")
											
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.15
											p\A = 0.5
											p.Particles = CreateParticle(EntityX(e\room\Objects[11],True),EntityY(Camera,True), EntityZ(e\room\Objects[11],True), 4, 8.0, 0, 50)
											p\speed = 0.25
											p\A = 0.5
											
											SelectedEnding = "A1"
											GodMode = 0
											NoClip = 0
											KillTimer = -0.1
											DeathMSG = ""
											Kill()
										EndIf
										
										If SelectedEnding <> "" Then
											CameraShake=CurveValue(2.0,CameraShake,10.0)
											LightFlash = CurveValue(2.0,LightFlash,8.0);Min(Abs(KillTimer)/100.0,1.0)
										EndIf
										
									EndIf
								EndIf
								
							Else ;contained106 = true
								
								If e\EventState2 = 0 Then
									;PositionEntity (e\room\NPC[5]\Collider, EntityX(e\room\obj,True)-3408*RoomScale, EntityY(e\room\obj,True)-796*RoomScale, EntityZ(e\room\obj,True)+4976, True)
									;ResetEntity e\room\NPC[5]\Collider
									e\EventState2 = 1
									
									For i = 5 To 8
										e\room\NPC[i]\State = 3
										
										e\room\NPC[i]\PathStatus = FindPath(e\room\NPC[i], EntityX(e\room\obj)-1.0+2.0*(i Mod 2),EntityY(Collider)+0.2,EntityZ(e\room\obj)-2.0*(i Mod 2))
										e\room\NPC[i]\PathTimer = 70*Rand(15,20)
										e\room\NPC[i]\LastSeen = 70*300
									Next
								Else
									
									For i = 5 To 8
										If e\room\NPC[i]\State = 5
											e\room\NPC[i]\EnemyX = EntityX(Collider)
											e\room\NPC[i]\EnemyY = EntityY(Collider)
											e\room\NPC[i]\EnemyZ = EntityZ(Collider)
										Else
											If EntityDistance(e\room\NPC[i]\Collider,Collider)<6.0
												e\room\NPC[i]\State = 5
												e\room\NPC[i]\CurrSpeed = 0
											EndIf
										EndIf
									Next
									
									If e\EventState2=<1 Then
										For i = 5 To 8
											If e\room\NPC[i]\State = 5 Then
												For temp = 5 To 8
													e\room\NPC[temp]\State = 5
													e\room\NPC[temp]\EnemyX = EntityX(Collider)
													e\room\NPC[temp]\EnemyY = EntityY(Collider)
													e\room\NPC[temp]\EnemyZ = EntityZ(Collider)
													e\room\NPC[temp]\PathTimer = 70*Rand(7,10)
													e\room\NPC[temp]\Reload = 2000
													UnableToMove% = True
												Next
												
												If e\EventState2=1 Then
													e\SoundCHN = PlaySound_Strict (LoadTempSound("SFX\Ending\GateA\STOPRIGHTTHERE.ogg"))
													e\EventState2=2			
												EndIf
											Else
												e\room\NPC[i]\LastSeen = 70*300
												e\room\NPC[i]\Reload = 2000
												e\room\NPC[i]\State3 = 70*145											
											EndIf
										Next										
									Else
										
										ShouldPlay = 0
										CurrSpeed = 0
										If ChannelPlaying(e\SoundCHN)=False Then
											PlaySound_Strict IntroSFX(9)
											SelectedEnding = "A2"
											GodMode = 0
											NoClip = 0
											KillTimer = -0.1
											DeathMSG = ""
											Kill()
											BlinkTimer = -10
											RemoveEvent(e)
											Exit
										EndIf
									EndIf									
									
								EndIf
								
							EndIf
						EndIf
						
					EndIf
				Else
					HideEntity e\room\obj
				EndIf
				;[End Block]
		End Select
	Next
	
	If ExplosionTimer > 0 Then
		ExplosionTimer = ExplosionTimer+FPSfactor
		
		If ExplosionTimer < 140.0 Then
			If ExplosionTimer-FPSfactor < 5.0 Then
				ExplosionSFX = LoadSound_Strict("SFX\Ending\GateB\Nuke1.ogg")
				PlaySound_Strict ExplosionSFX
				CameraShake = 10.0
				ExplosionTimer = 5.0
			EndIf
			
			CameraShake = CurveValue(ExplosionTimer/60.0,CameraShake, 50.0)
		Else
			CameraShake = Min((ExplosionTimer/20.0),20.0)
			If ExplosionTimer-FPSfactor < 140.0 Then
				BlinkTimer = 1.0
				ExplosionSFX = LoadSound_Strict("SFX\Ending\GateB\Nuke2.ogg")
				PlaySound_Strict ExplosionSFX				
				For i = 0 To (10+(10*(ParticleAmount+1)))
					p.Particles = CreateParticle(EntityX(Collider)+Rnd(-0.5,0.5),EntityY(Collider)-Rnd(0.2,1.5),EntityZ(Collider)+Rnd(-0.5,0.5),0, Rnd(0.2,0.6), 0.0, 350)	
					RotateEntity p\pvt,-90,0,0,True
					p\speed = Rnd(0.05,0.07)
				Next
			EndIf
			LightFlash = Min((ExplosionTimer-140.0)/10.0,5.0)
			
			If ExplosionTimer > 160 Then KillTimer = Min(KillTimer,-0.1)
			If ExplosionTimer > 500 Then ExplosionTimer = 0
			
			;a dirty workaround to prevent the collider from falling down into the facility once the nuke goes off,
			;causing the UpdateEvent function to be called again and crashing the game
			PositionEntity Collider, EntityX(Collider), 200, EntityZ(Collider)
		EndIf
		
	EndIf
	
End Function
































;~IDEal Editor Parameters:
;~F#1#14#29#13B#343#535#545#5B1#630#68D#6B4#6C2#6CC#6D9#8CA#8EB#93F#976#983#9BD
;~F#9CE#9EE#9F7#A01#A10#B0B#B2D#DE2#E29#E3F#E4B#E68#EB9#ED2#FA1#10A3#1123#113C#115B#11C6
;~F#11D3#11EC#1284#1439#152D#1581#1633#16D4#1796#17A9#187A#18A7#18C4#18EB#191B#1941#1969#19BB#19F8#1A29
;~F#1A3C#1AFD#1B6A#1B7D#1B8B#1BCF#1BF0#1CDE#1D53#1E50#1ED1#1F1F#1F24#1F73#1F79#213E
;~B#10D3#1DD1
;~C#Blitz3D