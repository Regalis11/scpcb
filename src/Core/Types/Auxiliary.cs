/*
 * Source: MapSystem.bb
 * Lines: 5818-5836
 * Type: TempWayPoints
 */

namespace SCPContainmentBreach.Core.Types;

public class TempWayPoints
{
    public float x, y, z;
    public RoomTemplates? roomtemplate;
}

/*
 * Source: MapSystem.bb
 * Lines: 5818-5836
 * Type: WayPoints
 */

public class WayPoints
{
    public int obj;
    public Doors? door;
    public Rooms? room;
    public int state;

    public WayPoints?[] connected = new WayPoints?[6];
    public float[] dist = new float[6];

    // A* costs
    public float Fcost;
    public float Gcost;
    public float Hcost;

    public WayPoints? parent;
}

/*
 * Source: MapSystem.bb
 * Lines: 6284-6308
 * Type: SecurityCams
 */

public class SecurityCams
{
    public int obj;
    public int MonitorObj;

    public int BaseObj;
    public int CameraObj;

    public int ScrObj;
    public float ScrWidth;
    public float ScrHeight;
    public int Screen;
    public int Cam;
    public int ScrTexture;
    public int ScrOverlay;

    public float angle;
    public float turn;
    public float CurrAngle;

    public float State;
    public int PlayerState;

    public int soundCHN;

    public int InSight;

    public float RenderInterval;

    public Rooms? room;

    public int FollowPlayer;
    public int CoffinEffect; // SCP-895
    public int AllowSaving;

    public float MinAngle;
    public float MaxAngle;
    public int dir;
}

/*
 * Source: MapSystem.bb
 * Lines: 6222-6227
 * Type: Screens
 */

public class Screens
{
    public int obj;
    public string imgpath = "";
    public int img;
    public Rooms? room;
}

/*
 * Source: MapSystem.bb
 * Lines: 6222-6227
 * Type: TempScreens
 */

public class TempScreens
{
    public string imgpath = "";
    public float x, y, z;
    public RoomTemplates? roomtemplate;
}

/*
 * Source: MapSystem.bb
 * Lines: 7002-7005
 * Type: Props
 */

public class Props
{
    public string file = "";
    public int obj;
}

/*
 * Source: MapSystem.bb
 * Lines: 5790-5799
 * Type: LightTemplates
 */

public class LightTemplates
{
    public RoomTemplates? roomtemplate;
    public int ltype;
    public float x, y, z;
    public float range;
    public int r, g, b;

    public float pitch;
    public float yaw;
    public int innerconeangle;
    public float outerconeangle;
}

/*
 * Source: Particles.bb
 * Lines: 5-15
 * Type: Particles
 */

public class Particles
{
    public int obj;
    public int pvt;

    public int image;

    public float R, G, B, A;
    public float size;

    public float speed;
    public float yspeed;
    public float gravity;

    public float Rchange, Gchange, Bchange, Achange;
    public float SizeChange;

    public float lifetime;
}

/*
 * Source: Particles.bb
 * Lines: 85-101
 * Type: Emitters
 */

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

/*
 * Source: Particles.bb
 * Lines: 198-207
 * Type: DevilEmitters
 */

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

/*
 * Source: Difficulty.bb
 * Lines: 1-14
 * Type: Const (Difficulty)
 */

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

/*
 * Source: Difficulty.bb
 * Lines: 1-14
 * Type: Difficulty
 */

public class Difficulty
{
    public string name = "";
    public string description = "";
    public int permaDeath;
    public int aggressiveNPCs;
    public int saveType;
    public int otherFactors;

    public int r;
    public int g;
    public int b;

    public int customizable;
}

/*
 * Source: MapSystem.bb
 * Lines: 3
 * Type: Materials
 */

public class Materials
{
    public string name = "";
    public int diff;
    public int bump;
}

/*
 * Source: Main.bb
 * Lines: 378-382
 * Type: ConsoleMsg
 */

public class ConsoleMsg
{
    public string txt = "";
    public int isCommand;
    public int r, g, b;
}

/*
 * Source: Main.bb
 * Lines: 10756
 * Type: Decals
 */

public class Decals
{
    public int obj;
    public float Size;
    public float alpha;
    public float blendmode;
    public float timer;
    public int id;
}

/*
 * Source: Main.bb
 * Lines: 2787
 * Type: SCP427
 */

public class SCP427
{
    public float Timer;
    public int Using;
}

/*
 * Source: Main.bb
 * Lines: 2796
 * Type: MapZones
 */

public class MapZones
{
    public int[] Transition = new int[2];
    public int HasCustomForest;
    public int HasCustomMT;
}

/*
 * Source: Menu.bb
 * Lines: 1337
 * Type: LoadingScreens
 */

public class LoadingScreens
{
    public int img;
    public string imgpath = "";
    public string title = "";
    public string txt = "";
}
