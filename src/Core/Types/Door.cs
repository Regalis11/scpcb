/*
 * Source: Main.bb
 * Lines: 1830-1861
 * Type: Doors
 */

namespace SCPContainmentBreach.Core.Types;

public class Doors
{
    public int obj;
    public int obj2;
    public int frameobj;
    public int[] buttons = new int[3];

    public int locked;
    public int open;
    public int angle;
    public float openstate;
    public int fastopen;

    public int dir;

    public int timer;
    public float timerstate;

    public int KeyCard;
    public string Code = "";

    public Rooms? room;

    public int DisableWaypoint;

    public float dist;

    public int SoundCHN;

    public int ID;

    public int Level;
    public int LevelDest;

    public int AutoClose;

    public Doors? LinkedDoor;

    public int IsElevatorDoor;

    public int MTFClose = 1;
    public int NPCCalledElevator;

    public int DoorHitOBJ;
}

/*
 * Source: MapSystem.bb
 * Lines: 8450
 * Type: ElevatorObj
 */

public class ElevatorObj
{
    public int obj;
}
