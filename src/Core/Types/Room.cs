/*
 * Source: MapSystem.bb
 * Lines: 1601-1606
 * Type: Const (Room shapes)
 */

namespace SCPContainmentBreach.Core.Types;

public static class RoomShapes
{
    public const int ROOM1 = 1;
    public const int ROOM2 = 2;
    public const int ROOM2C = 3;
    public const int ROOM3 = 4;
    public const int ROOM4 = 5;
}

public static class RoomConstants
{
    public const int MaxRoomLights = 32;
    public const int MaxRoomEmitters = 8;
    public const int MaxRoomObjects = 30;
    public const int gridsz = 19;
}

/*
 * Source: MapSystem.bb
 * Lines: 1609-1635
 * Type: RoomTemplates
 */

public class RoomTemplates
{
    public int obj;
    public int id;
    public string objPath = "";

    public int[] zone = new int[5];

    public int[] TempSoundEmitter = new int[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterX = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterY = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterZ = new float[RoomConstants.MaxRoomEmitters];
    public float[] TempSoundEmitterRange = new float[RoomConstants.MaxRoomEmitters];

    public int Shape;
    public string Name = "";
    public int Commonness;
    public int Large;
    public int DisableDecals;

    public int TempTriggerboxAmount;
    public int[] TempTriggerbox = new int[129];
    public string[] TempTriggerboxName = new string[129];

    public int UseLightCones;
    public int DisableOverlapCheck = 1;

    public float MinX, MinY, MinZ;
    public float MaxX, MaxY, MaxZ;
}

/*
 * Source: MapSystem.bb
 * Lines: 1765-1822
 * Type: Rooms
 */

public class Rooms
{
    public int zone;
    public int found;

    public int obj;

    public float x, y, z;
    public int angle;

    public RoomTemplates? RoomTemplate;

    public float dist;

    public int SoundCHN;

    public DrawPortal? dp;
    public Forest? fr;

    public int[] SoundEmitter = new int[RoomConstants.MaxRoomEmitters];
    public int[] SoundEmitterObj = new int[RoomConstants.MaxRoomEmitters];
    public float[] SoundEmitterRange = new float[RoomConstants.MaxRoomEmitters];
    public int[] SoundEmitterCHN = new int[RoomConstants.MaxRoomEmitters];

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

    public int[] Objects = new int[RoomConstants.MaxRoomObjects];
    public int[] Levers = new int[12];
    public Doors?[] RoomDoors = new Doors?[8];
    public NPCs?[] NPC = new NPCs?[13];
    public Grids? grid;

    public Rooms?[] Adjacent = new Rooms?[4];
    public Doors?[] AdjDoor = new Doors?[4];

    public int[] NonFreeAble = new int[11];
    public int[] Textures = new int[11];

    public int[] AlarmRotor = new int[2];
    public int[] AlarmRotorLight = new int[2];

    public int TriggerboxAmount;
    public int[] Triggerbox = new int[129];
    public string[] TriggerboxName = new string[129];

    public float MaxWayPointY;

    public float MinX, MinY, MinZ;
    public float MaxX, MaxY, MaxZ;
}

/*
 * Source: MapSystem.bb
 * Lines: 1824-1831
 * Type: Grids
 */

public class Grids
{
    private const int Size = RoomConstants.gridsz * RoomConstants.gridsz;

    public int[] grid = new int[Size];
    public int[] angles = new int[Size];
    public int[] Meshes = new int[8];
    public int[] Entities = new int[Size];
    public WayPoints?[] waypoints = new WayPoints?[Size];
}

/*
 * Source: MapSystem.bb
 * Lines: 831
 * Type: Forest
 */

public class Forest
{
    public int obj;
}

/*
 * Source: DrawPortals.bb
 * Lines: 1
 * Type: DrawPortal
 */

public class DrawPortal
{
    public int obj;
}
