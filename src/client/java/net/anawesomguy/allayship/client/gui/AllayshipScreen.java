package net.anawesomguy.allayship.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.anawesomguy.allayship.MagicalAllayship;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;

public class AllayshipScreen extends Screen {
    // tweak the positions here & in repositionElements, temp position for now
    private final Button transformButton = new Button(1, 28, 29, 34, 32);
    private final Button guideButton = new Button(2, 22, 27, 14, 19);
    private final Button fairyButton = new Button(3, 28, 20, 33, 10);
    private final Button settingsButton = new Button(4, 22, 27, 59, 19);
    private final Button[] buttons = {this.transformButton, this.guideButton, this.fairyButton, this.settingsButton};
    private Button pressedButton;

    public AllayshipScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();

        for (Button button : this.buttons) {
            button.load();
        }

        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        int posX = 0;
        int posY = this.height - 75;

        for (Button button : this.buttons) {
            button.x = posX + button.offsetX;
            button.y = posY + button.offsetY;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

        for (Button button : this.buttons) {
            Identifier texture = button.texture;
            if (this.pressedButton == button && button.isOverMask(mouseX, mouseY)) {
                texture = button.pressedTexture;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, button.x, button.y, 0.0F, 0.0F, button.width, button.height, button.width, button.height);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        for (Button button : this.buttons) {
            if (button.isOverMask((int) event.x(), (int) event.y())) {
                this.pressedButton = button;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.pressedButton != null) {
            this.pressedButton = null;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        super.removed();

        for (Button button : this.buttons) {
            if (button.mask != null) button.mask.close();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Identifier textureId(int button, String suffix) {
        return Identifier.fromNamespaceAndPath(MagicalAllayship.MOD_ID, "textures/gui/button_" + button + suffix + ".png");
    }

    // inner class so stuff is more tidy :3, I think this is fine ownership-wise here as it'll only be used here
    private final class Button {
        private final Identifier texture;
        private final Identifier pressedTexture;
        private final Identifier regionTexture;
        private final int width;
        private final int height;
        private final int offsetX;
        private final int offsetY;
        private NativeImage mask;
        private int color;
        private int x;
        private int y;

        private Button(int id, int width, int height, int offsetX, int offsetY) {
            this.texture = textureId(id, "_0");
            this.pressedTexture = textureId(id, "_1");
            this.regionTexture = textureId(id, "_region");
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        private void load() {
            if (this.mask != null) {
                return;
            }

            try {
                this.mask = NativeImage.read(minecraft.getResourceManager().open(this.regionTexture));

                for (int y = 0; y < this.mask.getHeight() && this.color == 0; y++) {
                    for (int x = 0; x < this.mask.getWidth(); x++) {
                        int pixel = this.mask.getPixel(x, y);
                        if (ARGB.alpha(pixel) > 0) {
                            this.color = pixel;
                            return;
                        }
                    }
                }
            } catch (IOException ignored) {}
        }

        private boolean isOverMask(int mouseX, int mouseY) {
            if (this.mask == null) return false;

            int x = mouseX - this.x;
            int y = mouseY - this.y;
            if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
                return false;
            }

            int pixel = this.mask.getPixel(x, y);
            return ARGB.alpha(pixel) > 0 && pixel == this.color;
        }
    }
}