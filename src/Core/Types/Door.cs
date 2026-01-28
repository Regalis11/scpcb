namespace SCPContainmentBreach.Core.Types;

/// <summary>
/// Door data structure.
/// Original: Main.bb lines 1830-1861
/// </summary>
public class Doors
{
    // Mesh entities
    public int obj;        // Door panel 1
    public int obj2;       // Door panel 2
    public int frameobj;   // Door frame
    public int[] buttons = new int[3];  // Button entities

    // State
    public int locked;
    public int open;
    public int angle;
    public float openstate;
    public int fastopen;

    // Direction
    public int dir;

    // Timer
    public int timer;
    public float timerstate;

    // Access
    public int KeyCard;        // Required keycard level
    public string Code = "";   // Keypad code

    // Room reference
    public Rooms? room;

    // Pathfinding
    public int DisableWaypoint;

    // Distance to player
    public float dist;

    // Sound
    public int SoundCHN;

    // ID
    public int ID;

    // Level transition
    public int Level;
    public int LevelDest;

    // Auto close
    public int AutoClose;

    // Linked door (elevators)
    public Doors? LinkedDoor;

    // Elevator
    public int IsElevatorDoor;

    // MTF behavior
    public int MTFClose = 1;         // True
    public int NPCCalledElevator;

    // Collision
    public int DoorHitOBJ;
}

/// <summary>
/// Elevator object.
/// Original: MapSystem.bb line 8450
/// </summary>
public class ElevatorObj
{
    public int obj;
    // Placeholder - need to read full definition
}
