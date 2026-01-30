/*
 * Source: Menu.bb
 * Lines: 1-900
 * Type: Main Menu System
 * 1:1 port of original SCP:CB main menu with intro sequence
 */

using System;
using System.Collections.Generic;
using Raylib_cs;
using SCPContainmentBreach.Core;
using SCPContainmentBreach.Core.Systems;

namespace SCPContainmentBreach.Scenes;

/// <summary>
/// Main menu recreation from Menu.bb
/// Implements intro sequence, menu tabs, difficulty selection
/// </summary>
public class MainMenuScene : IScene
{
    public string Name => "MainMenu";
    public bool IsLoaded { get; private set; }

    // Menu state
    private int _mainMenuTab; // 0=main, 1=new game, 2=load game, 3=options, 4=load map, 5=gfx, 6=audio, 7=controls
    private float _menuScale = 1.0f;

    // Intro sequence state
    private bool _introEnabled = true;
    private bool _playingIntro;
    private float _introTimer;
    private int _introPhase;
    private float _introAlpha;

    // Blinking text effect (Menu.bb lines 22-26, 77-119)
    private float _blinkTimer1 = 1;
    private float _blinkDuration1;
    private float _blinkTimer2 = 700;
    private float _blinkDuration2 = 20;
    private string _menuStr = "";
    private int _menuStrX, _menuStrY;

    // 173 blink effect
    private float _blink173Timer = 4000;
    private float _blink173Duration = 300;

    // New game settings
    private string _currSave = "";
    private string _randomSeed = "";
    private int _selectedDifficulty; // 0=SAFE, 1=EUCLID, 2=KETER, 3=CUSTOM

    // Button hover state
    private int _hoveredButton = -1;

    // Horror text options (Menu.bb lines 88-117)
    private readonly string[] _horrorTexts = {
        "DON'T BLINK",
        "DON'T BLINK",
        "DON'T BLINK",
        "Secure. Contain. Protect.",
        "Secure. Contain. Protect.",
        "You want happy endings? Fuck you.",
        "You want happy endings? Fuck you.",
        "You want happy endings? Fuck you.",
        "Sometimes we would have had time to scream.",
        "Sometimes we would have had time to scream.",
        "Sometimes we would have had time to scream.",
        "NIL",
        "NO",
        "black white black white black white gray",
        "Stone does not care",
        "9341",
        "It controls the doors",
        "e8m106]af173o+079m895w914",
        "NIL",
        "It has taken over everything",
        "The spiral is growing",
        "\"Some kind of gestalt effect due to massive reality damage.\""
    };

    // Difficulty definitions (matching Difficulty.bb)
    private readonly DifficultyDef[] _difficulties = {
        new DifficultyDef("SAFE", 100, 200, 100, "Recommended for players new to the game.\nThe player can save anywhere and SCPs are slower."),
        new DifficultyDef("EUCLID", 200, 255, 50, "The standard experience.\nThe player can only save in specific locations."),
        new DifficultyDef("KETER", 255, 100, 100, "For experienced players.\nPermadeath is enabled. One life only."),
        new DifficultyDef("CUSTOM", 255, 255, 255, "Customize your experience.\nChoose your own difficulty settings.")
    };

    // Random seed easter eggs (Menu.bb lines 150-178)
    private readonly string[] _easterEggSeeds = {
        "NIL", "NO", "d9341", "5CP_I73", "DONTBLINK", "CRUNCH",
        "die", "HTAED", "rustledjim", "larry", "JORGE", "dirtymetal", "whatpumpkin"
    };

    private Random _random = new Random();
    private float _animationTime;

    // Input state
    private bool _inputActive;
    private int _activeInputField; // 0=none, 1=save name, 2=seed

    // Load game state
    private List<SaveSlotInfo> _saveSlots = new();
    private int _loadPageOffset;
    private string _deleteConfirmSlot = "";
    private float _lastSaveRefresh;

    public void Load()
    {
        IsLoaded = true;
        _mainMenuTab = 0;
        _animationTime = 0;
        _selectedDifficulty = 1; // Default EUCLID

        // Calculate menu scale based on resolution
        _menuScale = Raylib.GetScreenHeight() / 768.0f;

        // Initialize blinking text
        _menuStrX = _random.Next(700, 1000);
        _menuStrY = _random.Next(100, 600);
        _menuStr = _horrorTexts[_random.Next(_horrorTexts.Length)];

        // Start intro if enabled
        if (_introEnabled)
        {
            _playingIntro = true;
            _introTimer = 0;
            _introPhase = 0;
            _introAlpha = 0;
        }

        Raylib.ShowCursor();
        Console.WriteLine("[MainMenuScene] Loaded");
    }

    public void Update(float deltaTime)
    {
        _animationTime += deltaTime;
        float fps = deltaTime * 70.0f; // FPSfactor

        if (_playingIntro)
        {
            UpdateIntro(deltaTime);
            return;
        }

        UpdateBlinkEffects(fps);
        UpdateMenuInput();
    }

    private void UpdateIntro(float deltaTime)
    {
        _introTimer += deltaTime;

        switch (_introPhase)
        {
            case 0: // Fade in "SECURE"
                _introAlpha = Math.Min(_introTimer / 2.0f, 1.0f);
                if (_introTimer >= 3.0f)
                {
                    _introPhase = 1;
                    _introTimer = 0;
                }
                break;

            case 1: // Fade in "CONTAIN"
                _introAlpha = Math.Min(_introTimer / 2.0f, 1.0f);
                if (_introTimer >= 3.0f)
                {
                    _introPhase = 2;
                    _introTimer = 0;
                }
                break;

            case 2: // Fade in "PROTECT"
                _introAlpha = Math.Min(_introTimer / 2.0f, 1.0f);
                if (_introTimer >= 3.0f)
                {
                    _introPhase = 3;
                    _introTimer = 0;
                }
                break;

            case 3: // Fade out
                _introAlpha = 1.0f - Math.Min(_introTimer / 2.0f, 1.0f);
                if (_introTimer >= 2.5f)
                {
                    _playingIntro = false;
                }
                break;
        }

        // Skip intro on any key/click
        if (Raylib.IsKeyPressed(KeyboardKey.Space) ||
            Raylib.IsKeyPressed(KeyboardKey.Enter) ||
            Raylib.IsKeyPressed(KeyboardKey.Escape) ||
            Raylib.IsMouseButtonPressed(MouseButton.Left))
        {
            _playingIntro = false;
        }
    }

    private void UpdateBlinkEffects(float fps)
    {
        // 173 image blink (Menu.bb lines 67-74)
        if (_random.Next(300) == 1)
        {
            _blink173Timer = _random.Next(4000, 8000);
            _blink173Duration = _random.Next(200, 500);
        }

        // Horror text blink (Menu.bb lines 78-118)
        _blinkTimer2 -= fps;
        if (_blinkTimer2 < 0)
        {
            _blinkTimer2 = _random.Next(700, 800);
            _blinkDuration2 = _random.Next(10, 35);
            _menuStrX = _random.Next((int)(700 * _menuScale), (int)(1000 * _menuScale));
            _menuStrY = _random.Next((int)(100 * _menuScale), (int)(600 * _menuScale));
            _menuStr = _horrorTexts[_random.Next(_horrorTexts.Length)];
        }
    }

    private void UpdateMenuInput()
    {
        int mouseX = Raylib.GetMouseX();
        int mouseY = Raylib.GetMouseY();
        bool clicked = Raylib.IsMouseButtonPressed(MouseButton.Left);

        // Keyboard input for text fields
        if (_activeInputField > 0)
        {
            int key = Raylib.GetCharPressed();
            while (key > 0)
            {
                if (key >= 32 && key <= 126)
                {
                    char c = (char)key;
                    if (_activeInputField == 1 && _currSave.Length < 15)
                    {
                        // Filter invalid filename chars
                        if (c != ':' && c != '.' && c != '/' && c != '\\' &&
                            c != '<' && c != '>' && c != '|' && c != '?' && c != '"' && c != '*')
                        {
                            _currSave += c;
                        }
                    }
                    else if (_activeInputField == 2 && _randomSeed.Length < 15)
                    {
                        _randomSeed += c;
                    }
                }
                key = Raylib.GetCharPressed();
            }

            if (Raylib.IsKeyPressed(KeyboardKey.Backspace))
            {
                if (_activeInputField == 1 && _currSave.Length > 0)
                    _currSave = _currSave[..^1];
                else if (_activeInputField == 2 && _randomSeed.Length > 0)
                    _randomSeed = _randomSeed[..^1];
            }

            if (Raylib.IsKeyPressed(KeyboardKey.Enter) || Raylib.IsKeyPressed(KeyboardKey.Tab))
            {
                _activeInputField = 0;
            }
        }

        if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            if (_mainMenuTab != 0)
            {
                _mainMenuTab = 0;
                _activeInputField = 0;
            }
        }
    }

    public void Draw()
    {
        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        if (_playingIntro)
        {
            DrawIntro(screenWidth, screenHeight);
            return;
        }

        // Black background
        Raylib.DrawRectangle(0, 0, screenWidth, screenHeight, Color.Black);

        // Draw menu background (simulated - would be MenuBack image)
        DrawMenuBackground(screenWidth, screenHeight);

        // Draw 173 background (blinking)
        Draw173Background(screenWidth, screenHeight);

        // Draw horror text
        DrawHorrorText();

        // Draw SCP logo text at bottom
        DrawLogoText(screenWidth, screenHeight);

        // Draw menu content based on current tab
        switch (_mainMenuTab)
        {
            case 0:
                DrawMainButtons(screenWidth, screenHeight);
                break;
            case 1:
                DrawNewGameMenu(screenWidth, screenHeight);
                break;
            case 2:
                DrawLoadGameMenu(screenWidth, screenHeight);
                break;
            case 3:
            case 5:
            case 6:
            case 7:
                DrawOptionsMenu(screenWidth, screenHeight);
                break;
        }

        // Draw cursor
        DrawCursor();
    }

    private void DrawIntro(int screenWidth, int screenHeight)
    {
        Raylib.ClearBackground(Color.Black);

        byte alpha = (byte)(255 * _introAlpha);
        Color textColor = new Color((byte)200, (byte)0, (byte)0, alpha);

        int fontSize = (int)(72 * _menuScale);
        string text = _introPhase switch
        {
            0 => "SECURE",
            1 => "CONTAIN",
            2 => "PROTECT",
            3 => "SECURE. CONTAIN. PROTECT.",
            _ => ""
        };

        if (_introPhase == 3)
            fontSize = (int)(48 * _menuScale);

        int textWidth = Raylib.MeasureText(text, fontSize);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 - fontSize / 2;

        Raylib.DrawText(text, x, y, fontSize, textColor);

        // Skip hint
        string hint = "Press any key to skip";
        int hintWidth = Raylib.MeasureText(hint, 16);
        Raylib.DrawText(hint, (screenWidth - hintWidth) / 2, screenHeight - 50, 16,
            new Color((byte)100, (byte)100, (byte)100, (byte)150));
    }

    private void DrawMenuBackground(int screenWidth, int screenHeight)
    {
        // Gradient background simulating the original menu
        for (int y = 0; y < screenHeight; y++)
        {
            float t = (float)y / screenHeight;
            byte gray = (byte)(10 + t * 20);
            Raylib.DrawLine(0, y, screenWidth, y, new Color(gray, gray, (byte)(gray + 5), (byte)255));
        }

        // Draw some atmospheric lines
        for (int i = 0; i < 10; i++)
        {
            int y = (int)((_animationTime * 20 + i * 80) % screenHeight);
            Raylib.DrawLine(0, y, screenWidth, y, new Color((byte)30, (byte)30, (byte)35, (byte)100));
        }
    }

    private void Draw173Background(int screenWidth, int screenHeight)
    {
        // Simulate 173 blinking in corner (Menu.bb lines 67-69)
        long ms = (long)(_animationTime * 1000);
        if ((ms % (int)_blink173Timer) >= _blink173Duration)
        {
            // Draw 173 silhouette in bottom right
            int size = (int)(200 * _menuScale);
            int x = screenWidth - size - 20;
            int y = screenHeight - size - 20;

            // Simple 173 silhouette
            Color c173 = new Color((byte)60, (byte)50, (byte)40, (byte)200);
            Raylib.DrawRectangle(x + size/3, y, size/3, (int)(size * 0.7f), c173);
            Raylib.DrawCircle(x + size/2, y, size/4, c173);
        }
    }

    private void DrawHorrorText()
    {
        // Draw blinking horror text (Menu.bb lines 79-82)
        if (_blinkTimer2 < _blinkDuration2)
        {
            int offsetX = _random.Next(-5, 6);
            int offsetY = _random.Next(-5, 6);

            Raylib.DrawText(_menuStr, _menuStrX + offsetX, _menuStrY + offsetY, 20,
                new Color((byte)50, (byte)50, (byte)50, (byte)255));
        }
    }

    private void DrawLogoText(int screenWidth, int screenHeight)
    {
        // SCP: CONTAINMENT BREACH title
        string title = "SCP: CONTAINMENT BREACH";
        int fontSize = (int)(42 * _menuScale);
        int titleWidth = Raylib.MeasureText(title, fontSize);

        float pulse = (float)(Math.Sin(_animationTime * 2) * 0.2 + 0.8);
        byte r = (byte)(200 * pulse);

        Raylib.DrawText(title, (screenWidth - titleWidth) / 2,
            (int)(50 * _menuScale), fontSize, new Color(r, (byte)30, (byte)30, (byte)255));

        // C# Port subtitle
        string subtitle = "C# Raylib Port v0.2";
        int subWidth = Raylib.MeasureText(subtitle, 18);
        Raylib.DrawText(subtitle, (screenWidth - subWidth) / 2,
            (int)(100 * _menuScale), 18, new Color((byte)100, (byte)100, (byte)100, (byte)255));
    }

    private void DrawMainButtons(int screenWidth, int screenHeight)
    {
        // Menu.bb lines 133-222 - Main menu buttons
        int x = (int)(159 * _menuScale);
        int width = (int)(400 * _menuScale);
        int height = (int)(70 * _menuScale);

        string[] buttons = { "NEW GAME", "LOAD GAME", "OPTIONS", "QUIT" };

        for (int i = 0; i < buttons.Length; i++)
        {
            int y = (int)((286 + 100 * i) * _menuScale);

            bool hovered = IsMouseInRect(x, y, width, height);
            bool clicked = hovered && Raylib.IsMouseButtonPressed(MouseButton.Left);

            DrawButton(x, y, width, height, buttons[i], hovered);

            if (clicked)
            {
                switch (i)
                {
                    case 0: // NEW GAME
                        _mainMenuTab = 1;
                        GenerateRandomSeed();
                        break;
                    case 1: // LOAD GAME
                        _mainMenuTab = 2;
                        break;
                    case 2: // OPTIONS
                        _mainMenuTab = 3;
                        break;
                    case 3: // QUIT
                        Game.Instance?.Exit();
                        break;
                }
            }
        }
    }

    private void GenerateRandomSeed()
    {
        // Menu.bb lines 150-188 - Random seed generation
        if (_random.Next(15) == 1)
        {
            _randomSeed = _easterEggSeeds[_random.Next(_easterEggSeeds.Length)];
        }
        else
        {
            _randomSeed = "";
            int length = _random.Next(4, 9);
            for (int i = 0; i < length; i++)
            {
                if (_random.Next(3) == 1)
                    _randomSeed += _random.Next(0, 10).ToString();
                else
                    _randomSeed += (char)_random.Next(97, 123);
            }
        }
    }

    private void DrawNewGameMenu(int screenWidth, int screenHeight)
    {
        // Menu.bb lines 260-409 - New game menu
        int x = (int)(159 * _menuScale);
        int y = (int)(286 * _menuScale);
        int width = (int)(400 * _menuScale);
        int height = (int)(70 * _menuScale);

        // Title frame
        DrawFrame(x, y, width, height);
        Raylib.DrawText("NEW GAME", x + width/2 - 60, y + height/2 - 12, 24, Color.White);

        // Back button
        int backX = x + width + (int)(20 * _menuScale);
        int backW = (int)(180 * _menuScale);
        if (DrawButton(backX, y, backW, height, "BACK", IsMouseInRect(backX, y, backW, height)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                _mainMenuTab = 0;
                _activeInputField = 0;
            }
        }

        // Content frame
        int contentY = y + height + (int)(20 * _menuScale);
        int contentWidth = (int)(580 * _menuScale);
        int contentHeight = (int)(330 * _menuScale);
        DrawFrame(x, contentY, contentWidth, contentHeight);

        // Name input (Menu.bb lines 282-294)
        int inputX = x + (int)(20 * _menuScale);
        int inputY = contentY + (int)(20 * _menuScale);
        Raylib.DrawText("Name:", inputX, inputY, 18, Color.White);
        DrawInputBox(x + (int)(150 * _menuScale), inputY - 5, (int)(200 * _menuScale), (int)(30 * _menuScale),
            _currSave, 1);

        // Seed input (Menu.bb lines 297-299)
        inputY += (int)(45 * _menuScale);
        Raylib.DrawText("Map seed:", inputX, inputY, 18, Color.White);
        DrawInputBox(x + (int)(150 * _menuScale), inputY - 5, (int)(200 * _menuScale), (int)(30 * _menuScale),
            _randomSeed, 2);

        // Intro toggle (Menu.bb lines 319-320)
        inputY += (int)(50 * _menuScale);
        Raylib.DrawText("Enable intro:", inputX, inputY, 18, Color.White);
        if (DrawTick(x + (int)(280 * _menuScale), inputY, _introEnabled))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                _introEnabled = !_introEnabled;
        }

        // Difficulty selection (Menu.bb lines 323-328)
        inputY += (int)(40 * _menuScale);
        Raylib.DrawText("Difficulty:", inputX, inputY, 18, Color.White);

        for (int i = 0; i < 4; i++)
        {
            int tickY = inputY + (int)((30 + 30 * i) * _menuScale);
            bool selected = _selectedDifficulty == i;

            if (DrawTick(inputX, tickY, selected))
            {
                if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                    _selectedDifficulty = i;
            }

            Color diffColor = new Color(
                (byte)_difficulties[i].R,
                (byte)_difficulties[i].G,
                (byte)_difficulties[i].B,
                (byte)255);
            Raylib.DrawText(_difficulties[i].Name, inputX + (int)(40 * _menuScale), tickY, 18, diffColor);
        }

        // Difficulty description box
        int descX = x + (int)(150 * _menuScale);
        int descY = inputY + (int)(5 * _menuScale);
        int descW = (int)(410 * _menuScale);
        int descH = (int)(150 * _menuScale);
        DrawFrame(descX, descY, descW, descH);

        // Draw description text
        string[] descLines = _difficulties[_selectedDifficulty].Description.Split('\n');
        for (int i = 0; i < descLines.Length; i++)
        {
            Raylib.DrawText(descLines[i], descX + 10, descY + 10 + i * 20, 16,
                new Color((byte)200, (byte)200, (byte)200, (byte)255));
        }

        // START button (Menu.bb lines 381-406)
        int startX = x + (int)(420 * _menuScale);
        int startY = contentY + contentHeight + (int)(20 * _menuScale);
        int startW = (int)(160 * _menuScale);
        int startH = (int)(70 * _menuScale);

        if (DrawButton(startX, startY, startW, startH, "START", IsMouseInRect(startX, startY, startW, startH)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                StartNewGame();
            }
        }
    }

    private void StartNewGame()
    {
        if (string.IsNullOrEmpty(_currSave))
            _currSave = "untitled";

        if (string.IsNullOrEmpty(_randomSeed))
            _randomSeed = DateTimeOffset.Now.ToUnixTimeMilliseconds().ToString();

        Console.WriteLine($"[MainMenuScene] Starting new game: {_currSave}, Seed: {_randomSeed}, Difficulty: {_difficulties[_selectedDifficulty].Name}");

        // Store settings for game initialization
        // Game.Instance?.SceneManager.LoadScene("Game");

        // For now, just exit menu (game starts directly)
        _mainMenuTab = 0;
    }

    private void DrawLoadGameMenu(int screenWidth, int screenHeight)
    {
        // Menu.bb lines 410-550 - Load game menu
        int x = (int)(159 * _menuScale);
        int y = (int)(286 * _menuScale);
        int width = (int)(400 * _menuScale);
        int height = (int)(70 * _menuScale);

        // Refresh save list periodically
        if (_animationTime - _lastSaveRefresh > 2.0f)
        {
            _saveSlots = SaveSystem.GetSaveSlots();
            _lastSaveRefresh = _animationTime;
        }

        // Title frame
        DrawFrame(x, y, width, height);
        Raylib.DrawText("LOAD GAME", x + width/2 - 70, y + height/2 - 12, 24, Color.White);

        // Back button
        int backX = x + width + (int)(20 * _menuScale);
        int backW = (int)(180 * _menuScale);
        if (DrawButton(backX, y, backW, height, "BACK", IsMouseInRect(backX, y, backW, height)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                _mainMenuTab = 0;
                _deleteConfirmSlot = "";
            }
        }

        // Content frame
        int contentY = y + height + (int)(20 * _menuScale);
        int contentWidth = (int)(580 * _menuScale);
        int contentHeight = (int)(400 * _menuScale);
        DrawFrame(x, contentY, contentWidth, contentHeight);

        if (_saveSlots.Count == 0)
        {
            // No saves message
            Raylib.DrawText("No saved games found.", x + (int)(20 * _menuScale),
                contentY + (int)(20 * _menuScale), 18, new Color((byte)150, (byte)150, (byte)150, (byte)255));
            Raylib.DrawText("Start a new game to create a save.", x + (int)(20 * _menuScale),
                contentY + (int)(45 * _menuScale), 16, new Color((byte)100, (byte)100, (byte)100, (byte)255));
            Raylib.DrawText("Use F3 console: save [name]", x + (int)(20 * _menuScale),
                contentY + (int)(70 * _menuScale), 14, new Color((byte)80, (byte)80, (byte)80, (byte)255));
        }
        else
        {
            // Draw save slots (up to 5 visible)
            int slotY = contentY + (int)(15 * _menuScale);
            int slotH = (int)(70 * _menuScale);
            int maxVisible = 5;

            for (int i = _loadPageOffset; i < _saveSlots.Count && i < _loadPageOffset + maxVisible; i++)
            {
                var slot = _saveSlots[i];
                int sy = slotY + (i - _loadPageOffset) * (slotH + (int)(10 * _menuScale));

                // Slot background
                bool slotHovered = IsMouseInRect(x + (int)(10 * _menuScale), sy, contentWidth - (int)(20 * _menuScale), slotH);
                Color bgColor = slotHovered ?
                    new Color((byte)40, (byte)40, (byte)50, (byte)255) :
                    new Color((byte)25, (byte)25, (byte)30, (byte)255);
                Raylib.DrawRectangle(x + (int)(10 * _menuScale), sy, contentWidth - (int)(20 * _menuScale), slotH, bgColor);
                Raylib.DrawRectangleLines(x + (int)(10 * _menuScale), sy, contentWidth - (int)(20 * _menuScale), slotH,
                    new Color((byte)80, (byte)80, (byte)80, (byte)255));

                // Slot info
                int textX = x + (int)(25 * _menuScale);
                Raylib.DrawText(slot.SlotName, textX, sy + 8, 18, Color.White);
                Raylib.DrawText($"{slot.SaveDate} {slot.SaveTime}", textX, sy + 30, 14,
                    new Color((byte)150, (byte)150, (byte)150, (byte)255));
                string playTime = SaveSystem.FormatPlayTime(slot.PlayTime);
                Raylib.DrawText($"Play time: {playTime}", textX, sy + 48, 12,
                    new Color((byte)100, (byte)100, (byte)100, (byte)255));

                // Position info
                Raylib.DrawText($"Pos: ({slot.PlayerX:F0}, {slot.PlayerY:F0}, {slot.PlayerZ:F0})",
                    textX + (int)(200 * _menuScale), sy + 30, 12,
                    new Color((byte)100, (byte)150, (byte)100, (byte)255));

                // Load button
                int btnX = x + contentWidth - (int)(130 * _menuScale);
                int btnW = (int)(50 * _menuScale);
                int btnH = (int)(25 * _menuScale);
                if (DrawButton(btnX, sy + 10, btnW, btnH, "Load", IsMouseInRect(btnX, sy + 10, btnW, btnH), 14))
                {
                    if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                    {
                        LoadSelectedSave(slot.SlotName);
                    }
                }

                // Delete button
                int delX = btnX + btnW + (int)(10 * _menuScale);
                if (DrawButton(delX, sy + 10, btnW, btnH, "Del", IsMouseInRect(delX, sy + 10, btnW, btnH), 14))
                {
                    if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                    {
                        _deleteConfirmSlot = slot.SlotName;
                    }
                }
            }

            // Pagination
            if (_saveSlots.Count > maxVisible)
            {
                int pageY = contentY + contentHeight - (int)(35 * _menuScale);
                int pageW = (int)(30 * _menuScale);

                // Previous page
                if (_loadPageOffset > 0)
                {
                    if (DrawButton(x + (int)(20 * _menuScale), pageY, pageW, (int)(25 * _menuScale), "<",
                        IsMouseInRect(x + (int)(20 * _menuScale), pageY, pageW, (int)(25 * _menuScale)), 16))
                    {
                        if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                            _loadPageOffset = Math.Max(0, _loadPageOffset - maxVisible);
                    }
                }

                // Page info
                int currentPage = (_loadPageOffset / maxVisible) + 1;
                int totalPages = ((_saveSlots.Count - 1) / maxVisible) + 1;
                Raylib.DrawText($"Page {currentPage}/{totalPages}", x + (int)(80 * _menuScale), pageY + 5, 14,
                    new Color((byte)150, (byte)150, (byte)150, (byte)255));

                // Next page
                if (_loadPageOffset + maxVisible < _saveSlots.Count)
                {
                    if (DrawButton(x + (int)(180 * _menuScale), pageY, pageW, (int)(25 * _menuScale), ">",
                        IsMouseInRect(x + (int)(180 * _menuScale), pageY, pageW, (int)(25 * _menuScale)), 16))
                    {
                        if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                            _loadPageOffset += maxVisible;
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (!string.IsNullOrEmpty(_deleteConfirmSlot))
        {
            DrawDeleteConfirmDialog(screenWidth, screenHeight);
        }
    }

    private void DrawDeleteConfirmDialog(int screenWidth, int screenHeight)
    {
        // Darken background
        Raylib.DrawRectangle(0, 0, screenWidth, screenHeight, new Color((byte)0, (byte)0, (byte)0, (byte)150));

        // Dialog box
        int dw = (int)(400 * _menuScale);
        int dh = (int)(150 * _menuScale);
        int dx = (screenWidth - dw) / 2;
        int dy = (screenHeight - dh) / 2;

        DrawFrame(dx, dy, dw, dh);

        // Message
        Raylib.DrawText("Delete this save?", dx + dw/2 - 80, dy + (int)(20 * _menuScale), 20, Color.White);
        Raylib.DrawText(_deleteConfirmSlot, dx + dw/2 - Raylib.MeasureText(_deleteConfirmSlot, 16)/2,
            dy + (int)(50 * _menuScale), 16, new Color((byte)255, (byte)200, (byte)100, (byte)255));

        // Buttons
        int btnW = (int)(100 * _menuScale);
        int btnH = (int)(35 * _menuScale);
        int btnY = dy + dh - btnH - (int)(20 * _menuScale);

        if (DrawButton(dx + (int)(50 * _menuScale), btnY, btnW, btnH, "DELETE",
            IsMouseInRect(dx + (int)(50 * _menuScale), btnY, btnW, btnH)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                SaveSystem.DeleteSave(_deleteConfirmSlot);
                _saveSlots = SaveSystem.GetSaveSlots();
                _deleteConfirmSlot = "";
            }
        }

        if (DrawButton(dx + dw - btnW - (int)(50 * _menuScale), btnY, btnW, btnH, "CANCEL",
            IsMouseInRect(dx + dw - btnW - (int)(50 * _menuScale), btnY, btnW, btnH)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                _deleteConfirmSlot = "";
            }
        }
    }

    private void LoadSelectedSave(string slotName)
    {
        Console.WriteLine($"[MainMenuScene] Loading save: {slotName}");
        // TODO: Transition to game with loaded save
        // For now just close menu
        _mainMenuTab = 0;
    }

    private void DrawOptionsMenu(int screenWidth, int screenHeight)
    {
        // Menu.bb lines 551-900 - Options menu
        int x = (int)(159 * _menuScale);
        int y = (int)(286 * _menuScale);
        int width = (int)(400 * _menuScale);
        int height = (int)(70 * _menuScale);

        // Title frame
        DrawFrame(x, y, width, height);
        Raylib.DrawText("OPTIONS", x + width/2 - 50, y + height/2 - 12, 24, Color.White);

        // Back button
        int backX = x + width + (int)(20 * _menuScale);
        int backW = (int)(180 * _menuScale);
        if (DrawButton(backX, y, backW, height, "BACK", IsMouseInRect(backX, y, backW, height)))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                _mainMenuTab = 0;
        }

        // Tab buttons
        int tabY = y + height + (int)(20 * _menuScale);
        int tabWidth = (int)(580 * _menuScale);
        int tabHeight = (int)(60 * _menuScale);
        DrawFrame(x, tabY, tabWidth, tabHeight);

        string[] tabs = { "GRAPHICS", "AUDIO", "CONTROLS", "ADVANCED" };
        int[] tabIds = { 3, 5, 6, 7 };
        int tabBtnWidth = (int)(tabWidth / 4.5f);

        for (int i = 0; i < tabs.Length; i++)
        {
            int btnX = x + (int)(20 * _menuScale) + i * (tabBtnWidth + (int)(5 * _menuScale));
            int btnY = tabY + (int)(10 * _menuScale);
            int btnH = (int)(40 * _menuScale);

            bool selected = _mainMenuTab == tabIds[i];
            bool hovered = IsMouseInRect(btnX, btnY, tabBtnWidth, btnH);

            if (selected)
            {
                Raylib.DrawRectangle(btnX - 5, btnY - 5, tabBtnWidth + 10, btnH + 10,
                    new Color((byte)0, (byte)100, (byte)0, (byte)255));
            }

            DrawButton(btnX, btnY, tabBtnWidth, btnH, tabs[i], hovered, 16);

            if (hovered && Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                _mainMenuTab = tabIds[i];
            }
        }

        // Content area
        int contentY = tabY + tabHeight + (int)(10 * _menuScale);
        int contentHeight = (int)(300 * _menuScale);
        DrawFrame(x, contentY, tabWidth, contentHeight);

        // Draw tab content
        switch (_mainMenuTab)
        {
            case 3: // Graphics
                DrawGraphicsOptions(x, contentY, tabWidth, contentHeight);
                break;
            case 5: // Audio
                DrawAudioOptions(x, contentY, tabWidth, contentHeight);
                break;
            case 6: // Controls
                DrawControlsOptions(x, contentY, tabWidth, contentHeight);
                break;
            case 7: // Advanced
                DrawAdvancedOptions(x, contentY, tabWidth, contentHeight);
                break;
        }
    }

    private void DrawGraphicsOptions(int x, int y, int w, int h)
    {
        int optX = x + (int)(20 * _menuScale);
        int optY = y + (int)(20 * _menuScale);

        Raylib.DrawText("VSync: [ON]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Anti-aliasing: [4x MSAA]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Room lights: [ON]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Screen gamma: [1.0]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Particle amount: [FULL]", optX, optY, 18, Color.White);

        Raylib.DrawText("(Options are placeholder - full implementation pending)",
            optX, y + h - 30, 14, new Color((byte)100, (byte)100, (byte)100, (byte)255));
    }

    private void DrawAudioOptions(int x, int y, int w, int h)
    {
        int optX = x + (int)(20 * _menuScale);
        int optY = y + (int)(20 * _menuScale);

        Raylib.DrawText("Music volume: [100%]", optX, optY, 18, Color.White);
        optY += (int)(40 * _menuScale);
        Raylib.DrawText("Sound volume: [100%]", optX, optY, 18, Color.White);
        optY += (int)(40 * _menuScale);
        Raylib.DrawText("Sound auto-release: [OFF]", optX, optY, 18, Color.White);
        optY += (int)(40 * _menuScale);
        Raylib.DrawText("User tracks: [OFF]", optX, optY, 18, Color.White);
    }

    private void DrawControlsOptions(int x, int y, int w, int h)
    {
        int optX = x + (int)(20 * _menuScale);
        int optY = y + (int)(20 * _menuScale);

        Raylib.DrawText("Mouse sensitivity: [1.0]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Invert mouse Y: [NO]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Mouse smoothing: [0.5]", optX, optY, 18, Color.White);

        optY += (int)(50 * _menuScale);
        Raylib.DrawText("Key bindings:", optX, optY, 18, Color.Yellow);
        optY += (int)(25 * _menuScale);

        string[] bindings = {
            "Move Forward: W", "Move Backward: S", "Strafe Left: A", "Strafe Right: D",
            "Sprint: SHIFT", "Crouch: CTRL", "Blink: SPACE", "Inventory: TAB"
        };

        for (int i = 0; i < bindings.Length; i++)
        {
            int col = i % 2;
            int row = i / 2;
            Raylib.DrawText(bindings[i], optX + col * (int)(280 * _menuScale),
                optY + row * (int)(22 * _menuScale), 14, new Color((byte)180, (byte)180, (byte)180, (byte)255));
        }
    }

    private void DrawAdvancedOptions(int x, int y, int w, int h)
    {
        int optX = x + (int)(20 * _menuScale);
        int optY = y + (int)(20 * _menuScale);

        Raylib.DrawText("Console auto-open: [OFF]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Show FPS: [ON]", optX, optY, 18, Color.White);
        optY += (int)(35 * _menuScale);
        Raylib.DrawText("Debug HUD: [ON]", optX, optY, 18, Color.White);
        optY += (int)(50 * _menuScale);

        Raylib.DrawText("Press F3 in-game to open console", optX, optY, 16,
            new Color((byte)150, (byte)150, (byte)150, (byte)255));
    }

    // UI Helper methods

    private void DrawFrame(int x, int y, int w, int h)
    {
        // Dark background
        Raylib.DrawRectangle(x, y, w, h, new Color((byte)0, (byte)0, (byte)0, (byte)200));
        // Border
        Raylib.DrawRectangleLines(x, y, w, h, new Color((byte)100, (byte)100, (byte)100, (byte)255));
        Raylib.DrawRectangleLines(x + 1, y + 1, w - 2, h - 2, new Color((byte)60, (byte)60, (byte)60, (byte)255));
    }

    private bool DrawButton(int x, int y, int w, int h, string text, bool hovered, int fontSize = 20)
    {
        Color bgColor = hovered ?
            new Color((byte)60, (byte)60, (byte)60, (byte)255) :
            new Color((byte)30, (byte)30, (byte)30, (byte)255);
        Color borderColor = hovered ?
            new Color((byte)150, (byte)150, (byte)150, (byte)255) :
            new Color((byte)100, (byte)100, (byte)100, (byte)255);
        Color textColor = hovered ? Color.White : new Color((byte)200, (byte)200, (byte)200, (byte)255);

        Raylib.DrawRectangle(x, y, w, h, bgColor);
        Raylib.DrawRectangleLines(x, y, w, h, borderColor);

        int textWidth = Raylib.MeasureText(text, fontSize);
        Raylib.DrawText(text, x + (w - textWidth) / 2, y + (h - fontSize) / 2, fontSize, textColor);

        return hovered;
    }

    private bool DrawTick(int x, int y, bool selected)
    {
        int size = (int)(20 * _menuScale);
        bool hovered = IsMouseInRect(x, y, size, size);

        Color boxColor = hovered ?
            new Color((byte)80, (byte)80, (byte)80, (byte)255) :
            new Color((byte)50, (byte)50, (byte)50, (byte)255);

        Raylib.DrawRectangle(x, y, size, size, boxColor);
        Raylib.DrawRectangleLines(x, y, size, size, Color.White);

        if (selected)
        {
            Raylib.DrawRectangle(x + 4, y + 4, size - 8, size - 8, Color.White);
        }

        return hovered;
    }

    private void DrawInputBox(int x, int y, int w, int h, string text, int fieldId)
    {
        bool hovered = IsMouseInRect(x, y, w, h);
        bool active = _activeInputField == fieldId;

        Color bgColor = active ?
            new Color((byte)40, (byte)40, (byte)40, (byte)255) :
            new Color((byte)20, (byte)20, (byte)20, (byte)255);
        Color borderColor = active ? Color.White :
            (hovered ? new Color((byte)150, (byte)150, (byte)150, (byte)255) :
                       new Color((byte)100, (byte)100, (byte)100, (byte)255));

        Raylib.DrawRectangle(x, y, w, h, bgColor);
        Raylib.DrawRectangleLines(x, y, w, h, borderColor);

        string displayText = text;
        if (active && (int)(_animationTime * 2) % 2 == 0)
            displayText += "_";

        Raylib.DrawText(displayText, x + 5, y + (h - 16) / 2, 16, Color.White);

        if (hovered && Raylib.IsMouseButtonPressed(MouseButton.Left))
        {
            _activeInputField = fieldId;
        }
    }

    private void DrawCursor()
    {
        int mx = Raylib.GetMouseX();
        int my = Raylib.GetMouseY();

        // Simple arrow cursor
        Raylib.DrawTriangle(
            new System.Numerics.Vector2(mx, my),
            new System.Numerics.Vector2(mx, my + 20),
            new System.Numerics.Vector2(mx + 14, my + 14),
            Color.White);
        Raylib.DrawTriangleLines(
            new System.Numerics.Vector2(mx, my),
            new System.Numerics.Vector2(mx, my + 20),
            new System.Numerics.Vector2(mx + 14, my + 14),
            Color.Black);
    }

    private bool IsMouseInRect(int x, int y, int w, int h)
    {
        int mx = Raylib.GetMouseX();
        int my = Raylib.GetMouseY();
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public void Unload()
    {
        IsLoaded = false;
        Console.WriteLine("[MainMenuScene] Unloaded");
    }
}

// Difficulty definition structure
internal record DifficultyDef(string Name, int R, int G, int B, string Description);
