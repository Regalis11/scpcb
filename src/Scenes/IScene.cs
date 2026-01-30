namespace SCPContainmentBreach.Scenes;

/// <summary>
/// Интерфейс сцены. Каждая сцена представляет отдельный экран игры
/// (главное меню, игровой мир, экран загрузки и т.д.)
/// </summary>
public interface IScene
{
    /// <summary>
    /// Имя сцены для идентификации
    /// </summary>
    string Name { get; }

    /// <summary>
    /// Загружена ли сцена
    /// </summary>
    bool IsLoaded { get; }

    /// <summary>
    /// Вызывается при загрузке сцены
    /// </summary>
    void Load();

    /// <summary>
    /// Вызывается каждый кадр для обновления логики
    /// </summary>
    /// <param name="deltaTime">Время с предыдущего кадра в секундах</param>
    void Update(float deltaTime);

    /// <summary>
    /// Вызывается каждый кадр для отрисовки
    /// </summary>
    void Draw();

    /// <summary>
    /// Вызывается при выгрузке сцены
    /// </summary>
    void Unload();
}
