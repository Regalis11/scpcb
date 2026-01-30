namespace SCPContainmentBreach.Scenes;

/// <summary>
/// Менеджер сцен. Управляет переключением между сценами игры.
/// Поддерживает:
/// - Регистрацию сцен по имени
/// - Плавные переходы между сценами
/// - Стек сцен для паузы/меню
/// </summary>
public class SceneManager
{
    private readonly Dictionary<string, IScene> _scenes = new();
    private IScene? _currentScene;
    private IScene? _pendingScene;
    private bool _isTransitioning;

    // Параметры перехода
    private float _transitionAlpha;
    private float _transitionSpeed = 2.0f;
    private TransitionState _transitionState = TransitionState.None;

    public string CurrentSceneName => _currentScene?.Name ?? "None";
    public bool IsTransitioning => _isTransitioning;

    private enum TransitionState
    {
        None,
        FadeOut,
        FadeIn
    }

    /// <summary>
    /// Регистрирует сцену в менеджере
    /// </summary>
    public void RegisterScene(string name, IScene scene)
    {
        if (_scenes.ContainsKey(name))
        {
            Console.WriteLine($"[SceneManager] Warning: Scene '{name}' already registered, replacing");
        }

        _scenes[name] = scene;
        Console.WriteLine($"[SceneManager] Registered scene: {name}");
    }

    /// <summary>
    /// Загружает сцену по имени
    /// </summary>
    /// <param name="name">Имя сцены</param>
    /// <param name="instant">Мгновенный переход без fade</param>
    public void LoadScene(string name, bool instant = false)
    {
        if (!_scenes.TryGetValue(name, out var scene))
        {
            Console.WriteLine($"[SceneManager] Error: Scene '{name}' not found!");
            return;
        }

        if (instant || _currentScene == null)
        {
            // Мгновенный переход
            _currentScene?.Unload();
            _currentScene = scene;
            _currentScene.Load();
            Console.WriteLine($"[SceneManager] Loaded scene: {name}");
        }
        else
        {
            // Переход с fade
            _pendingScene = scene;
            _isTransitioning = true;
            _transitionState = TransitionState.FadeOut;
            _transitionAlpha = 0f;
            Console.WriteLine($"[SceneManager] Starting transition to: {name}");
        }
    }

    /// <summary>
    /// Обновление логики
    /// </summary>
    public void Update(float deltaTime)
    {
        // Обработка перехода
        if (_isTransitioning)
        {
            UpdateTransition(deltaTime);
        }

        // Обновление текущей сцены
        _currentScene?.Update(deltaTime);
    }

    /// <summary>
    /// Обновление анимации перехода
    /// </summary>
    private void UpdateTransition(float deltaTime)
    {
        switch (_transitionState)
        {
            case TransitionState.FadeOut:
                _transitionAlpha += _transitionSpeed * deltaTime;
                if (_transitionAlpha >= 1f)
                {
                    _transitionAlpha = 1f;
                    // Меняем сцену
                    _currentScene?.Unload();
                    _currentScene = _pendingScene;
                    _currentScene?.Load();
                    _pendingScene = null;
                    _transitionState = TransitionState.FadeIn;
                }
                break;

            case TransitionState.FadeIn:
                _transitionAlpha -= _transitionSpeed * deltaTime;
                if (_transitionAlpha <= 0f)
                {
                    _transitionAlpha = 0f;
                    _transitionState = TransitionState.None;
                    _isTransitioning = false;
                    Console.WriteLine($"[SceneManager] Transition complete: {_currentScene?.Name}");
                }
                break;
        }
    }

    /// <summary>
    /// Отрисовка
    /// </summary>
    public void Draw()
    {
        // Отрисовка текущей сцены
        _currentScene?.Draw();

        // Отрисовка overlay перехода
        if (_isTransitioning && _transitionAlpha > 0)
        {
            DrawTransitionOverlay();
        }
    }

    /// <summary>
    /// Отрисовка overlay затемнения при переходе
    /// </summary>
    private void DrawTransitionOverlay()
    {
        byte alpha = (byte)(_transitionAlpha * 255);
        Raylib_cs.Raylib.DrawRectangle(
            0, 0,
            Raylib_cs.Raylib.GetScreenWidth(),
            Raylib_cs.Raylib.GetScreenHeight(),
            new Raylib_cs.Color(0, 0, 0, alpha)
        );
    }

    /// <summary>
    /// Выгрузка всех сцен
    /// </summary>
    public void UnloadAll()
    {
        foreach (var scene in _scenes.Values)
        {
            if (scene.IsLoaded)
            {
                scene.Unload();
            }
        }
        _scenes.Clear();
        _currentScene = null;
        Console.WriteLine("[SceneManager] All scenes unloaded");
    }

    /// <summary>
    /// Получить сцену по имени
    /// </summary>
    public IScene? GetScene(string name)
    {
        return _scenes.GetValueOrDefault(name);
    }
}
