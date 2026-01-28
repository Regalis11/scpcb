namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// Room shape constants.
/// Original: MapSystem.bb line 1606
/// </summary>
public static class RoomShapes
{
    public const int ROOM1 = 1;   // End cap
    public const int ROOM2 = 2;   // Corridor
    public const int ROOM2C = 3;  // Corner
    public const int ROOM3 = 4;   // T-junction
    public const int ROOM4 = 5;   // Crossroads
}

/// <summary>
/// Room array size constants.
/// Original: MapSystem.bb lines 1601-1603
/// </summary>
public static class RoomConstants
{
    public const int MaxRoomLights = 32;
    public const int MaxRoomEmitters = 8;
    public const int MaxRoomObjects = 30;
    public const int gridsz = 19;
}

/// <summary>
/// Room template - shared data for rooms of same type.
/// Original: MapSystem.bb lines 1609-1635
/// </summary>
public class RoomTemplates
{
    public int obj;       // Template mesh
    public int id;
    public string objPath = "";

    // Zone availability
    public int[] zone = new int[5];

    // Sound emitters template data
    public int[] TempSoundEmitter = new int[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterX = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterY = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterZ = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterRange = new float[RoomConstants.MaxRoomEmitters];

    // Room properties
    public int Shape;
    public string Name = "";
    public int Commonness;
    public int Large;
    public int DisableDecals;

    // Trigger boxes
    public int TempTriggerboxAmount;
    public int[] TempTriggerbox = new int[129];
    public string[] TempTriggerboxName = new string[129];

    // Lighting
    public int UseLightCones;

    // Overlap
    public int DisableOverlapCheck = 1; // True

    // Bounding box
    public float MinX, MinY, MinZ;
    public float MaxX, MaxY, MaxZ;
}

/// <summary>
/// Room instance - placed room in the map.
/// Original: MapSystem.bb lines 1765-1822
/// </summary>
public class Rooms
{
    public int zone;
    public int found;

    // Entity
    public int obj;

    // Position
    public float x, y, z;
    public int angle;

    // Template reference
    public RoomTemplates? RoomTemplate;

    // Distance to player
    public float dist;

    // Sound
    public int SoundCHN;

    // Portal/Forest links
    public DrawPortal? dp;
    public Forest? fr;

    // Sound emitters
    public int[] SoundEmitter = new int[RoomConstants.MaxRoomEmitters];
    public int[] SoundEmitterObj = new int[RoomConstants.MaxRoomEmitters];
    public float[] SoundEmitterRange = new float[RoomConstants.MaxRoomEmitters];
    public int[] SoundEmitterCHN = new int[RoomConstants.MaxRoomEmitters];

    // Lights
    public int[] Lights = new int[RoomConstants.MaxRoomLights];
    public float[] LightIntensity = new float[RoomConstants.MaxRoomLights];
    public int[] LightSprites = new int[RoomConstants.MaxRoomLights];
    public int MaxLights;
    public int[] LightSpriteHidden = new int[RoomConstants.MaxRoomLights];
    public int[] LightSpritesPivot = new int[RoomConstants.MaxRoomLights];
    public int[] LightSprites2 = new int[RoomConstants.MaxRoomLights];
    public int[] LightHidden = new int[RoomConstants.MaxRoomLights];
    public int[] LightFlicker = new int[RoomConstants.MaxRoomLights];
    public float[] LightR = new float[RoomConstants.MaxRoomLights];
    public float[] LightG = new float[RoomConstants.MaxRoomLights];
    public float[] LightB = new float[RoomConstants.MaxRoomLights];
    public int[] LightCone = new int[RoomConstants.MaxRoomLights];
    public int[] LightConeSpark = new int[RoomConstants.MaxRoomLights];
    public float[] LightConeSparkTimer = new float[RoomConstants.MaxRoomLights];

    // Objects in room
    public int[] Objects = new int[RoomConstants.MaxRoomObjects];
    public int[] Levers = new int[12];
    public Doors?[] RoomDoors = new Doors?[8];
    public NPCs?[] NPC = new NPCs?[13];
    public Grids? grid;

    // Adjacency
    public Rooms?[] Adjacent = new Rooms?[4];
    public Doors?[] AdjDoor = new Doors?[4];

    // Resources
    public int[] NonFreeAble = new int[11];
    public int[] Textures = new int[11];

    // Alarms
    public int[] AlarmRotor = new int[2];
    public int[] AlarmRotorLight = new int[2];

    // Trigger boxes
    public int TriggerboxAmount;
    public int[] Triggerbox = new int[129];
    public string[] TriggerboxName = new string[129];

    // Waypoint height
    public float MaxWayPointY;

    // Bounding box
    public float MinX, MinY, MinZ;
    public float MaxX, MaxY, MaxZ;
}

/// <summary>
/// Grid for tunnel/corridor system.
/// Original: MapSystem.bb lines 1825-1831
/// </summary>
public class Grids
{
    private const int Size = RoomConstants.gridsz * RoomConstants.gridsz;

    public int[] grid = new int[Size];
    public int[] angles = new int[Size];
    public int[] Meshes = new int[8];
    public int[] Entities = new int[Size];
    public WayPoints?[] waypoints = new WayPoints?[Size];
}

/// <summary>
/// Forest (SCP-860 dimension).
/// Original: MapSystem.bb line 831
/// </summary>
public class Forest
{
    // Placeholder - need to read full definition
    public int obj;
}

/// <summary>
/// Draw portal for room transitions.
/// Original: DrawPortals.bb line 1
/// </summary>
public class DrawPortal
{
    // Placeholder - need to read full definition
    public int obj;
}
