package io.github.firstwemine1.cleave.config;

import io.github.firstwemine1.cleave.utils.PredicateUtils;
import net.minecraft.world.entity.player.Player;

import java.text.MessageFormat;
import java.util.function.Predicate;

public enum TriggerActions {
    SHIFT_HELD("crouch_button_held", "while holding the crouch button", Player::isShiftKeyDown),
    SHIFT_NOT_HELD("crouch_button_not_held", "while not holding the crouch button", SHIFT_HELD.triggerCondition.negate()),
    ALWAYS("always", PredicateUtils::always)
    ;

    private final Predicate<Player> triggerCondition;
    /**
     * What players actually see: the command literal and the value printed by /cleave config
     * triggerWhen. Deliberately separate from the constant name, because the config file is
     * written with name() -- so the TOML keeps SHIFT_NOT_HELD and stays readable by the
     * Forge build, while nothing shown in game says "shift".
     */
    private final String displayName;
    public final String description;

    TriggerActions(String displayName, Predicate<Player> triggerCondition) {
        this(displayName, "", triggerCondition);
    }

    TriggerActions(String displayName, String description, Predicate<Player> triggerCondition) {
        this.displayName = displayName;
        this.triggerCondition = triggerCondition;
        this.description = description;
    }

    public boolean shouldTrigger(Player player) {
        return triggerCondition.test(player);
    }

    public String formatConditionText(String baseMessage, String addedText) {
        if (description.isEmpty()) {
            return MessageFormat.format(baseMessage, "");
        }

        return MessageFormat.format(baseMessage, MessageFormat.format(addedText, description));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
