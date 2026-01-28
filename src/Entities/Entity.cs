using System.Numerics;
using SCPContainmentBreach.Entities.Components;

namespace SCPContainmentBreach.Entities;

/// <summary>
/// Базовый класс Entity - аналог Blitz3D Entity.
/// Использует компонентный подход для расширения функциональности.
///
/// Blitz3D Entity типы, которые мы воссоздаём:
/// - Pivot (пустой контейнер для иерархии)
/// - Mesh (3D модель)
/// - Light (источник света)
/// - Camera (камера)
/// - Sprite (2D спрайт в 3D пространстве)
/// </summary>
public class Entity : IDisposable
{
    private static int _nextId = 1;

    // Идентификация
    public int Id { get; }
    public string Name { get; set; }
    public string Tag { get; set; } = "";
    public bool IsActive { get; set; } = true;

    // Transform
    public Transform Transform { get; }

    // Иерархия (как в Blitz3D EntityParent)
    public Entity? Parent { get; private set; }
    private readonly List<Entity> _children = new();
    public IReadOnlyList<Entity> Children => _children;

    // Компоненты
    private readonly Dictionary<Type, IComponent> _components = new();

    // События
    public event Action<Entity>? OnDestroyed;

    public Entity(string name = "Entity")
    {
        Id = _nextId++;
        Name = name;
        Transform = new Transform(this);
    }

    #region Hierarchy (аналог EntityParent в Blitz3D)

    /// <summary>
    /// Устанавливает родителя (EntityParent в Blitz3D)
    /// </summary>
    public void SetParent(Entity? parent, bool worldPositionStays = true)
    {
        if (parent == this) return;

        Vector3 worldPosition = Transform.WorldPosition;
        Quaternion worldRotation = Transform.WorldRotation;

        // Удаляем из старого родителя
        Parent?._children.Remove(this);

        // Добавляем к новому
        Parent = parent;
        parent?._children.Add(this);

        // Сохраняем мировую позицию если нужно
        if (worldPositionStays && parent != null)
        {
            Transform.WorldPosition = worldPosition;
            Transform.WorldRotation = worldRotation;
        }
    }

    /// <summary>
    /// Находит потомка по имени (рекурсивно)
    /// </summary>
    public Entity? FindChild(string name, bool recursive = true)
    {
        foreach (var child in _children)
        {
            if (child.Name == name) return child;

            if (recursive)
            {
                var found = child.FindChild(name, true);
                if (found != null) return found;
            }
        }
        return null;
    }

    #endregion

    #region Components

    /// <summary>
    /// Добавляет компонент
    /// </summary>
    public T AddComponent<T>() where T : IComponent, new()
    {
        var component = new T();
        component.Entity = this;
        _components[typeof(T)] = component;
        component.OnAttach();
        return component;
    }

    /// <summary>
    /// Добавляет существующий компонент
    /// </summary>
    public T AddComponent<T>(T component) where T : IComponent
    {
        component.Entity = this;
        _components[typeof(T)] = component;
        component.OnAttach();
        return component;
    }

    /// <summary>
    /// Получает компонент
    /// </summary>
    public T? GetComponent<T>() where T : IComponent
    {
        return _components.TryGetValue(typeof(T), out var component)
            ? (T)component
            : default;
    }

    /// <summary>
    /// Проверяет наличие компонента
    /// </summary>
    public bool HasComponent<T>() where T : IComponent
    {
        return _components.ContainsKey(typeof(T));
    }

    /// <summary>
    /// Удаляет компонент
    /// </summary>
    public bool RemoveComponent<T>() where T : IComponent
    {
        if (_components.TryGetValue(typeof(T), out var component))
        {
            component.OnDetach();
            _components.Remove(typeof(T));
            return true;
        }
        return false;
    }

    #endregion

    #region Update & Draw

    /// <summary>
    /// Обновление entity и всех компонентов
    /// </summary>
    public virtual void Update(float deltaTime)
    {
        if (!IsActive) return;

        foreach (var component in _components.Values)
        {
            if (component.IsEnabled)
            {
                component.Update(deltaTime);
            }
        }

        // Обновляем детей
        foreach (var child in _children)
        {
            child.Update(deltaTime);
        }
    }

    /// <summary>
    /// Отрисовка entity
    /// </summary>
    public virtual void Draw()
    {
        if (!IsActive) return;

        foreach (var component in _components.Values)
        {
            if (component.IsEnabled)
            {
                component.Draw();
            }
        }

        // Рисуем детей
        foreach (var child in _children)
        {
            child.Draw();
        }
    }

    #endregion

    #region Blitz3D-style convenience methods

    /// <summary>
    /// Устанавливает позицию (PositionEntity в Blitz3D)
    /// </summary>
    public void SetPosition(float x, float y, float z, bool global = false)
    {
        if (global)
            Transform.WorldPosition = new Vector3(x, y, z);
        else
            Transform.LocalPosition = new Vector3(x, y, z);
    }

    /// <summary>
    /// Перемещает entity (MoveEntity в Blitz3D)
    /// </summary>
    public void Move(float x, float y, float z)
    {
        Transform.Translate(new Vector3(x, y, z));
    }

    /// <summary>
    /// Поворачивает entity (RotateEntity в Blitz3D)
    /// </summary>
    public void SetRotation(float pitch, float yaw, float roll, bool global = false)
    {
        var rotation = Quaternion.CreateFromYawPitchRoll(
            yaw * MathF.PI / 180f,
            pitch * MathF.PI / 180f,
            roll * MathF.PI / 180f
        );

        if (global)
            Transform.WorldRotation = rotation;
        else
            Transform.LocalRotation = rotation;
    }

    /// <summary>
    /// Направляет entity на точку (PointEntity в Blitz3D)
    /// </summary>
    public void LookAt(Vector3 target)
    {
        Transform.LookAt(target);
    }

    /// <summary>
    /// Масштабирует entity (ScaleEntity в Blitz3D)
    /// </summary>
    public void SetScale(float x, float y, float z)
    {
        Transform.LocalScale = new Vector3(x, y, z);
    }

    #endregion

    public virtual void Dispose()
    {
        OnDestroyed?.Invoke(this);

        // Dispose компоненты
        foreach (var component in _components.Values)
        {
            component.OnDetach();
            if (component is IDisposable disposable)
            {
                disposable.Dispose();
            }
        }
        _components.Clear();

        // Dispose детей
        foreach (var child in _children.ToList())
        {
            child.Dispose();
        }
        _children.Clear();

        // Удаляем из родителя
        Parent?._children.Remove(this);
        Parent = null;

        GC.SuppressFinalize(this);
    }

    public override string ToString()
    {
        return $"Entity({Id}: {Name})";
    }
}
