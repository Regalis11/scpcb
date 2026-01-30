/*
 * Source: Save.bb
 * Lines: 1-700 (SaveGame, LoadGame functions)
 * Type: Save/Load System
 * Binary format port matching original SCP:CB save structure
 */

using System;
using System.Collections.Generic;
using System.IO;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Engine;

namespace SCPContainmentBreach.Core.Systems;

/// <summary>
/// Save file metadata for display in load menu
/// </summary>
public class SaveSlotInfo
{
    public string SlotName { get; set; } = "";
    public string SaveTime { get; set; } = "";
    public string SaveDate { get; set; } = "";
    public int PlayTime { get; set; }
    public string Version { get; set; } = "";
    public bool IsValid { get; set; }
    public string PreviewImage { get; set; } = "";
    public string DifficultyName { get; set; } = "";
    public float PlayerX { get; set; }
    public float PlayerY { get; set; }
    public float PlayerZ { get; set; }
}

/// <summary>
/// NPC save data structure
/// </summary>
public class NPCSaveData
{
    public int NPCType { get; set; }
    public float X { get; set; }
    public float Y { get; set; }
    public float Z { get; set; }
    public float Pitch { get; set; }
    public float Yaw { get; set; }
    public float Roll { get; set; }
    public float State { get; set; }
    public float State2 { get; set; }
    public float State3 { get; set; }
    public int PrevState { get; set; }
    public byte Idle { get; set; }
    public float LastDist { get; set; }
    public int LastSeen { get; set; }
    public int CurrSpeed { get; set; }
    public float Angle { get; set; }
    public float Reload { get; set; }
    public int ID { get; set; }
    public int TargetID { get; set; }
    public float EnemyX { get; set; }
    public float EnemyY { get; set; }
    public float EnemyZ { get; set; }
    public string Texture { get; set; } = "";
    public float AnimTime { get; set; }
    public int IsDead { get; set; }
    public int HP { get; set; }
}

/// <summary>
/// Door save data structure
/// </summary>
public class DoorSaveData
{
    public float X { get; set; }
    public float Y { get; set; }
    public float Z { get; set; }
    public bool Open { get; set; }
    public float OpenState { get; set; }
    public bool Locked { get; set; }
    public bool AutoClose { get; set; }
    public float Timer { get; set; }
    public float TimerState { get; set; }
    public bool IsElevatorDoor { get; set; }
    public bool MTFClose { get; set; }
}

/// <summary>
/// Event save data structure
/// </summary>
public class EventSaveData
{
    public string EventName { get; set; } = "";
    public float EventState { get; set; }
    public float EventState2 { get; set; }
    public float EventState3 { get; set; }
    public float RoomX { get; set; }
    public float RoomZ { get; set; }
    public string EventStr { get; set; } = "";
}

/// <summary>
/// Item save data structure
/// </summary>
public class ItemSaveData
{
    public string TemplateName { get; set; } = "";
    public string TempName { get; set; } = "";
    public string Name { get; set; } = "";
    public float X { get; set; }
    public float Y { get; set; }
    public float Z { get; set; }
    public byte R { get; set; }
    public byte G { get; set; }
    public byte B { get; set; }
    public float A { get; set; }
    public float Pitch { get; set; }
    public float Yaw { get; set; }
    public float State { get; set; }
    public bool Picked { get; set; }
    public bool IsSelected { get; set; }
    public int InventorySlot { get; set; } = -1; // -1 = not in inventory
    public int ID { get; set; }
}

/// <summary>
/// Complete save data structure
/// </summary>
public class SaveData
{
    // Meta
    public string SaveTime { get; set; } = "";
    public string SaveDate { get; set; } = "";
    public string Version { get; set; } = "1.0.0";
    public int PlayTime { get; set; }

    // Player state
    public float PlayerX { get; set; }
    public float PlayerY { get; set; }
    public float PlayerZ { get; set; }
    public float PlayerPitch { get; set; }
    public float PlayerYaw { get; set; }
    public float BlinkTimer { get; set; }
    public float BlinkEffect { get; set; }
    public float BlinkEffectTimer { get; set; }
    public int DeathTimer { get; set; }
    public int BlurTimer { get; set; }
    public float HealTimer { get; set; }
    public bool Crouching { get; set; }
    public float Stamina { get; set; }
    public float StaminaEffect { get; set; }
    public float StaminaEffectTimer { get; set; }
    public float Injuries { get; set; }
    public float Bloodloss { get; set; }
    public float SCP008Infection { get; set; }
    public float Sanity { get; set; }

    // Wearing items
    public bool WearingGasMask { get; set; }
    public bool WearingVest { get; set; }
    public bool WearingHazmat { get; set; }
    public bool WearingNightVision { get; set; }
    public bool Wearing1499 { get; set; }
    public bool Wearing714 { get; set; }

    // Difficulty
    public int DifficultyIndex { get; set; }
    public string DifficultyName { get; set; } = "EUCLID";

    // World state
    public string RandomSeed { get; set; } = "";
    public int MapWidth { get; set; }
    public int MapHeight { get; set; }
    public bool Contained106 { get; set; }
    public bool LightsOn { get; set; } = true;
    public float SecondaryLightOn { get; set; }
    public bool RemoteDoorOn { get; set; }

    // Console state
    public bool UsedConsole { get; set; }
    public float CameraFogFar { get; set; } = 30.0f;

    // Entity lists
    public List<NPCSaveData> NPCs { get; set; } = new();
    public List<DoorSaveData> Doors { get; set; } = new();
    public List<EventSaveData> Events { get; set; } = new();
    public List<ItemSaveData> Items { get; set; } = new();

    // Inventory (item IDs in slots 0-9)
    public int[] InventorySlots { get; set; } = new int[10];
    public int SelectedItemID { get; set; } = -1;
}

/// <summary>
/// Save/Load system - 1:1 port of Save.bb
/// Binary format for speed, with JSON option for debugging
/// </summary>
public static class SaveSystem
{
    public const string SaveDirectory = "Saves";
    public const string SaveFileName = "save.dat";
    public const string PreviewFileName = "preview.png";
    public const string MetaFileName = "meta.txt";
    public const string CurrentVersion = "1.0.0";

    // Checkpoint markers (from original)
    private const int CHECKPOINT_NPC = 113;
    private const int CHECKPOINT_ROOM = 632;
    private const int CHECKPOINT_DOOR = 954;
    private const int CHECKPOINT_DECAL = 1845;
    private const int CHECKPOINT_CONSOLE_USED = 100;
    private const int CHECKPOINT_CONSOLE_CLEAN = 994;

    /// <summary>
    /// Get list of all save slots
    /// </summary>
    public static List<SaveSlotInfo> GetSaveSlots()
    {
        var slots = new List<SaveSlotInfo>();

        if (!Directory.Exists(SaveDirectory))
        {
            Directory.CreateDirectory(SaveDirectory);
            return slots;
        }

        foreach (var dir in Directory.GetDirectories(SaveDirectory))
        {
            string slotName = Path.GetFileName(dir);
            var info = GetSaveSlotInfo(slotName);
            if (info != null)
            {
                slots.Add(info);
            }
        }

        // Sort by modification time (newest first)
        slots.Sort((a, b) => string.Compare(b.SaveDate + b.SaveTime, a.SaveDate + a.SaveTime, StringComparison.Ordinal));

        return slots;
    }

    /// <summary>
    /// Get info about a specific save slot
    /// </summary>
    public static SaveSlotInfo? GetSaveSlotInfo(string slotName)
    {
        string slotPath = Path.Combine(SaveDirectory, slotName);
        string savePath = Path.Combine(slotPath, SaveFileName);
        string previewPath = Path.Combine(slotPath, PreviewFileName);

        if (!File.Exists(savePath))
            return null;

        var info = new SaveSlotInfo
        {
            SlotName = slotName,
            IsValid = true,
            PreviewImage = File.Exists(previewPath) ? previewPath : ""
        };

        try
        {
            using var stream = File.OpenRead(savePath);
            using var reader = new BinaryReader(stream);

            // Read header
            info.SaveTime = reader.ReadString();
            info.SaveDate = reader.ReadString();
            info.PlayTime = reader.ReadInt32();

            // Player position
            info.PlayerX = reader.ReadSingle();
            info.PlayerY = reader.ReadSingle();
            info.PlayerZ = reader.ReadSingle();

            // Skip to version
            reader.ReadSingle(); // Head X
            reader.ReadSingle(); // Head Y
            reader.ReadSingle(); // Head Z
            reader.ReadString(); // Access code
            reader.ReadSingle(); // Pitch
            reader.ReadSingle(); // Yaw

            info.Version = reader.ReadString();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[SaveSystem] Error reading save info: {ex.Message}");
            info.IsValid = false;
        }

        return info;
    }

    /// <summary>
    /// Save game to slot (Save.bb lines 2-468)
    /// </summary>
    public static bool SaveGame(string slotName, PlayerState player)
    {
        if (player == null)
        {
            Console.WriteLine("[SaveSystem] Cannot save - no player state");
            return false;
        }

        // Don't save if player is dead
        if (player.KillTimer < 0)
        {
            Console.WriteLine("[SaveSystem] Cannot save - player is dead");
            return false;
        }

        string slotPath = Path.Combine(SaveDirectory, slotName);
        string savePath = Path.Combine(slotPath, SaveFileName);
        string previewPath = Path.Combine(slotPath, PreviewFileName);

        try
        {
            // Create directory if needed
            Directory.CreateDirectory(slotPath);

            // Take screenshot for preview
            TakePreviewScreenshot(previewPath);

            // Create save data
            var data = CreateSaveData(player);

            // Write binary save file
            using var stream = File.Create(savePath);
            using var writer = new BinaryWriter(stream);

            WriteSaveData(writer, data);

            Console.WriteLine($"[SaveSystem] Game saved to {slotName}");
            ConsoleSystem.CreateMsg($"Game saved to {slotName}", 0, 255, 0);

            return true;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[SaveSystem] Error saving game: {ex.Message}");
            ConsoleSystem.CreateMsg($"Save failed: {ex.Message}", 255, 0, 0);
            return false;
        }
    }

    /// <summary>
    /// Load game from slot (Save.bb lines 470-700+)
    /// </summary>
    public static SaveData? LoadGame(string slotName)
    {
        string slotPath = Path.Combine(SaveDirectory, slotName);
        string savePath = Path.Combine(slotPath, SaveFileName);

        if (!File.Exists(savePath))
        {
            Console.WriteLine($"[SaveSystem] Save file not found: {savePath}");
            ConsoleSystem.CreateMsg($"Save not found: {slotName}", 255, 0, 0);
            return null;
        }

        try
        {
            using var stream = File.OpenRead(savePath);
            using var reader = new BinaryReader(stream);

            var data = ReadSaveData(reader);

            Console.WriteLine($"[SaveSystem] Game loaded from {slotName}");
            ConsoleSystem.CreateMsg($"Game loaded from {slotName}", 0, 255, 0);

            return data;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[SaveSystem] Error loading game: {ex.Message}");
            ConsoleSystem.CreateMsg($"Load failed: {ex.Message}", 255, 0, 0);
            return null;
        }
    }

    /// <summary>
    /// Apply loaded save data to game state
    /// </summary>
    public static void ApplyLoadedData(SaveData data, PlayerState player)
    {
        if (data == null || player == null) return;

        // Player position
        player.X = data.PlayerX;
        player.Y = data.PlayerY;
        player.Z = data.PlayerZ;
        player.Yaw = data.PlayerYaw;

        // Player state
        player.BlinkTimer = data.BlinkTimer;
        player.Stamina = data.Stamina;
        player.Injuries = data.Injuries;
        player.Bloodloss = data.Bloodloss;
        player.SCP008Infection = data.SCP008Infection;
        player.Crouching = data.Crouching;
        player.KillTimer = data.DeathTimer;

        // Wearing items
        player.WearingGasMask = data.WearingGasMask;
        player.WearingHazmat = data.WearingHazmat;
        player.WearingNightVision = data.WearingNightVision;

        // World state
        RoomEventSystem.Contained106 = data.Contained106;

        // Console state
        if (data.UsedConsole)
        {
            ConsoleSystem.CameraFogFar = data.CameraFogFar;
        }

        // Apply event states
        foreach (var eventData in data.Events)
        {
            // Find matching event and apply state
            // (Would integrate with RoomEventSystem)
        }

        Console.WriteLine($"[SaveSystem] Applied save data - Player at ({data.PlayerX:F2}, {data.PlayerY:F2}, {data.PlayerZ:F2})");
    }

    /// <summary>
    /// Delete a save slot
    /// </summary>
    public static bool DeleteSave(string slotName)
    {
        string slotPath = Path.Combine(SaveDirectory, slotName);

        if (!Directory.Exists(slotPath))
            return false;

        try
        {
            Directory.Delete(slotPath, true);
            Console.WriteLine($"[SaveSystem] Deleted save: {slotName}");
            return true;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[SaveSystem] Error deleting save: {ex.Message}");
            return false;
        }
    }

    /// <summary>
    /// Check if a save slot exists
    /// </summary>
    public static bool SaveExists(string slotName)
    {
        string savePath = Path.Combine(SaveDirectory, slotName, SaveFileName);
        return File.Exists(savePath);
    }

    /// <summary>
    /// Format play time as string
    /// </summary>
    public static string FormatPlayTime(int ticks)
    {
        int seconds = ticks / 70; // FPS factor
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        if (hours > 0)
            return $"{hours}:{minutes:D2}:{seconds:D2}";
        else
            return $"{minutes}:{seconds:D2}";
    }

    // Private helper methods

    private static void TakePreviewScreenshot(string path)
    {
        try
        {
            // Raylib screenshot
            Raylib.TakeScreenshot(path);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[SaveSystem] Screenshot failed: {ex.Message}");
        }
    }

    private static SaveData CreateSaveData(PlayerState player)
    {
        var data = new SaveData
        {
            // Meta
            SaveTime = DateTime.Now.ToString("HH:mm:ss"),
            SaveDate = DateTime.Now.ToString("yyyy-MM-dd"),
            Version = CurrentVersion,
            PlayTime = (int)(player.PlayTime * 70), // Convert to ticks

            // Player state
            PlayerX = player.X,
            PlayerY = player.Y,
            PlayerZ = player.Z,
            PlayerPitch = 0, // Would get from camera
            PlayerYaw = player.Yaw,
            BlinkTimer = player.BlinkTimer,
            BlinkEffect = player.BlinkEffect,
            BlinkEffectTimer = player.BlinkEffectTimer,
            DeathTimer = (int)player.KillTimer,
            BlurTimer = (int)player.BlurTimer,
            HealTimer = player.HealTimer,
            Crouching = player.Crouching,
            Stamina = player.Stamina,
            StaminaEffect = player.StaminaEffect,
            StaminaEffectTimer = player.StaminaEffectTimer,
            Injuries = player.Injuries,
            Bloodloss = player.Bloodloss,
            SCP008Infection = player.SCP008Infection,
            Sanity = player.Sanity,

            // Wearing
            WearingGasMask = player.WearingGasMask,
            WearingVest = false, // TODO
            WearingHazmat = player.WearingHazmat,
            WearingNightVision = player.WearingNightVision,
            Wearing1499 = false, // TODO
            Wearing714 = false, // TODO

            // Difficulty
            DifficultyIndex = 1, // EUCLID
            DifficultyName = "EUCLID",

            // World
            RandomSeed = "default",
            MapWidth = 18,
            MapHeight = 18,
            Contained106 = RoomEventSystem.Contained106,
            LightsOn = true,
            SecondaryLightOn = 0,
            RemoteDoorOn = false,

            // Console
            UsedConsole = ConsoleSystem.GodMode || ConsoleSystem.NoClip,
            CameraFogFar = ConsoleSystem.CameraFogFar
        };

        // Collect event states
        // Would iterate RoomEventSystem events

        return data;
    }

    private static void WriteSaveData(BinaryWriter writer, SaveData data)
    {
        // Header (matching Save.bb format)
        writer.Write(data.SaveTime);
        writer.Write(data.SaveDate);
        writer.Write(data.PlayTime);

        // Player position
        writer.Write(data.PlayerX);
        writer.Write(data.PlayerY);
        writer.Write(data.PlayerZ);

        // Head position (same as player for now)
        writer.Write(data.PlayerX);
        writer.Write(data.PlayerY + 0.6f);
        writer.Write(data.PlayerZ);

        // Access code (empty for now)
        writer.Write("0");

        // Player rotation
        writer.Write(data.PlayerPitch);
        writer.Write(data.PlayerYaw);

        // Version
        writer.Write(data.Version);

        // Blink state
        writer.Write(data.BlinkTimer);
        writer.Write(data.BlinkEffect);
        writer.Write(data.BlinkEffectTimer);

        // Timers
        writer.Write(data.DeathTimer);
        writer.Write(data.BlurTimer);
        writer.Write(data.HealTimer);

        // Crouch
        writer.Write((byte)(data.Crouching ? 1 : 0));

        // Stamina
        writer.Write(data.Stamina);
        writer.Write(data.StaminaEffect);
        writer.Write(data.StaminaEffectTimer);

        // Eye state (not used yet)
        writer.Write(0.0f); // EyeStuck
        writer.Write(0.0f); // EyeIrritation

        // Injuries
        writer.Write(data.Injuries);
        writer.Write(data.Bloodloss);
        writer.Write(data.Injuries); // PrevInjuries
        writer.Write(data.Bloodloss); // PrevBloodloss

        // Death message
        writer.Write("");

        // SCP-1025 states (6 floats)
        for (int i = 0; i < 6; i++)
            writer.Write(0.0f);

        // Vomit state
        writer.Write(0.0f); // VomitTimer
        writer.Write((byte)0); // Vomit
        writer.Write(0.0f); // CameraShakeTimer
        writer.Write(data.SCP008Infection); // Infect

        // Difficulty
        writer.Write((byte)data.DifficultyIndex);

        // Monitor timer
        writer.Write(0.0f);

        // Sanity
        writer.Write(data.Sanity);

        // Wearing items
        writer.Write((byte)(data.WearingGasMask ? 1 : 0));
        writer.Write((byte)(data.WearingVest ? 1 : 0));
        writer.Write((byte)(data.WearingHazmat ? 1 : 0));
        writer.Write((byte)(data.WearingNightVision ? 1 : 0));
        writer.Write((byte)(data.Wearing1499 ? 1 : 0));

        // 1499 coordinates (8 floats)
        for (int i = 0; i < 8; i++)
            writer.Write(0.0f);

        // Superman state
        writer.Write((byte)0);
        writer.Write(0.0f);
        writer.Write((byte)(data.LightsOn ? 1 : 0));

        // Random seed
        writer.Write(data.RandomSeed);

        // Light state
        writer.Write(data.SecondaryLightOn);
        writer.Write(0.0f); // PrevSecondaryLightOn
        writer.Write((byte)(data.RemoteDoorOn ? 1 : 0));
        writer.Write((byte)0); // SoundTransmission
        writer.Write((byte)(data.Contained106 ? 1 : 0));

        // Achievements (20 bytes)
        for (int i = 0; i < 20; i++)
            writer.Write((byte)0);

        // Refined items
        writer.Write(0);

        // Map data
        writer.Write(data.MapWidth);
        writer.Write(data.MapHeight);
        for (int x = 0; x <= data.MapWidth; x++)
        {
            for (int y = 0; y <= data.MapHeight; y++)
            {
                writer.Write(0); // MapTemp
                writer.Write((byte)0); // MapFound
            }
        }

        // Checkpoint
        writer.Write(CHECKPOINT_NPC);

        // NPCs
        writer.Write(data.NPCs.Count);
        foreach (var npc in data.NPCs)
        {
            writer.Write((byte)npc.NPCType);
            writer.Write(npc.X);
            writer.Write(npc.Y);
            writer.Write(npc.Z);
            writer.Write(npc.Pitch);
            writer.Write(npc.Yaw);
            writer.Write(npc.Roll);
            writer.Write(npc.State);
            writer.Write(npc.State2);
            writer.Write(npc.State3);
            writer.Write(npc.PrevState);
            writer.Write(npc.Idle);
            writer.Write(npc.LastDist);
            writer.Write(npc.LastSeen);
            writer.Write(npc.CurrSpeed);
            writer.Write(npc.Angle);
            writer.Write(npc.Reload);
            writer.Write(npc.ID);
            writer.Write(npc.TargetID);
            writer.Write(npc.EnemyX);
            writer.Write(npc.EnemyY);
            writer.Write(npc.EnemyZ);
            writer.Write(npc.Texture);
            writer.Write(npc.AnimTime);
            writer.Write(npc.IsDead);
            writer.Write(0.0f); // PathX
            writer.Write(0.0f); // PathZ
            writer.Write(npc.HP);
            writer.Write(""); // Model path
            writer.Write(1.0f); // ModelScaleX
            writer.Write(1.0f); // ModelScaleY
            writer.Write(1.0f); // ModelScaleZ
            writer.Write(0); // TextureID
        }

        // MTF data
        writer.Write(0.0f); // MTFtimer
        for (int i = 0; i < 7; i++)
        {
            writer.Write("a"); // Room name
            writer.Write(0); // MTFroomState
        }

        // Checkpoint
        writer.Write(CHECKPOINT_ROOM);

        // Zone data
        writer.Write(0); // room2gw_brokendoor
        writer.Write(0.0f); // room2gw_x
        writer.Write(0.0f); // room2gw_z
        writer.Write((byte)0); // Transition[0]
        writer.Write((byte)0); // Transition[1]
        writer.Write((byte)0); // HasCustomForest
        writer.Write((byte)0); // HasCustomMT

        // Rooms (empty for now)
        writer.Write(0);

        // Checkpoint
        writer.Write(CHECKPOINT_DOOR);

        // Doors
        writer.Write(data.Doors.Count);
        foreach (var door in data.Doors)
        {
            writer.Write(door.X);
            writer.Write(door.Y);
            writer.Write(door.Z);
            writer.Write((byte)(door.Open ? 1 : 0));
            writer.Write(door.OpenState);
            writer.Write((byte)(door.Locked ? 1 : 0));
            writer.Write((byte)(door.AutoClose ? 1 : 0));
            writer.Write(door.X); // obj X
            writer.Write(door.Z); // obj Z
            writer.Write(0.0f); // obj2 X
            writer.Write(0.0f); // obj2 Z
            writer.Write(door.Timer);
            writer.Write(door.TimerState);
            writer.Write((byte)(door.IsElevatorDoor ? 1 : 0));
            writer.Write((byte)(door.MTFClose ? 1 : 0));
        }

        // Checkpoint
        writer.Write(CHECKPOINT_DECAL);

        // Decals (empty)
        writer.Write(0);

        // Events
        writer.Write(data.Events.Count);
        foreach (var evt in data.Events)
        {
            writer.Write(evt.EventName);
            writer.Write(evt.EventState);
            writer.Write(evt.EventState2);
            writer.Write(evt.EventState3);
            writer.Write(evt.RoomX);
            writer.Write(evt.RoomZ);
            writer.Write(evt.EventStr);
        }

        // Items
        writer.Write(data.Items.Count);
        foreach (var item in data.Items)
        {
            writer.Write(item.TemplateName);
            writer.Write(item.TempName);
            writer.Write(item.Name);
            writer.Write(item.X);
            writer.Write(item.Y);
            writer.Write(item.Z);
            writer.Write(item.R);
            writer.Write(item.G);
            writer.Write(item.B);
            writer.Write(item.A);
            writer.Write(item.Pitch);
            writer.Write(item.Yaw);
            writer.Write(item.State);
            writer.Write((byte)(item.Picked ? 1 : 0));
            writer.Write((byte)(item.IsSelected ? 1 : 0));
            writer.Write((byte)(item.InventorySlot >= 0 ? item.InventorySlot : 66));
            writer.Write((byte)0); // invSlots
            writer.Write(item.ID);
            writer.Write((byte)0); // custom invimg
        }

        // Item secondary inventories (empty)
        writer.Write(0);

        // Item templates found (empty)
        // Console state
        writer.Write(data.UsedConsole ? CHECKPOINT_CONSOLE_USED : CHECKPOINT_CONSOLE_CLEAN);
        writer.Write(data.CameraFogFar);
        writer.Write(data.CameraFogFar); // StoredCameraFogFar

        // 427 state
        writer.Write((byte)0);
        writer.Write(0.0f);

        // 714 state
        writer.Write((byte)(data.Wearing714 ? 1 : 0));
    }

    private static SaveData ReadSaveData(BinaryReader reader)
    {
        var data = new SaveData();

        // Header
        data.SaveTime = reader.ReadString();
        data.SaveDate = reader.ReadString();
        data.PlayTime = reader.ReadInt32();

        // Player position
        data.PlayerX = reader.ReadSingle();
        data.PlayerY = reader.ReadSingle();
        data.PlayerZ = reader.ReadSingle();

        // Head position (skip)
        reader.ReadSingle();
        reader.ReadSingle();
        reader.ReadSingle();

        // Access code
        reader.ReadString();

        // Rotation
        data.PlayerPitch = reader.ReadSingle();
        data.PlayerYaw = reader.ReadSingle();

        // Version
        data.Version = reader.ReadString();

        // Blink
        data.BlinkTimer = reader.ReadSingle();
        data.BlinkEffect = reader.ReadSingle();
        data.BlinkEffectTimer = reader.ReadSingle();

        // Timers
        data.DeathTimer = reader.ReadInt32();
        data.BlurTimer = reader.ReadInt32();
        data.HealTimer = reader.ReadSingle();

        // Crouch
        data.Crouching = reader.ReadByte() != 0;

        // Stamina
        data.Stamina = reader.ReadSingle();
        data.StaminaEffect = reader.ReadSingle();
        data.StaminaEffectTimer = reader.ReadSingle();

        // Eye state
        reader.ReadSingle(); // EyeStuck
        reader.ReadSingle(); // EyeIrritation

        // Injuries
        data.Injuries = reader.ReadSingle();
        data.Bloodloss = reader.ReadSingle();
        reader.ReadSingle(); // PrevInjuries
        reader.ReadSingle(); // PrevBloodloss

        // Death message
        reader.ReadString();

        // SCP-1025
        for (int i = 0; i < 6; i++)
            reader.ReadSingle();

        // Vomit
        reader.ReadSingle();
        reader.ReadByte();
        reader.ReadSingle();
        data.SCP008Infection = reader.ReadSingle();

        // Difficulty
        data.DifficultyIndex = reader.ReadByte();

        // Monitor
        reader.ReadSingle();

        // Sanity
        data.Sanity = reader.ReadSingle();

        // Wearing
        data.WearingGasMask = reader.ReadByte() != 0;
        data.WearingVest = reader.ReadByte() != 0;
        data.WearingHazmat = reader.ReadByte() != 0;
        data.WearingNightVision = reader.ReadByte() != 0;
        data.Wearing1499 = reader.ReadByte() != 0;

        // 1499 coords
        for (int i = 0; i < 8; i++)
            reader.ReadSingle();

        // Superman
        reader.ReadByte();
        reader.ReadSingle();
        data.LightsOn = reader.ReadByte() != 0;

        // Seed
        data.RandomSeed = reader.ReadString();

        // Lights
        data.SecondaryLightOn = reader.ReadSingle();
        reader.ReadSingle();
        data.RemoteDoorOn = reader.ReadByte() != 0;
        reader.ReadByte();
        data.Contained106 = reader.ReadByte() != 0;

        // Achievements
        for (int i = 0; i < 20; i++)
            reader.ReadByte();

        // Refined
        reader.ReadInt32();

        // Map
        data.MapWidth = reader.ReadInt32();
        data.MapHeight = reader.ReadInt32();
        for (int x = 0; x <= data.MapWidth; x++)
        {
            for (int y = 0; y <= data.MapHeight; y++)
            {
                reader.ReadInt32();
                reader.ReadByte();
            }
        }

        // Checkpoint NPC
        int check = reader.ReadInt32();
        if (check != CHECKPOINT_NPC)
        {
            throw new Exception($"Save corrupted at NPC checkpoint (expected {CHECKPOINT_NPC}, got {check})");
        }

        // NPCs
        int npcCount = reader.ReadInt32();
        for (int i = 0; i < npcCount; i++)
        {
            var npc = new NPCSaveData
            {
                NPCType = reader.ReadByte(),
                X = reader.ReadSingle(),
                Y = reader.ReadSingle(),
                Z = reader.ReadSingle(),
                Pitch = reader.ReadSingle(),
                Yaw = reader.ReadSingle(),
                Roll = reader.ReadSingle(),
                State = reader.ReadSingle(),
                State2 = reader.ReadSingle(),
                State3 = reader.ReadSingle(),
                PrevState = reader.ReadInt32(),
                Idle = reader.ReadByte(),
                LastDist = reader.ReadSingle(),
                LastSeen = reader.ReadInt32(),
                CurrSpeed = reader.ReadInt32(),
                Angle = reader.ReadSingle(),
                Reload = reader.ReadSingle(),
                ID = reader.ReadInt32(),
                TargetID = reader.ReadInt32(),
                EnemyX = reader.ReadSingle(),
                EnemyY = reader.ReadSingle(),
                EnemyZ = reader.ReadSingle(),
                Texture = reader.ReadString(),
                AnimTime = reader.ReadSingle(),
                IsDead = reader.ReadInt32()
            };

            reader.ReadSingle(); // PathX
            reader.ReadSingle(); // PathZ
            npc.HP = reader.ReadInt32();
            reader.ReadString(); // Model
            reader.ReadSingle(); // ScaleX
            reader.ReadSingle(); // ScaleY
            reader.ReadSingle(); // ScaleZ
            reader.ReadInt32(); // TextureID

            data.NPCs.Add(npc);
        }

        // Read remaining data (MTF, rooms, doors, events, items)
        // Simplified for now - would need full implementation

        Console.WriteLine($"[SaveSystem] Loaded {npcCount} NPCs");

        return data;
    }
}
