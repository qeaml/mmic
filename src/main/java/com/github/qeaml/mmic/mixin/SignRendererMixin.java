package com.github.qeaml.mmic.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.qeaml.mmic.Client;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer.SignModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.SignType;
import net.minecraft.util.math.RotationAxis;

@Mixin(SignBlockEntityRenderer.class)
public abstract class SignRendererMixin {
  @Shadow private Map<SignType, SignModel> typeToModel;
  @Shadow private TextRenderer textRenderer;

  @Invoker
  protected static int callGetColor(SignBlockEntity sign) {
    return 0;
  }

  @Invoker
  protected static boolean callShouldRender(SignBlockEntity sign, int n) {
    return false;
  }

  @Inject(
    method = "render(Lnet/minecraft/block/entity/SignBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
    at = @At("HEAD"),
    cancellable = true
  )
  private void renderHijack(SignBlockEntity blockEntity, float _f, MatrixStack matrices, VertexConsumerProvider vex, int light, int overlay, CallbackInfo ci) {
    // get & prep the sign model
    var blockState = blockEntity.getCachedState();
    var signType = AbstractSignBlock.getSignType(blockState.getBlock());
    var signModel = typeToModel.get(signType);

    matrices.push();
    matrices.translate(0.5d, 0.5d, 0.5d);

    // get the sign rotation
    float rotation;
    if(blockState.getBlock() instanceof SignBlock) {
      rotation = -(float)(blockState.get(SignBlock.ROTATION) * 360) / 16f;
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
      signModel.stick.visible = true;
    } else {
      rotation = -blockState.get(WallSignBlock.FACING).asRotation();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
      matrices.translate(0d, -5d/16d, Client.config.perfectSigns.get() ? -0.459d : -7d/16d);
      signModel.stick.visible = false;
    }

    // draw model
    matrices.push();
    matrices.scale(2f/3f, -2f/3f, -2f/3f);

    var spriteIdentifier = TexturedRenderLayers.getSignTextureId(signType);
    var vertices = spriteIdentifier.getVertexConsumer(vex, signModel::getLayer);
    signModel.root.render(matrices, vertices, light, overlay);
    
    matrices.pop();
    matrices.translate(0.0D, 0.3333333432674408D, 0.046666666865348816D);
    matrices.scale(0.010416667F, -0.010416667F, 0.010416667F);

    // figure out the text appearance
    boolean textOutline = false;
    int textOutlineColor = callGetColor(blockEntity);
    int textColor = textOutlineColor;
    int textLight = light;

    if(blockEntity.isGlowingText()) {
      textColor = blockEntity.getTextColor().getSignColor();
      textOutline = callShouldRender(blockEntity, textOutlineColor);
      textLight = 15728880;
    }

    // get the text
    var orderedTexts = blockEntity.updateSign(MinecraftClient.getInstance().shouldFilterText(), text -> {
      var list = textRenderer.wrapLines(text, 90);
      return list.isEmpty() ? OrderedText.EMPTY : list.get(0);
    });

    var lines = new OrderedText[4];
    int lineCount;
    if(Client.config.centeredSigns.get()) {
      lineCount = 0;
      for(var ln: orderedTexts) {
        if(ln != OrderedText.EMPTY) {
          lines[lineCount++] = ln;
        }
      }
    } else {
      lineCount = 4;
    }

    float baseY = -lineCount*5;

    // draw the text
    for(int lineNo = 0; lineNo < lineCount; ++lineNo) {
      OrderedText text;
      float y;
      if(Client.config.centeredSigns.get()) {
        text = lines[lineNo];
        y = baseY+lineNo*10;
      } else {
        text = orderedTexts[lineNo];
        y = lineNo*10-20;
      }
      float x = -textRenderer.getWidth(text) / 2;

      var matrix = matrices.peek().getPositionMatrix();
      if(textOutline) {
        textRenderer.drawWithOutline(text, x, y, textColor, textOutlineColor, matrix, vex, textLight);
      } else {
        textRenderer.draw(text, x, y, textColor, false, matrix, vex, false, 0, textLight);
      }
    }

    matrices.pop();

    ci.cancel();
  }
}
