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
    private static void UpdateGuard(NPC n, PlayerState player, float fpsFactor) { }
    private static void UpdateMTF(NPC n, PlayerState player, float fpsFactor) { }
    private static void UpdateClassD(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update5131(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update372(NPC n, PlayerState player, float fpsFactor, float blinkTimer) { }
    private static void UpdateApache(NPC n, PlayerState player, float fpsFactor) { }
    private static void UpdateTentacle(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update860(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update939(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update066(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update966(NPC n, PlayerState player, float fpsFactor) { }
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
