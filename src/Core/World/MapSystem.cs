/*
 * Source: MapSystem.bb
 * Lines: 1637-2100, 1746-1763
 * Type: MapSystem (Room generation)
 */

using System;
using System.Collections.Generic;
using System.IO;
using SCPContainmentBreach.Core.Engine;
using SCPContainmentBreach.Core.Types;

namespace SCPContainmentBreach.Core.World;

public class MapSystem
{
    // RoomScale = 8.0 / 2048.0 (MapSystem.bb:1748)
    public const float RoomScale = 8.0f / 2048.0f;

    public const int ZONEAMOUNT = 3;

    // Map dimensions
    public int MapWidth;
    public int MapHeight;

    // Map grid
    public int[,] MapTemp;
    public int[,] MapFound;

    // Room template storage
    public List<RoomTemplates> RoomTemplatesList = new();
    private int _roomTempID = 0;

    // Room instance storage
    public List<Rooms> RoomsList = new();

    // Room ambience sounds
    public int[] RoomAmbience = new int[20];

    // Global settings
    public float HideDistance = 15.0f;
    public bool SecondaryLightOn = true;
    public bool RemoteDoorOn = true;
    public bool Contained106 = false;

    private string _basePath = "";

    public MapSystem(string basePath = "")
    {
        _basePath = basePath;
        MapWidth = 20;
        MapHeight = 20;
        MapTemp = new int[MapWidth + 2, MapHeight + 2];
        MapFound = new int[MapWidth + 2, MapHeight + 2];
    }

    /// <summary>
    /// Set map dimensions from options
    /// </summary>
    public void SetMapSize(int size)
    {
        MapWidth = size;
        MapHeight = size;
        MapTemp = new int[MapWidth + 2, MapHeight + 2];
        MapFound = new int[MapWidth + 2, MapHeight + 2];
    }

    // ========================================================================
    // CreateRoomTemplate (MapSystem.bb:1637-1646)
    // ========================================================================

    /// <summary>
    /// Create a new room template with mesh path
    /// </summary>
    public RoomTemplates CreateRoomTemplate(string meshPath)
    {
        var rt = new RoomTemplates
        {
            objPath = meshPath,
            id = _roomTempID
        };

        _roomTempID++;
        RoomTemplatesList.Add(rt);

        return rt;
    }

    // ========================================================================
    // LoadRoomTemplates (MapSystem.bb:1648-1705)
    // ========================================================================

    /// <summary>
    /// Load room templates from INI file (Data/rooms.ini)
    /// </summary>
    public void LoadRoomTemplates(string file)
    {
        string fullPath = Path.Combine(_basePath, AssetManager.NormalizePath(file));

        if (!File.Exists(fullPath))
        {
            Console.WriteLine($"[MapSystem] ERROR: rooms.ini not found: {fullPath}");
            return;
        }

        string[] lines = File.ReadAllLines(fullPath);
        var ini = new IniParser(lines);

        string currentSection = "";

        foreach (string rawLine in lines)
        {
            string line = rawLine.Trim();

            // Skip empty lines and comments
            if (string.IsNullOrEmpty(line) || line.StartsWith(";"))
                continue;

            // Section header
            if (line.StartsWith("[") && line.EndsWith("]"))
            {
                currentSection = line.Substring(1, line.Length - 2);

                // Skip "room ambience" section
                if (currentSection.ToLower() == "room ambience")
                    continue;

                // Get mesh path for this room
                string meshPath = ini.GetString(currentSection, "mesh path", "");

                if (string.IsNullOrEmpty(meshPath))
                    continue;

                var rt = CreateRoomTemplate(meshPath);
                rt.Name = currentSection.ToLower();

                // Parse shape
                string shapeStr = ini.GetString(currentSection, "shape", "").ToLower();
                rt.Shape = shapeStr switch
                {
                    "room1" or "1" => RoomShapes.ROOM1,
                    "room2" or "2" => RoomShapes.ROOM2,
                    "room2c" or "2c" => RoomShapes.ROOM2C,
                    "room3" or "3" => RoomShapes.ROOM3,
                    "room4" or "4" => RoomShapes.ROOM4,
                    _ => 0
                };

                // Parse zones (zone1 through zone5)
                for (int i = 0; i < 5; i++)
                {
                    rt.zone[i] = ini.GetInt(currentSection, $"zone{i + 1}", 0);
                }

                // Parse other properties
                rt.Commonness = Math.Clamp(ini.GetInt(currentSection, "commonness", 0), 0, 100);
                rt.Large = ini.GetInt(currentSection, "large", 0);
                rt.DisableDecals = ini.GetInt(currentSection, "disabledecals", 0);
                rt.UseLightCones = ini.GetInt(currentSection, "usevolumelighting", 0);
                rt.DisableOverlapCheck = ini.GetInt(currentSection, "disableoverlapcheck", 0);
            }
        }

        // Load room ambience sounds
        for (int i = 1; i < 20; i++)
        {
            string ambiencePath = ini.GetString("room ambience", $"ambience{i}", "");
            if (string.IsNullOrEmpty(ambiencePath))
                break;

            // RoomAmbience[i] = LoadSound(ambiencePath) - placeholder
            RoomAmbience[i] = i;
        }

        Console.WriteLine($"[MapSystem] Loaded {RoomTemplatesList.Count} room templates");
    }

    // ========================================================================
    // LoadRoomMesh (MapSystem.bb:1708-1722)
    // ========================================================================

    /// <summary>
    /// Load mesh for a room template (lazy loading)
    /// </summary>
    public void LoadRoomMesh(RoomTemplates rt)
    {
        if (rt.obj != 0)
            return; // Already loaded

        string meshPath = AssetManager.NormalizePath(rt.objPath);

        if (meshPath.Contains(".rmesh"))
        {
            // RMesh format - custom room mesh
            rt.obj = LoadRMesh(meshPath, rt);
        }
        else
        {
            // B3D format
            if (!string.IsNullOrEmpty(meshPath))
            {
                rt.obj = LoadWorld(meshPath, rt);
            }
            else
            {
                // CreatePivot equivalent - empty entity
                rt.obj = CreatePivot();
            }
        }

        if (rt.obj == 0)
        {
            Console.WriteLine($"[MapSystem] ERROR: Failed to load room mesh: {meshPath}");
            return;
        }

        CalculateRoomTemplateExtents(rt);

        // Hide template mesh (it's used for copying)
        HideEntity(rt.obj);
    }

    /// <summary>
    /// Load all room meshes at once (with progress callback)
    /// </summary>
    public void LoadRoomMeshes(Action<int>? progressCallback = null)
    {
        int total = RoomTemplatesList.Count;
        int i = 0;

        foreach (var rt in RoomTemplatesList)
        {
            LoadRoomMesh(rt);

            if (progressCallback != null)
            {
                int progress = 30 + (int)(15.0f / total * i);
                progressCallback(progress);
            }
            i++;
        }
    }

    // ========================================================================
    // CreateRoom (MapSystem.bb:2023-2100)
    // ========================================================================

    /// <summary>
    /// Create a room instance at specified position
    /// </summary>
    public Rooms? CreateRoom(int zone, int roomShape, float x, float y, float z, string name = "")
    {
        var r = new Rooms
        {
            zone = zone,
            x = x,
            y = y,
            z = z
        };

        RoomTemplates? selectedTemplate = null;

        // If name specified, find template by name
        if (!string.IsNullOrEmpty(name))
        {
            name = name.ToLower();

            foreach (var rt in RoomTemplatesList)
            {
                if (rt.Name == name)
                {
                    selectedTemplate = rt;
                    break;
                }
            }
        }
        else
        {
            // Random selection based on zone, shape, and commonness
            int totalCommonness = 0;

            foreach (var rt in RoomTemplatesList)
            {
                for (int i = 0; i < 5; i++)
                {
                    if (rt.zone[i] == zone && rt.Shape == roomShape)
                    {
                        totalCommonness += rt.Commonness;
                        break;
                    }
                }
            }

            if (totalCommonness > 0)
            {
                int randomRoom = Random.Shared.Next(1, totalCommonness + 1);
                int accumulated = 0;

                foreach (var rt in RoomTemplatesList)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        if (rt.zone[i] == zone && rt.Shape == roomShape)
                        {
                            accumulated += rt.Commonness;

                            if (randomRoom > accumulated - rt.Commonness && randomRoom <= accumulated)
                            {
                                selectedTemplate = rt;
                                break;
                            }
                            break;
                        }
                    }

                    if (selectedTemplate != null)
                        break;
                }
            }
        }

        if (selectedTemplate == null)
        {
            Console.WriteLine($"[MapSystem] WARNING: No template found for zone={zone} shape={roomShape} name={name}");
            return null;
        }

        // Ensure mesh is loaded
        if (selectedTemplate.obj == 0)
        {
            LoadRoomMesh(selectedTemplate);
        }

        r.RoomTemplate = selectedTemplate;

        // CopyEntity - create instance from template
        r.obj = CopyEntity(selectedTemplate.obj);

        // Scale to RoomScale
        ScaleEntity(r.obj, RoomScale, RoomScale, RoomScale);

        // Set collision type (HIT_MAP)
        EntityType(r.obj, CollisionTypes.HIT_MAP);

        // Set pick mode
        EntityPickMode(r.obj, 2);

        // Position the room
        PositionEntity(r.obj, x, y, z);

        // Fill room with objects/entities
        FillRoom(r);

        // Add light cones if enabled
        if (selectedTemplate.UseLightCones != 0)
        {
            AddLightCones(r);
        }

        // Calculate room extents
        CalculateRoomExtents(r);

        RoomsList.Add(r);
        return r;
    }

    /// <summary>
    /// Set room rotation angle
    /// </summary>
    public void SetRoomAngle(Rooms r, int angle)
    {
        r.angle = angle;
        TurnEntity(r.obj, 0, angle, 0, true);
    }

    // ========================================================================
    // HELPER METHODS (Entity wrappers using BlitzEntity)
    // ========================================================================

    // Entity handle counter
    private static int _entityHandle = 1;
    private static readonly Dictionary<int, BlitzEntity> Entities = new();

    private int CreatePivot()
    {
        var entity = new BlitzEntity();
        int handle = _entityHandle++;
        Entities[handle] = entity;
        return handle;
    }

    private int CopyEntity(int sourceHandle)
    {
        if (!Entities.TryGetValue(sourceHandle, out var source))
        {
            // Create new empty entity if source not found
            return CreatePivot();
        }

        var copy = new BlitzEntity
        {
            Position = source.Position,
            Pitch = source.Pitch,
            Yaw = source.Yaw,
            Roll = source.Roll,
            Scale = source.Scale,
            Visible = true,
            Model = source.Model,
            Texture = source.Texture
        };

        int handle = _entityHandle++;
        Entities[handle] = copy;
        return handle;
    }

    private void HideEntity(int handle)
    {
        if (Entities.TryGetValue(handle, out var entity))
        {
            entity.Visible = false;
        }
    }

    private void ShowEntity(int handle)
    {
        if (Entities.TryGetValue(handle, out var entity))
        {
            entity.Visible = true;
        }
    }

    private void PositionEntity(int handle, float x, float y, float z)
    {
        if (Entities.TryGetValue(handle, out var entity))
        {
            BlitzEntity.PositionEntity(entity, x, y, z);
        }
    }

    private void ScaleEntity(int handle, float x, float y, float z)
    {
        if (Entities.TryGetValue(handle, out var entity))
        {
            BlitzEntity.ScaleEntity(entity, x, y, z);
        }
    }

    private void TurnEntity(int handle, float p, float y, float r, bool global = false)
    {
        if (Entities.TryGetValue(handle, out var entity))
        {
            BlitzEntity.TurnEntity(entity, p, y, r, global);
        }
    }

    private void EntityType(int handle, int collisionType)
    {
        // Collision type storage - placeholder
        // In full implementation, this sets collision layer
    }

    private void EntityPickMode(int handle, int mode)
    {
        // Pick mode storage - placeholder
        // Mode 2 = polygon picking
    }

    // ========================================================================
    // ROOM LOADING (Placeholder stubs - will be implemented in Part 2)
    // ========================================================================

    private int LoadRMesh(string path, RoomTemplates rt)
    {
        // RMesh loader - complex binary format
        // Will be implemented in Phase 4 Part 2
        Console.WriteLine($"[MapSystem] LoadRMesh: {path}");

        var entity = new BlitzEntity();
        // Load model via AssetManager when RMesh parser is ready
        // entity.Model = AssetManager.LoadMesh(path);

        int handle = _entityHandle++;
        Entities[handle] = entity;
        return handle;
    }

    private int LoadWorld(string path, RoomTemplates rt)
    {
        // B3D world loader
        Console.WriteLine($"[MapSystem] LoadWorld: {path}");

        var entity = new BlitzEntity
        {
            Model = AssetManager.LoadMesh(Path.Combine(_basePath, path))
        };

        int handle = _entityHandle++;
        Entities[handle] = entity;
        return handle;
    }

    private void FillRoom(Rooms r)
    {
        // Room-specific content setup
        // Will be fully implemented in Part 2
    }

    private void AddLightCones(Rooms r)
    {
        // Volumetric light cones
        // Will be implemented in Part 2
    }

    private void CalculateRoomExtents(Rooms r)
    {
        // Calculate room bounding box
        // Will be implemented in Part 2
    }

    private void CalculateRoomTemplateExtents(RoomTemplates rt)
    {
        // Calculate template bounding box
        // Will be implemented in Part 2
    }

    // ========================================================================
    // UTILITY: Get entity by handle
    // ========================================================================

    public BlitzEntity? GetEntity(int handle)
    {
        return Entities.TryGetValue(handle, out var entity) ? entity : null;
    }

    public BlitzEntity? GetRoomEntity(Rooms r)
    {
        return GetEntity(r.obj);
    }
}

// ========================================================================
// COLLISION TYPES (Main.bb)
// ========================================================================

public static class CollisionTypes
{
    public const int HIT_MAP = 1;
    public const int HIT_PLAYER = 2;
    public const int HIT_ITEM = 3;
    public const int HIT_APACHE = 4;
    public const int HIT_178 = 5;
    public const int HIT_DVDPLAYER = 6;
}

// ========================================================================
// INI PARSER (Utility)
// ========================================================================

public class IniParser
{
    private readonly Dictionary<string, Dictionary<string, string>> _sections = new();

    public IniParser(string[] lines)
    {
        Parse(lines);
    }

    public IniParser(string filePath)
    {
        if (File.Exists(filePath))
        {
            Parse(File.ReadAllLines(filePath));
        }
    }

    private void Parse(string[] lines)
    {
        string currentSection = "";

        foreach (string rawLine in lines)
        {
            string line = rawLine.Trim();

            // Skip empty and comments
            if (string.IsNullOrEmpty(line) || line.StartsWith(";"))
                continue;

            // Section header
            if (line.StartsWith("[") && line.EndsWith("]"))
            {
                currentSection = line.Substring(1, line.Length - 2).ToLower();
                if (!_sections.ContainsKey(currentSection))
                {
                    _sections[currentSection] = new Dictionary<string, string>();
                }
                continue;
            }

            // Key = Value
            int eqIndex = line.IndexOf('=');
            if (eqIndex > 0 && !string.IsNullOrEmpty(currentSection))
            {
                string key = line.Substring(0, eqIndex).Trim().ToLower();
                string value = line.Substring(eqIndex + 1).Trim();

                _sections[currentSection][key] = value;
            }
        }
    }

    public string GetString(string section, string key, string defaultValue = "")
    {
        section = section.ToLower();
        key = key.ToLower();

        if (_sections.TryGetValue(section, out var sectionData))
        {
            if (sectionData.TryGetValue(key, out var value))
            {
                return value;
            }
        }

        return defaultValue;
    }

    public int GetInt(string section, string key, int defaultValue = 0)
    {
        string value = GetString(section, key, "");

        if (string.IsNullOrEmpty(value))
            return defaultValue;

        // Handle "true"/"false"
        if (value.ToLower() == "true")
            return 1;
        if (value.ToLower() == "false")
            return 0;

        return int.TryParse(value, out int result) ? result : defaultValue;
    }

    public float GetFloat(string section, string key, float defaultValue = 0f)
    {
        string value = GetString(section, key, "");

        if (string.IsNullOrEmpty(value))
            return defaultValue;

        return float.TryParse(value, out float result) ? result : defaultValue;
    }
}
