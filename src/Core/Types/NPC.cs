/*
 * Source: NPCs.bb
 * Lines: 3-7
 * Type: Const (NPC types)
 */

namespace SCPContainmentBreach.Core.Types;

public static class NPCTypes
{
    public const int NPCtype173 = 1;
    public const int NPCtypeOldMan = 2;
    public const int NPCtypeGuard = 3;
    public const int NPCtypeD = 4;
    public const int NPCtype372 = 6;
    public const int NPCtypeApache = 7;
    public const int NPCtypeMTF = 8;
    public const int NPCtype096 = 9;
    public const int NPCtype049 = 10;
    public const int NPCtypeZombie = 11;
    public const int NPCtype5131 = 12;
    public const int NPCtypeTentacle = 13;
    public const int NPCtype860 = 14;
    public const int NPCtype939 = 15;
    public const int NPCtype066 = 16;
    public const int NPCtypePdPlane = 17;
    public const int NPCtype966 = 18;
    public const int NPCtype1048a = 19;
    public const int NPCtype1499 = 20;
    public const int NPCtype008 = 21;
    public const int NPCtypeClerk = 22;
}

/*
 * Source: NPCs.bb
 * Lines: 10-71
 * Type: NPCs
 */

public class NPCs
{
    public int obj;
    public int obj2;
    public int obj3;
    public int obj4;
    public int Collider;

    public int NPCtype;
    public int ID;

    public float DropSpeed;
    public int Gravity;

    public float State;
    public float State2;
    public float State3;
    public int PrevState;
    public int MakingNoise;

    public float Frame;
    public float Angle;

    public int Sound;
    public int SoundChn;
    public float SoundTimer;
    public int Sound2;
    public int SoundChn2;
    public int SoundChn_IsStream;
    public int SoundChn2_IsStream;

    public float Speed;
    public float CurrSpeed;

    public string texture = "";

    public float Idle;
    public float IdleTimer;

    public float Reload;

    public int LastSeen;
    public float LastDist;

    public float PrevX;
    public float PrevY;
    public float PrevZ;

    public NPCs? Target;
    public int TargetID;
    public float EnemyX;
    public float EnemyY;
    public float EnemyZ;

    public WayPoints?[] Path = new WayPoints?[21];
    public int PathStatus;
    public float PathTimer;
    public int PathLocation;
    public float PathX;
    public float PathZ;

    public float NVX; // NVG overlay position
    public float NVY;
    public float NVZ;
    public string NVName = "";

    public float GravityMult = 1.0f;
    public float MaxGravity = 0.2f;

    public int MTFVariant;
    public NPCs? MTFLeader;

    public int IsDead;
    public float BlinkTimer = 1.0f;
    public int IgnorePlayer;
    public int HP;

    public int ManipulateBone; // IK system
    public int ManipulationType;
    public string BoneToManipulate = "";
    public float BonePitch;
    public float BoneYaw;
    public float BoneRoll;

    public string NPCNameInSection = "";
    public int InFacility = 1;

    public int CanUseElevator;
    public ElevatorObj? CurrElevator;

    public string Model = "";
    public float ModelScaleX;
    public float ModelScaleY;
    public float ModelScaleZ;

    public int HideFromNVG;
    public int TextureID = -1;
    public float CollRadius;
    public float FallingPickDistance;
}
