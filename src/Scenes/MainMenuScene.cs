using Raylib_cs;
using SCPContainmentBreach.Core;

namespace SCPContainmentBreach.Scenes;

/// <summary>
/// Главное меню игры.
/// Воссоздаёт атмосферу оригинального SCP:CB.
/// </summary>
public class MainMenuScene : IScene
{
    public string Name => "MainMenu";
    public bool IsLoaded { get; private set; }

    // UI элементы
    private readonly string[] _menuItems = { "NEW GAME", "LOAD GAME", "OPTIONS", "QUIT" };
    private int _selectedIndex;
    private float _animationTime;

    // Визуальные настройки
    private const int FontSize = 32;
    private const int MenuStartY = 300;
    private const int MenuItemSpacing = 50;

    public void Load()
    {
        IsLoaded = true;
        _selectedIndex = 0;
        _animationTime = 0;

        // Показать курсор в меню
        Raylib.ShowCursor();

        Console.WriteLine("[MainMenuScene] Loaded");
    }

    public void Update(float deltaTime)
    {
        _animationTime += deltaTime;

        // Навигация по меню
        if (Raylib.IsKeyPressed(KeyboardKey.Up) || Raylib.IsKeyPressed(KeyboardKey.W))
        {
            _selectedIndex--;
            if (_selectedIndex < 0) _selectedIndex = _menuItems.Length - 1;
        }

        if (Raylib.IsKeyPressed(KeyboardKey.Down) || Raylib.IsKeyPressed(KeyboardKey.S))
        {
            _selectedIndex++;
            if (_selectedIndex >= _menuItems.Length) _selectedIndex = 0;
        }

        // Выбор пункта меню
        if (Raylib.IsKeyPressed(KeyboardKey.Enter) || Raylib.IsKeyPressed(KeyboardKey.Space))
        {
            SelectMenuItem(_selectedIndex);
        }

        // Клик мышью
        HandleMouseInput();
    }

    private void HandleMouseInput()
    {
        int mouseY = Raylib.GetMouseY();
        int screenWidth = Raylib.GetScreenWidth();

        for (int i = 0; i < _menuItems.Length; i++)
        {
            int itemY = MenuStartY + i * MenuItemSpacing;
            int textWidth = Raylib.MeasureText(_menuItems[i], FontSize);
            int textX = (screenWidth - textWidth) / 2;

            // Проверка hover
            if (Raylib.GetMouseX() >= textX - 20 &&
                Raylib.GetMouseX() <= textX + textWidth + 20 &&
                mouseY >= itemY - 5 &&
                mouseY <= itemY + FontSize + 5)
            {
                _selectedIndex = i;

                if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                {
                    SelectMenuItem(i);
                }
            }
        }
    }

    private void SelectMenuItem(int index)
    {
        switch (index)
        {
            case 0: // NEW GAME
                Console.WriteLine("[MainMenuScene] Starting new game...");
                Game.Instance?.SceneManager.LoadScene("Game");
                break;

            case 1: // LOAD GAME
                Console.WriteLine("[MainMenuScene] Load game - not implemented yet");
                break;

            case 2: // OPTIONS
                Console.WriteLine("[MainMenuScene] Options - not implemented yet");
                break;

            case 3: // QUIT
                Game.Instance?.Exit();
                break;
        }
    }

    public void Draw()
    {
        int screenWidth = Raylib.GetScreenWidth();
        int screenHeight = Raylib.GetScreenHeight();

        // Фон - градиент тёмно-серый
        Raylib.DrawRectangleGradientV(0, 0, screenWidth, screenHeight,
            new Color(20, 20, 25, 255),
            new Color(5, 5, 10, 255));

        // Заголовок
        string title = "SCP: CONTAINMENT BREACH";
        int titleWidth = Raylib.MeasureText(title, 48);
        float pulse = (float)(Math.Sin(_animationTime * 2) * 0.3 + 0.7);
        byte titleAlpha = (byte)(255 * pulse);

        Raylib.DrawText(title,
            (screenWidth - titleWidth) / 2, 150,
            48, new Color(200, 50, 50, titleAlpha));

        // Подзаголовок
        string subtitle = "C# Raylib Port";
        int subtitleWidth = Raylib.MeasureText(subtitle, 20);
        Raylib.DrawText(subtitle,
            (screenWidth - subtitleWidth) / 2, 210,
            20, new Color(150, 150, 150, 255));

        // Пункты меню
        for (int i = 0; i < _menuItems.Length; i++)
        {
            int textWidth = Raylib.MeasureText(_menuItems[i], FontSize);
            int x = (screenWidth - textWidth) / 2;
            int y = MenuStartY + i * MenuItemSpacing;

            Color color;
            if (i == _selectedIndex)
            {
                // Выбранный пункт - анимированный
                float blink = (float)(Math.Sin(_animationTime * 5) * 0.2 + 0.8);
                color = new Color((byte)(255 * blink), (byte)(100 * blink), (byte)(100 * blink), 255);

                // Индикатор выбора
                Raylib.DrawText(">", x - 30, y, FontSize, color);
                Raylib.DrawText("<", x + textWidth + 10, y, FontSize, color);
            }
            else
            {
                color = new Color(180, 180, 180, 255);
            }

            Raylib.DrawText(_menuItems[i], x, y, FontSize, color);
        }

        // Подсказка
        string hint = "Use Arrow Keys or Mouse to navigate, Enter to select";
        int hintWidth = Raylib.MeasureText(hint, 16);
        Raylib.DrawText(hint,
            (screenWidth - hintWidth) / 2, screenHeight - 50,
            16, new Color(100, 100, 100, 255));

        // Версия
        Raylib.DrawText("v0.1.0-alpha", 10, screenHeight - 30, 16, new Color(80, 80, 80, 255));
    }

    public void Unload()
    {
        IsLoaded = false;
        Console.WriteLine("[MainMenuScene] Unloaded");
    }
}
