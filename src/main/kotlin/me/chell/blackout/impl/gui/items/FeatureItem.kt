package me.chell.blackout.impl.gui.items

import com.mojang.logging.LogUtils
import me.chell.blackout.api.feature.Feature
import me.chell.blackout.api.util.mc
import me.chell.blackout.api.value.Setting
import me.chell.blackout.impl.gui.Button
import me.chell.blackout.impl.gui.CategoryTab
import me.chell.blackout.impl.gui.GuiItem
import me.chell.blackout.impl.gui.buttons.BooleanButton
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents

@Suppress("unchecked_cast")
class FeatureItem(val feature: Feature, override var x: Int, override var y: Int, private val parent: CategoryTab): GuiItem() {

    override val width = 239// 300-50-1-5-5
    override val height = 32
    var fullHeight = height

    private val expandable = feature.settings.isNotEmpty()
    private var expanded = false
    private val expandedHeight: Int

    private val settings = mutableListOf<SettingItem>()

    override val button = when(feature.mainSetting.value) {
        is Boolean -> BooleanButton(this, feature.mainSetting as Setting<Boolean>, expandable)
        else -> {
            LogUtils.getLogger().warn("Cannot create button for feature ${feature.name}")
            object : Button(this, false) {
                override val x = 0
                override val y = 0
                override val width = 0
                override val height = 0
            }
        }
    }

    init {
        var sY = y + height + margin
        for(setting in feature.settings) {
            val i = SettingItem(setting, x + SettingItem.offset, sY)
            settings.add(i)
            sY += i.height + margin
        }
        expandedHeight = sY - y - margin
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float){
        super.render(matrices, mouseX, mouseY, delta)

        val center = y.toFloat() + (height /2) - (mc.textRenderer.fontHeight/2)
        mc.textRenderer.drawWithShadow(matrices, feature.name, x + margin.toFloat(), center, -1)

        if(expanded) {
            for(item in settings) {
                item.render(matrices, mouseX, mouseY, delta)
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(button == 1 && expandable && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if(expanded) {
                mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_OUT, 1.0f, 1.0f))
                expanded = false
                fullHeight = height
            } else {
                mc.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f))
                fullHeight = expandedHeight
                expanded = true
            }
            parent.updateItems()
            return true
        }

        if(expanded) {
            for(item in settings) {
                if(item.mouseClicked(mouseX, mouseY, button)) return true
            }
        }

        return this.button.mouseClicked(mouseX, mouseY, button)
    }

}