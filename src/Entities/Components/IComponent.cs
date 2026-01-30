namespace SCPContainmentBreach.Entities.Components;

/// <summary>
/// Интерфейс компонента Entity.
/// Компоненты добавляют функциональность к Entity (MeshRenderer, Collider, AI и т.д.)
/// </summary>
public interface IComponent
{
    /// <summary>
    /// Entity-владелец этого компонента
    /// </summary>
    Entity? Entity { get; set; }

    /// <summary>
    /// Включен ли компонент
    /// </summary>
    bool IsEnabled { get; set; }

    /// <summary>
    /// Вызывается при добавлении компонента к Entity
    /// </summary>
    void OnAttach();

    /// <summary>
    /// Вызывается при удалении компонента
    /// </summary>
    void OnDetach();

    /// <summary>
    /// Обновление каждый кадр
    /// </summary>
    void Update(float deltaTime);

    /// <summary>
    /// Отрисовка каждый кадр
    /// </summary>
    void Draw();
}

/// <summary>
/// Базовая реализация компонента
/// </summary>
public abstract class ComponentBase : IComponent
{
    public Entity? Entity { get; set; }
    public bool IsEnabled { get; set; } = true;

    /// <summary>
    /// Быстрый доступ к Transform
    /// </summary>
    protected Transform Transform => Entity!.Transform;

    public virtual void OnAttach() { }
    public virtual void OnDetach() { }
    public virtual void Update(float deltaTime) { }
    public virtual void Draw() { }
}
