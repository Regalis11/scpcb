/*
 * Source: Main.bb (Options loading/saving)
 * Lines: 100-200 (INI file operations)
 * Type: Configuration System
 * Handles game options, key bindings, and persistent settings
 */

using System;
using System.Collections.Generic;
using System.IO;
using Raylib_cs;

namespace SCPContainmentBreach.Core.Systems;

/// <summary>
/// Key binding configuration
/// </summary>
public class KeyBindings
{
    public KeyboardKey MoveForward { get; set; } = KeyboardKey.W;
    public KeyboardKey MoveBackward { get; set; } = KeyboardKey.S;
    public KeyboardKey StrafeLeft { get; set; } = KeyboardKey.A;
    public KeyboardKey StrafeRight { get; set; } = KeyboardKey.D;
    public KeyboardKey Sprint { get; set; } = KeyboardKey.LeftShift;
    public KeyboardKey Crouch { get; set; } = KeyboardKey.LeftControl;
    public KeyboardKey Blink { get; set; } = KeyboardKey.Space;
    public KeyboardKey Inventory { get; set; } = KeyboardKey.Tab;
    public KeyboardKey Console { get; set; } = KeyboardKey.F3;
    public KeyboardKey QuickSave { get; set; } = KeyboardKey.F5;
}

/// <summary>
/// Graphics configuration
/// </summary>
public class GraphicsConfig
{
    public int ScreenWidth { get; set; } = 1280;
    public int ScreenHeight { get; set; } = 720;
    public bool Fullscreen { get; set; }
    public bool VSync { get; set; } = true;
    public bool AntiAlias { get; set; } = true;
    public bool BumpMapping { get; set; } = true;
    public bool RoomLights { get; set; } = true;
    public float Gamma { get; set; } = 1.0f;
    public int ParticleAmount { get; set; } = 2; // 0=minimal, 1=reduced, 2=full
    public int TextureLOD { get; set; } = 2; // 0-4, 2=default
    public bool SaveToVRAM { get; set; } = true;
}

/// <summary>
/// Audio configuration
/// </summary>
public class AudioConfig
{
    public float MusicVolume { get; set; } = 1.0f;
    public float SoundVolume { get; set; } = 1.0f;
    public bool SFXAutoRelease { get; set; }
    public bool UserTracks { get; set; }
    public bool UserTrackRepeat { get; set; }
}

/// <summary>
/// Controls configuration
/// </summary>
public class ControlsConfig
{
    public float MouseSensitivity { get; set; } = 1.0f;
    public bool InvertMouseY { get; set; }
    public float MouseSmoothing { get; set; } = 0.5f;
    public KeyBindings Keys { get; set; } = new();
}

/// <summary>
/// Advanced/debug configuration
/// </summary>
public class AdvancedConfig
{
    public bool ConsoleAutoOpen { get; set; }
    public bool ShowFPS { get; set; } = true;
    public bool DebugHUD { get; set; }
    public float HideDistance { get; set; } = 15.0f;
    public float FogNear { get; set; } = 0.5f;
    public float FogFar { get; set; } = 30.0f;
}

/// <summary>
/// Configuration system - handles loading/saving of all game settings
/// Port of Main.bb INI file operations
/// </summary>
public static class ConfigSystem
{
    private const string ConfigFile = "options.ini";
    private const string DefaultSection = "options";

    public static GraphicsConfig Graphics { get; private set; } = new();
    public static AudioConfig Audio { get; private set; } = new();
    public static ControlsConfig Controls { get; private set; } = new();
    public static AdvancedConfig Advanced { get; private set; } = new();

    // Cached INI data
    private static Dictionary<string, Dictionary<string, string>> _iniData = new();

    /// <summary>
    /// Initialize config system, loading from file if exists
    /// </summary>
    public static void Initialize()
    {
        Load();
        Console.WriteLine("[ConfigSystem] Initialized");
    }

    /// <summary>
    /// Load configuration from INI file
    /// </summary>
    public static void Load()
    {
        if (!File.Exists(ConfigFile))
        {
            Console.WriteLine("[ConfigSystem] No config file found, using defaults");
            Save(); // Create default config
            return;
        }

        try
        {
            _iniData.Clear();
            string currentSection = DefaultSection;

            foreach (string line in File.ReadAllLines(ConfigFile))
            {
                string trimmed = line.Trim();
                if (string.IsNullOrEmpty(trimmed) || trimmed.StartsWith(";") || trimmed.StartsWith("#"))
                    continue;

                if (trimmed.StartsWith("[") && trimmed.EndsWith("]"))
                {
                    currentSection = trimmed[1..^1].ToLower();
                    if (!_iniData.ContainsKey(currentSection))
                        _iniData[currentSection] = new Dictionary<string, string>();
                }
                else if (trimmed.Contains('='))
                {
                    int eqPos = trimmed.IndexOf('=');
                    string key = trimmed[..eqPos].Trim().ToLower();
                    string value = trimmed[(eqPos + 1)..].Trim();

                    if (!_iniData.ContainsKey(currentSection))
                        _iniData[currentSection] = new Dictionary<string, string>();

                    _iniData[currentSection][key] = value;
                }
            }

            // Parse graphics
            Graphics.ScreenWidth = GetInt("graphics", "width", 1280);
            Graphics.ScreenHeight = GetInt("graphics", "height", 720);
            Graphics.Fullscreen = GetBool("graphics", "fullscreen", false);
            Graphics.VSync = GetBool("graphics", "vsync", true);
            Graphics.AntiAlias = GetBool("graphics", "antialias", true);
            Graphics.BumpMapping = GetBool("graphics", "bumpmapping", true);
            Graphics.RoomLights = GetBool("graphics", "roomlights", true);
            Graphics.Gamma = GetFloat("graphics", "gamma", 1.0f);
            Graphics.ParticleAmount = GetInt("graphics", "particleamount", 2);
            Graphics.TextureLOD = GetInt("graphics", "texturelod", 2);
            Graphics.SaveToVRAM = GetBool("graphics", "vram", true);

            // Parse audio
            Audio.MusicVolume = GetFloat("audio", "musicvolume", 1.0f);
            Audio.SoundVolume = GetFloat("audio", "soundvolume", 1.0f);
            Audio.SFXAutoRelease = GetBool("audio", "sfxautorelease", false);
            Audio.UserTracks = GetBool("audio", "usertracks", false);
            Audio.UserTrackRepeat = GetBool("audio", "usertrackrepeat", false);

            // Parse controls
            Controls.MouseSensitivity = GetFloat("controls", "mousesensitivity", 1.0f);
            Controls.InvertMouseY = GetBool("controls", "invertmouse", false);
            Controls.MouseSmoothing = GetFloat("controls", "mousesmoothing", 0.5f);

            // Parse key bindings
            Controls.Keys.MoveForward = (KeyboardKey)GetInt("controls", "key_forward", (int)KeyboardKey.W);
            Controls.Keys.MoveBackward = (KeyboardKey)GetInt("controls", "key_backward", (int)KeyboardKey.S);
            Controls.Keys.StrafeLeft = (KeyboardKey)GetInt("controls", "key_left", (int)KeyboardKey.A);
            Controls.Keys.StrafeRight = (KeyboardKey)GetInt("controls", "key_right", (int)KeyboardKey.D);
            Controls.Keys.Sprint = (KeyboardKey)GetInt("controls", "key_sprint", (int)KeyboardKey.LeftShift);
            Controls.Keys.Crouch = (KeyboardKey)GetInt("controls", "key_crouch", (int)KeyboardKey.LeftControl);
            Controls.Keys.Blink = (KeyboardKey)GetInt("controls", "key_blink", (int)KeyboardKey.Space);
            Controls.Keys.Inventory = (KeyboardKey)GetInt("controls", "key_inventory", (int)KeyboardKey.Tab);
            Controls.Keys.Console = (KeyboardKey)GetInt("controls", "key_console", (int)KeyboardKey.F3);
            Controls.Keys.QuickSave = (KeyboardKey)GetInt("controls", "key_quicksave", (int)KeyboardKey.F5);

            // Parse advanced
            Advanced.ConsoleAutoOpen = GetBool("advanced", "consoleautoopen", false);
            Advanced.ShowFPS = GetBool("advanced", "showfps", true);
            Advanced.DebugHUD = GetBool("advanced", "debughud", false);
            Advanced.HideDistance = GetFloat("advanced", "hidedistance", 15.0f);
            Advanced.FogNear = GetFloat("advanced", "fognear", 0.5f);
            Advanced.FogFar = GetFloat("advanced", "fogfar", 30.0f);

            Console.WriteLine("[ConfigSystem] Loaded from " + ConfigFile);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[ConfigSystem] Error loading config: {ex.Message}");
        }
    }

    /// <summary>
    /// Save configuration to INI file
    /// </summary>
    public static void Save()
    {
        try
        {
            using var writer = new StreamWriter(ConfigFile);

            writer.WriteLine("; SCP: Containment Breach C# Port - Configuration");
            writer.WriteLine("; Generated: " + DateTime.Now);
            writer.WriteLine();

            // Graphics
            writer.WriteLine("[graphics]");
            writer.WriteLine($"width = {Graphics.ScreenWidth}");
            writer.WriteLine($"height = {Graphics.ScreenHeight}");
            writer.WriteLine($"fullscreen = {BoolToInt(Graphics.Fullscreen)}");
            writer.WriteLine($"vsync = {BoolToInt(Graphics.VSync)}");
            writer.WriteLine($"antialias = {BoolToInt(Graphics.AntiAlias)}");
            writer.WriteLine($"bumpmapping = {BoolToInt(Graphics.BumpMapping)}");
            writer.WriteLine($"roomlights = {BoolToInt(Graphics.RoomLights)}");
            writer.WriteLine($"gamma = {Graphics.Gamma:F2}");
            writer.WriteLine($"particleamount = {Graphics.ParticleAmount}");
            writer.WriteLine($"texturelod = {Graphics.TextureLOD}");
            writer.WriteLine($"vram = {BoolToInt(Graphics.SaveToVRAM)}");
            writer.WriteLine();

            // Audio
            writer.WriteLine("[audio]");
            writer.WriteLine($"musicvolume = {Audio.MusicVolume:F2}");
            writer.WriteLine($"soundvolume = {Audio.SoundVolume:F2}");
            writer.WriteLine($"sfxautorelease = {BoolToInt(Audio.SFXAutoRelease)}");
            writer.WriteLine($"usertracks = {BoolToInt(Audio.UserTracks)}");
            writer.WriteLine($"usertrackrepeat = {BoolToInt(Audio.UserTrackRepeat)}");
            writer.WriteLine();

            // Controls
            writer.WriteLine("[controls]");
            writer.WriteLine($"mousesensitivity = {Controls.MouseSensitivity:F2}");
            writer.WriteLine($"invertmouse = {BoolToInt(Controls.InvertMouseY)}");
            writer.WriteLine($"mousesmoothing = {Controls.MouseSmoothing:F2}");
            writer.WriteLine($"key_forward = {(int)Controls.Keys.MoveForward}");
            writer.WriteLine($"key_backward = {(int)Controls.Keys.MoveBackward}");
            writer.WriteLine($"key_left = {(int)Controls.Keys.StrafeLeft}");
            writer.WriteLine($"key_right = {(int)Controls.Keys.StrafeRight}");
            writer.WriteLine($"key_sprint = {(int)Controls.Keys.Sprint}");
            writer.WriteLine($"key_crouch = {(int)Controls.Keys.Crouch}");
            writer.WriteLine($"key_blink = {(int)Controls.Keys.Blink}");
            writer.WriteLine($"key_inventory = {(int)Controls.Keys.Inventory}");
            writer.WriteLine($"key_console = {(int)Controls.Keys.Console}");
            writer.WriteLine($"key_quicksave = {(int)Controls.Keys.QuickSave}");
            writer.WriteLine();

            // Advanced
            writer.WriteLine("[advanced]");
            writer.WriteLine($"consoleautoopen = {BoolToInt(Advanced.ConsoleAutoOpen)}");
            writer.WriteLine($"showfps = {BoolToInt(Advanced.ShowFPS)}");
            writer.WriteLine($"debughud = {BoolToInt(Advanced.DebugHUD)}");
            writer.WriteLine($"hidedistance = {Advanced.HideDistance:F2}");
            writer.WriteLine($"fognear = {Advanced.FogNear:F2}");
            writer.WriteLine($"fogfar = {Advanced.FogFar:F2}");

            Console.WriteLine("[ConfigSystem] Saved to " + ConfigFile);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[ConfigSystem] Error saving config: {ex.Message}");
        }
    }

    // INI helper methods

    private static string GetString(string section, string key, string defaultValue = "")
    {
        section = section.ToLower();
        key = key.ToLower();

        if (_iniData.TryGetValue(section, out var sectionData))
        {
            if (sectionData.TryGetValue(key, out var value))
                return value;
        }
        return defaultValue;
    }

    private static int GetInt(string section, string key, int defaultValue = 0)
    {
        string value = GetString(section, key);
        return int.TryParse(value, out int result) ? result : defaultValue;
    }

    private static float GetFloat(string section, string key, float defaultValue = 0f)
    {
        string value = GetString(section, key);
        return float.TryParse(value, out float result) ? result : defaultValue;
    }

    private static bool GetBool(string section, string key, bool defaultValue = false)
    {
        string value = GetString(section, key).ToLower();
        if (string.IsNullOrEmpty(value))
            return defaultValue;

        return value == "1" || value == "true" || value == "yes" || value == "on";
    }

    private static int BoolToInt(bool value) => value ? 1 : 0;

    /// <summary>
    /// Apply graphics settings to Raylib
    /// </summary>
    public static void ApplyGraphicsSettings()
    {
        // VSync is set during window creation
        // Other settings would be applied here
        Console.WriteLine("[ConfigSystem] Graphics settings applied");
    }

    /// <summary>
    /// Apply audio settings to sound system
    /// </summary>
    public static void ApplyAudioSettings()
    {
        // Would set master volumes here
        Console.WriteLine("[ConfigSystem] Audio settings applied");
    }
}
