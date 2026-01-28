namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// NPC type constants.
/// Original: NPCs.bb lines 3-7
/// </summary>
public static class NPCTypes
{
    public const int NPCtype173 = 1;
    public const int NPCtypeOldMan = 2;      // SCP-106
    public const int NPCtypeGuard = 3;
    public const int NPCtypeD = 4;           // D-Class
    public const int NPCtype372 = 6;
    public const int NPCtypeApache = 7;      // Helicopter
    public const int NPCtypeMTF = 8;
    public const int NPCtype096 = 9;
    public const int NPCtype049 = 10;
    public const int NPCtypeZombie = 11;     // SCP-049-2
    public const int NPCtype5131 = 12;       // SCP-513-1
    public const int NPCtypeTentacle = 13;
    public const int NPCtype860 = 14;        // SCP-860-2
    public const int NPCtype939 = 15;
    public const int NPCtype066 = 16;
    public const int NPCtypePdPlane = 17;    // Pocket dimension
    public const int NPCtype966 = 18;
    public const int NPCtype1048a = 19;
    public const int NPCtype1499 = 20;       // SCP-1499-1
    public const int NPCtype008 = 21;
    public const int NPCtypeClerk = 22;
}

/// <summary>
/// NPC data structure - direct port from NPCs.bb Type NPCs.
/// Original: NPCs.bb lines 10-71
/// </summary>
public class NPCs
{
    // Entity references
    public int obj;       // Main mesh
    public int obj2;      // Secondary mesh (eyes, etc)
    public int obj3;      // Tertiary mesh
    public int obj4;      // Quaternary mesh
    public int Collider;  // Collision pivot

    // Identity
    public int NPCtype;
    public int ID;

    // Physics
    public float DropSpeed;
    public int Gravity;

    // State machine
    public float State;
    public float State2;
    public float State3;
    public int PrevState;
    public int MakingNoise;

    // Animation
    public float Frame;

    // Rotation
    public float Angle;

    // Sound
    public int Sound;
    public int SoundChn;
    public float SoundTimer;
    public int Sound2;
    public int SoundChn2;
    public int SoundChn_IsStream;
    public int SoundChn2_IsStream;

    // Movement
    public float Speed;
    public float CurrSpeed;

    // Appearance
    public string texture = "";

    // Idle state
    public float Idle;
    public float IdleTimer;

    // Combat
    public float Reload;

    // Player tracking
    public int LastSeen;
    public float LastDist;

    // Previous position
    public float PrevX;
    public float PrevY;
    public float PrevZ;

    // Targeting
    public NPCs? Target;
    public int TargetID;
    public float EnemyX;
    public float EnemyY;
    public float EnemyZ;

    // Pathfinding
    public WayPoints?[] Path = new WayPoints?[21];
    public int PathStatus;
    public float PathTimer;
    public int PathLocation;
    public float PathX;
    public float PathZ;

    // NVG display name
    public float NVX;
    public float NVY;
    public float NVZ;
    public string NVName = "";

    // Gravity settings
    public float GravityMult = 1.0f;
    public float MaxGravity = 0.2f;

    // MTF specific
    public int MTFVariant;
    public NPCs? MTFLeader;

    // Status
    public int IsDead;
    public float BlinkTimer = 1.0f;
    public int IgnorePlayer;
    public int HP;

    // Bone manipulation
    public int ManipulateBone;
    public int ManipulationType;
    public string BoneToManipulate = "";
    public float BonePitch;
    public float BoneYaw;
    public float BoneRoll;

    // Location
    public string NPCNameInSection = "";
    public int InFacility = 1; // True

    // Elevator
    public int CanUseElevator;
    public ElevatorObj? CurrElevator;

    // Model info
    public string Model = "";
    public float ModelScaleX;
    public float ModelScaleY;
    public float ModelScaleZ;

    // Rendering
    public int HideFromNVG;
    public int TextureID = -1;
    public float CollRadius;
    public float FallingPickDistance;
}
