/*
 * Source: Main.bb (lines 4748-4790, 3268-3295, 5245-5420)
 * Menu.bb (lines 1665-1670 DrawFrame)
 * Type: HUD Rendering System
 */

using System;
using System.Numerics;
using Raylib_cs;
using SCPContainmentBreach.Core.Types;
using SCPContainmentBreach.Core.Systems;
using SCPContainmentBreach.Core.Engine;

namespace SCPContainmentBreach.Core.UI;

public static class HUDSystem
{
    private static Texture2D _blinkMeterImg;
    private static Texture2D _blinkIcon;
    private static Texture2D _staminaMeterImg;
    private static Texture2D _sprintIcon;
    private static Texture2D _crouchIcon;
    private static Texture2D _handIcon;
    private static Texture2D _handIcon2;
    private static Texture2D _cursorImg;
    private static Texture2D _menuWhite;
    private static Texture2D _menuBlack;

    private static bool _initialized;
    private static int _mouseSlot = 66;

    public static float MenuScale { get; private set; } = 1.0f;
    public static bool HUDEnabled { get; set; } = true;
    public static bool DebugHUD { get; set; }

    public static void Initialize()
    {
        if (_initialized) return;

        int screenHeight = Raylib.GetScreenHeight();
        MenuScale = screenHeight / 1024.0f;

        _blinkMeterImg = AssetManager.LoadTexture("GFX/blinkmeter.jpg");
        _blinkIcon = AssetManager.LoadTexture("GFX/blinkicon.png");
        _staminaMeterImg = AssetManager.LoadTexture("GFX/staminameter.jpg");
        _sprintIcon = AssetManager.LoadTexture("GFX/sprinticon.png");
        _crouchIcon = AssetManager.LoadTexture("GFX/sneakicon.png");
        _handIcon = AssetManager.LoadTexture("GFX/handsymbol.png");
        _handIcon2 = AssetManager.LoadTexture("GFX/handsymbol2.png");
        _cursorImg = AssetManager.LoadTexture("GFX/cursor.png");
        _menuWhite = AssetManager.LoadTexture("GFX/menu/menuwhite.jpg");
        _menuBlack = AssetManager.LoadTexture("GFX/menu/menublack.jpg");

        _initialized = true;
    }

    public static void Draw(PlayerState player, MenuState menu, InputState input)
    {
        if (!_initialized) return;

        int graphicWidth = Raylib.GetScreenWidth();
        int graphicHeight = Raylib.GetScreenHeight();

        if (HUDEnabled && player.KillTimer >= 0)
        {
            DrawBlinkMeter(player, graphicWidth, graphicHeight);
            DrawStaminaMeter(player, graphicWidth, graphicHeight);
        }

        DrawInteractionIcons(graphicWidth, graphicHeight, menu);
        DrawMessage(player, menu, graphicWidth, graphicHeight);

        if (menu.InvOpen != 0)
        {
            DrawInventory(player, menu, input, graphicWidth, graphicHeight);
        }

        if (DebugHUD)
        {
            DrawDebugInfo(player, graphicWidth, graphicHeight);
        }
    }

    /// <summary>
    /// Main.bb lines 4748-4770: Blink meter drawing
    /// </summary>
    private static void DrawBlinkMeter(PlayerState player, int graphicWidth, int graphicHeight)
    {
        int width = 204;
        int height = 20;
        int x = 80;
        int y = graphicHeight - 95;

        // Color 255, 255, 255 / Rect(x, y, width, height, False) - outline only
        Raylib.DrawRectangleLines(x, y, width, height, Color.White);

        // For i = 1 To Int(((width - 2) * (BlinkTimer / (BLINKFREQ))) / 10)
        float blinkRatio = player.BLINKFREQ > 0 ? player.BlinkTimer / player.BLINKFREQ : 0;
        int segmentCount = (int)(((width - 2) * blinkRatio) / 10);

        for (int i = 1; i <= segmentCount; i++)
        {
            // DrawImage(BlinkMeterIMG, x + 3 + 10 * (i - 1), y + 3)
            int segX = x + 3 + 10 * (i - 1);
            int segY = y + 3;

            if (AssetManager.IsTextureValid(_blinkMeterImg))
            {
                Raylib.DrawTexture(_blinkMeterImg, segX, segY, Color.White);
            }
            else
            {
                Raylib.DrawRectangle(segX, segY, 8, 14, new Color(100, 149, 237, 255));
            }
        }

        // Color 0, 0, 0 / Rect(x - 50, y, 30, 30) - filled black rect for icon bg
        Raylib.DrawRectangle(x - 50, y, 30, 30, Color.Black);

        // If EyeIrritation > 0 Then Color 200, 0, 0 / Rect(x - 50 - 3, y - 3, 30 + 6, 30 + 6)
        if (player.EyeIrritation > 0)
        {
            Raylib.DrawRectangle(x - 50 - 3, y - 3, 30 + 6, 30 + 6, new Color(200, 0, 0, 255));
        }

        // Color 255, 255, 255 / Rect(x - 50 - 1, y - 1, 30 + 2, 30 + 2, False) - white outline
        Raylib.DrawRectangleLines(x - 50 - 1, y - 1, 30 + 2, 30 + 2, Color.White);

        // DrawImage BlinkIcon, x - 50, y
        if (AssetManager.IsTextureValid(_blinkIcon))
        {
            Raylib.DrawTexture(_blinkIcon, x - 50, y, Color.White);
        }
    }

    /// <summary>
    /// Main.bb lines 4772-4788: Stamina meter drawing
    /// </summary>
    private static void DrawStaminaMeter(PlayerState player, int graphicWidth, int graphicHeight)
    {
        int width = 204;
        int height = 20;
        int x = 80;
        int y = graphicHeight - 55;

        // Color 255, 255, 255 / Rect (x, y, width, height, False)
        Raylib.DrawRectangleLines(x, y, width, height, Color.White);

        // For i = 1 To Int(((width - 2) * (Stamina / 100.0)) / 10)
        float staminaRatio = player.Stamina / 100.0f;
        int segmentCount = (int)(((width - 2) * staminaRatio) / 10);

        for (int i = 1; i <= segmentCount; i++)
        {
            // DrawImage(StaminaMeterIMG, x + 3 + 10 * (i - 1), y + 3)
            int segX = x + 3 + 10 * (i - 1);
            int segY = y + 3;

            if (AssetManager.IsTextureValid(_staminaMeterImg))
            {
                Raylib.DrawTexture(_staminaMeterImg, segX, segY, Color.White);
            }
            else
            {
                // Fallback: green segments, red when low stamina
                Color segColor = player.Stamina < 10 ? new Color(200, 50, 50, 255) : new Color(50, 200, 50, 255);
                Raylib.DrawRectangle(segX, segY, 8, 14, segColor);
            }
        }

        // Color 0, 0, 0 / Rect(x - 50, y, 30, 30)
        Raylib.DrawRectangle(x - 50, y, 30, 30, Color.Black);

        // Color 255, 255, 255 / Rect(x - 50 - 1, y - 1, 30 + 2, 30 + 2, False)
        Raylib.DrawRectangleLines(x - 50 - 1, y - 1, 30 + 2, 30 + 2, Color.White);

        // If Crouch Then DrawImage CrouchIcon Else DrawImage SprintIcon
        if (player.Crouch != 0)
        {
            if (AssetManager.IsTextureValid(_crouchIcon))
            {
                Raylib.DrawTexture(_crouchIcon, x - 50, y, Color.White);
            }
        }
        else
        {
            if (AssetManager.IsTextureValid(_sprintIcon))
            {
                Raylib.DrawTexture(_sprintIcon, x - 50, y, Color.White);
            }
        }
    }

    /// <summary>
    /// Main.bb lines 4697-4723: Hand icon drawing for interactions
    /// </summary>
    private static void DrawInteractionIcons(int graphicWidth, int graphicHeight, MenuState menu)
    {
        int centerX = graphicWidth / 2;
        int centerY = graphicHeight / 2;

        // If DrawHandIcon Then DrawImage(HandIcon, GraphicWidth / 2 - 32, GraphicHeight / 2 - 32)
        if (menu.DrawHandIcon != 0)
        {
            if (AssetManager.IsTextureValid(_handIcon))
            {
                Raylib.DrawTexture(_handIcon, centerX - 32, centerY - 32, Color.White);
            }
        }

        // ClosestItem check: draw HandIcon2 for items
        if (InteractionSystem.HasTarget)
        {
            var target = InteractionSystem.CurrentTarget;

            if (target.Type == InteractableType.Item)
            {
                if (AssetManager.IsTextureValid(_handIcon2))
                {
                    Raylib.DrawTexture(_handIcon2, centerX - 32, centerY - 32, Color.White);
                }
            }
            else if (target.Type == InteractableType.DoorButton)
            {
                if (AssetManager.IsTextureValid(_handIcon))
                {
                    Raylib.DrawTexture(_handIcon, centerX - 32, centerY - 32, Color.White);
                }
            }
        }
    }

    /// <summary>
    /// Main.bb lines 3268-3295: Message display with alpha fade
    /// </summary>
    private static void DrawMessage(PlayerState player, MenuState menu, int graphicWidth, int graphicHeight)
    {
        if (menu.MsgTimer <= 0 || string.IsNullOrEmpty(menu.Msg))
            return;

        // temp = False check for paper items
        bool isPaperView = false;
        if (menu.InvOpen == 0 && InventorySystem.SelectedItem != null)
        {
            string tempName = InventorySystem.SelectedItem.itemtemplate?.tempname ?? "";
            if (tempName == "paper" || tempName == "oldpaper")
            {
                isPaperView = true;
            }
        }

        // alpha = Min(MsgTimer / 2, 255) / 255.0
        float alpha = MathF.Min(menu.MsgTimer / 2.0f, 255.0f) / 255.0f;
        byte alphaByte = (byte)(alpha * 255);

        int x = graphicWidth / 2;
        int y;

        if (!isPaperView)
        {
            // Normal position: GraphicHeight / 2 + 200
            y = graphicHeight / 2 + 200;
        }
        else
        {
            // Paper view position: GraphicHeight * 0.94
            y = (int)(graphicHeight * 0.94f);
        }

        int fontSize = 20;
        int textWidth = Raylib.MeasureText(menu.Msg, fontSize);
        int textX = x - textWidth / 2;

        // Shadow: Color 0,0,0 / AAText((GraphicWidth / 2)+1, y + 1, ...)
        Raylib.DrawText(menu.Msg, textX + 1, y + 1, fontSize, new Color(0, 0, 0, alphaByte));

        // Text color: white or red for errors
        Color textColor;
        if (menu.Msg.Length >= 14 && menu.Msg.Substring(0, 14) == "Blitz3D Error!")
        {
            textColor = new Color(255, 0, 0, alphaByte);
        }
        else
        {
            textColor = new Color(255, 255, 255, alphaByte);
        }

        Raylib.DrawText(menu.Msg, textX, y, fontSize, textColor);
    }

    /// <summary>
    /// Main.bb lines 5245-5420: Full inventory rendering
    /// </summary>
    public static void DrawInventory(PlayerState player, MenuState menu, InputState input, int graphicWidth, int graphicHeight)
    {
        int width = 70;
        int height = 70;
        int spacing = 35;

        // x = GraphicWidth / 2 - (width * MaxItemAmount /2 + spacing * (MaxItemAmount / 2 - 1)) / 2
        int startX = graphicWidth / 2 - (width * InventorySystem.MaxItemAmount / 2 + spacing * (InventorySystem.MaxItemAmount / 2 - 1)) / 2;
        // y = GraphicHeight / 2 - height
        int startY = graphicHeight / 2 - height;

        int x = startX;
        int y = startY;

        Vector2 mousePos = Raylib.GetMousePosition();
        int scaledMouseX = (int)mousePos.X;
        int scaledMouseY = (int)mousePos.Y;

        _mouseSlot = 66;

        // For n% = 0 To MaxItemAmount - 1
        for (int n = 0; n < InventorySystem.MaxItemAmount; n++)
        {
            // isMouseOn check
            bool isMouseOn = false;
            if (scaledMouseX > x && scaledMouseX < x + width)
            {
                if (scaledMouseY > y && scaledMouseY < y + height)
                {
                    isMouseOn = true;
                }
            }

            Items? item = InventorySystem.GetItem(n);

            // Equipped item highlight: Color 200, 200, 200 / Rect(x - 3, y - 3, width + 6, height + 6)
            if (item != null)
            {
                string tempName = item.itemtemplate?.tempname ?? "";
                bool drawHighlight = false;

                switch (tempName)
                {
                    case "gasmask":
                        if (player.WearingGasMask == 1) drawHighlight = true;
                        break;
                    case "supergasmask":
                        if (player.WearingGasMask == 2) drawHighlight = true;
                        break;
                    case "gasmask3":
                        if (player.WearingGasMask == 3) drawHighlight = true;
                        break;
                    case "hazmatsuit":
                        if (player.WearingHazmat == 1) drawHighlight = true;
                        break;
                    case "hazmatsuit2":
                        if (player.WearingHazmat == 2) drawHighlight = true;
                        break;
                    case "hazmatsuit3":
                        if (player.WearingHazmat == 3) drawHighlight = true;
                        break;
                    case "vest":
                        if (player.WearingVest == 1) drawHighlight = true;
                        break;
                    case "finevest":
                        if (player.WearingVest == 2) drawHighlight = true;
                        break;
                    case "scp714":
                        if (player.Wearing714 == 1) drawHighlight = true;
                        break;
                    case "nvgoggles":
                        if (player.WearingNightVision == 1) drawHighlight = true;
                        break;
                    case "supernv":
                        if (player.WearingNightVision == 2) drawHighlight = true;
                        break;
                    case "finenvgoggles":
                        if (player.WearingNightVision == 3) drawHighlight = true;
                        break;
                }

                if (drawHighlight)
                {
                    Raylib.DrawRectangle(x - 3, y - 3, width + 6, height + 6, new Color(200, 200, 200, 255));
                }
            }

            // Mouse hover: Color 255, 0, 0 / Rect(x - 1, y - 1, width + 2, height + 2)
            if (isMouseOn)
            {
                _mouseSlot = n;
                Raylib.DrawRectangleLines(x - 1, y - 1, width + 2, height + 2, Color.Red);
            }

            // DrawFrame(x, y, width, height, (x Mod 64), (x Mod 64))
            DrawFrame(x, y, width, height, x % 64, x % 64);

            // Draw item icon: DrawImage(Inventory(n)\invimg, x + width / 2 - 32, y + height / 2 - 32)
            if (item != null)
            {
                if (InventorySystem.SelectedItem != item || isMouseOn)
                {
                    DrawItemIcon(item, x + width / 2 - 32, y + height / 2 - 32);
                }
            }

            // Item name tooltip when hovering
            if (item != null && InventorySystem.SelectedItem != item && isMouseOn && InventorySystem.SelectedItem == null)
            {
                // AAText(x + width / 2, y + height + spacing - 15, Inventory(n)\name, True)
                string itemName = item.name;
                int textWidth = Raylib.MeasureText(itemName, 16);
                int textX = x + width / 2 - textWidth / 2;
                int textY = y + height + spacing - 15;

                // Shadow
                Raylib.DrawText(itemName, textX + 1, textY + 1, 16, Color.Black);
                // Text
                Raylib.DrawText(itemName, textX, textY, 16, Color.White);
            }

            // x = x + width + spacing
            x = x + width + spacing;

            // If n = 4 Then y = y + height*2 / x = startX (row break)
            if (n == 4)
            {
                y = y + height * 2;
                x = startX;
            }
        }

        // Draw dragged item at mouse: If SelectedItem <> Null And MouseDown1
        if (InventorySystem.SelectedItem != null && Raylib.IsMouseButtonDown(MouseButton.Left))
        {
            if (_mouseSlot == 66 || InventorySystem.SelectedItem != InventorySystem.GetItem(_mouseSlot))
            {
                DrawItemIcon(InventorySystem.SelectedItem, scaledMouseX - 32, scaledMouseY - 32);
            }
        }

        // Draw cursor
        if (AssetManager.IsTextureValid(_cursorImg))
        {
            Raylib.DrawTexture(_cursorImg, scaledMouseX, scaledMouseY, Color.White);
        }
    }

    /// <summary>
    /// Menu.bb lines 1665-1670: DrawFrame with tiled textures
    /// DrawTiledImageRect(MenuWhite, xoffset, (y Mod 256), 512, 512, x, y, width, height)
    /// DrawTiledImageRect(MenuBlack, yoffset, (y Mod 256), 512, 512, x+3*MenuScale, y+3*MenuScale, width-6*MenuScale, height-6*MenuScale)
    /// </summary>
    private static void DrawFrame(int x, int y, int width, int height, int xoffset = 0, int yoffset = 0)
    {
        if (AssetManager.IsTextureValid(_menuWhite))
        {
            // Outer white frame - tiled
            DrawTiledTexture(_menuWhite, xoffset, y % 256, x, y, width, height);
        }
        else
        {
            Raylib.DrawRectangle(x, y, width, height, new Color(200, 200, 200, 255));
        }

        int innerOffset = (int)(3 * MenuScale);
        int innerWidth = width - (int)(6 * MenuScale);
        int innerHeight = height - (int)(6 * MenuScale);

        if (AssetManager.IsTextureValid(_menuBlack))
        {
            // Inner black fill - tiled
            DrawTiledTexture(_menuBlack, yoffset, y % 256, x + innerOffset, y + innerOffset, innerWidth, innerHeight);
        }
        else
        {
            Raylib.DrawRectangle(x + innerOffset, y + innerOffset, innerWidth, innerHeight, Color.Black);
        }
    }

    /// <summary>
    /// Blitz3D DrawTiledImageRect equivalent
    /// </summary>
    private static void DrawTiledTexture(Texture2D texture, int srcX, int srcY, int destX, int destY, int destWidth, int destHeight)
    {
        int texWidth = texture.Width;
        int texHeight = texture.Height;

        if (texWidth <= 0 || texHeight <= 0) return;

        // Tile the texture across the destination rectangle
        for (int ty = 0; ty < destHeight; ty += texHeight)
        {
            for (int tx = 0; tx < destWidth; tx += texWidth)
            {
                int drawWidth = Math.Min(texWidth, destWidth - tx);
                int drawHeight = Math.Min(texHeight, destHeight - ty);

                Rectangle srcRect = new Rectangle(
                    (srcX + tx) % texWidth,
                    (srcY + ty) % texHeight,
                    drawWidth,
                    drawHeight
                );

                Rectangle destRect = new Rectangle(
                    destX + tx,
                    destY + ty,
                    drawWidth,
                    drawHeight
                );

                Raylib.DrawTexturePro(texture, srcRect, destRect, Vector2.Zero, 0, Color.White);
            }
        }
    }

    /// <summary>
    /// Draw item inventory icon
    /// Main.bb: DrawImage(Inventory(n)\invimg, x + width / 2 - 32, y + height / 2 - 32)
    /// Items.bb: invimg is loaded from itemtemplate\invimgpath
    /// </summary>
    private static void DrawItemIcon(Items item, int x, int y)
    {
        // Icon size is 64x64 in original
        // invimg stored in item is the loaded texture from itemtemplate.invimgpath

        if (item.itemtemplate != null && !string.IsNullOrEmpty(item.itemtemplate.invimgpath))
        {
            Texture2D iconTex = AssetManager.LoadTexture(item.itemtemplate.invimgpath);

            if (AssetManager.IsTextureValid(iconTex))
            {
                Raylib.DrawTexture(iconTex, x, y, Color.White);
            }
            else
            {
                DrawItemIconFallback(item, x, y);
            }
        }
        else
        {
            DrawItemIconFallback(item, x, y);
        }
    }

    /// <summary>
    /// Fallback rendering when texture not available
    /// Draws gray rectangle with item name
    /// </summary>
    private static void DrawItemIconFallback(Items item, int x, int y)
    {
        Raylib.DrawRectangle(x, y, 64, 64, new Color(60, 60, 60, 255));
        Raylib.DrawRectangleLines(x, y, 64, 64, new Color(100, 100, 100, 255));

        string shortName = item.name;
        if (shortName.Length > 10)
        {
            shortName = shortName.Substring(0, 10);
        }
        int textW = Raylib.MeasureText(shortName, 10);
        Raylib.DrawText(shortName, x + 32 - textW / 2, y + 27, 10, Color.White);
    }

    /// <summary>
    /// Main.bb lines 4790-4810: Debug HUD
    /// </summary>
    private static void DrawDebugInfo(PlayerState player, int graphicWidth, int graphicHeight)
    {
        int x = 30;
        int y = 50;

        // AAText x - 50, 50, "Player Position: (" + f2s(EntityX, 3) + ...
        Raylib.DrawText($"Player Position: ({player.X:F3}, {player.Y:F3}, {player.Z:F3})",
            x, y, 16, Color.White);

        Raylib.DrawText($"Camera Rotation: (Pitch: {player.Pitch:F3}, Yaw: {player.Yaw:F3})",
            x, y + 20, 16, Color.White);

        if (player.PlayerRoom != null)
        {
            Raylib.DrawText($"Room: {player.PlayerRoom.RoomTemplate?.Name ?? "Unknown"}",
                x, y + 40, 16, Color.White);
        }

        Raylib.DrawText($"Stamina: {player.Stamina:F1} | Blink: {player.BlinkTimer:F1}/{player.BLINKFREQ:F1}",
            x, y + 60, 16, Color.White);
    }

    /// <summary>
    /// Main.bb line 3295: MsgTimer = MsgTimer - FPSfactor2
    /// </summary>
    public static void UpdateMessage(MenuState menu, float fpsFactor2)
    {
        if (menu.MsgTimer > 0)
        {
            menu.MsgTimer -= fpsFactor2;
        }
    }

    /// <summary>
    /// Set message with timer (70 ticks = 1 second at 70fps)
    /// Example: MsgTimer = 70 * 5 = 350 for 5 seconds
    /// </summary>
    public static void ShowMessage(MenuState menu, string msg, float duration = 350.0f)
    {
        menu.Msg = msg;
        menu.MsgTimer = duration;
    }

    /// <summary>
    /// Main.bb inventory input handling (lines 5337-5420)
    /// </summary>
    public static void HandleInventoryInput(PlayerState player, MenuState menu, InputState input)
    {
        if (menu.InvOpen == 0) return;

        int graphicWidth = Raylib.GetScreenWidth();
        int graphicHeight = Raylib.GetScreenHeight();

        int width = 70;
        int height = 70;
        int spacing = 35;

        int startX = graphicWidth / 2 - (width * InventorySystem.MaxItemAmount / 2 + spacing * (InventorySystem.MaxItemAmount / 2 - 1)) / 2;
        int startY = graphicHeight / 2 - height;

        Vector2 mousePos = Raylib.GetMousePosition();
        int scaledMouseX = (int)mousePos.X;
        int scaledMouseY = (int)mousePos.Y;

        int x = startX;
        int y = startY;
        int mouseSlot = 66;

        // Find hovered slot
        for (int n = 0; n < InventorySystem.MaxItemAmount; n++)
        {
            if (scaledMouseX > x && scaledMouseX < x + width)
            {
                if (scaledMouseY > y && scaledMouseY < y + height)
                {
                    mouseSlot = n;
                    break;
                }
            }

            x = x + width + spacing;
            if (n == 4)
            {
                y = y + height * 2;
                x = startX;
            }
        }

        // If MouseHit1 - select item
        if (input.MouseHit1 != 0)
        {
            input.MouseHit1 = 0;

            if (mouseSlot != 66)
            {
                Items? clickedItem = InventorySystem.GetItem(mouseSlot);

                if (clickedItem != null && InventorySystem.SelectedItem == null)
                {
                    InventorySystem.SelectedItem = clickedItem;

                    // Double click handling
                    if (input.DoubleClick != 0)
                    {
                        if (player.WearingHazmat > 0)
                        {
                            string tempName = InventorySystem.SelectedItem.itemtemplate?.tempname ?? "";
                            if (!tempName.Contains("hazmatsuit"))
                            {
                                ShowMessage(menu, "You cannot use any items while wearing a hazmat suit.", 70 * 5);
                                InventorySystem.SelectedItem = null;
                                return;
                            }
                        }
                        menu.InvOpen = 0;
                        input.DoubleClick = 0;
                    }
                }
                else if (InventorySystem.SelectedItem != null && clickedItem == null)
                {
                    // Move selected item to empty slot
                    MoveItemToSlot(InventorySystem.SelectedItem, mouseSlot);
                    InventorySystem.SelectedItem = null;
                }
            }
        }

        // Mouse released - drop item or place in slot
        if (input.MouseUp1 != 0 && InventorySystem.SelectedItem != null)
        {
            input.MouseUp1 = 0;

            if (mouseSlot == 66)
            {
                // Drop outside inventory
                string tempName = InventorySystem.SelectedItem.itemtemplate?.tempname ?? "";

                bool isWearable = tempName == "vest" || tempName == "finevest" ||
                                  tempName == "hazmatsuit" || tempName == "hazmatsuit2" || tempName == "hazmatsuit3";

                if (isWearable)
                {
                    ShowMessage(menu, "Double click on this item to take it off.", 70 * 5);
                }
                else
                {
                    Vector3 dropPos = new Vector3(player.X, player.Y, player.Z);
                    InventorySystem.DropItem(player, InventorySystem.SelectedItem, dropPos, player.Yaw);
                    InventorySystem.SelectedItem = null;
                    menu.InvOpen = 0;
                }
            }
            else
            {
                Items? targetItem = InventorySystem.GetItem(mouseSlot);
                if (targetItem == null)
                {
                    MoveItemToSlot(InventorySystem.SelectedItem, mouseSlot);
                }
                InventorySystem.SelectedItem = null;
            }
        }

        // Right click - close inventory
        if (Raylib.IsMouseButtonPressed(MouseButton.Right))
        {
            menu.InvOpen = 0;
            InventorySystem.SelectedItem = null;
        }
    }

    /// <summary>
    /// Move item from current slot to target slot
    /// Main.bb lines 5371-5374:
    /// For z% = 0 To MaxItemAmount - 1 / If Inventory(z) = SelectedItem Then Inventory(z) = Null
    /// Inventory(n) = SelectedItem
    /// </summary>
    private static void MoveItemToSlot(Items item, int targetSlot)
    {
        InventorySystem.MoveToSlot(item, targetSlot);
    }

    public static void Unload()
    {
        _initialized = false;
    }
}
