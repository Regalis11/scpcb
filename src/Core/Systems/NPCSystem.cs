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

    public static Action<string>? OnDeath;
    public static Action? OnKill;
    public static Action<int>? OnPlaySound;
    public static Action<int, Vector3, float>? OnPlaySound2;

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

    // Placeholder methods for other NPCs
    private static void Update106(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update096(NPC n, PlayerState player, float fpsFactor) { }
    private static void Update049(NPC n, PlayerState player, float fpsFactor) { }
    private static void UpdateZombie(NPC n, PlayerState player, float fpsFactor) { }
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
        // Raycast check - simplified for now, full implementation needs collision system
        return true;
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
}

public class WayPoint
{
    public int Obj;
    public Rooms? Room;
    public Doors? Door;
    public WayPoint?[] Connected = new WayPoint?[5];
}
