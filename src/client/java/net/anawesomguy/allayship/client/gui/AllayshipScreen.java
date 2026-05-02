package net.anawesomguy.allayship.client.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.blaze3d.platform.NativeImage;
import net.anawesomguy.allayship.MagicalAllayship;
import net.anawesomguy.allayship.entity.Fairy;
import net.anawesomguy.allayship.network.CallFairyPayload;
import net.anawesomguy.allayship.network.RequestTransformationPayload;
import net.anawesomguy.allayship.network.SetFairyNamePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class AllayshipScreen extends Screen {
    private static final Identifier GREEN_BOX = Identifier.fromNamespaceAndPath(MagicalAllayship.MOD_ID, "textures/gui/greenbox.png");
    private static final Identifier GREEN_DOT = Identifier.fromNamespaceAndPath(MagicalAllayship.MOD_ID, "textures/gui/greendot.png");
    private static final Identifier GREEN_SPOTS = Identifier.fromNamespaceAndPath(MagicalAllayship.MOD_ID, "textures/gui/greenspots.png");
    private final Button transformButton = new Button(1, 28, 29, 34, 32, Component.translatable("tooltip.magical-allayship.transform"));
    private final Button guideButton = new Button(2, 22, 27, 14, 19, Component.translatable("tooltip.magical-allayship.guide"));
    private final Button fairyButton = new Button(3, 28, 20, 33, 10, Component.translatable("tooltip.magical-allayship.fairy"));
    private final Button settingsButton = new Button(4, 22, 27, 59, 19, Component.translatable("tooltip.magical-allayship.settings"));
    private final Button[] buttons = {this.transformButton, this.guideButton, this.fairyButton, this.settingsButton};
    private final InteractionHand hand;
    private EditBox nameBox;
    private Button pressedButton;
    private int popupPage = -1;
    private String fairyName = "";

    public AllayshipScreen(InteractionHand hand) {
        super(Component.empty());
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();

        for (Button button : this.buttons) {
            button.load();
        }

        this.nameBox = this.addRenderableWidget(new EditBox(this.font, 0, 0, 72, 14, Component.translatable("gui.magical-allayship.name")));
        this.nameBox.setVisible(false);
        this.nameBox.setBordered(true);
        this.nameBox.setMaxLength(20); // if you change this make sure to also change in payload
        this.nameBox.setResponder(name -> this.saveName());
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        int posX = this.width / 2 - 48;
        int posY = this.height / 2 - 36;

        for (Button button : this.buttons) {
            button.x = posX + button.offsetX;
            button.y = posY + button.offsetY;
        }

        if (this.nameBox != null) {
            int x = this.settingsButton.x + this.settingsButton.width + 10;
            int y = this.settingsButton.y + this.settingsButton.height / 2 - 21;
            this.nameBox.setX(x + 23);
            this.nameBox.setY(y + 18);
        }
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {}

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        if (this.popupPage >= 0) {
            int x = this.settingsButton.x + this.settingsButton.width + 10;
            int y = this.settingsButton.y + this.settingsButton.height / 2 - 21;
            this.drawBox(graphics, x, y, 105, 42);
            if (this.popupPage == 0) {
                graphics.centeredText(this.font, Component.translatable("gui.magical-allayship.name"), x + 56, y + 8, ARGB.white(1.0F));
            } else {
                graphics.centeredText(this.font, Component.translatable("gui.magical-allayship.health"), x + 56, y + 10, ARGB.white(1.0F));
                graphics.centeredText(this.font, this.getHealthText(), x + 56, y + 22, ARGB.white(1.0F));
            }

            this.drawPages(graphics, x + 10, y + 14);
        }

        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

        for (Button button : this.buttons) {
            Identifier texture = button.texture;
            if (this.pressedButton == button && button.isOverMask(mouseX, mouseY)) {
                texture = button.pressedTexture;
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, button.x, button.y, 0.0F, 0.0F, button.width, button.height, button.width, button.height);
            if (button.isOverMask(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(this.font, button.tooltip, mouseX, mouseY);
            }
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
            if (this.pressedButton.isOverMask((int) event.x(), (int) event.y())) {
                if (this.pressedButton == this.transformButton) {
                    this.closePopup();
                    ClientPlayNetworking.send(new RequestTransformationPayload(this.hand == InteractionHand.MAIN_HAND));
                } else if (this.pressedButton == this.fairyButton) {
                    this.closePopup();
                    ClientPlayNetworking.send(new CallFairyPayload(this.hand == InteractionHand.MAIN_HAND));
                } else if (this.pressedButton == this.settingsButton) {
                    this.cyclePopup();
                } else if (this.pressedButton == this.guideButton) {
                    this.closePopup();
                }
            }

            this.pressedButton = null;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        this.closePopup();
        super.removed();

        for (Button button : this.buttons) {
            if (button.mask != null) button.mask.close();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void cyclePopup() {
        if (this.popupPage < 0) {
            this.popupPage = 0;
            this.fairyName = this.getFairyName();
            this.nameBox.setValue(this.fairyName);
        } else if (++this.popupPage >= 2) {
            this.closePopup();
            return;
        } else {
            this.saveName();
        }

        this.nameBox.setVisible(this.popupPage == 0);
        this.nameBox.setFocused(this.nameBox.isVisible());
    }

    private void closePopup() {
        this.saveName();
        this.popupPage = -1;
        this.nameBox.setVisible(false);
        this.nameBox.setFocused(false);
    }

    private Component getHealthText() {
        ItemStack held = this.minecraft.player == null ? ItemStack.EMPTY : this.minecraft.player.getItemInHand(this.hand);
        Either<UUID, CompoundTag> data = held.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (data == null) {
            return Component.empty();
        }

        if (data.left().isPresent()) {
            return Component.translatable("gui.magical-allayship.health_called");
        }

        float health = data.right().orElseGet(CompoundTag::new).getFloatOr("Health", 0.0F);
        if (health == Mth.floor(health)) {
            return Component.literal(Mth.floor(health) + "/10");
        }

        return Component.literal(String.format(Locale.ROOT, "%.1f/10", health));
    }

    private void drawBox(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        int corner = 8;
        int middle = 16;
        int middleWidth = width - corner * 2;
        int middleHeight = height - corner * 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x, y, 0.0F, 0.0F, corner, corner, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + corner, y, 8.0F, 0.0F, middleWidth, corner, middle, corner, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + width - corner, y, 24.0F, 0.0F, corner, corner, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x, y + corner, 0.0F, 8.0F, corner, middleHeight, corner, middle, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + corner, y + corner, 8.0F, 8.0F, middleWidth, middleHeight, middle, middle, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + width - corner, y + corner, 24.0F, 8.0F, corner, middleHeight, corner, middle, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x, y + height - corner, 0.0F, 24.0F, corner, corner, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + corner, y + height - corner, 8.0F, 24.0F, middleWidth, corner, middle, corner, 32, 32);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_BOX, x + width - corner, y + height - corner, 24.0F, 24.0F, corner, corner, 32, 32);
    }

    private void drawPages(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_SPOTS, x, y, 0.0F, 0.0F, 6, 14, 6, 14);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GREEN_DOT, x, y + 1 + this.popupPage * 6, 0.0F, 0.0F, 6, 6, 6, 6);
    }

    private String getFairyName() {
        ItemStack held = this.minecraft.player == null ? ItemStack.EMPTY : this.minecraft.player.getItemInHand(this.hand);
        Either<UUID, CompoundTag> data = held.get(MagicalAllayship.FAIRY_DATA_COMPONENT);
        if (data == null) {
            return "";
        }

        if (data.left().isPresent() &&
            this.minecraft.player != null &&
            this.minecraft.player.level().getEntity(data.left().get()) instanceof Fairy fairy &&
            fairy.hasCustomName()) {
            return fairy.getCustomName().getString();
        }

        CompoundTag tag = data.right().orElse(null);
        if (tag == null || !tag.contains("CustomName")) {
            return "";
        }

        return ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, tag.get("CustomName"))
                                            .result()
                                            .map(Component::getString)
                                            .orElse("");
    }

    private void saveName() {
        if (this.nameBox == null) {
            return;
        }

        String name = this.nameBox.getValue().trim();
        if (name.equals(this.fairyName)) {
            return;
        }

        ClientPlayNetworking.send(new SetFairyNamePayload(this.hand == InteractionHand.MAIN_HAND, name));
        this.fairyName = name;
    }

    private static Identifier textureId(int button, String suffix) {
        return Identifier.fromNamespaceAndPath(MagicalAllayship.MOD_ID, "textures/gui/button_" + button + suffix + ".png");
    }

    // inner class so stuff is more tidy :3, I think this is fine ownership-wise here as it'll only be used here
    private final class Button {
        private final Identifier texture;
        private final Identifier pressedTexture;
        private final Identifier regionTexture;
        private final Component tooltip;
        private final int width;
        private final int height;
        private final int offsetX;
        private final int offsetY;
        private NativeImage mask;
        private int color;
        private int x;
        private int y;

        private Button(int id, int width, int height, int offsetX, int offsetY, Component tooltip) {
            this.texture = textureId(id, "_0");
            this.pressedTexture = textureId(id, "_1");
            this.regionTexture = textureId(id, "_region");
            this.tooltip = tooltip;
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