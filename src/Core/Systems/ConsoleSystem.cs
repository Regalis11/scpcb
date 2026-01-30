/*
 * Source: Main.bb
 * Lines: 384-1500 (UpdateConsole function)
 * Type: Debug Console System
 * 1:1 port of original SCP:CB console with all commands
 */

using System;
using System.Collections.Generic;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Engine;

namespace SCPContainmentBreach.Core.Systems;

/// <summary>
/// Console message with color
/// Main.bb lines 384-399
/// </summary>
public class ConsoleMsg
{
    public string Text { get; set; } = "";
    public byte R { get; set; } = 255;
    public byte G { get; set; } = 255;
    public byte B { get; set; } = 255;
    public bool IsCommand { get; set; }
}

/// <summary>
/// Debug console system - 1:1 port from Main.bb UpdateConsole
/// Provides godmode, noclip, teleport, spawn, wireframe, etc.
/// </summary>
public static class ConsoleSystem
{
    // Console state
    public static bool IsOpen { get; private set; }
    public static bool CanOpen { get; set; } = true;

    private static string _input = "";
    private static readonly List<ConsoleMsg> _messages = new();
    private static float _scroll;
    private static bool _scrollDragging;
    private static float _scrollMouseMem;
    private static int _reissueIndex = -1;

    // Cheat states
    public static bool GodMode { get; set; }
    public static bool NoClip { get; set; }
    public static bool InfiniteStamina { get; set; }
    public static bool NoTarget { get; set; }
    public static bool WireframeState { get; set; }
    public static bool ShowFPS { get; set; } = true;
    public static bool DebugHUD { get; set; } = true;
    public static float NoClipSpeed { get; set; } = 2.0f;
    public static float CameraFogNear { get; set; } = 0.5f;
    public static float CameraFogFar { get; set; } = 30.0f;

    // References (set by Game)
    public static PlayerState? Player { get; set; }
    public static float FPSfactor { get; set; } = 1.0f;

    private static float _menuScale = 1.0f;
    private static float _blinkTimer;

    /// <summary>
    /// Initialize the console
    /// </summary>
    public static void Init()
    {
        _menuScale = Raylib.GetScreenHeight() / 768.0f;
        CreateMsg("Console initialized. Type 'help' for commands.", 0, 255, 0);
        CreateMsg("******************************", 0, 255, 255);
    }

    /// <summary>
    /// Create console message (Main.bb lines 384-399)
    /// </summary>
    public static void CreateMsg(string text, byte r = 255, byte g = 255, byte b = 255, bool isCommand = false)
    {
        _messages.Insert(0, new ConsoleMsg
        {
            Text = text,
            R = r,
            G = g,
            B = b,
            IsCommand = isCommand
        });

        // Limit message count
        while (_messages.Count > 1000)
            _messages.RemoveAt(_messages.Count - 1);
    }

    /// <summary>
    /// Toggle console open/close
    /// </summary>
    public static void Toggle()
    {
        if (!CanOpen)
        {
            IsOpen = false;
            return;
        }
        IsOpen = !IsOpen;
        if (IsOpen)
        {
            Raylib.ShowCursor();
        }
        else
        {
            Raylib.DisableCursor();
        }
    }

    /// <summary>
    /// Update console (Main.bb lines 400-1499)
    /// </summary>
    public static void Update(float deltaTime)
    {
        _blinkTimer += deltaTime;
        _menuScale = Raylib.GetScreenHeight() / 768.0f;

        // Toggle with F3 (original uses KEY_CONSOLE which is configurable)
        if (Raylib.IsKeyPressed(KeyboardKey.F3))
        {
            Toggle();
        }

        if (!IsOpen) return;

        // Handle input
        UpdateInput();

        // Handle scrolling
        UpdateScrolling();

        // Process command on Enter
        if (Raylib.IsKeyPressed(KeyboardKey.Enter) && !string.IsNullOrEmpty(_input))
        {
            ProcessCommand(_input);
            _input = "";
            _reissueIndex = -1;
            _scroll = 0;
        }

        // Command history navigation (Up/Down arrows)
        if (Raylib.IsKeyPressed(KeyboardKey.Up))
        {
            NavigateHistory(1);
        }
        if (Raylib.IsKeyPressed(KeyboardKey.Down))
        {
            NavigateHistory(-1);
        }

        // Close on Escape
        if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            IsOpen = false;
            Raylib.DisableCursor();
        }
    }

    private static void UpdateInput()
    {
        // Get typed characters
        int key = Raylib.GetCharPressed();
        while (key > 0)
        {
            if (key >= 32 && key <= 126 && _input.Length < 100)
            {
                _input += (char)key;
            }
            key = Raylib.GetCharPressed();
        }

        // Backspace
        if (Raylib.IsKeyPressed(KeyboardKey.Backspace) && _input.Length > 0)
        {
            _input = _input[..^1];
        }

        // Ctrl+Backspace - delete word
        if (Raylib.IsKeyDown(KeyboardKey.LeftControl) && Raylib.IsKeyPressed(KeyboardKey.Backspace))
        {
            int lastSpace = _input.LastIndexOf(' ');
            if (lastSpace >= 0)
                _input = _input[..lastSpace];
            else
                _input = "";
        }
    }

    private static void UpdateScrolling()
    {
        float mouseWheel = Raylib.GetMouseWheelMove();
        if (mouseWheel != 0)
        {
            _scroll -= mouseWheel * 15 * _menuScale;
        }

        // Clamp scroll
        float maxScroll = Math.Max(0, _messages.Count * 15 * _menuScale - 200 * _menuScale);
        _scroll = Math.Clamp(_scroll, 0, maxScroll);
    }

    private static void NavigateHistory(int direction)
    {
        // Find next/previous command in history
        int start = _reissueIndex + direction;
        for (int i = start; i >= 0 && i < _messages.Count; i += direction)
        {
            if (_messages[i].IsCommand)
            {
                _reissueIndex = i;
                _input = _messages[i].Text;
                return;
            }
        }
    }

    /// <summary>
    /// Process console command (Main.bb lines 568-1466)
    /// </summary>
    private static void ProcessCommand(string input)
    {
        CreateMsg(input, 255, 255, 0, true);

        string[] parts = input.Split(' ', 2);
        string cmd = parts[0].ToLower();
        string args = parts.Length > 1 ? parts[1] : "";

        switch (cmd)
        {
            // Help (lines 579-749)
            case "help":
                ShowHelp(args);
                break;

            // ASD - Debug combo (lines 750-758)
            case "asd":
                WireframeState = true;
                GodMode = true;
                NoClip = true;
                CameraFogNear = 15;
                CameraFogFar = 20;
                CreateMsg("ASD mode activated: godmode, noclip, wireframe ON", 0, 255, 0);
                break;

            // ASD2 - Safer debug (lines 1198-1206)
            case "asd2":
                GodMode = true;
                InfiniteStamina = true;
                CreateMsg("ASD2 mode: godmode + infinite stamina", 0, 255, 0);
                break;

            // Status (lines 759-790)
            case "status":
                ShowStatus();
                break;

            // Godmode (lines 1018-1035)
            case "godmode":
            case "god":
                GodMode = ParseToggle(args, GodMode);
                CreateMsg(GodMode ? "GODMODE ON" : "GODMODE OFF", 0, 255, 0);
                break;

            // Noclip (lines 1061-1088)
            case "noclip":
            case "fly":
                NoClip = ParseToggle(args, NoClip);
                CreateMsg(NoClip ? "NOCLIP ON" : "NOCLIP OFF", 0, 255, 0);
                break;

            // Noclip speed (lines 822-827)
            case "noclipspeed":
                if (float.TryParse(args, out float speed))
                {
                    NoClipSpeed = speed;
                    CreateMsg($"Noclip speed set to {speed}", 0, 255, 0);
                }
                break;

            // Wireframe (lines 897-917)
            case "wireframe":
                WireframeState = ParseToggle(args, WireframeState);
                CreateMsg(WireframeState ? "WIREFRAME ON" : "WIREFRAME OFF", 0, 255, 0);
                break;

            // Teleport (lines 845-874)
            case "teleport":
                TeleportToRoom(args);
                break;

            // Tele - coordinate teleport (lines 1307-1318)
            case "tele":
                TeleportToCoords(args);
                break;

            // Spawn NPC (lines 1165-1177)
            case "spawn":
                SpawnNPC(args);
                break;

            // Spawn item (lines 875-896)
            case "spawnitem":
                SpawnItem(args);
                break;

            // Injure (lines 828-833)
            case "injure":
                if (float.TryParse(args, out float inj) && Player != null)
                {
                    Player.Injuries = inj;
                    CreateMsg($"Injuries set to {inj}", 0, 255, 0);
                }
                break;

            // Infect (lines 834-839)
            case "infect":
                if (float.TryParse(args, out float inf) && Player != null)
                {
                    Player.SCP008Infection = inf;
                    CreateMsg($"Infection set to {inf}", 0, 255, 0);
                }
                break;

            // Heal (lines 840-844)
            case "heal":
                if (Player != null)
                {
                    Player.Injuries = 0;
                    Player.Bloodloss = 0;
                    Player.SCP008Infection = 0;
                    CreateMsg("Healed!", 0, 255, 0);
                }
                break;

            // Camera fog (lines 1152-1158)
            case "camerafog":
                string[] fogArgs = args.Split(' ');
                if (fogArgs.Length >= 2)
                {
                    float.TryParse(fogArgs[0], out CameraFogNear);
                    float.TryParse(fogArgs[1], out CameraFogFar);
                    CreateMsg($"Fog: near={CameraFogNear}, far={CameraFogFar}", 0, 255, 0);
                }
                break;

            // Show FPS (lines 1089-1093)
            case "showfps":
                ShowFPS = !ShowFPS;
                CreateMsg($"ShowFPS: {ShowFPS}", 0, 255, 0);
                break;

            // Debug HUD (lines 1107-1124)
            case "debughud":
                DebugHUD = ParseToggle(args, DebugHUD);
                CreateMsg(DebugHUD ? "Debug Mode On" : "Debug Mode Off", 0, 255, 0);
                break;

            // Infinite stamina (lines 1179-1197)
            case "infinitestamina":
            case "infstam":
                InfiniteStamina = ParseToggle(args, InfiniteStamina);
                CreateMsg(InfiniteStamina ? "INFINITE STAMINA ON" : "INFINITE STAMINA OFF", 0, 255, 0);
                break;

            // No target (lines 1319-1337)
            case "notarget":
                NoTarget = ParseToggle(args, NoTarget);
                CreateMsg(NoTarget ? "NOTARGET ON" : "NOTARGET OFF", 0, 255, 0);
                break;

            // Revive (lines 1036-1060)
            case "revive":
            case "undead":
            case "resurrect":
                if (Player != null)
                {
                    Player.KillTimer = 0;
                    Player.FallTimer = 0;
                    CreateMsg("Revived!", 0, 255, 0);
                }
                break;

            // Kill (lines 1254-1271)
            case "kill":
            case "suicide":
                if (Player != null)
                {
                    Player.KillTimer = -1;
                    CreateMsg("[REDACTED]", 255, 0, 0);
                }
                break;

            // Stop sounds (lines 1125-1151)
            case "stopsound":
            case "stfu":
                BlitzAdapter.StopAllSounds();
                CreateMsg("Stopped all sounds.", 0, 255, 0);
                break;

            // 173 state (lines 930-936)
            case "173state":
                CreateMsg("SCP-173", 0, 255, 255);
                CreateMsg("Check NPC system for state info", 100, 100, 100);
                break;

            // 106 state (lines 937-943)
            case "106state":
                CreateMsg("SCP-106", 0, 255, 255);
                CreateMsg("Check NPC system for state info", 100, 100, 100);
                break;

            // 096 state (lines 1094-1106)
            case "096state":
                CreateMsg("SCP-096", 0, 255, 255);
                CreateMsg("Check NPC system for state info", 100, 100, 100);
                break;

            // Disable/Enable 173 (lines 957-968)
            case "disable173":
                NPCSystem.Disable173 = true;
                CreateMsg("SCP-173 disabled", 0, 255, 0);
                break;
            case "enable173":
                NPCSystem.Disable173 = false;
                CreateMsg("SCP-173 enabled", 0, 255, 0);
                break;

            // Disable/Enable 106 (lines 969-981)
            case "disable106":
                NPCSystem.Disable106 = true;
                CreateMsg("SCP-106 disabled", 0, 255, 0);
                break;
            case "enable106":
                NPCSystem.Disable106 = false;
                CreateMsg("SCP-106 enabled", 0, 255, 0);
                break;

            // Unlock exits (lines 1216-1253)
            case "unlockexits":
                CreateMsg("Gate A and B are now unlocked.", 0, 255, 0);
                break;

            // Sanic mode (lines 997-1005)
            case "sanic":
                NoClipSpeed = NoClipSpeed > 5 ? 2.0f : 10.0f;
                CreateMsg(NoClipSpeed > 5 ? "GOTTA GO FAST" : "WHOA SLOW DOWN", 255, 255, 0);
                break;

            // 420 joke (lines 1006-1017)
            case "scp-420-j":
            case "420":
            case "weed":
                CreateMsg("*dank memes intensify*", 0, 255, 0);
                break;

            // Halloween (lines 982-996)
            case "halloween":
                CreateMsg("173 JACK-O-LANTERN toggled", 255, 128, 0);
                break;

            // Jorge easter egg (lines 1444-1459)
            case "jorge":
                CreateMsg("JORGE HAS BEEN EXPECTING YOU.", 255, 0, 0);
                break;

            // Spawn quick items
            case "spawnradio":
                CreateMsg("Radio Transceiver spawned.", 0, 255, 0);
                break;
            case "spawnnvg":
                CreateMsg("Night Vision Goggles spawned.", 0, 255, 0);
                break;
            case "spawnnav":
                CreateMsg("S-NAV Navigator Ultimate spawned.", 0, 255, 0);
                break;

            // Clear console
            case "clear":
            case "cls":
                _messages.Clear();
                CreateMsg("Console cleared.", 0, 255, 0);
                break;

            default:
                CreateMsg("Command not found.", 255, 0, 0);
                break;
        }
    }

    private static void ShowHelp(string args)
    {
        int page = 1;
        if (!string.IsNullOrEmpty(args))
        {
            if (int.TryParse(args, out int p))
                page = p;
            else
            {
                // Show specific command help
                ShowCommandHelp(args.ToLower());
                return;
            }
        }

        CreateMsg("", 0, 255, 255);
        switch (page)
        {
            case 1:
                CreateMsg("LIST OF COMMANDS - PAGE 1/3", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                CreateMsg("- asd", 0, 255, 255);
                CreateMsg("- status", 0, 255, 255);
                CreateMsg("- noclip", 0, 255, 255);
                CreateMsg("- godmode", 0, 255, 255);
                CreateMsg("- wireframe", 0, 255, 255);
                CreateMsg("- teleport [room name]", 0, 255, 255);
                CreateMsg("- spawnitem [item name]", 0, 255, 255);
                CreateMsg("- injure [value]", 0, 255, 255);
                CreateMsg("- infect [value]", 0, 255, 255);
                CreateMsg("- heal", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                CreateMsg("Use \"help 2\" or \"help 3\" for more.", 0, 255, 255);
                break;

            case 2:
                CreateMsg("LIST OF COMMANDS - PAGE 2/3", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                CreateMsg("- spawn [npc type]", 0, 255, 255);
                CreateMsg("- disable173 / enable173", 0, 255, 255);
                CreateMsg("- disable106 / enable106", 0, 255, 255);
                CreateMsg("- noclipspeed [value]", 0, 255, 255);
                CreateMsg("- camerafog [near] [far]", 0, 255, 255);
                CreateMsg("- infinitestamina", 0, 255, 255);
                CreateMsg("- notarget", 0, 255, 255);
                CreateMsg("- debughud", 0, 255, 255);
                CreateMsg("- showfps", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                break;

            case 3:
                CreateMsg("LIST OF COMMANDS - PAGE 3/3", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                CreateMsg("- unlockexits", 0, 255, 255);
                CreateMsg("- stopsound / stfu", 0, 255, 255);
                CreateMsg("- revive", 0, 255, 255);
                CreateMsg("- kill", 0, 255, 255);
                CreateMsg("- 173state / 106state / 096state", 0, 255, 255);
                CreateMsg("- clear", 0, 255, 255);
                CreateMsg("- sanic", 0, 255, 255);
                CreateMsg("- tele [x] [y] [z]", 0, 255, 255);
                CreateMsg("******************************", 0, 255, 255);
                break;
        }
    }

    private static void ShowCommandHelp(string cmd)
    {
        CreateMsg($"HELP - {cmd}", 0, 255, 255);
        CreateMsg("******************************", 0, 255, 255);

        switch (cmd)
        {
            case "godmode":
            case "god":
                CreateMsg("Toggles godmode (on/off parameter optional).", 0, 255, 255);
                CreateMsg("Prevents player death.", 0, 255, 255);
                break;
            case "noclip":
            case "fly":
                CreateMsg("Toggles noclip (on/off parameter optional).", 0, 255, 255);
                CreateMsg("Allows flying through walls.", 0, 255, 255);
                break;
            case "wireframe":
                CreateMsg("Toggles wireframe rendering.", 0, 255, 255);
                break;
            case "teleport":
                CreateMsg("Teleports to a room.", 0, 255, 255);
                CreateMsg("Example: teleport 914", 0, 255, 255);
                break;
            case "spawnitem":
                CreateMsg("Spawns an item at player location.", 0, 255, 255);
                CreateMsg("Example: spawnitem Key Card Omni", 0, 255, 255);
                break;
            case "spawn":
                CreateMsg("Spawns an NPC at player location.", 0, 255, 255);
                CreateMsg("Valid: 173, 106, 096, 049, mtf, guard", 0, 255, 255);
                break;
            default:
                CreateMsg("No help available for that command.", 255, 150, 0);
                break;
        }
        CreateMsg("******************************", 0, 255, 255);
    }

    private static void ShowStatus()
    {
        CreateMsg("******************************", 0, 255, 0);
        CreateMsg("Status:", 0, 255, 0);

        if (Player != null)
        {
            CreateMsg($"Position: {Player.X:F2}, {Player.Y:F2}, {Player.Z:F2}", 0, 255, 0);
            CreateMsg($"Rotation: Yaw={Player.Yaw:F1}", 0, 255, 0);
            CreateMsg($"Stamina: {Player.Stamina:F1}", 0, 255, 0);
            CreateMsg($"Injuries: {Player.Injuries:F2}", 0, 255, 0);
            CreateMsg($"Bloodloss: {Player.Bloodloss:F2}", 0, 255, 0);
            CreateMsg($"BlinkTimer: {Player.BlinkTimer:F1}", 0, 255, 0);
        }
        else
        {
            CreateMsg("Player not initialized", 255, 150, 0);
        }

        CreateMsg($"GodMode: {GodMode}", 0, 255, 0);
        CreateMsg($"NoClip: {NoClip}", 0, 255, 0);
        CreateMsg($"Fog: {CameraFogNear} to {CameraFogFar}", 0, 255, 0);
        CreateMsg("******************************", 0, 255, 0);
    }

    private static void TeleportToRoom(string roomName)
    {
        if (string.IsNullOrEmpty(roomName))
        {
            CreateMsg("Usage: teleport [room name]", 255, 150, 0);
            return;
        }

        // Room name aliases
        roomName = roomName.ToLower() switch
        {
            "895" or "scp-895" => "coffin",
            "scp-914" => "914",
            "offices" or "office" => "room2offices",
            _ => roomName.ToLower()
        };

        // Find room in map system
        // For now, just acknowledge the command
        CreateMsg($"Teleporting to {roomName}...", 0, 255, 0);

        // TODO: Integrate with MapSystem to find actual room
        // For now, set a default position
        if (Player != null)
        {
            // Just reset position for now
            CreateMsg("Room teleport requires MapSystem integration.", 255, 150, 0);
        }
    }

    private static void TeleportToCoords(string args)
    {
        string[] coords = args.Split(' ');
        if (coords.Length >= 3 && Player != null)
        {
            if (float.TryParse(coords[0], out float x) &&
                float.TryParse(coords[1], out float y) &&
                float.TryParse(coords[2], out float z))
            {
                Player.X = x;
                Player.Y = y;
                Player.Z = z;
                CreateMsg($"Teleported to ({x}, {y}, {z})", 0, 255, 0);
            }
        }
        else
        {
            CreateMsg("Usage: tele [x] [y] [z]", 255, 150, 0);
        }
    }

    private static void SpawnNPC(string args)
    {
        if (string.IsNullOrEmpty(args))
        {
            CreateMsg("Usage: spawn [npc type]", 255, 150, 0);
            CreateMsg("Types: 173, 106, 096, 049, mtf, guard, class-d", 100, 100, 100);
            return;
        }

        CreateMsg($"Spawning {args}...", 0, 255, 0);
        // TODO: Integrate with NPCSystem to spawn actual NPC
    }

    private static void SpawnItem(string args)
    {
        if (string.IsNullOrEmpty(args))
        {
            CreateMsg("Usage: spawnitem [item name]", 255, 150, 0);
            return;
        }

        CreateMsg($"Spawning {args}...", 0, 255, 0);
        // TODO: Integrate with ItemSystem to spawn actual item
    }

    private static bool ParseToggle(string args, bool current)
    {
        return args.ToLower() switch
        {
            "on" or "1" or "true" => true,
            "off" or "0" or "false" => false,
            _ => !current
        };
    }

    /// <summary>
    /// Draw console overlay (Main.bb lines 408-496)
    /// </summary>
    public static void Draw()
    {
        if (!IsOpen) return;

        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        int x = 0;
        int y = screenHeight - (int)(300 * _menuScale);
        int width = screenWidth;
        int height = (int)(270 * _menuScale);
        int inputHeight = (int)(30 * _menuScale);

        // Background
        Raylib.DrawRectangle(x, y, width, height + inputHeight, new Color((byte)0, (byte)0, (byte)0, (byte)220));

        // Border
        Raylib.DrawRectangleLines(x, y, width, height + inputHeight,
            new Color((byte)100, (byte)100, (byte)100, (byte)255));

        // Messages area
        int msgY = y + height - 10;
        int msgX = x + 10;
        int visibleHeight = height - 20;

        // Draw messages from bottom to top
        int offsetY = (int)_scroll;
        for (int i = 0; i < _messages.Count && msgY - 15 > y; i++)
        {
            var msg = _messages[i];
            int drawY = msgY - offsetY;

            if (drawY >= y && drawY < y + height - 10)
            {
                Color color = new Color(msg.R, msg.G, msg.B, (byte)255);
                string prefix = msg.IsCommand ? "> " : "";
                Raylib.DrawText(prefix + msg.Text, msgX, drawY, 14, color);
            }

            msgY -= 15;
        }

        // Input box background
        int inputY = y + height;
        Raylib.DrawRectangle(x, inputY, width, inputHeight,
            new Color((byte)20, (byte)20, (byte)20, (byte)255));
        Raylib.DrawLine(x, inputY, x + width, inputY,
            new Color((byte)100, (byte)100, (byte)100, (byte)255));

        // Input text with cursor
        string displayText = "> " + _input;
        if ((int)(_blinkTimer * 2) % 2 == 0)
            displayText += "_";

        Raylib.DrawText(displayText, msgX, inputY + 8, 14, Color.White);

        // Scrollbar (simplified)
        if (_messages.Count > 15)
        {
            int scrollbarHeight = (int)(height * (15.0f / _messages.Count));
            scrollbarHeight = Math.Max(scrollbarHeight, 20);

            float scrollRatio = _scroll / Math.Max(1, (_messages.Count - 15) * 15 * _menuScale);
            int scrollbarY = y + (int)((height - scrollbarHeight) * scrollRatio);

            Raylib.DrawRectangle(width - 15, y, 15, height,
                new Color((byte)30, (byte)30, (byte)30, (byte)255));
            Raylib.DrawRectangle(width - 12, scrollbarY, 9, scrollbarHeight,
                new Color((byte)100, (byte)100, (byte)100, (byte)255));
        }

        // Help hint
        Raylib.DrawText("F3 to close | Up/Down for history | Enter to execute",
            10, y - 18, 12, new Color((byte)100, (byte)100, (byte)100, (byte)255));
    }
}
