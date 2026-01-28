using Raylib_cs;
using SCPContainmentBreach.Scenes;
using System.Numerics;

namespace SCPContainmentBreach.Core;

/// <summary>
/// Главный класс игры. Управляет жизненным циклом приложения.
/// Паттерн: Game Loop с фиксированным Update и переменным Draw.
/// </summary>
public class Game : IDisposable
{
    // Конфигурация окна
    public const int DefaultScreenWidth = 1280;
    public const int DefaultScreenHeight = 720;
    public const string GameTitle = "SCP: Containment Breach";
    public const int TargetFPS = 60;

    // Состояние игры
    private bool _isRunning;
    private bool _isDisposed;

    // Менеджеры
    private readonly SceneManager _sceneManager;

    // Время
    public float DeltaTime { get; private set; }
    public float TotalTime { get; private set; }
    public int CurrentFPS => Raylib.GetFPS();

    // Singleton для доступа из других частей кода
    public static Game? Instance { get; private set; }

    public Game()
    {
        Instance = this;
        _sceneManager = new SceneManager();
    }

    /// <summary>
    /// Запуск игры
    /// </summary>
    public void Run()
    {
        Initialize();
        LoadContent();

        _isRunning = true;

        while (_isRunning && !Raylib.WindowShouldClose())
        {
            DeltaTime = Raylib.GetFrameTime();
            TotalTime += DeltaTime;

            Update(DeltaTime);
            Draw();
        }

        Unload();
    }

    /// <summary>
    /// Инициализация окна и Raylib
    /// </summary>
    private void Initialize()
    {
        // Настройка конфигурации до создания окна
        Raylib.SetConfigFlags(ConfigFlags.ResizableWindow | ConfigFlags.Msaa4xHint);

        // Создание окна
        Raylib.InitWindow(DefaultScreenWidth, DefaultScreenHeight, GameTitle);
        Raylib.SetTargetFPS(TargetFPS);

        // Инициализация аудио
        Raylib.InitAudioDevice();

        // Скрыть курсор для FPS-режима (опционально, включим позже)
        // Raylib.DisableCursor();

        Console.WriteLine($"[Game] Initialized: {DefaultScreenWidth}x{DefaultScreenHeight}");
        Console.WriteLine($"[Game] OpenGL Version: {Raylib.GetGPUVendor()}");
    }

    /// <summary>
    /// Загрузка контента (текстуры, модели, звуки)
    /// </summary>
    private void LoadContent()
    {
        Console.WriteLine("[Game] Loading content...");

        // Регистрируем сцены
        _sceneManager.RegisterScene("MainMenu", new MainMenuScene());
        _sceneManager.RegisterScene("Game", new GameWorldScene());

        // Загружаем начальную сцену
        _sceneManager.LoadScene("MainMenu");

        Console.WriteLine("[Game] Content loaded successfully");
    }

    /// <summary>
    /// Обновление игровой логики
    /// </summary>
    private void Update(float deltaTime)
    {
        // Глобальные хоткеи
        HandleGlobalInput();

        // Обновление текущей сцены
        _sceneManager.Update(deltaTime);
    }

    /// <summary>
    /// Отрисовка кадра
    /// </summary>
    private void Draw()
    {
        Raylib.BeginDrawing();
        Raylib.ClearBackground(Color.Black);

        // Отрисовка текущей сцены
        _sceneManager.Draw();

        // Debug overlay
        DrawDebugInfo();

        Raylib.EndDrawing();
    }

    /// <summary>
    /// Обработка глобального ввода
    /// </summary>
    private void HandleGlobalInput()
    {
        // F11 - Fullscreen toggle
        if (Raylib.IsKeyPressed(KeyboardKey.F11))
        {
            Raylib.ToggleFullscreen();
        }

        // Escape - выход (временно, потом будет меню)
        if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            if (_sceneManager.CurrentSceneName == "Game")
            {
                _sceneManager.LoadScene("MainMenu");
            }
            else
            {
                _isRunning = false;
            }
        }
    }

    /// <summary>
    /// Отрисовка отладочной информации
    /// </summary>
    private void DrawDebugInfo()
    {
        #if DEBUG
        int y = 10;
        Raylib.DrawText($"FPS: {CurrentFPS}", 10, y, 20, Color.Lime);
        y += 25;
        Raylib.DrawText($"Scene: {_sceneManager.CurrentSceneName}", 10, y, 20, Color.Lime);
        y += 25;
        Raylib.DrawText($"Time: {TotalTime:F1}s", 10, y, 20, Color.Lime);
        #endif
    }

    /// <summary>
    /// Выгрузка ресурсов
    /// </summary>
    private void Unload()
    {
        Console.WriteLine("[Game] Unloading...");

        _sceneManager.UnloadAll();

        Raylib.CloseAudioDevice();
        Raylib.CloseWindow();

        Console.WriteLine("[Game] Shutdown complete");
    }

    /// <summary>
    /// Остановка игры
    /// </summary>
    public void Exit()
    {
        _isRunning = false;
    }

    /// <summary>
    /// Доступ к SceneManager
    /// </summary>
    public SceneManager SceneManager => _sceneManager;

    public void Dispose()
    {
        if (_isDisposed) return;

        Unload();
        _isDisposed = true;

        GC.SuppressFinalize(this);
    }
}
