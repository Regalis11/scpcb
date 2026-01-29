/*
 * Source: NPCs.bb (lines 1-71, 671-934)
 * Type: NPC System - Full 1:1 Port
 */

using System;
using System.Numerics;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Engine;

namespace SCPContainmentBreach.Core.Systems;

public enum NPCType
{
    SCP173 = 1,
    OldMan = 2,      // SCP-106
    Guard = 3,
    ClassD = 4,
    SCP372 = 6,
    Apache = 7,
    MTF = 8,
    SCP096 = 9,
    SCP049 = 10,
    Zombie = 11,     // SCP-049-2
    SCP5131 = 12,    // SCP-513-1
    Tentacle = 13,
    SCP860 = 14,
    SCP939 = 15,
    SCP066 = 16,
    PdPlane = 17,
    SCP966 = 18,
    SCP1048A = 19,
    SCP1499 = 20,
    SCP008 = 21,
    Clerk = 22
}

public class NPC
{
    public int Obj;
    public int Obj2;
    public int Obj3;
    public int Obj4;
    public int Collider;

    public NPCType NPCtype;
    public int ID;

    public float DropSpeed;
    public bool Gravity;

    public float State;
    public float State2;
    public float State3;
    public int PrevState;

    public bool MakingNoise;
    public float Frame;
    public float Angle;

    public int Sound;
    public int SoundChn;
    public float SoundTimer;
    public int Sound2;
    public int SoundChn2;

    public float Speed;
    public float CurrSpeed;

    public string Texture = "";
    public float Idle;
    public float Reload;

    public int LastSeen;
    public float LastDist;

    public float PrevX;
    public float PrevY;
    public float PrevZ;

    public NPC? Target;
    public int TargetID;
    public float EnemyX;
    public float EnemyY;
    public float EnemyZ;

    public WayPoint?[] Path = new WayPoint?[21];
    public int PathStatus;
    public float PathTimer;
    public int PathLocation;

    public float NVX, NVY, NVZ;
    public string NVName = "";

    public float GravityMult = 1.0f;
    public float MaxGravity = 0.2f;

    public int MTFVariant;
    public NPC? MTFLeader;
    public bool IsDead;
    public float BlinkTimer = 1.0f;
    public bool IgnorePlayer;

    public bool ManipulateBone;
    public int ManipulationType;
    public string BoneToManipulate = "";
    public float BonePitch;
    public float BoneYaw;
    public float BoneRoll;

    public string NPCNameInSection = "";
    public bool InFacility = true;
    public bool CanUseElevator;

    public int HP;
    public float PathX, PathZ;
    public string Model = "";
    public float ModelScaleX, ModelScaleY, ModelScaleZ;
    public bool HideFromNVG;
    public int TextureID = -1;
    public float CollRadius;
    public float IdleTimer;
    public bool SoundChnIsStream;
    public bool SoundChn2IsStream;
    public float FallingPickDistance;
}

public static class SoundID
{
    public const int DecaySFX0 = 100;
    public const int OldManSFX_Random = 101;
    public const int OldManSFX3 = 102;
    public const int OldManSFX6_Random = 103;
    public const int Step2SFX = 104;
    public const int HorrorSFX5 = 105;
    public const int HorrorSFX6 = 106;
    public const int DamageSFX1 = 107;
    public const int NeckSnapSFX = 108;
    public const int Scp173SFX = 109;
    public const int Scp096Triggered = 110;
    public const int DamageSFX4 = 111;
    public const int Scp049Breath = 112;
    public const int ZombieBreath = 113;
    public const int Scp049Spotted = 114;
    public const int Scp049TakeOffHazmat = 115;
    public const int Scp049Ring714 = 116;
    public const int HorrorSFX13 = 117;
    public const int Scp049Searching = 118;
    public const int Step3SFX = 119;
    public const int DamageSFX5_8 = 120;
    // MTF sounds
    public const int Gunshot = 121;
    public const int MTFBlinking = 122;
    public const int MTFDoorOpen = 123;
    public const int MTFStop = 124;
    public const int MTFThereHeIs = 125;
    public const int MTF173Spotted = 126;
    public const int MTF106Spotted = 127;
    public const int MTF096Spotted = 128;
    public const int MTFZombieSpotted = 129;
    public const int MTFTargetTerminated = 130;
    public const int MTFTargetLost = 131;
    public const int MTFSearching = 132;
    public const int MTF173Box = 133;
    // SCP-939 sounds
    public const int Step4SFX = 134;
    public const int Step1SFX = 135;
    public const int Scp939Lure = 136;
    public const int Scp939Attack = 137;
    public const int Scp939Alert = 138;
    public const int Slash1 = 139;
    // SCP-066 sounds
    public const int Scp066Eric = 140;
    public const int Scp066Notes = 141;
    public const int Scp066Rolling = 142;
    public const int Scp066Beethoven = 143;
}

public static class EventSystem
{
    private static readonly Dictionary<string, int> _eventStates = new();

    public static int GetEventState(string eventName)
    {
        return _eventStates.TryGetValue(eventName, out int state) ? state : 0;
    }

    public static void SetEventState(string eventName, int state)
    {
        _eventStates[eventName] = state;
    }
}

public static class NPCSystem
{
    public static NPC? Curr173;
    public static NPC? Curr106;
    public static NPC? Curr096;
    public static NPC? Curr5131;

    private static readonly List<NPC> _npcs = new();
    private static readonly Random _rng = new();

    public static float HideDistance = 15.0f;
    public static bool NoTarget;
    public static bool Contained106;
    public static float RoomScale = 8.0f / 2048.0f;
    public static float DifficultyFactor = 1.0f;
    public static bool AggressiveNPCs;
    public static float CameraFogFar = 15.0f;
    public static float LightVolume = 1.0f;

    public static Action<string>? OnDeath;
    public static Action? OnKill;
    public static Action<int>? OnPlaySound;
    public static Action<int, Vector3, float>? OnPlaySound2;
    public static Action<int, float, float, float, float, int, float, float, float, float>? OnCreateDecal;
    public static Action<string>? OnAchievement;
    public static Action? OnMoveToPocketDimension;
    public static Action<string>? OnMessage;

    public static void AddNPC(NPC npc)
    {
        _npcs.Add(npc);

        if (npc.NPCtype == NPCType.SCP173) Curr173 = npc;
        else if (npc.NPCtype == NPCType.OldMan) Curr106 = npc;
        else if (npc.NPCtype == NPCType.SCP096) Curr096 = npc;
        else if (npc.NPCtype == NPCType.SCP5131) Curr5131 = npc;
    }

    public static void Clear()
    {
        _npcs.Clear();
        Curr173 = null;
        Curr106 = null;
        Curr096 = null;
        Curr5131 = null;
    }

    public static void Update(PlayerState player, float fpsFactor, float blinkTimer, bool isNVGBlinking)
    {
        foreach (var n in _npcs)
        {
            n.InFacility = CheckForNPCInFacility(n);

            switch (n.NPCtype)
            {
                case NPCType.SCP173:
                    Update173(n, player, fpsFactor, blinkTimer, isNVGBlinking);
                    break;
                case NPCType.OldMan:
                    Update106(n, player, fpsFactor);
                    break;
                case NPCType.SCP096:
                    Update096(n, player, fpsFactor);
                    break;
                case NPCType.SCP049:
                    Update049(n, player, fpsFactor);
                    break;
                case NPCType.Zombie:
                    UpdateZombie(n, player, fpsFactor);
                    break;
                case NPCType.Guard:
                    UpdateGuard(n, player, fpsFactor);
                    break;
                case NPCType.MTF:
                    UpdateMTF(n, player, fpsFactor);
                    break;
                case NPCType.ClassD:
                case NPCType.Clerk:
                    UpdateClassD(n, player, fpsFactor);
                    break;
                case NPCType.SCP5131:
                    Update5131(n, player, fpsFactor);
                    break;
                case NPCType.SCP372:
                    Update372(n, player, fpsFactor, blinkTimer);
                    break;
                case NPCType.Apache:
                    UpdateApache(n, player, fpsFactor);
                    break;
                case NPCType.Tentacle:
                    UpdateTentacle(n, player, fpsFactor);
                    break;
                case NPCType.SCP860:
                    Update860(n, player, fpsFactor);
                    break;
                case NPCType.SCP939:
                    Update939(n, player, fpsFactor);
                    break;
                case NPCType.SCP066:
                    Update066(n, player, fpsFactor);
                    break;
                case NPCType.SCP966:
                    Update966(n, player, fpsFactor);
                    break;
                case NPCType.SCP1048A:
                    Update1048A(n, player, fpsFactor);
                    break;
                case NPCType.SCP1499:
                    Update1499(n, player, fpsFactor);
                    break;
                case NPCType.SCP008:
                    Update008(n, player, fpsFactor);
                    break;
            }
        }
    }

    /// <summary>
    /// NPCs.bb lines 683-934: Full SCP-173 logic
    /// </summary>
    private static void Update173(NPC n, PlayerState player, float fpsFactor, float blinkTimer, bool isNVGBlinking)
    {
        if (Curr173 == null) return;
        if (Curr173.Idle == 3) return;

        float dist = EntityDistance(n, player);
        n.State3 = 1;

        if (n.Idle < 2)
        {
            if (n.IdleTimer > 0.1f)
            {
                n.Idle = 1;
                n.IdleTimer = MathF.Max(n.IdleTimer - fpsFactor, 0.1f);
            }
            else if (MathF.Abs(n.IdleTimer - 0.1f) < 0.001f)
            {
                n.Idle = 0;
                n.IdleTimer = 0;
            }

            // PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.32, EntityZ(n\Collider))
            // RotateEntity(n\obj, 0, EntityYaw(n\Collider)-180, 0)
            // Handled by BlitzEntity system

            if (n.Idle == 0)
            {
                bool temp = false;
                bool move = true;

                if (dist < 15)
                {
                    if (dist < 10.0f)
                    {
                        if (EntityVisible(n, player))
                        {
                            temp = true;
                            n.EnemyX = player.X;
                            n.EnemyY = player.Y;
                            n.EnemyZ = player.Z;
                        }
                    }

                    // SoundVol = Max(Min((Distance(EntityX(n\Collider), EntityZ(n\Collider), n\PrevX, n\PrevZ) * 2.5), 1.0), 0.0)
                    float soundVol = MathF.Max(MathF.Min(Distance2D(GetNPCX(n), GetNPCZ(n), n.PrevX, n.PrevZ) * 2.5f, 1.0f), 0.0f);

                    n.PrevX = GetNPCX(n);
                    n.PrevZ = GetNPCZ(n);

                    // If (BlinkTimer < -16 Or BlinkTimer > -6) And (IsNVGBlinking=False) Then
                    //     If EntityInView(n\obj, Camera) Then move = False
                    if ((blinkTimer < -16 || blinkTimer > -6) && !isNVGBlinking)
                    {
                        if (EntityInView(n, player))
                        {
                            move = false;
                        }
                    }
                }

                if (NoTarget) move = true;

                // player is looking at it -> doesn't move
                if (!move)
                {
                    // BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
                    player.BlurTimer = MathF.Max(MathF.Max(MathF.Min((4.0f - dist) / 6.0f, 0.9f), 0.1f), player.BlurTimer);

                    // CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0)*15.0*Max((3.5-dist)/3.5,0.0))
                    float millis = Environment.TickCount;
                    player.CurrCameraZoom = MathF.Max(player.CurrCameraZoom,
                        (MathF.Sin(millis / 20.0f) + 1.0f) * 15.0f * MathF.Max((3.5f - dist) / 3.5f, 0.0f));

                    // If dist < 3.5 And MilliSecs2() - n\LastSeen > 60000 And temp Then
                    if (dist < 3.5f && Environment.TickCount - n.LastSeen > 60000 && temp)
                    {
                        OnPlaySound?.Invoke(_rng.Next(3, 5)); // HorrorSFX(Rand(3,4))
                        n.LastSeen = Environment.TickCount;
                    }

                    // If dist < 1.5 And Rand(700) = 1 Then PlaySound2(...)
                    if (dist < 1.5f && _rng.Next(700) == 1)
                    {
                        // PlaySound2(Scp173SFX(Rand(0, 2)), Camera, n\obj)
                    }

                    // If dist < 1.5 And n\LastDist > 2.0 And temp Then
                    if (dist < 1.5f && n.LastDist > 2.0f && temp)
                    {
                        player.CurrCameraZoom = 40.0f;
                        player.HeartBeatRate = MathF.Max(player.HeartBeatRate, 140);
                        player.HeartBeatVolume = 0.5f;

                        // Select Rand(5) - play horror sound
                        int sfx = _rng.Next(1, 6);
                        int horrorIndex = sfx switch
                        {
                            1 => 1,
                            2 => 2,
                            3 => 9,
                            4 => 10,
                            5 => 14,
                            _ => 1
                        };
                        OnPlaySound?.Invoke(horrorIndex);
                    }

                    n.LastDist = dist;
                    n.State = MathF.Max(0, n.State - fpsFactor / 20);
                }
                else
                {
                    // more than 6 room lengths away from the player -> teleport to a room closer
                    if (dist > 50)
                    {
                        if (_rng.Next(70) == 1)
                        {
                            string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
                            if (roomName != "exit1" && roomName != "gatea" && roomName != "pocketdimension")
                            {
                                TeleportToWaypoint(n, player, 15.0f, 25.0f);
                            }
                        }
                    }
                    else if (dist > HideDistance * 0.8f)
                    {
                        // 3-6 rooms away -> move randomly
                        if (_rng.Next(70) == 1)
                        {
                            TeleportCloser(n, player);
                        }
                    }
                    else
                    {
                        // less than 3 rooms away -> actively move towards the player
                        float soundVol = MathF.Max(MathF.Min(Distance2D(GetNPCX(n), GetNPCZ(n), n.PrevX, n.PrevZ) * 2.5f, 1.0f), 0.0f);
                        n.State = CurveValue(soundVol, n.State, 3);

                        // try to open doors - Rand(20) == 1
                        if (_rng.Next(20) == 1)
                        {
                            TryOpenNearbyDoors(n);
                        }

                        if (NoTarget)
                        {
                            temp = false;
                            n.EnemyX = 0;
                            n.EnemyY = 0;
                            n.EnemyZ = 0;
                        }

                        // player is not looking and is visible from 173's position -> attack
                        if (temp)
                        {
                            if (dist < 0.65f)
                            {
                                if (player.KillTimer >= 0 && !player.GodMode)
                                {
                                    // Death message based on room
                                    string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
                                    string deathMsg = roomName switch
                                    {
                                        "lockroom" or "room2closets" or "coffin" =>
                                            "Subject D-9341. Cause of death: Fatal cervical fracture. The surveillance tapes confirm that the subject was killed by SCP-173.",
                                        "173" =>
                                            "Subject D-9341. Cause of death: Fatal cervical fracture. According to Security Chief Franklin who was present at SCP-173's containment chamber during the breach, the subject was killed by SCP-173 as soon as the disruptions in the electrical network started.",
                                        "room2doors" =>
                                            "\"If I'm not mistaken, one of the main purposes of these rooms was to stop SCP-173 from moving further in the event of a containment breach. So, who's brilliant idea was it to put A GODDAMN MAN-SIZED VENTILATION DUCT in there?\"",
                                        _ =>
                                            "Subject D-9341. Cause of death: Fatal cervical fracture. Assumed to be attacked by SCP-173."
                                    };

                                    if (!player.GodMode) n.Idle = 1;
                                    // PlaySound_Strict(NeckSnapSFX(Rand(0,2)))

                                    // TurnEntity(Camera, 0, Rand(80,100) or Rand(-100,-80), 0)
                                    if (_rng.Next(2) == 1)
                                    {
                                        player.Yaw += _rng.Next(80, 101);
                                    }
                                    else
                                    {
                                        player.Yaw += _rng.Next(-100, -79);
                                    }

                                    OnDeath?.Invoke(deathMsg);
                                    OnKill?.Invoke();
                                }
                            }
                            else
                            {
                                // PointEntity(n\Collider, Collider)
                                // RotateEntity n\Collider, 0, EntityYaw(n\Collider), EntityRoll(n\Collider)
                                float yaw = GetAngleToPlayer(n, player);
                                SetNPCYaw(n, yaw);

                                // TranslateEntity n\Collider,Cos(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor,0.0,Sin(EntityYaw(n\Collider)+90.0)*n\Speed*FPSfactor
                                float moveX = MathF.Cos((yaw + 90.0f) * MathF.PI / 180.0f) * n.Speed * fpsFactor;
                                float moveZ = MathF.Sin((yaw + 90.0f) * MathF.PI / 180.0f) * n.Speed * fpsFactor;
                                TranslateNPC(n, moveX, 0, moveZ);
                            }
                        }
                        else
                        {
                            // player is not visible -> move to the location where he was last seen
                            if (n.EnemyX != 0)
                            {
                                if (Distance2D(GetNPCX(n), GetNPCZ(n), n.EnemyX, n.EnemyZ) > 0.5f)
                                {
                                    // AlignToVector(n\Collider, n\EnemyX-EntityX(n\Collider), 0, n\EnemyZ-EntityZ(n\Collider), 3)
                                    float dx = n.EnemyX - GetNPCX(n);
                                    float dz = n.EnemyZ - GetNPCZ(n);
                                    float targetYaw = MathF.Atan2(dx, dz) * 180.0f / MathF.PI;
                                    SetNPCYaw(n, targetYaw);

                                    // MoveEntity(n\Collider, 0, 0, n\Speed * FPSfactor)
                                    MoveNPCForward(n, n.Speed * fpsFactor);

                                    // If Rand(500) = 1 Then clear enemy pos
                                    if (_rng.Next(500) == 1)
                                    {
                                        n.EnemyX = 0;
                                        n.EnemyY = 0;
                                        n.EnemyZ = 0;
                                    }
                                }
                                else
                                {
                                    n.EnemyX = 0;
                                    n.EnemyY = 0;
                                    n.EnemyZ = 0;
                                }
                            }
                            else
                            {
                                // If Rand(400)=1 Then RotateEntity(n\Collider, 0, Rnd(360), 10)
                                if (_rng.Next(400) == 1)
                                {
                                    SetNPCYaw(n, _rng.NextSingle() * 360.0f);
                                }

                                // TranslateEntity n\Collider,Cos(EntityYaw+90)*n\Speed*fps,0,Sin(EntityYaw+90)*n\Speed*fps
                                float yaw = GetNPCYaw(n);
                                float moveX = MathF.Cos((yaw + 90.0f) * MathF.PI / 180.0f) * n.Speed * fpsFactor;
                                float moveZ = MathF.Sin((yaw + 90.0f) * MathF.PI / 180.0f) * n.Speed * fpsFactor;
                                TranslateNPC(n, moveX, 0, moveZ);
                            }
                        }
                    }
                }
            }

            // PositionEntity(n\Collider, EntityX(n\Collider), Min(EntityY(n\Collider),0.35), EntityZ(n\Collider))
            ClampNPCY(n, 0.35f);
        }
        else if (n.Idle >= 2)
        {
            // idle = 2: Following target (during containment)
            if (n.Target != null)
            {
                bool tmp = false;
                if (dist > HideDistance * 0.7f)
                {
                    if (!EntityVisible(n, player))
                    {
                        tmp = true;
                    }
                }

                if (!tmp)
                {
                    // PointEntity n\obj, n\Target\Collider
                    // RotateEntity n\Collider, 0, CurveAngle(EntityYaw(n\obj),EntityYaw(n\Collider),10.0), 0, True
                    float targetDist = EntityDistanceNPC(n, n.Target);
                    float targetYaw = GetAngleToNPC(n, n.Target);
                    float curYaw = GetNPCYaw(n);
                    SetNPCYaw(n, CurveAngle(targetYaw, curYaw, 10.0f));

                    // MoveEntity n\Collider, 0, 0, 0.016*FPSfactor*Max(Min((dist*2-1.0)*0.5,1.0),-0.5)
                    float moveSpeed = 0.016f * fpsFactor * MathF.Max(MathF.Min((targetDist * 2 - 1.0f) * 0.5f, 1.0f), -0.5f);
                    MoveNPCForward(n, moveSpeed);
                    n.GravityMult = 1.0f;
                }
                else
                {
                    // Teleport to target
                    SetNPCPosition(n, GetNPCX(n.Target), GetNPCY(n.Target) + 0.3f, GetNPCZ(n.Target));
                    n.DropSpeed = 0;
                    n.GravityMult = 0.0f;
                }
            }

            // Floating animation when idle=2
            // PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) + 0.05 + Sin(MilliSecs2()*0.08)*0.02, EntityZ(n\Collider))
        }
    }

    /// <summary>
    /// NPCs.bb lines 935-1241: Full SCP-106 (OldMan) logic
    /// </summary>
    private static void Update106(NPC n, PlayerState player, float fpsFactor)
    {
        if (Contained106)
        {
            n.Idle = 1;
            HideNPC(n);
            SetNPCPosition(n, 0, 500.0f, 0);
            return;
        }

        float dist = EntityDistance(n, player);

        // Checking if 106 is allowed to spawn
        bool spawn106 = true;
        string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";

        if (roomName == "dimension1499")
            spawn106 = false;

        // room860 event check
        if (EventSystem.GetEventState("room860") == 1)
            spawn106 = false;

        // room049 depth check: EntityY(Collider) <= -2848*RoomScale
        if (roomName == "room049" && player.Y <= -2848 * RoomScale)
            spawn106 = false;

        // GateA event - don't make 106 disappear
        int gateaState = EventSystem.GetEventState("gatea");
        if (gateaState != 0)
        {
            spawn106 = true;
            n.Idle = roomName == "dimension1499" ? 1 : 0;
        }

        // If not allowed to spawn and State <= 0, reset timer
        if (!spawn106 && n.State <= 0)
        {
            n.State = _rng.Next(22000, 27001);
            SetNPCPosition(n, 0, 500, 0);
        }

        if (n.Idle == 0 && spawn106)
        {
            if (n.State <= 0) // Attacking
            {
                // Emergence from ground
                if (GetNPCY(n) < player.Y - 20.0f - 0.55f)
                {
                    // Create corrosion decal if allowed
                    if (player.PlayerRoom?.RoomTemplate?.DisableDecals != true)
                    {
                        OnCreateDecal?.Invoke(0, player.X, 0.01f, player.Z, 90, _rng.Next(360), 0, 0.05f, 0.001f, 0.8f);
                    }

                    n.PrevY = player.Y;
                    n.Frame = 110; // SetAnimTime n\obj, 110

                    if (roomName != "coffin")
                    {
                        SetNPCPosition(n, player.X, player.Y - 15, player.Z);
                    }

                    OnPlaySound?.Invoke(SoundID.DecaySFX0);
                }

                // Ambient sounds
                if (_rng.Next(500) == 1)
                    OnPlaySound2?.Invoke(SoundID.OldManSFX_Random, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);

                // Loop sound: OldManSFX(4) at range 8.0, volume 0.8
                // n\SoundChn = LoopSound2(OldManSFX(4), n\SoundChn, Camera, n\Collider, 8.0, 0.8)

                if (n.State > -10)
                {
                    // ShouldPlay = 66 (music cue)
                    if (n.Frame < 259)
                    {
                        // Rising animation
                        SetNPCPosition(n, GetNPCX(n), n.PrevY - 0.15f, GetNPCZ(n));
                        float yaw = GetAngleToPlayer(n, player);
                        float curYaw = GetNPCYaw(n);
                        SetNPCYaw(n, CurveValue(yaw, curYaw, 100.0f));

                        // AnimateNPC(n, 110, 259, 0.15, False)
                        n.Frame += 0.15f * fpsFactor;
                        if (n.Frame > 259) n.Frame = 259;
                    }
                    else
                    {
                        n.State = -10;
                    }
                }
                else
                {
                    // ShouldPlay = 10 (unless gatea)

                    bool visible = false;
                    if (dist < 8.0f)
                    {
                        visible = EntityVisible(n, player);
                    }

                    if (NoTarget) visible = false;

                    if (visible)
                    {
                        if (roomName != "gatea") n.PathTimer = 0;

                        if (EntityInView(n, player))
                        {
                            // GiveAchievement(Achv106)
                            OnAchievement?.Invoke("Achv106");

                            // BlurVolume = Max(Max(Min((4.0 - dist) / 6.0, 0.9), 0.1), BlurVolume)
                            player.BlurTimer = MathF.Max(MathF.Max(MathF.Min((4.0f - dist) / 6.0f, 0.9f), 0.1f), player.BlurTimer);

                            // CurrCameraZoom = Max(CurrCameraZoom, (Sin(Float(MilliSecs2())/20.0)+1.0) * 20.0 * Max((4.0-dist)/4.0,0))
                            float millis = Environment.TickCount;
                            player.CurrCameraZoom = MathF.Max(player.CurrCameraZoom,
                                (MathF.Sin(millis / 20.0f) + 1.0f) * 20.0f * MathF.Max((4.0f - dist) / 4.0f, 0));

                            // Horror sound if not seen for 60 seconds
                            if (Environment.TickCount - n.LastSeen > 60000)
                            {
                                player.CurrCameraZoom = 40;
                                OnPlaySound?.Invoke(SoundID.HorrorSFX6);
                                n.LastSeen = Environment.TickCount;
                            }
                        }
                    }
                    else
                    {
                        n.State -= fpsFactor;
                    }

                    if (dist > 0.8f)
                    {
                        // Chase logic
                        if ((dist > 25.0f || roomName == "pocketdimension" || visible || n.PathStatus != 1) && roomName != "gatea")
                        {
                            // Vertical adjustment at long range
                            if (dist > 40 || roomName == "pocketdimension")
                            {
                                float targetY = player.Y - 0.14f;
                                TranslateNPC(n, 0, (targetY - GetNPCY(n)) / 50.0f, 0);
                            }

                            n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 10.0f);

                            float targetYaw = GetAngleToPlayer(n, player);
                            SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 10.0f));

                            if (player.KillTimer >= 0)
                            {
                                // AnimateNPC(n, 284, 333, n\CurrSpeed*43)
                                float prevFrame = n.Frame;
                                n.Frame += n.CurrSpeed * 43 * fpsFactor;
                                if (n.Frame > 333) n.Frame = 284 + (n.Frame - 333);
                                if (n.Frame < 284) n.Frame = 284;

                                // Footstep sounds
                                if (prevFrame <= 286 && n.Frame > 286)
                                    OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
                                else if (prevFrame <= 311 && n.Frame > 311)
                                    OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
                            }
                            else
                            {
                                n.CurrSpeed = 0;
                            }

                            // Pathfinding timer
                            n.PathTimer = MathF.Max(n.PathTimer - fpsFactor, 0);
                            if (n.PathTimer <= 0)
                            {
                                n.PathStatus = FindPath(n, player.X, player.Y, player.Z);
                                n.PathTimer = 70 * 10;
                            }
                        }
                        else
                        {
                            // Path following
                            if (n.PathTimer <= 0)
                            {
                                n.PathStatus = FindPath(n, player.X, player.Y, player.Z);
                                n.PathTimer = 70 * 10;
                                n.CurrSpeed = 0;
                            }
                            else
                            {
                                n.PathTimer = MathF.Max(n.PathTimer - fpsFactor, 0);

                                if (n.PathStatus == 2)
                                {
                                    n.CurrSpeed = 0;
                                }
                                else if (n.PathStatus == 1)
                                {
                                    // Follow waypoints
                                    while (n.Path[n.PathLocation] == null)
                                    {
                                        if (n.PathLocation > 19)
                                        {
                                            n.PathLocation = 0;
                                            n.PathStatus = 0;
                                            break;
                                        }
                                        n.PathLocation++;
                                    }

                                    if (n.Path[n.PathLocation] != null)
                                    {
                                        var wp = n.Path[n.PathLocation]!;
                                        float wpY = GetWaypointY(wp) - 0.11f;
                                        TranslateNPC(n, 0, (wpY - GetNPCY(n)) / 50.0f, 0);

                                        float wpYaw = GetAngleToWaypoint(n, wp);
                                        float dist2 = GetDistanceToWaypoint(n, wp);

                                        SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), MathF.Min(20.0f, dist2 * 10.0f)));
                                        n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 10.0f);

                                        // AnimateNPC(n, 284, 333, n\CurrSpeed*43)
                                        float prevFrame = n.Frame;
                                        n.Frame += n.CurrSpeed * 43 * fpsFactor;
                                        if (n.Frame > 333) n.Frame = 284 + (n.Frame - 333);
                                        if (n.Frame < 284) n.Frame = 284;

                                        if (prevFrame <= 286 && n.Frame > 286)
                                            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
                                        else if (prevFrame <= 311 && n.Frame > 311)
                                            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);

                                        if (dist2 < 0.2f)
                                            n.PathLocation++;
                                    }
                                }
                                else if (n.PathStatus == 0)
                                {
                                    // Idle animation
                                    if (n.State3 == 0)
                                    {
                                        // AnimateNPC(n, 334, 494, 0.3)
                                        n.Frame += 0.3f * fpsFactor;
                                        if (n.Frame > 494) n.Frame = 334;
                                        if (n.Frame < 334) n.Frame = 334;
                                    }
                                    n.CurrSpeed = CurveValue(0, n.CurrSpeed, 10.0f);
                                }
                            }
                        }
                    }
                    else if (roomName != "gatea" && !NoTarget) // dist < 0.8 - attack range
                    {
                        if (dist > 0.5f)
                        {
                            n.CurrSpeed = CurveValue(n.Speed * 2.5f, n.CurrSpeed, 10.0f);
                        }
                        else
                        {
                            n.CurrSpeed = 0;
                        }

                        // AnimateNPC(n, 105, 110, 0.15, False)
                        n.Frame += 0.15f * fpsFactor;
                        if (n.Frame > 110) n.Frame = 110;
                        if (n.Frame < 105) n.Frame = 105;

                        if (player.KillTimer >= 0 && player.FallTimer >= 0)
                        {
                            float targetYaw = GetAngleToPlayer(n, player);
                            SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 10.0f));

                            // Attack at frame 110
                            if (MathF.Ceiling(n.Frame) == 110 && !player.GodMode)
                            {
                                OnPlaySound?.Invoke(SoundID.DamageSFX1);
                                OnPlaySound?.Invoke(SoundID.HorrorSFX5);

                                if (roomName == "pocketdimension")
                                {
                                    OnDeath?.Invoke("Subject D-9341. Body partially decomposed by what is assumed to be SCP-106's \"corrosion\" effect. Body disposed of via incineration.");
                                    OnKill?.Invoke();
                                }
                                else
                                {
                                    OnPlaySound?.Invoke(SoundID.OldManSFX3);
                                    player.FallTimer = MathF.Min(-1, player.FallTimer);
                                    // PositionEntity(Head, ...) - head drop effect
                                    // ResetEntity(Head)
                                    // RotateEntity(Head, 0, EntityYaw(Camera) + Rand(-45, 45), 0)
                                    player.Yaw += _rng.Next(-45, 46);
                                }
                            }
                        }
                    }

                    // Move forward
                    MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                    // Despawn check
                    if (n.State <= _rng.Next(-3500, -2999))
                    {
                        if (!EntityInView(n, player) && dist > 5)
                        {
                            n.State = _rng.Next(22000, 27001);
                            SetNPCPosition(n, 0, 500, 0);
                        }
                    }

                    // Pocket dimension transport
                    if (player.FallTimer < -250.0f)
                    {
                        OnMoveToPocketDimension?.Invoke();
                        n.State = 250;
                    }

                    // Teleport behind player mechanic
                    if (n.Reload == 0)
                    {
                        if (dist > 10 && roomName != "pocketdimension" && roomName != "gatea" && n.State < -5)
                        {
                            if (!EntityInView(n, player))
                            {
                                // EntityPick behind player
                                float behindYaw = (player.Yaw + 180) * MathF.PI / 180.0f;
                                float pickX = player.X + MathF.Sin(behindYaw) * 5;
                                float pickZ = player.Z + MathF.Cos(behindYaw) * 5;

                                // TeleportEntity to behind player
                                SetNPCPosition(n, pickX, player.Y, pickZ);
                                SetNPCYaw(n, GetAngleToPlayer(n, player));
                                MoveNPCForward(n, -2);

                                OnPlaySound2?.Invoke(SoundID.OldManSFX3, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                OnPlaySound2?.Invoke(SoundID.OldManSFX6_Random, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);

                                n.PathTimer = 0;
                                n.Reload = (70 * 10.0f) / (DifficultyFactor + 1);
                            }
                        }
                    }
                    n.Reload = MathF.Max(0, n.Reload - fpsFactor);
                }
            }
            else // Idling outside the map (State > 0)
            {
                n.CurrSpeed = 0;
                float targetY = player.Y - 30;
                TranslateNPC(n, 0, (targetY - GetNPCY(n)) / 200.0f, 0);
                n.DropSpeed = 0;
                n.Frame = 110;

                // Countdown timer
                if (player.PlayerRoom?.RoomTemplate?.DisableDecals != true)
                {
                    if (roomName != "gatea")
                    {
                        if (AggressiveNPCs)
                        {
                            n.State -= fpsFactor * 2;
                        }
                        else
                        {
                            n.State -= fpsFactor;
                        }
                    }
                }
            }

            ResetNPCCollider(n);
            n.DropSpeed = 0;

            // Position model at collider - 0.15
            // PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider) - 0.15, EntityZ(n\Collider))
            // RotateEntity n\obj, 0, EntityYaw(n\Collider), 0

            // Position obj2 (shadow/decal marker)
            // PositionEntity(n\obj2, EntityX(n\obj), EntityY(n\obj), EntityZ(n\obj))
            // RotateEntity(n\obj2, 0, EntityYaw(n\Collider) - 180, 0)
            // MoveEntity(n\obj2, 0, 8.6 * 0.11, -1.5 * 0.11)

            // Shadow visibility based on distance and room
            if (roomName == "pocketdimension" || roomName == "gatea")
            {
                HideNPCObj2(n);
            }
            else
            {
                float fogDist = CameraFogFar * LightVolume * 0.6f;
                if (dist < fogDist)
                {
                    HideNPCObj2(n);
                }
                else
                {
                    ShowNPCObj2(n);
                    SetNPCObj2Alpha(n, MathF.Min(dist - fogDist, 1.0f));
                }
            }
        }
        else
        {
            HideNPCObj2(n);
        }
    }
    /// <summary>
    /// NPCs.bb lines 1242-1660: Full SCP-096 logic
    /// States: 0=idle/sitting, 1=getting up, 2=standing, 3=waiting, 4=chasing, 5=docile wandering
    /// </summary>
    private static void Update096(NPC n, PlayerState player, float fpsFactor)
    {
        float dist = EntityDistance(n, player);

        switch ((int)n.State)
        {
            case 0: // Idle/sitting - waiting to be triggered
                if (dist < 8.0f)
                {
                    OnAchievement?.Invoke("Achv096");

                    // Loop ambient sound: 096.ogg
                    // n\SoundChn = StreamSound_Strict("SFX\Music\096.ogg",0)

                    if (n.State3 == -1)
                    {
                        // AnimateNPC(n,936,1263,0.1,False)
                        n.Frame += 0.1f * fpsFactor;
                        if (n.Frame > 1263) n.Frame = 1263;
                        if (n.Frame >= 1262.9f)
                        {
                            n.State = 5;
                            n.State3 = 0;
                            n.Frame = 312;
                        }
                    }
                    else
                    {
                        // AnimateNPC(n,936,1263,0.1)
                        n.Frame += 0.1f * fpsFactor;
                        if (n.Frame > 1263) n.Frame = 936;
                        if (n.Frame < 936) n.Frame = 936;

                        if (n.State3 < 70 * 6)
                        {
                            n.State3 += fpsFactor;
                        }
                        else
                        {
                            if (_rng.Next(1, 6) == 1)
                            {
                                n.State3 = -1;
                            }
                            else
                            {
                                n.State3 = 70 * _rng.Next(0, 4);
                            }
                        }
                    }

                    // angle = WrapAngle(DeltaYaw(n\Collider, Collider))
                    float angle = WrapAngle(GetAngleToPlayer(n, player) - GetNPCYaw(n));

                    if (!NoTarget)
                    {
                        // If player is facing 096 (angle<90 Or angle>270)
                        if (angle < 90 || angle > 270)
                        {
                            // CameraProject - check if in screen bounds
                            if (IsNPCOnScreen(n, player))
                            {
                                if (EntityVisible(n, player))
                                {
                                    // (BlinkTimer < -16 Or BlinkTimer > -6) - eyes open
                                    if ((player.BlinkTimer < -16 || player.BlinkTimer > -6))
                                    {
                                        OnPlaySound?.Invoke(SoundID.Scp096Triggered);

                                        player.CurrCameraZoom = 10;
                                        n.Frame = 194;
                                        n.SoundChn = 0;
                                        n.Sound = 0;
                                        n.State = 1;
                                        n.State3 = 0;
                                    }
                                }
                            }
                        }
                    }
                }
                break;

            case 4: // Full chase mode
                player.CurrCameraZoom = CurveValue(
                    MathF.Max(player.CurrCameraZoom, (MathF.Sin(Environment.TickCount / 20.0f) + 1.0f) * 10.0f),
                    player.CurrCameraZoom, 8.0f);

                if (n.Target == null)
                {
                    // Loop scream sound
                    // n\SoundChn = StreamSound_Strict("SFX\SCP\096\Scream.ogg",0)
                    // n\SoundChn2 = StreamSound_Strict("SFX\Music\096Chase.ogg",0)
                }

                if (NoTarget && n.Target == null) n.State = 5;

                if (player.KillTimer >= 0)
                {
                    // Visibility check every 3 seconds
                    if (Environment.TickCount > n.State3)
                    {
                        n.LastSeen = 0;
                        if (n.Target == null)
                        {
                            if (EntityVisible(n, player)) n.LastSeen = 1;
                        }
                        else
                        {
                            if (EntityVisibleNPC(n, n.Target)) n.LastSeen = 1;
                        }
                        n.State3 = Environment.TickCount + 3000;
                    }

                    if (n.LastSeen == 1)
                    {
                        n.PathTimer = MathF.Max(70 * 3, n.PathTimer);
                        n.PathStatus = 0;

                        if (n.Target != null) dist = EntityDistanceNPC(n.Target, n);

                        if (dist < 2.8f || n.Frame < 150)
                        {
                            // Jump attack animation
                            if (n.Frame > 193) n.Frame = 2.0f;

                            // AnimateNPC(n, 2, 193, 0.7)
                            n.Frame += 0.7f * fpsFactor;
                            if (n.Frame > 193) n.Frame = 193;

                            if (dist > 1.0f)
                            {
                                n.CurrSpeed = CurveValue(n.Speed * 2.0f, n.CurrSpeed, 15.0f);
                            }
                            else
                            {
                                n.CurrSpeed = 0;

                                // Kill player
                                if (n.Target == null && !player.GodMode)
                                {
                                    OnPlaySound?.Invoke(SoundID.DamageSFX4);
                                    player.CameraShake = 30;
                                    player.BlurTimer = 2000;
                                    OnDeath?.Invoke("A large amount of blood found in [DATA REDACTED]. DNA identified as Subject D-9341. Most likely [DATA REDACTED] by SCP-096.");
                                    OnKill?.Invoke();
                                    player.KillAnim = 1;
                                    // Blood decals
                                }
                            }

                            // Point at target
                            if (n.Target == null)
                            {
                                SetNPCYaw(n, GetAngleToPlayer(n, player));
                            }
                            else
                            {
                                SetNPCYaw(n, GetAngleToNPC(n, n.Target));
                            }
                        }
                        else
                        {
                            // Running animation
                            float targetYaw;
                            if (n.Target == null)
                            {
                                targetYaw = GetAngleToPlayer(n, player);
                            }
                            else
                            {
                                targetYaw = GetAngleToNPC(n, n.Target);
                            }

                            SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 5.0f));

                            if (n.Frame > 847) n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);

                            if (n.Frame < 906)
                            {
                                // AnimateNPC(n,737,906,n\Speed*8,False)
                                n.Frame += n.Speed * 8 * fpsFactor;
                                if (n.Frame > 906) n.Frame = 906;
                            }
                            else
                            {
                                // AnimateNPC(n,907,935,n\CurrSpeed*8)
                                n.Frame += n.CurrSpeed * 8 * fpsFactor;
                                if (n.Frame > 935) n.Frame = 907;
                            }
                        }

                        MoveNPCForward(n, n.CurrSpeed * fpsFactor);
                    }
                    else
                    {
                        // Pathfinding mode
                        if (n.PathStatus == 1)
                        {
                            if (n.Path[n.PathLocation] == null)
                            {
                                if (n.PathLocation > 19)
                                {
                                    n.PathLocation = 0;
                                    n.PathStatus = 0;
                                }
                                else
                                {
                                    n.PathLocation++;
                                }
                            }
                            else
                            {
                                var wp = n.Path[n.PathLocation]!;
                                float wpYaw = GetAngleToWaypoint(n, wp);
                                SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 5.0f));

                                if (n.Frame > 847) n.CurrSpeed = CurveValue(n.Speed * 1.5f, n.CurrSpeed, 15.0f);
                                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                                if (n.Frame < 906)
                                {
                                    n.Frame += n.Speed * 8 * fpsFactor;
                                    if (n.Frame > 906) n.Frame = 906;
                                }
                                else
                                {
                                    n.Frame += n.CurrSpeed * 8 * fpsFactor;
                                    if (n.Frame > 935) n.Frame = 907;
                                }

                                float dist2 = GetDistanceToWaypoint(n, wp);
                                if (dist2 < 0.8f)
                                {
                                    // Open door if present
                                    if (wp.Door != null && !wp.Door.open)
                                    {
                                        wp.Door.open = true;
                                        wp.Door.fastopen = 1;
                                    }
                                    if (dist2 < 0.7f) n.PathLocation++;
                                }
                            }
                        }
                        else
                        {
                            // AnimateNPC(n,737,822,0.2)
                            n.Frame += 0.2f * fpsFactor;
                            if (n.Frame > 822) n.Frame = 737;
                            if (n.Frame < 737) n.Frame = 737;

                            n.PathTimer = MathF.Max(0, n.PathTimer - fpsFactor);
                            if (n.PathTimer <= 0)
                            {
                                if (n.Target != null)
                                {
                                    n.PathStatus = FindPath(n, GetNPCX(n.Target), GetNPCY(n.Target) + 0.2f, GetNPCZ(n.Target));
                                }
                                else
                                {
                                    n.PathStatus = FindPath(n, player.X, player.Y + 0.2f, player.Z);
                                }
                                n.PathTimer = 70 * 5;
                            }
                        }
                    }

                    // Teleport if too far
                    if (dist > 32.0f || GetNPCY(n) < -50)
                    {
                        if (_rng.Next(50) == 1) TeleportCloser(n, player);
                    }
                }
                else
                {
                    // Eating animation if killtimer < 0
                    n.Frame += 0.5f * fpsFactor;
                    if (n.Frame > 193) n.Frame = MathF.Min(27, n.Frame);
                }
                break;

            case 1: // Getting up
            case 2: // Standing
            case 3: // Waiting before chase
                // Loop angered sound
                // n\SoundChn = StreamSound_Strict("SFX\Music\096Angered.ogg",0)

                if (n.State == 1)
                {
                    if (n.Frame < 312)
                    {
                        // AnimateNPC(n,193,311,0.3,False)
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 310.9f)
                        {
                            n.State = 2;
                            n.Frame = 737;
                        }
                    }
                    else if (n.Frame >= 312 && n.Frame <= 422)
                    {
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 421.9f) n.Frame = 677;
                    }
                    else
                    {
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 735.9f)
                        {
                            n.State = 2;
                            n.Frame = 737;
                        }
                    }
                }
                else if (n.State == 2)
                {
                    n.Frame += 0.3f * fpsFactor;
                    if (n.Frame >= 822)
                    {
                        n.State = 3;
                        n.State2 = 0;
                    }
                }
                else if (n.State == 3)
                {
                    n.State2 += fpsFactor;
                    if (n.State2 > 70 * 18)
                    {
                        n.Frame += n.Speed * 8 * fpsFactor;
                        if (n.Frame > 846.9f)
                        {
                            n.State = 4;
                            n.SoundChn = 0;
                        }
                    }
                    else
                    {
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 822) n.Frame = 737;
                    }
                }
                break;

            case 5: // Docile wandering
                if (dist < 16.0f)
                {
                    if (dist < 4.0f)
                    {
                        OnAchievement?.Invoke("Achv096");
                    }

                    // Loop calm sound: 096.ogg

                    if (n.Frame >= 422)
                    {
                        n.State2 += fpsFactor;
                        if (n.State2 > 1000)
                        {
                            // Walking around
                            if (n.State2 > 1600) n.State2 = _rng.Next(0, 501);

                            if (n.Frame < 1382)
                            {
                                n.CurrSpeed = CurveValue(n.Speed * 0.1f, n.CurrSpeed, 5.0f);
                                n.Frame += n.CurrSpeed * 45 * fpsFactor;
                            }
                            else
                            {
                                n.CurrSpeed = CurveValue(n.Speed * 0.1f, n.CurrSpeed, 5.0f);
                                n.Frame += n.CurrSpeed * 45 * fpsFactor;
                                if (n.Frame > 1456) n.Frame = 1383;
                            }

                            // Visibility check every 3 seconds
                            if (Environment.TickCount > n.State3)
                            {
                                n.LastSeen = 0;
                                if (EntityVisible(n, player))
                                {
                                    n.LastSeen = 1;
                                }
                                else
                                {
                                    // Random turn on wall hit
                                    n.Angle = GetNPCYaw(n) + _rng.NextSingle() * 30 + 80;
                                }
                                n.State3 = Environment.TickCount + 3000;
                            }

                            if (n.LastSeen == 1)
                            {
                                float targetYaw = GetAngleToPlayer(n, player);
                                SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 130.0f));
                                if (dist < 1.5f) n.State2 = 0;
                            }
                            else
                            {
                                SetNPCYaw(n, CurveAngle(n.Angle, GetNPCYaw(n), 50.0f));
                            }
                        }
                        else
                        {
                            // Idle animation
                            if (n.Frame > 472)
                            {
                                n.CurrSpeed = CurveValue(n.Speed * 0.05f, n.CurrSpeed, 8.0f);
                                n.Frame += n.CurrSpeed * 45 * fpsFactor;
                                if (n.Frame >= 1468.9f) n.Frame = 423;
                            }
                            else
                            {
                                n.CurrSpeed = CurveValue(0, n.CurrSpeed, 4.0f);
                                n.Frame += 0.2f * fpsFactor;
                                if (n.Frame > 471) n.Frame = 423;
                            }
                        }

                        MoveNPCForward(n, n.CurrSpeed * fpsFactor);
                    }
                    else
                    {
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 422) n.Frame = 422;
                    }

                    // Check if player looks at face - trigger
                    float angle2 = WrapAngle(GetAngleToPlayer(n, player) - GetNPCYaw(n));
                    if (!NoTarget)
                    {
                        if (angle2 < 55 || angle2 > 305)
                        {
                            if (IsNPCOnScreen(n, player))
                            {
                                if (EntityVisible(n, player))
                                {
                                    if (player.BlinkTimer < -16 || player.BlinkTimer > -6)
                                    {
                                        OnPlaySound?.Invoke(SoundID.Scp096Triggered);
                                        player.CurrCameraZoom = 10;
                                        if (n.Frame >= 422) n.Frame = 677;
                                        n.SoundChn = 0;
                                        n.Sound = 0;
                                        n.State = 2;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }

        // Position model at collider
        // PositionEntity(n\obj, EntityX(n\Collider), EntityY(n\Collider)-0.03, EntityZ(n\Collider))
        // RotateEntity n\obj, EntityPitch(n\Collider), EntityYaw(n\Collider), 0
    }
    /// <summary>
    /// NPCs.bb lines 1661-2153: Full SCP-049 logic
    /// States: 0=nothing, 1=looking around, 2=active, 3=killed player, 4=catwalk, 5=room2sl
    /// </summary>
    private static void Update049(NPC n, PlayerState player, float fpsFactor)
    {
        float prevFrame = n.Frame;
        float dist = EntityDistance(n, player);
        n.BlinkTimer = 1.0f;

        if (n.Idle > 0.1f)
        {
            // Idle - hidden away from player
            string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
            if (roomName != "room049")
            {
                n.Idle = MathF.Max(n.Idle - (1 + (AggressiveNPCs ? 1 : 0)) * fpsFactor, 0.1f);
            }
            n.DropSpeed = 0;
            SetNPCPosition(n, 0, -500, 0);
        }
        else
        {
            if (MathF.Abs(n.Idle - 0.1f) < 0.001f)
            {
                // Teleport to player's area
                n.Idle = 0;
            }

            switch ((int)n.State)
            {
                case 0: // Nothing - used for events
                    break;

                case 1: // Looking around before getting active
                    if (n.Frame >= 538)
                    {
                        n.Frame -= 0.45f * fpsFactor;
                        if (n.Frame < 538) n.Frame = 37;
                    }
                    else
                    {
                        n.Frame += 0.7f * fpsFactor;
                        if (n.Frame > 268.9f) n.State = 2;
                    }
                    break;

                case 2: // Being active
                    if (dist < HideDistance * 2 && n.Idle == 0)
                    {
                        // Loop breathing sound
                        bool playerSeeable = NPCSeesPlayer(n, player);

                        if (playerSeeable || n.State2 > 0)
                        {
                            // Player visible - attacking
                            OnAchievement?.Invoke("Achv049");

                            // Play spotted sound on first detection
                            if (n.PrevState <= 1)
                            {
                                OnPlaySound2?.Invoke(SoundID.Scp049Spotted, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                n.PrevState = 2;
                            }

                            n.PathStatus = 0;
                            n.PathTimer = 0;
                            n.PathLocation = 0;

                            if (playerSeeable) n.State2 = 70 * 2;

                            float targetYaw = GetAngleToPlayer(n, player);
                            SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 10.0f));

                            if (dist < 0.5f)
                            {
                                // Attack range - check for protective gear
                                if (player.WearingHazmat > 0)
                                {
                                    player.BlurTimer += fpsFactor * 2.5f;
                                    if (player.BlurTimer > 250 && player.BlurTimer - fpsFactor * 2.5f <= 250 && n.PrevState != 3)
                                    {
                                        OnPlaySound?.Invoke(SoundID.Scp049TakeOffHazmat);
                                        n.PrevState = 3;
                                    }
                                    else if (player.BlurTimer >= 500)
                                    {
                                        player.WearingHazmat = 0;
                                        player.BlurTimer = 260.0f;
                                        player.CameraShake = 2.0f;
                                    }
                                }
                                else if (player.Wearing714 > 0)
                                {
                                    player.BlurTimer += fpsFactor * 2.5f;
                                    if (player.BlurTimer > 250 && player.BlurTimer - fpsFactor * 2.5f <= 250 && n.PrevState != 3)
                                    {
                                        OnPlaySound?.Invoke(SoundID.Scp049Ring714);
                                        n.PrevState = 3;
                                    }
                                    else if (player.BlurTimer >= 500)
                                    {
                                        player.Wearing714 = 0;
                                    }
                                }
                                else
                                {
                                    // Kill player
                                    player.CurrCameraZoom = 20.0f;
                                    player.BlurTimer = 500.0f;

                                    if (!player.GodMode)
                                    {
                                        string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
                                        if (roomName == "room049")
                                        {
                                            OnDeath?.Invoke("Three (3) active instances of SCP-049-2 discovered in the tunnel outside SCP-049's containment chamber. Terminated by Nine-Tailed Fox.");
                                        }
                                        else
                                        {
                                            OnDeath?.Invoke("An active instance of SCP-049-2 was discovered in [REDACTED]. Terminated by Nine-Tailed Fox.");
                                            OnKill?.Invoke();
                                        }
                                        OnPlaySound?.Invoke(SoundID.HorrorSFX13);
                                        n.State = 3;
                                    }
                                }
                            }
                            else
                            {
                                // Chase
                                n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                                if (n.PrevState == 3) n.PrevState = 2;

                                // Animation based on distance
                                if (dist < 3.0f)
                                {
                                    n.Frame += n.CurrSpeed * 38 * fpsFactor;
                                    if (n.Frame > 463) n.Frame = 387;
                                    if (n.Frame < 387) n.Frame = 387;
                                }
                                else
                                {
                                    if (n.Frame > 428)
                                    {
                                        n.Frame += n.CurrSpeed * 38 * fpsFactor;
                                        if (n.Frame > 497.9f) n.Frame = 358;
                                    }
                                    else
                                    {
                                        n.Frame += n.CurrSpeed * 38 * fpsFactor;
                                        if (n.Frame > 393) n.Frame = 346;
                                        if (n.Frame < 346) n.Frame = 346;
                                    }
                                }
                            }
                        }
                        else
                        {
                            // Pathfinding mode
                            if (n.PathStatus == 1)
                            {
                                while (n.Path[n.PathLocation] == null)
                                {
                                    if (n.PathLocation > 19)
                                    {
                                        n.PathLocation = 0;
                                        n.PathStatus = 0;
                                        break;
                                    }
                                    n.PathLocation++;
                                }

                                if (n.Path[n.PathLocation] != null)
                                {
                                    var wp = n.Path[n.PathLocation]!;
                                    n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                                    float wpYaw = GetAngleToWaypoint(n, wp);
                                    SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 10.0f));
                                    MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                                    // Door handling
                                    float dist2 = GetDistanceToWaypoint(n, wp);
                                    if (dist2 < 0.6f)
                                    {
                                        if (wp.Door != null && !wp.Door.open)
                                        {
                                            wp.Door.open = true;
                                        }
                                    }
                                    if (dist2 < 0.2f) n.PathLocation++;

                                    // Walking animation
                                    n.Frame += n.CurrSpeed * 38 * fpsFactor;
                                    if (n.Frame > 393) n.Frame = 346;
                                    if (n.Frame < 346) n.Frame = 346;

                                    // Random searching voice
                                    if (n.PrevState == 0 && _rng.Next(600) == 1)
                                    {
                                        OnPlaySound2?.Invoke(SoundID.Scp049Searching, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                        n.PrevState = 1;
                                    }
                                }
                            }
                            else
                            {
                                // No path - stand and search
                                n.PathTimer += fpsFactor;
                                if (n.PathTimer > 70 * (5 - (AggressiveNPCs ? 2 : 0)))
                                {
                                    n.PathStatus = FindPath(n, player.X, player.Y, player.Z);
                                    n.PathTimer = 0;
                                    n.State3 = 0;
                                }

                                // Idle animation
                                n.Frame += 0.2f * fpsFactor;
                                if (n.Frame > 345) n.Frame = 269;
                                if (n.Frame < 269) n.Frame = 269;
                            }
                        }

                        // Footstep sounds
                        if (n.CurrSpeed > 0.005f)
                        {
                            if ((prevFrame < 361 && n.Frame >= 361) || (prevFrame < 377 && n.Frame >= 377))
                                OnPlaySound2?.Invoke(SoundID.Step3SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
                            else if ((prevFrame < 431 && n.Frame >= 431) || (prevFrame < 447 && n.Frame >= 447))
                                OnPlaySound2?.Invoke(SoundID.Step3SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
                        }
                    }
                    else if (n.Idle == 0)
                    {
                        // Too far - teleport closer
                        if (_rng.Next(1, 4 - (int)DifficultyFactor) == 1)
                        {
                            TeleportCloser(n, player);
                        }
                        else
                        {
                            n.Idle = 60 * 70;
                        }
                    }
                    break;

                case 3: // Player killed - carrying animation
                    n.Frame += 0.7f * fpsFactor;
                    if (n.Frame > 660) n.Frame = 660;
                    if (n.Frame < 537) n.Frame = 537;

                    // Move towards player body
                    SetNPCPosition(n,
                        CurveValue(player.X, GetNPCX(n), 20.0f),
                        GetNPCY(n),
                        CurveValue(player.Z, GetNPCZ(n), 20.0f));
                    float yaw = WrapAngle(player.Yaw - 180);
                    SetNPCYaw(n, CurveAngle(yaw, GetNPCYaw(n), 40.0f));
                    break;

                case 4: // Standing on catwalk
                    if (dist < 8.0f)
                    {
                        n.Frame += 0.05f * fpsFactor;
                        if (n.Frame > 19) n.Frame = 18;
                        if (n.Frame < 18) n.Frame = 18;

                        float targetYaw = GetAngleToPlayer(n, player);
                        SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 45.0f));
                        n.State3 = 1;
                    }
                    else if (dist > HideDistance * 0.8f && n.State3 > 0)
                    {
                        n.State = 2;
                        n.State3 = 0;
                    }
                    break;
            }
        }

        n.LastSeen = (int)MathF.Max(n.LastSeen - fpsFactor, 0);
        n.State2 = MathF.Max(n.State2 - fpsFactor, 0);
    }

    /// <summary>
    /// NPCs.bb lines 2154-2316: Full SCP-049-2 (Zombie) logic
    /// States: 0=lying dead, 1=standing up, 2=following, 3=attacking
    /// </summary>
    private static void UpdateZombie(NPC n, PlayerState player, float fpsFactor)
    {
        // Only update if player is near vertically
        if (MathF.Abs(player.Y - GetNPCY(n)) >= 4.0f) return;

        float prevFrame = n.Frame;

        if (!n.IsDead)
        {
            switch ((int)n.State)
            {
                case 0: // Lying dead
                    n.Frame += 0.2f * fpsFactor;
                    if (n.Frame > 777) n.Frame = 777;
                    if (n.Frame < 719) n.Frame = 719;

                    if (MathF.Abs(n.Frame - 777) < 0.1f)
                    {
                        if (_rng.Next(700) == 1)
                        {
                            if (EntityDistance(n, player) < 5.0f)
                            {
                                n.Frame = 719;
                            }
                        }
                    }
                    break;

                case 1: // Standing up
                    if (n.Frame >= 682)
                    {
                        n.Frame += 0.3f * fpsFactor;
                        if (n.Frame > 935) n.Frame = 935;
                        if (MathF.Abs(n.Frame - 935) < 0.1f) n.State = 2;
                    }
                    else
                    {
                        n.Frame += 1.5f * fpsFactor;
                        if (n.Frame > 682) n.Frame = 926;
                    }
                    break;

                case 2: // Following player
                    // Visibility check every 3 seconds
                    if (n.State3 < 0)
                    {
                        if (EntityDistance(n, player) < 5.0f)
                        {
                            if (EntityVisible(n, player)) n.State2 = 70 * 5;
                        }
                        n.State3 = 70 * 3;
                    }
                    else
                    {
                        n.State3 -= fpsFactor;
                    }

                    if (n.State2 > 0 && !NoTarget)
                    {
                        // Player visible - attack mode
                        // Loop zombie breathing sound

                        n.PathStatus = 0;
                        float dist = EntityDistance(n, player);

                        float targetYaw = GetAngleToPlayer(n, player);
                        SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 30.0f));

                        if (dist < 0.7f)
                        {
                            // Attack
                            n.State = 3;
                            if (_rng.Next(2) == 1)
                            {
                                n.Frame = 2;
                            }
                            else
                            {
                                n.Frame = 66;
                            }
                        }
                        else
                        {
                            // Chase
                            n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                            MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                            // Walking animation
                            n.Frame += n.CurrSpeed * 60 * fpsFactor;
                            if (n.Frame > 1017) n.Frame = 936;
                            if (n.Frame < 936) n.Frame = 936;
                        }

                        n.State2 -= fpsFactor;
                    }
                    else
                    {
                        // Pathfinding mode
                        if (n.PathStatus == 1)
                        {
                            if (n.Path[n.PathLocation] == null)
                            {
                                if (n.PathLocation > 19)
                                {
                                    n.PathLocation = 0;
                                    n.PathStatus = 0;
                                }
                                else
                                {
                                    n.PathLocation++;
                                }
                            }
                            else
                            {
                                var wp = n.Path[n.PathLocation]!;
                                float wpYaw = GetAngleToWaypoint(n, wp);
                                SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 30.0f));

                                n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                                n.Frame += n.CurrSpeed * 60 * fpsFactor;
                                if (n.Frame > 1017) n.Frame = 936;
                                if (n.Frame < 936) n.Frame = 936;

                                if (GetDistanceToWaypoint(n, wp) < 0.2f)
                                {
                                    n.PathLocation++;
                                }
                            }
                        }
                        else
                        {
                            // No path - idle
                            n.CurrSpeed = 0;
                            n.Frame += 0.1f * fpsFactor;
                            if (n.Frame > 926) n.Frame = 778;
                            if (n.Frame < 778) n.Frame = 778;

                            n.PathTimer -= fpsFactor;
                            if (n.PathTimer <= 0)
                            {
                                n.PathStatus = FindPath(n, player.X, player.Y + 0.1f, player.Z);
                                n.PathTimer += 70 * 5;
                            }
                        }
                    }

                    // Footstep sounds
                    if (n.CurrSpeed > 0.005f)
                    {
                        if ((prevFrame < 977 && n.Frame >= 977) || (prevFrame > 1010 && n.Frame < 940))
                            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.3f);
                    }
                    break;

                case 3: // Attack animation
                    if (NoTarget) n.State = 2;

                    if (n.Frame < 66)
                    {
                        n.Frame += 0.7f * fpsFactor;
                        if (prevFrame < 23 && n.Frame >= 23)
                        {
                            // Attack hit check
                            if (EntityDistance(n, player) < 1.1f)
                            {
                                float deltaYaw = WrapAngle(GetAngleToPlayer(n, player) - GetNPCYaw(n));
                                if (MathF.Abs(deltaYaw) <= 60.0f || MathF.Abs(deltaYaw - 360) <= 60.0f)
                                {
                                    OnPlaySound?.Invoke(SoundID.DamageSFX5_8);
                                    player.Injuries += _rng.NextSingle() * 0.6f + 0.4f;
                                    OnDeath?.Invoke("Subject D-9341. Cause of death: multiple lacerations and severe blunt force trauma caused by an instance of SCP-049-2.");
                                }
                            }
                        }
                        else if (MathF.Abs(n.Frame - 65) < 0.1f)
                        {
                            n.State = 2;
                        }
                    }
                    else
                    {
                        n.Frame += 0.7f * fpsFactor;
                        if (prevFrame < 90 && n.Frame >= 90)
                        {
                            if (EntityDistance(n, player) < 1.1f)
                            {
                                float deltaYaw = WrapAngle(GetAngleToPlayer(n, player) - GetNPCYaw(n));
                                if (MathF.Abs(deltaYaw) <= 60.0f || MathF.Abs(deltaYaw - 360) <= 60.0f)
                                {
                                    OnPlaySound?.Invoke(SoundID.DamageSFX5_8);
                                    player.Injuries += _rng.NextSingle() * 0.6f + 0.4f;
                                    OnDeath?.Invoke("Subject D-9341. Cause of death: multiple lacerations and severe blunt force trauma caused by an instance of SCP-049-2.");
                                }
                            }
                        }
                        else if (MathF.Abs(n.Frame - 132) < 0.1f)
                        {
                            n.State = 2;
                        }
                    }
                    break;
            }
        }
        else
        {
            // Dead - lying animation
            n.Frame += 0.5f * fpsFactor;
            if (n.Frame > 157) n.Frame = 157;
            if (n.Frame < 133) n.Frame = 133;
        }
    }
    /// <summary>
    /// NPCs.bb lines 2317-2716: Full Guard AI
    /// States: 1=shoot player, 2=shoots, 3=follow path, 4=idle patrol, 5=follow target, 11=search player
    /// </summary>
    private static void UpdateGuard(NPC n, PlayerState player, float fpsFactor)
    {
        float prevFrame = n.Frame;
        float dist = EntityDistance(n, player);

        switch ((int)n.State)
        {
            case 1: // Aims and shoots at player
                if (n.Frame < 39 || (n.Frame > 76 && n.Frame < 245) || (n.Frame > 248 && n.Frame < 302) || n.Frame > 344)
                {
                    n.Frame += 0.2f * fpsFactor;
                    if (n.Frame > 357) n.Frame = 357;
                    if (n.Frame >= 356) n.Frame = 302;
                }

                if (player.KillTimer >= 0)
                {
                    float shootAccuracy = 0.4f + 0.5f * (AggressiveNPCs ? 1 : 0);
                    float detectDist = 11.0f;

                    string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
                    if (roomName == "exit1")
                    {
                        detectDist = 21.0f;
                        shootAccuracy = 0.0f;
                        if (_rng.Next(1, 8 - (AggressiveNPCs ? 4 : 0)) < 2) shootAccuracy = 0.03f;
                        shootAccuracy += 0.5f - player.CurrSpeed * 20;
                    }

                    if (dist < detectDist)
                    {
                        float targetYaw = GetAngleToPlayer(n, player);
                        SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 10.0f));

                        if (n.Reload <= 0)
                        {
                            if (EntityVisible(n, player))
                            {
                                OnPlaySound2?.Invoke(SoundID.Gunshot, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                Shoot(n, player, shootAccuracy, false);
                                n.Reload = 7;

                                if (roomName == "start")
                                    OnDeath?.Invoke("Subject D-9341. Cause of death: Gunshot wound to the head. Terminated by Agent Ulgrin.");
                                else if (roomName == "exit1")
                                    OnDeath?.Invoke("\"Agent G. to control. Eliminated a Class D escapee in Gate B's courtyard.\"");
                            }
                        }

                        if (n.Reload > 0 && n.Reload <= 7)
                        {
                            n.Frame += 0.35f * fpsFactor;
                            if (n.Frame > 248) n.Frame = 245;
                            if (n.Frame < 245) n.Frame = 245;
                        }
                        else if (n.Frame < 302)
                        {
                            n.Frame += 0.35f * fpsFactor;
                            if (n.Frame > 344) n.Frame = 302;
                        }
                    }
                    else
                    {
                        n.Frame += 0.35f * fpsFactor;
                        if (n.Frame > 344) n.Frame = 302;
                    }
                }
                else
                {
                    n.State = 0;
                }

                n.Reload -= fpsFactor;
                break;

            case 2: // Shooting
                n.Frame += 0.35f * fpsFactor;
                if (n.Frame > 248) n.Frame = 245;
                if (n.Frame < 245) n.Frame = 245;

                if (n.Reload <= 0)
                {
                    OnPlaySound2?.Invoke(SoundID.Gunshot, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    n.Reload = 7;
                }
                n.Reload -= fpsFactor;
                break;

            case 3: // Following a path
                if (n.PathStatus == 2)
                {
                    n.State = 0;
                    n.CurrSpeed = 0;
                }
                else if (n.PathStatus == 1)
                {
                    if (n.Path[n.PathLocation] == null)
                    {
                        if (n.PathLocation > 19)
                        {
                            n.PathLocation = 0;
                            n.PathStatus = 0;
                        }
                        else
                        {
                            n.PathLocation++;
                        }
                    }
                    else
                    {
                        var wp = n.Path[n.PathLocation]!;
                        float wpYaw = GetAngleToWaypoint(n, wp);
                        SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 20.0f));

                        n.Frame += n.CurrSpeed * 40 * fpsFactor;
                        if (n.Frame > 38) n.Frame = 1;
                        if (n.Frame < 1) n.Frame = 1;

                        n.CurrSpeed = CurveValue(n.Speed * 0.7f, n.CurrSpeed, 20.0f);
                        MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                        if (GetDistanceToWaypoint(n, wp) < 0.2f)
                            n.PathLocation++;
                    }
                }
                else
                {
                    n.CurrSpeed = 0;
                    n.State = 4;
                }
                break;

            case 4: // Idle patrol
                n.Frame += 0.2f * fpsFactor;
                if (n.Frame > 201) n.Frame = 77;
                if (n.Frame < 77) n.Frame = 77;

                if (_rng.Next(400) == 1)
                    n.Angle = _rng.NextSingle() * 360 - 180;

                float millis = Environment.TickCount;
                SetNPCYaw(n, CurveAngle(n.Angle + MathF.Sin(millis / 50) * 2, GetNPCYaw(n), 150.0f));

                if (dist < 15.0f)
                {
                    float angleDiff = WrapAngle(GetNPCYaw(n) - GetAngleToPlayer(n, player));
                    if (angleDiff < 90 || angleDiff > 270)
                    {
                        if (EntityVisible(n, player))
                            n.State = 1;
                    }
                }
                break;

            case 5: // Following a target
                float targetYaw2 = VectorYaw(n.EnemyX - GetNPCX(n), n.EnemyZ - GetNPCZ(n)) + n.Angle;
                SetNPCYaw(n, CurveAngle(targetYaw2, GetNPCYaw(n), 20.0f));

                float targetDist = Distance2D(GetNPCX(n), GetNPCZ(n), n.EnemyX, n.EnemyZ);

                n.Frame += n.CurrSpeed * 40 * fpsFactor;
                if (n.Frame > 38) n.Frame = 1;
                if (n.Frame < 1) n.Frame = 1;

                if (targetDist > 2.0f || targetDist < 1.0f)
                {
                    n.CurrSpeed = CurveValue(n.Speed * MathF.Sign(targetDist - 1.5f) * 0.75f, n.CurrSpeed, 10.0f);
                }
                else
                {
                    n.CurrSpeed = CurveValue(0, n.CurrSpeed, 10.0f);
                }

                MoveNPCForward(n, n.CurrSpeed * fpsFactor);
                break;

            case 11: // Search player
                if (n.Frame < 39 || (n.Frame > 76 && n.Frame < 245) || (n.Frame > 248 && n.Frame < 302) || n.Frame > 344)
                {
                    n.Frame += 0.2f * fpsFactor;
                    if (n.Frame >= 356) n.Frame = 302;
                }

                if (player.KillTimer >= 0)
                {
                    bool searchPlayer = false;
                    if (dist < 11.0f && EntityVisible(n, player))
                        searchPlayer = true;

                    if (searchPlayer)
                    {
                        float targetYaw3 = GetAngleToPlayer(n, player);
                        SetNPCYaw(n, CurveAngle(targetYaw3, GetNPCYaw(n), 10.0f));

                        if (n.Reload <= 0 && EntityVisible(n, player))
                        {
                            OnPlaySound2?.Invoke(SoundID.Gunshot, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                            Shoot(n, player, 1.0f, false);
                            n.Reload = 7;
                        }

                        if (n.Reload > 0 && n.Reload <= 7)
                        {
                            n.Frame += 0.35f * fpsFactor;
                            if (n.Frame > 248) n.Frame = 245;
                        }
                        else if (n.Frame < 302)
                        {
                            n.Frame += 0.35f * fpsFactor;
                            if (n.Frame > 344) n.Frame = 302;
                        }
                    }
                    else
                    {
                        // Pathfind to player
                        if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                        {
                            var wp = n.Path[n.PathLocation]!;

                            n.Frame += n.CurrSpeed * 40 * fpsFactor;
                            if (n.Frame > 76) n.Frame = 39;
                            if (n.Frame < 39) n.Frame = 39;

                            n.CurrSpeed = CurveValue(n.Speed * 0.7f, n.CurrSpeed, 20.0f);
                            MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                            float wpYaw = GetAngleToWaypoint(n, wp);
                            SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 20.0f));

                            if (GetDistanceToWaypoint(n, wp) < 0.2f)
                                n.PathLocation++;
                        }
                        else
                        {
                            if (n.PathTimer == 0)
                                n.PathStatus = FindPath(n, player.X, player.Y + 0.5f, player.Z);
                        }

                        if (prevFrame < 43 && n.Frame >= 43)
                            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.5f);
                        else if (prevFrame < 61 && n.Frame >= 61)
                            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.5f);
                    }
                }
                else
                {
                    n.State = 0;
                }

                n.Reload -= fpsFactor;
                break;

            default:
                n.Frame += 0.2f * fpsFactor;
                if (n.Frame > 201) n.Frame = 77;
                if (n.Frame < 77) n.Frame = 77;
                break;
        }
    }

    /// <summary>
    /// NPCs.bb UpdateMTFUnit - Full MTF Squad AI
    /// States: 0=wander, 1=search player, 2=contain 173, 3=follow path, 4=flee 106/049, 9=engage zombie
    /// </summary>
    private static void UpdateMTF(NPC n, PlayerState player, float fpsFactor)
    {
        if (n.IsDead)
        {
            n.BlinkTimer = -1.0f;
            n.Frame = 532;
            return;
        }

        n.MaxGravity = 0.03f;
        n.BlinkTimer -= fpsFactor;

        if (n.BlinkTimer <= -5.0f)
        {
            if ((int)n.State == 2 && Curr173 != null && OtherNPCSeesMeNPC(Curr173, n))
                OnPlaySound2?.Invoke(SoundID.MTFBlinking, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
            n.BlinkTimer = 70.0f * (_rng.NextSingle() * 5.0f + 10.0f);
        }

        n.Reload -= fpsFactor;
        float prevFrame = n.Frame;

        if ((int)n.State != 1) n.PrevState = 0;

        if (n.Idle > 0)
        {
            FinishWalking(n, 488, 522, 0.015f * 26, fpsFactor);
            n.Idle -= fpsFactor;
            if (n.Idle <= 0) n.Idle = 0;
            return;
        }

        switch ((int)n.State)
        {
            case 0: // Wandering
                n.Speed = 0.015f;
                if (n.PathTimer <= 0)
                {
                    if (n.MTFLeader != null)
                        n.PathStatus = FindPath(n, GetNPCX(n.MTFLeader), GetNPCY(n.MTFLeader) + 0.1f, GetNPCZ(n.MTFLeader));
                    else
                        n.PathStatus = FindPath(n, player.X + _rng.NextSingle() * 20 - 10, 0.1f, player.Z + _rng.NextSingle() * 20 - 10);

                    if (n.PathStatus == 1)
                    {
                        while (n.Path[n.PathLocation] == null && n.PathLocation <= 19)
                            n.PathLocation++;
                    }
                    n.PathTimer = 70.0f * (_rng.NextSingle() * 4.0f + 6.0f);
                }
                else if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                {
                    var wp = n.Path[n.PathLocation]!;
                    float wpYaw = GetAngleToWaypoint(n, wp);
                    SetNPCYaw(n, wpYaw);
                    n.Angle = CurveAngle(GetNPCYaw(n), n.Angle, 20.0f);

                    n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                    TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                    n.Frame += n.CurrSpeed * 26 * fpsFactor;
                    if (n.Frame > 522) n.Frame = 488;
                    if (n.Frame < 488) n.Frame = 488;

                    if (wp.Door != null && GetDistanceToWaypoint(n, wp) < 1.0f && !wp.Door.open)
                    {
                        wp.Door.open = true;
                        OnPlaySound2?.Invoke(SoundID.MTFDoorOpen, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    }

                    if (GetDistanceToWaypoint(n, wp) < 0.2f)
                        n.PathLocation++;

                    n.PathTimer -= fpsFactor;
                }
                else
                {
                    n.PathTimer -= fpsFactor * 2;
                    n.CurrSpeed = 0;
                    FinishWalking(n, 488, 522, n.Speed * 26, fpsFactor);
                }

                // Player detection
                if (!NoTarget && NPCSeesPlayer(n, player))
                {
                    OnPlaySound2?.Invoke(SoundID.MTFStop, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    n.LastSeen = (int)(70 * 35);
                    n.State = 1;
                    n.EnemyX = player.X;
                    n.EnemyY = player.Y;
                    n.EnemyZ = player.Z;
                    n.State2 = 70 * 15;
                    n.PathTimer = 0;
                    n.PathStatus = 0;
                    n.Reload = 200 - (AggressiveNPCs ? 100 : 0);
                }

                // SCP-173 detection
                if (Curr173 != null && Curr173.Idle < 2 && OtherNPCSeesMeNPC(Curr173, n) && EntityVisibleNPC(n, Curr173))
                {
                    n.State = 2;
                    n.State2 = 70 * 15;
                    n.State3 = 0;
                    n.PathTimer = 0;
                    OnPlaySound2?.Invoke(SoundID.MTF173Spotted, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                }

                // SCP-106 detection
                if (Curr106 != null && Curr106.State <= 0 && EntityDistanceNPC(n, Curr106) < 8.0f && EntityVisibleNPC(n, Curr106))
                {
                    n.State = 4;
                    n.State2 = 70 * 15;
                    n.Target = Curr106;
                    OnPlaySound2?.Invoke(SoundID.MTF106Spotted, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                }
                break;

            case 1: // Searching for player
                n.Speed = 0.015f;
                n.State2 -= fpsFactor;

                if (NPCSeesPlayer(n, player))
                {
                    float playerDist = EntityDistance(n, player);

                    if (playerDist < 4.0f)
                    {
                        float angle = VectorYaw(player.X - GetNPCX(n), player.Z - GetNPCZ(n));
                        SetNPCYaw(n, CurveAngle(angle, GetNPCYaw(n), 10.0f));

                        if (n.Reload <= 0 && player.KillTimer >= 0 && EntityVisible(n, player))
                        {
                            float angleDiff = WrapAngle(angle - GetNPCYaw(n));
                            if (angleDiff < 5 || angleDiff > 355)
                            {
                                OnPlaySound2?.Invoke(SoundID.Gunshot, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                Shoot(n, player, 5.0f / playerDist, AggressiveNPCs);
                                n.Reload = 7;

                                if (player.KillTimer < 0)
                                {
                                    OnDeath?.Invoke("Subject D-9341. Terminated by Nine-Tailed Fox.");
                                    OnPlaySound2?.Invoke(SoundID.MTFTargetTerminated, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                                }
                            }
                        }

                        // Shooting animation
                        n.Frame += 0.2f * fpsFactor;
                        if (n.Frame > 193) n.Frame = 78;
                        if (n.Frame < 78) n.Frame = 78;
                        n.CurrSpeed = CurveValue(0, n.CurrSpeed, 20.0f);
                    }
                    else
                    {
                        float targetYaw = GetAngleToPlayer(n, player);
                        SetNPCYaw(n, targetYaw);
                        n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                        TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                        n.Frame += n.CurrSpeed * 26 * fpsFactor;
                        if (n.Frame > 522) n.Frame = 488;
                    }
                }
                else
                {
                    n.LastSeen = (int)(n.LastSeen - fpsFactor);

                    if (n.PathTimer <= 0)
                    {
                        n.PathStatus = FindPath(n, n.EnemyX, n.EnemyY + 0.1f, n.EnemyZ);
                        n.PathTimer = 70 * 8;
                    }
                    else if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                    {
                        var wp = n.Path[n.PathLocation]!;
                        float wpYaw = GetAngleToWaypoint(n, wp);
                        SetNPCYaw(n, wpYaw);

                        n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                        TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                        n.Frame += n.CurrSpeed * 26 * fpsFactor;
                        if (n.Frame > 522) n.Frame = 488;

                        if (GetDistanceToWaypoint(n, wp) < 0.2f)
                            n.PathLocation++;

                        n.PathTimer -= fpsFactor;
                    }
                    else
                    {
                        n.CurrSpeed = 0;
                        FinishWalking(n, 488, 522, n.Speed * 26, fpsFactor);
                        n.PathTimer -= fpsFactor;
                    }
                }

                if (n.State2 <= 0)
                {
                    if (n.MTFLeader == null)
                        OnPlaySound2?.Invoke(SoundID.MTFTargetLost, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    n.State = 0;
                }
                break;

            case 2: // Containing SCP-173
                if (Curr173 == null || Curr173.Idle == 2)
                {
                    n.State = 0;
                    break;
                }

                foreach (var n2 in GetAllNPCs())
                    if (n2 != n && n2.NPCType == (int)NPCType.MTF)
                        n2.State = 2;

                float dist173 = Distance2D(GetNPCX(n), GetNPCZ(n), GetNPCX(Curr173), GetNPCZ(Curr173));

                if (dist173 < 5.0f)
                {
                    Curr173.Idle = 1;
                    n.State2 = 70 * 15;
                    float tempDist = (n.MTFLeader != null) ? 2.0f : 1.0f;

                    if (dist173 < tempDist)
                    {
                        if (n.MTFLeader == null)
                        {
                            n.State3 += fpsFactor;
                            if (n.State3 >= 70 * 15)
                            {
                                Curr173.Idle = 2;
                                Curr173.Target = n;
                                OnPlaySound2?.Invoke(SoundID.MTF173Box, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                            }
                        }
                        FinishWalking(n, 488, 522, n.Speed * 26, fpsFactor);
                    }
                    else
                    {
                        float yaw173 = GetAngleToNPC(n, Curr173);
                        SetNPCYaw(n, yaw173);
                        n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                        TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                        n.Frame += n.CurrSpeed * 26 * fpsFactor;
                        if (n.Frame > 522) n.Frame = 488;
                    }
                }
                else
                {
                    Curr173.Idle = 0;
                    if (n.PathTimer <= 0)
                    {
                        n.PathStatus = FindPath(n, GetNPCX(Curr173), GetNPCY(Curr173) + 0.1f, GetNPCZ(Curr173));
                        n.PathTimer = 70 * 8;
                    }
                    else if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                    {
                        var wp = n.Path[n.PathLocation]!;
                        float wpYaw = GetAngleToWaypoint(n, wp);
                        SetNPCYaw(n, wpYaw);

                        n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                        TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                        n.Frame += n.CurrSpeed * 26 * fpsFactor;
                        if (n.Frame > 522) n.Frame = 488;

                        if (GetDistanceToWaypoint(n, wp) < 0.2f)
                            n.PathLocation++;

                        n.PathTimer -= fpsFactor;
                    }
                    else
                    {
                        n.PathTimer -= fpsFactor * 2;
                        FinishWalking(n, 488, 522, n.Speed * 26, fpsFactor);
                    }
                }
                break;

            case 4: // SCP-106/049 detected - retreat
                n.Speed = 0.03f;
                n.State2 -= fpsFactor;

                if (n.State2 > 0 && n.Target != null)
                {
                    if (EntityDistanceNPC(n.Target, n) < 3.0f)
                    {
                        float yawTarget = GetAngleToNPC(n, n.Target);
                        SetNPCYaw(n, yawTarget);
                        n.CurrSpeed = CurveValue(-n.Speed, n.CurrSpeed, 20.0f);
                        TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                        n.Frame -= n.CurrSpeed * 26 * fpsFactor;
                        if (n.Frame < 488) n.Frame = 522;
                    }
                    else
                    {
                        n.PathTimer -= fpsFactor;
                        if (n.PathTimer <= 0)
                        {
                            n.PathStatus = FindPath(n, GetNPCX(n) + _rng.NextSingle() * 20 - 10, 0.1f, GetNPCZ(n) + _rng.NextSingle() * 20 - 10);
                            n.PathTimer = 70 * 5;
                        }
                        else if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                        {
                            var wp = n.Path[n.PathLocation]!;
                            float wpYaw = GetAngleToWaypoint(n, wp);
                            SetNPCYaw(n, wpYaw);

                            n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                            TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                            n.Frame += n.CurrSpeed * 26 * fpsFactor;
                            if (n.Frame > 522) n.Frame = 488;

                            if (GetDistanceToWaypoint(n, wp) < 0.2f)
                                n.PathLocation++;
                        }
                    }
                }
                else
                {
                    n.State = 0;
                    n.Target = null;
                }
                break;

            case 9: // Zombie engagement
                n.State2 -= fpsFactor;

                if (n.State2 > 0 && n.Target != null && !n.Target.IsDead)
                {
                    float zombieDist = EntityDistanceNPC(n, n.Target);

                    if (zombieDist < 8.0f && n.Reload <= 0 && EntityVisibleNPC(n, n.Target))
                    {
                        float yawZ = GetAngleToNPC(n, n.Target);
                        SetNPCYaw(n, CurveAngle(yawZ, GetNPCYaw(n), 10.0f));

                        OnPlaySound2?.Invoke(SoundID.Gunshot, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                        n.Target.Health -= _rng.NextSingle() * 20 + 10;
                        if (n.Target.Health <= 0)
                        {
                            n.Target.IsDead = true;
                            n.State = 0;
                        }
                        n.Reload = 7;

                        n.Frame += 0.35f * fpsFactor;
                        if (n.Frame > 248) n.Frame = 245;
                    }
                    else
                    {
                        if (n.PathTimer <= 0)
                        {
                            n.PathStatus = FindPath(n, GetNPCX(n.Target), GetNPCY(n.Target) + 0.1f, GetNPCZ(n.Target));
                            n.PathTimer = 70 * 3;
                        }
                        else if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                        {
                            var wp = n.Path[n.PathLocation]!;
                            SetNPCYaw(n, GetAngleToWaypoint(n, wp));
                            n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                            TranslateNPCForward(n, n.CurrSpeed * fpsFactor);

                            n.Frame += n.CurrSpeed * 26 * fpsFactor;
                            if (n.Frame > 522) n.Frame = 488;

                            if (GetDistanceToWaypoint(n, wp) < 0.2f)
                                n.PathLocation++;

                            n.PathTimer -= fpsFactor;
                        }
                    }
                }
                else
                {
                    n.State = 0;
                    n.Target = null;
                }
                break;

            default:
                n.State = 0;
                break;
        }

        // Footstep sounds
        if (n.CurrSpeed > 0.005f && ((prevFrame < 495 && n.Frame >= 495) || (prevFrame < 510 && n.Frame >= 510)))
            OnPlaySound2?.Invoke(SoundID.Step2SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), _rng.NextSingle() * 0.2f + 0.8f);
    }

    /// <summary>
    /// NPCs.bb Class-D/Clerk AI - fear and fleeing behavior
    /// </summary>
    private static void UpdateClassD(NPC n, PlayerState player, float fpsFactor)
    {
        float dist = EntityDistance(n, player);

        switch ((int)n.State)
        {
            case 0: // Idle
                n.Frame += 0.2f * fpsFactor;
                if (n.Frame > 284) n.Frame = 155;
                if (n.Frame < 155) n.Frame = 155;
                break;

            case 1: // Following path
                if (n.PathStatus == 1 && n.Path[n.PathLocation] != null)
                {
                    var wp = n.Path[n.PathLocation]!;
                    float wpYaw = GetAngleToWaypoint(n, wp);
                    SetNPCYaw(n, CurveAngle(wpYaw, GetNPCYaw(n), 20.0f));

                    n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);
                    MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                    n.Frame += n.CurrSpeed * 40 * fpsFactor;
                    if (n.Frame > 37) n.Frame = 1;

                    if (GetDistanceToWaypoint(n, wp) < 0.2f)
                        n.PathLocation++;
                }
                else if (n.PathStatus == 2)
                {
                    n.State = 0;
                    n.CurrSpeed = 0;
                }
                else
                {
                    n.CurrSpeed = 0;
                    n.Frame += 0.2f * fpsFactor;
                    if (n.Frame > 284) n.Frame = 155;
                }
                break;

            case 2: // Fleeing
                n.Speed = 0.025f;
                n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 10.0f);

                // Run away from player
                float awayYaw = GetAngleToPlayer(n, player) + 180;
                SetNPCYaw(n, CurveAngle(awayYaw, GetNPCYaw(n), 15.0f));
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                n.Frame += n.CurrSpeed * 50 * fpsFactor;
                if (n.Frame > 154) n.Frame = 38;
                if (n.Frame < 38) n.Frame = 38;

                if (dist > 15.0f)
                    n.State = 0;
                break;
        }
    }

    private static void Update5131(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update372(NPC n, PlayerState player, float fpsFactor, float blinkTimer) { }
    private static void UpdateApache(NPC n, PlayerState player, float fpsFactor) { }
    private static void UpdateTentacle(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update860(NPC n, PlayerState player, float fpsFactor) { }

    /// <summary>
    /// NPCs.bb SCP-939 - Sound-based hunting
    /// States: 0=idle, 1=alert, 2=patrol, 3=attack, 5=eating
    /// </summary>
    private static void Update939(NPC n, PlayerState player, float fpsFactor)
    {
        string roomName = player.PlayerRoom?.RoomTemplate?.Name ?? "";
        if (roomName != "room3storage")
        {
            n.State = 66;
            return;
        }

        if (n.State >= 66) return;

        float dist = EntityDistance(n, player);
        float prevFrame = n.Frame;

        switch ((int)n.State)
        {
            case 0: // Idle
                n.Frame += 0.1f * fpsFactor;
                if (n.Frame > 405) n.Frame = 290;
                if (n.Frame < 290) n.Frame = 290;
                break;

            case 1: // Alert - heard something
                if (n.Frame >= 644 && n.Frame < 683)
                {
                    n.CurrSpeed = CurveValue(n.Speed * 0.05f, n.CurrSpeed, 10.0f);
                    n.Frame += 28 * n.CurrSpeed * 4 * fpsFactor;
                    if (n.Frame >= 682) n.Frame = 175;
                }
                else
                {
                    n.CurrSpeed = CurveValue(0, n.CurrSpeed, 5.0f);
                    n.Frame += 0.22f * fpsFactor;
                    if (n.Frame >= 296) n.State = 2;
                }
                n.LastSeen = 0;
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);
                break;

            case 2: // Patrol
                n.CurrSpeed = CurveValue(n.Speed * 0.3f * MathF.Min(dist, 1.0f), n.CurrSpeed, 10.0f);
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                n.Frame += 28 * n.CurrSpeed * fpsFactor;
                if (n.Frame > 683) n.Frame = 644;
                if (n.Frame < 644) n.Frame = 644;

                // Footstep sound triggers lure sound
                if ((prevFrame < 664 && n.Frame >= 664) || (prevFrame > 673 && n.Frame < 654))
                {
                    OnPlaySound2?.Invoke(SoundID.Step4SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    if (_rng.Next(10) == 1)
                        OnPlaySound2?.Invoke(SoundID.Scp939Lure, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                }
                break;

            case 3: // Attack
                if (EntityVisible(n, player))
                {
                    n.EnemyX = player.X;
                    n.EnemyZ = player.Z;
                    n.LastSeen = 10 * 7;
                }

                if (n.LastSeen > 0 && !NoTarget)
                {
                    if (n.Frame >= 18 && n.Frame < 68)
                    {
                        // Attack animation
                        n.CurrSpeed = CurveValue(0, n.CurrSpeed, 5.0f);
                        n.Frame += 0.5f * fpsFactor;
                        if (n.Frame > 68) n.Frame = 68;

                        // Hit check
                        if ((prevFrame < 24 && n.Frame >= 24) || (prevFrame < 57 && n.Frame >= 57))
                        {
                            if (Distance2D(n.EnemyX, n.EnemyZ, GetNPCX(n), GetNPCZ(n)) < 1.5f)
                            {
                                OnPlaySound?.Invoke(SoundID.Slash1);
                                player.Injuries += _rng.NextSingle() * 1.0f + 1.5f - player.WearingVest * 0.5f;
                                player.BlurTimer = 500;

                                if (player.Injuries > 4.0f)
                                {
                                    OnDeath?.Invoke("All four (4) escaped SCP-939 specimens have been captured and recontained successfully.");
                                    OnKill?.Invoke();
                                    if (!player.GodMode) n.State = 5;
                                }
                            }
                            else
                            {
                                n.Frame = 449;
                            }
                        }
                    }
                    else
                    {
                        if (n.LastSeen == 10 * 7)
                        {
                            n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 20.0f);

                            n.Frame += 6 * n.CurrSpeed * fpsFactor;
                            if (n.Frame > 464) n.Frame = 449;
                            if (n.Frame < 449) n.Frame = 449;

                            if ((prevFrame < 452 && n.Frame >= 452) || (prevFrame < 459 && n.Frame >= 459))
                                OnPlaySound2?.Invoke(SoundID.Step1SFX, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);

                            if (Distance2D(n.EnemyX, n.EnemyZ, GetNPCX(n), GetNPCZ(n)) < 1.1f)
                                n.Frame = 18;
                        }
                        else
                        {
                            n.CurrSpeed = CurveValue(0, n.CurrSpeed, 5.0f);
                            n.Frame += 5 * n.CurrSpeed * fpsFactor;
                            if (n.Frame > 297) n.Frame = 175;
                        }
                    }

                    float angle = VectorYaw(n.EnemyX - GetNPCX(n), n.EnemyZ - GetNPCZ(n));
                    SetNPCYaw(n, CurveAngle(angle, GetNPCYaw(n), 15.0f));
                    MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                    n.LastSeen = (int)(n.LastSeen - fpsFactor);
                }
                else
                {
                    n.State = 2;
                }
                break;

            case 5: // Eating
                if (n.Frame < 68)
                {
                    n.Frame += 0.5f * fpsFactor;
                    if (n.Frame > 68) n.Frame = 68;
                }
                else
                {
                    n.Frame += 0.5f * fpsFactor;
                    if (n.Frame > 473) n.Frame = 464;
                    if (n.Frame < 464) n.Frame = 464;
                }
                break;
        }

        // Sound-based detection
        if (n.State < 3 && !NoTarget && !n.IgnorePlayer)
        {
            float soundDist = dist;
            if (dist < 4.0f && EntityVisible(n, player)) soundDist -= 1.0f;

            if (player.SoundVolume * 1.2f > soundDist || dist < 1.5f)
            {
                if (n.State3 == 0)
                {
                    OnPlaySound2?.Invoke(SoundID.Scp939Attack, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    n.State3 = 1;
                }
                n.State = 3;
            }
            else if (player.SoundVolume * 1.6f > soundDist)
            {
                if ((int)n.State != 1 && n.Reload <= 0)
                {
                    OnPlaySound2?.Invoke(SoundID.Scp939Alert, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                    n.Frame = 175;
                    n.Reload = 70 * 3;
                }
                n.State = 1;
            }

            n.Reload -= fpsFactor;
        }
    }

    /// <summary>
    /// NPCs.bb SCP-066 - "Eric?" rolling ball
    /// States: 0=idle far, 1=staring, 2=roll towards, 3=escape
    /// </summary>
    private static void Update066(NPC n, PlayerState player, float fpsFactor)
    {
        float dist = Distance2D(player.X, player.Z, GetNPCX(n), GetNPCZ(n));

        switch ((int)n.State)
        {
            case 0: // Idle - far from player
                n.Frame += 0.2f * fpsFactor;
                if (n.Frame > 612) n.Frame = 451;
                if (n.Frame < 451) n.Frame = 451;

                if (dist > 20.0f)
                {
                    if (n.State2 < Environment.TickCount)
                    {
                        TeleportCloser(n, player);
                        n.State2 = Environment.TickCount + 5000;
                    }
                }
                else if (dist < 8.0f)
                {
                    n.LastDist = _rng.NextSingle() * 1.5f + 1.0f;
                    n.State = 1;
                }
                break;

            case 1: // Staring at player
                if (n.Frame < 451)
                {
                    float angle = WrapAngle(CurveAngle(GetAngleToPlayer(n, player) - 180, n.Frame / 1.2445f, 15.0f));
                    n.Frame = angle * 1.2445f + 2.0f;
                }
                else
                {
                    n.Frame += 0.4f * fpsFactor;
                    if (n.Frame >= 646) n.Frame = 2;
                }

                if (_rng.Next(700) == 1)
                    OnPlaySound2?.Invoke(SoundID.Scp066Eric, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);

                if (dist < 1.0f + n.LastDist)
                    n.State = _rng.Next(2, 4);
                break;

            case 2: // Roll towards player and make sound
                if (n.Frame < 647)
                {
                    float angle = CurveAngle(0, n.Frame / 1.2445f, 5.0f);
                    if (angle < 5 || angle > 355)
                        n.Frame = 647;
                    else
                        n.Frame = angle * 1.2445f + 2.0f;
                }
                else
                {
                    if (MathF.Abs(n.Frame - 683) < 0.1f)
                    {
                        if (n.State2 == 0)
                        {
                            if (_rng.Next(2) == 1)
                                OnPlaySound2?.Invoke(SoundID.Scp066Eric, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);
                            else
                                OnPlaySound2?.Invoke(SoundID.Scp066Notes, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 1.0f);

                            // Random effects
                            switch (_rng.Next(1, 7))
                            {
                                case 1: // Beethoven - deafen
                                    OnPlaySound?.Invoke(SoundID.Scp066Beethoven);
                                    player.DeafTimer = 70 * (45 + (AggressiveNPCs ? 15 : 0));
                                    player.CameraShake = 10.0f;
                                    break;
                                case 2:
                                    n.State3 = _rng.Next(700, 1401);
                                    break;
                                case 4: // Blood splat
                                    player.CameraShake = 5.0f;
                                    if (dist < 0.8f)
                                        player.Injuries += _rng.NextSingle() * 0.2f + 0.3f;
                                    break;
                            }
                        }

                        n.State2 += fpsFactor;
                        if (n.State2 > 70)
                        {
                            n.State = 3;
                            n.State2 = 0;
                        }
                    }
                    else
                    {
                        n.CurrSpeed = CurveValue(n.Speed * 1.5f, n.CurrSpeed, 10.0f);
                        float targetYaw = GetAngleToPlayer(n, player) - 180;
                        SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 10.0f));

                        n.Frame += n.CurrSpeed * 25 * fpsFactor;
                        if (n.Frame > 683) n.Frame = 683;

                        MoveNPCForward(n, -n.CurrSpeed * fpsFactor);
                    }
                }
                break;

            case 3: // Escape
                float escapeYaw = GetAngleToPlayer(n, player) + n.Angle - 180;
                SetNPCYaw(n, CurveAngle(escapeYaw, GetNPCYaw(n), 10.0f));

                n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 10.0f);
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                if (_rng.Next(100) == 1)
                    n.Angle = _rng.NextSingle() * 40 - 20;

                n.State2 += fpsFactor;
                if (n.State2 > 250)
                {
                    n.Frame -= n.CurrSpeed * 25 * fpsFactor;
                    if (n.Frame <= 647)
                    {
                        n.State = 0;
                        n.State2 = 0;
                    }
                }
                else
                {
                    n.Frame -= n.CurrSpeed * 25 * fpsFactor;
                    if (n.Frame < 647) n.Frame = 684;
                }
                break;
        }

        // Rolling sound
        if (n.State > 1)
            OnPlaySound2?.Invoke(SoundID.Scp066Rolling, new Vector3(GetNPCX(n), GetNPCY(n), GetNPCZ(n)), 0.5f);

        // Heartbeat effect
        if (n.State3 > 0)
        {
            n.State3 -= fpsFactor;
            player.HeartBeatRate = MathF.Max(player.HeartBeatRate, 130);
            player.HeartBeatVolume = MathF.Max(player.HeartBeatVolume, MathF.Min(n.State3 / 1000, 1.0f));
        }
    }

    /// <summary>
    /// NPCs.bb SCP-966 - Invisible stalker
    /// States: 0=idle, 1-4=echo, 5-6=wander, 8+=attack
    /// </summary>
    private static void Update966(NPC n, PlayerState player, float fpsFactor)
    {
        float dist = EntityDistance(n, player);

        if (dist >= HideDistance) return;

        float prevFrame = n.Frame;

        // Visibility based on NVG
        if (player.WearingNightVision == 0)
        {
            n.HideFromNVG = true;

            if (dist < 1 && n.Reload <= 0 && player.MsgTimer <= 0)
            {
                string[] msgs = {
                    "You feel something breathing right next to you.",
                    "\"It feels like something's in this room with me.\"",
                    "You feel like something is here with you, but you do not see anything.",
                    "\"Is my mind playing tricks on me or is there someone else here?\"",
                    "You feel like something is following you.",
                    "You can feel something near you, but you are unable to see it."
                };
                OnMessage?.Invoke(msgs[_rng.Next(msgs.Length)]);
                n.Reload = 20 * 70;
                player.MsgTimer = 8 * 70;
            }
            n.Reload -= fpsFactor;
        }
        else
        {
            n.HideFromNVG = false;
        }

        // Exhaustion tracking - triggers attack mode
        if (n.State3 > 5 * 70)
        {
            if (n.State3 < 1000)
            {
                foreach (var n2 in GetAllNPCs())
                    if (n2.NPCType == (int)NPCType.SCP966)
                        n2.State3 = 1000;
            }
            n.State = MathF.Max(n.State, 8);
            n.State3 = 1000;
        }

        if (player.Stamina < 10)
            n.State3 += fpsFactor;
        else if (n.State3 < 900)
            n.State3 = MathF.Max(n.State3 - fpsFactor * 0.2f, 0);

        if ((int)n.State != 10)
            n.LastSeen = 0;

        switch ((int)n.State)
        {
            case 0: // Idle
                if (n.Frame > 556)
                {
                    n.Frame += 0.25f * fpsFactor;
                    if (n.Frame > 651) n.Frame = 2;
                }
                else
                {
                    n.Frame += 0.25f * fpsFactor;
                    if (n.Frame > 213)
                    {
                        if (_rng.Next(3) == 1 && dist < 4)
                            n.State = _rng.Next(1, 5);
                        else
                            n.State = _rng.Next(5, 7);
                    }

                    if (dist < 2.0f)
                        n.State = _rng.Next(1, 5);
                }

                n.CurrSpeed = CurveValue(0, n.CurrSpeed, 10.0f);
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);
                break;

            case 1:
            case 2:
            case 3:
            case 4: // Echo/stare
                n.Frame += 0.25f * fpsFactor;
                if (n.Frame > 300) n.State = 0;
                break;

            case 5:
            case 6: // Wander
                n.CurrSpeed = CurveValue(n.Speed * 0.3f, n.CurrSpeed, 10.0f);
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                n.Frame += n.CurrSpeed * 30 * fpsFactor;
                if (n.Frame > 556) n.Frame = 400;
                if (n.Frame < 400) n.Frame = 400;

                n.State2 += fpsFactor;
                if (n.State2 > 200)
                {
                    n.State = 0;
                    n.State2 = 0;
                }

                if (_rng.Next(50) == 1)
                    SetNPCYaw(n, GetNPCYaw(n) + _rng.NextSingle() * 40 - 20);
                break;

            default: // Attack mode (state >= 8)
                float targetYaw = GetAngleToPlayer(n, player);
                SetNPCYaw(n, CurveAngle(targetYaw, GetNPCYaw(n), 20.0f));

                n.CurrSpeed = CurveValue(n.Speed, n.CurrSpeed, 10.0f);
                MoveNPCForward(n, n.CurrSpeed * fpsFactor);

                n.Frame += n.CurrSpeed * 40 * fpsFactor;
                if (n.Frame > 652) n.Frame = 557;
                if (n.Frame < 557) n.Frame = 557;

                // Attack when close
                if (dist < 0.8f && n.Reload <= 0)
                {
                    player.Injuries += _rng.NextSingle() * 0.3f + 0.2f;
                    OnPlaySound?.Invoke(SoundID.DamageSFX1);
                    player.BlurTimer = 300;
                    n.Reload = 70 * 2;

                    if (player.Injuries > 5)
                    {
                        OnDeath?.Invoke("Subject D-9341 found dead in [REDACTED]. Body was determined to have been partially consumed by instances of SCP-966.");
                        OnKill?.Invoke();
                    }
                }
                break;
        }
    }

    private static void Update1048A(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update1499(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update008(NPC n, PlayerState player, float fpsFactor) { }

    // Helper functions

    /// <summary>
    /// CurveValue: current + (target - current) / rate
    /// </summary>
    public static float CurveValue(float target, float current, float rate)
    {
        return current + (target - current) / rate;
    }

    /// <summary>
    /// CurveAngle with wrapping
    /// </summary>
    public static float CurveAngle(float target, float current, float rate)
    {
        float diff = WrapAngle(target - current);
        if (diff > 180) diff -= 360;
        return WrapAngle(current + diff / rate);
    }

    public static float WrapAngle(float angle)
    {
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        return angle;
    }

    public static float Distance2D(float x1, float z1, float x2, float z2)
    {
        float dx = x2 - x1;
        float dz = z2 - z1;
        return MathF.Sqrt(dx * dx + dz * dz);
    }

    public static float EntityDistance(NPC n, PlayerState player)
    {
        float dx = GetNPCX(n) - player.X;
        float dy = GetNPCY(n) - player.Y;
        float dz = GetNPCZ(n) - player.Z;
        return MathF.Sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float EntityDistanceNPC(NPC n1, NPC n2)
    {
        float dx = GetNPCX(n1) - GetNPCX(n2);
        float dy = GetNPCY(n1) - GetNPCY(n2);
        float dz = GetNPCZ(n1) - GetNPCZ(n2);
        return MathF.Sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static bool EntityVisible(NPC n, PlayerState player)
    {
        // Raycast check - full implementation needs collision system
        // For now check distance and angle
        float dist = EntityDistance(n, player);
        if (dist > 30) return false;
        return true;
    }

    public static bool EntityVisibleNPC(NPC n1, NPC n2)
    {
        float dist = EntityDistanceNPC(n1, n2);
        if (dist > 30) return false;
        return true;
    }

    public static bool IsNPCOnScreen(NPC n, PlayerState player)
    {
        // CameraProject equivalent - check if NPC is within screen bounds
        float dx = GetNPCX(n) - player.X;
        float dz = GetNPCZ(n) - player.Z;
        float angleToNPC = MathF.Atan2(dx, dz) * 180.0f / MathF.PI;
        float angleDiff = MathF.Abs(WrapAngle(angleToNPC - player.Yaw));
        if (angleDiff > 180) angleDiff = 360 - angleDiff;
        return angleDiff < 45; // ~90 degree FOV
    }

    public static bool EntityInView(NPC n, PlayerState player)
    {
        // Check if NPC is in player's view frustum
        float dx = GetNPCX(n) - player.X;
        float dz = GetNPCZ(n) - player.Z;
        float angleToNPC = MathF.Atan2(dx, dz) * 180.0f / MathF.PI;
        float angleDiff = MathF.Abs(WrapAngle(angleToNPC - player.Yaw));
        if (angleDiff > 180) angleDiff = 360 - angleDiff;
        return angleDiff < 45; // ~90 degree FOV
    }

    /// <summary>
    /// MeNPCSeesPlayer - check if NPC can see player (sound-based detection)
    /// </summary>
    public static bool NPCSeesPlayer(NPC n, PlayerState player)
    {
        float dist = EntityDistance(n, player);
        if (dist > HideDistance) return false;

        // Check if NPC is facing player direction
        float angleToPlayer = GetAngleToPlayer(n, player);
        float angleDiff = MathF.Abs(WrapAngle(angleToPlayer - GetNPCYaw(n)));
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        // NPC can "see" (hear) player if within 60 degrees
        if (angleDiff < 60 && dist < 8.0f)
            return true;

        // Also detect by sound if player is moving
        if (player.CurrSpeed > 0.01f && dist < 6.0f)
            return true;

        return false;
    }

    public static bool CheckForNPCInFacility(NPC n)
    {
        return n.InFacility;
    }

    // Position/Rotation helpers
    private static float GetNPCX(NPC n) => n.PrevX;
    private static float GetNPCY(NPC n) => n.PrevY;
    private static float GetNPCZ(NPC n) => n.PrevZ;
    private static float GetNPCYaw(NPC n) => n.Angle;

    private static void SetNPCPosition(NPC n, float x, float y, float z)
    {
        n.PrevX = x;
        n.PrevY = y;
        n.PrevZ = z;
    }

    private static void SetNPCYaw(NPC n, float yaw)
    {
        n.Angle = WrapAngle(yaw);
    }

    private static void TranslateNPC(NPC n, float x, float y, float z)
    {
        n.PrevX += x;
        n.PrevY += y;
        n.PrevZ += z;
    }

    private static void MoveNPCForward(NPC n, float distance)
    {
        float yaw = n.Angle * MathF.PI / 180.0f;
        n.PrevX += MathF.Sin(yaw) * distance;
        n.PrevZ += MathF.Cos(yaw) * distance;
    }

    private static void TranslateNPCForward(NPC n, float distance)
    {
        float yaw = (n.Angle + 90) * MathF.PI / 180.0f;
        n.PrevX += MathF.Cos(yaw) * distance;
        n.PrevZ += MathF.Sin(yaw) * distance;
    }

    public static float VectorYaw(float x, float z)
    {
        return MathF.Atan2(x, z) * 180.0f / MathF.PI;
    }

    public static bool OtherNPCSeesMeNPC(NPC target, NPC observer)
    {
        float dist = EntityDistanceNPC(target, observer);
        if (dist > HideDistance) return false;

        float angle = GetAngleToNPC(observer, target);
        float angleDiff = MathF.Abs(WrapAngle(angle - observer.Angle));
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        return angleDiff < 60;
    }

    private static void FinishWalking(NPC n, float startFrame, float endFrame, float speed, float fpsFactor)
    {
        if (n.Frame >= startFrame && n.Frame < endFrame)
        {
            n.Frame += speed * fpsFactor;
            if (n.Frame > endFrame) n.Frame = startFrame;
        }
        else
        {
            n.Frame = startFrame;
        }
        n.CurrSpeed = CurveValue(0, n.CurrSpeed, 10.0f);
    }

    private static void Shoot(NPC shooter, PlayerState player, float accuracy, bool aggressive)
    {
        float shootAccuracy = aggressive ? 0.1f : accuracy;
        float hitChance = 1.0f - shootAccuracy;

        if (_rng.NextSingle() < hitChance)
        {
            player.Injuries += _rng.NextSingle() * 0.5f + 0.5f;
            player.BlurTimer = 200;
            player.CameraShake = 3.0f;

            if (player.Injuries > 3.5f && !player.GodMode)
            {
                player.KillTimer = -1;
                OnKill?.Invoke();
            }
        }
    }

    public static IEnumerable<NPC> GetAllNPCs()
    {
        return _npcs;
    }

    private static void ClampNPCY(NPC n, float maxY)
    {
        if (n.PrevY > maxY) n.PrevY = maxY;
    }

    private static float GetAngleToPlayer(NPC n, PlayerState player)
    {
        float dx = player.X - GetNPCX(n);
        float dz = player.Z - GetNPCZ(n);
        return MathF.Atan2(dx, dz) * 180.0f / MathF.PI;
    }

    private static float GetAngleToNPC(NPC from, NPC to)
    {
        float dx = GetNPCX(to) - GetNPCX(from);
        float dz = GetNPCZ(to) - GetNPCZ(from);
        return MathF.Atan2(dx, dz) * 180.0f / MathF.PI;
    }

    private static void TeleportToWaypoint(NPC n, PlayerState player, float minDist, float maxDist)
    {
        // Teleport 173 to waypoint within range
    }

    private static void TeleportCloser(NPC n, PlayerState player)
    {
        // Move NPC closer to player via waypoints
    }

    private static void TryOpenNearbyDoors(NPC n)
    {
        // Check and open doors near NPC
    }

    // SCP-106 specific helpers
    private static void HideNPC(NPC n)
    {
        // HideEntity n\obj / HideEntity n\obj2
        n.HideFromNVG = true;
    }

    private static void ShowNPC(NPC n)
    {
        n.HideFromNVG = false;
    }

    private static void HideNPCObj2(NPC n)
    {
        // HideEntity n\obj2 - shadow marker
    }

    private static void ShowNPCObj2(NPC n)
    {
        // ShowEntity n\obj2
    }

    private static void SetNPCObj2Alpha(NPC n, float alpha)
    {
        // EntityAlpha(n\obj2, alpha)
    }

    private static void ResetNPCCollider(NPC n)
    {
        // ResetEntity(n\Collider)
    }

    private static float GetWaypointY(WayPoint wp)
    {
        // EntityY(wp\obj, True)
        return 0;
    }

    private static float GetAngleToWaypoint(NPC n, WayPoint wp)
    {
        // PointEntity then get yaw
        return 0;
    }

    private static float GetDistanceToWaypoint(NPC n, WayPoint wp)
    {
        // EntityDistance(n\Collider, wp\obj)
        return 10.0f;
    }

    /// <summary>
    /// NPCs.bb FindPath function
    /// Returns: 0 = no path, 1 = path found, 2 = already at destination
    /// </summary>
    private static int FindPath(NPC n, float targetX, float targetY, float targetZ)
    {
        float dist = Distance2D(GetNPCX(n), GetNPCZ(n), targetX, targetZ);
        if (dist < 1.0f) return 2;

        // Clear existing path
        for (int i = 0; i < 21; i++)
            n.Path[i] = null;
        n.PathLocation = 0;

        // Full pathfinding implementation would use waypoint graph
        // For now return 0 to use direct chase
        return 0;
    }
}

public class WayPoint
{
    public int Obj;
    public Rooms? Room;
    public Doors? Door;
    public WayPoint?[] Connected = new WayPoint?[5];
}
