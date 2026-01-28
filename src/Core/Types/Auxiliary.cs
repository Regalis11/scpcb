namespace SCPContainmentBreach.Core.Types;

// ============================================================================
// WAYPOINTS - Pathfinding
// Original: MapSystem.bb lines 5818-5836
// ============================================================================

public class TempWayPoints
{
    public float x, y, z;
    public RoomTemplates? roomtemplate;
}

public class WayPoints
{
    public int obj;  // Pivot entity
    public Doors? door;
    public Rooms? room;
    public int state;

    // Connected waypoints
    public WayPoints?[] connected = new WayPoints?[6];
    public float[] dist = new float[6];

    // A* pathfinding costs
    public float Fcost;
    public float Gcost;
    public float Hcost;

    // Parent for path reconstruction
    public WayPoints? parent;
}

// ============================================================================
// SECURITY CAMERAS
// Original: MapSystem.bb lines 6284-6308
// ============================================================================

public class SecurityCams
{
    public int obj;
    public int MonitorObj;

    public int BaseObj;
    public int CameraObj;

    // Screen
    public int ScrObj;
    public float ScrWidth;
    public float ScrHeight;
    public int Screen;
    public int Cam;
    public int ScrTexture;
    public int ScrOverlay;

    // Rotation
    public float angle;
    public float turn;
    public float CurrAngle;

    // State
    public float State;
    public int PlayerState;

    // Sound
    public int soundCHN;

    // Visibility
    public int InSight;

    // Rendering
    public float RenderInterval;

    // Room reference
    public Rooms? room;

    // Behavior
    public int FollowPlayer;
    public int CoffinEffect;  // SCP-895
    public int AllowSaving;

    // Angle limits
    public float MinAngle;
    public float MaxAngle;
    public int dir;
}

// ============================================================================
// SCREENS (documents/images)
// Original: MapSystem.bb lines 6222-6227
// ============================================================================

public class Screens
{
    public int obj;
    public string imgpath = "";
    public int img;
    public Rooms? room;
}

public class TempScreens
{
    public string imgpath = "";
    public float x, y, z;
    public RoomTemplates? roomtemplate;
}

// ============================================================================
// PROPS (decorations)
// Original: MapSystem.bb lines 7002-7005
// ============================================================================

public class Props
{
    public string file = "";
    public int obj;
}

// ============================================================================
// LIGHT TEMPLATES
// Original: MapSystem.bb lines 5790-5799
// ============================================================================

public class LightTemplates
{
    public RoomTemplates? roomtemplate;
    public int ltype;
    public float x, y, z;
    public float range;
    public int r, g, b;

    // Spotlight
    public float pitch;
    public float yaw;
    public int innerconeangle;
    public float outerconeangle;
}

// ============================================================================
// PARTICLES
// Original: Particles.bb lines 5-15, 85-101, 198-207
// ============================================================================

public class Particles
{
    public int obj;   // Sprite
    public int pvt;   // Pivot

    public int image;

    // Color
    public float R, G, B, A;
    public float size;

    // Movement
    public float speed;
    public float yspeed;
    public float gravity;

    // Animation
    public float Rchange, Gchange, Bchange, Achange;
    public float SizeChange;

    public float lifetime;
}

public class Emitters
{
    public int Obj;

    public float Size;
    public int MinImage;
    public int MaxImage;
    public float Gravity;
    public int LifeTime;

    public int Disable;

    public Rooms? Room;

    public int SoundCHN;

    public float Speed;
    public float RandAngle;
    public float SizeChange;
    public float Achange;
}

public class DevilEmitters
{
    public int obj;
    public float x, y, z;
    public int particleID;
    public Rooms? room;
    public float timer;
    public float maxtimer;
    public int SoundCHN;
    public int isDeconGas;
}

// ============================================================================
// DIFFICULTY
// Original: Difficulty.bb lines 1-14
// ============================================================================

public static class DifficultyConstants
{
    public const int SAFE = 0;
    public const int EUCLID = 1;
    public const int KETER = 2;
    public const int CUSTOM = 3;

    public const int SAVEANYWHERE = 0;
    public const int SAVEONQUIT = 1;
    public const int SAVEONSCREENS = 2;

    public const int EASY = 0;
    public const int NORMAL = 1;
    public const int HARD = 2;
}

public class Difficulty
{
    public string name = "";
    public string description = "";
    public int permaDeath;
    public int aggressiveNPCs;
    public int saveType;
    public int otherFactors;

    // Color
    public int r;
    public int g;
    public int b;

    public int customizable;
}

// ============================================================================
// MATERIALS (textures)
// Original: MapSystem.bb line 3
// ============================================================================

public class Materials
{
    public string name = "";
    public int diff;   // Diffuse texture
    public int bump;   // Normal map
    // Placeholder - need full definition
}

// ============================================================================
// CONSOLE MESSAGE
// Original: Main.bb lines 378-382
// ============================================================================

public class ConsoleMsg
{
    public string txt = "";
    public int isCommand;
    public int r, g, b;
}

// ============================================================================
// DECALS
// Original: Main.bb line 10756
// ============================================================================

public class Decals
{
    public int obj;
    public float Size;
    public float alpha;
    public float blendmode;
    public float timer;
    public int id;
    // Placeholder - need full definition
}

// ============================================================================
// SCP-427 state
// Original: Main.bb line 2787
// ============================================================================

public class SCP427
{
    public float Timer;
    public int Using;
    // Placeholder - need full definition
}

// ============================================================================
// MAP ZONES
// Original: Main.bb line 2796
// ============================================================================

public class MapZones
{
    public int[] Transition = new int[2];
    public int HasCustomForest;
    public int HasCustomMT;
    // Placeholder - need full definition
}

// ============================================================================
// LOADING SCREENS
// Original: Menu.bb line 1337
// ============================================================================

public class LoadingScreens
{
    public int img;
    public string imgpath = "";
    public string title = "";
    public string txt = "";
    // Placeholder - need full definition
}
